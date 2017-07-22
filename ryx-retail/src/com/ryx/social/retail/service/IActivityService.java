package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface IActivityService {
	
	//调查问卷列表
	public List<Map<String,Object>> getSurveyList(Map<String,String> map)throws Exception;
	
	//调查问卷明细
	public List<Map<String,Object>> getSurveyDetail(Map<String,String> map)throws Exception;
		
	//提交调查问卷
	public  void submitSurvey(Map<String,String> map)throws Exception;
	
	//消费者抽奖
	public Map<String,Object> getRaffleDetail(Map<String,String> map)throws Exception;

}