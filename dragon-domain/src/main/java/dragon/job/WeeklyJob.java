package dragon.job;

import dragon.model.food.Stat;
import dragon.service.Eat;
import dragon.service.EatBean;
import dragon.utils.BeanFinder;
import dragon.utils.QueueHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;

/**
 * Created by Lin on 2015/7/11.
 */
public class WeeklyJob extends AbstractJob {

    static Log logger = LogFactory.getLog(WeeklyJob.class);

    Eat t = null;

    public WeeklyJob() {
        super();
    }

    public WeeklyJob(Eat t) {
        super();
        this.t = t;
    }

    @Override
    protected void processJob(JobExecutionContext ctx) throws JobExecutionException {

        logger.info("job executing...");

        if(t == null){
            t = BeanFinder.getInstance().getLocalSessionBean(EatBean.class);
        }

        send();
    }

    public void send() {

        String mails = t.getMails();

        if (StringUtils.isNotEmpty(mails)) {

            String[] mailArr = mails.split(",");
            QueueHelper qh = new QueueHelper();

            try {
                qh.createDeliveryConnection(100);
                qh.initializeMessage();
                qh.initializeQueue("jms/EmailQueue");
                qh.addParameter("title", "Summary");
                qh.addParameter("body", buildBody());

                for (String mail : mailArr) {
                    qh.addParameter("to", mail);
                    qh.sendMsg();
                }
            } catch (Exception e) {
                logger.error("", e);
            } finally {
                if (qh != null) {
                    try {
                        qh.close();
                    } catch (Exception ex) {
                        logger.error("", ex);
                    }
                }
            }
        }
    }

    private String buildBody(){

        StringBuilder sb = new StringBuilder();

        Map<String, Stat> ss = t.stat2(7);
        sb.append(buildTable(ss, "[Last 7 Days]"));

        ss = t.stat2(30);
        sb.append(buildTable(ss, "[Last 30 Days]"));

        ss = t.stat(0, true);
        sb.append(buildTable(ss, "[All Time]"));

        return sb.toString();
    }

    private String buildTable(Map<String, Stat> ss, String title){
        StringBuilder sb = new StringBuilder();

        sb.append("<h3>" + title +  "</h3>");
        sb.append("<table border=\"1px\" cellspacing=\"1px\" style=\"border-collapse:collapse\"  width=\"800\">");
        sb.append("<tr><td>name</td><td>factor</td><td>score</td><td>visited</td><td>liked</td><td>disliked</td><td>vetoed</td></tr>");
        for(Stat s:ss.values()){
            sb.append("<tr>");
            sb.append("<td>" +  s.getName() + "</td>");
            sb.append("<td>" +  s.getFactor() + "</td>");
            sb.append("<td>" +  s.getScore() + "</td>");
            sb.append("<td>" +  s.getVisited() + "</td>");
            sb.append("<td>" +  s.getLiked() + "</td>");
            sb.append("<td>" +  s.getDisliked() + "</td>");
            sb.append("<td>" +  s.getVetoed() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }

    public static void main(String[] args){
        WeeklyJob j = new WeeklyJob(new EatBean());
        String s = j.buildBody();
        System.out.println(s);
    }
}