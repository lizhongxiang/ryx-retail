package com.ryx.social.retail.util;

import java.util.HashMap;

import com.ryx.framework.util.MapUtil;

public class WeatherCityMap extends HashMap<String, String> {
	
	private static WeatherCityMap me;
	
	private WeatherCityMap() {
		this.put("10370301", "101120301");
		this.put("10370401", "101121401");
		this.put("10370601", "101120501");
		this.put("10370701", "101120601");
		this.put("10370801", "101120701");
		this.put("10370901", "101120801");
		this.put("10371001", "101121301");
		this.put("10371101", "101121501");
		this.put("10371201", "101121601");
		this.put("10371301", "101120901");
		this.put("10371401", "101120401");
		this.put("10371501", "101121701");
		this.put("10371601", "101121101");
		this.put("10371701", "101121001");
		this.put("10370201", "101120201");
		this.put("10370501", "101121201");
		this.put("10370101", "101120101");
	}
	
	public static HashMap<String, String> getWeatherCityMap() {
		if(me==null) me = new WeatherCityMap();
		return me;
	}
	
	public static String getWeatherCityId(String comId, String defaultId) {
		if(me==null) me = new WeatherCityMap();
		return MapUtil.getString(me, comId, defaultId);
	}
	
	public static String getWeatherCityId(String comId) {
		return getWeatherCityId(comId, "");
	}
	
}
