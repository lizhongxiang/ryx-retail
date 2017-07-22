package com.ryx.social.retail.controller;

import java.util.ArrayList;
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
import com.ryx.framework.util.ResponseUtil;
import com.ryx.social.retail.service.IItemBarRelevanceService;

@Controller
public class ItemBarRelevanceController {

	private Logger logger = LoggerFactory.getLogger(ItemBarRelevanceController.class);

	@Resource
	private IItemBarRelevanceService itemBarRelevanceService;
	
	/**
	 * 查询单个商品推荐
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/recommend/itemBarRelevance/searchItemBarRelevance")
	public void searchItemBarRelevance(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		String params = request.getParameter("params");
		Map<String, Object> paramMap = JsonUtil.json2Map(params);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemBarRelevanceService.searchItemBarRelevance(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询通过主条码查询推荐商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 查询列表
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/recommend/itemBarRelevance/searchItemBarRelevanceList")
	public void searchItemBarRelevanceList(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		String params = request.getParameter("params");
		Map<String, Object> paramMap = JsonUtil.json2Map(params);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemBarRelevanceService.searchItemBarRelevanceList(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询所有推荐商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 添加推荐商品
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/recommend/itemBarRelevance/insertItemBarRelevance")
	public void insertItemBarRelevance(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		String params = request.getParameter("params");
		Map<String, Object> paramMap = JsonUtil.json2Map(params);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			// data =
			itemBarRelevanceService.insertItemBarRelevance(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 添加推荐商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value = "/recommend/itemBarRelevance/deleteItemBarRelevance")
	public void deleteItemBarRelevance(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		String params = request.getParameter("params");
		Map<String, Object> paramMap = JsonUtil.json2Map(params);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			itemBarRelevanceService.deleteItemBarRelevance(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 删除推荐商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value = "/recommend/itemBarRelevance/updateItemBarRelevance")
	public void updateItemBarRelevance(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		String params = request.getParameter("params");
		Map<String, Object> paramMap = JsonUtil.json2Map(params);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			itemBarRelevanceService.updateItemBarRelevance(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 修改推荐商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
}
