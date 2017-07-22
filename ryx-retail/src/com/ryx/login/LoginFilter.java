package com.ryx.login;


import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;

public class LoginFilter extends BaseSecurityFilter {
	
	private boolean valiateChallenge;
	
	public LoginFilter(String filterPath) {
		this(filterPath, true);
	}
	
	public LoginFilter(String filterPath, boolean valiateChallenge) {
		super(filterPath);
		this.valiateChallenge = valiateChallenge;
		
	}
	
	@Override
	protected boolean doFilterInternal(HttpServletRequest request, HttpServletResponse response) {
		SecurityContent content = new SecurityContent(request);
		// 检查挑战码是否正常
		if(valiateChallenge && !content.isChallengeFine()) {
			if(RequestUtil.isToJson(request)) {
				ResponseUtil.write(request, response, content.getCode(), content.getMessage(), null);
			}
			return false;
		}
		// 检查用户名密码是否存在
		if(!content.usernameAndPasswordNotEmpty()) {
			if(RequestUtil.isToJson(request)) {
				ResponseUtil.write(request, response, content.getCode(), content.getMessage(), null);
			}
			return false;
		}
		
		// 根据用户名密码从用户中心获取用户信息
		content.authenticateUsernamePasswordAndPackUsername();
		
		// 以下逻辑跟SecurityFilter的后半部分大体一致 但是要将新的username和clerkId放回到cookie中
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
			// 将完整的user信息放到session中
			generateSession(request, USER_SESSION, content.getUser());
			// 将token放到cookie中
			generateCookie(response, TOKEN_COOKIE, content.getToken(), 1);
			// 将专卖证号和工号放到cookie中
			generateCookie(response, USERNAME_COOKIE, content.getUsername(), 30);
			generateCookie(response, CLERK_COOKIE, content.getClerkId(), 30);
			if(RequestUtil.isToJson(request)) {
				ResponseUtil.write(request, response, content.getCode(), content.getMessage(), content.getUser());
			} else {
				return true;
			}
		} else {
			if(RequestUtil.isToJson(request)) {
				ResponseUtil.write(request, response, content.getCode(), content.getMessage(), null);
			}
		}
		return false;
	}

}
