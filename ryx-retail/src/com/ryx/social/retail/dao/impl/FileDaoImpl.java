package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IFileDao;
import com.ryx.social.retail.util.SQLUtil;
@Repository("fileDao")
public class FileDaoImpl extends BaseDaoImpl implements IFileDao {
	
	private Logger logger=LoggerFactory.getLogger(FileDaoImpl.class);
	
	public static final String insertMerchFileSql = initInsertMerchPictureSql();
	private static String initInsertMerchPictureSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("INSERT INTO MERCH_FILE (MERCH_ID, FILE_ID, FILE_NAME, FILE_TYPE, FILE_DESCRIPTION, FILE_PURPOSE, FILE_SEQUENCE, FILE_HEIGHT,");
		sb.append(" FILE_WIDTH, FILE_LOCATION, UPLOAD_DATE, UPLOAD_TIME, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();				
	}
	/**
	 * 梁凯
	 * 新增商户文件
	 */
	@Override
	public void insertMerchFile(Map<String, Object> fileMap) throws Exception {
		logger.debug("FileDaoImpl insertMerchFile fileMap:"+fileMap);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(fileMap.get("merch_id"));
		paramObject.add(fileMap.get("file_id"));
		paramObject.add(fileMap.get("file_name")); // 名称
		paramObject.add(fileMap.get("file_type")); // 文件后缀
		paramObject.add(fileMap.get("file_description")); // 描述
		paramObject.add(fileMap.get("file_purpose")); // 照片类型
		paramObject.add(fileMap.get("file_sequence")); // 照片序号
		paramObject.add(fileMap.get("file_height")); // 照片高度
		paramObject.add(fileMap.get("file_width")); // 照片宽度
		paramObject.add(fileMap.get("file_location")); // 存放位置
		paramObject.add(fileMap.get("upload_date")); // 上传日期
		paramObject.add(fileMap.get("upload_time")); // 上传时间
		paramObject.add(fileMap.get("status")); // 状态		
		this.executeSQL(insertMerchFileSql, paramObject.toArray());		
	}
	
	public static final String selectMerchFileSql = initSelectMerchFileSql();
	private static String initSelectMerchFileSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, FILE_ID, FILE_NAME, FILE_TYPE, FILE_DESCRIPTION, FILE_PURPOSE, FILE_SEQUENCE,");
		sb.append(" FILE_HEIGHT, FILE_WIDTH, FILE_LOCATION, UPLOAD_DATE, UPLOAD_TIME, STATUS");
		sb.append(" FROM MERCH_FILE WHERE MERCH_ID = ?");
		return sb.toString();
	}
	/**
	 * 梁凯
	 * 查询商户文件
	 */
	@Override
	public List<Map<String, Object>> selectMerchFile(Map<String, Object> fileParam) throws Exception {
		logger.debug("FileDaoImpl selectMerchFile fileParam: " + fileParam);
		StringBuilder sqlBuffer = new StringBuilder(selectMerchFileSql);
		List<Object> paramList = new ArrayList<Object>();
		String filePurpose = MapUtil.getString(fileParam, "file_purpose", null);
		String status = MapUtil.getString(fileParam, "status", null);
		paramList.add(fileParam.get("merch_id"));
		
		SQLUtil.initSQLEqual(fileParam, sqlBuffer, paramList, "file_id", "file_type");
		
		if(filePurpose!=null) {
			String[] purposeArray = filePurpose.split(",");
			if(purposeArray.length==1) {
				sqlBuffer.append(" AND FILE_PURPOSE = ?");
				paramList.add(purposeArray[0]);
			} else {
				sqlBuffer.append(" AND FILE_PURPOSE IN (");
				for(int i=0; i<purposeArray.length; i++) {
					paramList.add(purposeArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if(status!=null) {
			String[] statusArray = status.split(",");
			if(statusArray.length==1) {
				sqlBuffer.append(" AND STATUS = ?");
				paramList.add(statusArray[0]);
			} else {
				sqlBuffer.append(" AND STATUS IN (");
				for(int i=0; i<statusArray.length; i++) {
					paramList.add(statusArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		sqlBuffer.append(" ORDER BY FILE_PURPOSE DESC, UPLOAD_DATE DESC, UPLOAD_TIME DESC");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	public static final String deleteMerchFileSql = initDeleteMerchFileSql();
	private static String initDeleteMerchFileSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM MERCH_FILE WHERE MERCH_ID = ?");
		return sb.toString();
	}
	/**
	 * 梁凯
	 * 物理删除商户文件
	 */
	@Override
	public void deleteMerchFile(Map<String, Object> merchFileParam) throws Exception {
		logger.debug("FileDaoImpl selectMerchFile merchFileParam: " + merchFileParam);
		StringBuffer sqlBuffer = new StringBuffer(deleteMerchFileSql);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(merchFileParam.get("merch_id"));
		if(merchFileParam.get("file_id")!=null) {
			sqlBuffer.append(" AND FILE_ID = ?");
			paramList.add(merchFileParam.get("file_id"));
		}
		if(merchFileParam.get("file_purpose")!=null) {
			sqlBuffer.append(" AND FILE_PURPOSE = ?");
			paramList.add(merchFileParam.get("file_purpose"));
		}
		this.executeSQL(sqlBuffer.toString(), paramList.toArray());
	}
	
	public static final String updateMerchFileSql = initUpdateMerchFileSql();
	private static String initUpdateMerchFileSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE MERCH_FILE SET FILE_PURPOSE = ? WHERE MERCH_ID = ? AND FILE_PURPOSE IN ('01', '02')");
		return sb.toString();
	}
	/**
	 * 梁凯
	 * 修改商户文件, 必传merch_id
	 */
	@Override
	public void updateMerchFile(Map<String, Object> merchFileParam) throws Exception {
		logger.debug("FileDaoImpl updateMerchFile merchFileParam: " + merchFileParam);
		StringBuffer sqlBuffer = new StringBuffer(updateMerchFileSql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(merchFileParam.get("file_purpose"));
		paramObject.add(merchFileParam.get("merch_id"));
		if(merchFileParam.get("file_id")!=null) {
			sqlBuffer.append(" AND FILE_ID = ?");
			paramObject.add(merchFileParam.get("file_id"));
		}
		this.executeSQL(sqlBuffer.toString(), paramObject.toArray());
	}
	
	public static final String searchMaxFileSequenceSql = initSearchMaxFileSequenceSql();
	private static String initSearchMaxFileSequenceSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT nvl(max(FILE_SEQUENCE), 0) file_sequence FROM MERCH_FILE WHERE MERCH_ID = ? AND FILE_PURPOSE = ?");
		return sb.toString();
	}
	/**
	 * 梁凯 2014年5月23日11:59:59
	 * 获取某商户某种文件的最大序列号
	 */
	@Override
	public int searchMaxFileSequence(Map<String, Object> fileParam) throws Exception {
		logger.debug("FileDaoImpl searchMaxFileSequence fileParam: " + fileParam);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(fileParam.get("merch_id"));
		paramList.add(fileParam.get("file_purpose"));
		return this.selectIntBySqlQuery(searchMaxFileSequenceSql, paramList.toArray());
	}
	
	/**
	 * 
	 * 查询商户文件，可以不传merch_id
	 */
	@Override
	public List<Map<String, Object>> selectAllMerchFile(Map<String, Object> fileParam) throws Exception {
		logger.debug("FileDaoImpl selectAllMerchFile fileParam: " + fileParam);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		sql.append("SELECT MERCH_ID, FILE_ID, FILE_NAME, FILE_TYPE, FILE_DESCRIPTION, ");
		sql.append("FILE_PURPOSE, FILE_SEQUENCE,  FILE_HEIGHT, FILE_WIDTH, FILE_LOCATION, ");
		sql.append("UPLOAD_DATE, UPLOAD_TIME, STATUS ");
		sql.append("FROM MERCH_FILE WHERE 1=1 ");
		SQLUtil.initSQLEqual(fileParam, sql, list, "merch_id", "file_id", "status");
		List<Object> paramList = new ArrayList<Object>();
		String filePurpose = (String) fileParam.get("file_purpose");
		
		if(filePurpose!=null) {
			String[] purposeArray = filePurpose.split(",");
			if(purposeArray.length==1) {
				sql.append(" AND FILE_PURPOSE = ?");
				paramList.add(purposeArray[0]);
			} else {
				sql.append(" AND FILE_PURPOSE IN (");
				for(int i=0; i<purposeArray.length; i++) {
					paramList.add(purposeArray[i]);
					if(i==0) {
						sql.append("?");
					} else {
						sql.append(", ?");
					}
				}
				sql.append(")");
			}
		}
		sql.append(" ORDER BY FILE_PURPOSE DESC, UPLOAD_DATE DESC, UPLOAD_TIME DESC");
		return this.selectBySqlQuery(sql.toString(), paramList.toArray());
	}
	
	
}
