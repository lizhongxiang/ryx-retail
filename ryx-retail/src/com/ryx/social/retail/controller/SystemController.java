package com.ryx.social.retail.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IActivityService;
import com.ryx.social.retail.util.MultiCitySupport;
import com.ryx.social.retail.util.ParamUtil;

/**
 * @系统相关
 * @author 
 *
 */
@Controller
public class SystemController {
	private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
	
	/**
	 * 
	 */
	@RequestMapping(value="/system/comm/getconfig")
	public void getCommConfig(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> param = ParamUtil.getParamMap(request);
		String comId = MapUtil.getString(param, "com_id", "");
		
		StringBuffer sb = new StringBuffer();
		sb.append(request.getScheme()).append("://");
		sb.append(request.getServerName());
		sb.append(":").append(request.getServerPort());
		sb.append("/resource/download/");
		//sb.append("/public/retail/comm/CommClient" + comId + ".swf?v=1.2.10");
		// 20141123 客显屏程序个性化
		sb.append(MultiCitySupport.getCommClientPath(comId));
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("swfUrl", sb.toString());
		
		ResponseUtil.write(request, response, map);
	}

	@RequestMapping(value="/system/download/geturl")
	public void getDownloadUrl(HttpServletRequest request, HttpServletResponse response) {
		String reqType = request.getParameter("reqType");
		String str = request.getHeader("User-Agent");

		try {
			//ResponseUtil.write(request, response, "User-Agent:" + str);
			//if(str.contains("MicroMessenger")) {
				// 微信扫码，转向到下载地址
				//response.sendRedirect("http://www.ruishangtong.com:8889/resource/download/package/download.html");
				if("lxd".equals(reqType)) {
					response.sendRedirect("http://www.ruishangtong.com:8889/resource/download/package/lxd.html");
					return;
				} else if("gdb".equals(reqType)) {
					response.sendRedirect("http://www.ruishangtong.com:8889/resource/download/package/gdb.html");
					return;
				} else if("sjkd".equals(reqType)) {
					response.sendRedirect("http://www.ruishangtong.com:8889/resource/download/package/sjkd.html");
					return;
				}
				return;
			/*} else {
				if("lxd".equals(reqType)) {
					response.sendRedirect("http://www.ruishangtong.com:8889/resource/download/package/louxiadian.apk");
					return;
				} else if("gdb".equals(reqType)) {
					response.sendRedirect("http://www.ruishangtong.com:8889/resource/download/package/guandianbao.apk");
					return;
				} else if("sjkd".equals(reqType)) {
					response.sendRedirect("http://www.ruishangtong.com:8889/resource/download/package/shoujikandian.apk");
					return;
				}
			}*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("SystemControlles getDownloadUrl :",e);
		}
		
		ResponseUtil.write(request, response, Constants.CODE_FAIL, "无效的请求参数：reqType=" + reqType + ", userAgent=" + str, null);
	}
}
