package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface IMerchService {

	public Map getMerchInfo(String merchId) throws Exception;
	public Map getUserInfo(String userId) throws Exception;
	
	public List<Map<String, Object>> searchMerchJoinMerchFile(Map<String, Object> merchParam) throws Exception;
	public void updateMerchInfo(Map merchInfo) throws Exception;
	public void updateMerchBasicInfo(Map<String, Object> merchInfo) throws Exception;
	
	/**
	 * 修改小票设置
	 * @author 徐虎彬
	 * @date 2014年4月2日
	 * @param merchTicket
	 * @throws Exception
	 */
	public void updateAndInsertMerchTicket(Map<String,Object> merchTicket)throws Exception;
	/**
	 * 查询小票设置
	 * @author 徐虎彬
	 * @date 2014年4月2日
	 * @param merchTicket
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> selectMerchTicket(Map<String,Object> merchTicket)throws Exception;
	/**
	 * 查询商户信息
	 * @param merchMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerch(Map<String, Object> merchMap)throws Exception;
	/**
	 * 交接班
	 * @author 李钟祥
	 * @param merchTicket
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> dutyShift(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 查询交接班
	 * @author 李钟祥
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchDutyShift(Map<String, Object> paramsMap)throws Exception;
	/**
	 * 插入交接班
	 * @author 李钟祥
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> insertDutyShift(Map<String, Object> paramsMap)throws Exception;
	
	/**
	 * 更新商户的重新登陆时间
	 * @param tokenSecret
	 * @param merchID
	 */
	public void updateMerchLoginTime(String tokenSecret,String merchID);
	public void changePassword() throws Exception;
	
	/**
	 * 查询迁移数据，权限用户
	 */
	public List<Map<String, Object>> selectTransferMerch(Map<String, Object> paramMap)throws Exception;
	
	/**
	 * 修改迁移数据，权限用户
	 */
	public void updateTransferMerch(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> getOnlineDuration(Map<String, Object> param) throws Exception;
	
	/**
	 * 上传智能终端经纬度
	 * @param paramMap
	 * @throws Exception
	 */
	public Map<String, Object> uploadLocation(Map<String, Object> paramMap) throws Exception;
	
}
