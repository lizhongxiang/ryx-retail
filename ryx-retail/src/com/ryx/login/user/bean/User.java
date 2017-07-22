package com.ryx.login.user.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class User implements Serializable{
	
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	private String userId;
	private String userCode;
	private String userName;
	private String password;
	private String userType;
	private String email;
	private String phone;
	private String refId;
	private boolean isLocked;
	private int loginFailNum;
	private boolean isMrb;
	private String note;
	private String roleId;
	private String roleName;
	private String comId;
	private List<Map<String,Object>> permission;
	

	public String getComId() {
		return comId;
	}
	public void setComId(String comId) {
		this.comId = comId;
	}
	public List<Map<String, Object>> getPermission() {
		return permission;
	}
	public void setPermission(List<Map<String, Object>> permission) {
		this.permission = permission;
	}
	public String getUserId() {
		return userId.toUpperCase();
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public boolean isLocked() {
		return isLocked;
	}
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	public int getLoginFailNum() {
		return loginFailNum;
	}
	public void setLoginFailNum(int loginFailNum) {
		this.loginFailNum = loginFailNum;
	}
	public boolean isMrb() {
		return isMrb;
	}
	public void setMrb(boolean isMrb) {
		this.isMrb = isMrb;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
}
