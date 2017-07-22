package com.ryx.social.retail.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

public class HttpServletRequestUtil {
	
	/**
	 * 根据请求获取ip地址
	 * 优先获取header中被代理的ip 如果获取不到再取remoteAddr
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request)  {  
		String ip = request.getHeader("X-Forwarded-For");
		// 如果ip为null或者空串 或 ip为"unknown" 则顺序查找下一个
		if(!StringUtils.hasText(ip) || StringUtils.startsWithIgnoreCase(ip, "unknown")) {
			ip = request.getHeader("X-Real-IP");
			if(!StringUtils.hasText(ip) || StringUtils.startsWithIgnoreCase(ip, "unknown")) {
				ip = request.getHeader("REMOTE-HOST");
				if(!StringUtils.hasText(ip) || StringUtils.startsWithIgnoreCase(ip, "unknown")) {
					ip = request.getRemoteAddr();
				}
			}
		}
		return ip;
	 } 
	
}
