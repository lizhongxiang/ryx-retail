package com.ryx.social.retail.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.ISupplierInfoDao;

@Repository
public class SupplierInfoDaoImpl extends BaseDaoImpl implements ISupplierInfoDao {

	@Override
	public List<Map<String,Object>> getSupplierInfoList(String supplierIDS) throws SQLException {
		String sql = "select ID,SUPPLIER_NAME, MANAGER,PHONE ,ADDRESS from SUPPILER where ID in("+supplierIDS+") and STATUS = '1'";
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> list = this.selectBySqlQuery(sql);
		return list;
	}

}
