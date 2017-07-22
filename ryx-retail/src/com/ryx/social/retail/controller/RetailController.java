package com.ryx.social.retail.controller;

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
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.utils.MapUtil;
import com.ryx.login.BaseSecurityFilter;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.login.tool.SSOConfig;
import com.ryx.social.retail.service.ICgtOrderService;
import com.ryx.social.retail.service.IItemService;
import com.ryx.social.retail.service.IMerchService;
import com.ryx.social.retail.service.ISystemService;
import com.ryx.social.retail.util.MultiCitySupport;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.RetailConfig;

/**
 * Handles requests for the application home page.
 */
@Controller 
public class RetailController {
	
	private static final Logger logger = LoggerFactory.getLogger(RetailController.class);

	@Resource
	private IMerchService merchService;
	@Resource
	private IItemService itemService;
	@Resource
	private IMerchService merchServiceImpl;
	@Resource
	private ICgtOrderService cgtOrderServiceImpl;//CgtOrderServiceImpl
	@Resource
	private ISystemService systemServiceImpl;
	
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//return index(request, response);
		response.sendRedirect("retail/index");
	}
	
	@RequestMapping(value = "/testfile", method = RequestMethod.GET)
	public String testfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "testfile1";
	}
	
	@RequestMapping(value = "/retail/index2", method = RequestMethod.GET)
	public String index2(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String str = index(request, response);
		str = str.replace("/index", "/index_source");
		return str;
	}
	
	@RequestMapping("/login")
	public String login(Model model) {
		model.addAttribute("callbackURL", "retail/index");
		return "ycls/login";
	}
	
	@RequestMapping(value = "/retail/index", method = RequestMethod.GET)
	public String index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SessionInfo info = (SessionInfo) request.getSession().getAttribute(IdentityUtil.SESSIONINFO_IN_SESSION);
		String userType = info.getUserType();
		//检查零售商用户信息是否初始化
		if("10".equals(userType)){
			//Map<String, String> map = merchServiceImpl.getMerchInfo(info.getRefId());
			String isInit = info.isInit();
			if(isInit == null || "0".equals(isInit)){//用户未初始化
				Map<String, String> initMap = new HashMap<String, String>();
				initMap.put("comId", info.getComId());
				initMap.put("refId", info.getRefId());
				initMap.put("custId", info.getRefId());
				try {
					cgtOrderServiceImpl.getTobaccoItemListToBaseMerchItem(initMap);
					Map<String, String> map = new HashMap<String, String>();
					map.put("merch_id", info.getMerchId());
					map.put("is_init", "1");
					map.put("init_date", DateUtil.getToday());
					map.put("init_time", DateUtil.getCurrentTime().substring(8));
					merchServiceImpl.updateMerchInfo(map);
					
					info.setInit(map.get("is_init"));
					info.setInitDate(map.get("init_date"));
					info.setInitTime(map.get("init_time"));
				} catch (Exception e) {
					logger.info("用户 " + info.getUserCode() + "数据初始化失败:" + e.getMessage());
				}
				//更新状态

			}
		}
		
		Map<String, Object> user = getCurrentUser(request);
		request.setAttribute("user", user);
		// 转成小写
//		String userJson = JsonUtil.map2json(user).toLowerCase();
		request.setAttribute("user_json", JsonUtil.map2json(user));
		
		String context = request.getContextPath() + "/";
		StringBuffer sb = request.getRequestURL();
		String url = sb.substring(0, sb.indexOf(context));
		request.setAttribute("domain", url + context);
		request.setAttribute("resource", RetailConfig.getResourceServer());
		request.setAttribute("msg_push_url", RetailConfig.getMsgPushUrl() );
		//return "retail/index";
		// 20141123 主页面个性化
		return MultiCitySupport.getIndexPath(info.getComId());
	}
	
	@RequestMapping("/retail/getTerminalParameter")
	public void getTerminalParameter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SessionInfo info = IdentityUtil.getUser(request);
		if(info != null) {
			Map<String, Object> user = getCurrentUser(request);
			// 从返回的终端参数中去掉权限 permit
			user.remove("permit");
			String liceId = info.getUserCode();
			Map<String, String> param = new HashMap<String, String>();
			param.put("cig", liceId);
			RetailConfig.getCheckBillServer();
			// 从内管获取终端参数
			String cgtOrderUrl = RetailConfig.getRrximsServer()+"/yby/termSynInfo!getParam";
			logger.debug(" = * = * = * = * = 从内管获取终端参数的请求参数 = * = * = * = * = " + param);
			String json = HttpUtil.post(cgtOrderUrl, param);
			logger.debug(" = * = * = * = * = 从内管获取终端参数的返回数据 = * = * = * = * = " + json);
			try {
				List<Map<String, Object>> terminalParamResult = JsonUtil.json2List(json);
				if(!terminalParamResult.isEmpty()) {
					user.putAll(terminalParamResult.get(0));
				} else {
					logger.debug(" = * = * = * = * = 从内管获取终端参数的返回数据错误 = * = * = * = * = " + json);
				}
			} catch (Exception e) {
				logger.error(" = * = * = * = * = 从内管获取终端参数的返回数据错误 = * = * = * = * = " + json, e);
			}
			/*
			// 从支付获取终端参数
			String cgtOrderUrl = RetailConfig.getCheckBillServer()+"/ryxcmt/cmtlist.do";
			String json = HttpUtil.post(cgtOrderUrl, param);
			// 获取终端参数
			Map<String, Object> terminalParamResult = JsonUtil.json2Map(json);
			String code = MapUtil.getString(terminalParamResult, "code");
			if(Constants.CODE_SUCCESS.equals(code)) {
				Map<String, Object> terminalParam = MapUtil.get(terminalParamResult, "result", Collections.EMPTY_MAP);
				user.putAll(terminalParam);
			} else {
				throw new RuntimeException("获取终端参数失败: " + MapUtil.getString(terminalParamResult, "msg"));
			}
			*/
			ResponseUtil.write(request, response, Constants.CODE_SUCCESS, Constants.SUCCESS_MSG, user);
		} else {
			ResponseUtil.write(request, response, Constants.CODE_SUCCESS, Constants.SUCCESS_MSG, Collections.EMPTY_MAP);
		}
	}
	
	@RequestMapping(value = "/retail/test")
	public void test(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.CODE_SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		SessionInfo info = IdentityUtil.getUser(request);
		if(info != null) {
			String merchID = info.getMerchId();
			// token获取顺序: 1.从参数中获取 2.从cookie中获取 3.从session中获取
			String tokenSecret = request.getParameter("tokenSecret");
			if(!StringUtils.hasText(tokenSecret)) {
				tokenSecret = SSOConfig.getCookieContent(request, SSOConfig.cookieTokenSecretName);
				if(!StringUtils.hasText(tokenSecret)) {
					Object tokenInSession = request.getSession(true).getAttribute("token");
					tokenSecret = tokenInSession==null ? null : tokenInSession.toString();
				}
			}
			// 如果没有取到token 直接报错返回
			if(!StringUtils.hasText(tokenSecret)) {
				ResponseUtil.write(request, response, Constants.CODE_FAIL, "终端更新开机时间错误", null);
				return;
			}
			merchService.updateMerchLoginTime(tokenSecret, merchID);
			try {
				// 向rtms透传经纬度信息
				Map<String, Object> locationParam = new HashMap<String, Object>();
				locationParam.put("merch_id", merchID);
				locationParam.put("longitude", request.getParameter("longitude"));
				locationParam.put("latitude", request.getParameter("latitude"));
				merchService.uploadLocation(locationParam);
				
				Map<String, Object> paramMap = ParamUtil.getParamMap(request);
				paramMap.put("token", tokenSecret);
				paramMap.put("merch_id", paramMap.get("ref_id"));
				systemServiceImpl.submitTerminalSignupTime(paramMap);
			} catch (Exception e) {
				logger.error(" = * = * = * = * = 终端更新开机时间错误 = * = * = * = * = ", e);
				code = Constants.CODE_FAIL;
				msg = "终端更新开机时间错误";
			}
		} else {
			code = Constants.CODE_IDENTITY_ERROR;
			msg = Constants.FAIL_MSG;
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	@RequestMapping(value="/retail/submitBootupDuration")
	public void submitBootupDuration(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("user_token", request.getParameter("tokenSecret"));
		
		Map<String, Object> data = null;
		
		try {
			// 向rtms透传经纬度信息
			Map<String, Object> locationParam = new HashMap<String, Object>();
			locationParam.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
			locationParam.put("longitude", MapUtil.getString(paramMap, "longitude"));
			locationParam.put("latitude", MapUtil.getString(paramMap, "latitude"));
			merchService.uploadLocation(locationParam);
			// 保存离线时长
			systemServiceImpl.submitTerminalBootupDuration(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 终端上传开机时长错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
		
	}
	
	private Map<String, Object> getCurrentUser(HttpServletRequest request) throws Exception {
		SessionInfo info = IdentityUtil.getUser(request);
		
		Map map = merchService.getMerchInfo(info.getRefId());
		
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("user_id", info.getUserId());
		user.put("user_code", info.getUserCode());
		user.put("user_name", info.getUserName());
		user.put("merch_id", info.getRefId());
		user.put("merch_name", map.get("MERCH_NAME"));
		user.put("longitude", map.get("LONGITUDE"));
		user.put("latitude", map.get("LATITUDE"));
		user.put("manager", map.get("MANAGER"));
		user.put("telephone", map.get("TELEPHONE"));
		user.put("com_id", map.get("CGT_COM_ID"));
		user.put("lice_id", map.get("LICE_ID"));
		user.put("login_url", SSOConfig.loginURL);
		user.put("callback_url", SSOConfig.callbackURL);
		user.put("service_flag", SSOConfig.serviceFlag);
		user.put("roleID", info.getRoleId());
		user.put("roleName", info.getRoleName());
		user.put("amount_per_point", map.get("amount_per_point"));
		//List<Map> permit = userService.getUserResource(info.getUserCode());
		user.put("permit", info.getPermission());
		
		/* 20140410 不从此处获取店铺优惠,直接从双屏的程序中获取
		Map<String, Object> smsp = new HashMap<String, Object>();
		smsp.put("merch_id", info.getMerchId());
		smsp.put("ref_id", info.getRefId());
		Map<String, Object> promo1 = merchSalePromotionService.searchSalePromotion(smsp);
		if(promo1 != null) {
			Map<String, Object> promo2 = new HashMap<String, Object>();
			promo2.put("promotion_description", promo1.get("PROMOTION_DESCRIPTION"));
			promo2.put("promotion_start_date", promo1.get("PROMOTION_START_DATE"));
			promo2.put("promotion_end_date", promo1.get("PROMOTION_END_DATE"));
			promo2.put("promotion_id", promo1.get("PROMOTION_ID"));
			promo2.put("file_id", promo1.get("FILE_ID"));
			user.put("promo", promo2);
		}
		*/
		
		// 获取小票设置
		Map<String,Object> merchTicket=new HashMap<String,Object>();
		merchTicket.put("merch_id", info.getMerchId());
		merchTicket.put("ticket_type", "01");
		merchTicket.put("merch_name", info.getMerchName());
		merchTicket.put("phone", info.getPhone());
		Map<String, Object> ticket1 = merchServiceImpl.selectMerchTicket(merchTicket);
		Map<String, Object> ticket2 = new HashMap<String, Object>();
		ticket2.put("ticket_type", ticket1.get("TICKET_TYPE"));
		ticket2.put("welcome_word", ticket1.get("WELCOME_WORD"));
		ticket2.put("note", ticket1.get("NOTE"));
		ticket2.put("phone", ticket1.get("PHONE"));
		ticket2.put("num", ticket1.get("NUM"));
		ticket2.put("ticket_date", ticket1.get("TICKET_DATE"));
		user.put("ticket", ticket2);
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("merch_id", info.getMerchId());
		List<String> wlist = itemService.searchMerchWarningItem(params);
		user.put("wlist", wlist);
		
		return user;
	}
	
	@RequestMapping(value = "/retail/getMerchParams")
	public void getMerchParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		List<Map<String, Object>> kinds = itemService.searchItemKind(new HashMap<String, Object>());
		params.put("kinds", kinds);
		
		List<Map<String, Object>> units = itemService.searchItemUnit(new HashMap<String, Object>());
		params.put("units", units);
		/*
		try {
			List<Map<String, Object>> lmts = cgtOrderService.getTobaccoLmt(getSessionInfo(request));
			params.put("lmts", lmts);
		} catch(Exception e) {
			logger.error("获取卷烟限量出错", e);
		}
		*/
		ResponseUtil.write(request, response, params);
	}
	
	@RequestMapping(value = "/retail/updateOrInsertTicket")
	public void updateOrInsertTicket(HttpServletRequest request, HttpServletResponse response) {
		Map<String,Object> merchTicket=new HashMap<String,Object>();
		SessionInfo info = IdentityUtil.getUser(request);
		
		merchTicket.put("merch_id", info.getMerchId());
		merchTicket.put("ticket_type", "01");
		merchTicket.put("welcome_word", request.getParameter("welcome_word"));
		merchTicket.put("note", request.getParameter("note"));
		merchTicket.put("phone", request.getParameter("phone"));
		merchTicket.put("num", request.getParameter("num"));
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		try{
			merchServiceImpl.updateAndInsertMerchTicket(merchTicket);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "修改数据失败，请重试！";
		}
		ResponseUtil.write(request, response, code, msg,null);
	}
	
	@RequestMapping(value = "/retail/selectTicket")
	public void selectTicket(HttpServletRequest request, HttpServletResponse response) {
		Map<String,Object> merchTicket=new HashMap<String,Object>();
		SessionInfo info = IdentityUtil.getUser(request);
		
		merchTicket.put("merch_id", info.getMerchId());
		merchTicket.put("ticket_type", "01");
		merchTicket.put("merch_name", info.getMerchName());
		merchTicket.put("phone", info.getPhone());
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data = new HashMap<String, Object>();
		try{
			data=merchServiceImpl.selectMerchTicket(merchTicket);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = "系统繁忙请重试！";
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value = "/retail/widget", method = RequestMethod.GET)
	public String widget(HttpServletRequest request, HttpServletResponse response) throws Exception {
		index(request, response);
		return "retail/widget_source";
	}
	
	
	@RequestMapping(value = "/retail/module/jydh")
	public String jydh(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		List permitList = JsonUtil.json2List(
				"[{\"title\":\"卷烟配货\",\"haschildren\":\"1\",\"module_id\":\"jydh\",\"multi\":\"0\",\"haspage\":\"1\",\"parent_id\":\"\"}," + 
				"{\"title\":\"历史订单\",\"haschildren\":\"0\",\"module_id\":\"lsdd\",\"multi\":\"0\",\"haspage\":\"1\",\"parent_id\":\"jydh\"}]"
		);
		
		Map<String, Object> user = getCurrentUser(request);
		user.put("permit", permitList);
		request.setAttribute("user", user);
		request.setAttribute("user_json", JsonUtil.map2json(user));
		request.setAttribute("moduleId", "jydh");
		
		
		String context = request.getContextPath() + "/";
		StringBuffer sb = request.getRequestURL();
		String url = sb.substring(0, sb.indexOf(context));
		request.setAttribute("domain", url + context);
		request.setAttribute("resource", RetailConfig.getResourceServer());
		
		return "retail/widget_source";
	}
	
}
