package dragon.utils;

import dragon.service.EmailQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author lin.cheng
 */
public class QueueHelper {

    Log logger = LogFactory.getLog(EmailQueue.class);

    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private MapMessage message;
    private static Context ctx = null;
    private static ConnectionFactory connectionFactory = null;

    static {
        try {
            ctx = new InitialContext();
            if (ctx != null) {
                connectionFactory = (QueueConnectionFactory) ctx.lookup("jms/NotificationConnectionFactory");
            }
        } catch (NamingException nexp) {
        }
    }

    public QueueHelper() {
        if (ctx == null) {
            initialize();
        }
    }

    public boolean createDeliveryConnection() throws JMSException {
        return createDeliveryConnection(-1);
    }

    public boolean createDeliveryConnection(int timeout) throws JMSException {
        return createConnection(timeout);
    }

    private void initialize() {
        if (ctx == null) {
            try {
                ctx = new InitialContext();
                if (connectionFactory == null) {
                    connectionFactory = (ConnectionFactory) ctx.lookup("jms/NotificationConnectionFactory");
                }
            } catch (NamingException e) {
                logger.error("", e);
            }
        }
    }

    private boolean createConnection(int timeout) throws JMSException {
        boolean isRetry = false;
        int threshold = 10;  // 10 ms

        if (timeout < threshold) {
            threshold = timeout;
        }

        int max_count = timeout / threshold;
        int count = 0;
        messageProducer = null;
        boolean noWaiting = timeout <= 0;

        do {
            try {
                connection = connectionFactory.createConnection();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                isRetry = false;
            } catch (JMSException e) {
                if (messageProducer != null) {
                    messageProducer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
                connection = null;
                session = null;
                messageProducer = null;
                if (!noWaiting) {
                    count++;
                    isRetry = true;
                    System.out.println("Try create connection on " + count);
                    try {
                        Thread.sleep(threshold);
                    } catch (Throwable t) {
                        isRetry = false;
                    }
                } else {
                    isRetry = false;
                    throw e;
                }
            }
        } while (isRetry && count < max_count);

        if (isRetry && count >= max_count) {
            logger.warn("Give up creating connection.");
            return false;
        }

        return true;
    }

    public void initializeMessage() throws JMSException, NamingException {
        clear();
        message = session.createMapMessage();
    }

    public void initializeQueue(String q) throws JMSException, NamingException {
        Queue emailQueue = getQueue(ctx, q);
        messageProducer = session.createProducer(emailQueue);
    }

    private Queue getQueue(Context ctx, String name) throws NamingException {
        return (Queue) ctx.lookup(name);
    }

    public void addParameter(String name, String value) throws JMSException {
        message.setString(name, value);
    }

    public void addParameter(String name, int value) throws JMSException {
        message.setInt(name, value);
    }

    public void addParameter(String name, long value) throws JMSException {
        message.setLong(name, value);
    }

    public void addParameter(String name, boolean flag) throws JMSException {
        message.setBoolean(name, flag);
    }

    public String getParameter(String name) throws JMSException {
        return message.getString(name);
    }

    public void clear() throws JMSException {
        if (message != null) {
            message.clearBody();
            message.clearProperties();
        }
    }

    public void sendMsg() throws JMSException {
        if (messageProducer != null) {
            messageProducer.send(message);
        } else {
            logger.warn("MessageProducer not found.");
        }
    }

    public void close() throws JMSException, NamingException {

        if (messageProducer != null) {
            messageProducer.close();
        }

        if (session != null) {
            session.close();
        }

        if (connection != null) {
            connection.close();
        }
    }
}
