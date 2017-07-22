package com.ryx.social.retail.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IMerchShoppingCartService;


@Controller
public class MerchShoppingCartController {
	private static final Logger LOG = LoggerFactory.getLogger(MerchShoppingCartController.class);
	
	@Resource
	private IMerchShoppingCartService merchShoppingCartServiceImpl;
	
	/**
	 * 添加购物车
	 * @param request
	 * @param response
	 */
	@RequestMapping(value ="/retail/supplier/cart/add")
	public void getSupplierItemListByPos(HttpServletRequest request, HttpServletResponse response){
		String params = request.getParameter("params");
		@SuppressWarnings("unchecked")
		Map<String,String> cart = JsonUtil.json2Map(params);
		SessionInfo user = IdentityUtil.getUser(request);
		cart.put("merchID", user.getMerchId());
		try {
			merchShoppingCartServiceImpl.addMerchShoppingCart(cart);
			ResponseUtil.write(request, response, "");
		} catch (SQLException e) {
			LOG.error("POS接口获取供应商商品出错",e);
			ResponseUtil.write(request, response, "1001", "缺少必要参数", "");
		}
	}
	
	/**
	 * 获取零售户购物车列表
	 * @param request
	 * @param response
	 */
	@RequestMapping(value ="/retail/supplier/cart/list")
	public void getMerchCartLsit(HttpServletRequest request, HttpServletResponse response){
		SessionInfo user = IdentityUtil.getUser(request);
		String merchID = user.getMerchId();
		try {
			List<Map<String,Object>> supplierList =  merchShoppingCartServiceImpl.getMerchCartLsit(merchID);
			ResponseUtil.write(request, response, supplierList);
		} catch (Exception e) {
			LOG.error("获取零售户购物车出错",e);
			ResponseUtil.write(request, response, "1001", "", "");
		}
	}
	/**
	 * 更新购物车状态
	 * @param request
	 * @param response
	 */
	@RequestMapping(value ="/retail/supplier/cart/update")
	public void updateCartStatus(HttpServletRequest request, HttpServletResponse response){
		SessionInfo user = IdentityUtil.getUser(request);
		String params = request.getParameter("params");
		@SuppressWarnings("unchecked")
		Map<String,String> cart = JsonUtil.json2Map(params);
		cart.put("merchID", user.getMerchId());
		try {
			merchShoppingCartServiceImpl.updateCartStatus(cart);
			ResponseUtil.write(request, response, "");
		} catch (Exception e) {
			LOG.error("更新购物车状态出错",e);
			ResponseUtil.write(request, response,"1001", "更新状态成功","");
		}
	}
	/**
	 * 更新购物车状态
	 * @param request
	 * @param response
	 */
	@RequestMapping(value ="/retail/supplier/cart/delteAll")
	public void deletAllCarts(HttpServletRequest request, HttpServletResponse response){
		String params = request.getParameter("params");
		Map<String,String> map = JsonUtil.json2Map(params);
		SessionInfo user = IdentityUtil.getUser(request);
		String merchID = user.getMerchId();
		map.put("merchID", merchID);
		try {
			merchShoppingCartServiceImpl.deleteAllCart(map);
			ResponseUtil.write(request, response, "删除成功");
		} catch (Exception e) {
			LOG.error("清空购物车失败",e);
			ResponseUtil.write(request, response,"1001", "清空购物车失败","");
		}
	}
	
	
//	public  static void main(String []args){
//		System.out.println(DateUtil.currentDatetime());
//	}
	

}
