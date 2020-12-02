package zhongjian.util;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendMessageUtil {
    //http请求发送相同短信
    public static String sendMsg(String account, String passwd, String mobile, String content, String sig) throws Throwable {

        String url = "http://sms.sinoiov.com/NotificationService/rest/v1/message/sender";
//		String url="http://localhost:8080/NotificationService/rest/v1/message/sender";
        Map<String, String> mapHeader = new HashMap<String, String>();
        //账号
        mapHeader.put("account", account);
        //密码
        String password = MD5Utils.getDefaultMd5String(passwd);
        System.out.println(password);
        mapHeader.put("password", password);
        //请求ID
        mapHeader.put("requestId", "SMS" + System.currentTimeMillis());
        Map<String, String> jsonBody = new HashMap<String, String>();
        //手机号
        jsonBody.put("mobile", mobile);
        //短信内容
        jsonBody.put("content", content);
        //签名认证
        String mac = mobile + "_" + passwd + "" + account + "" + sig;
        jsonBody.put("mac", MD5Utils.getDefaultMd5String(mac));
        //请不要使用map.toString()方法.
        String jsonString = net.sf.json.JSONObject.fromObject(jsonBody).toString();
        JSONObject.toJSONString(mapHeader);
        System.out.println(jsonString);
        String result = HttpSendUtils.send(url, mapHeader, jsonString);
        System.out.println("testSendMsg-->" + result);
        return result;
    }
}
