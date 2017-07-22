package com.ryx.social.retail.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.ICheckBillService;

/**
 * 便民服务--对账服务
 * @author schg
 *
 */
@Controller
public class CheckBillController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CheckBillController.class);
	@Resource
	private ICheckBillService checkBillServiceImpl;
	/**
	 * 便民服务--对账服务--话费查询
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/bmfw/dzfw/phone")
	public void  checkBillPhoneFee(HttpServletRequest request, HttpServletResponse response){
		SessionInfo info = IdentityUtil.getUser(request);
		String cigliceid = info.getLiceId();
		try {
			Map<String,String> map = this.fitingParam(request);
			map.remove("cardno");
			map.put("cigliceid",cigliceid);
			if(request.getParameter("status") != null && !request.getParameter("status").equals("")){
				map.put("pay_status", String.valueOf(request.getParameter("status")));
			}
			else{
				map.put("pay_status", "ALL");
			}
			String phoneJson = checkBillServiceImpl.checkBillPhoneFee(map);
//			System.out.println(phoneJson);
			LOG.debug("phoneJson");
			@SuppressWarnings("unchecked")
			Map<String,String> backMap = JsonUtil.json2Map(phoneJson);
			if(backMap.get("sucess").equals("true")){
				ResponseUtil.write(request, response, backMap);
			}
			else{
				ResponseUtil.write(request,response,"1000","无数据","");
			}
			
		} catch (Exception e) {
			LOG.error("远程服务错误",e);
			ResponseUtil.write(request,response,"1000","远程服务错误","");
		}
		
	}
	@RequestMapping("/retail/bmfw/dzfw/consume")
	public void checkBillConsume(HttpServletRequest request, HttpServletResponse response){
		SessionInfo info = IdentityUtil.getUser(request);
		String cigliceid = info.getLiceId();
		try {
			Map<String,String> map = this.fitingParam(request);
			map.remove("phoneno");
			map.put("cigliceid",cigliceid);
			String phoneJson = checkBillServiceImpl.checkBillConsume(map);
			Map<String,String> backMap = JsonUtil.json2Map(phoneJson);
			if(backMap.get("sucess").equals("true")){
				ResponseUtil.write(request, response, backMap);
			}
			else{
				ResponseUtil.write(request,response,"1000","无数据","");
			}
			
		} catch (Exception e) {
			LOG.error("CheckBillController checkBillConsume :",e);
			ResponseUtil.write(request,response,"1000","远程服务错误","");
			
		}
	}
	@RequestMapping("/retail/bmfw/dzfw/gjf")
	public void checkBillGJF(HttpServletRequest request, HttpServletResponse response){
		String params =  request.getParameter("params");
		@SuppressWarnings("unchecked")
		Map<String,String> reqMap = JsonUtil.json2Map(params);
		SessionInfo info = IdentityUtil.getUser(request);
		String cigliceid = info.getLiceId();
		reqMap.put("cigliceid", cigliceid);
		try {
			String phoneJson = checkBillServiceImpl.checkBillGJF(reqMap);
			@SuppressWarnings("unchecked")
			Map<String,String> backMap = JsonUtil.json2Map(phoneJson);
			ResponseUtil.write(request, response, backMap);
		} catch (Exception e) {
			LOG.error("远程服务错误",e);
			ResponseUtil.write(request,response,"1000","远程服务错误","");
		}
	}
	
	
	@RequestMapping("/ipos/bill/phone")
	public void checkBillPhoneFeeForPos(HttpServletRequest request, HttpServletResponse response){
		try {
			Map<String,String> map = this.fitingParamForIpos(request);
			map.remove("cardno");
			if(request.getParameter("status") != null && !request.getParameter("status").equals("")){
				map.put("pay_status", String.valueOf(request.getParameter("status")));
			}
			else{
				map.put("pay_status", "ALL");
			}
			String phoneJson = checkBillServiceImpl.checkBillPhoneFee(map);
			@SuppressWarnings("unchecked")
			Map<String,String> backMap = JsonUtil.json2Map(phoneJson);
			if(backMap.get("sucess").equals("true")){
				ResponseUtil.write(request, response, backMap);
			}
			else{
				ResponseUtil.write(request,response,"1000","无数据","");
			}
			
		} catch (Exception e) {
			LOG.error("远程服务错误",e);
			ResponseUtil.write(request,response,"1000","远程服务错误","");
		}
	
	}
	@RequestMapping("/ipos/bill/consume")
	public void checkBillConsumeForPos(HttpServletRequest request, HttpServletResponse response){
		try {
			Map<String,String> map = this.fitingParamForIpos(request);
			map.remove("phoneno");
			String consumeJson = checkBillServiceImpl.checkBillConsume(map);
			@SuppressWarnings("unchecked")
			Map<String,String> backMap = JsonUtil.json2Map(consumeJson);
			if(backMap.get("sucess").equals("true")){
				ResponseUtil.write(request, response, backMap);
			}
			else{
				ResponseUtil.write(request,response,"1000","无数据","");
			}
			
		} catch (Exception e) {
			LOG.error("远程服务错误",e);
			ResponseUtil.write(request,response,"1000","远程服务错误","");
		}
	}
	/**
	 * 组装pos数据
	 * @param requestMap
	 * @return
	 */
	private Map<String,String> fitingParam(HttpServletRequest request){
		//烟草专卖证号 cigliceid
		//当前页页码 pagenumber
		//每页多少条数据 pagesize
		//可选参数---日期--格式：20130120 searchdate
		//可选参数 ---手机号 phoneno
		//可选参数---银行卡号 cardno
		//
		Map<String,String> map = new HashMap<String, String>();
		map.put("cigliceid", String.valueOf(request.getParameter("cigliceid")));
		map.put("page", String.valueOf(request.getParameter("pagenumber")));
		map.put("rows", String.valueOf(request.getParameter("pagesize")));
		String start = request.getParameter("dateStart");
		String end = request.getParameter("dateEnd");
		/*if(request.getParameter("searchdate") != null && !request.getParameter("searchdate").equals("")) {
			//消费查询
			map.put("start", String.valueOf(request.getParameter("searchdate")));
			map.put("end", String.valueOf(request.getParameter("searchdate")));
		}*/
		if(start != null && !start.equals("") && end != null && !end.equals("")){
			map.put("start", start);
			map.put("end", end);
		}
		else if((start!=null && !start.equals("")) && (end == null || "".equals(end))){
			map.put("start", start);
			map.put("end", DateUtil.getToday());
		}
		else if((start == null || "".equals("") && (end != null && !end.equals("")))){
			map.put("start", "20100101");
			map.put("end",end);
		}
		if(request.getParameter("phoneno") != null && !request.getParameter("phoneno").equals(""))
		map.put("phoneno", String.valueOf(request.getParameter("phoneno")));
		if(request.getParameter("cardno") != null && !request.getParameter("cardno").equals(""))
		map.put("cardno", String.valueOf(request.getParameter("cardno")));
		return map;
	}
	
	
	/**
	 * 组装pos数据
	 * @param requestMap
	 * @return
	 */
	private Map<String,String> fitingParamForIpos(HttpServletRequest request){
		//烟草专卖证号 cigliceid
		//当前页页码 pagenumber
		//每页多少条数据 pagesize
		//可选参数---日期--格式：20130120 searchdate
		//可选参数 ---手机号 phoneno
		//可选参数---银行卡号 cardno
		//
		Map<String,String> map = new HashMap<String, String>();
		map.put("cigliceid", String.valueOf(request.getParameter("cigliceid")));
		map.put("page", String.valueOf(request.getParameter("pagenumber")));
		map.put("rows", String.valueOf(request.getParameter("pagesize")));
		if(request.getParameter("searchdate") != null && !request.getParameter("searchdate").equals("")) {
			//电话查询
			map.put("searchdate", String.valueOf(request.getParameter("searchdate")));
		}
		if(!StringUtil.isBlank(request.getParameter("start")) &&
				!StringUtil.isBlank(request.getParameter("end"))){
			//消费查询
			map.put("start", String.valueOf(request.getParameter("start")));
			map.put("end", String.valueOf(request.getParameter("end")));
		}
		if(request.getParameter("phoneno") != null && !request.getParameter("phoneno").equals(""))
		map.put("phoneno", String.valueOf(request.getParameter("phoneno")));
		if(request.getParameter("cardno") != null && !request.getParameter("cardno").equals(""))
		map.put("cardno", String.valueOf(request.getParameter("cardno")));
		return map;
	}
}


