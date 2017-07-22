package com.ryx.social.retail.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface IInfointerService {
	
	//获取系统公告信息列表
	public List<Map<String,Object>> getNoticeList(Map<String,String> map) throws Exception;
	
	//获取系统公告信息明细
	public Map<String,Object> getNoticeDetail(Map<String,String> map) throws Exception;
	
	//获取活动信息列表
	public List<Map<String,Object>> getActivityList(Map<String,String> map) throws Exception;
	
	//获取咨询投诉列表
	public List<Map<String,Object>> getInfointerList(Map<String,String> map) throws Exception;
	
	//获取咨询投诉信息明细
	public Map<String,Object> getInfointerDetail(Map<String,String> map) throws Exception;
	
	//提交咨询投诉
	public void submitInfointer(Map<String,String> map) throws Exception;
	
	//更新阅读消息状态
	public void updateNoticeDetail(List<String> list) throws Exception;

}