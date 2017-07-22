package com.ryx.login.tool;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.ryx.framework.util.HttpUtil;
import com.ryx.login.identitificate.bean.CallBackMessage;
import com.ryx.login.user.bean.User;
import com.ryx.login.user.service.IUserService;
import com.ryx.login.user.service.impl.UserServiceImpl;
import com.ryx.social.retail.util.RetailConfig;

public class UserFactory {
	private static Logger logger = Logger.getLogger(UserFactory.class);


	private static IUserService userService;
	public static IUserService getUserService() {
		if(userService == null) {
			userService = new UserServiceImpl();
		}
		return userService;
	}
	public static User getUserFromRemote(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(true);
//		Object info = session.getAttribute(SSOConfig.casUserSessionkey);
//		if(info == null){
		
			String token = request.getParameter("token");
			if(token == null || "".equals(token)){
				token = SSOConfig.getCookieContent(request, "token");
			}
			String tokenSecret = token;
			
			if(tokenSecret == null || "".equals(tokenSecret)){
				tokenSecret = request.getParameter("tokenSecret");
				if(tokenSecret == null || "".equals(tokenSecret)){
					tokenSecret = SSOConfig.getCookieContent(request, SSOConfig.cookieTokenSecretName);
				}
			}
		
			logger.debug("通过token获取用户信息: token=" + tokenSecret);
			if(StringUtils.isNotEmpty(tokenSecret)){
				Map<String,String> map = new HashMap<String, String>();
				map.put("tokenSecret", tokenSecret);
				map.put("serviceFlag", SSOConfig.serviceFlag);
				logger.debug("获取用户信息: url=" + RetailConfig.getLoginServer()+SSOConfig.getUserFromCASUrl + ", params=" + map);
				String vInfo = HttpUtil.post( RetailConfig.getLoginServer()+SSOConfig.getUserFromCASUrl, map);
				logger.debug("获取到用户信息：" + vInfo);
				Gson gson = new Gson();
				CallBackMessage callBackMessage = gson.fromJson(vInfo, CallBackMessage.class);
				if(callBackMessage.isResult()){
					User user = callBackMessage.getUser();
					logger.debug("用户信息：" + user);
					//将用户信息添加到session中
					session.setAttribute(SSOConfig.casUserSessionkey, user);
					//更新cookie
					Cookie cookie = SSOConfig.setCookie(SSOConfig.cookieTokenSecretName,tokenSecret,Integer.valueOf(SSOConfig.cookieTokenSecretAge));
					response.addCookie(cookie);
					return user;
				}
			}
			return null;
//		}
//		else{
//			return (User)info;
//		}
	}
	
	public static void deleteRemoteCachehUserInfo(String tokenSecret){
		Map<String,String> map = new HashMap<String, String>();
		map.put("tokenSecret", tokenSecret);
		map.put("serviceFlag", SSOConfig.serviceFlag);
		HttpUtil.post(RetailConfig.getLoginServer()+SSOConfig.clearCacheURL, map);
	}
	
}
