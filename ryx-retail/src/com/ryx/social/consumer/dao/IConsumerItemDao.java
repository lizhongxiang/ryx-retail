package com.ryx.social.consumer.dao;

import java.util.List;
import java.util.Map;

public interface IConsumerItemDao  {
	/**
	 * 通过商品名称查找商品列表
	 * @param itemMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemByName (Map<String, Object> itemMap)throws Exception;
	/**
	 * 通过商品itemid 查找商品详情---完成
	 * @param itemmMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemInfoBySupplienId(Map<String, Object> itemmMap)throws Exception;
	/**
	 * 通过商品类别查找
	 * @param itemMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemAndMerchByName (Map<String, Object> itemMap)throws Exception;
	/**
	 * 店铺在售商品
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemOfInSaleByMerchId(Map<String, Object> map)throws Exception;
}
