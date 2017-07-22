package com.ryx.social.consumer.service;

import java.util.List;
import java.util.Map;

public interface IConsumerInteractionService {
	// 从tobaccoserver上获取系统信息
	public List<Map<String, Object>> searchNotice(Map<String, Object> infointerParam) throws Exception;
}
