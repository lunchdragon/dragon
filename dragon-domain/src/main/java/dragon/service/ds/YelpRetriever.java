package dragon.service.ds;

import dragon.comm.Pair;
import dragon.model.food.Restaurant;
import dragon.service.BizIntf;
import dragon.service.BizBean;
import dragon.service.GroupBean;
import dragon.service.GroupIntf;
import dragon.utils.BeanFinder;
import dragon.utils.ConfigHelper;
import dragon.utils.DbHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by lin.cheng
 */
public class YelpRetriever implements DsRetriever {

    static GroupIntf gb = getGb();
    static BizIntf eb = getEb();
    static Log logger = LogFactory.getLog(YelpRetriever.class);

    public String location = "";//must set by client apps
    public String category = "";//all if not set
    public String exclude = "";
    public String prefer = "";
    public String distance = ConfigHelper.instance().getConfig("distance");
    public String reviews = ConfigHelper.instance().getConfig("reviews");

    public YelpRetriever() {
    }

    public YelpRetriever(String settings) {
        String[] ss = settings.split(";");
        for (String s : ss){
            String key = StringUtils.trim(s.split("=")[0]);
            String value = StringUtils.trim(s.split("=")[1]);

            if("location".equalsIgnoreCase(key)){
                this.location = value;
            } else if("category".equalsIgnoreCase(key)){
                this.category = value;
            } else if("exclude".equalsIgnoreCase(key)){
                this.exclude = value;
            } else if("distance".equalsIgnoreCase(key)){
                this.distance = value;
            } else if("reviews".equalsIgnoreCase(key)){
                this.reviews = value;
            } else if("prefer".equalsIgnoreCase(key)){
                this.prefer = value;
            }
        }

    }

    public int searchAndImport(Long gid) {

        String[] exs = {};
        String[] pres = {};

        YelpAPI ya = new YelpAPI();
        YelpAPI.YelpAPICLI yaCli = getYaCli();
        if (StringUtils.isNotBlank(exclude)) {
            exs = exclude.split(",");
        }
        if (StringUtils.isNotBlank(prefer)) {
            pres = prefer.split(",");
        }

        int cnt = 0;

        for (int i = 0; i < 10; i++) {//max = 200
            yaCli.offset = 20 * i;
            String json = YelpAPI.queryAPI(ya, yaCli);

            JSONParser parser = new JSONParser();
            JSONObject response = null;
            try {
                response = (JSONObject) parser.parse(json);
            } catch (ParseException pe) {
                logger.error("Error: could not parse JSON response:" + json);
                return -1;
            }

            JSONArray businesses = (JSONArray) response.get("businesses");
            logger.info(String.format("%s businesses found ...", businesses==null ? 0 : businesses.size()));
            if (businesses == null || businesses.size() == 0) {
                break;//no more
            }

            Connection conn = null;

            try {
                conn = DbHelper.getConn();
                for (Object obj : businesses) {
                    JSONObject bo = (JSONObject) obj;

                    String cats = bo.get("categories").toString().toLowerCase();
                    String name = bo.get("name").toString();
                    String id = bo.get("id").toString();
                    Integer rc = Integer.parseInt(bo.get("review_count").toString());

                    boolean excluded = rc < Integer.parseInt(reviews);
                    double adjust = 0;
                    if(!excluded) {
                        for (String ex : exs) {
                            if (cats.contains(ex) || id.contains(ex)) {
                                excluded = true;
                                break;
                            }
                        }
                    }

                    if (excluded) {
                        logger.info(id + " excluded.");
                        continue;
                    }

                    for (String pre : pres) {
                        if (cats.contains(pre) || id.contains(pre)) {
                            logger.info(id + " prefered.");
                            adjust += 0.5;
                            break;
                        }
                    }

                    Long factor = Math.round(Math.pow(2, Float.parseFloat(bo.get("rating").toString()))); // 2^rating
                    Long factorForGrp = Math.round(Math.pow(2, adjust + Float.parseFloat(bo.get("rating").toString())));
                    if(factor > 30){
                        factor = 30L;
                    }
                    if(factorForGrp > 30){
                        factorForGrp = 30L;
                    }

                    String cat = cats.split(",")[0];
                    cat = cat.substring(cat.indexOf("\"") + 1, cat.lastIndexOf("\""));

                    Restaurant r = new Restaurant(id, bo.get("url").toString(), factor, name, cat);
                    Long rid = eb.saveRestaurant(r, conn);

                    if(gid != null && gid > 0) {
                        gb.saveRestaurantToGroup(rid, gid, factorForGrp);
                    }

                    cnt++;
                }
            } catch (SQLException e) {
                logger.error("", e);
            } finally {
                DbHelper.closeConn(conn);
            }
        }

        logger.info(String.format("Total : %s businesses found.", cnt));
        return cnt;
    }

    public Restaurant addByBid(Long gid, String bid){
        YelpAPI ya = new YelpAPI();
        String json = ya.searchByBusinessId(bid);
        JSONParser parser = new JSONParser();
        JSONObject bo = null;
        try {
            bo = (JSONObject) parser.parse(json);
        } catch (ParseException pe) {
            logger.error("Error: could not parse JSON response:" + json);
            return null;
        }

        if(bo == null || bo.get("name") == null){
            logger.error("Bid not found: " + bid);
            return null;
        }
        String name = bo.get("name").toString();
        String url = bo.get("url").toString();
        Long factor = Math.round(Math.pow(2, Float.parseFloat(bo.get("rating").toString())));
        String cats = bo.get("categories").toString().toLowerCase();
        String cat = cats.split(",")[0];
        cat = cat.substring(cat.indexOf("\"") + 1, cat.lastIndexOf("\""));

        Restaurant r = new Restaurant(bid, url, factor, name,  cat);
        logger.debug("saving " + r.toString());
        Long rid = eb.saveRestaurant(r, null);
        Restaurant ret = eb.getRestaurant(new Pair<String, Object>("name", r.getName()));
        if(gid != null && gid > 0) {
            gb.saveRestaurantToGroup(rid, gid, factor);
        }

        return ret;
    }

    private YelpAPI.YelpAPICLI getYaCli(){

        YelpAPI.YelpAPICLI yaCli = new YelpAPI.YelpAPICLI();
        yaCli.limit = 20;
        if (StringUtils.isNotBlank(location)) {
            yaCli.location = location;
        }
        if (StringUtils.isNotBlank(category)) {
            yaCli.cat = category;
        }
        if (StringUtils.isNotBlank(distance)) {
            yaCli.dis = distance;
        }
        return yaCli;
    }

    private static BizIntf getEb(){
        if(eb == null){
            try {
                eb = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            } catch (Exception e){
                eb = new BizBean();
            }
        }
        return eb;
    }

    private static GroupIntf getGb(){
        if(gb == null){
            try {
                gb = BeanFinder.getInstance().getLocalSessionBean(GroupBean.class);
            } catch (Exception e){
                gb = new GroupBean();
            }
        }
        return gb;
    }

    public static void main(String[] args) {
        new YelpRetriever().searchAndImport(null);
//        new YelpRetriever().addByBid(null, "chef-yu-hunan-gourmet-sunnyvale");
    }

}
