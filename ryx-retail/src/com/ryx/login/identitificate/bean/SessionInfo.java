package com.ryx.login.identitificate.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ryx.login.user.bean.User;



/**
 * SessionInfo 类 会话中用户信息的Bean类
 * 
 */

public class SessionInfo implements Serializable {
	
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	/** 用户ID */
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
	
	private String merchId;
	private String merchName;
	private String comId;
	private String liceId;
	private List<Map<String,Object>> permission;
	private String isInit;
	private String initDate;
	private String initTime;
	private String roleId;
	private String roleName;
	
	public void fillByUser(User user) {
		this.setUserId(user.getUserId());
		this.setUserCode(user.getUserCode());
		this.setUserName(user.getUserName());
		this.setPassword(user.getPassword());
		this.setUserType(user.getUserType());
		this.setEmail(user.getEmail());
		this.setPhone(user.getPhone());
		this.setRefId(user.getRefId());
		this.setLocked(user.isLocked());
		this.setLoginFailNum(user.getLoginFailNum());
		this.setMrb(user.isMrb());
		this.setNote(user.getNote());
		this.setPermission(user.getPermission());
		this.setRoleId(user.getRoleId());
		this.setRoleName(user.getRoleName());
	}
	
	public void fillByMerch(Map<String, Object> merch) {
		this.setMerchId((String)merch.get("MERCH_ID"));
		this.setMerchName((String)merch.get("MERCH_NAME"));
		this.setComId((String)merch.get("CGT_COM_ID"));
		this.setLiceId((String)merch.get("LICE_ID"));
		this.setInit((String)merch.get("IS_INIT"));
		this.setInitDate((String)merch.get("INIT_DATE"));
		this.setInitTime((String)merch.get("INIT_TIME"));
	}
	
	
	public List<Map<String, Object>> getPermission() {
		return permission;
	}

	public void setPermission(List<Map<String, Object>> permission) {
		this.permission = permission;
	}

	public String getUserId() {
		return userId;
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
	public String getComId() {
		return comId;
	}

	public void setComId(String comId) {
		this.comId = comId;
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
	public String getMerchId() {
		return merchId;
	}

	public void setMerchId(String merchId) {
		this.merchId = merchId;
	}

	public String getMerchName() {
		return merchName;
	}

	public void setMerchName(String merchName) {
		this.merchName = merchName;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getLiceId() {
		return liceId;
	}

	public void setLiceId(String liceId) {
		this.liceId = liceId;
	}

	public String isInit() {
		return isInit;
	}

	public void setInit(String isInit) {
		this.isInit = isInit;
	}

	public String getInitDate() {
		return initDate;
	}

	public void setInitDate(String initDate) {
		this.initDate = initDate;
	}

	public String getInitTime() {
		return initTime;
	}

	public void setInitTime(String initTime) {
		this.initTime = initTime;
	}

	public String getIsInit() {
		return isInit;
	}

	public void setIsInit(String isInit) {
		this.isInit = isInit;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
}
