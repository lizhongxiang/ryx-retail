package com.ryx.social.retail.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IPurchOrderDao;

@Repository
public class PurchOrderDaoImpl extends BaseDaoImpl implements IPurchOrderDao {
	
	private Logger logger = LoggerFactory.getLogger(PurchOrderDaoImpl.class);
	
	public static final String insertPurchOrderSql = getInsertPurchOrderSql();
	private static String getInsertPurchOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO PURCH_ORDER (ORDER_ID, MERCH_ID, ORDER_DATE, ORDER_TIME, STATUS, PMT_STATUS,");
		sb.append(" QTY_PURCH_TOTAL, AMT_PURCH_TOTAL, SUPPLIER_ID, VOUCHER_DATE, OPERATOR)");
		sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	/**
	 * 插入采购单表
	 */
	public void insertPurchOrder(Map<String, Object> purchParam) throws Exception {
		logger.debug("PurchOrderDaoImpl insertPurchOrder purchParam: " + purchParam);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(purchParam.get("order_id"));
		paramList.add(purchParam.get("merch_id"));
		paramList.add(purchParam.get("order_date"));
		paramList.add(purchParam.get("order_time"));
		paramList.add(purchParam.get("status"));
		paramList.add(purchParam.get("pmt_status"));
		paramList.add(MapUtil.getBigDecimal(purchParam, "qty_purch_total"));
		paramList.add(MapUtil.getBigDecimal(purchParam, "amt_purch_total"));
		paramList.add(purchParam.get("supplier_id"));
		paramList.add(purchParam.get("voucher_date"));
		paramList.add(purchParam.get("operator"));
		this.executeSQL(insertPurchOrderSql, paramList.toArray());
	}
	
	public static final String insertPurchOrderLineSql = getInsertPurchOrderLineSql();
	private static String getInsertPurchOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO PURCH_ORDER_LINE (ORDER_ID, ITEM_ID, PRI, QTY_REQ, QTY_ORD,");
		sb.append(" QTY_LMT, AMT_ORD, MERCH_ID,ITEM_NAME,ITEM_BAR,BIG_BAR,UNIT_RATIO,UNIT_NAME,BIG_UNIT_NAME,BIG_PRI) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?)");
		return sb.toString();
	}
	/**
	 * 插入采购单行表
	 */
	public void insertPurchOrderLine(Map<String, Object> purchOrderParam) throws Exception {
		logger.debug("PurchOrderDaoImpl insertPurchOrder purchOrderParam: " + purchOrderParam);
		List<Map<String, Object>> purchOrderLines = (List<Map<String, Object>>) purchOrderParam.get("list");
		String orderId = (String) purchOrderParam.get("order_id");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> purchOrderLineParam : purchOrderLines) {
			Object[] objectArray = new Object[15];
			objectArray[0] = orderId;
			objectArray[1] = purchOrderLineParam.get("item_id");
			if(purchOrderLineParam.containsKey("pri")&&purchOrderLineParam.get("pri")!=null&&!"".equals(purchOrderLineParam.get("pri"))){
				objectArray[2] = new BigDecimal(purchOrderLineParam.get("pri").toString());
			}else if(purchOrderLineParam.containsKey("pri1")&&purchOrderLineParam.get("pri1")!=null&&!"".equals(purchOrderLineParam.get("pri1"))){
				objectArray[2] = new BigDecimal(purchOrderLineParam.get("pri1").toString());
			}else{
				objectArray[2] = 0;
			}
			
			//非烟入库, 需求量==采购量==建议订量
			objectArray[3] = MapUtil.getBigDecimal(purchOrderLineParam, "qty_req");// new BigDecimal(purchOrderLineParam.get("qty_req").toString());
			objectArray[4] = MapUtil.getBigDecimal(purchOrderLineParam, "qty_ord");//  new BigDecimal(purchOrderLineParam.get("qty_ord").toString());
			objectArray[5] = MapUtil.getBigDecimal(purchOrderLineParam, "qty_rsn");// new BigDecimal(purchOrderLineParam.get("qty_rsn").toString());
			objectArray[6] = MapUtil.getBigDecimal(purchOrderLineParam, "amount");// new BigDecimal(purchOrderLineParam.get("amount").toString());
			objectArray[7] = purchOrderLineParam.get("merch_id");// new BigDecimal(purchOrderLineParam.get("merch_id").toString());
			
			objectArray[8] = purchOrderLineParam.get("item_name");
			objectArray[9] = purchOrderLineParam.get("item_bar");
			objectArray[10] =purchOrderLineParam.get("big_bar");
			objectArray[11] = MapUtil.getBigDecimal(purchOrderLineParam, "unit_ratio", new BigDecimal(1));// new BigDecimal(purchOrderLineParam.get("unit_ratio").toString());
			objectArray[12] =purchOrderLineParam.get("unit_name");
			objectArray[13] =purchOrderLineParam.get("big_unit_name");
			objectArray[14] =purchOrderLineParam.get("pri_wsale");
			paramArray.add(objectArray);
		}
		this.executeBatchSQL(insertPurchOrderLineSql, paramArray);
	}
	/**
	 * 商品入库, 入库前提交到采购单
	 */
	@Override
	public void submitPurchOrder(Map<String, Object> paramMap) throws Exception {
		insertPurchOrder(paramMap);
		insertPurchOrderLine(paramMap);
	}
	/**
	 * 手机端"经营提醒", 未入库订单
	 */
	private static final String selectPurchOrderSql = initSelectPurchOrderSql();
	private static String initSelectPurchOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, MERCH_ID, ORDER_DATE, ORDER_TIME, STATUS, PMT_STATUS, QTY_PURCH_TOTAL, AMT_PURCH_TOTAL,");
		sb.append(" SUPPLIER_ID, VOUCHER_DATE, OPERATOR FROM PURCH_ORDER WHERE MERCH_ID = ?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchPurchOrder(Map<String, Object> purchOrderParam) throws Exception {
		logger.debug("ItemDaoImpl searchPurchOrder purchOrderParam: " + purchOrderParam);
		StringBuffer sqlBuffer = new StringBuffer(selectPurchOrderSql);
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(purchOrderParam.get("merch_id"));
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
		return this.selectBySqlQuery(sqlBuffer.toString(), paramArray.toArray());
	}
	/**
	 * 获取某日采购总金额和采购笔数
	 */
	private static final String purchInfoSql = initPurchInfoSql();
	private static String initPurchInfoSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NVL(SUM(AMT_PURCH_TOTAL),0) purch_amount, NVL(COUNT(*),0) purch_count");
		sb.append(" FROM PURCH_ORDER WHERE MERCH_ID = ? AND ORDER_DATE = ?");
		return sb.toString();
	}
	@Override
	public Map<String, Object> getPurchInfo(Map<String, Object> purchParam) throws Exception {
		String merchId = (String) purchParam.get("merch_id");
		String orderId = (String) purchParam.get("order_id");
		List<Map<String, Object>> purchInfoResult = this.selectBySqlQuery(purchInfoSql, new Object[] {merchId, orderId});
		if(purchInfoResult.size()==1) {
			return purchInfoResult.get(0);
		}
		return new HashMap<String, Object>();
	}
	
	//采购单报表---年
	public static final String selectYearPurchOrderReportSql=getYearPurchOrderReportSql();
	private static String getYearPurchOrderReportSql(){
		StringBuffer sql=new StringBuffer();
		sql.append("  select sum(amt_purch_total) purchroom, substr(voucher_date,0,4) time_interval   ");
		sql.append("  from purch_order   ");
		sql.append("  where 1=1    ");
		sql.append("  and merch_id=?  ");
		sql.append("  and  substr(voucher_date,0,4)  >= ? and substr(voucher_date,0,4)<= ?  ");
		sql.append("  group by substr(voucher_date,0,4) ");
		sql.append("  order by substr(voucher_date,0,4) ");
		sql.append("   ");
		return sql.toString();
	}
	//采购单报表---月
	public static final String selectMonthPurchOrderReportSql=getMonthPurchOrderReportSql();
	private static String getMonthPurchOrderReportSql(){
		StringBuffer sql=new StringBuffer();
		sql.append("  select sum(amt_purch_total) purchroom, substr(voucher_date,0,6) time_interval   ");
		sql.append("  from purch_order   ");
		sql.append("  where 1=1    ");
		sql.append("  and merch_id=?  ");
		sql.append("  and  substr(voucher_date,0,6)  >= ? and substr(voucher_date,0,6)<= ?  ");
		sql.append("  group by substr(voucher_date,0,6) ");
		sql.append("  order by substr(voucher_date,0,6) ");
		sql.append("   ");
		return sql.toString();
		}
	//采购单报表---日
		public static final String selectDayPurchOrderReportSql=getDayPurchOrderReportSql();
		private static String getDayPurchOrderReportSql(){
			StringBuffer sql=new StringBuffer();
			sql.append("  select sum(amt_purch_total) purchroom,  voucher_date time_interval    ");
			sql.append("  from purch_order   ");
			sql.append("  where 1=1    ");
			sql.append("  and merch_id=? ");
			sql.append("  and   voucher_date  >= ? and  voucher_date <= ?  ");
			sql.append("  group by  voucher_date  ");
			sql.append("  order by  voucher_date  ");
			sql.append("   ");
			return sql.toString();
		}
	//采购单报表
	@Override
	public List<Map<String, Object>> searchPurchOrderReport(Map<String, Object> purchMap)throws Exception{
		String startDate=(String) purchMap.get("start_date");
		String endDate=(String)purchMap.get("end_date").toString();
		String timeinterval=(String) purchMap.get("time_interval");
		String refId=(String) purchMap.get("ref_id");		
		StringBuffer sql=new StringBuffer();//sql
		List<String> params=new ArrayList<String>();
		params.add(refId);
		params.add(startDate);
		params.add(endDate);
		
		if( StringUtil.isBlank(timeinterval) || timeinterval.equals("day") ){//日
			sql.append(this.selectDayPurchOrderReportSql);
		}
		else if(timeinterval.equals("annual")){//年
			sql.append(this.selectYearPurchOrderReportSql);			
		}else if(timeinterval.equals("monthly")){//月
			sql.append(this.selectMonthPurchOrderReportSql);
		}
		return selectBySqlQuery(sql.toString(), params.toArray());
	}
	
	
	public static final String getPurchOrderList = getPurchOrderList();
	private static String getPurchOrderList() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ORDER_ID, MERCH_ID, ORDER_DATE, ORDER_TIME, STATUS, PMT_STATUS, QTY_PURCH_TOTAL, AMT_PURCH_TOTAL, SUPPLIER_ID, VOUCHER_DATE,");
		sb.append(" OPERATOR FROM PURCH_ORDER WHERE MERCH_ID = ?");
		return sb.toString();
	}
	
	@Override
	public List<Map<String, Object>> getPurchOrderList(
			Map<String, Object> Param) throws Exception {
		StringBuffer sqlBuffer = new StringBuffer(getPurchOrderList);
		
		// 排序
		StringBuffer orderByBuffer = new StringBuffer();
		orderByBuffer.append(" VOUCHER_DATE DESC,ORDER_TIME DESC"); 
		orderByBuffer.insert(0, " ORDER BY");
		
		// 分页
		Integer pageIndex = 1;
		if(Param.get("page_index")!=null) {
			pageIndex = Integer.parseInt(Param.get("page_index").toString());
		}
		Integer pageSize = 20;
		if(Param.get("page_size")!=null) {
			pageSize = Integer.parseInt(Param.get("page_size").toString());
		}
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		paramArray.add(Param.get("merch_id"));
		if(Param.containsKey("date_begin")) {
			sqlBuffer.append(" AND VOUCHER_DATE >= ?");
			paramArray.add(Param.get("date_begin"));
		}
		if(Param.containsKey("date_end")) {
			sqlBuffer.append(" AND VOUCHER_DATE <= ?");
			paramArray.add(Param.get("date_end"));
		}
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.append(orderByBuffer.toString()).toString(), pageIndex, pageSize, paramArray.toArray());
		Param.put("page_count", pageResult.getPageSum());
		Param.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	@Override
	public List<Map<String, Object>> getPurchOrderDetail(
			Map<String, Object> purchOrderParam) throws Exception {
		String sql="SELECT ORDER_ID,MERCH_ID,ITEM_ID,PRI,QTY_REQ,QTY_ORD,QTY_LMT,AMT_ORD,ITEM_NAME,ITEM_BAR,BIG_BAR,UNIT_RATIO,UNIT_NAME,BIG_UNIT_NAME,BIG_PRI FROM　PURCH_ORDER_LINE WHERE　ORDER_ID = ? AND MERCH_ID = ? ";
		String order_id="";
		String merch_id="";
		if(purchOrderParam.containsKey("order_id")){
			order_id=purchOrderParam.get("order_id").toString();
		}
		if(purchOrderParam.containsKey("merch_id")){
			merch_id=purchOrderParam.get("merch_id").toString();
		}
		return this.selectBySqlQuery(sql, new Object[]{order_id,merch_id});
	}
	
	
}
