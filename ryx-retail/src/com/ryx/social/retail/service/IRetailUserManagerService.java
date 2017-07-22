package com.ryx.social.retail.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IRetailUserManagerService {
	/**
	 * 新增角色
	 * @param resourceIDS 资源IDS
	 * @param roleName	      角色名称
	 * @param userCode	  登录名	
	 */
	public void addRole(String resourceIDS, String roleName,String userCode,String roleID);
	
	/***
	 * 展示角色资源列表
	 * @param userCode
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> showRoleList(String userCode) throws SQLException;
	
	/**
	 * 添加用户和角色的关联关系 添加用户
	 * @param roleID 角色ID
	 * @param userMap	用户map
	 * @throws Exception 
	 */
	public void addUser(String roleID,Map<String,Object> userMap) throws Exception;
	/**
	 * 更新用户信息
	 * @author 朱鹏
	 * @param userMap
	 * @return
	 * @throws Exception
	 */
	public void updateUser(String roleID,String userID,Map<String,Object> userMap) throws Exception;
	/**
	 * 显示子账户列表
	 * @param merchId 用户登陆名
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> showUserList(String merchId, String token) throws SQLException;
	
	
	/**
	 * 判断是否存在用户名
	 * @param userCode
	 * @return
	 */
	public String isExistUserCode(String userCode);
	
	
}
