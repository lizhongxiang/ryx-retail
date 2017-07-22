package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface IMerchDao {

	public Map getMerchInfo(String merchId) throws Exception;
	public Map getUserInfo(String userId) throws Exception;
	
	public List<Map<String, Object>> selectMerch(Map<String, Object> merchParam) throws Exception;
	public void updateMerchInfo(Map merchInfo) throws Exception;
	
	
	public List<Map<String, Object>> searchMerch(Map<String, Object> merchParam) throws Exception;
	public void updateMerchBasicInfo(Map<String, Object> merchInfo) throws Exception;
	/**
	 * 添加小票设置
	 * @author 徐虎彬
	 * @date 2014年4月2日
	 * @param merchTicket
	 * @throws Exception
	 */
	public void insertMertchTicket(Map<String,Object> merchTicket)throws Exception;
	/**
	 * 修改小票设置
	 * @author 徐虎彬
	 * @date 2014年4月2日
	 * @param merchTicket
	 * @throws Exception
	 */
	public void updateMertchTicket(Map<String,Object> merchTicket)throws Exception;
	/**
	 * 根据merch_id查询小票设置信息
	 * @author 徐虎彬
	 * @date 2014年4月2日
	 * @param merchTicket
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectMerchTicket(Map<String,Object> merchTicket)throws Exception;
	/**
	 * 添加交接班
	 * @author 李钟祥
	 * @param paramsMap
	 * @throws Exception
	 */
	public void insertDutyShift(Map<String, Object> paramsMap) throws Exception;
	/**
	 * 查询交接班
	 * @author 李钟祥
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchDutyShift(Map<String, Object> paramsMap)throws Exception;
	public void updatePassword(List<String[]> passwordList) throws Exception;
	
	/**
	 * 修改迁移数据,权限用户
	 */
	public void updateTransferMerch(Map<String, Object> paramMap)throws Exception;
	
	/**
	 * 查询迁移数据，权限用户
	 */
	public List<Map<String, Object>> selectTransferMerch(Map<String, Object> paramMap)throws Exception;
}
