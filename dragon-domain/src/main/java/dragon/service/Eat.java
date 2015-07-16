package dragon.service;

import dragon.model.food.*;

import javax.ejb.Local;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Created by lin.cheng on 6/1/15.
 */
@Local
public interface Eat {
    Restaurant pickRestaurant(String condition);
    List<Restaurant> getRestaurants(String condition);
    Long saveRestaurant(Restaurant r, Connection conn);
    int importRestaurants(String csv);
    Long saveUser(User u);
    Boolean subscribe(String email, boolean sub);
    Boolean vote(Vote v, Boolean resend);
    void sendLunchEmail(String reason);
    Map<String, Stat> stat(long exId, Boolean sort);
    Map<String, Stat> stat2(int days);
    Long saveRecord(Record r);
    String saveSecret(String key, String value);
    String getSecret(String key);
    String getMails();
}
