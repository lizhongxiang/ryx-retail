package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IMerchConsumerDao {
	
	//111111
	/**
	 * 查询会员等级
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchConsumerGrade(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> searchMerchConsumerGradeAndConsumerNumber(Map<String, Object> paramMap) throws Exception;
	/**
	 * 插入会员等级
	 * @param paramList
	 * @throws Exception
	 */
	public void insertMerchConsumerGrades(List<Map<String, Object>> paramList)throws Exception;
	/**
	 * 修改会员等级--批量
	 * @param paramList
	 * @throws Exception
	 */
	public void updateMerchConsumerGrades(List<Map<String, Object>> paramList)throws Exception;
	/**
	 * 修改会员等级---单个
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateMerchConsumerGrade(Map<String, Object> paramMap)throws Exception;
	/**
	 * 删除会员等级
	 * @param paramMap
	 * @throws Exception
	 */
	public void deleteMerchConsumerGrades(List<Map<String, Object>> paramList)throws Exception;
	///
	/**
	 * 插入会员
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertMerchConsumer(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchConsumer(Map<String, Object> paramMap)throws Exception;
	/**
	 * 查询会员（只查询会员consumer_id，phone_number）
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchConsumerById(Map<String, Object> paramMap)throws Exception;
	/**
	 * 修改会员
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateMerchConsumer(Map<String, Object> paramMap) throws Exception;
	/**
	 * 批量修改会员
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateMerchConsumer(List<Map<String, Object>> consumerList) throws Exception;
	/**
	 * 表关联查询会员（会员表；会员级别表）
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchConsumer(Map<String, Object> paramMap)throws Exception;
	/**
	 * 查询会员等级人数
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchConsumerConunt(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询基本会员等级
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectBaseMerchConsumerGrade(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 修改会员的等级，用于删除会员等级
	 * @param paramList
	 * @throws Exception
	 */
	public void updateMerchConsumerByGrade(List<Map<String, Object>> paramList)throws Exception;
	
	/**
	 * 插入积分兑换商品
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertExchangePrize(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 查询积分兑换商品
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectExchangePrize(Map<String, Object> paramMap)throws Exception;
	
	/**
	 * 插入积分兑换流水
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertScoreExchange(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 查询积分兑换流水
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchScoreExchange(Map<String, Object> paramMap)throws Exception;
	
	/**
	 * 按年龄性别分组
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchCustomerByAgeSex(Map<String, Object> paramMap) throws Exception ;
	
	/**
	 * 按收入分组
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchCustomerByMoney(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 按学历分组
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchCustomerByEdu(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 会员行为分析
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchConsumeBehaviorAnalysis(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 修改积分兑换商品
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public void updateExchangePrize(Map<String, Object> paramMap)throws Exception;
}
