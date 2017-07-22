package com.ryx.login.support.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

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

/**
 * SecurityEnforceFilter 类 用于安全的过滤器，阻止用户访问未授权的资源
 * 
 */
public class SecurityEnforceFilter implements Filter {

	private static Logger logger = Logger.getLogger(SecurityEnforceFilter.class);

	
	/** 对未登录用户提供的服务 */
	private String[] securityUrl;
	

	private IUserService userService;
	
	

	/**
	 * 方法 destroy 重写Filter的destroy方法
	 */
	public void destroy() {
	}

	/**
	 * 方法 init 重写Filter的init方法
	 */
	public void init(FilterConfig config) throws ServletException {

		String sec = config.getInitParameter("securityUrl");
		if (sec != null) {
			securityUrl = sec.split(";");
		} else {
			securityUrl = new String[0];
		}
	
	}

	/**
	 * 方法 doFilter 重写Filter的doFilter方法
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		String servletPath = request.getServletPath();
		String pathInfo = request.getPathInfo();
		if (servletPath == null) servletPath = "";
		if (pathInfo == null) pathInfo = "";
		servletPath += pathInfo;
		if (servletPath.startsWith("/"))
			servletPath = servletPath.substring(1);
		
		if (servletPath.equals("")) {
			chain.doFilter(req, resp);
			return;
		}
		boolean pass = true;
		for (int i = 0; i < securityUrl.length; i++) {
			if (servletPath.startsWith(securityUrl[i])) {
				pass = false;
				break;
			}
		}
		if(servletPath.startsWith("druid") || servletPath.startsWith("monitoring") ){
			pass = true;
		}
		if(pass){
			chain.doFilter(req,  resp);
		} else{
			HttpSession session = request.getSession(true);
			SessionInfo info = (SessionInfo) session.getAttribute(IdentityUtil.SESSIONINFO_IN_SESSION);
			if(info == null){
				boolean success = false;
				try {
					User user = UserFactory.getUserFromRemote(request, response);
					if(user != null){
						SessionInfo sessionInfo = new SessionInfo();
						sessionInfo.fillByUser(user);
						
						Map<String, Object> merch = getUserService().getMerchInfo(user.getRefId());
						sessionInfo.fillByMerch(merch);

						// 在本地获取权限列表
						String comId = MapUtil.getString(merch, "cgt_com_id");
						List<Map<String, Object>> permissionList = getUserService().getUserPermission(user.getUserCode(), comId);
						sessionInfo.setPermission(permissionList);
						
						session.setAttribute(IdentityUtil.SESSIONINFO_IN_SESSION, sessionInfo);
						success = true;
					} else {
						logger.debug("从认证服务器获取用户信息为空");
					}
				} catch (Exception e) {
					logger.error("验证用户身份出错：", e);
				}
				
				if(success) {
					chain.doFilter(req, resp);
				} else {
					if(RequestUtil.isToJson(request)) {
						ResponseUtil.write(request, response, Constants.CODE_IDENTITY_ERROR, "未获取到用户身份信息", null);
						return;
					} else {
						response.sendRedirect(SSOConfig.createRequstLoginURL(request));
						return;
					}
				}
			} else{
				chain.doFilter(req,  resp);
			}
		}
	}
	
	public IUserService getUserService() {
		if(userService == null) {
			userService = new UserServiceImpl();
		}
		return userService;
	}
}
