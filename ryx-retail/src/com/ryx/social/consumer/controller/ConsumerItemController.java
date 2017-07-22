package com.ryx.social.consumer.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import com.ryx.social.consumer.service.IConsumerItemService;
import com.ryx.social.retail.service.IItemService;

@Controller
public class ConsumerItemController {

	private Logger logger = LoggerFactory
			.getLogger(ConsumerItemController.class);
	@Resource
	private IItemService itemService;
	@Resource
	private IConsumerItemService consumerItemService;

	@RequestMapping(value = "/consumer/item/searchMerchItemDetail")
	public void searchMerchItemJoinUnit(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		Map<String, Object> paramMap = null;;
		if (requestMap.get("params") != null) {
			paramMap = (Map<String, Object>) requestMap.get("params");
		} else {
			paramMap = requestMap;
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItemJoinMerchItemUnit(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 楼下店查询商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	@RequestMapping(value = "/consumer/item/searchItemInfoById")
	public void searchItemInfoByItemId(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
		try {
			String params = request.getParameter("params");
			logger.debug("ItemController searchItemInfoByItemId params:"
					+ params);
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.putAll(JsonUtil.json2Map(params));
			// paramsMap.putAll(this.getSessionInfo(request));
			paramsMap.put("status", "1");
			// String status = (String) itemParam.get("status");
			data = itemService.searchItem(paramsMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("ItemController searchItemInfoByItemId", e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	// 查找商品详情
	@RequestMapping(value = "/consumer/item/searchItemInfoByItemId")
	public void searchItemInfoById(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = null;
		Map<String, Object> dataMap = new HashMap<String, Object>();
		try {
			String params = request.getParameter("params");
			logger.debug("ItemController searchItemInfoByItemId params:"
					+ params);
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.putAll(JsonUtil.json2Map(params));
			data = consumerItemService.searchItemInfoByItemId(paramsMap);
			if (data.size() > 0) {
				dataMap = data.get(0);
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("ItemController searchItemInfoById", e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, dataMap);
	}

	// 通过商品名称查找商品列表
	@RequestMapping(value = "/consumer/item/searchItemByName")
	public void searchItemByName(HttpServletRequest request,
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
			data = consumerItemService.searchItemByName(paramsMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("ItemController searchItemInfoById", e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	// 通过商品名称查找商品列表
	@RequestMapping(value = "/consumer/item/searchItemAndMerchByName")
	public void searchItemAndMerchByName(HttpServletRequest request,
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
			data = consumerItemService.searchItemAndMerchByName(paramsMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error("ItemController searchItemInfoById", e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	// 店铺在售商品
	@RequestMapping(value = "/consumer/item/searchItemOfInSaleByMerchId")
	public void searchMerchItem(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> requestParam = RequestUtil.getParameterMap(request);
		Map<String, Object> paramMap = null;
		if (requestParam.get("params") != null) {
			paramMap = (Map<String, Object>) requestParam.get("params");
		} else {
			paramMap = requestParam;
		}
		if (paramMap.get("page_index") == null) {
			paramMap.put("page_index", 1);
		}
		if (paramMap.get("page_size") == null) {
			paramMap.put("page_size", 20);
		}
		paramMap.put("status", "1");
		Map<String, Object> pageParam = new HashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItem(paramMap);
			pageParam.put("page_index", paramMap.remove("page_index"));
			pageParam.put("page_size", paramMap.remove("page_size"));
			pageParam.put("page_count", paramMap.remove("page_count"));
			pageParam.put("count", paramMap.remove("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 楼下店查询商铺在售商品错误 = * = * = * = * = ",
					e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
		// Map<String, Object> pageParam = new HashMap<String, Object>();
		// Map<String, Object> requestParam =
		// RequestUtil.getParameterMap(request);
		// Map<String, Object> paramMap = (Map<String, Object>)
		// requestParam.get("params");
		// try {
		// if (paramMap.get("page_index") == null
		// || paramMap.get("page_index").equals("")) {
		// paramMap.put("page_index", "1");
		// }
		// if (paramMap.get("page_size") == null
		// || paramMap.get("page_size").equals("")) {
		// paramMap.put("page_size", "20");
		// }
		// data = consumerItemService.searchItemOfInSaleByMerchId(paramMap);
		// pageParam.put("page_index", paramMap.remove("page_index"));
		// pageParam.put("page_size", paramMap.remove("page_size"));
		// pageParam.put("page_count", paramMap.remove("page_count"));
		// pageParam.put("count", paramMap.remove("count"));
		// } catch (Exception e) {
		// code = Constants.FAIL;
		// msg = Constants.FAIL_MSG;
		// logger.error("ItemController searchItemInfoById", e);
		// }
	}

	/**
	 * 
	 * 搜附近商品
	 * 
	 * @param request
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/consumer/item/wildcardFindGoodsWithStores")
	public void findGoodsWithStores(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {

		request.setCharacterEncoding("UTF-8");
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
			Map<String, Object> requestMap = RequestUtil
					.getParameterMap(request);
			if (requestMap.containsKey("params")) {
				Map<String, String> paramsMap = (Map<String, String>) requestMap
						.get("params");
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
			logger.debug("ItemController wildcardFindGoodsWithStores key:"
					+ key);
			resultList = consumerItemService.findGoodsWithStores(latitude,
					longitude, key, distance, deliveryType);
		} catch (Exception e) {
			logger.error("ItemController wildcardFindGoodsWithStores ", e);
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
	@RequestMapping("/consumer/item/popularKeyWords")
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
			resultList = consumerItemService.popularKeyWords(size);
		} catch (Exception e) {
			logger.error("ItemController popularKeyWords ", e);
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
		}
		ResponseUtil.write(request, response, code, msg, resultList);
	}
}
