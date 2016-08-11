package dragon.comm;

import org.apache.commons.lang.StringUtils;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class MailSender {

    private String host = "localhost";
    private String user = null;
    private String password = null;
    private int port = 0;
    private boolean tlsFlag = false;
    private String errorMessage = null;

    public MailSender(String host, int port, String user, String password, boolean tlsFlag) {
        if(host != null) {
            this.host = host;
            if(port > 0) {
                this.port = port;
            }
        }
        if(user != null) {
            this.user = user;
            if(password != null) {
                this.password = password;
            }
        }
        this.tlsFlag = tlsFlag;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void sendTextContent(String to, String cc, String from, String subject, String content) throws IOException {
        Session session = startSession();
        try {
            Message msg = new MimeMessage(session);
            setHeader(msg, to, cc, from, subject);
            setTextContent(msg, content);
            sendEmail(session, msg);
        } catch (MessagingException mex) {
            emailExceptionHandler(mex);
        }
    }

    public void sendMultipartContent(String to, String cc, String from, String subject, String content) throws IOException {
        Session session = startSession();
        try {
            Message msg = new MimeMessage(session);
            setHeader(msg, to, cc, from, subject);
            setMultipartContent(msg, content);
            sendEmail(session, msg);
        } catch (MessagingException mex) {
            emailExceptionHandler(mex);
        }
    }

    public void sendAttachmentContent(String to, String cc, String from, String subject, String explanation, String fileName) throws IOException {
        Session session = startSession();
        try {
            Message msg = new MimeMessage(session);
            setHeader(msg, to, cc, from, subject);
            setFileAsAttachment(msg, explanation, fileName);
            sendEmail(session, msg);
        } catch (MessagingException mex) {
            emailExceptionHandler(mex);
        }
    }

    public void sendHtmlContent(String to, String cc, String from, String subject, String content) throws IOException, MessagingException {
        Session session = startSession();
        try {
            Message msg = new MimeMessage(session);
            setHeader(msg, to, cc, from, subject);
            setHTMLContent(msg, content);
            sendEmail(session, msg);
        } catch (MessagingException mex) {
            emailExceptionHandler(mex);
            throw mex;
        }
    }

    private Session startSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.auth", "true");

        if (port > 0) {
            props.put("mail.smtp.port", port);
        }
//        if (user != null) {
//            props.put("mail.smtp.user", user);
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.password", password);
//        }
        if (tlsFlag) {
            props.put("mail.smtp.starttls.enable", true);
        }
        //props.put("mail.debug", "true");
//        Session session = Session.getInstance(props);
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });
        return session;
    }

    private void sendEmail(Session session, Message msg) throws NoSuchProviderException, MessagingException {
        Transport tr = null;
        try {
            tr = session.getTransport("smtp");
            if (user != null && port > 0) {
                tr.connect(host, port, user, password);
            } else if (user != null) {
                tr.connect(host, user, password);
            } else {
                tr.connect();
            }
            msg.saveChanges();      // don't forget this

            if (msg.getAllRecipients() == null || msg.getAllRecipients().length == 0) {
                return;
            }
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
            tr.sendMessage(msg, msg.getAllRecipients());
        } finally {
            if (tr != null) {
                tr.close();
            }
        }
    }

    private void setHeader(Message msg, String to, String cc, String from, String subject) throws MessagingException {
        if (from == null) {
            msg.setFrom();
        } else {
            msg.setFrom(new InternetAddress(from));
        }
        to = to.replaceAll(";", ",");
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to, false));

        if (cc != null && cc.length() > 0) {
            cc = cc.replaceAll(";", ",");
            msg.setRecipients(Message.RecipientType.BCC,
                    InternetAddress.parse(cc, true));
        }

        msg.setSubject(subject);
        msg.setSentDate(new Date());

        msg.saveChanges();
    }

    private void setBcc(Message msg, String bcc) throws MessagingException{
        if (bcc != null && bcc.length() > 0) {
            bcc = bcc.replaceAll(";", ",");
            msg.setRecipients(Message.RecipientType.BCC,
                    InternetAddress.parse(bcc, true));
        }
        msg.saveChanges();
    }

    // A simple, single-part text/plain e-mail.
    private void setTextContent(Message msg, String content) throws MessagingException {
        msg.setContent(content, "text/plain;charset=utf-8");
        msg.saveChanges();
    }

    private void appendErrorMessage(Exception e) {
        String className = e.getClass().getSimpleName();
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            if (className.equals("AuthenticationFailedException")) {
                message = "Invalid username or password";
            }
        }

        String mesg = className + ": " + message;
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = mesg;
        } else {
            errorMessage += "\n" + mesg;
        }
    }

    private void emailExceptionHandler(MessagingException mex) {
        mex.printStackTrace();

        String mesg = mex.getClass().getSimpleName() + ": " + mex.getMessage();

        appendErrorMessage(mex);

        while (mex.getNextException() != null) {
            // Get next exception in chain
            Exception ex = mex.getNextException();

            mesg = ex.getClass().getSimpleName() + ": " + ex.getMessage();

            appendErrorMessage(ex);

            if (!(ex instanceof MessagingException)) {
                break;
            } else {
                mex = (MessagingException) ex;
            }
        }

    }

    // A simple multipart/mixed e-mail. Both body parts are text/plain.
    private void setMultipartContent(Message msg, String content) throws MessagingException {
        // Create and fill first part
        MimeBodyPart p1 = new MimeBodyPart();
        p1.setText("this is passed in " + content);

        // Create and fill second part
        MimeBodyPart p2 = new MimeBodyPart();
        p2.setText("This is the second part", "us-ascii");

        // Create the Multipart.  Add BodyParts to it.
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(p1);
        mp.addBodyPart(p2);

        // Set Multipart as the message's content
        msg.setContent(mp);
    }

    // Set a file as an attachment.  Uses JAF FileDataSource.
    private void setFileAsAttachment(Message msg, String explanation, String filename) throws MessagingException {
        // Create and fill first part
        Multipart mp = new MimeMultipart();
        MimeBodyPart p1 = new MimeBodyPart();
        p1.setText(explanation);
        mp.addBodyPart(p1);

        // Put a file in the second part
        if (filename != null) {
            MimeBodyPart p2 = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(filename) {

                @Override
                public String getContentType() {
                    return "application/octet-stream";
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(getFile());
                }
            };
            p2.setDataHandler(new DataHandler(fds));
            p2.setFileName(fds.getName());
            mp.addBodyPart(p2);
        }

        // Set Multipart as the message's content
        msg.setContent(mp);
    }

    // Set a single part html content.
    // Sending data of any type is similar.
    private void setHTMLContent(Message msg, String content) throws MessagingException {
        Multipart mp = new MimeMultipart();
        MimeBodyPart bodypart = new MimeBodyPart();
        content = hrefStr(content);
        bodypart.setDataHandler(new DataHandler(new HTMLDataSource(content)));
        mp.addBodyPart(bodypart);
        msg.setContent(mp);
    }

    /*
     * Inner class to act as a JAF datasource to send HTML e-mail content
     */
    static class HTMLDataSource implements DataSource {

        private String html;

        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        // Return html string in an InputStream.
        // A new stream must be returned each time.
        public InputStream getInputStream() throws IOException {
            if (html == null) {
                throw new IOException("Null HTML");
            }
            return new ByteArrayInputStream(html.getBytes());
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        public String getContentType() {
            return "text/html";
        }

        public String getName() {
            return "text/html dataSource";
        }
    }

    private String hrefStr(String src) {
        if (StringUtils.isBlank(src)) {
            return src;
        }

        src = src.trim();
        String[] arr = src.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String s : arr) {
            String tmp = s.trim().toLowerCase();
            if (tmp.startsWith("http://") || tmp.startsWith("https://")) {
                sb.append("<a href=\'").append(tmp).append("\'/>").append(s).append("</a>");
            } else {
                sb.append(s);
            }
            sb.append(" ");
        }

        return sb.toString();
    }

} //End of class

