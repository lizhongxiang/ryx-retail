package com.ryx.pub.mobile.dao.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.pub.mobile.dao.MobileDao;
import com.ryx.social.retail.dao.impl.WhseDaoImpl;

@Repository
public class MobileDaoImpl extends BaseDaoImpl implements MobileDao {
	
	private static final Logger logger = LoggerFactory.getLogger(WhseDaoImpl.class);
	
	@Override
	public List<Map<String, Object>> selectMobileInfoByMobileArea(
			String mobileNumber) throws Exception {
		String sql="SELECT MOBILENUMBER,MOBILEAREA,MOBILETYPE,AREATYPE,POSTTYPE FROM MOBILE_INFO WHERE MOBILENUMBER=?";
		logger.debug("MobileTopUpDaoImpl selectMobileInfoByMobileArea sql: " + sql);
		List<Map<String, Object>> mobileInfo=this.selectBySqlQuery(sql,new Object[]{mobileNumber});
		return mobileInfo;
	}

	@Override
	public List<Map<String, Object>> selectMobileMoneyByProvincesAndMobileType(
			Map<String, Object> mobileInfo) throws Exception {
		String sql="SELECT PROVINCES,MOBILETYPE,MONEY  FROM MOBILE_MONEY WHERE PROVINCES=? AND MOBILETYPE=? ORDER BY MONEY";
		logger.debug("MobileTopUpDaoImpl SelectMobileMoneyByProvincesAndMobileType sql: " + sql);
		List<Map<String, Object>> mobileTopUp=this.selectBySqlQuery(sql,new Object[]{mobileInfo.get("provinces"),mobileInfo.get("mobiletype")});
		return mobileTopUp;
	}

}
