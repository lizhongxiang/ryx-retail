package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IMerchConsumerDao;
import com.ryx.social.retail.dao.IOrderDao;
import com.ryx.social.retail.service.IMerchConsumerService;

@Service
public class MerchConsumerServiceImpl implements IMerchConsumerService {
	
	private Logger LOG = LoggerFactory.getLogger(MerchConsumerServiceImpl.class);
	
	@Resource
	private IMerchConsumerDao merchConsumerDao;
	
	@Resource
	private IOrderDao orderDao;

	//查询会员等级
	@Override
	public List<Map<String, Object>> searchMerchConsumerGrade(Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerServiceImpl searchMerchConsumerGrade paramMap: " + paramMap);
		List<Map<String, Object>> gradeList = merchConsumerDao.selectMerchConsumerGrade(paramMap);
		Map<String, Object> consumerCountParamMap = new HashMap<String, Object>();
		consumerCountParamMap.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		Map<String, Object> consumerCountMap = selectMerchConsumerConunt(consumerCountParamMap);
		String grade = null;
		String upgradeShould = null;
		String upgradeMust = null;
		
		for (Map<String, Object> map : gradeList) {
			
			upgradeShould = MapUtil.getString(map, "upgrade_should");
			upgradeMust = MapUtil.getString(map, "upgrade_must");
			if(!StringUtil.isBlank(upgradeShould) && !upgradeShould.equals("{}")){
				map.put("upgrade_should", JsonUtil.json2Map(upgradeShould));
			}else{
				map.put("upgrade_should", new HashMap<String, Object>());
			}
			if(!StringUtil.isBlank(upgradeMust) && !upgradeMust.equals("{}")){
				map.put("upgrade_must", JsonUtil.json2Map(upgradeMust));
			}else{
				map.put("upgrade_must", new HashMap<String, Object>());
			}
			
			grade = MapUtil.getString(map, "grade");
			if(consumerCountMap.containsKey(grade)){
				map.put("consumer_count", MapUtil.getString(consumerCountMap, grade, "0"));
			}else{
				map.put("consumer_count", "0");
			}
		}
		return gradeList;
	}

	@Override
	public List<Map<String, Object>> searchMerchConsumerGradeAndConsumerNumber(Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerServiceImpl searchMerchConsumerGradeAndConsumerNumber paramMap: " + paramMap);
		return merchConsumerDao.searchMerchConsumerGradeAndConsumerNumber(paramMap);
	}
	
	//查询基本会员级别
	public List<Map<String, Object>> selectBaseMerchConsumerGrade ( ) throws Exception{
		LOG.debug("selectBaseMerchConsumerGrade ");
		return merchConsumerDao.selectBaseMerchConsumerGrade(null);
	}
	
	//插入基本会员级别
	@Override
	public void insertDefMerchConsumerGrade(Map<String, Object> paramMap) throws Exception{
		List<Map<String, Object>> baseGradeList = selectBaseMerchConsumerGrade();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		for (Map<String, Object> map : baseGradeList) {
			map.put("merch_id", merchId);
		}
		insertMerchConsumerGrades(baseGradeList);
	}
	
	//插入会员等级
	@Override
	public void insertMerchConsumerGrades(List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("insertMerchConsumerGrade paramList:"+paramList);
		for (Map<String, Object> map : paramList) {
			map.put("upgrade_should", JsonUtil.object2json(MapUtil.get(map, "upgrade_should", new HashMap<String, Object>())));
			map.put("upgrade_must",JsonUtil.object2json(MapUtil.get(map, "upgrade_must", new HashMap<String, Object>())));
			map.put("grade_id", IDUtil.getId());
			map.put("discount", MapUtil.getString(map, "discount", "100"));
		}
		merchConsumerDao.insertMerchConsumerGrades(paramList);
	}
	
	//修改会员等级----批量
	@Override
	public void updateMerchConsumerGrades(List<Map<String, Object>> paramList)throws Exception{
		LOG.debug("updateMerchConsumerGrades paramList:"+paramList);
		for (Map<String, Object> map : paramList) {
			if(map.containsKey("upgrade_should")){
				map.put("upgrade_should", JsonUtil.object2json(MapUtil.get(map, "upgrade_should", "")));
			}
			if(map.containsKey("upgrade_must")){
				map.put("upgrade_must",JsonUtil.object2json(MapUtil.get(map, "upgrade_must", "")));
			}
		}
		merchConsumerDao.updateMerchConsumerGrades(paramList);
	}
	
	//修改会员等级----单个
	@Override
	public void updateMerchConsumerGrade(Map<String, Object> paramMap)throws Exception{
		LOG.debug("updateMerchConsumerGrade paramMap:"+paramMap);
		merchConsumerDao.updateMerchConsumerGrade(paramMap);
	}
	
	//操作会员等级
	@Override
	public void operateMerchConsumerGrade(List<Map<String, Object>> paramList)throws Exception{
		LOG.debug("updateMerchConsumerGrade paramList:"+paramList);
		List<Map<String, Object>> addGradeList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> updGradeList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> delGradeList = new ArrayList<Map<String,Object>>();
		String operationType = null;
		for (Map<String, Object> map : paramList) {
			operationType = MapUtil.getString(map, "operation_type");
			if(operationType.equals("add")){
				addGradeList.add(map);
			}else if(operationType.equals("upd")){
				updGradeList.add(map);
			}else if(operationType.equals("del")){
				delGradeList.add(map);
			}
		}
		if(!delGradeList.isEmpty()){
			updateMerchConsumerByGrade(delGradeList);
			deleteMerchConsumerGrades(delGradeList);
		}
		if(!updGradeList.isEmpty()){
			updateMerchConsumerGrades(updGradeList);
		}
		if(!addGradeList.isEmpty()){
			insertMerchConsumerGrades(addGradeList);
		}
	}
	
	//删除会员等级
	@Override
	public void deleteMerchConsumerGrades(List<Map<String, Object>> paramList)throws Exception{
		LOG.debug("deleteMerchConsumerGrade paramList:"+paramList);
		merchConsumerDao.deleteMerchConsumerGrades(paramList); 
	}
	
	//插入会员
	@Override
	public Map<String, Object> insertMerchConsumer(Map<String, Object> paramMap) throws Exception{
		LOG.debug("insertMerchConsumer paramMap:"+paramMap);
		String date = DateUtil.getCurrentTime();
//		String time = date.substring(8);
		String telephone = MapUtil.getString(paramMap, "telephone");
		String cardId = MapUtil.getString(paramMap, "card_id");
		String code = "0000";
		String msg = "请求成功";
		Map<String, Object> searchConsumerMap = new HashMap<String, Object>();
		searchConsumerMap.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		searchConsumerMap.put("repeat_card_id", cardId);
		searchConsumerMap.put("repeat_telephone", telephone);
		List<Map<String, Object>> list = this.selectMerchConsumer(searchConsumerMap);
		if(list.size() <= 0){
			paramMap.put("consumer_id", IDUtil.getId());
			paramMap.put("modified_timestamp", date);
			paramMap.put("status", "1");
			paramMap.put("topscore", MapUtil.getString(paramMap, "top_point", "0"));
			paramMap.put("curscore", MapUtil.getString(paramMap, "curscore", "0"));
			paramMap.put("grade", MapUtil.get(paramMap, "grade", "1"));
			merchConsumerDao.insertMerchConsumer(paramMap);
		}else{
			code = "1000";
			for (Map<String, Object> map : list) {
				if(telephone.equals(MapUtil.getString(map, "telephone"))){
					msg = "手机号码重复";
					break;
				}else if(cardId.equals(MapUtil.getString(map, "card_id"))){
					msg = "卡号重复";
					break;
				}
			}
		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("code", code);
		returnMap.put("msg", msg);
		return returnMap;
	}
	
	//查询会员通过id
	@Override
	public Map<String, Object> selectMerchConsumerById(Map<String, Object> paramMap)throws Exception{
		LOG.debug("selectMerchConsumerByIdOrPhone paramMap:"+paramMap);
		List<Map<String, Object>> consumerList = merchConsumerDao.selectMerchConsumerById(paramMap);
		Map<String, Object> consumerMap = null;
		if(!consumerList.isEmpty()){
			consumerMap = consumerList.get(0);
		}
		return consumerMap;
	}
	
	//查询会员级别人数
	public Map<String, Object> selectMerchConsumerConunt(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectMerchConsumerConunt paramMap:"+paramMap);
		List<Map<String, Object>> consumerCountList =  merchConsumerDao.selectMerchConsumerConunt(paramMap);
		String grade = null;
		String consumerCount = null;
		Map<String, Object> consumerCountMap = new HashMap<String, Object>();
		for (Map<String, Object> map : consumerCountList) {
			grade = MapUtil.getString(map, "grade");
			consumerCount = MapUtil.getString(map, "consumer_count");
			consumerCountMap.put(grade, consumerCount);
		}
		return consumerCountMap;
	}
	//查询会员
	@Override
	public List<Map<String, Object>> selectMerchConsumer(Map<String, Object> paramMap)throws Exception{
		LOG.debug("selectMerchConsumerByIdOrPhone paramMap:"+paramMap);
		return merchConsumerDao.selectMerchConsumer(paramMap);
	}
	
	//查询会员--表关联
	@Override
	public List<Map<String, Object>> searchMerchConsumer(Map<String, Object> paramMap)throws Exception{
		LOG.debug("selectMerchConsumerByIdOrPhone paramMap:"+paramMap);
		List<Map<String, Object>> consumerInfoList = merchConsumerDao.searchMerchConsumer(paramMap);
		String upgradeShould = null;
		String upgradeMust = null;
		for (Map<String, Object> map : consumerInfoList) {
			upgradeShould = MapUtil.getString(map, "upgrade_should");
			upgradeMust = MapUtil.getString(map, "upgrade_must");
			if(!StringUtil.isBlank(upgradeShould) && !upgradeShould.equals("{}")){
				map.put("upgrade_should", JsonUtil.json2Map(upgradeShould));
			}else{
				map.put("upgrade_should", new HashMap<String, Object>());
			}
			if(!StringUtil.isBlank(upgradeMust) && !upgradeMust.equals("{}")){
				map.put("upgrade_must", JsonUtil.json2Map(upgradeMust));
			}else{
				map.put("upgrade_must", new HashMap<String, Object>());
			}
		}
		return consumerInfoList;
	}
	
	
	//修改会员
	@Override
	public void updateMerchConsumer(Map<String, Object> paramMap)throws Exception{
		LOG.debug("updateMerchConsumer paramMap:"+paramMap);
		paramMap.put("modified_timestamp", DateUtil.getCurrentTime());
		merchConsumerDao.updateMerchConsumer(paramMap);
	}
	
	//批量修改会员信息
	@Override
	public void updateBatchMerchConsumer(List<Map<String, Object>> consumerList)throws Exception {
		LOG.debug("updateBatchMerchConsumer consumerList:"+consumerList);
	}
	//插入默认级别，或修改无级别会员（会员管理）
	@Override
	public void insertDefaultGrade (List<Map<String, Object>> paramList)throws Exception{
		LOG.debug("insertDefaultGrade paramList:"+paramList);
		
		Map<String, Object> map = null;
		int grade = 0;
		String isDefaultGrade = "0";//是否有默认级别，1：有
		String merchId = null;
		int index = 1;
		for(int i = paramList.size()-1; i >= 0; i--){
			map = paramList.get(i);
			merchId = MapUtil.getString(map, "merch_id");
			map.put("grade", index);
			
			if(MapUtil.getString(map, "is_default_grade", "0").equals("1")){
				grade = (index);
				isDefaultGrade = MapUtil.getString(map, "is_default_grade", "0");
			}
			index ++;
		}
		this.insertMerchConsumerGrades(paramList);
		if(isDefaultGrade.equals("1") && grade != 0 ){
			Map<String, Object> updConsumerMap = new HashMap<String, Object>();
			updConsumerMap.put("grade", grade);
			updConsumerMap.put("is_default_grade", "1");
			updConsumerMap.put("merch_id", merchId);
			updateMerchConsumer(updConsumerMap);
		}
	}
	
	
	//修改会员---------针对会员管理（防止会员手机号，卡号重复）
	@Override
	public Map<String, Object> updateMerchConsumerForHygl(Map<String, Object> paramMap)throws Exception{
		LOG.debug("updateMerchConsumer paramMap:"+paramMap);
		String code = "0000";
		String msg = "请求成功";
		
		String telephone = MapUtil.getString(paramMap, "telephone");
		String cardId = MapUtil.getString(paramMap, "card_id");
		String consumerId = MapUtil.getString(paramMap, "consumer_id");
		Map<String, Object> searchConsumerMap = new HashMap<String, Object>();
		searchConsumerMap.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		searchConsumerMap.put("not_consumer_id", consumerId);
		searchConsumerMap.put("repeat_card_id", cardId);//重复kah
		searchConsumerMap.put("repeat_telephone", telephone);//重复手机号
		List<Map<String, Object>> list = this.selectMerchConsumer(searchConsumerMap);
		if(list.size() <= 0){
			this.updateMerchConsumer(paramMap);//修改会员
		}else{
			code = "1000";
			for (Map<String, Object> map : list) {
				if(MapUtil.getString(map, "card_id").equals(cardId)){
					msg = "卡号重复";
					break;
				}else if(MapUtil.getString(map, "telephone").equals(telephone)){
					msg = "手机号码重复";
					break;
				}
			}
		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("code", code);
		returnMap.put("msg", msg);
		return returnMap;
	}
	
	//修改会员 的 等级----用于删除会员等级
	public void updateMerchConsumerByGrade(List<Map<String, Object>> paramList)throws Exception{
		LOG.debug("updateMerchConsumerByGrade paramList:"+paramList);
		merchConsumerDao.updateMerchConsumerByGrade(paramList);
	}
	
	
	////////////
	
//	插入积分兑换商品
	@Override
	public void insertExchangePrize (Map<String, Object> paramMap) throws Exception {
		LOG.debug("insertExchangePrize paramMap:"+paramMap);
		String today=DateUtil.getToday();//当前日期
		String dateAndTime=DateUtil.getCurrentTime();
		String newTime=dateAndTime.substring(8);
		paramMap.put("create_date", today);
		paramMap.put("create_time", newTime);
		paramMap.put("prize_id", IDUtil.getId());
		paramMap.put("status", "1");
		merchConsumerDao.insertExchangePrize(paramMap);
	}
	
//	修改积分兑换商品
	@Override
	public void updateExchangePrize (Map<String, Object> paramMap) throws Exception {
		LOG.debug("updateExchangePrize paramMap:"+paramMap);
		merchConsumerDao.updateExchangePrize(paramMap);
	}
	
	//查询积分兑换商品
	@Override
	public List<Map<String, Object>> searchExchangePrize (Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchExchangePrize paramMap:"+paramMap);
		return merchConsumerDao.selectExchangePrize(paramMap);
	}
	
	//插入积分兑换流水
	@Override
	public void insertScoreExchange (Map<String, Object> paramMap) throws Exception {
		LOG.debug("insertScoreExchange paramMap:"+paramMap);
		String today=DateUtil.getToday();//当前日期
		String dateAndTime=DateUtil.getCurrentTime();
		String newTime=dateAndTime.substring(8);
		paramMap.put("exchange_id", IDUtil.getId());
		paramMap.put("exchange_date", today );
		paramMap.put("exchange_time", newTime);
		merchConsumerDao.insertScoreExchange(paramMap);
		Map<String, Object> updConsumer = new HashMap<String, Object>();
		updConsumer.put("consumer_id", MapUtil.getString(paramMap, "consumer_id"));
		updConsumer.put("adjusted_point", BigDecimal.ZERO.subtract(MapUtil.getBigDecimal(paramMap, "score")));
		this.updateMerchConsumer(updConsumer);
	}
	
	//查询积分兑换流水
	@Override
	public List<Map<String, Object>> searchScoreExchange (Map<String, Object> paramMap) throws Exception {
		LOG.debug("searchScoreExchange paramMap:"+paramMap);
		return merchConsumerDao.searchScoreExchange(paramMap);
	}

	//结构分析:按年龄性别分组
	@Override
	public List<Map<String, Object>> searchMerchCustomerByAgeSex(
			Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerServiceImpl searchMerchCustomerByAgeSex paramMap"+ paramMap);
	
		return merchConsumerDao.searchMerchCustomerByAgeSex(paramMap);
	}

	//结构分析:按学历分组
	@Override
	public List<Map<String, Object>> searchMerchCustomerByEdu(
			Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerServiceImpl searchMerchCustomerByEdu paramMap"+ paramMap);
		
		return merchConsumerDao.searchMerchCustomerByEdu(paramMap);
	}

	//结构分析:按收入分组
	@Override
	public List<Map<String, Object>> searchMerchCustomerByMoney(
			Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerServiceImpl searchMerchCustomerByMoney paramMap"+ paramMap);
		
		return merchConsumerDao.searchMerchCustomerByMoney(paramMap);
	}
	
	//会员行为分析
	@Override
	public List<Map<String, Object>> searchConsumeBehaviorAnalysis(Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerServiceImpl searchConsumeBehaviorAnalysis paramMap:"+paramMap);
		return merchConsumerDao.searchConsumeBehaviorAnalysis(paramMap);
	}
	
	//会员行为分析单行
	@Override
	public List<Map<String, Object>> searchConsumeBehaviorAnalysisLine(Map<String, Object> paramMap) throws Exception {
		LOG.debug("MerchConsumerServiceImpl searchConsumeBehaviorAnalysis paramMap:"+paramMap);
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String consumerId = MapUtil.getString(paramMap, "consumer_id");
		
		Map<String, Object> saleOrderParam= new HashMap<String, Object>();
		saleOrderParam.put("merch_id", merchId);
		saleOrderParam.put("consumer_id", consumerId);
		saleOrderParam.put("page_index", "-1");
		saleOrderParam.put("page_size", "-1");
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		if(!StringUtil.isBlank(itemKindId)){
			saleOrderParam.put("item_kind_id", itemKindId);
		}
		if(!StringUtil.isBlank(startDate)){
			saleOrderParam.put("start_date", startDate);
		}
		if(!StringUtil.isBlank(endDate)){
			saleOrderParam.put("end_date", endDate);
		}
		
		List<Map<String, Object>> saleOrderList = orderDao.searchSaleOrderJoinLineAndItem(saleOrderParam);
//		Map<String, Map<String, Object>> saleOrderMap =  new HashMap<String, Map<String,Object>>();
		Map<String, Map<String, Object>> saleOrderMap =  new LinkedHashMap<String, Map<String,Object>>(); 
		
		StringBuilder orderIdStr = new StringBuilder();
		String orderId = "";
		for (Map<String, Object> map : saleOrderList) {
			orderId = MapUtil.getString(map, "order_id");
			orderIdStr.append(orderId);
			orderIdStr.append(",");
			map.put("line", new ArrayList<Map<String, Object>>());
			saleOrderMap.put(orderId, map);
		}
		Map<String, Object> saleOrderLineParam = new HashMap<String, Object>();
		saleOrderLineParam.put("order_id", orderIdStr.toString());
		saleOrderLineParam.put("merch_id", merchId);
		if(!StringUtil.isBlank(itemKindId)){
			saleOrderLineParam.put("item_kind_id", itemKindId);
		}
		saleOrderLineParam.put("page_index", "-1");
		saleOrderLineParam.put("page_size", "-1");
		List<Map<String, Object>> saleOrderLineList = orderDao.searchSaleOrderLineJoinItem(saleOrderLineParam);
//				orderDao.selectSaleOrderLine(saleOrderLineParam);
		
		List<Map<String, Object>> lienList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> map : saleOrderLineList) {
			orderId = MapUtil.getString(map, "order_id");
			if(saleOrderMap.containsKey(orderId)){
				lienList = MapUtil.get(saleOrderMap.get(orderId), "line", new ArrayList<Map<String, Object>>());//(List<Map<String, Object>>) saleOrderMap.get(orderId).get("line");
				lienList.add(map);
			}
		}
		saleOrderList = new ArrayList<Map<String,Object>>();
		for (String key : saleOrderMap.keySet()) {
			saleOrderList.add(saleOrderMap.get(key));
		}
		return saleOrderList;
	}
	
}
