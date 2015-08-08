package rest;

import com.google.gson.Gson;
import dragon.comm.Pair;
import dragon.model.job.Schedule;
import dragon.service.*;
import dragon.model.food.*;
import dragon.utils.BeanFinder;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lin.cheng on 6/17/15.
 */
@Path("/biz")
@Consumes("text/xml")
public class BizRest {

    @Path("what")
    @GET
    public String what(@QueryParam("gid") Long gid) {
        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        Restaurant r = t.pickRestaurant(gid);

        Record rec = new Record();
        rec.setResid(r.getId());
        rec.setgId(gid);
        rec = t.saveRecord(rec);

        return toJson(rec);
    }

    @Path("save")
    @PUT
    public String add(String json, @QueryParam("gid") Long gid) {
        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
        Gson gs = new Gson();
        Restaurant r = gs.fromJson(json, Restaurant.class);
        Long rid = t.saveRestaurant(r, null);
        if(gid != null && gid > 0) {
            gb.saveRestaurantToGroup(rid, gid, r.getFactor());
        }

        return toJson(r);
    }

    @Path("del")
    @DELETE
    public String del(@QueryParam("rid") Long rid, @QueryParam("gid") Long gid) {
        GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
        int cnt = gb.removeRestaurantFromGroup(rid, gid);
        return cnt + "";
    }

    @Path("{key}")
    @GET
    public String item(@PathParam("key") String key) {
        BizIntf bb = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        Restaurant r = bb.getRestaurant(new Pair<String, Object>("name", key));
        Gson gs = new Gson();
        return gs.toJson(r);
    }

    @Path("all")
    @GET
    public String view(@QueryParam("gid") Long gid) {
        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        List<Restaurant> rs = new ArrayList<Restaurant>();
        if(gid != null && gid > 0) {
            rs = t.getRestaurants(gid);
        } else {
            rs = t.getRestaurants("");
        }
        return toJson(rs);
    }

    @Path("schedule")
    @GET
    public String getSch(@QueryParam("gid") Long gid) {
        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        List<Schedule> rs = new ArrayList<Schedule>();
        if(gid != null && gid > 0) {
            rs = t.getSchedules("gid=" + gid);
        } else {
            rs = t.getSchedules(null);
        }
        return toJson(rs);
    }

    @Path("schedule")
    @PUT
    public String saveSch(String json) {
        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        Gson gs = new Gson();
        Schedule r = gs.fromJson(json, Schedule.class);
        r = t.saveSchedule(r);

        return toJson(r);
    }

    @Path("xadd")
    @PUT
    public String batchAdd(String csv) {
        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        int cnt = t.importRestaurants(csv);
        return cnt + " items updated.";
    }

    @Path("yadd")
    @GET
    public String addByYelpId(@QueryParam("yid") String yid, @QueryParam("gid") Long gid) {
        GroupIntf t = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
        Restaurant r = t.addByBizId(yid, gid);
        return toJson(r);
    }

    @Path("vote")
    @GET
    public String vote(@QueryParam("id") Long id, @QueryParam("mail") String mail, @QueryParam("vote") int vote) {

        Vote.Result res = Vote.Result.values()[vote];
        Vote v = new Vote();
        v.setRecId(id);
        v.setResult(res);
        v.setEmail(mail);
        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);

        if(t.vote(v, true)){
            return "Succeed!";
        } else{
            return "Error!";
        }
    }

    @Path("sec")
    @GET
    public String sec(@QueryParam("key") final String key, @QueryParam("value") final String value) {
        final BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        String ret = t.saveSecret(key, value);
        return ret;
    }

    private String toJson(Object obj){
        Gson gs = new Gson();
        return gs.toJson(obj);
    }

}
