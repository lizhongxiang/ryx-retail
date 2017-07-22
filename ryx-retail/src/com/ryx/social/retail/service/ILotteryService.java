package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface ILotteryService {
	/**
	 * 是否有抽奖活动
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param lotteryData
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> whetherLottery(Map<String,String> lotteryData)throws Exception;
	
	/**
	 * 抽奖活动列表参数
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param lotteryData
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getActivityParameter(Map<String,String> lotteryData)throws Exception;
	/**
	 * 活动详情
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param lotteryData
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> getCustomerRaffleDetails(Map<String,String> lotteryData)throws Exception;
	/**
	 * 抽奖
	 * @author 徐虎彬
	 * @date 2014年4月3日
	 * @param lotteryData
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> submitLottery(Map<String,String> lotteryData)throws Exception;
	/**
	 * 获取二维码信息
	 * @author 徐虎彬
	 * @date 2014年4月21日
	 * @param datMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getLotteryList(Map<String,Object> datMap)throws Exception;

	public Map<String, Object> getSubmitDetail(Map<String, String> lotteryData) throws Exception;

}
