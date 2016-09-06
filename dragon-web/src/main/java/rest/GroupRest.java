package rest;

import com.google.gson.internal.LinkedTreeMap;
import dragon.comm.JSONHelper;
import dragon.comm.Pair;
import dragon.model.food.Group;
import dragon.model.food.Restaurant;
import dragon.model.food.User;
import dragon.service.GroupBean;
import dragon.service.GroupIntf;
import dragon.service.ds.DsRetriever;
import dragon.service.ds.YelpRetriever;
import dragon.utils.BeanFinder;
import org.apache.commons.collections.CollectionUtils;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lin.cheng on 7/22/15.
 */
@Path("/biz/group")
public class GroupRest extends BaseRest {

    @Path("save")
    @POST
    public String add(String json, @QueryParam("apply") Boolean apply, @QueryParam("mail") String mail) {

        try {
            GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);

            Group g = JSONHelper.fromJson2(json, Group.class);
            g = gb.saveGroup(g);
            gb.saveUser(new User(mail));
            gb.saveUserToGroup(mail, g.getId(), true);
            if (apply) {
                gb.applyPreference(g);
            }

            return JSONHelper.toJson(g);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("scan")
    @POST
    public String scan(String setting) {
        DsRetriever dr = new YelpRetriever(setting);
        try {
            List<Restaurant> ret = dr.searchAndImport(null);
            return JSONHelper.toJson(ret);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("factors")
    @POST
    public String scan(String factors, @QueryParam("gid") Long gid) {
        try {
            List<LinkedTreeMap> list = (List<LinkedTreeMap>) JSONHelper.fromJson(factors, ArrayList.class);
            List<Pair> cs = new ArrayList();
            if (CollectionUtils.isNotEmpty(list)) {
                for (LinkedTreeMap item : list) {

                    Pair<String, String> p = new Pair(item.get("left").toString(), item.get("right").toString());
                    cs.add(p);
                }
            }

            GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            int ret = gb.saveRestaurantToGroupBatch(cs, gid);
            return JSONHelper.toJson(ret);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("all")
    @GET
    public String view(@QueryParam("uid") Long uid) {
        try {
            GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            List<Group> list = gb.getGroups(uid);
            return JSONHelper.toJson(list);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("{key}")
    @GET
    public String item(@PathParam("key") String key, @QueryParam("limit") int limit) {
        try {
            GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            Group g = gb.getGroup(new Pair<String, Object>("name", key));
            gb.loadDependencies(g, limit);
            return JSONHelper.toJson(g);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Path("sub")
    @GET
    public String sub(@QueryParam("mail") String mail, @QueryParam("gid") Long gid) {
        try {
            GroupIntf t = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            if (t.subscribe(mail, gid, true)) {
                return "Subscribed!";
            } else {
                return "Error!";
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @Path("mute")
    @GET
    public String sub(@QueryParam("gid") Long gid, @QueryParam("mute") boolean mute) {
        try {
            GroupIntf t = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            boolean ret = t.mute(gid, mute);
            return JSONHelper.toJson(ret);
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @Path("unsub")
    @GET
    public String unsub(@QueryParam("mail") String mail, @QueryParam("gid") Long gid) {
        try {
            GroupIntf t = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            if (t.subscribe(mail, gid, false)) {
                return "Unsubscribed!";
            } else {
                return "Error!";
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
