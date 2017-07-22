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
 * RequestFilter 类 用于监控请求
 * 
 */
public class RequestFilter implements Filter {

	private static Logger logger = Logger.getLogger(RequestFilter.class);

	/**
	 * 方法 destroy 重写Filter的destroy方法
	 */
	public void destroy() {
	}

	/**
	 * 方法 init 重写Filter的init方法
	 */
	public void init(FilterConfig config) throws ServletException {

	
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

		if(servletPath.startsWith("retail/basedata/uploadLocation")) {
			int len = request.getContentLength();
			logger.debug("请求拦截，长度：" + len + "，PATH：" + servletPath);
			ResponseUtil.write(request, response, "0000", "上传位置信息已被拦截", null);
		} else {
			chain.doFilter(req, response);
		}
	}
}
