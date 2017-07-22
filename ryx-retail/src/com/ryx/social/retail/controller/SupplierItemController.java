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

import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IMerchShoppingCartService;
import com.ryx.social.retail.service.ISupplierItemService;

/**
 * 供应商商品
 * @author 隋长国
 *
 */
@Controller
public class SupplierItemController {
	private static final Logger LOG = LoggerFactory.getLogger(SupplierItemController.class);
	@Resource
	private ISupplierItemService  supplierItemServiceImpl;
	@Resource
	private IMerchShoppingCartService merchShoppingCartServiceImpl;
	/**
	 * 获取供应商商品列表
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/supplier/item/list")
	public void getSupplierItemList(HttpServletRequest request, HttpServletResponse response){
		String params = request.getParameter("params");
		SessionInfo user = IdentityUtil.getUser(request);
		@SuppressWarnings("unchecked")
		Map<String,String> map = JsonUtil.json2Map(params);
		try {
			Map<String,Object> items = supplierItemServiceImpl.getSupplierItemList(map);
			
			Map<String, String> totalInfo = merchShoppingCartServiceImpl.getCartTotalInfo(user.getMerchId());
			if(totalInfo != null && totalInfo.get("ITEM_TOTAL") == null){
				totalInfo.put("ITEM_TOTAL", "0");
			}
			if(totalInfo != null && totalInfo.get("TOTAL_PRICE") == null){
				totalInfo.put("TOTAL_PRICE", "0");
			}
			Map<String,Object> mapInfo  = new HashMap<String, Object>();
			mapInfo.put("itemList", items);
			mapInfo.put("totalInfo", totalInfo);
			ResponseUtil.write(request, response, mapInfo);
		} catch (Exception e) {
			LOG.error("查询供应商商品列表出错"+e);
		}
		
	}
	
	/**
	 * 获取供应商商品列表
	 * @param request
	 * @param response
	 */
	@RequestMapping(value ="/ipos/supplier/item/list")
	public void getSupplierItemListByPos(HttpServletRequest request, HttpServletResponse response){
		String params = request.getParameter("params");
		if(params!= null && !params.equals("")){
			@SuppressWarnings("unchecked")
			Map<String,String> map = JsonUtil.json2Map(params);
			//String param = map.get("param");
			String pageNum = map.get("pageNum");
			String pageSize = map.get("pageSize");
			if(pageSize == null || pageSize.equals("") ||  pageNum == null || pageNum.equals("")){
				ResponseUtil.write(request, response, "1001", "页码参数错误", "");
			}
			try {
				Map<String,Object> items = supplierItemServiceImpl.getSupplierItemList(map);
				ResponseUtil.write(request,response,items);
			} catch (Exception e) {
				LOG.error("查询供应商商品列表出错"+e);
			}
		}
		else{
			ResponseUtil.write(request, response, "1001", "缺少必要参数", "");
		}
		
	}
	
}
