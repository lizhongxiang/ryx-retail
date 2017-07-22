package com.ryx.social.consumer.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.social.consumer.dao.IConsumerMerchDao;
@Repository("consumerMerchDao")
public class ConsumerMerchDaoImpl extends BaseDaoImpl implements IConsumerMerchDao {
	private static final Logger LOG = LoggerFactory.getLogger(ConsumerMerchDaoImpl.class);
	
	public List<Map<String, Object>> searchMerchById(Map<String, Object> merchMap) throws Exception{
		StringBuffer sb=new StringBuffer();
		sb.append(" select a.*, OPEN_TIME||'-'||CLOSE_TIME BUSI_TIME from BASE_MERCH a where 1=1  ");
		List<Object> paramList = new ArrayList<Object>();
		String merchId = (String) merchMap.get("merch_id");
		String[] merchIdArray = merchId.split(",");
		if(merchIdArray.length==1) {
			sb.append(" AND merch_id=?");
			paramList.add(merchIdArray[0]);
		} else {
			sb.append(" AND merch_id IN (");
			int index = 0;
			for(String id : merchIdArray) {
				if(index++==0) {
					sb.append("?");
				} else {
					sb.append(",?");
				}
				paramList.add(id);
			}
			sb.append(")");
		}
		return this.selectBySqlQuery(sb.toString(),paramList.toArray());
	}
	//通过名称查找店铺列表
	public List<Map<String, Object>> searchMerchByName( Map<String, Object> merchMap) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append(" select  bm.merch_id ShopID, max(merch_name) ShopName,min(mp.FILE_LOCATION) ShopFile, ");
		sql.append("  max(sqrt(power((to_number(LONGITUDE)-?)*1117000,2)+power((to_number(LATITUDE)-?)*1117000,2))) distance, ");
		sql.append(" max(OPEN_TIME||'-'||CLOSE_TIME) ShoppingTime,max(open_time) openTime ,max(close_time) closeTime , max(bm.FILE_ID) FILE_ID, max('y') songhuo ");
		sql.append(" from BASE_MERCH bm  left join  MERCH_FILE  mp  on bm.merch_id=mp.merch_id ");
		sql.append(" where bm.merch_name like ?  ");
		sql.append(" group by bm.merch_id  ");
		
		
//		sql.append("select * ");
//		sql.append("from base_merch ");
//		sql.append("where merch_name like ?");
		
		List<String> sqlList = new ArrayList<String>();
		sqlList.add((String) merchMap.get("lng"));
		sqlList.add((String) merchMap.get("lat"));
		sqlList.add("%"+(String) merchMap.get("merchName")+"%");
		String pageIndex = (String) merchMap.get("page_index");
		String pageSize = (String) merchMap.get("page_size");		
		Page pageResult = this.searchPaginatedBySql(sql.toString(),Integer.valueOf(pageIndex ), Integer.valueOf(pageSize),sqlList.toArray());
		merchMap.put("page_count", pageResult.getPageSum());
		merchMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	public static final String selectMerchFileSql = initSelectMerchFileSql();
	private static String initSelectMerchFileSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, FILE_ID, FILE_NAME, FILE_DESCRIPTION, FILE_TYPE, FILE_SEQUENCE, FILE_HEIGHT, FILE_WIDTH, FILE_LOCATION,");
		sb.append(" UPLOAD_DATE, UPLOAD_TIME, STATUS, FILE_PURPOSE FROM MERCH_FILE WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectMerchFile(Map<String, Object> paramMap) throws Exception {
		StringBuffer sqlBuffer = new StringBuffer(selectMerchFileSql);
		List<Object> paramList = new ArrayList<Object>();
		if(paramMap.get("merch_id")!=null) {
			String[] merchIdArray = paramMap.get("merch_id").toString().split(",");
			if(merchIdArray.length==1) {
				sqlBuffer.append(" AND MERCH_ID=?");
				paramList.add(merchIdArray[0]);
			} else {
				sqlBuffer.append(" AND MERCH_ID IN (");
//				int index = 0;
				for (int i = 0; i < merchIdArray.length; i++) {
//				for(String fileId : merchIdArray) {
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(",?");
					}
					paramList.add(merchIdArray[i]);
				}
				sqlBuffer.append(")");
			}
		}
		if(paramMap.get("file_id")!=null) {
			String[] fileIdArray = paramMap.get("file_id").toString().split(",");
			if(fileIdArray.length==1) {
				sqlBuffer.append(" AND FILE_ID=?");
				paramList.add(fileIdArray[0]);
			} else {
				sqlBuffer.append(" AND FILE_ID IN (");
				for (int i = 0; i < fileIdArray.length; i++) {
//				for(String fileId : fileIdArray) {
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(",?");
					}
					paramList.add(fileIdArray[i]);
				}
				sqlBuffer.append(")");
			}
		}
		if(paramMap.get("file_purpose")!=null) {
			sqlBuffer.append(" AND FILE_PURPOSE=?");
			paramList.add(paramMap.get("file_purpose"));
		}
		if(paramMap.get("status")!=null) {
			sqlBuffer.append(" AND STATUS=?");
			paramList.add(paramMap.get("status"));
		}
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
}
