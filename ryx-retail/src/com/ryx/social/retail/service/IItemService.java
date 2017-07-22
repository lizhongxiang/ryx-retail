package com.ryx.social.retail.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IItemService {
	public List<Map<String, Object>> searchItem(Map<String, Object> itemParam) throws Exception;
	public List<Map<String, Object>> searchItemUnit(Map<String, Object> itemUnitParam) throws Exception;
	public List<Map<String, Object>> searchItemKind(Map<String, Object> itemKindParam) throws Exception;
	public List<Map<String, Object>> searchMerchItem(Map<String, Object> merchItemParam) throws Exception;
	// 先查询merch_item表, 如果没有查询到则查询item表, 如果都没有返回null
	public List<Map<String, Object>> searchMerchItemAndSoOn(Map<String, Object> itemParam) throws Exception;
	/**
	 * 扫码查询
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchItemBySaoMa(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询商户已删除的商品
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchRemovedItem(Map<String, Object> itemParam) throws Exception;
	/**
	 * 查询商户未删除的商品
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchNoRemovedItem(Map<String, Object> itemParam) throws Exception;
	// 比上个方法加了库存信息
	public List<Map<String, Object>> searchMerchItemJoinWhseAndSoOn(Map<String, Object> itemParam) throws Exception;
	// 商品销售时使用先添加卷烟商品, 再查询
	public List<Map<String, Object>> searchMerchItemWithIncrease(Map<String, Object> merchItemParam) throws Exception;
	// 比上一个方法加了库存信息
	public List<Map<String, Object>> searchMerchItemJoinWhseWithIncrease(Map<String, Object> merchItemParam) throws Exception;
	// 根据seq_id, item_id, item_bar, big_bar先查询merch_item_unit再查询merch_item
	public List<Map<String, Object>> searchMerchItemUnitJoinMerchItem(Map<String, Object> merchItemUnitJoinMerchItemParam) throws Exception;
	// 根据item_id, item_bar先查询merch_item再查询merch_item_unit
	public List<Map<String, Object>> searchMerchItemJoinMerchItemUnit(Map<String, Object> merchItemJoinMerchItemUnitParam) throws Exception;
	// 根据item_id逻辑删除商户商品
	public void removeMerchItem(Map<String, Object> merchItemParam) throws Exception;
	// 修改商户商品
	public void modifyMerchItem(Map<String, Object> merchItemParam) throws Exception;
	// 修改商户商品和商品属性
	public void modifyMerchItemAndUnit(Map<String, Object> merchItemParam) throws Exception;
	// 新增非烟商品
	public void createMerchItem(Map<String, Object> paramMap) throws Exception;
	// 新增卷烟商品
	public void createMerchTobaccoItem(Map<String, Object> paramMap) throws Exception;
	// 批量新增卷烟商品
	public void createMerchTobaccoItem(List<Map<String, Object>> paramMap) throws Exception;
	
	public void operateMerchItem(Map<String, Object> paramMap) throws Exception;
	
	public List getLuceByKey(Map paramsMap) throws Exception;
	public void updateLuce() throws Exception;
	public List<Map<String, Object>> searchMerchItemByLucene(Map<String, Object> luceneParam) throws Exception;
	/**
	 * 使用lucene查询商品信息
	 * @param luceneParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> findMerchItemByLucene(Map<String, Object> luceneParam) throws Exception;
	public List<Map<String, Object>> searchMerchAndTobaccoItem(Map<String, Object> paramMap) throws Exception;
	public List<Map<String, Object>> searchMerchAndTobaccoItemWithIncrease(Map<String, Object> merchItemParam) throws Exception;
	
	/**
	 * 初始化数据
	 * @author 徐虎彬
	 * @date 2014年4月14日
	 * @param dataParam
	 * @throws Exception
	 */
    public void initData(Map<String,Object> dataParam)throws Exception;
    /**
     *  商品推荐
     * @param recommendMap
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> searchRecommendedItem(Map<String, Object> recommendMap)throws Exception;
   /**
	 * pos商品推荐接口
	 */
	public List<Map<String, Object>> searchRecommendedPOSItem(Map<String, Object> recommendMap) throws Exception;
	/**
	 * 先查询--然后才选择插入或修改
	 * @param ItemMap
	 * @return
	 * @throws Exception
	 */
	public void updateOrInsertItems(Map<String, Object> itemMap)throws Exception;
	public List<Map<String, Object>> searchMerchItemUnit(Map<String, Object> unitParam) throws Exception;
	/**
	 * 修改商品信息
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateMerchItems(Map<String, Object> paramMap) throws Exception;
	/**
	 * 插入商品预警信息
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertMerchWarningItem(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询预警商品信息
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<String> searchMerchWarningItem(Map<String, Object> paramMap)throws Exception;
	
	/**
	 * 通过luce查询数据，返回结果
	 * @param luceneParam
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> searchItemByLucene(Map<String, Object> luceneParam)throws Exception;
	
	/**
	 * pos数据对接
	 * 将pos数据库数据同步到tobacco数据库中（库存，商品，包装）
	 * @param paramMap
	 * @throws Exception
	 */
	public void transferPOSData(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 导出非烟数据
	 * @param paramMap
	 * @throws Exception
	 */
	public void exportItemTable(Map<String, Object> paramMap, OutputStream os) throws Exception;
	
	/**
	 * 导入非烟数据
	 * @param paramMap
	 * @throws Exception
	 */
	public void importItemTable(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 导出卷烟
	 * @param paramMap
	 * @throws Exception
	 */
	public void exportTobaccoTable(Map<String, Object> paramMap,  OutputStream os) throws Exception;
	/**
	 * 导入卷烟
	 * @param paramMap
	 * @throws Exception
	 */
	public void importTobaccoTable(Map<String, Object> paramMap) throws Exception;
}
