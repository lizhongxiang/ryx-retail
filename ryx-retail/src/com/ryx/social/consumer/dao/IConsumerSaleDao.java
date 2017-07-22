package com.ryx.social.consumer.dao;

import java.util.List;
import java.util.Map;

public interface IConsumerSaleDao {

	void submitSaleOrder(Map<String, Object> saleOrderParam) throws Exception;

	List<Map<String, Object>> selectConsumerSaleOrderLine(
	Map<String, Object> saleMap) throws Exception;

	List<Map<String, Object>> selectConsumerSaleOrder(
	Map<String, Object> saleMap) throws Exception;

	public List<Map<String, Object>> selectMerchSalePromotion(Map<String, Object> paramMap) throws Exception;

}
