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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IActivityNewService;

/**
 * @营销互动
 * @author 
 *
 */
@Controller
public class ActivityNewController {
	private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
	@Resource
	private IActivityNewService activityNewService;
	
	private Map<String, String> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, String> map = new HashMap<String, String>();
		if(user != null) {
			/*map.put("custId", user.getRefId());
			map.put("comId", user.getComId());
			map.put("userId", user.getUserId());
			map.put("userType", user.getUserType());
			*/
			map.put("cust_id", user.getRefId());
			map.put("com_id", user.getComId());
			map.put("user_id", user.getRefId());
			//map.put("user_type", user.getUserType());
		}
		return map;
	}
	
	@RequestMapping(value="/retail/activity/getRetailActList")
	public void getActList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		//String date1 = DateUtil.getToday();
		//map.put("date1",date1);
		//map.put("actType", "'01','02'");
		map.put("user_type", "01");
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = activityNewService.getActList(map);
			
		} catch (Exception e){
			code = Constants.FAIL;
			msg = "获取活动列表失败";
			logger.error("获取活动列表失败==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/retail/activity/getConsumeActList")
	public void getConsumeActList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		//String date1 = DateUtil.getToday();
		//map.put("date1",date1);
		//map.put("actType", "'01','02'");
		map.put("user_type", "02");
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = activityNewService.getActList(map);
			
		} catch (Exception e){
			code = Constants.FAIL;
			msg = "获取活动列表失败";
			logger.error("获取活动列表失败==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/retail/activity/getActDetail")
	public void getActDetail(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);	
		String actId = request.getParameter("act_id");
		map.put("act_id", actId);

		Map<String, Object> data = new HashMap<String, Object>();
		
		try {
			data = activityNewService.getActDetail(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取活动明细失败";
			logger.error("获取活动明细失败==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/retail/activity/submitAct")
	public void submitAct(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		
		Map<String, Object> jsonData = JsonUtil.json2Map(jsonParam);
		Map<String, String> map = getSessionInfo(request);	
		
		String actId = (String)jsonData.get("act_id");
		map.put("act_id", actId);
		
		List<Map<String,Object>> lineList = (List<Map<String,Object>>)jsonData.get("lineList");
		map.put("lineList",  JsonUtil.list2json(lineList));
		
		Map<String, String> map2 = new HashMap<String, String>();
		map2.put("params", JsonUtil.map2json(map));

		Map<String, Object> result = new HashMap<String, Object>();
		
		logger.debug("map=="+map2);
		try {
			result = activityNewService.submitAct(map2);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "提交活动信息错误";
			logger.error("提交活动信息错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, result);
	}
	
	
	
	@RequestMapping(value="/user/activity/getRetailActList")
	public void getActList1(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		if(!map.containsKey("cust_id")) {
			String cust_id = request.getParameter("cust_id");
			String user_id = request.getParameter("user_id");
			String com_id = request.getParameter("com_id");
			
			if(!StringUtils.hasText(cust_id)) {
				ResponseUtil.write(request, response, Constants.CODE_PARAMS_ERROR, "cust_id不能为空", null);
				return;
			}
			
			map.put("cust_id", cust_id);
			map.put("user_id", user_id);
			map.put("com_id", com_id);
		}
		map.put("user_type", "01");
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = activityNewService.getActList(map);
			
		} catch (Exception e){
			code = Constants.FAIL;
			msg = "获取活动列表失败";
			logger.error("获取活动列表失败==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/user/activity/getConsumeActList")
	public void getConsumeActList1(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		String params = request.getParameter("params");
		if(!StringUtils.hasText(params)) {
			if(!map.containsKey("cust_id")) {
				String cust_id = request.getParameter("cust_id");
				String user_id = request.getParameter("user_id");
				String com_id = request.getParameter("com_id");
				/*
				if(!StringUtils.hasText(cust_id) || !StringUtils.hasText(com_id)) {
					ResponseUtil.write(request, response, Constants.CODE_PARAMS_ERROR, "cust_id或com_id不能为空", null);
					return;
				}
				*/
				map.put("cust_id", cust_id);
				map.put("user_id", user_id);
				map.put("com_id", com_id);
			}
			map.put("user_type", "02");
		} else {
			map = JsonUtil.json2Map(params);
		}
		
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = activityNewService.getActList(map);
			
		} catch (Exception e){
			code = Constants.FAIL;
			msg = "获取活动列表失败";
			logger.error("获取活动列表失败==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/user/activity/getActDetail")
	public void getActDetail1(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		Map<String, String> map = getSessionInfo(request);
		
		String params = request.getParameter("params");
		if(!StringUtils.hasText(params)) {
			String actId = request.getParameter("act_id");
			if(!map.containsKey("cust_id")) {
				String cust_id = request.getParameter("cust_id");
				String user_id = request.getParameter("user_id");
				String com_id = request.getParameter("com_id");
				/*
				if(!StringUtils.hasText(cust_id)) {
					ResponseUtil.write(request, response, Constants.CODE_PARAMS_ERROR, "cust_id不能为空", null);
					return;
				}
				*/
				map.put("cust_id", cust_id);
				map.put("user_id", user_id);
				map.put("com_id", com_id);
			}
			map.put("act_id", actId);
			map.put("act_type", request.getParameter("act_type"));
			map.put("user_type", request.getParameter("user_type"));
			map.put("order_id", request.getParameter("order_id"));
		} else {
			map = JsonUtil.json2Map(params);
		}

		Map<String, Object> data = new HashMap<String, Object>();
		
		try {
			data = activityNewService.getActDetail(map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取活动明细失败";
			logger.error("获取活动明细失败==", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/user/activity/submitAct")
	public void submitAct1(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		String jsonParam = request.getParameter("params");
		logger.debug("测试：" + jsonParam);
		
		Map<String, Object> jsonData = JsonUtil.json2Map(jsonParam);
		logger.debug("Obj：" + jsonData);
		Map<String, String> map = getSessionInfo(request);	
		if(!map.containsKey("cust_id")) {
			String cust_id = (String)jsonData.get("cust_id");
			String user_id = (String)jsonData.get("user_id");
			String com_id = (String)jsonData.get("com_id");
			
			if(!StringUtils.hasText(cust_id)) {
				ResponseUtil.write(request, response, Constants.CODE_PARAMS_ERROR, "cust_id不能为空", null);
				return;
			}
			
			map.put("cust_id", cust_id);
			map.put("user_id", user_id);
			map.put("com_id", com_id);
		}
		map.put("act_id", (String)jsonData.get("act_id"));
		map.put("act_type", (String)jsonData.get("act_type"));
		map.put("user_type", (String)jsonData.get("user_type"));
		map.put("order_id", (String)jsonData.get("order_id"));
		map.put("consumer_id", (String)jsonData.get("consumer_id"));
		map.put("customer_id", (String)jsonData.get("customer_id"));

		
		List<Map<String,Object>> lineList = (List<Map<String,Object>>)jsonData.get("lineList");
		map.put("lineList",  JsonUtil.list2json(lineList));
		
		Map<String, String> map2 = new HashMap<String, String>();
		map2.put("params", JsonUtil.map2json(map));

		Map<String, Object> result = new HashMap<String, Object>();
		
		logger.debug("map=="+map2);
		try {
			result = activityNewService.submitAct(map2);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "提交活动信息错误";
			logger.error("提交活动信息错误==", e);
		}
		ResponseUtil.write(request, response, code, msg, result);
	}
	
	
	/**
	 * description 根据活动ID和零售商ID 新增爱心送伞服务活动 的零售商
	 * 
	 * */
	@RequestMapping(value="/retail/loveForUmbrella/insertUmbrellaUser")
	public void insertUmbrellaUser(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		//获取session 零售户信息
		Map<String, String> paramMap = getSessionInfo(request);
		paramMap.put("act_id", "un00001");
		Map<String, Object> umbrellaUserMap = new HashMap<String,Object>();
		try {
			umbrellaUserMap = activityNewService.insertUmbrellaUser(paramMap);
			
		} catch (Exception e){
			code = Constants.FAIL;
			msg = "申请爱心送伞服务失败";
			logger.error("申请爱心送伞服务失败==", e);
		}
		ResponseUtil.write(request, response, code, msg, umbrellaUserMap);
	}
	/**
	 * description 根据活动ID和零售商ID 查询爱心送伞服务活动 的零售商详情
	 * 
	 * */
	@RequestMapping(value="/retail/loveForUmbrella/getUmbrellaUser")
	public void selectUmbrellaUser(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = "请求成功";
		//客户端发送请求，获取session 中的零售户信息
		Map<String, String> paramMap = getSessionInfo(request);
		paramMap.put("act_id", "un00001");
		List<Map<String, Object>> umbrellaUserList = new ArrayList<Map<String,Object>>();
		try {
			umbrellaUserList = activityNewService.selectUmbrellaUserList(paramMap);
			
		} catch (Exception e){
			code = Constants.FAIL;
			msg = "获取爱心送伞服务状态失败";
			logger.error("获取爱心送伞服务状态失败", e);
		}
		ResponseUtil.write(request, response, code, msg, umbrellaUserList);
	}
	
}
