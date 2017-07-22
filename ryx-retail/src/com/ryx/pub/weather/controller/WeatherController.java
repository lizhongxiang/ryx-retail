package com.ryx.pub.weather.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.RetailConfig;

/**
 * Handles requests for the application home page.
 */
@Controller 
public class WeatherController {
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/pub/weather/getWeather")
	public void getWeather(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		String comId = MapUtil.getString(paramMap, "com_id");
		String remoteWeatherUrl = RetailConfig.getMasterServer()+"public/weather/getWeather";
		
		Map weather = null;
		if(!StringUtil.isBlank(comId)) {
			paramMap.put("com_id", comId);
			String json = HttpUtil.post(remoteWeatherUrl, paramMap);
			Map dataMap = JsonUtil.json2Map(json);
			weather = (Map) dataMap.get("result");
			if(weather != null) {
				weather.put("img_single", MapUtil.getString(weather, "img1"));
				weather.put("now", DateUtil.getCurrentTime());
			}
		}
		ResponseUtil.write(request, response, weather);
	}
}
