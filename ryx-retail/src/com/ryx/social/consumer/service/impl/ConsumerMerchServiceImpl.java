package com.ryx.social.consumer.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.social.consumer.contant.DataContant;
import com.ryx.social.consumer.dao.IConsumerMerchDao;
import com.ryx.social.consumer.service.IConsumerMerchService;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class ConsumerMerchServiceImpl implements IConsumerMerchService {

	private static final Logger logger = LoggerFactory
			.getLogger(ConsumerItemServiceImpl.class);
	@Resource
	private IConsumerMerchDao consumerMerchDao;

	private Gson gson = new Gson();

	public List<Map<String, Object>> searchMerchByName(
			Map<String, Object> merchMap) throws Exception {
		return consumerMerchDao.searchMerchByName(merchMap);
	}

	public List<Map<String, Object>> searchMerchById(
			Map<String, Object> merchMap) throws Exception {
		return consumerMerchDao.searchMerchById(merchMap);
	}

	@Override
	public List wildcardFind(String latitude, String longitude, String key,
			String distance, String deliveryType) throws Exception {

		List retList = Collections.EMPTY_LIST;
		Map<String, String> params = new HashMap<String, String>();
		params.put("latitude", latitude);
		params.put("longitude", longitude);
		params.put("key", key);
		params.put("distance", distance);
		params.put("deliveryType", deliveryType);

		String jsonStr = HttpUtil.post(
		// DataContant.SearchEngineConsumerWildcardSearchURL,
				RetailConfig.getLuceneServer()
						+ "tobacco/retail/retailStore/wildcardSearch/json", params);

		try {
			//retList = gson.fromJson(jsonStr, LinkedList.class);
			retList = JsonUtil.json2List(jsonStr);
		} catch (Exception e) {
			// logger.warn("com.ryx.social.consumer.service.impl.ConsumerMerchService.wildcardFind 附近店铺返回数据格式出错");
			logger.debug("附近店铺返回数据格式出错:" + jsonStr + ",params=" + JsonUtil.map2json(params), e);
		}
		return retList;
	}

	@Override
	public List popularKeyWords(String size) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("size", size);
		String jsonStr = HttpUtil.post(RetailConfig.getLuceneServer()
				+ "tobacco/retail/retailStore/popularKeyWords/json", params);
		return gson.fromJson(jsonStr, ArrayList.class);
	}
}
