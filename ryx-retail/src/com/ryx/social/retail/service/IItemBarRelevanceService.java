package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IItemBarRelevanceService {
	//查询单个商品推荐
	public List<Map<String, Object>> searchItemBarRelevance(Map<String, Object> itemParam) throws Exception;
	//查询列表
	public List<Map<String, Object>> searchItemBarRelevanceList(Map<String, Object> itemParam) throws Exception;
	//添加
	public void insertItemBarRelevance(Map<String, Object> itemParam) throws Exception;
	//删除
	public void deleteItemBarRelevance(Map<String, Object> itemParam) throws Exception;
	//修改
	public void updateItemBarRelevance(Map<String, Object> itemParam) throws Exception;
}
