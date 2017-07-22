package com.ryx.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginPageFilter extends BaseSecurityFilter {
	
	public LoginPageFilter(String filterPath) {
		super(filterPath);
	}

	@Override
	protected boolean doFilterInternal(HttpServletRequest request, HttpServletResponse response) {
		return true;
	}

}
