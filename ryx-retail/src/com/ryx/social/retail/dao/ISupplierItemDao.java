package com.ryx.social.retail.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 
 * 供应商商品数据操作类接口
 * @author 隋长国
 *
 */
public interface ISupplierItemDao {
	
	/**
	 * 获取商品信息列表
	 * @param itemName 商品名称
	 * @param supplierName 供应商名称
	 * @param pageStart 分页开始
	 * @param pageNum  分页结束
	 * @return
	 * @throws SQLException 
	 */
	public Map<String, Object> getSupplierItemList(Map<String,String> params) throws SQLException;
	
	
	/**
	 * 根据供应商ID和商品ID获取列表
	 * @param map
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> getItemListBySupplierIDAndItemID(Map<String,String> map) throws SQLException;
}
