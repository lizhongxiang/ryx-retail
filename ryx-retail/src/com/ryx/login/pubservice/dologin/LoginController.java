package com.ryx.login.pubservice.dologin;


import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.login.tool.SSOConfig;
import com.ryx.login.tool.UserFactory;
import com.ryx.login.user.bean.User;
import com.ryx.login.user.service.IUserService;
import com.ryx.login.user.service.impl.UserServiceImpl;
import com.ryx.social.retail.controller.CgtOrderController;

/**
 * LoginController 类 用户登录类
 * 
 */
@Controller
public class LoginController {
	private static final Logger logger = LoggerFactory.getLogger(CgtOrderController.class);
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	/** 验证成功转向的页面 */
	private String retailUrl = "/retail/index";
	
	private IUserService userService;
	
	
	
	
	@RequestMapping("/login/sso")
	public String ssoLogin(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession(true);
		boolean success = false;
		SessionInfo sessionInfo = null;
		try {
			User user = UserFactory.getUserFromRemote(request, response);
			if(user != null){
				sessionInfo = new SessionInfo();
				sessionInfo.fillByUser(user);
				
				Map<String, Object> merch = getUserService().getMerchInfo(user.getRefId());
				if(merch != null) {
					sessionInfo.fillByMerch(merch);
					
					// 在本地获取权限列表
					String comId = MapUtil.getString(merch, "cgt_com_id");
					List<Map<String, Object>> permissionList = getUserService().getUserPermission(user.getUserCode(), comId);
					sessionInfo.setPermission(permissionList);
					
					session.setAttribute(IdentityUtil.SESSIONINFO_IN_SESSION, sessionInfo);
					session.setAttribute("token", request.getParameter("tokenSecret"));
					success = true;
				} else {
					logger.error("获取商户信息出错，" + user.getUserName() + "无商户信息");
				}
			}
		} catch (Exception e) {
			logger.error("验证用户身份出错：", e);
		}
		
		if(success) {
			if(RequestUtil.isToJson(request)) {
				Map result = new HashMap();
				if(sessionInfo != null) {
					result.put("user_code", sessionInfo.getUserCode());
					result.put("merch_id", sessionInfo.getMerchId());
					result.put("merch_name", sessionInfo.getMerchId());
					result.put("ref_id", sessionInfo.getRefId());
					result.put("com_id", sessionInfo.getComId());
					result.put("lice_id", sessionInfo.getLiceId());
				}
				
				ResponseUtil.write(request, response, result);
				return null;
			} else {
				return "redirect:"+retailUrl;
			}
		} else {
			if(RequestUtil.isToJson(request)) {
				ResponseUtil.write(request, response, Constants.CODE_IDENTITY_ERROR, "未获取到用户身份信息", null);
				return null;
			} else {
				return "redirect:"+SSOConfig.createRequstLoginURL(request);
			}
		}
	}
	@RequestMapping("/user/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession(true);
		Cookie[] cookies = request.getCookies();
		String tokenSecret = "";
		//清空本地cookie
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(SSOConfig.cookieTokenSecretName)){
					tokenSecret = cookie.getValue();
				}
				if(!"username".equals(cookie.getName())) {
					cookie.setMaxAge(-1);
					cookie.setValue(null);
					cookie.setPath("/");
					response.addCookie(cookie);
				}
			}
		}
		//清空本地session
		@SuppressWarnings("unchecked")
		Enumeration<String> e=session.getAttributeNames(); 
		while(e.hasMoreElements()){ 
			String sessionName=e.nextElement(); 
			session.removeAttribute(sessionName); 
		}
		//清空认证服务cookie
		if(!tokenSecret.equals("")){
			UserFactory.deleteRemoteCachehUserInfo(tokenSecret);
		}
		
		if (RequestUtil.isToJson(request)) {
			 ResponseUtil.write(request, response, SSOConfig.createRequstLoginURL(request));
		}
		else{
			response.sendRedirect(SSOConfig.createRequstLoginURL(request));
		}
	}
	@RequestMapping("/user/login")
	public String userLogin(HttpServletRequest request, HttpServletResponse response){
		return "redirect:"+SSOConfig.createRequstLoginURL(request);
	}

	public IUserService getUserService() {
		if(userService == null) {
			userService = new UserServiceImpl();
		}
		return userService;
	}
}
