package com.ryx.pub.mobile.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface IMobileService {
	/**
	 * 根据提供的电话号码查询电话号码归属地及其可充值面额
	 * @author 徐虎彬
	 * @date 2014年3月27日
	 * @param mobilePhone 电话号码
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMobileInfo(String mobilePhone) throws Exception;
}
