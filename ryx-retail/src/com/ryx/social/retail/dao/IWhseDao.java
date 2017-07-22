package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface IWhseDao {
	public List<Map<String, Object>> selectWhseMerch(Map<String, Object> whseParam) throws Exception;
	/**
	 * 查询已删除的商品基础信息和库存信息
	 * @param whseParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectRemovedWhseMerchAndMerchItem(Map<String, Object> whseParam) throws Exception;
	/**
	 * 查询未删除的商品基础信息和库存信息
	 * @param whseParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectNotRemovedWhseMerchAndMerchItem(Map<String, Object> whseParam) throws Exception;
	/**
	 * 查询已删除的商品信息
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectRemovedItem(Map<String, Object> itemParam) throws Exception;
	/**
	 * 查询未删除的商品信息
	 * @param itemParam
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectNoRemovedItem(Map<String, Object> itemParam) throws Exception;
	public List<Map<String, Object>> selectWhseMerchJoinMerchItem(Map<String, Object> whseParam) throws Exception;
	public List<Map<String, Object>> selectWhseInfo(Map<String, Object> whseParam) throws Exception;
	public List<Map<String, Object>> selectWhseTurnInfo(Map<String, Object> whseTurnParam) throws Exception;
	public List<Map<String, Object>> selectWhseTurnCount(Map<String, Object> whseTurnParam) throws Exception;
	public void updateWhseMerch(Map<String, Object> whseMerchParam) throws Exception;
	public void updateBatchWhseMerch(List<Map<String, Object>> whseMerchParamList) throws Exception;
	public void insertWhseMerch(List<Map<String, Object>> whseMerchParam) throws Exception;
	
	public List<Map<String, Object>> getWhseList(Map<String, Object> whseParams) throws Exception;

	public List getOriginWhseList(Map<String, Object> map) throws Exception;

	public void updateOriginWhseList(Map map) throws Exception;

	public List searchMerchItemAndWhse(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询未删除的商品信息和库存信息
	 * @param map
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List selectNotRemovedMerchItemAndWhse(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询已删除的商品信息和库存信息
	 * @param map
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List selectRemovedMerchItemAndWhse(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> searchMerchWhseQuantityAndAmountByKind(Map<String, Object> paramMap) throws Exception;

	public void updateWhseWarnQuantity(Map<String, Object> map) throws Exception;
	
	public List getOriginWhseLists(Map<String,Object> map) throws Exception;
	/**
	 * 库存盘点功能
	 * @param map
	 * @throws Exception
	 */
	public void insertWhseTurn(Map<String, Object> turnParam) throws Exception;
	public void insertWhseTurnLine(Map<String, Object> turnLineParam) throws Exception;
	public void updateInventory(Map map) throws Exception;
	
	/**
	 * 盘点记录
	 */
	public List getTakeStock(Map<String, Object> pdmap) throws Exception;
	public List getTakeStockXiangXi(String itemId,String merchId,String beginTime,String endTime) throws Exception;
	
	public void insertWhseMerch(Map<String, Object> whseParam) throws Exception;
	public void updateWhseMerchSimple(List<Map<String, Object>> whseParams) throws Exception;
	
	/**
	 *订单入库-插入订单 
	 * @author 徐虎彬
	 * @date 2014年2月19日
	 * @throws Exception
	 */
	public void insertOrder(Map map) throws Exception;
	
	/**
	 * 订单入库-查询以入库订单列表
	 * @author 徐虎彬
	 * @date 2014年2月19日
	 * @param Map MERCH_ID
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectOrderList(Map map) throws Exception;
	
	/**
	 * 修改库存
	 * @author 徐虎彬
	 * @date 2014年2月19日
	 * @param List<Map<String, String>> MERCH_ID ITEM_ID QTY_WHSE
	 * @throws Exception
	 */
	public void updateWhse(List<Map<String, Object>> map) throws Exception;
	/**
	 * 查询是否含有库存（0库存也是有库存）
	 * @author 徐虎彬
	 * @date 2014年2月19日
	 * @param map MERCH_ID ITEM_ID
	 * @return 只要根据MERCH_ID ITEM_ID可以查到数据即返回true
	 * @throws Exception
	 */
	public boolean selectWhse(Map map) throws Exception;
	
	// 删除商户商品的库存信息, 商品管理使用
	public void deleteWhseMerch(Map<String, Object> whseMerchParam) throws Exception;
	//库存报表
	public List<Map<String, Object>> stockReport(Map<String, Object> stockMap)throws Exception;
	
	public List getWhseTurn(Map<String, Object> pdmap ) throws Exception;
	public List getTakeStockXiangXi(Map<String, Object> turnMap) throws Exception;
	public List<Map<String, Object>> selectWhseTurn(Map<String, Object> whseTurnParam) throws Exception;
	public List<Map<String, Object>> searchWhseTurnLineByItem(Map<String, Object> whseTurnLineParam) throws Exception;
	
	/**
	 * 删除商品库存（物理
	 * @param paramList
	 * @throws Exception
	 */
	public void deleteWhseMerch(List<Map<String, Object>> paramList) throws Exception;
}

