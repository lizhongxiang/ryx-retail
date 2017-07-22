package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IOrderDao {
	public List<Map<String, Object>> selectPurchOrder(Map<String, Object> purchOrderParam) throws Exception;
	public List<Map<String, Object>> selectPurchOrderLine(Map<String, Object> purchOrderLineParam) throws Exception;
	public List<Map<String, Object>> selectSaleOrder(Map<String, Object> saleOrderParam) throws Exception;
	public List<Map<String, Object>> selectSaleOrderLine(Map<String, Object> saleOrderLineParam) throws Exception;
	public List<Map<String, Object>> selectReturnOrder(Map<String, Object> returnOrderParam) throws Exception;
	// 可以查询某商品或者某类商品的销售额, 需要传key, merch_id, start_date, end_date
	public List<Map<String, Object>> searchSaleroom(Map<String, Object> saleroomParam) throws Exception;
	public List<Map<String, Object>> searchSaleOrderHistory(Map<String, Object> historyParam) throws Exception;
	public List<Map<String, Object>> searchPurchOrderHistory(Map<String, Object> historyParam) throws Exception;
	public List<Map<String, Object>> searchPurchOrderLineByItem(Map<String, Object> purchOrderLineParam) throws Exception;
	public List<Map<String, Object>> searchSaleOrderLineByItemId(Map<String, Object> saleOrderLineParam) throws Exception;
	/**
	 * 添加临时订购单
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @param dataMap
	 * @throws Exception
	 */
	public void insertTemporaryMerchOrder(Map<String,Object> dataMap)throws Exception;
	/**
	 * 查询指定商户指定时间段你的临时订单
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectTemporaryMerchOrder(Map<String,Object> dataMap)throws Exception;
	/**
	 * 修改零售户临时订单数据
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @param dataMap
	 * @throws Exception
	 */
	public void updateTemporaryMerchOrder(Map<String,Object> dataMap)throws Exception;
	/**
	 * 添加临时订购单行数据
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @param dataMapList
	 * @throws Exception
	 */
	public void insertTemporaryMerchOrderLine(List<Map<String,Object>> dataMapList)throws Exception;
	
	/**
	 * 查询临时订购单行数据
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectTemporaryMerchOrderLine(Map<String,Object> dataMap)throws Exception;
	/**
	 * 删除指定商户的临时订购单行数据
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @throws Exception
	 */
	public void deleteTemporaryMerchOrderLine(Map<String,Object> dataMap)throws Exception;
	/**
	 * 查询未读的网购订单条数
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public int searchUnreadWGDDCount(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 联表查询
	 * 销售管理(SALE_ORDER)
	 * 销售单行(SALE_ORDER_LINE)
	 * @param saleOrderParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectSaleOrderAndSaleOrderLine(
			Map<String, Object> saleOrderParam) throws Exception;
	/**
	 * 联表查询
	 * 采购管理(PURCH_ORDER)
	 * 采购单行(PURCH_ORDER_LINE)
	 * @param purchOrderParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectPurchOrderAndPurchOrderLine(
			Map<String, Object> purchOrderParam) throws Exception;
	/**
	 * 联表查询
	 * 销售单行(SALE_ORDER_LINE) 
	 * 退货单(RETURN_ORDER)
	 * @param returnOrderParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectSaleOrderLineAndReturnOrder(
			Map<String, Object> returnOrderParam) throws Exception;
	/**
	 * 网购订单
	 * @param saleOrderParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectWgddOrder(Map<String, Object> saleOrderParam)throws Exception;
	
	/**
	 * 查询销售单，，关联base_merch_id sale_order_line sale_order
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleOrderJoinLineAndItem(Map<String, Object> paramsMap)throws Exception;
	
	/**
	 * 查询销售单行，，关联base_merch_id sale_order_line
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleOrderLineJoinItem(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 获取退货单行
	 * @param orderMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchReturnOrderLine(Map<String, Object> orderMap) throws Exception;
	
	public List<Map<String, Object>> searchReturnOrderLineByItemId(Map<String, Object> saleOrderLineParam) throws Exception;

}
