package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IMerchSalePromotionService {
	/**
	 *添加
	 * @param paramsMap
	 * @throws Exception
	 */
	public void createSalePromotion(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 查询
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSalePromotion(Map<String, Object> paramsMap) throws Exception;
	/**
	 * 删除
	 * @param paramsMap
	 * @throws Exception
	 */
	public void removeSalePromotion(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 修改
	 * @param paramsMap
	 * @throws Exception
	 */
	public void modifySalePromotion(Map<String, Object> paramsMap) throws Exception;
	
}
