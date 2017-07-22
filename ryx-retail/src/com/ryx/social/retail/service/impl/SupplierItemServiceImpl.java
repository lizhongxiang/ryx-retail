package com.ryx.social.retail.service.impl;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ryx.social.retail.dao.ISupplierItemDao;
import com.ryx.social.retail.service.ISupplierItemService;

/**
 * 供应商商品列表
 * @author 隋长国
 *
 */
@Service
public class SupplierItemServiceImpl implements ISupplierItemService {
	@Resource
	private ISupplierItemDao supplierItemDaoImpl;

	@Override
	public Map<String, Object> getSupplierItemList(Map<String,String> params) throws SQLException {
		return supplierItemDaoImpl.getSupplierItemList(params);
	}

}
