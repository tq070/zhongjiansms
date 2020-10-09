package util;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

/**
 *请求工具类
 * @author SPZ
 *
 */
public final class HttpClientUtils {

	private final static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
	
	private final static RequestConfig requestConfig;
	
	private final static int MAX_TIMEOUT = 15 * 1000;
	
	static {  
        // 设置连接池
		poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(createRegistry());
        // 设置连接池大小
		poolingHttpClientConnectionManager.setMaxTotal(100);
		poolingHttpClientConnectionManager.setDefaultMaxPerRoute(poolingHttpClientConnectionManager.getMaxTotal());
		
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        //客户端在向服务器发送数据的时候，不需要先向服务器发起一个请求看服务器是否愿意接受客户端将要发送的数据
        requestConfigBuilder.setExpectContinueEnabled(false);
        //设置Cookie策略
        requestConfigBuilder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
        // 设置连接超时
        requestConfigBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        requestConfigBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        requestConfigBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        requestConfig = requestConfigBuilder.build();
    }
	
	public final static byte[] doGet(String url, Map<String, String> headers) {
		long start = System.currentTimeMillis();
		byte[] returnVal = null;
        CloseableHttpClient closeableHttpClient = null;
        try {
        	BasicHeader[] basicHeaders = packageBasicHeaders(headers);
        	closeableHttpClient = createCloseableHttpClient(url);
            HttpGet HttpGet = new HttpGet(url);
            HttpGet.setHeaders(basicHeaders);
            HttpResponse response = closeableHttpClient.execute(HttpGet);
            System.out.println("HTTP响应数据[" + response + "]");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                returnVal = EntityUtils.toByteArray(entity);
            } else {
            	System.out.println("HTTP响应状态不正常【" + response.getStatusLine().getStatusCode() + "】");
            }
            HttpGet.abort();
        } catch (Throwable t) {
        	t.printStackTrace();
        } finally {
//            if (closeableHttpClient != null) {
//                try {
//                	closeableHttpClient.close();
//                } catch (Throwable t) {
//                	logger.error("关闭HTTP连接时发生异常", t);
//                }
//            }
        	long end = System.currentTimeMillis();
        	System.out.println("HTTP请求完毕，耗时[" + (end - start) + "]ms");
        }
        return returnVal;
	}
	
	public final static String doPost(String url, Map<String, String> headers, Map<String, String> bodys, String fileAttributeName, String fileURL, String charset) {
		long start = System.currentTimeMillis();
		String returnVal = "";
		CloseableHttpClient closeableHttpClient = null;
		InputStream inputStream = null;
        try {
        	String[] fileURLSplit = fileURL.split("/");
        	String fileName = fileURLSplit[fileURLSplit.length - 1];
        	URLConnection urlConnection = new URL(fileURL).openConnection();
        	urlConnection.connect();
        	inputStream = urlConnection.getInputStream();
        	
        	BasicHeader[] basicHeaders = packageBasicHeaders(headers);
        	closeableHttpClient = createCloseableHttpClient(url);
        	MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addPart(fileAttributeName, new InputStreamBody(inputStream, ContentType.MULTIPART_FORM_DATA, fileName));
            for (Iterator<Entry<String, String>> it = bodys.entrySet().iterator(); it.hasNext();) {
                Entry<String, String> entry = it.next();
                multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue());
            }
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeaders(basicHeaders);
            httpPost.setEntity(multipartEntityBuilder.build());
            HttpResponse response = closeableHttpClient.execute(httpPost);
            System.out.println("HTTP响应数据[" + response + "]");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                returnVal = EntityUtils.toString(entity, Charset.forName(charset));
            } else {
            	System.out.println("HTTP响应状态不正常【" + response.getStatusLine().getStatusCode() + "】");
            }
            httpPost.abort();
        } catch (Throwable t) {
        	t.printStackTrace();
        } finally {
        	if (inputStream != null) {
        		try {
        			inputStream.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
        	}
//			if (closeableHttpClient != null) {
//				try {
//				  	closeableHttpClient.close();
//				} catch (Throwable t) {
//				  	logger.error("关闭HTTP连接时发生异常", t);
//				}
//			}
        	long end = System.currentTimeMillis();
        	System.out.println("HTTP请求完毕，耗时[" + (end - start) + "]ms");
        }
		return returnVal;
	}
	
	public final static String doPost(String url, Map<String, String> headers, Map<String, String> bodys, String fileAttributeName, byte[] fileBytes, String charset) {
		long start = System.currentTimeMillis();
		String returnVal = "";
		CloseableHttpClient closeableHttpClient = null;
        try {
        	BasicHeader[] basicHeaders = packageBasicHeaders(headers);
        	closeableHttpClient = createCloseableHttpClient(url);
        	MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addPart(fileAttributeName, new ByteArrayBody(fileBytes, ContentType.MULTIPART_FORM_DATA, UUID.randomUUID().toString()));
            for (Iterator<Entry<String, String>> it = bodys.entrySet().iterator(); it.hasNext();) {
                Entry<String, String> entry = it.next();
                multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue());
            }
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeaders(basicHeaders);
            httpPost.setEntity(multipartEntityBuilder.build());
            HttpResponse response = closeableHttpClient.execute(httpPost);
            System.out.println("HTTP响应数据[" + response + "]");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                returnVal = EntityUtils.toString(entity, Charset.forName(charset));
            } else {
            	System.out.println("HTTP响应状态不正常【" + response.getStatusLine().getStatusCode() + "】");
            }
            httpPost.abort();
        } catch (Throwable t) {
        	t.printStackTrace();
        } finally {
//			if (closeableHttpClient != null) {
//				try {
//				  	closeableHttpClient.close();
//				} catch (Throwable t) {
//				  	logger.error("关闭HTTP连接时发生异常", t);
//				}
//			}
        	long end = System.currentTimeMillis();
        	System.out.println("HTTP请求完毕，耗时[" + (end - start) + "]ms");
        }
		return returnVal;
	}
	
	public final static String doPost(String url, Map<String, String> headers, Map<String, String> bodys, String charset) {
		long start = System.currentTimeMillis();
		String returnVal = "";
        CloseableHttpClient closeableHttpClient = null;
        try {
        	BasicHeader[] basicHeaders = packageBasicHeaders(headers);
            List<BasicNameValuePair> nvps = packageBasicNameValuePairs(bodys);
            closeableHttpClient = createCloseableHttpClient(url);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeaders(basicHeaders);
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
            HttpResponse response = closeableHttpClient.execute(httpPost);
            System.out.println("HTTP响应数据[" + response + "]");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                returnVal = EntityUtils.toString(entity, Charset.forName(charset));
            } else {
            	System.out.println("HTTP响应状态不正常【" + response.getStatusLine().getStatusCode() + "】");
            }
            httpPost.abort();
        } catch (Throwable t) {
        	t.printStackTrace();
        } finally {
//            if (closeableHttpClient != null) {
//                try {
//                	closeableHttpClient.close();
//                } catch (Throwable t) {
//                	logger.error("关闭HTTP连接时发生异常", t);
//                }
//            }
        	long end = System.currentTimeMillis();
        	System.out.println("HTTP请求完毕，耗时[" + (end - start) + "]ms");
        }
        return returnVal;
    }

	public final static String doPost(String url, Map<String, String> headers, String bodys, String charset) {
		long start = System.currentTimeMillis();
		String returnVal = "";
        CloseableHttpClient closeableHttpClient = null;
        try {
        	//System.out.println(bodys);
        	//System.out.println(url);
        	BasicHeader[] basicHeaders = packageBasicHeaders(headers);
        	closeableHttpClient = createCloseableHttpClient(url);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeaders(basicHeaders);
            
            httpPost.setEntity(new StringEntity(bodys, charset));
            HttpResponse response = closeableHttpClient.execute(httpPost);
            //System.out.println("HTTP响应数据[" + response + "]");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                returnVal = EntityUtils.toString(entity, Charset.forName(charset));
            } else {
            	System.out.println("HTTP响应状态不正常【" + response.getStatusLine().getStatusCode() + "】");
            }
            httpPost.abort();
        } catch (Throwable t) {
        	t.printStackTrace();
        } finally {
//            if (closeableHttpClient != null) {
//                try {
//                	closeableHttpClient.close();
//                } catch (Throwable t) {
//                	logger.error("关闭HTTP连接时发生异常", t);
//                }
//            }
        	long end = System.currentTimeMillis();
        	System.out.println("HTTP请求完毕，耗时[" + (end - start) + "]ms");
        }
        return returnVal;
	}
	
	public final static String doPost(String url, Map<String, String> headers, byte[] bodys, String charset) {
		long start = System.currentTimeMillis();
		String returnVal = "";
        CloseableHttpClient closeableHttpClient = null;
        try {
        	BasicHeader[] basicHeaders = packageBasicHeaders(headers);
        	closeableHttpClient = createCloseableHttpClient(url);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeaders(basicHeaders);
            httpPost.setEntity(new ByteArrayEntity(bodys));
            HttpResponse response = closeableHttpClient.execute(httpPost);
            System.out.println("HTTP响应数据[" + response + "]");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                returnVal = EntityUtils.toString(entity, Charset.forName(charset));
            } else {
            	System.out.println("HTTP响应状态不正常【" + response.getStatusLine().getStatusCode() + "】");
            }
            httpPost.abort();
        } catch (Throwable t) {
        	t.printStackTrace();
        } finally {
//            if (closeableHttpClient != null) {
//                try {
//                	closeableHttpClient.close();
//                } catch (Throwable t) {
//                	logger.error("关闭HTTP连接时发生异常", t);
//                }
//            }
        	long end = System.currentTimeMillis();
        	System.out.println("HTTP请求完毕，耗时[" + (end - start) + "]ms");
        }
        return returnVal;
	}
	
	private final static CloseableHttpClient createCloseableHttpClient(String url) {
		CloseableHttpClient closeableHttpClient = null;
		try {
			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
			httpClientBuilder.setDefaultRequestConfig(requestConfig);
			httpClientBuilder.setUserAgent("user_agent");
			closeableHttpClient = httpClientBuilder.build();
		} catch (Throwable t) {
        	t.printStackTrace();
		}
		return closeableHttpClient;
	}
	
    private final static Registry<ConnectionSocketFactory> createRegistry() {
    	Registry<ConnectionSocketFactory> registry = null;
    	try {
    		SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        	sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
            	
        		@Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
                
            });
    		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE);
    		registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new PlainConnectionSocketFactory())
                    .register("https", sslConnectionSocketFactory)
                    .build();
		} catch (Throwable t) {
        	t.printStackTrace();
		}
    	return registry;
    }
	
    private final static BasicHeader[] packageBasicHeaders(Map<String, String> headers) {
    	BasicHeader[] basicHeaders = null;
    	if (headers != null && headers.size() > 0) {
    		basicHeaders = new BasicHeader[headers.size()];
        	int headerIndex = 0;
        	for (Iterator<Entry<String, String>> it = headers.entrySet().iterator(); it.hasNext();) {
        		Entry<String, String> entry = it.next();
        		basicHeaders[headerIndex++] = new BasicHeader(entry.getKey(), entry.getValue());
        	}
    	}
    	return basicHeaders;
    }
    
    private final static List<BasicNameValuePair> packageBasicNameValuePairs(Map<String, String> bodys)  {
    	List<BasicNameValuePair> basicNameValuePairs = null;
    	if (bodys != null && bodys.size() > 0) {
    		basicNameValuePairs = new ArrayList<BasicNameValuePair>(bodys.size());
            for (Iterator<Entry<String, String>> it = bodys.entrySet().iterator(); it.hasNext();) {
                Entry<String, String> entry = it.next();
                basicNameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
    	}
    	return basicNameValuePairs;
    }
    
}
