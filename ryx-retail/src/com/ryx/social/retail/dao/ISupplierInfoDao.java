package com.ryx.social.retail.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ISupplierInfoDao {
	/**
	 * 获取供应商列表
	 * @param supplierIDS
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,Object>> getSupplierInfoList(String supplierIDS) throws SQLException;

}
