package com.ryx.social.retail.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IItemBarRelevanceDao;
import com.ryx.social.retail.service.IItemBarRelevanceService;

@Service
public class ItemBarRelevanceServiceImpl implements IItemBarRelevanceService {

	private Logger LOG = LoggerFactory
			.getLogger(ItemBarRelevanceServiceImpl.class);

	@Resource
	private IItemBarRelevanceDao itemBarRelevanceDao;

	/**
	 * 根据主item_bar查询对应的一条数据
	 */
	@Override
	public List<Map<String, Object>> searchItemBarRelevance(Map<String, Object> itemBarRelevanceParam) throws Exception {
		LOG.debug("ItemBarRelevanceServiceImpl searchItemBarRelevance itemBarRelevanceParam: "+ itemBarRelevanceParam);
		List<Map<String, Object>> data = itemBarRelevanceDao.selectReferenceBar(itemBarRelevanceParam);
		String referencebar = null;
		for (Map<String, Object> map : data) {
			referencebar = MapUtil.get(map, "REFERENCEBAR", "");
			if (!StringUtil.isBlank(referencebar) && !referencebar.equals("[]")) {
				//put使用大写，否则存在两个
				map.put("REFERENCEBAR", JsonUtil.json2List(referencebar));
			} else {
				map.put("REFERENCEBAR", new ArrayList<String>());
			}
		}
		return data;
	}
	
	/**
	 * 查询全部
	 */
	@Override
	public List<Map<String, Object>> searchItemBarRelevanceList(
			Map<String, Object> itemParam) throws Exception {

		LOG.debug("ItemBarRelevanceServiceImpl searchItemBarRelevanceList itemBarRelevanceParam: "
				+ itemParam);
		List<Map<String, Object>> data = itemBarRelevanceDao
				.selectItemBarReferenceBarList(itemParam);
		String referencebar = null;
		for (Map<String, Object> map : data) {
			referencebar = MapUtil.get(map, "referencebar", "");
			if (!StringUtil.isBlank(referencebar) || !referencebar.equals("[]")) {
				// referencebar
				map.put("REFERENCEBAR", JsonUtil.json2List(referencebar));
			} else {
				map.put("REFERENCEBAR", new ArrayList<String>());
			}
		}
		return data;
	}

	/**
	 * 添加
	 */
	@Override
	public void insertItemBarRelevance(Map<String, Object> itemParam)
			throws Exception {
		LOG.debug("ItemBarRelevanceServiceImpl insertItemBarRelevance itemParam: "
				+ itemParam);
		String referencebar = JsonUtil.list2json((List<String>) itemParam
				.get("referencebar"));
		itemParam.put("referencebar", referencebar);
		itemBarRelevanceDao.insertItemBarRelevance(itemParam);
	}

	/**
	 * 删除推荐商品
	 */
	@Override
	public void deleteItemBarRelevance(Map<String, Object> itemParam)
			throws Exception {
		LOG.debug("ItemBarRelevanceServiceImpl deleteItemBarRelevance itemParam: "
				+ itemParam);
		itemBarRelevanceDao.deleteItemBarRelevance(itemParam);
	}

	/**
	 * 修改
	 */
	@Override
	public void updateItemBarRelevance(Map<String, Object> itemParam)
			throws Exception {
		LOG.debug("ItemBarRelevanceServiceImpl updateItemBarRelevance itemParam: "
				+ itemParam);
		String referencebar = JsonUtil.list2json((List<String>) itemParam
				.get("referencebar"));
		itemParam.put("referencebar", referencebar);
		itemBarRelevanceDao.updateItemBarRelevance(itemParam);
	}
}
