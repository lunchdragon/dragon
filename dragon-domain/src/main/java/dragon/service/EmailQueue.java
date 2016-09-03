package dragon.service;

import dragon.comm.MailSender;
import dragon.utils.ConfigHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.MessageDriven;
import javax.jms.MapMessage;
import javax.jms.Message;

/**
 * Created by lin.cheng on 6/29/15.
 */
@MessageDriven(mappedName = "jms/EmailQueue")
public class EmailQueue {

    Log logger = LogFactory.getLog(EmailQueue.class);

    public void onMessage(Message inMessage) {
        MapMessage msg = (MapMessage) inMessage;
        try {
            String to = msg.getString("to");
            String title = msg.getString("title");
            String body = msg.getString("body");

            logger.info("Sending email: " + title + " -> " + to);

            String server = ConfigHelper.instance().getConfig("mail");
            String port = ConfigHelper.instance().getConfig("mailport");
            String user = ConfigHelper.instance().getConfig("mailuser");
            String pwd = ConfigHelper.instance().getConfig("mailpwd");
            if(pwd.startsWith(ConfigHelper.EN_PF)){
                BizIntf t = new BizBean();
                pwd = t.getSecret(pwd);
            }

            MailSender ms = new MailSender(server, Integer.parseInt(port), user, pwd, true);
            ms.sendHtmlContent(to, "", user, title, body);

        } catch (Exception e) {
            logger.error("", e);
        }

    }
}
