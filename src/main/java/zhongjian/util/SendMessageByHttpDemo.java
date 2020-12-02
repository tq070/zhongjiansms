package zhongjian.util;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendMessageByHttpDemo {
    public static void main(String[] args) throws Throwable {
        //手机号
        String mobile="13810525644";
        String content="您正在密码找回，验证码为2341，请在5分钟内输入，谢谢使用！";
        String account="ZJWBGYS";
        String password="4HEXC3tM";
        String sig="hHzepSbx";
        /**
         * 账号信息
         *   平台编码:ZJWBGYS
         *   平台密码:4HEXC3tM
         *   平台签名:hHzepSbx
         */

        testSendMsg(account, password, mobile, content, sig);
    }
    //http请求发送相同短信
    public static void testSendMsg(String account, String passwd, String mobile, String content, String sig) throws Throwable {

        String url="http://sms.sinoiov.com/NotificationService/rest/v1/message/sender";
//		String url="http://localhost:8080/NotificationService/rest/v1/message/sender";
        Map<String, String> mapHeader = new HashMap<String, String>();
        //账号
        mapHeader.put("account", account);
        //密码
        String password = MD5Utils.getDefaultMd5String(passwd);
        System.out.println(password);
        mapHeader.put("password",password );
        //请求ID
        mapHeader.put("requestId", "SMS" + System.currentTimeMillis());
        Map<String, String> jsonBody = new HashMap<String, String>();
        //手机号
        jsonBody.put("mobile", mobile);
        //短信内容
        jsonBody.put("content", content);
        //签名认证
        String mac = mobile + "_" + passwd + "" + account + ""+sig;
        jsonBody.put("mac", MD5Utils.getDefaultMd5String(mac));
        //请不要使用map.toString()方法.
        String jsonString = net.sf.json.JSONObject.fromObject(jsonBody).toString();
        JSONObject.toJSONString(mapHeader);
        System.out.println(jsonString);
        String result = HttpSendUtils.send(url, mapHeader,jsonString);
        System.out.println("testSendMsg-->" + result);
    }
}
