package dragon.job;

import dragon.model.job.JobType;
import dragon.model.job.Schedule;
import dragon.service.BizBean;
import dragon.service.BizIntf;
import dragon.utils.BeanFinder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lin.cheng on 8/7/15.
 */
public class CustJob extends AbstractJob {

    static final String DEFAULT_GRP = "DEFAULT";

    static Log logger = LogFactory.getLog(CustJob.class);
    static Map<Long, Long> lastMap = new ConcurrentHashMap<Long, Long>();
    static Map<Long, Long> newMap = new ConcurrentHashMap<Long, Long>();

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
        newMap = new HashMap<Long, Long>();

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
            newMap.put(sid, ver);

            if (last != null && last.equals(ver)) {//No change
                continue;
            }

            JobDetail jobDetail = JobBuilder.newJob(jt.getClzz())
                    .withIdentity(sid.toString(), DEFAULT_GRP)
                    .storeDurably(false)
                    .usingJobData("gid", gid)
                    .usingJobData("params", params)
                    .build();

            cron = getRandomCron(cron);
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

            logger.info(jt.name() + " -> " + gid + " rescheduled:" + cron);
            newMap.put(sid, ver);
        }

        for (Long key:lastMap.keySet()){
            if(!newMap.containsKey(key)){
                logger.info("Removing schedule: " + key + " because it was deleted or no longer active.");
                ctx.getScheduler().deleteJob(new JobKey(key+"", DEFAULT_GRP));
            }
        }

        lastMap = newMap;
    }

    private String getRandomCron(String src){
        String[] ss = src.split(" ");
        Date d = new Date();
        d.setHours(Integer.parseInt(ss[2]));
        d.setMinutes(Integer.parseInt(ss[1]));
        d.setSeconds(Integer.parseInt(ss[0]));

        long rdm = Math.abs(new Random().nextLong()) % (1000*60*15-1);
        Date d2 = new Date(d.getTime() - 1000*60*30 + rdm);
        ss[0] = String.valueOf(d2.getSeconds());
        ss[1] = String.valueOf(d2.getMinutes());
        ss[2] = String.valueOf(d2.getHours());

        String ret = StringUtils.join(ss, " ");
        return ret;
    }
}
