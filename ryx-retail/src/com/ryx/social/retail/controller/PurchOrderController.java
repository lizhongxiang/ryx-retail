package com.ryx.social.retail.controller;

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
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.ICgtOrderService;
import com.ryx.social.retail.service.IPurchOrderService;
import com.ryx.social.retail.service.IWhseService;
import com.ryx.social.retail.util.ParamUtil;

@Controller
public class PurchOrderController {
	
	private Logger logger = LoggerFactory.getLogger(PurchOrderController.class);
	
	@Resource
	private IPurchOrderService purchOrderService;
	@Resource
	IWhseService whseService;
	
	private Map<String, Object> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("merch_id", user.getRefId());
		return map;
	}
	
	@RequestMapping(value="/retail/purch/submitPurchOrder")
	public void submitPurchOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;

		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("order_id", IDUtil.getId());
		paramMap.put("voucher_date", DateUtil.getToday());
		paramMap.put("order_date", DateUtil.getToday());
		paramMap.put("order_time", DateUtil.getCurrentTime().substring(8));
		paramMap.put("operator", paramMap.get("user_code"));
		String jsonParam = request.getParameter("params");
		paramMap.put("json_param", jsonParam);
		try {
			purchOrderService.commodityWarehousing(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("生成采购单失败: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//采购报表
	@RequestMapping(value="/retail/purch/searchPurchOrderReport")
	public void searchPurchOrderReport(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=new HashMap<String, Object>();
		paramsMap.putAll(userMap);		
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		
		try {
			String timeinterval=(String) paramsMap.get("time_interval");
			if(StringUtil.isBlank(timeinterval) || timeinterval.equals("day")){
				data=purchOrderService.searchDayPurchOrderReport(paramsMap);
			}else if(timeinterval.equals("annual")){//年
				data=purchOrderService.searchYearPurchOrderReport(paramsMap);
			}else if(timeinterval.equals("monthly")){//月
				data=purchOrderService.searchMonthPurchOrderReport(paramsMap);
			}else if(timeinterval.equals("week")){
				data=purchOrderService.searchWeekPurchOrderReport(paramsMap);
			}
			
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("采购报表失败",e);
			// TODO: handle exception
		}		
		ResponseUtil.write(request, response, code,msg,data);
	}
	
	/**
	 * 获取采购单行列表
	 * @author 徐虎彬
	 * @date 2014年4月11日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/retail/purch/getSomePurchOrderList")
	public void getSomePurchOrderList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = null;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(requestMap.containsKey("params")) {
			paramMap = MapUtil.get(requestMap, "params", new HashMap<String, Object>());
		} else {
			paramMap = requestMap;
		}
		if(!paramMap.containsKey("page_index")) {
			paramMap.put("page_index", 1);
		}
		if(!paramMap.containsKey("page_size")) {
			paramMap.put("page_size", 20);
		}
		paramMap.put("merch_id", userMap.get("ref_id"));
		List<Map<String, Object>> data = null;
		try {
			data = purchOrderService.getPurchOrderList(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询采购单列表错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, requestMap);
	}
	/**
	 * 获取采购单详情
	 * @author 徐虎彬
	 * @date 2014年4月11日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/retail/purch/getPurchOrderDetail")
	public void getPurchOrderDetail(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;

		Map<String, Object> paramMap = getSessionInfo(request);
		String order_id = request.getParameter("order_id");
		paramMap.put("order_id", order_id);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = purchOrderService.getPurchOrderDetail(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("获取采购单详情失败: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
}
