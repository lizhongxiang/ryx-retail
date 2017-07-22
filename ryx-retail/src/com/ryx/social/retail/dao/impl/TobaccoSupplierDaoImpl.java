package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.SpellUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.ISupplierDao;
import com.ryx.social.retail.util.DataUtil;

@Repository
public class TobaccoSupplierDaoImpl extends BaseDaoImpl implements ISupplierDao {
	private Logger LOG = LoggerFactory.getLogger(TobaccoSupplierDaoImpl.class);
	
	/**
	 * 获取卷烟供应商商品
	 */
	private static final String selectItemSql = getSelectItemSql();
	private static String getSelectItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ITEM_ID, ITEM_BAR, BIG_BAR, ITEM_NAME, SHORT_NAME, SHORT_CODE, ITEM_UNIT_NAME, UNIT_RATIO, PRI_WSALE, PRI_DRTL, FACT_NAME, BRAND_NAME,describe,subjectColor,tipColor,tipType,tarContent,co,pack，HAVE_IMG");
		sb.append(" FROM BASE_SUPPLIER_TOBACCO_ITEM WHERE SUPPLIER_ID = ? AND IS_INSALE = '1'");
		return sb.toString();
	}
	
	@Override
	public List<Map<String, Object>> getItemList(Map<String, String> param) throws Exception {
		String comId = (String) param.get("comId");
		String  sql=selectItemSql;
		if(param.containsKey("item_bar")&&param.get("item_bar")!=null&&param.get("item_bar").trim().length()>0){
			sql=selectItemSql+"AND (item_bar=? or big_bar=?)";
			if(comId!=null && !"".equals(comId)) {
				return this.selectBySqlQuery(sql, new Object[] {comId,param.get("item_bar"),param.get("item_bar")});
			}
		}else{
			if(comId!=null && !"".equals(comId)) {
				return this.selectBySqlQuery(sql, new Object[] {comId});
			}
		}
		
		return new ArrayList<Map<String,Object>>();
	}
	
	/**
	 * 增加卷烟商品
	 */
	private static final String insertItemSql = getInsertItemSql();
	private static String getInsertItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO BASE_SUPPLIER_TOBACCO_ITEM(SUPPLIER_ID, ITEM_ID, ITEM_BAR, ITEM_NAME, ITEM_UNIT_NAME, SHORT_CODE, SHORT_NAME, ITEM_KIND_ID, KIND, FACT_NAME, BRAND_NAME, BIG_BAR, BIG_UNIT_NAME, UNIT_RATIO, PRI_WSALE, PRI_DRTL, IS_INSALE)");
		sb.append(" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	
	@Override
	public void insertItems(String comId, List<Map<String, Object>> params) throws Exception {
		List list = new ArrayList();
		
		for (int i = 0; i < params.size(); i++) {
			Map<String, Object> param = params.get(i);
			Map map=DataUtil.getAllAndHead(param.get("item_name").toString());
			list.add(new String[] {comId,
		                                param.get("item_id").toString(),
		                                param.get("box_bar").toString(),
		                                param.get("item_name").toString(),
		                                "盒",
//		                                param.get("short_id"),//全拼
//		                                param.get("item_name"),//简拼
//		                                map.get("ALL").toString(),
//		                                map.get("HEAD").toString(),
		                                SpellUtil.getFullSpell(param.get("item_name").toString()),
										SpellUtil.getShortSpell(param.get("item_name").toString()),
		                                "01",
		                                "5",
		                                param.get("fact_name").toString(),
		                                param.get("brand_name").toString(),
		                                param.get("item_bar").toString(),
		                                "条",
		                                param.get("rods").toString(),
		                                param.get("pri_wsale").toString(),
		                                param.get("pri_drtl").toString(),
		                                param.get("status").toString()
		                            });
		}
//		this.executeSQL(deleteItemsql);
		this.executeBatchSQL(insertItemSql, list);
	}
	
	@Override
	public void insertItem(Map<String, String> param) throws Exception {
		
		
		this.executeSQL(insertItemSql, new String[] {param.get("comId"),
		                                param.get("item_id"),
		                                param.get("box_bar"),
		                                param.get("item_name"),
		                                "盒",
		                                param.get("short_id"),
		                                param.get("item_name"),
		                                "01",
		                                "5",
		                                param.get("fact_name"),
		                                param.get("brand_name"),
		                                param.get("item_bar"),
		                                "条",
		                                param.get("rods"),
		                                param.get("pri_wsale"),
		                                param.get("pri_drtl"),
		                                param.get("status")});
	}

	@Override
	public void updateItems(List<Map<String, Object>> itemList,String string)
			throws Exception {
		   String sql="UPDATE BASE_SUPPLIER_TOBACCO_ITEM SET SHORT_CODE=?, SHORT_NAME=? "
		   		+ "WHERE SUPPLIER_ID=? AND ITEM_ID=?";//只更新全拼和简拼
			List list = new ArrayList();
		  for (int i = 0; i < itemList.size(); i++) {
				Map<String, Object> param = itemList.get(i);
				list.add(
						new String[] {
								SpellUtil.getFullSpell(param.get("item_name").toString()),
								SpellUtil.getShortSpell(param.get("item_name").toString()),
								param.get("supplier_id").toString(),
								param.get("item_id").toString()
								}
						);
			}
			this.executeBatchSQL(sql, list);
		   
	}
	
	@Override
	public void updateItems(List<Map<String, Object>> itemList)
			throws Exception {
		   String sql="UPDATE BASE_SUPPLIER_TOBACCO_ITEM "
		   		+ "SET SHORT_CODE=?, SHORT_NAME=? , DESCRIBE=?,SUBJECTCOLOR=? ,"
		   		+ "TIPCOLOR=? ,TIPTYPE=?, TARCONTENT=? ,CO=? ,PACK=? "
		   		+ "WHERE SUPPLIER_ID=? AND ITEM_ID=?";
			List list = new ArrayList();
		  for (int i = 0; i < itemList.size(); i++) {
				Map<String, Object> param = itemList.get(i);
				list.add(
						new String[] {
								SpellUtil.getFullSpell(param.get("item_name").toString()),
								SpellUtil.getShortSpell(param.get("item_name").toString()),
								param.get("describe").toString(),
								param.get("subjectColor").toString(),
								param.get("tipColor").toString(),
								param.get("tipType").toString(),
								param.get("tarContent").toString(),
								param.get("co").toString(),
								param.get("pack").toString(),
								param.get("supplier_id").toString(),
								param.get("item_id").toString()
								}
						);
			}
			this.executeBatchSQL(sql, list);
		   
	}
	
	/**
	 * 
	 */
	private static final String updateItemSql = updateItemSql();
	private static String updateItemSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE BASE_SUPPLIER_TOBACCO_ITEM "
				+ "SET ITEM_BAR=?, ITEM_NAME=?, SHORT_CODE=?, SHORT_NAME=?, "
				+ "ITEM_KIND_ID=?, FACT_NAME=?, BRAND_NAME=?, BIG_BAR=?, "
				+ "UNIT_RATIO=?, PRI_WSALE=?, PRI_DRTL=?, IS_INSALE=? "
				+ "WHERE SUPPLIER_ID=? AND ITEM_ID=?");
		return sb.toString();
	}
	
	@Override
	public void updateItems2(List<Map<String,Object>> itemList)throws Exception{
		  List list = new ArrayList();
		  for (int i = 0; i < itemList.size(); i++) {
				Map<String, Object> param = itemList.get(i);
				list.add(
						new String[] {
								
								param.get("box_bar").toString(),
								param.get("item_name").toString(),
								SpellUtil.getFullSpell(param.get("item_name").toString()),
								SpellUtil.getShortSpell(param.get("item_name").toString()),
								"01",
								param.get("fact_name").toString(),
								param.get("brand_name").toString(),
								param.get("item_bar").toString(),
								param.get("rods").toString(),
								param.get("pri_wsale").toString(),
								param.get("pri_drtl").toString(),
								param.get("status").toString(),
								param.get("supplier_id").toString(),
	                            param.get("item_id").toString()
	                            
								});
			}
			this.executeBatchSQL(updateItemSql, list);
	}

	@Override
	public List<Map<String, Object>> getAllItemList() throws Exception {
		String sql="SELECT SUPPLIER_ID,ITEM_ID,ITEM_NAME FROM BASE_SUPPLIER_TOBACCO_ITEM";
		return this.selectBySqlQuery(sql);
	}

	
	/**
	 * 条件查询供应商卷烟信息
	 */
	private static final String selectItemSqlByCondition = getSelectItemByConditionSql();
	private static String getSelectItemByConditionSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT *");
		sb.append(" FROM BASE_SUPPLIER_TOBACCO_ITEM WHERE SUPPLIER_ID = ?");
		return sb.toString();
	}
	
	
	@Override
	public List<Map<String, Object>> selectItem(Map<String, Object> thisMap) throws Exception {
		String supplier_id=MapUtil.getString(thisMap, "supplier_id",null);
		String item_id=MapUtil.getString(thisMap, "item_id",null);
		String bar=MapUtil.getString(thisMap,"item_bar",null );
				
		StringBuffer sb=new StringBuffer();
		sb.append(selectItemSqlByCondition);
//		if(item_id!=null ){
		if(!StringUtil.isBlank(item_id)){
			sb.append("AND ITEM_ID IN ("+item_id+")");
		}
//		if(bar!=null ){
		if(!StringUtil.isBlank(bar)){
			sb.append("AND (item_bar=? OR big_bar=?)");
			return this.selectBySqlQuery(sb.toString(), new Object[]{supplier_id,bar,bar});
		}
		return this.selectBySqlQuery(sb.toString(), new Object[]{supplier_id});
	}
	
	@Override
	public List<Map<String, Object>> getTobaccoItem(String itemId) throws Exception {
		LOG.debug("TobaccoSupplierDaoImpl getTobaccoItem itemId: " + itemId);
		return this.selectBySqlQuery("select * from base_supplier_tobacco_item where item_id=? order by supplier_id asc", new Object[]{itemId});
	}
	
}
