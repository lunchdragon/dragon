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
public interface bar {
    Restaurant pickRestaurant();
    List<Restaurant> getRestaurants();
    Long saveRestaurant(Restaurant r, Connection conn);
    int importRestaurants(String csv);
    Long saveUser(User u);
    Boolean subscribe(String email, boolean sub);
    Boolean vote(Vote v);
    void sendLunchEmail(String reason);
    Map<String, Stat> stat();
    Long saveRecord(Record r);
}
