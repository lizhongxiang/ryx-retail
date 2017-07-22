package com.ryx.social.consumer.service;

import java.util.List;
import java.util.Map;

public interface IConsumerMerchService {
	/**
	 * 通过名称查找商铺
	 * @param merchMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchByName(Map<String, Object> merchMap)throws Exception;
	/**
	 * 通过id查找商铺
	 * @param merchMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> searchMerchById(Map<String, Object>merchMap)throws Exception;
	/**
	 * 模糊搜店铺
	 * 
	 * @param latitude
	 * @param longitude
	 * @param key
	 * @param distance
	 * @return
	 */
	public List wildcardFind(String latitude, String longitude, String key,
			String distance, String deliveryType) throws Exception;
	public List popularKeyWords(String size);
}
