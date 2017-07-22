package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IItemDao {
	/**
	 * 查询非烟库商品
	 * @param itemParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectItem(Map<String, Object> itemParam) throws Exception;
	/**
	 * 查询已删除的商品信息(不包括库存信息)
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectRemovedItem(Map<String, Object> itemParam) throws Exception;
	/**
	 * 查询未删除的商品信息(不包括库存信息)-下架或正常（0：删除，1：正常，2：下架
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectNoRemovedItem(Map<String, Object> itemParam) throws Exception;
	/**
	 * 查询已删除的商品信息及库存信息
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectRemovedItemAndWhse(Map<String, Object> itemParam) throws Exception;
	/**
	 * 查询未删除的商品信息及库存信息
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectNoRemovedItemAndWhse(Map<String, Object> itemParam) throws Exception;
	public List<Map<String, Object>> selectItemUnit(Map<String, Object> itemUnitParam) throws Exception;
	public List<Map<String, Object>> selectItemKind(Map<String, Object> itemKindParam) throws Exception;
	public List<Map<String, Object>> selectMerchItem(Map<String, Object> merchItemParam) throws Exception;
	/**
	 * 查询未删除的商品信息(不包括库存和包装)
	 * @param merchItemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectRemovedMerchItem(Map<String, Object> merchItemParam) throws Exception;
	/**
	 * 查询未删除的商品信息(不包括库存和包装)
	 * @param merchItemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectNotRemovedMerchItem(Map<String, Object> merchItemParam) throws Exception;
	public List<Map<String, Object>> selectMerchItemUnit(Map<String, Object> merchItemUnitParam) throws Exception;
	public List<Map<String, Object>> selectTobaccoItem(Map<String, Object> tobaccoItemParam) throws Exception;
	public void insertMerchItem(Map<String, Object> merchItemParam) throws Exception;
	public void insertMerchItem(List<Map<String, Object>> merchItemParam) throws Exception;
	public void insertMerchItemUnit(List<Map<String, Object>> merchItemUnitParam) throws Exception;
	public void insertMerchItemUnit(Map<String, Object> merchItemUnitParam) throws Exception;
	
	public void updateMerchItem(Map<String, Object> merchItemParam) throws Exception;
	public void updateMerchItemUnit(List<Map<String, Object>> merchItemUnitParam) throws Exception;
	public void updateMerchItemUnit(Map<String, Object> merchItemUnitParam) throws Exception;
	
	public void deleteMerchItem(Map<String, Object> itemParam) throws Exception;
	public void deleteMerchItemUnit(List<Map<String, Object>> itemParam) throws Exception;
	public void deleteMerchItemUnit(Map<String, Object> itemParam) throws Exception;
	
	public void insertMerchItemUnitWithSelf(Map<String, Object> itemParam) throws Exception;
	public void updateMerchItemWithStatus(Map<String, Object> itemParam) throws Exception;
	public void updateMerchItemUnitWithSelf(Map<String, Object> itemParam) throws Exception;
	
	public List<Map<String, Object>> searchGoods(Map<String, Object> goodsRequest) throws Exception;
	
	// 非烟/卷烟入库使用  改变成本价和采购价
	public void updateMerchItemCostAndPri1(List<Map<String, Object>> merchItemParamList) throws Exception;
	// 非烟/卷烟入库使用 改变成本价
	public void updateMerchItemCost(List<Map<String, Object>> itemParamList) throws Exception;
	// 非烟/卷烟入库使用 改变采购价
	public void updateMerchItemPri1(List<Map<String, Object>> merchItemParamList) throws Exception;
	/**
	 * pos商品推荐
	 * @param recommendMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectRecommendedItem(Map<String, Object> recommendMap)throws Exception;
	/**
	 * 修改BASE_MERCH_ITEM_UNIT,只能一次修改一次，
	 * @param itemUnit
	 * @throws Exception
	 */
	public void updateMerchItemUnit1(List<Map<String, Object>> paramList) throws Exception;
	/**
	 * 得到推荐商品的详细信息
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchRecommendedPOSItemInfo(Map<String, Object> paramsMap) throws Exception;
	
	/**
	 * 查询预警商品信息
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMerchWarningItem(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 插入预警商品信息
	 * @param paramList
	 * @throws Exception
	 */
	public void insertMerchWarningItem(List<Map<String, Object>> paramList)throws Exception;
	
	public List<Map<String, Object>> selectGoods(Map<String, Object> paramMap)throws Exception;
	public List<Map<String, Object>> selectMerchItemById(Map<String, Object> paramMap)throws Exception;
	public void updateMerchItemUnitByIdBar(List<Map<String, Object>> merchItemUnitParam)throws Exception;
	public void updateMoreMerchItemInfo(List<Map<String, Object>> merchItemParamList)throws Exception;
	/**
	 * 删除商品
	 * @param paramList
	 * @throws Exception
	 */
	public void deleteMerchItem(List<Map<String, Object>> paramList) throws Exception;
	
}
