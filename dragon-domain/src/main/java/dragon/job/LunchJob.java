package dragon.job;

import dragon.utils.BeanFinder;
import dragon.service.BizIntf;
import dragon.service.BizBean;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class LunchJob extends AbstractJob {

    BizIntf t = null;

    public LunchJob() {
        super();
    }

    public LunchJob(BizIntf t) {
        super();
        this.t = t;
    }

    @Override
    protected void processJob(JobExecutionContext ctx) throws JobExecutionException {

        LogFactory.getLog(LunchJob.class).info("job executing...");

        if(t == null){
            t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        }

        JobDataMap map = ctx.getJobDetail().getJobDataMap();
        Long grp = map.getLongValue("gid");

        if(grp == null) {
//            t.sendLunchEmail(null);
        } else {
            t.sendLunchEmail(null, grp);
        }

    }
}
