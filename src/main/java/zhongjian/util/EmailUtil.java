package zhongjian.util;

import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class EmailUtil {
    /**
     *
     * @param toUser
     * @param title
     * @param content
     * @throws MessagingException
     * @throws GeneralSecurityException
     */
    public static void sendEmail(String toUser, String title, String content) throws MessagingException, GeneralSecurityException {
        System.out.println("========================"+content);
        String userName = "86984642@qq.com";//发件邮箱
        String password = "ctoputslvcgqbhfa";//密码
        String smtp = "smtp.qq.com";//smtp地址
        //创建一个配置文件并保存
        Properties properties = new Properties();
        //下面是qq的配置方式，请更换为企业邮箱
        properties.setProperty("mail.host", "smtp.qq.com");

        properties.setProperty("mail.transport.protocol", "smtp");

        properties.setProperty("mail.smtp.auth", "true");


        //QQ存在一个特性设置SSL加密
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);

        //创建一个session对象
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });

        //开启debug模式
        session.setDebug(true);

        //获取连接对象
        Transport transport = session.getTransport();

        //连接服务器
        transport.connect(smtp, password, password);

        //创建邮件对象
        MimeMessage mimeMessage = new MimeMessage(session);

        //邮件发送人
        mimeMessage.setFrom(new InternetAddress(userName));

        //邮件接收人
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toUser));

        //邮件标题
        mimeMessage.setSubject(title);

        //邮件内容
        mimeMessage.setContent(content, "text/html;charset=UTF-8");

        //发送邮件
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

        //关闭连接
        transport.close();
    }


//    public static void main(String[] args) throws MessagingException, GeneralSecurityException {
//        sendEmail("86984642@qq.com", "ceshi", "ceshi");
//    }
}
