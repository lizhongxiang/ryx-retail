package com.ryx.social.retail.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ryx.framework.util.HttpUtil;
import com.ryx.social.retail.service.ICheckBillService;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class CheckBillServiceImpl implements ICheckBillService{
	private  String phoneFee = "/phonebill/listbypage.do";
	private  String consume = "/consume/listbypage.do";
	private  String gjf = "/pubcharge/listbypage.do";
	@Override
	public String checkBillPhoneFee(Map<String, String> map) {
		String url = RetailConfig.getCheckBillServer()+phoneFee;
		String callbackContent = HttpUtil.post( url, map);
		return callbackContent;
	}
	@Override
	public String checkBillConsume(Map<String, String> map){
		String url = RetailConfig.getCheckBillServer()+consume;
		String callbackContent = HttpUtil.post(url, map);
		return callbackContent;
	
	}
	@Override
	public String checkBillGJF(Map<String, String> map) {
		String url = RetailConfig.getCheckBillServer()+gjf;
		String callbackContent = HttpUtil.post(url, map);
		
		return callbackContent.replace(" ","");
	}


}
