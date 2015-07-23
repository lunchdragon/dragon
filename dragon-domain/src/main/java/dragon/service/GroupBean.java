package dragon.service;

import dragon.model.food.Group;
import dragon.model.food.Restaurant;
import dragon.model.food.User;
import dragon.service.ds.DsRetriever;
import dragon.service.ds.YelpRetriever;
import dragon.utils.DbHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lin.cheng on 7/20/15.
 */
@Stateless
public class GroupBean implements GroupIntf {

    @EJB
    BizIntf eb;

    static Log logger = LogFactory.getLog(GroupBean.class);

    public Group saveGroup(Group g) {

        if(g == null) {
            return null;
        }

        Boolean isNew = g.getId() == null || g.getId() <= 0;

        if(isNew){
            logger.info("Adding group " + g.getName());

            String key = g.getName();
            Long gid = DbHelper.runWithSingleResult2(null, "select id from dragon_group where name =?", key);
            if(gid != null) {
//                throw new RuntimeException("Group already exists: " + key);
                logger.warn("Group already exists: " + key);
            }
            Long id = DbHelper.getNextId(null);
            DbHelper.runUpdate2(null, "insert into dragon_group (id,name,preference,active,no_approve,alias) VALUES(?,?,?,?,?,?)",
                    id, g.getName(), g.getPreference(), g.getActive(), g.getNoApprove(), g.getAlias());
        } else {
            logger.info("Updating group " + g.getName());

            DbHelper.runUpdate2(null, "update dragon_group set preference=?,no_approve=?,alias=?,active=? where id=?",
                    g.getPreference(), g.getNoApprove(), g.getAlias(), g.getActive(), g.getId());
        }

        Group ret = getGroup(g.getName());
        return ret;
    }

    public int applyPreference(Group g){

        logger.info("Applying: " + g.getName());

        if(g == null){
            return -1;
        }

        DsRetriever dr = new YelpRetriever(g.getPreference());
        int cnt = dr.searchAndImport(g.getId());

        return cnt;
    }

    public List<Group> getGroups() {
        Connection conn = null;
        List<Group> list = new ArrayList<Group>();
        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from dragon_group");

            while (rs.next()) {
                list.add(new Group(rs.getLong("id"), rs.getString("name"), rs.getString("alias"), rs.getString("preference"),
                        rs.getBoolean("active"), rs.getBoolean("no_approve")));
            }
        } catch (Exception e) {
            logger.error("");
        } finally {
            DbHelper.closeConn(conn);
        }
        return list;
    }

    public Group getGroup(String key){
        Connection conn = null;
        Group g = null;

        try {
            conn = DbHelper.getConn();
            PreparedStatement st = conn.prepareStatement("select * from dragon_group where name = ?");
            DbHelper.setParameters(st, key);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                g = new Group(rs.getLong("id"), rs.getString("name"), rs.getString("alias"), rs.getString("preference"),
                        rs.getBoolean("active"), rs.getBoolean("no_approve"));
                return g;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            DbHelper.closeConn(conn);
        }

        return g;
    }

    public Restaurant addByBizId(String yid, Long gid){
        DsRetriever dr = new YelpRetriever();
        Restaurant ret = dr.addByBid(gid, yid);
        return ret;
    }

    public int saveUserToGroup(String email, String gname, boolean admin) {
        Long gid = DbHelper.runWithSingleResult2(null, "select id from dragon_group where name = ?", gname);
        int cnt = saveUserToGroup(email, gid, admin);
        return cnt;
    }

    public int removeUserFromGroup(String email, String gname) {

        Long gid = DbHelper.runWithSingleResult2(null, "select id from dragon_group where name = ?", gname);
        int cnt = removeUserFromGroup(email, gid);
        return cnt;
    }

    public int saveUserToGroup(String email, Long gid, boolean admin) {

        logger.info("Adding user: " + email + " -> " + gid);

        int cnt = 0;
        Long uid = DbHelper.runWithSingleResult2(null, "select id from dragon_user where email = ?", email);
        Boolean ex =  DbHelper.runWithSingleResult2(null, "select admin from dragon_group_user where u_id = ? and g_id = ?", uid, gid);
        if(ex == null) {
            cnt = DbHelper.runUpdate2(null, "insert into dragon_group_user (g_id,u_id,admin) VALUES(?,?,?)",
                    gid, uid, admin);
        } else if(ex != admin) {
            cnt = DbHelper.runUpdate2(null, "update dragon_group_user set admin=? where g_id=? and u_id=?", admin, gid, uid);
        }

        return cnt;
    }

    public int removeUserFromGroup(String email, Long gid) {

        logger.info("Removing user: " + email + " -> " + gid);

        Long uid = DbHelper.runWithSingleResult2(null, "select id from dragon_user where email = ?", email);
        int cnt = DbHelper.runUpdate2(null, "delete from dragon_group_user where g_id=? and u_id=?", gid, uid);
        return cnt;
    }

    public int saveRestaurantByName(Long rid, String gname, Long factor) {

        Long gid = DbHelper.runWithSingleResult2(null, "select id from dragon_group where name = ?", gname);
        int cnt = saveRestaurantToGroup(rid, gid, factor);

        return cnt;
    }

    public int saveRestaurantToGroup(Long rid, Long gid, Long factor) {

        logger.info("Saving biz: " + rid + " -> " + gid);

        int cnt = 0;
        Long ex =  DbHelper.runWithSingleResult2(null, "select factor from dragon_group_rest where res_id = ? and g_id = ?", rid, gid);
        if(ex == null) {
            logger.info("Adding biz: " + rid + " -> " + gid);
            cnt = DbHelper.runUpdate2(null, "insert into dragon_group_rest (g_id,res_id,factor) VALUES(?,?,?)", gid, rid, factor);
        } else if(Long.compare(ex, factor) != 0 && factor > 0) {
            if(factor > 30){
                factor = 30L;
            }
            logger.info("Factor changed:" + rid + "|" + gid);
            cnt = DbHelper.runUpdate2(null, "update dragon_group_rest set factor=? where g_id=? and res_id=?", factor, gid, rid);
        }

        return cnt;
    }

    public int removeRestaurantFromGroup(Long rid, Long gid) {

        logger.info("Removing biz: " + rid + " -> " + gid);

        int cnt = DbHelper.runUpdate2(null, "delete from dragon_group_rest where g_id=? and res_id=?", gid, rid);
        return cnt;
    }

    //Not thread safe
    public Boolean subscribe(String email, Long gid, boolean sub) {

        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return false;
        }

        logger.info(email + "->" + gid + (sub ? " subing..." : " unsubing..."));

        Long uid = DbHelper.runWithSingleResult2(null, "select id from dragon_user where email = ?", email);
        if (uid == null) {
            DbHelper.runUpdate2(null, "insert into dragon_user (id,email,subscribed,name) VALUES(?,?,?,?)",
                    DbHelper.getNextId(null), email, true, email.split("@")[0]);
        }

        int cnt = 0;
        if(gid != null && gid > 0) {
            if (sub) {
                cnt = saveUserToGroup(email, gid, false);
            } else {
                cnt = removeUserFromGroup(email, gid);
            }
        }

        return cnt > 0;
    }

    public Long saveUser(User u) {

        logger.info("Saving user: " + u.getEmail());

        String key = u.getEmail();
        int cnt = DbHelper.runUpdate2(null, "update dragon_user set subscribed=?, name=? where email=?",
                u.getSubscribed(), u.getName(), key);

        Long id = DbHelper.runWithSingleResult2(null, "select id from dragon_user where email = ?", key);

        if (cnt > 0) {
            return id;
        }

        id = DbHelper.getNextId(null);
        DbHelper.runUpdate2(null, "insert into dragon_user (id,email,subscribed,name) VALUES(?,?,?,?)",
                id, key, u.getSubscribed(), u.getName());

        return id;
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

}
