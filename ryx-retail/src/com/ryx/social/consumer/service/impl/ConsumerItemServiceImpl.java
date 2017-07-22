package com.ryx.social.consumer.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.ryx.framework.util.HttpUtil;
import com.ryx.social.consumer.dao.IConsumerItemDao;
import com.ryx.social.consumer.service.IConsumerItemService;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class ConsumerItemServiceImpl implements IConsumerItemService {
	private static final Logger logger = LoggerFactory
			.getLogger(ConsumerItemServiceImpl.class);
	@Resource
	private IConsumerItemDao consumerItemDao;

	private Gson gson = new Gson();

	@Override
	public List<Map<String, Object>> searchItemInfoByItemId(
			Map<String, Object> itemmMap) throws Exception {
		// TODO Auto-generated method stub
		logger.debug("ConsumerItemServiceImpl searchItemInfoBySupplienId itemMap:"
				+ itemmMap);
		return consumerItemDao.searchItemInfoBySupplienId(itemmMap);
	}

	public List<Map<String, Object>> searchItemByName(
			Map<String, Object> itemMap) throws Exception {
		return consumerItemDao.searchItemByName(itemMap);
	}

	public List<Map<String, Object>> searchItemAndMerchByName(
			Map<String, Object> itemMap) throws Exception {
		return consumerItemDao.searchItemAndMerchByName(itemMap);
	}

	public List<Map<String, Object>> searchItemOfInSaleByMerchId(
			Map<String, Object> map) throws Exception {
		return consumerItemDao.searchItemOfInSaleByMerchId(map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List findGoodsWithStores(String latitude, String longitude,
			String key, String distance, String deliveryType) throws Exception {
		List storesList = Collections.EMPTY_LIST;
		List retList = new LinkedList<Map>();

		Map<String, String> storeParams = new HashMap<String, String>();
		storeParams.put("latitude", latitude);
		storeParams.put("longitude", longitude);
		storeParams.put("distance", distance);
		storeParams.put("deliveryType", deliveryType);
		String jsonStr = HttpUtil.post(
				//DataContant.SearchEngineConsumerWildcardSearchURL,
				RetailConfig.getLuceneServer() + "tobacco/retail/retailStore/wildcardSearch/json",
				storeParams);
		try {
			storesList = gson.fromJson(jsonStr, LinkedList.class);
		} catch (Exception e) {
			//logger.warn("com.ryx.social.consumer.service.impl.ConsumerItemServiceImpl.findGoodsWithStores, 附近店铺返回数据格式出错");
			logger.debug("findGoodsWithStores 附近店铺返回数据格式出错:" + jsonStr, e);
			throw e;
		}

		for (int counter = 0; counter < storesList.size(); counter++) {
			Map storeMap = (Map) storesList.get(counter);
			String merchId = (String) storeMap.get("merch_id");
			// 取店铺内匹配商品
			Map<String, String> goodsParams = new HashMap<String, String>();
			goodsParams.put("merchId", merchId);
			goodsParams.put("key", key);
			goodsParams.put("currPage", "-1");
			// 得到店铺内的商品
			String storeGoodsjsonStr = HttpUtil.post(
					//DataContant.SearchEngineRetailWildcardSearchURL,
					RetailConfig.getLuceneServer() + "tobacco/retail/retailGoods/wildcardSearch/json",
					goodsParams);
			try {
				Map storeGoodsMap = gson.fromJson(storeGoodsjsonStr, Map.class);
				List storeGoodsList = (List) ((Map) storeGoodsMap.get("result"))
						.get("item_list");
				// 当有匹配的商品时，返回数据中加入该店与该店匹配的商品
				if (storeGoodsList.size() > 0) {
					storeMap.put("goodsList", storeGoodsList);
					retList.add(storeMap);
				}
			} catch (Exception e) {
				//logger.warn("com.ryx.social.consumer.service.impl.ConsumerItemServiceImpl.findGoodsWithStores, 附近店铺商品返回数据格式出错");
				logger.debug("findGoodsWithStores 附近店铺商品返回数据格式出错:" + storeGoodsjsonStr, e);
				throw e;
			}
		}

		return retList;
	}

	@Override
	public List popularKeyWords(String size) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("size", size);
		String jsonStr = HttpUtil.post(RetailConfig.getLuceneServer() + "tobacco/retail/retailGoods/popularKeyWords/json", params);
		return gson.fromJson(jsonStr, ArrayList.class);
	}

}
