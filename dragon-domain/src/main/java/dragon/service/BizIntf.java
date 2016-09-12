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
    Restaurant pickRestaurant(Long gid)throws Exception;
    List<Restaurant> getRestaurants(String condition);
    List<Restaurant> getRestaurants(Long gid);
    Long saveRestaurant(Restaurant r, Connection conn);
    int importRestaurants(String csv);
    String vote(Vote v, Boolean resend, Boolean admin) throws Exception;
    Restaurant pickup(String reason, Long gid, boolean notify) throws Exception;
    Map<String, Stat> stat(long gid, long exId, Boolean sort);
    Map<String, Stat> stat2(long gid, int days);
    Record saveRecord(Record r) throws Exception;
    String saveSecret(String key, String value)throws Exception;
    String getSecret(String key)throws Exception;
    List<String> getMails(Long gid)throws Exception;
    Restaurant getRestaurant(Pair<String, Object> p);
    Restaurant getRestaurantById(Long id);
    List<Schedule> getSchedules(String condition);
    Schedule saveSchedule(Schedule s)throws Exception;
    void printPerfData();
    public void sendSummaryEmail(Long gid) throws Exception;
    List<Record> getRecords(Long gid, int limit) throws Exception;
    Record getRecord(Long recId) throws Exception;
}
