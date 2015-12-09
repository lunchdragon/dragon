package dragon.service;

import dragon.comm.Pair;
import dragon.model.food.Group;
import dragon.model.food.Restaurant;
import dragon.model.food.User;

import javax.ejb.Local;
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
    int saveRestaurantByName(Long rid, String gname, Long factor);
    int removeRestaurantFromGroup(Long rid, Long gid);
    int applyPreference(Group g);
    Restaurant addByBizId(String yid, Long gid);
    Boolean subscribe(String email, Long gid, boolean sub);
    List<Group> getGroups(Long uid);
    Group getGroup(Pair<String, Object> p);
    Long saveUser(User u);
}
