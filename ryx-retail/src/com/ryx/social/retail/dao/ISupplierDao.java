package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface ISupplierDao {
	public List<Map<String, Object>> getItemList(Map<String, String> param) throws Exception;

	public void insertItem(Map<String, String> param) throws Exception;

	public void insertItems(String comId, List<Map<String, Object>> params) throws Exception;
	/**
	 * 获取所有烟草供应商烟草数据
	 * @author 徐虎彬
	 * @date 2014年3月3日
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAllItemList() throws Exception;
	
	/**
	 * 修改本地烟草供应商烟草数据
	 * @author 徐虎彬
	 * @date 2014年3月3日
	 * @param itemList 网站截取数据
	 * @throws Exception
	 */
	public void updateItems(List<Map<String,Object>> itemList)throws Exception; 
	/**
	 * 修改本地烟草供应商烟草数据(全拼简拼)
	 * @author 徐虎彬
	 * @date 2014年3月3日
	 * @param itemList 网站截取数据
	 * @throws Exception
	 */
	public void updateItems(List<Map<String,Object>> itemList,String string)throws Exception; 
	/**
	 * 更新本地烟草供应商烟草数据
	 * @author 徐虎彬
	 * @date 2014年3月12日
	 * @param itemList 烟草数据库获取
	 * @throws Exception
	 */
	public void updateItems2(List<Map<String,Object>> itemList)throws Exception;
	/**
	 * 条件查询卷烟供应商商品信息
	 * @author 徐虎彬
	 * @date 2014年3月10日
	 * @param listMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectItem(Map<String,Object> listMap) throws Exception;

	public List<Map<String, Object>> getTobaccoItem(String itemId) throws Exception;
}
