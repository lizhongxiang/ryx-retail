package com.ryx.login.tool;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.ryx.login.identitificate.bean.SessionInfo;

public class IdentityUtil {

	public static String SESSIONINFO_IN_SESSION = "SESSIONINFO"; 
	/**
	 * 获取已登录的用户信息，如果用户未登录，则返回null
	 * @param request
	 * @return
	 */
	public static SessionInfo getUser(HttpServletRequest request) {
		
		HttpSession session = request.getSession(true);
		SessionInfo info = (SessionInfo) session
				.getAttribute(IdentityUtil.SESSIONINFO_IN_SESSION);
		return info;
	}
	
	public static Map<String, Object> getUserMap(HttpServletRequest request) {
		SessionInfo info = getUser(request);
		Map<String, Object> userMap = new HashMap<String, Object>();
		if(info==null) {
			return userMap;
		}
		userMap.put("user_id", info.getUserId());
		userMap.put("com_id", info.getComId());
		userMap.put("user_code", info.getUserCode());
		userMap.put("user_name", info.getUserName());
		userMap.put("password", info.getPassword());
		userMap.put("user_type", info.getUserType());
		userMap.put("email", info.getEmail());
		userMap.put("phone", info.getPhone());
		userMap.put("ref_id", info.getRefId());
		userMap.put("is_locked", info.isLocked());
		userMap.put("login_fail_num", info.getLoginFailNum());
		userMap.put("is_mrb", info.isMrb());
		userMap.put("note", info.getNote());
		userMap.put("lice_id", info.getLiceId());
		userMap.put("is_init", info.isInit());
		userMap.put("init_date", info.getInitDate());
		userMap.put("init_time", info.getInitTime());
		userMap.put("role_id", info.getRoleId());
		userMap.put("role_name", info.getRoleName());
		return userMap;
	}
	
}
