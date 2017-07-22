package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.IMerchSalePromotionDao;
@Repository("merchSalePromotionDao")
public class MerchSalePromotionDaoImpl extends BaseDaoImpl implements IMerchSalePromotionDao {
	private Logger logger=LoggerFactory.getLogger(MerchSalePromotionDaoImpl.class);
	//插入
	public static final String insSalePromotion=initInsertSalePromotionSql();
	private static String initInsertSalePromotionSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("insert into MERCH_SALE_PROMOTION(PROMOTION_ID,MERCH_ID,PROMOTION_DESCRIPTION,PROMOTION_START_DATE,PROMOTION_END_DATE,CREATE_DATE,CREATE_TIME,FILE_ID) ");
		sb.append("values (?,?,?,?,?,?,?,?)");
		return sb.toString();
	}
	@Override
	public void insertMerchSalePromotion(Map<String, Object> paramsMap)throws Exception{
		logger.debug("MerchSalePromotoinDaoImpl insertMerchSalePromotion paramsMap:"+paramsMap);
		String promotionId=(String) paramsMap.get("promotion_id");
		String merchId=(String) paramsMap.get("merch_id");
		String description=(String) paramsMap.get("promotion_description");
		String statrDate=(String) paramsMap.get("promotion_start_date");
		String endDate=(String) paramsMap.get("promotion_end_date");
		String createDate=(String) paramsMap.get("create_date");
		String createTime=(String) paramsMap.get("create_time");
		String fileId=(String) paramsMap.get("file_id");
		this.executeSQL(insSalePromotion, new Object[]{promotionId,merchId,description,statrDate,endDate,createDate,createTime,fileId});
	}
	//修改
	public static final String updSalePromotion=initUpdSalePromotionSql();
	private static String initUpdSalePromotionSql(){
		StringBuffer sb=new StringBuffer();
//		sb.append(" update MERCH_SALE_PROMOTION ");
//		sb.append(" set PROMOTION_DESCRIPTION=?,PROMOTION_START_DATE=?,PROMOTION_END_DATE=?, CREATE_DATE=?, CREATE_TIME=?, FILE_ID=?");
//		sb.append(" where PROMOTION_ID=? ");
		sb.append("UPDATE MERCH_SALE_PROMOTION SET PROMOTION_DESCRIPTION=?, PROMOTION_START_DATE=?, PROMOTION_END_DATE=?, FILE_ID=?");
		sb.append(" WHERE MERCH_ID=?");
		return sb.toString();
	}
	@Override
	public void updateMerchSalePromotion(Map<String, Object> paramsMap)throws Exception{
		logger.debug("MerchSalePromotionDaoImpl updateMerchSalePromotion paramsMap:"+paramsMap);
		StringBuffer sqlBuffer = new StringBuffer(updSalePromotion);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(paramsMap.get("promotion_description"));
		paramList.add(paramsMap.get("promotion_start_date"));
		paramList.add(paramsMap.get("promotion_end_date"));
		paramList.add(paramsMap.get("file_id"));
		paramList.add(paramsMap.get("merch_id"));
		this.executeSQL(sqlBuffer.toString(), paramList.toArray());
	}
	//删除
	public static final String deleteSalePromotion=initDelSalePromotionSql();
	private static String initDelSalePromotionSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("delete from MERCH_SALE_PROMOTION where MERCH_ID=?");
		return sb.toString();
	}
	@Override
	public void delMerchSalePromotion(Map<String, Object> paramsMap)throws Exception{
		logger.debug("MerchSalePromotionPromotionDaoImpl delMerchSalePromotion paramsMap:"+paramsMap);
		StringBuffer sqlBuffer = new StringBuffer(deleteSalePromotion);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(paramsMap.get("merch_id"));
		if(paramsMap.get("promotion_id")!=null) {
			sqlBuffer.append("AND PROMOTION_ID=?");
			paramList.add(paramsMap.get("promotion_id"));
		}
		this.executeSQL(sqlBuffer.toString(), paramList.toArray());
	}	
	// 查询
	public static final String selectSalePromotionSql = intiSelectSalePromotion();
	private static String intiSelectSalePromotion() {
		StringBuffer sb = new StringBuffer();
		sb.append("select PROMOTION_ID, MERCH_ID, CREATE_DATE, CREATE_TIME, PROMOTION_DESCRIPTION, PROMOTION_START_DATE, PROMOTION_END_DATE,");
		sb.append(" FILE_ID from MERCH_SALE_PROMOTION where 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selMerchSalePromotion( Map<String, Object> paramMap) throws Exception {
		logger.debug("MerchSalePromotionDaoImpl seMerchSalePromoton paramMap:"+paramMap);
		StringBuffer sqlBuffer = new StringBuffer(selectSalePromotionSql);
		List<Object> paramList = new ArrayList<Object>();
		if(paramMap.get("promotion_id")!=null) {
			sqlBuffer.append(" AND PROMOTION_ID=?");
			paramList.add(paramMap.get("promotion_id"));
		}
		if(paramMap.get("merch_id")!=null) {
			sqlBuffer.append(" AND MERCH_ID=?");
			paramList.add(paramMap.get("merch_id"));
		}
		if(paramMap.get("create_date_floor")!=null) {
			sqlBuffer.append(" AND CREATE_DATE>=?");
			paramList.add(paramMap.get("create_date_floor"));
		}
		if(paramMap.get("create_date_ceiling")!=null) {
			sqlBuffer.append(" AND CREATE_DATE<=?");
			paramList.add(paramMap.get("create_date_ceiling"));
		}
		if(paramMap.get("promotion_start_date_floor")!=null) {
			sqlBuffer.append(" AND PROMOTION_START_DATE>=?");
			paramList.add(paramMap.get("promotion_start_date_floor"));
		}
		if(paramMap.get("promotion_start_date_ceiling")!=null) {
			sqlBuffer.append(" AND PROMOTION_START_DATE<=?");
			paramList.add(paramMap.get("promotion_start_date_ceiling"));
		}
		sqlBuffer.append(" ORDER BY CREATE_DATE DESC, CREATE_TIME DESC");
		return selectBySqlQuery(sqlBuffer.toString(),paramList.toArray());
	}
}
