package com.ryx.social.consumer.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.social.consumer.dao.IConsumerSaleDao;

@Repository("consumerSaleDao")
public class ConsumerSaleDaoImpl extends BaseDaoImpl implements IConsumerSaleDao {
	
	Logger logger = LoggerFactory.getLogger(ConsumerSaleDaoImpl.class);
	
	@Override
	public List<Map<String, Object>> selectMerchSalePromotion(Map<String, Object> paramMap) throws Exception {
		logger.debug("MerchSalePromotionDaoImpl seMerchSalePromoton paramMap:"+paramMap);
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT PROMOTION_ID, a.MERCH_ID, CREATE_DATE, CREATE_TIME, PROMOTION_DESCRIPTION, PROMOTION_START_DATE, PROMOTION_END_DATE,");
		sqlBuffer.append(" CASE WHEN FILE_ID IS NULL THEN MERCH_FILE_ID ELSE FILE_ID END FILE_ID");
		sqlBuffer.append(" FROM MERCH_SALE_PROMOTION a, (SELECT MERCH_ID , MAX(FILE_ID) MERCH_FILE_ID FROM MERCH_FILE WHERE FILE_PURPOSE='01' GROUP BY MERCH_ID) b");
		sqlBuffer.append(" WHERE a.MERCH_ID = b.MERCH_ID(+)");
		List<Object> paramList = new ArrayList<Object>();
		if(paramMap.get("promotion_id")!=null) {
			sqlBuffer.append(" AND PROMOTION_ID=?");
			paramList.add(paramMap.get("promotion_id"));
		}
		if(paramMap.get("merch_id")!=null) {
			sqlBuffer.append(" AND a.MERCH_ID=?");
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
		Integer pageIndex = 1;
		if(paramMap.get("page_index")!=null) {
			pageIndex = Integer.parseInt(paramMap.get("page_index").toString());
		}
		Integer pageSize = 20;
		if(paramMap.get("page_size")!=null) {
			pageSize = Integer.parseInt(paramMap.get("page_size").toString());
		}
		sqlBuffer.append(" ORDER BY CREATE_DATE DESC, CREATE_TIME DESC");
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.toString(), pageIndex, pageSize, paramList.toArray());
		paramMap.put("page_count", pageResult.getPageSum());
		paramMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	
	/**
	 * 插入销售单
	 */
	private static String insertConsumerSaleOrderSql = initInsertConsumerSaleOrderSql();
	private static String initInsertConsumerSaleOrderSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO SALE_ORDER (ORDER_ID, CONSUMER_ID, MERCH_ID, ORDER_TYPE, ORDER_DATE, ORDER_TIME,");
		sb.append(" STATUS, PMT_STATUS, QTY_ORD_TOTAL, AMTYS_ORD_TOTAL, AMT_ORD_TOTAL, NOTE)");
		sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	public void insertConsumerSaleOrder(Map<String, Object> saleOrderParam)throws Exception{
		String orderId = (String) saleOrderParam.get("order_id");
		String consumerId = (String) saleOrderParam.get("consumer_id");
		String merchId = (String) saleOrderParam.get("merch_id");
		String orderType = (String) saleOrderParam.get("order_type");
		String orderDate = (String) saleOrderParam.get("order_date");
		String orderTime = (String) saleOrderParam.get("order_time");
		String status = (String) saleOrderParam.get("status");
		String pmtStatus = (String) saleOrderParam.get("pmt_status");
		BigDecimal qtyOrdTotal = new BigDecimal(saleOrderParam.get("qty_ord_total").toString());
		BigDecimal amtysOrdTotal = new BigDecimal(saleOrderParam.get("amtys_ord_total").toString());
		BigDecimal amtOrdTotal = new BigDecimal(saleOrderParam.get("amt_ord_total").toString());
		String note = (String) saleOrderParam.get("note");
		this.executeSQL(insertConsumerSaleOrderSql,
				new Object[] {orderId, consumerId, merchId, orderType, orderDate, orderTime, 
					status, pmtStatus, qtyOrdTotal, amtysOrdTotal, amtOrdTotal, note});
	}
	/**
	 * 插入销售单行
	 */
	private static final String inserConsumertSaleOrderLineSql = initInsertConsumerSaleOrderLineSql();
	private static String initInsertConsumerSaleOrderLineSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO SALE_ORDER_LINE (ORDER_ID, LINE_NUM, ITEM_ID, ITEM_NAME, UNIT_NAME, PRI3, DISCOUNT,");
		sb.append(" QTY_ORD, AMT_ORD, NOTE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	public void insertConsumerSaleOrderLine(Map<String, Object> saleOrderParam) throws Exception {
		String orderId = (String) saleOrderParam.get("order_id");
		List<Map<String, Object>> saleOrderUnitList = (List<Map<String, Object>>) saleOrderParam.get("list");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		int i=1;
		for(Map<String, Object> saleOrderUnit : saleOrderUnitList) {
			Object[] objectArray = new Object[10];
			objectArray[0] = orderId;
			if(saleOrderUnit.get("line_label")!=null){
				objectArray[1] = saleOrderUnit.get("line_label");
			}else{
				objectArray[1] = i+"";
				i++;
			}
			objectArray[2] = saleOrderUnit.get("item_id");
			objectArray[3] = saleOrderUnit.get("item_name");
			objectArray[4] = saleOrderUnit.get("unit_name");
			objectArray[5] = saleOrderUnit.get("pri3");
			objectArray[6] = saleOrderUnit.get("discount");
			objectArray[7] = saleOrderUnit.get("qty_ord");
			objectArray[8] = saleOrderUnit.get("amt_ord");
			String note = (String) saleOrderUnit.get("note");
			if(note!=null) {
				objectArray[9] = saleOrderUnit.get("note");
			} else {
				objectArray[9] = "";
//				objectArray[9] = null;
			}
			paramArray.add(objectArray);
			
		}
		this.executeBatchSQL(inserConsumertSaleOrderLineSql, paramArray);
	}
	/**
	 * 提交销售单
	 */
	@Override
	public void submitSaleOrder(Map<String, Object> saleOrderParam) throws Exception {
		//插入销售单		
		insertConsumerSaleOrder(saleOrderParam);
		//插入销售单行
		insertConsumerSaleOrderLine(saleOrderParam);
	}
	
	//查询销售订单行
	public static final String selectConsumerSaleOrderLineSql=getConsumerSaleOrderLineSql();
	private static String getConsumerSaleOrderLineSql (){
		StringBuffer sql=new StringBuffer();
		sql.append("  select order_id,line_num,item_id,pri3,discount,qty_ord,amt_ord,note,item_name,unit_name  ");
		sql.append("  from sale_order_line ");
		sql.append("  where 1=1 ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> selectConsumerSaleOrderLine(Map<String,Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(selectConsumerSaleOrderLineSql);
		List<String> paramsList=new ArrayList<String>();
		String order_id=(String) saleMap.get("order_id");		
		if(order_id!=null){
			sql.append(" and order_id=? ");
			paramsList.add(order_id);
		}
		
		return selectBySqlQuery(sql.toString(),paramsList.toArray());
	}
	//查询销售单
	public static final String selectConsumerSaleOrderSql=getConsumerSaleOrderSql();
	private static String getConsumerSaleOrderSql (){
		StringBuffer sql=new StringBuffer();
		sql.append(" select order_id,consumer_id,merch_id,order_type,order_date,status,pmt_status,qty_ord_total,  ");
		sql.append(" amtys_ord_total, amt_ord_total,note,order_time ");
		sql.append(" from sale_order  ");
		sql.append(" where 1=1   ");
		sql.append("   ");
		sql.append("   ");
		sql.append("  ");
		return sql.toString();
	}
	
	@Override
	public List<Map<String, Object>> selectConsumerSaleOrder(Map<String, Object> saleMap)throws Exception{			
		StringBuffer sql=new StringBuffer(selectConsumerSaleOrderSql);
		List<Object> parameterList=new ArrayList<Object>();
		
		String order_id=(String) saleMap.get("order_id");	
		String merch_id=(String) saleMap.get("merch_id");
		String startDate=(String) saleMap.get("start_date");
		String endDate=(String) saleMap.get("end_date");		
		if(merch_id!=null){
			sql.append(" and merch_id=? ");
			parameterList.add(merch_id);
		}
		if(order_id!=null){
			sql.append(" and order_id=? ");
			parameterList.add(order_id);
		}
		if(startDate!=null&&endDate!=null){
			sql.append("  and order_date between ? and ? ");
			parameterList.add(startDate);
			parameterList.add(endDate);			
		}
		sql.append("  order by order_date+order_time   ");
		return selectBySqlQuery(sql.toString(),parameterList.toArray());
	}
}
