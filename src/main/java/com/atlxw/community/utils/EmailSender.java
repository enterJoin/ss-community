package com.atlxw.community.utils;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * 发送邮件的工具类
 */
public class EmailSender {
    public static void sendEmail(String title,String content,String receiver) throws Exception{
        Properties prop = new Properties();
        prop.setProperty("mail.host", "smtp.qq.com");
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");

        //有些邮箱的服务 需要开启SSL的安全认证，因此加上这个SSL认证！
        final String smtpPort = "465";
        prop.setProperty("mail.smtp.port", smtpPort);
        prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.setProperty("mail.smtp.socketFactory.fallback", "false");
        prop.setProperty("mail.smtp.socketFactory.port", smtpPort);


        //使用JavaMail发送邮件的5个步骤
        //1、创建session
        Session session = Session.getInstance(prop);
        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);
        //2、通过session得到transport对象
        Transport ts = session.getTransport();
        //3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
        ts.connect("smtp.qq.com", "1578042115@qq.com", "jckurjbzwirsfhia");
        //4、创建邮件
        Message message = createSimpleMail(session,title,content,receiver);
        //5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    /**
     * 创建邮件
     * @param session
     * @param title
     * @param content
     * @param receiver
     * @return
     * @throws Exception
     */
    private static MimeMessage createSimpleMail(Session session, String title, String content, String receiver) throws Exception {
        //1、创建邮件对象
        MimeMessage message = new MimeMessage(session);
        //2、指明邮件的发件人
        message.setFrom(new InternetAddress("1578042115@qq.com"));
        //3、指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
        //4、邮件的标题
        message.setSubject(title);
        //5、邮件的文本内容
        message.setContent(content, "text/html;charset=UTF-8");
        //6、设置发件时间
        message.setSentDate(new Date());
        //7、保存设置
        message.saveChanges();

        //返回创建好的邮件对象
        return message;
    }
}
