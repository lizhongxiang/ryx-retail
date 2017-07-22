package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IPromotionService {

	/**
	 * 查询促销信息
	 * @author lizhxi1995
	 * @param promoMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchPromotion(Map<String, Object> paramMap)throws Exception;
	/**
	 * 插入促销信息
	 * @param promoMap
	 * @throws Exception
	 */
	public void insertMerchPromotion(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询促销流水
	 * @param promoMap
	 * @return 
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchPromotionRecord(Map<String, Object> paramMap) throws Exception;
	/**
	 * 插入促销流水
	 * @param promoMap
	 * @throws Exception
	 */
	public void insertMerchPromotionRecord(List<Map<String, Object>> paramList) throws Exception;
	/**
	 * 插入促销奖品
	 * @param promoMap
	 * @throws Exception
	 */
	public void insertPromotionPrize(List<Map<String, Object>> paramList) throws Exception;
	
	/**
	 * 查询促销奖品
	 * @param promoMap
	 * @return 
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchPromotionPrize(Map<String, Object> paramMap) throws Exception;
	/**
	 * 删除促销信息（逻辑删除）
	 * @param paramMap
	 * @throws Exception
	 */
	public void removeMerchPromotion(Map<String, Object> paramMap) throws Exception;
	/**
	 * 修改促销奖品
	 * @param paramMap
	 * @throws Exception
	 */
	public void updatePromotionPrize(Map<String, Object> paramMap) throws Exception;
	/**
	 * 修改促销细腻
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateMerchPromotion(Map<String, Object> paramMap) throws Exception;
	
	
}
