package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.IUploadDataToRtmsService;
import com.ryx.social.retail.thread.IRtmsThread;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.ThreadConfig;

@Service
public class IUploadDataToRtmsServiceImpl implements IUploadDataToRtmsService {
	
	private Logger LOG = LoggerFactory.getLogger(IUploadDataToRtmsServiceImpl.class);
	
	@Resource
	private IRtmsThread rtmsThread;
	@Resource
	private IWhseDao whseDao;

	@Override
	public void submitSaleOrder2RTMS(Map<String, Object> dataMap)throws Exception {
		LOG.debug("IUploadDataToRtmsServiceImpl submitSaleOrder2RTMS dataMap:"+dataMap);
		
		Map<String, Object> rtmsSaleOrder = new HashMap<String, Object>();
		rtmsSaleOrder.put("cust_id", dataMap.get("merch_id"));
		rtmsSaleOrder.put("sale_id", dataMap.get("order_id"));
		if(dataMap.containsKey("consumer_id")){
			rtmsSaleOrder.put("consumer_id", dataMap.get("consumer_id"));
		}
		rtmsSaleOrder.put("sale_date", dataMap.get("order_date"));
		rtmsSaleOrder.put("sale_time", dataMap.get("order_time"));
		rtmsSaleOrder.put("pay_time", dataMap.get("order_time"));
		// 兼容智能终端
		if (dataMap.containsKey("total_amount")) {
			rtmsSaleOrder.put("amt_sum", dataMap.get("total_amount"));//订单总金额
			rtmsSaleOrder.put("amt_payable", dataMap.get("total_amount"));//应付金额
		} else {
			rtmsSaleOrder.put("amt_sum", dataMap.get("amtys_ord_total"));//订单总金额
			rtmsSaleOrder.put("amt_payable", dataMap.get("amtys_ord_total"));//应收金额
		}
		rtmsSaleOrder.put("qty_sum", dataMap.get("qty_ord_total"));//订单量
		// 兼容智能终端实收
		if (dataMap.containsKey("received_amount")) {
			rtmsSaleOrder.put("amt_paid", dataMap.get("received_amount"));//实付金额
		} else {
			rtmsSaleOrder.put("amt_paid", dataMap.get("amt_ord_total"));//实付金额
		}
		
		if (dataMap.get("pay_type")==null) {
			rtmsSaleOrder.put("pay_type", "1");//支付方式 1:现金 2：刷卡
		} else {
			rtmsSaleOrder.put("pay_type", dataMap.get("pay_type"));
		}
		
		BigDecimal amtOrdTotal = BigDecimal.ZERO;
		BigDecimal qtyOrdTotal = BigDecimal.ZERO;
		List<Map<String, Object>> itemList = (List<Map<String, Object>>) dataMap.get("list");
		List<Map<String, Object>> newItemList = new ArrayList<Map<String, Object>>();
		
		StringBuffer itemIdList=new StringBuffer();
		for(Map<String, Object> map : itemList){
			if(ParamUtil.isTobacco(map)){
				Map<String,Object> itemMap=new HashMap<String,Object>();
//<<<<<<< .working
//				
//				String itemId = MapUtil.getString(map, "item_id");//id
//				BigDecimal saleAmt = BigDecimal.ZERO;//销售金额 
//				BigDecimal lineRebateAmt = MapUtil.getBigDecimal(map, "line_rebate_amount");//单行折扣
//				BigDecimal otherAdjustedAmt = MapUtil.getBigDecimal(map, "other_adjusted_amount");//行分配折扣 
//				BigDecimal unitRatio = MapUtil.getBigDecimal(map, "unit_ratio");
//				BigDecimal qty = BigDecimal.ONE;//数量
//				BigDecimal price = BigDecimal.ZERO;//单价
//				
//=======
				
				String itemId = MapUtil.getString(map, "item_id");//id
				BigDecimal saleAmt = BigDecimal.ZERO;//销售金额 
				BigDecimal lineRebateAmt = MapUtil.getBigDecimal(map, "line_rebate_amount");//单行折扣
				BigDecimal otherAdjustedAmt = MapUtil.getBigDecimal(map, "other_adjusted_amount");//行分配折扣 
				BigDecimal unitRatio = MapUtil.getBigDecimal(map, "unit_ratio");
				BigDecimal qty = BigDecimal.ONE;//数量
				BigDecimal price = BigDecimal.ZERO;//单价
				
				if(map.containsKey("sale_quantity")) {
					qty = MapUtil.getBigDecimal(map, "sale_quantity", BigDecimal.ONE);
				} else {
					qty = MapUtil.getBigDecimal(map, "qty_ord", BigDecimal.ONE);
				}
				
				if("1".equals(map.get("unit_ratio").toString())){
					itemMap.put("um_id", "02");//单位：01:支，02:盒，03:条，默认:02)
				}else{
					itemMap.put("um_id", "03");
				}
				
				// 兼容智能终端
				if(map.containsKey("sale_amount")) {
					saleAmt = MapUtil.getBigDecimal(map, "sale_amount");
				} else {
					saleAmt = MapUtil.getBigDecimal(map, "amt_ord");
				}
				
				// 兼容智能终端
				if(map.containsKey("line_num")) {
					itemMap.put("line_num", map.get("line_num"));
				} else {
					itemMap.put("line_num", map.get("line_label"));
				}
				
				saleAmt = saleAmt.subtract(lineRebateAmt).subtract(otherAdjustedAmt);//实际销售价格
				price = saleAmt.divide(qty, 2, BigDecimal.ROUND_HALF_UP);
				saleAmt = saleAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
				itemMap.put("item_id", itemId);
				itemMap.put("amt", saleAmt);//总销售价格
				itemMap.put("qty", qty);//数量
				itemMap.put("price", price);//map.get("big_pri4")//单价
				itemMap.put("unit_ratio", unitRatio);//转换比
				itemMap.put("discount", MapUtil.get(map, "discount", "100"));
				newItemList.add(itemMap);
				itemIdList.append(","+itemId.trim());
				
				amtOrdTotal = amtOrdTotal.add(saleAmt);
				qtyOrdTotal = qtyOrdTotal.add(qty.multiply(unitRatio));
			}
		}
		
		Map<String, String> params = null;
		
		if(!newItemList.isEmpty()) {
			
			newItemList = selectWhse(newItemList,dataMap,itemIdList);
			
			rtmsSaleOrder.put("item_list", newItemList);
			rtmsSaleOrder.put("amt_cigarette", amtOrdTotal);//烟金额
			rtmsSaleOrder.put("qty_cigarette", qtyOrdTotal);//烟的定购量
			rtmsSaleOrder.put("ip", MapUtil.getString(dataMap, "ip"));//烟的定购量
			rtmsSaleOrder.put("order_type", MapUtil.getString(dataMap, "order_type"));
			String jsonString = JsonUtil.map2json(rtmsSaleOrder);
			params = new HashMap<String, String>();
			params.put("params", jsonString);
		}
		
		if(params!=null){
			Map<String,Object> data2RtmsMap=new HashMap<String,Object>();
			data2RtmsMap.put("url", "retail/uploadData/uploadRetailCo");
			data2RtmsMap.put("data", params);
			LOG.debug("向rtms提交销售单数据  IUploadDataToRtmsServiceImpl submitSaleOrder2RTMS data2RtmsMap:"+data2RtmsMap);
			ThreadConfig.concurrentLinkedQueue.add(data2RtmsMap);
			rtmsThread.startThread();
		}
	}
	
	
//	@Override
//	public void submitSaleOrder2RTMS2(Map<String, Object> dataMap)throws Exception {
//		LOG.debug("IUploadDataToRtmsServiceImpl submitSaleOrder2RTMS dataMap:"+dataMap);
//		Map<String, Object> rtmsSaleOrder = new HashMap<String, Object>();
//		rtmsSaleOrder.put("cust_id", dataMap.get("merch_id"));
//		rtmsSaleOrder.put("sale_id", dataMap.get("order_id"));
//		if(dataMap.containsKey("consumer_id")){
//			rtmsSaleOrder.put("consumer_id", dataMap.get("consumer_id"));
//		}
//		rtmsSaleOrder.put("sale_date", dataMap.get("order_date"));
//		rtmsSaleOrder.put("sale_time", dataMap.get("order_time"));
//		rtmsSaleOrder.put("pay_time", dataMap.get("order_time"));
//		// 兼容智能终端
////		if (dataMap.containsKey("total_amount")) {
////			rtmsSaleOrder.put("amt_sum", dataMap.get("total_amount"));//订单总金额
////			rtmsSaleOrder.put("amt_payable", dataMap.get("input_amtys_ord_total"));//应付金额
////		} else {
//			rtmsSaleOrder.put("amt_sum", dataMap.get("order_amt_total"));//订单总金额（未折扣金额
//			rtmsSaleOrder.put("amt_payable", MapUtil.getBigDecimal(dataMap, "input_amtys_ord_total", MapUtil.getBigDecimal(dataMap, "amtys_ord_total")));//应付金额（输入应收
//					
////		}
//		rtmsSaleOrder.put("qty_sum", dataMap.get("qty_ord_total"));//订单量
//		// 兼容智能终端实收
////		if (dataMap.containsKey("received_amount")) {
////			rtmsSaleOrder.put("amt_paid", dataMap.get("received_amount"));//实付金额
////		} else {
//			rtmsSaleOrder.put("amt_paid", dataMap.get("amt_ord_total"));//实付金额
////		}
//		
//		if (dataMap.get("pay_type")==null) {
//			rtmsSaleOrder.put("pay_type", "1");//支付方式 1:现金 2：刷卡
//		} else {
//			rtmsSaleOrder.put("pay_type", dataMap.get("pay_type"));
//		}
//		
//		BigDecimal amtOrdTotal = BigDecimal.ZERO;
//		BigDecimal qtyOrdTotal = BigDecimal.ZERO;
//		List<Map<String, Object>> itemList = (List<Map<String, Object>>) dataMap.get("list");
//		List<Map<String, Object>> newItemList = new ArrayList<Map<String, Object>>();
//		
//		StringBuffer itemIdList=new StringBuffer();
//		for(Map<String, Object> map : itemList){
//			if(ParamUtil.isTobacco(map)){
//				Map<String,Object> itemMap=new HashMap<String,Object>();
//				itemMap.put("item_id", map.get("item_id"));
//				itemMap.put("price", map.get("big_pri4"));
//>>>>>>> .merge-right.r7908
//				if(map.containsKey("sale_quantity")) {
//					qty = MapUtil.getBigDecimal(map, "sale_quantity", BigDecimal.ONE);
//				} else {
//					qty = MapUtil.getBigDecimal(map, "qty_ord", BigDecimal.ONE);
//				}
//				
//				if("1".equals(map.get("unit_ratio").toString())){
//					itemMap.put("um_id", "02");//单位：01:支，02:盒，03:条，默认:02)
//				}else{
//					itemMap.put("um_id", "03");
//				}
//				
//				// 兼容智能终端
//<<<<<<< .working
//				if(map.containsKey("sale_amount")) {
//					saleAmt = MapUtil.getBigDecimal(map, "sale_amount");
//				} else {
//					saleAmt = MapUtil.getBigDecimal(map, "amt_ord");
//				}
//=======
////				if(map.containsKey("sale_amount")) {
//					BigDecimal lineSaleAmt = MapUtil.getBigDecimal(map, "sale_amount").subtract(MapUtil.getBigDecimal(map, "line_rebate_amount")).subtract(MapUtil.getBigDecimal(map, "other_adjusted_amount"));
//					itemMap.put("amt", lineSaleAmt);
////				} else {
////					itemMap.put("amt", map.get("amt_ord"));
////				}
//>>>>>>> .merge-right.r7908
//				
//				// 兼容智能终端
//				if(map.containsKey("line_num")) {
//					itemMap.put("line_num", map.get("line_num"));
//				} else {
//					itemMap.put("line_num", map.get("line_label"));
//				}
//				
//				saleAmt = saleAmt.subtract(lineRebateAmt).subtract(otherAdjustedAmt);//实际销售价格
//				price = saleAmt.divide(qty, 2, BigDecimal.ROUND_HALF_UP);
//				saleAmt = saleAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//				itemMap.put("item_id", itemId);
//				itemMap.put("amt", saleAmt);//总销售价格
//				itemMap.put("qty", qty);//数量
//				itemMap.put("price", price);//map.get("big_pri4")//单价
//				itemMap.put("unit_ratio", unitRatio);//转换比
//				itemMap.put("discount", MapUtil.get(map, "discount", "100"));
//				newItemList.add(itemMap);
//				itemIdList.append(","+itemId.trim());
//				
//<<<<<<< .working
//				amtOrdTotal = amtOrdTotal.add(saleAmt);
//				qtyOrdTotal = qtyOrdTotal.add(qty.multiply(unitRatio));
//=======
//				double qtyOrd = 0;
//				if(map.containsKey("sale_quantity")) {
//					qtyOrd = Double.valueOf(map.get("sale_quantity").toString());
//				} else {
//					qtyOrd = Double.valueOf(map.get("qty_ord").toString());
//				}
//				double unitRatio = Double.valueOf(map.get("unit_ratio").toString());
//				// 兼容智能终端
////				if(map.containsKey("sale_amount")) {
//					amtOrdTotal = amtOrdTotal.add(lineSaleAmt);
////				} else {
////					amtOrdTotal = amtOrdTotal.add(new BigDecimal(map.get("amt_ord").toString()));
////				}
//				qtyOrdTotal = qtyOrdTotal.add(new BigDecimal(qtyOrd*unitRatio));
//>>>>>>> .merge-right.r7908
//			}
//		}
//		
//		Map<String, String> params = null;
//		
//		if(!newItemList.isEmpty()) {
//			
//			newItemList = selectWhse(newItemList,dataMap,itemIdList);
//			
//			rtmsSaleOrder.put("item_list", newItemList);
//			rtmsSaleOrder.put("amt_cigarette", amtOrdTotal);//烟金额
//			rtmsSaleOrder.put("qty_cigarette", qtyOrdTotal);//烟的定购量
//			String jsonString = JsonUtil.map2json(rtmsSaleOrder);
//			params = new HashMap<String, String>();
//			params.put("params", jsonString);
//		}
//		
//		if(params!=null){
//			Map<String,Object> data2RtmsMap=new HashMap<String,Object>();
//			data2RtmsMap.put("url", "retail/uploadData/uploadRetailCo");
//			data2RtmsMap.put("data", params);
//			LOG.debug("------向rtms提交销售单  IUploadDataToRtmsServiceImpl submitSaleOrder2RTMS dataMap:"+dataMap);
//			LOG.debug("------向rtms提交销售单  IUploadDataToRtmsServiceImpl submitSaleOrder2RTMS data2RtmsMap:"+data2RtmsMap);
//			ThreadConfig.concurrentLinkedQueue.add(data2RtmsMap);
//			rtmsThread.startThread();
//		}
//	}
	
	@Override
	public void submitBatchSaleOrder2RTMS(List<Map<String, Object>> orderParamList) throws Exception {
		LOG.debug("IUploadDataToRtmsServiceImpl submitBatchSaleOrder2RTMS orderParamList:"+orderParamList);
		for (Map<String, Object> dataMap : orderParamList) {
			Map<String, Object> rtmsSaleOrder = new HashMap<String, Object>();
			rtmsSaleOrder.put("cust_id", dataMap.get("merch_id"));
			rtmsSaleOrder.put("sale_id", dataMap.get("order_id"));
			if(dataMap.containsKey("consumer_id")){
				rtmsSaleOrder.put("consumer_id", dataMap.get("consumer_id"));
			}
			rtmsSaleOrder.put("sale_date", dataMap.get("order_date"));
			rtmsSaleOrder.put("sale_time", dataMap.get("order_time"));
			rtmsSaleOrder.put("pay_time", dataMap.get("order_time"));
			rtmsSaleOrder.put("amt_sum", dataMap.get("amtys_ord_total"));
			rtmsSaleOrder.put("qty_sum", dataMap.get("qty_ord_total"));
			rtmsSaleOrder.put("amt_payable", dataMap.get("amtys_ord_total"));
			rtmsSaleOrder.put("amt_paid", dataMap.get("amt_ord_total"));
			
			if(dataMap.get("pay_type")==null) {
				rtmsSaleOrder.put("pay_type", "1");
			} else {
				rtmsSaleOrder.put("pay_type", dataMap.get("pay_type"));
			}
			
			BigDecimal amtOrdTotal = BigDecimal.ZERO;
			BigDecimal qtyOrdTotal = BigDecimal.ZERO;
			List<Map<String, Object>> itemList = (List<Map<String, Object>>) dataMap.get("list");
			List<Map<String, Object>> newItemList = new ArrayList<Map<String, Object>>();
			
			StringBuffer itemIdList=new StringBuffer();
			for(Map<String, Object> map : itemList){
				if(ParamUtil.isTobacco(map)){
					Map<String,Object> itemMap=new HashMap<String,Object>();
					itemMap.put("item_id", map.get("item_id"));
					itemMap.put("price", map.get("big_pri4"));
					itemMap.put("qty", map.get("qty_ord"));
					if("1".equals(map.get("unit_ratio").toString())){
						itemMap.put("um_id", "02");
					}else{
						itemMap.put("um_id", "03");
					}
					itemMap.put("unit_ratio", map.get("unit_ratio"));
					itemMap.put("amt", map.get("amt_ord"));
					itemMap.put("discount", map.get("discount"));
					itemMap.put("line_num", map.get("line_label"));
					
					newItemList.add(itemMap);
					itemIdList.append(","+map.get("item_id").toString().trim());
					
					double qtyOrd = Double.valueOf(map.get("qty_ord").toString());
					double unitRatio = Double.valueOf(map.get("unit_ratio").toString());
					amtOrdTotal = amtOrdTotal.add(new BigDecimal(map.get("amt_ord").toString()));
					qtyOrdTotal = qtyOrdTotal.add(new BigDecimal(qtyOrd*unitRatio));
				}
			}
			
			Map<String, String> params = null;
			
			if(!newItemList.isEmpty()) {
				
				newItemList = selectWhse(newItemList,dataMap,itemIdList);
				
				rtmsSaleOrder.put("item_list", newItemList);
				rtmsSaleOrder.put("amt_cigarette", amtOrdTotal);
				rtmsSaleOrder.put("qty_cigarette", qtyOrdTotal);
				String jsonString = JsonUtil.map2json(rtmsSaleOrder);
				params = new HashMap<String, String>();
				params.put("params", jsonString);
			}
			
			if(params!=null){
				Map<String,Object> data2RtmsMap=new HashMap<String,Object>();
				data2RtmsMap.put("url", "retail/uploadData/uploadRetailCo");
				data2RtmsMap.put("data", params);
				ThreadConfig.concurrentLinkedQueue.add(data2RtmsMap);
				rtmsThread.startThread();
			}
		}
	}
	
	
	@Override
	public void insertWhseTobaccoToRTMS(Map<String, Object> dataMap)throws Exception {
		LOG.debug("IUploadDataToRtmsServiceImpl insertWhseTobaccoToRTMS dataMap:"+dataMap);
//		Map<String,Object> purchMap=new HashMap<String,Object>();
		Map<String,Object> purchParamMap=new HashMap<String,Object>();
		purchParamMap.put("cust_id", dataMap.get("merch_id"));   //商户id
		purchParamMap.put("tune_date", dataMap.get("turn_date"));
		purchParamMap.put("tune_time", dataMap.get("crt_time"));

		double qtyLoss = Double.valueOf(dataMap.get("qty_loss").toString());
		double amtLoss = Double.valueOf(dataMap.get("amt_loss").toString());
		double qtyProfit = Double.valueOf(dataMap.get("qty_profit").toString());
		double amtProfit = Double.valueOf(dataMap.get("amt_profit").toString());
		
		purchParamMap.put("tune_qty", qtyProfit-qtyLoss); 
		purchParamMap.put("tune_amt", amtProfit-amtLoss); 
		
		List<Map<String,Object>> purchList=(List<Map<String,Object>>)dataMap.get("list");
		List<Map<String,Object>> itemList=new ArrayList<Map<String,Object>>();
		
		StringBuffer itemIdList=new StringBuffer();
		for(Map<String, Object> map : purchList) {
			if(ParamUtil.isTobacco(map)){
				Map<String,Object> itemMap=new HashMap<String,Object>();
				itemMap.put("item_id", map.get("item_id"));
				itemMap.put("price", map.get("cost"));
				itemMap.put("qty", map.get("qty_pl"));
				itemMap.put("um_id", "02");
				itemMap.put("unit_ratio", "1");
				itemMap.put("amt", map.get("amt_pl"));
				
				itemList.add(itemMap);
				itemIdList.append(","+map.get("item_id").toString().trim());
			}
		}
		
		Map<String, String> params = null;
		if(itemList!=null&&itemList.size()>0){
			itemList = selectWhse(itemList,dataMap,itemIdList); 
			purchParamMap.put("item_list", itemList);
			String jsonString = JsonUtil.map2json(purchParamMap);
			params = new HashMap<String, String>();
			params.put("params", jsonString);
		}
		
		if(params!=null){
			Map<String,Object> data2RtmsMap=new HashMap<String,Object>();
			data2RtmsMap.put("url", "retail/uploadData/uploadTake");
			data2RtmsMap.put("data", params);
			ThreadConfig.concurrentLinkedQueue.add(data2RtmsMap);
			rtmsThread.startThread();
		}
	}

	@Override
	public void insertPurchTobaccoToRTMS(Map<String, Object> dataMap)throws Exception {
		LOG.debug("IUploadDataToRtmsServiceImpl insertPurchTobaccoToRTMS dataMap:"+dataMap);
		
		Map<String,Object> whseParamMap=new HashMap<String,Object>();

		whseParamMap.put("cust_id", dataMap.get("merch_id"));   //零售户编号
		whseParamMap.put("co_num", dataMap.get("order_id"));   //零售户编号
		whseParamMap.put("in_date", DateUtil.getToday());//库存调整日期
		whseParamMap.put("in_time", DateUtil.getCurrentTime().substring(8));//库存调整时间
		
		//入库总量
		BigDecimal tuneQtyTotal = BigDecimal.ZERO;
		//入库总金额
		BigDecimal tuneAmtTotal = BigDecimal.ZERO;
		
		Map<String, String> params = null;
		
		//商品入库(包含卷烟订单)
		if(dataMap.get("list")!=null){
			List<Map<String,Object>> whseList=(List<Map<String,Object>>)dataMap.get("list");
			List<Map<String,Object>> itemList=new ArrayList<Map<String,Object>>();
			StringBuffer itemIdList=new StringBuffer();
			for(Map<String, Object> map : whseList) {
				String type="";
				if(map.get("item_kind_id")!=null){
					type=map.get("item_kind_id").toString().trim();
				}
				if(map.get("ITEM_KIND_ID")!=null){
					type=map.get("ITEM_KIND_ID").toString().trim();
				}
				if(ParamUtil.isTobacco(type)){
					Map<String,Object> itemMap = new HashMap<String,Object>();
					itemMap.put("item_id", map.get("item_id"));
					itemMap.put("price", map.get("pri_wsale"));
					itemMap.put("qty", map.get("qty_ord"));
					itemMap.put("amt", map.get("amount"));
					itemMap.put("unit_ratio", map.get("unit_ratio"));
					if("1".equals(map.get("unit_ratio").toString().trim())){
						itemMap.put("um_id", "02");
					}else{
						itemMap.put("um_id", "03");
					}
					tuneQtyTotal=tuneQtyTotal.add(new BigDecimal(map.get("qty_ord").toString()));
					tuneAmtTotal=tuneAmtTotal.add(new BigDecimal(map.get("amount").toString()));
					itemIdList.append(","+map.get("item_id").toString().trim());
					itemList.add(itemMap);
				}
			}
			
			//查找库存信息
			if(itemList!=null&&itemList.size()>0){
				itemList = selectWhse(itemList,dataMap,itemIdList);
				
				whseParamMap.put("item_list", itemList);
				whseParamMap.put("whsein_qty", tuneQtyTotal);  //盘点库存量
				whseParamMap.put("whsein_amt", tuneAmtTotal);  //盘点库存金额
				String jsonString = JsonUtil.map2json(whseParamMap);
				params = new HashMap<String, String>();
				params.put("params", jsonString);
			}
		}
		
		if(params!=null){
			Map<String,Object> data2RtmsMap=new HashMap<String,Object>();
			data2RtmsMap.put("url", "retail/uploadData/uploadWhse");
			data2RtmsMap.put("data", params);
			ThreadConfig.concurrentLinkedQueue.add(data2RtmsMap);
			rtmsThread.startThread();
		}

	}
	
	/**
	 * 查询并添加库存信息
	 * @author 徐虎彬
	 * @date 2014年5月6日
	 * @param itemList
	 * @param dataMap
	 * @param itemIdList
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectWhse(List<Map<String,Object>> itemList,Map<String, Object> dataMap,StringBuffer itemIdList)throws Exception{
		LOG.debug("IUploadDataToRtmsServiceImpl selectWhse itemList:"+itemList);
		LOG.debug("IUploadDataToRtmsServiceImpl selectWhse dataMap:"+dataMap);
		LOG.debug("IUploadDataToRtmsServiceImpl selectWhse itemIdList:"+itemIdList);
		
		Map<String,Object> selectWhseMap=new HashMap<String,Object>();
		
		selectWhseMap.put("merch_id", dataMap.get("merch_id"));
		selectWhseMap.put("item_id",itemIdList.toString().substring(1) );
		selectWhseMap.put("page_index", "-1");
		selectWhseMap.put("page_size", "-1");
		
		List<Map<String,Object>> whseMapList=whseDao.selectWhseMerch(selectWhseMap);
		Map<String,Object> whseMap=new HashMap<String,Object>();
		if(whseMapList!=null&&whseMapList.size()>0){
			for(Map<String,Object> map:whseMapList){
				whseMap.put(map.get("item_id").toString(), map);
			}
		}
		if(itemList!=null&&whseMap!=null){
			for(int i=0;i<itemList.size();i++){
				Map<String,Object> map = (Map<String,Object>)whseMap.get(itemList.get(i).get("item_id").toString());
				if(map!=null){
					itemList.get(i).put("whse_qty", map.get("qty_whse"));
					BigDecimal pri= MapUtil.getBigDecimal(itemList.get(i), "price").setScale(3, BigDecimal.ROUND_HALF_UP);
					BigDecimal whseQty= MapUtil.getBigDecimal(map, "qty_whse");
					BigDecimal unitRatio= MapUtil.getBigDecimal(itemList.get(i), "unit_ratio", 1);
					BigDecimal whseAmt=(whseQty.divide(unitRatio, 2, BigDecimal.ROUND_HALF_UP)).multiply(pri);
					itemList.get(i).put("whse_amt", whseAmt);
				}
			}
		}
		return itemList;
	}
}
