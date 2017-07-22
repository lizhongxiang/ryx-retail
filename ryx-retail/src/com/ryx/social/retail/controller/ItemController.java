package com.ryx.social.retail.controller;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.SpellUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IItemService;
import com.ryx.social.retail.service.IOrderService;
import com.ryx.social.retail.service.IWhseService;
import com.ryx.social.retail.util.ParamUtil;


@Controller
public class ItemController {
	
	private Logger logger = LoggerFactory.getLogger(ItemController.class);
	
	@Resource
	private IItemService itemService;
	@Resource
	private IOrderService orderSerice;
	@Resource
	private IWhseService whseService;
	
	private Map<String, Object> getParamMap(HttpServletRequest request) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(userMap!=null) {
			paramMap.putAll(userMap);
			paramMap.put("merch_id", paramMap.get("ref_id"));
		}
		if(requestMap.get("params")!=null) { // 如果前台传merch_id会覆盖session中的merch_id
			paramMap.putAll((Map<String, Object>) requestMap.get("params"));
		} else {
			paramMap.putAll(requestMap);
		}
		return paramMap;
	}
	
	private Map<String, Object> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("merch_id", user.getRefId());
		return map;
	}
	
	/**
	 * 获取商品分类信息
	 */
	@RequestMapping(value="/retail/item/searchItemKind")
	public void searchItemKind(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getSessionInfo(request);
		paramMap.put("item_kind_id", request.getParameter("item_kind_id"));
		paramMap.put("item_kind_parent", request.getParameter("item_kind_parent"));
		paramMap.put("item_kind_level", request.getParameter("item_kind_level"));
		String status = request.getParameter("status");
		// 如果没有传status, 则默认查询可用的item_kind
		if(status ==null) {
			paramMap.put("status", "1");
		} else {
			paramMap.put("status", status);
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchItemKind(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商品分类错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 获取商品单位信息
	 */
	@RequestMapping(value="/retail/item/searchItemUnit")
	public void searchItemUnit(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchItemUnit(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商品单位错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 根据item_id或者item_bar获取商品信息list
	 */
	@RequestMapping(value="/retail/item/getItemDetail")
	public void searchMerchItemUnitJoinMerchItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		if(paramMap.get("page_index")==null) {
			paramMap.put("page_index", 1);
		}
		if(paramMap.get("page_size")==null) {
			paramMap.put("page_size", 20);
		}
		if(paramMap.get("status")==null) {
			paramMap.put("status", paramMap.get("status"));
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItemUnitJoinMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户商品属性和商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	/**
	 * 适用于商品管理中的新增重码商品查询
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/retail/item/searchItemDetail")
	public void searchItemDetail(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		if(paramMap.get("page_index")==null) {
			paramMap.put("page_index", 1);
		}
		if(paramMap.get("page_size")==null) {
			paramMap.put("page_size", 20);
		}
		if(paramMap.get("status")==null) {
			paramMap.put("status", paramMap.get("status"));
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchNoRemovedItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户商品属性和商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 销售新增商品专用, 如果merch_item中没有则新增商品
	 */
	@RequestMapping(value="/retail/item/searchMerchItemWithIncrease")
	public void searchMerchItemWithIncrease(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		if(!paramMap.containsKey("status")) {
			paramMap.put("status", "0,1,2");
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItemWithIncrease(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商品信息或新增商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 非烟入库新增商品专用, 如果merch_item中没有则新增商品
	 */
	@RequestMapping(value="/retail/item/searchMerchItemJoinWhseWithIncrease")
	public void searchMerchItemJoinWhseWithIncrease(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
//		if(!paramMap.containsKey("status")) {
//			paramMap.put("status", "1,2");
//		}
		if(!paramMap.containsKey("page_index")) {
			paramMap.put("page_index", 1);
		}
		if(!paramMap.containsKey("page_size")) {
			paramMap.put("page_size", 20);
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItemJoinWhseWithIncrease(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商品/库存信息或新增商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 商品管理, 查询商品列表, 先查询商户商品, 如果没有则返回基本商品列表
	 */
//	@RequestMapping(value="/retail/item/searchItem")
//	public void searchMerchItemOrItem(HttpServletRequest request, HttpServletResponse response) {
//		String code = Constants.SUCCESS;
//		String msg = Constants.SUCCESS_MSG;
//		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
//		/*
//		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
//		Map<String, Object> paramMap = RequestUtil.getParameterMap(request);
//		*/
//		paramMap.put("merch_id", paramMap.get("ref_id"));
//		if(!paramMap.containsKey("item_bar")) {
//			paramMap.put("item_bar", paramMap.get("big_bar"));
//		}
//		if(!paramMap.containsKey("page_index")) {
//			paramMap.put("page_index", 1);
//		}
//		if(!paramMap.containsKey("page_size")) {
//			paramMap.put("page_size", 20);
//		}
//		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
//		try {
//			data = itemService.searchMerchItemAndSoOn(paramMap);
//		} catch (Exception e) {
//			code = Constants.FAIL;
//			msg = Constants.FAIL_MSG;
//			logger.error(" = * = * = * = * = 查询商户商品或基本商品错误 = * = * = * = * = ", e);
//		}
//		ResponseUtil.write(request, response, code, msg, data, paramMap);
//	}
	
	@RequestMapping(value="/retail/item/searchItem")///retail/item/searchItemBySaoMa
	public void searchMerchItemItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("merch_id", paramMap.get("ref_id"));
		if(!paramMap.containsKey("item_bar")) {
			paramMap.put("item_bar", paramMap.get("big_bar"));
		}
		if(!paramMap.containsKey("page_index")) {
			paramMap.put("page_index", 1);
		}
		if(!paramMap.containsKey("page_size")) {
			paramMap.put("page_size", 20);
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItemBySaoMa(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户商品或基本商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	/**
	 * 商品管理, 仅查询商户商品列表
	 */
	@RequestMapping(value="/retail/item/searchMerchItem")
	public void searchMerchItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		if(paramMap.get("status")==null) paramMap.put("status", "1,2");
		if(paramMap.get("page_index")==null) paramMap.put("page_index", 1);
		if(paramMap.get("page_size")==null) paramMap.put("page_size", 20);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, 
				MapUtil.remain(paramMap, "page_index", "page_size", "page_count", "count"));
	}
	
	/**
	 * 查询异常商品列表
	 */
	@RequestMapping(value="/retail/item/searchUnqualifiedMerchItemList")
	public void findUnqualifiedMerchantItemList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("isUnqualified", true);
		if(paramMap.get("status")==null) paramMap.put("status", "1");
		if(paramMap.get("page_index")==null) paramMap.put("page_index", 1);
		if(paramMap.get("page_size")==null) paramMap.put("page_size", 20);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询异常商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, 
				MapUtil.remain(paramMap, "page_index", "page_size", "page_count", "count"));
	}
	
	
	/**
	 * 商品管理, 查询基础商品列表
	 */
	@RequestMapping(value="/item/searchBaseItem")
	public void searchItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getSessionInfo(request);
		paramMap.put("item_id", request.getParameter("item_id"));
		paramMap.put("item_bar", request.getParameter("item_bar"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = itemService.searchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询基础商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	

	@RequestMapping(value="/retail/item/searchMerchItemUnit")
	public void searchMerchItemUnit(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		if(paramMap.get("page_index")==null) {
			paramMap.put("page_index", 1);
		} 
		if(paramMap.get("page_size")==null) {
			paramMap.put("page_size", 20);
		} 
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = itemService.searchMerchItemUnit(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("page_count", paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商品包装错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	
	/**
	 * 商品管理, 查询商品详情和商品属性
	 */
	@RequestMapping(value="/retail/item/searchMerchItemJoinMerchItemUnit")
	public void searchMerchItemJoinMerchItemUnit(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getSessionInfo(request);
		paramMap.put("item_id", request.getParameter("item_id"));
		paramMap.put("item_bar", request.getParameter("item_bar"));
		Map<String, Object> data = null;
		try {
			List<Map<String, Object>> itemList = itemService.searchMerchItemJoinMerchItemUnit(paramMap);
			if(!itemList.isEmpty()) {
				data = itemList.get(0);
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户商品和商户商品属性错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 逻辑删除商户商品
	 */
	@RequestMapping(value="/retail/item/removeMerchItem")
	public void removeMerchItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			itemService.removeMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 删除商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 修改商户商品
	 */
	@RequestMapping(value="/retail/item/modifyMerchItem")
	public void modifyMerchItemAndUnit(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("short_code", SpellUtil.getFullSpell(MapUtil.getString(paramMap, "item_name")));
		paramMap.put("short_name", SpellUtil.getShortSpell(MapUtil.getString(paramMap, "item_name")));
		try {
			// 修改商品, 已经有item_id和seq_id 生成新增商品和商品属性的参数
			Map<String, Object> newMerchItemUnitMap = new HashMap<String, Object>();
			newMerchItemUnitMap.put("operation_type", "UPD");
			newMerchItemUnitMap.put("merch_id", paramMap.get("merch_id"));
			newMerchItemUnitMap.put("seq_id", paramMap.get("seq_id"));
			newMerchItemUnitMap.put("item_id", paramMap.get("item_id"));
			newMerchItemUnitMap.put("item_bar", paramMap.get("item_bar"));
			newMerchItemUnitMap.put("item_unit_name", paramMap.get("unit_name"));
			newMerchItemUnitMap.put("big_bar", paramMap.get("item_bar"));
			newMerchItemUnitMap.put("big_unit_name", paramMap.get("unit_name"));
			newMerchItemUnitMap.put("unit_ratio", 1);
			newMerchItemUnitMap.put("pri4", paramMap.get("pri4"));
			List<Map<String, Object>> merchItemUnitList = MapUtil.get(paramMap, "list", new ArrayList<Map<String, Object>>());
			// 已经有seq_id 获取新增商品属性需要的参数
			if(!merchItemUnitList.isEmpty()) {
				for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
					String operationType = MapUtil.getString(merchItemUnitMap, "operation_type");
					if("ADD".equalsIgnoreCase(operationType)) {
						merchItemUnitMap.put("seq_id", IDUtil.getId());
					}
					if(!"DEL".equalsIgnoreCase(operationType)) {
						merchItemUnitMap.put("item_id", paramMap.get("item_id"));
						merchItemUnitMap.put("merch_id", paramMap.get("merch_id"));
						merchItemUnitMap.put("item_bar", paramMap.get("item_bar"));
						merchItemUnitMap.put("item_unit_name", paramMap.get("unit_name"));
					} else {
						merchItemUnitMap.put("merch_id", paramMap.get("merch_id"));
					}
				}
			}
			merchItemUnitList.add(newMerchItemUnitMap);
			paramMap.put("list", merchItemUnitList);
			itemService.modifyMerchItemAndUnit(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 修改商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, paramMap);
	}

	/**
	 * 上架商品
	 */
	@RequestMapping(value="/retail/item/onsaleItem")
	public void onsaleItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("status", "1");
		try {
			itemService.modifyMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 上架商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	/**
	 * 下架商品
	 */
	@RequestMapping(value="/retail/item/unsaleItem")
	public void unsaleItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("status", "2");
		try {
			itemService.modifyMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 下架商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	/**
	 * 删除商品
	 */
	@RequestMapping(value="/retail/item/deleteItem")
	public void deleteItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("status", "0");
		try {
			itemService.modifyMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 删除商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	/**
	 * 恢复删除商品
	 */
	@RequestMapping(value="/retail/item/recoverItem")
	public void recoverItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("status", "1");
		try {
			itemService.modifyMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 删除商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	/**
	 * 新增商品
	 */
	@RequestMapping(value="/retail/item/createMerchItem")
	public void createMerchItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String itemId = MapUtil.getString(paramMap, "item_id", IDUtil.getId());
		String itemBar  = MapUtil.getString(paramMap, "item_bar");
		String unitName  = MapUtil.getString(paramMap, "unit_name");
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		try {
			// 商品属性
			paramMap.put("item_id", itemId);
			paramMap.put("status", "1");
			paramMap.put("create_date", DateUtil.getToday());
			paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
			// 商品基础包装
			Map<String, Object> minPack = new HashMap<String, Object>();
			minPack.put("merch_id", merchId);
			minPack.put("seq_id", IDUtil.getId());
			minPack.put("item_id", itemId);
			minPack.put("item_bar", itemBar);
			minPack.put("item_unit_name", unitName);
			minPack.put("big_bar", itemBar);
			minPack.put("item_kind_id", itemKindId);
			minPack.put("big_unit_name", unitName);
			minPack.put("unit_ratio", 1);
			minPack.put("pri4", paramMap.get("pri4"));
			List<Map<String, Object>> packList = MapUtil.get(paramMap, "list", new ArrayList<Map<String, Object>>());
			for(Map<String, Object> pack : packList) {
				pack.put("seq_id", IDUtil.getId());
				pack.put("merch_id", merchId);
				pack.put("item_id", itemId);
				pack.put("item_bar", itemBar);
				pack.put("item_unit_name", unitName);
			}
			packList.add(minPack);
			paramMap.put("list", packList);
			itemService.createMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 新增商户商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, paramMap);
	}
	
	/**
	 * 新增卷烟商品
	 */
	@RequestMapping(value="/retail/item/createMerchTobaccoItem")
	public void createMerchTobaccoItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getSessionInfo(request);
		try {
			paramMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
			if(!paramMap.containsKey("discount")) {
				paramMap.put("discount", 100);
			}
			paramMap.put("create_date", DateUtil.getToday());
			paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
			// 生成新增商品和商品属性的参数
			Map<String, Object> newMerchItemUnitMap = new HashMap<String, Object>();
			newMerchItemUnitMap.put("merch_id", paramMap.get("merch_id"));
			newMerchItemUnitMap.put("seq_id", IDUtil.getId());
			newMerchItemUnitMap.put("item_id", paramMap.get("item_id"));
			newMerchItemUnitMap.put("item_bar", paramMap.get("item_bar"));
			newMerchItemUnitMap.put("item_unit_name", paramMap.get("unit_name"));
			newMerchItemUnitMap.put("big_bar", paramMap.get("item_bar"));
			newMerchItemUnitMap.put("big_unit_name", paramMap.get("unit_name"));
			newMerchItemUnitMap.put("unit_ratio", 1);
			newMerchItemUnitMap.put("pri4", paramMap.get("pri4"));
			List<Map<String, Object>> merchItemUnitList = new ArrayList<Map<String, Object>>();
			if(paramMap.containsKey("list")) {
				// 获取新增商品属性需要的参数
				merchItemUnitList = (List<Map<String, Object>>) paramMap.get("list");
				for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
					merchItemUnitMap.put("seq_id", IDUtil.getId());
				}
			}
			merchItemUnitList.add(newMerchItemUnitMap);
			paramMap.put("list", merchItemUnitList);
			itemService.createMerchTobaccoItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 新增商户卷烟商品错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 批量新增商品
	 */
	@RequestMapping(value="/retail/item/createMerchTobaccoItemList")
	public void createMerchTobaccoItemList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap = RequestUtil.getParameterMap(request);
		try {
			List<Map<String, Object>> MerchItemList = (List<Map<String, Object>>) ((Map<String, Object>)paramMap.get("params")).get("item_list");
			
			for(Map<String, Object> merchItemMap : MerchItemList) {
				merchItemMap.put("merch_id", userMap.get("ref_id"));
				if(merchItemMap.get("pri1")==null) {
					merchItemMap.put("pri1", BigDecimal.ZERO);
				}
				merchItemMap.put("cost", merchItemMap.get("pri1"));
				if(merchItemMap.get("item_id")==null) {
					merchItemMap.put("item_id", IDUtil.getId());
				}
				if(!merchItemMap.containsKey("discount")) {
					merchItemMap.put("discount", 100);
				}
				paramMap.put("create_date", DateUtil.getToday());
				paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
				
				// 生成新增商品和商品属性的参数
				List<Map<String, Object>> merchItemUnitList = new ArrayList<Map<String, Object>>();
				Map<String, Object> newMerchItemUnitMap = new HashMap<String, Object>();
				newMerchItemUnitMap.put("merch_id", userMap.get("ref_id"));
				newMerchItemUnitMap.put("seq_id", IDUtil.getId());
				newMerchItemUnitMap.put("item_id", merchItemMap.get("item_id"));
				newMerchItemUnitMap.put("item_bar", merchItemMap.get("item_bar"));
				newMerchItemUnitMap.put("item_unit_name", merchItemMap.get("unit_name"));
				newMerchItemUnitMap.put("big_bar", merchItemMap.get("item_bar"));
				newMerchItemUnitMap.put("big_unit_name", merchItemMap.get("unit_name"));
				newMerchItemUnitMap.put("unit_ratio", 1);
				newMerchItemUnitMap.put("pri4", merchItemMap.get("pri4"));
				if(merchItemMap.containsKey("list")) {
					// 获取新增商品属性需要的参数
					merchItemUnitList = (List<Map<String, Object>>) merchItemMap.get("list");
					for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
						merchItemUnitMap.put("seq_id", IDUtil.getId());
					}
				}
				merchItemUnitList.add(newMerchItemUnitMap);
				
				// 如果有big_bar, 则说明是卷烟, 或者用TOBACCO标示
				if(merchItemMap.get("big_bar")!=null && merchItemMap.get("item_bar")!=null && !merchItemMap.get("item_bar").equals(merchItemMap.get("item_id"))) {
					Map<String, Object> newMerchItemUnitMap2 = new HashMap<String, Object>();
					newMerchItemUnitMap2.put("merch_id", userMap.get("ref_id"));
					newMerchItemUnitMap2.put("seq_id", IDUtil.getId());
					newMerchItemUnitMap2.put("item_id", merchItemMap.get("item_id"));
					newMerchItemUnitMap2.put("item_bar", merchItemMap.get("item_bar"));
					newMerchItemUnitMap2.put("item_unit_name", merchItemMap.get("unit_name"));
					newMerchItemUnitMap2.put("big_bar", merchItemMap.get("big_bar"));
					newMerchItemUnitMap2.put("big_unit_name", merchItemMap.get("big_unit_name"));
					newMerchItemUnitMap2.put("unit_ratio", merchItemMap.get("unit_ratio"));
					newMerchItemUnitMap2.put("pri4", merchItemMap.get("pri_drtl"));
					merchItemUnitList.add(newMerchItemUnitMap2);
				}
				merchItemMap.put("list", merchItemUnitList);
			}
			itemService.createMerchTobaccoItem(MerchItemList);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 新增商户卷烟商品列表错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 商品管理, 操作商品信息和商品属性
	 */
	@RequestMapping(value="/retail/item/operateMerchItem")
	public void operateMerchItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = this.getParamMap(request);
		paramMap.remove("status");
		if(paramMap.get("item_name")!=null) {
			paramMap.put("short_code", SpellUtil.getFullSpell((String)paramMap.get("item_name")));
			paramMap.put("short_name", SpellUtil.getShortSpell((String)paramMap.get("item_name")));
		}
		if(paramMap.get("discount")==null) {
			paramMap.put("discount", 100);
		}
		if("C".equals(paramMap.get("type"))) {
			paramMap.put("item_id", IDUtil.getId());
			paramMap.put("create_date", DateUtil.getToday());
			paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
		}
		try {
			itemService.operateMerchItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 新增商户商品和商户商品属性错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 商品管理, 操作商品信息和商品属性
	 */
	@RequestMapping(value="/retail/item/updateMerchItems")
	public void updateMerchItems(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = this.getParamMap(request);
		paramMap.remove("status");
		if(paramMap.get("item_name")!=null) {
			paramMap.put("short_code", SpellUtil.getFullSpell((String)paramMap.get("item_name")));
			paramMap.put("short_name", SpellUtil.getShortSpell((String)paramMap.get("item_name")));
		}
		if(paramMap.get("discount")==null) {
			paramMap.put("discount", 100);
		}
		try {
			itemService.updateMerchItems(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 新增商户商品和商户商品属性错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	
	
	/**
	 * 检查此商品是否可以被删除
	 */
	@RequestMapping(value="/retail/item/canIDelete")
	public void canIDelete(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = orderSerice.searchPurchSaleOrderLineByItemId(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 检查此商品是否可以被删除时错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/retail/item/searchMerchItemByLucene")
	public void searchMerchItemByLucene(HttpServletRequest request,HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		paramMap.put("merch_id", paramMap.get("ref_id"));
		// 默认不分页
		if(!paramMap.containsKey("page_index")) {
			paramMap.put("page_index", -1);
		}
		if(!paramMap.containsKey("page_size")) {
			paramMap.put("page_size", -1);
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = itemService.searchMerchItemByLucene(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("count", paramMap.get("count"));
			pageParam.put("page_count", paramMap.get("page_count"));
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = lucene查询商户商品错误 = * = * = * = * = ",e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	//更新luce数据
	@RequestMapping(value="/retail/item/updateluce")
	public void updateLuce(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		try {
//			String cgtItemUrl="http://192.168.0.3:8889/ryxShopAssistantInterface/tobacco/retail/db2luce";
//			String dataJson = HttpUtil.post(cgtItemUrl, null);
			itemService.updateLuce();
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("更新luce失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg,null);
	}
	//调用luce   传入key("模糊商品名称")
	@RequestMapping(value="/retail/item/getluce")
	public void getLuce(HttpServletRequest request ,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List luceData=null;//luce返回
		Map<String, String> paramsMap=new HashMap<String, String>();//参数
		List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
		try {
			request.setCharacterEncoding("utf-8");			
//			paramsMap.put("merchId", "1037010507633");
//			paramsMap.put("key", "金");			
			String ref_id=IdentityUtil.getUserMap(request).get("ref_id")+"";//店铺内码
			paramsMap.put("merchId", ref_id);			
			paramsMap.putAll(JsonUtil.json2Map(request.getParameter("params")));
			paramsMap.put("key", "QQ");
//			String cgtItemUrl="http://192.168.0.3:8889/ryxShopAssistantInterface/tobacco/retail/fuzzySearch/json";
//			String dataJson = HttpUtil.post(cgtItemUrl, paramsMap);
//			data=JsonUtil.json2List(dataJson);			
			luceData=itemService.getLuceByKey(paramsMap);			
			for (int i = 0; i < luceData.size(); i++) {			
				Map<String, Object> itemMap=new HashMap<String, Object>();
				itemMap.put("item_id", luceData.get(i));
				itemMap.put("merch_id", ref_id);
				data.addAll(itemService.searchMerchItem(itemMap));
			}			
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("调用luce接口失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
	//新增或修改item
	@RequestMapping(value="/retail/item/updateOrInsertItems")
	public void updateOrInsertItems(HttpServletRequest request ,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=getParamMap(request);
		String merchId=(String) userMap.get("ref_id");
		paramsMap.put("merch_id",merchId);
		try{
			itemService.updateOrInsertItems(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("新增或修改item",e);
		// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg,null);
	}
	
//	插入预警商品信息
	@RequestMapping(value="/retail/item/insertMerchWarningItem")
	public void insertMerchWarningItem(HttpServletRequest request ,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		List<String> paramItemId = JsonUtil.json2List(request.getParameter("params"));
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("item_ids", paramItemId);
		String merchId=(String) userMap.get("ref_id");
		paramMap.put("merch_id",merchId);
		try{
			itemService.insertMerchWarningItem(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("新增预警商品信息",e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	
	// 智能终端迁移数据错误
	@RequestMapping(value="/retail/item/transferPOSData")
	public void transferPOSData(HttpServletRequest request ,HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try{
			itemService.transferPOSData(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 智能终端迁移数据错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//导出非烟
	@RequestMapping(value = "/item/exportItemTable")
	public void exportItemTable(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		try {
			 OutputStream os = response.getOutputStream();
			 response.reset();// 清空输出流   
			 response.setHeader("Content-disposition", "attachment; filename="+MapUtil.getString(paramMap, "lice_id")+".xls");// 设定输出文件头   
			itemService.exportItemTable(paramMap, os);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("导出商品错误", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}

	//导入非烟
	@RequestMapping(value = "/item/importItemTable")
	public void importItemTable(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		try {
			itemService.importItemTable(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("导入商品错误", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//导出卷烟
	@RequestMapping(value = "/item/exportTobaccoTable")
	public void exportTobaccoTable(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
//				ParamUtil.getParamMap(request);
		try {
			OutputStream os = response.getOutputStream();// 取得输出流   
			paramMap.put("os", os);
			response.reset();// 清空输出流   
//		    response.setHeader("Content-disposition", "attachment; filename="++".xls");// 设定输出文件头   
		    response.addHeader("Content-Disposition", "attachment; filename=\""+URLEncoder.encode(MapUtil.getString(paramMap, "lice_id"), "UTF-8")+".xls\"");
			itemService.exportTobaccoTable(paramMap, os);
		    
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("导出卷烟错误", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//导入卷烟
	@RequestMapping(value = "/item/importTobaccoTable")
	public void importTobaccoTable(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = JsonUtil.json2Map(request.getParameter("params"));
		try {
			itemService.importTobaccoTable(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("导入卷烟错误", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
}
