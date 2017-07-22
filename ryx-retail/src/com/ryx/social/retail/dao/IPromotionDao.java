package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IPromotionDao {

	/**
	 * 查询促销活动
	 * @author 李钟祥
	 * @param promoMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchPromotion(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询促销奖品表
	 * @author 李钟祥
	 * @param prizeMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchPromotionPrize(Map<String, Object> paramMap)throws Exception;
	/**
	 * 查询促销流水
	 * @author 李钟祥
	 * @param promotionMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchPromotionRecord(Map<String, Object> recordMap) throws Exception;
	/**
	 * 插入促销流水
	 * @author 李钟祥
	 * @param promotionMap
	 * @throws Exception
	 */
	public void insertMerchPromotionRecord(List<Map<String, Object>> recordList) throws Exception;
	/**
	 * 插入促销信息
	 * @author 李钟祥
	 * @param promotionMap
	 * @throws Exception
	 */
	public void insertMerchPromotion(Map<String, Object> paramMap)throws Exception;
	/**
	 * 插入促销奖品表
	 * @author 李钟祥
	 * @param prizeMap
	 * @throws Exception
	 */
	public void insertPromotionPrize(List<Map<String, Object>> paramsList) throws Exception;
	/**
	 * 修改促销信息
	 * @param promotionMap
	 * @throws Exception
	 */
	public void updateMerchPromotion(Map<String, Object> paramMap)throws Exception;
	/**
	 * 修改促销奖品
	 * @param prizeMap
	 * @throws Exception
	 */
	public void updatePromotionPrize(Map<String, Object> paramMap) throws Exception;
}
