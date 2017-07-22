package com.ryx.social.retail.controller;


import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IMerchOrderService;
import com.ryx.social.retail.service.IMerchService;


@Controller
public class MerchOrderController {
	private static final Logger LOG = LoggerFactory.getLogger(MerchOrderController.class);
	
	@Resource
	private IMerchOrderService merchOrderServiceImpl;
	@Resource
	private IMerchService merchService;
	/**
	 * 提交订单
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/supplier/order/submit")
	public void orderSumit(HttpServletRequest request, HttpServletResponse response){
		String cartIDS = request.getParameter("cartIDS");
		String addressID = request.getParameter("addressID");
		SessionInfo user = IdentityUtil.getUser(request);
		String merchID = user.getMerchId();
		try {
			List<Map<String,String>> list = merchOrderServiceImpl.insertOrder(merchID,cartIDS, addressID);
			List<Map<String,Object>> orderList = merchOrderServiceImpl.getOrderList(list,merchID);
			@SuppressWarnings("unchecked")
			Map<String,Object> map = merchService.getMerchInfo(merchID);
			map.put("orderList", orderList);
			ResponseUtil.write(request, response, map);
		} catch (Exception e) {
			LOG.error("提交订单出错", e);
		}
	}
	/**
	 * 获取订单历史数据
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/supplier/order/listHis")
	public void getSupplierItemListHis(HttpServletRequest request, HttpServletResponse response){
		String params = request.getParameter("params");
		@SuppressWarnings("unchecked")
		Map<String,String> map = JsonUtil.json2Map(params);
		SessionInfo user = IdentityUtil.getUser(request);
		String merchID = user.getMerchId();
		map.put("merchID", merchID);
		try {
			Map<String,Object> orderHis = merchOrderServiceImpl.getOrderListHis(map);
			ResponseUtil.write(request, response, orderHis);
		} catch (Exception e) {
			LOG.error("获取历史订单出错", e);
		}
	}
	/**
	 * 获取订单详情
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/supplier/order/info")
	public void getOrderInfo(HttpServletRequest request, HttpServletResponse response){
		String params = request.getParameter("params");
		@SuppressWarnings("unchecked")
		Map<String,String> map = JsonUtil.json2Map(params);
		String orderID = map.get("orderID");
		String supplierID = map.get("supplierID");
		SessionInfo user = IdentityUtil.getUser(request);
		String merchID = user.getMerchId();
		map.put("merchID", merchID);
		try {
			Map<String,Object> orderInfo = merchOrderServiceImpl.getOrderInfo(orderID, merchID,supplierID);
			@SuppressWarnings("unchecked")
			Map<String,Object> addressInfo = merchService.getMerchInfo(merchID);
			orderInfo.put("addressInfo", addressInfo);
			ResponseUtil.write(request, response, orderInfo);
		} catch (Exception e) {
			LOG.error("获取订单详细信息出错", e);
		}
	}
	@RequestMapping("/retail/supplier/order/submitforipos")
	public void  orderSubmitForPos(HttpServletRequest request, HttpServletResponse response){
		String params = request.getParameter("params");
		@SuppressWarnings("unchecked")
		Map<String,Object> pramMap = JsonUtil.json2Map(params);
		SessionInfo user = IdentityUtil.getUser(request);
		pramMap.put("merchID", user.getMerchId());
		try {
			merchOrderServiceImpl.orderSubmitForPos(pramMap);
			ResponseUtil.write(request, response, "");
		} catch (Exception e) {
			LOG.error("获取订单详细信息出错", e);
			ResponseUtil.write(request, response, "1001","",null);
		}
	}
	
	
}
