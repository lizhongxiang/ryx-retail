package com.ryx.social.retail.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ISaleOrderDao {
	public List<Map<String, Object>> selectSaleOrder(Map<String, Object> saleOrderParam) throws Exception;
	public List<Map<String, Object>> selectSaleOrderLine(Map<String, Object> saleOrderLineParam) throws Exception;
	public Map<String, Object> getSaleOrderDetail(Map<String, Object> saleOrderParam) throws Exception;
	public void updateSaleOrder(Map<String, Object> saleOrderParam) throws Exception;
	public List<Map<String, Object>> getSaleInfoWithOrderType(Map<String, Object> saleOrderParam) throws Exception;
	public List<Map<String, Object>> getSaleInfoWithKindId(Map<String, Object> saleOrderParam) throws Exception;
	public List<Map<String, Object>> getSaleInfoWithRank(Map<String, Object> saleOrderParam) throws Exception;
	public List<Map<String, Object>> getSaleInfoWithLastOrders(Map<String, Object> saleOrderParam) throws Exception;
	public List<Map<String, Object>> selectSaleQuantityDaily(Map<String, Object> saleQuantityDailyParam) throws Exception;
	public List<Map<String, Object>> selectSaleQuantityMonthly(Map<String, Object> saleQuantityMonthlyParam) throws Exception;
	public List<Map<String, Object>> selectGrossMarginDaily(Map<String, Object> grossMarginDailyParam) throws Exception;
	public List<Map<String, Object>> selectGrossMarginMonthly(Map<String, Object> grossMarginMonthlyParam) throws Exception;
	public void insertSaleOrder(Map<String, Object> orderParam) throws Exception;
	public void insertSaleOrderLine(Map<String, Object> lineParam) throws Exception;
	
	public void insertBatchSaleOrder(List<Map<String, Object>> orderParam) throws Exception;
	public void insertBatchSaleOrderLine(List<Map<String, Object>> lineParam) throws Exception;
}
