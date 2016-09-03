package dragon.job;

import java.util.logging.Level;
import java.util.logging.Logger;

import dragon.comm.EasyHttpClient;
import dragon.utils.ConfigHelper;
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

    public final void execute(JobExecutionContext arg0) throws JobExecutionException {
        try {
            processJob(arg0);
        } catch (Exception ex) {
            Logger.getLogger(AbstractJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected abstract void processJob(JobExecutionContext ctx) throws Exception;

    public EasyHttpClient getHttpClient() {
        String server = ConfigHelper.instance().getConfig("server");
        String port = ConfigHelper.instance().getConfig("port");

        if (httpClient == null) {
            httpClient = getHttpClient(server, Integer.parseInt(port), false, null, null);
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
