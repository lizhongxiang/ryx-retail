package com.ryx.social.retail.dao.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IPromotionDao;
import com.ryx.social.retail.util.SQLUtil;

@Repository("promotionDao")
public class PromotionDaoImpl extends BaseDaoImpl implements IPromotionDao {
	private Logger LOG = LoggerFactory.getLogger(PromotionDaoImpl.class);
	
	//查询促销信息
	public static final String selectMerchPromotionSql = initSelectMerchPromotionSql();
	private static String initSelectMerchPromotionSql() {
		StringBuilder sql= new StringBuilder();
		sql.append("SELECT ");
		sql.append("PROMOTION_ID, MERCH_ID, PROMOTION_TYPE, CREATE_DATE, CREATE_TIME, PROMOTION_DESC, ");
		sql.append("PROMOTION_CONTENT, PROMOTION_MUST, PROMOTION_SHOULD, PROMOTION_ACTION, START_DATE, ");
		sql.append("START_TIME, END_DATE, END_TIME, STATUS, DIMENSION_ID, IS_COEXISTENT, IS_INSISTENT ");
		sql.append("From MERCH_PROMOTION ");
		sql.append("where 1=1 ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> searchMerchPromotion(Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchMerchPromotion paramMap :"+paramMap);
		
		StringBuffer sql = new StringBuffer(selectMerchPromotionSql);
		List<Object> list = new ArrayList<Object>();
		
		String promotionId = MapUtil.getString(paramMap, "promotion_id",null);
		String merch_id = MapUtil.getString(paramMap, "merch_id");
		String promotionType = MapUtil.getString(paramMap, "promotion_type");
		String promotionDesc = MapUtil.getString(paramMap, "promotion_desc");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		String status = MapUtil.getString(paramMap, "status");
		Integer pageIndex = MapUtil.getInt(paramMap, "page_index", -1);
		Integer pageSize = MapUtil.getInt(paramMap, "page_size", -1);
		String notPromotionId = MapUtil.getString(paramMap, "not_promotion_id");//不包含promotion_id
		String isClash = MapUtil.getString(paramMap, "is_clash");//是否冲突   1：是
		Map<String, Object> orderParams = MapUtil.get(paramMap, "order_params", null);
		
		if(!StringUtil.isBlank(notPromotionId)) {
			sql.append("AND PROMOTION_ID != ?");
			list.add(notPromotionId);
		}
		
		sql.append("AND MERCH_ID = ? ");
		list.add(merch_id);
		
		if(!StringUtil.isBlank(promotionDesc)){
			sql.append("AND PROMOTION_DESC = ? ");
			list.add(promotionDesc);
		}
		if(!StringUtil.isBlank(promotionId)) {
			String [] promotionIdArr = promotionId.split(",");
			if(promotionIdArr.length>=1 ){
				sql.append("AND PROMOTION_ID IN(?");
				list.add(promotionIdArr[0]);
			}
			for (int i = 1; i < promotionIdArr.length; i++) {
				sql.append(",?");
				list.add(promotionIdArr[i]);
			}
			sql.append(") ");
		}
		
		if(!StringUtil.isBlank(isClash)){
			if (!StringUtil.isBlank(startDate) && !StringUtil.isBlank(endDate)) {
				sql.append("AND START_DATE <= ? AND END_DATE >= ? ");
				list.add(endDate);
				list.add(startDate);
			} else if(!StringUtil.isBlank(startDate)) {
				//通过开始时间，查询未过期促销
				sql.append("AND END_DATE >= ? ");
				list.add(startDate);
			}
		}else{
			if (!StringUtil.isBlank(startDate)) {
				sql.append("AND  START_DATE >= ? ");
				list.add(startDate);
			}
			if(!StringUtil.isBlank(endDate)) {
				sql.append("AND  END_DATE <= ? ");
				list.add(endDate);
			}
		}
		
		if (!StringUtil.isBlank(status)) {
			sql.append("AND  STATUS = ? ");
			list.add(status);
		}
		if (!StringUtil.isBlank(promotionType)) {
			sql.append("AND  PROMOTION_TYPE = ? ");
			list.add(promotionType);
		}
		if(orderParams != null){
			sql.append("ORDER BY ");
			String[] orderArray = new String[]{"is_insistent", "create_date", "create_time"};
			int index = 0;
			for(String order : orderArray) {
				if(orderParams.get(order)!=null) {
					if(index++!=0) sql.append(", ");
					sql.append(order + " " + orderParams.get(order));
				}
			}
		}else{
			sql.append("ORDER BY CREATE_DATE DESC, CREATE_TIME DESC ");
		}
		
		Page pageResult = this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}

	//插入促销信息
	public static final String insertMerchPromotionSql = initInsertMerchPromotionSql();
	private static String initInsertMerchPromotionSql() {
		StringBuilder sql= new StringBuilder();
		sql.append("INSERT INTO MERCH_PROMOTION ");
		sql.append("(");
		sql.append("PROMOTION_ID, MERCH_ID, PROMOTION_TYPE, CREATE_DATE, CREATE_TIME, PROMOTION_DESC, ");
		sql.append("PROMOTION_CONTENT, PROMOTION_MUST, PROMOTION_SHOULD, PROMOTION_ACTION, START_DATE, ");
		sql.append("START_TIME, END_DATE, END_TIME, STATUS, DIMENSION_ID, IS_COEXISTENT, IS_INSISTENT ");
		sql.append(") ");
		sql.append("VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sql.toString();
	}
	@Override
	public void insertMerchPromotion(Map<String, Object> paramMap) throws Exception {
		LOG.debug("insertMerchPromotion paramMap :"+paramMap);
		StringBuffer sql = new StringBuffer(insertMerchPromotionSql);
		List<Object> list = new ArrayList<Object>();
		
		String promotionId = MapUtil.getString(paramMap, "promotion_id", IDUtil.getId());
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String promotionType = MapUtil.getString(paramMap, "promotion_type");
		String createDate = DateUtil.getToday();
		String createTime = DateUtil.getCurrentTime().substring(8);
		String promotionDesc = MapUtil.getString(paramMap, "promotion_desc");
		String promotionContent = MapUtil.getString(paramMap, "promotion_content");
		String promotionMust = MapUtil.getString(paramMap, "promotion_must");
		String promotionShould = MapUtil.getString(paramMap, "promotion_should");
		String promotionAction = MapUtil.getString(paramMap, "promotion_action");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String startTime = MapUtil.getString(paramMap, "start_time");
		String endDate = MapUtil.getString(paramMap, "end_date");
		String endTime = MapUtil.getString(paramMap, "end_time");
		String status = MapUtil.getString(paramMap, "status", "1");
		String dimensionId = MapUtil.getString(paramMap, "dimension_id", "0");
		String isCoexistent = MapUtil.getString(paramMap, "is_coexistent", "0");
		String isInsistent = MapUtil.getString(paramMap, "is_insistent", "0");
		
		list.add(promotionId);
		list.add(merchId);
		list.add(promotionType);
		list.add(createDate);
		list.add(createTime);
		list.add(promotionDesc);
		list.add(promotionContent);
		list.add(promotionMust);
		list.add(promotionShould);
		list.add(promotionAction);
		list.add(startDate);
		list.add(startTime);
		list.add(endDate);
		list.add(endTime);
		list.add(status);
		list.add(dimensionId);
		list.add(isCoexistent);
		list.add(isInsistent);
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//修改促销信息
	@Override
	public void updateMerchPromotion(Map<String, Object> paramMap) throws Exception {
		LOG.debug("updateMerchPromotion paramMap :"+paramMap);
		
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		
		sql.append("UPDATE MERCH_PROMOTION ");
		
		String promotionId = MapUtil.getString(paramMap, "promotion_id");
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String promotionType = MapUtil.getString(paramMap, "promotion_type");
		String promotionDesc = MapUtil.getString(paramMap, "promotion_desc");
		String promotionContent = MapUtil.getString(paramMap, "promotion_content");
		String promotionMust = MapUtil.getString(paramMap, "promotion_must");
		String promotionShould = MapUtil.getString(paramMap, "promotion_should");
		String promotionAction = MapUtil.getString(paramMap, "promotion_action");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String startTime = MapUtil.getString(paramMap, "start_time");
		String endDate = MapUtil.getString(paramMap, "end_date");
		String endTime = MapUtil.getString(paramMap, "end_time");
		String status = MapUtil.getString(paramMap, "status");
		String isCoexistent = MapUtil.getString(paramMap, "is_coexistent");
		String isInsistent = MapUtil.getString(paramMap, "is_insistent");
		
		sql.append("SET PROMOTION_ID = ? ");
		list.add(promotionId);
		
		if (!StringUtil.isBlank(promotionType)) {
			sql.append(",PROMOTION_TYPE = ? ");
			list.add(promotionType);
		}
		if (!StringUtil.isBlank(promotionDesc)) {
			sql.append(",PROMOTION_DESC = ? ");
			list.add(promotionDesc);
		}
		if (!StringUtil.isBlank(promotionContent)) {
			sql.append(",PROMOTION_CONTENT = ? ");
			list.add(promotionContent);
		}
		if (!StringUtil.isBlank(promotionMust)) {
			sql.append(",PROMOTION_MUST = ? ");
			list.add(promotionMust);
		}
		if (!StringUtil.isBlank(promotionShould)) {
			sql.append(",PROMOTION_SHOULD = ? ");
			list.add(promotionShould);
		}
		if (!StringUtil.isBlank(promotionAction)) {
			sql.append(",PROMOTION_ACTION = ? ");
			list.add(promotionAction);
		}
		if (!StringUtil.isBlank(startDate)) {
			sql.append(",START_DATE = ? ");
			list.add(startDate);
		}
		if (!StringUtil.isBlank(startTime)) {
			sql.append(",START_TIME = ? ");
			list.add(startTime);
		}
		if (!StringUtil.isBlank(endDate)) {
			sql.append(",END_DATE = ? ");
			list.add(endDate);
		}
		if (!StringUtil.isBlank(endTime)) {
			sql.append(",END_TIME = ? ");
			list.add(endTime);
		}
		if (!StringUtil.isBlank(status)) {
			sql.append(",STATUS = ? ");
			list.add(status);
		}
		if (!StringUtil.isBlank(isCoexistent)) {
			sql.append(",IS_COEXISTENT = ? ");
			list.add(isCoexistent);
		}
		if (!StringUtil.isBlank(isInsistent)) {
			sql.append(",IS_INSISTENT = ? ");
			list.add(isInsistent);
		}
		
		sql.append("WHERE PROMOTION_ID = ? AND MERCH_ID = ? ");
		list.add(promotionId);
		list.add(merchId);
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//查询促销奖品信息
	public static final String selectPromotionPrizeSql = initSelectPromotionPrizeSql();
	private static String initSelectPromotionPrizeSql() {
		StringBuilder sql= new StringBuilder();
		sql.append("SELECT ");
		sql.append("PROMOTION_ID, PRIZE_ID, PRIZE_NAME, PRIZE_LEVEL, PRIZE_TOTAL, PRIZE_LEFT, RANDOM_FLOOR, RANDOM_CEILING ");
		sql.append("FROM PROMOTION_PRIZE ");
		sql.append("WHERE 1=1 ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> searchPromotionPrize(Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchPromotionPrize paramMap :"+paramMap);
		StringBuffer sql = new StringBuffer(selectPromotionPrizeSql);
		List<Object> list = new ArrayList<Object>();
		String promotionId = MapUtil.getString(paramMap, "promotion_id");
		String prizeId = MapUtil.getString(paramMap, "prize_id");
		String random = MapUtil.getString(paramMap, "random");
		
		if (!StringUtil.isBlank(promotionId)) {
			sql.append("AND PROMOTION_ID = ? ");
			list.add(promotionId);
		}
		if (!StringUtil.isBlank(prizeId)) {
			sql.append("AND PRIZE_ID = ? ");
			list.add(prizeId);
		}
		if (!StringUtil.isBlank(random)) {
			sql.append("AND RANDOM_FLOOR <= ? AND RANDOM_CEILING >= ? ");
			list.add(random);
			list.add(random);
		}
		
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	//插入促销奖品
	public static final String insertPromotionPrizeSql = initInsertPromotionPrizeSql();
	private static String initInsertPromotionPrizeSql() {
		StringBuilder sql= new StringBuilder();
		sql.append("INSERT INTO PROMOTION_PRIZE ");
		sql.append("(");
		sql.append("PROMOTION_ID, PRIZE_ID, PRIZE_NAME, PRIZE_LEVEL, PRIZE_TOTAL, PRIZE_LEFT, RANDOM_FLOOR, RANDOM_CEILING");
		sql.append(") ");
		sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?) ");
		return sql.toString();
	}
	@Override
	public void insertPromotionPrize(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("insertPromotionPrize paramMap :"+paramList);
		StringBuffer sql = new StringBuffer(insertPromotionPrizeSql);
		List<Object> list = new ArrayList<Object>();
		String promotionId, prizeId, prizeName ,prizeLevel ,prizeTotal ,prizeLeft ,randomFloor ,randomCeiling ;
		Object [] objArr =  null;
		for (Map<String, Object> paramMap: paramList) {
			objArr = new Object[8];
			promotionId = MapUtil.getString(paramMap, "promotion_id");
			prizeId = MapUtil.getString(paramMap, "prize_id", IDUtil.getId());
			prizeName = MapUtil.getString(paramMap, "prize_name");
			prizeLevel = MapUtil.getString(paramMap, "prize_level");
			prizeTotal = MapUtil.getString(paramMap, "prize_total");
			prizeLeft = MapUtil.getString(paramMap, "prize_left");
			randomFloor = MapUtil.getString(paramMap, "random_floor");
			randomCeiling = MapUtil.getString(paramMap, "random_ceiling");
			objArr[0] = promotionId;
			objArr[1] = prizeId;
			objArr[2] = prizeName;
			objArr[3] = prizeLevel;
			objArr[4] = prizeTotal;
			objArr[5] = prizeLeft;
			objArr[6] = randomFloor;
			objArr[7] = randomCeiling;
			list.add(objArr);
		}
		
		this.executeBatchSQL(sql.toString(), list);
	}
	
	//修改促销奖品
	@Override
	public void updatePromotionPrize(Map<String, Object> paramMap) throws Exception {
		LOG.debug("updatePromotionPrize paramMap :"+paramMap);
		
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		
		String promotionId = MapUtil.getString(paramMap, "promotion_id");
		String prizeId = MapUtil.getString(paramMap, "prize_id");
		String prizeName = MapUtil.getString(paramMap, "prize_name");
		String prizeLevel = MapUtil.getString(paramMap, "prize_level");
		String prizeTotal = MapUtil.getString(paramMap, "prize_total");
		String prizeLeft = MapUtil.getString(paramMap, "prize_left");
		String randomFloor = MapUtil.getString(paramMap, "random_floor");
		String randomCeiling = MapUtil.getString(paramMap, "random_ceiling");
		
		sql.append("UPDATE PROMOTION_PRIZE ");
		sql.append("SET PROMOTION_ID = ? ");
		list.add(promotionId);
		if (!StringUtil.isBlank(prizeId)) {
			sql.append(",PRIZE_ID = ? ");
			list.add(prizeId);
		}
		if (!StringUtil.isBlank(prizeName)) {
			sql.append(",PRIZE_NAME = ? ");
			list.add(prizeName);
		}
		if (!StringUtil.isBlank(prizeLevel)) {
			sql.append(",PRIZE_LEVEL = ? ");
			list.add(prizeLevel);
		}
		if (!StringUtil.isBlank(prizeTotal)) {
			sql.append(",PRIZE_TOTAL = ? ");
			list.add(prizeTotal);
		}
		if (!StringUtil.isBlank(prizeLeft)) {
			sql.append(",PRIZE_LEFT = ? ");
			list.add(prizeLeft);
		}
		if (!StringUtil.isBlank(randomFloor)) {
			sql.append(",RANDOM_FLOOR = ? ");
			list.add(randomFloor);
		}
		if (!StringUtil.isBlank(randomCeiling)) {
			sql.append(",RANDOM_CEILING = ? ");
			list.add(randomCeiling);
		}

		sql.append("WHERE PROMOTION_ID = ? AND PRIZE_ID = ? ");
		list.add(promotionId);
		list.add(prizeId);
		
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	// 查询促销流水
	@Override
	public List<Map<String, Object>> selectMerchPromotionRecord(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectMerchPromotionRecord paramMap :" + paramMap);
		StringBuilder sql = new StringBuilder();
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		List<Object> paramList = new ArrayList<Object>();
		sql.append("SELECT RECORD_ID, MPR.CONSUMER_ID, MPR.ORDER_ID, PACK_ID, PROMOTION_KEY, RECORD_DATE,");
		sql.append("RECORD_TIME, LINE_NUM, MP.PROMOTION_ID, MP.PROMOTION_TYPE, CREATE_DATE, CREATE_TIME,");
		sql.append("PROMOTION_DESC, PROMOTION_CONTENT,MP.STATUS, IS_COEXISTENT,IS_INSISTENT,BMC.TELEPHONE,");
		sql.append("BMC.CONSUMER_NAME,BMC.CARD_ID,SO.AMTYS_ORD_TOTAL,SO.AMT_ORD_PROFIT ");
		sql.append("FROM MERCH_PROMOTION_RECORD MPR, MERCH_PROMOTION MP ,BASE_MERCH_CONSUMER BMC, SALE_ORDER SO ");
		sql.append("WHERE MPR.PROMOTION_ID = MP.PROMOTION_ID ");
		sql.append("AND MPR.CONSUMER_ID = BMC.CONSUMER_ID(+) ");
		sql.append("AND MPR.ORDER_ID = SO.ORDER_ID ");
		
		SQLUtil.initSQLEqual(paramMap, sql, paramList
				, "mpr.merch_id", "record_id", "mp.promotion_id", "mpr.order_id", "so.order_id", "mp.promotion_type");
		if(!StringUtil.isBlank(startDate)){
			sql.append(" AND RECORD_DATE >= ? ");
			paramList.add(startDate);
		}
		if(!StringUtil.isBlank(endDate)){
			sql.append(" AND RECORD_DATE <= ? ");
			paramList.add(endDate);
		}
		
		sql.append(" order by RECORD_DATE DESC, RECORD_TIME desc ");
		Page result = this.searchPaginatedBySql(sql.toString()
				, MapUtil.getInt(paramMap, "page_index", 1), MapUtil.getInt(paramMap, "page_size", 20), paramList.toArray());
		paramMap.put("page_count", result.getPageSum());
		paramMap.put("count", result.getTotal());
		return result.getRows();
	}
	
	// 新增促销流水
	@Override
	public void insertMerchPromotionRecord(List<Map<String, Object>> recordList) throws Exception {
		LOG.debug("insertMerchPromotionRecord recordList :" + recordList);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO MERCH_PROMOTION_RECORD");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		List<Object> paramArray = null;
		int index = 0;
		for(Map<String, Object> recordMap : recordList) {
			paramArray = new ArrayList<Object>();
			if(index++==0) {
				sqlBuilder.append(SQLUtil.initSQLInsertValues(recordMap, paramArray
						, "record_id", "promotion_id", "merch_id", "consumer_id", "order_id", "line_num", "pack_id"
						, "promotion_type", "promotion_key", "record_date", "record_time"));
			} else {
				SQLUtil.initSQLInsertValues(recordMap, paramArray
						, "record_id", "promotion_id", "merch_id", "consumer_id", "order_id", "line_num", "pack_id"
						, "promotion_type", "promotion_key", "record_date", "record_time");
			}
			paramArrayList.add(paramArray.toArray());
		}
		this.executeBatchSQL(sqlBuilder.toString(), paramArrayList);
	}
	
}
