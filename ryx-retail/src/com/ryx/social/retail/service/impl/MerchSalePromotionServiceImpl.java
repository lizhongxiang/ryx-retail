package com.ryx.social.retail.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.social.retail.dao.IMerchSalePromotionDao;
import com.ryx.social.retail.service.IMerchSalePromotionService;
@Service
public class MerchSalePromotionServiceImpl implements
		IMerchSalePromotionService {
	private static final Logger logger=LoggerFactory.getLogger(MerchSalePromotionServiceImpl.class);
	@Resource
	private IMerchSalePromotionDao merchSalePromotionDao;
	@Override
	public void createSalePromotion(Map<String, Object> paramsMap)
			throws Exception {
		logger.debug("merchSalePromotionServiceImpl addSalePromotion paramsMap:"+paramsMap);
		Map<String, Object> deleteParam = new HashMap<String, Object>();
		deleteParam.put("merch_id", paramsMap.get("merch_id"));
		merchSalePromotionDao.delMerchSalePromotion(deleteParam);
		merchSalePromotionDao.insertMerchSalePromotion(paramsMap);
	}

	@Override
	public List<Map<String, Object>> searchSalePromotion(Map<String, Object> paramsMap) throws Exception {
		logger.debug("MerchSalePromotionServiceServiceImpl serchSalePromotion paramsMap:"+paramsMap);
		return merchSalePromotionDao.selMerchSalePromotion(paramsMap);
	}

	@Override
	public void removeSalePromotion(Map<String, Object> paramsMap)
			throws Exception {
		logger.debug("MerchSalePromotionServiceImpl delSalePromotion paramsMap:"+paramsMap);
		merchSalePromotionDao.delMerchSalePromotion(paramsMap);

	}

	@Override
	public void modifySalePromotion(Map<String, Object> paramsMap)
			throws Exception {
		logger.debug("MerchSalePromotoinSercieImp reviseSalePromotion paramsMap:"+paramsMap);
		merchSalePromotionDao.updateMerchSalePromotion(paramsMap);

	}

}
