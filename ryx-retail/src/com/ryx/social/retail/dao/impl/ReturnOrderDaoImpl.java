package com.ryx.social.retail.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IReturnOrderDao;
import com.ryx.social.retail.util.SQLUtil;

@Repository
public class ReturnOrderDaoImpl extends BaseDaoImpl implements IReturnOrderDao {
	private Logger logger = LoggerFactory.getLogger(ReturnOrderDaoImpl.class);
	/**
	 * 插入退货单
	 */
	private static final String insertReturnOrderSql = initInsertReturnOrderSql();
	private static String initInsertReturnOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO RETURN_ORDER ");
		sb.append(" (RETURN_ORDER_ID, ORDER_ID, RETURN_ORDER_DATE, RETURN_ORDER_TIME, RETURN_STATUS, ");
		sb.append(" QTY_RETURN_TOTAL, AMT_RETURN_TOTAL, OPERATOR, ");
		sb.append(" CONSUMER_ID, ORDER_TYPE, RETURN_PMT_STATUS, QTY_RETURN_COUNT, NOTE,MERCH_ID, amt_return_loss ");
		sb.append("  ) ");
		sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		return sb.toString();
	}
	@Override
	public void insertReturnOrder(Map<String, Object> returnOrderParam) throws Exception {
		String returnOrderId = MapUtil.getString(returnOrderParam, "return_order_id");
		String orderId = MapUtil.getString(returnOrderParam, "order_id");
		String returnOrderDate = MapUtil.getString(returnOrderParam, "return_order_date");
		String returnOrderTime = MapUtil.getString(returnOrderParam, "return_order_time");
		String status = MapUtil.getString(returnOrderParam, "status");
		BigDecimal qtyReturnTotal = MapUtil.getBigDecimal(returnOrderParam, "qty_return_total");
		BigDecimal amtReturnTotal =  MapUtil.getBigDecimal(returnOrderParam, "amt_return_total");
		String operator = MapUtil.getString(returnOrderParam, "operator");
		String consumer_id = MapUtil.getString(returnOrderParam, "consumer_id");
		String order_type = MapUtil.getString(returnOrderParam, "order_type");
		String return_pmt_status = MapUtil.getString(returnOrderParam, "return_pmt_status");
		BigDecimal qty_return_count = MapUtil.getBigDecimal(returnOrderParam, "qty_return_count");
		String note = MapUtil.getString(returnOrderParam, "note");
		String merch_id = MapUtil.getString(returnOrderParam, "merch_id");
		BigDecimal amt_return_loss = MapUtil.getBigDecimal(returnOrderParam, "amt_return_loss");//总利润
		List<Object> param=new ArrayList<Object>();
		param.add( returnOrderId );
		param.add( orderId );
		param.add(  returnOrderDate);
		param.add(  returnOrderTime);
		param.add( status );
		param.add(  qtyReturnTotal+"");
		param.add(  amtReturnTotal+"");
		param.add(  operator);
		param.add(  consumer_id);
		param.add(  order_type);
		param.add(  return_pmt_status);
		param.add(  qty_return_count);
		param.add(  note); 
		param.add( merch_id);
		param.add(amt_return_loss);
		this.executeSQL(insertReturnOrderSql, param.toArray());
	}
	/**
	 * 插入退货单行表 -- 退货单行表复用销售单行表
	 */
	private static final String insertReturnOrderLineSql = initInsertReturnOrderLineSql();
	private static String initInsertReturnOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO RETURN_ORDER_LINE ");
		sb.append("( ");
		sb.append("ORDER_ID, LINE_NUM, ITEM_ID, QTY_ORD, AMT_ORD, NOTE, ITEM_NAME, PROFIT, ");
		sb.append("MERCH_ID, BIG_BAR, BIG_UNIT_NAME, UNIT_RATIO, BIG_PRI3, ADJUSTED_AMOUNT, ");
		sb.append("IS_RESALABLE, SALE_ORDER_ID, UNSALE_AMOUNT, SALE_LINE_NUM  ");
		sb.append(" ) ");
		sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	@Override
	public void insertReturnOrderLine(Map<String, Object> returnOrderParam) throws Exception {
		String returnOrderId = (String) returnOrderParam.get("return_order_id");
		String merch_id=(String) returnOrderParam.get("merch_id");
		List<Map<String, Object>> returnOrderLines = MapUtil.get(returnOrderParam, "list", new ArrayList<Map<String, Object>>());
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> itemMap : returnOrderLines) {
			Object[] objectArray = new Object[18];
			objectArray[0] = returnOrderId;
			objectArray[1] = MapUtil.getInt(itemMap, "line_num", 1);
			objectArray[2] = MapUtil.getString(itemMap, "item_id");
			objectArray[3] = MapUtil.getBigDecimal(itemMap, "qty_ord");
			objectArray[4] = MapUtil.getBigDecimal(itemMap, "return_money");
			objectArray[5] = MapUtil.getString(itemMap, "note", "");
			objectArray[6] = MapUtil.getString(itemMap, "item_name");
			objectArray[7] = MapUtil.getBigDecimal(itemMap, "profit");
			objectArray[8] = merch_id;
			objectArray[9] = MapUtil.getString(itemMap, "big_bar");
			objectArray[10] = MapUtil.getString(itemMap, "big_unit_name");
			objectArray[11] = MapUtil.getString(itemMap, "unit_ratio");
			objectArray[12] = MapUtil.getBigDecimal(itemMap, "big_pri3");
			objectArray[13] = MapUtil.getBigDecimal(itemMap, "adjusted_amount");
			
			objectArray[14] = MapUtil.getBigDecimal(itemMap, "is_resalable");//是否再次销售，（是否入库，0：不入库，1：入库
			objectArray[15] = MapUtil.getBigDecimal(returnOrderParam, "order_id");//销售单号
			objectArray[16] = MapUtil.getBigDecimal(itemMap, "unsale_amount");//退货造成的销售额损失=sale_amount/sale_quantity*refund_quantity
			objectArray[17] = MapUtil.getBigDecimal(itemMap, "sale_line_num");//销售单行
			
			paramArray.add(objectArray);
		}
		this.executeBatchSQL(insertReturnOrderLineSql, paramArray);
	}
	
	/**
	 * 某日退货总额
	 */
	private static final String selectAmtReturnTotalSql = initSelectAmtReturnTotalSql();
	private static String initSelectAmtReturnTotalSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NVL(SUM(AMT_RETURN_TOTAL),0) return_total FROM RETURN_ORDER return, SALE_ORDER sale");
		sb.append(" WHERE return.ORDER_ID = sale.ORDER_ID AND sale.MERCH_ID = ? AND return.RETURN_ORDER_DATE = ?");
		return sb.toString();
	}
	@Override
	public BigDecimal getAmtReturnTotal(Map<String, Object> returnOrderParam) throws Exception {
		logger.debug("ReturnOrderDaoImpl getAmtReturnTotal returnOrderParam:" + returnOrderParam);
		String merchId = (String) returnOrderParam.get("merch_id");
		String returnOrderDate = (String) returnOrderParam.get("return_order_date");
		List<Map<String, Object>> amtReturnTotalResult = this.selectBySqlQuery(selectAmtReturnTotalSql, new Object[] {merchId, returnOrderDate});
		if(amtReturnTotalResult.size()==1) {
			return (BigDecimal) amtReturnTotalResult.get(0).get("return_total");
		} else {
			return BigDecimal.ZERO;
		}
	}
	/**
	 * 查询退货单明细
	 */
	private static final String selectReturnOrderSql = initSelectReturnOrderSql();
	private static String initSelectReturnOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT return.RETURN_ORDER_ID, return.ORDER_ID, RETURN_ORDER_DATE, RETURN_ORDER_TIME,");
		sb.append(" return.STATUS return_status, QTY_RETURN_TOTAL, AMT_RETURN_TOTAL, OPERATOR, sale.CONSUMER_ID,");
		sb.append(" MERCH_ID, ORDER_TYPE, ORDER_DATE, ORDER_TIME, sale.STATUS sale_status, PMT_STATUS,");
		sb.append(" QTY_ORD_TOTAL, AMTYS_ORD_TOTAL, AMT_ORD_TOTAL, NOTE FROM RETURN_ORDER return, SALE_ORDER sale");
		sb.append(" WHERE return.ORDER_ID = sale.ORDER_ID AND sale.MERCH_ID = ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectReturnOrder(Map<String, Object> returnOrderParam) throws Exception {
		StringBuffer sqlBuffer = new StringBuffer(selectReturnOrderSql);
		List<Object> paramArray = new ArrayList<Object>();
		String merchId = (String) returnOrderParam.get("merch_id");
		paramArray.add(merchId);
		String orderId = (String) returnOrderParam.get("order_id");
		if(orderId!=null) {
			sqlBuffer.append(" AND sale.ORDER_ID = ?");
			paramArray.add(orderId);
		}
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	//退货单
	public static final String selectMerchReturnOrderSql = initSelectMerchReturnOrderSql();
	private static String initSelectMerchReturnOrderSql(){
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT RETURN_ORDER_ID, ORDER_ID, RETURN_ORDER_DATE, RETURN_ORDER_TIME, RETURN_STATUS, QTY_RETURN_TOTAL,");
		sql.append(" AMT_RETURN_TOTAL, OPERATOR, CONSUMER_ID, ORDER_TYPE, RETURN_PMT_STATUS, NOTE, MERCH_ID, AMT_RETURN_LOSS");
		sql.append(" FROM RETURN_ORDER WHERE 1=1");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> selectMerchReturnOrder(Map<String, Object> orderParam) throws Exception {
		logger.debug("ReturnOrderDaoImpl selectMerchReturnOrder orderParam: " + orderParam);
		StringBuilder sqlBuilder = new StringBuilder(selectMerchReturnOrderSql);
		List<Object> paramList = new ArrayList<Object>();
		
//		if(orderParam.get("merch_id")!=null) {
//			sqlBuffer.append(" AND MERCH_ID=?");
//			paramList.add(orderParam.get("merch_id"));
//		}
		SQLUtil.initSQLEqual(orderParam, sqlBuilder , paramList, "merch_id");
//		if(orderParam.get("return_order_id")!=null) {
//			sqlBuffer.append(" AND RETURN_ORDER_ID=?");
//			paramList.add(orderParam.get("return_order_id"));
//		}
		SQLUtil.initSQLIn(orderParam, sqlBuilder , paramList, "return_order_id");
		String roleIds =MapUtil.getString(orderParam, "role_id", "1");
		String startDate = MapUtil.get(orderParam, "start_date", null);
		String endDate = MapUtil.get(orderParam, "end_date", null);
		String startTime = MapUtil.get(orderParam, "start_time", null);
		String endTime = MapUtil.get(orderParam, "end_time", null);
		
		//RETURN_ORDER_DATE,RETURN_ORDER_TIME
		if (startDate!=null && startTime!=null) {
			sqlBuilder.append(" AND RETURN_ORDER_DATE||RETURN_ORDER_TIME >= ? ");
			paramList.add(startDate+startTime);
		} else if (startDate!=null) {
			sqlBuilder.append(" AND RETURN_ORDER_DATE >=? ");
			paramList.add(startDate);
		} else if (startTime!=null) {
			sqlBuilder.append(" AND RETURN_ORDER_TIME >=? ");
			paramList.add(startTime);
		}
		if (roleIds.equals("3")) {
			String userCode = MapUtil.getString(orderParam, "user_code");
			sqlBuilder.append(" AND OPERATOR = ? ");
			paramList.add(userCode);
		}
		if (endDate!=null && endTime!=null) {
			sqlBuilder.append(" AND RETURN_ORDER_DATE||RETURN_ORDER_TIME <= ? ");
			paramList.add(endDate+endTime);
		} else if (endDate!=null) {
			sqlBuilder.append(" AND RETURN_ORDER_DATE <=? ");
			paramList.add(endDate);
		} else if (endTime!=null) {
			sqlBuilder.append(" AND RETURN_ORDER_TIME <=? ");
			paramList.add(endTime);
		}
		
//		SQLUtil.initSQLBetweenAnd(orderParam, sqlBuilder, paramList, "return_order_date", true, true, "start_date",  "end_date" );
//		SQLUtil.initSQLBetweenAnd(orderParam, sqlBuilder, paramList, "return_order_time", true, true, "start_time", "end_time" );
//		if(orderParam.get("order_id")!=null) {
//			String[] orderIdArray = orderParam.get("order_id").toString().split(",");
//			if(orderIdArray.length>0) {
//				if(orderIdArray.length==1) {
//					sqlBuffer.append(" AND ORDER_ID=?");
//					paramList.add(orderIdArray[0]);
//				} else {
//					sqlBuffer.append(" AND ORDER_ID IN (");
//					int index = 0;
//					for(String orderId : orderIdArray) {
//						if(index++==0) sqlBuffer.append("?");
//						else sqlBuffer.append(", ?");
//						paramList.add(orderId);
//					}
//					sqlBuffer.append(")");
//				}
//			}
//		}
		SQLUtil.initSQLIn(orderParam, sqlBuilder , paramList, "order_id");
		
		SQLUtil.initSQLEqual(orderParam, sqlBuilder, paramList, "return_order_date" , "return_status" , "order_type"  );
		
//		Integer pageIndex = 1;
//		if(orderParam.get("page_index")!=null && !"".equals(orderParam.get("page_index"))) {
//			pageIndex = Integer.valueOf(orderParam.get("page_index").toString());
//		}
		Integer pageIndex=MapUtil.getInt(orderParam,"page_index",1 );
//		Integer pageSize = 20;
//		if(orderParam.get("page_size")!=null && !"".equals(orderParam.get("page_size"))) {
//			pageSize = Integer.valueOf(orderParam.get("page_size").toString());
//		}
		Integer pageSize = MapUtil.getInt(orderParam, "page_size",20);
//		sqlBuilder .append(" ORDER BY RETURN_ORDER_DATE DESC, RETURN_ORDER_TIME DESC");
		SQLUtil.initSQLOrder(sqlBuilder, "RETURN_ORDER_DATE", "2", "RETURN_ORDER_TIME","2");
		logger.debug("return Sql:"+sqlBuilder.toString());
		logger.debug("return params:"+paramList.toString());
		
		Page pageResult = this.searchPaginatedBySql(sqlBuilder.toString(), pageIndex, pageSize, paramList.toArray());
		orderParam.put("page_count", pageResult.getPageSum());
		orderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
}
