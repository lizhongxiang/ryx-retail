package com.ryx.social.retail.controller;

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
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IMerchSalePromotionService;
@Controller
public class MerchSalePromotionController {
	
	private Logger logger = LoggerFactory.getLogger(MerchSalePromotionController.class);
	
	@Resource
	private IMerchSalePromotionService merchSalePromotionService;
	
	// 获取session中的ref_id和request中的参数, 如果需要认证身份, 判断merch_id==ref_id
	private Map<String, Object> getParamMap(HttpServletRequest request) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(userMap!=null) {
			paramMap.put("merch_id", userMap.get("ref_id"));
			paramMap.put("ref_id", userMap.get("ref_id"));
		}
		if(requestMap.get("params")!=null) {
			paramMap.putAll((Map<String, Object>) requestMap.get("params"));
		} else {
			paramMap.putAll(requestMap);
		}
		return paramMap;
	}
	
	//添加
	@RequestMapping(value="/retail/sales/addSalePromotion")
	public void createSalePromotion(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		paramMap.put("create_date", DateUtil.getToday());
		paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
		paramMap.put("promotion_id", IDUtil.getId());
		try {
			merchSalePromotionService.createSalePromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("添加销售活动失败",e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//删除
	@RequestMapping(value="/retail/sales/delSalePromotion")	
	public void removeSalePromotion(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		try {
			merchSalePromotionService.removeSalePromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("删除销售活动失败",e);
		}
		ResponseUtil.write(request, response, code,msg, null);
	}
	
	@RequestMapping(value="/retail/sales/searchSalePromotion")	
	public void searchSalePromotion(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		Map<String, Object> data = null;
		try {
			List<Map<String, Object>> promotionList = merchSalePromotionService.searchSalePromotion(paramMap);
			if(!promotionList.isEmpty()) {
				data = promotionList.get(0);
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("查看销售活动失败",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	//修改
	@RequestMapping(value="/retail/sales/updateSalePromotion")	
	public void modifySalePromotion(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		try {		
			merchSalePromotionService.modifySalePromotion(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("修改销售活动失败",e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	} 
	
}
