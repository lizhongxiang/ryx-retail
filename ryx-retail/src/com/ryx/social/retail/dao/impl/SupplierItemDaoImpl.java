package com.ryx.social.retail.dao.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.social.retail.dao.ISupplierItemDao;

/**
 * 供应商商品操作类
 * @author 隋长国
 *
 */
@Repository
public class SupplierItemDaoImpl extends BaseDaoImpl implements ISupplierItemDao {
	private Logger LOG = LoggerFactory.getLogger(SupplierItemDaoImpl.class);

	@Override 
	public Map<String, Object> getSupplierItemList(Map<String,String> params) throws SQLException {
		String param = params.get("param");
		String pageNum = params.get("pageNum");
		String pageSize = params.get("pageSize");
		StringBuilder sb = new StringBuilder("select s.supplier_name, s.manager, s.telephone, s.phone, si.* "); 
		sb.append("from SUPPILER s ");
		sb.append("join SUPPLIER_ITEM si ");
		sb.append("on si.supplier_id = s.id ");
		sb.append("and s.status = '1' ");
		sb.append("and si.status = '1' ");
		if(param != null && !param.equals("")){
			sb.append(" and (s.supplier_name like '%"+param+"%' or si.ITEM_NAME like '%"+param+"%' ) ");
		}
		LOG.debug("查询供应商商品表SQL:"+sb.toString());
		Page page = this.searchPaginatedBySql(sb.toString(), Integer.parseInt(pageNum), Integer.parseInt(pageSize));
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("page_sum", page.getPageSum());
		map.put("total", page.getTotal());
		map.put("rows", page.getRows());
		return map;
	}

	@Override
	public List<Map<String, Object>> getItemListBySupplierIDAndItemID(Map<String, String> map) throws SQLException {
		StringBuffer sql = new StringBuffer();
		int i = 0;
		for(String supplierID : map.keySet()){
			i++;
			String cartIDS = map.get(supplierID);
			sql.append(" select si.id item_id, si.supplier_id , si.item_bar, si.item_name, si.item_unit_name,si.whole_prise, si.retail_prise ");
			sql.append(" from SUPPLIER_ITEM si where si.supplier_id = '"+supplierID+"'  and si.id in ("+cartIDS+") ");
			if(i < map.size()){
				sql.append(" union all ");
			}
		}
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> list = this.selectBySqlQuery(sql.toString());
		return list;
	}
	

}
