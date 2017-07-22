package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.dao.IPurchOrderDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.IPurchOrderService;
import com.ryx.social.retail.service.IUploadDataToRtmsService;

@Service
public class PurchOrderServiceImpl implements IPurchOrderService {
	private Logger LOG = LoggerFactory.getLogger(PurchOrderServiceImpl.class);
	@Resource
	private IPurchOrderDao purchOrderDao;
	@Resource
	private IWhseDao whseDao;
	@Resource
	private IItemDao itemDao;
	@Resource
	private IUploadDataToRtmsService uploadDataToRtmsService;
	/**
	 * 退货
	 */
	@Override
	public void submitPurchOrder(Map<String, Object> paramMap) throws Exception {
		LOG.debug("PurchOrderServiceImpl submitPurchOrder paramMap: " + paramMap);
		String jsonParam = (String) paramMap.get("json_param");
		Map<String, Object> purchOrderParam = JsonUtil.json2Map(jsonParam);
		purchOrderParam.put("merch_id", paramMap.get("merch_id"));
		String today = DateUtil.getToday();
		String orderId = IDUtil.getId();
		purchOrderParam.put("order_id", orderId);
		purchOrderParam.put("order_date", today);
		purchOrderParam.put("voucher_date", today);
		//将数据插入采购单数据
		purchOrderDao.submitPurchOrder(purchOrderParam);
		//修改对应商品的库存
		List<Map<String, Object>> whseParams = (List<Map<String, Object>>) purchOrderParam.get("list");
		whseDao.updateWhseMerchSimple(whseParams);
	}
	
	
	
	@Override
	public void insertPurchOrder(Map<String, Object> paramMap) throws Exception {
		LOG.debug("PurchOrderServiceImpl insertPurchOrder paramMap: " + paramMap);
		String jsonParam = (String) paramMap.get("json_param");
		Map<String, Object> purchOrderParam = JsonUtil.json2Map(jsonParam);
		purchOrderParam.put("merch_id", paramMap.get("merch_id"));
		String today = DateUtil.getToday();
		String orderId = IDUtil.getId();
		if(paramMap.containsKey("order_id")){
			purchOrderParam.put("order_id", paramMap.get("order_id"));
		}else{
			purchOrderParam.put("order_id", orderId);
		}
		purchOrderParam.put("order_date", MapUtil.getString(paramMap, "order_date", today));
		purchOrderParam.put("order_time", DateUtil.getCurrentTime().substring(8));
		purchOrderParam.put("voucher_date", MapUtil.getString(paramMap, "voucher_date", today));
		List<Map<String, Object>> purchOrderLines = (List<Map<String, Object>>) purchOrderParam.get("list");
//		for(Map<String, Object> purchOrderLine : purchOrderLines) {
//			purchOrderLine.put("merch_id", paramMap.get("merch_id"));
//		}
		for(int i=0;i<purchOrderLines.size();i++) {
			purchOrderLines.get(i).put("merch_id", paramMap.get("merch_id"));
			if(!purchOrderLines.get(i).containsKey("qty_ord")){
				purchOrderLines.get(i).put("qty_ord", purchOrderLines.get(i).get("quantity"));
			}
			if(!purchOrderLines.get(i).containsKey("pri_wsale")){
				double  tAmount=Double.valueOf(purchOrderLines.get(i).get("amount").toString().trim());
				double  tQuantity=Double.valueOf(purchOrderLines.get(i).get("quantity").toString().trim());
//				double  tRatio=Double.valueOf(purchOrderLines.get(i).get("unit_ratio").toString().trim());
				purchOrderLines.get(i).put("pri_wsale", tAmount/tQuantity);
			}
			if(!purchOrderLines.get(i).containsKey("qty_rsn")){
				purchOrderLines.get(i).put("qty_rsn", purchOrderLines.get(i).get("quantity"));
			}
			if(!purchOrderLines.get(i).containsKey("qty_req")){
				purchOrderLines.get(i).put("qty_req", purchOrderLines.get(i).get("quantity"));
			}
			
		}
		//将数据插入采购单数据
		/* 梁凯 2014年6月3日14:02:43
		 * 插入入库单的数据添加operator字段
		 */
		purchOrderParam.put("operator", paramMap.get("operator"));
		purchOrderDao.submitPurchOrder(purchOrderParam);
		//数据同步到RTMS
		uploadDataToRtmsService.insertPurchTobaccoToRTMS(purchOrderParam);
		
/*		Map<String,Object> resultJson=null;
		String code=null;
		if(!"04".equals((String)purchOrderParam.get("order_type"))){
			resultJson=insertPurchTobaccoToRTMS(purchOrderParam);
			code=(String)resultJson.get("code");
			if(code=="0000"){
				logger.info("======楼上店之外的库存上传成功======"+(String)resultJson.get("msg"));
			}else{
				logger.info("======楼上点之外的库存上传失败======"+(String)resultJson.get("msg"));
			}
		}else{
			if("03".equals((String)purchOrderParam.get("status"))){
				resultJson=insertPurchTobaccoToRTMS(purchOrderParam);
				code=(String)resultJson.get("code");
				if(code=="0000"){
					logger.info("======楼上店完成的库存上传成功======"+(String)resultJson.get("msg"));
				}else{
					logger.info("======楼上店完成的库存上传失败======"+(String)resultJson.get("msg"));
				}
			}else{
			}
		}
		*/
	}



	@Override
	public void commodityWarehousing(Map<String, Object> paramMap) throws Exception {
		LOG.debug("PurchOrderServiceImpl commodityWarehousing paramMap: " + paramMap);
		//调用添加采购单服务插入采购单
		insertPurchOrder(paramMap);
	
		String jsonParam = (String) paramMap.get("json_param");
		Map<String, Object> purchOrderParam = JsonUtil.json2Map(jsonParam);
		
		Map<String,Object> od=new HashMap<String,Object>();
		od.put("whse_date", DateUtil.getToday());
		List<Map<String,Object>> odList=(List<Map<String,Object>>)purchOrderParam.get("list");
		
		for(Map<String, Object> odMap : odList) {
			odMap.put("merch_id", paramMap.get("merch_id"));
		}
		
		//批量修改本次采购价及平均采购价
		modifyMerchItemCostAndPri1(odList);
		
		for(int i=0;i<odList.size();i++){
			od.put("merch_id", odList.get(i).get("merch_id"));
			double tQuantity=Double.valueOf(odList.get(i).get("quantity").toString().trim());
			double tUnitRatio=Double.valueOf(odList.get(i).get("unit_ratio").toString().trim());
			odList.get(i).put("qty_add",tQuantity*tUnitRatio);
		}
		//清除不必要数据
		clearData(odList);
		
		od.put("list", odList);
		//修改库存
		whseDao.updateWhseMerch(od);
	}
	
	/**
	 * 修改商品本次采购价和成本价
	 * 成本价=(库存额+进货额)/(库存量+进货量)
	 */
	@Override
	public void modifyMerchItemCostAndPri1(List<Map<String, Object>> purchLines) throws Exception{
		LOG.debug("PurchOrderServiceImpl modifyMerchItemCostAndPri1 purchLines: " + purchLines);
		List<Map<String, Object>> processedCostAndPri1Lines = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> processedPri1Lines = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> processedCostLines = new ArrayList<Map<String, Object>>();
		
		for(Map<String,Object> line : purchLines){
			Map<String,Object> processedLine = new HashMap<String, Object>();
			processedLine.put("modified_timestamp", DateUtil.getCurrentTime());
			processedLine.put("merch_id", MapUtil.getString(line, "merch_id"));
			processedLine.put("item_id", MapUtil.getString(line, "item_id"));
			
			BigDecimal purchaseAmount = MapUtil.getBigDecimal(line, "amount");//采购金额
			BigDecimal unitRatio = MapUtil.getBigDecimal(line, "unit_ratio");//转换比
			BigDecimal purchaseQuantity = MapUtil.getBigDecimal(line, "quantity");//采购量
			// 采购量按最小包装计算
			purchaseQuantity = purchaseQuantity.multiply(unitRatio);
			BigDecimal inventoryQuantity = MapUtil.getBigDecimal(line, "qty_whse");//原库存量
			// 更新进货价/成本价时不需要原进货价
			BigDecimal cost = MapUtil.getBigDecimal(line, "cost");//原成本价
			BigDecimal inventoryQuantityPlusPurchaseQuantity = inventoryQuantity.add(purchaseQuantity);
			BigDecimal inventoryAmountPlusPurchaseAmount = inventoryQuantity.multiply(cost).add(purchaseAmount);
			// 采购后库存额>0 && 采购后库存量>0时 更新成本价和进货价 否则采购量!=0时 更新进货价 
			if(inventoryAmountPlusPurchaseAmount.compareTo(BigDecimal.ZERO)>0 
					&& inventoryQuantityPlusPurchaseQuantity.compareTo(BigDecimal.ZERO)>0) {
				
				processedLine.put("cost", inventoryAmountPlusPurchaseAmount.divide(inventoryQuantityPlusPurchaseQuantity, 5, BigDecimal.ROUND_HALF_UP));
				
				// 如果采购量!=0时 更新进货价 否则只更新成本价
				if(purchaseQuantity.compareTo(BigDecimal.ZERO)!=0) {
					processedLine.put("pri1", purchaseAmount.divide(purchaseQuantity, 5, BigDecimal.ROUND_HALF_UP));
					processedCostAndPri1Lines.add(processedLine);
				} else {
					processedCostLines.add(processedLine);
				}
			} else if(purchaseQuantity.compareTo(BigDecimal.ZERO)!=0) {
				processedLine.put("pri1", purchaseAmount.divide(purchaseQuantity, 5, BigDecimal.ROUND_HALF_UP));
				processedPri1Lines.add(processedLine);
			}
		}
		if(!processedCostAndPri1Lines.isEmpty()) itemDao.updateMerchItemCostAndPri1(processedCostAndPri1Lines);
		if(!processedCostLines.isEmpty()) itemDao.updateMerchItemCost(processedCostLines);
		if(!processedPri1Lines.isEmpty()) itemDao.updateMerchItemPri1(processedPri1Lines);
	}
	
	/**
	 * 清理无用数据
	 * @author 徐虎彬
	 * @date 2014年3月24日
	 * @param odList
	 * @return
	 */
	public List<Map<String,Object>> clearData(List<Map<String,Object>> odList){
		LOG.debug("PurchOrderServiceImpl clearData purchLines: " + odList);
		for(int i=0;i<odList.size();i++){
			if(odList.get(i).containsKey("qty_whse")){
				odList.get(i).remove("qty_whse");
			}
			if(odList.get(i).containsKey("qty_locked")){
				odList.get(i).remove("qty_locked");
			}
			if(odList.get(i).containsKey("qty_whse_warn")){
				odList.get(i).remove("qty_whse_warn");
			}
			if(odList.get(i).containsKey("qty_whse_init")){
				odList.get(i).remove("qty_whse_init");
			}
			if(odList.get(i).containsKey("whse_init_date")){
				odList.get(i).remove("whse_init_date");
			}
		}
		return odList;
	}
	
	
	//采购单报表--年
	@Override
	public List<Map<String, Object>> searchYearPurchOrderReport(Map<String, Object> purchMap)
			throws Exception{
		LOG.debug("PurchOrderServiceImpl searchYearPurchOrderReport purchMap :"+purchMap);
		Integer number=Integer.valueOf( (String) purchMap.get("number"));//周期
		Date date  = new Date();
		String newDate =(new SimpleDateFormat("yyyy")).format(date ) ;		
		Calendar cal  = Calendar.getInstance();
		cal .setTime(date );		
		cal .add(Calendar.YEAR, -number+ 1);
		String oldDate = (new SimpleDateFormat("yyyy")).format(cal .getTime());
		List yearList=DateUtil.getListBetweenStartingAndEnding(oldDate,newDate );//日期集合
		List<Map<String, Object>> data=  purchOrderDao.searchPurchOrderReport(purchMap);
		 for (int j = 0; j < yearList.size(); j++) {
			if(!data.get(j).get("time_interval").equals(yearList.get(j))||j>=data.size()){
				Map<String, Object> nullMap=new HashMap<String, Object>();
	    		nullMap.put("time_interval", yearList.get(j));
	    		nullMap.put("purchroom", "0");
	    		data.add(j,nullMap);
			}
		}
		 return data;
	}
	//采购单报表--月
	@Override
	public List<Map<String, Object>> searchMonthPurchOrderReport(Map<String, Object> purchMap)
			throws Exception{
		LOG.debug("PurchOrderServiceImpl searchMonthPurchOrderReport purchMap :"+purchMap);
		Integer number=Integer.valueOf( (String) purchMap.get("number"));//周期
		Date date  = new Date();
		String newDate  = (new SimpleDateFormat("yyyyMMdd")).format(date ) ;
		newDate =newDate .substring(0,newDate.length()-2);		
		Calendar cal  = Calendar.getInstance();
		cal .setTime(date );
		cal .add(Calendar.MARCH, -number+1);
		String oldDate  = (new SimpleDateFormat("yyyyMMdd")).format(cal .getTime());
		oldDate =oldDate .substring(0,oldDate.length()-2);
		purchMap.put("start_date", oldDate);//开始日期
		purchMap.put("end_date", newDate);//结束日期
		List<String> monthList=DateUtil.getListBetweenStartingAndEnding(oldDate,newDate);//得到开始、结束日期
		List<Map<String, Object>> data=purchOrderDao.searchPurchOrderReport(purchMap);
		for (int i = 0; i < monthList.size(); i++) {
			String today=monthList.get(i);
			if(i>=data.size()||!data.get(i).get("time_interval").equals(today)){
				Map<String,Object> nullMap=new HashMap<String, Object>();
				nullMap.put("time_interval", today);
				nullMap.put("purchroom", 0);
				data.add(i,nullMap);
			}
		}	
		return data;
	}
	//采购单报表--日\
	@Override
	public List<Map<String, Object>> searchDayPurchOrderReport(Map<String, Object> purchMap)
			throws Exception{
		LOG.debug("PurchOrderServiceImpl searchDayPurchOrderReport purchMap :"+purchMap);
		Integer number=Integer.valueOf( (String) purchMap.get("number"));//周期
		Date date  = new Date();
		String newDate = (new SimpleDateFormat("yyyyMMdd")).format(date ) ;
		Calendar cal  = Calendar.getInstance();
		cal .setTime(date );	
		cal .add(Calendar.DATE, -number+1);
		String oldDate = (new SimpleDateFormat("yyyyMMdd")).format(cal .getTime());
		purchMap.put("start_date", oldDate);//开始日期
		purchMap.put("end_date", newDate);//结束日期		
		List<Map<String, Object>> data=purchOrderDao.searchPurchOrderReport(purchMap);
		List<String> dayList=DateUtil.getListBetweenStartingAndEnding(oldDate+"",newDate+"" );//日期集合
		for (int i = 0; i < dayList.size(); i++) {
			if(i>=data.size()||!dayList.get(i).equals(data.get(i).get("time_interval"))){
				Map<String, Object> nullMap=new HashMap<String, Object>();
				nullMap.put("time_interval", dayList.get(i));
				nullMap.put("purchroom", 0);
				data.add(i,nullMap);
			}
		}
		return data;
	}
	//采购单报表--周
	@Override
	public List<Map<String, Object>> searchWeekPurchOrderReport(Map<String, Object> purchMap)
			throws Exception{
		LOG.debug("PurchOrderServiceImpl searchWeekPurchOrderReport purchMap :"+purchMap);
		Integer number=Integer.valueOf( (String) purchMap.get("number"));//周期
		Integer day=number*7;
		Date date = new Date();
		String newDate = (new SimpleDateFormat("yyyyMMdd")).format(date);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);	
		cal.add(Calendar.DATE, -day+1);
		String oldDate = (new SimpleDateFormat("yyyyMMdd")).format(cal.getTime());	
		purchMap.put("start_date", oldDate);//开始日期
		purchMap.put("end_date", newDate);//结束日期		
		List<Map<String, Object>> data=purchOrderDao.searchPurchOrderReport(purchMap);
		List<String> dayList=DateUtil.getListBetweenStartingAndEnding(oldDate+"",newDate+"" );//日期集合
		List<Map<String, Object>>weekData=new ArrayList<Map<String,Object>>();
		int weekNum=0;//判读日期集合（daylist）,防止重复读取
		int dataInde=0;//data的下标
		for (int i = 0; i < number; i++) {
			BigDecimal sumPurch = new BigDecimal("0");
			Map<String, Object> nullMap=new HashMap<String, Object>();
			for (int j = weekNum; j < dayList.size(); j++) {
//				if(data.size()>dataInde){
//					System.out.print(j+"==="+weekNum+"==="+dayList.get(j)+"-----"+data.get(dataInde).get("time_interval")+"::"+data.get(dataInde).get("purchroom"));
//				}
				//daylist[j]这一天data[i]中也有
				if(data.size()>dataInde&&data.get(dataInde).get("time_interval").equals(dayList.get(j))){
					BigDecimal purch = new BigDecimal(data.get(dataInde).get("purchroom")+"");
					sumPurch=sumPurch.add(purch);
					dataInde++;//data的下一个下标
				}
				weekNum++;
//				System.out.print("```"+weekNum+"==="+j+"==="+i);
				if((weekNum)%7==0){//一个周末，
//					System.out.println("------"+(i+1)+"周了");
					nullMap.put("time_interval", "第"+(i+1)+"周");
					nullMap.put("purchroom", sumPurch);
					weekData.add(i,nullMap);
					break;
				}
//				System.out.println();
			}
		}		
		return weekData;
	}



	@Override
	public List<Map<String,Object>> getPurchOrderList(Map<String, Object> paramMap)
			throws Exception {
		LOG.debug("PurchOrderServiceImpl getPurchOrderList purchMap :"+paramMap);
		return purchOrderDao.getPurchOrderList(paramMap);
	}



	@Override
	public List<Map<String,Object>> getPurchOrderDetail(Map<String, Object> paramMap)
			throws Exception {
		LOG.debug("PurchOrderServiceImpl getPurchOrderDetail purchMap :"+paramMap);
		return purchOrderDao.getPurchOrderDetail(paramMap);
	}

}
