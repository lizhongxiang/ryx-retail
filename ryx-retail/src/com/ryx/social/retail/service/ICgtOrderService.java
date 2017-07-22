package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;


public interface ICgtOrderService {
	public List<Map<String, Object>> getTobaccoItemList(Map<String, String> paramMap) throws Exception;
	public Map<String, Object> getTobaccoOrder(Map<String, String> paramMap) throws Exception;
	public Map<String, Object> submitTobaccoOrder(Map<String, String> paramMap) throws Exception;
	
	//下载订货参数
	public Map<String, Object> getOrderParams(Map<String, String> paramMap)throws Exception;
		
	//订单列表
	public List<Map<String, Object>> getOrderList(Map<String, String> paramMap) throws Exception;
	
	//订单明细
	public Map<String, Object> getOrderDetail(Map<String, String> paramMap) throws Exception;
	
	//订单入库
	public List<Map<String, Object>> updateAndGetOrderList(Map<String, String> paramMap) throws Exception;
	
	// 更新卷烟数据
	public void updateTobaccoList(Map<String, String> paramMap) throws Exception;
	
	// 获取卷烟限量
	public List<Map<String, Object>> getTobaccoLmt(Map<String, String> paramMap) throws Exception;
	
	// 获取卷烟部分商品限量
	public List<Map<String, Object>> getTobaccoItemLmt(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 烟草供应商烟草数据更新（页面获取的数据）
	 * @author 徐虎彬
	 * @date 2014年3月3日
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateTobaccoData() throws Exception;
	/**
	 * 从服务器获取卷烟信息并添加到商户商品表中
	 * @author 徐虎彬
	 * @date 2014年3月15日
	 * @param paramMap
	 * @throws Exception
	 */
	public void getTobaccoItemListToBaseMerchItem(Map<String, String> paramMap) throws Exception;
	/**
	 *  订单支付
	 * @author 徐虎彬
	 * @date 2014年3月18日
	 * @param paramMap
	 * @throws Exception
	 */
	public Map<String,Object> orderPayment(Map<String, String> paramMap)throws Exception;
	
	
	public Map<String,String> orderPayByPOS(Map<String,String> orderMap, Map<String,String> serachOrderParm);
	
	public Map<String, Object> searchTobaccoItem(Map<String, Object> paramMap) throws Exception;
	
}
