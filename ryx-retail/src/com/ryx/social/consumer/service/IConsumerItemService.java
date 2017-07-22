package com.ryx.social.consumer.service;

import java.util.List;
import java.util.Map;

public interface IConsumerItemService {
	/**
	 * 通过itemid 找到商品详情
	 * @param itemmMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemInfoByItemId(Map<String, Object> itemmMap)throws Exception;
	
	/**
	 * 通过商品名称查找商品列表
	 * @param itemMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemByName(Map<String, Object> itemMap)throws Exception;
	
	/**
	 * 查找商品和商铺的列表
	 * @param itemMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemAndMerchByName(Map<String, Object>itemMap)throws Exception;	
	/**
	 * 店铺在售商品
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemOfInSaleByMerchId(Map<String, Object> map)throws Exception;
	/**
	 * 搜索附近商品
	 * 
	 * @param latitude
	 * @param longitude
	 * @param key
	 * @param distance
	 * @param encodingType 
	 * @return
	 */
	public List findGoodsWithStores(String latitude, String longitude,
			String key, String distance, String deliveryType)throws Exception;

	public List popularKeyWords(String size);
}
