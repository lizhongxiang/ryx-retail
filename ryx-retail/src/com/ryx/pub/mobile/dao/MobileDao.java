package com.ryx.pub.mobile.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface MobileDao {
	/**
	 * 根据电话地区号查询区号信息
	 * @author 徐虎彬
	 * @date 2014年3月27日
	 * @param mobileAray
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectMobileInfoByMobileArea(String mobileArea)throws Exception;
	/**
	 * 根据省份及类型查询可充值花费金额
	 * @author 徐虎彬
	 * @date 2014年3月27日
	 * @param mobileInfo PROVINCES:省份   MOBILETYPE:类型
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectMobileMoneyByProvincesAndMobileType(Map<String,Object> mobileInfo)throws Exception;
}
