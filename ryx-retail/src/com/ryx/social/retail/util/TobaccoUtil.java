package com.ryx.social.retail.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;

public class TobaccoUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(TobaccoUtil.class);
	
	public static Map<String, Object>  getJsonFromServer(Map<String,String> paraMap, String url) throws Exception{
		String remoteUrl = RetailConfig.getTobaccoServer() + url;

		logger.debug("请求Tobacco获取数据:url=" + remoteUrl + ", params=" + JsonUtil.map2json(paraMap));
		
		//从服务器上获取数据
		String json = HttpUtil.post(remoteUrl, paraMap);
		
		logger.debug("获取到Tobacco的数据：data=" + json);
		
		//Map<String, Object> jsonMap = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
		Map<String, Object> jsonMap = JsonUtil.json2Map(json);
		
		return jsonMap;
	}
	
	public static Map<String, Object>  getJsonFromTobasrv(Map<String,String> paraMap, String url) throws Exception{
		//String remoteUrl = RetailConfig.getTobasrvURL() + url;
		String remoteUrl = RetailConfig.getTobaccoServer() + url;
		logger.debug("请求Tobacco获取数据:url=" + remoteUrl + ", params=" + JsonUtil.map2json(paraMap));
		Map<String,String> map=new HashMap<String,String>();
		map.put("params", JsonUtil.map2json(paraMap));
		//从服务器上获取数据
		String json = HttpUtil.post(remoteUrl, map);
		logger.debug("获取到Tobacco的数据：data=" + json);
		
		Map<String, Object> jsonMap = JsonUtil.json2Map(json);
		
		return jsonMap;
	}
	
}
