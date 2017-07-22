package com.ryx.social.retail.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IReturnOrderDao {
//	public void submitReturnOrder(Map<String, Object> returnOrderParam) throws Exception;
	public BigDecimal getAmtReturnTotal(Map<String, Object> returnOrderParam) throws Exception;
	public List<Map<String, Object>> selectReturnOrder(Map<String, Object> returnOrderParam) throws Exception;
	/**
	 * 查询退货单
	 * @param orderMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchReturnOrder(Map<String, Object> orderMap) throws Exception;
	
	
	/**
	 * 提交退货单
	 * @param returnOrderParam
	 * @throws Exception
	 */
	public void insertReturnOrder(Map<String, Object> returnOrderParam)throws Exception;
	
	/**
	 * 提交退货单行
	 * @param returnOrderParam
	 * @throws Exception
	 */
	public void insertReturnOrderLine(Map<String, Object> returnOrderParam)throws Exception;
}
