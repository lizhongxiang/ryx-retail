package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IOrderDao;
import com.ryx.social.retail.util.SQLUtil;

@Repository
public class OrderDaoImpl extends BaseDaoImpl implements IOrderDao {
	
	private Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);

	/**
	 * 查询进货单, 可以根据order_id, supplier_id, order_date, stauts, pmt_status查询, 带排序分页
	 */
	public static final String selectPurchOrderSql = initSelectPurchOrderSql();
	private static String initSelectPurchOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, MERCH_ID, ORDER_DATE, ORDER_TIME, STATUS, PMT_STATUS, QTY_PURCH_TOTAL, AMT_PURCH_TOTAL,");
		sb.append(" SUPPLIER_ID, VOUCHER_DATE, OPERATOR FROM PURCH_ORDER WHERE MERCH_ID = ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectPurchOrder(Map<String, Object> purchOrderParam) throws Exception {
		logger.debug("OrderDaoImpl searchPurchOrder purchOrderParam: " + purchOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectPurchOrderSql);
		Map<String, Object> orderByMap = (Map<String, Object>) purchOrderParam.get("order_by");
		
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
		orderByBuffer.append(" ORDER_ID DESC"); // 默认按照ORDER_ID倒序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 分页
		Integer pageIndex = 1;
		if(purchOrderParam.get("page_index")!=null) {
			pageIndex = Integer.parseInt(purchOrderParam.get("page_index").toString());
		}
		Integer pageSize = 20;
		if(purchOrderParam.get("page_size")!=null) {
			pageSize = Integer.parseInt(purchOrderParam.get("page_size").toString());
		}
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(purchOrderParam.get("merch_id"));
		if(purchOrderParam.containsKey("order_id")) {
			String[] orderIdArray = purchOrderParam.get("order_id").toString().split(",");
			if(orderIdArray.length==1) {
				sqlBuffer.append(" AND ORDER_ID = ?");
				paramArray.add(orderIdArray[0]);
			} else {
				sqlBuffer.append(" AND ORDER_ID IN (");
				for(int i=0; i<orderIdArray.length; i++) {
					paramArray.add(orderIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if(purchOrderParam.containsKey("supplier_id")) {
			sqlBuffer.append(" AND SUPPLIER_ID = ?");
			paramArray.add(purchOrderParam.get("supplier_id"));
		}
		if(purchOrderParam.containsKey("voucher_date")) {
			sqlBuffer.append(" AND VOUCHER_DATE = ?");
			paramArray.add(purchOrderParam.get("voucher_date"));
		}
		if(purchOrderParam.containsKey("order_date")) {
			sqlBuffer.append(" AND ORDER_DATE = ?");
			paramArray.add(purchOrderParam.get("order_date"));
		} else {
			if(purchOrderParam.containsKey("order_date_floor")) {
				sqlBuffer.append(" AND ORDER_DATE >= ?");
				paramArray.add(purchOrderParam.get("order_date_floor"));
			}
			if(purchOrderParam.containsKey("order_date_ceiling")) {
				sqlBuffer.append(" AND ORDER_DATE <= ?");
				paramArray.add(purchOrderParam.get("order_date_ceiling"));
			}
		}
		if(purchOrderParam.containsKey("order_time")) {
			sqlBuffer.append(" AND ORDER_TIME = ?");
			paramArray.add(purchOrderParam.get("order_time"));
		} else {
			if(purchOrderParam.containsKey("order_time_floor")) {
				sqlBuffer.append(" AND ORDER_TIME >= ?");
				paramArray.add(purchOrderParam.get("order_time_floor"));
			}
			if(purchOrderParam.containsKey("order_time_ceiling")) {
				sqlBuffer.append(" AND ORDER_TIME <= ?");
				paramArray.add(purchOrderParam.get("order_time_ceiling"));
			}
		}
		if(purchOrderParam.containsKey("status")) {
			String[] statusArray = purchOrderParam.get("status").toString().split(",");
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
		if(purchOrderParam.containsKey("pmt_status")) {
			String[] pmtStatusArray = purchOrderParam.get("pmt_status").toString().split(",");
			if(pmtStatusArray.length==1) {
				sqlBuffer.append(" AND PMT_STATUS = ?");
				paramArray.add(pmtStatusArray[0]);
			} else {
				sqlBuffer.append(" AND PMT_STATUS IN (");
				for(int i=0; i<pmtStatusArray.length; i++) {
					paramArray.add(pmtStatusArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), pageIndex, pageSize, paramArray.toArray());
		purchOrderParam.put("page_count", pageResult.getPageSum());
		purchOrderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	/**
	 * 查询进货单行, 按照order_id查询, 不分页, 不排序
	 */
	public static final String selectPurchOrderLineSql = initSelectPurchOrderLineSql();
	private static String initSelectPurchOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, MERCH_ID, ITEM_ID, PRI, QTY_REQ, QTY_ORD, QTY_LMT, AMT_ORD FROM PURCH_ORDER_LINE WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectPurchOrderLine(Map<String, Object> purchOrderLineParam) throws Exception {
		logger.debug("OrderDaoImpl selectPurchOrderLine purchOrderLineParam: " + purchOrderLineParam);
		StringBuffer sqlBuffer = new StringBuffer(selectPurchOrderLineSql);
		String orderId = (String) purchOrderLineParam.get("order_id");
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		if(orderId!=null) {
			String[] orderIdArray = orderId.split(",");
			if(orderIdArray.length==1) {
				sqlBuffer.append(" AND ORDER_ID = ?");
				paramArray.add(orderIdArray[0]);
			} else {
				sqlBuffer.append(" AND ORDER_ID IN (");
				for(int i=0; i<orderIdArray.length; i++) {
					paramArray.add(orderIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
			return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
		}
		// 如果不是按照order_id查询, 则返回空list
		return new ArrayList<Map<String, Object>>();
	}
	
	// 仅用来统计每日进货单品的总量和总额, 必须输入order_id, 否则为空list
	public static final String searchPurchOrderLineByItemSql = initSearchPurchOrderLineByItemSql();
	private static String initSearchPurchOrderLineByItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ITEM_ID, NVL(SUM(QTY_ORD*NVL(UNIT_RATIO,1)),0) PURCH_QUANTITY, NVL(SUM(AMT_ORD*NVL(UNIT_RATIO,1)),0) PURCH_AMOUNT");
		sb.append(" FROM PURCH_ORDER_LINE WHERE ORDER_ID");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchPurchOrderLineByItem(Map<String, Object> purchOrderLineParam) throws Exception {
		logger.debug("OrderDaoImpl searchPurchOrderLine purchOrderLineParam: " + purchOrderLineParam);
		StringBuffer sqlBuffer = new StringBuffer(searchPurchOrderLineByItemSql);
		List<Object> paramObject = new ArrayList<Object>();
		String orderId = (String) purchOrderLineParam.get("order_id");
		if(orderId==null || "".equals(orderId)) { // 如果没有传order_id, 则直接返回空字符串
			return new ArrayList<Map<String, Object>>();
		}
		String[] orderIdArray = orderId.split(",");
		if(orderIdArray.length==1) {
			sqlBuffer.append(" = ?");
			paramObject.add(orderIdArray[0]);
		} else {
			sqlBuffer.append(" IN (");
			for(int i=0; i<orderIdArray.length; i++) {
				paramObject.add(orderIdArray[i]);
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
	 * 联表查询
	 * 采购管理(PURCH_ORDER)   采购单行(PURCH_ORDER_LINE)
	 * */
	public static final String selectPurchOrderAndPurchOrderLineSql = initSelectPurchOrderAndPurchOrderLineSql();
	private static String initSelectPurchOrderAndPurchOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT po.ORDER_ID, po.MERCH_ID, po.ORDER_DATE, po.ORDER_TIME, po.STATUS, po.PMT_STATUS,");
		sb.append("po.QTY_PURCH_TOTAL, po.AMT_PURCH_TOTAL,po.SUPPLIER_ID, po.VOUCHER_DATE, po.OPERATOR, ");
		sb.append("pol.ITEM_ID, pol.PRI, pol.QTY_REQ, pol.QTY_ORD, pol.QTY_LMT, pol.AMT_ORD");
		sb.append("FROM PURCH_ORDER po,PURCH_ORDER_LINE pol WHERE MERCH_ID = ? AND po.ORDER_ID=pol.ORDER_ID");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectPurchOrderAndPurchOrderLine(
			Map<String, Object> purchOrderParam) throws Exception {
		logger.debug("OrderDaoImpl searchPurchOrder purchOrderParam: " + purchOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectPurchOrderSql);
		Map<String, Object> orderByMap = (Map<String, Object>) purchOrderParam.get("order_by");
		
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
		orderByBuffer.append(" ORDER_ID DESC"); // 默认按照ORDER_ID倒序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 分页
		Integer pageIndex = 1;
		if(purchOrderParam.get("page_index")!=null) {
			pageIndex = Integer.parseInt(purchOrderParam.get("page_index").toString());
		}
		Integer pageSize = 20;
		if(purchOrderParam.get("page_size")!=null) {
			pageSize = Integer.parseInt(purchOrderParam.get("page_size").toString());
		}
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(purchOrderParam.get("merch_id"));
		if(purchOrderParam.containsKey("order_id")) {
			String[] orderIdArray = purchOrderParam.get("order_id").toString().split(",");
			if(orderIdArray.length==1) {
				sqlBuffer.append(" AND ORDER_ID = ?");
				paramArray.add(orderIdArray[0]);
			} else {
				sqlBuffer.append(" AND ORDER_ID IN (");
				for(int i=0; i<orderIdArray.length; i++) {
					paramArray.add(orderIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if(purchOrderParam.containsKey("supplier_id")) {
			sqlBuffer.append(" AND SUPPLIER_ID = ?");
			paramArray.add(purchOrderParam.get("supplier_id"));
		}
		if(purchOrderParam.containsKey("order_date")) {
			sqlBuffer.append(" AND ORDER_DATE = ?");
			paramArray.add(purchOrderParam.get("order_date"));
		} else {
			if(purchOrderParam.containsKey("order_date_floor")) {
				sqlBuffer.append(" AND ORDER_DATE >= ?");
				paramArray.add(purchOrderParam.get("order_date_floor"));
			}
			if(purchOrderParam.containsKey("order_date_ceiling")) {
				sqlBuffer.append(" AND ORDER_DATE <= ?");
				paramArray.add(purchOrderParam.get("order_date_ceiling"));
			}
		}
		if(purchOrderParam.containsKey("order_time")) {
			sqlBuffer.append(" AND ORDER_TIME = ?");
			paramArray.add(purchOrderParam.get("order_time"));
		} else {
			if(purchOrderParam.containsKey("order_time_floor")) {
				sqlBuffer.append(" AND ORDER_TIME >= ?");
				paramArray.add(purchOrderParam.get("order_time_floor"));
			}
			if(purchOrderParam.containsKey("order_time_ceiling")) {
				sqlBuffer.append(" AND ORDER_TIME <= ?");
				paramArray.add(purchOrderParam.get("order_time_ceiling"));
			}
		}
		if(purchOrderParam.containsKey("status")) {
			String[] statusArray = purchOrderParam.get("status").toString().split(",");
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
		if(purchOrderParam.containsKey("pmt_status")) {
			String[] pmtStatusArray = purchOrderParam.get("pmt_status").toString().split(",");
			if(pmtStatusArray.length==1) {
				sqlBuffer.append(" AND PMT_STATUS = ?");
				paramArray.add(pmtStatusArray[0]);
			} else {
				sqlBuffer.append(" AND PMT_STATUS IN (");
				for(int i=0; i<pmtStatusArray.length; i++) {
					paramArray.add(pmtStatusArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), pageIndex, pageSize, paramArray.toArray());
		purchOrderParam.put("page_count", pageResult.getPageSum());
		purchOrderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	/**
	 * 联表查询
	 * 
	 * 销售单行(SALE_ORDER_LINE) 
	 * 退货单(RETURN_ORDER)
	 * */
	public static final String selectSaleOrderLineAndReturnOrderSql = initSaleOrderLineAndReturnOrderSql();
	private static String initSaleOrderLineAndReturnOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ro.RETURN_ORDER_ID, ro.ORDER_ID, ro.RETURN_ORDER_DATE, ro.RETURN_ORDER_TIME, ro.QTY_RETURN_TOTAL,");
		sb.append("ro.AMT_RETURN_TOTAL, ro.AMT_RETURN_LOSS,ro.RETURN_STATUS, ro.OPERATOR,ORDER_TYPE ,");
		sb.append("sol.LINE_NUM, sol.ITEM_ID, sol.ITEM_NAME, sol.UNIT_NAME , sol.PRI3,");
		sb.append("sol.DISCOUNT,sol.QTY_ORD, sol.AMT_ORD, sol.NOTE,sol.PROFIT, ");
		sb.append("sol.BIG_BAR, sol.ITEM_BAR, sol.BIG_UNIT_NAME, sol.UNIT_RATIO, sol.BIG_PRI3, sol.COST  ");
		sb.append("FROM RETURN_ORDER ro,SALE_ORDER_LINE sol WHERE so.MERCH_ID = ? AND ro.ORDER_ID=sol.ORDER_ID");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectSaleOrderLineAndReturnOrder(
			Map<String, Object> returnOrderParam) throws Exception {
		logger.debug("OrderDaoImpl selectReturnOrder returnOrderParam: " + returnOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectReturnOrderSql);
		String returnOrderId = (String) returnOrderParam.get("return_order_id");
		String orderId = (String) returnOrderParam.get("order_id");
		String startDate = (String) returnOrderParam.get("start_date");
		String endDate = (String) returnOrderParam.get("end_date");
		String returnOrderDate = (String) returnOrderParam.get("return_order_date");
		String status = (String) returnOrderParam.get("status");
		String operator = (String) returnOrderParam.get("operator");
		Map<String, Object> orderByMap = (Map<String, Object>) returnOrderParam.get("order_by");
		
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
		orderByBuffer.append(" RETURN_ORDER_DATE DESC, RETURN_ORDER_TIME DESC"); // 默认按照DATE, TIME倒序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 分页
		Integer pageIndex = 1;
		if(returnOrderParam.get("page_index")!=null) {
			pageIndex = Integer.parseInt(returnOrderParam.get("page_index").toString());
		}
		Integer pageSize = 20;
		if(returnOrderParam.get("page_size")!=null) {
			pageSize = Integer.parseInt(returnOrderParam.get("page_size").toString());
		}
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(returnOrderParam.get("merch_id"));
		if(returnOrderId!=null) {
			sqlBuffer.append(" AND RETURN_ORDER_ID = ?");
			paramArray.add(returnOrderId);
		}
		if(orderId!=null) {
			String[] orderIdArray = returnOrderParam.get("order_id").toString().split(",");
			if(orderIdArray.length>0) {
				if(orderIdArray.length==1) {
					sqlBuffer.append(" AND ORDER_ID=?");
					paramArray.add(orderIdArray[0]);
				} else {
					sqlBuffer.append(" AND ORDER_ID IN (");
					int index = 0;
					for(String id : orderIdArray) {
						if(index++==0) sqlBuffer.append("?");
						else sqlBuffer.append(", ?");
						paramArray.add(id);
					}
					sqlBuffer.append(")");
				}
			}
		}
		if(returnOrderDate!=null) {
			sqlBuffer.append(" AND RETURN_ORDER_DATE = ?");
			paramArray.add(returnOrderDate);
		} else {
			if(startDate!=null) {
				sqlBuffer.append(" AND RETURN_ORDER_DATE >= ?");
				paramArray.add(startDate);
			}
			if(endDate!=null) {
				sqlBuffer.append(" AND RETURN_ORDER_DATE <= ?");
				paramArray.add(endDate);
			}
		}
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
		if(operator!=null) {
			sqlBuffer.append(" AND OPERATOR = ?");
			paramArray.add(operator);
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), pageIndex, pageSize, paramArray.toArray());
		returnOrderParam.put("page_count", pageResult.getPageSum());
		returnOrderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	/**
	 * 联表查询
	 * 
	 * 销售管理(SALE_ORDER)  
	 * 销售单行(SALE_ORDER_LINE) 关联
	 * */
	public static final String selectSaleOrderAndSaleOrderLineSql = initselectSaleOrderAndSaleOrderLineSql();
	private static String initselectSaleOrderAndSaleOrderLineSql(){
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT so.ORDER_ID, so.CONSUMER_ID, so.MERCH_ID, so.ORDER_TYPE, so.PAY_TYPE, so.ORDER_DATE, so.ORDER_TIME, so.STATUS, so.PMT_STATUS,");
		sb.append(" so.QTY_ORD_TOTAL, so.AMTYS_ORD_TOTAL, so.AMT_ORD_TOTAL, so.AMT_ORD_CHANGE, so.AMT_ORD_LOSS, so.AMT_ORD_PROFIT, so.NOTE, so.OPERATOR, so.QTY_ORD_COUNT, ");
		sb.append("sol.LINE_NUM, sol.ITEM_ID, sol.ITEM_NAME, sol.UNIT_NAME , sol.PRI3,");
		sb.append("sol.DISCOUNT,sol.QTY_ORD, sol.AMT_ORD, sol.NOTE,sol.PROFIT, ");
		sb.append("sol.BIG_BAR, sol.ITEM_BAR, sol.BIG_UNIT_NAME, sol.UNIT_RATIO, sol.BIG_PRI3, sol.COST  ");
		sb.append("FROM SALE_ORDER so,SALE_ORDER_LINE sol WHERE so.MERCH_ID = ? AND so.ORDER_ID = sol.ORDER_ID ");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectSaleOrderAndSaleOrderLine(
			Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectSaleOrderAndSaleOrderLine saleOrderParam: " + saleOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectSaleOrderAndSaleOrderLineSql);
		String merchId = (String) saleOrderParam.get("merch_id");
		String orderId = (String) saleOrderParam.get("order_id");
		String orderType = (String) saleOrderParam.get("order_type");
		String startDate = (String) saleOrderParam.get("order_date_floor");
		String endDate = (String) saleOrderParam.get("order_date_ceiling");
		String orderDate = (String) saleOrderParam.get("order_date");
		String orderTime = (String) saleOrderParam.get("order_time");
		String orderTimeFloor = (String) saleOrderParam.get("order_time_floor");
		String orderTimeCeiling = (String) saleOrderParam
				.get("order_time_ceiling");
		String status = (String) saleOrderParam.get("status");
		String pmtStatus = (String) saleOrderParam.get("pmt_status");
		Map<String, Object> orderByMap = (Map<String, Object>) saleOrderParam
				.get("order_by");
		// 排序
		StringBuffer orderByBuffer = new StringBuffer();
		if (orderByMap != null) {
			for (String orderBy : orderByMap.keySet()) {
				if ("1".equals(orderByMap.get(orderBy))) {
					orderByBuffer.append(" " + orderBy + " DESC");
				} else {
					orderByBuffer.append(" " + orderBy + " ASC");
				}
			}
		}
		orderByBuffer.append(" ORDER_DATE DESC, ORDER_TIME DESC"); // 默认按照ORDER_ID倒序排列
		orderByBuffer.insert(0, " ORDER BY");

		// 分页
		Integer pageIndex = 1;
		if (saleOrderParam.get("page_index") != null) {
			pageIndex = Integer.parseInt(saleOrderParam.get("page_index")
					.toString());
		}
		saleOrderParam.put("page_index", pageIndex);
		Integer pageSize = 20;
		if (saleOrderParam.get("page_size") != null) {
			pageSize = Integer.parseInt(saleOrderParam.get("page_size")
					.toString());
		}
		saleOrderParam.put("page_size", pageSize);

		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(merchId);
		if (orderId != null) {
			sqlBuffer.append(" AND ORDER_ID = ?");
			paramArray.add(orderId);
		}
		if (orderType != null) {
			String[] typeArray = orderType.split(",");
			if (typeArray.length > 1) {
				sqlBuffer.append(" AND (");
				for (int i = 0; i < typeArray.length; i++) {
					if (i != 0) {
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
		if (status != null) {
			String[] statusArray = status.split(",");
			if (statusArray.length == 1) {
				sqlBuffer.append(" AND STATUS = ?");
				paramArray.add(statusArray[0]);
			} else {
				sqlBuffer.append(" AND STATUS IN (");
				for (int i = 0; i < statusArray.length; i++) {
					paramArray.add(statusArray[i]);
					if (i == 0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if (pmtStatus != null) {
			String[] pmtStatusArray = pmtStatus.split(",");
			if (pmtStatusArray.length == 1) {
				sqlBuffer.append(" AND PMT_STATUS = ?");
				paramArray.add(pmtStatusArray[0]);
			} else {
				sqlBuffer.append(" AND PMT_STATUS IN (");
				for (int i = 0; i < pmtStatusArray.length; i++) {
					paramArray.add(pmtStatusArray[i]);
					if (i == 0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if (orderDate != null) {
			sqlBuffer.append(" AND ORDER_DATE = ?");
			paramArray.add(orderDate);
		} else {
			if (startDate != null) {
				sqlBuffer.append(" AND ORDER_DATE >= ?");
				paramArray.add(startDate);
			}
			if (endDate != null) {
				sqlBuffer.append(" AND ORDER_DATE <= ?");
				paramArray.add(endDate);
			}
		}
		if (orderTime != null) {
			sqlBuffer.append(" AND ORDER_TIME = ?");
			paramArray.add(orderTime);
		} else {
			if (orderTimeFloor != null) {
				sqlBuffer.append(" AND ORDER_TIME >= ?");
				paramArray.add(orderTimeFloor);
			}
			if (orderTimeCeiling != null) {
				sqlBuffer.append(" AND ORDER_TIME <= ?");
				paramArray.add(orderTimeCeiling);
			}
		}
		
		if (saleOrderParam.get("consumer_id") != null
				&& !"".equals(saleOrderParam.get("consumer_id"))) {
			String[] consumerIdArray = saleOrderParam.get("consumer_id")
					.toString().split(",");
			if (consumerIdArray.length == 1) {
				sqlBuffer.append(" AND CONSUMER_ID = ?");
				paramArray.add(consumerIdArray[0]);
			} else {
				sqlBuffer.append(" AND CONSUMER_ID IN (");
				for (int i = 0; i < consumerIdArray.length; i++) {
					paramArray.add(consumerIdArray[i]);
					if (i == 0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		Page pageResult = this
				.searchPaginatedBySql(sqlBuffer
						.append(orderByBuffer.toString()).toString(),
						(Integer) saleOrderParam.get("page_index"),
						(Integer) saleOrderParam.get("page_size"), paramArray
								.toArray());
		saleOrderParam.put("page_count", pageResult.getPageSum());
		saleOrderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}

	
	public static final String selectWgddOrderSql = initSelectWgddOrderSql();
	private static String initSelectWgddOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, so.CONSUMER_ID, MC.MERCH_ID, ORDER_TYPE, PAY_TYPE, ORDER_DATE, ");
		sb.append("CASE WHEN MC.TELEPHONE IS NULL THEN SO.CONSUMER_ID  ELSE MC.TELEPHONE  END TELEPHONE, ");
		sb.append("SO.STATUS, PMT_STATUS,QTY_ORD_TOTAL, AMTYS_ORD_TOTAL, AMT_ORD_TOTAL, AMT_ORD_CHANGE, ");
		sb.append("AMT_ORD_LOSS, AMT_ORD_PROFIT, ADJUSTED_AMOUNT, OPERATOR, QTY_ORD_COUNT ,ORDER_TIME, NOTE ");
		sb.append("FROM SALE_ORDER SO ,BASE_MERCH_CONSUMER MC ");
		sb.append("WHERE MC.CONSUMER_ID (+)= SO.CONSUMER_ID ");
		sb.append("AND SO.MERCH_ID = ? ");
		return sb.toString();
	}
	
	@Override
	public List<Map<String, Object>> selectWgddOrder(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("selectWgddOrder selectSaleOrder saleOrderParam: " + saleOrderParam);
		StringBuilder sqlBuffer = new StringBuilder(selectWgddOrderSql);
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(MapUtil.getString(saleOrderParam, "merch_id"));
		SQLUtil.initSQLEqual(saleOrderParam, sqlBuffer, paramArray, "order_type");
		SQLUtil.initSQLBetweenAnd(saleOrderParam, sqlBuffer, paramArray, "order_date", "order_date_floor", "order_date_ceiling");
		sqlBuffer.append(" ORDER BY CASE WHEN SO.STATUS='01' THEN 'ZZ' ELSE STATUS END DESC, ORDER_DATE DESC, ORDER_TIME DESC ");
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.toString(), MapUtil.getInt(saleOrderParam, "page_index", 1), MapUtil.getInt(saleOrderParam, "page_size", 20), paramArray.toArray());
		saleOrderParam.put("page_count", pageResult.getPageSum());
		saleOrderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	
	/**
	 * 查询销售单, 网络销售和实体销售
	 */
	public static final String selectSaleOrderSql = initSelectSaleOrderSql();
	private static String initSelectSaleOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, CONSUMER_ID, MERCH_ID, ORDER_TYPE, PAY_TYPE, ORDER_DATE, ORDER_TIME, STATUS, PMT_STATUS,");
		sb.append(" QTY_ORD_TOTAL, AMTYS_ORD_TOTAL, AMT_ORD_TOTAL, AMT_ORD_CHANGE, AMT_ORD_LOSS, AMT_ORD_PROFIT, ADJUSTED_AMOUNT, NOTE,");
		sb.append(" OPERATOR, QTY_ORD_COUNT, CONSUMER_REBATE_AMOUNT FROM SALE_ORDER WHERE MERCH_ID = ?");
		return sb.toString();
	}
	
	@Override
	public List<Map<String, Object>> selectSaleOrder(Map<String, Object> saleOrderParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectSaleOrder saleOrderParam: " + saleOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectSaleOrderSql);
		String merchId = (String) saleOrderParam.get("merch_id");
		String orderId = (String) saleOrderParam.get("order_id");
		String orderType = (String) saleOrderParam.get("order_type");
		String startDate = (String) saleOrderParam.get("order_date_floor");
		String endDate = (String) saleOrderParam.get("order_date_ceiling");
		String orderDate = (String) saleOrderParam.get("order_date");
		String orderTime = (String) saleOrderParam.get("order_time");
		String orderTimeFloor = (String) saleOrderParam.get("order_time_floor");
		String orderTimeCeiling = (String) saleOrderParam.get("order_time_ceiling");
		String status = (String) saleOrderParam.get("status");
		String pmtStatus = (String) saleOrderParam.get("pmt_status");
		String roleIds =MapUtil.getString(saleOrderParam, "role_id", "1");
		Map<String, Object> orderByMap = (Map<String, Object>) saleOrderParam.get("order_by");
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
		orderByBuffer.append(" ORDER_DATE DESC, ORDER_TIME DESC"); // 默认按照ORDER_ID倒序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 分页
		Integer pageIndex = MapUtil.getInt(saleOrderParam, "page_index", 1);
		Integer pageSize = MapUtil.getInt(saleOrderParam, "page_size", 20);
		saleOrderParam.put("page_index", pageIndex);
		saleOrderParam.put("page_size", pageSize);
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(merchId);
		if(orderId!=null) {
			sqlBuffer.append(" AND ORDER_ID = ?");
			paramArray.add(orderId);
		}
		if (roleIds.equals("3")) {
			String userCode = MapUtil.getString(saleOrderParam, "user_code");
			sqlBuffer.append(" AND OPERATOR = ? ");
			paramArray.add(userCode);
		}
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
		if(pmtStatus!=null) {
			String[] pmtStatusArray = pmtStatus.split(",");
			if(pmtStatusArray.length==1) {
				sqlBuffer.append(" AND PMT_STATUS = ?");
				paramArray.add(pmtStatusArray[0]);
			} else {
				sqlBuffer.append(" AND PMT_STATUS IN (");
				for(int i=0; i<pmtStatusArray.length; i++) {
					paramArray.add(pmtStatusArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		if(orderDate!=null) {
			sqlBuffer.append(" AND ORDER_DATE = ?");
			paramArray.add(orderDate);
		} else {
			if(startDate!=null) {
				sqlBuffer.append(" AND ORDER_DATE >= ?");
				paramArray.add(startDate);
			}
			if(endDate!=null) {
				sqlBuffer.append(" AND ORDER_DATE <= ?");
				paramArray.add(endDate);
			}
		}
		if(orderTime!=null) {
			sqlBuffer.append(" AND ORDER_TIME = ?");
			paramArray.add(orderTime);
		} else {
			if(orderTimeFloor!=null) {
				sqlBuffer.append(" AND ORDER_TIME >= ?");
				paramArray.add(orderTimeFloor);
			}
			if(orderTimeCeiling!=null) {
				sqlBuffer.append(" AND ORDER_TIME <= ?");
				paramArray.add(orderTimeCeiling);
			}
		}
		if(saleOrderParam.get("consumer_id")!=null && !"".equals(saleOrderParam.get("consumer_id"))) {
			String[] consumerIdArray = saleOrderParam.get("consumer_id").toString().split(",");
			if(consumerIdArray.length==1) {
				sqlBuffer.append(" AND CONSUMER_ID = ?");
				paramArray.add(consumerIdArray[0]);
			} else {
				sqlBuffer.append(" AND CONSUMER_ID IN (");
				for(int i=0; i<consumerIdArray.length; i++) {
					paramArray.add(consumerIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), (Integer)saleOrderParam.get("page_index"), (Integer)saleOrderParam.get("page_size"), paramArray.toArray());
		saleOrderParam.put("page_count", pageResult.getPageSum());
		saleOrderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	/**
	 * 查询销售单商品行, 必须按照order_id查询, 不分页, 不排序
	 */
	public static final String selectSaleOrderLineSql = initSelectSaleOrderLineSql();
	private static String initSelectSaleOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, LINE_NUM, ITEM_ID, ITEM_NAME, UNIT_NAME , PRI3, DISCOUNT, QTY_ORD, AMT_ORD, NOTE,PROFIT, ");
		sb.append(" BIG_BAR, ITEM_BAR, BIG_UNIT_NAME, UNIT_RATIO, BIG_PRI3, COST, ADJUSTED_AMOUNT, OTHER_ADJUSTED_AMOUNT ");
		sb.append(" FROM SALE_ORDER_LINE WHERE 1=1");
		return sb.toString();
	}
	
	@Override
	public List<Map<String, Object>> selectSaleOrderLine(Map<String, Object> saleOrderLineParam) throws Exception {
		logger.debug("SaleOrderDaoImpl selectSaleOrderLine saleOrderLineParam: " + saleOrderLineParam);
		StringBuffer sqlBuffer = new StringBuffer(selectSaleOrderLineSql);
		String orderId = (String) saleOrderLineParam.get("order_id");
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		if(orderId!=null) {
			String[] orderIdArray = orderId.split(",");
			if(orderIdArray.length==1) {
				sqlBuffer.append(" AND ORDER_ID = ?");
				paramArray.add(orderIdArray[0]);
			} else {
				sqlBuffer.append(" AND ORDER_ID IN (");
				for(int i=0; i<orderIdArray.length; i++) {
					paramArray.add(orderIdArray[i]);
					if(i==0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(")");
			}
			return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
		}
		// 如果没有用order_id查询, 则报错
		return new ArrayList<Map<String, Object>>();
	}
	
	
	public static final String searchSaleOrderLineByItemIdSql = initSearchSaleOrderLineByItemIdSql();
	private static String initSearchSaleOrderLineByItemIdSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ITEM_ID, NVL(SUM(PROFIT*QTY_ORD*UNIT_RATIO),0) PROFIT_AMOUNT,");
		sb.append(" NVL(SUM(AMT_ORD),0) SALE_AMOUNT, NVL(SUM(QTY_ORD*UNIT_RATIO),0) SALE_QUANTITY, ");
		sb.append("NVL(SUM(ADJUSTED_AMOUNT),0) ADJUSTED_AMOUNT, NVL(SUM(OTHER_ADJUSTED_AMOUNT),0)OTHER_ADJUSTED_AMOUNT ");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchSaleOrderLineByItemId(Map<String, Object> saleOrderLineParam) throws Exception {
		logger.debug("SaleOrderDaoImpl searchSaleOrderLineByOrderId saleOrderLineParam: " + saleOrderLineParam);
		StringBuffer sqlBuffer = new StringBuffer(searchSaleOrderLineByItemIdSql);
		List<Object> paramObject = new ArrayList<Object>();
		
		String orderId = (String) saleOrderLineParam.get("order_id");
		if(orderId==null) return Collections.EMPTY_LIST;
		List<Object> orderIdList = new ArrayList<Object>();
		StringBuffer orderSqlBuffer = new StringBuffer(" FROM SALE_ORDER_LINE WHERE ");
		orderSqlBuffer.append(SQLUtil.initSQLIn(saleOrderLineParam, orderIdList, "order_id"));
		orderSqlBuffer.append(" GROUP BY ITEM_ID");
		
		// offline的order_id, 如果为空全为0, online的order_id为全部的
		String offlineOrderId = (String) saleOrderLineParam.get("offline_order_id");
		String onlineOrderId = (String) saleOrderLineParam.get("online_order_id");
		if(offlineOrderId!=null && onlineOrderId!=null && !"".equals(offlineOrderId) && !"".equals(onlineOrderId)) {
			List<Object> offlineOrderIdList = new ArrayList<Object>();
			String offlineOrderSql = ", NVL(SUM(CASE WHEN " + SQLUtil.initSQLIn(saleOrderLineParam, offlineOrderIdList, "order_id");
			String onlineOrderSql = ", NVL(SUM(CASE WHEN " + SQLUtil.initSQLNotIn(saleOrderLineParam, null, "order_id");
			sqlBuffer.append(offlineOrderSql+" THEN PROFIT*QTY_ORD*UNIT_RATIO ELSE 0 END),0) PROFIT_AMOUNT_OFFLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(offlineOrderSql+" THEN AMT_ORD ELSE 0 END),0) SALE_AMOUNT_OFFLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(offlineOrderSql+" THEN QTY_ORD*UNIT_RATIO ELSE 0 END),0) SALE_QUANTITY_OFFLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(onlineOrderSql+" THEN PROFIT*QTY_ORD*UNIT_RATIO ELSE 0 END),0) PROFIT_AMOUNT_ONLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(onlineOrderSql+" THEN AMT_ORD ELSE 0 END),0) SALE_AMOUNT_ONLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(onlineOrderSql+" THEN QTY_ORD*UNIT_RATIO ELSE 0 END),0) SALE_QUANTITY_ONLINE");
			paramObject.addAll(offlineOrderIdList);
		}
		sqlBuffer.append(orderSqlBuffer.toString());
		paramObject.addAll(orderIdList);
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	
	
	@Override
	public List<Map<String, Object>> searchReturnOrderLineByItemId(Map<String, Object> saleOrderLineParam) throws Exception {
		logger.debug("SaleOrderDaoImpl searchSaleOrderLineByOrderId saleOrderLineParam: " + saleOrderLineParam);
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT ITEM_ID, NVL(SUM(PROFIT*QTY_ORD*UNIT_RATIO),0) PROFIT_AMOUNT,");
		sqlBuffer.append(" NVL(SUM(AMT_ORD),0) SALE_AMOUNT, NVL(SUM(QTY_ORD*UNIT_RATIO),0) SALE_QUANTITY, ");
		sqlBuffer.append("NVL(SUM(ADJUSTED_AMOUNT),0) ADJUSTED_AMOUNT ");
		
		List<Object> paramObject = new ArrayList<Object>();
		String orderId = (String) saleOrderLineParam.get("order_id");
		if(orderId==null) return Collections.EMPTY_LIST;
		List<Object> orderIdList = new ArrayList<Object>();
		StringBuffer orderSqlBuffer = new StringBuffer(" FROM Return_ORDER_LINE WHERE ");
		orderSqlBuffer.append(SQLUtil.initSQLIn(saleOrderLineParam, orderIdList, "order_id"));
		orderSqlBuffer.append(" GROUP BY ITEM_ID");
		
		// offline的order_id, 如果为空全为0, online的order_id为全部的
		String offlineOrderId = (String) saleOrderLineParam.get("offline_order_id");
		String onlineOrderId = (String) saleOrderLineParam.get("online_order_id");
		if(offlineOrderId!=null && onlineOrderId!=null && !"".equals(offlineOrderId) && !"".equals(onlineOrderId)) {
			List<Object> offlineOrderIdList = new ArrayList<Object>();
			String offlineOrderSql = ", NVL(SUM(CASE WHEN " + SQLUtil.initSQLIn(saleOrderLineParam, offlineOrderIdList, "order_id");
			String onlineOrderSql = ", NVL(SUM(CASE WHEN " + SQLUtil.initSQLNotIn(saleOrderLineParam, null, "order_id");
			sqlBuffer.append(offlineOrderSql+" THEN PROFIT*QTY_ORD*UNIT_RATIO ELSE 0 END),0) PROFIT_AMOUNT_OFFLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(offlineOrderSql+" THEN AMT_ORD ELSE 0 END),0) SALE_AMOUNT_OFFLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(offlineOrderSql+" THEN QTY_ORD*UNIT_RATIO ELSE 0 END),0) SALE_QUANTITY_OFFLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(onlineOrderSql+" THEN PROFIT*QTY_ORD*UNIT_RATIO ELSE 0 END),0) PROFIT_AMOUNT_ONLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(onlineOrderSql+" THEN AMT_ORD ELSE 0 END),0) SALE_AMOUNT_ONLINE");
			paramObject.addAll(offlineOrderIdList);
			sqlBuffer.append(onlineOrderSql+" THEN QTY_ORD*UNIT_RATIO ELSE 0 END),0) SALE_QUANTITY_ONLINE");
			paramObject.addAll(offlineOrderIdList);
		}
		sqlBuffer.append(orderSqlBuffer.toString());
		paramObject.addAll(orderIdList);
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	
	
	/**
	 * 查询退货单
	 */
	public static final String selectReturnOrderSql = initSelectReturnOrderSql();
	private static String initSelectReturnOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT RETURN_ORDER_ID, ORDER_ID, RETURN_ORDER_DATE, RETURN_ORDER_TIME,");
		sb.append("QTY_RETURN_TOTAL, AMT_RETURN_TOTAL, AMT_RETURN_LOSS,");
		sb.append("RETURN_STATUS, OPERATOR,ORDER_TYPE,NOTE ");
		sb.append("FROM RETURN_ORDER ");
		sb.append("WHERE MERCH_ID=? ");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectReturnOrder(Map<String, Object> returnOrderParam) throws Exception {
		logger.debug("OrderDaoImpl selectReturnOrder returnOrderParam: " + returnOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectReturnOrderSql);
		String returnOrderId = (String) returnOrderParam.get("return_order_id");
		String orderId = (String) returnOrderParam.get("order_id");
		String startDate = (String) returnOrderParam.get("start_date");
		String endDate = (String) returnOrderParam.get("end_date");
		String returnOrderDate = (String) returnOrderParam.get("return_order_date");
		String status = (String) returnOrderParam.get("status");
		String operator = (String) returnOrderParam.get("operator");
		Map<String, Object> orderByMap = (Map<String, Object>) returnOrderParam.get("order_by");
		String roleIds =MapUtil.getString(returnOrderParam, "role_id", "1");
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
		orderByBuffer.append(" RETURN_ORDER_DATE DESC, RETURN_ORDER_TIME DESC"); // 默认按照DATE, TIME倒序排列
		orderByBuffer.insert(0, " ORDER BY");
		
		// 分页
		Integer pageIndex = 1;
		if(returnOrderParam.get("page_index")!=null) {
			pageIndex = Integer.parseInt(returnOrderParam.get("page_index").toString());
		}
		Integer pageSize = 20;
		if(returnOrderParam.get("page_size")!=null) {
			pageSize = Integer.parseInt(returnOrderParam.get("page_size").toString());
		}
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(returnOrderParam.get("merch_id"));
		if(returnOrderId!=null) {
			sqlBuffer.append(" AND RETURN_ORDER_ID = ?");
			paramArray.add(returnOrderId);
		}
		if(orderId!=null) {
			String[] orderIdArray = returnOrderParam.get("order_id").toString().split(",");
			if(orderIdArray.length>0) {
				if(orderIdArray.length==1) {
					sqlBuffer.append(" AND ORDER_ID=?");
					paramArray.add(orderIdArray[0]);
				} else {
					sqlBuffer.append(" AND ORDER_ID IN (");
					int index = 0;
					for(String id : orderIdArray) {
						if(index++==0) sqlBuffer.append("?");
						else sqlBuffer.append(", ?");
						paramArray.add(id);
					}
					sqlBuffer.append(")");
				}
			}
		}
		if (roleIds.equals("3")) {
			String userCode = MapUtil.getString(returnOrderParam, "user_code");
			sqlBuffer.append(" AND OPERATOR = ? ");
			paramArray.add(userCode);
		}
		if(returnOrderDate!=null) {
			sqlBuffer.append(" AND RETURN_ORDER_DATE = ?");
			paramArray.add(returnOrderDate);
		} else {
			if(startDate!=null) {
				sqlBuffer.append(" AND RETURN_ORDER_DATE >= ?");
				paramArray.add(startDate);
			}
			if(endDate!=null) {
				sqlBuffer.append(" AND RETURN_ORDER_DATE <= ?");
				paramArray.add(endDate);
			}
		}
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
		if(operator!=null) {
			sqlBuffer.append(" AND OPERATOR = ?");
			paramArray.add(operator);
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), pageIndex, pageSize, paramArray.toArray());
		returnOrderParam.put("page_count", pageResult.getPageSum());
		returnOrderParam.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}

	/**
	 * 销售额占比, 可以按照商品编码或者商品分类查询, 可以按月按日按年查询
	 */
	public static final String searchSaleroomSql = initSearchSaleroomSql();
	private static String initSearchSaleroomSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT item.ITEM_KIND_ID group_key, sale.ORDER_DATE time_interval, NVL(SUM(line.AMT_ORD),0) saleroom");
		sb.append(" FROM SALE_ORDER sale, SALE_ORDER_LINE line, BASE_MERCH_ITEM item");
		sb.append(" WHERE sale.ORDER_ID = line.ORDER_ID AND line.ITEM_ID = item.ITEM_ID AND sale.MERCH_ID = item.MERCH_ID");
		sb.append(" AND item.STATUS!='0' AND sale.MERCH_ID = ? AND sale.ORDER_DATE BETWEEN ? AND ?");
		sb.append(" GROUP BY sale.ORDER_DATE, item.ITEM_KIND_ID ORDER BY sale.ORDER_DATE, item.ITEM_KIND_ID");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchSaleroom(Map<String, Object> saleroomParam) throws Exception {
		logger.debug("OrderDaoImpl searchSaleroot saleroomParam:" + saleroomParam);
		StringBuffer sqlBuffer = new StringBuffer(searchSaleroomSql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(saleroomParam.get("merch_id"));
		paramObject.add(saleroomParam.get("start_date"));
		paramObject.add(saleroomParam.get("end_date"));
		if("item_id".equals(saleroomParam.get("group_key"))) {
			sqlBuffer = new StringBuffer(searchSaleroomSql.replaceAll("ITEM_KIND_ID", "ITEM_ID"));
		} else if(!"item_kind_id".equals(saleroomParam.get("group_key"))) {
			sqlBuffer = new StringBuffer( searchSaleroomSql.replaceAll("item.ITEM_KIND_ID group_key, ", "").replaceAll("item.ITEM_KIND_ID, ", "").replaceAll(", item.ITEM_KIND_ID", "") );
		} // else 是按照item_kind_id查询
		if("monthly".equals(saleroomParam.get("time_interval"))) {
			sqlBuffer = new StringBuffer(sqlBuffer.toString().replaceAll("sale.ORDER_DATE time_interval", "SUBSTR(sale.ORDER_DATE, 0, 6) time_interval").replaceAll("GROUP BY sale.ORDER_DATE", "GROUP BY SUBSTR(sale.ORDER_DATE, 0, 6)").replaceAll("ORDER BY sale.ORDER_DATE", "ORDER BY SUBSTR(sale.ORDER_DATE, 0, 6)"));
		} else if("annual".equals(saleroomParam.get("time_interval"))) {
			sqlBuffer = new StringBuffer(sqlBuffer.toString().replaceAll("sale.ORDER_DATE time_interval", "SUBSTR(sale.ORDER_DATE, 0, 4) time_interval").replaceAll("GROUP BY sale.ORDER_DATE", "GROUP BY SUBSTR(sale.ORDER_DATE, 0, 4)").replaceAll("ORDER BY sale.ORDER_DATE", "ORDER BY SUBSTR(sale.ORDER_DATE, 0, 4)"));
		} // else 是dialy
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	
	/**
	 * 获取商户某日销售数据
	 */
	public static final String searchSaleOrderHistorySql = initSearchSaleOrderHistorySql();
	private static String initSearchSaleOrderHistorySql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, NVL(SUM(CASE WHEN ORDER_TYPE='03' THEN 1 ELSE 0 END),0) SALE_COUNT_ONLINE,");
		sb.append(" NVL(SUM(CASE WHEN ORDER_TYPE='03' THEN 0 ELSE 1 END),0) SALE_COUNT_OFFLINE,");
		sb.append(" NVL(SUM(CASE WHEN ORDER_TYPE='03' THEN AMT_ORD_TOTAL-AMT_ORD_CHANGE ELSE 0 END),0) SALE_AMOUNT_ONLINE,");
		sb.append(" NVL(SUM(CASE WHEN ORDER_TYPE='03' THEN 0 ELSE AMT_ORD_TOTAL-AMT_ORD_CHANGE END),0) SALE_AMOUNT_OFFLINE,");
		sb.append(" NVL(SUM(AMT_ORD_TOTAL-AMT_ORD_CHANGE),0) SALE_AMOUNT, NVL(SUM(1),0) SALE_COUNT, NVL(SUM(AMT_ORD_LOSS),0) SALE_LOSS");
		sb.append(" FROM SALE_ORDER WHERE ORDER_DATE=?");
		sb.append(" GROUP BY MERCH_ID");
		return sb.toString();
	} 
	@Override
	public List<Map<String, Object>> searchSaleOrderHistory(Map<String, Object> historyParam) throws Exception {
		logger.debug("OrderDaoImpl searchSaleOrderHistory historyParam: " + historyParam);
		StringBuffer sqlBuffer = new StringBuffer(searchSaleOrderHistorySql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(historyParam.get("order_date"));
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	
	/**
	 * 获取商户某日进货数据, 包括进货总量和进货总额
	 */
	public static final String searchPurchOrderHistorySql = initSearchPurchOrderHistorySql();
	private static String initSearchPurchOrderHistorySql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, NVL(SUM(AMT_PURCH_TOTAL),0) PURCH_AMOUNT, NVL(SUM(QTY_PURCH_TOTAL),0) PURCH_COUNT");
		sb.append(" FROM PURCH_ORDER WHERE VOUCHER_DATE=? GROUP BY MERCH_ID");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchPurchOrderHistory(Map<String, Object> historyParam) throws Exception {
		logger.debug("OrderDaoImpl searchPurchOrderHistory historyParam: " + historyParam);
		StringBuffer sqlBuffer = new StringBuffer(searchPurchOrderHistorySql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(historyParam.get("voucher_date"));
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}
	@Override
	public void insertTemporaryMerchOrder(Map<String, Object> dataMap)
			throws Exception {
		String sql="INSERT INTO TEMPORARY_MERCH_ORDER(MERCH_ID,ORDER_ID,ORDER_DATE,ORDERDATE,DATETIME) VALUES(?,?,?,TO_CHAR(SYSDATE,'yyyyMMdd'),TO_CHAR(SYSDATE,'hh24miss'))";
		Object[] obj=new Object[3];
		obj[0]=dataMap.get("merch_id");
		obj[1]=dataMap.get("order_id");
		obj[2]=dataMap.get("order_date");
		this.executeSQL(sql, obj);
	}
	@Override
	public List<Map<String, Object>> selectTemporaryMerchOrder(
			Map<String, Object> dataMap) throws Exception {
		logger.debug("OrderDaoImpl selectTemporaryMerchOrder dataMap: " + dataMap);
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT MERCH_ID,ORDER_ID,ORDER_DATE,ORDERDATE,DATETIME FROM TEMPORARY_MERCH_ORDER WHERE MERCH_ID=?");
		List<Map<String,Object>> orderDataList=null;
		if(dataMap.containsKey("orderDate")){
			sql.append("AND ORDERDATE>?");
			orderDataList=this.selectBySqlQuery(sql.toString(), new Object[]{dataMap.get("merch_id"),dataMap.get("orderDate")}); 
		}else{
			orderDataList=this.selectBySqlQuery(sql.toString(), new Object[]{dataMap.get("merch_id")});
		}
		return orderDataList;
	}
	@Override
	public void updateTemporaryMerchOrder(Map<String, Object> dataMap)
			throws Exception {
		String sql="UPDATE TEMPORARY_MERCH_ORDER SET ORDERDATE=TO_CHAR(SYSDATE,'yyyyMMdd'),DATETIME=TO_CHAR(SYSDATE,'hh24miss'),ORDER_ID=?,ORDER_DATE=? WHERE MERCH_ID=?";
		this.executeSQL(sql, new Object[]{dataMap.get("order_id"),dataMap.get("order_date"),dataMap.get("merch_id")});
	}
	@Override
	public void insertTemporaryMerchOrderLine(
			List<Map<String, Object>> dataMapList) throws Exception {
		String sql="INSERT INTO TEMPORARY_MERCH_ORDER_LINE(MERCH_ID,ITEM_ID,QTY_REQ,QTY_ORD) VALUES(?,?,?,?)";
		List<Object[]> list=new ArrayList<Object[]>();
		for(Map<String,Object> map:dataMapList){
			Object[] obj=new Object[4];
			obj[0]=map.get("merch_id");
			obj[1]=map.get("item_id");
			obj[2]=map.get("qty_req");
			obj[3]=map.get("qty_ord");
			list.add(obj);
		}
		this.executeBatchSQL(sql, list);
	}
	@Override
	public List<Map<String, Object>> selectTemporaryMerchOrderLine(
			Map<String, Object> dataMap) throws Exception {
		String sql="SELECT MERCH_ID,ITEM_ID,QTY_REQ,QTY_ORD FROM TEMPORARY_MERCH_ORDER_LINE WHERE MERCH_ID=?";
		List<Map<String,Object>> orderDataList=this.selectBySqlQuery(sql, new Object[]{dataMap.get("merch_id")});
		return orderDataList;
	}
	@Override
	public void deleteTemporaryMerchOrderLine(Map<String,Object> dataMap) throws Exception {
		String sql="DELETE FROM TEMPORARY_MERCH_ORDER_LINE WHERE MERCH_ID=?";
		this.executeSQL(sql,new Object[]{dataMap.get("merch_id")});
	}
	
	
	//获得未读的网购订单调试
	public static final String selectUnreadWGDECount=initUnreadWGDECount();
	public static String initUnreadWGDECount(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select count(*) from SALE_ORDER  ");
		sql.append(" where  merch_id=? ");
		sql.append(" and ORDER_TYPE='04' ");
		sql.append(" and STATUS ='01' ");
		return sql.toString();
	}
	@Override
	public int searchUnreadWGDDCount(Map<String, Object> paramsMap)throws Exception{
		logger.debug("OrderDaoImpl searchUnreadWGDECount paramsMap:"+paramsMap);
		StringBuffer sql=new StringBuffer(selectUnreadWGDECount);
		Object orderDate=paramsMap.get("order_date");
		Object orderTime=paramsMap.get("order_time");
		List<Object> list=new ArrayList<Object>();
		list.add(paramsMap.get("merch_id"));
		if(orderDate!=null&&!orderDate.equals("")){
			sql.append(" and ORDER_DATE>=? ");
			list.add(orderDate);
			if(orderTime!=null&&!orderDate.equals("")){
				sql.append(" and ORDER_TIME>=? ");
				list.add(orderTime);
			}
		}
		return this.selectIntBySqlQuery(sql.toString(), list.toArray());
	}
	
	//查询销售单，，关联base_merch_id sale_order_line sale_order
	public static final String searchSaleOrderJoinLineAndItemSql = initSearchSaleOrderJoinLineAndItemSql();
	public static String initSearchSaleOrderJoinLineAndItemSql(){
		StringBuffer sql=new StringBuffer();
		sql.append("select sol.order_id, max(so.consumer_id)consumer_id, max(so.order_type)order_type, max(so.pay_type)pay_type, ");
		sql.append("max(so.order_date)order_date, max(so.order_time)order_time, max(so.status)status, max(so.pmt_status)pmt_status, ");
		sql.append("max(so.qty_ord_total)qty_ord_total, max(so.amtys_ord_total)amtys_ord_total, max(so.amt_ord_total)amt_ord_total, ");
		sql.append("max(so.amt_ord_change)amt_ord_change, max(so.amt_ord_loss)amt_ord_loss, max(so.amt_ord_profit)amt_ord_profit, ");
		sql.append("max(so.adjusted_amount)adjusted_amount, max(so.note)note, max(so.operator)operator, max(so.qty_ord_count)qty_ord_count ");
		sql.append("from sale_order so, sale_order_line sol, base_merch_item bmi ");
		sql.append("where so.order_id = sol.order_id and sol.item_id = bmi.item_id and so.merch_id = bmi.merch_id ");
		sql.append("and 1=1 ");//------------merch_id;
		sql.append("");
		return sql.toString();
	}
	
	//查询销售单，，关联base_merch_id sale_order_line sale_order
	@Override
	public List<Map<String, Object>> searchSaleOrderJoinLineAndItem(Map<String, Object> paramMap )throws Exception{
		logger.debug("OrderDaoImpl searchSaleOrderJoinLineAndItem paramMap:"+paramMap);
		StringBuilder sql=new StringBuilder(searchSaleOrderJoinLineAndItemSql);
		List<Object> list = new ArrayList<Object>();
		int pageIndex = MapUtil.getInt(paramMap, "page_index", 1);
		int pageSize = MapUtil.getInt(paramMap, "page_size", 20);
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		SQLUtil.initSQLEqual(paramMap, sql, list, "so.merch_id", "bmi.item_kind_id", "so.consumer_id");
		if(!StringUtil.isBlank(startDate)){
			sql.append(" and so.order_date||so.order_time >= ? ");;
			list.add(startDate);
		}
		if(!StringUtil.isBlank(endDate)){
			sql.append(" and so.order_date||so.order_time <= ? ");
			list.add(endDate);
		}
		
		sql.append("group by sol.order_id ");
		sql.append("order by max(so.order_date)desc, max(so.order_time)desc ");
		Page pageResult = this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	
	//查询销售单行，，关联base_merch_id sale_order_line 
	@Override
	public List<Map<String, Object>> searchSaleOrderLineJoinItem(Map<String, Object> paramMap )throws Exception{
		logger.debug("OrderDaoImpl searchSaleOrderLineJoinItem paramMap:"+paramMap);
		StringBuilder sql=new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		int pageIndex = MapUtil.getInt(paramMap, "page_index", 1);
		int pageSize = MapUtil.getInt(paramMap, "page_size", 20);
		
		sql.append("select sol.order_id order_id, sol.line_num line_num, bmi.item_id item_id, bmi.item_name item_name, bmi.big_unit_ratio, ");
		sql.append("bmi.unit_name unit_name, bmi.item_kind_id item_kind_id, sol.pri3 pri3, sol.discount discount, sol.qty_ord qty_ord, sol.amt_ord amt_ord, ");
		sql.append("sol.note note,sol.profit profit, sol.big_bar big_bar, sol.item_bar item_bar, sol.big_unit_name big_unit_name, ");
		sql.append("sol.unit_ratio unit_ratio, sol.big_pri3 big_pri3, sol.cost cost, sol.adjusted_amount adjusted_amount ");
		sql.append("from sale_order_line sol, base_merch_item bmi ");
		sql.append("where sol.item_id = bmi.item_id and sol.merch_id = bmi.merch_id ");
		SQLUtil.initSQLEqual(paramMap, sql, list, "sol.merch_id", "bmi.item_kind_id");
		
		String orderId = MapUtil.getString(paramMap, "order_id");
		if(!StringUtil.isBlank(orderId)){
			String [] orderIdArr = orderId.split(",");
			if(orderIdArr.length>0){
				sql.append(" and order_id in (?");
				list.add(orderIdArr[0]);
			}
			for (int i = 1; i < orderIdArr.length; i++) {
				sql.append(",?");
				list.add(orderIdArr[i]);
			}
			sql.append(")");
		}
		
		Page pageResult = this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	
	//退货单行
	@Override
	public List<Map<String, Object>> searchMerchReturnOrderLine(Map<String, Object> orderMap)throws Exception{
		StringBuilder sql=new StringBuilder();
		sql.append("SELECT ORDER_ID, LINE_NUM, ITEM_ID, QTY_ORD, AMT_ORD, NOTE, ITEM_NAME, ");
		sql.append("PROFIT, MERCH_ID, BIG_BAR, BIG_UNIT_NAME, UNIT_RATIO, BIG_PRI3, ");
		sql.append("IS_RESALABLE, SALE_LINE_NUM, SALE_ORDER_ID, UNSALE_AMOUNT ");
		sql.append("FROM RETURN_ORDER_LINE WHERE 1=1 ");
		List<Object> list=new ArrayList<Object>();
		String orderId = MapUtil.getString(orderMap, "order_id");
		SQLUtil.initSQLEqual(orderMap, sql, list, "merch_id");
		
		if(orderId!=null){
			String[] orderIdArray = orderId.split(",");
			if(orderIdArray.length==1) {
				sql.append(" AND ORDER_ID = ? ");
				list.add(orderIdArray[0]);
			} else {
				sql.append(" AND ORDER_ID IN (");
				for(int i=0; i<orderIdArray.length; i++) {
					list.add(orderIdArray[i]);
					if(i==0) {
						sql.append("?");
					} else {
						sql.append(", ?");
					}
				}
				sql.append(") ");
			}
//			sql.append("  and order_id=? ");
//			list.add(orderId);
		}
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	
	
}
