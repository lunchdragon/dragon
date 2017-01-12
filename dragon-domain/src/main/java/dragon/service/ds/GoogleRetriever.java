package dragon.service.ds;

import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.*;
import dragon.comm.Pair;
import dragon.model.food.Group;
import dragon.model.food.Restaurant;
import dragon.service.BizBean;
import dragon.service.BizIntf;
import dragon.service.GroupBean;
import dragon.service.GroupIntf;
import dragon.utils.BeanFinder;
import dragon.utils.ConfigHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lin on 2016/9/17.
 */
public class GoogleRetriever implements DsRetriever {

    private final GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyD0aVimCj5Sl8aChM4dRqqH69j3RAhrRTc");

    static GroupIntf gb = getGb();
    static BizIntf eb = getEb();
    static Log logger = LogFactory.getLog(GoogleRetriever.class);

    public String location = "37.375835,-122.011051";//must set by client apps
    public String category = "chinese";
    public String exclude = "";
    public String prefer = "";
    public String nopre = "";
    public String distance = "5000"; //meters
    public String reviews = ConfigHelper.instance().getConfig("reviews");

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getPrefer() {
        return prefer;
    }

    public void setPrefer(String prefer) {
        this.prefer = prefer;
    }

    public String getNopre() {
        return nopre;
    }

    public void setNopre(String nopre) {
        this.nopre = nopre;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    public GoogleRetriever() {
    }

    public GoogleRetriever(String settings) {
        applySettings(settings);
    }

    public Restaurant addByBid(Long gid, String bid) throws Exception {

        PlaceDetails p = PlacesApi.placeDetails(context, bid).await();

        String name = p.placeId;
        String alias = p.name;
        Restaurant r = new Restaurant(name);
        r.setAlias(alias);
        eb.saveRestaurant(r, null);
        r = eb.getRestaurant(new Pair<String, Object>("name", r.getName()));

        return r;
    }

    public List<Restaurant> searchAndImport(Long gid) throws Exception {
        List<Restaurant> ret = new ArrayList<Restaurant>();

        if (gid != null) {
            Group group = gb.getGroup(new Pair<String, Object>("id", gid));
            if (group == null) {
                logger.error("Group not found: " + gid);
                return ret;
            }
            applySettings(group.getPreference());
        }

        search(ret, null);

        for(Restaurant r:ret){
            if(gid != null && gid > 0) {
                gb.saveRestaurantToGroup(r.getId(), gid, r.getFactor());
            }
        }

        System.out.println(ret.size());
        return ret;
    }

    @Override
    public Restaurant find(String bid) throws Exception {
        PlaceDetails p = PlacesApi.placeDetails(context, bid).await();

        if(p == null){
            return null;
        }
        Restaurant r = new Restaurant(p.placeId);
        r.setAlias(p.name);
        r.setLink(p.url.toString());
        r.setSource("g");
        r.setOpen(isOpen(p.openingHours));

        return r;
    }

    private Boolean isOpen(OpeningHours oh) {
        if(oh == null) {
            return null;
        }
        if(oh.openNow){
            return true;
        }
        return false;//TODO
    }

    private void search(final List<Restaurant> list, String next) throws Exception {
        LatLng location = new LatLng(Double.parseDouble(this.location.split(",")[0]), Double.parseDouble(this.location.split(",")[1]));
        NearbySearchRequest req = PlacesApi.nearbySearchQuery(context, location);

        if (next == null) {
            req = req
                    .radius(Integer.parseInt(distance))
                    .keyword(category)
                    .name("name")
//                .openNow(true)
                    .type(PlaceType.RESTAURANT);
        } else {
            req = req.pageToken(next);
        }

        PlacesSearchResponse response = req.await();

        for (PlacesSearchResult p : response.results) {
            String name = p.placeId;
            String alias = p.name;
            Long factor = Math.round(Math.pow(2, p.rating));
            Restaurant r = new Restaurant(name);
            r.setAlias(alias);
            r.setFactor(factor);
//            r.setLink(p.url.toString());
            r.setSource("g");
            eb.saveRestaurant(r, null);
            r = eb.getRestaurant(new Pair<String, Object>("name", r.getName()));
            list.add(r);
        }

        if (response.nextPageToken != null) {
            Thread.sleep(1000);
            search(list, response.nextPageToken);
        }
    }

    private void applySettings(String settings) {
        String[] ss = settings.split(";");
        for (String s : ss) {
            String key = StringUtils.trim(s.split("=")[0]);
            String value = StringUtils.trim(s.split("=")[1]);

            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                continue;
            }

            Class clazz = this.getClass();
            String getStr = "set" + StringUtils.capitalize(key);
            Method setMethod = null;
            try {
                setMethod = clazz.getMethod(getStr, String.class);
                setMethod.invoke(this, value);
            } catch (Exception e) {
                logger.error("Invalid attribute: " + key);
                continue;
            }
        }
    }

    private static BizIntf getEb() {
        if (eb == null) {
            try {
                eb = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            } catch (Exception e) {
                eb = new BizBean();
            }
        }
        return eb;
    }

    private static GroupIntf getGb() {
        if (gb == null) {
            try {
                gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            } catch (Exception e) {
                gb = new GroupBean();
            }
        }
        return gb;
    }

    private static void testThread() throws Exception {
        for (int i = 0; i < 9; i++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        System.out.println("Thread " + currentThread().toString());
                        new GoogleRetriever().searchAndImport(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
    }

    public static void main(String[] args) {
        try {
            new GoogleRetriever().find("ChIJSZ40JCvPj4ARrtROah50NPI");
//            testThread();
            new GoogleRetriever().searchAndImport(49172L);
//            new GoogleRetriever().addByBid(null, "ChIJybh1Zz_Kj4ARwEaFrpqiSh8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
