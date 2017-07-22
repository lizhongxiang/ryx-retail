package com.ryx.social.retail.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.login.tool.IdentityUtil;

public class ParamUtil {
	
	/**
	 * <p>封装用户请求的参数和保存在session中的用户登录信息，包括密码密文等，还包括session中的token信息。</p>
	 * <p>因为是保存到Map中，如果键重复会发生覆盖的情况，具体覆盖规则如下：</p>
	 * <p>请求参数覆盖登录信息，token覆盖请求参数。基于以上规则，不要约定token关键字作为参数的键。</p>
	 * <p>如果商户编号（merch_id）有可能被覆盖，可以使用登录信息中的关联编号（ref_id）作为商户编号。</p>
	 * @param request
	 * @return 包含用户登录信息、请求参数列表、和session中的token
	 */
	public static Map<String, Object> getParamMap(HttpServletRequest request) {
		// 返回的结果
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// 用户登录信息
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		// 请求参数
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		// 将登录信息添加到返回的结果中
		if(userMap!=null && !userMap.isEmpty()) {
			paramMap.putAll(userMap);
			paramMap.put("merch_id", paramMap.get("ref_id"));
		}
		// 如果请求的参数中有params关键字 则将其中的参数添加到返回的结果中
		if(requestMap.get("params")!=null) {
			paramMap.putAll((Map<String, Object>) requestMap.get("params"));
		}
		// 否则将所有的请求参数添加到返回的结果中
		else {
			paramMap.putAll(requestMap);
		}
		// 把session中的token添加到返回的结果中
		Object token = request.getSession(true).getAttribute("token");
		paramMap.put("token", token);
		return paramMap;
	}
	
	public static boolean isTobacco(Map inputMap) {
		return MapUtil.getString(inputMap, "item_kind_id").startsWith("01");
	}
	
	public static boolean isTobacco(Object itemKindId) {
		return itemKindId==null ? false : itemKindId.toString().startsWith("01");
	}
	
	public static int compareTo(String date1, String date2) {
		return date1==null ? (date2==null ? 0 : -1) : (int) (Integer.parseInt(date1)-Integer.parseInt(date2));
	}
	
}
