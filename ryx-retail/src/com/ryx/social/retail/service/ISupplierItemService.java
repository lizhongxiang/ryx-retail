package com.ryx.social.retail.service;

import java.sql.SQLException;
import java.util.Map;

/**
 * 供应商商品列表
 * @author 隋长国
 *
 */
public interface ISupplierItemService {
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
}
