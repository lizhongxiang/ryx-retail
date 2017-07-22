package com.ryx.social.retail.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IRetailUserManagerDao {
	
	/**
	 * 向角色表中添加角色
	 * @param resources	资源的IDS
	 * @param roleName	角色名称
	 * @param userCode	用户
	 * @param roleID
	 * @throws SQLException 
	 */
	public void addRole(String servcieFlag, String roleName,String roleID) throws SQLException;
	
	public void addRetailRole(String userCode,String roleIDS) throws SQLException;
	
	public void addRoleResources(String roleID,String resources) throws SQLException;
	
	
	/**
	 * 展示角色列表
	 * @param userCode 用户登录名
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String, Object>> showRoleList(String userCode) throws SQLException;
	
	/**
	 * 更新角色状态
	 * @param username
	 * @param isLocked
	 */
	public void updateRole(String username,String isLocked,String roleID);
	
	/**
	 * 更改角色功能关联表
	 * @param resourceID
	 * @param roleID
	 */
	public void updateRoleResources(String resourceID,String roleID);
	/**
	 * 添加用户
	 * @param userMap 用户对象
	 * @throws SQLException 
	 */
	public void addUser(Map<String,Object> userMap) throws SQLException;
	/**
	 * 添加用户和角色的关联关系
	 * @param userCode  用户登录名
	 * @param roleID	角色ID
	 */
	public void addUserRole(String userCode,String roleID);
	
	/**
	 * 关联pub_user pub_role pub_user_role 查询店员和商户的用户和角色信息列表
	 * 返回user_id, user_code, is_locked, role_id, role_name
	 * @param merchId 商户编号
	 * @return 店员和商户的用户和角色信息列表
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> showUserList(String merchId) throws SQLException;
	
	/**
	 * 更新更新用户登录名
	 * @param userID 用户ID
	 * @param newUserCode 新的用户ID
	 * @return	旧的用户ID
	 * @throws SQLException 
	 */
	public String updateUserCode(String userID,String newUserCode,String password,String isLocked) throws SQLException;
	/**
	 * 更新用户 角色关联表
	 * @param oldUserCodem 旧的用户登陆名
	 * @param newUserCode  新的用户登录名
	 */
	public void updateUserCodeInUserAndRole(String oldUserCodem,String newUserCode,String roleID);
	/**
	 * 判断是否存在用户名
	 * @param userCode
	 * @return
	 */
	public String isExistUserCode(String userCode);
	
	/**
	 * 根据usercode查询用户信息
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectUserByUserCode(String userCode) throws Exception;
}
