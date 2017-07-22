package com.ryx.social.retail.controller;

import java.util.HashMap;
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
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IFeedbackService;

@Controller
public class FeedbackController {
	private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
	@Resource
	private IFeedbackService feedbackService;
	private Map<String, Object> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("merch_id", user.getRefId());
		map.put("user_code", user.getUserCode());
		map.put("user_name", user.getUserName());
		return map;
	}
	/**
	 * 新增反馈信息
	 */
	@RequestMapping(value="/retail/feedback/submitFeedbackInfo")
	public void submitFeedbackInfo(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getSessionInfo(request);
		//检验参数是否合法
		String params = (String) request.getParameter("params");
		if(params==null || "".equals(params)) {
			code = Constants.CODE_PARAMS_ERROR;
			msg = "params 参数为空";
			ResponseUtil.write(request, response, code, msg, null);
			return;
		}
		Map<String, Object> jsonParam = JsonUtil.json2Map(params);
		try {
			String infoTitle = (String) jsonParam.get("info_title");
			String infoContext = (String) jsonParam.get("info_context");
			if(infoTitle==null||"".equals(infoTitle)||infoContext==null||"".equals(infoContext)) {
				code = Constants.CODE_PARAMS_ERROR;
				msg = "info_title/info_context 参数为空";
				ResponseUtil.write(request, response, code, msg, null);
				return;
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 新增反馈信息时参数转换错误 = * = * = * = * = ", e);
		}
		paramMap.putAll(jsonParam);
		//生成id日期时间
		String infoId = IDUtil.getId();
		String infoDate = DateUtil.getToday();
		String infoTime = DateUtil.getCurrentTime().substring(8);
		paramMap.put("info_id", infoId);
		paramMap.put("info_date", infoDate);
		paramMap.put("info_time", infoTime);
		try {
			feedbackService.submitFeedbackInfo(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 新增反馈信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
}
