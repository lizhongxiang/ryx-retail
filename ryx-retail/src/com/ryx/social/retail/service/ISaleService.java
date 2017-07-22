package com.ryx.social.retail.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ISaleService {
	public Map<String, Object> submitSaleOrder(Map<String, Object> paramMap) throws Exception;
	public Map<String, Object> submitBatchSaleOrder(Map<String, Object> paramMap, List<Map<String, Object>> paramList) throws Exception;
	public Map<String, Object> getSaleOrderDetail(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> selectSaleOrder(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> searchSaleQuantityDaily(Map<String, Object> saleQuantityDailyParam) throws Exception;
	public List<Map<String, Object>> searchSaleQuantityMonthly(Map<String, Object> saleQuantityMonthlyParam) throws Exception;
	public List<Map<String, Object>> searchGrossMarginDaily(Map<String, Object> grossMarginDailyParam) throws Exception;
	public List<Map<String, Object>> searchGrossMarginMonthly(Map<String, Object> grossMarginMonthlyParam) throws Exception;
	public void updateSaleOrder(Map<String, Object> saleOrderParam)throws Exception;
}
