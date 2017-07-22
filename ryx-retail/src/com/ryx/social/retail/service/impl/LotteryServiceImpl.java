package com.ryx.social.retail.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.ILotteryDao;
import com.ryx.social.retail.service.ILotteryService;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class LotteryServiceImpl implements ILotteryService {

	@Resource
	private ILotteryDao lotteryDao;

	private static final Logger LOG = LoggerFactory.getLogger(LotteryServiceImpl.class);
	
	public static Map<String,Object> activityParameter=new HashMap<String,Object>();
	
	@Override
	public Map<String, Object> whetherLottery(Map<String, String> lotteryData)
			throws Exception {
		String whetherLotteryUrl = RetailConfig.getTobaccoServer() + "lottery/whetherLottery";

		//从卷烟系统服务上获取数据
		
		String json =null;
		Map<String, Object>  LotteryDataMap=new HashMap<String,Object>();
		json = HttpUtil.post(whetherLotteryUrl, lotteryData);
		Map<String, Object> dataMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			dataMap = JsonUtil.json2Map(json);
		}
		if("0000".equals(MapUtil.getString(dataMap, "code", "1000"))){
			if(MapUtil.getString(dataMap, "result", null)!=null){
				LotteryDataMap=(Map<String, Object>)dataMap.get("result");
			}
		}else{
			LOG.debug("whetherLottery error:"+json);
		}
		return LotteryDataMap;
	}

	@Override
	public List<Map<String, Object>> getActivityParameter(
			Map<String, String> lotteryData) throws Exception {
		
		if(LotteryServiceImpl.activityParameter!=null){
			if(LotteryServiceImpl.activityParameter.containsKey(lotteryData.get("cust_id"))){
				Map<String,Object> thisDataMap=(Map<String,Object>)LotteryServiceImpl.activityParameter.get(lotteryData.get("cust_id"));
				Date d=(Date)thisDataMap.get("time");
				long thisTime=new Date().getTime()-d.getTime();
				long date=thisTime/(1000*60*60*2);
				if(date>1){
					List<Map<String, Object>> dataActivityList=getActivityParameterList(lotteryData);
				    Map<String,Object> merchActivity=new HashMap<String,Object>();
				    merchActivity.put("list", dataActivityList);
				    merchActivity.put("time", new Date());
				    LotteryServiceImpl.activityParameter.put(lotteryData.get("custId"), merchActivity);
					return dataActivityList;
				}else{
					return new ArrayList();
				}
			}else{
				List<Map<String, Object>> dataActivityList=getActivityParameterList(lotteryData);
			    Map<String,Object> merchActivity=new HashMap<String,Object>();
			    merchActivity.put("list", dataActivityList);
			    merchActivity.put("time", new Date());
			    LotteryServiceImpl.activityParameter.put(lotteryData.get("custId"), merchActivity);
				return dataActivityList;
			}
		}
		
			List<Map<String, Object>> dataActivityList=getActivityParameterList(lotteryData);
		    Map<String,Object> merchActivity=new HashMap<String,Object>();
		    merchActivity.put("list", dataActivityList);
		    merchActivity.put("time", new Date());
		    LotteryServiceImpl.activityParameter.put(lotteryData.get("custId"), merchActivity);
			return dataActivityList;
		
		
	}
	
	public List<Map<String, Object>> getActivityParameterList(Map<String, String> lotteryData)throws Exception{
		String getActivityParameterUrl = RetailConfig.getTobaccoServer() + "lottery/getActivityParameter";

		//从卷烟系统服务上获取数据
		String json =null;
		List<Map<String, Object>>  LotteryDataMap=new ArrayList<Map<String,Object>>();
		json = HttpUtil.post(getActivityParameterUrl, lotteryData);
		Map<String, Object> dataMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			dataMap =JsonUtil.json2Map(json);
		}
		if("0000".equals(MapUtil.getString(dataMap, "code", "1000"))){
			if(MapUtil.getString(dataMap, "result", null)!=null){
				LotteryDataMap=(List<Map<String, Object>>)dataMap.get("result");
			}
		}else{
			LOG.debug("getActivityParameterList error:"+json);
		}
		return LotteryDataMap;
	}

	@Override
	public Map<String, Object> getCustomerRaffleDetails(
			Map<String, String> lotteryData) throws Exception {
		String getCustomerRaffleDetailsUrl = RetailConfig.getTobaccoServer() + "lottery/getCustomerRaffleDetails";

		//从卷烟系统服务上获取数据
		String json =null;
		Map<String, Object>  LotteryDataMap=null;
		json = HttpUtil.post(getCustomerRaffleDetailsUrl, lotteryData);
		Map<String, Object> dataMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			dataMap =JsonUtil.json2Map(json);
		}
		if("0000".equals(MapUtil.getString(dataMap, "code", "1000"))){
			if(MapUtil.getString(dataMap, "result", null)!=null){
				LotteryDataMap=(Map<String, Object>)dataMap.get("result");
			}
		}else{
			LOG.debug("getCustomerRaffleDetails error:"+json);
		}
		return LotteryDataMap;
	}

	@Override
	public Map<String, Object> getSubmitDetail( Map<String, String> lotteryData) throws Exception {
		String getSubmitDetailUrl = RetailConfig.getTobaccoServer() + "lottery/participation";
		//从卷烟系统服务上获取数据
		String json =null;
		List<Map<String, Object>>  LotteryDataMapList=null;
		Map<String, Object> LotteryDataMap=null;
		
		json = HttpUtil.post(getSubmitDetailUrl, lotteryData);
		Map<String, Object> dataMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			dataMap =JsonUtil.json2Map(json);
		}
		if("0000".equals(MapUtil.getString(dataMap, "code", "1000"))){
			if(MapUtil.getString(dataMap, "result", null)!=null){
				LotteryDataMapList=(List<Map<String, Object>>) dataMap.get("result");
				if(LotteryDataMapList.size() > 0) {
					LotteryDataMap = LotteryDataMapList.get(0);
				}
			}
		}else{
			LOG.debug("getSubmitDetail error:"+json);
		}
		
		return LotteryDataMap;
	}
	
	@Override
	public Map<String, Object> submitLottery(Map<String, String> lotteryData)
			throws Exception {

		String submitLotteryUrl = RetailConfig.getTobaccoServer() + "lottery/submitLottery";

		//从卷烟系统服务上获取数据
		
		String json =null;
		Map<String, Object>  LotteryDataMap=null;
		json = HttpUtil.post(submitLotteryUrl, lotteryData);
		Map<String, Object> dataMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			dataMap =JsonUtil.json2Map(json);
		}
		if("0000".equals(MapUtil.getString(dataMap, "code", "1000"))){
			if(MapUtil.getString(dataMap, "result", null)!=null){
				LotteryDataMap=(Map<String, Object>)dataMap.get("result");
			}
		}else{
			LOG.debug("submitLottery error:"+json);
		}
		return LotteryDataMap;
	}

	@Override
	public List<Map<String, Object>> getLotteryList(Map<String, Object> datMap)
			throws Exception {
		return lotteryDao.selectLottery(datMap);
	}

}
