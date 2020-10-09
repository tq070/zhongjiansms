package util;

import java.util.Map;

public class HttpSendUtils {

	public static String send(String url, Map<String, String> mapHeader,
			Map<String, String> mapBodys) {

		String result = HttpClientUtils.doPost(url, mapHeader, mapBodys,
				"UTF-8");
		return result;
	}

	public static String send(String url, Map<String, String> mapHeaders,
			String jsonBodys) {
		String result = HttpClientUtils.doPost(url, mapHeaders, jsonBodys,
				"UTF-8");
		return result;
}
}
