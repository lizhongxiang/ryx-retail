package com.ryx.social.retail.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class MyHttpUtil {
	private static final String CHARSET = HTTP.UTF_8;
	private static HttpClient customerHttpClient;

	private MyHttpUtil() {

	}

	public static synchronized HttpClient getHttpClient() {
		if (null == customerHttpClient) {
			HttpParams params = new BasicHttpParams();
			// 设置一些基本参数
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) " + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			
			// 超时设置
			/* 从连接池中取连接的超时时间 */
			ConnManagerParams.setTimeout(params, 10000);
			/* 连接超时 */
			HttpConnectionParams.setConnectionTimeout(params, 20000);
			/* 请求超时 */
			HttpConnectionParams.setSoTimeout(params, 20000);

			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			customerHttpClient = new DefaultHttpClient(conMgr, params);
		}
		return customerHttpClient;
	}
	/*
	public static String post(String url, NameValuePair... params) {
		return null;
	}
	 */
	public static String post(String url, Map<String, String> params) {
		try {
			// 创建POST请求
			HttpPost request = new HttpPost(url);
			if(params != null && !params.isEmpty()) {
				// 编码参数
				List<NameValuePair> formparams = new ArrayList<NameValuePair>(); // 请求参数
				for (String k : params.keySet()) {
					String v = params.get(k);
					NameValuePair n = new BasicNameValuePair(k, v);
					formparams.add(n);
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, CHARSET);
				request.setEntity(entity);
			}
//			request.addHeader("Cookie", "JSESSIONID=0000HW5eVP-OHOXdX-DQj1uKwnF:189e73tto; path=/; domain=sd.xinshangmeng.com");
			request.addHeader("Cookie", "JSESSIONID=0000P8y3nd7FzXXzZJcbjbCq2Y6:17q3dhefu; path=/; domain=sd.xinshangmeng.com");
			// 发送请求
			HttpClient client = getHttpClient();
			HttpResponse response = client.execute(request);
//			String res=EntityUtils.toString(response.getEntity(), CHARSET);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//				String sss=EntityUtils.toString(response.getEntity(), CHARSET);
				// throw new RuntimeException("请求失败");
				return "{\"code\":\"4000\", \"msg\":\"请求服务器失败，返回代码：" + response.getStatusLine().getStatusCode() + "\", \"result\":'" + response.getStatusLine().toString() + "'}";
			}
			HttpEntity resEntity = response.getEntity();
			return (resEntity == null) ? null : EntityUtils.toString(resEntity, CHARSET);
		} catch (UnsupportedEncodingException e) {
			//Log.e(TAG, e.getMessage());
			return "{\"code\":\"4002\", \"msg\":\"请求服务器失败，返回的数据编码错误！\", \"result\":'UnsupportedEncodingException:" + e.getMessage() + "'}";
		} catch (ClientProtocolException e) {
			//Log.e(TAG, e.getMessage());
			return "{\"code\":\"4003\", \"msg\":\"请求服务器失败，不支持的网络协议！\", \"result\":'ClientProtocolException:" + e.getMessage() + "'}";
		} catch (IOException e) {
			//Log.e(TAG, e.getMessage());
			return "{\"code\":\"4001\", \"msg\":\"请求服务器失败！\", \"result\":'IOException:" + e.getMessage() + "'}";
		}
	}
}