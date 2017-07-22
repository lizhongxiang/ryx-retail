package com.ryx.social.retail.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IDeprecatedDao;
import com.ryx.social.retail.service.IDeprecatedService;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class DeprecatedServiceImpl implements IDeprecatedService {
	
	private Logger LOG = LoggerFactory.getLogger(DeprecatedServiceImpl.class);
	@Resource
	private IDeprecatedDao deprecatedDao;
	
	//查询零售户商圈排名
	@Override
	public Map<String, Object> searchGMerchSqGatherRranking(Map<String, Object> paramMap) throws Exception {
		LOG.debug("DeprecatedServiceImpl searchGMerchSqGatherRranking paramMap:"+paramMap);
		String month = MapUtil.getString(paramMap, "month");
		if(StringUtil.isBlank(month)){
			paramMap.put("month", DateUtil.getCurrentTimeMillisAsString("yyyyMM"));
		}
		List<Map<String, Object>> merchSqGather = this.searchGMerchSqGather(paramMap);
		if(merchSqGather.size() <= 0){
			return new HashMap<String, Object>();
		}
		Map<String, Object> merchGatherMap = merchSqGather.get(0);
		merchGatherMap.put("merch_name", MapUtil.getString(paramMap, "merch_name"));
		paramMap.put("type", MapUtil.getString(merchGatherMap, "type"));
		
		List<Map<String, Object>> data =  deprecatedDao.selectGCustSQGatherRranking(paramMap);
		merchGatherMap.put("list", data);
		
		Map<String,String> gatherTextParam=new HashMap<String,String>();
		gatherTextParam.put("cust_id", MapUtil.getString(paramMap, "merch_id"));
		
//		http://192.168.0.14:9081/tobaccoserver/transinfo/getCustGuideInfoList?cust_id=1037010407467
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "transinfo/getCustGuideInfoList";
		String json = HttpUtil.post(cgtItemUrl, gatherTextParam);
		
		Map<String, Object> gatherTextMap = new HashMap<String, Object>();
		if(!StringUtils.isEmpty(json)){
			gatherTextMap =JsonUtil.json2Map(json);
		}
		List<Map<String, Object>> gatherTextResult = null;
		if("0000".equals(MapUtil.getString(gatherTextMap, "code", "1000"))){
			if(MapUtil.getString(gatherTextMap, "result", null)!=null){
				gatherTextResult = MapUtil.get(gatherTextMap, "result", new ArrayList<Map<String, Object>>());//(List<Map<String, Object>>)gatherTextMap.get();
				merchGatherMap.put("guide_information", gatherTextResult);
			}
		}
//		data.add(merchGatherMap);
		return merchGatherMap;
	}
	
	//查询零售户商圈
	@Override
	public List<Map<String, Object>> searchGMerchSqGather(Map<String, Object> paramMap) throws Exception {
		LOG.debug("DeprecatedServiceImpl searchGMerchSqGather paramMap:"+paramMap);
		return deprecatedDao.selectGMerchSqGather(paramMap);
	}
	
}
