package rest;

import com.google.gson.Gson;
import dragon.comm.Pair;
import dragon.model.food.Group;
import dragon.model.food.Restaurant;
import dragon.model.food.User;
import dragon.service.BizBean;
import dragon.service.BizIntf;
import dragon.service.GroupBean;
import dragon.service.GroupIntf;
import dragon.utils.BeanFinder;

import javax.ws.rs.*;
import java.util.List;

/**
 * Created by lin.cheng on 7/22/15.
 */
@Path("/biz/group")
public class GroupRest {

    @Path("save")
    @PUT
    public String add(String json, @QueryParam("apply") Boolean apply, @QueryParam("mail") String mail) {
        GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);

        Gson gs = new Gson();
        Group g = gs.fromJson(json, Group.class);
        g = gb.saveGroup(g);
        gb.saveUser(new User(mail));
        gb.saveUserToGroup(mail, g.getId(), true);
        if(apply) {
            gb.applyPreference(g);
        }

        return toJson(g);
    }

    @Path("all")
    @GET
    public String view(@QueryParam("uid") Long uid) {
        GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
        List<Group> list = gb.getGroups(uid);
        return toJson(list);
    }

    @Path("{key}")
    @GET
    public String item(@PathParam("key") String key) {
        GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
        Group g = gb.getGroup(new Pair<String, Object>("name", key));
        return toJson(g);
    }

    @Path("sub")
    @GET
    public String sub(@QueryParam("mail") String mail, @QueryParam("gid") Long gid) {
        GroupIntf t = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
        if(t.subscribe(mail, gid, true)){
            return "Subscribed!";
        } else{
            return "Error!";
        }
    }

    @Path("unsub")
    @GET
    public String unsub(@QueryParam("mail") String mail, @QueryParam("gid") Long gid) {
        GroupIntf t = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
        if(t.subscribe(mail, gid, false)){
            return "Unsubscribed!";
        } else{
            return "Error!";
        }
    }

    private String toJson(Object obj){
        Gson gs = new Gson();
        return gs.toJson(obj);
    }

}
