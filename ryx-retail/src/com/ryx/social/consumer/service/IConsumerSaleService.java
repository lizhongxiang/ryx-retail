package com.ryx.social.consumer.service;

import java.util.List;
import java.util.Map;

public interface IConsumerSaleService {

	void submitConsumerSaleOrder(Map<String, Object> paramMap) throws Exception;

	List<Map<String, Object>> searSaleOrderandLine(Map<String, Object> saleMap) throws Exception;

	public List<Map<String, Object>> searchMerchPromotion(Map<String, Object> paramMap) throws Exception;


}
