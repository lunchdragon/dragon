package rest;

import javax.ws.rs.*;
import dragon.*;
import dragon.service.*;

@Path("/app")
@Consumes("text/xml")
public class dragonRest {

    @Path("test")
    @GET
    public String test() {
        return "test";
    }

}


