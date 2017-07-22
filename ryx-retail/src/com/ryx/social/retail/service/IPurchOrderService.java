package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IPurchOrderService {
	public void submitPurchOrder(Map<String, Object> paramMap) throws Exception;
	/**
	 * 添加采购单
	 * @author 徐虎彬
	 * @date 2014年3月10日
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertPurchOrder(Map<String, Object> paramMap) throws Exception;
	/**
	 * 商品入库
	 * @author 徐虎彬
	 * @date 2014年2月27日
	 * @param paramMap
	 * @throws Exception
	 */
	public void commodityWarehousing(Map<String, Object> paramMap) throws Exception;
	/**
	 * 获取采购单列表
	 * @author 徐虎彬
	 * @date 2014年4月11日
	 * @param paramMap
	 * @throws Exception
	 */
	public List<Map<String,Object>> getPurchOrderList(Map<String, Object> paramMap) throws Exception;	
	/**
	 * 获取采购单详情信息
	 * @author 徐虎彬
	 * @date 2014年4月11日
	 * @param paramMap
	 * @throws Exception
	 */
	public List<Map<String,Object>> getPurchOrderDetail(Map<String, Object> paramMap) throws Exception;
	
	public List<Map<String, Object>> searchWeekPurchOrderReport(Map<String, Object> purchMap) throws Exception;
	public List<Map<String, Object>> searchYearPurchOrderReport(Map<String, Object> purchMap) throws Exception;
	public List<Map<String, Object>> searchMonthPurchOrderReport(Map<String, Object> purchMap) throws Exception;
	public List<Map<String, Object>> searchDayPurchOrderReport(Map<String, Object> purchMap) throws Exception;
	public void modifyMerchItemCostAndPri1(List<Map<String, Object>> odList) throws Exception;
	
	/**
	 * description 盘点库存分类为烟的数据上传至RTMS
	 * */
//	public Map<String,Object> insertPurchTobaccoToRTMS(Map<String,Object> purchParamMap)throws Exception;
	
}
