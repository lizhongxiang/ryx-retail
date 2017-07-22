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
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.ILotteryService;
/**
 * 抽奖
 * @author 徐虎彬
 * @date 2014年4月3日
 */
@Controller
public class LotteryController {

	private static final Logger logger = LoggerFactory.getLogger(CgtOrderController.class);
	
	@Resource
	private ILotteryService lotteryService;
	
	private Map<String, String> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, String> map = new HashMap<String, String>();
		map.put("cust_id",user.getRefId());
		return map;
	}
	/**
	 * 是否有抽奖活动
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/retail/lottery/whetherLottery")
	public void whetherLottery(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = lotteryService.whetherLottery(new HashMap<String,String>());
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取是否有抽奖活动信息出错！";
			logger.error("获取是否有抽奖活动信息出错：", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 抽奖活动列表参数
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/retail/lottery/getActivityParameter")
	public void getActivityParameter(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = lotteryService.getActivityParameter(getSessionInfo(request));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取抽奖活动列表参数出错！";
			logger.error("获取抽奖活动列表参数出错:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 活动详情	
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/user/lottery/getCustomerRaffleDetails")
	public void getCustomerRaffleDetails(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String,String> lotteryData=getSessionInfo(request);
		lotteryData.put("act_id", request.getParameter("act_id"));
		
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
	 * 抽奖
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/user/lottery/submitLottery")
	public void submitLottery(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String,String> lotteryData=new HashMap<String,String>();
		lotteryData.put("cust_id", request.getParameter("cust_id"));
		lotteryData.put("act_id", request.getParameter("act_id"));
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
