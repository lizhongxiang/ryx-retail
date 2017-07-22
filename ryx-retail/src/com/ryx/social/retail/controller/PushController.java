package com.ryx.social.retail.controller;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
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
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IPushService;
import com.ryx.social.retail.service.ISaleService;
import com.ryx.social.retail.service.IStatisticsService;

@Controller
public class PushController {
	
	Logger logger = LoggerFactory.getLogger(PushController.class);
	
	@Resource
	private IPushService pushService;
	@Resource
	private ISaleService saleService;
	@Resource
	private IStatisticsService statisticsService;
	
	
	private Map<String, Object> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("merch_id", user.getRefId());
		map.put("com_id", user.getComId());
		return map;
	}
	
	private Map<String, Object> getParamMap(HttpServletRequest request) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(userMap!=null) {
			paramMap.putAll(userMap);
			paramMap.put("merch_id", paramMap.get("ref_id"));
		}
		if(requestMap.get("params")!=null) { // 如果前台传merch_id会覆盖session中的merch_id
			paramMap.putAll((Map<String, Object>) requestMap.get("params"));
		} else {
			paramMap.putAll(requestMap);
		}
		return paramMap;
	}
	
	/**
	 * 查询经营提醒信息
	 */
	@RequestMapping(value="/retail/push/notifyManager")
	public void searchBusinessNotify(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getSessionInfo(request);
		String supplierId = "987654321";
		paramMap.put("supplier_id", supplierId); //烟草供货商
		String status = "04";
		paramMap.put("status", status); //发货
		String orderType = "03";
		paramMap.put("order_type", orderType); //手机
		BigDecimal qtyWhseFloor = new BigDecimal(30); 
		paramMap.put("qty_whse_floor", qtyWhseFloor); //库存下限
		String orderDate = request.getParameter("order_date");
		if(orderDate!=null && !"".equals(orderDate)) {
			paramMap.put("order_date", orderDate);
		}
		String orderMonth = request.getParameter("order_month");
		if(orderMonth!=null && !"".equals(orderMonth)) {
			paramMap.put("start_date", orderMonth+"01");
			paramMap.put("end_date", orderMonth+"31");
		}
		Map<String, Object> data = null;
		try {
			//调用service
			data = pushService.notifyManager(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询经营提醒信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	/**
	 * 查询经营分析数据
	 */
	@RequestMapping(value="/retail/push/analysisBusiness")
	public void searchBusinessAnalysis(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getSessionInfo(request);
		String orderDate = request.getParameter("order_date");
		String orderMonth = request.getParameter("order_month");
		//先判断order_date, 再判断order_month, 如果都没有则取当天
		if(orderDate!=null && orderDate.length()==8) {
			paramMap.put("start_date", orderDate);
			paramMap.put("end_date", orderDate);
		} else if(orderMonth!=null && orderMonth.length()==6) {
			paramMap.put("start_date", orderMonth+"01");
			paramMap.put("end_date", orderMonth+"31");
		} else {
			paramMap.put("order_date", DateUtil.getToday());
		}
		Map<String, Object> data = null;
		try {
			//调用service
			data = pushService.analysisBusiness(paramMap);
			
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询经营分析数据错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 查询日销售总量, start_date, end_date
	 */
	@RequestMapping(value="/retail/push/searchSaleQuantityDaily")
	public void searchSaleQuantityDaily(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
//		Map<String, Object> paramMap = getSessionInfo(request);
//		paramMap.put("start_date", request.getParameter("start_date"));
//		paramMap.put("end_date", request.getParameter("end_date"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			String params =URLDecoder.decode(request.getParameter("params"),"utf-8");
			logger.debug("PushController searchSaleQuantityDaily params:"+params);
			Map map=JsonUtil.json2Map(params);			
			for (Object	 obj: map.keySet()) {
				if(map.get(obj).toString().length()!=8){
					logger.debug("PushController serchSaleQuantityDaily params  传参格式错误");
					code = Constants.FAIL;
					msg = "数据格式错误，开始日期/结束日期必须是8为数字，如(20140101)";
					ResponseUtil.write(request, response, code, msg, data);
					return;
				}
			}			
			map.putAll(this.getSessionInfo(request));
			data = saleService.searchSaleQuantityDaily(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询日销售总量错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 查询月销售总量, start_month, end_month
	 */
	@RequestMapping(value="/retail/push/searchSaleQuantityMonthly")
	public void searchSaleQuantityMonthly(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
//		Map<String, Object> paramMap = getSessionInfo(request);
//		paramMap.put("start_month", request.getParameter("start_month"));
//		paramMap.put("end_month", request.getParameter("end_month"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			String params=URLDecoder.decode(request.getParameter("params"),"utf-8");
			logger.debug("PushController searchSaleQuantityMonthly params:"+params);
			Map map=JsonUtil.json2Map(params);
			for (Object obj : map.keySet()) {
				if(map.get(obj).toString().length()!=6){
					logger.debug("PushController serchSaleQuantityMonthly params 传入参数格式错误");
					code = Constants.FAIL;
					msg = "数据格式错误，开始月份/结束月份必须为6位,如(201401)";
					ResponseUtil.write(request, response, code, msg, data);
					return;
				}
			}
			map.putAll(this.getSessionInfo(request));
			data = saleService.searchSaleQuantityMonthly(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询月销售总量错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 查询日毛利, start_date, end_date
	 */
	@RequestMapping(value="/retail/push/searchGrossMarginDaily")
	public void searchGrossMarginDaily(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		paramMap.put("merch_id", paramMap.get("ref_id"));
//		paramMap.put("start_date", request.getParameter("start_date"));
//		paramMap.put("end_date", request.getParameter("end_date"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
//			String params=URLDecoder.decode(request.getParameter("params"), "utf-8");
//			logger.debug("PushController searchGrossMarginDaily params:"+params);
//			Map map=JsonUtil.json2Map(params);
//			for (Object	 obj:map.keySet()) {
//				if(map.get(obj).toString().length()!=8){
//					logger.debug("PushCOntroller searchGrossMarginDaily 传参格式错误");
//					code = Constants.FAIL;
//					msg = "数据格式错误，开始日期/结束日期必须是8位数字，如(20140101)";
//					ResponseUtil.write(request, response, code, msg, data);
//					return;
//				}
//			}
//			map.putAll(this.getSessionInfo(request));
//			data = saleService.searchGrossMarginDaily(paramMap);
			data = statisticsService.searchMerchDailyGrossMargin(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询日毛利错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 查询月毛利, start_month, end_month
	 */
	@RequestMapping(value="/retail/push/searchGrossMarginMonthly")
	public void searchGrossMarginMonthly(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		paramMap.put("merch_id", paramMap.get("ref_id"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
//			String params=URLDecoder.decode(request.getParameter("params"),"utf-8");
//			logger.debug("PushXontroller serchGrossMarginMonthly params:"+params);
//			Map paramMap=JsonUtil.json2Map(params);
//			for (Object obj : paramMap.keySet()) {
//				if(paramMap.get(obj).toString().length()!=6){
//					logger.debug("PushController searchGrossMarginMonthly 传参错误");
//					code = Constants.FAIL;
//					msg = "数据格式错误，开始月份/结束月份必须为6位数字,如(201401)";
//					ResponseUtil.write(request, response, code, msg, data);
//					return;
//				}
//			}
//			paramMap.putAll(this.getSessionInfo(request));
//			data = saleService.searchGrossMarginMonthly(paramMap);
			data = statisticsService.searchMerchMonthlyGrossMargin(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询月毛利错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 楼下店接口, 按照距离
	 */
	@RequestMapping(value="/push/searchMerchItemDetail")
	public void searchGoodsByKeywords(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
//		Map<String, Object> paramMap = IdentityUtil.getUserMap(request);
//		paramMap.put("merch_id", paramMap.remove("ref_id"));
//		String params = request.getParameter("params");
//		paramMap.putAll(JsonUtil.json2Map(params));
		
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String params = request.getParameter("params");
		Map<String, Object> paramMap = JsonUtil.json2Map(params);
//		Map<String, Object> verifiedResult = (Map<String, Object>) request.getAttribute("__verified_result");
//		paramMap.putAll((Map<String, Object>)verifiedResult.get("params"));	
//		if(paramMap.get("pageIndex")==null||paramMap.get("pageIndex").equals("")){
//			paramMap.put("page_index", 1);	
//		}
		paramMap.put("page_index", paramMap.remove("pageIndex"));
		if(paramMap.get("pageSize")==null){
			paramMap.put("page_size", 20);
		} else {
			paramMap.put("page_size", paramMap.remove("pageSize"));
		}
		try {
			data = pushService.searchGoodsByKeywords(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("查询商户商品信息错误: ",e);
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
	// 2.搜索附近店铺（根据距离查看）	 
	@RequestMapping(value="/push/searchMerchDetail")
	public void searchShopsByKeywords(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
//		Map<String, Object> paramMap = IdentityUtil.getUserMap(request);
//		paramMap.put("merch_id", paramMap.remove("ref_id"));
		String params = request.getParameter("params");
		Map<String, Object> paramMap = JsonUtil.json2Map(params);
//		paramMap.putAll((Map<String, Object>)((Map<String, Object>) request.getAttribute("__verified_result")).get("params"));	
		paramMap.put("page_index", paramMap.remove("pageIndex"));
		if(paramMap.get("pageSize")==null){
			paramMap.put("page_size", 20);
		} else {
			paramMap.put("page_size", paramMap.remove("pageSize"));
		}
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();	
		try {
			data = pushService.searchShopsByKeywords(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("查询商户信息错误: ",e);
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
}
