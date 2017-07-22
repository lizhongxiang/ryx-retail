package com.ryx.social.retail.dao.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.impl.WhseServiceImpl;
import com.ryx.social.retail.util.SQLUtil;


@Repository
public class WhseDaoImpl extends BaseDaoImpl implements IWhseDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(WhseDaoImpl.class);
	
	/**
	 * 根据item_id查询库存信息, 还可以根据qty_whse的上下限来查询, 加入分页
	 */
	public static final String selectWhseMerchSql = initSelectWhseMerchSql();
	private static String initSelectWhseMerchSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, ITEM_ID, QTY_WHSE, QTY_LOCKED, QTY_WHSE_WARN, OUTPUT_DATE, QTY_WHSE_INIT, WHSE_INIT_DATE");
		sb.append(" FROM WHSE_MERCH WHERE MERCH_ID = ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectWhseMerch(Map<String, Object> whseMerchParam) throws Exception {
		LOG.debug("WhseDaoImpl selectWhseMerch whseMerchParam: " + whseMerchParam);
		StringBuilder sqlBuffer = new StringBuilder(selectWhseMerchSql);
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(whseMerchParam.get("merch_id"));
		int pageIndex = MapUtil.getInt(whseMerchParam, "page_index", 1);
		int pageSize = MapUtil.getInt(whseMerchParam, "page_size", 20);
		Map<String, Object> orderByMap = MapUtil.get(whseMerchParam, "order_by", null);//(Map<String, Object>) whseMerchParam.get("order_by");
		
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
		orderByBuffer.append(" ITEM_ID ASC"); // 默认按照ITEM_ID正序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 查询
		// 用sqlUtil来查询库存, 防止参数超过1000
		SQLUtil.initSQLIn(whseMerchParam, sqlBuffer, paramArray, "item_id");
		/*
		if(itemId!=null) {
			String[] itemIdArray = itemId.split(",");
			if(itemIdArray.length==1) {
				sqlBuffer.append(" AND ITEM_ID = ?");
				paramArray.add(itemIdArray[0]);
			} else {
				sqlBuffer.append(" AND ITEM_ID IN (");
				for(int i=0; i<itemIdArray.length; i++) {
					paramArray.add(itemIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		*/
		if(whseMerchParam.get("qty_whse_floor")!=null) { // 库存数量下限
			BigDecimal qtyWhseFloor = new BigDecimal(whseMerchParam.get("qty_whse_floor").toString());
			sqlBuffer.append(" AND QTY_WHSE > ?");
			paramArray.add(qtyWhseFloor);
		}
		if(whseMerchParam.get("qty_whse_ceiling")!=null) { // 库存数量上限
			BigDecimal qtyWhseCeiling = new BigDecimal(whseMerchParam.get("qty_whse_ceiling").toString());
			sqlBuffer.append(" AND QTY_WHSE < ?");
			paramArray.add(qtyWhseCeiling);
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), pageIndex, pageSize, paramArray.toArray());
		whseMerchParam.put("page_count", pageResult.getPageSum());
		whseMerchParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	/**
	 * 根据item_id查询商品基础信息和库存信息,还可以根据qty_whse的上下限来查询, 加入分页
	 */
	public static final String selectWhseMerchAndMerchItemSql = initSelectWhseMerchAndMerchItemSql();
	private static String initSelectWhseMerchAndMerchItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT 'MERCH' flag,BMI.MERCH_ID,BMI.ITEM_ID,ITEM_BAR,ITEM_NAME,SHORT_NAME,SHORT_CODE,ITEM_KIND_ID,UNIT_NAME,COST,");
		sb.append("PRI1,PRI2,PRI4,BIG_BAR,BIG_UNIT_NAME,BIG_PRI4,BIG_UNIT_RATIO UNIT_RATIO,BMI.STATUS,SPEC,QTY_WHSE,");
		sb.append("QTY_LOCKED,QTY_WHSE_WARN,OUTPUT_DATE,QTY_WHSE_INIT,WHSE_INIT_DATE");
		sb.append(" FROM WHSE_MERCH WM");
		sb.append(" INNER JOIN BASE_MERCH_ITEM BMI ON BMI.MERCH_ID=WM.MERCH_ID AND BMI.ITEM_ID=WM.ITEM_ID");
		sb.append(" WHERE WM.MERCH_ID=? ");
		return sb.toString();
	}
	
	@Override
	public List<Map<String, Object>> selectRemovedWhseMerchAndMerchItem(Map<String, Object> whseParam) throws Exception {
		LOG.debug("WhseDaoImpl selectRemovedWhseMerchAndMerchItem whseMerchParam: " + whseParam);
		StringBuilder sqlBuffer = new StringBuilder(selectWhseMerchAndMerchItemSql);
		sqlBuffer.append(" AND BMI.STATUS=0 ");
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(whseParam.get("merch_id"));
		
		SQLUtil.initSQLIn(whseParam, sqlBuffer, paramArray, "wm.item_id");
		if(whseParam.get("qty_whse_floor")!=null) { // 库存数量下限
			BigDecimal qtyWhseFloor = new BigDecimal(whseParam.get("qty_whse_floor").toString());
			sqlBuffer.append(" AND QTY_WHSE > ?");
			paramArray.add(qtyWhseFloor);
		}
		if(whseParam.get("qty_whse_ceiling")!=null) { // 库存数量上限
			BigDecimal qtyWhseCeiling = new BigDecimal(whseParam.get("qty_whse_ceiling").toString());
			sqlBuffer.append(" AND QTY_WHSE < ?");
			paramArray.add(qtyWhseCeiling);
		}
//		if(whseParam.get("status")!=null){
//			SQLUtil.initSQLIn(whseParam,sqlBuffer ,paramArray, "bmi.status");
//		}
		sqlBuffer.append(" ORDER BY BMI.ITEM_ID ASC");// 默认按照ITEM_ID正序排列
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.toString(), MapUtil.getInt(whseParam, "page_index", 1),
				MapUtil.getInt(whseParam, "page_size", 20), paramArray.toArray());
		whseParam.put("page_count", pageResult.getPageSum());
		whseParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	@Override
	public List<Map<String, Object>> selectNotRemovedWhseMerchAndMerchItem(Map<String, Object> whseParam) throws Exception {
		LOG.debug("WhseDaoImpl selectNotRemovedWhseMerchAndMerchItem whseMerchParam: " + whseParam);
		StringBuilder sqlBuffer = new StringBuilder(selectWhseMerchAndMerchItemSql);
		sqlBuffer.append(" AND BMI.STATUS IN (1,2) ");
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(whseParam.get("merch_id"));
		
		SQLUtil.initSQLIn(whseParam, sqlBuffer, paramArray, "wm.item_id");
		if(whseParam.get("qty_whse_floor")!=null) { // 库存数量下限
			BigDecimal qtyWhseFloor = new BigDecimal(whseParam.get("qty_whse_floor").toString());
			sqlBuffer.append(" AND QTY_WHSE > ?");
			paramArray.add(qtyWhseFloor);
		}
		if(whseParam.get("qty_whse_ceiling")!=null) { // 库存数量上限
			BigDecimal qtyWhseCeiling = new BigDecimal(whseParam.get("qty_whse_ceiling").toString());
			sqlBuffer.append(" AND QTY_WHSE < ?");
			paramArray.add(qtyWhseCeiling);
		}
//		if(whseParam.get("status")!=null){
//			SQLUtil.initSQLIn(whseParam,sqlBuffer ,paramArray, "bmi.status");
//		}
		sqlBuffer.append(" ORDER BY BMI.ITEM_ID ASC");// 默认按照ITEM_ID正序排列
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.toString(), MapUtil.getInt(whseParam, "page_index", 1),
				MapUtil.getInt(whseParam, "page_size", 20), paramArray.toArray());
		whseParam.put("page_count", pageResult.getPageSum());
		whseParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	/**
	 * 更新whse_merch表, 根据参数拼接sql, 必传merch_id, item_id
	 */
	@Override
	public void updateWhseMerch(Map<String, Object> whseMerchParam) throws Exception {
		LOG.debug("WhseDaoImpl updateWhseMerch whseMerchParam: " + whseMerchParam);
		StringBuffer sqlBuffer = new StringBuffer("UPDATE WHSE_MERCH SET");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		String merchId = MapUtil.getString(whseMerchParam, "merch_id");
		String whseDate = MapUtil.getString(whseMerchParam, "whse_date", DateUtil.getToday());
		List<Map<String, Object>> whseMerchList = MapUtil.get(whseMerchParam, "list", null);
		if(whseMerchList==null || whseMerchList.isEmpty()) return; // 如果没有传修改库存的商品列表则不执行sql 梁凯 2014年5月26日17:19:49
		StringBuffer sqlParamBuffer = null;
		for(Map<String, Object> whseMerchMap : whseMerchList) {
			List<Object> paramObject = new ArrayList<Object>();
			sqlParamBuffer = new StringBuffer();
			if(whseMerchMap.get("qty_sub")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = QTY_WHSE - ?,");
				paramObject.add(whseMerchMap.get("qty_sub"));
			} else if(whseMerchMap.get("qty_add")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = QTY_WHSE + ?,");
				paramObject.add(whseMerchMap.get("qty_add"));
			} else if(whseMerchMap.get("qty_whse")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = ?,");
				paramObject.add(whseMerchMap.get("qty_whse"));
			}
			if(whseMerchMap.get("qty_locked")!=null) {
				sqlParamBuffer.append(" QTY_LOCKED = ?,");
				paramObject.add(whseMerchMap.get("qty_locked"));
			}
			if(whseMerchMap.get("qty_whse_warn")!=null) {
				sqlParamBuffer.append(" QTY_WHSE_WARN = ?,");
				paramObject.add(whseMerchMap.get("qty_whse_warn"));
			}
			sqlParamBuffer.append(" OUTPUT_DATE = ?,");
			paramObject.add(whseDate);
			if(whseMerchMap.get("qty_whse_init")!=null) {
				sqlParamBuffer.append(" QTY_WHSE_INIT = ?,");
				paramObject.add(whseMerchMap.get("qty_whse_init"));
			}
			if(whseMerchMap.get("whse_init_date")!=null) {
				sqlParamBuffer.append(" WHSE_INIT_DATE = ?,");
				paramObject.add(whseDate);
			}
			paramObject.add(merchId);
			paramObject.add(whseMerchMap.get("item_id"));
			paramArrayList.add(paramObject.toArray());
		}
		// 删掉最后一个逗号
		if(sqlParamBuffer!=null && sqlParamBuffer.length()!=0){
			sqlParamBuffer.delete(sqlParamBuffer.length()-1, sqlParamBuffer.length());
			sqlBuffer.append(sqlParamBuffer.toString());
		}
		sqlBuffer.append(" WHERE MERCH_ID = ? AND ITEM_ID = ?");
		
		// 执行批量更新
		this.executeBatchSQL(sqlBuffer.toString(), paramArrayList);
	}
	
	/**
	 * 批量更新whse_merch表, 根据参数拼接sql, 必传merch_id, item_id
	 */
	@Override
	public void updateBatchWhseMerch(List<Map<String, Object>> whseMerchParamList) throws Exception {
		LOG.debug("WhseDaoImpl updateBatchWhseMerch whseMerchParamList: " + whseMerchParamList);
		StringBuffer sqlBuffer = new StringBuffer("UPDATE WHSE_MERCH SET");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		if(whseMerchParamList==null || whseMerchParamList.isEmpty()) return; // 如果没有传修改库存的商品列表则不执行sql
		StringBuffer sqlParamBuffer = null;
		for (Map<String, Object> whseMerchMap : whseMerchParamList) {
			List<Object> paramObject = new LinkedList<Object>();
			sqlParamBuffer = new StringBuffer();
			if(whseMerchMap.get("qty_sub")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = QTY_WHSE - ?,");
				paramObject.add(whseMerchMap.get("qty_sub"));
			} else if(whseMerchMap.get("qty_add")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = QTY_WHSE + ?,");
				paramObject.add(whseMerchMap.get("qty_add"));
			} else if(whseMerchMap.get("qty_whse")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = ?,");
				paramObject.add(whseMerchMap.get("qty_whse"));
			}
			if(whseMerchMap.get("qty_locked")!=null) {
				sqlParamBuffer.append(" QTY_LOCKED = ?,");
				paramObject.add(whseMerchMap.get("qty_locked"));
			}
			if(whseMerchMap.get("qty_whse_warn")!=null) {
				sqlParamBuffer.append(" QTY_WHSE_WARN = ?,");
				paramObject.add(whseMerchMap.get("qty_whse_warn"));
			}
			if(whseMerchMap.get("qty_whse_init")!=null) {
				sqlParamBuffer.append(" QTY_WHSE_INIT = ?,");
				paramObject.add(whseMerchMap.get("qty_whse_init"));
			}
			paramObject.add(MapUtil.getString(whseMerchMap, "merch_id"));
			paramObject.add(whseMerchMap.get("item_id"));
			paramArrayList.add(paramObject.toArray());
		}
		// 删掉最后一个逗号
		if(sqlParamBuffer!=null && sqlParamBuffer.length()!=0){
			sqlParamBuffer.delete(sqlParamBuffer.length()-1, sqlParamBuffer.length());
			sqlBuffer.append(sqlParamBuffer.toString());
		}
		sqlBuffer.append(" WHERE MERCH_ID = ? AND ITEM_ID = ?");
		// 执行批量更新
		this.executeBatchSQL(sqlBuffer.toString(), paramArrayList);
	}
	/**
	 * 获取盘点单信息
	 */
	public static final String selectWhseTurnSql = initSelectWhseTurnSql();
	private static String initSelectWhseTurnSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT TURN_ID, MERCH_ID, TURN_DATE, QTY_PROFIT, QTY_LOSS, AMT_PROFIT, AMT_LOSS, STATUS, CRT_DATE, CRT_TIME,");
		sb.append(" NOTE FROM WHSE_TURN WHERE MERCH_ID=?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectWhseTurn(Map<String, Object> whseTurnParam) throws Exception {
		LOG.debug("WhseDaoImpl selectWhseTurn whseTurnParam: " + whseTurnParam);
		StringBuffer sqlBuffer = new StringBuffer(selectWhseTurnSql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(whseTurnParam.get("merch_id"));
		if(whseTurnParam.containsKey("turn_date")) {
			sqlBuffer.append(" AND TURN_DATE=?");
			paramObject.add(whseTurnParam.get("turn_date"));
		}
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	

	/**
	 * 获取盘点单信息
	 */
	public static final String searchWhseTurnLineByItemSql = initSearchWhseTurnLineByItemSql();
	private static String initSearchWhseTurnLineByItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ITEM_ID, NVL(SUM(QTY_PL),0) QTY_PL, NVL(SUM(AMT_PL),0) AMT_PL FROM WHSE_TURN_LINE WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchWhseTurnLineByItem(Map<String, Object> whseTurnLineParam) throws Exception {
		LOG.debug("WhseDaoImpl searchWhseTurnLineByItem whseTurnLineParam: " + whseTurnLineParam);
		StringBuffer sqlBuffer = new StringBuffer(searchWhseTurnLineByItemSql);
		List<Object> paramObject = new ArrayList<Object>();
		String turnId = (String) whseTurnLineParam.get("turn_id");
		if(turnId==null || "".equals(turnId)) { // 如果没有传order_id, 则直接返回空字符串
			return new ArrayList<Map<String, Object>>();
		}
		String[] turnIdArray = turnId.split(",");
		if(turnIdArray.length==1) {
			sqlBuffer.append(" AND TURN_ID = ?");
			paramObject.add(turnIdArray[0]);
		} else {
			sqlBuffer.append(" AND TURN_ID IN (");
			for(int i=0; i<turnIdArray.length; i++) {
				paramObject.add(turnIdArray[i]);
				if(i==0) {
					sqlBuffer.append("?");
				} else {
					sqlBuffer.append(", ?");
				}
			}
			sqlBuffer.append(")");
		}
		sqlBuffer.append(" GROUP BY ITEM_ID"); // 按照item_id分组
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	
	
	/**
	 * 获取今日库存总额和商品种类
	 */
	private static final String selectWhseInfoSql = initSelectWhseInfoSql();
	private static String initSelectWhseInfoSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT item.ITEM_ID, item.ITEM_NAME, NVL(SUM(PRI1 * QTY_WHSE),0) AMT_WHSE_TOTAL FROM BASE_MERCH_ITEM item, WHSE_MERCH whse");
		sb.append(" WHERE item.MERCH_ID = whse.MERCH_ID AND item.ITEM_ID = whse.ITEM_ID AND item.MERCH_ID = ?");
		sb.append(" GROUP BY item.ITEM_ID, item.ITEM_NAME HAVING SUM(PRI1 * QTY_WHSE)!=0");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectWhseInfo(Map<String, Object> whseParam) throws Exception {
		LOG.debug("WhseDaoImpl selectWhseInfo whseParam: " + whseParam);
		return this.selectBySqlQuery(selectWhseInfoSql, new Object[] {whseParam.get("merch_id")});
	}
	/**
	 * 获取某日各商品库存盘差
	 */
	private static final String selectWhseTurnInfoSql = initSelectWhseTurnInfoSql();
	private static String initSelectWhseTurnInfoSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT item.ITEM_ID, item.ITEM_NAME, NVL(SUM(CASE WHEN QTY_PL>=0 THEN PRI1 * QTY_PL END),0) AMT_WHSE_TURN_PROFIT,");
		sb.append(" NVL(SUM(CASE WHEN QTY_PL<0 THEN PRI1 * QTY_PL END),0) AMT_WHSE_TURN_LOSS");
		sb.append(" FROM BASE_MERCH_ITEM item, WHSE_TURN turn, WHSE_TURN_LINE line");
		sb.append(" WHERE item.MERCH_ID = turn.MERCH_ID AND turn.TURN_ID = line.TURN_ID");
		sb.append(" AND item.MERCH_ID = ? AND turn.TURN_DATE = ? GROUP BY item.ITEM_ID, item.ITEM_NAME");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectWhseTurnInfo(Map<String, Object> whseTurnParam) throws Exception {
		LOG.debug("WhseDaoImpl selectWhseTurnInfo whseTurnParam: " + whseTurnParam);
		String merchId = (String) whseTurnParam.get("merch_id");
		String turnDate = (String) whseTurnParam.get("turn_date");
		return this.selectBySqlQuery(selectWhseTurnInfoSql, new Object[] {merchId, turnDate});
	}
	/**
	 * 获取某日库存盘点次数
	 */
	private static final String selectWhseTurnCountSql = initSelectWhseTurnCountSql();
	private static String initSelectWhseTurnCountSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NVL(COUNT(TURN_ID),0) WHSE_TURN_COUNT FROM WHSE_TURN WHERE MERCH_ID = ? AND TURN_DATE = ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectWhseTurnCount(Map<String, Object> whseTurnParam) throws Exception {
		LOG.debug("WhseDaoImpl selectWhseTurnCount whseTurnParam: " + whseTurnParam);
		String merchId = (String) whseTurnParam.get("merch_id");
		String turnDate = (String) whseTurnParam.get("turn_date");
		return this.selectBySqlQuery(selectWhseTurnCountSql, new Object[] {merchId, turnDate});
	}
	/**
	 * 查询库存 -- 关联商品信息 -- 手机,经营提醒
	 */
	public static final String selectWhseMerchJoinMerchItemSql = initSelectWhseMerchJoinMerchItemSql();
	private static String initSelectWhseMerchJoinMerchItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT WHSE.MERCH_ID, WHSE.ITEM_ID,  QTY_LOCKED, QTY_WHSE_WARN, QTY_WHSE_INIT, ");
		sb.append("CASE WHEN QTY_WHSE<0 THEN 0 ELSE QTY_WHSE END QTY_WHSE, OUTPUT_DATE, ");
		sb.append("WHSE_INIT_DATE, item.ITEM_BAR, ITEM_NAME, SHORT_CODE, SHORT_NAME, SPEC, ");
		sb.append("(CASE WHEN  ITEM_KIND_ID='01' THEN '卷烟' ELSE '非卷烟' END) AS ITEM_KIND_ID,");
		sb.append("UNIT_NAME, COST, PRI1, PRI2, PRI4, DISCOUNT, ITEM.BIG_UNIT_NAME, ITEM.BIG_UNIT_RATIO, ");
		sb.append("START_DATE, END_DATE, IS_NEW, IS_OUTSTOCK, IS_RECOMMEND, IS_PROMOTION ");
		sb.append("FROM WHSE_MERCH WHSE, BASE_MERCH_ITEM ITEM ");
		sb.append("WHERE WHSE.MERCH_ID=ITEM.MERCH_ID AND WHSE.ITEM_ID=ITEM.ITEM_ID ");
		sb.append("AND WHSE.MERCH_ID = ? ");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectWhseMerchJoinMerchItem(Map<String, Object> whseParam) throws Exception {
		LOG.debug("WhseDaoImpl selectWhseMerchJoinMerchItem whseParam: " + whseParam);
		StringBuffer sqlBuffer = new StringBuffer(selectWhseMerchJoinMerchItemSql);
		if (WhseServiceImpl.showPositiveWhse) {
			sqlBuffer.append("AND CASE WHEN QTY_WHSE<0 THEN 0 ELSE QTY_WHSE END < QTY_WHSE_WARN ");
		} else {
			sqlBuffer.append("AND QTY_WHSE < QTY_WHSE_WARN ");
		}
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(whseParam.get("merch_id"));
		String itemId = (String) whseParam.get("item_id");
		if(itemId!=null && !"".equals(itemId)) {
			sqlBuffer.append(" AND ITEM_ID = ?");
			paramArray.add(itemId);
		}
		if(whseParam.get("qty_whse_floor")!=null) {
			BigDecimal qtyWhseFloor = new BigDecimal(whseParam.get("qty_whse_floor").toString());
			sqlBuffer.append(" AND QTY_WHSE < ?");
			paramArray.add(qtyWhseFloor);
		}
		sqlBuffer.append(" AND ITEM_KIND_ID=?");
		paramArray.add("01");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	
	
	@Override
	public List<Map<String, Object>> searchMerchItemAndWhse(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseDaoImpl searchMerchItemAndWhse paramMap: " + paramMap);
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT A.MERCH_ID, A.ITEM_ID, A.BIG_BAR ITEM_BAR, A.ITEM_NAME, A.ITEM_KIND_ID, A.BIG_UNIT_NAME UNIT_NAME, ");
		sqlBuilder.append("A.BIG_UNIT_RATIO UNIT_RATIO, A.COST*A.BIG_UNIT_RATIO COST, A.PRI1*A.BIG_UNIT_RATIO PRI1, ");
		sqlBuilder.append("A.BIG_PRI4 PRI4, B.QTY_WHSE/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END QTY_WHSE, ");
		sqlBuilder.append("B.QTY_WHSE_WARN/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END  QTY_WHSE_WARN ");
		sqlBuilder.append("FROM BASE_MERCH_ITEM A, WHSE_MERCH B ");
		sqlBuilder.append("WHERE A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID ");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap, sqlBuilder, paramList, "a.merch_id");
		
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if(MapUtil.getBoolean(paramMap, "isUnqualified")){
			sqlBuilder.append(" AND (A.ITEM_KIND_ID='01' OR A.ITEM_KIND_ID='0102') ");
			sqlBuilder.append(" AND (B.QTY_WHSE<0 OR B.QTY_WHSE/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END>1000 ");
			//此处添加查询合理库存量大于1000条的条件
			sqlBuilder.append(" OR B.QTY_WHSE_WARN/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END>1000) ");
		}
		
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sqlBuilder.append(" AND ( A.ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sqlBuilder.append(" OR A.ITEM_KIND_ID = ? ) ");
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
			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
		}
		String status = MapUtil.getString(paramMap, "status");
		if(!StringUtil.isBlank(status)){
			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.status");
		}
		String itemBar = MapUtil.getString(paramMap, "item_bar");
		if(!StringUtil.isBlank(itemBar)){
			sqlBuilder.append("AND ( a.item_bar = ? or a.BIG_BAR = ? )");
			paramList.add(itemBar);
			paramList.add(itemBar);
		}
		SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_id");
		String keyWord = MapUtil.getString(paramMap, "keyword");
		if(!StringUtil.isBlank(keyWord)){
			StringBuilder keyWordSbl = new StringBuilder();
			keyWordSbl.append(" ");
			keyWordSbl.append(keyWord);
			keyWordSbl.append(" ");
			String newKeyWord = (keyWordSbl.toString().toUpperCase()).replaceAll("\\s+", "%");

			sqlBuilder.append(" AND (a.ITEM_NAME LIKE ?  OR a.ITEM_BAR LIKE ? OR a.BIG_BAR LIKE ? OR a.SHORT_CODE LIKE ? OR a.SHORT_NAME LIKE ? ) ");
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			
		}
		
		Map<String, Object> sortRule = MapUtil.get(paramMap, "sort_rule", Collections.EMPTY_MAP);
		if(MapUtil.remain(sortRule, "qty_whse", "item_id").isEmpty()) {
			SQLUtil.initSQLOrder(sqlBuilder, "a.create_date", "desc", "a.create_time", "desc", "a.item_id", "desc");
		} else {
			SQLUtil.initSQLOrder(sqlBuilder, sortRule, "b.qty_whse", "a.item_id");
		}
		Page pageResult=this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(paramMap, "page_index", 1), MapUtil.getInt(paramMap, "page_size", 20), paramList.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	@Override
	public List selectNotRemovedMerchItemAndWhse(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseDaoImpl searchNotRemovedMerchItemAndWhse paramMap: " + paramMap);
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT A.MERCH_ID, A.ITEM_ID, A.BIG_BAR ITEM_BAR, A.ITEM_NAME, A.ITEM_KIND_ID, A.BIG_UNIT_NAME UNIT_NAME, ");
		sqlBuilder.append("A.BIG_UNIT_RATIO UNIT_RATIO, A.COST*A.BIG_UNIT_RATIO COST, A.PRI1*A.BIG_UNIT_RATIO PRI1, A.STATUS, ");
		sqlBuilder.append("A.BIG_PRI4 PRI4, B.QTY_WHSE/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END QTY_WHSE, ");
		sqlBuilder.append("B.QTY_WHSE_WARN/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END  QTY_WHSE_WARN ");
		sqlBuilder.append("FROM BASE_MERCH_ITEM A, WHSE_MERCH B ");
		sqlBuilder.append("WHERE A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID AND A.STATUS IN (1,2)");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap, sqlBuilder, paramList, "a.merch_id");
		
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if(MapUtil.getBoolean(paramMap, "isUnqualified")){
			sqlBuilder.append(" AND (A.ITEM_KIND_ID='01' OR A.ITEM_KIND_ID='0102') ");
			sqlBuilder.append(" AND (B.QTY_WHSE<0 OR B.QTY_WHSE/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END>1000 ");
			//此处添加查询合理库存量大于1000条的条件
			sqlBuilder.append(" OR B.QTY_WHSE_WARN/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END>1000) ");
		}
		
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sqlBuilder.append(" AND ( A.ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sqlBuilder.append(" OR A.ITEM_KIND_ID = ? ) ");
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
			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
		}
		
		String itemBar = MapUtil.getString(paramMap, "item_bar");
		if(!StringUtil.isBlank(itemBar)){
			sqlBuilder.append("AND ( a.item_bar = ? or a.BIG_BAR = ? )");
			paramList.add(itemBar);
			paramList.add(itemBar);
		}
		SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_id");
		String keyWord = MapUtil.getString(paramMap, "keyword");
		if(!StringUtil.isBlank(keyWord)){
			StringBuilder keyWordSbl = new StringBuilder();
			keyWordSbl.append(" ");
			keyWordSbl.append(keyWord);
			keyWordSbl.append(" ");
			String newKeyWord = (keyWordSbl.toString().toUpperCase()).replaceAll("\\s+", "%");

			sqlBuilder.append(" AND (a.ITEM_NAME LIKE ?  OR a.ITEM_BAR LIKE ? OR a.BIG_BAR LIKE ? OR a.SHORT_CODE LIKE ? OR a.SHORT_NAME LIKE ? ) ");
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			
		}
		
		Map<String, Object> sortRule = MapUtil.get(paramMap, "sort_rule", Collections.EMPTY_MAP);
		if(MapUtil.remain(sortRule, "qty_whse", "item_id").isEmpty()) {
			SQLUtil.initSQLOrder(sqlBuilder, "a.create_date", "desc", "a.create_time", "desc", "a.item_id", "desc");
		} else {
			SQLUtil.initSQLOrder(sqlBuilder, sortRule, "b.qty_whse", "a.item_id");
		}
		Page pageResult=this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(paramMap, "page_index", 1), MapUtil.getInt(paramMap, "page_size", 20), paramList.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	@Override
	public List selectRemovedMerchItemAndWhse(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseDaoImpl searchRemovedMerchItemAndWhse paramMap: " + paramMap);
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT A.MERCH_ID, A.ITEM_ID, A.BIG_BAR ITEM_BAR, A.ITEM_NAME, A.ITEM_KIND_ID, A.BIG_UNIT_NAME UNIT_NAME, ");
		sqlBuilder.append("A.BIG_UNIT_RATIO UNIT_RATIO, A.COST*A.BIG_UNIT_RATIO COST, A.PRI1*A.BIG_UNIT_RATIO PRI1, A.STATUS, ");
		sqlBuilder.append("A.BIG_PRI4 PRI4, B.QTY_WHSE/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END QTY_WHSE, ");
		sqlBuilder.append("B.QTY_WHSE_WARN/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END  QTY_WHSE_WARN ");
		sqlBuilder.append("FROM BASE_MERCH_ITEM A, WHSE_MERCH B ");
		sqlBuilder.append("WHERE A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID AND A.STATUS=0");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramMap, sqlBuilder, paramList, "a.merch_id");
		
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if(MapUtil.getBoolean(paramMap, "isUnqualified")){
			sqlBuilder.append(" AND (A.ITEM_KIND_ID='01' OR A.ITEM_KIND_ID='0102') ");
			sqlBuilder.append(" AND (B.QTY_WHSE<0 OR B.QTY_WHSE/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END>1000 ");
			//此处添加查询合理库存量大于1000条的条件
			sqlBuilder.append(" OR B.QTY_WHSE_WARN/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END>1000) ");
		}
		
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sqlBuilder.append(" AND ( A.ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sqlBuilder.append(" OR A.ITEM_KIND_ID = ? ) ");
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
			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
		}
		
		String itemBar = MapUtil.getString(paramMap, "item_bar");
		if(!StringUtil.isBlank(itemBar)){
			sqlBuilder.append("AND ( a.item_bar = ? or a.BIG_BAR = ? )");
			paramList.add(itemBar);
			paramList.add(itemBar);
		}
		SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_id");
		String keyWord = MapUtil.getString(paramMap, "keyword");
		if(!StringUtil.isBlank(keyWord)){
			StringBuilder keyWordSbl = new StringBuilder();
			keyWordSbl.append(" ");
			keyWordSbl.append(keyWord);
			keyWordSbl.append(" ");
			String newKeyWord = (keyWordSbl.toString().toUpperCase()).replaceAll("\\s+", "%");

			sqlBuilder.append(" AND (a.ITEM_NAME LIKE ?  OR a.ITEM_BAR LIKE ? OR a.BIG_BAR LIKE ? OR a.SHORT_CODE LIKE ? OR a.SHORT_NAME LIKE ? ) ");
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			paramList.add(newKeyWord);
			
		}
		
		Map<String, Object> sortRule = MapUtil.get(paramMap, "sort_rule", Collections.EMPTY_MAP);
		if(MapUtil.remain(sortRule, "qty_whse", "item_id").isEmpty()) {
			SQLUtil.initSQLOrder(sqlBuilder, "a.create_date", "desc", "a.create_time", "desc", "a.item_id", "desc");
		} else {
			SQLUtil.initSQLOrder(sqlBuilder, sortRule, "b.qty_whse", "a.item_id");
		}
		Page pageResult=this.searchPaginatedBySql(sqlBuilder.toString(), 
				MapUtil.getInt(paramMap, "page_index", 1), MapUtil.getInt(paramMap, "page_size", 20), paramList.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	@Override
	public List<Map<String, Object>> searchMerchWhseQuantityAndAmountByKind(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseDaoImpl countMerchWhseQuantityAndAmountByKind paramMap: " + paramMap);
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT A.ITEM_KIND_ID, ");
		sqlBuilder.append("SUM(B.QTY_WHSE/ CASE WHEN A.BIG_UNIT_RATIO IS NULL OR A.BIG_UNIT_RATIO = 0 THEN 1 ELSE A.BIG_UNIT_RATIO END) WHSE_QUANTITY, ");
		sqlBuilder.append("SUM(B.QTY_WHSE*A.COST) WHSE_AMOUNT ");
		sqlBuilder.append("FROM BASE_MERCH_ITEM A, WHSE_MERCH B WHERE A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID ");
		sqlBuilder.append("AND A.STATUS IN ('1','2') ");
		
		List<Object> paramList = new ArrayList<Object>();
		
		SQLUtil.initSQLEqual(paramMap, sqlBuilder, paramList, "a.merch_id");
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sqlBuilder.append(" AND ( A.ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sqlBuilder.append(" OR A.ITEM_KIND_ID = ? ) ");
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
			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
		}
		
		if (WhseServiceImpl.showPositiveWhse) {
			sqlBuilder.append(" AND B.QTY_WHSE>0 ");
		}
		
		sqlBuilder.append(" GROUP BY a.ITEM_KIND_ID");
		return this.selectBySqlQuery(sqlBuilder.toString(), paramList.toArray());
	}
	
	private static final String whseBaseSql = getWhseBaseSql();
	private static String getWhseBaseSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT wm.MERCH_ID, wm.ITEM_ID, QTY_WHSE, QTY_LOCKED,");
		sb.append(" QTY_WHSE_WARN, WHSE_INIT_DATE, QTY_WHSE_INIT, bmiu.BIG_BAR ITEM_BAR, bmiu.SEQ_ID, case when unit_ratio is null or unit_ratio=0 then 1 else unit_ratio end unit_ratio");
		sb.append(" FROM WHSE_MERCH wm, BASE_MERCH_ITEM_UNIT bmiu");
		sb.append(" WHERE wm.MERCH_ID = bmiu.MERCH_ID AND wm.ITEM_ID = bmiu.ITEM_ID and bmiu.item_id=bmiu.big_bar");
		sb.append(" AND wm.MERCH_ID = ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> getWhseList(Map<String, Object> whseParams) throws Exception {
		LOG.debug("WhseDaoImpl whseBaseSql=" + whseBaseSql);
		String merchId =MapUtil.getString(whseParams, "merch_id");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if(whseParams.get("seq_id")!=null) {
			String seqId = (String) whseParams.get("seq_id");
			StringBuffer sqlBuffer = new StringBuffer(whseBaseSql);
			sqlBuffer.append(" AND bmiu.SEQ_ID = ?");
			resultList = this.selectBySqlQuery(sqlBuffer.toString(), new Object[]{merchId, seqId});
		} else if(whseParams.get("item_id")!=null) {
			String itemId = (String) whseParams.get("item_id");
			StringBuffer sqlBuffer = new StringBuffer(whseBaseSql);
			sqlBuffer.append(" AND wm.ITEM_ID = ?");
			resultList = this.selectBySqlQuery(sqlBuffer.toString(), new Object[]{merchId, itemId});
		} else if(whseParams.get("item_bar")!=null) {
			String itemBar = (String) whseParams.get("item_bar");
			StringBuffer sqlBuffer = new StringBuffer(whseBaseSql);
			sqlBuffer.append(" AND BIG_BAR = ?");
			resultList = this.selectBySqlQuery(sqlBuffer.toString(), new Object[]{merchId, itemBar});
		} else {
			resultList = this.selectBySqlQuery(whseBaseSql, new Object[]{merchId});
		}
		return resultList;
	}
	public static final String originWhseSql =getOriginWhseSql();	
	private static String getOriginWhseSql() {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_BAR,BMI.ITEM_NAME,BMI.UNIT_NAME,");
		sb.append(" bmi.cost,bmi.pri4,");
		sb.append(" BMI.SHORT_CODE,WM.QTY_WHSE,WM.QTY_WHSE_WARN,WM.QTY_WHSE_INIT,WM.WHSE_INIT_DATE");
		sb.append(" FROM WHSE_MERCH WM,BASE_MERCH_ITEM BMI");
		sb.append(" WHERE WM.MERCH_ID = BMI.MERCH_ID AND WM.ITEM_ID=BMI.ITEM_ID AND WM.MERCH_ID=?");
		sb.append(" AND WM.OUTPUT_DATE IS NULL");
		return sb.toString();
	}
	@Override
	public List getOriginWhseList(Map<String, Object> map) throws Exception {
		String merchId = (String) map.get("refId");
//		String itemBar=(String) map.get("itemBar");
		String pageSize=(String) map.get("page_size");
		String pageIndex=(String) map.get("page_index");
		Page pageResult=this.searchPaginatedBySql(originWhseSql, Integer.parseInt(pageIndex), Integer.parseInt(pageSize),new Object[]{merchId});
		LOG.debug("WhseDaoImpl originWhseSql=" + originWhseSql);
		map.put("count", pageResult.getTotal());
		map.put("page_count", pageResult.getPageSum());
		return pageResult.getRows(); 
	}
	public static final String originWhseSqls =getOriginWhseSqls();
	private static String getOriginWhseSqls() {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_BAR,BMI.ITEM_NAME,BMI.UNIT_NAME,bmi.cost,bmi.pri4,");
		sb.append(" BMI.SHORT_CODE,WM.QTY_WHSE,WM.QTY_WHSE_WARN,WM.QTY_WHSE_INIT,WM.WHSE_INIT_DATE");
		sb.append(" FROM WHSE_MERCH WM,BASE_MERCH_ITEM BMI");
		sb.append(" WHERE WM.MERCH_ID = BMI.MERCH_ID AND WM.ITEM_ID=BMI.ITEM_ID AND WM.MERCH_ID=?");
		sb.append(" AND WM.OUTPUT_DATE IS NULL");
		sb.append(" and bmi.item_bar=?");
		return sb.toString();
	}
	@Override
	public List getOriginWhseLists(Map<String,Object> map) throws Exception {
		String merchId = (String) map.get("refId");
		String itemBar=(String) map.get("itemBar");
		String pageIndex=(String) map.get("page_index");
		String pageSize=(String) map.get("page_size");		
		LOG.debug("WhseDaoImpl originWhseSql=" + originWhseSqls);
		Page pageResult=this.searchPaginatedBySql(originWhseSqls, Integer.parseInt(pageIndex), Integer.parseInt(pageSize), new Object[]{merchId,itemBar});
		map.put("count", pageResult.getTotal());
		map.put("page_count", pageResult.getPageSum());
		return pageResult.getRows(); 
	} 
	public static final String getTakeStockSql=getTakeStockSql();
	private static String getTakeStockSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("select max(bmi.item_name)item_name ,");
	    sb.append(" wtl.item_id,");
	    sb.append(" max(bmi.item_bar) item_bar,");
	    sb.append(" sum(wtl.qty_pl) qty_pl,");
	    sb.append(" max(bmi.cost) cost,max(bmi.pri4) pri4");
	    sb.append(" from whse_turn_line wtl, whse_turn wt, base_merch_item bmi");
	    sb.append(" where bmi.item_id = wtl.item_id"); 
	    sb.append(" and wt.merch_id=bmi.merch_id and wt.merch_id=?");
	    sb.append(" and wt.turn_id = wtl.turn_id");
	    sb.append(" and wt.turn_date >= ?");
	    sb.append(" and wt.turn_date <= ?");
	    sb.append(" group by wtl.item_id");
		return sb.toString();
	}
	/**
	 * 盘点记录
	 */
	@Override
	public List getTakeStock(Map<String, Object> pdmap) throws Exception{
		LOG.debug("WhseDaoImpl getTakeStockSql=" + getTakeStockSql);		
		String merch_id=pdmap.get("refId").toString();
		String begin_Time=pdmap.get("beginTime").toString();
		String end_Time=pdmap.get("endTime").toString();
		String pageIndex=pdmap.get("page_index").toString();
		String pageSize=pdmap.get("page_size").toString();		
		Page pageResult=this.searchPaginatedBySql(getTakeStockSql, Integer.parseInt(pageIndex), Integer.parseInt(pageSize),new Object[]{merch_id,begin_Time,end_Time});
//		List<Map<String, Object>> resultList = this.selectBySqlQuery(getTakeStockSql, new Object[]{merch_id,begin_Time,end_Time});		
//		List<Map<String, Object>>list= pageResult.getRows();		
		pdmap.put("page_count", pageResult.getPageSum());
		pdmap.put("count", pageResult.getTotal());
		
		return pageResult.getRows(); 
	} 
	
	/**
	 * 盘点记录
	 */
	public static final String selectWhseTurn=getWhseTurnSql();
	private static String getWhseTurnSql(){
		StringBuffer sql=new StringBuffer();
//		sql.append(" select wt.turn_id, status, crt_date, crt_time, note, turn_item_count, qty_p, qty_l  ");
//		sql.append(" from whse_turn wt,  ");
//		sql.append(" ( select turn_id, count(*) turn_item_count, nvl(sum(case when qty_pl>0 then qty_pl end),0) qty_p,   ");
//		sql.append(" nvl(sum(case when qty_pl<0 then qty_pl end),0) qty_l  from whse_turn_line group by turn_id ) wtl   ");
//		sql.append(" where wt.turn_id = wtl.turn_id 	  ");
//		sql.append(" and wt.merch_id=?   ");
//		sql.append(" and wt.CRT_DATE between ? and ?  ");
//		sql.append(" order by crt_date desc,crt_time desc  ");
		sql.append(" select TURN_ID, MERCH_ID, TURN_DATE, QTY_PROFIT, QTY_LOSS, AMT_PROFIT, AMT_LOSS, STATUS, ");
		sql.append(" CRT_DATE, CRT_TIME, OPERATOR, NOTE  ");
		sql.append(" from WHSE_TURN  ");
		sql.append(" where MERCH_ID=? ");
		sql.append(" and CRT_DATE between ? and ? ");
		sql.append(" order by crt_date desc,crt_time desc ");
		return sql.toString();
	}
	public List getWhseTurn(Map<String, Object> pdmap ) throws Exception{ 
		String merch_id=(String) pdmap.get("merch_id") ;
		String begin_Time=(String) pdmap.get("begintime");
		String end_Time=(String) pdmap.get("endtime");
		String pageIndex=(String) pdmap.get("page_index") ;
		String pageSize=(String) pdmap.get("page_size"); 
		Page pageResult=this.searchPaginatedBySql(selectWhseTurn, Integer.parseInt(pageIndex), Integer.parseInt(pageSize),new Object[]{merch_id,begin_Time,end_Time});
//		List<Map<String, Object>> resultList = this.selectBySqlQuery(getTakeStockSql, new Object[]{merch_id,begin_Time,end_Time});		
//		List<Map<String, Object>>list= pageResult.getRows();		
		pdmap.put("page_count", pageResult.getPageSum());
		pdmap.put("count", pageResult.getTotal());
		return pageResult.getRows(); 
	} 
	public static final String getTakeStockXiangXiSql=getTakeStockXiangXiSql();
	private static String getTakeStockXiangXiSql(){
		StringBuffer sb=new StringBuffer();
			sb.append("select bmi.item_name, wtl.qty_pl, wtl.pl_reason, wt.turn_date, wt.crt_time");
			sb.append(" from whse_turn_line wtl, base_merch_item bmi, whse_turn wt");
			sb.append(" where wtl.item_id = ?");
			sb.append(" and bmi.item_id = wtl.item_id");
			sb.append(" and wt.turn_id = wtl.turn_id");
			sb.append(" and wt.turn_date >= ?");
			sb.append(" and wt.turn_date <= ?");
			sb.append(" and wt.merch_id = bmi.merch_id");
			sb.append(" and wt.merch_id = ?");
			sb.append(" order by wt.turn_date desc, wt.crt_time desc");
			return sb.toString();
	}
	@Override
	public List getTakeStockXiangXi(String itemId,String merchId,String beginTime,String endTime) throws Exception{
		LOG.debug("WhseDaoImpl getTakeStockXiangXiSql=" + getTakeStockXiangXiSql);
		String item_id=itemId;
		String merch_id=merchId;
		String begin_Time=beginTime;
		String end_Time=endTime;
		List<Map<String,Object>> list=this.selectBySqlQuery(getTakeStockXiangXiSql,new Object[]{item_id,begin_Time,end_Time,merch_id});
		return list;
	}
	
	//获得盘点记录单行
	public static final String selectWhseTurnLine=getWhseTurnLineSql();
	private static String getWhseTurnLineSql(){
		StringBuffer sql=new StringBuffer();
		sql.append("  select turn_id,bmi.item_name,bmi.item_bar,qty_whse,qty_turn,qty_pl,pl_reason,note,bmi.item_id,wtl.amt_pl ");
		sql.append("  from whse_turn_line wtl ,base_merch_item bmi  ");
		sql.append("  where bmi.item_id=wtl.item_id ");
		sql.append("  and  turn_id=?  ");
		sql.append("  and bmi.merch_id=?  ");
		return sql.toString();				
	}
	@Override
	public List getTakeStockXiangXi(Map<String, Object> turnMap) throws Exception{
		LOG.debug("WhseDaoImpl getTakeStockXiangXi turnMap:"+turnMap);
		return this.selectBySqlQuery(selectWhseTurnLine,new Object[]{turnMap.get("turn_id"),turnMap.get("merch_id")});
//		LOG.debug("WhseDaoImpl getTakeStockXiangXiSql=" + getTakeStockXiangXiSql);
//		String item_id=itemId;
//		String merch_id=merchId;
//		String begin_Time=beginTime;
//		String end_Time=endTime;
//		List<Map<String,Object>> list=this.selectBySqlQuery(getTakeStockXiangXiSql,new Object[]{item_id,begin_Time,end_Time,merch_id});
//		return list;
	}
	public static final String updateOriginWhseSql = getUpdateOriginWhseSql();	
	private static String getUpdateOriginWhseSql() {
		StringBuffer sb = new StringBuffer();
		sb.append(" UPDATE WHSE_MERCH");
		sb.append(" SET QTY_WHSE_INIT=?,WHSE_INIT_DATE=?,QTY_WHSE=?");
		sb.append(" WHERE MERCH_ID = ?");
		sb.append(" AND ITEM_ID = ?");
		return sb.toString();
	}
	@Override
	public void updateOriginWhseList(Map map) throws Exception {		
		LOG.debug("WhseDaoImpl updateOriginWhseSql=" + updateOriginWhseSql);
		//获取参数
		String curDate = (String) map.get("curDate");
		String merchId = (String) map.get("refId");
		List<Map<String, Object>> list = (List) map.get("list");
		//声明对象
		List sqlList = new ArrayList();
		for(Map mapRow : list){
			Object[] array = new Object[5];
			array[0] = mapRow.get("qty_whse_init").toString();
			array[1] = curDate;
			array[2] = mapRow.get("qty_whse_init").toString();
			array[3] = merchId;
			array[4] = mapRow.get("item_id").toString();
			sqlList.add(array);
		}
		this.executeBatchSQL(updateOriginWhseSql, sqlList);		
	}	
	public static final String updateWarnWhseSql = getUpdateWarnWhseSql();	
	private static String getUpdateWarnWhseSql() {
		StringBuffer sb = new StringBuffer();
		sb.append(" UPDATE WHSE_MERCH");
		sb.append(" SET QTY_WHSE_WARN=?");
		sb.append(" WHERE MERCH_ID = ?");
		sb.append(" AND ITEM_ID = ?");
		return sb.toString();
	}
	@Override
	public void updateWhseWarnQuantity(Map<String, Object> whseParam) throws Exception {		
		LOG.debug("WhseDaoImpl updateWhseWarnQuantity whseParam: " + whseParam);
		//获取参数
		List<Map<String, Object>> list = (List) whseParam.get("list");
		//声明对象
		List sqlList = new ArrayList();
		for(Map<String, Object> mapRow : list){
			Object[] array = new Object[3];
			array[0] = mapRow.get("qty_whse_warn");
			array[1] = whseParam.get("merch_id");
			array[2] = mapRow.get("item_id");
			sqlList.add(array);
		}
		this.executeBatchSQL(updateWarnWhseSql, sqlList);		
	}
	
	public static final String insertWhseTurnSql = initInsertWhseTurnSql();
	private static String initInsertWhseTurnSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("INSERT INTO WHSE_TURN (TURN_ID, MERCH_ID, TURN_DATE, CRT_DATE, CRT_TIME, QTY_PROFIT, QTY_LOSS,");
		sb.append(" AMT_PROFIT, AMT_LOSS, NOTE, OPERATOR, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	/**
	 * 梁凯	2014年5月29日15:00:20
	 * 插入盘点单
	 */
	@Override
	public void insertWhseTurn(Map<String, Object> turnParam) throws Exception{
		LOG.debug("WhseDaoImpl insertWhseTurn turnParam: " + turnParam);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(MapUtil.getString(turnParam, "turn_id"));
		paramList.add(MapUtil.getString(turnParam, "merch_id"));
		paramList.add(MapUtil.getString(turnParam, "turn_date"));
		paramList.add(MapUtil.getString(turnParam, "crt_date"));
		paramList.add(MapUtil.getString(turnParam, "crt_time"));
		paramList.add(MapUtil.get(turnParam, "qty_profit", 0.0));
		paramList.add(MapUtil.get(turnParam, "qty_loss", 0.0));
		paramList.add(MapUtil.get(turnParam, "amt_profit", 0.0));
		paramList.add(MapUtil.get(turnParam, "amt_loss", 0.0));
		paramList.add(MapUtil.getString(turnParam, "note"));
		paramList.add(MapUtil.getString(turnParam, "operator"));
		paramList.add(MapUtil.getString(turnParam, "status")); // STATUS = '02'
		this.executeSQL(insertWhseTurnSql, paramList.toArray());
	} 
	
	public static final String insertWhseTurnLineSql = initInsertWhseTurnLineSql();
	private static String initInsertWhseTurnLineSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("INSERT INTO WHSE_TURN_LINE (TURN_ID, MERCH_ID, ITEM_ID, QTY_WHSE, QTY_TURN, QTY_PL,");
		sb.append(" AMT_PL, PL_REASON, NOTE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	/**
	 * 梁凯	2014年5月29日14:57:03
	 * 插入盘点单行
	 */
	@Override
	public void insertWhseTurnLine(Map<String, Object> turnLineParam) throws Exception{
		LOG.debug("WhseDaoImpl insertWhseTurnLine turnLineParam: " + turnLineParam);
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		List<Object> paramList = null;
		String merchId = MapUtil.getString(turnLineParam, "merch_id");
		String turnId = MapUtil.getString(turnLineParam, "turn_id");
		List<Map<String, Object>> turnLineList = MapUtil.get(turnLineParam, "list", Collections.EMPTY_LIST);
		if(!turnLineList.isEmpty()) {
			for(Map<String, Object> turnLineMap : turnLineList) {
				paramList = new ArrayList<Object>();
				paramList.add(turnId);
				paramList.add(merchId);
				paramList.add(MapUtil.getString(turnLineMap, "item_id"));
				paramList.add(MapUtil.get(turnLineMap, "qty_whse", 0.0));
				paramList.add(MapUtil.get(turnLineMap, "qty_turn", 0.0));
				paramList.add(MapUtil.get(turnLineMap, "qty_pl", 0.0));
				paramList.add(MapUtil.get(turnLineMap, "amt_pl", 0.0));
				paramList.add(MapUtil.getString(turnLineMap, "pl_reason"));
				paramList.add(MapUtil.getString(turnLineMap, "note"));
				paramArrayList.add(paramList.toArray());
			}
		}
		this.executeBatchSQL(insertWhseTurnLineSql, paramArrayList);
	} 
	
	//扣减库存，更新最后入库日期
	public static final String updateInventorySql=updateInventorySql();
	private static String updateInventorySql(){
		StringBuffer sb=new StringBuffer();
		sb.append("update WHSE_MERCH");
		sb.append(" set qty_whse=?,output_date=?");
		sb.append(" WHERE MERCH_ID = ?");
		sb.append(" AND ITEM_ID = ?");
		return sb.toString();
	}
	@Override
	public void updateInventory(Map map) throws Exception {		
		LOG.debug("WhseDaoImpl updateInventorySql=" + updateInventorySql);
		//获取参数
		String output_date =(String)map.get("turnDate");
		String merch_id=(String)map.get("refId");
		List<Map<String, Object>> list = (List) map.get("list");
		//声明对象
		List sqlList = new ArrayList();
		for(Map mapRow : list){
			Object[] array = new Object[4];
			array[0] = mapRow.get("qty_turn").toString();
			array[1] = output_date;
			array[2] = merch_id;
			array[3] =mapRow.get("item_id").toString();
			sqlList.add(array);
		}
		this.executeBatchSQL(updateInventorySql, sqlList);
	} 
	public static final String insertWhseMerchSql = initInsertWhseMerchSql();
	private static String initInsertWhseMerchSql(){
		StringBuffer sb = new StringBuffer("INSERT INTO WHSE_MERCH (MERCH_ID, ITEM_ID, QTY_WHSE, STATUS) VALUES (?, ?, ?, ?)");
		return sb.toString();
	} 
	@Override
	public void insertWhseMerch(List<Map<String, Object>> whseMerchParam) throws Exception {
		LOG.debug("WhseDaoImpl insertWhseMerch whseMerchParam: " + whseMerchParam);
		StringBuffer sqlBuffer = new StringBuffer(insertWhseMerchSql);
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> whseMerchMap : whseMerchParam) {
			Object[] paramObject = new Object[4];
			paramObject[0] = whseMerchMap.get("merch_id");
			paramObject[1] = whseMerchMap.get("item_id");
			paramObject[2] = whseMerchMap.get("qty_whse");
			paramObject[3] = whseMerchMap.get("status");
			paramArray.add(paramObject);
		}
		this.executeBatchSQL(sqlBuffer.toString(), paramArray);
	} 
	@Override
	public void insertWhseMerch(Map<String, Object> whseParams) throws Exception {
		LOG.debug("WhseDaoImpl insertWhseMerch: " + insertWhseMerchSql);
		StringBuffer sqlBuffer = new StringBuffer(insertWhseMerchSql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(whseParams.get("merch_id"));
		paramObject.add(whseParams.get("item_id"));
		paramObject.add(whseParams.get("qty_whse"));
		paramObject.add(whseParams.get("status"));
		executeSQL(sqlBuffer.toString(), paramObject.toArray());
	} 
	private static final String updateWhseMerchSimpleSql = getUpdateWhseMerchSql();
	private static String getUpdateWhseMerchSql(){
		StringBuffer sb = new StringBuffer("UPDATE WHSE_MERCH SET QTY_WHSE = ? WHERE MERCH_ID = ? AND ITEM_ID = ?");
		return sb.toString();
	} 
	@Override
	public void updateWhseMerchSimple(List<Map<String, Object>> whseParams) throws Exception {
		LOG.debug("WhseDaoImpl updateWhseMerch: " + updateWhseMerchSimpleSql);
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> whseParam : whseParams) {
			Object[] objectArray = new Object[3];
			BigDecimal quantity = new BigDecimal(whseParam.get("quantity").toString());
			BigDecimal qtyWhse = new BigDecimal(whseParam.get("qty_whse").toString());
			objectArray[0] = quantity.add(qtyWhse);
			objectArray[1] = whseParam.get("merch_id");
			objectArray[2] = whseParam.get("item_id");
			paramArray.add(objectArray);
		}
		this.executeBatchSQL(updateWhseMerchSimpleSql, paramArray);
	}	
	@Override
	public void insertOrder(Map map) throws Exception {
		String sql="INSERT INTO ORDER_MERCH (MERCH_ID, ORDER_ID, IN_TIME, NOTE) VALUES (?, ?, ?, ?) ";
//		LOG.debug("WhseDaoImpl insertOrder: " + sql);
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	     String thisData = sdf.format(new Date());
		executeSQL(sql, new Object[] {map.get("MERCH_ID"), map.get("ORDER_ID"), thisData, map.get("NOTE")});
	}	
	
	
	@Override
	public void updateWhse(List<Map<String, Object>> mapp) throws Exception {
		String sql="UPDATE WHSE_MERCH SET QTY_WHSE =QTY_WHSE+? , OUTPUT_DATE=TO_CHAR(SYSDATE,'yyyyMMdd') WHERE MERCH_ID = ? AND ITEM_ID = ?";
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map map : mapp) {
	    	Object[] objectArray = new Object[3];
	    	 if(map.containsKey("QTY_WHSE")){
	    		 objectArray[0] = Integer.valueOf(((String)map.get("QTY_WHSE")).trim());
	    	 }else{
	    		 objectArray[0] = Integer.valueOf(((String)map.get("qty_whse")).trim());
	    	 }
	    	 if(map.containsKey("MERCH_ID")){
	    		 objectArray[1] = map.get("MERCH_ID");
	    	 }else{
	    		 objectArray[1] = map.get("merch_id");
	    	 }
	    	 if(map.containsKey("ITEM_ID")){
	    		 objectArray[2] = map.get("ITEM_ID");
	    	 }else{
	    		 objectArray[2] = map.get("item_id");
	    	 }
			
			
			
			paramArray.add(objectArray);
	     }
		this.executeBatchSQL(sql, paramArray);
		
	}
	@Override
	public boolean selectWhse(Map map) throws Exception {
		String sql="SELECT MERCH_ID,ITEM_ID FROM WHSE_MERCH WHERE MERCH_ID=? AND ITEM_ID=?  ";
		List<Map<String, Object>> resultList=null;
		if(map.containsKey("MERCH_ID")&&map.containsKey("ITEM_ID")){
			resultList = this.selectBySqlQuery(sql, new Object[]{map.get("MERCH_ID"),map.get("ITEM_ID")});
		}else{
			resultList = this.selectBySqlQuery(sql, new Object[]{map.get("merch_id"),map.get("item_id")});
		}
		
		if(resultList.size()>0){
			return true;
		}
		return false;
	}
	@Override
	public List<Map<String, Object>> selectOrderList(Map map) throws Exception {
		LOG.debug("WhseDaoImpl selectOrderList map: " + map);
		String sql="SELECT MERCH_ID,ORDER_ID, NOTE FROM ORDER_MERCH WHERE MERCH_ID=?";
		List<Map<String, Object>> resultList=null;
		if(map.containsKey("MERCH_ID")){
			 resultList = this.selectBySqlQuery(sql, new Object[]{map.get("MERCH_ID")});
		}else{
			 resultList = this.selectBySqlQuery(sql, new Object[]{map.get("merch_id")});
		}
		return resultList;
	}
	
	public static final String deleteWhseMerchSql = initDeleteWhseMerchSql();
	private static String initDeleteWhseMerchSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE WHSE_MERCH SET STATUS='0' WHERE MERCH_ID=? AND ITEM_ID=?");
		return sb.toString();
	}
	
	@Override
	public void deleteWhseMerch(Map<String, Object> whseMerchParam) throws Exception {
		LOG.debug("WhseDaoImpl deleteWhseMerch whseMerchParam: " + whseMerchParam);
		StringBuffer sqlBuffer = new StringBuffer(deleteWhseMerchSql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(whseMerchParam.get("merch_id"));
		paramObject.add(whseMerchParam.get("item_id"));
		
		this.executeSQL(sqlBuffer.toString(), paramObject.toArray());
	}
	
	@Override
	public void deleteWhseMerch(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("WhseDaoImpl deleteWhseMerch paramList: " + paramList);		
		StringBuilder sql = new StringBuilder("DELETE WHSE_MERCH WHERE");
		List<Object[]> list = new ArrayList<Object[]>();
		int index = 0;
		for(Map<String, Object> gift : paramList) {
			List<Object> list2 = new ArrayList<Object>();
			if(index++==0) {
				SQLUtil.initSQLEqual(gift, sql, list2, "merch_id", "item_id");
			} else {
				SQLUtil.initSQLEqual(gift, list2, "merch_id", "item_id");
			}
			list.add(list2.toArray());
		}
		this.executeBatchSQL(sql.toString(), list);
	}
	
	public static final String selectStockReport=getStockReport();
	private static String getStockReport(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select  nvl(substr(bmi.item_kind_id,0,2),99) item_kind,sum(QTY_WHSE) sum_qty_whse ,sum(pri4*QTY_WHSE) sum_money  ");
		sql.append(" from base_merch_item bmi,WHSE_MERCH wm  ");
		sql.append(" where bmi.ITEM_ID=wm.ITEM_ID  and bmi.merch_id=wm.merch_id	   ");
		sql.append("   and  bmi.merch_id=? ");
		sql.append(" group by substr(bmi.item_kind_id,0,2)   ");
		return sql.toString();
	}
	//库存报表
	public List<Map<String, Object>> stockReport(Map<String, Object> stockMap)throws Exception{
		LOG.debug("WhseDaoImpl stockReport stockMap:"+stockMap);
		Object[] obj={stockMap.get("merch_id")};
	
		return selectBySqlQuery(selectStockReport,obj);
	}
	@Override
	public List<Map<String, Object>> selectNoRemovedItem(Map<String, Object> itemParam) throws Exception {
		StringBuilder sql = new StringBuilder("SELECT 'MERCH' FLAG,BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_BAR,BMI.BIG_UNIT_NAME UNIT_NAME,BMIU.ITEM_UNIT_NAME,");
		sql.append("BMI.SHORT_CODE,BMI.SHORT_NAME,BMI.ITEM_NAME,BMI.ITEM_KIND_ID,BMI.BIG_BAR,BMI.BIG_UNIT_RATIO UNIT_RATIO,BMI.BIG_UNIT_RATIO,");
		sql.append("BMIU.BIG_UNIT_NAME,BMI.PRI4*NVL(BMI.BIG_UNIT_RATIO,1) PRI4,");
		sql.append("BMI.COST*NVL(BMI.BIG_UNIT_RATIO,1) COST,BMI.PRI1,BMI.PRI4*NVL(BMI.BIG_UNIT_RATIO,1) BIG_PRI4,");
		sql.append("BMI.STATUS,BMI.SPEC,WM.QTY_WHSE/NVL(BMI.BIG_UNIT_RATIO,1) QTY_WHSE,WM.QTY_LOCKED,");
		sql.append("WM.QTY_WHSE_WARN/NVL(BMI.BIG_UNIT_RATIO,1) QTY_WHSE_WARN,WM.OUTPUT_DATE,WM.QTY_WHSE_INIT,WM.WHSE_INIT_DATE");
		sql.append(" FROM BASE_MERCH_ITEM BMI,BASE_MERCH_ITEM_UNIT BMIU,WHSE_MERCH WM");
		sql.append(" WHERE BMI.MERCH_ID=BMIU.MERCH_ID AND BMI.ITEM_ID=BMIU.ITEM_ID AND BMI.MERCH_ID=WM.MERCH_ID AND BMI.ITEM_ID=WM.ITEM_ID AND BMI.STATUS IN (1,2)");
		
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sql, paramList, "BMIU.merch_id", "BMIU.big_bar");
		SQLUtil.initSQLOrder(sql, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		Page pageResult = this.searchPaginatedBySql(sql.toString(), 
				MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	@Override
	public List<Map<String, Object>> selectRemovedItem(Map<String, Object> itemParam) throws Exception {
		StringBuilder sql = new StringBuilder("SELECT 'MERCH' FLAG,BMI.MERCH_ID,BMI.ITEM_ID,BMI.ITEM_BAR,BMI.BIG_UNIT_NAME UNIT_NAME,BMIU.ITEM_UNIT_NAME,");
		sql.append("BMI.SHORT_CODE,BMI.SHORT_NAME,BMI.ITEM_NAME,BMI.ITEM_KIND_ID,BMI.BIG_BAR,BMI.BIG_UNIT_RATIO UNIT_RATIO,BMI.BIG_UNIT_RATIO,");
		sql.append("BMIU.BIG_UNIT_NAME,BMI.PRI4*NVL(BMI.BIG_UNIT_RATIO,1) PRI4,");
		sql.append("BMI.COST*NVL(BMI.BIG_UNIT_RATIO,1) COST,BMI.PRI1,BMI.PRI4*NVL(BMI.BIG_UNIT_RATIO,1) BIG_PRI4,");
		sql.append("BMI.STATUS,BMI.SPEC,WM.QTY_WHSE/NVL(BMI.BIG_UNIT_RATIO,1) QTY_WHSE,WM.QTY_LOCKED,");
		sql.append("WM.QTY_WHSE_WARN/NVL(BMI.BIG_UNIT_RATIO,1) QTY_WHSE_WARN,WM.OUTPUT_DATE,WM.QTY_WHSE_INIT,WM.WHSE_INIT_DATE");
		sql.append(" FROM BASE_MERCH_ITEM BMI,BASE_MERCH_ITEM_UNIT BMIU,WHSE_MERCH WM");
		sql.append(" WHERE BMI.MERCH_ID=BMIU.MERCH_ID AND BMI.ITEM_ID=BMIU.ITEM_ID AND BMI.MERCH_ID=WM.MERCH_ID AND BMI.ITEM_ID=WM.ITEM_ID AND BMI.STATUS=0");
		
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sql, paramList, "BMIU.merch_id", "BMIU.big_bar");
		SQLUtil.initSQLOrder(sql, "create_date", "desc", "create_time", "desc", "item_id", "desc");
		Page pageResult = this.searchPaginatedBySql(sql.toString(), 
				MapUtil.getInt(itemParam, "page_index", 1), MapUtil.getInt(itemParam, "page_size", 20), paramList.toArray());
		itemParam.put("page_count", pageResult.getPageSum());
		itemParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
}
