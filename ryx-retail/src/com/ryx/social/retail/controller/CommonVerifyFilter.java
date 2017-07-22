package com.ryx.social.retail.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.ResponseUtil;

public class CommonVerifyFilter implements Filter {
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String realPath = httpRequest.getSession().getServletContext().getRealPath("/") + "WEB-INF/validate.xml";
		CommonVerifier verifier = new CommonVerifier(realPath);
		VerifiedResult result = verifier.validateParameter(httpRequest);
		if(result.isFlag()) {
			httpRequest.setAttribute("__params", result.getData());
			chain.doFilter(httpRequest, httpResponse);
		} else {
			ResponseUtil.write(httpRequest, httpResponse, Constants.FAIL, result.getMessage(), null);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
