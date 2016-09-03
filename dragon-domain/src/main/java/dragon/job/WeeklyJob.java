package dragon.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * Created by Lin on 2015/7/11.
 */
public class WeeklyJob extends AbstractJob {

    static Log logger = LogFactory.getLog(WeeklyJob.class);

    public WeeklyJob() {
        super();
    }

    @Override
    protected void processJob(JobExecutionContext ctx) throws Exception {

        logger.info("job executing...");

        JobDataMap map = ctx.getJobDetail().getJobDataMap();
        Long grp = map.getLongValue("gid");

        if(grp != null) {
            String uri = BASE + "biz/summary?gid=" + grp;
            String ret = getHttpClient().executeGet(uri);
            logger.info(ret);
        }
    }
}
