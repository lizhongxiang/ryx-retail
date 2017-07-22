package com.ryx.social.retail.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import com.ryx.framework.util.SpellUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.SQLUtil;

@Repository
public class ItemDaoImpl extends BaseDaoImpl implements IItemDao {
	
	private static Logger LOG = LoggerFactory.getLogger(ItemDaoImpl.class);
	
	/**
	 * 查询基本商品表, 只查询STATUS!='0'的商品, 在Service层增加flag='BASE'字段标识从base_item表获取的数据, 
	 */
	public static final String selectItemSql = initSelectItemSql();
	private static String initSelectItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ITEM_ID, ITEM_BAR, ITEM_NAME, SHORT_CODE, SHORT_NAME, ITEM_KIND_ID, UNIT_NAME, PRI1, PRI4, STATUS");
		sb.append(" FROM BASE_ITEM WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectItem(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectItem itemParam: " + itemParam);
		StringBuffer sqlBuffer = new StringBuffer(selectItemSql);
		String itemId = (String) itemParam.get("item_id");
		List<Object> paramObject = new ArrayList<Object>();
		String itemBar = (String) itemParam.get("item_bar");
		String status = (String) itemParam.get("status");
		Map<String, Object> orderByMap = (Map<String, Object>) itemParam.get("order_by");
		
		// 排序
		StringBuffer orderByBuffer = new StringBuffer();
		if(orderByMap!=null) {
			for(String orderBy : orderByMap.keySet()) {
				if("1".equals(orderByMap.get(orderBy))) {
					orderByBuffer.append(" " + orderBy + " DESC");
				} else {
					orderByBuffer.append(" " + orderBy + " ASC");
				}
			}
		}
		orderByBuffer.append(" ITEM_ID ASC"); // 默认按照item_id正序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 分页
		Integer pageIndex = 1;
		if(itemParam.get("page_index")!=null) {
			pageIndex = Integer.parseInt(itemParam.get("page_index").toString());
		}
		Integer pageSize = 20;
		if(itemParam.get("page_size")!=null) {
			pageSize = Integer.parseInt(itemParam.get("page_size").toString());
		}
		
		// 查询, item_id和item_bar互斥, 如果都不传强制返回空list
		if(itemId!=null){
			String[] myItemId=itemId.split(",");
			if(myItemId.length>1){
				sqlBuffer.append("  AND ITEM_ID in (  ");
				for (int i = 0; i < myItemId.length; i++) {
					if(myItemId[i]==null||myItemId[i].equals("")){
						continue;
					}
					paramObject.add(myItemId[i]);
					sqlBuffer.append("  ?  ");
					if(i!=myItemId.length-1){
						sqlBuffer.append("  ,  ");
					}
				}
				sqlBuffer.append(" ) ");
			}else if(myItemId.length==1){
				sqlBuffer.append(" AND ITEM_ID = ? ");
				paramObject.add(myItemId[0]);
			}
		}else if(itemBar!=null) {
			sqlBuffer.append(" AND ITEM_BAR = ?");
			paramObject.add(itemBar);
		} else {
			return new ArrayList<Map<String, Object>>();
		}
		if(status!=null) {
			String[] statusArray = status.split(",");
			if(statusArray.length==1) {
				sqlBuffer.append(" AND STATUS = ?");
				paramObject.add(status);
			} else {
				sqlBuffer.append(" AND STATUS IN (");
				for(int i=0; i<statusArray.length; i++) {
					paramObject.add(statusArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), pageIndex, pageSize, paramObject.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	@Override
	public List<Map<String, Object>> selectNoRemovedItem(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectNoRemovedItem itemParam: " + itemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' FLAG, BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_NAME,BMI.SHORT_CODE,BMI.SHORT_NAME,BMI.ITEM_KIND_ID,");
		sqlBuilder.append("BMIu.BIG_BAR,BMIu.BIG_UNIT_NAME,BMIu.pri4 BIG_PRI4,BMI.UNIT_NAME,BMI.COST,BMI.PRI1,BMI.PRI2,BMI.PRI4,");
		sqlBuilder.append("BMIU.UNIT_RATIO,BMI.STATUS,BMI.SPEC,ROWNUM,BMIU.SEQ_ID,BMIU.ITEM_BAR,BMIU.ITEM_UNIT_NAME");
		sqlBuilder.append(" FROM BASE_MERCH_ITEM BMI,BASE_MERCH_ITEM_UNIT BMIU");
		sqlBuilder.append(" WHERE BMI.MERCH_ID=BMIU.MERCH_ID AND BMI.ITEM_ID=BMIU.ITEM_ID AND BMI.STATUS IN ('1','2')");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "BMIU.merch_id", "BMIU.big_bar");
		
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	@Override
	public List<Map<String, Object>> selectRemovedItem(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectRemovedItem itemParam: " + itemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' FLAG, BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_NAME,BMI.SHORT_CODE,BMI.SHORT_NAME,BMI.ITEM_KIND_ID,");
		sqlBuilder.append("BMI.BIG_BAR,BMI.BIG_UNIT_NAME,BMI.BIG_PRI4,BMI.UNIT_NAME,BMI.COST,BMI.PRI1,BMI.PRI2,BMI.PRI4,");
		sqlBuilder.append("BMIU.UNIT_RATIO,BMI.STATUS,BMI.SPEC,ROWNUM,BMIU.SEQ_ID,BMIU.ITEM_BAR,BMIU.ITEM_UNIT_NAME");
		sqlBuilder.append(" FROM BASE_MERCH_ITEM BMI,BASE_MERCH_ITEM_UNIT BMIU");
		sqlBuilder.append(" WHERE BMI.MERCH_ID=BMIU.MERCH_ID AND BMI.ITEM_ID=BMIU.ITEM_ID AND BMI.STATUS=0");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "BMIU.merch_id", "BMIU.big_bar");
		
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	@Override
	public List<Map<String, Object>> selectNoRemovedItemAndWhse(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectNoRemovedItemAndWhse itemParam: " + itemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' FLAG, BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_NAME,BMI.SHORT_CODE,BMI.SHORT_NAME,BMI.ITEM_KIND_ID,");
		sqlBuilder.append("BMIu.BIG_BAR,BMIu.BIG_UNIT_NAME,BMIu.pri4 BIG_PRI4,BMI.UNIT_NAME,BMI.COST,BMI.PRI1,BMI.PRI2,BMI.PRI4,");
		sqlBuilder.append("BMIU.UNIT_RATIO,BMI.STATUS,BMI.SPEC,ROWNUM,BMIU.SEQ_ID,BMIU.ITEM_BAR,BMIU.ITEM_UNIT_NAME,");
		sqlBuilder.append("WM.QTY_WHSE,WM.QTY_LOCKED,WM.QTY_WHSE_WARN,WM.OUTPUT_DATE,WM.QTY_WHSE_INIT,WM.WHSE_INIT_DATE");
		sqlBuilder.append(" FROM BASE_MERCH_ITEM BMI,BASE_MERCH_ITEM_UNIT BMIU,WHSE_MERCH WM");
		sqlBuilder.append(" WHERE BMI.MERCH_ID=BMIU.MERCH_ID AND BMI.ITEM_ID=BMIU.ITEM_ID AND BMI.MERCH_ID=WM.MERCH_ID AND BMI.ITEM_ID=WM.ITEM_ID AND BMI.STATUS IN ('1','2')");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "BMIU.merch_id");
		SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "BMIU.big_bar");
		
		if (itemParam.containsKey("not_item_kind_id")) {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("item_kind_id", MapUtil.getString(itemParam, "not_item_kind_id"));
	        SQLUtil.initSQLNotIn(param, sqlBuilder, paramList, "BMI.item_kind_id");
		}
		
        SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "BMI.item_kind_id");
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	@Override
	public List<Map<String, Object>> selectRemovedItemAndWhse(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectRemovedItemAndWhse itemParam: " + itemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' FLAG, BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_NAME,BMI.SHORT_CODE,BMI.SHORT_NAME,BMI.ITEM_KIND_ID,");
		sqlBuilder.append("BMIu.BIG_BAR,BMIu.BIG_UNIT_NAME,BMIu.pri4 BIG_PRI4,BMI.UNIT_NAME,BMI.COST,BMI.PRI1,BMI.PRI2,BMI.PRI4,");
		sqlBuilder.append("BMIU.UNIT_RATIO,BMI.STATUS,BMI.SPEC,ROWNUM,BMIU.SEQ_ID,BMIU.ITEM_BAR,BMIU.ITEM_UNIT_NAME,");
		sqlBuilder.append("WM.QTY_WHSE,WM.QTY_LOCKED,WM.QTY_WHSE_WARN,WM.OUTPUT_DATE,WM.QTY_WHSE_INIT,WM.WHSE_INIT_DATE");
		sqlBuilder.append(" FROM BASE_MERCH_ITEM BMI,BASE_MERCH_ITEM_UNIT BMIU,WHSE_MERCH WM");
		sqlBuilder.append(" WHERE BMI.MERCH_ID=BMIU.MERCH_ID AND BMI.ITEM_ID=BMIU.ITEM_ID AND BMI.MERCH_ID=WM.MERCH_ID AND BMI.ITEM_ID=WM.ITEM_ID AND BMI.STATUS=0");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "BMIU.merch_id", "BMIU.big_bar");
		
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	
	/**
	 * 查询item_unit计量单位表, 按照status查询, 无分页
	 */
	public static final String selectItemUnitSql = initSelectItemUnitSql();
	private static String initSelectItemUnitSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT UNIT_ID, UNIT_NAME, UNIT_CODE, STATUS,UNIT_SPELL FROM BASE_ITEM_UNIT WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectItemUnit(Map<String, Object> itemUnitParam) throws Exception {
		LOG.debug("ItemDaoImpl selectItemUnit itemUnitParam: " + itemUnitParam);
		StringBuffer sqlBuffer = new StringBuffer(selectItemUnitSql);
		String status = (String) itemUnitParam.get("status");
		Map<String, Object> orderByMap = (Map<String, Object>) itemUnitParam.get("order_by");
		
		// 排序, 默认按照UNIT_ID正序排列
		StringBuffer orderByBuffer = new StringBuffer();
		if(orderByMap!=null) {
			for(String orderBy : orderByMap.keySet()) {
				if("1".equals(orderByMap.get(orderBy))) {
					orderByBuffer.append(" " + orderBy + " DESC");
				} else {
					orderByBuffer.append(" " + orderBy + " ASC");
				}
			}
		}
		orderByBuffer.append(" UNIT_ID ASC"); // 默认按照unit_id正序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 查询
		List<Object> paramObject = new ArrayList<Object>();
		if(status!=null) {
			String[] statusArray = status.split(",");
			if(statusArray.length==1) {
				sqlBuffer.append(" AND STATUS = ?");
				paramObject.add(status);
			} else {
				sqlBuffer.append(" AND STATUS IN (");
				for(int i=0; i<statusArray.length; i++) {
					paramObject.add(statusArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		return this.selectBySqlQuery(sqlBuffer.append(orderByBuffer.toString()).toString(), paramObject.toArray());
	}
	
	
	/**
	 * 查询商品分类表, 无分页
	 */
	public static final String selectItemKindSql = initSelectItemKindSql();
	private static String initSelectItemKindSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ITEM_KIND_ID, ITEM_KIND_NAME, ITEM_KIND_SHORT_NAME, ITEM_KIND_CODE, ITEM_KIND_LEVEL, ITEM_KIND_PARENT, STATUS");
		sb.append(" FROM BASE_ITEM_KIND WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectItemKind(Map<String, Object> itemKindParam) throws Exception {
		LOG.debug("ItemDaoImpl selectItemKind itemKindParam" + itemKindParam);
		StringBuffer sqlBuffer = new StringBuffer(selectItemKindSql);
		String itemKindId = (String) itemKindParam.get("item_kind_id");
		String itemKindParent = (String) itemKindParam.get("item_kind_parent");
		String itemKindLevel = (String) itemKindParam.get("item_kind_level");
		String status = (String) itemKindParam.get("status");
		Map<String, Object> orderByMap = (Map<String, Object>) itemKindParam.get("order_by");
		
		// 排序
		StringBuffer orderByBuffer = new StringBuffer();
		if(orderByMap!=null) {
			for(String orderBy : orderByMap.keySet()) {
				if("1".equals(orderByMap.get(orderBy))) {
					orderByBuffer.append(" " + orderBy + " DESC");
				} else {
					orderByBuffer.append(" " + orderBy + " ASC");
				}
			}
		}
		orderByBuffer.append(" ITEM_KIND_ID ASC"); // 默认按照ITEM_KIND_ID正序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		if(status!=null) {
			String[] statusArray = status.split(",");
			if(statusArray.length==1) {
				sqlBuffer.append(" AND STATUS = ?");
				paramArray.add(statusArray[0]);
			} else {
				sqlBuffer.append(" AND STATUS IN (");
				for(int i=0; i<statusArray.length; i++) {
					paramArray.add(statusArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if(itemKindId!=null) {
			sqlBuffer.append(" AND ITEM_KIND_ID = ?");
			paramArray.add(itemKindId);
		}
		if(itemKindParent!=null) {
			sqlBuffer.append(" AND ITEM_KIND_PARENT = ?");
			paramArray.add(itemKindParent);
		}
		if(itemKindLevel!=null) {
			sqlBuffer.append(" AND ITEM_KIND_LEVEL = ?");
			paramArray.add(itemKindLevel);
		}
		return this.selectBySqlQuery(sqlBuffer.append(orderByBuffer.toString()).toString(), paramArray.toArray());
	}
	
	/**
	 * 查询商户商品表BASE_MERCH_ITEM, 如果需要按照item_kind_id模糊查询, 则需要传__item_kind_id_like_flag, item_kind_id为6位
	 */
	public static final String selectMerchItemSql = initSeleteMerchItemSql();
	private static String initSeleteMerchItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, ITEM_ID, ITEM_BAR, ITEM_NAME, SHORT_CODE, SHORT_NAME, SPEC, ITEM_KIND_ID, UNIT_NAME,");
		sb.append(" COST, PRI1, PRI2, PRI4, STATUS FROM BASE_MERCH_ITEM WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectMerchItem(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectMerchItem itemParam: " + itemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' flag, MERCH_ID, ITEM_ID, ITEM_BAR, ITEM_NAME, SHORT_NAME, SHORT_CODE, ITEM_KIND_ID,");
		sqlBuilder.append(" UNIT_NAME, COST, PRI1, PRI2, PRI4, BIG_BAR, BIG_UNIT_NAME, BIG_PRI4, BIG_UNIT_RATIO unit_ratio, STATUS, SPEC FROM BASE_MERCH_ITEM WHERE");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "merch_id");
		SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "item_id");
		SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "status");
		SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "item_bar");
		
		String itemKindId = MapUtil.getString(itemParam, "item_kind_id");
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sqlBuilder.append(" AND ( ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sqlBuilder.append(" OR ITEM_KIND_ID = ? ) ");
			paramList.add("01");
			paramList.add("02");
			paramList.add("03");
			paramList.add("04");
			paramList.add("05");
			paramList.add("06");
			paramList.add("07");
			paramList.add("08");
			paramList.add("0102");
			paramList.add("99");
		}else{
			SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "item_kind_id");
		}
		if(MapUtil.getBoolean(itemParam, "isUnqualified")){
			sqlBuilder.append(" AND (PRI4<COST OR COST*5<=PRI4 OR COST<=0 OR PRI4<=0 OR PRI1<=0) ");
		}
		String floor_big_pri4 = MapUtil.getString(itemParam, "floor_big_pri4");
		String ceiling_big_pri4 = MapUtil.getString(itemParam, "ceiling_big_pri4");
		if(!floor_big_pri4.equals("") && !ceiling_big_pri4.equals("")){
			SQLUtil.initSQLBetweenAnd(itemParam, sqlBuilder, paramList, "BIG_PRI4", "floor_big_pri4", "ceiling_big_pri4");
		}
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		/*
		return this.selectBySqlQuery(sqlBuilder.toString(), paramList.toArray());
		*/
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	
	@Override
	public List<Map<String, Object>> selectNotRemovedMerchItem(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectNotRemovedMerchItem merchItemParam: " + merchItemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' flag, MERCH_ID, ITEM_ID, ITEM_BAR, ITEM_NAME, SHORT_NAME, SHORT_CODE, ITEM_KIND_ID,");
		sqlBuilder.append(" UNIT_NAME, COST, PRI1, PRI2, PRI4, BIG_BAR, BIG_UNIT_NAME, BIG_PRI4, BIG_UNIT_RATIO unit_ratio, STATUS, SPEC FROM BASE_MERCH_ITEM WHERE STATUS IN (1,2)");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(merchItemParam, sqlBuilder, paramList, "merch_id", "item_bar");
		SQLUtil.initSQLIn(merchItemParam, sqlBuilder, paramList, "item_id");
		
		/*String itemKindId = MapUtil.getString(merchItemParam, "item_kind_id");
		if (itemKindId.equals("99")) {
			sqlBuilder.append(" AND ( ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sqlBuilder.append(" OR ITEM_KIND_ID = ? ) ");
			paramList.add("01");
			paramList.add("02");
			paramList.add("03");
			paramList.add("04");
			paramList.add("05");
			paramList.add("06");
			paramList.add("07");
			paramList.add("08");
			paramList.add("0102");
			paramList.add("99");
		}else{
			SQLUtil.initSQLIn(merchItemParam, sqlBuilder, paramList, "item_kind_id");
		}
		if(MapUtil.getBoolean(merchItemParam, "isUnqualified")){
			sqlBuilder.append(" AND (PRI4<COST OR COST*5<=PRI4 OR COST<=0 OR PRI4<=0 OR PRI1<=0) ");
		}
		String floor_big_pri4 = MapUtil.getString(merchItemParam, "floor_big_pri4");
		String ceiling_big_pri4 = MapUtil.getString(merchItemParam, "ceiling_big_pri4");
		if(!floor_big_pri4.equals("") && !ceiling_big_pri4.equals("")){
			SQLUtil.initSQLBetweenAnd(merchItemParam, sqlBuilder, paramList, "BIG_PRI4", "floor_big_pri4", "ceiling_big_pri4");
		}*/
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(merchItemParam, "page_index", 1), MapUtil.getInt(merchItemParam, "page_size", 20), paramList.toArray());
		merchItemParam.put("page_count", pageResult.getPageSum());
		merchItemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	@Override
	public List<Map<String, Object>> selectRemovedMerchItem(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectNotRemovedMerchItem merchItemParam: " + merchItemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' flag, MERCH_ID, ITEM_ID, ITEM_BAR, ITEM_NAME, SHORT_NAME, SHORT_CODE, ITEM_KIND_ID,");
		sqlBuilder.append(" UNIT_NAME, COST, PRI1, PRI2, PRI4, BIG_BAR, BIG_UNIT_NAME, BIG_PRI4, BIG_UNIT_RATIO unit_ratio, STATUS, SPEC FROM BASE_MERCH_ITEM WHERE STATUS=0");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(merchItemParam, sqlBuilder, paramList, "merch_id", "item_bar");
		SQLUtil.initSQLIn(merchItemParam, sqlBuilder, paramList, "item_id");
		
		/*String itemKindId = MapUtil.getString(merchItemParam, "item_kind_id");
		if (itemKindId.equals("99")) {
			sqlBuilder.append(" AND ( ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sqlBuilder.append(" OR ITEM_KIND_ID = ? ) ");
			paramList.add("01");
			paramList.add("02");
			paramList.add("03");
			paramList.add("04");
			paramList.add("05");
			paramList.add("06");
			paramList.add("07");
			paramList.add("08");
			paramList.add("0102");
			paramList.add("99");
		}else{
			SQLUtil.initSQLIn(merchItemParam, sqlBuilder, paramList, "item_kind_id");
		}
		if(MapUtil.getBoolean(merchItemParam, "isUnqualified")){
			sqlBuilder.append(" AND (PRI4<COST OR COST*5<=PRI4 OR COST<=0 OR PRI4<=0 OR PRI1<=0) ");
		}
		String floor_big_pri4 = MapUtil.getString(merchItemParam, "floor_big_pri4");
		String ceiling_big_pri4 = MapUtil.getString(merchItemParam, "ceiling_big_pri4");
		if(!floor_big_pri4.equals("") && !ceiling_big_pri4.equals("")){
			SQLUtil.initSQLBetweenAnd(merchItemParam, sqlBuilder, paramList, "BIG_PRI4", "floor_big_pri4", "ceiling_big_pri4");
		}*/
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(merchItemParam, "page_index", 1), MapUtil.getInt(merchItemParam, "page_size", 20), paramList.toArray());
		merchItemParam.put("page_count", pageResult.getPageSum());
		merchItemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}

	public List<Map<String, Object>> selectMerchItemAndUnit(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectMerchItemAndUnit itemParam: " + itemParam);
		StringBuilder sqlBuilder = new StringBuilder("SELECT 'MERCH' flag, bmi.MERCH_ID, bmi.ITEM_ID, bmi.ITEM_BAR, bmi.ITEM_NAME, bmi.ITEM_KIND_ID,");
		sqlBuilder.append(" bmi.UNIT_NAME, bmi.COST, bmi.PRI1, bmi.PRI2, bmi.PRI4, bmi.BIG_BAR manager_bar, bmi.BIG_UNIT_NAME, bmi.BIG_PRI4, bmi.BIG_UNIT_RATIO unit_ratio, bmi.STATUS, bmi.SPEC");
		sqlBuilder.append(" FROM BASE_MERCH_ITEM bmi, BASE_MERCH_ITEM_UNIT bmiu WHERE bmi.MERCH_ID=bmiu.MERCH_ID AND bmi.ITEM_ID=bmiu.ITEM_ID");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "merch_id", "item_bar");
		SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "item_id");
		SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "item_kind_id");
		SQLUtil.initSQLIn(itemParam, sqlBuilder, paramList, "status");
		SQLUtil.initSQLOrder(sqlBuilder, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	
	/**
	 * 查询商户商品属性表merch_item_unit
	 */
	public static final String selectMerchItemUnitSql = initSeleteMerchItemUnitSql();
	private static String initSeleteMerchItemUnitSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SEQ_ID, MERCH_ID, ITEM_ID, ITEM_BAR, ITEM_UNIT_NAME, BIG_BAR, BIG_UNIT_NAME, UNIT_RATIO, PRI4");
		sb.append(" FROM BASE_MERCH_ITEM_UNIT WHERE MERCH_ID = ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectMerchItemUnit(Map<String, Object> merchItemUnitParam) throws Exception {
		LOG.debug("ItemDaoImpl selectMerchItemUnit merchItemUnitParam: " + merchItemUnitParam);
		StringBuilder sqlBuffer = new StringBuilder(selectMerchItemUnitSql);
		String merchId = (String) merchItemUnitParam.get("merch_id");
		String itemId = (String) merchItemUnitParam.get("item_id");
		String itemBar = (String) merchItemUnitParam.get("item_bar");
		String bigBar = (String) merchItemUnitParam.get("big_bar");
		Map<String, Object> orderByMap = (Map<String, Object>) merchItemUnitParam.get("order_by");
		
		// 排序
		StringBuffer orderByBuffer = new StringBuffer();
		if(orderByMap!=null) {
			for(String orderBy : orderByMap.keySet()) {
				if("1".equals(orderByMap.get(orderBy))) {
					orderByBuffer.append(" " + orderBy + " DESC");
				} else {
					orderByBuffer.append(" " + orderBy + " ASC");
				}
			}
		}
		orderByBuffer.append(" SEQ_ID ASC"); // 默认按照SEQ_ID正序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		Integer pageIndex = MapUtil.getInt(merchItemUnitParam, "page_index", 1);
		Integer pageSize = MapUtil.getInt(merchItemUnitParam, "page_size", 20);
		
		/* 梁凯 2014年5月23日15:50:10
		 * 原来查询包装表时要求item_id和big_bar互斥, 现在不需要互斥
		 * 因为有时需要根据item_id和big_bar来唯一确定一个包装
		 */
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(merchId);
		if (!StringUtil.isBlank(itemId) || !StringUtil.isBlank(bigBar)) {
			SQLUtil.initSQLIn(merchItemUnitParam, sqlBuffer, paramObject, "item_id");
			SQLUtil.initSQLIn(merchItemUnitParam, sqlBuffer, paramObject, "big_bar");
		} else if (!StringUtil.isBlank(itemBar)) {
			SQLUtil.initSQLIn(merchItemUnitParam, sqlBuffer, paramObject, "item_bar");
		}
		
		SQLUtil.initSQLIn(merchItemUnitParam, sqlBuffer, paramObject, "seq_id");
		String finalSql = sqlBuffer.append(orderByBuffer.toString()).toString();
		Page pageResult = this.searchPaginatedBySql(finalSql, pageIndex, pageSize, paramObject.toArray());
		merchItemUnitParam.put("page_count", pageResult.getPageSum());
		merchItemUnitParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	public static final String selectTobaccoItemSql = initSelectTobaccoItemSql();
	private static String initSelectTobaccoItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SUPPLIER_ID, ITEM_ID, ITEM_BAR, ITEM_NAME, ITEM_UNIT_NAME, SHORT_CODE, SHORT_NAME, SPEC, ITEM_KIND_ID, KIND,");
		sb.append(" FACT_NAME, BRAND_NAME, BIG_BAR, BIG_UNIT_NAME, UNIT_RATIO, PRI_WSALE, PRI_DRTL, ITEM_CATAGORY, IS_TYPICAL, IS_NEW,");
		sb.append(" IS_RECOMMEND, IS_INSALE, describe, subjectColor, tipColor, tipType, tarContent, co, pack");
		sb.append(" FROM BASE_SUPPLIER_TOBACCO_ITEM WHERE 1=1");
		return sb.toString();
	}
	
	@Override
	public List<Map<String, Object>> selectTobaccoItem(Map<String, Object> tobaccoItemParam) throws Exception {
		LOG.debug("ItemDaoImpl selectTobaccoItem tobaccoItemParam: " + tobaccoItemParam);
		StringBuilder sql = new StringBuilder(selectTobaccoItemSql);
		List<Object> paramObject = new ArrayList<Object>();
		SQLUtil.initSQLEqual(tobaccoItemParam, sql, paramObject, "supplier_id:10370101", "item_id");// 如果没有supplier_id的话就用济南
		if(tobaccoItemParam.containsKey("bar")) {
			sql.append(" AND ? IN (ITEM_BAR, BIG_BAR)");
			paramObject.add(tobaccoItemParam.get("bar"));
		} else if(tobaccoItemParam.containsKey("item_bar")) {
			SQLUtil.initSQLIn(tobaccoItemParam, sql, paramObject, "item_bar");
		} else if(tobaccoItemParam.containsKey("big_bar")) {
			SQLUtil.initSQLIn(tobaccoItemParam, sql, paramObject, "big_bar");
		}
		sql.append(" ORDER BY ITEM_ID");
		return this.selectBySqlQuery(sql.toString(), paramObject.toArray());
	}
	
	/**
	 * 逻辑删除商户商品信息
	 */
	public static final String deleteMerchItemSql = initDeleteMerchItemSql();
	private static String initDeleteMerchItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE BASE_MERCH_ITEM SET STATUS = '0', MODIFIED_TIMESTAMP = ? WHERE MERCH_ID = ? AND ITEM_ID = ?");
		return sb.toString();
	}
	@Override
	public void deleteMerchItem(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemDaoImpl deleteMerchItem merchItemParam" + merchItemParam);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(merchItemParam.get("modified_timestamp"));
		paramList.add(merchItemParam.get("merch_id"));
		paramList.add(merchItemParam.get("item_id"));
		this.executeSQL(deleteMerchItemSql, paramList.toArray());
	}

	/**
	 * 更新商户商品表
	 */
	@Override
	public void updateMerchItem(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemDaoImpl updateMerchItem merchItemParam: " + merchItemParam);
		StringBuffer sqlBuffer = new StringBuffer("UPDATE BASE_MERCH_ITEM SET MODIFIED_TIMESTAMP = ?");
		List<Object> paramList = new ArrayList<Object>();
		
		paramList.add(merchItemParam.get("modified_timestamp"));
		
		String maxBigBar = MapUtil.getString(merchItemParam, "item_bar");
		String maxBigUnitName = MapUtil.getString(merchItemParam, "unit_name");
		BigDecimal maxBigPri4 = MapUtil.getBigDecimal(merchItemParam, "pri4");
		int maxUnitRatio = 1;
		// 如果是卷烟/雪茄 需要竞争管理单位
		if(ParamUtil.isTobacco(merchItemParam)) {
			List<Map<String, Object>> unitList = MapUtil.get(merchItemParam, "list", Collections.EMPTY_LIST);
			for(Map<String, Object> unitMap : unitList) {
				String operationType = MapUtil.getString(unitMap, "type");
				String bigBar = MapUtil.getString(unitMap, "big_bar");
				String bigUnitName = MapUtil.getString(unitMap, "big_unit_name");
				BigDecimal bigPri4 = MapUtil.getBigDecimal(unitMap, "pri4");
				int unitRatio = MapUtil.getInt(unitMap, "unit_ratio");
				// 删除的包装不会作为管理包装的候选, 记录没有删除的最大包装(如果相同取后一个)
				if(!"D".equalsIgnoreCase(operationType) && unitRatio>=maxUnitRatio) {
					maxBigBar = bigBar;
					maxBigUnitName = bigUnitName;
					maxBigPri4 = bigPri4;
					maxUnitRatio = unitRatio;
				}
			}
		}
		// 不是卷烟、雪茄，或者只有一个包装的卷烟、雪茄， 直接把最小单位作为管理包装
		//是卷烟，如果多个包装（比如：用户新增多个包装），取最后一个为管理包装
		merchItemParam.put("big_bar", maxBigBar);
		merchItemParam.put("big_unit_name", maxBigUnitName);
		merchItemParam.put("big_pri4", maxBigPri4);
		merchItemParam.put("big_unit_ratio", maxUnitRatio);
		
		if(merchItemParam.get("big_bar")!=null) {
			sqlBuffer.append(", BIG_BAR=?");
			paramList.add(merchItemParam.get("big_bar"));
		}
		if(merchItemParam.get("big_unit_name")!=null) {
			sqlBuffer.append(", BIG_UNIT_NAME=?");
			paramList.add(merchItemParam.get("big_unit_name"));
		}
		if(merchItemParam.get("big_pri4")!=null) {
			sqlBuffer.append(", BIG_PRI4=?");
			paramList.add(merchItemParam.get("big_pri4"));
		}
		if(merchItemParam.get("big_unit_ratio")!=null) {
			sqlBuffer.append(", BIG_UNIT_RATIO=?");
			paramList.add(merchItemParam.get("big_unit_ratio"));
		}
		
		if(merchItemParam.containsKey("item_bar")) {
			sqlBuffer.append(", ITEM_BAR=?");
			paramList.add(merchItemParam.get("item_bar"));
		}
		if(merchItemParam.containsKey("item_name")) {
			sqlBuffer.append(", ITEM_NAME=?");
			String itemName = MapUtil.getString(merchItemParam, "item_name");
			paramList.add(itemName);
			sqlBuffer.append(", SHORT_CODE=?");
			paramList.add(SpellUtil.getFullSpell(itemName));
			sqlBuffer.append(", SHORT_NAME=?");
			paramList.add(SpellUtil.getShortSpell(itemName));
		}
		
		if(merchItemParam.containsKey("spec")) {
			sqlBuffer.append(", SPEC=?");
			paramList.add(merchItemParam.get("spec"));
		}
		if(merchItemParam.get("item_kind_id")!=null && !"".equals(merchItemParam.get("item_kind_id"))) {
			sqlBuffer.append(", ITEM_KIND_ID=?");
			paramList.add(merchItemParam.get("item_kind_id"));
		} else {
			sqlBuffer.append(", ITEM_KIND_ID=?");
			paramList.add("99");
		}
		if(merchItemParam.get("unit_name")!=null) {
			String unitName = merchItemParam.get("unit_name").toString();
			sqlBuffer.append(", UNIT_NAME=?");
			paramList.add(unitName.trim());
		}
		if(merchItemParam.containsKey("cost")) {
			sqlBuffer.append(", COST=?");
			paramList.add(merchItemParam.get("cost"));
		}
		if(merchItemParam.containsKey("pri1")) {
			sqlBuffer.append(", PRI1=?");
			paramList.add(merchItemParam.get("pri1"));
		}
		if(merchItemParam.containsKey("pri2")) {
			sqlBuffer.append(", PRI2=?");
			paramList.add(merchItemParam.get("pri2"));
		}
		if(merchItemParam.containsKey("pri4")) {
			sqlBuffer.append(", PRI4=?");
			paramList.add(merchItemParam.get("pri4"));
		}
		if(merchItemParam.containsKey("status")) {
			sqlBuffer.append(", STATUS=?");
			paramList.add(merchItemParam.get("status"));
		}
		sqlBuffer.append(" WHERE MERCH_ID=? AND ITEM_ID=?");
		paramList.add(merchItemParam.get("merch_id"));
		paramList.add(merchItemParam.get("item_id"));
		this.executeSQL(sqlBuffer.toString(), paramList.toArray());
	}
	
	
	@Override
	public void updateMerchItemCostAndPri1(List<Map<String, Object>> itemParamList) throws Exception{
		String sql="UPDATE BASE_MERCH_ITEM SET COST = ?, PRI1 = ?, MODIFIED_TIMESTAMP = ? WHERE MERCH_ID = ? AND ITEM_ID = ?";
		
		List<Object[]> placeholders = new ArrayList<Object[]>();
		for(Map<String, Object> itemParam : itemParamList){
			ArrayList<Object> placeholder = new ArrayList<Object>();
			placeholder.add(MapUtil.getBigDecimal(itemParam, "cost"));
			placeholder.add(MapUtil.getBigDecimal(itemParam, "pri1"));
			placeholder.add(MapUtil.getString(itemParam, "modified_timestamp", DateUtil.getCurrentTime()));
			placeholder.add(MapUtil.getString(itemParam, "merch_id"));
			placeholder.add(MapUtil.getString(itemParam, "item_id"));
			placeholders.add(placeholder.toArray());
		}
		this.executeBatchSQL(sql, placeholders);	
	}
	
	@Override
	public void updateMerchItemPri1(List<Map<String, Object>> itemParamList) throws Exception{
		String sql="UPDATE BASE_MERCH_ITEM SET PRI1 = ?, MODIFIED_TIMESTAMP = ? WHERE MERCH_ID = ? AND ITEM_ID = ?";
		
		List<Object[]> placeholders = new ArrayList<Object[]>();
		for(Map<String, Object> itemParam : itemParamList){
			ArrayList<Object> placeholder = new ArrayList<Object>();
			placeholder.add(MapUtil.getBigDecimal(itemParam, "pri1"));
			placeholder.add(MapUtil.getString(itemParam, "modified_timestamp", DateUtil.getCurrentTime()));
			placeholder.add(MapUtil.getString(itemParam, "merch_id"));
			placeholder.add(MapUtil.getString(itemParam, "item_id"));
			placeholders.add(placeholder.toArray());
		}
		
		this.executeBatchSQL(sql, placeholders);	
	}
	
	@Override
	public void updateMerchItemCost(List<Map<String, Object>> itemParamList) throws Exception{
		String sql="UPDATE BASE_MERCH_ITEM SET COST = ?, MODIFIED_TIMESTAMP = ? WHERE MERCH_ID = ? AND ITEM_ID = ?";
		
		List<Object[]> placeholders = new ArrayList<Object[]>();
		for(Map<String, Object> itemParam : itemParamList){
			ArrayList<Object> placeholder = new ArrayList<Object>();
			placeholder.add(MapUtil.getBigDecimal(itemParam, "cost"));
			placeholder.add(MapUtil.getString(itemParam, "modified_timestamp", DateUtil.getCurrentTime()));
			placeholder.add(MapUtil.getString(itemParam, "merch_id"));
			placeholder.add(MapUtil.getString(itemParam, "item_id"));
			placeholders.add(placeholder.toArray());
		}
		
		this.executeBatchSQL(sql, placeholders);	
	}
	
	/**
	 * 更新商户商品属性表
	 */
	public static final String updateMerchItemUnitSql = initUpdateMerchItemUnitSql();
	private static String initUpdateMerchItemUnitSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE BASE_MERCH_ITEM_UNIT SET ITEM_BAR = ?, ITEM_UNIT_NAME = ?, BIG_BAR = ?, BIG_UNIT_NAME = ?, UNIT_RATIO = ?, PRI4 = ?");
		sb.append(" WHERE 1=1 ");
		return sb.toString();
	}
	public void updateMerchItemUnit(List<Map<String, Object>> merchItemUnitParam) throws Exception {
		LOG.debug("ItemDaoImpl updateMerchItemUnit merchItemUnitParam: " + merchItemUnitParam);
		StringBuffer sqlBuffer = new StringBuffer(updateMerchItemUnitSql);
		sqlBuffer.append(" AND SEQ_ID = ? ");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> merchItemUnitParamMap : merchItemUnitParam) {
			Object[] paramObject = new Object[7];
			paramObject[0] = merchItemUnitParamMap.get("item_bar");
			paramObject[1] = merchItemUnitParamMap.get("item_unit_name");
			paramObject[2] = merchItemUnitParamMap.get("big_bar");
			paramObject[3] = merchItemUnitParamMap.get("big_unit_name");
			paramObject[4] = merchItemUnitParamMap.get("unit_ratio");
			paramObject[5] = merchItemUnitParamMap.get("pri4");
			paramObject[6] = merchItemUnitParamMap.get("seq_id");
			paramArray.add(paramObject);
		}
		this.executeBatchSQL(sqlBuffer.toString(), paramArray);
	}
	@Override
	public void updateMerchItemUnit(Map<String, Object> merchItemUnitParam) throws Exception {
		LOG.debug("ItemDaoImpl updateMerchItemUnit merchItemUnitParam: " + merchItemUnitParam);
		StringBuffer sql=new StringBuffer(updateMerchItemUnitSql);
		sql.append(" AND SEQ_ID = ?  ");
//		if(merchItemUnitParam.size()>0) {
//			List<Object[]> paramArray = new ArrayList<Object[]>();
//			for(Map<String, Object> merchItemUnitMap : merchItemUnitParam) {
//				Object[] paramObject = new Object[7];
//				paramObject[0] = merchItemUnitParam.get("item_bar");
//				paramObject[1] = merchItemUnitParam.get("item_unit_name");
//				paramObject[2] = merchItemUnitParam.get("big_bar");
//				paramObject[3] = merchItemUnitParam.get("big_unit_name");
//				paramObject[4] = new BigDecimal(merchItemUnitParam.get("unit_ratio").toString());
//				paramObject[5] = new BigDecimal(merchItemUnitParam.get("pri4").toString());
//				paramObject[6] = merchItemUnitParam.get("seq_id");
//				paramArray.add(paramObject);
//			}
//		} // 如果参数为空list, 则什么都不做
		String itemBar = (String) merchItemUnitParam.get("item_bar");
		String unitName = (String) merchItemUnitParam.get("unit_name");
		List<Map<String, Object>> itemUnitParams = (List<Map<String, Object>>) merchItemUnitParam.get("list");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		if(itemUnitParams!=null && !itemUnitParams.isEmpty()) {
			for(Map<String, Object> itemUnitParam : itemUnitParams) {
				if("U".equals((String) itemUnitParam.get("type"))) {
					Object[] objectArray = new Object[7];
					objectArray[0] = itemBar; //小包条码
					objectArray[1] = unitName; //小包单位
					objectArray[2] = (String) itemUnitParam.get("big_bar"); //大包条码
					objectArray[3] = (String) itemUnitParam.get("big_unit_name"); //大包单位
					objectArray[4] = new BigDecimal(itemUnitParam.get("unit_ratio").toString()).intValue(); //转换比例
					objectArray[5] = new BigDecimal(itemUnitParam.get("pri4").toString());
					objectArray[6] = (String) itemUnitParam.get("seq_id");
					paramArray.add(objectArray);
				}
			}
		}
		this.executeBatchSQL(sql.toString(), paramArray);
	}
	
	/**
	 * 批量新增商户商品
	 */
	public void insertMerchItem(List<Map<String, Object>> itemList) throws Exception {
		LOG.debug("ItemDaoImpl insertMerchItem itemList: " + itemList);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO BASE_MERCH_ITEM");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		List<Object> paramArray = null;
		int index = 0;
		for(Map<String, Object> itemMap : itemList) {
			paramArray = new ArrayList<Object>();
			
			// 将管理包装信息更新到商品表中
			if(ParamUtil.isTobacco(itemMap)) {
				String itemBar = MapUtil.getString(itemMap, "item_bar");
				List<Map<String, Object>> unitList = MapUtil.get(itemMap, "list", Collections.EMPTY_LIST);
				int unitIndex = 0;
				for(Map<String, Object> unitMap : unitList) {
					String bigBar = MapUtil.getString(unitMap, "big_bar");
					String unitName = MapUtil.getString(unitMap, "big_unit_name");
					BigDecimal pri4 = MapUtil.getBigDecimal(unitMap, "pri4");
					double unitRatio = MapUtil.getDouble(unitMap, "unit_ratio", 1);
					if(unitIndex++==0) {
						itemMap.put("big_bar", bigBar);
						itemMap.put("big_unit_name", unitName);
						itemMap.put("big_pri4", pri4);
						itemMap.put("big_unit_ratio", unitRatio);
					} else if(!itemBar.equals(bigBar) && unitRatio!=1) {
						itemMap.put("big_bar", bigBar);
						itemMap.put("big_unit_name", unitName);
						itemMap.put("big_pri4", pri4);
						itemMap.put("big_unit_ratio", unitRatio);
						break;
					}
				}
			} else {
				itemMap.put("big_bar", MapUtil.getString(itemMap, "item_bar"));
				itemMap.put("big_unit_name", MapUtil.getString(itemMap, "unit_name"));
				itemMap.put("big_pri4", MapUtil.getBigDecimal(itemMap, "pri4"));
				itemMap.put("big_unit_ratio", 1);
			}
			
			String itemName = MapUtil.getString(itemMap, "item_name");
			if(index++==0) {
				sqlBuilder.append(SQLUtil.initSQLInsertValues(itemMap, paramArray, "merch_id", "item_id", "item_bar", "item_name", 
						"short_code:"+SpellUtil.getFullSpell(itemName), "short_name:"+SpellUtil.getShortSpell(itemName), 
						"spec: ", "item_kind_id", "unit_name", "cost", "pri1", "pri4", "big_bar", 
						"big_unit_name", "big_unit_ratio", "big_pri4", "create_date", "create_time", "modified_timestamp", "status:1"));
			} else {
				SQLUtil.initSQLInsertValues(itemMap, paramArray, "merch_id", "item_id", "item_bar", "item_name", 
						"short_code:"+SpellUtil.getFullSpell(itemName), "short_name:"+SpellUtil.getShortSpell(itemName), 
						"spec: ", "item_kind_id", "unit_name", "cost", "pri1", "pri4", "big_bar", 
						"big_unit_name", "big_unit_ratio", "big_pri4", "create_date", "create_time", "modified_timestamp", "status:1");
			}
			paramArrayList.add(paramArray.toArray());
		}
		this.executeBatchSQL(sqlBuilder.toString(), paramArrayList);
	}
	
	/**
	 * 新增商户商品
	 */
	@Override
	public void insertMerchItem(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl insertMerchItem itemParam: " + itemParam);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO BASE_MERCH_ITEM");
		List<Object> paramList = new ArrayList<Object>();
		
		// 将管理包装信息更新到商品表中
		int index = 0;
		if(ParamUtil.isTobacco(itemParam)) {
			String itemBar = MapUtil.getString(itemParam, "item_bar");
			List<Map<String, Object>> unitList = MapUtil.get(itemParam, "list", Collections.EMPTY_LIST);
			for(Map<String, Object> unitMap : unitList) {
				String bigBar = MapUtil.getString(unitMap, "big_bar");
				String unitName = MapUtil.getString(unitMap, "big_unit_name");
				BigDecimal pri4 = MapUtil.getBigDecimal(unitMap, "pri4");
				int unitRatio = MapUtil.getInt(unitMap, "unit_ratio");
				if(index++==0) {
					itemParam.put("big_bar", bigBar);
					itemParam.put("big_unit_name", unitName);
					itemParam.put("big_pri4", pri4);
					itemParam.put("big_unit_ratio", unitRatio);
				} else if(!itemBar.equals(bigBar) && unitRatio!=1) {
					itemParam.put("big_bar", bigBar);
					itemParam.put("big_unit_name", unitName);
					itemParam.put("big_pri4", pri4);
					itemParam.put("big_unit_ratio", unitRatio);
					break;
				}
			}
		} else {
			itemParam.put("big_bar", MapUtil.getString(itemParam, "item_bar"));
			itemParam.put("big_unit_name", MapUtil.getString(itemParam, "unit_name"));
			itemParam.put("big_pri4", MapUtil.getBigDecimal(itemParam, "pri4"));
			itemParam.put("big_unit_ratio", 1);
		}
		String itemName = MapUtil.getString(itemParam, "item_name");
		sqlBuilder.append(SQLUtil.initSQLInsertValues(itemParam, paramList, "merch_id", "item_id", "item_bar", "item_name", 
				"short_code:"+SpellUtil.getFullSpell(itemName), "short_name:"+SpellUtil.getShortSpell(itemName), "spec", "item_kind_id", "unit_name", "cost", "pri1", "pri4", "big_bar", 
				"big_unit_name", "big_unit_ratio", "big_pri4", "create_date", "create_time", "modified_timestamp", "status:1"));
		this.executeSQL(sqlBuilder.toString(), paramList.toArray());
	}
	
	public static final String insertMerchItemUnitSql = getInsertMerchItemUnitSql();
	private static String getInsertMerchItemUnitSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO BASE_MERCH_ITEM_UNIT (SEQ_ID, MERCH_ID, ITEM_ID, ITEM_BAR, ITEM_UNIT_NAME,");
		sb.append(" BIG_BAR, BIG_UNIT_NAME, UNIT_RATIO, PRI4)");
		sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	
	@Override
	public void insertMerchItemUnit(List<Map<String, Object>> merchItemUnitParam) throws Exception {
		LOG.debug("ItemDaoImpl insertMerchItemUnit merchItemUnitParam" + merchItemUnitParam);
		StringBuffer sqlBuffer = new StringBuffer(insertMerchItemUnitSql);
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> merchItemUnitParamMap : merchItemUnitParam) {
			Object[] paramObject = new Object[9];
			paramObject[0] = MapUtil.getString(merchItemUnitParamMap, "seq_id", IDUtil.getId());
			paramObject[1] = MapUtil.getString(merchItemUnitParamMap, "merch_id");
			paramObject[2] = MapUtil.getString(merchItemUnitParamMap, "item_id");
			paramObject[3] = MapUtil.getString(merchItemUnitParamMap, "item_bar");
			paramObject[4] = MapUtil.getString(merchItemUnitParamMap, "item_unit_name");
			paramObject[5] = MapUtil.getString(merchItemUnitParamMap, "big_bar");
			paramObject[6] = MapUtil.getString(merchItemUnitParamMap, "big_unit_name");
			paramObject[7] = MapUtil.getString(merchItemUnitParamMap, "unit_ratio", "1");
			paramObject[8] = MapUtil.getString(merchItemUnitParamMap, "pri4", "0");
			paramArray.add(paramObject);
		}
		this.executeBatchSQL(sqlBuffer.toString(), paramArray);
	}
	
	@Override
	public void insertMerchItemUnit(Map<String, Object> itemParam) throws Exception {
		String merchId = (String) itemParam.get("merch_id");
		String itemId = (String) itemParam.get("item_id");
		String itemBar = (String) itemParam.get("item_bar");
		String unitName = (String) itemParam.get("unit_name");
		List<Map<String, Object>> itemUnitParams = (List<Map<String, Object>>) itemParam.get("list");
		if(itemUnitParams!=null && !itemUnitParams.isEmpty()) {
			List<Object[]> paramArray = new ArrayList<Object[]>();
			for(Map<String, Object> itemUnitParam : itemUnitParams) {
				if("C".equals((String) itemUnitParam.get("type"))) {
					Object[] objectArray = new Object[9];
					objectArray[0] = IDUtil.getId();
					objectArray[1] = merchId;
					objectArray[2] = itemId;
					objectArray[3] = itemBar; //小包条码
					objectArray[4] = unitName; //小包单位
					objectArray[5] = itemBar; //大包条码
					if(itemUnitParam.get("big_bar")!=null) {
						objectArray[5] = (String) itemUnitParam.get("big_bar");
					}
					objectArray[6] = unitName; //大包单位
					if(itemUnitParam.get("big_unit_name")!=null) {
						objectArray[6] = (String) itemUnitParam.get("big_unit_name");
					}
					objectArray[7] = 1;
					if(itemUnitParam.get("unit_ratio")!=null) {
						objectArray[7] = new BigDecimal(itemUnitParam.get("unit_ratio").toString()).intValue();
					}
					objectArray[8] = new BigDecimal(itemUnitParam.get("pri4").toString());
					paramArray.add(objectArray);
				}
			}
			this.executeBatchSQL(insertMerchItemUnitSql, paramArray);
		}
	}
	
	public void insertMerchItemUnitWithSelf(Map<String, Object> itemParam) throws Exception {
		String merchId = (String) itemParam.get("merch_id");
		String itemId = (String) itemParam.get("item_id");
		String itemBar = (String) itemParam.get("item_bar");
		String unitName = (String) itemParam.get("unit_name");
		BigDecimal pri4 = BigDecimal.ZERO;
		if(itemParam.get("pri4")!=null) {
			pri4 = new BigDecimal(itemParam.get("pri4").toString());
		}
		List<Map<String, Object>> itemUnitParams = (List<Map<String, Object>>) itemParam.get("list");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		Object[] selfArray = new Object[] {IDUtil.getId(), merchId, itemId, itemBar, unitName, itemBar, unitName, 1, pri4};
		paramArray.add(selfArray);
		if(itemUnitParams!=null && !itemUnitParams.isEmpty()) {
			for(Map<String, Object> itemUnitParam : itemUnitParams) {
				if("C".equals((String) itemUnitParam.get("type"))) {
					Object[] objectArray = new Object[9];
					objectArray[0] = IDUtil.getId(); // seq_id
					objectArray[1] = merchId;
					objectArray[2] = itemId;
					objectArray[3] = itemBar; //小包条码
					objectArray[4] = unitName; //小包单位
					objectArray[5] = itemBar; //大包条码
					if(itemUnitParam.get("big_bar")!=null && !"".equals(itemUnitParam.get("big_bar"))) {
						objectArray[5] = (String) itemUnitParam.get("big_bar");
					}
					objectArray[6] = unitName; //大包单位
					if(itemUnitParam.get("big_unit_name")!=null && !"".equals(itemUnitParam.get("big_unit_name"))) {
						objectArray[6] = (String) itemUnitParam.get("big_unit_name");
					}
					objectArray[7] = 1;
					if(itemUnitParam.get("unit_ratio")!=null && !"".equals(itemUnitParam.get("unit_ratio"))) {
						objectArray[7] = new BigDecimal(itemUnitParam.get("unit_ratio").toString()).intValue();
					}
					objectArray[8] = pri4;
					if(itemUnitParam.get("pri4")!=null && !"".equals(itemUnitParam.get("pri4"))) {
						objectArray[8] = new BigDecimal(itemUnitParam.get("pri4").toString());
					}
					
					paramArray.add(objectArray);
				}
			}
		}
		this.executeBatchSQL(insertMerchItemUnitSql, paramArray);
	}
	
	private static final String updateMerchItemWithStatusSql = getUpdateMerchItemWithStatusSql();
	private static String getUpdateMerchItemWithStatusSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE BASE_MERCH_ITEM SET STATUS = ?, MODIFIED_TIMESTAMP = ? WHERE MERCH_ID = ? AND ITEM_ID = ?");
		return sb.toString();
	}
	
	public void updateMerchItemWithStatus(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl updateMerchItemWithStatus itemParam: " + itemParam);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(itemParam.get("status"));
		paramList.add(itemParam.get("modified_timestamp"));
		paramList.add(itemParam.get("merch_id"));
		paramList.add(itemParam.get("item_id"));
		String item_bar = MapUtil.getString(itemParam, "item_bar");
		String sql = updateMerchItemWithStatusSql;
		if(!item_bar.equals("")){
			sql=sql+" AND ITEM_BAR = ?";
			paramList.add(item_bar);
		}
		this.executeSQL(sql, paramList.toArray());
	}
	
	public void updateMerchItemUnitWithSelf(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemDaoImpl updateMerchItemUnitWithSelf itemParam: " + itemParam);
		StringBuffer sql=new StringBuffer(updateMerchItemUnitSql);
		sql.append(" AND SEQ_ID = ? ");
		String itemBar = (String) itemParam.get("item_bar");
		String unitName = (String) itemParam.get("unit_name");
		BigDecimal pri4 = BigDecimal.ZERO;
		if(itemParam.get("pri4")!=null) {
			pri4 = new BigDecimal(itemParam.get("pri4").toString());
		}
		String seqId = (String) itemParam.get("seq_id");
		List<Map<String, Object>> itemUnitParams = (List<Map<String, Object>>) itemParam.get("list");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		Object[] selfArray = new Object[] {itemBar, unitName, itemBar, unitName, 1, pri4, seqId};
		paramArray.add(selfArray);
		if(itemUnitParams!=null && !itemUnitParams.isEmpty()) {
			for(Map<String, Object> itemUnitParam : itemUnitParams) {
				if("U".equals((String) itemUnitParam.get("type"))) {
					Object[] objectArray = new Object[7];
					objectArray[0] = itemBar; //小包条码
					objectArray[1] = unitName; //小包单位
					objectArray[2] = (String) itemUnitParam.get("big_bar"); //大包条码
					objectArray[3] = (String) itemUnitParam.get("big_unit_name"); //大包单位
					objectArray[4] = new BigDecimal(itemUnitParam.get("unit_ratio").toString()).intValue(); //转换比例
					objectArray[5] = new BigDecimal(itemUnitParam.get("pri4").toString());
					objectArray[6] = (String) itemUnitParam.get("seq_id");
					paramArray.add(objectArray);
				}
			}
		}
		this.executeBatchSQL(sql.toString(), paramArray);
	}
	
	public static final String deleteMerchItemUnitSql = initDeleteMerchItemUnitSql();
	private static String initDeleteMerchItemUnitSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM BASE_MERCH_ITEM_UNIT WHERE MERCH_ID = ? AND SEQ_ID = ?");
		return sb.toString();
	}
	@Override
	public void deleteMerchItemUnit(List<Map<String, Object>> paramList) throws Exception{
		LOG.debug("ItemDaoImpl deleteMerchItemUnit paramList: " + paramList);
		
		StringBuilder sql = new StringBuilder("DELETE BASE_MERCH_ITEM_UNIT WHERE");
		List<Object[]> list = new ArrayList<Object[]>();
		int index = 0;
		for(Map<String, Object> gift : paramList) {
			List<Object> list2 = new ArrayList<Object>();
			if(index++==0) {
				SQLUtil.initSQLEqual(gift, sql, list2, "merch_id", "item_bar", "item_id", "seq_id");
			} else {
				SQLUtil.initSQLEqual(gift, list2, "merch_id", "item_bar", "item_id", "seq_id");
			}
			list.add(list2.toArray());
		}
		this.executeBatchSQL(sql.toString(), list);
	}
	
//	@Override
	public void deleteMerchItemUnit1(List<Map<String, Object>> itemUnitParamList) throws Exception{
		LOG.debug("ItemDaoImpl deleteMerchItemUnit itemUnitParamList: " + itemUnitParamList);
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> itemUnitMap : itemUnitParamList) {
			Object[] paramObject = new Object[]{itemUnitMap.get("merch_id"), itemUnitMap.get("seq_id")};
			paramArray.add(paramObject);
		}
		this.executeBatchSQL(deleteMerchItemUnitSql, paramArray);
	}
	
	@Override
	public void deleteMerchItem(List<Map<String, Object>> paramList) throws Exception{
		LOG.debug("ItemDaoImpl deleteMerchItem paramList: " + paramList);
		
		StringBuilder sql = new StringBuilder("DELETE BASE_MERCH_ITEM WHERE");
		List<Object[]> list = new ArrayList<Object[]>();
		int index = 0;
		for(Map<String, Object> gift : paramList) {
			List<Object> list2 = new ArrayList<Object>();
			if(index++==0) {
				SQLUtil.initSQLEqual(gift, sql, list2, "merch_id", "item_bar", "item_id");
			} else {
				SQLUtil.initSQLEqual(gift, list2, "merch_id", "item_bar", "item_id");
			}
			list.add(list2.toArray());
		}
		this.executeBatchSQL(sql.toString(), list);
	}
	
	@Override
	public void deleteMerchItemUnit(Map<String, Object> itemParam) throws Exception {
		List<Map<String, Object>> itemUnitParamList = (List<Map<String, Object>>) itemParam.get("list");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		if(itemUnitParamList!=null && !itemUnitParamList.isEmpty()) {
			for(Map<String, Object> itemUnitParam : itemUnitParamList) {
				if("D".equals((String) itemUnitParam.get("type"))) {
					Object[] objectArray = new Object[] {itemParam.get("merch_id"), itemUnitParam.get("seq_id")};
					paramArray.add(objectArray);
				}
			}
		}
		this.executeBatchSQL(deleteMerchItemUnitSql, paramArray);
	}
	
	
	//1.搜索商品包括扫码查询(距离)手机接口
	public static final String searchGoodsSql = initSearchGoodsSql();
	private static String initSearchGoodsSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("select bmi.ITEM_ID GoodId, bm.merch_id ShopID, bmi.ITEM_NAME GoodName, ");
		sb.append(" sqrt(power((to_number(LONGITUDE)-?)*1117000,2)+power((to_number(LATITUDE)-?)*1117000,2)) distance,");
		sb.append(" bm.MERCH_NAME ShopName, open_time||'-'||close_time ShoppingTime,FILE_ID,close_time closeTime,open_time openTime, 'Y' Songhuo, bmi.PRI4 GoodPrice, bmi.UNIT_NAME GoodUnit");
		sb.append(" from base_merch bm, BASE_MERCH_ITEM bmi, BASE_MERCH_ITEM_UNIT bmiu");
		sb.append(" where bm.merch_id= bmi.merch_id and bmi.item_id = bmiu.item_id"); // 暂时先不加状态判断, 因为有的状态是null
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchGoods(Map<String, Object> goodsRequest) throws Exception {
		StringBuffer sqlBuffer = new StringBuffer(searchGoodsSql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(goodsRequest.get("lng"));
		paramObject.add(goodsRequest.get("lat"));
		if(goodsRequest.get("disitence")!=null) {
			sqlBuffer.append(" AND sqrt(power((to_number(LONGITUDE)-?)*1117000,2)+power((to_number(LATITUDE)-?)*1117000,2)) <= ?");
			paramObject.add(goodsRequest.get("lng"));
			paramObject.add(goodsRequest.get("lat"));
			paramObject.add(goodsRequest.get("disitence"));
		}
		if(goodsRequest.get("msg")!=null && !"0".equals(goodsRequest.get("msg"))) {
			sqlBuffer.append(" AND bmiu.BIG_BAR = ?");
			paramObject.add(goodsRequest.get("msg"));
		}
		if("1".equals(goodsRequest.get("order"))) {
			sqlBuffer.append(" ORDER BY sqrt(power((to_number(LONGITUDE)-?)*1117000,2)+power((to_number(LATITUDE)-?)*1117000,2))");
			paramObject.add(goodsRequest.get("lng"));
			paramObject.add(goodsRequest.get("lat"));
		} else if("2".equals(goodsRequest.get("order"))) {
			sqlBuffer.append(" ORDER BY bmi.PRI4");
		}
		int pageIndex = Integer.parseInt(goodsRequest.get("page_index").toString());
		int pageSize = Integer.parseInt(goodsRequest.get("page_size").toString());
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.toString(), pageIndex, pageSize, paramObject.toArray());
		Integer pageSum=pageResult.getPageSum();
		Integer total=pageResult.getTotal();
		goodsRequest.put("page_count", pageSum);
		goodsRequest.put("count", total);
		return pageResult.getRows();
	}
	
	public static final String selectRecommendedItemSql=initSelectRecommendedItemSql();
	private static String initSelectRecommendedItemSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("SELECT ITEM_ID, RECOMMEND_TO, STATUS FROM RECOMMENDED_ITEM WHERE 1=1 ");
		return sb.toString();
	}
	
	// 获取推荐商品
	@Override
	public List<Map<String, Object>> selectRecommendedItem (Map<String, Object> itemParam) throws Exception{
		LOG.debug("ItemDaoImpl selectRecommendedItem itemParam:"+itemParam);
		StringBuffer sqlBuffer = new StringBuffer(selectRecommendedItemSql);
		List<Object> paramList = new ArrayList<Object>();
		if(itemParam.get("status")!=null) {
			sqlBuffer.append(" AND STATUS=? ");
			paramList.add(itemParam.get("status"));
		}
		if(itemParam.get("recommend_to")!=null) {
			sqlBuffer.append(" AND RECOMMEND_TO=? ");
			paramList.add(itemParam.get("recommend_to"));
		}
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	//修改商品，通过bar_bar=item_bar ,merch_id,item_id
	@Override
	public void updateMerchItemUnit1(List<Map<String, Object>> paramList)throws Exception{
		StringBuffer sql = new StringBuffer("UPDATE BASE_MERCH_ITEM_UNIT");
		sql.append(" SET ITEM_UNIT_NAME = ?, BIG_UNIT_NAME = ?, PRI4 = ?");
		sql.append(" WHERE MERCH_ID = ? AND ITEM_ID = ? AND BIG_BAR = ?");
		List<Object> list = new ArrayList<Object>();
		for (Map<String, Object> map : paramList) {
			Object [] itemUnit = new Object[6];
			itemUnit[0] = MapUtil.getString(map, "item_unit_name");
			itemUnit[1] = MapUtil.getString(map, "big_unit_name");
			itemUnit[2] = MapUtil.getString(map, "pri4");
			itemUnit[3] = MapUtil.getString(map, "merch_id");
			itemUnit[4] = MapUtil.getString(map, "item_id");
			itemUnit[5] = MapUtil.getString(map, "big_bar");
			list.add(itemUnit);
		}
		
		this.executeBatchSQL(sql.toString(), list);
	}
	//得到推荐商品的信息
	private static final String selectRecommendedPOSItemInfoSql=getRecommendedPOSItemInfoSql();
	private static String getRecommendedPOSItemInfoSql(){
		StringBuffer sql=new StringBuffer();
		sql.append("  SELECT BI.ITEM_ID,ITEM_BAR,ITEM_NAME,SHORT_NAME,SHORT_CODE, ");
		sql.append("  ITEM_KIND_ID,UNIT_NAME,PRI1,PRI4,SPEC,BI.STATUS  ");
		sql.append("  FROM RECOMMENDED_ITEM RI,BASE_ITEM BI ");
		sql.append("  WHERE BI.ITEM_ID=RI.ITEM_ID ");
		sql.append("  AND RI.STATUS=? ");
		sql.append("  AND RECOMMEND_TO=? ");
		sql.append("   ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> searchRecommendedPOSItemInfo(Map<String, Object> paramsMap)throws Exception{
		LOG.debug("ItemDaoImpl  searchRecommendedPOSItemInfo paramsMap:"+paramsMap);
		StringBuffer sql=new StringBuffer(selectRecommendedPOSItemInfoSql);
		List<Object> list=new ArrayList<Object>();
		list.add(paramsMap.get("status"));
		list.add(paramsMap.get("recommend_to"));
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	//插入预警商品信息
	@Override
	public void insertMerchWarningItem(List<Map<String, Object>> paramList)throws Exception{
		LOG.debug("insertMerchWarningItem paramList:"+paramList);
		StringBuilder sql = new StringBuilder("INSERT INTO MERCH_WARNING_ITEM ");
		List<Object[]> list = new ArrayList<Object[]>();
		List<Object> paramArray = null;
		
		for (int i = 0; i < paramList.size(); i++) {
			paramArray = new ArrayList<Object>();
			if(i == 0){
				sql.append(SQLUtil.initSQLInsertValues(paramList.get(i), paramArray
						, "seq_id", "merch_id", "item_id"));
			}else{
				SQLUtil.initSQLInsertValues(paramList.get(i), paramArray
						, "seq_id", "merch_id", "item_id");
			}
			LOG.debug("----------param:"+paramArray);
			list.add(paramArray.toArray());
		}
		LOG.debug("----------sql:"+sql.toString());
		this.executeBatchSQL(sql.toString(), list);
	}
		
	//查询预警商品
	@Override
	public List<Map<String, Object> > selectMerchWarningItem(Map<String, Object> paramMap)throws Exception{
		LOG.debug("selectMerchWarningItem paramMap:"+paramMap); 
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		sql.append("SELECT SEQ_ID, MERCH_ID, ITEM_ID FROM MERCH_WARNING_ITEM WHERE 1=1 ");
		SQLUtil.initSQLEqual(paramMap,sql, list, "seq_id", "merch_id", "item_id");
		LOG.debug("----------sql:"+sql.toString());
		LOG.debug("----------list:"+list);
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	

	//修改base_merch_item_unit : merch_id, item_id, big_bar
	@Override
	public void updateMerchItemUnitByIdBar(List<Map<String, Object>> merchItemUnitParam) throws Exception {
		LOG.debug("ItemDaoImpl updateMerchItemUnit merchItemUnitParam: " + merchItemUnitParam);
		StringBuffer sqlBuffer = new StringBuffer(updateMerchItemUnitSql);
		sqlBuffer.append(" AND merch_id = ? and item_id = ? and big_bar = ?  ");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> merchItemUnitParamMap : merchItemUnitParam) {
			Object[] paramObject = new Object[9];
			paramObject[0] = merchItemUnitParamMap.get("item_bar");
			paramObject[1] = merchItemUnitParamMap.get("item_unit_name");
			paramObject[2] = merchItemUnitParamMap.get("big_bar");
			paramObject[3] = merchItemUnitParamMap.get("big_unit_name");
			paramObject[4] = merchItemUnitParamMap.get("unit_ratio");
			paramObject[5] = merchItemUnitParamMap.get("pri4");
			paramObject[6] = merchItemUnitParamMap.get("merch_id");//merch_id
			paramObject[7] = merchItemUnitParamMap.get("item_id");//item_id
			paramObject[8] = merchItemUnitParamMap.get("big_bar");//big_bar
			paramArray.add(paramObject);
		}
		this.executeBatchSQL(sqlBuffer.toString(), paramArray);
	}
	
	
	@Override
	public void updateMoreMerchItemInfo(List<Map<String, Object>> merchItemParamList) throws Exception{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE BASE_MERCH_ITEM SET PRI1 = ?, COST = ?, MODIFIED_TIMESTAMP = ?, ");
		sql.append("ITEM_BAR = ?, ITEM_NAME = ?, SHORT_CODE = ?, SHORT_NAME = ?, ITEM_KIND_ID = ?, UNIT_NAME = ?,  ");
		sql.append(" PRI4 = ?, BIG_BAR = ?, BIG_UNIT_NAME = ?, BIG_UNIT_RATIO = ?, BIG_PRI4 = ? ");
		sql.append("WHERE MERCH_ID = ? AND ITEM_ID = ? ");
		
		List<Object[]> list = new ArrayList<Object[]>();
		
		for(Map<String, Object> map:merchItemParamList){
			Object[] ob=new Object[16];
			ob[0]=map.get("pri1");
			ob[1]=map.get("cost");
			ob[2]=map.get("modified_timestamp");
			ob[3]=map.get("item_bar");
			ob[4]=map.get("item_name");
			ob[5]=map.get("short_code");
			ob[6]=map.get("short_name");
			ob[7]=map.get("item_kind_id");
			ob[8]=map.get("unit_name");
			ob[9]=map.get("pri4");
			ob[10]=map.get("big_bar");
			ob[11]=map.get("big_unit_name");
			ob[12]=map.get("big_unit_ratio");
			ob[13]=map.get("big_pri4");
			ob[14]=map.get("merch_id");
			ob[15]=map.get("item_id");
			list.add(ob);
		}
		this.executeBatchSQL(sql.toString(), list);	
	}
	
	@Override
	public List<Map<String, Object> > selectMerchItemById(Map<String, Object> paramMap)throws Exception{
		LOG.debug("selectMerchItemById paramMap:"+paramMap);
		List<Object> list = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder(selectMerchItemSql);
		String itemId = MapUtil.getString(paramMap, "item_id");
		if(!StringUtil.isBlank(itemId)){
			String [] itemIdArr = itemId.split(",");
			if(itemIdArr.length>=1){
				sql.append(" AND ITEM_ID IN (?");
				list.add(itemIdArr[0]);
				for (int i = 1; i < itemIdArr.length; i++) {
					sql.append(",?");
					list.add(itemIdArr[i]);
				}
				sql.append(") ");
			}
		}
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	// 查找db数据库文件 
	@Override
	public List<Map<String, Object>> selectGoods(Map<String, Object> paramMap) throws Exception{
		LOG.debug("ItemDaoImpl selectGoods paramMap: " + paramMap);
		String path = MapUtil.getString(paramMap, "db_path");
		String merchId = MapUtil.getString(paramMap, "merch_id");
		
		Class.forName("org.sqlite.JDBC");
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+path);
//			/home/ryx/nfsdata/user_file/1037010407467/1409637326324468/pos_retail.db
//			conn = DriverManager.getConnection("jdbc:sqlite:/"+"192.168.0.3:1521"+path );
		StringBuilder sql = new StringBuilder();
		sql.append("select g.good_id item_id, good_name item_name, case when cat_id='001' then '01' else cat_id end item_kind_id, item_code item_bar,");
		sql.append(" big_code big_bar, (case when whole_price is null then 0 else whole_price end)/rods cost,");
		sql.append(" (case when out_price is null then 0 else out_price end)/rods pri4, (case when out_price is null then 0 else out_price end) big_pri4,");
		sql.append(" 1 unit_ratio, rods big_unit_ratio, status, case when cur_whse is null then 0 else cur_whse end qty_whse,");
		sql.append(" case when warn_whse is null then 0 else warn_whse end qty_whse_warn");
		sql.append(" from goods g left join whse w on g.good_id=w.good_id ");
		PreparedStatement ps = connection.prepareStatement(sql.toString());
		ResultSet resultSet = ps.executeQuery();
		List<Map<String, Object>> goodsList = new ArrayList<Map<String, Object>>();
		while(resultSet.next()) {
			Map<String, Object> goods = new HashMap<String, Object>();
			goods.put("merch_id", merchId);
			goods.put("item_id", resultSet.getString("item_id"));
			goods.put("item_name", resultSet.getString("item_name"));
			goods.put("item_kind_id", resultSet.getString("item_kind_id"));
			goods.put("item_bar", resultSet.getString("item_bar"));
			goods.put("big_bar", resultSet.getString("big_bar"));
			goods.put("cost", resultSet.getDouble("cost"));
			goods.put("pri1", resultSet.getDouble("cost"));
			goods.put("pri4", resultSet.getDouble("pri4"));
			goods.put("big_pri4", resultSet.getDouble("big_pri4"));
			goods.put("unit_ratio", resultSet.getDouble("unit_ratio"));
			goods.put("big_unit_ratio", resultSet.getDouble("big_unit_ratio"));
			goods.put("status", resultSet.getString("status"));
			goods.put("qty_whse", resultSet.getDouble("qty_whse"));
			goods.put("qty_whse_warn", resultSet.getDouble("qty_whse_warn"));
			goodsList.add(goods);
		}
		if(null!=resultSet) resultSet.close();
		if(null!=ps) ps.close();
		if(null!=connection) connection.close();
		
		return goodsList;
	}
}
