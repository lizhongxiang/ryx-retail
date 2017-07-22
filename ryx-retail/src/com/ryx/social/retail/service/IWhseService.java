package com.ryx.social.retail.service;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public interface IWhseService {
	
	public List<Map<String, Object>> searchWhseMerch(Map<String, Object> whseMerchParam) throws Exception;
	public List<Map<String, Object>> searchMerchItemUnitJoinMerchItemJoinWhseMerch(Map<String, Object> merchItemUnitJoinMerchItemJoinWhseMerchParam) throws Exception;
	public void modifyWhseMerch(Map<String, Object> whseMerchParam) throws Exception;
	public void modifyBatchWhseMerch(List<Map<String, Object>> whseMerchParamList) throws Exception;
	public Map<String, Object> searchMerchWhseQuantityAndAmountByKind(Map<String, Object> paramMap) throws Exception;
	/**
	 * 获取原始库存列表
	 */
	public List getOriginWhseList(Map map) throws Exception;
	
	public List<Map<String, Object>> searchMerchItemAndReasonWhse(Map<String,Object> paramMap) throws Exception;
	
	public List getTakeStock(Map map) throws Exception;
	
	//获取盘点明细
	public List getTakeStockXiangXi(Map map) throws Exception;
	
	public List getWhseTurn(Map map ) throws Exception ;

	public void updateOriginWhseList(Map map) throws Exception;

	public void modifyWhseWarnQuantity(Map<String, Object> map) throws Exception;
	
	public void createWhseTurn(Map<String, Object> turnParam) throws Exception;
	
	public void insertWhseMerch(Map<String, Object> whseParams) throws Exception;
	
	public void updateWhseMerch(Map<String, Object> whseParam) throws Exception;
	
	public List getWhseTurnXiangXi(Map map ) throws Exception;
	/*public List<Map<String, Object>> searchMerchItemJoinWhseMerchByLucene(Map<String, Object> paramMap) throws Exception;*/
	
	/**
	 * 按条码查询商品库存信息
	 * @param paramMap
	 * @author 朱鹏
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> findMerchItemAndWhseMerchByLucene(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * description 入库 分类为烟的盘点数据数据上传至RTMS
	 * */
//	public Map<String,Object> insertWhseTobaccoToRTMS(Map<String,Object> paramMap);
	/**
	 * 修改库存，并生成盘查，通过tobacco service
	 * @param paramMap
	 * @author 李钟祥
	 * @return
	 * @throws Exception
	 */
	public Object modifyWhseByTBCSVR(Map<String, Object> paramMap)throws Exception;
	/**
	 * 合理库存 搜索(建议量)
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchItemJoinAdvWhseByLucene(Map<String, Object> paramMap) throws Exception;
	/**
	 * * 从merch_item_unit查到merch_item再查到whse_merch, 认为是用seq_id或者big_bar查询的,所以只有一个item_id
	 * 用于库存盘点
	 * @param merchItemUnitJoinMerchItemJoinWhseMerchParam
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchMerchItemUnitJoinMerchItemJoinWhseMerchForWhse( Map<String, Object> merchItemUnitJoinMerchItemJoinWhseMerchParam)throws Exception;
	/**
	 * 修改库存显示状态（0：显示为负库存，1：显示为正库存
	 * @param paramMap
	 * @throws Exception
	 */
	public void updateShowWhseStatus(Map<String, Object> paramMap) throws Exception;
	/**
	 * 查询异常库存列表
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> findUnqualifiedMerchantInventoryList(Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 获得库存（商品和库存）
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectNotRemovedMerchItemAndWhse(Map<String, Object> paramMap) throws Exception;
	
}