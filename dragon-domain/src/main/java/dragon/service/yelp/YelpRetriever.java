package dragon.service.yelp;

import dragon.model.food.Restaurant;
import dragon.service.Eat;
import dragon.service.EatBean;
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
public class YelpRetriever {

    static Log logger = LogFactory.getLog(EatBean.class);

    public static int importFromYelp() {

        String location = ConfigHelper.instance().getConfig("location");
        String category = ConfigHelper.instance().getConfig("category");
        String exclude = ConfigHelper.instance().getConfig("exclude");
        String distance = ConfigHelper.instance().getConfig("distance");
        String reviews = ConfigHelper.instance().getConfig("reviews");

        String[] exs = {};

        YelpAPI ya = new YelpAPI();
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
        if (StringUtils.isNotBlank(exclude)) {
            exs = exclude.split(",");
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
            logger.info(String.format("%s businesses found ...", businesses.size()));
            if (businesses.size() == 0) {
                break;//no more
            }

            Connection conn = null;

            try {
                conn = DbHelper.getConn();
                for (Object obj : businesses) {
                    JSONObject bo = (JSONObject) obj;

                    String cats = bo.get("categories").toString().toLowerCase();
                    Integer factor = ((Long)Math.round(Math.pow(2, Float.parseFloat(bo.get("rating").toString())))).intValue(); // 2^rating
                    String name = bo.get("name").toString();
                    Integer rc = Integer.parseInt(bo.get("review_count").toString());

                    boolean excluded = rc < Integer.parseInt(reviews);
                    if(!excluded) {
                        for (String ex : exs) {
                            if (cats.contains(ex)) {
                                excluded = true;
                                break;
                            }
                        }
                    }
                    if (name.contains("Express") || name.contains("Tea")) {
                        excluded = true;
                    }

                    if (excluded) {
                        logger.info(name + " excluded.");
                        continue;
                    }

                    if (cats.contains("japan") || cats.contains("korea")) {
                        factor -= 10;
                    }
                    if (cats.contains("canton")) {
                        factor -= 5;
                    }
                    if (name.contains("Seafood") || name.contains("BBQ")) {
                        factor -= 5;
                    }

                    String cat = cats.split(",")[0];
                    cat = cat.substring(cat.indexOf("\"") + 1, cat.lastIndexOf("\""));

                    Restaurant r = new Restaurant(name, bo.get("url").toString(), factor, 20, "", cat);
                    logger.debug("saving " + r.toString());

                    Eat t = new EatBean();
                    t.saveRestaurant(r, conn);

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

    public static void main(String[] args) {
        importFromYelp();
    }

}
