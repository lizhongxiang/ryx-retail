package com.ryx.social.retail.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.IDUtil;
import com.ryx.social.retail.dao.IMerchOrderDao;

@Repository
public class MerchOrderDaoImpl extends BaseDaoImpl implements IMerchOrderDao {

	@Override
	public void insertOrder(List<Map<String,String>> orders,String cardIDS) throws SQLException {
		StringBuilder sql = new StringBuilder("insert into SUPPLIER_MERCH_ORDER ");
		sql.append("(ID,SUPPLIER_ID,MERCH_ID,NUM,PRISE_TOTAL,CREATE_DATE,ORDER_STATUS,PAY_STATUS,ADDRESS_ID,STATUS)");
		sql.append(" values ");
		sql.append("(?,?,?,?,?,?,?,?,?,?)");
		List<Object[]> totalList = new ArrayList<Object[]>();
		List<String> detailSQL = new ArrayList<String>();
		for(Map<String,String> order : orders){
			Object[] obj= new Object[10];
			obj[0] = order.get("id");
			obj[1] = order.get("supplierID");
			obj[2] = order.get("merchID");
			obj[3] = order.get("num");
			obj[4] = order.get("priseTotal");
			obj[5] = order.get("crateDate");
			obj[6] = order.get("orderStatus");
			obj[7] = order.get("payStatus");
			obj[8] = order.get("addressID");
			obj[9] = order.get("status");
			totalList.add(obj);
			detailSQL.add(this.createInsertOrderDetail(order.get("id"),order.get("merchID"),order.get("supplierID"),cardIDS));
		}
		//添加订单主表
		this.executeBatchSQL(sql.toString(), totalList);
		//添加订单子表
		this.executeSQL(detailSQL);
	}
	/**
	 * 生成订单详情表的sql
	 * @param orderID 订单ID
 	 * @param supplierID 供应商ID
	 * @param itemIDS 
	 * @return
	 */
	private String createInsertOrderDetail(String orderID,String merchID,String supplierID,String cardIDS){
		/**
		 * 序列SQL
		 * 
			CREATE SEQUENCE order_detail_id  --序列名
			INCREMENT BY 1   -- 每次加几个  
			START WITH 1       -- 从1开始计数  
			NOMAXVALUE        -- 不设置最大值  
			NOCYCLE               -- 一直累加，不循环  
			CACHE 10;
			select  order_detail_id.nextval ,'orderID',si.id ,si.item_bar,si.item_name ,si.item_unit_name,si.whole_prise  ,si.retail_prise , smsc.num 
		      from SUPPLIER_MERCH_SHOPPING_CARD smsc
		      left join SUPPLIER_ITEM si
		        on smsc.item_id = si.id
		       and smsc.MERCHR_ID = ''
		       and smsc.ID in ('')
		       and smsc.STATUS = '1'
		       and smsc.merchr_id = ''
      
		 */
		StringBuilder d = new StringBuilder();
		d.append("insert into SUPPLIER_MERCH_ORDER_DETAIL ");
		d.append("(id, ORDER_ID,SUPPLIER_ITEM_ID,ITEM_BAR,ITEM_NAME,ITEM_UNIT_NAME, WHOLE_PRISE, RETAIL_PRISE,NUM) ");
		d.append("select  order_detail_id.nextval ,"+orderID+",si.id ,si.item_bar,si.item_name ,si.item_unit_name,si.whole_prise  ,si.retail_prise , smsc.num ");
		d.append("from SUPPLIER_MERCH_SHOPPING_CARD smsc ");
		d.append("join SUPPLIER_ITEM si ");
		d.append("on smsc.item_id = si.id ");
		d.append("and smsc.MERCHR_ID = '"+merchID+"' ");
		d.append("and smsc.ID in ("+cardIDS+") ");
		d.append("and smsc.STATUS = '1' ");
		d.append("and smsc.supplier_id = '"+supplierID+"' ");
		return d.toString();
	}
	@Override
	public Map<String,Object> findOrderList(String merchID, String orderID,String startDate, String endDate, String status, int pageNum, int pageSize,String searchParam) throws SQLException {
		/*    
    select smo_.*, mra.name revNam
  from SUPPLIER_MERCH_ORDER smo_
  join (select smo.id  from SUPPLIER_MERCH_ORDER smo   join SUPPLIER_MERCH_ORDER_DETAIL smod
            on smo.id = smod.order_id  and smo.merch_id = '1037010500764'
             and (smod.item_bar like '%%' or smod.item_name like '%%')
         group by smo.id
        union
        select smo.id from SUPPLIER_MERCH_ORDER smo join SUPPILER s
           on smo.supplier_id = s.id and smo.merch_id = '1037010500764'
           and s.supplier_name like '%%'
         group by smo.id
         
         ) aaaaa
    on smo_.id = aaaaa.id
   and smo_.create_date < 1
  left join merch_receive_address mra
    on smo_.id = mra.merch_id
 order by create_date

    
*/    
		StringBuilder sql = new StringBuilder();
		sql.append("select smo_.*, mra.name revNam ,supp.supplier_name ");
		sql.append("from SUPPLIER_MERCH_ORDER smo_ ");
		sql.append("join (");
		sql.append("select smo.id  from SUPPLIER_MERCH_ORDER smo  join SUPPLIER_MERCH_ORDER_DETAIL smod ");
		sql.append("on smo.id = smod.order_id  and smo.merch_id = '"+merchID+"' ");
		if(searchParam != null && !searchParam.equals("")){
			sql.append("and (smod.item_bar like '%"+searchParam+"%' or smod.item_name like '%"+searchParam+"%') ");
		}
		sql.append("group by smo.id ");
		sql.append("union ");
		sql.append("select smo.id from SUPPLIER_MERCH_ORDER smo join SUPPILER s ");
		sql.append("on smo.supplier_id = s.id and smo.merch_id = '"+merchID+"' ");
		if(searchParam != null && !searchParam.equals("")){
			sql.append("and s.supplier_name like '%"+searchParam+"%' ");
		}
		sql.append("group by smo.id ");
		sql.append(") searchResult ");
		sql.append("on smo_.id = searchResult.id ");
		if(status != null && !status.equals("")){
			sql.append("and smo_.order_status = '"+status+"' ");
		}
		if((startDate != null && !startDate.equals("")) && (endDate != null && !endDate.equals(""))){
			sql.append("and (smo_.CREATE_DATE > '"+startDate+"' and smo_.CREATE_DATE <= '"+endDate+"') ");
		}
		else if((startDate == null || startDate.equals("")) && (endDate != null && !endDate.equals(""))){
			sql.append("and smo_.CREATE_DATE >= '"+startDate+"' ");
		}
		else if((startDate != null && !startDate.equals("")) && (endDate == null || endDate.equals(""))){
			sql.append("and smo_.CREATE_DATE <= '"+endDate+"' ");
		}
		sql.append("left join merch_receive_address mra ");
		sql.append("on smo_.id = mra.merch_id ");
		sql.append("left join SUPPILER supp ");
		sql.append("on smo_.supplier_id = supp.id ");
		sql.append("order by create_date ");
		
		Page page = this.searchPaginatedBySql(sql.toString(), pageNum, pageSize);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("total", page.getTotal());
		map.put("page_sum", page.getPageSum());
		map.put("rows", page.getRows());
		return map;
	}
	@Override
	public Map<String,String> findOrderDetail(String orderID) throws SQLException {
		String sql = "select * from SUPPLIER_MERCH_ORDER_DETAIL where ORDER_ID = '"+orderID+"'";
		@SuppressWarnings("unchecked")
		List<Map<String, String>> list = this.selectBySqlQuery(sql);
		return list.get(0) ;
	}
	@Override
	public void updateOrder(String orderID, String orderStatus, String updateDate, String updateUser, String payStatus, String payDate) throws SQLException {
		StringBuffer sql = new StringBuffer("update SUPPLIER_MERCH_ORDER set SUPPLIER_ID = '"+orderID+"'");
		if(orderStatus != null && !orderStatus.equals("")){
			sql.append(",ORDER_STATUS = '"+orderStatus+"'");
		}
		if(updateDate != null && !updateDate.equals("")){
			sql.append(",UPDATE_DATE = '"+updateDate+"'");
		}
		if(updateUser != null && !updateUser.equals("")){
			sql.append(",UPDATE_USER = '"+updateUser+"'");
		}
		if(payStatus != null && !payStatus.equals("")){
			sql.append(",PAY_STATUS = '"+payStatus+"'");
		}
		if(payDate != null && !payDate.equals("")){
			sql.append(",PAY_DATE = '"+payDate+"'");
		}
		sql.append(" where ORDER_STATUS = '"+orderID+"'");
		this.executeSQL(sql.toString());
		
	}
	@Override
	public List<Map<String, String>> getOrderList(String orderIDS,String merchID) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select smo.SUPPLIER_ID , smod.* ");
		sql.append("from SUPPLIER_MERCH_ORDER smo ");
		sql.append("join SUPPLIER_MERCH_ORDER_DETAIL smod ");
		sql.append("on smo.id = smod.order_id ");
		sql.append("and smo.merch_id = '"+merchID+"' ");
		if(orderIDS != null && !orderIDS.equals("")){
			sql.append(" and smo.id in ("+orderIDS+")");
		}
		@SuppressWarnings("unchecked")
		List<Map<String,String>> list = this.selectBySqlQuery(sql.toString());
		return list;
	}
	@Override
	public List<Map<String, String>> getOrderListNoDetail(String orderIDS, String merchID) throws SQLException {

		StringBuilder sql = new StringBuilder();
		sql.append("select smo.*  ");
		sql.append("from SUPPLIER_MERCH_ORDER smo ");
		sql.append("where smo.merch_id = '"+merchID+"' ");
		if(orderIDS != null && !orderIDS.equals("")){
			sql.append(" and smo.id in ("+orderIDS+")");
		}
		@SuppressWarnings("unchecked")
		List<Map<String,String>> list = this.selectBySqlQuery(sql.toString());
		return list;
	}
	@Override
	public void submitOrderForIpos(List<Map<String, String>> orders, List<Map<String, Object>> orderDetails) throws SQLException {
		StringBuilder sqlOrder = new StringBuilder("insert into SUPPLIER_MERCH_ORDER ");
		sqlOrder.append("(ID,SUPPLIER_ID,MERCH_ID,NUM,PRISE_TOTAL,CREATE_DATE,ORDER_STATUS,PAY_STATUS,ADDRESS_ID,STATUS)");
		sqlOrder.append(" values ");
		sqlOrder.append("(?,?,?,?,?,?,?,?,?,?)");
		List<Object[]> orderList = new ArrayList<Object[]>();
		for(Map<String,String> order : orders){
			Object[] obj= new Object[10];
			obj[0] = order.get("id");
			obj[1] = order.get("supplierID");
			obj[2] = order.get("merchID");
			obj[3] = order.get("num");
			obj[4] = order.get("priseTotal");
			obj[5] = order.get("crateDate");
			obj[6] = "1";
			obj[7] = order.get("payStatus");
			obj[8] = order.get("addressID");
			obj[9] = "1";
			orderList.add(obj);
		}
		this.executeBatchSQL(sqlOrder.toString(), orderList);
		StringBuilder sqlOrderDetail = new StringBuilder("insert into SUPPLIER_MERCH_ORDER_DETAIL ");
		sqlOrderDetail.append("(ID,ORDER_ID,SUPPLIER_ITEM_ID,ITEM_BAR,ITEM_NAME,ITEM_UNIT_NAME,WHOLE_PRISE,RETAIL_PRISE,NUM)");
		sqlOrderDetail.append(" values ");
		sqlOrderDetail.append("(?,?,?,?,?,?,?,?,?)");
		List<Object[]> orderDetailList = new ArrayList<Object[]>();
		for(Map<String,Object> order : orderDetails){
			Object[] obj= new Object[9];
			obj[0] = IDUtil.getId();
			obj[1] = order.get("orderID");
			obj[2] = order.get("ITEM_ID");
			obj[3] = order.get("ITEM_BAR");
			obj[4] = order.get("ITEM_NAME");
			obj[5] = order.get("ITEM_UNIT_NAME");
			obj[6] = order.get("WHOLE_PRISE");
			obj[7] = order.get("RETAIL_PRISE");
			obj[8] = order.get("num");
			orderDetailList.add(obj);
		}
		this.executeBatchSQL(sqlOrderDetail.toString(), orderDetailList);
	}

}
