package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IMerchConsumerService {
	
	/**
	 * 查询会员等级
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchConsumerGrade(Map<String, Object> paramMap) throws Exception;
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
	 * 修改会员等级--单个
	 * @param paramsMap
	 * @throws Exception
	 */
	public void updateMerchConsumerGrade(Map<String, Object> paramsMap)throws Exception;
	
	/**
	 * 删除会员等级
	 * @param paramsMap
	 * @throws Exception
	 */
	public void deleteMerchConsumerGrades(List<Map<String, Object>> paramList)throws Exception;
	
	/**
	 * 查询通过主键consumer_id
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectMerchConsumerById(Map<String, Object> paramsMap) throws Exception;
	
	/**
	 * 查询会员
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchConsumer(Map<String, Object> paramsMap)throws Exception;
	
	/**
	 * 查询会员（表关联-会员表，会员级别）
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchConsumer(Map<String, Object> paramsMap)throws Exception;
	
	/**
	 * 修改会员
	 * @param paramsMap
	 * @return 
	 * @throws Exception
	 */
	public void updateMerchConsumer(Map<String, Object> paramsMap) throws Exception;
	/**
	 * 批量修改会员
	 * @param paramsMap
	 * @return 
	 * @throws Exception
	 */
	public void updateBatchMerchConsumer(List<Map<String, Object>> consumerList) throws Exception;
	
	/**
	 * 插入会员
	 * @param paramsMap
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> insertMerchConsumer(Map<String, Object> paramsMap) throws Exception;
	
	/**
	 * 操作会员等级
	 * @param paramsMap
	 * @throws Exception
	 */
	public void operateMerchConsumerGrade(List<Map<String, Object>> paramList)throws Exception;
	
	/**
	 * 插入级别会员级别
	 * @param paramsMap
	 * @throws Exception
	 */
	public void insertDefMerchConsumerGrade(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 会员修改---用于会员管理（控制手机号，卡号重复）
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateMerchConsumerForHygl(Map<String, Object> paramMap)throws Exception;
	
	/**
	 * 查询积分兑换流水
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchScoreExchange(Map<String, Object> paramMap)throws Exception;
	
	/**插入积分兑换流水
	 * 
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertScoreExchange(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询积分兑换商品
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchExchangePrize(Map<String, Object> paramMap)throws Exception;
	/**
	 * 插入积分兑换商品
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertExchangePrize(Map<String, Object> paramMap) throws Exception;
	
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
	 * 插入默认级别，或修改无级别会员（会员管理）
	 * @param paramList
	 * @throws Exception
	 */
	public void insertDefaultGrade(List<Map<String, Object>> paramList)throws Exception;
	
	/**
	 * 修改积分兑换商品
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateExchangePrize(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 会员行为分析单行
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchConsumeBehaviorAnalysisLine(Map<String, Object> paramMap) throws Exception;
	
}
