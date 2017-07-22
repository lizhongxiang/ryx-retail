package com.ryx.login;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SecurityFilterChainProxy implements Filter {
	
	private ArrayList<BaseSecurityFilter> ryxSecurityFilters;
	
	public SecurityFilterChainProxy() {
		
	}
	
	public void setRyxSecurityFilters(ArrayList<BaseSecurityFilter> ryxSecurityFilters) {
		this.ryxSecurityFilters = ryxSecurityFilters;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		boolean isPassable = true;
		// 每个过滤器负责一个功能: 登陆页 登出 挑战码 登入 认证
		// 如果其中某个过滤器的路径不匹配 则返回true
		// 如果其中某个过滤器的路径匹配但逻辑不匹配 返回false
		for(BaseSecurityFilter filter : ryxSecurityFilters) {
			if(!filter.doFilter(request, response)) {
				isPassable = false;
				break;
			}
		}
		if(isPassable) {
			chain.doFilter(req, resp);
		}
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException { }

	@Override
	public void destroy() { }

}
