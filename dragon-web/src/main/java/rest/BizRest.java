package rest;

import dragon.comm.JSONHelper;
import dragon.comm.Pair;
import dragon.model.job.Schedule;
import dragon.service.*;
import dragon.model.food.*;
import dragon.service.sec.SecureContexts;
import dragon.utils.BeanFinder;
import dragon.utils.DbHelper;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lin.cheng on 6/17/15.
 */
@Path("/biz")
@Consumes("text/xml")
public class BizRest extends BaseRest {

    @Path("what")
    @GET
    public String what(@QueryParam("reason") String reason,
                       @QueryParam("gid") Long gid,
                       @QueryParam("notify") boolean notify) {

        try {
            BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            Restaurant r = t.pickup(reason, gid, notify);
            return JSONHelper.toJson(r);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("summary")
    @GET
    public String summary(@QueryParam("gid") Long gid) {

        BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        try {
            t.sendSummaryEmail(gid);
            return JSONHelper.toJson("OK");
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("save")
    @POST
    public String add(String json, @QueryParam("gid") Long gid) {
        try {
            BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            Restaurant r = JSONHelper.fromJson2(json, Restaurant.class);
            Long rid = t.saveRestaurant(r, null);
            if (gid != null && gid > 0) {
                gb.saveRestaurantToGroup(rid, gid, r.getFactor());
            }

            return JSONHelper.toJson(r);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("del")
    @DELETE
    public String del(@QueryParam("rid") Long rid, @QueryParam("gid") Long gid) {
        try {
            GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            int cnt = gb.removeRestaurantFromGroup(rid, gid);
            return cnt + "";
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("{key}")
    @GET
    public String item(@PathParam("key") String key) {
        try {
            BizIntf bb = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            Restaurant r = bb.getRestaurant(new Pair<String, Object>("name", key));
            return JSONHelper.toJson(r);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("/record/{id}")
    @GET
    public String getRecord(@PathParam("id") Long rid) {
        try {
            BizIntf bb = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            Record r = bb.getRecord(rid);
            return JSONHelper.toJson(r);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("/restaurant/{id}")
    @GET
    public String getRestaurant(@PathParam("id") Long rid) {
        try {
            BizIntf bb = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            Restaurant r = bb.getRestaurantById(rid);
            return JSONHelper.toJson(r);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("all")
    @GET
    public String view(@QueryParam("gid") Long gid) {
        try {
            BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            List<Restaurant> rs = new ArrayList<Restaurant>();
            if (gid != null && gid > 0) {
                rs = t.getRestaurants(gid);
            } else {
                rs = t.getRestaurants("");
            }
            return JSONHelper.toJson(rs);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("schedule")
    @GET
    public String getSch(@QueryParam("gid") Long gid) {
        try {
            BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            List<Schedule> rs = new ArrayList<Schedule>();
            if (gid != null && gid > 0) {
                rs = t.getSchedules("gid=" + gid);
            } else {
                rs = t.getSchedules(null);
            }
            return JSONHelper.toJson(rs);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("schedule/save")
    @POST
    public String saveSch(String json) {
        try {
            BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            Schedule r = JSONHelper.fromJson2(json, Schedule.class);
            r = t.saveSchedule(r);

            return JSONHelper.toJson(r);
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @Path("xadd")
    @POST
    public String batchAdd(String csv) {
        try {
            BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            int cnt = t.importRestaurants(csv);
            return cnt + " items updated.";
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @Path("yadd")
    @GET
    public String addByYelpId(@QueryParam("yid") String yid, @QueryParam("gid") Long gid) {
        try {
            GroupIntf t = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            Restaurant r = t.addByBizId(yid, gid);
            return JSONHelper.toJson(r);
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @Path("vote")
    @GET
    public String vote(@QueryParam("id") Long id, @QueryParam("mail") String mail,
                       @QueryParam("vote") int vote, @QueryParam("nosend") boolean nosend, @QueryParam("x") boolean x) {

        try {
            Vote.Result res = Vote.Result.values()[vote];
            Vote v = new Vote();
            v.setRecId(id);
            v.setResult(res);
            v.setEmail(mail);
            v.setIp(SecureContexts.getRemoteAddr());
            BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);

            return t.vote(v, !nosend, x);
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @Path("sec")
    @GET
    public String sec(@QueryParam("key") final String key, @QueryParam("value") final String value) {
        try {
            final BizIntf t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            String ret = t.saveSecret(key, value);
            return ret;
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @Path("pia")
    @GET
    public void pia(@QueryParam("gid") Long gid) {
        if (gid == null) {
            gid = 29229L;
        }
        DbHelper.runUpdate2(null, "update dragon_schedule set active=NOT active where gid=?", gid);
    }
}
