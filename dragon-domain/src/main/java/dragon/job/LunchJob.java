package dragon.job;

import dragon.utils.BeanFinder;
import dragon.service.Eat;
import dragon.service.EatBean;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class LunchJob extends AbstractJob {

    Eat t = null;

    public LunchJob() {
        super();
    }

    public LunchJob(Eat t) {
        super();
        this.t = t;
    }

    @Override
    protected void processJob(JobExecutionContext ctx) throws JobExecutionException {

        LogFactory.getLog(LunchJob.class).info("job executing...");

        if(t == null){
            t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        }

        t.sendLunchEmail(null);

    }
}
