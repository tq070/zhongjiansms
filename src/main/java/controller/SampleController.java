package controller;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import service.MailService;
import util.EmailUtil;
import util.SendMessageUtil;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Random;

@Controller
@EnableAutoConfiguration
public class SampleController {

    private MailService mailService;

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }


    @RequestMapping("/sendMail")
    @ResponseBody
    @CrossOrigin
    ResponseEntity<String> sendMail(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException, MessagingException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        String info = "";
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String companyName = request.getParameter("companyName");
        String email = request.getParameter("email");
        String content = request.getParameter("content");
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(companyName) && StringUtils.isNotBlank(email)) {
            info = "姓名：" + name + " 手机号：" + phone + " 公司名称：" + companyName + " 邮箱：" + email + " 内容：" + content;
            System.out.println("=====" + info);
            EmailUtil.sendEmail("86984642@qq.com", "ceshi", info);
            //mailService.sendSimpleMail("tq070@qq.com","test simple mail"," hello this is simple mail");
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
        String mobile = request.getParameter("phone");;
        String content = "【中交兴路】验证码为" + r + "，请在5分钟内输入，谢谢使用！";
        String account = "ZJWBGYS";
        String password = "4HEXC3tM";
        String sig = "hHzepSbx";
        /**
         * 账号信息
         *   平台编码:ZJWBGYS
         *   平台密码:4HEXC3tM
         *   平台签名:hHzepSbx
         */

        String result = SendMessageUtil.sendMsg(account, password, mobile, content, sig);
        JSONObject object = JSONObject.fromObject(result);
        int status = Integer.parseInt(object.get("status").toString());
        if (status == 0) {
            return ResponseEntity.ok(r + "");
        } else {
            return ResponseEntity.ok("400");
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SampleController.class, args);
    }
}