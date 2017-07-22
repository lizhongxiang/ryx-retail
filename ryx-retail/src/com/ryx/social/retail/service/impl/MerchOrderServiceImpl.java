package com.ryx.social.retail.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IMerchOrderDao;
import com.ryx.social.retail.dao.IMerchShoppingCartDao;
import com.ryx.social.retail.dao.ISupplierInfoDao;
import com.ryx.social.retail.dao.ISupplierItemDao;
import com.ryx.social.retail.service.IMerchOrderService;

@Service
public class MerchOrderServiceImpl implements IMerchOrderService {

	@Resource
	private IMerchShoppingCartDao merchShoppCartDaoImpl;
	@Resource
	private IMerchOrderDao merchOrderDaoImpl;
	@Resource
	private ISupplierInfoDao supplierInfoDaoImpl;
	@Resource
	private ISupplierItemDao supplierItemDaoImpl;
	
	@Override
	public List<Map<String,String>> insertOrder(String merchID, String cardIDS,String addressID) throws SQLException {
		cardIDS = this.FormatIDS(cardIDS);
		List<Map<String,Object>> list = merchShoppCartDaoImpl.getMerchCardGroup(merchID, cardIDS);
		List<Map<String,String>> orders = new ArrayList<Map<String,String>>();
		//返回当前供应商和订单编号的关系
		List<Map<String,String>> orderSucList = new ArrayList<Map<String,String>>();
 		for(Map<String,Object> map : list){
			//存储返回的数据，供查询订单使用
			Map<String,String> orderSucMap = new HashMap<String, String>();
			String orderID = IDUtil.getId();
			orderSucMap.put("orderID", orderID);
			orderSucMap.put("supplierID", map.get("SUPPLIER_ID").toString());
			orderSucList.add(orderSucMap);
			Map<String,String> order =  new HashMap<String, String>();
			order.put("id", orderID);
			order.put("supplierID", map.get("SUPPLIER_ID").toString());
			order.put("num", String.valueOf(map.get("TOTAL_NUM")));
			order.put("priseTotal", map.get("TOTAL_MONEY").toString());
			order.put("crateDate", DateUtil.getCurrentTime());
			order.put("orderStatus", "1");
			order.put("payStatus", "0");
			order.put("addressID", addressID);
			order.put("status", "1");
			order.put("merchID", merchID);
			orders.add(order);
		}
		//添加订单信息
		merchOrderDaoImpl.insertOrder(orders,cardIDS);
		//删除购物车中提交的订单
		merchShoppCartDaoImpl.deleteShoppingCard(cardIDS);
		return orderSucList;
		//
	}
	@Override
	public Map<String, Object> findOrderList(String merchID, String orderID,String startDate, String endDate, String status, int pageNum, int pageSize,
			String searchParam) throws SQLException {
		return merchOrderDaoImpl.findOrderList(merchID, orderID, startDate, endDate, status, pageNum, pageSize,searchParam);
	}
	@Override
	public List<Map<String, String>> findOrderDetail(String orderID) throws SQLException {
		//return merchOrderDaoImpl.findOrderDetail(orderID);
		return null;
	}
	@Override
	public void updateOrder(String orderID, String orderStatus, String updateUser, String payStatus) throws SQLException {
		String updateDate = DateUtil.currentDatetime();
		String payDate = "";
		if(payStatus != null && !payStatus.equals("")){
			payDate = DateUtil.currentDatetime();
		}
		merchOrderDaoImpl.updateOrder(orderID, orderStatus, updateDate, updateUser, payStatus, payDate);
	}
	@Override
	public List<Map<String,Object>> getOrderList(List<Map<String,String>> list,String merchID) throws SQLException{
		//获取零售户信息列表
		StringBuilder supplierIDSb = new StringBuilder();
		for(Map<String,String> map : list){
			supplierIDSb.append(MapUtil.getString(map, "supplierID"));
			supplierIDSb.append(",");
		}
		String supplierIDS = this.FormatIDS(supplierIDSb.toString());
		List<Map<String,Object>> supplierList = supplierInfoDaoImpl.getSupplierInfoList(supplierIDS);
		//获取订单列表
		String orderIDS = "";
		for(Map<String,String> map : list){
			orderIDS = orderIDS + map.get("orderID")+",";
		}
		orderIDS = this.FormatIDS(orderIDS);
		//组装商户和订单关系
		List<Map<String, String>> orderListNoDetail = merchOrderDaoImpl.getOrderListNoDetail(orderIDS, merchID);
		for(int i=0;i<supplierList.size();i++){
			String supplierID = supplierList.get(i).get("ID").toString();
			for(int j=0 ; j<orderListNoDetail.size();j++){
				if(orderListNoDetail.get(j).get("SUPPLIER_ID").equals(supplierID)){
					supplierList.get(i).putAll(orderListNoDetail.get(j));
					break;
				}
			}
		}
		
		List<Map<String, String>> orderList = merchOrderDaoImpl.getOrderList(orderIDS, merchID);
		//组装商户和订单详细信息关系
		for(int i = 0 ; i < supplierList.size() ; i++){
			String supplierID = supplierList.get(i).get("SUPPLIER_ID").toString();
			List<Map<String,String>> aSupplierOrder = new ArrayList<Map<String,String>>();
			for(int j = 0 ; j < orderList.size() ; j++){
				if(orderList.get(j).get("SUPPLIER_ID").equals(supplierID)){
					aSupplierOrder.add(orderList.get(j));
					continue;
				}
			}
			supplierList.get(i).put("orders", aSupplierOrder);
		}
		return supplierList;
	}
	
	
	@Override
	public Map<String,Object>  getOrderListHis(Map<String, String> map) throws NumberFormatException, SQLException {
		String merchID = map.get("merchID");
		String orderID = map.get("orderID");
		String startDate = map.get("startDate");
		String endDate = map.get("endDate");
		String status = map.get("status");
		String pageNum = map.get("pageNum");
		String pageSize = map.get("pageSize");
		String searchParam = map.get("searchParam");
		return merchOrderDaoImpl.findOrderList(merchID, orderID, startDate, endDate, status, Integer.valueOf(pageNum), Integer.valueOf(pageSize),searchParam);
	}
	
	@Override
	public Map<String,Object>  getOrderInfo(String orderID, String merchID, String supplierID) throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> map = new HashMap<String, String>();
		map.put("supplierID", supplierID);
		map.put("orderID", orderID);
		list.add(map);
		return this.getOrderList(list, merchID).get(0);
	}
	/***
	 * author:隋长国
	 */
	@Override
	public void orderSubmitForPos(Map<String,Object> param) throws SQLException {
		String addressID = (String) param.get("addressID");
		String merchID = (String)param.get("merchID");
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> orderInfo = (List<Map<String, Object>>) param.get("orders");
		Map<String,String> supplierItemMap = new HashMap<String, String>();
		for(Map<String,Object> supplierMap : orderInfo){
			String supplierID = (String) supplierMap.get("supplierID");
			@SuppressWarnings("unchecked")
			List<Map<String,String>> itemsList = (List<Map<String, String>>) supplierMap.get("items");
			StringBuilder itemIDS = new StringBuilder();
			for(Map<String,String> item : itemsList){
				itemIDS.append(MapUtil.getString(item, "itemID"));
				itemIDS.append(",");
				
			}
			supplierItemMap.put(supplierID, FormatIDS(itemIDS.toString()));
		}
		//用于订单详情表入库
		List<Map<String,Object>> itemList = supplierItemDaoImpl.getItemListBySupplierIDAndItemID(supplierItemMap);
		//用于订单入库
		List<Map<String,String>> orderInList = new ArrayList<Map<String,String>>();
		for(Map<String,Object> supplierMap : orderInfo){
			String supplierID = (String) supplierMap.get("supplierID");
			String orderID = IDUtil.getId();
			int supplierNumTotal = 0 ;
			float supplierPriceTotal = 0;
			@SuppressWarnings("unchecked")
			List<Map<String,String>> itemsList = (List<Map<String, String>>) supplierMap.get("items");
			for(Map<String,String> orderItem : itemsList){
				String itemID = orderItem.get("itemID");
				String num = orderItem.get("num");
				for(Map<String,Object> item : itemList){
					String supplierID_ = String.valueOf(item.get("SUPPLIER_ID"));
					String itemID_ = String.valueOf(item.get("ITEM_ID"));
					String wholePrice = String.valueOf(item.get("WHOLE_PRISE").toString());
					if(supplierID.equals(supplierID_) && itemID.equals(itemID_)){
						supplierNumTotal = supplierNumTotal + Integer.valueOf(num);
						supplierPriceTotal = supplierPriceTotal + Integer.valueOf(num)*Float.valueOf(wholePrice);
						item.put("num", num);
						item.put("orderID", orderID);
						item.put("merchID", merchID);
					}
				}
				
			}
			Map<String,String> order = new HashMap<String, String>();
			order.put("id", orderID);
			order.put("supplierID", supplierID);
			order.put("num", String.valueOf(supplierNumTotal));
			order.put("priseTotal", String.valueOf(supplierPriceTotal));
			order.put("crateDate", DateUtil.getCurrentTime());
			order.put("orderStatus", "1");
			order.put("payStatus", "0");
			order.put("addressID", addressID);
			order.put("status", "1");
			order.put("merchID", merchID);
			orderInList.add(order);
		}
		merchOrderDaoImpl.submitOrderForIpos(orderInList, itemList);
		
		
	}
//	public static void main(String[] args) {
//		String a = "";
//		System.out.println(a=="");
//	}
	private String FormatIDS(String IDS){
		String [] idsAry =IDS.split(",");
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i<idsAry.length;i++){
			if(!idsAry[i].equals("")){
				sb.append("'"+idsAry[i]+"'");
				if(i+1<idsAry.length){
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}
}
