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

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.dao.IMerchDao;
import com.ryx.social.retail.dao.IPurchOrderDao;
import com.ryx.social.retail.dao.IReturnOrderDao;
import com.ryx.social.retail.dao.ISaleOrderDao;
import com.ryx.social.retail.dao.IStatisticsDao;
import com.ryx.social.retail.dao.ISupplierDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.ICgtOrderService;
import com.ryx.social.retail.service.IPushService;
import com.ryx.social.retail.service.IStatisticsService;

@Service
public class PushServiceImpl implements IPushService {
	private Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);
	@Resource
	private IPurchOrderDao purchOrderDao;
	@Resource
	private ISaleOrderDao saleOrderDao;
	@Resource
	private IWhseDao whseDao;
	@Resource
	private IStatisticsDao statisticsDao;
	@Resource
	private IMerchDao merchDao;
	@Resource
	private IItemDao itemDao;
	@Resource
	private ISupplierDao supplierDao;
	@Resource
	private ICgtOrderService cgtOrderService;
	@Resource
	private IStatisticsService statisticService;
	/**
	 * 获取经营提醒信息
	 */
	private String getUnstockedListString(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> purchOrderParam = new HashMap<String, Object>();
		purchOrderParam.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		purchOrderParam.put("supplier_id", MapUtil.getString(paramMap, "supplier_id"));
		purchOrderParam.put("status", MapUtil.getString(paramMap, "status"));
		List<Map<String, Object>> unstockedOrderList = purchOrderDao.searchPurchOrder(purchOrderParam);
		if(unstockedOrderList.size()==0) {
			return "";
		}
		StringBuffer unstockedBuffer = new StringBuffer();
		for(Map<String, Object> unstockedOrder : unstockedOrderList) {
			unstockedBuffer.append(unstockedOrder.get("order_id")+",");
		}
		String unstockedListString = unstockedBuffer.toString();
		return unstockedListString.substring(0, unstockedListString.length()-1);
	}
	private String getUndisposedListString(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> saleOrderParam = new HashMap<String, Object>();
		saleOrderParam.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		saleOrderParam.put("order_type", "04");
		saleOrderParam.put("status", "01");
		List<Map<String, Object>> undisposedOrderList = saleOrderDao.selectSaleOrder(saleOrderParam);
		if(undisposedOrderList.size()==0) {
			return "";
		}
		StringBuffer undisposedBuffer = new StringBuffer();
		for(Map<String, Object> undisposedOrder : undisposedOrderList) {
			undisposedBuffer.append(undisposedOrder.get("order_id")+",");
		}
		String undisposedListString = undisposedBuffer.toString();
		return undisposedListString.substring(0, undisposedListString.length()-1);
	}
	private List<Map<String, Object>> getStockoutList(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> whseParam = new HashMap<String, Object>();
		whseParam.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		List<Map<String, Object>> stockoutWhseList = whseDao.selectWhseMerchJoinMerchItem(whseParam);
		List<Map<String, Object>> newStockoutWhseList = new ArrayList<Map<String, Object>>();
		BigDecimal qtyWhse = BigDecimal.ZERO;
		BigDecimal bigUnitRatio = BigDecimal.ZERO;
		BigDecimal newQtyWhse = BigDecimal.ZERO;
		for(Map<String, Object> stockoutWhse : stockoutWhseList) {
			Map<String, Object> newStockoutWhse = new HashMap<String, Object>();
			qtyWhse = MapUtil.getBigDecimal(stockoutWhse, "qty_whse");
			bigUnitRatio = MapUtil.getBigDecimal(stockoutWhse, "big_unit_ratio", new BigDecimal("1"));
			bigUnitRatio = bigUnitRatio.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("1") : bigUnitRatio;
			newQtyWhse = qtyWhse.divide(bigUnitRatio);
			newStockoutWhse.put("item_id", MapUtil.getString(stockoutWhse, "item_id"));
			newStockoutWhse.put("item_name", MapUtil.getString(stockoutWhse, "item_name"));
			newStockoutWhse.put("unit_name", MapUtil.getString(stockoutWhse, "big_unit_name"));
			newStockoutWhse.put("qty_whse", newQtyWhse);
			newStockoutWhse.put("item_kind_id", MapUtil.getString(stockoutWhse, "item_kind_id"));
			newStockoutWhseList.add(newStockoutWhse);
		}
		return newStockoutWhseList;
	}
	@Override
	public Map<String, Object> notifyManager(Map<String, Object> pushParam) throws Exception {
		logger.debug("PushServiceImpl notifyManager pushParam: " + pushParam);
		Map<String, Object> infoMap = new HashMap<String, Object>();
		Map<String, String> jsonParam = new HashMap<String, String>();
		jsonParam.put("custId", MapUtil.getString(pushParam, "merch_id"));
		jsonParam.put("comId", MapUtil.getString(pushParam, "com_id"));
		Map<String, Object> orderParamsResult = new HashMap<String, Object>();
		try {
			orderParamsResult = cgtOrderService.getOrderParams(jsonParam);
		} catch (Exception e) {
			logger.debug(" = * = * = * = * = 获取卷烟订货参数错误 = * = * = * = * = " + e);
		}
		infoMap.put("order_params", orderParamsResult);
		jsonParam.put("orderDate", MapUtil.getString(orderParamsResult, "order_date"));
		Map<String, Object> orderDetailResult = new HashMap<String, Object>();
		try {
			orderDetailResult = cgtOrderService.getOrderDetail(jsonParam);
		} catch (Exception e) {
			logger.debug(" = * = * = * = * = 获取卷烟订货详情错误 = * = * = * = * = " + e);
		}
		orderDetailResult.remove("purch_order_line"); // 从结果中移除无用的list
		orderDetailResult.remove("purch_order_line_temp");
		infoMap.put("order_detail", orderDetailResult);
//		String getOrderParamsUrl = this.getRootPath((String)pushParam.get("com_id")) + "cgtorder/orderparams";
//		Map<String, String> jsonParam = new HashMap<String, String>();
//		jsonParam.put("custId", (String)pushParam.get("merch_id"));
//		String orderParamResultJson = HttpUtil.post(getOrderParamsUrl, jsonParam);
//		Map<String, Object> orderParamResult = JsonUtil.json2Map(orderParamResultJson);
		// 是否可以订货
//		String orderStatus = "0";
//		String orderDate = "";
//		String orderId = "";
//		String code = (String) orderParamResult.get("code");
//		if(code!=null && Constants.SUCCESS.equals(code)) {
//			Map<String, Object> resultMap = (Map<String, Object>)orderParamResult.get("result");
//			if(resultMap!=null) {
//				orderDate = (String)resultMap.get("order_date");
//			}
//		}
		// 如果可以订货, 取订单状态
		// 卷烟订货的状态: 0不是访销周期, 1今天未订货, 2订单未结算, 9今天订货已完成
//		String getOrderDetailUrl = this.getRootPath((String)pushParam.get("merch_id")) + "cgtorder/getOrderDetail";
//		jsonParam.put("orderDate", orderDate); // 传日期, 返回coNum
//		String orderDetailResultJson = HttpUtil.post(getOrderDetailUrl, jsonParam);
//		Map<String, Object> orderDetailResult = (Map<String, Object>)JsonUtil.json2Map(orderDetailResultJson);
//		code = (String) orderParamResult.get("code"); // 重用这个code
//		if(code!=null && Constants.SUCCESS.equals(code)) {
//			Map<String, Object> resultMap = (Map<String, Object>)orderDetailResult.get("result");
//			if(resultMap!=null) {
//				String flag = (String) resultMap.get("flag");
//				String pmtStatus = (String) resultMap.get("pmt_status");
//				orderId = (String) resultMap.get("co_num"); // 订单号
//				if("0".equals(flag)) { // 未订货
//					orderStatus = "1";
//				} else if("1".equals(flag)) {
//					if("01".equals(pmtStatus)) { // pmt_status支付状态(01未支付，02已支付)
//						orderStatus = "2";
//					} else if("02".equals(pmtStatus)) {
//						orderStatus = "9";
//					}
//				}
//			}
//		}
//		infoMap.put("cgt_order_status", orderStatus);
//		infoMap.put("cgt_order_id", orderId);
//		infoMap.put("cgt_order_date", orderDate);
		//卷烟未入库的订单数, 查询purch_order固定SUPPLIER_ID, STATUS='04'的订单
		String unstockedListString = getUnstockedListString(pushParam);
		infoMap.put("cgt_order_unstocked_list", unstockedListString);
		//未处理订单, 查询sale_order ORDER_TYPE='03'
		String undisposedListString = getUndisposedListString(pushParam);
		infoMap.put("sale_order_undisposed_list", undisposedListString);
		//缺货提醒
		List<Map<String, Object>> stockoutList = getStockoutList(pushParam);
		infoMap.put("whse_stockout_list", stockoutList);
		return infoMap;
	}
//	/**
//	 * 获取经营分析数据
//	 */
//	//毛利润 = 销售额-销售商品进价
//	//毛利润<1000提示利润一般
//	private Map<String, Object> getGrossMargin(Map<String, Object> paramMap) throws Exception {
//		Map<String, Object> dataMap = new HashMap<String, Object>();
//		Map<String, Object> saleOrderParam =  new HashMap<String, Object>();
//		saleOrderParam.put("merch_id", paramMap.get("merch_id"));
//		saleOrderParam.put("start_date", paramMap.get("order_date"));
//		saleOrderParam.put("end_date", paramMap.get("order_date"));
//		List<Map<String, Object>> grossMarginDailyList = saleOrderDao.selectGrossMarginDaily(saleOrderParam);
//		BigDecimal grossMargin = BigDecimal.ZERO;
//		if(grossMarginDailyList.size()>0) {
//			grossMargin = (BigDecimal) grossMarginDailyList.get(0).get("gross_margin");
//		}
//		Map<String, Object> returnOrderParam =  new HashMap<String, Object>();
//		returnOrderParam.put("merch_id", paramMap.get("merch_id"));
//		returnOrderParam.put("return_order_date", paramMap.get("order_date"));
//		BigDecimal amtReturnTotal = returnOrderDao.getAmtReturnTotal(returnOrderParam);
//		grossMargin = grossMargin.subtract(amtReturnTotal);
//		dataMap.put("profit_amount", grossMargin);
//		String profitMessage = "利润稳定！再接再励！";
//		switch(Integer.parseInt(paramMap.get("merch_id").toString().substring(paramMap.get("merch_id").toString().length()-1))%3) {
//		case 0:profitMessage = "利润率提高！请多努力！"; break;
//		case 1:profitMessage = "利润大于平均水平！继续保持！";
//		}
////		String profitMessage = "恭喜! 利润丰厚!";
////		if(grossMargin.compareTo(new BigDecimal(1000))<0) {
////			profitMessage = "利润一般, 请多努力!";
////		}
//		dataMap.put("profit_message", profitMessage);
//		return dataMap;
//	}
//	private Map<String, Object> getPurchInfo(Map<String, Object> paramMap) throws Exception {
//		Map<String, Object> purchOrderParam =  new HashMap<String, Object>();
//		purchOrderParam.put("merch_id", paramMap.get("merch_id"));
//		purchOrderParam.put("order_date", paramMap.get("order_date"));
//		Map<String, Object> dataMap = purchOrderDao.getPurchInfo(purchOrderParam);
//		String purchMessage = "进货额";
//		dataMap.put("purch_message", purchMessage);
//		return dataMap;
//	}
	
//	private Map<String, Object> getSaleInfo(Map<String, Object> paramMap) throws Exception {
//		Map<String, Object> dataMap = new HashMap<String, Object>();
//		Map<String, Object> saleOrderParam =  new HashMap<String, Object>();
//		saleOrderParam.put("merch_id", paramMap.get("merch_id"));
//		saleOrderParam.put("order_date", paramMap.get("order_date"));
//		List<Map<String, Object>> saleInfoList = saleOrderDao.getSaleInfoWithOrderType(saleOrderParam);
//		BigDecimal saleShopAmount = BigDecimal.ZERO;
//		BigDecimal saleShopCount = BigDecimal.ZERO;
//		BigDecimal saleOnlineAmount = BigDecimal.ZERO;
//		BigDecimal saleOnlineCount = BigDecimal.ZERO;
//		for(Map<String, Object> saleInfo : saleInfoList) {
//			String orderType = (String) saleInfo.get("order_type");
//			if("03".equals(orderType)) {
//				saleOnlineAmount = saleOnlineAmount.add((BigDecimal) saleInfo.get("amt_total"));
//				saleOnlineCount = saleOnlineCount.add((BigDecimal) saleInfo.get("order_count"));
//			} else {
//				saleShopAmount = saleShopAmount.add((BigDecimal) saleInfo.get("amt_total"));
//				saleShopCount = saleShopAmount.add((BigDecimal) saleInfo.get("order_count"));
//			}
//		}
//		BigDecimal saleTotalAmount = saleShopAmount.add(saleOnlineAmount);
//		BigDecimal saleTotalCount = saleShopCount.add(saleOnlineCount);
//		String saleMessage = "柜台销售等于网点销售";
//		if(saleShopAmount.compareTo(saleOnlineAmount)>0) {
//			saleMessage = "柜台销售大于网点销售";
//		} else if(saleShopAmount.compareTo(saleOnlineAmount)<0) {
//			saleMessage = "柜台销售小于网点销售";
//		}
//		dataMap.put("sale_message", saleMessage);
//		dataMap.put("sale_shop_amount", saleShopAmount);
//		dataMap.put("sale_shop_count", saleShopCount);
//		dataMap.put("sale_online_amount", saleOnlineAmount);
//		dataMap.put("sale_online_count", saleOnlineCount);
//		dataMap.put("sale_total_amount", saleTotalAmount);
//		dataMap.put("sale_total_count", saleTotalCount);
//		return dataMap;
//	}
//	private Map<String, Object> getWhseInfo(Map<String, Object> paramMap) throws Exception {
//		Map<String, Object> dataMap = new HashMap<String, Object>();
//		Map<String, Object> whseTurnParam =  new HashMap<String, Object>();
//		whseTurnParam.put("merch_id", paramMap.get("merch_id"));
//		whseTurnParam.put("turn_date", paramMap.get("order_date"));
//		//盘点金额 AMT_WHSE_TOTAL, 库存商品数量(排除库存=0的商品)
//		BigDecimal amtWhseTotal = BigDecimal.ZERO;
//		int whseCount = 0;
//		List<Map<String, Object>> whseInfoList = whseDao.selectWhseInfo(whseTurnParam);
//		for(Map<String, Object> whseInfo : whseInfoList) {
//			amtWhseTotal = amtWhseTotal.add((BigDecimal) whseInfo.get("amt_whse_total"));
//			whseCount++;
//		}
//		dataMap.put("whse_amount", amtWhseTotal);
//		dataMap.put("whse_count", whseCount);
//		//盘差 AMT_WHSE_TURN_PROFIT AMT_WHSE_TURN_LOSS WHSE_TURN_COUNT
//		BigDecimal amtWhseTurnProfit = BigDecimal.ZERO;
//		BigDecimal amtWhseTurnLoss = BigDecimal.ZERO;
//		List<Map<String, Object>> whseTurnInfoList = whseDao.selectWhseTurnInfo(whseTurnParam);
//		for(Map<String, Object> whseTurnInfo : whseTurnInfoList) {
//			amtWhseTurnProfit = amtWhseTurnProfit.add((BigDecimal) whseTurnInfo.get("amt_whse_turn_profit"));
//			amtWhseTurnLoss = amtWhseTurnLoss.add((BigDecimal) whseTurnInfo.get("amt_whse_turn_loss"));
//		}
//		dataMap.put("whse_turn_profit", amtWhseTurnProfit);
//		dataMap.put("whse_turn_loss", amtWhseTurnLoss);
//		BigDecimal whseTurnCount = BigDecimal.ZERO;
//		List<Map<String, Object>> whseTurnCountList = whseDao.selectWhseTurnCount(whseTurnParam);
//		if(whseTurnCountList.size()==1) {
//			whseTurnCount = (BigDecimal) whseTurnCountList.get(0).get("whse_turn_count");
//		}
//		dataMap.put("whse_turn_count", whseTurnCount);
//		String whseTurnMessage = amtWhseTurnLoss.compareTo(new BigDecimal(100))<0?"盘差可控, 不错喔":"盘差失控! 请查看详情";
//		dataMap.put("whse_turn_message", whseTurnMessage);
//		return dataMap;
//	}
	private Map<String, Object> getBusinessHistory(Map<String, Object> paramMap) throws Exception {
//		Map<String, Object> dataMap = new HashMap<String, Object>();
		Map<String, Object> statisticsParam =  new HashMap<String, Object>();
		statisticsParam.put("merch_id", paramMap.get("merch_id"));
		statisticsParam.put("start_date", paramMap.get("start_date"));
		statisticsParam.put("end_date", paramMap.get("end_date"));
		Map<String, Object> businessHistoryMap = statisticsDao.getBusinessHistory(statisticsParam);
//		BigDecimal profitAmount = (BigDecimal) businessHistoryMap.get("profit_amount");
		// 利润信息
		String profitMessage = "利润稳定！再接再励！";
		switch(Integer.parseInt(paramMap.get("merch_id").toString().substring(paramMap.get("merch_id").toString().length()-1))%3) {
			case 0:profitMessage = "利润率提高！请多努力！"; break;
			case 1:profitMessage = "利润大于平均水平！继续保持！";
		}
//		if(profitAmount.compareTo(new BigDecimal(1000))<0) {
//			profitMessage = "利润一般, 请多努力!";
//		}
		businessHistoryMap.put("profit_message", profitMessage);
		// 进货信息
		String purchMessage = "进货额";
		businessHistoryMap.put("purch_message", purchMessage);
		// 销售信息
		BigDecimal saleShopAmount = (BigDecimal) businessHistoryMap.get("sale_shop_amount");
		BigDecimal saleOnlineAmount = (BigDecimal) businessHistoryMap.get("sale_online_amount");
		String saleMessage = "柜台销售等于网点销售";
		if(saleShopAmount.compareTo(saleOnlineAmount)>0) {
			saleMessage = "柜台销售大于网点销售";
		} else if(saleShopAmount.compareTo(saleOnlineAmount)<0) {
			saleMessage = "柜台销售小于网点销售";
		}
		businessHistoryMap.put("sale_message", saleMessage);
		// 库存信息
		BigDecimal amtWhseTurnLoss = (BigDecimal) businessHistoryMap.get("whse_turn_loss");
		String whseTurnMessage = amtWhseTurnLoss.compareTo(new BigDecimal(100))<0?"盘差可控, 不错喔":"盘差失控! 请查看详情";
		businessHistoryMap.put("whse_turn_message", whseTurnMessage);
		return businessHistoryMap;
	}
	/**
	 * 获取经营分析数据
	 */
	@Override
	public Map<String, Object> analysisBusiness(Map<String, Object> pushParam) throws Exception {
		logger.debug("PushServiceImpl analysisBusiness pushParam: " + pushParam);
		Map<String, Object> dataMap = new HashMap<String, Object>();
		if(pushParam.get("order_date")!=null) {
			//今日 利/存 -卷烟/非卷烟利润，本月利润
			Map<String,Object> profitWhseSaleInfoMap=statisticService.searchBusinessAnalysis(pushParam);
			dataMap.putAll(profitWhseSaleInfoMap);
			return dataMap;
		} else {
			return getBusinessHistory(pushParam);
		}
	}
	
	//搜索附近的店铺
	public List searchShopsByKeywords(Map<String, Object> paramsMap)throws Exception{
		logger.debug("PushServiceImpl searchShopsByKeywords paramsMap:"+paramsMap);
		return merchDao.searchMerch(paramsMap);
	}
	//搜索商品包括扫码查询(距离)
	public List searchGoodsByKeywords(Map<String, Object> paramsMap)throws Exception{
		logger.debug("PushServiceImpl searchGoodsByKeywords paramsMap:"+paramsMap);		
		
		return itemDao.searchGoods(paramsMap);
	}
	@Override
	public List<Map<String, Object>> selectSupplier(Map<String,Object> thisMap) throws Exception {
		Map<String,Object> listMap=new HashMap<String,Object>();
		listMap.put("supplier_id", thisMap.get("supplier_id"));
		listMap.put("item_bar", thisMap.get("item_bar"));
		return supplierDao.selectItem(listMap);
	}
	
	
}
