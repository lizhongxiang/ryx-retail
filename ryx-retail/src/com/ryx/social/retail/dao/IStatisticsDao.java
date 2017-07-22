package com.ryx.social.retail.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IStatisticsDao {
	public Map<String, Object> getBusinessHistory(Map<String, Object> saleOrderParam) throws Exception;

	public void insertMerchDailySettlement(Map<String, Object> settlementParam) throws Exception;

//	public void insertMerchItemDailySettlement(List<Map<String, Object>> settlementParamList) throws Exception;

	public List<Map<String, Object>> selectMerchDailySettlement(Map<String, Object> merchDailyParam) throws Exception;

	public List<Map<String, Object>> selectMerchItemDailySettlement(Map<String, Object> merchItemDailyParam) throws Exception;

	public void insertMerchItemDailySettlement(Collection<Map<String, Object>> settlementParamList) throws Exception;
	
	/**
	 * description   商户日结进销存利本月前三条最高利润
	 * @param
	 * @return
	 * */
	public List<Map<String,Object>> selectMerchMonthMaxProfit(Map<String,Object> topProfitParam) throws Exception; 
	
	/**
	 * description   商户日结进销存利本月前三条最低利润
	 * @param
	 * @return
	 * */
	public List<Map<String,Object>> selectMerchMonthMinProfit(Map<String,Object> bottomProfitParam) throws Exception; 
	/**
	 * description   进销存利本月利润率最高商品
	 * @param
	 * @return
	 * */
	public List<Map<String,Object>> selectMerchMonthMaxProfitRate(Map<String,Object> topProfitRateParam) throws Exception;
	
	/**
	 * description   进销存利本月利润率最低商品
	 * @param
	 * @return
	 * */
	public List<Map<String,Object>> selectMerchMonthMinProfitRate(Map<String,Object> bottomProfitRateParam) throws Exception;
	/**
	 * 进销存表格
	 */
	public List<Map<String, Object>> searchJxcReportTable(Map<String, Object> reportMap)throws Exception;

	/**
	 * 销售表格详细
	 */
	public List<Map<String, Object>> searchSaleReportTable(Map<String, Object> saleMap)throws Exception;

	public String selectSystemSettlement() throws Exception;

	public void updateSystemSettlement(String settlementDate) throws Exception;

	public String selectWarehousingDate() throws Exception;

	public void updateWarehousingDate(String warehouseDate) throws Exception;

	public List<Map<String, Object>> searchMerchMonthlySettlement(Map<String, Object> settlementParam) throws Exception;

	public List<Map<String, Object>> searchMerchItemMonthlySettlement(Map<String, Object> merchItemMonthlyParam) throws Exception;
	/**
	 * 商户日结和
	 * 通过 item_kind_id,settlement_date
	 * @param settlementParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSumMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception;

	/**
	 * 查询商品日结表，通过item_id
	 * @param saleMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchItemDailySettlement(Map<String, Object> saleMap) throws Exception;
	/**
	 * 进货明细表====new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectPurchRecords(Map<String, Object> paramMap)throws Exception;
	/**
	 * 销售明细表---new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectSalesRecords(Map<String, Object> paramMap)throws Exception;

	/**
	 * 库存明细表----new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectWhseRecords(Map<String, Object> paramMap)throws Exception;

	/**
	 * 利润明细表---new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectProfitRecords(Map<String, Object> paramMap)throws Exception;

	public List<Map<String, Object>> resetMerchDailySettlement(Map<String, Object> settlementParam) throws Exception;

	public List<Map<String, Object>> resetMerchTodaySettlement(Map<String, Object> settlementParam) throws Exception;

	public void tempMerchDailySettlement(Map<String, Object> settlementParam) throws Exception;

	public void tempMerchItemDailySettlement(List<Map<String, Object>> settlementParamList) throws Exception;

	public void refreshMerchDailySettlement(Map<String, Object> settlementParam) throws Exception;

	public void refreshMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception;

	public void backupMerchDailySettlement(Map<String, Object> settlementParam) throws Exception;

	public void backupMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception;

	public void cleanMerchTempSettlement(Map<String, Object> settlementParam) throws Exception;

	public void cleanMerchItemTempSettlement(Map<String, Object> settlementParam) throws Exception;

	/**
	 * 搜索卷烟数据---潍坊
	 * 通过查询
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchTobaccoDailyData(Map<String, Object> paramMap) throws Exception;

}
