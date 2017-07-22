package com.ryx.social.consumer.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.consumer.service.IConsumerMerchService;

@Controller
public class ConsumerMerchController {

	private Logger logger = LoggerFactory
			.getLogger(ConsumerMerchController.class);

	@Resource
	private IConsumerMerchService consumerMerchService;

	@RequestMapping(value = "/consumer/merch/searchMerchById")
	public void searchMerchById(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
		try {
			request.setCharacterEncoding("utf-8");
			// String
			// params=URLDecoder.decode(request.getParameter("params"),"utf-8");
			String params = new String(request.getParameter("params")
					.getBytes(), "gbk");
			logger.debug("ItemController searchItemInfoByItemId params:"
					+ params);
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.putAll(JsonUtil.json2Map(params));
			data = consumerMerchService.searchMerchById(paramsMap);

		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("ItemController searchItemInfoById", e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	@RequestMapping(value = "/consumer/merch/searchMerchByName")
	public void searchMerchByName(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
		try {
			request.setCharacterEncoding("utf-8");
			// String
			// params=URLDecoder.decode(request.getParameter("params"),"utf-8");
			String params = new String(request.getParameter("params")
					.getBytes(), "gbk");
			logger.debug("ItemController searchItemInfoByItemId params:"
					+ params);
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.putAll(JsonUtil.json2Map(params));
			if (paramsMap.get("page_index") == null
					|| paramsMap.get("page_index").equals("")) {
				paramsMap.put("page_index", "1");
			}
			if (paramsMap.get("page_size") == null
					|| paramsMap.get("page_size").equals("")) {
				paramsMap.put("page_size", "20");
			}
			if (paramsMap.get("merchName") == null
					|| paramsMap.get("merchName").equals("")) {
				paramsMap.put("merchName", "济南");
			}

			data = consumerMerchService.searchMerchByName(paramsMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("ItemController searchItemInfoById", e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 参数 ： 经纬度、模糊名、距离 返回 ：匹配店铺简情
	 * 
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping(value = "/consumer/merch/wildcardFind")
	public void wildcardFind(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("utf-8");
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		// 接收参数
		String latitude = "";
		String longitude = "";
		String key = "";
		String distance = "";
		String deliveryType = "";
		

		List resultList = Collections.EMPTY_LIST;
		try {
			Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
			if(requestMap.containsKey("params")) { 
				Map<String, String> paramsMap = (Map<String, String>) requestMap.get("params");
				latitude = paramsMap.get("latitude");
				longitude = paramsMap.get("longitude");
				key = paramsMap.get("key");
				distance = paramsMap.get("distance");
				deliveryType = paramsMap.get("deliveryType");
				
			} else {
				latitude = request.getParameter("latitude");
				longitude = request.getParameter("longitude");
				key = request.getParameter("key");
				distance = request.getParameter("distance");
				deliveryType = request.getParameter("deliveryType");
			}
			resultList = consumerMerchService.wildcardFind(latitude,
					longitude, key, distance, deliveryType);
		} catch (Exception e) {
			logger.debug("wildcardFind 异常消息 : ", e);
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
		}
		ResponseUtil.write(request, response, code, msg, resultList);
	}
	/**
	 * 返回热门搜索关键词
	 * 
	 * @param request
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/consumer/merch/popularKeyWords")
	public void popularKeyWords(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {

		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;

		String size = null;

		List resultList = Collections.EMPTY_LIST;
		try {
			Map<String, Object> requestMap = RequestUtil
					.getParameterMap(request);
			if (requestMap.containsKey("params")) {
				Map<String, String> paramsMap = (Map<String, String>) requestMap
						.get("params");
				size = paramsMap.get("size");
			} else {
				size = request.getParameter("size");
			}
			resultList = consumerMerchService.popularKeyWords(size);
		} catch (Exception e) {
			logger.error("ItemController popularKeyWords ", e);
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
		}
		ResponseUtil.write(request, response, code, msg, resultList);
	}
}
