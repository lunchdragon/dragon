package dragon.jtest;

import dragon.db.DbHelper;
import dragon.model.food.*;
import dragon.service.bar;
import dragon.service.barBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.sql.*;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by lin.cheng on 6/8/15.
 */
public class barBeanTest {

    static bar f = new barBean();
    static Log logger = LogFactory.getLog(barBean.class);

    @BeforeClass
    public static void init(){
        for(int i = 0; i < 10; i++){
            Restaurant r = new Restaurant("test" + i, "", 2, 5);
            f.saveRestaurant(r, null);
        }
    }

    @AfterClass
    public static void cleanup(){
        //clean up test data
        DbHelper.runUpdate(null, "delete from dragon_vote where rec_id in (select id from dragon_record where res_id in (select id from dragon_restaurant where name like '%s'))", "test%");
        DbHelper.runUpdate(null, "delete from dragon_record where res_id in (select id from dragon_restaurant where name like '%s')", "test%");
        DbHelper.runUpdate(null, "delete from dragon_restaurant where name like '%s'", "test%");
    }

    @Test
    public void testPickRestaurant()  {
        for (int i = 0; i < 30; i++) {
            Restaurant r = f.pickRestaurant();

            Record rec = new Record();
            rec.setResid(r.getId());
            f.saveRecord(rec);

            assertNotNull(r);
        }

        Map<String, Stat> ret = f.stat();

        System.out.printf("%-10s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "selected", "liked", "disliked", "vetoed");
        for (String name : ret.keySet()) {
            Stat s = ret.get(name);
            System.out.println(s.toPrintString());
        }

    }

    @Test
    public void testVote(){

        Connection conn = null;
        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select id from dragon_record order by id desc limit 30");

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

        Map<String, Stat> ret = f.stat();

        System.out.printf("%-10s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "selected", "liked", "disliked", "vetoed");
        for (String name : ret.keySet()) {
            Stat s = ret.get(name);
            System.out.println(s.toPrintString());
        }
    }

    @Test
    public void testSaveRestaurant()  {
        Restaurant r = new Restaurant("test15", "", 2, 5);
        f.saveRestaurant(r, null);
        assertNotNull(r);
    }

    @Test
    public void testGetRestaurants()  {
        int cnt = f.getRestaurants().size();
        assertTrue(cnt >= 0);
    }

    @Test
    public void testSendEmail()  {
    }

    @Test
    public void testSaveUser()  {
    }

    @Test
    public void testSubscribe()  {
        Boolean r = f.subscribe("b@c.com", false);
        assertTrue(r);
    }

    @Test
    public void testImportRestaurants()  {
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