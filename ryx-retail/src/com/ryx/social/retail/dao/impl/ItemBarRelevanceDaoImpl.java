package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.IItemBarRelevanceDao;

@Repository("itemBarRelevanceDao")
public class ItemBarRelevanceDaoImpl extends BaseDaoImpl implements
		IItemBarRelevanceDao {
	private Logger logger = LoggerFactory
			.getLogger(ItemBarRelevanceDaoImpl.class);

	public static final String selectItemBarSql = initSelectItemBarSql();

	private static String initSelectItemBarSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ITEM_BAR,REFERENCEBAR FROM");
		sb.append(" ITEM_BAR_RELEVANCE WHERE 1=1");
		return sb.toString();
	}

	/**
	 * 通过主条码查询推荐商品条码
	 * */
	@Override
	public List<Map<String, Object>> selectReferenceBar(
			Map<String, Object> itemBarRelevanceParam) throws Exception {
		logger.debug("ItemBarRelevanceDaoImpl selectReferenceBar itemBarRelevanceParam: "
				+ itemBarRelevanceParam);
		StringBuffer sqlBuffer = new StringBuffer(selectItemBarSql);
		String itemBar = (String) itemBarRelevanceParam.get("item_bar");
		List<Object> paramObject = new ArrayList<Object>();
		if (itemBar != null) {
			String[] itemBarArray = itemBar.split(",");
			if (itemBarArray.length == 1) {
				sqlBuffer.append(" AND ITEM_BAR = ?");
				paramObject.add(itemBarArray[0]);
			} else {
				sqlBuffer.append(" AND ITEM_BAR IN (");
				for (int i = 0; i < itemBarArray.length; i++) {
					paramObject.add(itemBarArray[i]);
					if (i == 0) {
						sqlBuffer.append("?");
					} else {
						sqlBuffer.append(", ?");
					}
				}
				sqlBuffer.append(") ");
			}
		}
		return this.selectBySqlQuery(sqlBuffer.toString(), paramObject.toArray());
	}

	/**
	 * 通过推荐商品条码,查询主条码
	 * */
	@Override
	public List<Map<String, Object>> selectItemBarReferenceBarList(
			Map<String, Object> itemBarRelevanceParam) throws Exception {
		logger.debug("ItemBarRelevanceDaoImpl selectItemBarReferenceBarList itemBarRelevanceParam: "
				+ itemBarRelevanceParam);
		return 	this.selectBySqlQuery(selectItemBarSql);
	}

	/**
	 * 添加推荐商品
	 */
	@Override
	public void insertItemBarRelevance(Map<String, Object> itemBarRelevanceParam)
			throws Exception {
		String sql = "INSERT INTO ITEM_BAR_RELEVANCE(ITEM_BAR,REFERENCEBAR) VALUES (?,?)";
		Object[] obj = new Object[2];
		obj[0] = itemBarRelevanceParam.get("item_bar");
		obj[1] = itemBarRelevanceParam.get("referencebar");
		this.executeSQL(sql, obj);
	}

	/**
	 * 通过主条码删除推荐商品
	 */
	@Override
	public void deleteItemBarRelevance(Map<String, Object> itemBarRelevanceParam)
			throws Exception {
		String sql = "DELETE FROM ITEM_BAR_RELEVANCE WHERE ITEM_BAR=?";
		Object[] obj = new Object[1];
		obj[0] = itemBarRelevanceParam.get("item_bar");
		this.executeSQL(sql,obj);
	}
	/**
	 * 通过主条码修改推荐商品
	 */
	@Override
	public void updateItemBarRelevance(Map<String, Object> itemBarRelevanceParam)
			throws Exception {
		String sql = "UPDATE ITEM_BAR_RELEVANCE SET REFERENCEBAR=? WHERE ITEM_BAR=?";
		Object[] obj = new Object[2];
		obj[0] = itemBarRelevanceParam.get("referencebar");
		obj[1] = itemBarRelevanceParam.get("item_bar");
		this.executeSQL(sql,obj);
	}
}
