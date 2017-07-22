package com.ryx.social.retail.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import com.ryx.social.retail.service.IInfointerService;

/**
 * @营销互动
 * @author 
 *
 */
@Controller
public class InfointerController {
	private static final Logger logger = LoggerFactory.getLogger(InfointerController.class);
	@Resource
	private IInfointerService infointerService;
	
	private Map<String, String> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, String> map = new HashMap<String, String>();
		String userId = user.getUserId();
		if(null == userId || "".equals(userId))
			userId="test";//赋一个测试id
		
		String comId = user.getComId();
		if(null == comId || "".equals(comId))
			comId="10370101";//赋一个测试id
		
		map.put("userId", userId);
		map.put("comId", comId);
		map.put("liceId", user.getLiceId());
		return map;
	}
	
	@RequestMapping(value="/retail/infointer/getNoticeList")
	public void getNoticeList(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("infointerController -- getNoticeList -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		String date1 = DateUtil.getToday();
		String page_index = request.getParameter("page_index");
		if(page_index == null || "".equals(page_index)){
			page_index = "1";
		}
		String page_size = request.getParameter("page_size");
		if(page_size == null || "".equals(page_size)){
			page_size = "20";
		}
		map.put("date1",date1);
		map.put("page_index", page_index);
		map.put("page_size", page_size);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = infointerService.getNoticeList(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("前台获取公告信息列表错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data, map);
		logger.debug("infointerController -- getNoticeList -- end");
	}
	
	@RequestMapping(value="/retail/infointer/getNoticeDetail")
	public void getNoticeDetail(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("infointerController -- getNoticeDetail -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		Map<String, String> jsonData = new Gson().fromJson(jsonParam, new TypeToken<Map<String, String>>() {}.getType());
		
		Map<String, String> map = getSessionInfo(request);	
		String date1 = DateUtil.getToday();
		map.put("date1", date1);
		map.putAll(jsonData);
		
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = infointerService.getNoticeDetail(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("读取公告信息明细错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("infointerController -- getNoticeDetail -- end");
	}
	
	@RequestMapping(value="/retail/infointer/updateNoticeDetail")
	public void updateNoticeDetail(HttpServletRequest request, HttpServletResponse response){
		logger.debug("infointerController -- updateNoticeDetail -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		Map<String, String> jsonData = new Gson().fromJson(jsonParam, new TypeToken<Map<String, String>>() {}.getType());
		
		Map<String, String> map = getSessionInfo(request);	
		String date1 = DateUtil.getToday();
		String time = DateUtil.getCurrentTime().substring(8);
		map.put("date1", date1);
		map.putAll(jsonData);
		
		Map<String, Object> data = new HashMap<String, Object>();
		List<String> list = new ArrayList<String>();
		list.add(map.get("noticeId"));
		list.add(map.get("userId"));
		list.add(date1);
		list.add(time);
		list.add("''");
		try {
			
			infointerService.updateNoticeDetail(list);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("更新公告信息阅读状态错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("infointerController -- updateNoticeDetail -- end");
	}
	
	@RequestMapping(value="/retail/infointer/getActivityList")
	public void getActivityList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, String> map = getSessionInfo(request);
		String date1 = DateUtil.getToday();
		map.put("date1",date1);
		//系统公告类型
		//01、系统公告。02：货源信息。03:1532非烟商品
		String noticeType = request.getParameter("notice_type");
		if (!StringUtils.isBlank(noticeType)) {
			map.put("noticeType", noticeType);
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = infointerService.getActivityList(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询01、系统公告。02：货源信息。03:1532非烟商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, map);
	}
	
	@RequestMapping(value="/retail/infointer/getInfointerList")
	public void getInfointerList(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("infointerController -- getInfointerList -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = infointerService.getInfointerList(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("前台获取咨询投诉信息列表错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("infointerController -- getInfointerList -- end");
	}
	
	@RequestMapping(value="/retail/infointer/getInfointerDetail")
	public void getInfointerDetail(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("infointerController -- getInfointerDetail -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		Map<String, String> jsonData = new Gson().fromJson(jsonParam, new TypeToken<Map<String, String>>() {}.getType());
		Map<String, String> map = getSessionInfo(request);	
		map.putAll(jsonData);
		
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = infointerService.getInfointerDetail(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("读取咨询投诉信息明细错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("infointerController -- getInfointerDetail -- end");
	}
	
	@RequestMapping(value="/retail/infointer/submitInfointer")
	public void submitInfointer(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("infointerController -- getInfointerDetail -- star");
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		Map<String, String> map = getSessionInfo(request);
		
		Map<String, String> jsonData = new Gson().fromJson(jsonParam, new TypeToken<Map<String, String>>() {}.getType());
		map.putAll(jsonData);
			
		String date1 = DateUtil.getToday();
		map.put("date1",date1);
		
		String time1 = DateUtil.getCurrentTime().substring(8);
		map.put("time1",time1);
		logger.debug("time1=="+time1);
		
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			infointerService.submitInfointer(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error("提交咨询投诉信息错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		logger.debug("infointerController -- getInfointerDetail -- end");
	}
}
