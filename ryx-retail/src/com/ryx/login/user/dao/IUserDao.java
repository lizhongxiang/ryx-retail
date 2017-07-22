package com.ryx.login.user.dao;

import java.util.List;
import java.util.Map;

public interface IUserDao {
	public Map getUserByCode(String userCode);
	public Map getMerchInfoById(String refId);
	public Map getUserRoles(String userId);
	public Map getRoleResources(String roleId);
	public Map getResources(String resourcesId);
	public List getResourcesChildren(String resourcesId);
	public List getResourcesList(String ids);
	
	public void updateUserLoginMeg(Map<String ,String> map);
	public Map<String, Object> getUserRoleIds(String userCode) throws Exception;
	public Map<String, Object> getRoleResourceIds(Map<String, Object> roleIds) throws Exception;
	public List<Map<String, Object>> getResourceList(Map<String, Object> resourceIds) throws Exception;
}
