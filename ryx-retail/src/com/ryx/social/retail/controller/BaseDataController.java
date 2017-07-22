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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.JsonSyntaxException;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.login.tool.SSOConfig;
import com.ryx.social.retail.service.IMerchService;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.RetailConfig;

@Controller 
public class BaseDataController {

	private static final Logger logger = LoggerFactory.getLogger(BaseDataController.class);
	
	@Resource
	private IMerchService merchService;
	

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
	
	@RequestMapping(value = "/retail/basedata/getMerchInfo")
	public void getMerchInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		SessionInfo info = IdentityUtil.getUser(request);
		
		Map map = merchService.getMerchInfo(info.getRefId());
		ResponseUtil.write(request, response, map);
	}
	
	@RequestMapping(value = "/retail/basedata/searchMerchDetail")
	public void searchMerchJoinMerchFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", paramMap.get("ref_id"));
		String params = request.getParameter("params");
		paramMap.putAll(JsonUtil.json2Map(params));
//		paramMap.putAll((Map<String, Object>) ((Map<String, Object>) request.getAttribute("__verified_result")).get("params"));
		Map<String, Object> data = null;
		try {
			List<Map<String, Object>> merchList = merchService.searchMerchJoinMerchFile(paramMap);
			if(merchList.size()>0) {
				data = merchList.get(0);
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 查询商户信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 修改商户密码 本地不做逻辑只调用用户中心接口
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="retail/basedata/updateMerchPwd")
	public void changeMerchPassword(HttpServletRequest request, HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> changePasswordParam = MapUtil.rename(paramMap
				, "user_code", "newPassword.new_pwd", "oldPassword.old_pwd", "token");
		// 获取智能终端2.0的token
		String tokenSecret = SSOConfig.getCookieContent(request, "tokenSecret");
		if(StringUtils.hasText(tokenSecret)) {
			changePasswordParam.put("token", tokenSecret);
		}
		String changePasswordInUcenter = RetailConfig.getUcenterServer() + "ucenter/updateMerchants";
		try {
			String changePasswordResultJson = HttpUtil.post(changePasswordInUcenter, changePasswordParam);
			// 先判断返回的数据是否正常
			if(StringUtils.hasText(changePasswordResultJson)) {
				Map<String, Object> changePasswordResult = JsonUtil.json2Map(changePasswordResultJson);
				if(!Constants.SUCCESS.equals(changePasswordResult.get("code"))) {
					code = Constants.FAIL;
					msg = MapUtil.getString(changePasswordResult, "msg");
				}
			}
		} catch (JsonSyntaxException jse) {
			code = Constants.FAIL;
			msg = "返回数据格式异常";
			logger.error(" = * = * = * = * = 修改商户密码错误  = * = * = * = * = ", jse);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 修改商户密码错误  = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	
	//修改基本信息//type===merchAddr:修改经纬度，merchTime：修改营业时间，merchScope:经营范围
	@RequestMapping(value = "/retail/basedata/updateMerchBasicInfo")
	public void updateMerchBasicInfo(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> params = getParamMap(request);
		try {
			merchService.updateMerchBasicInfo(params);
		} catch (Exception e) {
			logger.error("BaseDateController ",e);
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
		}
		ResponseUtil.write(request, response, code,msg,null);
	}
	@RequestMapping(value = "/retail/basedata/updateMerchInfo")
	public void updateMerchInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map paramMap=IdentityUtil.getUserMap(request);
		paramMap.put("merch_id", paramMap.remove("ref_id"));
		String params = request.getParameter("params");
		paramMap.putAll(JsonUtil.json2Map(params));
//		paramMap.putAll((Map<String, Object>) ((Map<String, Object>) request.getAttribute("__verified_result")).get("params"));
		try {
			merchService.updateMerchInfo(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("修改失败",e);
			// TODO: handle exception
		}
//		System.out.println(merchId);

		
		
//		SessionInfo info = IdentityUtil.getUser(request);
//		
//		String merchId = request.getParameter("merch_id");
//		if(info.getRefId().equals(merchId)) {
//			Map params = new HashMap();
//			params.put("merch_id", merchId);
//			if(request.getParameter("merch_name")!=null){
//				params.put("merch_name", request.getParameter("merch_name"));
//			}
//			if(request.getParameter("manager")!=null)
//			params.put("manager", request.getParameter("manager"));
//			if(request.getParameter("telephone")!=null)
//			params.put("telephone", request.getParameter("telephone"));
//			if(request.getParameter("address")!=null)
//			params.put("address", request.getParameter("address"));
//			if(request.getParameter("busi_time")!=null)
//			params.put("busi_time", request.getParameter("busi_time"));
//			if(request.getParameter("busi_scope")!=null)
//			params.put("busi_scope", request.getParameter("busi_scope"));
//			if(request.getParameter("longitude")!=null)
//			params.put("longitude", request.getParameter("longitude"));
//			if(request.getParameter("latitude")!=null)
//			params.put("latitude", request.getParameter("latitude"));
//			if(request.getParameter("lice_id")!=null)
//			params.put("lice_id", request.getParameter("lice_id"));
//			
//			merchService.updateMerchInfo(params);
			
			ResponseUtil.write(request, response, code,msg,null);
//		} else {
//			ResponseUtil.write(request, response, Constants.FAIL, "您没有修改店铺设置的权限！", null);
//		}
	}
	//交接班
	@RequestMapping(value = "/retail/basedata/dutyShift")
	public void dutyShift (HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=new HashMap<String, Object>();
		paramsMap.put("merch_id", MapUtil.get(userMap, "ref_id", null));
		paramsMap.put("user_code", MapUtil.get(userMap, "user_code", null));
		paramsMap.put("role_id", MapUtil.getString(userMap, "role_id"));
		paramsMap.put("role_name", MapUtil.getString(userMap, "role_name"));
		Map<String, Object> data=null;
//		paramMap.putAll((Map<String, Object>) ((Map<String, Object>) request.getAttribute("__verified_result")).get("params"));
		try {
			data=merchService.dutyShift(paramsMap);
			if(data==null){
				code="1000";
				msg="请输入操作人编码！";
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("交班失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg, data);
	}
	//插入交接班
	@RequestMapping(value = "/retail/basedata/insertDutyShift")
	public void insertDutyShift (HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap = JsonUtil.json2Map(request.getParameter("params"));
		paramsMap.put("merch_id", MapUtil.get(userMap, "ref_id", null));
		paramsMap.put("user_code", MapUtil.get(userMap, "user_code", null));
		Map<String, Object> data=null;
//			paramMap.putAll((Map<String, Object>) ((Map<String, Object>) request.getAttribute("__verified_result")).get("params"));
		try {
			data=merchService.insertDutyShift(paramsMap);
			if(data==null){
				code="1000";
				msg="请输入操作人编码！";
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("交班失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg, data);
	}
	//查询交接班
	@RequestMapping(value = "/retail/basedata/searchDutyShift")
	public void searchDutyShift (HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		Map<String, Object> paramsMap=JsonUtil.json2Map(request.getParameter("params"));
		paramsMap.put("merch_id", MapUtil.get(userMap, "ref_id", null));
		paramsMap.put("role_id", MapUtil.getString(userMap, "role_id"));
		paramsMap.put("role_name", MapUtil.getString(userMap, "role_name"));
		paramsMap.put("user_code", MapUtil.getString(userMap, "user_code"));
		List<Map<String, Object>> data=null;
		try {
			data=merchService.searchDutyShift(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			logger.error("查询失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg, data, paramsMap);
	}
	
	@RequestMapping(value = "/retail/basedata/getOnlineDuration")
	public void getOnlineTime(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> data = Collections.EMPTY_LIST;
		try {
			data = merchService.getOnlineDuration(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 获取终端在线时长错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	//上传智能终端经纬度
	@RequestMapping(value = "/retail/basedata/uploadLocation")
	public void uploadLocation(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> data = Collections.EMPTY_MAP;
		try {
			data = merchService.uploadLocation(paramMap);
			if (data != null) {
				msg = MapUtil.getString(data, "msg");
				code = MapUtil.getString(data, "code");
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 上传智能终端经纬度 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
}
