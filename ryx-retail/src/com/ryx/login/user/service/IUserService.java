package com.ryx.login.user.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ryx.login.user.bean.User;

/**
 * IUserService 接口 用户的领域接口
 * 
 */
@Service
public interface IUserService {

	/**
	 * 获取零售户信息
	 * @param refId
	 * @return
	 */
	public Map<String, Object> getMerchInfo(String refId);
	/**
	 * 接口方法 query 查询用户
	 * 
	 * @param map
	 *            查询及排序条件
	 */
	public List getUserResource(String userId);
	/**
	 * 更新用户登陆信息
	 * @param userId 用户ID
	 * @param id	用户IP
	 * @param loginTime	此次登陆时间
	 */
	//public void updateUserLoginMeg(String userId, String ip,String loginTime);
	public List<Map<String, Object>> getUserPermission(String userCode, String comId) throws Exception;
}
