package com.ryx.social.retail.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 购物车service
 * @author schg
 *
 */
public interface IMerchShoppingCartService {

	/**
	 * 添加购物车
	 * @throws SQLException 
	 */
	public void addMerchShoppingCart(Map<String,String> cart) throws SQLException;
	
	
	/**
	 * 获取零售户购物车列表
	 * @param merchID
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> getMerchCartLsit(String merchID) throws SQLException;
	
	/**
	 * 更新购物车的状态
	 * @param map
	 * @throws SQLException 
	 */
	public void updateCartStatus(Map<String,String> map) throws SQLException;
	

	/**
	 * 获取购物车商品数量信息   总金额  总量  种类总量
	 * @param merchID
	 * @return
	 * @throws SQLException 
	 */
	public Map<String,String> getCartTotalInfo(String merchID) throws SQLException;
	
	
	/**
	 * 清空购物车
	 * @param merchID
	 * @throws SQLException 
	 */
	public void deleteAllCart(Map<String,String> map ) throws SQLException;
}
