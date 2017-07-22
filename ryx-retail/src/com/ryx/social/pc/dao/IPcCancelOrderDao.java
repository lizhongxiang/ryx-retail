package com.ryx.social.pc.dao;

import java.util.List;
import java.util.Map;

public interface IPcCancelOrderDao {

	/**
	 * 得到之后时间的盘点记录单
	 * @param whseMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchWhseOrderByTime(Map<String, Object> whseMap)throws Exception;
	/**
	 * 更新库存
	 * @param whseList
	 * @throws Exception
	 */
	public void updateWhseMerchQtyWhse(List<Map<String, Object>> whseList)throws Exception;
	/**
	 * 查找销售单，通过编号
	 * @param saleMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleOrderById(Map<String, Object> saleMap)throws Exception;
	/**
	 * 查询销售单行，通过编号
	 * @param saleMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchSaleOrderLineById(Map<String, Object> saleMap) throws Exception;
	/**
	 * 删除销售单
	 * @param saleMap
	 * @throws Exception
	 */
	public void deleteSaleOrder(Map<String, Object> saleMap) throws Exception;
	/**
	 * 删除销售单行
	 * @param saleMap
	 * @throws Exception
	 */
	public void deleteSaleOrderLine(Map<String, Object> saleMap) throws Exception;
	/**
	 * 得到退货单
	 * @param returnMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchReturnOrderById(Map<String, Object> returnMap) throws Exception;
	/**
	 * 删除退货单
	 * @param saleMap
	 * @throws Exception
	 */
	public void deleteReturnOrder(Map<String, Object> saleMap) throws Exception;
	/**
	 * 得到盘点单
	 * @param turnMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searWhseTurnByIdSql(Map<String, Object> turnMap)throws Exception;
	/**
	 * 得到盘点单行
	 * @param turnMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searWhseTurnLineByIdSql(Map<String, Object> turnMap) throws Exception;
	/**
	 * 删除盘点单
	 * @param whseTurnMap
	 * @throws Exception
	 */
	public void deleteWhseTurn(Map<String, Object> whseTurnMap) throws Exception;
	/**
	 * 删除销售单行
	 * @param whseTurnMap
	 * @throws Exception
	 */
	public void deleteWhseTurnLine(Map<String, Object> whseTurnMap) throws Exception;
	/**
	 * 删除入库单
	 * @param puchOrderMap
	 * @throws Exception
	 */
	public void deletePuchOrder(Map<String, Object> puchOrderMap) throws Exception;
	/**
	 * 删除入库单行
	 * @param puchOrderMap
	 * @throws Exception
	 */
	public void deletePuchOrderLine(Map<String, Object> puchOrderMap) throws Exception;
	/**
	 * 得到采购单行
	 * @param turnMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searPuchOrderLineByIdSql(Map<String, Object> turnMap) throws Exception;
	/**
	 * 得到采购单
	 * @param puchMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> searPuchOrderByIdSql(Map<String, Object> puchMap)throws Exception;
 
	

}
