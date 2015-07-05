package rest;

import com.google.gson.Gson;
import dragon.utils.DbHelper;
import dragon.service.*;
import dragon.model.food.*;
import dragon.utils.BeanFinder;

import javax.ws.rs.*;
import java.util.List;

/**
 * Created by lin.cheng on 6/17/15.
 */
@Path("/eat")
@Consumes("text/xml")
public class eatRest {
    @Path("what")
    @GET
    public String what(@QueryParam("save") Boolean save) {
        Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        Restaurant r = t.pickRestaurant(null);

        if(save) {
            Record rec = new Record();
            rec.setResid(r.getId());
            t.saveRecord(rec);
        }

        Gson gs = new Gson();
        return gs.toJson(r);
    }

    @Path("xadd")
    @PUT
    public String batchAdd(String csv) {
        Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        int cnt = t.importRestaurants(csv);
        return cnt + "";
    }

    @Path("add")
    @PUT
    public String add(String json) {
        Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        Gson gs = new Gson();
        Restaurant r = gs.fromJson(json, Restaurant.class);
        t.saveRestaurant(r, null);

        return "Succeed!";
    }

    @Path("del")
    @DELETE
    public String del(@QueryParam("name") String name) {
        int cnt = DbHelper.runUpdate(null, "delete from dragon_restaurant where lower(name) = '%s'", name.toLowerCase());
        return cnt + "";
    }

    @Path("view")
    @GET
    public String view() {
        Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        List<Restaurant> rs = t.getRestaurants(null);
        Gson gs = new Gson();
        return gs.toJson(rs);

    }

    @Path("sub")
    @GET
    public String sub(@QueryParam("mail") String mail) {
        Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        if(t.subscribe(mail, true)){
            return "Subscribed!";
        } else{
            return "Error!";
        }
    }

    @Path("unsub")
    @GET
    public String unsub(@QueryParam("mail") String mail) {
        Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        if(t.subscribe(mail, false)){
            return "Unsubscribed!";
        } else{
            return "Error!";
        }
    }

    @Path("vote")
    @GET
    public String vote(@QueryParam("id") Long id, @QueryParam("mail") String mail, @QueryParam("vote") int vote) {

        Vote.Result res = Vote.Result.values()[vote];
        Vote v = new Vote();
        v.setRecId(id);
        v.setResult(res);
        v.setEmail(mail);
        Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);

        if(t.vote(v)){
            return "Succeed!";
        } else{
            return "Error!";
        }
    }

    @Path("sec")
    @GET
    public String sec(@QueryParam("key") final String key, @QueryParam("value") final String value) {
        final Eat t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        String ret = t.saveSecret(key, value);
        return ret;
    }

}
