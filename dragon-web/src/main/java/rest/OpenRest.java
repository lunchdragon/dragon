package rest;

import dragon.comm.JSONHelper;
import dragon.model.food.User;
import dragon.service.GroupBean;
import dragon.service.GroupIntf;
import dragon.service.sec.AccessController;
import dragon.service.sec.Identity;
import dragon.service.sec.LoginBean;
import dragon.utils.BeanFinder;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Created by lin.cheng on 11/2/15.
 */
@Path("/open")
public class OpenRest extends BaseRest {

    /***
     *
     * Method: GET<br>
     * Path: /h5/open/currentUser<br>
     *
     * @return Identity in JSON format
     */
    @GET
    @Path("currentUser")
    public String getCurrentUser() {
        try {
            Identity id = AccessController.getIdentity();
            return JSONHelper.toJson(id);
        } catch (Exception e){
            return createErrorResponse(e);
        }
    }

    /***
     *
     * Method: GET<br>
     * Path: /h5/open/isLoggedIn<br>
     *
     * @return "true"|"false"
     */
    @GET
    @Path("isLoggedIn")
    public String isLoggedIn() {
        try {
            Boolean ret = AccessController.isLoggedIn();
            return ret.toString();
        } catch (Exception e){
            return createErrorResponse(e);
        }
    }

    /***
     *
     * Method: POST<br>
     * Path: /h5/open/login<br>
     *
     * @param loginBean LoginBean in JSON format
     * @return "success" | "fail"
     */
    @POST
    @Path("login")
    public String login(String loginBean) {
        try {
            LoginBean lb = (LoginBean) JSONHelper.fromJson(loginBean, LoginBean.class);
            String ret = lb.login();
            return ret;
        } catch (Exception e){
            return createErrorResponse(e);
        }
    }

    /***
     *
     * Method: POST<br>
     * Path: /h5/sec/register<br>
     * Corresponding FLEX API: N/A<br>
     *
     * @param user User in JSON format
     * @return ID
     */
    @POST
    @Path("register")
    public String register(String user) {
        try {
            User u = (User) JSONHelper.fromJson(user, User.class);
            GroupIntf gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            Long ret = gb.saveUser(u, true);
            return JSONHelper.toJson(ret);
        } catch (Exception e){
            return createErrorResponse(e);
        }
    }
}
