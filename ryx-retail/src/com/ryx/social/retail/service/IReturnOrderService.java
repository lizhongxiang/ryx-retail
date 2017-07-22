package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IReturnOrderService {
	public Map<String, Object> submitReturnOrder(Map<String, Object> returnOrderParam) throws Exception;
	/**
	 * 查询退货单
	 * @param orderMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchReturnOrder(Map<String, Object> orderMap) throws Exception;
	
//	/**
//	 * 获取退货单行
//	 * @param orderMap
//	 * @return
//	 * @throws Exception
//	 */
//	public List<Map<String, Object>> searchReturnOrderLine(Map<String, Object> orderMap)throws Exception;
}
