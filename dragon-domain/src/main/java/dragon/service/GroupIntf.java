package dragon.service;

import dragon.comm.Pair;
import dragon.model.food.Group;
import dragon.model.food.Restaurant;
import dragon.model.food.User;

import javax.ejb.Local;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by lin.cheng on 7/20/15.
 */
@Local
public interface GroupIntf {
    Group saveGroup(Group g);
    int saveUserToGroup(String email, String gname, boolean admin);
    int removeUserFromGroup(String email, String gname);
    int saveUserToGroup(String email, Long gid, boolean admin);
    int removeUserFromGroup(String email, Long gid);
    int saveRestaurantToGroup(Long rid, Long gid, Long factor);
    int saveRestaurantToGroupBatch(List<Pair> pair, Long gid) throws Exception;
    int saveRestaurantByName(Long rid, String gname, Long factor);
    int removeRestaurantFromGroup(Long rid, Long gid);
    int applyPreference(Group g);
    Restaurant addByBizId(String yid, Long gid);
    Boolean subscribe(String email, Long gid, boolean sub);
    Boolean subscribe(String email, Long gid, boolean sub, boolean admin);
    List<Group> getGroups(Long uid);
    Group getGroup(Pair<String, Object> p);
    Long saveUser(User u);
    void loadDependencies(Group g, int limit) throws Exception;
    User getUser(Long uid);
    List<User> getUsers(Long uid) throws Exception;
    Boolean mute(Long gid, boolean mute);
}
