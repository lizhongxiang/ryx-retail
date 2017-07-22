package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IOrderService {
	public List<Map<String, Object>> searchSaleOrder(Map<String, Object> saleOrderParam) throws Exception;
	public Map<String, Object> searchSaleOrderJoinSaleOrderLine(Map<String, Object> saleOrderJoinSaleOrderLineParam) throws Exception;
	public List<Map<String, Object>> searchSaleReturnOrderJoinSaleReturnOrderLine(Map<String, Object> saleReturnOrderParam) throws Exception;
	public Map<String, Object> searchSaleOrderJoinSaleOrderLineJoinReturnQty(Map<String, Object> saleOrderJoinSaleOrderLineParam ) throws Exception ;
	public List<Map<String, Object>> searchPurchSaleOrderLineByItemId(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> searchPurchOrder(Map<String, Object> purchOrderParam) throws Exception;
	public Map<String, Object> searchPurchOrderJoinPurchOrderLine(Map<String, Object> purchOrderJoinPurchOrderLineParam) throws Exception;
	public List<Map<String, Object>> searchReturnOrder(Map<String, Object> returnOrderParam) throws Exception;
	public Map<String, Object> searchReturnOrderJoinSaleOrderLine(Map<String, Object> returnOrderJoinSaleOrderLineParam) throws Exception;
	public List<Map<String, Object>> searchConsumerJoinSaleReturnOrder(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询未读的网购订单条数
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public int searchUnreadWGDDCount(Map<String, Object> paramsMap) throws Exception;
	
	/**
	 * 联表查询
	 * 销售管理(SALE_ORDER)  销售单行(SALE_ORDER_LINE)
	 */
	public List<Map<String, Object>> searchSaleOrderAndSaleOrderLine(
			Map<String, Object> paramMap) throws Exception;
	/**
	 * 联表查询
	 * 采购管理(PURCH_ORDER)   采购单行(PURCH_ORDER_LINE)
	 */
	public List<Map<String, Object>> searchPurchOrderAndPurchOrderLine(
			Map<String, Object> paramMap) throws Exception;
//	/**
//	 * 联表查询
//	 * 销售单行(SALE_ORDER_LINE)  退货单(RETURN_ORDER)
//	 */
//	public List<Map<String, Object>> searchSaleOrderLineAndReturnOrder(
//			Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询网购订单
	 * @param saleOrderParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchWgddOrder(Map<String, Object> saleOrderParam)throws Exception;
	/**
	 * 查询销售单和商品
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleOrderLineJoinItem(Map<String, Object> paramMap) throws Exception;
	/**
	 * 获取退货单行
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchReturnOrderLine(Map<String, Object> paramMap)throws Exception;

}
