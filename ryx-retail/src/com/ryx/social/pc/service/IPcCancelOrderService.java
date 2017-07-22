package com.ryx.social.pc.service;

import java.util.Map;

public interface IPcCancelOrderService {
	
	/**
	 * 撤销销售单
	 * @param paramsMap
	 * @throws Exception
	 */
	public  void cancelSaleOrder(Map<String, Object> paramsMap) throws Exception;

	/**
	 * 撤销退货单
	 * @param paramsMap
	 * @throws Exception
	 */
	public void cancelReturnOrder(Map<String, Object> paramsMap) throws Exception;
	/**
	 * 撤销盘点单
	 * @param paramsMap
	 * @throws Exception
	 */
	public void cancelWhseTurn(Map<String, Object> paramsMap) throws Exception;
	/**
	 * 撤销采购单
	 * @param paramsMap
	 * @throws Exception
	 */
	public void cancelPuchOrder(Map<String, Object> paramsMap) throws Exception;

}
