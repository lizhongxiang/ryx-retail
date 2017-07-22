package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.IItemService;
import com.ryx.social.retail.service.IUploadDataToRtmsService;
import com.ryx.social.retail.service.IWhseService;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class WhseServiceImpl implements IWhseService {

	private static final Logger LOG = LoggerFactory.getLogger(WhseServiceImpl.class);
	/**
	 * 显示正数库存 false(0：显示为负库存，true（1：显示为正库存
	 */
	public static boolean showPositiveWhse = true;

	@Resource
	private IWhseDao whseDao;
	@Resource
	private IItemDao itemDao;
	@Resource
	private IItemService itemService;
	
	@Resource
	private IUploadDataToRtmsService uploadDataToRtmsService;

	@Override
	public List<Map<String, Object>> searchWhseMerch(Map<String, Object> whseMerchParam) throws Exception {
		LOG.debug("WhseServiceImpl searchWhseMerch whseMerchParam: "+ whseMerchParam);
		return whseDao.selectWhseMerch(whseMerchParam);
	}
	
	@Override
	public Map<String, Object> searchMerchWhseQuantityAndAmountByKind(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchMerchWhseQuantityAndAmountByKind paramMap: " + paramMap);
		paramMap = MapUtil.remain(paramMap, "merch_id", "item_kind_id");
		if(paramMap.get("item_kind_id")==null || "".equals(paramMap.get("item_kind_id"))) paramMap = MapUtil.filter(paramMap, "item_kind_id");
		List<Map<String, Object>> whseInfo = whseDao.searchMerchWhseQuantityAndAmountByKind(paramMap);
		if(whseInfo.size()>1) {
			Map<String, Object> whseInfoTotal = new HashMap<String, Object>();
			BigDecimal whseQuantity = BigDecimal.ZERO;
			BigDecimal whseAmount = BigDecimal.ZERO;
			for(Map<String, Object> whse : whseInfo) {
				whseQuantity = whseQuantity.add(MapUtil.getBigDecimal(whse, "whse_quantity"));
				whseAmount = whseAmount.add(MapUtil.getBigDecimal(whse, "whse_amount"));
			}
			whseInfoTotal.put("whse_quantity", whseQuantity);
			whseInfoTotal.put("whse_amount", whseAmount);
			whseInfo.add(0, whseInfoTotal);
		}
		return whseInfo.isEmpty() ? Collections.EMPTY_MAP : whseInfo.get(0);
	}
	
	/**
	 * 从merch_item_unit查到merch_item再查到whse_merch, 认为是用seq_id或者big_bar查询的,所以只有一个item_id
	 */
	@Override
	public List<Map<String, Object>> searchMerchItemUnitJoinMerchItemJoinWhseMerch(Map<String, Object> paramMap)throws Exception {
		LOG.debug("WhseServiceImpl searchMerchItemUnitJoinMerchItemJoinWhseMerch paramMap: "+ paramMap);
		List<Map<String, Object>> merchItemUnitList = itemDao.selectMerchItemUnit(paramMap);
		Set<String> merchItemUnitItemIdSet = new HashSet<String>(); // 从item_unit的结果集中去重item_id
		for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
			merchItemUnitItemIdSet.add((String) merchItemUnitMap.get("item_id"));
		}
		Map<String, Map<String, Object>> merchItemListMap = new HashMap<String, Map<String, Object>>(); // merch_item结果集
		Map<String, Map<String, Object>> whseMerchListMap = new HashMap<String, Map<String, Object>>(); // whse_merch结果集
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", paramMap.get("merch_id"));
		merchItemParam.put("page_index",paramMap.get("page_index"));
		merchItemParam.put("page_size",paramMap.get("page_size"));
		if(paramMap.get("status")!=null) merchItemParam.put("status", paramMap.get("status")); // 不传状态会查出已经删除的商品
		if(paramMap.get("item_kind_id")!=null) merchItemParam.put("item_kind_id", paramMap.get("item_kind_id")); // 库存盘点查询用
		for(String itemId : merchItemUnitItemIdSet) {
			merchItemParam.put("item_id", itemId);
			for(Map<String, Object> merchItemMap : itemDao.selectMerchItem(merchItemParam)) {
				merchItemListMap.put((String)merchItemMap.get("item_id"), merchItemMap);
			}
			for(Map<String, Object> whseMerchMap : whseDao.selectWhseMerch(merchItemParam)) {
				whseMerchListMap.put((String)whseMerchMap.get("item_id"), whseMerchMap);
			}
		}
		List<Map<String, Object>> merchItemJoinUnitJoinWhseList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
			String itemId = (String) merchItemUnitMap.get("item_id");
			if(merchItemListMap.containsKey(itemId) && whseMerchListMap.containsKey(itemId)) {
				merchItemUnitMap.put("spec", MapUtil.getString(merchItemListMap.get(itemId), "spec"));
				merchItemUnitMap.put("big_pri4", merchItemUnitMap.get("pri4"));
				merchItemUnitMap.put("big_unit_ratio", merchItemUnitMap.get("unit_ratio"));
				merchItemUnitMap.put("item_name", merchItemListMap.get(itemId).get("item_name"));
				merchItemUnitMap.put("item_kind_id", merchItemListMap.get(itemId).get("item_kind_id"));
				merchItemUnitMap.put("pri4", merchItemListMap.get(itemId).get("pri4"));
				merchItemUnitMap.put("pri1", merchItemListMap.get(itemId).get("pri1"));
				merchItemUnitMap.put("cost", merchItemListMap.get(itemId).get("cost"));
				merchItemUnitMap.put("status", merchItemListMap.get(itemId).get("status"));
				merchItemUnitMap.put("unit_name", merchItemListMap.get(itemId).get("unit_name"));
//				merchItemUnitMap.putAll(MapUtil.filter(merchItemListMap.get(itemId), "big_pri4", "big_unit_name", "big_bar"));
				merchItemUnitMap.putAll(whseMerchListMap.get(itemId));
				merchItemJoinUnitJoinWhseList.add(merchItemUnitMap);
			}
		}
		return merchItemJoinUnitJoinWhseList;
	}
	
	/**
	 * 从merch_item_unit查到merch_item再查到whse_merch, 认为是用seq_id或者big_bar查询的,所以只有一个item_id
	 * 用于库存盘点
	 */
	@Override
	public List<Map<String, Object>> searchMerchItemUnitJoinMerchItemJoinWhseMerchForWhse(Map<String, Object> merchItemUnitJoinMerchItemJoinWhseMerchParam)throws Exception {
		LOG.debug("WhseServiceImpl searchMerchItemUnitJoinMerchItemJoinWhseMerch merchItemUnitJoinMerchItemJoinWhseMerchParam: "+ merchItemUnitJoinMerchItemJoinWhseMerchParam);
		
		List<Map<String, Object>> itemAndUnitList = whseDao.selectNoRemovedItem(merchItemUnitJoinMerchItemJoinWhseMerchParam);
		if(!itemAndUnitList.isEmpty()){
			return itemAndUnitList;
		}else{
			return whseDao.selectRemovedItem(merchItemUnitJoinMerchItemJoinWhseMerchParam);
		}
		//此处为原来做法,可以删除
		/*List<Map<String, Object>> merchItemUnitList = itemDao.selectMerchItemUnit(merchItemUnitJoinMerchItemJoinWhseMerchParam);
		Set<String> merchItemUnitItemIdSet = new HashSet<String>(); // 从item_unit的结果集中去重item_id
		for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
			merchItemUnitItemIdSet.add((String) merchItemUnitMap.get("item_id"));
		}
		Map<String, Map<String, Object>> merchItemListMap = new HashMap<String, Map<String, Object>>(); // merch_item结果集
		Map<String, Map<String, Object>> whseMerchListMap = new HashMap<String, Map<String, Object>>(); // whse_merch结果集
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", merchItemUnitJoinMerchItemJoinWhseMerchParam.get("merch_id"));
		merchItemParam.put("page_index",merchItemUnitJoinMerchItemJoinWhseMerchParam.get("page_index"));
		merchItemParam.put("page_size",merchItemUnitJoinMerchItemJoinWhseMerchParam.get("page_size"));
		if(merchItemUnitJoinMerchItemJoinWhseMerchParam.get("status") != null){
			merchItemParam.put("status", merchItemUnitJoinMerchItemJoinWhseMerchParam.get("status")); // 不传状态会查出已经删除的商品
		}
		
		if(merchItemUnitJoinMerchItemJoinWhseMerchParam.get("item_kind_id") != null){
			merchItemParam.put("item_kind_id", merchItemUnitJoinMerchItemJoinWhseMerchParam.get("item_kind_id")); // 库存盘点查询用
		}

		for(String itemId : merchItemUnitItemIdSet) {
			merchItemParam.put("item_id", itemId);
			for(Map<String, Object> merchItemMap : itemDao.selectMerchItem(merchItemParam)) {
				merchItemListMap.put((String)merchItemMap.get("item_id"), merchItemMap);
			}
			for(Map<String, Object> whseMerchMap : whseDao.selectWhseMerch(merchItemParam)) {
				whseMerchListMap.put((String)whseMerchMap.get("item_id"), whseMerchMap);
			}
		}
		List<Map<String, Object>> merchItemJoinUnitJoinWhseList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
			
			String itemId = (String) merchItemUnitMap.get("item_id");
			if(merchItemListMap.containsKey(itemId) && whseMerchListMap.containsKey(itemId)) {
				BigDecimal unitRatio = MapUtil.getBigDecimal(merchItemListMap.get(itemId), "unit_ratio", new BigDecimal("1"));
				merchItemUnitMap.put("item_bar", merchItemListMap.get(itemId).get("big_bar"));
				merchItemUnitMap.put("pri4", merchItemListMap.get(itemId).get("big_pri4"));
				merchItemUnitMap.put("unit_name", merchItemListMap.get(itemId).get("big_unit_name"));
				merchItemUnitMap.put("big_pri4", merchItemUnitMap.get("pri4"));
				merchItemUnitMap.put("big_unit_ratio", merchItemUnitMap.get("unit_ratio"));
				merchItemUnitMap.put("item_name", merchItemListMap.get(itemId).get("item_name"));
				merchItemUnitMap.put("item_kind_id", merchItemListMap.get(itemId).get("item_kind_id"));
				merchItemUnitMap.put("unit_ratio", unitRatio);
				merchItemUnitMap.put("pri1", merchItemListMap.get(itemId).get("pri1"));
				merchItemUnitMap.put("cost", MapUtil.getBigDecimal(merchItemListMap.get(itemId), "cost").multiply(unitRatio));
				merchItemUnitMap.put("status", merchItemListMap.get(itemId).get("status"));
				
//				merchItemUnitMap.putAll(MapUtil.filter(merchItemListMap.get(itemId), "big_pri4", "big_unit_name", "big_bar"));
				merchItemUnitMap.putAll(whseMerchListMap.get(itemId));
				merchItemUnitMap.put("qty_whse", MapUtil.getBigDecimal(whseMerchListMap.get(itemId),"qty_whse").divide(unitRatio));
				merchItemUnitMap.put("qty_whse_warn", MapUtil.getBigDecimal(whseMerchListMap.get(itemId),"qty_whse_warn").divide(unitRatio));
				merchItemJoinUnitJoinWhseList.add(merchItemUnitMap);
			}
		}
		return merchItemJoinUnitJoinWhseList;*/
	}
	
	
	/*@Override 此方法废弃,可删除
	public List<Map<String, Object>> searchMerchItemJoinWhseMerchByLucene(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl searchMerchItemJoinWhseMerchByLucene paramMap: "+ paramMap);
		List<Map<String, Object>> itemList = itemService.searchMerchItemByLucene(paramMap);
		if(itemList.isEmpty()) {
			return itemList;
		}
		Map<String, Map<String, Object>> itemListMap = new HashMap<String, Map<String, Object>>();
		for(Map<String, Object> itemMap : itemList) {
			itemListMap.put(itemMap.get("item_id").toString(), itemMap);
		}
		Map<String, Object> whseParam = new HashMap<String, Object>();
		whseParam.put("merch_id", paramMap.get("merch_id"));
		whseParam.put("item_id", paramMap.get("item_id"));
		whseParam.put("status", "1");
		whseParam.put("page_index", -1);
		whseParam.put("page_size", -1);
		List<Map<String, Object>> whseList = whseDao.selectWhseMerch(whseParam);
//		BigDecimal unitRatio = new BigDecimal("1");
		Map<String, Object> itemMap = null;
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> whseMap : whseList) {
			String itemId = whseMap.get("item_id").toString();
//			BigDecimal unitRatio = MapUtil.getBigDecimal(itemListMap.get(itemId), "unit_ratio", new BigDecimal("1"));
			LOG.debug("for start-----itemId:"+itemId);
			itemMap = itemListMap.get(itemId);
			
			if(itemMap!=null){
				LOG.debug("-----itemMap:"+itemMap);
				whseMap.putAll(itemMap);
				LOG.debug("-----whseMap:"+whseMap);
				BigDecimal unitRatio = MapUtil.getBigDecimal(itemMap, "unit_ratio", new BigDecimal("1") );
				whseMap.put("cost", MapUtil.getBigDecimal(itemListMap.get(itemId), "cost").multiply(unitRatio));
				whseMap.put("item_bar", itemListMap.get(itemId).get("big_bar"));
				whseMap.put("pri4", itemListMap.get(itemId).get("big_pri4"));
				whseMap.put("qty_whse", MapUtil.getBigDecimal(whseMap, "qty_whse").divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
				whseMap.put("unit_name", itemListMap.get(itemId).get("big_unit_name"));
				whseMap.put("qty_whse_warn", MapUtil.getBigDecimal(whseMap, "qty_whse_warn").divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
				dataList.add(whseMap);
				LOG.debug("for end-----whseMap:"+whseMap);
			}
		}
//		paramMap.put("page_count", paramMap.get("page_count"));
		paramMap.put("count", dataList.size());
		return whseList;
	}*/
	

	@Override
	public List<Map<String, Object>> findMerchItemAndWhseMerchByLucene(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl findMerchItemAndWhseMerchByLucene paramMap: "+ paramMap);
		Map<String, Object> itemMap = itemService.findMerchItemByLucene(paramMap);
		if(itemMap.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}
		//保留原来做法
//		List<Map<String, Object>> whseList = whseDao.selectWhseMerchAndMerchItem(paramMap);
		List<Map<String, Object>> whseList = whseDao.selectNotRemovedWhseMerchAndMerchItem(paramMap);
		if(whseList.isEmpty()){
			whseList = whseDao.selectRemovedWhseMerchAndMerchItem(paramMap);
		}
		
		for (Map<String, Object> itemWhseMap : whseList) {
			BigDecimal unitRatio = MapUtil.getBigDecimal(itemWhseMap, "unit_ratio", new BigDecimal("1"));
			itemWhseMap.put("cost", MapUtil.getBigDecimal(itemWhseMap, "cost").multiply(unitRatio));
			itemWhseMap.put("item_bar", MapUtil.getString(itemWhseMap, "big_bar"));
			itemWhseMap.put("pri4", MapUtil.getString(itemWhseMap, "big_pri4"));
			BigDecimal qtyWhse = MapUtil.getBigDecimal(itemWhseMap, "qty_whse").divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP);
			if (WhseServiceImpl.showPositiveWhse && qtyWhse.compareTo(BigDecimal.ZERO)<0) {
				qtyWhse = BigDecimal.ZERO;
			}
			itemWhseMap.put("qty_whse", qtyWhse);
			itemWhseMap.put("unit_name", MapUtil.getString(itemWhseMap, "big_unit_name"));
			itemWhseMap.put("qty_whse_warn", MapUtil.getBigDecimal(itemWhseMap, "qty_whse_warn").divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
		}
		return whseList;
	}
	
	
	//合理库存搜索查询（建议合理库存量）
	@Override
	public List<Map<String, Object>> searchMerchItemJoinAdvWhseByLucene(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl searchMerchItemJoinAdvWhseByLucene paramMap: "+ paramMap);
		//保留原来做法
//		List<Map<String, Object>> itemList = itemService.searchMerchItemByLucene(paramMap);
		
		Map<String, Object> merchItemMap = itemService.findMerchItemByLucene(paramMap);
		List<Map<String, Object>> itemList = itemDao.selectNotRemovedMerchItem(merchItemMap);
		if(itemList.isEmpty()){
			itemList = itemDao.selectRemovedMerchItem(merchItemMap);
		}
		if(itemList.isEmpty()) {
			return itemList;
		}
		
		Map<String, Map<String, Object>> itemListMap = new HashMap<String, Map<String, Object>>();
		for(Map<String, Object> itemMap : itemList) {
			itemListMap.put(itemMap.get("item_id").toString(), itemMap);
		}
		Map<String, Object> whseParam = new HashMap<String, Object>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		whseParam.put("merch_id", merchId);
		whseParam.put("item_id", paramMap.get("item_id"));
		whseParam.put("status", "1");
		whseParam.put("page_index", -1);
		whseParam.put("page_size", -1);
		List<Map<String, Object>> whseList = whseDao.selectWhseMerch(whseParam);
		BigDecimal newWhseQty = BigDecimal.ZERO;
		BigDecimal newQtyWhseWarn = BigDecimal.ZERO;
//		BigDecimal unitRatio = new BigDecimal("1");
		for(Map<String, Object> whseMap : whseList) {
			String itemId = MapUtil.getString(whseMap, "item_id"); 
			if(itemListMap.containsKey(itemId)){
				itemListMap.get(itemId).putAll(whseMap);
				BigDecimal unitRatio = MapUtil.getBigDecimal(itemListMap.get(itemId), "unit_ratio", new BigDecimal("1"));//转换比
				newWhseQty = MapUtil.getBigDecimal(whseMap, "qty_whse").divide(unitRatio);//包装库存
				if (newWhseQty.compareTo(BigDecimal.ZERO)<0 && WhseServiceImpl.showPositiveWhse) {
					newWhseQty = BigDecimal.ZERO;
				}
				newQtyWhseWarn = MapUtil.getBigDecimal(whseMap, "qty_whse_warn").divide(unitRatio);//合理库存
				itemListMap.get(itemId).put("pri1", MapUtil.getBigDecimal(itemListMap.get(itemId), "pri1").multiply(unitRatio));
				itemListMap.get(itemId).put("qty_whse", newWhseQty);
				itemListMap.get(itemId).put("qty_whse_warn", newQtyWhseWarn);
				itemListMap.get(itemId).put("pri4", itemListMap.get(itemId).get("big_pri4"));
				itemListMap.get(itemId).put("unit_name", itemListMap.get(itemId).get("big_unit_name"));
				itemListMap.get(itemId).put("item_bar", itemListMap.get(itemId).get("big_bar"));
				LOG.debug("---itemId:"+itemId);
				LOG.debug("---whseMap:"+whseMap);
				LOG.debug("----itemlistMap:"+itemListMap.get(itemId));
				LOG.debug("1");
			}
		}
		
		Map<String,String> AdvWhseParamMap=new HashMap<String,String>();
		AdvWhseParamMap.put("cust_id", MapUtil.getString(paramMap, "ref_id"));
		if(whseList.size()==1){
			AdvWhseParamMap.put("item_id", MapUtil.getString(whseList.get(0), "item_id"));
		}
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "reasonWhse/getReasonWhseList";
		String json = HttpUtil.post(cgtItemUrl, AdvWhseParamMap);
		Map<String, Object> AdvWhseMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			AdvWhseMap =JsonUtil.json2Map(json);
		}
		List<Map<String, Object>> AdvWhseResultList=new ArrayList<Map<String, Object>>();
		if("0000".equals(MapUtil.getString(AdvWhseMap, "code", "1000"))){
			if(MapUtil.getString(AdvWhseMap, "result", null)!=null){
				AdvWhseResultList=(List<Map<String, Object>>)AdvWhseMap.get("result");
			}
		}
		String advItemId = null;
		List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
		BigDecimal advWhse = BigDecimal.ZERO;
		for (Map<String, Object> map : AdvWhseResultList) {
			advItemId = MapUtil.getString(map, "ITEM_ID");
			advWhse = MapUtil.getBigDecimal(map, "ADV_WHSE"); 
			if(itemListMap.containsKey(advItemId)){
				itemListMap.get(advItemId).put("adv_whse", advWhse);
				returnList.add(itemListMap.get(advItemId));
			}
		}
		return returnList;
	}
	
	/**
	 * 更新whse_merch表, 支持批量
	 */
	@Override
	public void modifyWhseMerch(Map<String, Object> paramMap)throws Exception {
		LOG.debug("WhseServiceImpl modifyWhseMerch paramMap: "+ paramMap);
		whseDao.updateWhseMerch(paramMap);
	}

	/**
	 * 批量更新whse_merch表
	 */
	@Override
	public void modifyBatchWhseMerch(List<Map<String, Object>> whseMerchParamList) throws Exception {
		LOG.debug("WhseServiceImpl modifyBatchWhseMerch whseMerchParamList: "+ whseMerchParamList);
		whseDao.updateBatchWhseMerch(whseMerchParamList);
	}

	@Override
	public List getOriginWhseList(Map map) throws Exception {
		List list = null;
		LOG.debug("WhseServiceImpl getOriginWhseList map=" + map);
//		String merchId = (String) map.get("refId");
		String itemBar = (String) map.get("itemBar");
		if (itemBar != null && !itemBar.equals("")) {
			list = whseDao.getOriginWhseLists(map);
		} else {
			list = whseDao.getOriginWhseList(map);
		}
		return list;
	}

	@Override
	public void updateOriginWhseList(Map map) throws Exception {

		LOG.debug("WhseServiceImpl getOriginWhseList map=" + map);
		whseDao.updateOriginWhseList(map);
	}
	
	public List<Map<String, Object>> searchWhseMerchJoinMerchItemUnit(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> unitParam = new HashMap<String, Object>();
		unitParam.put("merch_id", paramMap.get("merch_id"));
		unitParam.put("big_bar", paramMap.get("big_bar"));
		unitParam.put("status", "1,2");
		unitParam.put("page_index", -1);
		unitParam.put("page_size", -1);
		List<Map<String, Object>> itemList = itemService.searchMerchItemUnitJoinMerchItem(unitParam);
		Set<String> itemIdSet = new HashSet<String>();
		StringBuffer itemIdBuffer = new StringBuffer();
		for(Map<String, Object> itemMap : itemList) {
			itemIdSet.add(itemMap.get("item_id").toString());
		}
		for(String itemId : itemIdSet) {
			if(itemIdBuffer.length()==0) itemIdBuffer.append(itemId);
			else itemIdBuffer.append(","+itemId);
		}
		return null;
	}
	
	@Override
	public List<Map<String, Object>> selectNotRemovedMerchItemAndWhse(Map<String,Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl selectNotRemovedMerchItemAndWhse paramMap: " + paramMap);
		return whseDao.selectNotRemovedMerchItemAndWhse(paramMap);
	}
	
	@Override
	public List<Map<String, Object>> searchMerchItemAndReasonWhse(Map<String,Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl searchMerchItemAndWhse paramMap: " + paramMap);
		//备份原来做法
//		List<Map<String,Object>> list = whseDao.searchMerchItemAndWhse(paramMap);
		List<Map<String,Object>> list = this.selectNotRemovedMerchItemAndWhse(paramMap);
		if(list.isEmpty()){
			list = whseDao.selectRemovedMerchItemAndWhse(paramMap);
		}
		
		//获取卷烟系统服务地址
		Map<String,String> postParam=new HashMap<String,String>();
		postParam.put("cust_id", MapUtil.getString(paramMap, "ref_id"));
		if(list.size()==1){
			postParam.put("item_id", MapUtil.getString(list.get(0), "item_id"));
		}
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "reasonWhse/getReasonWhseList";
		
		//从卷烟系统服务上获取数据
		String json = HttpUtil.post(cgtItemUrl, postParam);
		LOG.debug("获取建议合理库存: " + json);
		Map<String, Object> itemMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			itemMap =JsonUtil.json2Map(json);
		}
		List<Map<String, Object>> itemMapList=new ArrayList<Map<String, Object>>();
		if("0000".equals(MapUtil.getString(itemMap, "code", "1000"))){
			if(MapUtil.getString(itemMap, "result", null)!=null){
				itemMapList=(List<Map<String, Object>>)itemMap.get("result");
			}
		}
		
		Map<String,Object> xianLiangMap=new HashMap<String,Object>();
		if(itemMapList!=null && itemMapList.size()>0){
			for(int i=0;i<itemMapList.size();i++){
				int advWhse= MapUtil.getInt(itemMapList.get(i), "ADV_WHSE");
				xianLiangMap.put(MapUtil.getString(itemMapList.get(i), "ITEM_ID"),advWhse);
			}
		}
		
		if(!list.isEmpty()){
			for(int i=0;i<list.size();i++){
				if (MapUtil.getBigDecimal(list.get(i), "qty_whse").compareTo(BigDecimal.ZERO)<0 && WhseServiceImpl.showPositiveWhse) {
					list.get(i).put("qty_whse", "0");
				}
				if(xianLiangMap.containsKey(list.get(i).get("item_id"))){
					list.get(i).put("adv_whse", xianLiangMap.get(list.get(i).get("item_id")));
				}else{
					list.get(i).put("adv_whse", "0");
				}
				
			}
		}
		return list;
	}
	
//	@Override
	public List<Map<String, Object>> findUnqualifiedMerchantInventoryList(Map<String,Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl searchMerchItemAndWhse paramMap: " + paramMap);
		//备份原来做法
//		List<Map<String,Object>> list = whseDao.searchMerchItemAndWhse(paramMap);
		List<Map<String,Object>> list = this.selectNotRemovedMerchItemAndWhse(paramMap);
		if(list.isEmpty()){
			list = whseDao.selectRemovedMerchItemAndWhse(paramMap);
		}
		
		//获取卷烟系统服务地址
		Map<String,String> postParam=new HashMap<String,String>();
		postParam.put("cust_id", MapUtil.getString(paramMap, "ref_id"));
		if(list.size()==1){
			postParam.put("item_id", MapUtil.getString(list.get(0), "item_id"));
		}
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "reasonWhse/getReasonWhseList";
		
		//从卷烟系统服务上获取数据
		String json = HttpUtil.post(cgtItemUrl, postParam);
		LOG.debug("获取建议合理库存: " + json);
		Map<String, Object> itemMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			itemMap =JsonUtil.json2Map(json);
		}
		List<Map<String, Object>> itemMapList=new ArrayList<Map<String, Object>>();
		if("0000".equals(MapUtil.getString(itemMap, "code", "1000"))){
			if(MapUtil.getString(itemMap, "result", null)!=null){
				itemMapList=(List<Map<String, Object>>)itemMap.get("result");
			}
		}
		
		Map<String,Object> xianLiangMap=new HashMap<String,Object>();
		if(itemMapList!=null && itemMapList.size()>0){
			for(int i=0;i<itemMapList.size();i++){
				int advWhse= MapUtil.getInt(itemMapList.get(i), "ADV_WHSE");
				xianLiangMap.put(MapUtil.getString(itemMapList.get(i), "ITEM_ID"),advWhse);
			}
		}
		
		if(!list.isEmpty()){
			for(int i=0;i<list.size();i++){
				if (MapUtil.getBigDecimal(list.get(i), "qty_whse").compareTo(BigDecimal.ZERO)<0 && WhseServiceImpl.showPositiveWhse) {
					list.get(i).put("qty_whse", "0");
				}
				if(xianLiangMap.containsKey(list.get(i).get("item_id"))){
					list.get(i).put("adv_whse", xianLiangMap.get(list.get(i).get("item_id")));
				}else{
					list.get(i).put("adv_whse", "0");
				}
				
			}
		}
		
		return list;
	}


	@Override
	public void modifyWhseWarnQuantity(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl modifyWhseWarnQuantity paramMap:" + paramMap);
		whseDao.updateWhseWarnQuantity(paramMap);
		
		List<Map<String,String>> updateMapList = MapUtil.get(paramMap, "list", Collections.EMPTY_LIST);
		for(int i=0;i<updateMapList.size();i++){
			double renWhse=MapUtil.getDouble(updateMapList.get(i), "qty_whse_warn", 0);
			updateMapList.get(i).put("rsn_whse",(renWhse/10)+"");
			updateMapList.get(i).put("RSN_WHSE",(renWhse/10)+"");
			updateMapList.get(i).put("ITEM_ID", updateMapList.get(i).get("item_id"));
			updateMapList.get(i).remove("qty_whse_warn");
		}

		Map<String,Object> updateMap=new HashMap<String,Object>();
		updateMap.put("list", updateMapList);
		
		Map<String,String> updateMapData=new HashMap<String,String>();
		updateMapData.put("cust_id", paramMap.get("merch_id").toString());
		updateMapData.put("params", JsonUtil.map2json(updateMap));
		
		//获取卷烟系统服务地址
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "reasonWhse/updateReasonWhse";
//		String cgtItemUrl ="http://192.168.0.112:8080/tobasrv/reasonWhse/updateReasonWhse";
		//从卷烟系统服务上获取数据
		try{
		String json = HttpUtil.post(cgtItemUrl, updateMapData);
			LOG.debug("调用修改合理库存远程服务器返回数据：" +json);
		}catch(Exception e){
			LOG.debug("调用远程服务器修改合理库存：" + e);
		}
	}

	@Override
	public void createWhseTurn(Map<String, Object> turnParam) throws Exception {
		LOG.debug("WhseServiceImpl createWhseTurn turnParam: " + turnParam);
		List<Map<String, Object>> turnLineList = MapUtil.get(turnParam, "list", Collections.EMPTY_LIST);
		if(turnLineList.isEmpty()) throw new RuntimeException("盘点单数据格式错误：没有盘点单明细商品");
		BigDecimal amtProfit = BigDecimal.ZERO;
		BigDecimal amtLoss = BigDecimal.ZERO;
		BigDecimal qtyProfit = BigDecimal.ZERO;
		BigDecimal qtyLoss = BigDecimal.ZERO;
		StringBuilder allItemName = new StringBuilder();
		int turnLineListSize = turnLineList.size();
		int i = 1;
		for(Map<String, Object> turnLineMap : turnLineList) {
			allItemName.append(MapUtil.getString(turnLineMap, "item_name"));
			if(i!=turnLineListSize){
				allItemName.append(",");
			}
			BigDecimal cost = MapUtil.getBigDecimal(turnLineMap, "cost");
			BigDecimal unitRatio = MapUtil.getBigDecimal(turnLineMap, "unit_ratio", BigDecimal.ONE);
			BigDecimal qtyPL = MapUtil.getBigDecimal(turnLineMap, "qty_pl").setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal amtPL = qtyPL.multiply(cost).divide(unitRatio, 2, BigDecimal.ROUND_HALF_UP);
			if(qtyPL.compareTo(BigDecimal.ZERO)>0) {
				amtProfit = amtProfit.add(amtPL);
				qtyProfit = qtyProfit.add(qtyPL);
			} else {
				amtLoss = amtLoss.add(amtPL);
				qtyLoss = qtyLoss.add(qtyPL);
			}
			turnLineMap.put("amt_pl", amtPL);
			i++;
		}
		if(allItemName.length()>500){
			allItemName = new StringBuilder(allItemName.substring(0, 500)).append("...");
		}
		turnParam.put("note", allItemName.toString());
		turnParam.put("amt_profit", amtProfit.setScale(2, BigDecimal.ROUND_HALF_UP));
		turnParam.put("amt_loss", amtLoss.setScale(2, BigDecimal.ROUND_HALF_UP));
		turnParam.put("qty_profit", qtyProfit.setScale(2, BigDecimal.ROUND_HALF_UP));
		turnParam.put("qty_loss", qtyLoss.setScale(2, BigDecimal.ROUND_HALF_UP));
		this.insertWhseTurn(turnParam);
		this.insertWhseTurnLine(turnParam);
		// 为了修改库存
		for(Map<String, Object> turnLineMap : turnLineList) {
			turnLineMap.put("qty_whse", MapUtil.get(turnLineMap, "qty_turn", 0));
		}
		whseDao.updateWhseMerch(turnParam);
		
		// 原代码判断turnParam中的order_type!="04"时向rtms推送数据, 但是参数中并没有order_type字段, 故删除此代码
		if(!"04".equals(turnParam.get("order_type"))){
			uploadDataToRtmsService.insertWhseTobaccoToRTMS(turnParam);
		}else if("03".equals((String)turnParam.get("status"))){
			uploadDataToRtmsService.insertWhseTobaccoToRTMS(turnParam);
		}
		
		
	}

	@Override
	public List getTakeStock(Map map) throws Exception {

		List list = whseDao.getTakeStock(map);
		return list;

	}
	public List getWhseTurn(Map map ) throws Exception {
		LOG.debug("WhseServiceImpl getWhseTurn map:"+map);
		List list = whseDao.getWhseTurn(map);
		return list;
	}

	@Override
	public List getTakeStockXiangXi(Map map) throws Exception {
		String merchId = (String) map.get("refId");
		String beginTime = (String) map.get("beginTime");
		String endTime = (String) map.get("endTime");
		String itemId = (String) map.get("itemId");
		List list = whseDao.getTakeStockXiangXi(itemId, merchId, beginTime,endTime);
		return list;
	}
	//盘点记录详情
	@Override
	public List getWhseTurnXiangXi(Map map ) throws Exception {
		LOG.debug("WhseServiceImp getTakeStockXiangXi map:"+map);
		return whseDao.getTakeStockXiangXi(map);
	}
	//盘点记录
	@Override
	public void insertWhseMerch(Map<String, Object> whseParams)throws Exception {
		LOG.debug("WhseServiceImpl insertWhseMerch whseParams: "+ whseParams);
		whseDao.insertWhseMerch(whseParams);
	}

	@Override
	public void updateWhseMerch(Map<String, Object> whseParam) throws Exception {
		LOG.debug("WhseServiceImpl updateWhseMerch whseParams: " + whseParam);
		List<Map<String, Object>> whseParamList = (List<Map<String, Object>>) whseParam.get("list");
		whseDao.updateWhseMerchSimple(whseParamList);
	}
	@Override
	public Object modifyWhseByTBCSVR(Map<String, Object> paramMap)throws Exception {
		// TODO Auto-generated method stub
		LOG.debug("===updateWhseByTobaccoServerGetWhseQty paramMap:"+paramMap);
		//tobacco service map参数
		Map<String, String> searchItemWhseParams=new HashMap<String, String>();
		searchItemWhseParams.put("cust_id", paramMap.get("merch_id").toString());
		Object creaed= paramMap.get("created");
		if(creaed!=null){
			searchItemWhseParams.put("created",creaed.toString());
		}
		LOG.debug("===updateWhseByTobaccoServerGetWhseQty searchItemWhseParams:"+searchItemWhseParams);
		
//		String cgtItemUrl = RetailConfig.getTobaccoServer() + "uploadData/getWhseQty";
		String cgtItemUrl ="http://192.168.0.190:8080/tobasrv/retail/uploadData/getWhseQty";
		LOG.debug("===updateWhseByTobaccoServerGetWhseQty cgtItemUrl:"+cgtItemUrl);
		//得到tobaccoservice的json
		String whseJson=HttpUtil.post(cgtItemUrl, searchItemWhseParams);
		LOG.debug("===updateWhseByTobaccoServerGetWhseQty whseJson:"+whseJson);
		
		//将json转为map
		Map<String, Object>tobaccoSrvJsonMap=new HashMap<String, Object>();
		//得到json.result
		List<Map<String, Object>> tobaccoSrvWhseList=new ArrayList<Map<String,Object>>();
		if(!StringUtils.isEmpty(whseJson) ){
			tobaccoSrvJsonMap=JsonUtil.json2Map(whseJson);//将json转为map
		}
		if("0000".equals(MapUtil.getString(tobaccoSrvJsonMap, "code", "1000"))){
			if(tobaccoSrvJsonMap.containsKey("result")&&tobaccoSrvJsonMap.get("result")!=null){
				//得到json.result
				tobaccoSrvWhseList=(List<Map<String, Object>>) tobaccoSrvJsonMap.get("result");
			}
		}else{
			return MapUtil.getString(tobaccoSrvJsonMap, "code", "1000");
		}
		List<Map<String, Object>> oldItemWhseList=  whseDao.searchMerchItemAndWhse(paramMap);//得到tobacco的库存
		//将tobacco库存转为map<list>
		Map<String, Map<String, Object>> oldItemWhseMap=new HashMap<String, Map<String,Object>>();
		//遍历tobacco库存
		for (Map<String, Object> map : oldItemWhseList) {
			oldItemWhseMap.put(map.get("item_id").toString(), map);
		}
		String itemId="";//编号
		Map<String, Object> upItemWhse=new HashMap<String, Object>();
		List<Map<String, Object>> upItemWhseList=new ArrayList<Map<String,Object>>(); 
		Map<String, Object> upItemWhseMap=null;
		BigDecimal newWhseQty=BigDecimal.ZERO;//新库存量
		BigDecimal oldWhseQty=BigDecimal.ZERO;//旧库存量
		BigDecimal plWhseQty=BigDecimal.ZERO;//损益库存量
		BigDecimal cost=BigDecimal.ZERO;//cost
		for (Map<String, Object> map : tobaccoSrvWhseList) {
			itemId=(String) map.get("ITEM_ID");
			if(oldItemWhseMap.containsKey(itemId)){
				upItemWhseMap=new HashMap<String, Object>();
				//新库存
				newWhseQty=MapUtil.getBigDecimal(map, "QTY", BigDecimal.ZERO);
				//cost
				cost=MapUtil.getBigDecimal(oldItemWhseMap.get(itemId),"cost", BigDecimal.ZERO);
				//旧库存
				oldWhseQty=MapUtil.getBigDecimal(oldItemWhseMap.get(itemId), "qty_whse", BigDecimal.ZERO);
				//损益量
				plWhseQty=newWhseQty.subtract(oldWhseQty);
				
				if(newWhseQty.compareTo(oldWhseQty)==0){
					continue;
				}
				
				upItemWhseMap.put("qty_turn", newWhseQty);//新库存量（盘点后的）
				upItemWhseMap.put("qty_whse", oldWhseQty);//原库存量
				upItemWhseMap.put("qty_pl", plWhseQty);//损益量	
				upItemWhseMap.put("cost", cost);//成本
				upItemWhseMap.put("item_kind_id", "01");//分类
				upItemWhseMap.put("item_id", itemId);//编号
				upItemWhseMap.put("pl_reason", "");
				upItemWhseList.add(upItemWhseMap);
			}
		}
		upItemWhse.put("list", upItemWhseList);
		upItemWhse.put("refId", paramMap.get("merch_id").toString());
		String turnId=IDUtil.getId();
		String turnDate=DateUtil.getToday();
		String crtTime=DateUtil.getCurrentTimeMillisAsString("HHmmss");
		upItemWhse.put("turnId",turnId);
		upItemWhse.put("turnDate",turnDate);
		upItemWhse.put("crtTime",crtTime);
		this.createWhseTurn(upItemWhse);
		return "0000";
	}
	
	private void insertWhseTurn(Map<String, Object> turnParam) throws Exception {
		LOG.debug("WhseServiceImpl insertWhseTurn turnParam: " + turnParam);
		whseDao.insertWhseTurn(turnParam);
	}
	
	private void insertWhseTurnLine(Map<String, Object> turnLineParam) throws Exception {
		LOG.debug("WhseServiceImpl insertWhseTurnLine turnLineParam: " + turnLineParam);
		whseDao.insertWhseTurnLine(turnLineParam);
	}
	
	@Override
	public void updateShowWhseStatus(Map<String, Object> paramMap) throws Exception {
		LOG.debug("WhseServiceImpl updateShowWhseStatus paramMap: " + paramMap);
		 
		String status = MapUtil.getString(paramMap, "show_whse_status", "1");
		if (status.equals("0")) {
			WhseServiceImpl.showPositiveWhse = false;
		} else {
			WhseServiceImpl.showPositiveWhse = true;
		}
	}

}
