package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IStatisticsService {
	/**
	 * 销售单 年报表
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleroomAnnual(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 销售单：月报表
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleroomMonthly(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 销售单： 周报表
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleroomWeek(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 销售单  日报表
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleroomDay(Map<String, Object> paramsMap)throws Exception;
	//库存报表
	public List<Map<String, Object>> stockReport(Map<String, Object> stockMap)throws Exception;
	// 日结表
	public void createDailySettlement(Map<String, Object> paramMap) throws Exception;
	// 自动入库
	public void warehousingMerchTobaccoOrder(Map<String, Object> paramMap) throws Exception;
	// 获取商户日结信息
	public List<Map<String, Object>> searchMerchDailySettlement(Map<String, Object> paramMap) throws Exception;
	// 获取商户商品日结信息
	public List<Map<String, Object>> searchMerchItemDailySettlement(Map<String, Object> paramMap) throws Exception;
	// 获取商户当前结算信息
	public Map<String, Object> searchMerchCurrentSettlement(Map<String, Object> paramMap) throws Exception;
	/**
	 * description
	 *  获取当日商户商品进销存利数据
	 *  @param paramMap
	 * */
	public Map<String, Object> searchMerchItemCurrentSettlement(Map<String, Object> paramMap) throws Exception;
	/**
	 * description
	 *  获取商户某日/某月进销存利数据
	 *  @param paramMap
	 * */
	public Map<String, Object> searchMerchSettlement(Map<String, Object> paramMap) throws Exception;

	/**
	 * 利润报表--日
	 */
	public List<Map<String, Object>> searchMerchDailyProfit(Map<String, Object> profitMap) throws Exception;
	/**
	 * 利润报表--周
	 */
	public List<Map<String, Object>> searchMerchWeeklyProfit(Map<String, Object> profitMap) throws Exception;
	/**
	 * 利润报表--月
	 */
	public List<Map<String, Object>> searchMerchMonthlyProfit(Map<String, Object> profitMap) throws Exception;
	/**
	 * 进销存报表---月
	 */
	public List<Map<String, Object>> searchMonthJxcReportForms(Map<String, Object> profitMap) throws Exception;
	/**
	 * 进销存报表---周
	 */
	public List<Map<String, Object>> searchWeekJxcReportForms(Map<String, Object> profitMap) throws Exception;
	/**
	 * 进销存报表--日
	 */
	public List<Map<String, Object>> searchDayJxcReportForms(Map<String, Object> profitMap) throws Exception;
	/**
	 * 进销存表格
	 */
	public List<Map<String, Object>> searchJxcReportTable(Map<String, Object> reportMap)throws Exception;
	
	public List<Map<String,Object>> searchMerchMonthProfit(Map<String,Object> profitParam) throws Exception; 
	
	public List<Map<String,Object>> searchMerchMonthProfitRate(Map<String,Object> profitRateParam) throws Exception;
	/**
	 * 销售表格详细
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleReportTable(Map<String, Object> saleMap)throws Exception;
	
	
	/**
	 * description  今日 利/存 -卷烟/非卷烟利润，本月利润
	 * @return
	 * */
	public Map<String, Object> searchBusinessAnalysis(Map<String, Object> paramMap) throws Exception ;
	public void autoWarehousingMerchTobaccoOrder(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> searchMerchMonthlySettlement(Map<String, Object> paramsMap) throws Exception;
	public List<Map<String, Object>> searchMerchMonthlyGrossMargin(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> searchMerchDailyGrossMargin(Map<String, Object> paramMap) throws Exception;
	/**
	 * 获取用户商品库存分类合计
	 * @author 徐虎彬
	 * @date 2014年5月20日
	 * @param stockMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> stockClassificationCombined(Map<String,Object> stockMap)throws Exception;
	
	/**
	 * 获取指定日期内的销售单统计数据
	 * @author 徐虎彬
	 * @date 2014年4月22日
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> getSaleDailySettlement(Map<String,Object> dataMap)throws Exception;
	/**
	 * 进货明细表---new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> searchPurchRecords(Map<String, Object> paramMap)throws Exception;
	/**
	 * 销售明细表-----new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> searchSalesRecords(Map<String, Object> paramMap)throws Exception;
	/**
	 * 库存明细表---new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> searchWhseRecords(Map<String, Object> paramMap)throws Exception;
	/**
	 * 利润明细表=====new
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> searchProfitRecords(Map<String, Object> paramMap) throws Exception;
	
	// 正向重日结
	public void resetDailySettlementAscendingly(Map<String, Object> paramMap) throws Exception;
	// 逆向重日结
	public void resetDailySettlementDescendingly(Map<String, Object> paramMap) throws Exception;
	
	public List<Map<String, Object>> getImportitemQty(Map<String, Object> paramMap) throws Exception;
	
	public String searchSystemSettlement() throws Exception;
	
	public void updateSystemSettlement(String settlementDate) throws Exception;
	
	/**
	 * 搜索卷烟日结数据-----潍坊
	 * 
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchTobaccoDailyData(Map<String, Object> paramMap) throws Exception;
	
}
