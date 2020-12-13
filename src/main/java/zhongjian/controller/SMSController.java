package zhongjian.controller;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zhongjian.service.MailService;
import zhongjian.util.EmailUtil;
import zhongjian.util.LocalCacheUtil;
import zhongjian.util.MapCacheUtil;
import zhongjian.util.SendMessageUtil;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Random;

@Controller
@EnableAutoConfiguration
public class SMSController {
    /**
     * 账号信息
     * 平台编码:ZJWBGYS
     * 平台密码:4HEXC3tM
     * 平台签名:hHzepSbx
     */
    private String account = "ZJWBGYS";
    private String password = "4HEXC3tM";
    private String sig = "hHzepSbx";
    private LocalCacheUtil localCacheUtil = new LocalCacheUtil();

    //    private MailService mailService;
    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }


    @RequestMapping("/sendMail")
    @ResponseBody
    @CrossOrigin
    ResponseEntity<String> sendMail(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        System.out.println("========================");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        String info = "";
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String companyName = request.getParameter("companyName");
        String email = request.getParameter("email");
        String content = request.getParameter("content");
        System.out.println("=====" + content);
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(companyName) && StringUtils.isNotBlank(email)) {
            /**
             * 然后作为用户是点击验证码，手机收到：验证码，点击预约，
             * 手机收到：【中交兴路】预约成功！感谢您对中交兴路的关注，我们将尽快与您联系。如想了解更多中交兴路动态，请关注我们的微信公众号：中交兴路（sinoiov2004）或直戳链接关注http://r6e.cn/e8uL；
             * 作为甲方，当用户点击完预约，发送预约信息：“您收到一条来自官网的新的业务问询，内容如下：姓名，手机，xxxxx。”至marketing@sinoiov.com
             */
            // 下面内容为邮件发送内容，后期可更改
            info = "您收到一条来自官网的新的业务问询，内容如下：姓名：" + name + " 手机号：" + phone + " 公司名称：" + companyName + " 邮箱：" + email + " 内容：" + content;
            System.out.println("=====" + info);
            /**
             * sendEmail
             * @param toUser 收件人
             * @param title 邮件主题
             * @param content 邮件内容
             * @throws MessagingException
             * @throws GeneralSecurityException
             */
            EmailUtil.sendEmail("marketing@sinoiov.com", "来自官网的新的业务问询", info);
            String smsContent = "【中交兴路】预约成功！感谢您对中交兴路的关注，我们将尽快与您联系。如想了解更多中交兴路动态，请关注我们的微信公众号：中交兴路（sinoiov2004）或直戳链接关注http://r6e.cn/e8uL；";
            SendMessageUtil.sendMsg(account, password, phone, smsContent, sig);
            return ResponseEntity.ok("200");
        } else {
            return ResponseEntity.ok("400");
        }
    }

    @RequestMapping("/sendSMS")
    @ResponseBody
    ResponseEntity<String> sendSMS(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        //手机号
        Random random = new Random();
        int r = random.nextInt(9999);
        String mobile = request.getParameter("phone");

        String content = "验证码为" + r + "，请在5分钟内输入，谢谢使用！";
        String result = SendMessageUtil.sendMsg(account, password, mobile, content, sig);
        JSONObject object = JSONObject.fromObject(result);

        localCacheUtil.putValue(mobile, r, 300);
        int status = Integer.parseInt(object.get("status").toString());
        if (status == 0) {
            return ResponseEntity.ok(String.valueOf(r));
        } else {
            return ResponseEntity.ok("400");
        }
    }

    @RequestMapping("/getSMS")
    @ResponseBody
    ResponseEntity<String> getSMS(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        String mobile = request.getParameter("phone");
        String code = request.getParameter("code");
        if (StringUtils.isNotBlank(mobile) && StringUtils.isNotBlank(code)) {
            if (localCacheUtil != null && localCacheUtil.getValue(mobile) != null) {
                String value = localCacheUtil.getValue(mobile).toString();
                if (value.equals(code)) {
                    return ResponseEntity.ok("200");
                }
            }
        }
        return ResponseEntity.ok("400");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SMSController.class, args);
    }
}