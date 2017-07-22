package com.ryx.social.retail.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IMerchReceiveAddressDao {
	/**
	 * 添加供应商地址
	 * @param address 地址信息
	 * @throws SQLException 
	 */
	public void addMerchReceiveAddress(Map<String ,String> address) throws SQLException;
	
	/**
	 * 删除缴费地址
	 * @param addressID
	 * @throws SQLException 
	 */
	public void deleteMerchReceiveAddress(String addressID) throws SQLException;
	
	/**
	 * 
	 * @param address
	 * @throws SQLException 
	 */
	public void updateMerchReceiveAddress(Map<String ,String> address) throws SQLException;
	/**
	 * 获取供应商收货地址列表
	 * @param merchID
	 * @throws SQLException 
	 */
	public List<Map<String,String>> getMerchReceiveAddressList(String merchID) throws SQLException;
	/**
	 * 获取某个收货地址的详细信息
	 * @param id
	 * @param merchID
	 * @return
	 * @throws SQLException 
	 */
	public Map<String,String> getMerchReceiveAddress(String id,String merchID) throws SQLException;

}
