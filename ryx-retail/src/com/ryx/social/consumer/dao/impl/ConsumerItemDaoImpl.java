package com.ryx.social.consumer.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.social.consumer.dao.IConsumerItemDao;

@Repository("consumerItemDao")
public class ConsumerItemDaoImpl extends BaseDaoImpl implements IConsumerItemDao{
	private static final Logger logger=LoggerFactory.getLogger(ConsumerItemDaoImpl.class);
	
	//通过商品名称查找商品列表
	@Override
	public List<Map<String, Object>> searchItemByName(Map<String, Object> itemMap) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sql=new StringBuffer();
		sql.append(" select ITEM_ID,item_name  ");
		sql.append(" from base_item ");
		sql.append(" where ITEM_NAME like ? ");
		sql.append(" group by ITEM_ID,item_name ");
		sql.append(" UNION ");
		sql.append(" select ITEM_ID,ITEM_NAME  ");
		sql.append(" from BASE_SUPPLIER_TOBACCO_ITEM ");
		sql.append(" where ITEM_NAME like ? ");
		sql.append(" group by ITEM_ID,item_name  ");
		
		List<String> sqlList=new ArrayList<String>();
		sqlList.add("%"+(String) itemMap.get("itemName")+"%");
		sqlList.add("%"+(String) itemMap.get("itemName")+"%");
		String pageIndex = (String) itemMap.get("page_index");
		String pageSize = (String) itemMap.get("page_size");	
		Page pageResult=this.searchPaginatedBySql(sql.toString(), Integer.valueOf(pageIndex), Integer.valueOf(pageSize),sqlList.toArray());
		itemMap.put("page_count", pageResult.getPageSum());
		itemMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	
	@Override
	public List<Map<String, Object>> searchItemAndMerchByName( Map<String, Object> itemMap) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sql=new StringBuffer();
		sql.append("  select ITEM_ID, ITEM_NAME,PRI4,  ");
		sql.append("  sqrt(power((to_number(LONGITUDE)-2)*1117000,2)+power((to_number(LATITUDE)-3)*1117000,2)) distance,  ");
		sql.append("  '200' jiaoyou,bm.MERCH_ID,MERCH_NAME,OPEN_TIME||'-'||CLOSE_TIME BUSI_TIME,CLOSE_TIME,OPEN_TIME,FILE_ID,ITEM_KIND_ID  ");
		sql.append("  from BASE_MERCH_ITEM bmi,BASE_MERCH bm  ");
		sql.append("   where bmi.MERCH_ID=bm.MERCH_ID ");
		sql.append("  and bmi.ITEM_name like ?  ");
		
		List<String> sqlList=new ArrayList<String>();
		sqlList.add("%"+itemMap.get("itemName")+"%");
		String pageIndex = (String) itemMap.get("page_index");
		String pageSize = (String) itemMap.get("page_size");
		Page pageResult=this.searchPaginatedBySql(sql.toString(), Integer.valueOf(pageIndex), Integer.valueOf(pageSize),sqlList.toArray());
		itemMap.put("page_count", pageResult.getPageSum());
		itemMap.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
	//通过商品itemId 查找商品详情
	@Override
	public List<Map<String, Object>> searchItemInfoBySupplienId( Map<String, Object> itemmMap) throws Exception {
		StringBuffer sql=new StringBuffer();
		sql.append(" select item_id,ITEM_NAME ,'白色' mainColor, '黄色' filterColor ,'1.3' tar,'22' monoxide,UNIT_NAME ");
		sql.append(" from base_item   ");
		sql.append(" where item_id=?  ");
		sql.append(" UNION  ");
		sql.append(" select item_id,ITEM_NAME ,'白色' mainColor, '黄色' filterColor ,'1.3' tar,'22' monoxide,ITEM_UNIT_NAME  ");
		sql.append(" from BASE_SUPPLIER_TOBACCO_ITEM ");
		sql.append(" where  ");
		sql.append("  item_id=? ");
		List<String> sqlList=new ArrayList<String>();
		sqlList.add((String) itemmMap.get("itemId"));
		sqlList.add((String) itemmMap.get("itemId"));
		return this.selectBySqlQuery(sql.toString(),sqlList.toArray());		
	}
	
	public List<Map<String, Object>> searchItemOfInSaleByMerchId(Map<String	, Object> map)throws Exception{
		StringBuffer sql=new StringBuffer();
		sql.append(" select MERCH_ID, ITEM_ID, ITEM_BAR, ITEM_NAME, SHORT_CODE, SHORT_NAME, ITEM_KIND_ID, UNIT_NAME, COST, PRI2, PRI4,");
		sql.append(" DISCOUNT, START_DATE, END_DATE, IS_NEW, IS_OUTSTOCK, IS_RECOMMEND, IS_PROMOTION, PRI1, SPEC, STATUS, CREATE_DATE, CREATE_TIME");
		sql.append("  from BASE_MERCH_ITEM ");
		sql.append("  where MERCH_ID=? ");
		sql.append("  and STATUS=1 "); 
		String pageIndex = (String) map.get("page_index");
		String pageSize = (String) map.get("page_size");
		List<String> sqlList=new ArrayList<String>();
		sqlList.add((String) map.get("merch_id"));
		Page pageResult=this.searchPaginatedBySql(sql.toString(), Integer.valueOf(pageIndex), Integer.valueOf(pageSize),sqlList.toArray());
		map.put("page_count", pageResult.getPageSum());
		map.put("count", pageResult.getTotal());
		return pageResult.getRows();
	}
}