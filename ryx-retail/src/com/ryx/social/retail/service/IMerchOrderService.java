package com.ryx.social.retail.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IMerchOrderService {
	
	/**
	 * 提交订单
	 * @param merchID 商户ID
	 * @param shopCardIDS 购物车中提交订单的IDS
	 * @throws SQLException 
	 */
	public List<Map<String,String>> insertOrder(String merchID,String cardIDS,String addressID) throws SQLException;
	/**
	 * 获取零售户订单
	 * @param merchID 零售户ID
	 * @param pageNum 页数
	 * @param pageSize 每页条数
	 * @throws SQLException 
	 */
	public  Map<String,Object>  findOrderList(String merchID, String orderID,String startDate, String endDate, String status, int pageNum, int pageSize,String searchParam) throws SQLException;
	/**
	 * 获取订单的相信信息
	 * @param orderID 订单ID
	 * @return 
	 * @throws SQLException 
	 */
	public List<Map<String,String>> findOrderDetail(String orderID) throws SQLException;
	
	/**
	 * 更新订单状态
	 * @param orderID 订单ID
	 * @param orderStatus 订单状态
	 * @param updateUser 更新人嗯
	 * @param payStatus 支付状态
	 * @throws SQLException 
	 */
	public void updateOrder(String orderID,String orderStatus,String updateUser,String payStatus) throws SQLException;
	
	/**
	 * 获取当前购物车提交的订单信息
	 * @param list 订单信息   key:orderID,key:supplierID
	 * @param merchID 用户ID
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> getOrderList(List<Map<String,String>> list,String merchID) throws SQLException;
	/**
	 * 获取零售户历史订单信息
	 * @param map
	 * @return
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 */
	public Map<String,Object>  getOrderListHis(Map<String,String> map) throws NumberFormatException, SQLException;
	
	/**
	 * 获取某个订单的详细状态
	 * @param orderID
	 * @param merchID
	 * @return
	 * @throws SQLException 
	 */
	public Map<String,Object>  getOrderInfo(String orderID,String merchID,String supplierID) throws SQLException;
	/**
	 * POS终端提交的订单接口
	 * @param list
	 * @throws SQLException 
	 */
	public void orderSubmitForPos(Map<String,Object> param) throws SQLException;

}
