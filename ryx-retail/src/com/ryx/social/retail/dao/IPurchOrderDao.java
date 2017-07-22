package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IPurchOrderDao {
	/**
	 * 获取采购单列表
	 * @author 徐虎彬
	 * @date 2014年4月11日
	 * @param purchOrderParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getPurchOrderList(Map<String, Object> purchOrderParam) throws Exception;
	/**
	 * 获取采购单详情信息
	 * @author 徐虎彬
	 * @date 2014年4月11日
	 * @param purchOrderParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getPurchOrderDetail(Map<String, Object> purchOrderParam) throws Exception;
	
	public void submitPurchOrder(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> searchPurchOrder(Map<String, Object> purchOrderParam) throws Exception;
	public Map<String, Object> getPurchInfo(Map<String, Object> purchParam) throws Exception;
	public List<Map<String, Object>> searchPurchOrderReport(Map<String, Object> purchMap) throws Exception;
}
