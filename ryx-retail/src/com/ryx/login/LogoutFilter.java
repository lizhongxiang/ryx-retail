package com.ryx.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;

public class LogoutFilter extends BaseSecurityFilter {
	
	public LogoutFilter(String filterPath) {
		super(filterPath);
	}

	@Override
	protected boolean doFilterInternal(HttpServletRequest request, HttpServletResponse response) {
		clearCookies(request, response);
		clearSessions(request);
		if(RequestUtil.isToJson(request)) {
			ResponseUtil.write(request, response, request.getContextPath()+LOGIN_URL);
			return false;
		}
		return true;
	}
}
