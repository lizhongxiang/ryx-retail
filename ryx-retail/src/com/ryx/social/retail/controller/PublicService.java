package com.ryx.social.retail.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.utils.Constants;
import com.ryx.framework.utils.HttpUtil;
import com.ryx.framework.utils.MapUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.ServiceHttpUtil;
import com.ryx.social.retail.util.DateUtil;
import com.ryx.social.retail.util.RetailConfig;


@Controller
public class PublicService extends BaseDaoImpl {
	private final Logger LOG = LoggerFactory.getLogger(PublicService.class);
	//齐鲁银行--查询
	private static String serviceSearch= "/qlb/300000001";
	//齐鲁银行-缴费
	private static String servicePay = "/qlb/300000002";
	//查询用户绑定的卡号信息
	private static String merchBindCard = "/qlb/700000003";
	//九七惠手机缴费地址
	private static String phonePay = "/rest/resource/tobacco/700000001";
	
	private static String creditCardRepay = "/rest/resource/tobacco/400000005";
	
	private static String qq = "http://192.168.0.41:7000/rtserver/rest/resource/tobacco/710000001";
	
	private final static int MAX_SYSTEM_TRACE_AUDIT_NUMBER = 999999;
	
	private static int systemTraceAuditNumber = 1;
	
	@RequestMapping("/qq/queryPrice")
	public void qqQueryPrice(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.CODE_SUCCESS;
		String msg = Constants.MSG_SUCCESS;
		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		data.put("220612", BigDecimal.ONE); // QQ币
		data.put("222309", BigDecimal.TEN); // 会员
		data.put("222301", BigDecimal.TEN); // 红钻
		data.put("222302", BigDecimal.TEN); // 黄钻
		data.put("222306", BigDecimal.TEN); // 绿钻
		data.put("222308", BigDecimal.TEN); // 蓝钻
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping("/qq/recharge")
	public void qqRecharge(@RequestParam Map<String, String> requestParam, HttpServletResponse response) {
		// 组装返回客户端的结果信息
		Map<String, Object> outgoing = new HashMap<String, Object>();
		outgoing.put("code", Constants.CODE_SUCCESS);
		outgoing.put("msg", Constants.MSG_SUCCESS);
		// 组装请求信用卡信息的参数
		String url = qq;
		
		Map<String, String> params = JsonUtil.json2Map(requestParam.get("params"));
		
		String businessType = params.get("business_type");
		String qqNumber = params.get("qq_number");
		String rechargeAmount = params.get("recharge_amount");
		String rechargeQuantity = params.get("recharge_quantity");
		String track2 = params.get("track_2_data");
		String track3 = params.get("track_3_data");
		// 组装各个域的值
		Map<String, String> queryParam = new HashMap<String, String>();
		
		// 卡号
		queryParam.put("field2", params.get("field2"));
		// 交易处理码 跟手机充值一致
		queryParam.put("field3", "000000"); 
		// 充值金额
		queryParam.put("field4", parse2Field4(rechargeAmount, 12));
		// 系统跟踪号 跟手机充值一致
		queryParam.put("field11", "");
		// 卡有效期 跟手机充值一致
		queryParam.put("field14", "");
		if(this.isIcCard(track2)){
			queryParam.put("field22", "051");
			queryParam.put("field23", "001");
		} else{
			queryParam.put("field22", "021");
			queryParam.put("field23", "000");
		}
		queryParam.put("field25", "00");
		queryParam.put("field26", "06");
		// 2磁信息
		queryParam.put("field35", track2);
		// 3磁信息
		queryParam.put("field36", track3);
		queryParam.put("field41", params.get("field41"));
		queryParam.put("field42", params.get("field42"));
		// 币种 只支持人民币
		queryParam.put("field49", "156");
		queryParam.put("field52", params.get("field52"));
		// 安全控制信息 不加密
		queryParam.put("field53", "2600000000000000");
		queryParam.put("field55", params.get("field55"));
		// POSP
		queryParam.put("field60", "22000074");
		// POSP
		queryParam.put("field62", "");

		// 交易码
		queryParam.put("fieldTrancode", "710000001");
		queryParam.put("fieldChannel", "03");
		queryParam.put("fieldMAB", params.get("fieldMAB"));
		queryParam.put("fieldMAC", params.get("fieldMAC"));
		queryParam.put("Mobile", qqNumber); // QQ号码
		queryParam.put("cardid", businessType); // 业务编号
		queryParam.put("tran_num", rechargeQuantity); // 充值数量
		queryParam.put("PSAMID", "");
		queryParam.put("ReaderID", "");
		queryParam.put("termMobile", "");
		queryParam.put("fieldAddress", "");
		
		try {
			// 对外请求信用卡信息 并组装结果信息
			LOG.debug("QQ充值报文: " + queryParam);
			String queryResultString = ServiceHttpUtil.post(url, JsonUtil.map2json(queryParam));
			LOG.debug("QQ充值返回信息: " + queryResultString);
			Map<String, Object> queryResult = JsonUtil.json2Map(queryResultString);
			if(queryResult == null ||(queryResult.get("code") != null && queryResult.get("code").equals("4001"))){
				outgoing.put("code", Constants.CODE_FAIL);
				outgoing.put("msg", "远程服务错误，请稍后操作。");
			}else{
				outgoing.put("result", queryResult);
			}
		} catch (Exception e) {
			outgoing.put("code", Constants.CODE_FAIL);
			outgoing.put("msg", Constants.MSG_FAIL);
			outgoing.remove("result");
		}
		// 向客户端返回结果
		ResponseUtil.doWriteResponse(response, JsonUtil.map2json(outgoing, JsonUtil.LOWER));
	}
	
	/**
	 * 查询信用卡是否支持还款
	 */
	@RequestMapping("/retail/finance/queryCreditCard")
	public void queryCreditCard(@RequestParam Map<String, String> params, HttpServletResponse response) {
		// 组装返回客户端的结果信息
		Map<String, Object> outgoing = new HashMap<String, Object>();
		outgoing.put("code", Constants.CODE_SUCCESS);
		outgoing.put("msg", Constants.MSG_SUCCESS);
		// 组装请求信用卡信息的参数
		String url = RetailConfig.getQueryCreditCardUrl() + "/repayment/cardinfo";
		String creditCardNumber = params.get("credit_card_number");
		Map<String, String> queryParam = new HashMap<String, String>();
		queryParam.put("card_no", creditCardNumber);
		
		try {
			// 对外请求信用卡信息 并组装结果信息
			String queryResultString = HttpUtil.post(url, queryParam);
			Map<String, Object> queryResult = JsonUtil.json2Map(queryResultString);
			if(Constants.CODE_SUCCESS.equals(MapUtil.getString(queryResult, "code"))) {
				Map<String, Object> result = MapUtil.get(queryResult, "result", Collections.EMPTY_MAP);
				outgoing.put("result", result);
			} else {
				outgoing.put("code", Constants.CODE_FAIL);
				outgoing.put("msg", MapUtil.getString(queryResult, "msg"));
			}
		} catch (Exception e) {
			outgoing.put("code", Constants.CODE_FAIL);
			outgoing.put("msg", Constants.MSG_FAIL);
			outgoing.remove("result");
		}
		// 向客户端返回结果
		ResponseUtil.doWriteResponse(response, JsonUtil.map2json(outgoing, JsonUtil.LOWER));
	}
	
	/**
	 * 信用卡还款
	 */
	@RequestMapping("/retail/finance/repayCreditCard")
	public void repayCreditCard(@RequestParam Map<String, String> requestParam, HttpServletResponse response) {
		// 组装返回客户端的结果信息
		Map<String, Object> outgoing = new HashMap<String, Object>();
		outgoing.put("code", Constants.CODE_SUCCESS);
		outgoing.put("msg", Constants.MSG_SUCCESS);
		// 组装请求信用卡信息的参数
		String url = RetailConfig.getRepayCreditCardUrl() + creditCardRepay;
		
		Map<String, String> params = JsonUtil.json2Map(requestParam.get("params"));
		
		String reservedPhoneNumber = params.get("cellphone_number");
		String creditCardNumber = params.get("credit_card_number");
		String repaymentAmount = params.get("repayment_amount");
		String datetimeString = DateUtil.getCompactDateTimeString(DateUtil.getNow());
		// 组装各个域的值
		Map<String, String> queryParam = new HashMap<String, String>();
		queryParam.put("field2", params.get("field2"));
		queryParam.put("field3", "740000"); // 此交易类型固定值
		// 还款金额
		queryParam.put("field4", parse2Field4(repaymentAmount, 12));
		// 还款时间
		queryParam.put("field7", datetimeString);
		queryParam.put("field11", parse2Field11(getSystemTraceAuditNumber(), 6));
		queryParam.put("field22", "021"); // 磁条卡 021 IC卡 051
		queryParam.put("field25", "00"); // 固定值
//		queryParam.put("field26", "06"); // 服务点PIN获取码 获取不到且不是必传
		// 2磁信息
		queryParam.put("field35", params.get("track_2_data"));
		// 3磁信息
		queryParam.put("field36", params.get("track_3_data"));
		queryParam.put("field41", params.get("field41"));
		queryParam.put("field42", params.get("field42"));
		// 手机号 + 卡号位数 + 卡号
		queryParam.put("field48", parse2Field48(reservedPhoneNumber, creditCardNumber));
		queryParam.put("field49", "156"); // 只支持人民币
		queryParam.put("field52", params.get("field52"));
		queryParam.put("field53", "2600000000000000"); // 不加密
//		queryParam.put("field55", params.get("field55"));
		queryParam.put("field60", "00000074000500"); // 固定
		queryParam.put("fieldChannel", "03");
		queryParam.put("fieldMAB", params.get("fieldMAB"));
		queryParam.put("fieldMAC", params.get("fieldMAC"));
		queryParam.put("fieldTrancode", "400000005"); // 后台约定
		
		try {
			// 对外请求信用卡信息 并组装结果信息
			LOG.debug("信用卡还款报文: " + queryParam);
			String queryResultString = ServiceHttpUtil.post(url, JsonUtil.map2json(queryParam));
			LOG.debug("信用卡还款返回信息: " + queryResultString);
			Map<String, Object> queryResult = JsonUtil.json2Map(queryResultString);
			if(Constants.CODE_SUCCESS.equals(MapUtil.getString(queryResult, "code"))) {
				Map<String, Object> result = MapUtil.get(queryResult, "result", Collections.EMPTY_MAP);
				outgoing.put("result", result);
			} else {
				outgoing.put("code", Constants.CODE_FAIL);
				outgoing.put("msg", MapUtil.getString(queryResult, "msg"));
			}
		} catch (Exception e) {
			outgoing.put("code", Constants.CODE_FAIL);
			outgoing.put("msg", Constants.MSG_FAIL);
			outgoing.remove("result");
		}
		// 向客户端返回结果
		ResponseUtil.doWriteResponse(response, JsonUtil.map2json(outgoing, JsonUtil.LOWER));
	}
	
	private int getSystemTraceAuditNumber() {
		if(systemTraceAuditNumber>=MAX_SYSTEM_TRACE_AUDIT_NUMBER) systemTraceAuditNumber=1;
		return systemTraceAuditNumber++;
	}
	
	private String parse2Field4(String amount, int length) {
		return StringUtils.leftPad(String.valueOf((int) (Double.parseDouble(amount)*100)), length, "0");
	}
	
	private String parse2Field11(int number, int length) {
		return StringUtils.leftPad(String.valueOf(number), length, "0");
	}
	
	private String parse2Field48(String cellphoneNumber, String creditCardNumber) {
		return cellphoneNumber + creditCardNumber.length() + creditCardNumber;
	}
	
	/**
	 * 用户信息查询
	 * fieldChannel 02渠道查询用户信息；
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/pay/search")
	public void search(HttpServletRequest request, HttpServletResponse response){
		String paySearchURL = RetailConfig.getPublicServiceServer()+serviceSearch;
		Map<String,String> map = new HashMap<String, String>();
		//String userType = request.getParameter("user_type");
		
		String field3 = request.getParameter("field3");//缴费类型
		String pay_no = request.getParameter("pay_no");
		String oper_type = request.getParameter("oper_type");
		String area_no = request.getParameter("area_no");
		

		map.put("pay_no", pay_no);
		map.put("fieldChannel", "02");
		map.put("pay_year_mon", "000001");
		
		if(field3.equals("Ext_QueryRQ")){
			map.put("oper_type", oper_type);
			map.put("field3", field3);
			map.put("message_code", "D104");
		}
		else if(field3.equals("Ext_QueryYX")){
			map.put("oper_type", oper_type);
			map.put("field3", field3);
			map.put("message_code", "D108");
		}
		else if(field3.equals("Ext_QueryDF")){
			map.put("oper_type", oper_type);
			map.put("field3", field3);
			map.put("query_type", oper_type);
			map.put("message_code","D103");
		}
		else if(field3.equals("Ext_QueryDX") ){
			map.put("field3", "Ext_QueryDX");
			map.put("prd_uid", "01");
			map.put("message_code", "D101");
			map.put("area_no", area_no);
		}
		else if(field3.equals("Ext_QueryWT") ){
			map.put("field3", "Ext_QueryWT");
			map.put("prd_uid", "02");
			map.put("message_code", "D102");
			map.put("area_no", area_no);
			//手机
			if(pay_no.length() == 11){
				map.put("oper_type", "GSM");
			}
			//联通座机
			else{
				map.put("area_no", area_no);
			}
		}
		String responseData = "";
		String reqParam = JsonUtil.map2json(map);
		try {
			LOG.info(" = * = * = * = * =公缴费查询提交报文02渠道= * = * = * = * = "+reqParam);
			responseData = ServiceHttpUtil.post(paySearchURL, reqParam);
			LOG.info(" = * = * = * = * =公缴费查询响应报文02渠道= * = * = * = * = "+responseData);
			@SuppressWarnings("unchecked")
			Map<String,String> repMap = JsonUtil.json2Map(responseData);
			if(repMap == null ||(repMap.get("code") != null && repMap.get("code").equals("4001"))){
				LOG.error(" = * = * = * = * =公缴费查询超时02渠道= * = * = * = * = "+reqParam);
				ResponseUtil.write(request, response, "1002", "远程服务出错，请售后操作。", "");
			}else{
				ResponseUtil.write(request, response, repMap);
			}
		} catch (Exception e) {
			LOG.error(" = * = * = * = * =公缴费查询出错02渠道= * = * = * = * = "+reqParam, e);
			ResponseUtil.write(request, response, "1001", "请稍后操作", "");
		}
	}
	
	
	
	/**
	 * 齐鲁银行查询----使用POS方式查询
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/pay/searchbypos")
	public void searchByPOS(HttpServletRequest request, HttpServletResponse response){
		String paySearchURL = RetailConfig.getPublicServiceServer()+serviceSearch;
		String field3 = request.getParameter("field3");//缴费类型
		String pay_no = request.getParameter("pay_no");
		String field41 = request.getParameter("field41");
		String field42 = request.getParameter("field42");
		String fieldMAB = request.getParameter("fieldMAB");
		String fieldMAC = request.getParameter("fieldMAC");
		String oper_type = request.getParameter("oper_type");
		String area_no = request.getParameter("area_no");
		String reqMeg = "";
		if(field3.equals("Ext_QueryRQ")){
			reqMeg = this.rqSearchMeg(field3, pay_no, field41, field42,fieldMAB,fieldMAC);
		}
		else if(field3.equals("Ext_QueryYX")){
			reqMeg = this.yxSearchMeg(field3, pay_no, field41, field42,fieldMAB,fieldMAC);
		}
		else if(field3.equals("Ext_QueryDF")){
			reqMeg = this.ydSearchMeg( field3, pay_no, field41, field42,fieldMAB,fieldMAC,oper_type);
		}
		else if(field3.equals("Ext_QueryDX") || field3.equals("Ext_QueryWT") ){
			reqMeg = this.dhSearchMeg( field3, pay_no, field41, field42,fieldMAB,fieldMAC,oper_type,area_no);
		}
		try {
			LOG.info(" = * = * = * = * =公缴费查询提交报文= * = * = * = * = "+reqMeg);
			String responseData = ServiceHttpUtil.post(paySearchURL, reqMeg);
			LOG.info(" = * = * = * = * =公缴费查询响应报文= * = * = * = * = "+responseData);
			@SuppressWarnings("unchecked")
			Map<String,String> repMap = JsonUtil.json2Map(responseData);
			if(repMap == null ||(repMap.get("code") != null && repMap.get("code").equals("4001"))){
				LOG.error(" = * = * = * = * =公缴费查询超时= * = * = * = * = "+reqMeg);
				ResponseUtil.write(request, response, "1002", "远程服务出错，请售后操作。", "");
			}else{
				ResponseUtil.write(request, response, repMap);
			}
			
		} catch (Exception e) {
			LOG.error(" = * = * = * = * =公缴费查询出错= * = * = * = * = "+reqMeg, e);
			ResponseUtil.write(request, response, "1001", "请稍后操作", "");
		}
		
	}
	/**
	 * 齐鲁银行缴费，使用输入卡号密码方式支付
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/pay/merchManual/")
	public void payByMerchCardBind(HttpServletRequest request, HttpServletResponse response){
		String payTVURL =RetailConfig.getPublicServiceServer()+servicePay;
		String cardNo = request.getParameter("cardNo");
		String password = request.getParameter("password");
		String payMoney = request.getParameter("payMoney");
		String backResponseData = request.getParameter("responseData");
		String feeType = request.getParameter("feeType");
		String payJson = "";
		try {
			@SuppressWarnings("unchecked")
			Map<String,String> responseDataMap = JsonUtil.json2Map(backResponseData);
			payJson=this.payFitting(feeType,cardNo, payMoney, password, responseDataMap);
			LOG.info(" = * = * = * = * =公缴费缴费提交报文02渠道= * = * = * = * = "+payJson);
			String payResponseData = ServiceHttpUtil.post(payTVURL, payJson);
			LOG.info(" = * = * = * = * =公缴费缴费响应报文02渠道= * = * = * = * = "+payResponseData);
			@SuppressWarnings("unchecked")
			Map<String,String> repMap = JsonUtil.json2Map(payResponseData);
			if(repMap == null ||(repMap.get("code") != null && repMap.get("code").equals("4001"))){
				LOG.error(" = * = * = * = * =公缴费缴费超时02渠道= * = * = * = * = "+payJson);
				ResponseUtil.write(request, response, "1002", "远程服务出错，请售后操作。", "");
			}else{
				ResponseUtil.write(request, response, repMap);
			}
			
		} catch (Exception e) {
			LOG.error(" = * = * = * = * == 公缴费缴费出错02渠道* = * = * = * = "+payJson, e);
			ResponseUtil.write(request, response, "1001", "请稍后操作", "");
		}
		
		
	}
	
	/**
	 * 齐鲁银行缴费  刷卡方式
	 * @param request
	 * @param response
	 */
	@RequestMapping("/retail/pay/pay")
	public void digitalTVPay(HttpServletRequest request, HttpServletResponse response){
		String payTVURL =RetailConfig.getPublicServiceServer()+servicePay;
		String field2 = request.getParameter("field2");//卡号
		String field52 = request.getParameter("field52");//密码
		String field4 = request.getParameter("field4");//金额
		String backResponseData = request.getParameter("responseData");//查询数据
		String field3 = request.getParameter("field3");//缴费类型
		String track_2_data = request.getParameter("track_2_data");//二磁
		String track_3_data = request.getParameter("track_3_data");//三磁
		String fieldMAB = request.getParameter("fieldMAB");//mab域信息
		String fieldMAC = request.getParameter("fieldMAC");//mac加密信息
		String field41 = request.getParameter("field41");//终端编号
		String field42 = request.getParameter("field42");//商户编号
		String field55 = request.getParameter("field55");
		/* 20140920 IC卡后台已发布到生产，可以支持IC卡
		if(this.isIcCard(track_2_data)){
			ResponseUtil.write(request, response, "1002", "增值业务暂不支持IC卡!", "");
			return;
		}*/
		@SuppressWarnings("unchecked")
		Map<String,String> responseDataMap = JsonUtil.json2Map(backResponseData);
		String payMeg = "";
		if("Ext_QueryRQ".equals(field3)){
			payMeg = this.fitRQPayMeg(field2, field4, field52, responseDataMap,
					track_2_data,track_3_data,fieldMAB,fieldMAC,field41,field42,field55);
		}
		else if("Ext_QueryYX".equals(field3)) {
			payMeg = this.fitYXPayMeg(field2, field4, field52, responseDataMap,
					track_2_data,track_3_data,fieldMAB,fieldMAC,field41,field42,field55);
		}
		else if("Ext_QueryDF".equals(field3)) {
			payMeg = this.fitDFPayMeg(field2, field4, field52, responseDataMap,
					track_2_data,track_3_data,fieldMAB,fieldMAC,field41,field42,field55);
		}
		else if("Ext_QueryDX".equals(field3)||"Ext_QueryWT".equals(field3)){
			payMeg = this.fitDHPayMeg(field3,field2, field4, field52, responseDataMap,
					track_2_data,track_3_data,fieldMAB,fieldMAC,field41,field42,field55);
		}
		try {
			LOG.info(" = * = * = * = * =公缴费请求报文数据 = * = * = * = * = "+payMeg);
			String payResponseData = ServiceHttpUtil.post(payTVURL, payMeg);
			LOG.info(" = * = * = * = * =公缴费响应报文数据 = * = * = * = * = "+payResponseData);
			@SuppressWarnings("unchecked")
			Map<String,String> repMap = JsonUtil.json2Map(payResponseData);
			if(repMap == null ||(repMap.get("code") != null && repMap.get("code").equals("4001"))){
				LOG.error(" = * = * = * = * =公缴费缴费超时 = * = * = * = * = "+payMeg);
				ResponseUtil.write(request, response, "1002", "远程服务错误，请售后操作。", "");
			}else{
				ResponseUtil.write(request, response, repMap);
			}
		} catch (Exception e) {
			LOG.error(" = * = * = * = * = 公缴费出错 = * = * = * = * = "+payMeg, e);
			ResponseUtil.write(request, response, "1001", "请稍后操作", "");
		}
		
	}
	
	/**
	 * 手机充值
	 * @param request
	 * @param response
	 */
	@RequestMapping("retail/pay/phone")
	public void payPhoenFee(HttpServletRequest request, HttpServletResponse response){
		String phonePayServerURL = RetailConfig.getPhonePayServer()+phonePay;
		//"cardNo":cardNo,"payMoney":payMoney,"password":password,"phoneNum":phoneNum
		String cardNo= request.getParameter("cardNo");
		String payMoney= request.getParameter("payMoney");
		String password= request.getParameter("password");
		String phoneNum = request.getParameter("phoneNum");
		String track_2_data = request.getParameter("track_2_data");
		String track_3_data = request.getParameter("track_3_data");
		String fieldMAB = request.getParameter("fieldMAB");
		String fieldMAC = request.getParameter("fieldMAC");
		String field41 = request.getParameter("field41");
		String field42 = request.getParameter("field42");
		String field55 = request.getParameter("field55");
		/// 20140922 手机充值暂不支持IC卡，因IC卡交易在百富POS机上流程存在问题，需重新设计
		if(this.isIcCard(track_2_data)){
			ResponseUtil.write(request, response, "1002", "手机充值暂不支持IC卡!", "");
			return;
		}
		Map<String ,String> map = this.fittingPayPhoenFee(cardNo, payMoney, phoneNum, password,track_2_data,track_3_data,fieldMAB,fieldMAC,field41,field42,field55);
		String payJson =JsonUtil.map2json(map); 
		try {
			LOG.info(" = * = * = * = * = 手机充值请求报文数据 = * = * = * = * = "+payJson);
			String payResponseData = ServiceHttpUtil.post(phonePayServerURL, payJson);
			LOG.info(" = * = * = * = * = 手机充值响应报文数据= * = * = * = * = "+payResponseData);
			@SuppressWarnings("unchecked")
			Map<String,String> repMap = JsonUtil.json2Map(payResponseData);
			if(repMap == null ||(repMap.get("code") != null && repMap.get("code").equals("4001"))){
				LOG.error(" = * = * = * = * =手机充值超时 * = * = * = * = "+payJson);
				ResponseUtil.write(request, response, "1002", "远程服务错误，请稍后操作。", null);
			}else{
				ResponseUtil.write(request, response, repMap);
			}
		} catch (Exception e) {
			LOG.error(" = * = * = * = * = 手机充值出错 = * = * = * = * = "+payJson, e);
			ResponseUtil.write(request, response, "1001", "请稍后操作", "");
		}
	}
	@RequestMapping("retail/pay/phone/bind")
	public void payPhoenFeeByBind(HttpServletRequest request, HttpServletResponse response){
		String phonePayServerURL =RetailConfig.getPhonePayServer()+phonePay;
		//"cardNo":cardNo,"payMoney":payMoney,"password":password,"phoneNum":phoneNum
		String cardNo= request.getParameter("cardNo");
		String payMoney= request.getParameter("payMoney");
		String password= request.getParameter("password");
		String phoneNum = request.getParameter("phoneNum");
		String payJson = "";
		try {
			Map<String ,String> map = this.fittingPayPhoenFee(cardNo, payMoney, phoneNum, password);
			payJson =JsonUtil.map2json(map); 
			LOG.info(" = * = * = * = * = 手机充值绑定卡支付提交报文 = * = * = * = * = "+payJson);
			String payResponseData = ServiceHttpUtil.post(phonePayServerURL, payJson);
			LOG.info(" = * = * = * = * = 手机充值绑定卡支付响应报文 = * = * = * = * = "+payResponseData);
			@SuppressWarnings("unchecked")
			Map<String,String> repMap = JsonUtil.json2Map(payResponseData);
			if(repMap == null ||(repMap.get("code") != null && repMap.get("code").equals("4001"))){
				LOG.error(" = * = * = * = * = 手机支付绑定卡支付超时 = * = * = * = * = "+payJson);
				ResponseUtil.write(request, response, "1001", "远程服务错误，请稍后操作。", null);
			}else{
				ResponseUtil.write(request, response, repMap);
			}
		} catch (Exception e) {
			LOG.error(" = * = * = * = * = 手机支付绑定卡支付出错 = * = * = * = * = "+payJson, e);
			ResponseUtil.write(request, response, "1002", "请稍后操作", "");
		}
	}
	private Map<String,String> fittingPayPhoenFee(String cardNum,String fee,String phoneNum,String password){
		Map<String,String> map = new HashMap<String, String>();
		map.put("fieldTrancode", "700000001");
		map.put("field2", cardNum);
		map.put("field3", "000000");
		map.put("field4", fee);
		map.put("field11","");
		map.put("field22", "021");
		map.put("field23", "000");
		map.put("field25", "00");
		map.put("field26", "06");
		map.put("field41", "88500400");
		map.put("field42", "885110159930073");
		map.put("field49", "156");
		map.put("field53", "2600000000000000");
		map.put("field60", "22000074");
		map.put("Mobile", phoneNum);
		map.put("fieldChannel", "02");
		map.put("field52",password);
		return map;
	}
	private String rqSearchMeg(String field3,String pay_no,String field41,String field42,String fieldMAB,String fieldMAC){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60", "22000074");//pos数据
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("fieldChannel", "03");
		map.put("oper_type", "");
		map.put("pay_year_mon", "000001");
		map.put("field55", "");
		map.put("field53", "2600000000000000");
		map.put("field26", "06");
		map.put("prd_uid", "");
		map.put("field3", "Ext_QueryRQ");
		map.put("field25", "00");
		map.put("field42", "");
		map.put("field23", "000");
		map.put("field49", "156");
		map.put("field22", "021");
		map.put("field7", "");
		map.put("area_no", "");
		map.put("field11", "");
		map.put("field41", "");
		//map.put("fieldMAC", fieldMAC);
		//map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode", "300000001");
		map.put("pay_no", pay_no);
		map.put("message_code", "D104");
		return JsonUtil.map2json(map);
	}
	private String yxSearchMeg(String field3,String pay_no,String field41,String field42,String fieldMAB,String fieldMAC){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60", "22000074");//pos数据
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("fieldChannel", "03");
		map.put("oper_type", "");
		map.put("pay_year_mon", "000001");
		map.put("field55", "");
		map.put("field53", "2600000000000000");
		map.put("field26", "06");
		map.put("prd_uid", "");
		map.put("field3", "Ext_QueryYX");
		map.put("field25", "00");
		map.put("field42", field42);
		map.put("field23", "000");
		map.put("field49", "156");
		map.put("field22", "021");
		map.put("field7", "");
		map.put("area_no", "");
		map.put("field11", "");
		map.put("field41", field41);
		map.put("fieldMAC", fieldMAC);
		map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode", "300000001");
		map.put("pay_no", pay_no);
		map.put("message_code", "D108");
		return JsonUtil.map2json(map);
	}
	private String ydSearchMeg(String field3,String pay_no,String field41,String field42,String fieldMAB,String fieldMAC,String oper_type){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60", "22000074");//pos数据
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("fieldChannel", "03");
		map.put("oper_type", oper_type);
		map.put("pay_year_mon", "000001");
		map.put("field55", "");
		map.put("field53", "2600000000000000");
		map.put("field26", "06");
		map.put("prd_uid", "");
		map.put("field3", "Ext_QueryDF");
		map.put("field25", "00");
		map.put("field42", field42);
		map.put("field23", "000");
		map.put("field49", "156");
		map.put("field22", "021");
		map.put("field7", "");
		map.put("area_no", "");
		map.put("field11", "");
		map.put("field41", field41);
		map.put("fieldMAC", fieldMAC);
		map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode", "300000001");
		map.put("pay_no", pay_no);
		map.put("message_code", "D103");
		return JsonUtil.map2json(map);
	}
	private String dhSearchMeg(String field3,String pay_no,String field41,String field42,String fieldMAB,String fieldMAC,String oper_type,String area_no){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60", "22000074");//pos数据
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("fieldChannel", "03");
		map.put("oper_type", oper_type);
		map.put("pay_year_mon", "000001");
		map.put("field55", "");
		map.put("field53", "2600000000000000");
		map.put("field26", "06");
		
		map.put("field3", field3);
		map.put("field25", "00");
		map.put("field42", field42);
		map.put("field23", "000");
		map.put("field49", "156");
		map.put("field22", "021");
		map.put("field7", "");
		map.put("area_no", area_no);
		map.put("field11", "");
		map.put("field41", field41);
		map.put("fieldMAC", fieldMAC);
		map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode", "300000001");
		map.put("pay_no", pay_no);
		if(field3.equals("Ext_QueryWT")){
			map.put("prd_uid", "02");
			map.put("message_code", "D102");
		}
		else{
			map.put("message_code", "D101");
			map.put("prd_uid", "01");
		}
		
		return JsonUtil.map2json(map);
	}
	@RequestMapping("/retail/pay/card")
	public void getMerchCardInfo(HttpServletRequest request, HttpServletResponse response){
		SessionInfo user = IdentityUtil.getUser(request);
		String liceID = user.getLiceId();
		String merchBindCardInfoURL = RetailConfig.getPublicServiceServer()+merchBindCard;
		Map<String,String> param = new HashMap<String, String>();
		param.put("fieldZMID", liceID);
		try {
			String responseData = ServiceHttpUtil.post(merchBindCardInfoURL, JsonUtil.map2json(param));
			//String responseData = "{"cardNo":"6223795310109656953","fieldTranCode":"700000003","field39":"0","fieldMessage":"查询成功！"}";
			@SuppressWarnings("unchecked")
			Map<String,Object> responseMap = JsonUtil.json2Map(responseData);
			List<Map<String,String>> list = new ArrayList<Map<String,String>>();
			if("0".equals(responseMap.get("field39"))){
				String[] codNoArray = responseMap.get("cardNo").toString().split(",");
				for(int i = 0 ; i < codNoArray.length ; i++){
					Map<String,String> cards = new HashMap<String, String>();
					cards.put("card", codNoArray[i]);
					list.add(cards);
				}
				responseMap.put("cardNo", list);
			}
			else{
				responseMap.put("cardNo", list);
			}
			ResponseUtil.write(request, response, responseMap);
		} catch (Exception e) {
			LOG.info("PublicService getMerchCardInfo", e);
			ResponseUtil.write(request, response, "1001","获取卡号信息出错",null);
		}
		
	}
	
	/**
	 * 手机充值报文 信息
	 * @param cardNum 卡号
	 * @param fee     充值金额
	 * @param phoneNum 手机号
	 * @param password 密码密文
	 * @param track_2_data 二磁信息
	 * @param track_3_data 三磁信息
	 * @param fieldMAB  
	 * @param fieldMAC
	 * @param field41 终端号
	 * @param field42 用户号
	 * @return
	 */
	private Map<String,String> fittingPayPhoenFee(String cardNum,String fee,String phoneNum,String password,
			String track_2_data,String track_3_data,String fieldMAB,String fieldMAC,String field41,String field42,String field55){
		Map<String,String> map = new HashMap<String, String>();
		map.put("fieldTrancode", "700000001");
		map.put("field2", cardNum);
		map.put("field3", "000000");
		map.put("field4", fee);
		map.put("field11", "");
		if(this.isIcCard(track_2_data)){
			map.put("field23", "001");
			map.put("field22", "051");
		}
		else{
			map.put("field23", "000");
			map.put("field22", "021");
		}
		map.put("field25", "00");
		map.put("field26", "06");
		map.put("field41", field41);
		map.put("field42", field42);
		map.put("field49", "156");
		map.put("field53", "2600000000000000");
		map.put("field60", "22000074");
		map.put("Mobile", phoneNum);
		map.put("fieldChannel", "03");
		map.put("field52",password);
		map.put("field35", track_2_data);
		map.put("field36",track_3_data);
		map.put("fieldMAB", fieldMAB);
		map.put("fieldMAC", fieldMAC);
		map.put("fieldAddress", "");
		map.put("PSAMID", "");
		map.put("field62", "");
		map.put("field62", "");
		map.put("ReaderID", "");
		map.put("field55", field55);
		map.put("field14", "");
		map.put("termMobile", "");
		return map;
	}
	/**
	 * 燃气缴费报文信息
	 * @param feeType 
	 * @param cardNo
	 * @param payMoney
	 * @param password
	 * @param responseDataMap
	 * @param track_2_data
	 * @param track_3_data
	 * @param fieldMAB
	 * @param fieldMAC
	 * @param field41
	 * @param field42
	 * @return
	 */
	private String fitRQPayMeg(String field2, String field4, String field52, Map<String,String> responseDataMap,
			String track_2_data,String track_3_data,String fieldMAB,String fieldMAC,String field41,String field42,String field55){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60","22000074" ); 
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("must_pay_sum_amt", responseDataMap.get("must_pay_sum_amt"));
		map.put("last_bal", responseDataMap.get("last_bal"));
		map.put("field26", "06");//pos上传报文
		map.put("field4",field4);//
		map.put("field3", "Ext_Fee");
		map.put("field25","00" );
		map.put("field42", field42);///商户编号
		map.put("field2", field2);//卡号
		map.put("field49",responseDataMap.get("field49"));//pos上传报文
		
		if(this.isIcCard(track_2_data)){
			map.put("field23", "001");
			map.put("field22", "051");
		}
		else{
			map.put("field23", "000");
			map.put("field22", "021");
		}
		
		
		
		map.put("pay_type", "2");
		map.put("field7", "20140505095455");//pos上传报文
		map.put("fieldMAC",fieldMAC );
		map.put("field41", field41);//终端编号
		map.put("arrear_no", responseDataMap.get("arrear_no"));
		map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode","300000002" );
		map.put("message_code", "D114");
		map.put("field52", field52);//密码
		map.put("fieldChannel", "03");
		map.put("oper_type","");
		map.put("pay_year_mon", "000001");
		map.put("field55", field55);
		map.put("field53", "2600000000000000");
		map.put("current_arrearage", responseDataMap.get("current_arrearage"));
		map.put("user_name",responseDataMap.get("user_name") );
		map.put("prd_uid", "04");
		map.put("field35", track_2_data);//二磁
		map.put("field36",track_3_data );//三次
		map.put("address", responseDataMap.get("address"));
		map.put("area_no", "");
		map.put("field11", "");
		map.put("pay_no", responseDataMap.get("pay_no"));
		map.put("termMobile", "");
		map.put("ReaderID", "");
		
		
		String payJson = JsonUtil.map2json(map);
		return payJson;
	}
	/**
	 * 有限电视缴费
	 * @param feeType 
	 * @param cardNo
	 * @param payMoney
	 * @param password
	 * @param responseDataMap
	 * @param track_2_data
	 * @param track_3_data
	 * @param fieldMAB
	 * @param fieldMAC
	 * @param field41
	 * @param field42
	 * @return
	 */
	private String fitYXPayMeg(String field2,String  field4, String field52, Map<String,String> responseDataMap,
			String track_2_data,String track_3_data,String fieldMAB,String fieldMAC,String field41,String field42,String field55){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60","22000074" ); 
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("must_pay_sum_amt", responseDataMap.get("must_pay_sum_amt"));
		map.put("last_bal", responseDataMap.get("last_bal"));
		map.put("field26", "06");//pos上传报文
		map.put("field4",field4);//
		map.put("field3", "Ext_Fee");
		map.put("field25","00" );
		map.put("field42", field42);///商户编号
		map.put("field2", field2);//卡号
		map.put("field49",responseDataMap.get("field49"));//pos上传报文
		
		if(this.isIcCard(track_2_data)){
			map.put("field23", "001");
			map.put("field22", "051");
		}
		else{
			map.put("field23", "000");
			map.put("field22", "021");
		}
		
		map.put("pay_type", "2");
		map.put("field7", "20140505095455");//pos上传报文
		map.put("fieldMAC",fieldMAC );
		map.put("field41", field41);//终端编号
		map.put("arrear_no", responseDataMap.get("arrear_no"));
		map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode","300000002" );
		map.put("message_code", "D118");
		map.put("field52", field52);//密码
		map.put("fieldChannel", "03");
		map.put("oper_type","");
		map.put("pay_year_mon", "000001");
		map.put("field55", field55);
		map.put("field53", "2600000000000000");
		map.put("current_arrearage", responseDataMap.get("current_arrearage"));
		map.put("user_name",responseDataMap.get("user_name") );
		map.put("prd_uid", "23");
		map.put("field35", track_2_data);//二磁
		map.put("field36",track_3_data );//三次
		map.put("address", responseDataMap.get("address"));
		map.put("area_no", "");
		map.put("field11", "");
		map.put("pay_no", responseDataMap.get("pay_no"));
		String payJson = JsonUtil.map2json(map);
		return payJson;
	
	}
	/**
	 * 用电缴费
	 * @param feeType 
	 * @param cardNo
	 * @param payMoney
	 * @param password
	 * @param responseDataMap
	 * @param track_2_data
	 * @param track_3_data
	 * @param fieldMAB
	 * @param fieldMAC
	 * @param field41
	 * @param field42
	 * @return
	 */
	private String fitDFPayMeg(String field2, String field4, String field52,Map<String,String>  responseDataMap,
			String track_2_data,String track_3_data,String fieldMAB,String fieldMAC,String field41,String field42,String field55){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60","22000074" ); 
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("must_pay_sum_amt", responseDataMap.get("must_pay_sum_amt"));
		map.put("last_bal", responseDataMap.get("last_bal"));
		map.put("field26", "06");//pos上传报文
		map.put("field4",field4);//
		map.put("field3", "Ext_Fee");
		map.put("field25","00" );
		map.put("field42", field42);///商户编号
		map.put("field2", field2);//卡号
		map.put("field49",responseDataMap.get("field49"));//pos上传报文
		if(this.isIcCard(track_2_data)){
			map.put("field23", "001");
			map.put("field22", "051");
		}
		else{
			map.put("field23", "000");
			map.put("field22", "021");
		}
		map.put("pay_type", "2");
		map.put("field7", "20140505095455");//pos上传报文
		map.put("fieldMAC",fieldMAC );
		map.put("field41", field41);//终端编号
		map.put("arrear_no", responseDataMap.get("arrear_no"));
		map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode","300000002" );
		map.put("message_code", "D113");
		map.put("field52", field52);//密码
		map.put("fieldChannel", "03");
		map.put("oper_type",responseDataMap.get("field44"));
		map.put("pay_year_mon", "000001");
		map.put("field55", field55);
		map.put("field53", "2600000000000000");
		map.put("current_arrearage", responseDataMap.get("current_arrearage"));
		map.put("user_name",responseDataMap.get("user_name") );
		map.put("prd_uid", "03");
		map.put("field35", track_2_data);//二磁
		map.put("field36",track_3_data );//三次
		map.put("address", responseDataMap.get("address"));
		map.put("area_no", "");
		map.put("field11", "");
		map.put("pay_no", responseDataMap.get("pay_no"));
		String payJson = JsonUtil.map2json(map);
		return payJson;
	
	}
	/**
	 * 电话充值
	 * @param feeType 
	 * @param cardNo
	 * @param payMoney
	 * @param password
	 * @param responseDataMap
	 * @param track_2_data
	 * @param track_3_data
	 * @param fieldMAB
	 * @param fieldMAC
	 * @param field41
	 * @param field42
	 * @return
	 */
	private String fitDHPayMeg(String field3,String field2, String field4, String field52, Map<String,String> responseDataMap,
			String track_2_data,String track_3_data,String fieldMAB,String fieldMAC,String field41,String field42,String field55){
		Map<String,String> map = new HashMap<String, String>();
		map.put("field60","22000074" ); 
		map.put("warm_user_type", "");
		map.put("fieldAddress", "");
		map.put("must_pay_sum_amt", responseDataMap.get("must_pay_sum_amt"));
		map.put("last_bal", responseDataMap.get("last_bal"));
		map.put("field26", "06");//pos上传报文
		map.put("field4",field4);//
		map.put("field3", "Ext_Fee");
		map.put("field25","00" );
		map.put("field42", field42);///商户编号
		map.put("field2", field2);//卡号
		map.put("field49",responseDataMap.get("field49"));//pos上传报文
		
		if(this.isIcCard(track_2_data)){
			map.put("field23", "001");
			map.put("field22", "051");
		}
		else{
			map.put("field23", "000");
			map.put("field22", "021");
		}
		map.put("pay_type", "2");
		map.put("field7", "20140505095455");//pos上传报文
		map.put("fieldMAC",fieldMAC );
		map.put("field41", field41);//终端编号
		map.put("arrear_no", responseDataMap.get("arrear_no"));
		map.put("fieldMAB", fieldMAB);
		map.put("fieldTrancode","300000002" );
		map.put("field52", field52);//密码
		map.put("fieldChannel", "03");
		if(field3.equals("Ext_QueryWT")){
			map.put("message_code", "D112");
			map.put("oper_type","");
			map.put("prd_uid", "02");
		}
		else if(field3.equals("Ext_QueryDX")){
			if(responseDataMap.get("pay_no") != null && responseDataMap.get("pay_no").length() == 11){
				map.put("oper_type","GSM");
			}
			else{
				map.put("oper_type","");
			}
			
			map.put("message_code", "D111");
			map.put("prd_uid", "01");
		}
		map.put("pay_year_mon", "000001");
		map.put("field55", field55);
		map.put("field53", "2600000000000000");
		map.put("current_arrearage", responseDataMap.get("current_arrearage"));
		map.put("user_name",responseDataMap.get("user_name") );
		map.put("field35", track_2_data);//二磁
		map.put("field36",track_3_data );//三次
		map.put("address", responseDataMap.get("address"));
		map.put("area_no", "");
		map.put("field11", "");
		map.put("pay_no", responseDataMap.get("pay_no"));
		String payJson = JsonUtil.map2json(map);
		return payJson;
	}
	/**
	 * 齐鲁银行输入卡号密码支付
	 * @param feeType
	 * @param cardNo
	 * @param payMoney
	 * @param password
	 * @param responseDataMap
	 * @return
	 */
	private String payFitting(String feeType,String cardNo,String payMoney,String password,Map<String,String> responseDataMap){
		Map<String,String> map = new HashMap<String, String>();
		
		
		map.put("field3", "Ext_Fee");
		map.put("oper_type",responseDataMap.get("field44"));
		if(feeType.equals("Ext_QueryRQ")){
			map.put("prd_uid",  "04");
			map.put("message_code","D114");
		}
		else if(feeType.equals("Ext_QueryYX")){
			map.put("prd_uid",  "23");
			map.put("message_code","D118");
		}
		else if(feeType.equals("Ext_QueryDF")){
			map.put("prd_uid",  "03");
			map.put("message_code","D113");
		}
		else if(feeType.equals("Ext_QueryDX") ){
			map.put("prd_uid",  "02");
			map.put("message_code","D112");
		}
		else if(feeType.equals("Ext_QueryWT") ){
			map.put("prd_uid",  "02");
			map.put("message_code","D112");
			if(responseDataMap.get("pay_no").length() == 11){
				map.put("oper_type","GSM");
			}
		}
		else if(feeType.equals("Ext_QueryQN")){
			map.put("prd_uid",  "09");
			map.put("message_code","5167");
			map.put("field3", responseDataMap.get("field37"));
			map.put("warm_user_type", responseDataMap.get("field38"));
		}
		else{
			//交易处理码
		}
		//交易码
		map.put("fieldTrancode","300000002");
		//缴费账号(银行卡号)
		map.put("field2", cardNo);
		//实缴金额
		map.put("field4", payMoney);
		//售卡机终端标志码
		map.put("field41", "12345678");
		//售卡方标志码（商户编号）
		map.put("field42", "000000000000000");
		//欠费查询编号
		map.put("arrear_no", responseDataMap.get("arrear_no"));
		//缴费方式
		map.put("pay_type", "2");
		//密码
		map.put("password", password);
		//缴费年份
		map.put("pay_year_mon", "000001");
		//用户编号
		map.put("pay_no", responseDataMap.get("pay_no"));
		//地址
		map.put("address", responseDataMap.get("address"));
		//用户名
		map.put("user_name", responseDataMap.get("user_name"));
		//当前欠费
		map.put("current_arrearage", responseDataMap.get("current_arrearage"));
		//应缴总额
		map.put("must_pay_sum_amt", responseDataMap.get("must_pay_sum_amt"));
		//上次余额
		map.put("last_bal", responseDataMap.get("last_bal"));
		//
		map.put("fieldChannel", "02");				
		String payJson = JsonUtil.map2json(map);
		return payJson;
	}
	
	public  boolean isIcCard(String track2data){
 		
 		if("".equals(track2data)||track2data==null){
 			return false;
 		}
 		if((!track2data.contains("="))&&(!track2data.contains("D"))){
 			return false;
 		}
 		String temp[]=null;
 		String key ="";
 		if(track2data.contains("=")){
 			temp=track2data.split("=");
 			key = temp[1].substring(4,5);
 		}else if(track2data.contains("D")){
 			temp=track2data.split("D");
 			key = temp[1].substring(4,5);
 		}else{
 			return false;
 		}
 		return  "2".equals(key)||"6".equals(key);
 	}

}


