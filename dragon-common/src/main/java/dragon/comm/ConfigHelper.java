package dragon.comm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class ConfigHelper {
    static final String PATH = "/opt/dragon/config.txt";
    static final Map<String, String> map = new HashMap<String, String>();

    private static ConfigHelper instance;
    private static Object lock = new Object();

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
                    while ((line = bufferedReader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() == 0) {
                            continue;
                        }

                        String[] property;
                        int index = line.indexOf("=");
                        if (index > 0) {
                            property = new String[2];
                            property[0] = line.substring(0, index).trim();
                            property[1] = line.substring(index + 1).trim();
                        } else {
                            property = null;
                        }
                        if (property != null) {
                            map.put(property[0], property[1]);
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger("ConfigHelper").log(Level.SEVERE, e.getMessage());
                }
            }
        }

        return instance;
    }

    public String getConfig(String key) {
        return map.get(key);
    }
}
