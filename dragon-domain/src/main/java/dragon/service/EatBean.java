package dragon.service;

import dragon.utils.ConfigHelper;
import dragon.comm.Utils;
import dragon.comm.crypto.CryptoALG;
import dragon.comm.crypto.CryptoUtils;
import dragon.utils.DbHelper;
import dragon.utils.QueueHelper;
import dragon.model.food.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.SecretKey;
import javax.ejb.Stateless;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Created by lin.cheng on 6/1/15.
 */
@Stateless
public class EatBean implements Eat {

    static Log logger = LogFactory.getLog(EatBean.class);
    static final String KEY = "KEY";

    public int importRestaurants(String csv) {
        List<String[]> data = new ArrayList<String[]>();
        Utils.parseHeaderAndData(csv, data, true);

        Connection conn = null;

        try {
            conn = DbHelper.getConn();
            for (String[] item : data) {
                Restaurant r = new Restaurant(item[0], item[1], Integer.parseInt(item[2]), Integer.parseInt(item[3]));
                saveRestaurant(r, conn);
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            DbHelper.closeConn(conn);
        }

        return data.size();
    }

    //Thread safe
    public Long saveRestaurant(Restaurant r, Connection conn) {

        String key = r.getName();
        boolean reuse = conn != null;//Remember to close connection outside
        Long id = 0L;

        try {
            if (!reuse) {
                conn = DbHelper.getConn();
            }
            conn.setAutoCommit(false);
            Statement st1 = conn.createStatement();

            st1.execute("LOCK TABLE dragon_restaurant IN ACCESS EXCLUSIVE MODE");

            logger.debug("Locked: " + Thread.currentThread().getId());

            id = DbHelper.runWithSingleResult("select id from dragon_restaurant where name ='" + key + "'", conn);

            if (id != null) {
                DbHelper.runUpdate(conn, "update dragon_restaurant set link='%s', factor=%s,score=%s where name='%s'",
                        r.getLink(), r.getFactor(), r.getScore(), key);

            } else {
                id = getNextId(conn);

                logger.debug("Add: " + Thread.currentThread().getId());

                DbHelper.runUpdate(conn, "insert into dragon_restaurant (name,link,factor,score,id) VALUES('%s','%s',%s,%s,%s)",
                        key, r.getLink(), r.getFactor(), r.getScore(), id);
            }

            conn.commit();

            logger.debug("Commit and unlock: " + Thread.currentThread().getId());

        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (!reuse) {
                DbHelper.closeConn(conn);
            }
        }

        return id;
    }

    //Not thread safe
    public Long saveUser(User u) {

        String key = u.getEmail();
        int cnt = DbHelper.runUpdate(null, "update dragon_user set subscribed=%s, name='%s' where email='%s'",
                u.getSubscribed(), u.getName(), key);

        Long id = DbHelper.runWithSingleResult("select id from dragon_user where email ='" + key + "'", null);

        if (cnt > 0) {
            return id;
        }

        id = getNextId(null);
        DbHelper.runUpdate(null, "insert into dragon_user (id,email,subscribed,name) VALUES(%s,'%s',%s,'%s')",
                id, key, u.getSubscribed(), u.getName());

        return id;
    }

    //Not thread safe
    public Boolean subscribe(String email, boolean sub) {

        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return false;
        }

        logger.info(email + (sub ? " subing..." : " unsub..."));

        int cnt = DbHelper.runUpdate(null, "update dragon_user set subscribed = %s where email = '%s'", sub, email);

        if (cnt > 0) {
            return true;
        }

        cnt = DbHelper.runUpdate(null, "insert into dragon_user (id,email,subscribed,name) VALUES(%s,'%s',%s,'%s')",
                getNextId(null), email, sub, email.split("@")[0]);

        return cnt > 0;
    }

    //Not thread safe
    public Boolean vote(Vote v) {

        Long recId = v.getRecId();
        Record rec = getRecord(recId);
        if (rec == null) {
            logger.error("Record not found: " + recId);
            return false;
        }

        Long resId = rec.getResid();
        Restaurant res = getRestaurant(resId);
        if (res == null) {
            logger.error("Restaurant not found: " + resId);
            return false;
        }

        if (saveVote(v, null)) {
            //change weight
            int score = v.getResult().getScore();
            res.setScore(res.getScore() + score);
            saveRestaurant(res, null);
        }

        if (v.getResult() == Vote.Result.killme) {

            if (!rec.getVeto() && System.currentTimeMillis() - rec.getGoTime() < 1000 * 60 * 60) {//within 1 hour
                rec.setVeto(true);
                try {
                    saveRecord(rec);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return false;
                }
                Long t4 = System.currentTimeMillis();

                //re-pickup
                sendLunchEmail("重新选一家，因为" + v.getEmail().split("@")[0] + "表示打死都不去。");

                Long t5 = System.currentTimeMillis();
                logger.info("Email takes: " + (t5 - t4));
            } else {
                logger.info("Already vetoed or time passed.");
                return false;
            }
        }

        return true;
    }

    public List<Restaurant> getRestaurants() {
        Connection conn = null;
        List<Restaurant> list = new ArrayList<Restaurant>();
        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from dragon_restaurant");

            while (rs.next()) {
                list.add(new Restaurant(rs.getString("name"), rs.getString("link"), rs.getInt("factor"), rs.getInt("score"), rs.getLong("id")));
            }
        } catch (Exception e) {
            logger.error("");
        } finally {
            DbHelper.closeConn(conn);
        }
        return list;
    }

    public Restaurant pickRestaurant() {

        List<Restaurant> list = getRestaurants();
        if (list == null || list.size() == 0) {
            return null;
        }

        long totalWeight = 0;
        Long preId = DbHelper.runWithSingleResult("select res_id from dragon_record order by id desc limit 1", null);

        for (Restaurant r : list) {
            if (preId != null && preId.equals(r.getId()) && list.size() > 1) {
                continue;
            }
            totalWeight += r.getWeight();
        }

        long rdm = Math.abs(new Random().nextLong());
        long selected = rdm % totalWeight;
        long pos = 0;
        for (Restaurant r : list) {

            if (preId != null && preId.equals(r.getId()) && list.size() > 1) {
                continue;
            }

            if (pos <= selected && selected < pos + r.getWeight()) {
                return r;
            }
            pos += r.getWeight();
        }

        return null;
    }

    public void sendLunchEmail(String reason) {

        String mails = getMails();

        if (StringUtils.isNotEmpty(mails)) {
            Restaurant r = pickRestaurant();
            if (r == null) return;

            Record rec = new Record();
            rec.setResid(r.getId());
            Long id = saveRecord(rec);

            String[] mailArr = mails.split(",");
            QueueHelper qh = new QueueHelper();

            try {
                qh.createDeliveryConnection(100);
                qh.initializeMessage();
                qh.initializeQueue("jms/EmailQueue");
                qh.addParameter("title", r.getName());

                for (String mail : mailArr) {
                    qh.addParameter("to", mail);
                    qh.addParameter("body", buildBody(mail, r, reason, id));
                    qh.sendMsg();
                }
            } catch (Exception e) {
                logger.error("", e);
            } finally {
                if (qh != null) {
                    try {
                        qh.close();
                    } catch (Exception ex) {
                        logger.error("", ex);
                    }
                }
            }
        }
    }

    public Map<String, Stat> stat() {
        Connection conn = null;
        Map<String, Stat> ret = new HashMap<String, Stat>();

        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "select res.name,res.factor,res.score,v.vote,count(*) from dragon_restaurant res " +
                            "inner join dragon_record r on r.res_id=res.id left join dragon_vote v on v.rec_id=r.id " +
                            "group by res.name,res.factor,res.score,v.vote");
            while (rs.next()) {
                String name = rs.getString(1);
                int factor = rs.getInt(2);
                int score = rs.getInt(3);
                Object vote = rs.getObject(4);
                int cnt = rs.getInt(5);

                Vote.Result vr = vote == null ? null : Vote.Result.values()[(Integer) vote];
                Stat s = null;
                if (!ret.containsKey(name)) {
                    s = new Stat(name, factor, score);
                    ret.put(name, s);
                } else {
                    s = ret.get(name);
                }

                s.setSelected(s.getSelected() + cnt);
                if (vr == Vote.Result.dislike) {
                    s.setDisliked(s.getDisliked() + cnt);
                }
                if (vr == Vote.Result.like) {
                    s.setLiked(s.getLiked() + cnt);
                }
                if (vr == Vote.Result.killme) {
                    s.setVetoed(s.getVetoed() + cnt);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            DbHelper.closeConn(conn);
        }

        return ret;
    }

    private String buildBody(String mail, Restaurant r, String reason, Long id) {
        String server = ConfigHelper.instance().getConfig("server");
        String port = ConfigHelper.instance().getConfig("port");

        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(reason)) {
            sb.append(reason).append("<br><br>");
        }

        sb.append(r.getLink()).append("<br><br>");

        Map<String, Stat> ss = stat();
        Stat s = ss.get(r.getName());
        if (s != null) {
            sb.append(s.toString()).append("<br><br>");
        }

        String url = "http://" + server + ":" + port + "/dragon/rest/eat/";

        sb.append("<a href=\'").append(url).append("vote?mail=").append(mail).append("&id=").append(id).append("&vote=0").append("\'/>").append("打死都不去</a>").append("<br>");
        sb.append("<a href=\'").append(url).append("vote?mail=").append(mail).append("&id=").append(id).append("&vote=2").append("\'/>").append("Like</a>").append("<br>");
        sb.append("<a href=\'").append(url + "vote?mail=" + mail + "&id=" + id + "&vote=1").append("\'/>").append("Dislike</a>").append("<br>");
        sb.append("<a href=\'").append(url + "unsub?mail=" + mail).append("\'/>").append("Unsubscribe</a>").append("<br>");

        return sb.toString();
    }

    public Long saveRecord(Record r) {

        if (r.getId() == null || r.getId() <= 0) {
            Long id = getNextId(null);
            DbHelper.runUpdate(null, "insert into dragon_record (id,res_id,go_time) VALUES(%s,'%s',%s)",
                    id, r.getResid(), System.currentTimeMillis());
            return id;
        } else {
            DbHelper.runUpdate(null, "update dragon_record set veto=%s where id=%s", r.getVeto(), r.getId());
            return r.getId();
        }
    }

    @Override
    public String saveSecret(String key, String value) {
        String name = DbHelper.runWithSingleResult("select name from dragon_secret where name ='" + key + "'", null);
        String custKey = getOrCreateKey();
        String enValue = null;
        try {
            enValue = CryptoUtils.encryptPwd(value, custKey);
        } catch (Exception e) {
            logger.error("", e);
            enValue = value;
        }

        if(name == null) {
            DbHelper.runUpdate(null, "insert into dragon_secret (name,value) VALUES('%s','%s')", key, enValue);
        } else {
            DbHelper.runUpdate(null, "update dragon_secret set value='%s' where name='%s'", enValue, key);
        }

        return enValue;
    }

    @Override
    public String getSecret(String key) {
        String enValue = DbHelper.runWithSingleResult("select value from dragon_secret where name ='" + key + "'", null);

        if(enValue == null){
            return null;
        }

        String custKey = getOrCreateKey();
        String value = null;
        try {
            value = CryptoUtils.decryptPwd(enValue,custKey);
        } catch (Exception e) {
            logger.error("", e);
            value = enValue;
        }

        return value;
    }

    private String getOrCreateKey(){

        String key = DbHelper.runWithSingleResult("select value from dragon_secret where name ='" + KEY + "'", null);

        if(key != null){
            return key;
        }

        SecretKey secretKey = null;
        try {
            secretKey = CryptoUtils.generateKey(CryptoALG.AES.getSpecName());
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
        }
        String strKey = CryptoUtils.secretKeyToString(secretKey);
        DbHelper.runUpdate(null, "insert into dragon_secret (name,value) VALUES('%s','%s')", KEY, strKey);

        return strKey;
    }

    private Record getRecord(Long recId) {
        Connection conn = null;
        Record rec = null;

        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from dragon_record where id = " + recId);

            if (rs.next()) {
                rec = new Record();
                rec.setId(recId);
                rec.setVeto(rs.getBoolean("veto"));
                rec.setResid(rs.getLong("res_id"));
                rec.setGoTime(rs.getLong("go_time"));
            }

            return rec;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            DbHelper.closeConn(conn);
        }
    }

    private Restaurant getRestaurant(Long id) {
        Connection conn = null;
        Restaurant ret = null;

        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from dragon_restaurant where id = " + id);

            if (rs.next()) {
                ret = new Restaurant(rs.getString("name"), rs.getString("link"), rs.getInt("factor"), rs.getInt("score"), rs.getLong("id"));
            }

            return ret;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            DbHelper.closeConn(conn);
        }
    }

    private User getUser(Long uid) {
        Connection conn = null;
        User rec = null;

        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from dragon_user where id = " + uid);

            if (rs.next()) {
                rec = new User();
                rec.setId(uid);
                rec.setEmail(rs.getString("email"));
                rec.setSubscribed(rs.getBoolean("subscribed"));
                rec.setName(rs.getString("name"));
            }

            return rec;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            DbHelper.closeConn(conn);
        }
    }

    private Boolean saveVote(Vote r, Connection conn) {

        String mail = r.getEmail();
        Long rid = r.getRecId();
        int res = r.getResult().ordinal();

        boolean reuse = conn != null;//Remember to close connection outside

        try {
            if (!reuse) {
                conn = DbHelper.getConn();
            }

            Object obj = DbHelper.runWithSingleResult("select vote from dragon_vote where email ='" + mail + "' and rec_id =" + rid, conn);
            if (obj != null) {
                if (res != (Integer) obj) {
                    DbHelper.runUpdate(conn, "update dragon_vote set vote=%s where email='%s' and rec_id=%s", res, mail, rid);
                } else {
                    return false;
                }
            } else {
                DbHelper.runUpdate(conn, "insert into dragon_vote (rec_id,vote,email) VALUES(%s,'%s','%s')", rid, res, mail);
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (!reuse) {
                DbHelper.closeConn(conn);
            }
        }

        return true;
    }

    private String getMails() {
        Connection conn = null;
        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select email from dragon_user where subscribed=true");

            List<String> list = new ArrayList<String>();
            while (rs.next()) {
                list.add(rs.getString("email"));
            }

            return StringUtils.join(list, ",");
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            DbHelper.closeConn(conn);
        }

        return "";
    }

    private Long getNextId(Connection connection) {
        Long id = DbHelper.runWithSingleResult("select nextval ('dragon_id_sec')", connection);
        return id;
    }
}