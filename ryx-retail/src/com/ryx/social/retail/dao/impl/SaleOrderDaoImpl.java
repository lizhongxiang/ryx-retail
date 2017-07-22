package com.ryx.social.retail.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.ISaleOrderDao;

@Repository("saleOrderDao")
public class SaleOrderDaoImpl extends BaseDaoImpl implements ISaleOrderDao {
	private Logger logger = LoggerFactory.getLogger(SaleOrderDaoImpl.class);
	/**
	 * 查询销售单 -- 销售流水,网络订单
	 */
	private static String selectSaleOrderSql = initSelectSaleOrderSql();
	private static String initSelectSaleOrderSql() {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ORDER_ID, CONSUMER_ID, MERCH_ID, ORDER_TYPE, ORDER_DATE, ORDER_TIME, ");
		sql.append(" AMT_ORD_LOSS, AMT_ORD_PROFIT, ADJUSTED_AMOUNT, STATUS, PMT_STATUS, QTY_ORD_TOTAL, AMTYS_ORD_TOTAL, ");
		sql.append(" AMT_ORD_TOTAL, AMT_ORD_CHANGE, NOTE, PAY_TYPE  ");
		sql.append(" FROM SALE_ORDER ");
		sql.append(" WHERE 1=1 ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> selectSaleOrder(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectSaleOrder saleOrderParam: " + saleOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectSaleOrderSql);
		List<Object> paramArray = new ArrayList<Object>();
		String merchId = (String) saleOrderParam.get("merch_id");
		String roleIds =MapUtil.getString(saleOrderParam, "role_id", "1");
		if (roleIds.equals("3")) {
			String userCode = MapUtil.getString(saleOrderParam, "user_code");
			sqlBuffer.append(" AND OPERATOR = ? ");
			paramArray.add(userCode);
		}
		if(merchId!=null){
			sqlBuffer.append("  and MERCH_ID = ? ");
			paramArray.add(merchId);
		}
		String consumerId=(String) saleOrderParam.get("consumer_id");
		if(consumerId!=null){
			sqlBuffer.append("  and CONSUMER_ID= ? ");
			paramArray.add(consumerId);
		}
		String orderId = (String) saleOrderParam.get("order_id");
		if(orderId!=null) {
			sqlBuffer.append(" AND ORDER_ID = ?");
			paramArray.add(orderId);
		}
		String orderType = (String) saleOrderParam.get("order_type");
		if(orderType!=null) {
			String[] typeArray = orderType.split(",");
			if(typeArray.length>1) {
				sqlBuffer.append(" AND (");
				for(int i=0; i<typeArray.length; i++) {
					if(i!=0) {
						sqlBuffer.append(" OR");
					}
					sqlBuffer.append(" ORDER_TYPE = ?");
					paramArray.add(typeArray[i]);
				}
				sqlBuffer.append(")");
			} else {
				sqlBuffer.append(" AND ORDER_TYPE = ?");
				paramArray.add(typeArray[0]);
			}
		}
		String status = (String) saleOrderParam.get("status");
		if(status!=null) {
			sqlBuffer.append(" AND STATUS = ?");
			paramArray.add(status);
		}
		String pmtStatus = (String) saleOrderParam.get("pmt_status");
		if(pmtStatus!=null) {
			sqlBuffer.append(" AND PMT_STATUS = ?");
			paramArray.add(pmtStatus);
		}
		
		String startDate = (String) saleOrderParam.get("start_date");
		String startTime=(String) saleOrderParam.get("start_time");
		if (startDate!=null && startTime!=null) {
			sqlBuffer.append(" AND ORDER_DATE||ORDER_TIME >= ? ");
			paramArray.add(startDate+startTime);
		} else if (startDate!=null) {
			sqlBuffer.append(" AND ORDER_DATE >= ?");
			paramArray.add(startDate);
		} else if (startTime!=null) {
			sqlBuffer.append(" AND ORDER_TIME >= ?");
			paramArray.add(startTime);
		}
		
		String endDate = (String) saleOrderParam.get("end_date");
		String endTime=(String) saleOrderParam.get("end_time");
		if (endDate!=null && endTime!=null) {
			sqlBuffer.append(" AND ORDER_DATE||ORDER_TIME <= ? ");
			paramArray.add(endDate+endTime);
		} else if (endDate!=null) {
			sqlBuffer.append(" AND ORDER_DATE <= ?");
			paramArray.add(endDate);
		} else if (endTime!=null) {
			sqlBuffer.append(" AND ORDER_TIME <= ?");
			paramArray.add(endTime);
		}
		
		String orderDate = (String) saleOrderParam.get("order_date");
		String orderTime = (String) saleOrderParam.get("order_time");
		if(orderDate!=null) {
			sqlBuffer.append(" AND ORDER_DATE = ?");
			paramArray.add(orderDate);
		}
		if(orderTime!=null) {
			sqlBuffer.append(" AND ORDER_TIME = ?");
			paramArray.add(orderTime);
		}
		sqlBuffer.append(" ORDER BY ORDER_DATE DESC, ORDER_TIME DESC");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	/**
	 * 查询销售单商品行, 必须按照order_id查询
	 */
	private static String selectSaleOrderLineSql = initSelectSaleOrderLineSql();
	private static String initSelectSaleOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, LINE_NUM, ITEM_ID, ITEM_NAME, UNIT_NAME, PRI3, DISCOUNT, QTY_ORD, AMT_ORD, NOTE, ");
		sb.append("MERCH_ID,ITEM_BAR,BIG_BAR,UNIT_RATIO,BIG_UNIT_NAME,COST,BIG_PRI3,PROFIT,ADJUSTED_AMOUNT, OTHER_ADJUSTED_AMOUNT ");
		sb.append("FROM SALE_ORDER_LINE ");
		sb.append(" WHERE ORDER_ID = ? ");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectSaleOrderLine(Map<String, Object> saleOrderLineParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectSaleOrderLine saleOrderLineParam: " + saleOrderLineParam);
		StringBuilder sql = new StringBuilder(selectSaleOrderLineSql);
		List<Object> list = new ArrayList<Object>();
		String orderId = (String) saleOrderLineParam.get("order_id");
		String lineNums = MapUtil.getString(saleOrderLineParam, "line_nums");
		
		list.add(orderId);
		if (!StringUtil.isBlank(lineNums)) {
			String [] lineNumArr = lineNums.split(",");
			
			if(lineNumArr.length>1) {
				sql.append(" AND  LINE_NUM IN (? ");
				list.add(lineNumArr[0]);
				for(int i=1; i<lineNumArr.length; i++) {
					sql.append(", ?");
					list.add(lineNumArr[i]);
				}
				sql.append(")");
			} else {
				sql.append(" AND LINE_NUM = ?");
				list.add(lineNumArr[0]);
			}
			
		}
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	/**
	 * 获取销售单明细
	 */
	@Override
	public Map<String, Object> getSaleOrderDetail(Map<String, Object> saleOrderParam) throws Exception {
		//查询SALE_ORDER
		Map<String, Object> saleOrderMap = new HashMap<String, Object>();
		List<Map<String, Object>> saleOrderList = selectSaleOrder(saleOrderParam);
		if(saleOrderList!=null && !saleOrderList.isEmpty()) {
			saleOrderMap = saleOrderList.get(0);
			saleOrderParam.put("order_id", saleOrderMap.get("ORDER_ID"));
		}
		//查询SALE_ORDER_LINE
		List<Map<String, Object>> saleOrderLineList = selectSaleOrderLine(saleOrderParam);
		saleOrderMap.put("list", saleOrderLineList);
		return saleOrderMap;
	}
	/**
	 * 修改销售单的销售状态status, 支付状态pmt_status, 支付类型pay_type
	 */
	@Override
	public void updateSaleOrder(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl updateSaleOrder saleOrderParam: " + saleOrderParam);
		StringBuffer sqlBuffer = new StringBuffer("UPDATE SALE_ORDER SET ORDER_ID=ORDER_ID");
		List<Object> paramList = new ArrayList<Object>();
		if(saleOrderParam.get("status")!=null) {
			sqlBuffer.append(", STATUS = ?");
			paramList.add(saleOrderParam.get("status"));
		}
		if(saleOrderParam.get("pmt_status")!=null) {
			sqlBuffer.append(", PMT_STATUS = ?");
			paramList.add(saleOrderParam.get("pmt_status"));
		}
		if(saleOrderParam.get("pay_type")!=null) {
			sqlBuffer.append(", PAY_TYPE = ?");
			paramList.add(saleOrderParam.get("pay_type"));
		}
		if(saleOrderParam.get("certificate_id") != null){
			sqlBuffer.append(", CERTIFICATE_ID = ?");
			paramList.add(saleOrderParam.get("certificate_id"));
		}
		if(saleOrderParam.get("trade_info") != null){
			sqlBuffer.append(", TRADE_INFO = ?");
			paramList.add(saleOrderParam.get("trade_info"));
		} 
		if(saleOrderParam.get("actual_input") != null){
			sqlBuffer.append(", TRADE_INFO = ?");
			paramList.add(saleOrderParam.get("trade_info"));
		} 
		
		if(saleOrderParam.get("amt_ord_total") != null){
			sqlBuffer.append(", AMT_ORD_TOTAL = ?");
			paramList.add(saleOrderParam.get("amt_ord_total"));
		} 
		if(saleOrderParam.get("amtys_ord_total") != null){
			sqlBuffer.append(", AMTYS_ORD_TOTAL = ?");
			paramList.add(saleOrderParam.get("amtys_ord_total"));
		} 
		if(saleOrderParam.get("amt_ord_change") != null){
			sqlBuffer.append(", AMT_ORD_CHANGE = ?");
			paramList.add(saleOrderParam.get("amt_ord_change"));
		} 
		if (saleOrderParam.containsKey("ip")) {
			sqlBuffer.append(", IP = ? ");
			paramList.add(saleOrderParam.get("ip"));
		}
		
		sqlBuffer.append(" WHERE ORDER_ID = ?");
		paramList.add(saleOrderParam.get("order_id"));
		this.executeSQL(sqlBuffer.toString(), paramList.toArray());
	}
	/**
	 * 插入销售单
	 */
	public static String insertSaleOrderSql = initInsertSaleOrderSql();
	private static String initInsertSaleOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO SALE_ORDER (ORDER_ID, CONSUMER_ID, MERCH_ID, ORDER_TYPE, PAY_TYPE, ORDER_DATE, ORDER_TIME,");
		sb.append(" STATUS, PMT_STATUS, QTY_ORD_TOTAL, AMTYS_ORD_TOTAL, AMT_ORD_TOTAL, NOTE, AMT_ORD_CHANGE,");
		sb.append(" AMT_ORD_LOSS, AMT_ORD_PROFIT, ADJUSTED_AMOUNT, QTY_ORD_COUNT, OPERATOR, CONSUMER_REBATE_AMOUNT, IP )");
		sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
		return sb.toString();
	}
	@Override
	public void insertSaleOrder(Map<String, Object> orderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl insertSaleOrder orderParam: " + orderParam);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(MapUtil.getString(orderParam, "order_id"));
		paramList.add(MapUtil.getString(orderParam, "consumer_id"));
		paramList.add(MapUtil.getString(orderParam, "merch_id"));
		paramList.add(MapUtil.getString(orderParam, "order_type"));
		paramList.add(MapUtil.getString(orderParam, "pay_type"));
		paramList.add(MapUtil.getString(orderParam, "order_date"));
		paramList.add(MapUtil.getString(orderParam, "order_time"));
		paramList.add(MapUtil.getString(orderParam, "status"));
		paramList.add(MapUtil.getString(orderParam, "pmt_status"));
		paramList.add(MapUtil.getDouble(orderParam, "qty_ord_total"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "amtys_ord_total"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "amt_ord_total"));
		paramList.add(MapUtil.getString(orderParam, "note"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "change_amount"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "amt_ord_loss"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "amt_ord_profit"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "total_rebate_amount"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "qty_ord_count"));
		paramList.add(MapUtil.getString(orderParam, "operator"));
		paramList.add(MapUtil.getBigDecimal(orderParam, "consumer_rebate_amount", BigDecimal.ZERO));
		paramList.add(MapUtil.getString(orderParam, "ip", ""));
//		paramList.add(MapUtil.getBigDecimal(orderParam, "actual_amtys_ord_total"));
		this.executeSQL(insertSaleOrderSql, paramList.toArray());
	}
	/**
	 * 插入销售单行
	 */
	public static String insertSaleOrderLineSql = initInsertSaleOrderLineSql();
	private static String initInsertSaleOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO SALE_ORDER_LINE (ORDER_ID, LINE_NUM, ITEM_ID, ITEM_NAME, UNIT_NAME, PRI3, DISCOUNT,");
		sb.append(" QTY_ORD, AMT_ORD, NOTE,PROFIT, ADJUSTED_AMOUNT, MERCH_ID, ITEM_BAR, BIG_BAR, BIG_UNIT_NAME, UNIT_RATIO, BIG_PRI3, COST, OTHER_ADJUSTED_AMOUNT)");
		sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		return sb.toString();
	}
	@Override
	public void insertSaleOrderLine(Map<String, Object> lineParam) throws Exception {
		logger.debug("SaleOrderDaoImpl insertSaleOrderLine lineParam: " + lineParam);
		String orderId = (String) lineParam.get("order_id");
		List<Map<String, Object>> saleOrderLineList = (List<Map<String, Object>>) lineParam.get("list");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> saleOrderLine : saleOrderLineList) {
			Object[] objectArray = new Object[20];
			Object note = saleOrderLine.get("note");
			objectArray[0] = orderId;
			objectArray[1] = MapUtil.getString(saleOrderLine, "line_num", "1");
			objectArray[2] = MapUtil.getString(saleOrderLine, "item_id");
			objectArray[3] = MapUtil.getString(saleOrderLine, "item_name");
			objectArray[4] = MapUtil.getString(saleOrderLine, "item_unit_name");
			objectArray[5] = MapUtil.getBigDecimal(saleOrderLine, "pri4"); // 原来用pri, 现在pri只显示用, 存用pri4
			objectArray[6] = MapUtil.getString(saleOrderLine, "discount", "100");
			objectArray[7] = MapUtil.getDouble(saleOrderLine, "sale_quantity", 1);
			objectArray[8] = MapUtil.getBigDecimal(saleOrderLine, "sale_amount");
			if(note!=null) {
				objectArray[9] = note;
			} else {
				objectArray[9] = "";
			}
			objectArray[10] = MapUtil.getBigDecimal(saleOrderLine, "profit");
			objectArray[11] = MapUtil.getBigDecimal(saleOrderLine, "line_rebate_amount");
			objectArray[12] = MapUtil.getString(saleOrderLine, "merch_id");
			objectArray[13] = MapUtil.getString(saleOrderLine, "item_bar");
			objectArray[14] = MapUtil.getString(saleOrderLine, "big_bar");
			objectArray[15] = MapUtil.getString(saleOrderLine, "big_unit_name");
			objectArray[16] = MapUtil.getString(saleOrderLine, "unit_ratio", "1");
			objectArray[17]= MapUtil.getBigDecimal(saleOrderLine, "big_pri4");
			objectArray[18]= MapUtil.getBigDecimal(saleOrderLine, "cost");
			objectArray[19]=MapUtil.getBigDecimal(saleOrderLine, "other_adjusted_amount");
			paramArray.add(objectArray);
		}
		this.executeBatchSQL(insertSaleOrderLineSql, paramArray);
	}
	
	/**
	 * 批量插入销售单
	 */
	@Override
	public void insertBatchSaleOrder(List<Map<String, Object>> orderParam)throws Exception {
		logger.debug("SaleOrderDaoImpl insertBatchSaleOrder orderParam: " + orderParam);
		List<Object> paramList = new LinkedList<Object>();//值集合
		for (Map<String, Object> saleOrderMap : orderParam) {
			Object[] objectArray = new Object[19];
			objectArray[0] = MapUtil.getString(saleOrderMap, "order_id");
			objectArray[1] = MapUtil.getString(saleOrderMap, "consumer_id");
			objectArray[2] = MapUtil.getString(saleOrderMap, "merch_id");
			objectArray[3] = MapUtil.getString(saleOrderMap, "order_type");
			objectArray[4] = MapUtil.getString(saleOrderMap, "pay_type");
			objectArray[5] = MapUtil.getString(saleOrderMap, "order_date");
			objectArray[6] = MapUtil.getString(saleOrderMap, "order_time");
			objectArray[7] = MapUtil.getString(saleOrderMap, "status");
			objectArray[8] = MapUtil.getString(saleOrderMap, "pmt_status");
			objectArray[9] = MapUtil.getString(saleOrderMap, "qty_ord_total");
			objectArray[10] = MapUtil.getString(saleOrderMap, "amtys_ord_total");
			objectArray[11] = MapUtil.getString(saleOrderMap, "amt_ord_total");
			objectArray[12] = MapUtil.getString(saleOrderMap, "note");
			objectArray[13] = MapUtil.getString(saleOrderMap, "amt_ord_change");
			objectArray[14] = MapUtil.getString(saleOrderMap, "amt_ord_loss");
			objectArray[15] = MapUtil.getString(saleOrderMap, "amt_ord_profit");
			objectArray[16] = MapUtil.getString(saleOrderMap, "adjusted_amount");
			objectArray[17] = MapUtil.getString(saleOrderMap, "qty_ord_count");
			objectArray[18] = MapUtil.getString(saleOrderMap, "operator");
			objectArray[18] = MapUtil.getString(saleOrderMap, "ip", "");
			paramList.add(objectArray);
		}
		this.executeBatchSQL(insertSaleOrderSql, paramList);
	}
	
	/**
	 * 批量插入销售单行
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public void insertBatchSaleOrderLine(List<Map<String, Object>> lineParam)throws Exception {
		logger.debug("SaleOrderDaoImpl insertBatchSaleOrderLine lineParam: " + lineParam);
		List<Object> paramList = new LinkedList<Object>();//值集合
		
		for (Map<String, Object> saleOrderLineMap : lineParam) {
			String orderId = (String) saleOrderLineMap.get("order_id");
			List<Map<String, Object>> saleOrderLineList = (List<Map<String, Object>>) saleOrderLineMap.get("list");//获取list中的数据
			for (Map<String, Object> map : saleOrderLineList) {
				Object[] objectArray = new Object[20];
				Object note = map.get("note");
				objectArray[0] = orderId;
				objectArray[1] = map.get("line_label");
				objectArray[2] = map.get("item_id");
				objectArray[3] = map.get("item_name");
				objectArray[4] = map.get("unit_name");
				objectArray[5] = map.get("pri4"); // 原来用pri, 现在pri只显示用, 存用pri4
				objectArray[6] = map.get("discount");
				objectArray[7] = map.get("qty_ord");
				objectArray[8] = map.get("amt_ord");
				if(note!=null) {
					objectArray[9] = note;
				} else {
					objectArray[9] = "";
				}
				objectArray[10] = map.get("profit");
				objectArray[11] = map.get("adjusted_amount");
				objectArray[12]= map.get("merch_id");
				objectArray[13]= map.get("item_bar");
				objectArray[14]= map.get("big_bar");
				objectArray[15]= map.get("big_unit_name");
				objectArray[16]= map.get("unit_ratio");
				objectArray[17]= map.get("big_pri4");
				objectArray[18]= map.get("cost");
				objectArray[19]=MapUtil.getBigDecimal(map, "other_adjusted_amount", BigDecimal.ZERO);
				
				paramList.add(objectArray);
			}
		}
		this.executeBatchSQL(insertSaleOrderLineSql, paramList);
	}
	
	/**
	 * 获取某日每种order_type对应的销售额和销售笔数
	 */
	private static final String selectSaleInfoWithOrderTypeSql = initSelectSaleInfoWithOrderTypeSql();
	private static String initSelectSaleInfoWithOrderTypeSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_TYPE, NVL(SUM(AMTYS_ORD_TOTAL),0) amt_total, COUNT(*) order_count");
		sb.append("	FROM SALE_ORDER WHERE MERCH_ID = ? AND ORDER_DATE = ? GROUP BY ORDER_TYPE");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> getSaleInfoWithOrderType(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl getSaleInfoWithOrderType saleOrderParam:" + saleOrderParam);
		String merchId = (String) saleOrderParam.get("merch_id");
		String orderDate = (String) saleOrderParam.get("order_date");
		return this.selectBySqlQuery(selectSaleInfoWithOrderTypeSql, new Object[] {merchId, orderDate});
	}
	/**
	 * 销售额各商品分类占比
	 */
	private static final String selectSaleInfoWithKindIdSql = initSelectSaleInfoWithKindIdSql();
	private static String initSelectSaleInfoWithKindIdSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT item.ITEM_KIND_ID, NVL(SUM(line.AMT_ORD),0) KIND_AMOUNT");
		sb.append(" FROM SALE_ORDER ord, SALE_ORDER_LINE line, BASE_MERCH_ITEM item");
		sb.append(" WHERE ord.ORDER_ID = line.ORDER_ID AND line.ITEM_ID = item.ITEM_ID AND ord.MERCH_ID = ? AND ord.ORDER_DATE BETWEEN ? AND ?");
		sb.append(" GROUP BY item.ITEM_KIND_ID");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> getSaleInfoWithKindId(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl getSaleInfoWithKindId saleOrderParam:" + saleOrderParam);
		String merchId = (String) saleOrderParam.get("merch_id");
		String startDate = (String) saleOrderParam.get("start_date");
		String endDate = (String) saleOrderParam.get("end_date");
		return this.selectBySqlQuery(selectSaleInfoWithKindIdSql, new Object[] {merchId, startDate, endDate});
	}
	/**
	 * 销售量top10
	 */
	private static final String selectSaleInfoWithRankSql = initSelectSaleInfoWithRankSql();
	private static String initSelectSaleInfoWithRankSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT item.ITEM_ID, NVL(SUM(line.QTY_ORD),0) ITEM_QTY FROM SALE_ORDER ord, SALE_ORDER_LINE line, BASE_MERCH_ITEM item");
		sb.append(" WHERE ord.ORDER_ID = line.ORDER_ID AND line.ITEM_ID = item.ITEM_ID AND ord.MERCH_ID = ? AND ord.ORDER_DATE BETWEEN ? AND ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> getSaleInfoWithRank(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl getSaleInfoWithRank saleOrderParam:" + saleOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectSaleInfoWithRankSql);
		List<Object> paramArray = new ArrayList<Object>();
		String merchId = (String) saleOrderParam.get("merch_id");
		paramArray.add(merchId);
		String startDate = (String) saleOrderParam.get("start_date");
		paramArray.add(startDate);
		String endDate = (String) saleOrderParam.get("end_date");
		paramArray.add(endDate);
		String itemKindId = (String) saleOrderParam.get("item_kind_id");
		if(itemKindId!=null) {
			String[] typeArray = itemKindId.split(",");
			if(typeArray.length>1) {
				sqlBuffer.append(" AND (");
				for(int i=0; i<typeArray.length; i++) {
					if(i!=0) {
						sqlBuffer.append(" OR");
					}
					sqlBuffer.append(" item.ITEM_KIND_ID = ?");
					paramArray.add(typeArray[i]);
				}
				sqlBuffer.append(")");
			} else {
				sqlBuffer.append(" AND item.ITEM_KIND_ID = ?");
				paramArray.add(typeArray[0]);
			}
		}
		sqlBuffer.append(" GROUP BY item.ITEM_ID");
		sqlBuffer.append(" ORDER BY NVL(SUM(line.QTY_ORD),0) DESC, item.ITEM_ID ASC");
		sqlBuffer.insert(0, "SELECT t.ITEM_ID, t.ITEM_QTY FROM (");
		sqlBuffer.append(" ) t WHERE ROWNUM <= ?");
		int number = (Integer) saleOrderParam.get("number");
		paramArray.add(number);
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	/**
	 * 查询最近几笔销售单的销售金额
	 */
	private static final String selectSaleInfoWithLastOrdersSql = initSelectSaleInfoWithLastOrdersSql();
	private static String initSelectSaleInfoWithLastOrdersSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT t.ORDER_ID, t.AMTYS_ORD_TOTAL FROM (");
		sb.append(" SELECT ORDER_ID, AMTYS_ORD_TOTAL FROM SALE_ORDER WHERE MERCH_ID = ? ORDER BY ORDER_DATE DESC, ORDER_TIME DESC");
		sb.append(" ) t WHERE ROWNUM <= ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> getSaleInfoWithLastOrders(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl getSaleInfoWithLastOrders saleOrderParam:" + saleOrderParam);
		String merchId = (String) saleOrderParam.get("merch_id");
		int number = (Integer) saleOrderParam.get("number");
		return this.selectBySqlQuery(selectSaleInfoWithLastOrdersSql, new Object[] {merchId, number});
	}
	
	public static final String selectSaleQuantityDailySql = initSelectSaleQuantityDailySql();
	private static String initSelectSaleQuantityDailySql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_DATE, NVL(SUM(QTY_ORD_TOTAL),0) sale_quantity FROM SALE_ORDER");
		sb.append(" WHERE MERCH_ID = ? AND ORDER_DATE BETWEEN ? AND ? GROUP BY ORDER_DATE ORDER BY ORDER_DATE");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectSaleQuantityDaily(Map<String, Object> saleQuantityDailyParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectSaleQuantityDaily saleQuantityDailyParam:" + saleQuantityDailyParam);
		StringBuffer sqlBuffer = new StringBuffer(selectSaleQuantityDailySql);
		String merchId = (String) saleQuantityDailyParam.get("merch_id");
		String startDate = (String) saleQuantityDailyParam.get("start_date");
		String endDate = (String) saleQuantityDailyParam.get("end_date");
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(merchId);
		paramArray.add(startDate);
		paramArray.add(endDate);
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	
	public static final String selectSaleQuantityMonthlySql = initSelectSaleQuantityMonthlySql();
	private static String initSelectSaleQuantityMonthlySql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SUBSTR(ORDER_DATE, 1, 6) order_month, NVL(SUM(QTY_ORD_TOTAL),0) sale_quantity FROM SALE_ORDER");
		sb.append(" WHERE MERCH_ID = ? AND ORDER_DATE BETWEEN ? AND ? GROUP BY SUBSTR(ORDER_DATE, 1, 6) ORDER BY SUBSTR(ORDER_DATE, 1, 6)");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectSaleQuantityMonthly(Map<String, Object> saleQuantityMonthlyParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectSaleQuantityMonthly saleQuantityMonthlyParam:" + saleQuantityMonthlyParam);
		StringBuffer sqlBuffer = new StringBuffer(selectSaleQuantityMonthlySql);
		String merchId = (String) saleQuantityMonthlyParam.get("merch_id");
		String startMonth = (String) saleQuantityMonthlyParam.get("start_month");
		String endMonth = (String) saleQuantityMonthlyParam.get("end_month");
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(merchId);
		paramArray.add(startMonth+"01");
		paramArray.add(endMonth+"31");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	
	public static final String selectGrossMarginDailySql = initSelectGrossMarginDailySql();
	private static String initSelectGrossMarginDailySql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_DATE, NVL(SUM(line.AMT_ORD) - SUM(item.PRI1 * line.QTY_ORD),0) gross_margin");
		sb.append(" FROM SALE_ORDER sale, SALE_ORDER_LINE line, BASE_MERCH_ITEM item");
		sb.append(" WHERE sale.ORDER_ID = line.ORDER_ID AND line.ITEM_ID = item.ITEM_ID AND sale.MERCH_ID = item.MERCH_ID");
		sb.append(" AND sale.MERCH_ID = ? AND ORDER_DATE BETWEEN ? AND ? GROUP BY ORDER_DATE ORDER BY ORDER_DATE");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectGrossMarginDaily(Map<String, Object> grossMarginDailyParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectGrossMarginDaily grossMarginDailyParam:" + grossMarginDailyParam);
		StringBuffer sqlBuffer = new StringBuffer(selectGrossMarginDailySql);
		String merchId = (String) grossMarginDailyParam.get("merch_id");
		String startDate = (String) grossMarginDailyParam.get("start_date");
		String endDate = (String) grossMarginDailyParam.get("end_date");
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(merchId);
		paramArray.add(startDate);
		paramArray.add(endDate);
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	
	public static final String selectGrossMarginMonthlySql = initSelectGrossMarginMonthlySql();
	private static String initSelectGrossMarginMonthlySql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SUBSTR(ORDER_DATE, 1, 6) order_month, NVL(SUM(line.AMT_ORD) - SUM(item.PRI1 * line.QTY_ORD),0) gross_margin");
		sb.append(" FROM SALE_ORDER sale, SALE_ORDER_LINE line, BASE_MERCH_ITEM item");
		sb.append(" WHERE sale.ORDER_ID = line.ORDER_ID AND line.ITEM_ID = item.ITEM_ID AND sale.MERCH_ID = item.MERCH_ID");
		sb.append(" AND sale.MERCH_ID = ? AND ORDER_DATE BETWEEN ? AND ? GROUP BY SUBSTR(ORDER_DATE, 1, 6) ORDER BY SUBSTR(ORDER_DATE, 1, 6)");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectGrossMarginMonthly(Map<String, Object> grossMarginMonthlyParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectGrossMarginMonthly grossMarginMonthlyParam:" + grossMarginMonthlyParam);
		StringBuffer sqlBuffer = new StringBuffer(selectGrossMarginMonthlySql);
		String merchId = (String) grossMarginMonthlyParam.get("merch_id");
		String startMonth = (String) grossMarginMonthlyParam.get("start_month");
		String endMonth = (String) grossMarginMonthlyParam.get("end_month");
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(merchId);
		paramArray.add(startMonth+"01");
		paramArray.add(endMonth+"31");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
}
