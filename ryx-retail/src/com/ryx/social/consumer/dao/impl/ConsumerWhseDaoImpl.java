package com.ryx.social.consumer.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.consumer.dao.IConsumerWhseDao;

@Repository
public class ConsumerWhseDaoImpl extends BaseDaoImpl implements IConsumerWhseDao {
	private static final Logger logger=LoggerFactory.getLogger(ConsumerWhseDaoImpl.class);
	/**
	 * 更新whse_merch表, 根据参数拼接sql, 必传merch_id, item_id
	 */
	@Override
	public void updateWhseMerch(Map<String, Object> whseMerchParam) throws Exception {
		logger.debug("WhseDaoImpl updateWhseMerch whseMerchParam: " + whseMerchParam);
		StringBuffer sqlBuffer = new StringBuffer("UPDATE WHSE_MERCH SET");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		String merchId = (String) whseMerchParam.get("merch_id");
		String whseDate = (String) whseMerchParam.get("whse_date");
		List<Map<String, Object>> whseMerchList = (List<Map<String, Object>>) whseMerchParam.get("list");
		StringBuffer sqlParamBuffer = new StringBuffer();
		for(Map<String, Object> whseMerchMap : whseMerchList) {
			List<Object> paramObject = new ArrayList<Object>();
			sqlParamBuffer = new StringBuffer();
			if(whseMerchMap.get("qty_sub")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = QTY_WHSE - ?,");
				paramObject.add(whseMerchMap.get("qty_sub"));
			} else if(whseMerchMap.get("qty_add")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = QTY_WHSE + ?,");
				paramObject.add(whseMerchMap.get("qty_add"));
			} else if(whseMerchMap.get("qty_whse")!=null) {
				sqlParamBuffer.append(" QTY_WHSE = ?,");
				paramObject.add(whseMerchMap.get("qty_whse"));
			}
			if(whseMerchMap.get("qty_locked")!=null) {
				sqlParamBuffer.append(" QTY_LOCKED = ?,");
				paramObject.add(whseMerchMap.get("qty_locked"));
			}
			if(whseMerchMap.get("qty_whse_warn")!=null) {
				sqlParamBuffer.append(" QTY_WHSE_WARN = ?,");
				paramObject.add(whseMerchMap.get("qty_whse_warn"));
			}
			sqlParamBuffer.append(" OUTPUT_DATE = ?,");
			paramObject.add(whseDate);
			if(whseMerchMap.get("qty_whse_init")!=null) {
				sqlParamBuffer.append(" QTY_WHSE_INIT = ?,");
				paramObject.add(whseMerchMap.get("qty_whse_init"));
			}
			if(whseMerchMap.get("whse_init_date")!=null) {
				sqlParamBuffer.append(" WHSE_INIT_DATE = ?,");
				paramObject.add(whseDate);
			}
			paramObject.add(merchId);
			paramObject.add(whseMerchMap.get("item_id"));
			paramArray.add(paramObject.toArray());
		}
		sqlParamBuffer.delete(sqlParamBuffer.length()-1, sqlParamBuffer.length()); // 删掉最后一个逗号
		sqlBuffer.append(sqlParamBuffer.toString());
		sqlBuffer.append(" WHERE MERCH_ID = ? AND ITEM_ID = ?");
		
		// 执行批量更新
		this.executeBatchSQL(sqlBuffer.toString(), paramArray);
	}
}
