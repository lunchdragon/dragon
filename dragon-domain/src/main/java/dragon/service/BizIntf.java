package dragon.service;

import dragon.comm.Pair;
import dragon.model.food.*;
import dragon.model.job.Schedule;

import javax.ejb.Local;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Created by lin.cheng on 6/1/15.
 */
@Local
public interface BizIntf {
    Restaurant pickRestaurant(Long gid);
    List<Restaurant> getRestaurants(String condition);
    List<Restaurant> getRestaurants(Long gid);
    Long saveRestaurant(Restaurant r, Connection conn);
    int importRestaurants(String csv);
    String vote(Vote v, Boolean resend, Boolean admin);
    void sendLunchEmail(String reason, Long gid);
    Map<String, Stat> stat(long gid, long exId, Boolean sort);
    Map<String, Stat> stat2(long gid, int days);
    Record saveRecord(Record r);
    String saveSecret(String key, String value);
    String getSecret(String key);
    List<String> getMails(Long gid);
    Restaurant getRestaurant(Pair<String, Object> p);
    List<Schedule> getSchedules(String condition);
    Schedule saveSchedule(Schedule s);
    void printPerfData();
}
