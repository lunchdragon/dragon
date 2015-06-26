package rest;

import com.google.gson.Gson;
import dragon.*;
import dragon.db.DbHelper;
import dragon.service.*;
import dragon.model.food.*;

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
    public String what(@QueryParam("save") Boolean save) throws Exception {
        bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);
        Restaurant r = t.pickRestaurant();

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
    public String batchAdd(String csv) throws Exception{
        bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);
        int cnt = t.importRestaurants(csv);
        return cnt + "";
    }

    @Path("add")
    @PUT
    public String add(String json) throws Exception{
        bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);
        Gson gs = new Gson();
        Restaurant r = gs.fromJson(json, Restaurant.class);
        t.saveRestaurant(r, null);

        return "Succeed!";
    }

    @Path("del")
    @DELETE
    public String del(@QueryParam("name") String name) throws Exception{
        int cnt = DbHelper.runUpdate(null, "delete from dragon_restaurant where lower(name) = '%s'", name.toLowerCase());
        return cnt + "";
    }

    @Path("view")
    @GET
    public String view() throws Exception{
        bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);
        List<Restaurant> rs = t.getRestaurants();
        Gson gs = new Gson();
        return gs.toJson(rs);

    }

    @Path("sub")
    @GET
    public String sub(@QueryParam("mail") String mail) throws Exception{
        bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);
        if(t.subscribe(mail, true)){
            return "Subscribed!";
        } else{
            return "Error!";
        }
    }

    @Path("unsub")
    @GET
    public String unsub(@QueryParam("mail") String mail) throws Exception{
        bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);
        if(t.subscribe(mail, false)){
            return "Unsubscribed!";
        } else{
            return "Error!";
        }
    }

    @Path("vote")
    @GET
    public String vote(@QueryParam("id") Long id, @QueryParam("mail") String mail, @QueryParam("vote") int vote) throws Exception{

        Vote.Result res = Vote.Result.values()[vote];
        Vote v = new Vote();
        v.setRecId(id);
        v.setResult(res);
        v.setEmail(mail);
        bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);

        if(t.vote(v)){
            return "Succeed!";
        } else{
            return "Error!";
        }
    }

    //Threading test
    @Path("test")
    @GET
    public String thread(@QueryParam("name") final String name) throws Exception{
        final bar t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);

        for(int i = 0; i < 50; i++){
            new Thread() {
                public void run() {
                    System.out.println(this.getId());
                    Restaurant r = new Restaurant(name, "", 2, 5);
                    t.saveRestaurant(r, null);
                }
            }.start();
        }

        return name;
    }

}
