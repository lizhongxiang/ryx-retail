package com.ryx.login.user.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ryx.framework.util.MapUtil;
import com.ryx.login.user.dao.IUserDao;
import com.ryx.login.user.dao.impl.UserDaoImpl;
import com.ryx.login.user.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	
	private static Map<String, List> rolecache = new HashMap<String, List>();
	private static UserDaoImpl userDao;
	
	public IUserDao getUserDao() {
		if(userDao == null) {
			userDao = new UserDaoImpl();
		}
		return userDao;
	}
	
	public Map<String, Object> getMerchInfo(String refId) {
		if(StringUtils.hasText(refId)) {
			return getUserDao().getMerchInfoById(refId);
		} else {
			return null;
		}
	}
	

	@Override
	public List<Map<String,Object>> getUserPermission(String userCode, String comId) throws Exception {
		//获取用户角色
		Map<String, Object> roleIds = getUserDao().getUserRoleIds(userCode);
		Map<String, Object> resourceIds = getUserDao().getRoleResourceIds(roleIds);
		// 除了资源号resource_id外还需要公司号com_id才能查询到资源列表
		resourceIds.put("com_id", comId);
		List<Map<String, Object>> resourceList = getUserDao().getResourceList(resourceIds);
		List<Map<String, Object>> resourceResult = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> resource : resourceList) {
			Map<String, Object> resourceRow = new HashMap<String, Object>();
			resourceRow.put("module_id", resource.get("resources_id"));
			resourceRow.put("title", resource.get("resources_name"));
			resourceRow.put("multi", resource.get("multi"));
			resourceRow.put("haschildren", resource.get("resources_children"));
			resourceRow.put("haspage", resource.get("resources_haspage"));
			resourceRow.put("parent_id", resource.get("resources_parent_id"));
			resourceResult.add(resourceRow);
		}

		LOG.debug("获取用户权限信息：userCode=" + userCode + ",list=" + resourceResult);
		return resourceResult;
	}

	@Override
	public List getUserResource(String userId) {
		Map roleMap = null;
		Map resourceMap = null;
		List list =new ArrayList();
		roleMap=getUserDao().getUserRoles(userId);
		String role="", r;
		String resourcesid;
		List<Map> resources, resources2;
		Map rmap;
		if(roleMap!=null){
			 role=roleMap.get("ROLE_IDS").toString();
		}
		String[] roles=role.split("\\,");
		for(int i=0;i<roles.length;i++){
			
			r = roles[i];
			
			if(rolecache.containsKey(r)) {
				list.addAll(rolecache.get(r));
			}
			else {
				resourceMap=getUserDao().getRoleResources(r);
				if(resourceMap != null) {
					resourcesid=(String)resourceMap.get("RESOURCES_ID");
					
					String[] arr = resourcesid.split(",");
					StringBuffer strbuff = new StringBuffer();
					for (int s = 0; s < arr.length; s++) {
						strbuff.append("'").append(arr[s]).append("'");
						if(s + 1 != arr.length) {
							strbuff.append(",");
						}
					}
					
					resources = getUserDao().getResourcesList(strbuff.toString());
					resources2 = new ArrayList<Map>();
					for (int j = 0; j < resources.size(); j++) {
						rmap = new HashMap();
						rmap.put("module_id", resources.get(j).get("RESOURCES_ID"));
						rmap.put("title", resources.get(j).get("RESOURCES_NAME"));
						rmap.put("haschildren", resources.get(j).get("RESOURCES_CHILDREN"));
						rmap.put("haspage", resources.get(j).get("RESOURCES_HASPAGE"));
						rmap.put("parent_id", resources.get(j).get("RESOURCES_PARENT_ID"));
						resources2.add(rmap);
					}
					
					rolecache.put(r, resources2);
					list.addAll(resources2);
				}
			}
		}
		// TODO Auto-generated method stub
		return list;
	}
	public void updateUserLoginMeg(String userId, String ip,String loginTime){
		Map<String,String> map = new HashMap<String, String>();
		map.put("userID", userId);
		map.put("loginIP", ip);
		map.put("loginTime", loginTime);
		userDao.updateUserLoginMeg(map);
	}
}
