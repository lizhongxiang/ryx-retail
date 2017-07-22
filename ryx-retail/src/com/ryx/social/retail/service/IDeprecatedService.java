package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;
@Deprecated
public interface IDeprecatedService {

	/**
	 * 查询零售户商圈排名
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public List<Map<String, Object>> searchGMerchSqGather(Map<String, Object> paramMap)throws Exception;
	
	
	/**
	 * 查询零售户商圈
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public Map<String, Object> searchGMerchSqGatherRranking(Map<String, Object> paramMap) throws Exception;

}
