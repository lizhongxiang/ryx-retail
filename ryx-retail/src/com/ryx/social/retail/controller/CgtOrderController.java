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
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.ICgtOrderService;
import com.ryx.social.retail.util.DesUtil;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.RetailConfig;

/**
 * Handles requests for the application home page.
 */
@Controller
public class CgtOrderController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CgtOrderController.class);
	@Resource
	private ICgtOrderService cgtOrderService;
	
	private Map<String, String> getSessionInfo(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, String> map = new HashMap<String, String>();
		map.put("refId", user.getRefId());
		map.put("comId", user.getComId());
		map.put("custId",user.getRefId());
		
		map.put("ref_id", user.getRefId());
		map.put("com_id", user.getComId());
		map.put("cust_id",user.getRefId());
		map.put("init_date", user.getInitDate());
		map.put("init_time", user.getInitTime());
		return map;
	}

	private Map<String, Object> getSessionInfoByMap(HttpServletRequest request) {
		SessionInfo user = IdentityUtil.getUser(request);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("refId", user.getRefId());
		map.put("comId", user.getComId());
		map.put("custId",user.getRefId());
		
		map.put("ref_id", user.getRefId());
		map.put("com_id", user.getComId());
		map.put("cust_id",user.getRefId());
		map.put("init_date", user.getInitDate());
		map.put("init_time", user.getInitTime());
		return map;
	}
	

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
	
	/**
	 * 获取卷烟商品列表
	 */
	@RequestMapping(value = "/retail/tobacco/getTobaccoList")
	public void getTobaccoList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = cgtOrderService.getTobaccoItemList(getSessionInfo(request));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取卷烟列表出错：" + e.getMessage();
			LOG.error("前台获取卷烟商品列表错误:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 获取零售户本周期订单
	 */
	@RequestMapping(value = "/retail/tobacco/getRecurrentOrder")
	public void getRecurrentOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = cgtOrderService.getTobaccoOrder(getSessionInfo(request));
			//data.put("order_id", "");
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取本周期订单出错：" + e.getMessage();
			LOG.error("前台获取零售户本周期订单错误：", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 获取卷烟限量
	 */
	@RequestMapping(value = "/retail/tobacco/getTobaccoLmt")
	public void getTobaccoLmt(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		String snychType=request.getParameter("snychType");
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, String> paramMap = getSessionInfo(request);
		paramMap.put("snychType", snychType);
		try {
				data = cgtOrderService.getTobaccoLmt(paramMap);
			//data.put("order_id", "");
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取商品限量出错：" + e.getMessage();
			LOG.error("获取商品限量出错：", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 获取卷烟部分商品限量
	 */
	@RequestMapping(value = "/retail/tobacco/getTobaccoItemLmt")
	public void getTobaccoItemLmt(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = null;
		Map<String, Object> paramMap = getSessionInfoByMap(request);
		
		Map<String, Object> params = ParamUtil.getParamMap(request);
		List list = (List)params.get("item_list");
		if(list.size() != 0) {
			paramMap.put("item_list", list);
			paramMap.put("item_id", (String)list.get(0));
			try {
				data = cgtOrderService.getTobaccoItemLmt(paramMap);
			} catch (Exception e) {
				code = Constants.FAIL;
				msg = "获取单品限量出错：" + e.getMessage();
				LOG.error("获取单品限量出错：", e);
			}
		} else {
			code = Constants.CODE_PARAMS_ERROR;
			msg = "无请求限量的商品";
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 添加烟类部分数据(从网页截取的信息)
	 * @author 徐虎彬
	 * @date 2014年3月3日
	 * @param request
	 * @param response
	 */
//	/*
	@RequestMapping(value = "/retail/tobacco/addUrlData")
	public void addUrlData(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		try {
			cgtOrderService.updateTobaccoData();
		} catch (Exception e) {
			code = Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("addUrlData",e);
		}	
		ResponseUtil.write(request, response, code, msg, null);
	}
//	*/
	/**
	 * 更新卷烟信息
	 */
//	/*
	@RequestMapping(value = "/retail/tobacco/updateTobaccoList")
	public void updateTobaccoList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			cgtOrderService.updateTobaccoList(getSessionInfo(request));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "更新卷烟列表出错: " + e.getMessage();
			LOG.error("更新卷烟列表出错：", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
//	*/
	
	/**
	 * 提交卷烟订单
	 */
	@RequestMapping(value="/retail/tobacco/submitOrder")
	public void commitRecurrentOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, String> paramMap = getSessionInfo(request);
		String jsonParam = request.getParameter("params");
		paramMap.put("jsonParam", jsonParam);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = cgtOrderService.submitTobaccoOrder(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "提交卷烟订单出错：" + e.getMessage();
			LOG.error("提交卷烟订单出错:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 下载订货参数
	 */
	@RequestMapping(value="/retail/tobacco/getOrderParams")
	public void getOrderParams(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, String> paramMap = getSessionInfo(request);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = cgtOrderService.getOrderParams(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "下载订货参数出错：" + e.getMessage();
			LOG.error("下载订货参数出错：", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 订单列表
	 */
	@RequestMapping(value="/retail/tobacco/getOrderList")
	public void getOrderList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, String> paramMap = getSessionInfo(request);
		paramMap.put("beginDate", request.getParameter("begin_date"));
		paramMap.put("endDate", request.getParameter("end_date"));
		
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = cgtOrderService.getOrderList(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "获取订单列表错误：" + e.getMessage();
			LOG.error("前台获取订单列表错误:", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 获取订单明细
	 */
	@RequestMapping(value = "/retail/tobacco/getOrderDetail")
	public void getOrderDetail(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> data = null;
		
		String orderId = request.getParameter("order_id");
		
		if(orderId != null && !"".equals(orderId)) {
			
			Map<String, String> paramMap = getSessionInfo(request);	
			paramMap.put("coNum", orderId);
			
			try {
				data = cgtOrderService.getOrderDetail(paramMap);
			} catch (Exception e) {
				code = Constants.FAIL;
				msg = Constants.FAIL_MSG;
				LOG.error("前台获取订单明细错误:", e);
			}
		} else {
			code = Constants.FAIL;
			msg = "订单号不能为空";
		}
		
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 订单入库
	 */
	@RequestMapping(value = "/retail/tobacco/putOrder")
	public void saleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = getParamMap(request);
		
		List<Map<String, Object>> data = null;
		
		if(paramMap.get("order_id")!=null && !"".equals(paramMap.get("order_id"))) {
			Map<String, String> orderParam = new HashMap<String, String>();
			orderParam.put("orderId", MapUtil.getString(paramMap, "order_id"));
			orderParam.put("coNum", MapUtil.getString(paramMap, "order_id"));
			orderParam.put("custId", MapUtil.getString(paramMap, "merch_id"));
			orderParam.put("cust_id", MapUtil.getString(paramMap, "merch_id"));
			orderParam.put("refId", MapUtil.getString(paramMap, "ref_id"));
			orderParam.put("ref_id", MapUtil.getString(paramMap, "ref_id"));
			orderParam.put("init_time", MapUtil.getString(paramMap, "init_time"));
			orderParam.put("init_date", MapUtil.getString(paramMap, "init_date"));
			orderParam.put("com_id", MapUtil.getString(paramMap, "com_id"));
			orderParam.put("comId", MapUtil.getString(paramMap, "com_id"));
			orderParam.put("operator", MapUtil.getString(paramMap, "user_code"));
			orderParam.put("note", MapUtil.getString(paramMap, "note"));
			if(paramMap.get("begin_date")!=null) {
				orderParam.put("beginDate",MapUtil.getString(paramMap, "begin_date"));
			}
			if(paramMap.get("end_date")!=null) {
				orderParam.put("endDate", MapUtil.getString(paramMap, "end_date"));
			}
			try {
				data = cgtOrderService.updateAndGetOrderList(orderParam);
			} catch (Exception e) {
				code = Constants.FAIL;
				msg = Constants.FAIL_MSG;
				LOG.error("前台获取订单明细错误:", e);
			}
		} else {
			code = Constants.FAIL;
			msg = "订单号不能为空";
		}
		
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	/**
	 * 订单支付
	 */
	@RequestMapping(value = "retail/tobacco/orderPayment")
	public void orderPayment(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data = null;
		Map<String,Object> parames = IdentityUtil.getUserMap(request);
		Map<String,String> parame=getSessionInfo(request);
		parame.put("lice_id", parames.get("lice_id").toString().trim());
		try {
			data = cgtOrderService.orderPayment(parame);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			LOG.error("资金归集失败：", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 测试从服务器获取卷烟信息并添加到商户商品表中接口
	 * @author 徐虎彬
	 * @date 2014年3月15日
	 * @param request
	 * @param response
	 */
	/*
	@RequestMapping(value = "/retail/tobacco/testGetTobaccoItemListToBaseMerchItem")
	public void testGetTobaccoItemListToBaseMerchItem(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, String> paramMap = getSessionInfo(request);
		paramMap.put("beginDate", request.getParameter("begin_date"));
		paramMap.put("endDate", request.getParameter("end_date"));
		try {
			cgtOrderService.getTobaccoItemListToBaseMerchItem(paramMap);
			msg="0000";//表示入库成功
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("前台获取订单明细错误:", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	*/
	@RequestMapping("retail/tobacco/payOrderByCard")
	public void payOrderBySwipCard(HttpServletRequest request, HttpServletResponse response){
		//String pay = "http://10.2.2.103:7000/rtserver/rest/resource/tobacco/200000022";\
		//支付参数
		String params = request.getParameter("params");
		@SuppressWarnings("unchecked")
		Map<String,String> payMeg = JsonUtil.json2Map(params);
		//查询订单参数
		Map<String,Object> parames = IdentityUtil.getUserMap(request);
		Map<String,String> serachOrderParam=getSessionInfo(request);
		serachOrderParam.put("lice_id", parames.get("lice_id").toString().trim());
		
		Map<String,String> payResponseData = null;
		try {
			payResponseData =  cgtOrderService.orderPayByPOS(payMeg,serachOrderParam);
			ResponseUtil.write(request, response, payResponseData);
		} catch (Exception e) {
			ResponseUtil.write(request, response, Constants.CODE_FAIL, e.getMessage(),null);
			LOG.info("卷烟刷卡支付失败："+e.getMessage(), e);
		}
		
	}
	
	@RequestMapping("tobacco/getTobaccoItem")
	public void getTobaccoItem(HttpServletRequest request, HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> data = null;
		try {
			data = cgtOrderService.searchTobaccoItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.info(" = * = * = * = * = 获取卷烟商品信息失败 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}

	/**
	 * 卷烟订货 身份认证
	 * t_username 用户名 t_password 密码
	 */
	@RequestMapping("retail/tobacco/authenticate")
	public void authenticate(HttpServletRequest request, HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			Map<String, Object> loginParam = MapUtil.rename(paramMap, "ref_id.cust_id", "t_username.j_username", "t_password.j_password");
			if (loginParam.containsKey("j_username")) {
				loginParam.put("j_username", DesUtil.decode(MapUtil.getString(loginParam, "j_username")));
			}
			if (loginParam.containsKey("j_password")) {
				loginParam.put("j_password", DesUtil.decode(MapUtil.getString(loginParam, "j_password")));
			}
			// 向rtms请求登录接口
			String authenticationUrl = RetailConfig.getTobaccoServer() + "cgtorder/loginCertificate";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("params", JsonUtil.map2json(loginParam));
			LOG.debug("卷烟订货身份认证 URL: " + authenticationUrl + ", PARAM: " + param);
			String callbackJson = HttpUtil.post(authenticationUrl, param);
			LOG.debug("卷烟订货身份认证 返回信息: " + callbackJson);
			Map<String, Object> callback = JsonUtil.json2Map(callbackJson);
			// 如果返回不是"0000" 则返回"1033"
			if(!MapUtil.getString(callback, "code").equals(Constants.CODE_SUCCESS)) {
				code = "1033";
				msg = MapUtil.getString(callback, "msg");
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.info(" = * = * = * = * = 卷烟订货身份认证失败 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}

	/**
	 * 卷烟订货 查询银行卡余额
	 */
	@RequestMapping("retail/tobacco/getAccountBalance")
	public void getAccountBalance(HttpServletRequest request, HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> data = null;
		try {
			Map<String, Object> accParam = MapUtil.rename(paramMap, "ref_id.cust_id");
			// 向rtms请求余额接口
			String authenticationUrl = RetailConfig.getTobaccoServer() + "cgtorder/getBankAcc";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("params", JsonUtil.map2json(accParam));
			LOG.debug("卷烟订货查询余额 URL: " + authenticationUrl + ", PARAM: " + param);
			String callbackJson = HttpUtil.post(authenticationUrl, param);
			LOG.debug("卷烟订货查询余额 返回信息: " + callbackJson);
			Map<String, Object> callback = JsonUtil.json2Map(callbackJson);
			Map<String, Object> result = MapUtil.get(callback, "result", Collections.EMPTY_MAP);
			// 如果返回"0000" 将余额返回 如果不是"0000" 则返回"1033"
			if(MapUtil.getString(callback, "code").equals(Constants.CODE_SUCCESS)) {
				data = new HashMap<String, Object>();
				data.put("balance", MapUtil.getBigDecimal(result, "account"));
			} else {
				code = Constants.FAIL;
				msg = MapUtil.getString(callback, "msg");
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.info(" = * = * = * = * = 卷烟订货查询银行卡余额失败 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
}
