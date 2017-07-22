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
import com.ryx.social.retail.service.IActivityService;
import com.ryx.social.retail.util.RetailConfig;
import com.ryx.social.retail.util.TobaccoUtil;

@Service("activityService")
public class ActivityServiceImpl implements IActivityService {
	
	private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
	
	//调查问卷列表
	public List<Map<String,Object>> getSurveyList(Map<String,String> map) throws Exception{
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"activity/getSurveyList");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		List<Map<String, Object>> surveyList = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			surveyList = (List<Map<String, Object>>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		return surveyList;
	}
	
	//调查问卷明细
	public List<Map<String,Object>> getSurveyDetail(Map<String,String> map)throws Exception{
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"activity/getSurveyDetail");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		List<Map<String, Object>> surveyList = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			surveyList = (List<Map<String, Object>>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		return surveyList;		
	}
	
	//提交调查问卷
	public void submitSurvey(Map<String,String> map)throws Exception{
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"activity/submitSurvey");
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
	}
	
	
	//消费者抽奖
	public Map<String,Object> getRaffleDetail(Map<String,String> map)throws Exception{
		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"activity/getRaffleDetail");
		
		//解析获取的数据
		Map<String, Object> dataMap = null;
		String code = (String)jsonMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			dataMap = (Map<String, Object>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}	
		return dataMap;
	}
}
