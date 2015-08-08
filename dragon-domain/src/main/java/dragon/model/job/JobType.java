package dragon.model.job;

import dragon.job.LunchJob;
import dragon.job.WeeklyJob;

/**
 *
 * @author lin.cheng
 */
public enum JobType {
    Pick(LunchJob.class),
    Summary(WeeklyJob.class);

    private Class clzz;

    JobType(Class c) {
        this.clzz = c;
    }

    public Class getClzz() {
        return clzz;
    }
}
