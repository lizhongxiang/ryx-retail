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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IActivityService;

/**
 * @营销互动
 * @author 
 *
 */
@Controller
public class ActivityController {
	private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
	@Resource
	private IActivityService activityService;
	
	private Map<String, String> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("cust_id", user.getRefId());
		map.put("com_id", user.getComId());
		map.put("user_id", user.getRefId());
		map.put("user_type", user.getUserType());
		return map;
	}
	
	@RequestMapping(value="/retail/activity/getSurveyList")
	public void getSurveyList(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("ActivityController -- getSurveyList -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		String date1 = DateUtil.getToday();
		map.put("date1",date1);
		map.put("actType", "'01','02'");
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = activityService.getSurveyList(map);
		} catch (Exception e){
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("前台获取问卷调查列表错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("ActivityController -- getSurveyList -- end");
	}
	
	@RequestMapping(value="/retail/activity/getSurveyDetail")
	public void getSurveyDetail(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("ActivityController -- getSurveyDetail -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		Map<String, String> jsonData = new Gson().fromJson(jsonParam, new TypeToken<Map<String, String>>() {}.getType());
		Map<String, String> map = getSessionInfo(request);	
		map.putAll(jsonData);

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = activityService.getSurveyDetail(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("查询问卷调查明细错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("ActivityController -- getSurveyDetail -- end");
	}
	
	@RequestMapping(value="/retail/activity/submitSurvey")
	public void submitSurvey(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("ActivityController -- submitSurvey -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		
		Map<String, Object> jsonData = new Gson().fromJson(jsonParam, new TypeToken<Map<String, Object>>() {}.getType());
		Map<String, String> map = getSessionInfo(request);	
		
		String actId = (String)jsonData.get("actId");
		map.put("actId", actId);
		
		List<Map<String,Object>> lineList = (List<Map<String,Object>>)jsonData.get("lineList");
		map.put("lineList",  new Gson().toJson(lineList));

		logger.debug("map=="+map);
		try {
			activityService.submitSurvey(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("提交调查问卷错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
		logger.debug("ActivityController -- submitSurvey -- end");
	}
	
	
	
	@RequestMapping(value="/retail/activity/getConsumerRaffleList")
	public void getConsumerRaffleList(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("ActivityController -- getConsumerRaffleList -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		String date1 = DateUtil.getToday();
		map.put("date1",date1);
		map.put("actType", "'04'");
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = activityService.getSurveyList(map);
		} catch (Exception e){
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("前台获取消费者抽奖列表错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("ActivityController -- getConsumerRaffleList -- end");
	}
	
	
	@RequestMapping(value="/retail/activity/getConsumerRaffleDetail")
	public void getConsumerRaffleDetail(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("ActivityController -- getConsumerRaffleDetail -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		Map<String, String> jsonData = new Gson().fromJson(jsonParam, new TypeToken<Map<String, String>>() {}.getType());
		Map<String, String> map = getSessionInfo(request);	
		String date1 = DateUtil.getToday();
		map.put("date1",date1);
		map.putAll(jsonData);
		
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = activityService.getRaffleDetail(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("查询消费者抽奖明细错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("ActivityController -- getConsumerRaffleDetail -- end");
	}
	
}
