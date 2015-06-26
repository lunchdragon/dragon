package dragon.job;

import dragon.BeanFinder;
import dragon.service.bar;
import dragon.service.barBean;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class LunchJob extends AbstractJob {

    bar t = null;

    public LunchJob() {
        super();
    }

    public LunchJob(bar t) {
        super();
        this.t = t;
    }

    @Override
    protected void processJob(JobExecutionContext ctx) throws JobExecutionException {

        LogFactory.getLog(LunchJob.class).info("job excuting...");

        if(t == null){
            t = BeanFinder.getInstance().getLocalSessionBean(barBean.class);
        }

        t.sendLunchEmail(null);

    }
}
