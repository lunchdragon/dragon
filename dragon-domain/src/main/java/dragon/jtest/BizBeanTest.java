package dragon.jtest;

import dragon.comm.Utils;
import dragon.model.food.*;
import dragon.model.job.Schedule;
import dragon.service.BizBean;
import dragon.service.BizIntf;
import dragon.service.GroupBean;
import dragon.service.GroupIntf;
import dragon.service.sec.SecureContexts;
import dragon.utils.ConfigHelper;
import dragon.utils.DbHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
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
        for(int i = 0; i < 2; i++){
            int location = 95050 + i;
            Group g = new Group(TESTID + "_group" + i, "location=" + location +";category=chinese,japanese,taiwanese,korean;exclude=indian,thai;prefer=chinese,jang;distance=2800;reviews=90");
            g = gb.saveGroup(g);
            gb.applyPreference(g);
        }
        logger.info("Adding test users...");
        for(int i = 0; i < 10; i++){
            User u = new User();
            u.setName(TESTID + "_user" + i);
            u.setEmail(u.getName() + "@aaa.com");
            gb.saveUser(u);

            int gIndex = i%2;
            gb.saveUserToGroup(u.getEmail(), TESTID + "_group" + gIndex, false);
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
            for (int i = 0; i < 33; i++) {
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
                    Vote.Result vr = getRandomVote();

                    Vote v = new Vote();
                    v.setEmail("unITtEsT_user3@aaa.com");
                    v.setResult(vr);
                    v.setRecId(id);
                    v.setIp("127.0.0.1");
                    String cr = eb.vote(v, false, false);
                    logger.info(cr);

                    Vote v2 = new Vote();
                    v2.setEmail("unITtEsT_user5@aaa.com");
                    v2.setResult(vr);
                    v2.setRecId(id);
                    v2.setIp("127.0.0.1");
                    cr = eb.vote(v2, false, true);
                    logger.info(cr);
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

//    @Ignore
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
        String src = "/phoenix/html/index.html#/incidents/123";
        String[] arr = src.split("#/");

        System.out.println(arr.length);
        System.out.println(arr[1]);
        System.out.println( 1/Math.pow(0, 1));
    }

    @Test
    public void bSort(){
        int [] iarr = {2,5,6,9,0,87,34,21,78,54,21,98,5,6};
        int len = iarr.length;
        int st = 0;
        int lt = 0;

        for(int i=len-1; i>0; i--){
            for(int j=0; j<i; j++){
                lt ++;
                if(iarr[j] > iarr[j+1]){
                    st ++;
                    swap(iarr, j, j+1);
                }
            }
        }

        System.out.println(Arrays.toString(iarr));
        System.out.println("Sawpd: " + st);
        System.out.println("Looped: " + lt);
    }

    @Test
    public void sSort(){
        int [] iarr = {2,5,6,9,0,87,34,21,78,54,21,98,5,6};
        int len = iarr.length;
        int st = 0;
        int lt = 0;

        for(int i=0; i<len-1; i++){
            int min = i;
            for(int j=i; j<=len-1; j++){
                lt ++;
                if(iarr[j] < iarr[min]){
                    min = j;
                }
            }
            if(min != i){
                st ++;
                swap(iarr, min, i);
            }
        }

        System.out.println(Arrays.toString(iarr));
        System.out.println("Sawpd: " + st);
        System.out.println("Looped: " + lt);
    }

    @Test
    public void mSort(){
        int [] iarr = {2,5,6,9,0,87,34,21,78,54,21,98,5,6};
        int len = iarr.length;

        recMsort(iarr, 0, len-1);
        System.out.println(Arrays.toString(iarr));
    }

    @Test
    public void qSort(){
        int [] iarr = {2,5,6,9,0,87,34,21,78,54,21,98,5,6};
        int len = iarr.length;

        partition(iarr, 0, len-1);
        System.out.println(Arrays.toString(iarr));
    }

    private void partition(int[] arr, int i1, int i2){
        if(i1 >= i2)return;
        int pivot = arr[i1];
        int tmpArr[] = new int[i2-i1+1];

        int p1 = 0;
        int p2 = i2-i1;
        for(int i=i1+1; i<=i2; i++){
            if(arr[i] < pivot){
                tmpArr[p1++] = arr[i];
            } else{
                tmpArr[p2--] = arr[i];
            }
        }
        tmpArr[p1] = pivot;
        for(int i=0; i<=i2-i1; i++) {
            arr[i1+i] = tmpArr[i];
        }

        partition(arr, i1, i1+p1-1);
        partition(arr, i1+p1+1, i2);
    }

    private void recMsort(int[] arr, int i1, int i2){
        if(i1==i2)return;
        int mid = (i2-i1)/2 + i1;
        recMsort(arr, i1, mid);
        recMsort(arr, mid+1, i2);
        merge(arr, i1, mid, i2);
    }

    private void merge(int[] arr, int i1, int mid, int i2){

        int tmpArr[] = new int[i2-i1+1];
        int p = 0;
        int p1 = i1;
        int p2 = mid+1;

        while (p1 <= mid && p2 <= i2){
            if(arr[p1] < arr[p2]){
                tmpArr[p++] = arr[p1++];
            } else {
                tmpArr[p++] = arr[p2++];
            }
        }
        if (p1 > mid) {
            for (int i = p2; i <= i2; i++) {
                tmpArr[p++] = arr[i];
            }
        } else {
            for (int i = p1; i <= mid; i++) {
                tmpArr[p++] = arr[i];
            }
        }

        for(int i=0; i<=i2-i1; i++) {
            arr[i1+i] = tmpArr[i];
        }

    }

    private void swap(int[] arr, int i1, int i2){
        int t = arr[i1];
        arr[i1] = arr[i2];
        arr[i2] = t;
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