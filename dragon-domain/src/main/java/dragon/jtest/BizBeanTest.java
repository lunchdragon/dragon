package dragon.jtest;

import dragon.comm.Utils;
import dragon.model.job.Schedule;
import dragon.service.GroupBean;
import dragon.service.GroupIntf;
import dragon.utils.ConfigHelper;
import dragon.utils.DbHelper;
import dragon.model.food.*;
import dragon.service.BizBean;
import dragon.service.BizIntf;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by lin.cheng on 6/8/15.
 */
public class BizBeanTest {

    static BizIntf eb = new BizBean();
    static GroupIntf gb = new GroupBean();
    static Log logger = LogFactory.getLog(BizBeanTest.class);

    static final String TESTID = "unITtEsT";

    @BeforeClass
    public static void init() {
        ConfigHelper.instance();
        logger.info("Adding test groups...");
        for(int i = 0; i < 5; i++){
            int location = 95050 + i;
            Group g = new Group(TESTID + "_group" + i, "location=" + location +";category=chinese,japanese,taiwanese,korean;exclude=indian,thai;prefer=chinese,jang;distance=2800;reviews=90");
            g = gb.saveGroup(g);
            gb.applyPreference(g);
        }
        logger.info("Adding test users...");
        for(int i = 0; i < 50; i++){
            User u = new User();
            u.setName(TESTID + "_user" + i);
            u.setEmail(u.getName() + "@aaa.com");
            gb.saveUser(u);

            int gIndex = i%5;
            gb.saveUserToGroup(u.getEmail(), TESTID + "_group" + gIndex, true);
        }
    }

    @AfterClass
    public static void cleanup() {
        //clean up test data
        int cnt = DbHelper.runUpdate2(null, "delete from dragon_group_user where g_id in (select id from dragon_group where name like ?)", TESTID + "%");
        logger.info(cnt + " test gus removed.");
        cnt = DbHelper.runUpdate2(null, "delete from dragon_group_user where u_id in (select id from dragon_user where name like ?)", TESTID + "%");
        logger.info(cnt + " test gus removed.");
        cnt = DbHelper.runUpdate2(null, "delete from dragon_group_rest where g_id in (select id from dragon_group where name like ?)", TESTID + "%");
        logger.info(cnt + " test grs removed.");
        cnt = DbHelper.runUpdate2(null, "delete from dragon_group_rest where res_id in (select id from dragon_restaurant where name like ?)", TESTID + "%");
        logger.info(cnt + " test grs removed.");
        cnt = DbHelper.runUpdate2(null, "delete from dragon_vote where rec_id in (select id from dragon_record where g_id in (select id from dragon_group where name like ?))", TESTID + "%");
        logger.info(cnt + " test votes removed.");
        cnt = DbHelper.runUpdate2(null, "delete from dragon_record where g_id in (select id from dragon_group where name like ?)", TESTID + "%");
        logger.info(cnt + " test records removed.");
        cnt = DbHelper.runUpdate2(null, "delete from dragon_restaurant where name like ?", TESTID + "%");
        logger.info(cnt + " test restaurants removed.");
        cnt =  DbHelper.runUpdate2(null, "delete from dragon_secret where name like ?", TESTID + "%");
        logger.info(cnt + " test secrets removed.");
        cnt =  DbHelper.runUpdate2(null, "delete from dragon_user where name like ?", TESTID + "%");
        logger.info(cnt + " test users removed.");
        cnt =  DbHelper.runUpdate2(null, "delete from dragon_group where name like ?", TESTID + "%");
        logger.info(cnt + " test groups removed.");
    }

    @Test
    public void testPickNVote() {

        System.out.println("testPickNVote...");
        List<Long> testGids = DbHelper.getFirstColumnList(null, "select id from dragon_group where name like ?", TESTID + "%");

        Connection conn = null;
        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            for (int i = 0; i < 66; i++) {
                Long gid = testGids.get(i % testGids.size());

                Restaurant r = eb.pickRestaurant(gid);

                Record rec = new Record();
                rec.setResid(r.getId());
                rec.setgId(gid);
                eb.saveRecord(rec);

                assertNotNull(r);

                ResultSet rs = st.executeQuery("select id from dragon_record where g_id =" + gid + " order by id desc limit 1");

                while (rs.next()) {
                    Long id = rs.getLong(1);
                    Vote v = new Vote();
                    v.setEmail("a@b.com");
                    v.setResult(getRandomVote());
                    v.setRecId(id);
                    eb.vote(v, false);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            DbHelper.closeConn(conn);
        }

        for (Long gid : testGids) {
            System.out.println("Group: " + gid);
            Map<String, Stat> ret = eb.stat(gid, 0, true);
            System.out.printf("%-45s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
            for (Stat s : ret.values()) {
                System.out.println(s.toPrintString());
            }
        }

        eb.printPerfData();
    }

    @Ignore
    @Test
    public void testSaveGroup() {
        Group g = new Group("AOGroup2", "location=95035;category=chinese,japanese,taiwanese,korean;exclude=indian,thai;distance=4200;reviews=90");
        gb.saveGroup(g);
    }

    @Ignore
    @Test
    public void testSaveRest2Group() {
        List<Restaurant> rs = eb.getRestaurants("name like '%k%'");
        for (Restaurant r : rs) {
            gb.saveRestaurantByName(r.getId(), "AOGroup2", r.getFactor());
        }
    }

    @Ignore
    @Test
    public void testSaveUser2Group() {
        List<String> mails = DbHelper.getFirstColumnList(null, "select distinct email from dragon_user");
        for (String mail : mails) {
            gb.saveUserToGroup(mail, "AOGroup1", true);
        }
    }

    @Test
    public void testSaveRestaurant() {
        for (int i = 0; i < 5; i++) {
            Restaurant r = new Restaurant(TESTID + "_res" + i, "", 2L, null, "");
            eb.saveRestaurant(r, null);
        }
    }

    @Test
    public void testGetRestaurants() {
        System.out.println("testGetRestaurants...");

        int cnt = eb.getRestaurants("").size();
        assertTrue(cnt >= 0);
    }

    @Test
    public void testGetRestaurantsByGroup() {
        System.out.println("testGetRestaurantsByGroup...");

        int cnt = eb.getRestaurants(29229L).size();
        assertTrue(cnt >= 0);
    }

    @Test
    public void testStat() {
        System.out.println("testStat...");

        Map<String, Stat> ret = eb.stat(29229L, 0, true);

        System.out.printf("%-45s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
        for (Stat s : ret.values()) {
            System.out.println(s.toPrintString());
        }
    }

    @Test
    public void testStat2() {
        System.out.println("testStat2...");

        Map<String, Stat> ret = eb.stat2(29229L, 7);

        System.out.println("------------ Last Week ------------");
        System.out.printf("%-45s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
        for (Stat s : ret.values()) {
            System.out.println(s.toPrintString());
        }

        ret = eb.stat2(29229L, 30);

        System.out.println("------------ Last Month ------------");
        System.out.printf("%-45s%-10s%-10s%-10s%-10s%-10s%-10s\n", "name", "factor", "score", "visited", "liked", "disliked", "vetoed");
        for (Stat s : ret.values()) {
            System.out.println(s.toPrintString());
        }
    }

    @Test
    public void testSubscribe() {
        System.out.println("testSubscribe...");
        Long gid = DbHelper.runWithSingleResult2(null, "select id from dragon_group where name like ? limit 1", TESTID + "%");
        String mail = DbHelper.runWithSingleResult2(null, "select email from dragon_user where name like ? limit 1", TESTID + "%");

        gb.subscribe(mail, gid, true);
        Boolean r = gb.subscribe(mail, gid, false);
        assertTrue(r);
    }

    @Ignore
    @Test
    public void testImportRestaurants() {
        System.out.println("testImportRestaurants...");

        try {
            String csv = Utils.readRawContentFromFile("D:\\dragon\\resource\\Milpitas.csv");
            int cnt = eb.importRestaurants(csv);
            assertTrue(cnt >= 0);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Ignore
    @Test
    public void testSaveSch() {
        System.out.println("testSaveSchedule...");

        try {
            Schedule s = new Schedule("Pick", 32497L, "0 30 11 ? * MON-FRI *");
            Schedule ret = eb.saveSchedule(s);
            assertTrue(StringUtils.equals(s.getCron(), ret.getCron()));

            s = new Schedule("Summary", 32497L, "0 30 11 ? * SAT *");
            ret = eb.saveSchedule(s);
            assertTrue(StringUtils.equals(s.getCron(), ret.getCron()));

//            s = new Schedule("Pick", 29229L, "0 50 11 ? * MON-FRI *");
//            ret = eb.saveSchedule(s);
//            assertTrue(StringUtils.equals(s.getCron(), ret.getCron()));
//
//            s = new Schedule("Summary", 29229L, "0 50 11 ? * SAT *");
//            ret = eb.saveSchedule(s);
//            assertTrue(StringUtils.equals(s.getCron(), ret.getCron()));

        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Test
    public void testGetSch() {
        System.out.println("testGetSchedules...");

        try {
            List<Schedule> ret = eb.getSchedules("active = true");
            assertTrue(ret.size() >= 0);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Test
    public void testSecret() {
        System.out.println("testSecret...");

        String sec = "(jWohE68N";
        eb.saveSecret(TESTID, sec);
        String v = eb.getSecret(TESTID );

        assertEquals(sec, v);
    }

    @Ignore
    @Test
    public void testTrans() throws Exception {
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

                if (v.getResult() == Vote.Result.killme) {
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
    public void testBatch() throws Exception {
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
    public void testThread() throws Exception {
        for (int i = 0; i < 50; i++) {
            Thread t = new Thread() {
                public void run() {
                    testSaveRestaurant();
                }
            };
            t.start();
            t.join();
        }
    }

    @Test
    public void whatever(){
        String src = "aaa,";
        src.substring(0, src.length() - 1);
        System.out.println(src.substring(0, src.length()-1));
    }

    private Vote.Result getRandomVote() {
        int r = Math.abs(new Random().nextInt() % 7);
        if (r == 0) {
            return Vote.Result.killme;
        } else if (r < 3) {
            return Vote.Result.dislike;
        } else {
            return Vote.Result.like;
        }
    }

}