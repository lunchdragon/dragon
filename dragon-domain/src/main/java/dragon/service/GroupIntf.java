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
    Group saveGroup(Group g)throws Exception;
    int saveUserToGroup(String email, String gname, boolean admin)throws Exception;
    int removeUserFromGroup(String email, String gname)throws Exception;
    int saveUserToGroup(String email, Long gid, boolean admin)throws Exception;
    int removeUserFromGroup(String email, Long gid)throws Exception;
    int saveRestaurantToGroup(Long rid, Long gid, Long factor)throws Exception;
    int saveRestaurantToGroupBatch(List<Pair> pair, Long gid) throws Exception;
    int saveRestaurantByName(Long rid, String gname, Long factor)throws Exception;
    int removeRestaurantFromGroup(Long rid, Long gid)throws Exception;
    int applyPreference(Group g);
    Restaurant addByBizId(String yid, Long gid)throws Exception;
    Boolean subscribe(String email, Long gid, boolean sub)throws Exception;
    Boolean subscribe(String email, Long gid, boolean sub, boolean admin)throws Exception;
    List<Group> getGroups(Long uid);
    Group getGroup(Pair<String, Object> p);
    Long saveUser(User u, boolean reg)throws Exception;
    void loadDependencies(Group g, int limit) throws Exception;
    User getUser(String name);
    List<User> getUsers(Long uid) throws Exception;
    Boolean mute(Long gid, boolean mute)throws Exception;
}
