package com.ryx.login.tool;

import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.ryx.login.SystemStaticParameters;
import com.ryx.social.retail.util.RetailConfig;

public class SSOConfig {
	public static String getUserFromCASUrl = "/sso/user";
	public static String cookieTokenSecretName;
	public static String cookieTokenSecretPath;
	public static String cookieTokenSecretAge;
	public static String casUserSessionkey;
	public static String serviceFlag;
	public static String loginURL = "/sso/showlogin";
	public static String callbackURL;
	//public static String logoutURL = "/user/logout";
	public static String clearCacheURL = "/sso/clear";
	public static String updateUserPwdURL = "/sso/updatePwd";

	public static void init(Properties serverProperties) {
		callbackURL = SystemStaticParameters.getContextPath() + serverProperties.getProperty("callbackURL");
		serviceFlag	= serverProperties.getProperty("serviceFlag");
		cookieTokenSecretName =  serverProperties.getProperty("cookieTokenSecretName");
		cookieTokenSecretPath = serverProperties.getProperty("cookieTokenSecretPath");
		cookieTokenSecretAge = serverProperties.getProperty("cookieTokenSecretAge");
		casUserSessionkey= serverProperties.getProperty("casUserSessionkey");
	}
	
	public static String createRequstLoginURL(HttpServletRequest request){
		StringBuffer sb = new StringBuffer(RetailConfig.getLoginServer()+SSOConfig.loginURL);
		sb.append("?");
		sb.append("serviceFlag="+SSOConfig.serviceFlag);
		sb.append("&");
		sb.append("callbackURL="+createCallbackURL(request));
		sb.append("&onceFlag=");
		return sb.toString();
	}
	
	public static String getCookieContent(HttpServletRequest request,String cookieName){
		Cookie [] cookies = request.getCookies();
		String cookieContent = "";
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(cookieName)){
					cookieContent = cookie.getValue();
					break;
				}
			}
		}
		return cookieContent;
	}
	private static String createCallbackURL(HttpServletRequest request){
		String cbu = request.getRequestURI();
		if(cbu.endsWith("/retail/module/jydh")) {
			String context = request.getContextPath() + "/";
			StringBuffer sb = request.getRequestURL();
			String url = sb.substring(0, sb.indexOf(context));
			url = new StringBuffer(url).append(cbu).toString();
			return url;
		} else {
			String context = request.getContextPath() + "/";
			StringBuffer sb = request.getRequestURL();
			String url = sb.substring(0, sb.indexOf(context));
			url = new StringBuffer(url).append(callbackURL).toString();
			return url;
		}
	}
	public static  Cookie setCookie(String cookieName,String CookieContent,int maxAge){
		Cookie cookie = new Cookie(cookieName,CookieContent);
		cookie.setPath("/");
		//cookie.setPath(SSOConfig.cookieTokenSecretPath);
		//cookie.setMaxAge(60*60*24*maxAge);
		return cookie;
	}
}
