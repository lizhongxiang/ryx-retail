package com.ryx.social.consumer.controller;

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
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.social.retail.service.ILotteryService;
import com.ryx.social.retail.util.RetailConfig;
@Controller
public class ConsumerLotteryController {
	
	@Resource
	private ILotteryService lotteryService;
	
	private Logger logger = LoggerFactory.getLogger(ConsumerLotteryController.class);
	
	@RequestMapping(value = "/consumer/lottery")
	public void redirect2Lottery(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		Map<String, Object> data = new HashMap<String, Object>();

		try {
			List<Map<String,Object>> activityData=lotteryService.getLotteryList(requestMap);
			
			String orderId = "";
			String merchId = "";
			String actId = "";
			
			if(activityData!=null&&activityData.size()>0){
				Map<String,Object> dataMap=activityData.get(0);
				orderId = dataMap.get("ORDERID").toString();
				merchId = dataMap.get("CUSTID").toString();
				actId = dataMap.get("ACTID").toString();
				
				data.put("order_id", orderId);
				data.put("cust_id", merchId);
				data.put("customer_id", merchId);
				data.put("act_id", actId);
			}
			
			if(!RequestUtil.isToJson(request)) {
				String serverUrl = RetailConfig.getResourceServer()+"activities/jsps/jsp/infoActive/choujiang/choujiangnew/m/choujiang.jsp" + "?order_id=" + orderId + "&cust_id=" + merchId + "&customer_id=" + merchId + "&act_id=" + actId;
				response.sendRedirect(serverUrl);
				return;
			}

		} catch (Exception e) {
			logger.error(" = * = * = * = * = 转向抽奖页面错误 = * = * = * = * = ", e);
		}
		
		if(RequestUtil.isToJson(request)) {
			ResponseUtil.write(request, response, data);
		}
	}

	/**
	 * 活动详情	
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/consumer/lottery/getDetail")
	public void getDetail(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String,String> lotteryData = new HashMap<String, String>();
		
		lotteryData.put("act_id", request.getParameter("act_id"));
		lotteryData.put("cust_id", request.getParameter("cust_id"));
		lotteryData.put("customer_id", request.getParameter("customer_id"));
		lotteryData.put("order_id", request.getParameter("order_id"));
		
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = lotteryService.getCustomerRaffleDetails(lotteryData);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "查询活动详情失败！";
			logger.error("查询活动详情失败:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 获取抽奖信息	
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/consumer/lottery/getSubmitInfo")
	public void getSubmitInfo(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String,String> lotteryData = new HashMap<String, String>();
		
		lotteryData.put("act_id", request.getParameter("act_id"));
		lotteryData.put("cust_id", request.getParameter("cust_id"));
		lotteryData.put("order_id", request.getParameter("order_id"));
		/*
		lotteryData.put("act_id", "be9090453b28a901453c013244004e");
		lotteryData.put("cust_id", "1037010407467");
		lotteryData.put("order_id", "20140411185123");
		*/
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = lotteryService.getSubmitDetail(lotteryData);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取抽奖信息失败！";
			logger.error("获取抽奖信息失败:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 抽奖
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/consumer/lottery/submit")
	public void submitLottery(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String,String> lotteryData=new HashMap<String,String>();
		lotteryData.put("cust_id", request.getParameter("cust_id"));
		lotteryData.put("act_id", request.getParameter("act_id"));
		lotteryData.put("customer_id", request.getParameter("customer_id"));
		lotteryData.put("order_id", request.getParameter("order_id"));
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = lotteryService.submitLottery(lotteryData);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "抽奖系统繁忙，请稍后重试！";
			logger.error("抽奖系统繁忙:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
}
