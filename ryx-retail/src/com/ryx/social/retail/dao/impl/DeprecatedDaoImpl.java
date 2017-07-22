package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IDeprecatedDao;
import com.ryx.social.retail.util.SQLUtil;

@Repository
public class DeprecatedDaoImpl extends BaseDaoImpl implements IDeprecatedDao {
	
	private Logger LOG = LoggerFactory.getLogger(DeprecatedDaoImpl.class);
	
	//查询零售户商圈排名
	@Override
	public List<Map<String, Object>> selectGCustSQGatherRranking(Map<String, Object> paramMap) throws Exception {
		LOG.debug("DeprecatedDaoImpl selectGCustSQGatherRranking paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		String month = MapUtil.getString(paramMap, "month");
		String type = MapUtil.getString(paramMap, "type");
		sql.append("SELECT QTY_GATHER.* ,  'GJ' GATHER_TYPE FROM ");
		sql.append("( ");
		sql.append("SELECT MERCH_ID, QTY_ORD QTY, QTY_PM PM, QTY_PM_LAST PM_LAST FROM G_MERCH_SQ_GATHER "); 
		sql.append("WHERE 1=1 AND MONTH = ? AND TYPE = ? ");
		sql.append("ORDER BY QTY_PM ");
		sql.append(") QTY_GATHER ");
		sql.append("WHERE ROWNUM <= 4 ");
		sql.append("UNION ALL ");
		sql.append("SELECT SALE_GATHER.*, 'XS' GATHER_TYPE FROM "); 
		sql.append("( ");
		sql.append("SELECT MERCH_ID, QTY_SALE, SALE_PM, SALE_PM_LAST FROM G_MERCH_SQ_GATHER "); 
		sql.append(" WHERE 1=1 AND MONTH = ? AND TYPE = ? ");
		sql.append("ORDER BY SALE_PM ");
		sql.append(") SALE_GATHER ");
		sql.append("WHERE ROWNUM <= 4 ");
		sql.append("UNION ALL ");
		sql.append("SELECT PROFIT_GATHER.*, 'YL' GATHER_TYPE FROM ");
		sql.append(" ( ");
		sql.append("SELECT MERCH_ID, PROFIT, PROFIT_PM, PROFIT_PM_LAST FROM G_MERCH_SQ_GATHER "); 
		sql.append("WHERE 1=1 AND MONTH = ? AND TYPE = ? ");
		sql.append("ORDER BY PROFIT_PM ");
		sql.append(") PROFIT_GATHER ");
		sql.append("WHERE ROWNUM <= 4 ");
		sql.append(" UNION ALL ");
		sql.append("SELECT PROFIT_GATHER.*, 'DT' GATHER_TYPE FROM "); 
		sql.append("( ");
		sql.append("SELECT MERCH_ID, AVG_PRI, AVG_PRI_PM, AVG_PRI_PM_LAST FROM G_MERCH_SQ_GATHER "); 
		sql.append("WHERE 1=1 AND MONTH = ? AND TYPE = ? ");
		sql.append("ORDER BY AVG_PRI_PM ");
		sql.append(") PROFIT_GATHER ");
		sql.append("WHERE ROWNUM <= 4 ");
		sql.append(" UNION ALL ");
		sql.append("SELECT '', 1, 1, COUNT(*) SUM_NUM, 'TJ' TYPE FROM G_MERCH_SQ_GATHER WHERE MONTH= ? GROUP BY MONTH");
		list.add(month);
		list.add(type);
		list.add(month);
		list.add(type);
		list.add(month);
		list.add(type);
		list.add(month);
		list.add(type);
		list.add(month);
		
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	//查询零售户商圈
	@Override
	public List<Map<String, Object>> selectGMerchSqGather(Map<String, Object> paramMap) throws Exception {
		LOG.debug("DeprecatedDaoImpl selectGMerchSqGather paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		sql.append("select  ");
		sql.append("MERCH_ID, TYPE, MONTH, QTY_ORD, QTY_ORD_AVG, QTY_ORD_MAX, QTY_ORD_MIN,  ");
		sql.append("QTY_PM, QTY_PM_LAST, QTY_SALE, QTY_SALE_AVG, QTY_SALE_MAX, QTY_SALE_MIN,  ");
		sql.append("SALE_PM, SALE_PM_LAST, QTY_WHSE, QTY_WHSERATE, QTY_WHSERATE_AVG, QTY_WHSERATE_MAX,  ");
		sql.append("QTY_WHSERATE_MIN, WHSERATE_PM, WHSERATE_PM_LAST, AVG_PRI, AVG_PRI_AVG, AVG_PRI_MAX,  ");
		sql.append("AVG_PRI_MIN, AVG_PRI_PM, AVG_PRI_PM_LAST, PROFIT, PROFIT_AVG, PROFIT_MAX, PROFIT_MIN,  ");
		sql.append("PROFIT_PM, PROFIT_PM_LAST ");
		sql.append("from G_MERCH_SQ_GATHER ");
		sql.append("where 1=1 ");
		
		SQLUtil.initSQLEqual(paramMap, sql, list, "merch_id", "month", "type");
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
}
