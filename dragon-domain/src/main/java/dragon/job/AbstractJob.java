package dragon.job;

import java.util.logging.Level;
import java.util.logging.Logger;

import dragon.comm.ApplicationException;
import dragon.comm.EasyHttpClient;
import dragon.service.BizBean;
import dragon.service.BizIntf;
import dragon.utils.BeanFinder;
import dragon.utils.ConfigHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * Created by lin.cheng on 6/18/15.
 */
public abstract class AbstractJob implements Job {

    private volatile EasyHttpClient httpClient;
    public static String BASE = "/dragon/rest/";
    static Log logger = LogFactory.getLog(AbstractJob.class);

    public final void execute(JobExecutionContext arg0) throws JobExecutionException {
        try {
            processJob(arg0);
        } catch (Exception ex) {
            logger.error("", ex);
            throw new ApplicationException(ex.getMessage());
        }
    }

    protected abstract void processJob(JobExecutionContext ctx) throws Exception;

    public EasyHttpClient getHttpClient() throws Exception {

        if (httpClient == null) {
            String server = ConfigHelper.instance().getConfig("server");
            String port = ConfigHelper.instance().getConfig("port");
            BizIntf eb = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
            String pwd = eb.getSecret("svcpwd");
            httpClient = getHttpClient(server, Integer.parseInt(port), false, "service", pwd);
        }
        return httpClient;
    }

    public EasyHttpClient getHttpClient(String host, int port, Boolean ssl, String user, String pwd) {
        if (httpClient == null) {
            httpClient = new EasyHttpClient(host, port, ssl);
            if(user != null && pwd != null) {
                httpClient.setCredentials(user, pwd);
            }
        }
        return httpClient;
    }
}
