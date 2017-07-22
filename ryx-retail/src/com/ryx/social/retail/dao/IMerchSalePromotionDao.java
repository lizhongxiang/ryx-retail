package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IMerchSalePromotionDao {
	//增加
	public void insertMerchSalePromotion(Map<String, Object> paramsMap)throws Exception;
	//修改
	public void updateMerchSalePromotion(Map<String, Object> paramsMap)throws Exception;
	//删除
	public void delMerchSalePromotion(Map<String, Object> paramsMap)throws Exception;
	//查询
	public List<Map<String, Object>> selMerchSalePromotion(Map<String, Object> paramsMap)throws Exception;
	
}
