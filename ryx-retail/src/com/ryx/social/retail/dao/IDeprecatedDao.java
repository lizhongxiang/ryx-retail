package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IDeprecatedDao {

	/**
	 * 查询零售户商圈排名
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectGCustSQGatherRranking(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 查询零售户商圈
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectGMerchSqGather(Map<String, Object> paramMap)throws Exception;

}
