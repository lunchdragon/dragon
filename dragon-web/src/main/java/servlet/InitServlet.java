package servlet;

import java.io.File;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author mli
 */
public class InitServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        config.getServletContext().log("Initializing System ...");
        String log4jConfigFile = config.getInitParameter("log4j-config-file");
        if (StringUtils.isBlank(log4jConfigFile)) {
            log4jConfigFile = "/opt/dragon/log4j.properties";
        }
        initLog4j(log4jConfigFile);
        config.getServletContext().log("System ready.");
    }

    private void initLog4j(String configFile) {
        if (new File(configFile).exists()) {
            PropertyConfigurator.configureAndWatch(configFile, 10000L); // 10 sec
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
