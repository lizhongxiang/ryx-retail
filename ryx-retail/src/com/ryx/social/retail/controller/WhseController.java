package com.ryx.social.retail.controller;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IStatisticsService;
import com.ryx.social.retail.service.IWhseService;
import com.ryx.social.retail.util.ParamUtil;

@Controller
public class WhseController {

	private static final Logger LOG = LoggerFactory.getLogger(WhseController.class);
	
	@Resource
	IWhseService whseService;
	@Resource
	IStatisticsService statisticsService;
	
	private Map<String, Object> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("merch_id", user.getRefId());
		return map;
	}
	@RequestMapping(value = "/retail/whse/getItemDetailWithWhse")
	public void searchMerchItemUnitJoinMerchItemJoinWhseMerch(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		if (paramMap.containsKey("big_bar")) {
			paramMap.put("item_bar", MapUtil.getString(paramMap, "big_bar"));
		}
		if (paramMap.containsKey("page_index")) {
			paramMap.put("page_index", 1);
		}
		if (paramMap.containsKey("page_size")) {
			paramMap.put("page_size", 20);
		}
//		
//		Map<String, Object> paramMap = getSessionInfo(request);
//		String itemId = request.getParameter("item_id");
//		String seqId = request.getParameter("seq_id");
//		String bigBar = request.getParameter("big_bar");
//		
//		paramMap.put("item_id", itemId);
//		paramMap.put("seq_id", request.getParameter("seq_id"));
//		paramMap.put("item_bar", request.getParameter("big_bar"));
//		paramMap.put("big_bar", request.getParameter("big_bar"));
//		String pageIndex = request.getParameter("page_index");
//		if(pageIndex==null) {
//			paramMap.put("page_index", 1);
//		}
//		String pageSize = request.getParameter("page_size");
//		if(pageSize==null) {
//			paramMap.put("page_size", 20);
//		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = whseService.searchMerchItemUnitJoinMerchItemJoinWhseMerch(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("出错:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 获取原始库存列表
	 */
	@RequestMapping(value = "/retail/whse/getOriginWhseList")
	public void getOriginWhseList(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> result = null;
		try {
			SessionInfo user = IdentityUtil.getUser(request);
			String cgtComId = user.getComId();
			String refId = user.getRefId();
			String itemBar=request.getParameter("itemBar");
			Map<String, Object> map = new HashMap<String, Object>();
			String page_size=request.getParameter("page_size");
			String page_inded=request.getParameter("page_index");
			if(page_size==null||page_size.equals("")){
				page_size="20";
			}
			if(page_inded==null||page_inded.equals("")){
				page_inded="1";
			}
			if(itemBar==null||itemBar.equals("")){
				itemBar=null;
			}
			map.put("cgtComId", cgtComId);
			map.put("refId", refId);
			map.put("itemBar",itemBar);
			map.put("page_size", page_size);
			map.put("page_index", page_inded);
			result = whseService.getOriginWhseList(map);		
//			ResponseUtil.write(request, response, code,msg,result,map);
			ResponseUtil.write(request, response, code, msg, result,map);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("出错:", e);
			ResponseUtil.write(request, response, code, msg, null);
		}
	}

	/**
	 * 更新原始库存
	 */
	@RequestMapping(value = "/retail/whse/updateOriginWhseList")
	public void updateOriginWhseList(HttpServletRequest request,
			HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		try {
			//IdentityUtil里获取的是用户登录的信息
			SessionInfo user = IdentityUtil.getUser(request);
			String cgtComId = user.getComId();
			String refId = user.getRefId();
			String curDate = DateUtil.getToday();
			//原始库存参数
			String originParams = request.getParameter("params");
			LOG.debug("==originParams=="+originParams);
			Map<String, Object> map = new Gson().fromJson(originParams, new TypeToken<Map<String, Object>>() {}.getType());
			map.put("cgtComId", cgtComId);
			map.put("refId", refId);
			map.put("curDate", curDate);
			whseService.updateOriginWhseList(map);
			ResponseUtil.write(request, response, code, msg, "更新原始库存成功");
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("出错:", e);
			ResponseUtil.write(request, response, code, msg, null);
		}
	}

	@RequestMapping(value = "/retail/whse/searchWhseMerchJoinMerchItemUnit")
	public void searchWhseMerchJoinMerchItemUnit(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			Map<String, Object> paramMap = ParamUtil.getParamMap(request);
//			paramMap.put("status", "1,2");//2015-3-10 11:06 朱鹏,此处添加状态是为了在库存盘点扫码查询时不查询已删除的商品
			if(paramMap.get("page_index")==null) {
				paramMap.put("page_index", 1);
			}
			if(paramMap.get("page_size")==null) {
				paramMap.put("page_size", 20);
			}
			data = whseService.searchMerchItemUnitJoinMerchItemJoinWhseMerchForWhse(paramMap);
			pageParam.put("page_index", 1);
			pageParam.put("page_size", 20);
			pageParam.put("page_count", paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 根据包装条码查询商品库存错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code,msg, data, pageParam);	
	}
	
	/**
	 * 获取库存列表
	 */
	@RequestMapping(value = "/retail/whse/getWhseMerchList")
	public void searchMerchItemAndWhse(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			if(paramMap.get("page_index")==null) paramMap.put("page_index", 1);
			if(paramMap.get("page_size")==null) paramMap.put("page_size", 20);
			data = whseService.searchMerchItemAndReasonWhse(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("page_count", paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询商品库存错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	
	/**
	 * 查询异常库存列表
	 */
	@RequestMapping(value = "/retail/whse/searchUnqualifiedWhseMerchList")
	public void findUnqualifiedMerchantInventoryList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			paramMap.put("isUnqualified", true);
			if(paramMap.get("page_index")==null) paramMap.put("page_index", 1);
			if(paramMap.get("page_size")==null) paramMap.put("page_size", 20);
			data = whseService.findUnqualifiedMerchantInventoryList(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("page_count", paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询商品库存错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}

	/**
	 * 更新合理库存
	 */
	@RequestMapping(value = "/retail/whse/updateWarnWhseList")
	public void modifyWhseWarnQuantity(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			whseService.modifyWhseWarnQuantity(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 更新合理库存错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	
	/**
	 * 库存盘点, 获取商品信息+库存信息
	 */
	@RequestMapping(value="/retail/whse/searchMerchItemJoinWhseMerchByLucene")
	public void searchMerchItemJoinWhseMerchByLucene(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		// item_kind_id, key
		paramMap.put("merch_id", paramMap.get("ref_id"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = whseService.findMerchItemAndWhseMerchByLucene(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("page_count", paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count"));
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询商户商品库存错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	//合理库存搜索查询（建议合理库存量）
	@RequestMapping(value="/retail/whse/searchMerchItemJoinAdvWhseByLucene")
	public void searchMerchItemJoinAdvWhseByLucene(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		// item_kind_id, key
		paramMap.put("merch_id", paramMap.get("ref_id"));
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = whseService.searchMerchItemJoinAdvWhseByLucene(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("page_count", paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count"));
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询商户商品库存错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	/**
	 * 库存盘点，库存列表
	 */
	@RequestMapping(value="/retail/whse/getStocktakingList")
	public void getStocktakingList(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			Map<String, Object> paramMap = ParamUtil.getParamMap(request);
			if(paramMap.get("page_index")==null) paramMap.put("page_index", 1);
			if(paramMap.get("page_size")==null) paramMap.put("page_size", 20);
			data = whseService.searchMerchItemUnitJoinMerchItemJoinWhseMerch(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("page_count", paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count"));
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("出错：",e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	
	/**
	 * 按类型统计商户商品库存量和金额
	 */
	@RequestMapping(value="/retail/whse/getItemKindList")
	public void searchMerchWhseQuantityAndAmountByKind(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> data = null;
		try {
			data = whseService.searchMerchWhseQuantityAndAmountByKind(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 按类型统计商户商品库存量和金额错误 = * = * = * = * = ",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 梁凯	2014年5月29日14:30:56
	 * 提交库存盘点
	 */
	@RequestMapping(value="/retail/whse/submitWhseTurn")
	public void submitWhseTurn(HttpServletRequest request, HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		String today= DateUtil.getToday();
		paramMap.put("turn_id", IDUtil.getId());
		paramMap.put("turn_date", today);
		paramMap.put("crt_date", today);
		paramMap.put("crt_time", DateUtil.getCurrentTime().substring(8));
		paramMap.put("operator", paramMap.get("user_code"));
		paramMap.put("status", "02");
		try {
			whseService.createWhseTurn(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 提交库存盘点错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 为了保留接口名, 定义一个临时方法
	 */
	@RequestMapping("/retail/whse/updateStocktakingList")
	public void submitWhseTurnTemp(HttpServletRequest request, HttpServletResponse response){
		submitWhseTurn(request, response);
	}
	
	/**
	 * 盘点记录
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/retail/whse/getTakeStock")
	public void getTakeStock(HttpServletRequest request,
			HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String,Object>> result=null;
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=RequestUtil.getParameterMap(request);
		try {
			Map<String, Object> map=new HashMap<String, Object>();
//			map.putAll(m)
			map.putAll((Map<String,Object>)paramsMap);
			map.put("merch_id", userMap.get("ref_id"));
			//IdentityUtil里获取的是用户登录的信息
			
			if(map.get("page_index")==null||map.get("page_index").equals("")){
				map.put("page_index", "1");
			}
			if(map.get("page_size")==null||map.get("page_size").equals("")){
				map.put("page_size", "20");
			} 
			result=whseService.getWhseTurn(map);
			ResponseUtil.write(request, response, code,msg,(List<Map<String, Object>>)result,map);
//			ResponseUtil.write(request, response, code,msg,result);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("出错：",e);
			ResponseUtil.write(request, response, code,msg,null);
		}	
	}	
	
	/**
	 * 盘点记录,详细信息
	 */
	@RequestMapping(value = "/retail/whse/getTakeStockXiangXi")
	public void getTakeStockXiangXi(HttpServletRequest request,
			HttpServletResponse response){
		String Code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String,Object>> result=null;
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=RequestUtil.getParameterMap(request);
		Map<String, Object> turnMap=new HashMap<String, Object>();
		try {
			turnMap.putAll(paramsMap);
			turnMap.put("merch_id",userMap.get("ref_id"));			
			result=whseService.getWhseTurnXiangXi(turnMap);
			ResponseUtil.write(request, response, Code,msg,result);
		} catch (Exception e) {
			Code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("出错：",e);
			ResponseUtil.write(request, response, Code,msg,null);
		}
		
	}
	
	//修改库存，通过tobacco service 
	@RequestMapping(value="/retail/whse/modifyWhseByTBCSVR")
	public void modifyWhseByTBCSVR(HttpServletRequest request,HttpServletResponse response){
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=new HashMap<String, Object>();
		String json=request.getParameter("params");
		if(json!=null){
			paramsMap.putAll(JsonUtil.json2Map(json));
		}
		paramsMap.put("page_index", "-1");
		paramsMap.put("page_size", "-1");
		paramsMap.put("merch_id", userMap.get("ref_id"));
		paramsMap.put("item_kind_id", "01");
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		try {
			code=whseService.modifyWhseByTBCSVR(paramsMap).toString();
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("读取updateWhseByTobaccoServerGetWhseQty失败: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//修改库存，通过tobacco service 
	@RequestMapping(value="/retail/whse/updateShowWhseStatus")
	public void updateShowWhseStatus(HttpServletRequest request,HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramsMap = ParamUtil.getParamMap(request);
		try {
			whseService.updateShowWhseStatus(paramsMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("读取updateWhseByTobaccoServerGetWhseQty失败: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	
}
