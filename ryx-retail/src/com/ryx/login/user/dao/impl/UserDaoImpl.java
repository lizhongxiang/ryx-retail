package com.ryx.login.user.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.ryx.login.user.dao.IUserDao;
import com.ryx.social.retail.util.SQLUtil;

public class UserDaoImpl extends JdbcDaoSupport implements IUserDao{
	private static final Logger logger = LoggerFactory
			.getLogger(UserDaoImpl.class);
	
	private static ApplicationContext context = null;
	private static Object lock = new Object();
	
	private static synchronized ApplicationContext getContext() {
		if(context == null) {
			context = new ClassPathXmlApplicationContext(
					"../spring/root-context.xml");
		}
		return context;
	}
	
	public UserDaoImpl() {
		//ApplicationContext context = new ClassPathXmlApplicationContext(
		//		"../spring/root-context.xml");
		//DataSource dataSource = (DataSource) context.getBean("dataSource");
		DataSource dataSource = (DataSource) getContext().getBean("dataSource");
		setDataSource(dataSource);
	}
	
	@Override
	public Map<String, Object> getUserRoleIds(String userCode) throws Exception {
		List<Map<String, Object>> result = this.getJdbcTemplate().queryForList("select role_ids from pub_user_role where user_code = ?", new Object[] {userCode});
		return result.isEmpty() ? new HashMap<String, Object>() : result.get(0);
	}
	
	@Override
	public Map<String, Object> getRoleResourceIds(Map<String, Object> roleIds) throws Exception {
		StringBuilder sql = new StringBuilder("select resources_id from pub_role_resources where");
		ArrayList<Object> placeholders = new ArrayList<Object>();
		roleIds.put("role_id", roleIds.remove("role_ids"));
		SQLUtil.initSQLIn(roleIds, sql, placeholders, "role_id");
		List<Map<String, Object>> result = this.getJdbcTemplate().queryForList(sql.toString(), placeholders.toArray());
		return result.isEmpty() ? new HashMap<String, Object>() : result.get(0);
	}
	
	@Override
	public List<Map<String, Object>> getResourceList(Map<String, Object> resourceIds) throws Exception {
		// 如果非all的resources_id有值则用
		StringBuilder sql = new StringBuilder("select case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_ID else pr1.RESOURCES_ID end resources_id,");
		sql.append(" case when pr2.RESOURCES_NAME is not null then pr2.RESOURCES_NAME else pr1.RESOURCES_NAME end resources_name,");
		sql.append(" case when pr2.RESOURCES_CHILDREN is not null then pr2.RESOURCES_CHILDREN else pr1.RESOURCES_CHILDREN end resources_children,");
		sql.append(" case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_PARENT_ID else pr1.RESOURCES_PARENT_ID end resources_parent_id,");
		sql.append(" case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_HASPAGE else pr1.RESOURCES_HASPAGE end resources_haspage,");
		sql.append(" case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_ORDER else pr1.RESOURCES_ORDER end resources_order,");
		sql.append(" case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_SERVICEFLAG else pr1.RESOURCES_SERVICEFLAG end resources_serviceflag,");
		sql.append(" case when pr2.RESOURCES_ID is not null then '1' else '0' end multi");
		sql.append(" from (select * from pub_resources where com_id='all') pr1 full join (select * from pub_resources where com_id=?) pr2");
		sql.append(" on pr1.RESOURCES_ID=pr2.RESOURCES_ID ");
		ArrayList<Object> placeholders = new ArrayList<Object>();
		placeholders.add(MapUtils.getString(resourceIds, "com_id"));
		// 拼接上形如: "where pr1.resources_id in ('jxcmxb','dzfw1') or pr2.resources_id in ('jxcmxb','dzfw1')" 的sql
		if(resourceIds.containsKey("resources_id")) {
			sql.append("where ");
			sql.append(SQLUtil.initSQLIn(resourceIds, placeholders, "pr1.resources_id"));
			sql.append(" or ");
			sql.append(SQLUtil.initSQLIn(resourceIds, placeholders, "pr2.resources_id"));
		}
		sql.append(" ORDER BY RESOURCES_ORDER ");
		return this.getJdbcTemplate().queryForList(sql.toString(), placeholders.toArray());
	}
	
	
	public Map getUserByCode(String userCode) {
		List<Map<String, Object>> result = this.getJdbcTemplate()
				.queryForList("select USER_ID, USER_CODE, USER_NAME, PASSWORD, USER_TYPE, EMAIL, PHONE, REF_ID, IS_LOCKED, LOGIN_FAIL_NUM,"
						+ " IS_MRB, NOTE, LAST_LOGIN_IP, LAST_LOGIN_TIME from pub_user where user_code=?", new Object[] {userCode});
		if(result.size() > 0) return result.get(0);
		else return null;
	}
	public Map getMerchInfoById(String refId) {
		List<Map<String, Object>> result = this.getJdbcTemplate()
				.queryForList("select MERCH_ID, MERCH_NAME, MANAGER, TELEPHONE, ADDRESS, BUSI_TIME, BUSI_SCOPE, LONGITUDE, LATITUDE,"
						+ " IS_HEAD, IS_SOCIAL, IS_ORD_CGT, LICE_ID, CGT_COM_ID, STATUS, IS_INIT, DELIVERY_TYPE, OPEN_TIME, CLOSE_TIME," 
						+ " INIT_DATE, INIT_TIME from base_merch where merch_id=?", new Object[] {refId});
		if(result.size() > 0) return result.get(0);
		else return null;
	}

	public Map getUserRoles(String userId) {
		List<Map<String, Object>> result = this.getJdbcTemplate().queryForList("select role_ids from pub_user_role where user_code=?", new Object[] {userId});
		if(result.size() > 0) return result.get(0);
		else return null;
	}
	
	public Map getRoleResources(String roleId) {
		List<Map<String, Object>> result = this.getJdbcTemplate().queryForList("select RESOURCES_ID from PUB_ROLE_RESOURCES where ROLE_ID=?", new Object[] {roleId});
		if(result.size() > 0) {
			return result.get(0);
		}
		else {
			return null;
		}
	}
	
	public Map getResources(String resourcesId) {
		List<Map<String, Object>> result = this.getJdbcTemplate()
				.queryForList("select RESOURCES_ID, RESOURCES_NAME, RESOURCES_CHILDREN, RESOURCES_PARENT_ID, RESOURCES_HASPAGE, RESOURCES_ORDER,"
						+ " RESOURCES_SERVICEFLAG from PUB_RESOURCES where RESOURCES_ID=?", new Object[] {resourcesId});
		if(result.size() > 0) return result.get(0);
		else return null;
	}
	public List getResourcesChildren(String resourcesId) {
		return this.getJdbcTemplate()
				.queryForList("select RESOURCES_ID, RESOURCES_NAME, RESOURCES_CHILDREN, RESOURCES_PARENT_ID, RESOURCES_HASPAGE, RESOURCES_ORDER,"
						+ " RESOURCES_SERVICEFLAG from PUB_RESOURCES where RESOURCES_PARENT_ID=?", new Object[] {resourcesId});
	}
	
	public List getResourcesList(String ids) {
		return this.getJdbcTemplate()
				.queryForList("select RESOURCES_ID, RESOURCES_NAME, RESOURCES_CHILDREN, RESOURCES_PARENT_ID, RESOURCES_HASPAGE, RESOURCES_ORDER,"
						+ " RESOURCES_SERVICEFLAG from PUB_RESOURCES where RESOURCES_ID in (" + ids + ") ORDER BY RESOURCES_ORDER");
	}

	@Override
	public void updateUserLoginMeg(Map<String, String> map) {
		StringBuffer sql = new StringBuffer("update pub_user set LAST_LOGIN_IP=? , LAST_LOGIN_TIME = ? where USER_ID = ?");
		this.getJdbcTemplate().update(sql.toString(), new Object[]{map.get("loginIP"),map.get("loginTime"),map.get("userID")});
		
	}
}
