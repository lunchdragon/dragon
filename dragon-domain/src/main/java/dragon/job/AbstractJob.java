package dragon.job;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by lin.cheng on 6/18/15.
 */
public abstract class AbstractJob implements Job {

    public final void execute(JobExecutionContext arg0) throws JobExecutionException {
        try {
            processJob(arg0);
        } catch (Exception ex) {
            Logger.getLogger(AbstractJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected abstract void processJob(JobExecutionContext ctx) throws JobExecutionException;
}
