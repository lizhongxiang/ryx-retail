package com.ryx.social.consumer.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.consumer.dao.IConsumerMerchDao;
import com.ryx.social.consumer.dao.IConsumerSaleDao;
import com.ryx.social.consumer.service.IConsumerSaleService;
@Service
public class ConsumerSaleServiceImpl  implements IConsumerSaleService {
 
	private static final Logger logger=LoggerFactory.getLogger(ConsumerSaleServiceImpl.class);
	@Resource
	private IConsumerSaleDao consumerSaleOrderDao;
	@Resource
	private IConsumerMerchDao consumerMerchDao;
	
	@Override
	public List<Map<String, Object>> searchMerchPromotion(Map<String, Object> paramMap) throws Exception {
		logger.debug("ConsumerSaleServiceImpl searchMerchPromotion paramMap: " + paramMap);
		StringBuilder merchIdBuffer = new StringBuilder();
		List<Map<String, Object>> promotionList = consumerSaleOrderDao.selectMerchSalePromotion(paramMap);
		for(Map<String, Object> promotionMap : promotionList) {
			String fileId = MapUtil.getString(promotionMap, "file_id");
			String merchId = MapUtil.getString(promotionMap, "merch_id");
			promotionMap.put("file_path", StringUtil.isBlank(fileId)?"":"user_file/"+merchId+"/"+fileId);
			if(merchIdBuffer.length()!=0) merchIdBuffer.append(",");
			merchIdBuffer.append(merchId);
		}
		Map<String, Object> merchParam = new HashMap<String, Object>();
		merchParam.put("merch_id", merchIdBuffer.toString());
		
		List<Map<String, Object>> merchList = consumerMerchDao.searchMerchById(merchParam);
		for(Map<String, Object> promotionMap : promotionList) {
			if(promotionMap.get("merch_id")!=null) {
				for(Map<String, Object> merchMap : merchList) {
					if(promotionMap.get("merch_id").toString().equals(merchMap.get("merch_id"))) {
						merchMap.remove("file_id");
						promotionMap.putAll(merchMap);
						break;
					}
				}
			}
		}
		
		return promotionList;
	}
	
	
	/**
	 * 提交销售单
	 */
	@Override
	public void submitConsumerSaleOrder(Map<String, Object> paramMap) throws Exception {
		logger.debug("SaleServiceImpl submitSaleOrder paramMap: " + paramMap);
		consumerSaleOrderDao.submitSaleOrder(paramMap);
	}
	//获得销售单，和销售单行
	@Override
	public List<Map<String, Object>> searSaleOrderandLine(Map<String, Object> saleMap)throws Exception{
		logger.debug("ConsumerSaleServiceImpl searSaleOrderandLine saleMap: "+saleMap);
		List<Map<String, Object>>saleOrder= consumerSaleOrderDao.selectConsumerSaleOrder(saleMap);
		List<Map<String, Object>> saleOrderLine=consumerSaleOrderDao.selectConsumerSaleOrderLine(saleMap);
		for (Map<String,Object> sale : saleOrder) {
			List  saleLineList=new ArrayList();
			for (Map<String, Object> saleLine : saleOrderLine) {
				if(saleLine.get("order_id").equals(sale.get("order_id"))){
					saleLineList.add(saleLine);
				}
			}
			sale.put("list", saleLineList);
		}
		return saleOrder;
	}
}

