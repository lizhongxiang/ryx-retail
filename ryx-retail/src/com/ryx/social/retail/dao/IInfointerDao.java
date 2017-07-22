package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IInfointerDao {

	//获取系统信息列表
	public List<Map<String, Object>> getNoticeList(Map<String,String> map) throws Exception;
	
	//更新阅读消息状态
	public void updateNoticeDetail(List<String> list) throws Exception;
}
