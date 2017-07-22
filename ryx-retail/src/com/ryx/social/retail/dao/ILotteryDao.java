package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface ILotteryDao {

	/**
	 * 添加抽奖活动数据（二维码）
	 * @author 徐虎彬
	 * @date 2014年4月21日
	 * @param dataMap
	 * @throws Exceprion
	 */
	public void insertLottery(List<Map<String, Object>> dataMapList)throws Exception;
	/**
	 * 查询抽奖活动数据（二维码）
	 * @author 徐虎彬
	 * @date 2014年4月21日
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectLottery(Map<String,Object> dataMap) throws Exception;
}
