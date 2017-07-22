package com.ryx.social.consumer.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.social.consumer.service.IConsumerInteractionService;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class ConsumerInteractionServiceImpl implements IConsumerInteractionService {
	
	private Logger logger = LoggerFactory.getLogger(ConsumerInteractionServiceImpl.class);
	
	// 从tobaccoserver上获取系统信息
	@Override
	public List<Map<String, Object>> searchNotice(Map<String, Object> noticeParam) throws Exception {
		logger.debug("ConsumerInfointerServiceImpl searchNotice noticeParam: " + noticeParam);
		String noticeUrl = RetailConfig.getTobaccoServer() + "infointer/getNoticeList";
		Map<String, String> httpParam = new HashMap<String, String>(); // 没有参数
		httpParam.put("userId", (String)noticeParam.get("userId"));
		httpParam.put("comId", (String)noticeParam.get("comId"));
		httpParam.put("date1", (String)noticeParam.get("date1"));
		String noticeJson = HttpUtil.post(noticeUrl, httpParam);
		Map<String, Object> noticeResultMap = JsonUtil.json2Map(noticeJson);
		if(noticeResultMap.containsKey("result")) {
			return (List<Map<String, Object>>) noticeResultMap.get("result");
		} else {
			throw new RuntimeException((String) noticeResultMap.get("msg"));
		}
	}
	
}
