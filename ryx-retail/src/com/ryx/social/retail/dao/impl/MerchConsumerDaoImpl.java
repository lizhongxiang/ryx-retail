package com.ryx.social.retail.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IMerchConsumerDao;
import com.ryx.social.retail.util.SQLFlag;
import com.ryx.social.retail.util.SQLUtil;

@Repository
public class MerchConsumerDaoImpl extends BaseDaoImpl implements IMerchConsumerDao {
	private Logger LOG = LoggerFactory.getLogger(MerchConsumerDaoImpl.class);
	
	//查询基本会员级别
	private static final String selectBaseMerchConsumerGradeSql = initBaseMerchConsumerGradeSql();
	private static String initBaseMerchConsumerGradeSql(){
		StringBuilder sql=new StringBuilder();
		sql.append("SELECT ");
		sql.append("GRADE, GRADE_NAME ");
		sql.append("FROM BASE_CONSUMER_GRADE ");
		return sql.toString();
	}
	
	//查询基本会员级别
	@Override
	public List<Map<String, Object>> selectBaseMerchConsumerGrade (Map<String, Object> paramMap) throws Exception{
		LOG.debug("selectBaseMerchConsumerGrade paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder(selectBaseMerchConsumerGradeSql);
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	// 查询会员级别
	@Override
	public List<Map<String, Object>> selectMerchConsumerGrade(Map<String, Object> paramMap) throws Exception{
		LOG.debug("MerchConsumerDaoImpl selectMerchConsumerGrade paramMap: "+paramMap);
		StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM MERCH_CONSUMER_GRADE WHERE");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap, sqlBuilder, paramList, "merch_id", "grade", "grade_id");
		SQLUtil.initSQLOrder(sqlBuilder, "grade", "2");
		return this.selectBySqlQuery(sqlBuilder.toString(), paramList.toArray());
	}
	
	// 查询会员级别和对应会员数量
	public List<Map<String, Object>> searchMerchConsumerGradeAndConsumerNumber(Map<String, Object> paramMap) throws Exception{
		LOG.debug("MerchConsumerDaoImpl searchMerchConsumerGradeAndConsumerCount paramMap: "+paramMap);
		StringBuilder sqlBuilder = new StringBuilder("SELECT A.*, CASE WHEN B.CONSUMER_NUMBER IS NULL THEN 0 ELSE B.CONSUMER_NUMBER END CONSUMER_NUMBER");
		sqlBuilder.append(" FROM MERCH_CONSUMER_GRADE A, (SELECT GRADE, COUNT(*) CONSUMER_NUMBER FROM BASE_MERCH_CONSUMER WHERE MERCH_ID=? GROUP BY GRADE) B");
		sqlBuilder.append(" WHERE A.GRADE=B.GRADE(+)");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap, paramList, "merch_id");
		SQLUtil.initSQLEqual(paramMap, sqlBuilder, paramList, "merch_id");
		SQLUtil.initSQLOrder(sqlBuilder, "a.grade", "2");
		return this.selectBySqlQuery(sqlBuilder.toString(), paramList.toArray());
	}
	
	//插入会员等级
	@Override
	public void insertMerchConsumerGrades(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("insertMerchConsumerGrade paramList:"+paramList);
		StringBuilder sql = new StringBuilder("INSERT INTO MERCH_CONSUMER_GRADE ");
		List<Object[]> list = new ArrayList<Object[]>();
		List<Object> paramArray = null;
		int index = 0;
		for(Map<String, Object> map : paramList) {
			paramArray = new ArrayList<Object>();
			if(index++==0) {
				sql.append(SQLUtil.initSQLInsertValues(map, paramArray
						, "merch_id", "grade_id", "grade", "grade_name", "upgrade_must", "upgrade_should", "discount"));
			} else {
				SQLUtil.initSQLInsertValues(map, paramArray
						, "merch_id", "grade_id", "grade", "grade_name", "upgrade_must", "upgrade_should", "discount");
			}
			LOG.debug("----------param:"+paramArray);
			list.add(paramArray.toArray());
		}
		LOG.debug("----------sql:"+sql.toString());
		this.executeBatchSQL(sql.toString(), list);
	}
	
	//修改会员等级---单个
	@Override
	public void updateMerchConsumerGrade(Map<String, Object> paramMap) throws Exception {
		LOG.debug("updateMerchConsumer2 paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		sql.append("UPDATE MERCH_CONSUMER_GRADE SET ");
		sql.append(SQLUtil.initSQLEqual(paramMap, list, SQLFlag.COMMA,
				 "grade_name", "upgrade_must", "upgrade_should", "discount"));
		if(paramMap.containsKey("grade_name") || paramMap.containsKey("upgrade_must") || paramMap.containsKey("upgrade_should")){
			if(paramMap.containsKey("new_garde")){
				sql.append(",grade = ? ");
				list.add(MapUtil.getString(paramMap, "new_garde"));
			}
		}else{
			if(paramMap.containsKey("new_garde")){
				sql.append("grade = ? ");
				list.add(MapUtil.getString(paramMap, "new_garde"));
			}
		}
		sql.append(" WHERE ");
		SQLUtil.initSQLEqual(paramMap, sql, list, "merch_id", "grade_id", "grade");
		LOG.debug("----------sql:"+sql.toString());
		LOG.debug("----------param:"+list);
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//修改会员等级--
	@Override
	public void updateMerchConsumerGrades(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("updateMerchConsumerGrade paramList:"+paramList);
		
		StringBuilder sql = new StringBuilder("UPDATE MERCH_CONSUMER_GRADE SET");
		List<Object[]> list = new ArrayList<Object[]>();
		List<Object> paramArray = null;
		int index = 0;
		for(Map<String, Object> itemMap : paramList) {
			paramArray = new ArrayList<Object>();
			if(index++==0) {
				sql.append(SQLUtil.initSQLEqual(itemMap, paramArray, SQLFlag.COMMA
						, "grade", "grade_name", "upgrade_must", "upgrade_should", "discount"));
				sql.append(" WHERE");
				sql.append(SQLUtil.initSQLEqual(itemMap, paramArray, "merch_id", "grade_id"));
			} else {
				SQLUtil.initSQLEqual(itemMap, paramArray
						, "grade", "grade_name", "upgrade_must", "upgrade_should", "discount", "merch_id", "grade_id");
			}
			LOG.debug("----------param:"+paramArray);
			list.add(paramArray.toArray());
		}
		LOG.debug("----------sql:"+sql.toString());
		this.executeBatchSQL(sql.toString(), list);
	}
	
	//删除会员等级
	@Override
	public void deleteMerchConsumerGrades(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("deleteMerchConsumerGrade paramList:"+paramList);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		sql.append("delete from merch_consumer_grade ");
		sql.append("where 1=1 ");
		List<Object> paramArray = null;
		for (int i = 0; i < paramList.size(); i++) {
			paramArray = new ArrayList<Object>();
			if(i==0){
				SQLUtil.initSQLEqual(paramList.get(i),sql, paramArray,
						 "merch_id", "grade_id");
			}else{
				SQLUtil.initSQLEqual(paramList.get(i), paramArray,
						"merch_id", "grade_id");
			}
//			System.out.println("`````"+paramArray);;
			list.add(paramArray.toArray());
		}
		LOG.debug("----------sql:"+sql.toString());
		this.executeBatchSQL(sql.toString(), list);
	}
	
	//-----------------------
	
	//插入会员
	@Override
	public void insertMerchConsumer(Map<String, Object> paramMap) throws Exception {
		LOG.debug("insertMerchConsumer paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder("INSERT INTO BASE_MERCH_CONSUMER ");
		List<Object> list = new ArrayList<Object>();
		SQLUtil.initSQLInsertValues(paramMap, sql, list, 
				"consumer_id", "merch_id", "card_id", "telephone", "consumer_name", "status", 
				"curscore", "topscore", "grade", "modified_timestamp", "cert_id", "gender", "birthday", 
				"zipcode", "month_salary", "degree", "email", "cert_type", "address");
		LOG.debug("----------param:"+list);
		LOG.debug("----------sql:"+sql.toString());
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//查询会员
	public static final String selectMerchConsumerSql = initSelectMerchConsumerSql();
	private static String initSelectMerchConsumerSql() {
		StringBuilder sql = new StringBuilder();
		
		sql.append("SELECT CONSUMER_ID, MERCH_ID, CARD_ID, CONSUMER_NAME, TELEPHONE, CERT_TYPE, ");
		sql.append("CERT_ID, GENDER, BIRTHDAY, ZIPCODE, MONTH_SALARY, DEGREE, DAILY_USE, CURSCORE, CERT_TYPE, ");
		sql.append("ADDRESS, EMAIL, UNIT_TYPE, BRAND_FAVOR, STATUS, MODIFIED_TIMESTAMP, GRADE, TOPSCORE ");
		sql.append("FROM BASE_MERCH_CONSUMER ");
		sql.append("WHERE 1=1 ");
		return sql.toString();
	}
	
	//查询会员--表关联（会员表，会员级别）
	@Override
	public List<Map<String, Object>> searchMerchConsumer(Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchMerchConsumer paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CONSUMER_ID, BMC.MERCH_ID, CARD_ID, CONSUMER_NAME, TELEPHONE, GENDER, BIRTHDAY,CERT_TYPE,");
		sql.append("ZIPCODE, MONTH_SALARY, DEGREE, DAILY_USE, CURSCORE, ADDRESS, EMAIL, UNIT_TYPE, BRAND_FAVOR,CERT_ID,");
		sql.append("STATUS, MODIFIED_TIMESTAMP, BMC.GRADE,TOPSCORE, GRADE_ID, CASE WHEN GRADE_NAME IS NULL THEN '暂无' ELSE GRADE_NAME END grade_name, UPGRADE_MUST, UPGRADE_SHOULD, DISCOUNT ");
		sql.append("FROM BASE_MERCH_CONSUMER BMC,MERCH_CONSUMER_GRADE MCG ");
		sql.append("WHERE BMC.MERCH_ID=MCG.MERCH_ID(+) AND BMC.GRADE=MCG.GRADE(+) ");
		Integer pageIndex = MapUtil.getInt(paramMap, "page_index", -1);
		Integer pageSize = MapUtil.getInt(paramMap, "page_size", -1);
		
		List<Object> list = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap,sql, list, "bmc.merch_id","consumer_id", "status", "bmc.grade");
		String keyWord = MapUtil.getString(paramMap, "keyword");
		if(!StringUtil.isBlank(keyWord)){
			sql.append(" AND (CARD_ID LIKE ? OR TELEPHONE LIKE ? )");
			list.add(keyWord);
			list.add(keyWord);
		}else{
			SQLUtil.initSQLEqual(paramMap,sql, list, "telephone","consumer_name");
		}
//		return this.selectBySqlQuery(sql.toString(),list.toArray());
		sql.append(" ORDER BY TOPSCORE DESC, CONSUMER_ID ");
		Page pageResult = this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	//查询会员等级人数
	@Override
	public List<Map<String, Object>> selectMerchConsumerConunt(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectMerchConsumer paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		sql.append("SELECT GRADE, COUNT(*) CONSUMER_COUNT ");
		sql.append("FROM BASE_MERCH_CONSUMER ");
		sql.append("WHERE 1=1 ");
		SQLUtil.initSQLEqual(paramMap,sql, list, "merch_id");
		sql.append("GROUP BY GRADE ");
		
		LOG.debug("----------param:"+list);
		LOG.debug("----------sql:"+sql.toString());
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	//全部查询，没有排序
	@Override
	public List<Map<String, Object>> selectMerchConsumer(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectMerchConsumer paramMap:"+paramMap);
		String keyWord = MapUtil.getString(paramMap, "keyword");
		String notConsumerId = MapUtil.getString(paramMap, "not_consumer_id");//查询数据不包含此consumeId
		String repeatCardId = MapUtil.getString(paramMap, "repeat_card_id");//重复卡号
		String repeatTelephone = MapUtil.getString(paramMap, "repeat_telephone");//重复手机号
		
		StringBuilder sql = new StringBuilder(selectMerchConsumerSql);
		List<Object> list = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap,sql, list, "merch_id", "status", "grade", "is_upgradable", "telephone", 
				"consumer_name", "card_id");
		String consumerId = MapUtil.getString(paramMap, "consumer_id");
		if(!StringUtil.isBlank(consumerId)){
			String[] consumerIdArr = consumerId.split(",");
			if(consumerIdArr.length >= 1){
				sql.append(" AND CONSUMER_ID IN (?");
				list.add(consumerIdArr[0]);
				for (int i = 1; i < consumerIdArr.length; i++) {
					sql.append(",?");
					list.add(consumerIdArr[i]);
				}
				sql.append(") ");
			}
		}
		if(!StringUtil.isBlank(keyWord)){
			//sql.append("AND (PHONE_NUMBER LIKE ? OR NICKNAME LIKE ? )");
			sql.append("and (telephone = ? or card_id = ? )");
			//list.add(keyWord+"%");
			//list.add(keyWord+"%");
			list.add(keyWord);
			list.add(keyWord);
		}
		
		if(!StringUtil.isBlank(notConsumerId)){
			sql.append("and consumer_id != ? ");
			list.add(notConsumerId);
		}
		
		if(!StringUtil.isBlank(repeatCardId) && !StringUtil.isBlank(repeatTelephone)){
			sql.append("and (card_id = ? or telephone = ?) ");
			list.add(repeatCardId);
			list.add(repeatTelephone);
		}
		
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	//只查询consumer_id,
	@Override
	public List<Map<String, Object>> selectMerchConsumerById(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectMerchConsumer paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CONSUMER_ID, MERCH_ID, CARD_ID, CONSUMER_NAME, TELEPHONE, GENDER, BIRTHDAY, CERT_ID, ");
		sql.append("ZIPCODE, MONTH_SALARY, DEGREE, DAILY_USE, CURSCORE, ADDRESS, EMAIL, UNIT_TYPE, BRAND_FAVOR,");
		sql.append("STATUS, MODIFIED_TIMESTAMP, GRADE,TOPSCORE, CERT_TYPE ");
		sql.append("FROM BASE_MERCH_CONSUMER ");
		sql.append("WHERE CONSUMER_ID = ? ");
		List<Object> list = new ArrayList<Object>();
		list.add(MapUtil.getString(paramMap, "consumer_id"));
		LOG.debug("----------param:"+list);
		LOG.debug("----------sql:"+sql.toString());
		return  this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	//修改会员
	@Override
	public void updateMerchConsumer(Map<String, Object> paramMap) throws Exception {
		LOG.debug("updateMerchConsumer2 paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		BigDecimal adjustPoint = MapUtil.getBigDecimal(paramMap, "adjusted_point");//调整积分（新增或修改积分）
		String oldGrade = MapUtil.getString(paramMap, "old_grade");//旧的等级
		String isDefaultGrade = MapUtil.getString(paramMap, "is_default_grade", "0");//是否有默认级别，1：有
		sql.append("UPDATE BASE_MERCH_CONSUMER ");
		sql.append("SET ");
		sql.append(SQLUtil.initSQLEqual(paramMap, list, SQLFlag.COMMA,
				"modified_timestamp", "card_id", "cert_id", "consumer_name", "telephone", "gender", "birthday", "brand_favor",
				"zipcode", "month_salary", "degree", "daily_use", "curscore", "address", "email", "unit_type", 
				"status", "grade", "topscore", "cert_type"));
		if(adjustPoint.compareTo(BigDecimal.ZERO) == 1){
			sql.append(",curscore = curscore + ?, topscore = topscore + ? ");
			list.add(adjustPoint);
			list.add(adjustPoint);
		}else if(adjustPoint.compareTo(BigDecimal.ZERO) == -1){
			sql.append(",curscore = curscore + ? ");
			list.add(adjustPoint);
		}
		sql.append(" WHERE ");
		SQLUtil.initSQLEqual(paramMap, sql, list, "merch_id", "consumer_id");
		if(!StringUtil.isBlank(oldGrade)){
			sql.append(" AND GRADE = ? ");
			list.add(oldGrade);
		}
		if(isDefaultGrade.equals("1")){
			sql.append(" AND GRADE IS NULL ");
		}
		LOG.debug("----------sql:"+sql.toString());
		LOG.debug("----------param:"+list);
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//批量修改会员信息
	@SuppressWarnings("unused")
	@Override
	public void updateMerchConsumer(List<Map<String, Object>> consumerList)throws Exception {
		LOG.debug("updateMerchConsumer consumerList:"+consumerList);
		StringBuilder sql = new StringBuilder();
		List<Object> paramList = new LinkedList<Object>();
		for (Map<String, Object> paramMap : consumerList) {
			List<Object> list = new LinkedList<Object>();
			BigDecimal adjustPoint = MapUtil.getBigDecimal(paramMap, "adjusted_point");//调整积分（新增或修改积分）
			String oldGrade = MapUtil.getString(paramMap, "old_grade");//旧的等级
			String isDefaultGrade = MapUtil.getString(paramMap, "is_default_grade", "0");//是否有默认级别，1：有
			sql.append("UPDATE BASE_MERCH_CONSUMER ");
			sql.append("SET ");
			sql.append(SQLUtil.initSQLEqual(paramMap, list, SQLFlag.COMMA,
					"modified_timestamp", "card_id", "cert_id", "consumer_name", "telephone", "gender", "birthday", "brand_favor",
					"zipcode", "month_salary", "degree", "daily_use", "curscore", "address", "email", "unit_type", 
					"status", "grade", "topscore", "cert_type"));
			if(adjustPoint.compareTo(BigDecimal.ZERO) == 1){
				sql.append(",curscore = curscore + ?, topscore = topscore + ? ");
				list.add(adjustPoint);
				list.add(adjustPoint);
			}else if(adjustPoint.compareTo(BigDecimal.ZERO) == -1){
				sql.append(",curscore = curscore + ? ");
				list.add(adjustPoint);
			}
			sql.append(" WHERE ");
			SQLUtil.initSQLEqual(paramMap, sql, list, "merch_id", "consumer_id");
			if(!StringUtil.isBlank(oldGrade)){
				sql.append(" AND GRADE = ? ");
				list.add(oldGrade);
			}
			if(isDefaultGrade.equals("1")){
				sql.append(" AND GRADE IS NULL ");
			}
			paramList.add(list.toArray());
		}
		this.executeBatchSQL(sql.toString(), paramList);
	}
	//修改会员的等级，用于删除会员等级
	@Override
	public void updateMerchConsumerByGrade(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("updateMerchConsumerByGrade paramList:"+paramList);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		sql.append("UPDATE BASE_MERCH_CONSUMER ");
		sql.append("SET ");
		sql.append("GRADE = GRADE - 1 ");
		sql.append("WHERE ");
		sql.append("GRADE != 1 AND GRADE >= ? ");
		Object[] paramArr = new Object[1];
		for (int i = 0; i < paramList.size(); i++) {
			paramArr[0] = MapUtil.getString(paramList.get(i), "grade");
//			LOG.debug("----------paramArr:"+paramArr);
			list.add(paramArr);
		}
		LOG.debug("----------sql:"+sql.toString());
		this.executeBatchSQL(sql.toString(), list);
	}
	
	
	//会员积分兑换
	//////////////////
	
	
	//插入积分兑换商品
	@Override
	public void insertExchangePrize (Map<String, Object> paramMap) throws Exception {
		LOG.debug("insertExchangePrize paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO EXCHANGE_PRIZE ");
		List<Object> list = new ArrayList<Object>();
		SQLUtil.initSQLInsertValues(paramMap, sql, list, 
				"merch_id", "prize_id", "prize_name", "score", "create_date", "create_time", "status" );
		LOG.debug("----------param:"+list);
		LOG.debug("----------sql:"+sql.toString());
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//修改积分兑换商品
	@Override
	public void updateExchangePrize (Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectExchangePrize paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		
		sql.append("update exchange_prize set ");
		sql.append(SQLUtil.initSQLEqual(paramMap, list, SQLFlag.COMMA, "prize_name", "status", "score"));
		sql.append(" where 1=1 ");
		SQLUtil.initSQLEqual(paramMap, sql, list, "merch_id", "prize_id");
		
		this.executeSQL(sql.toString(), list.toArray());
//		this.executeBatchSQL(sql.toString(), list.toArray());
	}
	
	//查询积分兑换商品
	@Override
	public List<Map<String, Object>> selectExchangePrize (Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectExchangePrize paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		Integer pageIndex = MapUtil.getInt(paramMap, "page_index", 1);
		Integer pageSize = MapUtil.getInt(paramMap, "page_size", 20);
		sql.append("SELECT MERCH_ID, PRIZE_ID, PRIZE_NAME, SCORE, CREATE_DATE, CREATE_TIME, STATUS FROM EXCHANGE_PRIZE WHERE 1=1 ");
		List<Object> list = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap, sql, list, 
				"merch_id", "prize_id", "score", "create_date", "create_time", "status" );
		String prizeName = MapUtil.getString(paramMap, "prize_name");
		if(!StringUtil.isBlank(prizeName)){
			sql.append(" AND prize_name like ? ");
			list.add(prizeName+"%");
		}
		LOG.debug("----------param:"+list);
		LOG.debug("----------sql:"+sql.toString());
		sql.append(" ORDER BY CREATE_DATE DESC, CREATE_TIME DESC ");
		Page pageResult = this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
//		return  this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	//插入积分兑换流水
	@Override
	public void insertScoreExchange (Map<String, Object> paramMap) throws Exception {
		LOG.debug("insertScoreExchange paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO SCORE_EXCHANGE ");
		List<Object> list = new ArrayList<Object>();
		SQLUtil.initSQLInsertValues(paramMap, sql, list, 
				"merch_id", "exchange_id", "consumer_id", "prize_id", "prize_name", "score", 
				"exchange_date", "exchange_time" );
		LOG.debug("----------param:"+list);
		LOG.debug("----------sql:"+sql.toString());
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	
	//查询积分兑换流水
	@Override
	public List<Map<String, Object>> searchScoreExchange (Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchScoreExchange paramMap:"+paramMap);
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		int pageIndex = MapUtil.getInt(paramMap, "page_index", 1);
		int pageSize = MapUtil.getInt(paramMap, "page_size", 20);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT EXCHANGE_ID, SE.MERCH_ID, BC.CONSUMER_ID, PRIZE_ID, PRIZE_NAME, SCORE, EXCHANGE_DATE, EXCHANGE_TIME, " );
		sql.append("CONSUMER_NAME, TELEPHONE, CARD_ID, TOPSCORE, CURSCORE ");
		sql.append("FROM SCORE_EXCHANGE SE, BASE_MERCH_CONSUMER BC ");
		sql.append("WHERE SE.MERCH_ID = BC.MERCH_ID AND SE.CONSUMER_ID = BC.CONSUMER_ID ");
		List<Object> list = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap, sql, list, "exchange_id", "se.consumer_id", "se.merch_id", "prize_id" );
		String minScore = MapUtil.getString(paramMap, "min_score");
		String maxScore = MapUtil.getString(paramMap, "max_score");
		String prize_name = MapUtil.getString(paramMap, "prize_name");
		if(!StringUtil.isBlank(prize_name)){
			sql.append(" and prize_name like ? ");
			list.add(prize_name+"%");
		}
		if(!StringUtil.isBlank(minScore) && !StringUtil.isBlank(maxScore)){
			sql.append("and score >= ? and score <= ? ");
			list.add(minScore);
			list.add(maxScore);
		}
		if(!StringUtil.isBlank(startDate)){
			sql.append(" AND EXCHANGE_DATE >= ? ");
			list.add(startDate);
		}
		if(!StringUtil.isBlank(endDate)){
			sql.append(" AND EXCHANGE_DATE <= ? ");
			list.add(endDate);
		}
		sql.append(" ORDER BY EXCHANGE_DATE DESC, EXCHANGE_TIME DESC ");
		
		LOG.debug("----------param:"+list);
		LOG.debug("----------sql:"+sql.toString());
		Page pageResult = this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
//		return  this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	//////////////////////

	/* 
	 * 按年龄性别分组
	 */
	@Override
	public List<Map<String, Object>> searchMerchCustomerByAgeSex(
			Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerDaoImpl searchMerchCustomerByAgeSex paramMap:"+paramMap);
		
		StringBuilder sql=new StringBuilder();
		sql.append("select (to_number(to_char(sysdate, 'yyyy'))-to_number((substr(BIRTHDAY,0,4)))) age ");
		sql.append(" ,gender,count(*) customer_number from base_merch_consumer where 1=1 ");
		//sql.append(" and (to_number(to_char(sysdate, 'yyyy'))-to_number((substr(BIRTHDAY,0,4))))>=18 ");
		List<Object> paramList=new ArrayList<Object>();
		SQLUtil.initSQLIn(paramMap, sql, paramList, "merch_id");
		sql.append(" group by gender,(to_number(to_char(sysdate, 'yyyy'))-to_number((substr(BIRTHDAY,0,4)))) ");
		
		return this.selectBySqlQuery(sql.toString(), paramList.toArray());
	}

	/* 
	 * 按学历分组
	 */
	@Override
	public List<Map<String, Object>> searchMerchCustomerByEdu(
			Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerDaoImpl searchMerchCustomerByEdu paramMap:"+paramMap);
		
		StringBuilder sql=new StringBuilder();
		sql.append("select degree,count(*) customer_number from base_merch_consumer where 1=1 ");
		List<Object> paramList=new ArrayList<Object>();
		SQLUtil.initSQLIn(paramMap, sql, paramList, "merch_id");
		sql.append(" group by degree");
		
		return this.selectBySqlQuery(sql.toString(), paramList.toArray());
	}

	/* 
	 * 按收入分组
	 */
	@Override
	public List<Map<String, Object>> searchMerchCustomerByMoney(
			Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerDaoImpl searchMerchCustomerByMoney paramMap:"+paramMap);
		
		StringBuilder sql=new StringBuilder();
		sql.append("select month_salary,count(*) customer_number from base_merch_consumer where 1=1 ");
		List<Object> paramList=new ArrayList<Object>();
		SQLUtil.initSQLIn(paramMap, sql, paramList, "merch_id");
		sql.append(" group by month_salary");
		
		return this.selectBySqlQuery(sql.toString(), paramList.toArray());
	}
	
	
	//会员行为分析
	@Override
	public List<Map<String, Object>> searchConsumeBehaviorAnalysis(Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerDaoImpl searchConsumeBehaviorAnalysis paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		int pageIndex = MapUtil.getInt(paramMap, "page_index", 1);
		int pageSize = MapUtil.getInt(paramMap, "page_size", 20);
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
//		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
//		sql.append("select mc.consumer_id, mc.consumer_name, mc.card_id, mc.telephone, sol.sum_qty_ord_total,");
//		sql.append(" so.sum_amtys_ord_total, so.amt_ord_profit, so.count from base_merch_consumer mc, ");
//		sql.append(" (select consumer_id, sum(qty_ord_total) sum_qty_ord_total, sum(amtys_ord_total) sum_amtys_ord_total,");
//		sql.append(" sum(amt_ord_profit) amt_ord_profit, count(*) count  ");
//		sql.append("from sale_order where merch_id=? group by consumer_id) so,")
//		list.add(MapUtil.getString(paramMap, "merch_id"));
//		sql.append(" (select so.consumer_id, sum(case when bmi.item_kind_id='01' then qty_ord*sol.unit_ratio/bmi.big_unit_ratio else qty_ord end) sum_qty_ord_total ");
//		sql.append(" from sale_order so, sale_order_line sol, base_merch_item bmi");
//		sql.append(" where so.order_id=sol.order_id and sol.merch_id=bmi.merch_id and sol.item_id=bmi.item_id and so.merch_id=?");
//		list.add(MapUtil.getString(paramMap, "merch_id"));
//		SQLUtil.initSQLEqual(paramMap, sql, list, "bmi.item_kind_id");
//		sql.append(" group by so.consumer_id) sol where mc.consumer_id = so.consumer_id  and so.consumer_id=sol.consumer_id");
//		sql.append(" and mc.merch_id=? order by sum_amtys_ord_total desc");
//		list.add(MapUtil.getString(paramMap, "merch_id"));
		
		
		
//		sql.append("select mc.consumer_id, mc.consumer_name, mc.card_id, mc.telephone, sol.sum_qty_ord_total,");
//		sql.append("so.sum_amtys_ord_total, so.amt_ord_profit, so.count ");
//		sql.append("from base_merch_consumer mc, ");
//		sql.append("( ");
//		sql.append("select consumer_id, sum(qty_ord_total) sum_qty_ord_total, sum(amtys_ord_total) sum_amtys_ord_total,");
//		sql.append("sum(amt_ord_profit) amt_ord_profit, count(*) count ");
//		sql.append("from sale_order where merch_id=? ");//--------------merch_id
//		list.add(merchId);
//		if(!StringUtil.isBlank(startDate)){
//			sql.append("and ORDER_DATE >= ? ");//------------start_date
//			list.add(startDate);
//		}
//		if(!StringUtil.isBlank(endDate)){
//			sql.append("and ORDER_DATE <= ? ");//------------end_date
//			list.add(endDate);
//		}
//		sql.append("group by consumer_id ");
//		sql.append(") so, ");
//		sql.append("( ");
//		sql.append("select so.consumer_id,");
//		sql.append("sum(case when bmi.item_kind_id='01' then qty_ord*sol.unit_ratio/bmi.big_unit_ratio else qty_ord end) sum_qty_ord_total ");
//		sql.append("from sale_order so, sale_order_line sol, base_merch_item bmi ");
//		sql.append("where so.order_id=sol.order_id and sol.merch_id=bmi.merch_id ");
//		sql.append("and sol.item_id=bmi.item_id and so.merch_id=? ");//-------merch_id
//		list.add(merchId);
//		if(!StringUtil.isBlank(itemKindId)){
//			sql.append("and bmi.item_kind_id = ? ");//------------item_kind_id
//			list.add(itemKindId);
//		}
//		sql.append("group by so.consumer_id ");
//		sql.append(") sol ");
//		sql.append("where mc.consumer_id = so.consumer_id  and so.consumer_id=sol.consumer_id ");
//		sql.append("and mc.merch_id = ? ");//-------------merch_id
//		list.add(merchId);
//		sql.append("order by sum_amtys_ord_total desc ");
		
//		sql.append("select count(*) count, bmc.consumer_id, max(bmc.card_id) card_id, max(bmc.consumer_name) consumer_name, ");
//		sql.append("max(bmc.telephone) telephone, sum(sos.amtys_ord_total) sum_amtys_ord_total, ");
//		sql.append("sum(sos.qty_ord_total) sum_qty_ord_total, sum(amt_ord_profit) amt_ord_profit ");
//		sql.append("from base_merch_consumer bmc, ");
//		sql.append("( ");
//		sql.append("select so.order_id, max(so.consumer_id)consumer_id,");
//		sql.append("max(so.qty_ord_total)qty_ord_total, max(so.amtys_ord_total)amtys_ord_total, max(so.amt_ord_total)amt_ord_total, ");
//		sql.append("max(so.amt_ord_change)amt_ord_change, max(so.amt_ord_loss)amt_ord_loss, max(so.amt_ord_profit)amt_ord_profit, ");
//		sql.append("max(so.adjusted_amount)adjusted_amount ");
//		sql.append("from sale_order so, sale_order_line sol, base_merch_item bmi ");
//		sql.append("where so.order_id = sol.order_id and sol.item_id = bmi.item_id and so.merch_id = bmi.merch_id ");
//		sql.append("and so.merch_id = ? ");//---------------merch_id
//		list.add(merchId);
//		if(!StringUtil.isBlank(itemKindId)){
//			sql.append("and bmi.item_kind_id = ? ");//---------item_kind_id
//			list.add(itemKindId);
//		}
//		if(!StringUtil.isBlank(startDate)){
//			sql.append("and so.order_date >= ? ");//-------start_date
//			list.add(startDate);
//		}
//		if(!StringUtil.isBlank(endDate)){
//			sql.append("and so.order_date <= ? ");//--------end-date
//			list.add(endDate);
//		}
//		
//		sql.append("and consumer_id  is not null ");
//		sql.append("group by so.order_id ");
//		sql.append(")sos ");
//		sql.append("where sos.consumer_id = bmc.consumer_id ");
//		sql.append("group by bmc.consumer_id ");
//		sql.append("order by sum_amtys_ord_total desc ");
		
		sql.append("select so.consumer_id, max(bmc.consumer_name) consumer_name , max(bmc.card_id) card_id, ");
		sql.append("max(bmc.telephone) telephone, count(*) count, sum(sol.amt_ord) sum_amtys_ord_total, ");
//		sql.append("sum(sol.qty_ord) sum_qty_ord_total, " );
		sql.append("sum(case when (bmi.ITEM_KIND_ID = '01' or bmi.ITEM_KIND_ID = '0102' ) and SOL.UNIT_RATIO = '1'  then sol.qty_ord/bmi.BIG_UNIT_RATIO else sol.qty_ord  end ) sum_qty_ord_total, ");
		sql.append("sum(sol.profit*sol.qty_ord*sol.UNIT_RATIO ) amt_ord_profit, ");
		sql.append("sum(sol.adjusted_amount)sum_adjusted_amount ");
		sql.append("from sale_order so, sale_order_line sol, base_merch_item bmi, base_merch_consumer bmc ");
		sql.append("where so.order_id = sol.order_id and sol.item_id = bmi.item_id and so.merch_id = bmi.merch_id ");
		sql.append("and bmc.consumer_id = so.consumer_id and so.merch_id = sol.merch_id ");
		sql.append("and so.merch_id = ? ");//---------------merch_id
		list.add(merchId);
		SQLUtil.initSQLEqual(paramMap, sql, list, "bmi.item_kind_id");//---------item_kind_id
		if(!StringUtil.isBlank(startDate)){
			sql.append("and so.order_date||so.order_time >= ? ");//-------start_date
			list.add(startDate);
		}
		if(!StringUtil.isBlank(endDate)){
			sql.append("and so.order_date||so.order_time <= ? ");//--------end-date
			list.add(endDate);
		}
		
		sql.append("and so.consumer_id  is not null ");
		sql.append("group by so.consumer_id ");
		sql.append("order by sum_amtys_ord_total desc ");
		
		Page pageResult = this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
}
