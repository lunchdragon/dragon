package dragon.jtest;

import dragon.comm.Utils;
import dragon.utils.ConfigHelper;
import dragon.utils.DbHelper;
import dragon.model.food.*;
import dragon.service.EatBean;
import dragon.service.Eat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.sql.*;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by lin.cheng on 6/8/15.
 */
public class eatBeanTest {

    static Eat f = new EatBean();
    static Log logger = LogFactory.getLog(EatBean.class);

    @BeforeClass
    public static void init(){
        ConfigHelper.instance();
        for(int i = 0; i < 10; i++){
            Restaurant r = new Restaurant("test" + i, "", 2, 20, null, "");
            f.saveRestaurant(r, null);
        }
    }

    @AfterClass
    public static void cleanup(){
        //clean up test data
        DbHelper.runUpdate(null, "delete from dragon_vote where rec_id in (select id from dragon_record where res_id in (select id from dragon_restaurant where name like '%s'))", "test%");
        DbHelper.runUpdate(null, "delete from dragon_record where res_id in (select id from dragon_restaurant where name like '%s')", "test%");
        DbHelper.runUpdate(null, "delete from dragon_restaurant where name like '%s'", "test%");
        DbHelper.runUpdate(null, "delete from dragon_secret where name like '%s'", "test%");
    }

    @Test
    public void testPickNVote()  {

        System.out.println("testPickNVote...");

        for (int i = 0; i < 5; i++) {
//            Restaurant r = f.pickRestaurant(null);
            Restaurant r = f.pickRestaurant("name like 'test%'");

            Record rec = new Record();
            rec.setResid(r.getId());
            f.saveRecord(rec);

            assertNotNull(r);
        }

        Connection conn = null;
        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select id from dragon_record order by id desc limit 5");

            while (rs.next()) {
                Long id = rs.getLong(1);
                Vote v = new Vote();
                v.setEmail("a@b.com");
                v.setResult(getRandomVote());
                v.setRecId(id);
                f.vote(v);
            }

        } catch(Exception e){
            logger.error("", e);
        } finally {
            DbHelper.closeConn(conn);
        }

        Map<String, Stat> ret = f.stat(0);

        System.out.printf("%-35s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
        for (Stat s : ret.values()) {
            System.out.println(s.toPrintString());
        }
    }

    @Test
    public void testSaveRestaurant()  {
        for(int i = 0; i < 5; i++){
            Restaurant r = new Restaurant("testsave" + i, "", 2, 5, null, "");
            f.saveRestaurant(r, null);
        }
    }

    @Test
    public void testGetRestaurants()  {
        System.out.println("testGetRestaurants...");

        int cnt = f.getRestaurants(null).size();
        assertTrue(cnt >= 0);
    }

    @Test
    public void testStat()  {
        System.out.println("testStat...");

        Map<String, Stat> ret = f.stat(0);

        System.out.printf("%-35s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
        for (Stat s : ret.values()) {
            System.out.println(s.toPrintString());
        }
    }

    @Test
    public void testStat2()  {
        System.out.println("testStat2...");

        Map<String, Stat> ret = f.stat2(7);

        System.out.println("------------ Last Week ------------");
        System.out.printf("%-35s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
        for (Stat s : ret.values()) {
            System.out.println(s.toPrintString());
        }

        ret = f.stat2(30);

        System.out.println("------------ Last Month ------------");
        System.out.printf("%-35s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
        for (Stat s : ret.values()) {
            System.out.println(s.toPrintString());
        }
    }

    @Test
    public void testSubscribe()  {
        System.out.println("testSubscribe...");

        Boolean r = f.subscribe("b@c.com", false);
        assertTrue(r);
    }

    @Ignore
    @Test
    public void testImportRestaurants()  {
        System.out.println("testImportRestaurants...");

        try {
            String csv = Utils.readRawContentFromFile("D:\\dragon\\resource\\Milpitas.csv");
            int cnt = f.importRestaurants(csv);
            assertTrue(cnt >= 0);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Test
    public void testSecret(){
        System.out.println("testSecret...");

        String sec = "(jWohE68N";
        f.saveSecret("test", sec);
        String v = f.getSecret("test");

        assertEquals(sec, v);
    }

    @Ignore
    @Test
    public void testTrans() throws Exception{
        Connection conn = null;
        try {
            Long t1 = System.currentTimeMillis();

            conn = DbHelper.getConn();
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();
            PreparedStatement st2 = conn.prepareStatement("insert into dragon_vote (rec_id,vote,email) VALUES(?, ? , ?)");
            ResultSet rs = st.executeQuery("select id from dragon_record order by id desc limit 9");

            while (rs.next()) {
                Long id = rs.getLong(1);
                Vote v = new Vote();
                v.setEmail("a@b.com");
                v.setResult(getRandomVote());
                v.setRecId(id);
                System.out.println(v.getResult());

                st2.setLong(1, id);
                st2.setInt(2, v.getResult().ordinal());
                st2.setString(3, v.getEmail());

                st2.executeUpdate();

                if (v.getResult() == Vote.Result.killme){
                    throw new RuntimeException();//no record will be saved
                }
            }

            conn.commit();

            Long t2 = System.currentTimeMillis();
            System.out.println(t2 - t1);

        } finally {
            DbHelper.closeConn(conn);
        }
    }

    @Ignore
    @Test
    public void testBatch() throws Exception{
        Connection conn = null;
        try {
            Long t1 = System.currentTimeMillis();

            conn = DbHelper.getConn();
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();
            PreparedStatement st2 = conn.prepareStatement("insert into dragon_vote (rec_id,vote,email) VALUES(?, ? , ?)");
            ResultSet rs = st.executeQuery("select id from dragon_record order by id desc limit 9");

            while (rs.next()) {
                Long id = rs.getLong(1);
                Vote v = new Vote();
                v.setEmail("a@b.com");
                v.setResult(getRandomVote());
                v.setRecId(id);
                System.out.println(v.getResult());

                st2.setLong(1, id);
                st2.setInt(2, v.getResult().ordinal());
                st2.setString(3, v.getEmail());
                st2.addBatch();
            }

            st2.executeBatch();
            conn.commit();

            Long t2 = System.currentTimeMillis();
            System.out.println(t2 - t1);

        } finally {
            DbHelper.closeConn(conn);
        }
    }

    @Ignore
    @Test
    public void testThread() throws Exception{
        for(int i = 0; i < 50; i++){
            Thread t = new Thread() {
                public void run() {
                    testSaveRestaurant();
                }
            };
            t.start();
            t.join();
        }
    }

    private Vote.Result getRandomVote(){
        int r = Math.abs(new Random().nextInt() % 7);
        if(r == 0){
            return Vote.Result.killme;
        } else if(r < 3){
            return Vote.Result.dislike;
        } else {
            return Vote.Result.like;
        }
    }

}