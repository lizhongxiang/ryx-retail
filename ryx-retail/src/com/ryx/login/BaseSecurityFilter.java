package com.ryx.login;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;


public abstract class BaseSecurityFilter extends BaseDaoImpl {

	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseSecurityFilter.class);
	
	public static final String CHALLENGE_COOKIE = "cookieValidteCode";
	public static final String TOKEN_COOKIE = "tokenSecret";
	public static final String USERNAME_COOKIE = "username";
	public static final String CLERK_COOKIE = "clerkNum";
	
	public static final String USERNAME_PARAMETER = "username";
	public static final String PASSWORD_PARAMETER = "password";
	public static final String CHALLENGE_PARAMETER = "validateCode";

	public static final String TOKEN_SESSION = "token";
	// 根据token从用户中心取到User后直接放到这个session中
	public static final String REMOTE_USER_SESSION = "casUserSessionkey";
	// 组装之后的SessionInfo对象放到这个session中
	public static final String USER_SESSION = "SESSIONINFO";
	
	public static final String LOGIN_URL = "/login";
	protected String filterPath;
	
	protected BaseSecurityFilter(String filterPath) {
		this.filterPath = filterPath;
	}
	
	private boolean matches(HttpServletRequest request) {
		return request.getServletPath().startsWith(filterPath);
	}
	
	protected void generateCookie(HttpServletResponse response, String cookieName, String cookieValue, int maxDays) {
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(maxDays * 24 * 3600);
		response.addCookie(cookie);
	}
	
	/**
	 * 可以指定删除一个/多个cookie或者删除所有cookie
	 * @param request
	 * @param response
	 * @param cookieNames
	 */
	protected void clearCookies(HttpServletRequest request, HttpServletResponse response, String... cookieNames) {
		Cookie[] cookies = request.getCookies();
		// 没有cookie就直接返回
		if(cookies==null || cookies.length<=0) return;
		// 如果指定要删除的cookie
		if(cookieNames!=null && cookieNames.length>0) {
			for(String cookieName : cookieNames) {
				if(cookieName==null) continue;
				for(Cookie cookie : cookies) {
					// 清空指定的cookie
					if(cookieName.equals(cookie.getName())) {
						cookie.setValue(null);
						response.addCookie(cookie);
					}
				}
			}
		} 
		// 没有指定就删除除username外所有的cookie
		else {
			for(Cookie cookie : cookies) {
				// 不清除cookie中的username
				if(USERNAME_COOKIE.equals(cookie.getName())) continue;
				// 其他的都清空
				cookie.setValue(null);
				response.addCookie(cookie);
			}
		}
	}
	
	protected void generateHeader(HttpServletResponse response, String headerName, String headerValue) {
        response.setHeader(headerName, headerValue);
	}
	
	protected void generateSession(HttpServletRequest request, String sessionName, Object sessionValue) {
		HttpSession session = request.getSession(true);
		session.setAttribute(sessionName, sessionValue);
	}
	
	/**
	 * 可以指定删除一个/多个session 或者删除所有session
	 * @param request
	 * @param sessionNames
	 */
	@SuppressWarnings("unchecked")
	protected void clearSessions(HttpServletRequest request, String... sessionNames) {
		HttpSession session = request.getSession();
		if(session==null) return;
		Enumeration<String> attributeNames = session.getAttributeNames();
		if(sessionNames!=null && sessionNames.length>0) {
			while(attributeNames.hasMoreElements()) {
				String attributeName = attributeNames.nextElement();
				if(attributeName==null) continue;
				for(String sessionName : sessionNames) {
					if(sessionName==null) continue;
					if(attributeName.equals(sessionName)) {
						session.removeAttribute(attributeName);
					}
				}
			}
		} else {
			while(attributeNames.hasMoreElements()) {
				String attributeName = attributeNames.nextElement();
				session.removeAttribute(attributeName);
			}
		}
	}
	
	protected void forwardToLoginPage(HttpServletRequest request, HttpServletResponse response) {
		forward(request, response, LOGIN_URL);
	}
	
	protected void forward(HttpServletRequest request, HttpServletResponse response, String path) {
		try {
			request.getRequestDispatcher(path).forward(request, response);
		} catch (ServletException e) {
			LOGGER.error(" = * = * = * = * = * = * = 页面跳转失败 = * = * = * = * = * = * = ", e);
		} catch (IOException e) {
			LOGGER.error(" = * = * = * = * = * = * = 页面跳转失败 = * = * = * = * = * = * = ", e);
		}
	}
	
	protected void redirectToLoginPage(HttpServletRequest request, HttpServletResponse response) {
		redirect(request, response, LOGIN_URL);
	}
	
	protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) {
		try {
			response.sendRedirect(request.getContextPath() + path);
		} catch (IOException e) {
			LOGGER.error(" = * = * = * = * = * = * = 页面跳转失败 = * = * = * = * = * = * = ", e);
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @return 返回true说明后续的filter需要继续执行 false说明不需要执行后续的filter
	 */
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) {
		if(matches(request)) {
			return doFilterInternal(request, response);
		}
		return true;
	}
	
	protected abstract boolean doFilterInternal(HttpServletRequest request, HttpServletResponse response);
	
}
