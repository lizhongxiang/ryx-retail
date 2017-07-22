package com.ryx.social.consumer.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IConsumerMerchDao {	
	/**
	 * 通过名称查找店铺列表
	 * @param merchMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchByName(Map<String, Object> merchMap) throws Exception;
	/**
	 * 通过id查找店铺info
	 * @param merchMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchById(Map<String, Object> merchMap) throws Exception;
	public List<Map<String, Object>> selectMerchFile(Map<String, Object> paramMap) throws Exception;
}
