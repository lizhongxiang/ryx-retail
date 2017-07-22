package com.ryx.social.retail.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 商户购物车
 * @author 隋长国
 *
 */
public interface IMerchShoppingCartDao {

	/**
	 * 添加购物车
	 * @param card
	 * @throws SQLException 
	 */  
	public void insertCart(Map<String,String> cart) throws SQLException;
	
	/**
	 * 获取购物车列表
	 * @param merchID 商户ID
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,String>> getShoppingCartList(String merchID) throws SQLException;
	
	/**
	 * 删除购物车信息
	 * @param cardID 购物车ID列表
	 * @throws SQLException 
	 */
	public void deleteShoppingCard(String cartID) throws SQLException;
	
	/**
	 * @throws SQLException 
	 * 
	 */
	public void updateShopingCard(Map<String,String> map) throws SQLException;
	/**
	 * 根据供应商分组获取订单信息
	 * @param merchID 商户ID
	 * @param cardIDS 购物车ID列表
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> getMerchCardGroup(String merchID,String cardIDS) throws SQLException;
	
	/**
	 * 看某种商品购物车中是否存在
	 * @param map merchID 商户号 suuplierID 供货商号 itemID 商品ID
	 * @return
	 * @throws SQLException 
	 */
	public Map<String,String> existIncarts(Map<String,String> map) throws SQLException;
	
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
