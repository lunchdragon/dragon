package dragon.utils;

import dragon.service.Eat;
import dragon.service.EatBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class ConfigHelper {
    static final String PATH = "/opt/dragon/config.txt";
    static final String EN_PF = "en.";
    static final Map<String, String> map = new HashMap<String, String>();

    private static ConfigHelper instance;
    private static final Object lock = new Object();
    static Log logger = LogFactory.getLog(ConfigHelper.class);

    private ConfigHelper() {
    }

    public static ConfigHelper instance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ConfigHelper();
                }
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(PATH)));
                    String line = null;
                    Eat t = new EatBean();
                    while ((line = bufferedReader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() == 0) {
                            continue;
                        }

                        String[] property;
                        String value = null;
                        int index = line.indexOf("=");
                        if (index > 0) {
                            property = new String[2];
                            property[0] = line.substring(0, index).trim();
                            value = line.substring(index + 1).trim();

                            if(value.startsWith(EN_PF)){
                                value = t.getSecret(value);
                            }
                        } else {
                            property = null;
                        }
                        if (property != null) {
                            map.put(property[0] ,value);
                        }
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }

        return instance;
    }

    public String getConfig(String key) {
        return map.get(key);
    }
}
