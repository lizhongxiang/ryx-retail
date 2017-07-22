package com.ryx.social.retail.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IDeprecatedService;


@Controller
public class DeprecatedController {
	
	private static final Logger LOG = LoggerFactory.getLogger(DeprecatedController.class);
	@Resource
	private IDeprecatedService deprecatedService;
	
	//查询零售户商品排名
	@RequestMapping(value="/retail/statistics/searchGMerchSqGatherRranking")
	public void searchGMerchSqGatherRranking(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		Map<String, Object> data = null;
		try {
			data = deprecatedService.searchGMerchSqGatherRranking(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("查询零售户商品排名", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
		
}
