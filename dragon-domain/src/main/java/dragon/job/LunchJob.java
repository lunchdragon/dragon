package dragon.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class LunchJob extends AbstractJob {

    static Log logger = LogFactory.getLog(LunchJob.class);

    public LunchJob() {
        super();
    }

    @Override
    protected void processJob(JobExecutionContext ctx) throws Exception {

        LogFactory.getLog(LunchJob.class).info("job executing...");

        JobDataMap map = ctx.getJobDetail().getJobDataMap();
        Long grp = map.getLongValue("gid");

        if(grp != null) {
            String uri = BASE + "biz/what?gid=" + grp + "&notify=true";
            String ret = getHttpClient().executeGet(uri);
            logger.info(ret);
        }

    }
}
