package com.ryx.social.consumer.controller;

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
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.consumer.service.IConsumerInteractionService;

@Controller
public class ConsumerInteractionController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsumerInteractionController.class);
	
	@Resource
	private IConsumerInteractionService infointerService;
	
	/**
	 * 从tobaccoserver上获取系统信息
	 */
	@RequestMapping(value="/interaction/searchNotice")
	public void searchNotice(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = null;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(requestMap.containsKey("params")) {
			paramMap = (Map<String, Object>) requestMap.get("params");
		} else {
			paramMap = requestMap;
		}
		paramMap.put("userId", userMap.get("user_id"));
		paramMap.put("comId", "10370101"); // 山东烟草com_id
		paramMap.put("date1", DateUtil.getToday());
		List<Map<String, Object>> data = null;
		try {
			data = infointerService.searchNotice(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询系统公告错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
}
