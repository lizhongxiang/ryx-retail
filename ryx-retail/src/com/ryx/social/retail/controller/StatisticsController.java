package com.ryx.social.retail.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IStatisticsService;
import com.ryx.social.retail.util.ParamUtil;


@Controller
public class StatisticsController {
	
	private Logger logger = LoggerFactory.getLogger(StatisticsController.class);
	
	@Resource
	private IStatisticsService statisticsService;
	
	//销售额   报表
	@RequestMapping(value="/retail/statistics/searchSaleroom")
	public void searchSaleroom(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		// 获取前台参数 type：按月还是按周之类的 number:几周或者几月
		// 如果是10周， 如果现在是星期五，today-4-7*9是开始日期，today-1是结束日期
		// 把参数传到service 传start_date， end_date， type
		// service调dao 获取的是按日的， 然后start_date往后加7天是第一个销售额，再加7天是第二个销售额，。。。
		
		//前台传入---
		//	params{
		//		time_interval:时间间隔类型：annual（年）   monthly（月） week(周)  day(日)
		//		number:周期数
		//		group_key:分组类型：item_id, item_kind_id 如果都不传返回时间内总销售量
		//	}
		
		
		
		//dao传入
		//merch_id:用户编号
		//start_date：开始日期
		//end_date：结束日期
		//group_key：分组类型
		//time_interval：是年（annual）、月（monthly）、日（默认--不传）
		
		
		Map<String, Object> paramsMap =  ParamUtil.getParamMap(request);;
		List<Map<String, Object>> data = null;
		try {
			String type=(String) paramsMap.get("time_interval");
			if(type.equals("annual")){//按年
				data=statisticsService.searchSaleroomAnnual(paramsMap);
			}else if(type.equals("monthly")){//按月
				data=statisticsService.searchSaleroomMonthly(paramsMap);
			}else if(type.equals("week")){//按周
				data=statisticsService.searchSaleroomWeek(paramsMap);
			}else{//按日
				data=statisticsService.searchSaleroomDay(paramsMap);
			}
			logger.debug("StatisticesController serrchSaleroomMonthly data:"+data);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("销售额 月报表错误 StatisticsController searchSaleRoomMonthly",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg,data);
	}

	/**
	 * 进销存明细表
	 */
	@RequestMapping(value="/retail/statistics/jxcreporttable")
	public void searchJxcReportTable(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = null;
		Map<String,Object> paramsMap = new HashMap<String, Object>();
		String params = request.getParameter("params");
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		paramsMap.put("merch_id", userMap.get("ref_id"));
		try {
			data=statisticsService.searchJxcReportTable(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response,code,msg, data);
	}	
	
	/**
	 * 销售明细表
	 */
	@RequestMapping(value="/retail/statistics/salereporttable")
	public void searchSaleReportTable(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = null; 
		Map<String, Object> userMap = new HashMap<String, Object>();
		userMap.putAll(IdentityUtil.getUserMap(request));
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		paramsMap.put("merch_id", userMap.get("ref_id"));
		try {
			data=statisticsService.searchSaleReportTable(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("StatisticsController searchSaleReportTable 错误",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response,code,msg, data);
	}	
	
	/**
	 * 库存图表
	 */
	@RequestMapping(value="/retail/statistics/stockReport")
	public void stockReport(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> usermMap=IdentityUtil.getUserMap(request);
		usermMap.put("merch_id", usermMap.get("ref_id"));
		List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
		try {
			data=statisticsService.stockReport(usermMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			code=Constants.FAIL_MSG;
			logger.error("销售报表失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
	
	/**
	 * 利润报表
	 */
	@RequestMapping(value="/retail/statistics/profitReportForms")
	public void profitReportForms(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=new HashMap<String, Object>();
//		paramsMap.putAll(userMap);	
		paramsMap.put("merch_id", userMap.get("ref_id"));
//		paramsMap.put("status", "1,2");
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));		
		try {
			String timeinterval=MapUtil.getString(paramsMap, "time_interval");
			if(StringUtil.isBlank(timeinterval) || timeinterval.equals("day") ){//日
				data=statisticsService.searchMerchDailyProfit(paramsMap);
			}else if(timeinterval.equals("monthly")){//月
				data=statisticsService.searchMerchMonthlyProfit(paramsMap);
			}else if(timeinterval.equals("week")){
				data=statisticsService.searchMerchWeeklyProfit(paramsMap);
			}
		} catch (Exception e) {
			// TODO: handle exception
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("利润报表失败",e);
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
	
	/**
	 * 进销存图表
	 */
	@RequestMapping(value="/retail/statistics/jxcreportforms")
	public void jxcReportForms(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=new HashMap<String, Object>();
//			paramsMap.putAll(userMap);
		paramsMap.put("merch_id", userMap.get("ref_id"));
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			String timeinterval=(String) paramsMap.get("time_interval");
			if(StringUtil.isBlank(timeinterval) || timeinterval.equals("day") ){//日
				data=statisticsService.searchDayJxcReportForms(paramsMap);
			}else if(timeinterval.equals("monthly")){//月
				data=statisticsService.searchMonthJxcReportForms(paramsMap);
			}else if(timeinterval.equals("week")){
				data=statisticsService.searchWeekJxcReportForms(paramsMap);
			}		
		} catch (Exception e) {
			// TODO: handle exception
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("进销存图表失败",e);
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
	
	@RequestMapping(value="/statistics/createDailySettlement")
	public void createDailySettlement(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		// user_code, ref_id, settlement_date 370113100592
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			statisticsService.createDailySettlement(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 日结错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	@RequestMapping(value="/statistics/resetDailySettlement")
	public void resetDailySettlement(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		// merch_id, start_date, end_date
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		String startDate = MapUtil.getString(paramMap, "start_date");
		try {
			if(startDate.equalsIgnoreCase("today")) { // 开始日期为today则为逆向日结
				statisticsService.resetDailySettlementDescendingly(paramMap);
			} else { // 否则为正向日结
				statisticsService.resetDailySettlementAscendingly(paramMap);
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 重新日结错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	@RequestMapping(value="/statistics/warehousingMerchTobaccoOrder")
	public void warehousingMerchTobaccoOrder(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			statisticsService.warehousingMerchTobaccoOrder(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 自动入库错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	@RequestMapping(value="/retail/statistics/searchMerchCurrentSettlement")
	public void searchMerchCurrentSettlement(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = IdentityUtil.getUserMap(request); // merch_id
		paramMap.put("merch_id", paramMap.get("ref_id"));
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = statisticsService.searchMerchCurrentSettlement(paramMap);
			BigDecimal SaleAmount=BigDecimal.ZERO;
			BigDecimal returnAmount=BigDecimal.ZERO;
			BigDecimal saleAmountOnline=BigDecimal.ZERO;
			BigDecimal returnAmountOnline=BigDecimal.ZERO;
			BigDecimal saleAmountOffline=BigDecimal.ZERO;
			BigDecimal returnAmountOffline=BigDecimal.ZERO;
			BigDecimal profitAmount=BigDecimal.ZERO;
			BigDecimal lossAmount=BigDecimal.ZERO;
			BigDecimal profitAmountOffline=BigDecimal.ZERO;
			BigDecimal lossAmountOffline=BigDecimal.ZERO;
			BigDecimal profitAmountOnline=BigDecimal.ZERO;
			BigDecimal lossAmountOnline=BigDecimal.ZERO;
			if(data.containsKey("sale_amount")&&data.get("sale_amount")!=null){
				SaleAmount=new BigDecimal(data.get("sale_amount")+"");
			}
			if(data.containsKey("return_amount")&&data.get("return_amount")!=null){
				returnAmount=new BigDecimal(data.get("return_amount")+"");
			}
			if(data.containsKey("sale_amount_online")&&data.get("sale_amount_online")!=null){
				saleAmountOnline=new BigDecimal(data.get("sale_amount_online")+"");
			}
			if(data.containsKey("return_amount_online")&&data.get("return_amount_online")!=null){
				returnAmountOnline=new BigDecimal(data.get("return_amount_online")+"");
			}
			if(data.containsKey("sale_amount_offline")&&data.get("sale_amount_offline")!=null){
				saleAmountOffline=new BigDecimal(data.get("sale_amount_offline")+"");
			}
			if(data.containsKey("return_amount_offline")&&data.get("return_amount_offline")!=null){
				returnAmountOffline=new BigDecimal(data.get("return_amount_offline")+"");
			}
			if(data.containsKey("profit_amount")&&data.get("profit_amount")!=null){
				profitAmount=new BigDecimal(data.get("profit_amount")+"");
			}
			if(data.containsKey("loss_amount")&&data.get("loss_amount")!=null){
				lossAmount=new BigDecimal(data.get("loss_amount")+"");
			}
			if(data.containsKey("profit_amount_offline")&&data.get("profit_amount_offline")!=null){
				profitAmountOffline=new BigDecimal(data.get("profit_amount_offline")+"");
			}
			if(data.containsKey("loss_amount_offline")&&data.get("loss_amount_offline")!=null){
				lossAmountOffline=new BigDecimal(data.get("loss_amount_offline")+"");
			}
			if(data.containsKey("profit_amount_online")&&data.get("profit_amount_online")!=null){
				profitAmountOnline=new BigDecimal(data.get("profit_amount_online")+"");
			}
			if(data.containsKey("loss_amount_online")&&data.get("loss_amount_online")!=null){
				lossAmountOnline=new BigDecimal(data.get("loss_amount_online")+"");
			}
			SaleAmount=SaleAmount.subtract(returnAmount);
			saleAmountOnline=saleAmountOnline.subtract(returnAmountOnline);
			saleAmountOffline=saleAmountOffline.subtract(returnAmountOffline);
			profitAmount=profitAmount.subtract(new BigDecimal("0").subtract(lossAmount));
			profitAmountOffline=profitAmountOffline.subtract(new BigDecimal("0").subtract(lossAmountOffline));
			profitAmountOnline=profitAmountOnline.subtract(new BigDecimal("0").subtract(lossAmountOnline));
			data.put("sale_amount",SaleAmount );
			data.put("sale_amount_online",saleAmountOnline);
			data.put("sale_amount_offline",saleAmountOffline );
			data.put("profit_amount",profitAmount );
			data.put("profit_amount_offline",profitAmountOffline );
			data.put("profit_amount_online", profitAmountOnline);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户当前结算信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	@RequestMapping(value="/retail/statistics/searchMerchItemCurrentSettlement")
	public void searchMerchItemCurrentSettlement(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = IdentityUtil.getUserMap(request); // merch_id
		paramMap.put("merch_id", paramMap.get("ref_id"));
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = statisticsService.searchMerchItemCurrentSettlement(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户商品当前结算信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	@RequestMapping(value="/retail/statistics/searchMerchSettlement")
	public void searchMerchSettlement(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request); // merch_id, day, month
		paramMap.put("merch_id", paramMap.get("ref_id"));
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = statisticsService.searchMerchSettlement(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户日结算信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/retail/statistics/searchMerchDailySettlement")
	public void searchMerchDailySettlement(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request); // merch_id, settlement_date, settlement_date_ceiling, settlement_date_floor
		paramMap.put("merch_id", paramMap.get("ref_id"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchMerchDailySettlement(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户日结算信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	@RequestMapping(value="/retail/statistics/searchMerchItemDailySettlement")
	public void searchMerchItemDailySettlement(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request); // merch_id, settlement_date
		paramMap.put("merch_id", paramMap.get("ref_id"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchMerchItemDailySettlement(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户商品日结算信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * description   商户日结进销存利本月最高利润
	 * @param
	 * @return
	 * */
	@RequestMapping(value="/retail/statistics/searchMerchMonthProfit")
	public void searchMerchMonthProfit(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request); // merch_id, day, month
		paramMap.put("merch_id", paramMap.get("ref_id"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		//List<Map<String, Object>> bottProfitData = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchMerchMonthProfit(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户日结算信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	@RequestMapping(value="/retail/statistics/searchMerchMonthProfitRate")
	public void searchMerchMonthProfitRate(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request); // merch_id, day, month
		paramMap.put("merch_id", paramMap.get("ref_id"));
		//最高利润
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		//最低利润
		//List<Map<String, Object>> bottProfitData = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchMerchMonthProfitRate(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户日结算信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	//进货明细表---new
	@RequestMapping(value="/retail/statistics/searchPurchRecords")
	public void searchPurchRecords(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id",userMap.get("ref_id"));
		paramMap.put("status", "1,2");
		Map<String, Object> data = new HashMap<String, Object>();
		//List<Map<String, Object>> bottProfitData = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchPurchRecords(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 进货明细表错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	//销售明细表---new
	@RequestMapping(value="/retail/statistics/searchSalesRecords")
	public void searchSalesRecords(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		paramMap.put("status", "1,2");
		Map<String, Object> data = new HashMap<String, Object>();
		//List<Map<String, Object>> bottProfitData = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchSalesRecords(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 销售明细表---new = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	//库存明细表---new
	@RequestMapping(value="/retail/statistics/searchWhseRecords")
	public void searchWhseRecords(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		Map<String, Object> data = new HashMap<String, Object>();
		//List<Map<String, Object>> bottProfitData = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchWhseRecords(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 销售明细表---new= * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	//利润明细表--new
	@RequestMapping(value="/retail/statistics/searchProfitRecords")
	public void searchProfitRecords(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		paramMap.put("status", "1,2");
		Map<String, Object> data = new HashMap<String, Object>();
		//List<Map<String, Object>> bottProfitData = new ArrayList<Map<String, Object>>();
		try {
			data = statisticsService.searchProfitRecords(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 利润明细表--new = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 投放策略卷烟部分限量
	 */
	@RequestMapping(value = "/retail/tobacco/getImportitemQty")
	public void getImportitemQty(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> data = null;
		try {
			data = statisticsService.getImportitemQty(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("卷烟投放策略", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 客户日销量及库存----------潍坊
	 * 搜索潍坊卷烟日结数据
	 */
	@RequestMapping(value = "/weifang/searchTobaccoDailyData")
	public void searchTobaccoDailyData(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> data = Collections.EMPTY_LIST;
		try {
			if(paramMap.get("date")!=null) {
				data = statisticsService.searchTobaccoDailyData(paramMap);
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("搜索潍坊卷烟日结数据", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
}
