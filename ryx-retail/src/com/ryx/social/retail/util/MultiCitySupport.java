package com.ryx.social.retail.util;

import java.util.HashMap;
import java.util.Map;

public class MultiCitySupport {

	private static Map<String, String> indexMultiCity = new HashMap<String, String>();
	private static Map<String, String> commClientMultiCity = new HashMap<String, String>();
	
	static {
		// 主页个性化地市
		indexMultiCity.put("10370701", "");		// 潍坊
		
		// 客显屏个性化地市
		commClientMultiCity.put("10370701", "?v=1.5.4");		// 潍坊
	}
	
	/**
	 * 主页个性化路径
	 * @param comId
	 * @param path
	 * @return
	 */
	public static String getIndexPath(String comId) {
		if(indexMultiCity.containsKey(comId)) {
			return "retail/multi/" + comId + "/index"; 
		}
		
		return "retail/index";
	}
	
	/**
	 * 获取客显屏个性化地址
	 * @param comId
	 * @param path
	 * @return
	 */
	public static String getCommClientPath(String comId) {
		if(commClientMultiCity.containsKey(comId)) {
			return "comm/multi/" + comId + "/CommClient.swf" + commClientMultiCity.get(comId); 
		}
		
		return "comm/CommClient.swf?v=1.5.4";
	}
}
