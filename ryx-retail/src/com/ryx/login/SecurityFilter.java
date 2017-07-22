package com.ryx.login;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;

public class SecurityFilter extends BaseSecurityFilter {
	
	public SecurityFilter() {
		super("/");
	}
	
	public SecurityFilter(String filterPath) {
		super(filterPath);
	}
	
	@Override
	protected boolean doFilterInternal(HttpServletRequest request, HttpServletResponse response) {
		SecurityContent content = new SecurityContent(request);
		// 在session中有用户信息且是启用状态 就放行
		if(content.isActive()) {
			return true;
		} 
		// 如果有用户且不是启用状态 就报错
		else if(content.hasUser()) {
			if(RequestUtil.isToJson(request)) {
				ResponseUtil.write(request, response, content.getCode(), content.getMessage(), content.getUser());
			}
			return false;
		}
		// 先从本地获取token 如果没有则ok=false code="1000"
		content.authenticateLocalToken(this);
		// 如果本地没有这个token 则从用户中心获取用户信息
		if(!content.isOk()) {
			content.authenticateToken();
		}
		
		// 以下逻辑跟LoginFilter的后半部分大体一致 只是不将username和clerkId放回到cookie中
		// 如果都获取正常
		if(content.isOk()) {
			// 从本地获取user和merch信息
			try {
				content.fillUserAndMerch(this);
				if(!content.isOk()) {
					if(RequestUtil.isToJson(request)) {
						ResponseUtil.write(request, response, content.getCode(), content.getMessage(), content.getUser());
					}
					return false;
				}
			} catch (SQLException e) {
				LOGGER.error(" = * = * = * = * = * = * = 根据专卖证号获取用户信息失败 = * = * = * = * = * = * = ", e);
			}
			// 从本地获取权限
			try {
				content.fillPermission(this);
			} catch (SQLException e) {
				LOGGER.error(" = * = * = * = * = * = * = 根据专卖证号获取用户权限失败 = * = * = * = * = * = * = ", e);
			}
			// 将token放到session中
			generateSession(request, TOKEN_SESSION, content.getToken());
			// 将完整的信息放到session中
			generateSession(request, USER_SESSION, content.getUser());
			// 继续访问
			return true;
		} else {
			if(RequestUtil.isToJson(request)) {
				ResponseUtil.write(request, response, content.getCode(), content.getMessage(), null);
			} else {
				redirectToLoginPage(request, response);
			}
		}
		// 都取不到禁止访问
		return false;
	}

}
