package com.ryx.social.retail.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IPromotionDao;
import com.ryx.social.retail.service.IPromotionService;


@Service
public class PromotionServiceImpl implements IPromotionService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PromotionServiceImpl.class);
	
	@Resource
	private IPromotionDao promotionDao;
	
	private Pattern ceilingAmountPattern = Pattern.compile("ceiling.{3,5}(\\d+)");

	//查询促销信息
	@Override
	public List<Map<String, Object>> searchMerchPromotion(Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchMerchPromotion paramMap :"+paramMap);
		List<Map<String, Object>> data = promotionDao.searchMerchPromotion(paramMap);
		String promotionMust = null;
		String promotionShould = null;
		String promotionAction = null;
		for (Map<String, Object> map : data) {
			promotionMust = MapUtil.get(map, "promotion_must", "");
			promotionShould = MapUtil.get(map, "promotion_should", "");
			promotionAction = MapUtil.get(map, "promotion_action", "");
			if(!StringUtil.isBlank(promotionMust) && !promotionMust.equals("{}")){
				map.put("promotion_must", JsonUtil.json2Map(promotionMust));
			}else{
				map.put("promotion_must", new HashMap<String, Object>());
			}
			if(!StringUtil.isBlank(promotionShould) && !promotionShould.equals("{}")){
				map.put("promotion_should", JsonUtil.json2Map(promotionShould));
			}else{
				map.put("promotion_should", new HashMap<String, Object>());
			}
			if(!StringUtil.isBlank(promotionAction) && !promotionAction.equals("{}")){
				map.put("promotion_action", JsonUtil.json2Map(promotionAction));
			}else{
				map.put("promotion_action", new HashMap<String, Object>());
			}
		}
		return data;
	}
	
	//插入促销信息
	@Override
	public void insertMerchPromotion(Map<String, Object> paramMap) throws Exception {
		LOG.debug("insertMerchPromotion paramMap :"+paramMap);
		String promotionMust = JsonUtil.object2json(paramMap.get("promotion_must"));
		String promotionShould = JsonUtil.object2json(paramMap.get("promotion_should"));
		String promotionAction = JsonUtil.object2json(paramMap.get("promotion_action"));
		paramMap.put("promotion_must", promotionMust);
		paramMap.put("promotion_should", promotionShould);
		paramMap.put("promotion_action", promotionAction);
		modifyCeilingReductionInsistent(paramMap);
		promotionDao.insertMerchPromotion(paramMap);
	}
	
	//修改促销信息
	@Override
	public void updateMerchPromotion(Map<String, Object> paramMap) throws Exception {
		LOG.debug("updateMerchPromotion paramMap :"+paramMap);
		modifyCeilingReductionInsistent(paramMap);
		promotionDao.updateMerchPromotion(paramMap);
	}
	
	/**
	 * 满减促销的优先级 设置为 用户选择+促销金额 为了可以实现 自动按照金额排序的逻辑
	 */
	private void modifyCeilingReductionInsistent(Map<String, Object> paramMap) {
		String insistent = MapUtil.getString(paramMap, "is_insistent");
		String promotionType = MapUtil.getString(paramMap, "promotion_type");
		String promotionAction = JsonUtil.object2json(paramMap.get("promotion_action"));
		if("40".equals(promotionType)) {
			Matcher ceilingAmountMatcher = ceilingAmountPattern.matcher(promotionAction);
			if(ceilingAmountMatcher.find()) {
				String ceilingAmount = ceilingAmountMatcher.group(1);
				ceilingAmount = StringUtils.leftPad(ceilingAmount, 16, "0");
				paramMap.put("is_insistent", insistent+ceilingAmount);
			}
		}
	}
	
	//修改促销信息
	@Override
	public void removeMerchPromotion(Map<String, Object> paramMap) throws Exception {
		LOG.debug("removeMerchPromotion paramMap :"+paramMap);
		paramMap.put("status", "0");
		this.updateMerchPromotion(paramMap);
	}
	
	
	// 查询促销流水
	@Override
	public List<Map<String, Object>> selectMerchPromotionRecord(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectMerchPromotionRecord paramMap: " + paramMap);
		return promotionDao.selectMerchPromotionRecord(paramMap);
	}
	
	// 批量插入促销流水
	@Override
	public void insertMerchPromotionRecord(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("insertMerchPromotionRecord paramList: " + paramList);
		String time = DateUtil.getCurrentTime().substring(8);
		String date = DateUtil.getToday();
		for (Map<String, Object> map : paramList) {
			map.put("record_date", MapUtil.getString(map, "record_date", date));
			map.put("record_time", MapUtil.getString(map, "record_time", time));
			map.put("record_id", IDUtil.getId());
		}
		promotionDao.insertMerchPromotionRecord(paramList);
	}
	
	//查询促销奖品
	@Override
	public List<Map<String, Object>> searchPromotionPrize(Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchPromotionPrize paramMap :"+paramMap);
		return promotionDao.searchPromotionPrize(paramMap);
	}	
	
	//插入促销奖品
	@Override
	public void insertPromotionPrize(List<Map<String, Object>> paramMap) throws Exception {
		LOG.debug("insertPromotionPrize paramMap :"+paramMap);
		promotionDao.insertPromotionPrize(paramMap);
	}
	//修改促销奖品
	@Override
	public void updatePromotionPrize(Map<String, Object> paramMap) throws Exception {
		LOG.debug("updatePromotionPrize paramMap :"+paramMap);
		promotionDao.updatePromotionPrize(paramMap);
	}
}
