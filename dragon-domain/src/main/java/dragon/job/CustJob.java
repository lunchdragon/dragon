package dragon.job;

import dragon.model.job.JobType;
import dragon.model.job.Schedule;
import dragon.service.BizBean;
import dragon.service.BizIntf;
import dragon.utils.BeanFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by lin.cheng on 8/7/15.
 */
public class CustJob extends AbstractJob {

    static final String DEFAULT_GRP = "DEFAULT";

    static Log logger = LogFactory.getLog(CustJob.class);
    static Map<Long, Long> lastMap = new Hashtable<Long, Long>();//thread safe

    BizIntf t = null;

    public CustJob() {
        super();
    }

    public CustJob(BizIntf t) {
        super();
        this.t = t;
    }

    protected void processJob(JobExecutionContext ctx) throws SchedulerException {

        if (t == null) {
            t = BeanFinder.getInstance().getLocalSessionBean(BizBean.class);
        }

        List<Schedule> ss = t.getSchedules("active = true");

        for (Schedule s : ss) {
            Long sid = s.getId();
            Long ver = s.getModified();
            String params = s.getParam();
            String cron = s.getCron();
            Long gid = s.getGid();
            JobType jt = JobType.valueOf(s.getType());

            if (jt == null) {
                logger.error("Invalid job type: " + s.getType());
                continue;
            }

            Long last = lastMap.get(sid);

            if (last != null && last.equals(ver)) {//No change
                continue;
            }

            JobDetail jobDetail = JobBuilder.newJob(jt.getClzz())
                    .withIdentity(sid.toString(), DEFAULT_GRP)
                    .storeDurably(false)
                    .usingJobData("gid", gid)
                    .usingJobData("params", params)
                    .build();


            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("t_" + sid.toString(), DEFAULT_GRP)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .forJob(jobDetail)
                    .build();

            if (ctx.getScheduler().getJobDetail(jobDetail.getKey()) != null) {
                ctx.getScheduler().rescheduleJob(trigger.getKey(), trigger);
            } else {
                ctx.getScheduler().scheduleJob(jobDetail, trigger);
            }

            logger.info(jt.name() + " -> " + gid + " rescheduled.");
            lastMap.put(sid, ver);
        }

    }
}
