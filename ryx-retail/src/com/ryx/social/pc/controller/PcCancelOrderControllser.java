package com.ryx.social.pc.controller;

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
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.social.pc.service.IPcCancelOrderService;
import com.ryx.social.retail.service.ICgtOrderService;

@Controller
public class PcCancelOrderControllser {
	private static Logger logger=LoggerFactory.getLogger(PcCancelOrderControllser.class);
	
	@Resource
	private IPcCancelOrderService pcCancelOrderService;
	//撤销销售单
	@RequestMapping(value = "/pc/sale/cancelSaleOrder")
	public void cancelSaleOrder(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramsMap=new HashMap<String, Object>();
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			pcCancelOrderService.cancelSaleOrder(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("----",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	//撤销退货单
	@RequestMapping(value = "/pc/sale/cancelReturnOrder")
	public void cancelReturnOrder(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramsMap=new HashMap<String, Object>();
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			pcCancelOrderService.cancelReturnOrder(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("----",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	//撤销盘点单
	@RequestMapping(value = "/pc/sale/cancelWhseTurn")
	public void cancelWhseTurn(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramsMap=new HashMap<String, Object>();
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			pcCancelOrderService.cancelWhseTurn(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("----",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	//撤销入库单
	@RequestMapping(value = "/pc/sale/cancelPurchOracle")
	public void cancelPurchOracle(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramsMap=new HashMap<String, Object>();
		paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
		try {
			pcCancelOrderService.cancelPuchOrder(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("----",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
}
