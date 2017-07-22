package com.ryx.social.retail.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IRetailUserManagerDao;

@Repository
public class RetailUserManagerDaoImpl extends BaseDaoImpl implements IRetailUserManagerDao {

	private static final Logger LOG = LoggerFactory.getLogger(RetailUserManagerDaoImpl.class);
	@Override
	public void addRole(String servcieFlag, String roleName,String roleID) throws SQLException {
		String sql = "insert into  PUB_ROLE(ROLE_NAME,IS_LOCKED,ROLE_ID,SERVICEFLAG)values(?,?,?,?)";
		this.executeSQL(sql,new Object[]{roleName,"0",roleID,servcieFlag});
	}
	
	@Override
	public void addRetailRole(String userCode,String roleIDS) throws SQLException{
		String sql = "insert into  PUB_RETAIL_ROLE(RETAIL_USERCODE,ROLE_ID)values(?,?)";
		this.executeSQL(sql,new Object[]{userCode,roleIDS});
	}
	
	@Override
	public void addRoleResources(String roleID,String resources) throws SQLException{
		String sql = "insert into  PUB_ROLE_RESOURCES(ROLE_ID,RESOURCES_ID)values(?,?)";
		this.executeSQL(sql,new Object[]{roleID,resources});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> showRoleList(String userCode) throws SQLException {
		StringBuffer sb = new StringBuffer("select rol.*, rrr.resources_id ");
		sb.append("from PUB_ROLE_RESOURCES rrr ");
		sb.append("join (select r.role_name, r.role_id, r.is_locked, r.serviceflag ");
		sb.append("from pub_role r ");
		sb.append("join PUB_RETAIL_ROLE rr ");
		sb.append("on rr.role_id = r.role_id ");   
		sb.append("and rr.retail_usercode = '"+userCode+"') rol ");
		sb.append("on rrr.role_id = rol.role_id");
		return this.selectBySqlQuery(sb.toString());
		
	}
	@Override
	public void updateRole(String username,String isLocked,String roleID){
		StringBuffer sb = new StringBuffer("update  PUB_ROLE set ROLE_ID = '"+roleID+"' ");
		if(username !=null && !username.equals("")){
			sb.append(",ROLE_NAME='"+username+"' ");
		}
		if(isLocked !=null && !isLocked.equals("")){
			sb.append(",IS_LOCKED='"+isLocked+"' ");
		}
		sb.append(" where ROLE_ID='"+roleID+"'");
		this.getJdbcTemplate().execute(sb.toString());
	}

	@Override
	public void updateRoleResources(String resourceID, String roleID) {
		String sql = "update PUB_ROLE_RESOURCES set RESOURCES_ID = '"+resourceID+"' where ROLE_ID = '"+roleID+"'";
		this.getJdbcTemplate().execute(sql);
	}
	
	@Override
	public void addUser(Map<String, Object> userMap) throws SQLException {
		String sql = "insert into pub_user(USER_ID , USER_CODE ,USER_NAME,PASSWORD,USER_TYPE,EMAIL,PHONE,REF_ID,IS_LOCKED,LOGIN_FAIL_NUM,IS_MRB,NOTE)";
		sql += " values ";
		sql += "(?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] obj = this.fittingUser(userMap);
		this.executeSQL(sql, obj);
	}
	
	@Override
	public void addUserRole(String userCode, String roleID) {
		String sql = "insert into PUB_USER_ROLE(USER_CODE,ROLE_IDS) values ('"+userCode+"','"+roleID+"')";
		this.getJdbcTemplate().execute(sql);
	}

	// 返回user_id, user_code, is_locked, role_id, role_name 从pub_user pub_role pub_user_role中获取
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> showUserList(String merchId) throws SQLException {
		String sql = "select pupur.user_id, pupur.user_code, pupur.is_locked, pr.role_id, pr.role_name from (select pu.user_id, pu.user_code, pu.is_locked, pur.role_ids from pub_user pu left join pub_user_role pur on pu.user_code=pur.user_code where pu.ref_id=?) pupur left join pub_role pr on pupur.role_ids=pr.role_id order by pupur.user_code";
		return this.selectBySqlQuery(sql, new Object[] {merchId});
	}
	
	
	
	private Object[] fittingUser(Map<String, Object> userMap){
		Object[] obj = new Object[12];
		obj[0] =  IDUtil.getIdByLen(32);
		obj[1] = String.valueOf(userMap.get("user_code"));
		obj[2] = String.valueOf(userMap.get("user_name"));
		obj[3] = String.valueOf(userMap.get("password"));
		obj[4] = String.valueOf(userMap.get("user_type"));
		obj[5] = String.valueOf(userMap.get("email"));
		obj[6] = String.valueOf(userMap.get("phone"));
		obj[7] = String.valueOf(userMap.get("ref_id"));
		obj[8] = String.valueOf(userMap.get("is_locked"));
		obj[9] = String.valueOf(userMap.get("login_fail_num"));
		if(String.valueOf(userMap.get("is_mrb")).equals("false")){
			obj[10] =1;
		}
		else{
			obj[10] =0;
		}
		obj[11] = String.valueOf(userMap.get("note"));
		return obj;
	}
	
	public String updateUserCode(String userID,String newUserCode,String password,String isLocked) throws SQLException{
//		String upSQL = "update pub_user set user_code = '"+newUserCode+"', is_locked ='"+isLocked+"', password='"+password+"' where user_id = '"+userID+"'";
		StringBuilder upSQL = new StringBuilder();
		upSQL.append(" update pub_user ");
//		upSQL.append(" set user_code = '"+newUserCode+"', is_locked ='"+isLocked+"' ");
		upSQL.append(" set is_locked ='"+isLocked+"' ");
		if(!StringUtil.isBlank(password)){
			upSQL.append(" , password='"+password+"' ");
		}
//		upSQL.append(" where user_id = '"+userID+"' ");
		upSQL.append(" where user_code = '"+newUserCode+"' ");
		
		LOG.debug("sql:"+upSQL.toString());
		this.getJdbcTemplate().execute(upSQL.toString());
		return newUserCode;
	}
	
	public void updateUserCodeInUserAndRole(String oldUserCodem,String newUserCode,String roleID){
		String sql = "update PUB_USER_ROLE set user_code = '"+newUserCode+"', role_ids='"+roleID+"' where user_code='"+oldUserCodem+"'";
		this.getJdbcTemplate().execute(sql);
	}
	
	public String isExistUserCode(String userCode){
		String sql = "select user_id from PUB_USER where user_code='"+userCode+"'";
		String userID = "";
		try {
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> list = this.selectBySqlQuery(sql);
			if(list != null && list.size() >0){
				userID = String.valueOf(list.get(0).get("USER_ID"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOG.error("isExistUserCode 错误",e);
		}
		return userID;
	}

	@Override
	public List<Map<String, Object>> selectUserByUserCode(String userCode) throws Exception {
		String sql = "select * from pub_user pu where pu.user_code='"+userCode+"'";
		return this.selectBySqlQuery(sql);
	}

}
