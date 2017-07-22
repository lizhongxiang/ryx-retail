package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IPushService {
	public Map<String, Object> notifyManager(Map<String, Object> pushParam) throws Exception;
	public Map<String, Object> analysisBusiness(Map<String, Object> pushParam) throws Exception;
	public List searchShopsByKeywords(Map<String, Object> paramsMap)throws Exception;
	public List searchGoodsByKeywords(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 根据烟草供应商ID及条码获取信息
	 * @author 徐虎彬
	 * @date 2014年3月11日
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectSupplier(Map<String,Object> thisMap)throws Exception;
}
