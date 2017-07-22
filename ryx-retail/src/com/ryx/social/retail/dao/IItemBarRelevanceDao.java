package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IItemBarRelevanceDao {
	//通过主条码查询推荐商品条码
	public List<Map<String, Object>> selectReferenceBar(Map<String, Object> itemBarRelevanceParam)throws Exception;
	//通过推荐商品条码,查询主条码
	public List<Map<String, Object>> selectItemBarReferenceBarList(Map<String, Object> itemBarRelevanceParam)throws Exception;
	//新增
	public void insertItemBarRelevance(Map<String,Object> itemBarRelevanceParam)throws Exception;
	//删除
	public void deleteItemBarRelevance(Map<String,Object> itemBarRelevanceParam)throws Exception;
	//修改
	public void updateItemBarRelevance(Map<String,Object> itemBarRelevanceParam)throws Exception;
}
