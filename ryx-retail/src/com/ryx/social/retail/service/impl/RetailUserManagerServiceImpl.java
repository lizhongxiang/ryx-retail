package com.ryx.social.retail.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.login.tool.SSOConfig;
import com.ryx.social.retail.dao.IRetailUserManagerDao;
import com.ryx.social.retail.service.IRetailUserManagerService;
import com.ryx.social.retail.util.RetailConfig;


@Service
public class RetailUserManagerServiceImpl implements IRetailUserManagerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(RetailUserManagerServiceImpl.class);
	
	@Resource
	private IRetailUserManagerDao  retailUserManagerDaoImpl;
	
	@Override
	public void addRole(String resourceIDS, String roleName,String userCode,String roleID) {
		LOG.debug("RetailUserManagerServiceImpl addRole ");
		String servcieFlag = SSOConfig.serviceFlag;
		//如果没有roleID 表示新增的
 		if(roleID != null && roleID.equals("")){
			roleID = IDUtil.getIdByLen(32);
			try {
				retailUserManagerDaoImpl.addRole(servcieFlag, roleName, roleID);
				retailUserManagerDaoImpl.addRetailRole(userCode, roleID);
				retailUserManagerDaoImpl.addRoleResources(roleID, resourceIDS);
			} catch (Exception e) {
				LOG.error("----RetailUserManagerServiceImpl addRole:"+e);
			}
		}
 		//没有是更新。
		else{
			retailUserManagerDaoImpl.updateRole(roleName, "", roleID);
			retailUserManagerDaoImpl.updateRoleResources(resourceIDS, roleID);
		}
	}
	
	public List<Map<String,Object>> showRoleList(String userCode) throws SQLException{
		LOG.debug("RetailUserManagerServiceImpl showRoleList userCode:"+userCode);
		return retailUserManagerDaoImpl.showRoleList(userCode);
	}
	
	/**
	 * 添加销售员
	 */
	public void addUser(String roleID,Map<String,Object> userMap) throws Exception{
		LOG.debug("RetailUserManagerServiceImpl addUser userMap:"+userMap);
		
		userMap.put("status", MapUtil.getString(userMap, "status", "01"));//注意status的值
		userMap.put("user_pwd", MapUtil.getString(userMap, "password"));
		
		String addUserUrl = RetailConfig.getUcenterServer()+"ucenter/addClerk";
		String json = HttpUtil.post(addUserUrl, userMap);
		Map<String, Object> resultMap = (Map<String, Object>)JsonUtil.json2Map(json);
		
		String code = (String) resultMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			retailUserManagerDaoImpl.addUser(userMap);
			retailUserManagerDaoImpl.addUserRole(userMap.get("user_code").toString(), roleID);
		}else{
			throw new Exception((String) resultMap.get("msg"));
		}
	}

	/**
	 * 修改销售员
	 */
	@Override
	public void updateUser(String roleID,String userID,Map<String, Object> userMap) throws Exception {
		LOG.debug("RetailUserManagerServiceImpl updateUser userMap:"+userMap);
		
		String userCode=MapUtil.getString(userMap, "user_code","");
		String status = MapUtil.getString(userMap, "status", "01");//注意status的值
		
		List<Map<String, Object>> userList = retailUserManagerDaoImpl.selectUserByUserCode(userCode);//查询是否有此用户
		if (userList==null || userList.isEmpty() || userList.size() > 1) {
			throw new RuntimeException("用户修改失败，无此用户或此用户数据错误(重复)");
		}
		
		String oldPwd = MapUtil.getString(userList.get(0), "password");
		String newPwd=MapUtil.getString(userMap, "password", oldPwd);
		
		userMap.put("status", status);
		userMap.put("new_pwd", newPwd);
		userMap.put("old_pwd", oldPwd);
		String updateUserUrl = RetailConfig.getUcenterServer()+"ucenter/updateMerchants";
		String json = HttpUtil.post(updateUserUrl, userMap);
		Map<String, Object>	resultMap = (Map<String, Object>)JsonUtil.json2Map(json);
		LOG.debug("RetailUserManagerServiceImpl updateUser json:"+json);
		String code = (String) resultMap.get("code");
		
		if(code!=null && Constants.SUCCESS.equals(code)) {
			String oldUserCode = retailUserManagerDaoImpl.updateUserCode(userID, userCode,newPwd,String.valueOf(userMap.get("is_locked")));
			retailUserManagerDaoImpl.updateUserCodeInUserAndRole(oldUserCode, userCode, roleID);
		}else{
			throw new Exception((String) resultMap.get("msg"));
		}
	}
	
	/**
	 * 查询用户管理列表
	 */
	public List<Map<String,Object>> showUserList(String merchId, String token) throws SQLException{
		LOG.debug("RetailUserManagerServiceImpl showUserList merchId: " + merchId + " token: " + token);
		
		String showUserUrl = RetailConfig.getUcenterServer()+"ucenter/getClerk";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("token", token);
		String json = HttpUtil.post(showUserUrl, paramMap);
		Map<String, Object> userMap = (Map<String, Object>)JsonUtil.json2Map(json);
		String code = (String) userMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			List<Map<String, Object>> userResultList = (List<Map<String, Object>>) userMap.get("result");
			List<Map<String, Object>> userList = retailUserManagerDaoImpl.showUserList(merchId);
			
			List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
			Map<String, Map<String, Object>> userDataMap = new HashMap<String, Map<String, Object>>();
			for (Map<String, Object> map : userList) {//tobacco数据
				userDataMap.put(MapUtil.getString(map, "user_code"), map);
				if ("1".equals(MapUtil.getString(map, "role_id"))) {
					returnList.add(map);
				}
			}
			for (Map<String, Object> map : userResultList) {//用户中心数据
				String userCode = MapUtil.getString(map, "user_code");
				if (userDataMap.containsKey(userCode)) {
					if ("01".equals(MapUtil.getString(map, "status"))) {
						map.put("is_locked", "0");
					} else if ("02".equals(MapUtil.getString(map, "status"))) {
						map.put("is_locked", "1");
					}
					map.put("role_id", "3");
					map.put("role_name", "销售员");
					returnList.add(map);
				}
			}
			return returnList;
		}else{
			throw new RuntimeException((String) userMap.get("msg"));
		}
	}
	
	/**
	 * 判断是否有此用户
	 */
	@Override
	public String isExistUserCode(String userCode) {
		LOG.debug("RetailUserManagerServiceImpl isExistUserCode userCode: " + userCode);
		return retailUserManagerDaoImpl.isExistUserCode(userCode);
	}

}
