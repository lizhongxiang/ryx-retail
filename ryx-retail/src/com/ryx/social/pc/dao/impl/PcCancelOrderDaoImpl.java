package com.ryx.social.pc.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.pc.dao.IPcCancelOrderDao;
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.dao.impl.ItemDaoImpl;

@Repository
public class PcCancelOrderDaoImpl extends BaseDaoImpl implements IPcCancelOrderDao  {
	private Logger logger = LoggerFactory.getLogger(PcCancelOrderDaoImpl.class);	 
	//删除采购单
	private static final String delPuchOrderByIdSql=deletePuchOrderByIdSql();
	private static String deletePuchOrderByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" delete from PURCH_ORDER where ORDER_ID=?  ");
		return sql.toString();
	}
	@Override
	public void deletePuchOrder(Map<String , Object> puchOrderMap)throws Exception{
		StringBuffer sql=new StringBuffer(delPuchOrderByIdSql);
		List<Object>list=new ArrayList<Object>();
		String orderId=(String) puchOrderMap.get("order_id"); 
		list.add(orderId); 
		this.executeSQL(sql.toString(), list.toArray());
	}
	//删除采购单行
	private static final String delPuchOrderLineByIdSql=deletePuchOrderLineByIdSql();
	private static String deletePuchOrderLineByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" delete from PURCH_ORDER_LINE where ORDER_ID=?  ");
		return sql.toString();
	}
	@Override
	public void deletePuchOrderLine(Map<String , Object> puchOrderMap)throws Exception{
		StringBuffer sql=new StringBuffer(delPuchOrderLineByIdSql);
		List<Object>list=new ArrayList<Object>();
		String orderId=(String) puchOrderMap.get("order_id"); 
		list.add(orderId); 
		this.executeSQL(sql.toString(), list.toArray());
	}
	//删除盘点单
	private static final String delWhseTurnByIdSql=deleteWhseTurnByIdSql();
	private static String deleteWhseTurnByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" delete from WHSE_TURN where TURN_ID=?  ");
		return sql.toString();
	}
	@Override
	public void deleteWhseTurn(Map<String , Object> whseTurnMap)throws Exception{
		StringBuffer sql=new StringBuffer(delWhseTurnByIdSql);
		List<Object>list=new ArrayList<Object>();
		String orderId=(String) whseTurnMap.get("turn_id"); 
		list.add(orderId); 
		this.executeSQL(sql.toString(), list.toArray());
	}
	//删除盘点单行
	private static final String delWhseTurnLineByIdSql=deleteWhseTurnLineByIdSql();
	private static String deleteWhseTurnLineByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" delete from WHSE_TURN_LINE where TURN_ID=?  ");
		return sql.toString();
	}
	@Override
	public void deleteWhseTurnLine(Map<String , Object> whseTurnMap)throws Exception{
		StringBuffer sql=new StringBuffer(delWhseTurnLineByIdSql);
		List<Object>list=new ArrayList<Object>();
		String orderId=(String) whseTurnMap.get("turn_id"); 
		list.add(orderId); 
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//删除退货
	private static final String delReturnOrderByIdSql=deleteReturnOrderByIdSql();
	private static String deleteReturnOrderByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" delete from RETURN_ORDER where RETURN_ORDER_ID=?  ");
		return sql.toString();
	}
	@Override
	public void deleteReturnOrder(Map<String , Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(delReturnOrderByIdSql);
		List<Object>list=new ArrayList<Object>();
		String orderId=(String) saleMap.get("order_id"); 
		list.add(orderId); 
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	
	//删除销售单
	private static final String delSaleOrderByIdSql=deleteSaleOrderByIdSql();
	private static String deleteSaleOrderByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" delete from SALE_ORDER where order_id=?  ");
		return sql.toString();
	}
	@Override
	public void deleteSaleOrder(Map<String , Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(delSaleOrderByIdSql);
		List<Object>list=new ArrayList<Object>();
		String orderId=(String) saleMap.get("order_id"); 
		list.add(orderId); 
		this.executeSQL(sql.toString(), list.toArray());
	}
	//删除销售单行
	private static final String delSaleOrderLineByIdSql=deleteSaleOrderLineByIdSql();
	private static String deleteSaleOrderLineByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" delete from SALE_ORDER_LINE where order_id=?   ");
		return sql.toString();
	}
	@Override
	public void deleteSaleOrderLine(Map<String , Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(delSaleOrderLineByIdSql);
		List<Object>list=new ArrayList<Object>();
		String orderId=(String) saleMap.get("order_id"); 
		list.add(orderId); 
		this.executeSQL(sql.toString(), list.toArray());
	}
	
	//得到入库单
	private static final String getPuchOrderByIdSql=selectPuchOrderByIdSql();
	private static String selectPuchOrderByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select ORDER_ID,MERCH_ID,ORDER_DATE,ORDER_TIME, STATUS, PMT_STATUS, QTY_PURCH_TOTAL,AMT_PURCH_TOTAL,VOUCHER_DATE ");
		sql.append(" from PURCH_ORDER ");
		sql.append(" where ORDER_ID=? ");
		sql.append("   ");
		return sql.toString();
	}
	@Override
	public List<Map<String , Object>> searPuchOrderByIdSql(Map<String, Object> puchMap)throws Exception{
		StringBuffer sql=new StringBuffer(getPuchOrderByIdSql);
		List<Object> list=new ArrayList<Object>();
		String orderId=(String) puchMap.get("order_id");
		list.add(orderId);
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}	
	//得到入库单行
	private static final String getPuchOrderLineByIdSql=selectPuchOrderLineByIdSql();
	private static String selectPuchOrderLineByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select ORDER_ID,MERCH_ID,ITEM_ID,QTY_ORD ");
		sql.append(" from PURCH_ORDER_LINE ");
		sql.append(" where ORDER_ID=? ");
		sql.append("   ");
		return sql.toString();
	}
	@Override
	public List<Map<String , Object>> searPuchOrderLineByIdSql(Map<String, Object> puchMap)throws Exception{
		StringBuffer sql=new StringBuffer(getPuchOrderLineByIdSql);
		List<Object> list=new ArrayList<Object>();
		String orderId=(String) puchMap.get("order_id");
		list.add(orderId);
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	
	
	//得到盘点单行
	private static final String getWhseTurnLineByIdSql=selectWhseTurnLineByIdSql();
	private static String selectWhseTurnLineByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select TURN_ID,ITEM_ID,QTY_WHSE,QTY_TURN,QTY_PL,PL_REASON,NOTE,AMT_PL,MERCH_ID ");
		sql.append(" from WHSE_TURN_LINE ");
		sql.append(" where TURN_ID=? ");
		sql.append("   ");
		return sql.toString();
	}
	@Override
	public List<Map<String , Object>> searWhseTurnLineByIdSql(Map<String, Object> turnMap)throws Exception{
		StringBuffer sql=new StringBuffer(getWhseTurnLineByIdSql);
		List<Object> list=new ArrayList<Object>();
		String orderId=(String) turnMap.get("turn_id");
		list.add(orderId);
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	//得到盘点单
	private static final String getWhseTurnByIdSql=selectWhseTurnByIdSql();
	private static String selectWhseTurnByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select TURN_ID,MERCH_ID,TURN_DATE,STATUS,CRT_DATE, ");
		sql.append(" CRT_TIME,USER_ID,QTY_PROFIT,QTY_LOSS,AMT_PROFIT,AMT_LOSS ");
		sql.append(" from WHSE_TURN  ");
		sql.append(" where TURN_ID=?  ");
		sql.append("   ");
		return sql.toString();
	}
	@Override
	public List<Map<String , Object>> searWhseTurnByIdSql(Map<String, Object> turnMap)throws Exception{
		StringBuffer sql=new StringBuffer(getWhseTurnByIdSql);
		List<Object> list=new ArrayList<Object>();
		String orderId=(String) turnMap.get("turn_id");
		list.add(orderId);
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	
	//得到退货单
	private static final String getReturnOrderByIdSql=selectReturnOrderByIdSql();
	private static String selectReturnOrderByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select RETURN_ORDER_ID,ORDER_ID,RETURN_ORDER_DATE,RETURN_ORDER_TIME, ");
		sql.append(" RETURN_STATUS,QTY_RETURN_TOTAL,AMT_RETURN_TOTAL,OPERATOR,CONSUMER_ID, ");
		sql.append(" ORDER_TYPE,RETURN_PMT_STATUS,NOTE,QTY_RETURN_COUNT,MERCH_ID,AMT_RETURN_LOSS ");
		sql.append(" from RETURN_ORDER  ");
		sql.append(" where RETURN_ORDER_ID=? ");
		return sql.toString();
	}
	@Override
	public List<Map<String , Object>> searchReturnOrderById(Map<String, Object> returnMap)throws Exception{
		StringBuffer sql=new StringBuffer(getReturnOrderByIdSql);
		List<Object> list=new ArrayList<Object>();
		String orderId=(String) returnMap.get("order_id");
		list.add(orderId);
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	//得到销售单
	private static final String getSaleOrderByIdSql=selectSaleOrderByIdSql();
	private static String selectSaleOrderByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select ORDER_ID,CONSUMER_ID,MERCH_ID,ORDER_TYPE,ORDER_DATE,ORDER_TIME, ");
		sql.append(" STATUS,PMT_STATUS,QTY_ORD_TOTAL,AMTYS_ORD_TOTAL,AMT_ORD_TOTAL,NOTE, ");
		sql.append(" AMT_ORD_CHANGE,AMT_ORD_LOSS,AMT_ORD_PROFIT,OPERATOR,QTY_ORD_COUNT,PAY_TYPE,CERTIFICATE_ID ");
		sql.append(" PAY_TYPE,CERTIFICATE_ID ");
		sql.append(" from SALE_ORDER ");
		sql.append(" where ORDER_ID=?  ");
		sql.append("  ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> searchSaleOrderById(Map<String, Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(getSaleOrderByIdSql);
		List<Object> list=new ArrayList<Object>();
		String orderId=(String) saleMap.get("order_id"); 
		list.add(orderId); 
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	
	
	//得到销售单行
	private static final String getSaleOrderLineByIdSql=selectSaleOrderLineByIdSql();
	private static String selectSaleOrderLineByIdSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select ORDER_ID,LINE_NUM,ITEM_ID,PRI3,DISCOUNT,QTY_ORD,AMT_ORD,NOTE,ITEM_NAME, ");
		sql.append(" UNIT_NAME,PROFIT,MERCH_ID,ITEM_BAR,BIG_BAR,BIG_UNIT_NAME,UNIT_RATIO,BIG_PRI3,COST ");
		sql.append(" from SALE_ORDER_LINE ");
		sql.append(" where ORDER_ID=? ");
		sql.append("  ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> searchSaleOrderLineById(Map<String, Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(getSaleOrderLineByIdSql);
		List<Object> list=new ArrayList<Object>();
		String orderId=(String) saleMap.get("order_id");
		list.add(orderId); 
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	//得到此后的盘点单
	private static final String getWhseOrderByTimeSql=selectWhseOrderByTimeSql();
	private static  String selectWhseOrderByTimeSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select  TURN_ID,MERCH_ID,TURN_DATE,STATUS,CRT_DATE,CRT_TIME,USER_ID,NOTE,QTY_PROFIT,QTY_LOSS,AMT_PROFIT,AMT_LOSS ");
		sql.append(" from WHSE_TURN ");
		sql.append(" where 1=1 ");
		sql.append(" and MERCH_ID= ? ");
		sql.append(" and CRT_DATE||CRT_TIME> ?  ");//'20140421110414'
		sql.append("  ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> searchWhseOrderByTime(Map<String, Object> whseMap) throws Exception{
		StringBuffer sql=new StringBuffer(getWhseOrderByTimeSql);
		List<Object> list=new ArrayList<Object>();
		String merchId=(String) whseMap.get("merch_id");
		String newDate=(String) whseMap.get("crt_date");
		String newTime=(String) whseMap.get("crt_time");
		list.add(merchId);
		list.add(newDate+newTime);
		return this.selectBySqlQuery(sql.toString(),list.toArray());
	}
	
	//更新库存
	private static final String reviseWhseMerchQtyWhseSql=updateWhseMerchQtyWhseSql();
	private static String updateWhseMerchQtyWhseSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" update WHSE_MERCH set QTY_WHSE=QTY_WHSE+? where MERCH_ID =? and ITEM_ID=? ");
		
		return sql.toString();
	}
	@Override
	public void updateWhseMerchQtyWhse(List<Map<String, Object>> whseList)throws Exception{
		if (whseList==null || whseList.isEmpty()) {
			return ;
		}
		StringBuffer sql=new StringBuffer(reviseWhseMerchQtyWhseSql);
		List<Object> list=new ArrayList<Object>();
		for (Map<String, Object> map : whseList) {
			String merchId=(String) map.get("merch_id");
			Object qtyWhse= map.get("qty_whse");
			String itemId=(String) map.get("item_id");
			Object[] objArr={qtyWhse,merchId,itemId};
			list.add(objArr);
		}
//		this.executeSQL(sql.toString(), list.toArray());
		this.executeBatchSQL(sql.toString(), list );
	}
	 
}
