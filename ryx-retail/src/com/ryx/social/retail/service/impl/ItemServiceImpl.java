package com.ryx.social.retail.service.impl;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.SpellUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.IFileService;
import com.ryx.social.retail.service.IItemService;
import com.ryx.social.retail.service.IMerchService;
import com.ryx.social.retail.service.IWhseService;
import com.ryx.social.retail.util.ExcelUtil;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class ItemServiceImpl implements IItemService {
	
	private Logger LOG = LoggerFactory.getLogger(ItemServiceImpl.class); 
	
	@Resource
	private IItemDao itemDao;
	@Resource
	private IWhseDao whseDao;
	@Resource
	private IWhseService whseService;
	@Resource
	private IFileService fileService;
	@Resource
	private IMerchService merchService;

	/**
	 * 根据条码或者id查询base_item表, id一般不用
	 */
	@Override
	public List<Map<String, Object>> searchItem(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchItem itemParam: " + itemParam);
		return itemDao.selectItem(itemParam);
	}
	
	/**
	 * 获取商品单位信息
	 */
	@Override
	public List<Map<String, Object>> searchItemUnit(Map<String, Object> itemUnitParam) throws Exception {
		LOG.debug("ItemServiceImpl searchItemUnit itemUnitParam: " + itemUnitParam);
		return itemDao.selectItemUnit(itemUnitParam);
	}
	
	/**
	 * 获取商品分类信息
	 */
	@Override
	public List<Map<String, Object>> searchItemKind(Map<String, Object> itemKindParam) throws Exception {
		LOG.debug("ItemServiceImpl searchItemKind itemKindParam: " + itemKindParam);
		return itemDao.selectItemKind(itemKindParam);
	}
	
	/**
	 * 根据条码或者id查询商户商品信息
	 */
	@Override
	public List<Map<String, Object>> searchMerchItem(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItem merchItemParam: " + merchItemParam);
		return itemDao.selectMerchItem(merchItemParam);
	}
	
	@Override
	public List<Map<String, Object>> searchMerchItemUnit(Map<String, Object> unitParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemUnit unitParam: " + unitParam);
		return itemDao.selectMerchItemUnit(unitParam);
	}
	
	@Override
	public List<Map<String, Object>> searchMerchItemWithIncrease(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemWithIncrease merchItemParam: " + merchItemParam);
		// 必须有big_bar
		if(!merchItemParam.containsKey("bar")) {
			merchItemParam.put("bar", merchItemParam.get("big_bar"));
		}
		if(!merchItemParam.containsKey("item_bar")) {
			merchItemParam.put("item_bar", merchItemParam.get("big_bar"));
		}
//		List<Map<String, Object>> merchItemList = searchMerchItemAndSoOn(merchItemParam);
		List<Map<String, Object>> merchItemList = searchMerchItemBySaoMa(merchItemParam);//获得商户商品，或卷烟商品，或库中非烟商品
		if (!merchItemList.isEmpty()) {
			Map<String, Object> searchMerchItemParam = new HashMap<String, Object>();
			searchMerchItemParam.put("item_id", MapUtil.getString(merchItemList.get(0), "item_id"));
			searchMerchItemParam.put("merch_id", MapUtil.getString(merchItemParam, "merch_id"));
			List<Map<String, Object>> merchItem = itemDao.selectMerchItem(searchMerchItemParam);
			if (!merchItem.isEmpty() && "TOBACCO".equals(MapUtil.getString(merchItemList.get(0), "FLAG"))) {
				return new ArrayList<Map<String,Object>>();
			}
		}
		
		if(!merchItemList.isEmpty()) {
			for(Map<String, Object> merchItemMap : merchItemList) {
				// flag不可能同时包含多种取值, 如果查询出烟草商品只返回第一个
				if("TOBACCO".equals(merchItemMap.get("FLAG"))) {
					String itemBar = MapUtil.getString(merchItemMap, "item_bar");
					if(StringUtils.isBlank(itemBar)
							|| MapUtil.getString(merchItemMap, "item_id").equals(itemBar)){
						merchItemMap.put("unit_name", merchItemMap.get("big_unit_name"));
						merchItemMap.put("pri1", MapUtil.getBigDecimal(merchItemMap, "pri_wsale"));
						merchItemMap.put("pri4", MapUtil.getBigDecimal(merchItemMap, "pri_drtl"));
					} else {
						merchItemMap.put("unit_name", merchItemMap.get("item_unit_name"));
						merchItemMap.put("pri1", MapUtil.getBigDecimal(merchItemMap, "pri_wsale").divide(MapUtil.getBigDecimal(merchItemMap, "unit_ratio"),5,BigDecimal.ROUND_HALF_UP));
						merchItemMap.put("pri4", MapUtil.getBigDecimal(merchItemMap, "pri_drtl").divide(MapUtil.getBigDecimal(merchItemMap, "unit_ratio"),5,BigDecimal.ROUND_HALF_UP));
					}
					merchItemMap.put("merch_id", merchItemParam.get("merch_id"));
					merchItemMap.put("cost", merchItemMap.get("pri1"));
					List<Map<String, Object>> newMerchItemUnitList = packageTabaccoItem(merchItemMap);//组装数据
					
					merchItemMap.put("list", newMerchItemUnitList);
					this.createMerchItem(merchItemMap);
					return this.searchMerchItemUnitJoinMerchItem(merchItemParam);
				}
				// 如果是基础商品/商户商品表中的数据, 则全部返回
			}
		}
		return merchItemList;
	}
	
	@Override
	public List<Map<String, Object>> searchMerchAndTobaccoItemWithIncrease(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchAndTobaccoItemWithIncrease merchItemParam: " + merchItemParam);
		// 必须有big_bar
		if(!merchItemParam.containsKey("bar")) {
			merchItemParam.put("bar", merchItemParam.get("big_bar"));
		}
		if(!merchItemParam.containsKey("item_bar")) {
			merchItemParam.put("item_bar", merchItemParam.get("big_bar"));
		}
		List<Map<String, Object>> merchItemList = searchMerchAndTobaccoItem(merchItemParam);
		if(!merchItemList.isEmpty()) {
			for(Map<String, Object> merchItemMap : merchItemList) {
				// flag不可能同时包含多种取值, 如果查询出烟草商品只返回第一个
				if("TOBACCO".equals(merchItemMap.get("FLAG"))) {
					merchItemMap.put("merch_id", merchItemParam.get("merch_id"));
					merchItemMap.put("unit_name", merchItemMap.get("item_unit_name"));
					merchItemMap.put("pri1", MapUtil.getBigDecimal(merchItemMap, "pri_wsale")
							.divide(MapUtil.getBigDecimal(merchItemMap, "unit_ratio"),5,BigDecimal.ROUND_HALF_UP));
					merchItemMap.put("cost", merchItemMap.get("pri1"));
					merchItemMap.put("pri4", MapUtil.getBigDecimal(merchItemMap, "pri_drtl")
							.divide(MapUtil.getBigDecimal(merchItemMap, "unit_ratio"),5,BigDecimal.ROUND_HALF_UP));
					List<Map<String, Object>> newMerchItemUnitList = null;
					//组装数据
					newMerchItemUnitList=packageTabaccoItem(merchItemMap);
					
					merchItemMap.put("list", newMerchItemUnitList);
					this.createMerchItem(merchItemMap);
					return this.searchMerchItemUnitJoinMerchItem(merchItemParam);
				}
				// 如果是商户商品表中的数据, 则全部返回
			}
		}
		return merchItemList;
	}
	
	/**
	 * 商品管理-新增商品根据big_bar查询商品, 如果没有, 根据item_bar查询tobacco, 如果没有再查base_item, 如果都没有返回空list
	 */
	@Override
	public List<Map<String, Object>> searchMerchItemAndSoOn(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemAndSoOn paramMap: " + paramMap);
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", paramMap.get("merch_id"));
		merchItemParam.put("big_bar", paramMap.get("big_bar"));
		List<Map<String, Object>> merchItemList = searchMerchItemUnitJoinMerchItem(merchItemParam);
		paramMap.put("count", merchItemParam.get("count"));
		paramMap.put("page_count", merchItemParam.get("page_count"));
		if(!merchItemList.isEmpty()) {
			for(Map<String, Object> merchItemMap : merchItemList) {
				merchItemMap.put("FLAG", "MERCH");
			}
			return merchItemList;
		}
		Map<String, Object> tobaccoItemParam = new HashMap<String, Object>();
		tobaccoItemParam.put("supplier_id", MapUtil.getString(paramMap, "com_id", "10370101"));
		tobaccoItemParam.put("bar", paramMap.get("big_bar"));
		List<Map<String, Object>> tobaccoItemList = itemDao.selectTobaccoItem(tobaccoItemParam);
		if(!tobaccoItemList.isEmpty()) {
			for(Map<String, Object> tobaccoItemMap : tobaccoItemList) {
				tobaccoItemMap.put("FLAG", "TOBACCO");
				if(tobaccoItemMap.get("item_bar")==null || "".equals(tobaccoItemMap.get("item_bar"))) {
					tobaccoItemMap.put("item_bar", tobaccoItemMap.get("item_id"));
				}
			}
			return tobaccoItemList;
		}
		Map<String, Object> baseItemParam = new HashMap<String, Object>();
		baseItemParam.put("item_bar", paramMap.get("big_bar"));
		List<Map<String, Object>> itemList = itemDao.selectItem(baseItemParam);
		if(!itemList.isEmpty()) {
			for(Map<String, Object> itemMap : itemList) {
				itemMap.put("FLAG", "BASE");
			}
		}
		return itemList;
	}
	

	@Override
	public List<Map<String, Object>> searchMerchItemBySaoMa(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemBySaoMa paramMap: " + paramMap);
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", paramMap.get("merch_id"));
		merchItemParam.put("big_bar", paramMap.get("big_bar"));
		//查询商户商品（正常、下架、删除
		List<Map<String, Object>> itemAndUnitList = judgeItemStatus(merchItemParam);
		if(!itemAndUnitList.isEmpty()){
			paramMap.put("count", merchItemParam.get("count"));
			paramMap.put("page_count", merchItemParam.get("page_count"));
			return itemAndUnitList;
		}
	
		Map<String, Object> tobaccoItemParam = new HashMap<String, Object>();
		tobaccoItemParam.put("supplier_id", MapUtil.getString(paramMap, "com_id", "10370101"));
		tobaccoItemParam.put("bar", paramMap.get("big_bar"));
		List<Map<String, Object>> tobaccoItemList = itemDao.selectTobaccoItem(tobaccoItemParam);//查询卷烟库商品
		if(!tobaccoItemList.isEmpty()) {
			for(Map<String, Object> tobaccoItemMap : tobaccoItemList) {
				tobaccoItemMap.put("FLAG", "TOBACCO");
				if(tobaccoItemMap.get("item_bar")==null || "".equals(tobaccoItemMap.get("item_bar"))) {
					tobaccoItemMap.put("item_bar", tobaccoItemMap.get("item_id"));
				}
			}
			return tobaccoItemList;
		}
		Map<String, Object> baseItemParam = new HashMap<String, Object>();
		baseItemParam.put("item_bar", paramMap.get("big_bar"));
		List<Map<String, Object>> itemList = itemDao.selectItem(baseItemParam);//查询非烟库商品
		if(!itemList.isEmpty()) {
			for(Map<String, Object> itemMap : itemList) {
				itemMap.put("FLAG", "BASE");
			}
		}
		return itemList;
	}
	
	/**
	 * 非烟入库专用
	 * 1. 查询此条码现存的商品 如果有直接返回
	 * 2. 如果没有现存商品 则查询已删除的商品 并返回 
	 */
	private List<Map<String, Object>> judgeItemStatusForWhse(Map<String, Object> itemParam) throws Exception {
		List<Map<String, Object>> itemAndUnitList = itemDao.selectNoRemovedItemAndWhse(itemParam);
		if(!itemAndUnitList.isEmpty()){
			return itemAndUnitList;
		}else{
			return itemDao.selectRemovedItemAndWhse(itemParam);
		}
	}
	
	/**
	 * 1. 查询此条码现存的商品 如果有直接返回（删除、下架、正常
	 * 2. 如果没有现存商品 则查询已删除的商品 并返回 
	 */
	private List<Map<String, Object>> judgeItemStatus(Map<String, Object> itemParam) throws Exception {
		List<Map<String, Object>> itemAndUnitList = searchNoRemovedItem(itemParam);
		if(!itemAndUnitList.isEmpty()){
			return itemAndUnitList;
		}else{
			return searchRemovedItem(itemParam);
		}
	}
	
	@Override
	public List<Map<String, Object>> searchNoRemovedItem(Map<String, Object> itemParam) throws Exception {
		return itemDao.selectNoRemovedItem(itemParam);
	}

	@Override
	public List<Map<String, Object>> searchRemovedItem(Map<String, Object> itemParam) throws Exception {
		return itemDao.selectRemovedItem(itemParam);
	}
	
	
	
	
	@Override
	public List<Map<String, Object>> searchMerchAndTobaccoItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchAndTobaccoItem paramMap: " + paramMap);
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", paramMap.get("merch_id"));
		merchItemParam.put("big_bar", paramMap.get("big_bar"));
		List<Map<String, Object>> merchItemList = searchMerchItemUnitJoinMerchItem(merchItemParam);
		paramMap.put("count", merchItemParam.get("count"));
		paramMap.put("page_count", merchItemParam.get("page_count"));
		if(!merchItemList.isEmpty()) {
			for(Map<String, Object> merchItemMap : merchItemList) {
				merchItemMap.put("FLAG", "MERCH");
			}
			return merchItemList;
		}
		Map<String, Object> tobaccoItemParam = new HashMap<String, Object>();
		tobaccoItemParam.put("supplier_id", MapUtil.getString(paramMap, "com_id", "10370101"));
		tobaccoItemParam.put("bar", paramMap.get("big_bar"));
		List<Map<String, Object>> tobaccoItemList = itemDao.selectTobaccoItem(tobaccoItemParam);
		if(!tobaccoItemList.isEmpty()) {
			for(Map<String, Object> tobaccoItemMap : tobaccoItemList) {
				tobaccoItemMap.put("FLAG", "TOBACCO");
			}
		}
		return tobaccoItemList;
	}
	
	@Override
	public List<Map<String, Object>> searchMerchItemJoinWhseWithIncrease(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemJoinWhseWithIncrease merchItemParam: " + merchItemParam);
		// 必须有big_bar
		if(!merchItemParam.containsKey("bar")) {
			merchItemParam.put("bar", merchItemParam.get("big_bar"));
		}
		if(!merchItemParam.containsKey("item_bar")) {
			merchItemParam.put("item_bar", merchItemParam.get("big_bar"));
		}
		List<Map<String, Object>> merchItemList = searchMerchItemAndWhseForFeiYanRuKu(merchItemParam);
		if(!merchItemList.isEmpty()) {
			for(Map<String, Object> merchItemMap : merchItemList) {
				// flag不可能同事包含多种取值, 如果查询出烟草商品只返回第一个
				if("TOBACCO".equals(merchItemMap.get("FLAG"))) {
					merchItemMap.put("merch_id", merchItemParam.get("merch_id"));
					merchItemMap.put("unit_name", merchItemMap.get("item_unit_name"));
					merchItemMap.put("pri1", new BigDecimal(merchItemMap.get("pri_wsale").toString()).divide(new BigDecimal(merchItemMap.get("unit_ratio").toString()),5,BigDecimal.ROUND_HALF_UP));
					merchItemMap.put("pri4", new BigDecimal(merchItemMap.get("pri_drtl").toString()).divide(new BigDecimal(merchItemMap.get("unit_ratio").toString()),5,BigDecimal.ROUND_HALF_UP));
					List<Map<String, Object>> newMerchItemUnitList = null;
					//组装数据
					newMerchItemUnitList=packageTabaccoItem(merchItemMap);
					
					merchItemMap.put("list", newMerchItemUnitList);
					this.createMerchItem(merchItemMap);
					return this.searchMerchItemUnitJoinMerchItem(merchItemParam);
				}
				// 如果是基础商品/商户商品表中的数据, 则全部返回
			}
		}
		return merchItemList;
	}
	
	/**
	 * 查询商品信息和库才信息,非烟入库专用
	 * @param itemParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchItemAndWhseForFeiYanRuKu(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemAndWhseForFeiYanRuKu itemParam: " + itemParam);
		
		List<Map<String, Object>> merchItemList = judgeItemStatusForWhse(itemParam);
		if(!merchItemList.isEmpty()) {
			return merchItemList;
		}
		
		itemParam.put("supplier_id", MapUtil.getString(itemParam, "com_id", "10370101"));
		List<Map<String, Object>> tobaccoItemList = itemDao.selectTobaccoItem(itemParam);
		if(!tobaccoItemList.isEmpty()) {
			for(Map<String, Object> tobaccoItemMap : tobaccoItemList) {
				tobaccoItemMap.put("FLAG", "TOBACCO");
			}
			return tobaccoItemList;
		}
		List<Map<String, Object>> itemList = itemDao.selectItem(itemParam);
		if(!itemList.isEmpty()) {
			for(Map<String, Object> itemMap : itemList) {
				itemMap.put("FLAG", "BASE");
			}
		}
		return itemList;
	}
	
	@Override
	public List<Map<String, Object>> searchMerchItemJoinWhseAndSoOn(Map<String, Object> itemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemJoinWhseAndSoOn itemParam: " + itemParam);
		List<Map<String, Object>> merchItemList = judgeItemStatus(itemParam);
		if(!merchItemList.isEmpty()) {
			return merchItemList;
		}
		//备份原来做法
//		List<Map<String, Object>> merchItemList = whseService.searchMerchItemUnitJoinMerchItemJoinWhseMerch(itemParam);
//		if(!merchItemList.isEmpty()) {
//			for(Map<String, Object> merchItemMap : merchItemList) {
//				merchItemMap.put("FLAG", "MERCH");
//			}
//			return merchItemList;
//		}
		itemParam.put("supplier_id", MapUtil.getString(itemParam, "com_id", "10370101"));
		List<Map<String, Object>> tobaccoItemList = itemDao.selectTobaccoItem(itemParam);
		if(!tobaccoItemList.isEmpty()) {
			for(Map<String, Object> tobaccoItemMap : tobaccoItemList) {
				tobaccoItemMap.put("FLAG", "TOBACCO");
			}
			return tobaccoItemList;
		}
		List<Map<String, Object>> itemList = itemDao.selectItem(itemParam);
		if(!itemList.isEmpty()) {
			for(Map<String, Object> itemMap : itemList) {
				itemMap.put("FLAG", "BASE");
			}
		}
		return itemList;
	}
	/**
	 * 获取零售户商品信息, 包含商品属性, 可以用seq_id, item_id, item_bar, big_bar查询
	 * 原来用表连接实现的查询, 现在用逻辑实现, 从base_merch_item_unit表中查询的结果集拿出item_id来再从base_merch_item表中查询
	 * 正常返回merchItemWithMerchItemUnitList, 如果item_id在base_merch_item_unit中存在而在base_merch_item不存在, 返回空list
	 */
	@Override
	public List<Map<String, Object>> searchMerchItemUnitJoinMerchItem(Map<String, Object> merchItemUnitJoinMerchItemParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemUnitJoinMerchItem merchItemUnitJoinMerchItemParam: " + merchItemUnitJoinMerchItemParam);
		List<Map<String, Object>> merchItemUnitList = itemDao.selectMerchItemUnit(merchItemUnitJoinMerchItemParam);
		if(merchItemUnitList.size()>0) {
			Set<String> merchItemUnitItemIdSet = new HashSet<String>();
			for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
				merchItemUnitItemIdSet.add((String) merchItemUnitMap.get("item_id"));
			}
			List<Map<String, Object>> merchItemList = new ArrayList<Map<String, Object>>();
			Map<String, Object> merchItemParam = new HashMap<String, Object>();
			merchItemParam.put("merch_id", merchItemUnitJoinMerchItemParam.get("merch_id"));
//			merchItemParam.put("status", "0,1,2"); // 查询在销、下架和已删除的商品
			for(String itemId : merchItemUnitItemIdSet) {
				merchItemParam.put("item_id", itemId);
				List<Map<String, Object>> itemList = itemDao.selectMerchItem(merchItemParam);
				merchItemList.addAll(itemList);
			}
			if(merchItemList.size()>0) {
//				List<Map<String, Object>> merchItemJoinMerchItemUnitList = new ArrayList<Map<String, Object>>();
				for(Map<String, Object> merchItemMap : merchItemList) {
					for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
						if(merchItemMap.get("item_id").equals(merchItemUnitMap.get("item_id"))) {
							// 用base_merch_item中的数据覆盖base_merch_item_unit的同名数据
							merchItemMap.putAll(MapUtil.rename(merchItemUnitMap, "seq_id", "item_bar", "item_unit_name", "pri4.big_pri4", "big_bar", "big_unit_name", "unit_ratio")); 
//							merchItemJoinMerchItemUnitList.add(merchItemUnitMap);
						}
						
					}
				}
				return merchItemList;
//				return merchItemJoinMerchItemUnitList;
			} // else return 空list
		}
		return new ArrayList<Map<String, Object>>(); // item_id在base_merch_item表中不存在是异常情况, 应该从dao层查找问题根源
	}
	
	/**
	 * 商品管理中修改商品前的查询, 用item_id查询, 先查询merch_item表再查询merch_item_unit表
	 * 如果查询出多个merch_item, 则选取item_id最小的
	 */
	@Override
	public List<Map<String, Object>> searchMerchItemJoinMerchItemUnit(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemJoinMerchItemUnit paramMap: " + paramMap);
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", paramMap.get("merch_id"));
		merchItemParam.put("item_id", paramMap.get("item_id"));
		merchItemParam.put("page_index", -1);
		merchItemParam.put("page_size", -1);
		merchItemParam.put("status", "0,1,2");
		Map<String, Map<String, Object>> merchItemListMap = new HashMap<String, Map<String, Object>>(); // merch_id+,+item_id
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(merchItemParam);
//		Map<String, Object> merchItemResult = null; // 默认返回null
		if(!merchItemList.isEmpty()) {
			for(Map<String, Object> merchItemMap : merchItemList) {
				merchItemListMap.put(merchItemMap.get("merch_id")+","+merchItemMap.get("item_id"), merchItemMap);
			}
//			merchItemResult = merchItemList.get(0);
			// 防止用item_bar查询的merch_item表, 要重新组织参数map再查merch_item_unit
			Map<String, Object> merchItemUnitParam = new HashMap<String, Object>();
			merchItemUnitParam.put("merch_id", paramMap.get("merch_id"));
			merchItemUnitParam.put("item_id", paramMap.get("item_id"));
			merchItemUnitParam.put("page_index", -1); // 不分页
			merchItemUnitParam.put("page_size", -1);
//			String itemBar = (String) merchItemResult.get("item_bar");
			List<Map<String, Object>> merchItemUnitList = itemDao.selectMerchItemUnit(merchItemUnitParam);
			for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
				// unit_ratio==1 && big_bar==item_bar则从包装中删除这条记录, 如果item_bar==null则big_bar==null就删除
				// 从merchItemUnitList中删除, 再添加到merchItemMap中
				String merchItemIdentity = merchItemUnitMap.get("merch_id")+","+merchItemUnitMap.get("item_id");
				Map<String, Object> merchItem = merchItemListMap.get(merchItemIdentity);
				if(merchItem!=null) {
					// 如果是unit_ratio==1, 则把unit信息整合到item里
					if(new BigDecimal(merchItemUnitMap.get("unit_ratio").toString()).compareTo(BigDecimal.ONE)==0) {
						merchItem.put("seq_id", merchItemUnitMap.get("seq_id"));
//						if(merchItem.get("item_bar")==null) {
//							if(merchItem.get("item_bar")==merchItemUnitMap.get("big_bar")) {
//								merchItem.putAll(merchItemUnitMap);
//							}
//						} else {
//							if(merchItem.get("item_bar").equals(merchItemUnitMap.get("big_bar"))) {
//								merchItem.putAll(merchItemUnitMap);
//							}
//						}
					}
					else { // 否则添加到list列表中
						if(merchItem.get("list")==null) {
							merchItem.put("list", new ArrayList<Map<String, Object>>());
						}
						((List<Map<String, Object>>) merchItem.get("list")).add(merchItemUnitMap);
					}
				}
//				if(new BigDecimal(merchItemUnitMap.get("unit_ratio").toString()).compareTo(BigDecimal.ONE)==0 && itemBar==null?itemBar==merchItemUnitMap.get("big_bar"):itemBar.equals(merchItemUnitMap.get("big_bar"))) {
//					merchItemUnitList.remove(merchItemUnitMap);
//					merchItemResult.putAll(merchItemUnitMap);
//					break;
//				}
			}
			// 将merchItemUnitList剩下的数据作为list添加到merchItemMap中
//			merchItemResult.put("list", merchItemUnitList);
		}
//		return merchItemMap;
		List<Map<String, Object>> merchItemResult = new ArrayList<Map<String, Object>>();
		merchItemResult.addAll(merchItemListMap.values());
		return merchItemResult;
	}
	
	/**
	 * 逻辑删除商户商品
	 */
	@Override
	public void removeMerchItem(Map<String, Object> merchItemParam) throws Exception {
		LOG.debug("ItemServiceImpl removeMerchItem merchItemParam: " + merchItemParam);
		merchItemParam.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
		itemDao.deleteMerchItem(merchItemParam);
		whseDao.deleteWhseMerch(merchItemParam);
	}
	
	@Override
	public void modifyMerchItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl modifyMerchItem paramMap: " + paramMap);
		paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
		itemDao.updateMerchItemWithStatus(paramMap);
	}
	
	/**
	 * 修改商户商品
	 */
	@Override
	public void modifyMerchItemAndUnit(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl modifyMerchItemAndUnit paramMap: " + paramMap);
		// 更新商品信息
		paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
		itemDao.updateMerchItem(paramMap);
		List<Map<String, Object>> merchItemUnitList = MapUtil.get(paramMap, "list", null);
		if(merchItemUnitList!=null && !merchItemUnitList.isEmpty()) {
			List<Map<String, Object>> merchItemUnitAddList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> merchItemUnitUpdList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> merchItemUnitDelList = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> merchItemUnitMap : merchItemUnitList) {
				if(merchItemUnitMap.containsKey("operation_type")) {
					String operationType = MapUtil.getString(merchItemUnitMap, "operation_type");
					// seq_id, merch_id, item_id, item_bar, item_unit_name
					if("ADD".equalsIgnoreCase(operationType)) merchItemUnitAddList.add(merchItemUnitMap);
					else if("UPD".equalsIgnoreCase(operationType)) merchItemUnitUpdList.add(merchItemUnitMap);
					else if("DEL".equalsIgnoreCase(operationType)) merchItemUnitDelList.add(merchItemUnitMap);
				}
			}
			if(!merchItemUnitAddList.isEmpty()) itemDao.insertMerchItemUnit(merchItemUnitAddList);
			if(!merchItemUnitUpdList.isEmpty()) itemDao.updateMerchItemUnit(merchItemUnitUpdList);
			if(!merchItemUnitDelList.isEmpty()) itemDao.deleteMerchItemUnit(merchItemUnitDelList);
		}
	}
	
	/**
	 * 新增商品3个步骤: 1. merch_item, 2. item_unit, 3. whse_merch
	 * item_id, item_bar, pri4
	 */
	@Override
	public void createMerchItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl createMerchItem paramMap: " + paramMap);
		if(paramMap.get("qty_whse")==null) {
			paramMap.put("qty_whse", 0);
		}
		if(paramMap.get("pri1")==null) {
			paramMap.put("pri1", 0);
		}
		if(paramMap.get("cost")==null || BigDecimal.ZERO.compareTo(new BigDecimal(paramMap.get("cost").toString()))==0) {
			paramMap.put("cost", paramMap.get("pri1"));
		}
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if(itemKindId == null || itemKindId.equals("")){
			paramMap.put("item_kind_id", "99");
		}else{
			paramMap.put("item_kind_id", itemKindId);
		}
		paramMap.put("create_date", DateUtil.getToday());
		paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
		paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
		itemDao.insertMerchItem(paramMap);
		// 新增商品属性
		if(paramMap.containsKey("list")) {
			List<Map<String, Object>> merchItemUnitList = (List<Map<String, Object>>) paramMap.get("list");
			itemDao.insertMerchItemUnit(merchItemUnitList);
		}
		// 新增库存, 如果没有qty_whse字段则新增为0, status=1
		paramMap.put("status", "1");
		whseDao.insertWhseMerch(paramMap);
	}
	
	/**
	 * 卷烟入库, 先根据item_id判断商户商品表里是不是有此商品, 然后再进行新增
	 */
	@Override
	public void createMerchTobaccoItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl createMerchTobaccoItem paramMap: " + paramMap);
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", paramMap.get("merch_id"));
		merchItemParam.put("item_id", paramMap.get("item_id"));
		// 卷烟入库先判断商户商品表中是否有这个商品编码, 如果没有再新增
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(merchItemParam);
		if(merchItemList.isEmpty()) {
			if(paramMap.get("discount")==null) {
				paramMap.put("discount", 100);
			}
			if(paramMap.get("pri1")==null) {
				paramMap.put("pri1", 0);
			}
			if(paramMap.get("cost")==null || BigDecimal.ZERO.compareTo(new BigDecimal(paramMap.get("cost").toString()))==0) {
				paramMap.put("cost", paramMap.get("pri1"));
			}
			paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
			itemDao.insertMerchItem(paramMap);
			if(paramMap.get("qty_whse")==null) {
				paramMap.put("qty_whse", 0);
			}
			paramMap.put("create_date", DateUtil.getToday());
			paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
			// 新增商品属性
			if(paramMap.containsKey("list")) {
				List<Map<String, Object>> merchItemUnitList = (List<Map<String, Object>>) paramMap.get("list");
				itemDao.insertMerchItemUnit(merchItemUnitList);
			}
			// 新增库存, 如果没有qty_whse字段则新增为0
			paramMap.put("status", "1");
			whseDao.insertWhseMerch(paramMap);
		}
	}
	@Override
	public void createMerchTobaccoItem(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("ItemServiceImpl createMerchTobaccoItem paramList: " + paramList);
		List<Map<String, Object>> merchItemParamList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> merchItemUnitParamList = new ArrayList<Map<String, Object>>();
		String merchId="";
		if(paramList!=null && !paramList.isEmpty()){
			if(paramList.get(0).get("merch_id")!=null){
				merchId=(String) paramList.get(0).get("merch_id");
			}
		}
		Map<String, Object>userMap=new HashMap<String, Object>();
		userMap.put("merch_id", merchId);
		userMap.put("page_index", -1);
		userMap.put("page_size", -1);
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(userMap);//用户下所有商品
		List<Map<String, Object>>IntersectionList=new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> differenceList1=new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> differenceList2=new ArrayList<Map<String,Object>>();
		this.filterItem(paramList, merchItemList, IntersectionList, differenceList1, differenceList2);
		for(Map<String, Object> paramMap : differenceList1) {
			Map<String, Object> itemParam = new HashMap<String, Object>();
			itemParam.put("merch_id", paramMap.get("merch_id"));
			itemParam.put("item_id", paramMap.get("item_id"));
			if(paramMap.get("qty_whse")==null) {
				paramMap.put("qty_whse", 0);
			}
			if(paramMap.get("discount")==null) {
				paramMap.put("discount", 100);
			}
			if(paramMap.get("pri1")==null) {
				paramMap.put("pri1", 0);
			}
			if(paramMap.get("cost")==null || BigDecimal.ZERO.compareTo(new BigDecimal(paramMap.get("cost").toString()))==0) {
				paramMap.put("cost", paramMap.get("pri1"));
			}
			paramMap.put("create_date", DateUtil.getToday());
			paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
			paramMap.put("status", "1");
			paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
			merchItemParamList.add(paramMap);
			merchItemUnitParamList.addAll((List<Map<String, Object>>) paramMap.get("list"));
		}
		itemDao.insertMerchItem(merchItemParamList);
		itemDao.insertMerchItemUnit(merchItemUnitParamList);
		whseDao.insertWhseMerch(merchItemParamList);
	}
	//操作商品信息和商品属性表
	@Override
	public void operateMerchItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl operateMerchItem paramMap: " + paramMap);
		/* 梁凯 2014年6月4日11:25:39
		 * 如果paramMap中没有cost则用pri1, 如果没有pri1, 则为0, 原来如果cost是0也用pri1, 取消这个逻辑
		 */
		paramMap.put("cost", MapUtil.getBigDecimal(paramMap, "cost", MapUtil.getBigDecimal(paramMap, "pri1")));
		paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
		if("C".equals(paramMap.get("type"))) { //增加
			itemDao.insertMerchItem(paramMap);
			itemDao.insertMerchItemUnitWithSelf(paramMap);
			paramMap.put("qty_whse", 0);
			paramMap.put("status", "1");
			whseDao.insertWhseMerch(paramMap);
		} else if("U".equals(paramMap.get("type"))) { //修改
			itemDao.updateMerchItem(paramMap);
			itemDao.updateMerchItemUnitWithSelf(paramMap);
			itemDao.insertMerchItemUnit(paramMap);
			itemDao.deleteMerchItemUnit(paramMap);
		} else if("D".equals(paramMap.get("type"))){ //删除 "D"
			// 直接用item_id删除item_unit表中的数据, 所以不再需要调用deleteMerchItemUnit方法
			itemDao.deleteMerchItem(paramMap);
			whseDao.deleteWhseMerch(paramMap);
		}
	}
	@Override
	public List<Map<String, Object>> searchMerchItemByLucene(Map<String, Object> luceneParam) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchItemByLucene luceneParam: " + luceneParam);
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		Map<String, String> postParam = new HashMap<String, String>();
		if(luceneParam.get("item_kind_id")!=null&&!luceneParam.get("item_kind_id").equals("")) {
			postParam.put("itemKindId", luceneParam.get("item_kind_id").toString());
		}
		if(luceneParam.get("merch_id")!=null) {
			merchItemParam.put("merch_id", luceneParam.get("merch_id"));
			postParam.put("merchId", luceneParam.get("merch_id").toString());
		}
		if(luceneParam.get("key")!=null&&!luceneParam.get("key").equals("")) {
			postParam.put("key", luceneParam.get("key").toString());
		}
		if(luceneParam.get("page_index")!=null) {
			merchItemParam.put("page_index", luceneParam.get("page_index"));
			postParam.put("currPage", luceneParam.get("page_index").toString());
		} else {
			merchItemParam.put("page_index", 1);
		}
		if(luceneParam.get("page_size")!=null) {
			merchItemParam.put("page_size", luceneParam.get("page_size"));
			postParam.put("pageSize", luceneParam.get("page_size").toString());
		} else {
			merchItemParam.put("page_size", 20);
		}
		String json = HttpUtil.post(RetailConfig.getLuceneServer()+"tobacco/retail/retailGoods/wildcardSearch/json", postParam);
		StringBuffer itemIdBuffer = new StringBuffer();
		Map<String, Object> luceneResult = JsonUtil.json2Map(json);
		if(luceneResult!=null) {
			if(Constants.SUCCESS.equals(luceneResult.get("code"))) {
				Map<String, Object> result = (Map<String, Object>) luceneResult.get("result");
				List<Map<String, Object>> itemList = (List<Map<String, Object>>) result.get("item_list");
				int index = 1;
				for(Map<String, Object> item : itemList) {
					if(index++==1) {
						itemIdBuffer.append(item.get("item_id").toString());
					} else {
						itemIdBuffer.append(","+item.get("item_id").toString());
					}
				}
				luceneParam.put("item_id", itemIdBuffer.toString());
				luceneParam.put("page_index", result.get("page_index"));
				luceneParam.put("page_size", result.get("page_size"));
				luceneParam.put("page_count", result.get("page_count"));
				luceneParam.put("count", result.get("count"));
			}else{
				luceneParam.put("item_id", itemIdBuffer.toString());
				luceneParam.put("page_index", "1");
				luceneParam.put("page_size", "0");
				luceneParam.put("page_count", "1");
				luceneParam.put("count", "0");
			}
		} else {
			return new ArrayList<Map<String, Object>>();
		}
		if(itemIdBuffer.length()==0) {
			return new ArrayList<Map<String, Object>>();
		}
//		json = json.replaceAll("\\s|\\[|\\]|\"", "");
//		merchItemParam.put("item_id", json);
		merchItemParam.put("merch_id", luceneParam.get("merch_id"));
		merchItemParam.put("item_id", itemIdBuffer.toString());
		merchItemParam.put("status", MapUtil.getString(luceneParam, "status","1,2"));
		merchItemParam.put("page_index", -1);
		merchItemParam.put("page_size", -1);
		luceneParam.put("item_id", itemIdBuffer.toString());
		return itemDao.selectMerchItem(merchItemParam);
	}
	

	@Override
	public Map<String, Object> findMerchItemByLucene(Map<String, Object> luceneParam) throws Exception {
		LOG.debug("ItemServiceImpl findMerchItemByLucene luceneParam: " + luceneParam);
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		Map<String, String> postParam = new HashMap<String, String>();
		if(luceneParam.get("item_kind_id")!=null&&!luceneParam.get("item_kind_id").equals("")) {
			postParam.put("itemKindId", luceneParam.get("item_kind_id").toString());
		}
		if(luceneParam.get("merch_id")!=null) {
			merchItemParam.put("merch_id", luceneParam.get("merch_id"));
			postParam.put("merchId", luceneParam.get("merch_id").toString());
		}
		if(luceneParam.get("key")!=null&&!luceneParam.get("key").equals("")) {
			postParam.put("key", luceneParam.get("key").toString());
		}
		if(luceneParam.get("page_index")!=null) {
			merchItemParam.put("page_index", luceneParam.get("page_index"));
			postParam.put("currPage", luceneParam.get("page_index").toString());
		} else {
			merchItemParam.put("page_index", 1);
		}
		if(luceneParam.get("page_size")!=null) {
			merchItemParam.put("page_size", luceneParam.get("page_size"));
			postParam.put("pageSize", luceneParam.get("page_size").toString());
		} else {
			merchItemParam.put("page_size", 20);
		}
		String json = HttpUtil.post(RetailConfig.getLuceneServer()+"tobacco/retail/retailGoods/wildcardSearch/json", postParam);
		StringBuffer itemIdBuffer = new StringBuffer();
		Map<String, Object> luceneResult = JsonUtil.json2Map(json);
		if(luceneResult!=null) {
			if(Constants.SUCCESS.equals(luceneResult.get("code"))) {
				Map<String, Object> result = (Map<String, Object>) luceneResult.get("result");
				List<Map<String, Object>> itemList = (List<Map<String, Object>>) result.get("item_list");
				int index = 1;
				for(Map<String, Object> item : itemList) {
					if(index++==1) {
						itemIdBuffer.append(item.get("item_id").toString());
					} else {
						itemIdBuffer.append(","+item.get("item_id").toString());
					}
				}
				luceneParam.put("item_id", itemIdBuffer.toString());
				luceneParam.put("page_index", result.get("page_index"));
				luceneParam.put("page_size", result.get("page_size"));
				luceneParam.put("page_count", result.get("page_count"));
				luceneParam.put("count", result.get("count"));
			}else{
				luceneParam.put("item_id", itemIdBuffer.toString());
				luceneParam.put("page_index", "1");
				luceneParam.put("page_size", "0");
				luceneParam.put("page_count", "1");
				luceneParam.put("count", "0");
			}
		} else {
			return new HashMap<String, Object>();
		}
		if(itemIdBuffer.length()==0) {
			return new HashMap<String, Object>();
		}
//		json = json.replaceAll("\\s|\\[|\\]|\"", "");
//		merchItemParam.put("item_id", json);
		merchItemParam.put("merch_id", luceneParam.get("merch_id"));
		merchItemParam.put("item_id", itemIdBuffer.toString());
		merchItemParam.put("status", MapUtil.getString(luceneParam, "status","1,2"));
		merchItemParam.put("page_index", -1);
		merchItemParam.put("page_size", -1);
		luceneParam.put("item_id", itemIdBuffer.toString());
		return merchItemParam;
	}
	
	
	//传入key（模糊商品名称）  得到item_id
	@Override
	public List getLuceByKey(Map paramsMap)throws Exception{
		String cgtItemUrl="http://192.168.0.3:8889/ryxShopAssistantInterface/tobacco/retail/fuzzySearch/json";
		String dataJson = HttpUtil.post(cgtItemUrl, paramsMap);
		return JsonUtil.json2List(dataJson);
	}
	//更新luce中的数据
	@Override
	public void updateLuce()throws Exception{
		String cgtItemUrl="http://192.168.0.3:8889/ryxShopAssistantInterface/tobacco/retail/db2luce";
		String dataJson = HttpUtil.post(cgtItemUrl);
		LOG.debug("ItemServiceImpl updateLuce 结果 ："+dataJson);
	}

	@Override
	public void initData(Map<String, Object> dataParam) throws Exception {
		List<Map<String,Object>> dataMapList=null;
		dataMapList=(List<Map<String,Object>>)dataParam.get("list");
		if(dataMapList!=null&&dataMapList.size()>0){
			for(int i=0;i<dataMapList.size();i++){
				Map<String,Object> dataMap=dataMapList.get(i);
				Map<String,Object> selectDataMap=new HashMap<String,Object>();
				selectDataMap.put("merch_id", dataParam.get("merch_id"));
				selectDataMap.put("big_bar", dataMap.get("big_bar"));
				selectDataMap.put("com_id", dataMap.get("com_id")); // 公司号是用来获取卷烟的
				List<Map<String, Object>> getData=searchMerchAndTobaccoItemWithIncrease(selectDataMap);
				if(getData==null||getData.size()<1){
					Map<String,Object> addDataMap=new HashMap<String,Object>();
					addDataMap.put("status", "1");
					addDataMap.put("create_date", DateUtil.getToday());
					addDataMap.put("create_time", DateUtil.getCurrentTime().substring(8));
					addDataMap.put("merch_id", dataParam.get("merch_id"));
					addDataMap.put("pri1", dataMap.get("pri1"));
					addDataMap.put("pri4", dataMap.get("pri4"));
					addDataMap.put("item_name", dataMap.get("item_name"));
					addDataMap.put("item_id", dataMap.get("item_id"));
					addDataMap.put("item_bar", dataMap.get("item_bar"));
					addDataMap.put("unit_name", dataMap.get("unit_name"));
					addDataMap.put("big_bar", dataMap.get("big_bar"));
					addDataMap.put("discount", dataMap.get("discount"));
					Map<String, Object> newMerchItemUnitMap = new HashMap<String, Object>();
					newMerchItemUnitMap.put("merch_id", dataParam.get("merch_id"));
					newMerchItemUnitMap.put("seq_id", IDUtil.getId());
					newMerchItemUnitMap.put("item_id", dataMap.get("item_id"));
					newMerchItemUnitMap.put("item_bar", dataMap.get("item_bar"));
					newMerchItemUnitMap.put("item_unit_name", dataMap.get("unit_name"));
					newMerchItemUnitMap.put("big_bar", dataMap.get("item_bar"));
					newMerchItemUnitMap.put("big_unit_name", dataMap.get("unit_name"));
					newMerchItemUnitMap.put("unit_ratio", dataMap.get("unit_ratio"));
					newMerchItemUnitMap.put("pri4", dataMap.get("pri4"));
					List<Map<String, Object>> merchItemUnitList = new ArrayList<Map<String, Object>>();
					merchItemUnitList.add(newMerchItemUnitMap);
					addDataMap.put("list", merchItemUnitList);
					createMerchItem(addDataMap);
				}
			}
		}
	}
	// 查询推荐商品
	@Override
	public List<Map<String, Object>> searchRecommendedItem (Map<String, Object> recommendMap) throws Exception{
		LOG.debug("ItemServiceImpl searchRecommendedItem recommendMap: "+recommendMap);
		List<Map<String, Object>> recommendedItemList = itemDao.selectRecommendedItem(recommendMap);
		// 没有查到推荐商品直接返回空list
		LOG.debug("ItemServiceImpl recommendedItemList:"+recommendedItemList);
		if(recommendedItemList.size()<=0) {
			LOG.debug("ItemServiceImpl searchRecommendedItem  recommendedItemList为空--推荐商品");
			return new ArrayList<Map<String, Object>>();		
		}
		StringBuffer itemIdBuffer = new StringBuffer();
		for(Map<String, Object> recommendedItemMap : recommendedItemList) {
			if(itemIdBuffer.length()==0) itemIdBuffer.append(recommendedItemMap.get("item_id"));
			else itemIdBuffer.append(","+recommendedItemMap.get("item_id"));
		}
		Map<String, Object> itemParam = new HashMap<String, Object>();
		itemParam.put("item_id", itemIdBuffer.toString());
		itemParam.put("page_index", -1);
		itemParam.put("page_size", -1);
		return itemDao.selectItem(itemParam);
	}
	@Override
	public List<Map<String, Object>> searchRecommendedPOSItem(Map<String, Object> recommendMap)throws Exception{
		LOG.debug("ItemServiceImpl searchRecommendedPOSItem recommendMap:"+recommendMap);
//		List<Map<String, Object>> recommendItem1 =this.searchRecommendedItem(recommendMap);//得到推荐商品集合
		//得到推荐商品集合
		List<Map<String, Object>> recommendItem=itemDao.searchRecommendedPOSItemInfo(recommendMap);
		String merchId=(String) recommendMap.get("merch_id");
		if(!recommendItem.isEmpty()){
			for (Map<String, Object> map : recommendItem) {
				map.put("merch_id", merchId);
			}
		}
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(recommendMap);// 商户商品
		List<Map<String, Object>>IntersectionList=new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> differenceList1=new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> differenceList2=new ArrayList<Map<String,Object>>();
		//过滤商品
		this.filterItem(recommendItem, merchItemList, IntersectionList, differenceList1, differenceList2);
		for (Map<String, Object> itemMap : differenceList1) {
			Map<String, Object> newMerchItemUnitMap = new HashMap<String, Object>();
			newMerchItemUnitMap.put("merch_id",merchId);
			newMerchItemUnitMap.put("seq_id", IDUtil.getId());
			newMerchItemUnitMap.put("item_id", itemMap.get("item_id"));
			newMerchItemUnitMap.put("item_bar", itemMap.get("item_bar"));
			newMerchItemUnitMap.put("item_unit_name", itemMap.get("unit_name"));
			newMerchItemUnitMap.put("big_bar", itemMap.get("item_bar"));
			newMerchItemUnitMap.put("big_unit_name", itemMap.get("unit_name"));
			newMerchItemUnitMap.put("unit_ratio", 1);
			newMerchItemUnitMap.put("pri4", itemMap.get("pri4"));
			List<Map<String, Object>> newMerchItemUnitList=new ArrayList<Map<String,Object>>();
			newMerchItemUnitList.add(newMerchItemUnitMap);
			itemMap.put("list",newMerchItemUnitList); 
		}
		if(!differenceList1.isEmpty()) this.createMerchTobaccoItem(differenceList1);//插入商户商品表,包装，库存
		return recommendItem;
	}
	/**
	 * 比较两个list
	 * @param list1:list1
	 * @param list2:list2
	 * @param IntersectionList:交集
	 * @param differenceList1：list1-list2
	 * @param differenceList2:list2-list2
	 * @throws Exception
	 */
	public void filterItem (List<Map<String, Object>> list1,List<Map<String, Object>>list2,List<Map<String, Object>>IntersectionList,List<Map<String, Object>> differenceList1,List<Map<String, Object>>differenceList2 )throws Exception{
		Map<String, Object> itemMap1=new HashMap<String, Object>();
		Map<String, Object> itemMap2=new HashMap<String, Object>();
		for (Map<String, Object> map : list1) {
			StringBuffer key=new StringBuffer();
			if(map.containsKey("merch_id")&&map.get("merch_id")!=null){
				key.append(map.get("merch_id"));
				key.append(",");
			}
			if(map.containsKey("item_id")&&map.get("item_id")!=null){
				key.append(map.get("item_id"));
			}
			itemMap1.put(key.toString(), map);
		}
		for (Map<String, Object> map : list2) {
			StringBuffer key=new StringBuffer();
			if(map.containsKey("merch_id")&&map.get("merch_id")!=null){
				key.append(map.get("merch_id"));
				key.append(",");
			}
			if(map.containsKey("item_id")&&map.get("item_id")!=null){
				key.append(map.get("item_id"));
			}
			itemMap2.put(key.toString(), map);
		}
//		IntersectionList:交集
//		differenceList1：list1-list2
//		differenceList2:list2-list1
		for (Map<String, Object> map : list2) {
			StringBuffer key=new StringBuffer();
			if(map.containsKey("merch_id")&&map.get("merch_id")!=null){
				key.append(map.get("merch_id"));
				key.append(",");
			}
			if(map.containsKey("item_id")&&map.get("item_id")!=null){
				key.append(map.get("item_id"));
			}
			if(!itemMap1.containsKey(key.toString())){
				differenceList2.add(map);
			}
		}
		for (Map<String, Object> map : list1) {
			StringBuffer key=new StringBuffer();
			if(map.containsKey("merch_id")&&map.get("merch_id")!=null){
				key.append(map.get("merch_id"));
				key.append(",");
			}
			if(map.containsKey("item_id")&&map.get("item_id")!=null){
				key.append(map.get("item_id"));
			}
			if(itemMap2.containsKey(key.toString())){//交集
				IntersectionList.add(map);
			}else{
				differenceList1.add(map);
			}
		}
	}
	/**
	 * 新增卷烟商品组装数据
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @param merchItemMap
	 * @return
	 */
	private List<Map<String, Object>> packageTabaccoItem(Map<String, Object> merchItemMap){
		List<Map<String, Object>> newMerchItemUnitList = new ArrayList<Map<String, Object>>();
		/* 梁凯 2014年5月22日
		 * 特殊包装的卷烟商品包括item_bar为空格的
		 */
		String itemBar = (String) merchItemMap.get("item_bar");
		if(itemBar==null || "".equals(itemBar.trim()) || merchItemMap.get("item_id").equals(itemBar)){
			merchItemMap.put("item_bar", merchItemMap.get("item_id"));
			// 添加1:1的商品属性 数据不全的卷烟 最小包装价格使用条价格 转换比为1 单位使用'条'
			Map<String, Object> newMerchItemUnitMap = new HashMap<String, Object>();
			newMerchItemUnitMap.put("merch_id", merchItemMap.get("merch_id"));
			newMerchItemUnitMap.put("seq_id", IDUtil.getId());
			newMerchItemUnitMap.put("item_id", merchItemMap.get("item_id"));
			newMerchItemUnitMap.put("item_bar", merchItemMap.get("item_id"));
			newMerchItemUnitMap.put("item_unit_name", merchItemMap.get("big_unit_name"));
			newMerchItemUnitMap.put("big_bar", merchItemMap.get("item_id"));
			newMerchItemUnitMap.put("big_unit_name", merchItemMap.get("big_unit_name"));
			newMerchItemUnitMap.put("unit_ratio", 1);
			newMerchItemUnitMap.put("pri4", merchItemMap.get("pri_drtl"));
			newMerchItemUnitList.add(newMerchItemUnitMap);
		} else {
			// 添加1:1的商品属性
			Map<String, Object> selfMerchItemUnitMap = new HashMap<String, Object>();
			selfMerchItemUnitMap.put("merch_id", merchItemMap.get("merch_id"));
			selfMerchItemUnitMap.put("seq_id", IDUtil.getId());
			selfMerchItemUnitMap.put("item_id", merchItemMap.get("item_id"));
			selfMerchItemUnitMap.put("item_bar", itemBar);
			selfMerchItemUnitMap.put("item_unit_name", merchItemMap.get("item_unit_name"));
			selfMerchItemUnitMap.put("big_bar", itemBar);
			selfMerchItemUnitMap.put("big_unit_name", merchItemMap.get("item_unit_name"));
			selfMerchItemUnitMap.put("unit_ratio", 1);
			selfMerchItemUnitMap.put("pri4", merchItemMap.get("pri4"));
			newMerchItemUnitList.add(selfMerchItemUnitMap);
			// 添加1:10的商品属性
			Map<String, Object> newMerchItemUnitMap = new HashMap<String, Object>();
			newMerchItemUnitMap.put("merch_id", merchItemMap.get("merch_id"));
			newMerchItemUnitMap.put("seq_id", IDUtil.getId());
			newMerchItemUnitMap.put("item_id", merchItemMap.get("item_id"));
			newMerchItemUnitMap.put("item_bar", itemBar);
			newMerchItemUnitMap.put("item_unit_name", merchItemMap.get("item_unit_name"));
			newMerchItemUnitMap.put("big_bar", merchItemMap.get("big_bar"));
			newMerchItemUnitMap.put("big_unit_name", merchItemMap.get("big_unit_name"));
			newMerchItemUnitMap.put("unit_ratio", merchItemMap.get("unit_ratio"));
			newMerchItemUnitMap.put("pri4", merchItemMap.get("pri_drtl"));
			newMerchItemUnitList.add(newMerchItemUnitMap);
		}
		return newMerchItemUnitList;
	}
	
	@Override
	public void updateOrInsertItems(Map<String, Object> paramMap)throws Exception {
		LOG.debug("ItemServiceImp updateOrInsertItems paramMap:"+paramMap);
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String itemId = MapUtil.getString(paramMap, "item_id");
		String itemBar = MapUtil.getString(paramMap, "item_bar");
		
		Map<String, Object> searchItem=new HashMap<String, Object>();
		searchItem.put("item_id", itemId);
		searchItem.put("merch_id", merchId);
		List<Map<String, Object>> itemList=this.searchMerchItem(searchItem);//查询是否有此商品
		
		Map<String, Object> tobaccoItemParam = new HashMap<String, Object>();
		tobaccoItemParam.put("supplier_id", MapUtil.getString(paramMap, "com_id", "10370101"));
		tobaccoItemParam.put("bar", itemBar);
		tobaccoItemParam.put("item_id", itemId);
		//查询卷烟表BASE_SUPPLIER_TOBACCO_ITEM
		List<Map<String, Object>> tobaccoItemList = itemDao.selectTobaccoItem(tobaccoItemParam);
		
		BigDecimal pri4 = MapUtil.getBigDecimal(paramMap, "pri4");
		BigDecimal pri1 = MapUtil.getBigDecimal(paramMap, "pri1");
		Map<String, Object> opertionItemMap = paramMap;//c插入：base_merch_item数据
		List<Map<String, Object>> opertionItemUnitList = new ArrayList<Map<String,Object>>();//插入： base_merch_item_unit数据
		
		
		if (!tobaccoItemList.isEmpty() ) {//是卷烟类商品，
			Map<String, Object> tobaccoItem = tobaccoItemList.get(0);
			BigDecimal unitRatio = MapUtil.getBigDecimal(tobaccoItem, "unit_ratio");
			if (itemId.equals(itemBar)) {//是条包装
				pri1 = pri1.divide(unitRatio);
				paramMap.put("pri1", pri1);
				paramMap.put("pri4", pri4.divide(unitRatio));
			}
			
			paramMap.put("big_pri4", MapUtil.getBigDecimal(paramMap, "pri4").multiply(unitRatio));
			
			//对于卷烟商品，只运行修改价格，
			tobaccoItem.putAll(MapUtil.rename(paramMap, "item_name", "pri1", "pri4", "big_pri4", "merch_id"));
			tobaccoItem.putAll(MapUtil.rename(tobaccoItem, "item_unit_name.unit_name" ));
			opertionItemMap = tobaccoItem;
			//条包装
			opertionItemUnitList.add(MapUtil.rename(opertionItemMap, "item_id", "item_bar", "big_bar", "item_unit_name", "seq_id:"+IDUtil.getId(), "merch_id", 
					"big_unit_name", "unit_ratio", "big_pri4.pri4"));
		}
		opertionItemMap.putAll(MapUtil.rename(opertionItemMap, "cost:"+pri1));
		Map<String, Object> itemUnit = new HashMap<String, Object>();
		itemUnit.putAll(MapUtil.rename(opertionItemMap, "item_id", "item_bar", "item_bar.big_bar", "item_unit_name", "seq_id:"+IDUtil.getId(), "merch_id", 
				"item_unit_name.big_unit_name", "pri4.pri4"));
		itemUnit.put("unit_ratio", "1");
		opertionItemUnitList.add(itemUnit);
		opertionItemMap.put("list", opertionItemUnitList);
		
		//新增
		if (itemList.isEmpty()) {
			this.createMerchItem(opertionItemMap);//插入
		} else {//修改
			itemDao.updateMerchItemUnit1(opertionItemUnitList);
			this.modifyMerchItemAndUnit(opertionItemMap); // 修改商品, 之前调用的方法只修改状态
		}
	}
	
	//修改商品信息
	@Override
	public void updateMerchItems(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl operateMerchItem paramMap: " + paramMap);
		paramMap.put("cost", MapUtil.getBigDecimal(paramMap, "cost", MapUtil.getBigDecimal(paramMap, "pri1")));
		paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
		itemDao.updateMerchItem(paramMap);
		itemDao.updateMerchItemUnitWithSelf(paramMap);
		itemDao.insertMerchItemUnit(paramMap);
		itemDao.deleteMerchItemUnit(paramMap);
	}
	
	//插入预警商品信息
	@Override
	public void insertMerchWarningItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl insertMerchWarningItem paramMap: " + paramMap);
		List<Map<String, Object>> itemList = new ArrayList<Map<String,Object>>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		List<Object> itemIdList = MapUtil.get(paramMap, "item_ids", new ArrayList<Object>());
		Map<String, Object> itemMap = null;
		for (Object itemId : itemIdList) {
			itemMap = new HashMap<String, Object>();
			itemMap.put("merch_id", merchId);
			itemMap.put("seq_id", IDUtil.getId());
			itemMap.put("item_id", itemId);
			itemList.add(itemMap);
		}
		itemDao.insertMerchWarningItem(itemList);
	}
	
	//查询商品预警信息
	@Override
	public List<String> searchMerchWarningItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl searchMerchWarningItem paramMap: " + paramMap);
		List<Map<String, Object>> warningItem = itemDao.selectMerchWarningItem(paramMap);
		List<String> itemIdList = new ArrayList<String>();
		for (Map<String, Object> map : warningItem) {
			itemIdList.add(MapUtil.getString(map, "item_id"));
		}
		return itemIdList;
	}
	
	//通过lucene查询
	@Override
	public Map<String, Object> searchItemByLucene(Map<String, Object> luceneParam) throws Exception{
//		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		Map<String, String> lcSearchParam = new HashMap<String, String>();
		
		String itemKindId = MapUtil.getString(luceneParam, "item_kind_id");
		String merchId = MapUtil.getString(luceneParam, "merch_id");
		String key = MapUtil.getString(luceneParam, "key");
		String pageIndex = MapUtil.getString(luceneParam, "page_index", "-1");
		String pageSize = MapUtil.getString(luceneParam, "page_size", "-1");
		
		lcSearchParam.put("currPage", pageIndex);
		lcSearchParam.put("pageSize", pageSize);
		if(!StringUtil.isBlank(itemKindId)){
			lcSearchParam.put("itemKindId", itemKindId);
		}
		if(!StringUtil.isBlank(merchId)){
			lcSearchParam.put("merchId", merchId);
		}
		if(!StringUtil.isBlank(key)){
			lcSearchParam.put("key", key);
		}
		
		String json = HttpUtil.post(RetailConfig.getLuceneServer()+"tobacco/retail/retailGoods/wildcardSearch/json", lcSearchParam);
		Map<String, Object> luceneResult = JsonUtil.json2Map(json);
		if(luceneResult!=null) {
			if(Constants.SUCCESS.equals(luceneResult.get("code"))) {
				
				Map<String, Object> result = (Map<String, Object>) luceneResult.get("result");
				return result;
//				List<Map<String, Object>> itemList = (List<Map<String, Object>>) result.get("item_list");
//				int index = 1;
//				for(Map<String, Object> item : itemList) {
//					if(index++==1) {
//						itemIdBuffer.append(item.get("item_id").toString());
//					} else {
//						itemIdBuffer.append(","+item.get("item_id").toString());
//					}
//				}
//				luceneParam.put("item_id", itemIdBuffer.toString());
//				luceneParam.put("page_index", result.get("page_index"));
//				luceneParam.put("page_size", result.get("page_size"));
//				luceneParam.put("page_count", result.get("page_count"));
//				luceneParam.put("count", result.get("count"));
//			
			}
		}
		return new  HashMap<String, Object>();
	}
	
	
	// 迁移智能终端1.0数据(sqlite)到微商盟后台(oracle): 商品, 库存, 合理库存
	@Override
	public void transferPOSData(Map<String, Object> paramMap) throws Exception{
		LOG.debug("ItemServiceImpl transferPOSData paramMap: " + paramMap);
		
		List<Map<String, Object>> transferMerchList = merchService.selectTransferMerch(paramMap);
		if(transferMerchList.isEmpty()){
			return;
		}
		Map<String, Object> transferMap = transferMerchList.get(0);
		
		String merchId = MapUtil.getString(paramMap, "merch_id");
		Map<String, Object> fileParamMap = new HashMap<String, Object>();
		fileParamMap.put("merch_id", merchId);
		fileParamMap.put("file_purpose", "88");
		
		List<Map<String, Object>> merchFileList = fileService.searchMerchFile(fileParamMap);
		if(merchFileList.isEmpty()) return;
		
		// dao层中默认排序是按时间倒序, 获取最新商户数据库文件的保存路径
		String fileLocation = MapUtil.getString(merchFileList.get(0), "file_location");
		Map<String, Object> goodsParam = new HashMap<String, Object>();
		goodsParam.put("db_path", RetailConfig.getNfsdataPath() + fileLocation);
//		goodsParam.put("db_path", "/g:/"+fileLocation);
		goodsParam.put("merch_id", merchId);
		List<Map<String, Object>> goodsList = null;
		LOG.debug(" = * = * = * = * = 读取的数据库文件路径 = * = * = * = * = " + RetailConfig.getNfsdataPath() + fileLocation);
		try {
			goodsList = itemDao.selectGoods(goodsParam);
		} catch (Exception e) {
			if(!e.getMessage().equals("no such table: goods")){
				throw e;
			}else{
				LOG.debug("=======================pos数据迁移错误：读取数据库错误，造成原因可能为：数据文件为2.0");
				return;
			}
		}
		
		List<Map<String, Object>> unitList = null;
		for (Map<String, Object> goods : goodsList) {
			List<Map<String, Object>> packList = new ArrayList<Map<String, Object>>();
			String itemKindId = MapUtil.getString(goods, "item_kind_id");
			// 处理包装信息: 卷烟
			if(ParamUtil.isTobacco(itemKindId)){
				if(StringUtil.isBlank(MapUtil.getString(goods, "item_bar"))) { // 异型烟只有1个包装
					packList.add(MapUtil.rename(goods, "seq_id:"+IDUtil.getId(), "merch_id", "item_id", "big_bar.item_bar", "big_bar", 
							"item_unit_name:条", "big_unit_name:条", "unit_ratio", "big_pri4.pri4"));
					goods.put("unit_name", "条");
					goods.put("big_unit_name", "条");
				} else { // 其他卷烟有2个包装
					packList.add(MapUtil.rename(goods, "seq_id:"+IDUtil.getId(), "merch_id", "item_id", "item_bar", "item_bar.big_bar",
							"item_unit_name:盒", "big_unit_name:盒", "unit_ratio", "pri4"));
					packList.add(MapUtil.rename(goods, "seq_id:"+IDUtil.getId(), "merch_id", "item_id", "item_bar", "big_bar",
							"item_unit_name:盒", "big_unit_name:条", "big_unit_ratio.unit_ratio", "big_pri4.pri4"));
					goods.put("unit_name", "盒");
					goods.put("big_unit_name", "条");
				}
			} else { // 非烟只有一个包装
				packList.add(MapUtil.rename(goods, "seq_id:"+IDUtil.getId(), "merch_id", "item_id", "item_bar", "big_bar", 
						"item_unit_name:", "big_unit_name:", "unit_ratio", "pri4"));
				goods.put("unit_name", "");
				goods.put("big_unit_name", "");
			}
			goods.put("list", packList);
			// 处理商品信息
			String itemName = MapUtil.getString(goods, "item_name");
			goods.put("short_code", SpellUtil.getFullSpell(itemName));
			goods.put("short_name", SpellUtil.getShortSpell(itemName));
			goods.put("create_date", DateUtil.getToday());
			goods.put("create_time", DateUtil.getCurrentTime().substring(8));
		}
		
		Map<String, Object>userMap=new HashMap<String, Object>();
		userMap.put("merch_id", merchId);
		userMap.put("page_index", -1);
		userMap.put("page_size", -1);
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(userMap);//用户下所有商品
		List<Map<String, Object>> intersectionList=new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> differenceList1=new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> differenceList2=new ArrayList<Map<String,Object>>();
		this.filterItem(goodsList, merchItemList, intersectionList, differenceList1, differenceList2);
		if(!differenceList1.isEmpty()){
			insertItems(differenceList1);
			Map<String, Object> whseMap = new HashMap<String, Object>();
			whseMap.put("merch_id", merchId);
			whseMap.put("list", differenceList1);

			whseDao.updateWhseMerch(whseMap);
		}
		
		if(!intersectionList.isEmpty()){
			itemDao.updateMoreMerchItemInfo(intersectionList);
			List<Map<String, Object>> allUnitListParam = new ArrayList<Map<String,Object>>();
			for (Map<String, Object> map : intersectionList) {
				unitList = (List<Map<String, Object>>) map.get("list");
				allUnitListParam.addAll(unitList);
			}
			itemDao.updateMerchItemUnitByIdBar(allUnitListParam);
			Map<String, Object> whseMap = new HashMap<String, Object>();
			whseMap.put("merch_id", merchId);
			whseMap.put("list", intersectionList);

			whseDao.updateWhseMerch(whseMap);
		}
		
		String newDate = DateUtil.getToday();
		String newTime = DateUtil.getCurrentTimeMillisAsString("HHmmss");
		Map<String, Object> updateTransferMap = new HashMap<String, Object>();
		updateTransferMap.put("merch_id", merchId);
		updateTransferMap.put("first_date", MapUtil.getString(transferMap, "first_date", newDate));
		updateTransferMap.put("first_time", MapUtil.getString(transferMap, "first_time", newTime));
		updateTransferMap.put("last_date",  newDate);
		updateTransferMap.put("last_time",  newTime);
		
		merchService.updateTransferMerch(updateTransferMap);
	}

	//插入多条商品(base_merch_item，base_merch_item_unit，whse_merch)
	public void insertItems(List<Map<String, Object>> paramList) throws Exception{
		LOG.debug("ItemServiceImpl insertItems paramList:" + paramList);
		List<Map<String, Object>> merchItemParamList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> merchItemUnitParamList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> paramMap : paramList) {
			Map<String, Object> itemParam = new HashMap<String, Object>();
			itemParam.put("merch_id", paramMap.get("merch_id"));
			itemParam.put("item_id", paramMap.get("item_id"));
			if(paramMap.get("qty_whse")==null) {
				paramMap.put("qty_whse", 0);
			}
			if(paramMap.get("discount")==null) {
				paramMap.put("discount", 100);
			}
			if(paramMap.get("pri1")==null) {
				paramMap.put("pri1", 0);
			}
			if(paramMap.get("cost")==null || BigDecimal.ZERO.compareTo(new BigDecimal(paramMap.get("cost").toString()))==0) {
				paramMap.put("cost", paramMap.get("pri1"));
			}
			paramMap.put("create_date", DateUtil.getToday());
			paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
			paramMap.put("status", "1");
			paramMap.put("modified_timestamp", DateUtil.getCurrentTimeMillis());
			merchItemParamList.add(paramMap);
			merchItemUnitParamList.addAll((List<Map<String, Object>>) paramMap.get("list"));
		}
		itemDao.insertMerchItem(merchItemParamList);
		itemDao.insertMerchItemUnit(merchItemUnitParamList);
		whseDao.insertWhseMerch(merchItemParamList);
	}	
	
	//非烟导入
	@Override
	public void importItemTable(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl importItemTable paramMap:" + paramMap);
		String path = "E:/home/RYX/excel/"+MapUtil.getString(paramMap, "lice_id")+".xls";
		List<Map<String, Object>> merchList = merchService.selectMerch(paramMap);
		if (merchList.size() < 1) return;
		String merchId = "";
		for (Map<String, Object> map : merchList) merchId = MapUtil.getString(map, "merch_id");
		
		List<Map<String, Object>> itemList = ExcelUtil.readExcel(2, 0, path);
		StringBuffer itemBarBf = new StringBuffer();
        List<Map<String, Object>> itemAllList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> map : itemList) {
			int index = 6;
			String itemId = IDUtil.getId();
			map.put("item_id", itemId);
			map.put("merch_id", merchId);
			Map<String, Object> itemMap = new HashMap<String, Object>();
			Map<String, Object> unitMap = null;
			List<Map<String, Object>> unitList = new ArrayList<Map<String,Object>>();
			itemMap.putAll(MapUtil.rename(map, "6.qty_whse", "3.pri1", "3.cost","1.item_name", "0.item_bar", "2.item_kind_id", "5.unit_name", "4.pri4", "item_id", "merch_id"));
			unitMap = new HashMap<String, Object>();
			unitMap.putAll(MapUtil.rename(map, "0.item_bar", "5.item_unit_name", "0.big_bar", "5.big_unit_name", "unit_ratio:1", "4.pri4", "item_id", "merch_id"));
			unitList.add(unitMap);
			while(true) {
				if (!map.containsKey((index+1)+"")) {
					break;
				}
				unitMap = new HashMap<String, Object>();
				unitMap.putAll(MapUtil.rename(map, "0.item_bar", "5.item_unit_name", index+1+".big_bar", index+3+".big_unit_name", index+4+".unit_ratio", index+2+".pri4", "item_id", "merch_id"));
				unitList.add(unitMap);
				itemMap.putAll(MapUtil.rename(unitMap, "big_unit_name", "big_bar", "pri4.big_pri4", "unit_ratio.big_unit_ratio"));
				index+=4;
			}
			if (!unitList.isEmpty()) {
				itemMap.put("list", unitList);
			}
			itemBarBf.append(MapUtil.getString(itemMap, "item_bar"));
			itemBarBf.append(",");
			itemAllList.add(itemMap);
		}
		
		Map<String, Object> searchItemParam = new HashMap<String, Object>();
        searchItemParam.put("merch_id", merchId);
        searchItemParam.put("item_bar", itemBarBf.toString());
        searchItemParam.put("page_size", -1);
        searchItemParam.put("page_index", -1);
        List<Map<String, Object>> searchItemList = this.searchMerchItem(searchItemParam);
        
        Map<String, Map<String, Object>> dataItemMap = new HashMap<String, Map<String,Object>>();
        for (Map<String, Object> map : searchItemList) dataItemMap.put(MapUtil.getString(map, "item_bar"), map);
        
        List<Map<String, Object>> delItemList = new ArrayList<Map<String,Object>>();
        List<Map<String, Object>> addItemList = new ArrayList<Map<String,Object>>();
        
        for (Map<String, Object> map : itemAllList) {
        	String itemBar = MapUtil.getString(map, "item_bar");
			if (dataItemMap.containsKey(itemBar)) {
				String itemId = MapUtil.getString(dataItemMap.get(itemBar), "item_id");
				map.put("item_id", itemId);
				List<Map<String, Object>> itemUnitList = MapUtil.get(map, "list", new ArrayList<Map<String, Object>>());
				for (Map<String, Object> map2 : itemUnitList) {
					map2.put("item_id", itemId);
				}
				delItemList.add(map);
			}
			addItemList.add(map);
		}
        if (!delItemList.isEmpty()) {
        	itemDao.deleteMerchItemUnit(delItemList);
        	itemDao.deleteMerchItem(delItemList);
        	whseDao.deleteWhseMerch(delItemList);
        }
        if (!addItemList.isEmpty()) insertItems(addItemList);
	}
	
	//非烟导出
	@Override
	public void exportItemTable(Map<String, Object> paramMap, OutputStream os) throws Exception {
		LOG.debug("ItemServiceImpl exportItemTable paramMap:" + paramMap);
		String liceId = MapUtil.getString(paramMap, "lice_id");
		List<Map<String, Object>> merchList = merchService.selectMerch(paramMap);
		if (merchList.size() < 1) return;
		String merchId = "";
		for (Map<String, Object> map : merchList) merchId = MapUtil.getString(map, "merch_id");
		Map<String, Object> itemParam = new HashMap<String, Object>();
        itemParam.put("merch_id", merchId);
        itemParam.put("page_index", "-1");
        itemParam.put("page_size", "-1");
        itemParam.put("not_item_kind_id", "01,0102");
        List<Map<String, Object>> itemList = itemDao.selectNoRemovedItemAndWhse(itemParam);
        List<Map<String, Object>> newItemList = getUnitName(itemList);
        
        String mouldPath = "E:/home/RYX/excel/Template/item.xls";//模板地址
        ExcelUtil.writeItemExcelByMould(mouldPath,  os, newItemList);
	}
	
	public List<Map<String, Object>> getUnitName(List<Map<String, Object>> itemList) throws Exception{
		List<Map<String, Object>> itemKindList = this.searchItemKind(new HashMap<String, Object>());
		Map<String, Object> itemKindMap = new HashMap<String, Object>();
		for (Map<String, Object> map : itemKindList) {
			itemKindMap.put(MapUtil.getString(map, "item_kind_id"), MapUtil.getString(map, "item_kind_name"));
		}
		for (Map<String, Object> map : itemList) {
			String itemKindId = MapUtil.getString(map, "item_kind_id");
			String itemKindName = itemKindId;
			if (itemKindMap.containsKey(itemKindId)) {
				itemKindName = MapUtil.getString(itemKindMap, itemKindId);
			}
			map.put("item_kind_name", itemKindName);
		}
		return itemList;
	}
	
	//卷烟导入
	@Override
	public void importTobaccoTable(Map<String, Object> paramMap) throws Exception {
		LOG.debug("ItemServiceImpl importTobaccoTable paramMap:" + paramMap);
		String path = "E:/home/RYX/excel/"+MapUtil.getString(paramMap, "lice_id")+".xls";
		List<Map<String, Object>> merchList = merchService.selectMerch(paramMap);
		if (merchList.size() < 1) return;
		String merchId = "";
		for (Map<String, Object> map : merchList) merchId = MapUtil.getString(map, "merch_id");
		List<Map<String, Object>> itemList = ExcelUtil.readExcel(1, 0, path);
		Map<String, Object> searchItemParam = new HashMap<String, Object>();
        searchItemParam.put("merch_id", merchId);
        searchItemParam.put("page_index", "-1");
        searchItemParam.put("page_size", "-1");
        List<Map<String, Object>> searchItemList = itemDao.selectNoRemovedItemAndWhse(searchItemParam);//查询商户商品包装
        Map<String, Map<String, Object>> dataItemMap = new HashMap<String, Map<String,Object>>();
        for (Map<String, Object> map : searchItemList) dataItemMap.put(MapUtil.getString(map, "big_bar"), map);
        
        List<Map<String, Object>> updItemList = new ArrayList<Map<String,Object>>();
        Map<String, Object> whseMap = new HashMap<String, Object>();
        StringBuilder bigBar = new StringBuilder();
        for (Map<String, Object> map : itemList) {
        	String itemBar = MapUtil.getString(map, "0");
			if (dataItemMap.containsKey(itemBar)) {
				updItemList.add(MapUtil.rename(map, "item_id:"+MapUtil.getString(dataItemMap.get(itemBar), "item_id"), "merch_id:"+merchId, "4.qty_whse"));
			} else {
				bigBar.append(itemBar+",");
				whseMap.put(itemBar, MapUtil.getString(map, "4", "0"));			
			}
		}
        
        Map<String, Object> tobaccoItemParam = new HashMap<String, Object>();
		tobaccoItemParam.put("supplier_id",  MapUtil.getString(merchList.get(0), "cgt_com_id", "10370101"));
		tobaccoItemParam.put("big_bar", bigBar);
		List<Map<String, Object>> tobaccoItemList = itemDao.selectTobaccoItem(tobaccoItemParam);
        List<Map<String, Object>> addItemList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> addItemUnitList = null;
		Map<String, Object> itemMap = null;
		for (Map<String, Object> map : tobaccoItemList) {
			itemMap = new HashMap<String, Object>();
			addItemUnitList = new ArrayList<Map<String,Object>>();
			map.put("pri4", MapUtil.getString(map, "pri_drtl"));
			map.put("cost", MapUtil.getString(map, "pri_wsale"));
			String itemBar = MapUtil.getString(map, "big_bar");
			if(!itemBar.equals(MapUtil.getString(map, "item_bar"))) {
				map.put("pri4", MapUtil.getBigDecimal(map, "pri_drtl").divide(MapUtil.getBigDecimal(map, "unit_ratio")));
				map.put("cost", MapUtil.getBigDecimal(map, "pri_wsale").divide(MapUtil.getBigDecimal(map, "unit_ratio")));
				addItemUnitList.add(MapUtil.rename(map,  "merch_id:"+merchId, "item_id", "item_bar", "big_bar", "item_unit_name", "big_unit_name", "pri_drtl.pri4", "unit_ratio"));
			}
			addItemUnitList.add(MapUtil.rename(map,  "merch_id:"+merchId, "item_id", "item_bar", "item_bar.big_bar", "item_unit_name", "item_unit_name.big_unit_name", "pri4" ));			
			itemMap.putAll(MapUtil.rename(map,  "merch_id:"+merchId, "item_id", "item_bar", "item_name", "item_kind_id", "item_unit_name.unit_name", "cost", "pri4", "pri4.pri1", "unit_ratio", "qty_whse:"+MapUtil.getString(whseMap, itemBar)));
			itemMap.put("list", addItemUnitList);
			addItemList.add(itemMap);
		}
        if (!updItemList.isEmpty()) whseDao.updateBatchWhseMerch(updItemList);
        
        if (!addItemList.isEmpty()) insertItems(addItemList);
	}
	
	//卷烟导出
	@Override
	public void exportTobaccoTable(Map<String, Object> paramMap,  OutputStream os) throws Exception {
		LOG.debug("ItemServiceImpl exportTobaccoTable paramMap:" + paramMap);
		
		String liceId = MapUtil.getString(paramMap, "lice_id");
		List<Map<String, Object>> merchList = merchService.selectMerch(paramMap);
		if (merchList.size() < 1) return;
		String merchId = "";
		for (Map<String, Object> map : merchList) merchId = MapUtil.getString(map, "merch_id");
		Map<String, Object> itemParam = new HashMap<String, Object>();
        itemParam.put("merch_id", merchId);
        itemParam.put("page_index", "-1");
        itemParam.put("page_size", "-1");
        itemParam.put("item_kind_id", "01,0102");
        List<Map<String, Object>> itemList = whseDao.selectNotRemovedMerchItemAndWhse(itemParam);
        List<Map<String, Object>> newItemList = this.getUnitName(itemList);
        
        String mouldPath = "E:/home/RYX/excel/Template/tobacco.xls";//模板地址
        ExcelUtil.writeTobaccoExcelByMould(mouldPath, os, newItemList);
        
	    /*
		String liceId = MapUtil.getString(paramMap, "lice_id");
		List<Map<String, Object>> merchList = merchService.selectMerch(paramMap);
		if (merchList.size() < 1) return;
		String merchId = "";
		for (Map<String, Object> map : merchList) merchId = MapUtil.getString(map, "merch_id");
		Map<String, Object> itemParam = new HashMap<String, Object>();
        itemParam.put("merch_id", merchId);
        itemParam.put("page_index", "-1");
        itemParam.put("page_size", "-1");
        itemParam.put("item_kind_id", "01,0102");		    
        List<Map<String, Object>> itemList = whseService.selectNotRemovedMerchItemAndWhse(itemParam);
		
	    String[][] headers = new String[][] {
				new String[] {"item_bar", "条码"},
				new String[] {"item_name", "商品名称"},
				new String[] {"item_kind_id", "商品类型"},
				new String[] {"unit_name", "单位名称"},
				new String[] {"qty_whse", "单位数量"},
		};
	    
		ExcelUtil.write(headers, itemList, os, ExcelUtil.FIRST, ExcelUtil.EMPTY_HEADERNMAE);
	    */
	}
	
}
