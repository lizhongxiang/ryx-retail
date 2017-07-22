package com.ryx.social.retail.controller;

import java.util.ArrayList;
import java.util.Collections;
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
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IPromotionService;
import com.ryx.social.retail.util.ParamUtil;

@Controller
public class PromotionController {
	
	private static final Logger LOG = LoggerFactory.getLogger(PromotionController.class);
	
	@Resource
	private IPromotionService promotionService;
	
	//获得促销信息----------销售所用
	@RequestMapping(value="/retail/promotion/searchMerchPromotionForSale")
	public void searchMerchPromotionForSale(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		//用于只查询可用促销，（无效，过期）
		paramMap.put("is_clash", "1");
		paramMap.put("start_date", DateUtil.getToday());
		String status = MapUtil.getString(paramMap, "status");
		if(!status.equals("1")){
			paramMap.put("status", "1");
		}
		Map<String, Object> orderParams = new HashMap<String, Object>();
		orderParams.put("is_insistent", "desc");
		orderParams.put("create_date", "desc");
		orderParams.put("create_time", "desc");
		paramMap.put("order_params", orderParams);
		try {
			data = promotionService.searchMerchPromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("获得促销信息",e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	//获得促销信息
	@RequestMapping(value="/retail/promotion/searchMerchPromotion")
	public void searchMerchPromotion(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			data = promotionService.searchMerchPromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("获得促销信息",e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	//修改促销信息
	@RequestMapping(value="/retail/promotion/updateMerchPromotion")
	public void updateMerchPromotion(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		String paramsJson = request.getParameter("params");
		LOG.debug("paramsJson "+paramsJson );
//		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			String promotionMust = JsonUtil.object2json(paramMap.get("promotion_must"));
			String promotionShould = JsonUtil.object2json(paramMap.get("promotion_should"));
			String promotionAction = JsonUtil.object2json(paramMap.get("promotion_action"));
			paramMap.put("promotion_must", promotionMust);
			paramMap.put("promotion_should", promotionShould);
			paramMap.put("promotion_action", promotionAction);
			promotionService.updateMerchPromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("移除促销信息",e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//移除促销信息
	@RequestMapping(value="/retail/promotion/removeMerchPromotion")
	public void removeMerchPromotion(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			promotionService.removeMerchPromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("移除促销信息",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	//插入促销信息
	@RequestMapping(value="/retail/promotion/insertMerchPromotion")
	public void insertMerchPromotion(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		String paramsStr = request.getParameter("params");
//		System.out.println(paramsStr);
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			promotionService.insertMerchPromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("插入促销信息",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	// 查询促销流水
	@RequestMapping(value="/retail/promotion/searchMerchPromotionRecord")
	public void searchMerchPromotionRecord(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = Collections.EMPTY_LIST;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("merch_id", paramMap.get("ref_id"));
		try {
			data = promotionService.selectMerchPromotionRecord(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询查询促销流水错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	//查询促销奖品
	@RequestMapping(value="/retail/promotion/searchPromotionPrize")
	public void searchPromotionPrize(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			data = promotionService.searchPromotionPrize(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("查询促销奖品",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	//插入促销奖品
	@RequestMapping(value="/retail/promotion/insertPromotionPrize")
	public void insertPromotionPrize(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			List<Map<String, Object>> paramList = (List<Map<String, Object>>) paramMap.get("list");
			promotionService.insertPromotionPrize(paramList);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("插入促销奖品",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	//修改促销奖品
	@RequestMapping(value="/retail/promotion/updatePromotionPrize")
	public void updatePromotionPrize(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap=new HashMap<String, Object>();
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			promotionService.updatePromotionPrize(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("修改促销奖品",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
}
