package com.ryx.login.identitificate.bean;

import com.ryx.login.user.bean.User;

/**
 * 认证服务返回给应用认证结果消息的实体类
 * @author 
 *
 */
public class CallBackMessage {
	private boolean result;
	private int statusNum;
	private String message;
	private String tokenSecret;
	private User user;
	private String loginPageURL;
	
	public String getLoginPageURL() {
		return loginPageURL;
	}
	public void setLoginPageURL(String loginPageURL) {
		this.loginPageURL = loginPageURL;
	}
	private String tokenID;
	
	public String getTokenID() {
		return tokenID;
	}
	public void setTokenID(String tokenID) {
		this.tokenID = tokenID;
	}
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	public int getStatusNum() {
		return statusNum;
	}
	public void setStatusNum(int statusNum) {
		this.statusNum = statusNum;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getTokenSecret() {
		return tokenSecret;
	}
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
}
