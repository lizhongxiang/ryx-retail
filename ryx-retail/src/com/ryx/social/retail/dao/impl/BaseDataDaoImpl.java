package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.IBaseDataDao;
import com.ryx.social.retail.util.SQLUtil;

@Repository
public class BaseDataDaoImpl extends BaseDaoImpl implements IBaseDataDao {
	
	private Logger logger = LoggerFactory.getLogger(BaseDataDaoImpl.class);
	
	public static final String selectPubUserSql = initSelectPubUserSql();
	private static String initSelectPubUserSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT USER_ID, USER_CODE, USER_NAME, PASSWORD, USER_TYPE, EMAIL, PHONE, REF_ID,");
		sb.append(" IS_LOCKED, LOGIN_FAIL_NUM, IS_MRB, NOTE FROM PUB_USER WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectPubUser(Map<String, Object> userParam) throws Exception {
		logger.debug("BaseDataDaoImpl selectPubUser userParam: " + userParam);
		StringBuffer sqlBuffer = new StringBuffer(selectPubUserSql);
		List<Object> paramObject = new ArrayList<Object>();
		if(userParam.get("user_code")!=null) {
			String[] userCodeArray = userParam.get("user_code").toString().split(",");
			if(userCodeArray.length==1) {
				sqlBuffer.append(" AND USER_CODE = ?");
				paramObject.add(userCodeArray[0]);
			} else {
				sqlBuffer.append(" AND USER_CODE IN (");
				for(int i=0; i<userCodeArray.length; i++) {
					paramObject.add(userCodeArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if(userParam.get("ref_id")!=null) {
			String[] refIdArray = userParam.get("ref_id").toString().split(",");
			if(refIdArray.length==1) {
				sqlBuffer.append(" AND REF_ID = ?");
				paramObject.add(refIdArray[0]);
			} else {
				sqlBuffer.append(" AND REF_ID IN (");
				for(int i=0; i<refIdArray.length; i++) {
					paramObject.add(refIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	
	public static final String selectBaseMerchSql = initSelectBaseMerchSql();
	private static String initSelectBaseMerchSql() {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT MERCH_ID, MERCH_NAME, MANAGER, TELEPHONE, ADDRESS, BUSI_TIME, BUSI_SCOPE, LONGITUDE, LATITUDE, IS_HEAD, IS_SOCIAL,");
		sb.append(" IS_ORD_CGT, LICE_ID, CGT_COM_ID, STATUS, IS_INIT, DELIVERY_TYPE, OPEN_TIME, CLOSE_TIME, FILE_ID, INIT_DATE, INIT_TIME");
		sb.append(" FROM BASE_MERCH WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectBaseMerch(Map<String, Object> merchParam) throws Exception {
		logger.debug("BaseDataDaoImpl selectBaseMerch merchParam: " + merchParam);
		StringBuilder sqlBuffer = new StringBuilder(selectBaseMerchSql);
		List<Object> paramList = new ArrayList<Object>();
		if(merchParam.get("merch_id")!=null) {
			String[] merchIdArray = merchParam.get("merch_id").toString().split(",");
			if(merchIdArray.length==1) {
				sqlBuffer.append(" AND MERCH_ID = ?");
				paramList.add(merchIdArray[0]);
			} else {
				sqlBuffer.append(" AND MERCH_ID IN (");
				for(int i=0; i<merchIdArray.length; i++) {
					paramList.add(merchIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if(merchParam.containsKey("lice_id") && merchParam.get("lice_id")!=null) {
			SQLUtil.initSQLIn(merchParam, sqlBuffer, paramList, "lice_id");
		}
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	
}
