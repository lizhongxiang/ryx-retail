package com.ryx.social.retail.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.social.retail.service.IActivityNewService;
import com.ryx.social.retail.service.IActivityService;
import com.ryx.social.retail.util.RetailConfig;
import com.ryx.social.retail.util.TobaccoUtil;

@Service("activityNewService")
public class ActivityNewServiceImpl implements IActivityNewService {
	
	private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
	
	//获取活动列表
	public List<Map<String,Object>> getActList(Map<String,String> map) throws Exception{
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"activity/getActList");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		List<Map<String, Object>> surveyList = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			surveyList = (List<Map<String, Object>>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		logger.debug("获取活动列表：" + JsonUtil.map2json(jsonMap));
		return surveyList;
	}
	
	//获取活动明细
	public Map<String,Object> getActDetail(Map<String,String> map)throws Exception{
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"activity/getActDetail");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		Map<String, Object> actDetail = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			actDetail = (Map<String, Object>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		return actDetail;
	}
	
	//提交活动信息
	public Map<String, Object> submitAct(Map<String,String> map)throws Exception{
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"activity/submitAct");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			return (Map<String,Object>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
	}
	
	/**
	 * description 新增参与爱心送伞服务的零售商
	 * */
	@Override
	public Map<String, Object> insertUmbrellaUser(Map<String, String> paramMap)
			throws Exception {
		Map<String,Object> jsonMap = (Map<String,Object>)TobaccoUtil.getJsonFromServer(paramMap, "loveForUmbrella/insertUmbrellaUser");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		Map<String, Object> returnMsg = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			returnMsg = (Map<String, Object>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		logger.debug("============The ActivityNewServiceImpl insertUmbrellaUser Method  infomation:" + returnMsg);
		return returnMsg;
	}
	
	/**
	 * description 查询参与爱心送伞服务的零售商
	 * */
	@Override
	public List<Map<String, Object>> selectUmbrellaUserList(Map<String, String> paramMap)
			throws Exception {
		//根据Map封装参数、后缀url字符串返回参与送伞活动服务信息
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(paramMap, "loveForUmbrella/getUmbrellaUser");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		List<Map<String, Object>> umbrellaUserList = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			umbrellaUserList = (List<Map<String, Object>>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		logger.debug("============The ActivityNewServiceImpl selectUmbrellaUserList Method  infomation:" + JsonUtil.map2json(jsonMap));
		return umbrellaUserList;
	}
	
	
}
