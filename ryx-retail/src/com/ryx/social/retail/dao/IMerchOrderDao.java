package com.ryx.social.retail.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IMerchOrderDao {
	/**
	 * 添加订单列表
	 * @param orders
	 * @throws SQLException 
	 */
	public void  insertOrder(List<Map<String,String>> orders,String cardIDS) throws SQLException;
	
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
	public Map<String,String> findOrderDetail(String orderID) throws SQLException;
	/**
	 * 更新订单状态
	 * @param orderID 订单ID
	 * @param orderStatus 订单状态
	 * @param updateDate 更新时间
	 * @param updateUser 更新人嗯
	 * @param payStatus 支付状态
	 * @param payDate 支付时间
	 * @throws SQLException 
	 */
	public void updateOrder(String orderID,String orderStatus,String updateDate,String updateUser,String payStatus,String payDate) throws SQLException;
	
	
	/**
	 * 根据订单ID批量获取订单列表
	 * @param orderIDS 订单ID列表及其子订单的信息
	 * @throws SQLException 
	 */
	public List<Map<String,String>> getOrderList(String orderIDS,String merchID) throws SQLException;
	
	/**
	 * 获取零售户点单表列表
	 * @param orderIDS
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,String>> getOrderListNoDetail(String orderIDS,String merchID) throws SQLException;
	
	/**
	 * POS订单入库主表
	 * @param map
	 * @throws SQLException 
	 */
	public void submitOrderForIpos(List<Map<String,String>> orders,List<Map<String,Object>> orderDetails) throws SQLException;
	
	
}
