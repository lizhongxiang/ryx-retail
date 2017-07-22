package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface IActivityNewService {
	
	//调查活动列表
	public List<Map<String,Object>> getActList(Map<String,String> map)throws Exception;
	
	//调查活动明细
	public Map<String,Object> getActDetail(Map<String,String> map)throws Exception;
		
	//提交活动问卷
	public Map<String, Object> submitAct(Map<String,String> map)throws Exception;
	
	/**
	 * description  根据Map中的活动ID和零售商ID 插入爱心参与送伞活动零售商
	 * */
	public Map<String,Object> insertUmbrellaUser(Map<String,String> map)throws Exception;
	
	
	/**
	 * description  根据Map中封装的活动ID和零售商ID 查询爱心送伞活动详情
	 * 
	 * */
	public List<Map<String,Object>> selectUmbrellaUserList(Map<String,String> map)throws Exception;
	
}