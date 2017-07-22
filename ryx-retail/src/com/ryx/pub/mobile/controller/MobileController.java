package com.ryx.pub.mobile.controller;

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
import com.ryx.framework.util.ResponseUtil;
import com.ryx.pub.mobile.service.IMobileService;
import com.ryx.social.retail.controller.CgtOrderController;

@Controller
public class MobileController {
	
	private static final Logger logger = LoggerFactory.getLogger(CgtOrderController.class);
	
	@Resource
	private IMobileService mobileService;
	
	@RequestMapping(value = "/pub/mobile/getMobileInfo")
	public void getMobileInfo(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data= new HashMap<String, Object>();
		String mobilePhone = request.getParameter("mobile");
		try {
			data = mobileService.getMobileInfo(mobilePhone);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取手机信息错误";
			logger.error("获取手机信息错误:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
}
