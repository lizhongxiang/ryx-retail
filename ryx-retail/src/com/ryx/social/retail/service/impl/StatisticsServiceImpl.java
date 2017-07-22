package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IBaseDataDao;
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.dao.IOrderDao;
import com.ryx.social.retail.dao.IStatisticsDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.ICgtOrderService;
import com.ryx.social.retail.service.IStatisticsService;
import com.ryx.social.retail.util.ParamUtil;
import com.ryx.social.retail.util.RetailConfig;

@Service("statisticsService")
public class StatisticsServiceImpl implements IStatisticsService {
	private final Logger LOG = LoggerFactory.getLogger(StatisticsServiceImpl.class);
	
	private static final int RFAMTDIGIT = 2; //报表 金额类 显示位数
	private static final int RFQTYDIGIT = 1; //报表 数量类 显示位数
	private static final int RFPERCENTAGEDIGIT = 4; //报表 百分类 计算 位数
	
	@Resource
	private IOrderDao orderDao;
	@Resource
	private IWhseDao whseDao;
	@Resource
	private IItemDao itemDao;
	@Resource
	private IBaseDataDao baseDataDao;
	@Resource
	private IStatisticsDao statisticsDao;
	@Resource
	private ICgtOrderService cgtOrderService;
	
	
	// 销售单  月报表	  
	public List<Map<String, Object>> searchSaleroomMonthly(Map<String, Object> paramsMap)throws Exception{
		LOG.debug("StatisticsServiceImpl searchSaleroomMonthly paramsMap:"+paramsMap);
		Integer number=Integer.parseInt(paramsMap.get("number")+"");
		Date date = new Date();
		String newDate = (new SimpleDateFormat("yyyyMMdd")).format(date) ;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
//		Integer number = 2;
		cal.add(Calendar.MARCH, -number+1);
		String oldDate = (new SimpleDateFormat("yyyyMM")).format(cal.getTime());
		oldDate+="01";
		paramsMap.put("start_date", oldDate);
	    paramsMap.put("end_date", newDate);
	    List<String> monthList=DateUtil.getListBetweenStartingAndEnding(oldDate.substring(0,6),newDate.substring(0,6));//得到开始、结束日期	   
	    List<Map<String, Object>> returnList=orderDao.searchSaleroom(paramsMap);//dao得到的数据	    
	    for (int i = 0; i < monthList.size(); i++) {
			String today=monthList.get(i);
			if(i>=returnList.size()||!returnList.get(i).get("time_interval").equals(today)){
				Map<String,Object> nullMap=new HashMap<String, Object>();
				nullMap.put("time_interval", today);
				nullMap.put("saleroom", 0);
				returnList.add(i,nullMap);
			}
		}	 
		return returnList;
	}
	//销售单  日报表
	@Override
	public List<Map<String, Object>> searchSaleroomDay(Map<String, Object> paramsMap) throws Exception {
		LOG.debug("StatisticsServiceImpl serchSaleRoomDay paramsMap:"+paramsMap);
		 Date date = new Date();
		 Integer newDate=Integer.parseInt((new SimpleDateFormat("yyyyMMdd")).format(date))-1;
	     Calendar cal = Calendar.getInstance();
	     cal.setTime(date);
	     Integer number=Integer.parseInt((String) paramsMap.get("number"));
	     cal.add(Calendar.DATE, -number);
	     String oldDate=(new SimpleDateFormat("yyyyMMdd")).format(cal.getTime());
	     List<String> dayList=DateUtil.getListBetweenStartingAndEnding(oldDate+"",newDate+"" );
	     LOG.debug("周期类型：日-----开始日期："+oldDate+"---结束日期："+newDate+"---周期数："+number);
	     paramsMap.put("start_date", oldDate);
	     paramsMap.put("end_date", newDate);
	     List<Map<String, Object>> returnList=orderDao.searchSaleroom(paramsMap);//dao层接受的数据
	     for (int i = 0; i < dayList.size(); i++) {
			if(i>=returnList.size()||!dayList.get(i).equals(returnList.get(i).get("time_interval"))){
				Map<String, Object> nullMap=new HashMap<String, Object>();
				nullMap.put("time_interval", dayList.get(i));
				nullMap.put("saleroom", 0);
				returnList.add(i,nullMap);
			}
		}
	     return returnList;
	}
	//销售单   周报表
	@Override
	public List<Map<String, Object>> searchSaleroomWeek(Map<String, Object> paramsMap) throws Exception {
		Calendar canlendar = Calendar.getInstance(); // java.util包
        canlendar.add(Calendar.DATE, 0); // 日期减 如果不够减会将月变动        
		Date date1 = canlendar.getTime();
        SimpleDateFormat sdfd = new SimpleDateFormat("yyyyMMdd");
        String newDate=sdfd.format(date1);         
        SimpleDateFormat sdf = new SimpleDateFormat("E");
        String newWeek=sdf.format(date1);        
        Integer number=Integer.parseInt(paramsMap.get("number")+"")-1;
        int surplusDay=0;//这一周剩余几天
        if(newWeek.equals("星期一")){
			number=number*7;
		}else if(newWeek.equals("星期二")){
			surplusDay=1;
			number=(1)+7*number;
		}else if(newWeek.equals("星期三")){
			surplusDay=2;
			number=(2)+7*number;
		}else if(newWeek.equals("星期四")){
			surplusDay=3;
			number=(3)+7*number;
		}else if(newWeek.equals("星期五")){
			surplusDay=4;
			number=(4)+7*number;
		}else if(newWeek.equals("星期六")){
			surplusDay=5;
			number=(5)+7*number;
		}else if(newWeek.equals("星期日")){
			surplusDay=6;
			number=(6)+7*number;
		}
        BigDecimal sumSale = new BigDecimal(0);
//        double sumSale=0; 
        canlendar.add(Calendar.DATE, -number);
		Date date2 = canlendar.getTime();
		String oldDate=sdfd.format(date2); 
		List<Map<String, Object>> retuList=new ArrayList<Map<String,Object>>();
		List dateList=DateUtil.getListBetweenStartingAndEnding(oldDate,newDate ); 
		int wb=1;
		for (int i = 1; i <= dateList.size(); i++) {
			if(i%7==1){
				paramsMap.put("start_date", dateList.get(i-1)); //开始日期
			}
			if(i%7==0||dateList.get(i-1).equals(newDate)){	
			    paramsMap.put("end_date", dateList.get(i-1));//结束日期			   
			    List<Map<String, Object>>  result=orderDao.searchSaleroom(paramsMap);//dao中的数据			    
			    for (Map<String, Object> map : result) {
//			    	BigDecimal saleroom = new BigDecimal(map.get("saleroom")+"");
			    	sumSale=sumSale.add(new BigDecimal(map.get("saleroom")+""));			    	
				}
			    Map<String, Object> wbMap=new HashMap<String, Object>();
			    wbMap.put("time_interval","第"+wb+"周" );
			    wbMap.put("saleroom", sumSale);
			    retuList.add(wbMap);
			    wb++;
			    sumSale=new BigDecimal(0);
			}
		}
		return retuList;
	}
	//销售单  年报表
	@Override
	public List<Map<String, Object>> searchSaleroomAnnual(Map<String, Object> paramsMap) throws Exception {
		Date date  = new Date();		
		String newDate =(new SimpleDateFormat("yyyyMMdd")).format(date ) ;		
		Calendar cal  = Calendar.getInstance();
		cal .setTime(date );
		Integer number = Integer.parseInt(paramsMap.get("number")+"");
		cal .add(Calendar.YEAR, -number+ 1);
		String oldDate = (new SimpleDateFormat("yyyy")).format(cal .getTime());		
		oldDate+="0101";
		paramsMap.put("start_date", oldDate);
	    paramsMap.put("end_date", newDate);
	    List<Map<String, Object>> returnList =orderDao.searchSaleroom(paramsMap);//在dao中接受的list	
	    List yearList=DateUtil.getListBetweenStartingAndEnding(oldDate.substring(0,4),newDate.substring(0,4) );
	    for (int j = 0; j < yearList.size(); j++) {
			if(!returnList.get(j).get("time_interval").equals(yearList.get(j))||j>=returnList.size()){
				Map<String, Object> nullMap=new HashMap<String, Object>();
	    		nullMap.put("time_interval", yearList.get(j));
	    		nullMap.put("saleroom", "0");
	    		returnList.add(j,nullMap);
			}
		}	    	   
	    return returnList;
	}
	//库存报表
	public List<Map<String, Object>> stockReport(Map<String, Object> stockMap)throws Exception{
		LOG.debug("StatisticsServiceImpl stockReport stockMap:"+stockMap);
		Map<String, Object> merchHistoryMap = new HashMap<String, Object>();
		//卷烟和非卷烟利润
//		Map<String, Object> cigaProfitMap=new HashMap<String,Object>();
		Map<String, Map<String, Object>> itemHistoryMap = new HashMap<String, Map<String, Object>>();
		String merchId = (String) stockMap.get("merch_id");
		String settlementDate = DateUtil.getToday();
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", merchId);
		merchItemParam.put("page_index", -1);
		merchItemParam.put("page_size", -1);
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(merchItemParam);
		String itemHistoryIdentity=null;
		Map<String, Object> itemHistory = null;
		for(Map<String, Object> merchItemMap : merchItemList) {
			itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			itemHistory = new HashMap<String, Object>();
			itemHistory.put("settlement_id", IDUtil.getId());
			itemHistory.put("settlement_date", settlementDate);
			itemHistory.put("merch_id", merchId);
			itemHistory.put("item_id", merchItemMap.get("item_id"));
			itemHistory.put("item_bar", merchItemMap.get("item_bar"));
			itemHistory.put("item_name", merchItemMap.get("item_name"));
			itemHistory.put("item_kind_id", merchItemMap.get("item_kind_id"));
			itemHistory.put("unit_name", merchItemMap.get("unit_name"));
			itemHistory.put("cost", merchItemMap.get("cost"));
			itemHistory.put("pri1", merchItemMap.get("pri1"));
			itemHistory.put("pri2", merchItemMap.get("pri2"));
			itemHistory.put("pri4", merchItemMap.get("pri4"));
			itemHistory.put("discount", merchItemMap.get("discount"));
			itemHistoryMap.put(itemHistoryIdentity, itemHistory);
		}
		this.whseDailySettlement(stockMap.get("merch_id")+"", settlementDate, merchHistoryMap, itemHistoryMap);
		List<Map<String, Object>> itemKindList=itemDao.selectItemKind(stockMap);//分类
		Collection<Map<String, Object>> itemWhseCollection=itemHistoryMap.values();
		List<Map<String, Object>> stockList=new ArrayList<Map<String,Object>>();
		Map<String, Object> itemKind = null;
		BigDecimal sumQtyWhse=BigDecimal.ZERO;
		BigDecimal sumMoney=BigDecimal.ZERO;
		for (Map<String, Object> kindMap : itemKindList) {//分类
			itemKind =new HashMap<String, Object>();
			sumQtyWhse=BigDecimal.ZERO;
			sumMoney=BigDecimal.ZERO;
			for (Map<String, Object> itemWhse : itemWhseCollection) {
				 if(
						 (itemWhse.get("item_kind_id")==null && kindMap.get("item_kind_id").equals("99"))
						 ||(itemWhse.get("item_kind_id")!=null && itemWhse.get("item_kind_id").equals(kindMap.get("item_kind_id")))
						 ||(kindMap.get("item_kind_id").equals("99") && itemWhse.get("isok")==null)) 
				 {
					itemWhse.put("isok", true);
					if ( itemWhse.get("whse_quantity")!=null ) {
						sumQtyWhse=sumQtyWhse.add(MapUtil.getBigDecimal(itemWhse, "whse_quantity"));
					}
					if (itemWhse.get("whse_amount")!=null) {
						sumMoney=sumMoney.add(MapUtil.getBigDecimal(itemWhse, "whse_amount"));
					}
					continue;
				}
			}
			itemKind.put("item_kind_id", kindMap.get("item_kind_id"));
			itemKind.put("item_kind_name", kindMap.get("item_kind_name"));
			itemKind.put("sum_qty_whse", sumQtyWhse);
			itemKind.put("sum_money", sumMoney);
			stockList.add(itemKind);
		}
		return stockList;
	}
	
	public Map<String,Object> stockClassificationCombined(Map<String,Object> stockMap)throws Exception{
		LOG.debug("StatisticsServiceImpl stockClassificationCombined stockMap:"+stockMap);
		
		List<Map<String, Object>> whseMerchList = whseDao.selectWhseMerch(stockMap);//itemDao.selectMerchItem(stockMap);//获取用火所有商品库存信息
		Map<String,Object> stockItemMap=new HashMap<String,Object>();
		for(Map<String, Object> map:whseMerchList){
			stockItemMap.put(map.get("item_id").toString(), map.get("qty_whse"));
		}
		
		BigDecimal sumqtywhse=BigDecimal.ZERO;
		BigDecimal summoney=BigDecimal.ZERO;
		
		List<Map<String, Object>> itemMerchList = itemDao.selectMerchItem(stockMap);
		
		for(Map<String, Object> map:itemMerchList){
			
			BigDecimal qty = MapUtil.getBigDecimal(stockItemMap, MapUtil.getString(map, "item_id",""), BigDecimal.ZERO);
			BigDecimal cost = MapUtil.getBigDecimal(map,"cost",BigDecimal.ZERO);
			summoney = summoney.add(qty.multiply(cost));
			sumqtywhse = sumqtywhse.add(qty);
			
		}
		
		Map<String,Object> stockWhseMap=new HashMap<String,Object>();
		stockWhseMap.put("sumqtywhse", sumqtywhse);
		stockWhseMap.put("summoney", summoney);
		return stockWhseMap;
	}
	
	/**
	 * 日结：计算销售
	 * 日结
	 * @param merchId
	 * @param date
	 * @param merchHistoryMap
	 * @param itemHistoryMap
	 * @throws Exception
	 */
	private void saleDailySettlement(String merchId, String date, Map<String, Object> merchHistoryMap, Map<String, Map<String, Object>> itemHistoryMap) throws Exception {
		BigDecimal profitAmount = BigDecimal.ZERO; // 利润总额
		BigDecimal saleAmount = BigDecimal.ZERO; // 销售总额
		BigDecimal saleLoss = BigDecimal.ZERO; // 销售抹零总额
		
		int saleCount = 0; // 销售总笔数
		BigDecimal profitAmountOnline = BigDecimal.ZERO; // 网络销售总额
		BigDecimal saleAmountOnline = BigDecimal.ZERO; // 网络销售总额
		BigDecimal saleLossOnline = BigDecimal.ZERO; // 网络销售抹零总额
		int saleCountOnline = 0; // 网络销售笔数
		BigDecimal profitAmountOffline = BigDecimal.ZERO; // 线下销售总额
		BigDecimal saleAmountOffline = BigDecimal.ZERO; // 线下销售总额
		BigDecimal saleLossOffline = BigDecimal.ZERO; // 线下销售抹零总额
		int saleCountOffline = 0; // 线下销售笔数
		StringBuffer saleOrderIdBuffer = new StringBuffer();
		StringBuffer onlineSaleOrderIdBuffer = new StringBuffer();
		StringBuffer offlineSaleOrderIdBuffer = new StringBuffer();
		// 参数
		Map<String, Object> saleOrderParam = new HashMap<String, Object>();
		saleOrderParam.put("merch_id", merchId);
		saleOrderParam.put("order_date", date);
		saleOrderParam.put("page_index", -1);
		saleOrderParam.put("page_size", -1);
		// 因为要区分各种销售种类的order_id, 所以取出明细用逻辑加起来, 不用group by
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(saleOrderParam);
		List<String> onlineOrderIdList = new ArrayList<String>();
		List<String> offlineOrderIdList = new ArrayList<String>();
		List<String> orderIdList = new ArrayList<String>();
		int orderCount = 0;
		for(Map<String, Object> saleOrderMap : saleOrderList) {
			// 把此商户下的order_id按在线和线下销售存储
			String orderId = (String) saleOrderMap.get("order_id");
			if("03".equals(saleOrderMap.get("status"))) { // 完成状态的才会计算利润/销售
				if("04".equals(saleOrderMap.get("order_type"))) { // 楼下店
					if(onlineSaleOrderIdBuffer.length()==0) {
						onlineSaleOrderIdBuffer.append(orderId);
					} else {
						onlineSaleOrderIdBuffer.append(","+orderId);
					}
					profitAmountOnline = profitAmountOnline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					saleAmountOnline = saleAmountOnline.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					saleLossOnline = saleLossOnline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					saleCountOnline++;
					if(saleOrderIdBuffer.length()==0) {
						saleOrderIdBuffer.append(orderId);
					} else {
						saleOrderIdBuffer.append(","+orderId);
					}
					// 计算商户利润总额
					profitAmount = profitAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					// 商户销售总额
					saleAmount = saleAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					// 销售抹零总额
					saleLoss = saleLoss.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					// 商户销售笔数
					saleCount++;
				} else { //柜台销售
					if(offlineSaleOrderIdBuffer.length()==0) {
						offlineSaleOrderIdBuffer.append(orderId);
					} else {
						offlineSaleOrderIdBuffer.append(","+orderId);
					}
					profitAmountOffline = profitAmountOffline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					//应收金额 = 应收金额-抹零金额
					saleAmountOffline = saleAmountOffline.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					//抹零金额
					saleLossOffline = saleLossOffline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					saleCountOffline++;
					if(saleOrderIdBuffer.length()==0) {
						saleOrderIdBuffer.append(orderId);
					} else {
						saleOrderIdBuffer.append(","+orderId);
					}
					// 计算商户利润总额
					profitAmount = profitAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					// 商户销售总额
//					saleAmount = saleAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total")).subtract(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					saleAmount = saleAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					// 销售抹零总额
					saleLoss = saleLoss.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					// 商户销售笔数
					saleCount++;
				}
				if(++orderCount%10000==0) { // 赋值和重置
					onlineOrderIdList.add(onlineSaleOrderIdBuffer.toString());
					onlineSaleOrderIdBuffer = new StringBuffer();
					offlineOrderIdList.add(offlineSaleOrderIdBuffer.toString());
					offlineSaleOrderIdBuffer = new StringBuffer();
					orderIdList.add(saleOrderIdBuffer.toString());
					saleOrderIdBuffer = new StringBuffer();
				}
			}
		}
		onlineOrderIdList.add(onlineSaleOrderIdBuffer.toString());
		offlineOrderIdList.add(offlineSaleOrderIdBuffer.toString());
		orderIdList.add(saleOrderIdBuffer.toString());
		
		// 赋值
		merchHistoryMap.put("profit_amount", profitAmount);
		merchHistoryMap.put("sale_amount", saleAmount);
		merchHistoryMap.put("sale_loss", saleLoss);
		merchHistoryMap.put("sale_count", saleCount);
		merchHistoryMap.put("profit_amount_online", profitAmountOnline);
		merchHistoryMap.put("sale_amount_online", saleAmountOnline);
		merchHistoryMap.put("sale_loss_online", saleLossOnline);
		merchHistoryMap.put("sale_count_online", saleCountOnline);
		merchHistoryMap.put("profit_amount_offline", profitAmountOffline);
		merchHistoryMap.put("sale_amount_offline", saleAmountOffline);
		merchHistoryMap.put("sale_loss_offline", saleLossOffline);
		merchHistoryMap.put("sale_count_offline", saleCountOffline);
		
		// = * = * = * = * = 销售单行 = * = * = * = * = 
		// 参数
		List<Map<String, Object>> saleOrderLineList = new ArrayList<Map<String, Object>>();
		int orderSize = orderIdList.size();
		for(int orderIndex=0; orderIndex<orderSize; orderIndex++) {
			String onlineOrderId = onlineOrderIdList.get(orderIndex);
			String offlineOrderId = offlineOrderIdList.get(orderIndex);
			String orderId = orderIdList.get(orderIndex);
			Map<String, Object> saleOrderLineParam = new HashMap<String, Object>();
			saleOrderLineParam.put("order_id", orderId);
			saleOrderLineParam.put("online_order_id", onlineOrderId);
			saleOrderLineParam.put("offline_order_id", offlineOrderId);
			saleOrderLineList.addAll(orderDao.searchSaleOrderLineByItemId(saleOrderLineParam));
		}
//		Map<String, Object> saleOrderLineParam = new HashMap<String, Object>();
//		saleOrderLineParam.put("order_id", saleOrderIdBuffer.toString());
//		saleOrderLineParam.put("online_order_id", onlineSaleOrderIdBuffer.toString());
//		saleOrderLineParam.put("offline_order_id", offlineSaleOrderIdBuffer.toString());
//		List<Map<String, Object>> saleOrderLineList = orderDao.searchSaleOrderLineByItemId(saleOrderLineParam);
		for(Map<String, Object> saleOrderLineMap : saleOrderLineList) {
			String itemHistoryIdentity = merchId+","+saleOrderLineMap.get("item_id"); // merch_id+item_id作为map的key
//			Map<String, Object> itemHistory = new HashMap<String, Object>();
//			itemHistory.put("profit_amount", saleOrderLineMap.get("profit_amount"));
//			itemHistory.put("sale_amount", saleOrderLineMap.get("sale_amount"));
//			itemHistory.put("sale_quantity", saleOrderLineMap.get("sale_quantity"));
			BigDecimal itemProfitAmount = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount");
			BigDecimal itemSaleAmount = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount");
			BigDecimal itemSaleQuantity = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity");
			BigDecimal itemAdjustedAmount = MapUtil.getBigDecimal(saleOrderLineMap, "adjusted_amount");
			BigDecimal otherAdjustedAmount = MapUtil.getBigDecimal(saleOrderLineMap, "other_adjusted_amount");
			BigDecimal itemProfitAmountOnline = BigDecimal.ZERO;
			BigDecimal itemSaleAmountOnline = BigDecimal.ZERO;
			BigDecimal itemSaleQuantityOnline = BigDecimal.ZERO;
			BigDecimal itemProfitAmountOffline = BigDecimal.ZERO;
			BigDecimal itemSaleAmountOffline = BigDecimal.ZERO;
			BigDecimal itemSaleQuantityOffline = BigDecimal.ZERO;
			if(onlineSaleOrderIdBuffer.length()==0) {
				itemProfitAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_offline");
				itemSaleAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_offline");
				itemSaleQuantityOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_offline");
			} else if(offlineSaleOrderIdBuffer.length()==0) {
				itemProfitAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_online");
				itemSaleAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_online");
				itemSaleQuantityOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_online");
			} else {
				itemProfitAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_online");
				itemSaleAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_online");
				itemSaleQuantityOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_online");
				itemProfitAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_offline");
				itemSaleAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_offline");
				itemSaleQuantityOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_offline");
			}
			// 梁凯 2014年6月4日17:20:44 为了不在每次调用MapUtil的时候new HashMap, 用if分支来判断
			Map<String, Object> tempItemHistory = MapUtil.get(itemHistoryMap, itemHistoryIdentity, null);
			if(tempItemHistory==null) {
				tempItemHistory = new HashMap<String, Object>();
				tempItemHistory.put("settlement_id", IDUtil.getId());
				tempItemHistory.put("settlement_date", date);
				tempItemHistory.put("merch_id", merchId);
				tempItemHistory.put("item_id", saleOrderLineMap.get("item_id"));
			}
			tempItemHistory.put("adjusted_amount", itemAdjustedAmount);
			tempItemHistory.put("other_adjusted_amount", otherAdjustedAmount);
			tempItemHistory.put("profit_amount", itemProfitAmount);
			tempItemHistory.put("sale_amount", itemSaleAmount);
			tempItemHistory.put("sale_quantity", itemSaleQuantity);
			tempItemHistory.put("profit_amount_online", itemProfitAmountOnline);
			tempItemHistory.put("sale_amount_online", itemSaleAmountOnline);
			tempItemHistory.put("sale_quantity_online",itemSaleQuantityOnline);
			tempItemHistory.put("profit_amount_offline", itemProfitAmountOffline);
			tempItemHistory.put("sale_amount_offline", itemSaleAmountOffline);
			tempItemHistory.put("sale_quantity_offline", itemSaleQuantityOffline);
//			if(itemHistoryMap.containsKey(itemHistoryIdentity)) {
//				((Map<String, Object>)itemHistoryMap.get(itemHistoryIdentity)).putAll(itemHistory);
//			} else {
//				tempItemHistory = new HashMap<String, Object>();
//				tempItemHistory.put("settlement_id", IDUtil.getId());
//				tempItemHistory.put("settlement_date", date);
//				tempItemHistory.put("merch_id", merchId);
//				tempItemHistory.put("item_id", saleOrderLineMap.get("item_id"));
//				itemHistoryMap.put(itemHistoryIdentity, tempItemHistory);
//			}
			itemHistoryMap.put(itemHistoryIdentity, tempItemHistory);
		}
		//添加默认值	梁凯		2014年5月30日18:14:26
//		Collection<Map<String, Object>> merchItemList=itemHistoryMap.values();
//		for(Map<String, Object> merchItemMap : merchItemList) {
//			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
//			if(itemHistoryMap.get(itemHistoryIdentity).get("sale_quantity")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("sale_quantity", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("sale_amount")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("sale_amount", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("profit_amount")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("profit_amount", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("sale_quantity_offline")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("sale_quantity_offline", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("sale_quantity_online")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("sale_quantity_online", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("sale_amount_offline")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("sale_amount_offline", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("sale_amount_online")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("sale_amount_online", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("profit_amount_offline")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("profit_amount_offline", BigDecimal.ZERO);
//			}
//			if(itemHistoryMap.get(itemHistoryIdentity).get("profit_amount_online")==null){
//				itemHistoryMap.get(itemHistoryIdentity).put("profit_amount_online", BigDecimal.ZERO);
//			}
//		}
	}
	/**
	 * saleDailySettlement方法重载
	 * 应用与在销售流水中，打印今日日结
	 * @author lizhxi1995
	 * String merchId, String date,
	 * @param merchHistoryMap
	 * @param itemHistoryMap
	 * @throws Exception
	 */
	private void saleDailySettlement(Map<String, Object> userMap, Map<String, Object> merchHistoryMap, Map<String, Map<String, Object>> itemHistoryMap) throws Exception {
		BigDecimal profitAmount = BigDecimal.ZERO; // 利润总额
		BigDecimal saleAmount = BigDecimal.ZERO; // 销售总额
		BigDecimal saleLoss = BigDecimal.ZERO; // 销售抹零总额
		String merchId = MapUtil.getString(userMap, "merch_id");
		String orderDate = MapUtil.getString(userMap, "order_date");
		int saleCount = 0; // 销售总笔数
		BigDecimal profitAmountOnline = BigDecimal.ZERO; // 网络销售总额
		BigDecimal saleAmountOnline = BigDecimal.ZERO; // 网络销售总额
		BigDecimal saleLossOnline = BigDecimal.ZERO; // 网络销售抹零总额
		int saleCountOnline = 0; // 网络销售笔数
		BigDecimal profitAmountOffline = BigDecimal.ZERO; // 线下销售总额
		BigDecimal saleAmountOffline = BigDecimal.ZERO; // 线下销售总额
		BigDecimal saleLossOffline = BigDecimal.ZERO; // 线下销售抹零总额
		int saleCountOffline = 0; // 线下销售笔数
		StringBuffer saleOrderIdBuffer = new StringBuffer();
		StringBuffer onlineSaleOrderIdBuffer = new StringBuffer();
		StringBuffer offlineSaleOrderIdBuffer = new StringBuffer();
		// 参数
		Map<String, Object> saleOrderParam = new HashMap<String, Object>();
		saleOrderParam.put("merch_id", merchId );
		saleOrderParam.put("order_date", orderDate);
		saleOrderParam.put("role_id", MapUtil.getString(userMap, "role_id"));
		saleOrderParam.put("role_name", MapUtil.getString(userMap, "role_name"));
		saleOrderParam.put("user_code", MapUtil.getString(userMap, "user_code"));
		saleOrderParam.put("page_index", -1);
		saleOrderParam.put("page_size", -1);
		// 因为要区分各种销售种类的order_id, 所以取出明细用逻辑加起来, 不用group by
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(saleOrderParam);
		List<String> onlineOrderIdList = new ArrayList<String>();
		List<String> offlineOrderIdList = new ArrayList<String>();
		List<String> orderIdList = new ArrayList<String>();
		int orderCount = 0;
		for(Map<String, Object> saleOrderMap : saleOrderList) {
			// 把此商户下的order_id按在线和线下销售存储
			String orderId = (String) saleOrderMap.get("order_id");
			if("03".equals(saleOrderMap.get("status"))) { // 完成状态的才会计算利润/销售
				if("04".equals(saleOrderMap.get("order_type"))) { // 楼下店
					if(onlineSaleOrderIdBuffer.length()==0) {
						onlineSaleOrderIdBuffer.append(orderId);
					} else {
						onlineSaleOrderIdBuffer.append(","+orderId);
					}
					profitAmountOnline = profitAmountOnline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					saleAmountOnline = saleAmountOnline.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					saleLossOnline = saleLossOnline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					saleCountOnline++;
					if(saleOrderIdBuffer.length()==0) {
						saleOrderIdBuffer.append(orderId);
					} else {
						saleOrderIdBuffer.append(","+orderId);
					}
					// 计算商户利润总额
					profitAmount = profitAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					
					// 商户销售总额
					saleAmount = saleAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					// 销售抹零总额
					saleLoss = saleLoss.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					// 商户销售笔数
					saleCount++;
				} else {
					if(offlineSaleOrderIdBuffer.length()==0) {
						offlineSaleOrderIdBuffer.append(orderId);
					} else {
						offlineSaleOrderIdBuffer.append(","+orderId);
					}
					profitAmountOffline = profitAmountOffline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					saleAmountOffline = saleAmountOffline.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					saleLossOffline = saleLossOffline.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					saleCountOffline++;
					if(saleOrderIdBuffer.length()==0) {
						saleOrderIdBuffer.append(orderId);
					} else {
						saleOrderIdBuffer.append(","+orderId);
					}
					// 计算商户利润总额
					profitAmount = profitAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_profit"));
					// 商户销售总额
					saleAmount = saleAmount.add(MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total"));
					// 销售抹零总额
					saleLoss = saleLoss.add(MapUtil.getBigDecimal(saleOrderMap, "amt_ord_loss"));
					// 商户销售笔数
					saleCount++;
				}
				if(++orderCount%10000==0) { // 赋值和重置
					onlineOrderIdList.add(onlineSaleOrderIdBuffer.toString());
					onlineSaleOrderIdBuffer = new StringBuffer();
					offlineOrderIdList.add(offlineSaleOrderIdBuffer.toString());
					offlineSaleOrderIdBuffer = new StringBuffer();
					orderIdList.add(saleOrderIdBuffer.toString());
					saleOrderIdBuffer = new StringBuffer();
				}
			}
		}
		onlineOrderIdList.add(onlineSaleOrderIdBuffer.toString());
		offlineOrderIdList.add(offlineSaleOrderIdBuffer.toString());
		orderIdList.add(saleOrderIdBuffer.toString());
		
		// 赋值
		merchHistoryMap.put("profit_amount", profitAmount);
		merchHistoryMap.put("sale_amount", saleAmount);
		merchHistoryMap.put("sale_loss", saleLoss);
		merchHistoryMap.put("sale_count", saleCount);
		merchHistoryMap.put("profit_amount_online", profitAmountOnline);
		merchHistoryMap.put("sale_amount_online", saleAmountOnline);
		merchHistoryMap.put("sale_loss_online", saleLossOnline);
		merchHistoryMap.put("sale_count_online", saleCountOnline);
		merchHistoryMap.put("profit_amount_offline", profitAmountOffline);
		merchHistoryMap.put("sale_amount_offline", saleAmountOffline);
		merchHistoryMap.put("sale_loss_offline", saleLossOffline);
		merchHistoryMap.put("sale_count_offline", saleCountOffline);

		// = * = * = * = * = 销售单行 = * = * = * = * = 
		// 参数
		List<Map<String, Object>> saleOrderLineList = new ArrayList<Map<String, Object>>();
		int orderSize = orderIdList.size();
		for(int orderIndex=0; orderIndex<orderSize; orderIndex++) {
			String onlineOrderId = onlineOrderIdList.get(orderIndex);
			String offlineOrderId = offlineOrderIdList.get(orderIndex);
			String orderId = orderIdList.get(orderIndex);
			Map<String, Object> saleOrderLineParam = new HashMap<String, Object>();
			saleOrderLineParam.put("order_id", orderId);
			saleOrderLineParam.put("online_order_id", onlineOrderId);
			saleOrderLineParam.put("offline_order_id", offlineOrderId);
			saleOrderLineList.addAll(orderDao.searchSaleOrderLineByItemId(saleOrderLineParam));
		}
		for(Map<String, Object> saleOrderLineMap : saleOrderLineList) {
			String itemHistoryIdentity = merchId+","+saleOrderLineMap.get("item_id"); // merch_id+item_id作为map的key
			BigDecimal itemProfitAmount = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount");
			BigDecimal itemSaleAmount = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount");
			BigDecimal itemSaleQuantity = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity");
			BigDecimal itemAdjustedAmount = MapUtil.getBigDecimal(saleOrderLineMap, "adjusted_amount");
			BigDecimal itemOtherAdjustedAmount = MapUtil.getBigDecimal(saleOrderLineMap, "other_adjusted_amount");
			BigDecimal itemProfitAmountOnline = BigDecimal.ZERO;
			BigDecimal itemSaleAmountOnline = BigDecimal.ZERO;
			BigDecimal itemSaleQuantityOnline = BigDecimal.ZERO;
			BigDecimal itemProfitAmountOffline = BigDecimal.ZERO;
			BigDecimal itemSaleAmountOffline = BigDecimal.ZERO;
			BigDecimal itemSaleQuantityOffline = BigDecimal.ZERO;
			if(onlineSaleOrderIdBuffer.length()==0) {
				itemProfitAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_offline");
				itemSaleAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_offline");
				itemSaleQuantityOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_offline");
			} else if(offlineSaleOrderIdBuffer.length()==0) {
				itemProfitAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_online");
				itemSaleAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_online");
				itemSaleQuantityOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_online");
			} else {
				itemProfitAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_online");
				itemSaleAmountOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_online");
				itemSaleQuantityOnline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_online");
				itemProfitAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "profit_amount_offline");
				itemSaleAmountOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_amount_offline");
				itemSaleQuantityOffline = MapUtil.getBigDecimal(saleOrderLineMap, "sale_quantity_offline");
			}
			// 梁凯 2014年6月4日17:20:44 为了不在每次调用MapUtil的时候new HashMap, 用if分支来判断
			Map<String, Object> tempItemHistory = MapUtil.get(itemHistoryMap, itemHistoryIdentity, null);
			if(tempItemHistory==null) {
				tempItemHistory = new HashMap<String, Object>();
				tempItemHistory.put("settlement_id", IDUtil.getId());
				tempItemHistory.put("settlement_date", orderDate);
				tempItemHistory.put("merch_id", merchId);
				tempItemHistory.put("item_id", saleOrderLineMap.get("item_id"));
			}
			tempItemHistory.put("profit_amount", MapUtil.getBigDecimal(tempItemHistory, "profit_amount").add(itemProfitAmount));
			tempItemHistory.put("sale_amount", MapUtil.getBigDecimal(tempItemHistory, "sale_amount").add(itemSaleAmount));
			tempItemHistory.put("sale_quantity", MapUtil.getBigDecimal(tempItemHistory, "sale_quantity").add(itemSaleQuantity));
			tempItemHistory.put("profit_amount_online", MapUtil.getBigDecimal(tempItemHistory, "profit_amount_online").add(itemProfitAmountOnline));
			tempItemHistory.put("sale_amount_online", MapUtil.getBigDecimal(tempItemHistory, "sale_amount_online").add(itemSaleAmountOnline));
			tempItemHistory.put("sale_quantity_online", MapUtil.getBigDecimal(tempItemHistory, "sale_quantity_online").add(itemSaleQuantityOnline));
			tempItemHistory.put("profit_amount_offline", MapUtil.getBigDecimal(tempItemHistory, "profit_amount_offline").add(itemProfitAmountOffline));
			tempItemHistory.put("sale_amount_offline", MapUtil.getBigDecimal(tempItemHistory, "sale_amount_offline").add(itemSaleAmountOffline));
			tempItemHistory.put("sale_quantity_offline", MapUtil.getBigDecimal(tempItemHistory, "sale_quantity_offline").add(itemSaleQuantityOffline));
			
			tempItemHistory.put("sale_adjusted_amount", itemAdjustedAmount);
			tempItemHistory.put("sale_other_adjusted_amount", itemOtherAdjustedAmount);
			
			itemHistoryMap.put(itemHistoryIdentity, tempItemHistory);
		}
	}
	
	/**
	 * 日结：计算退货
	 * @param merchId
	 * @param date
	 * @param merchHistoryMap
	 * @param itemHistoryMap
	 * @throws Exception
	 */
	private void returnDailySettlement(String merchId, String date, Map<String, Object> merchHistoryMap, Map<String, Map<String, Object>> itemHistoryMap) throws Exception {
		BigDecimal lossAmount = BigDecimal.ZERO; // 退货损失利润总额
		BigDecimal lossAmountOnline = BigDecimal.ZERO; // 网络退货损失利润
		BigDecimal lossAmountOffline = BigDecimal.ZERO; // 线下退货损失利润
		BigDecimal returnAmount = BigDecimal.ZERO; // 退货总额
		BigDecimal returnAmountOnline = BigDecimal.ZERO; // 网络退货总额
		BigDecimal returnAmountOffline = BigDecimal.ZERO; // 线下退货总额
		StringBuffer returnOrderIdBuffer = new StringBuffer();
		StringBuffer onlineReturnOrderIdBuffer = new StringBuffer();
		StringBuffer offlineReturnOrderIdBuffer = new StringBuffer();
		// 参数
		Map<String, Object> returnOrderParam = new HashMap<String, Object>();
		returnOrderParam.put("merch_id", merchId);
		returnOrderParam.put("return_order_date", date);
		returnOrderParam.put("page_index", -1);
		returnOrderParam.put("page_size", -1);
		// 因为要区分各种销售种类的order_id, 所以取出明细用逻辑加起来, 不用group by
		List<Map<String, Object>> returnOrderList = orderDao.selectReturnOrder(returnOrderParam);
		for(Map<String, Object> returnOrderMap : returnOrderList) {
			String returnOrderId = (String) returnOrderMap.get("return_order_id");
			if("04".equals(returnOrderMap.get("order_type"))) { // 楼下店
				if(onlineReturnOrderIdBuffer.length()==0) {
					onlineReturnOrderIdBuffer.append(returnOrderId);
				} else {
					onlineReturnOrderIdBuffer.append(","+returnOrderId);
				}
				lossAmountOnline = lossAmountOnline.add(MapUtil.getBigDecimal(returnOrderMap, "amt_return_loss"));
				returnAmountOnline = returnAmountOnline.add(MapUtil.getBigDecimal(returnOrderMap, "amt_return_total"));
			} else {
				if(offlineReturnOrderIdBuffer.length()==0) {
					offlineReturnOrderIdBuffer.append(returnOrderId);
				} else {
					offlineReturnOrderIdBuffer.append(","+returnOrderId);
				}
				lossAmountOffline = lossAmountOffline.add(MapUtil.getBigDecimal(returnOrderMap, "amt_return_loss"));
				returnAmountOffline = returnAmountOffline.add(MapUtil.getBigDecimal(returnOrderMap, "amt_return_total"));
			}
			if(returnOrderIdBuffer.length()==0) {
				returnOrderIdBuffer.append(returnOrderId);
			} else {
				returnOrderIdBuffer.append(","+returnOrderId);
			}
			lossAmount = lossAmount.add(MapUtil.getBigDecimal(returnOrderMap, "amt_return_loss"));
			returnAmount = returnAmount.add(MapUtil.getBigDecimal(returnOrderMap, "amt_return_total"));
		}
		
		// 赋值
		merchHistoryMap.put("loss_amount", lossAmount);
		merchHistoryMap.put("loss_amount_online", lossAmountOnline);
		merchHistoryMap.put("loss_amount_offline", lossAmountOffline);
		merchHistoryMap.put("return_amount", returnAmount);
		merchHistoryMap.put("return_amount_online", returnAmountOnline);
		merchHistoryMap.put("return_amount_offline", returnAmountOffline);
		
		// = * = * = * = * = 退货单行 = * = * = * = * = 
		// 参数
		Map<String, Object> returnOrderLineParam = new HashMap<String, Object>();
		returnOrderLineParam.put("order_id", returnOrderIdBuffer.toString());
		returnOrderLineParam.put("online_order_id", onlineReturnOrderIdBuffer.toString());
		returnOrderLineParam.put("offline_order_id", offlineReturnOrderIdBuffer.toString());
		List<Map<String, Object>> returnOrderLineList = orderDao.searchReturnOrderLineByItemId(returnOrderLineParam);
		for(Map<String, Object> returnOrderLineMap : returnOrderLineList) {
			String itemHistoryIdentity = merchId+","+returnOrderLineMap.get("item_id"); // merch_id+item_id作为map的key
			Map<String, Object> itemHistory = new HashMap<String, Object>();
			itemHistory.put("loss_amount", returnOrderLineMap.get("profit_amount"));
			itemHistory.put("return_amount", returnOrderLineMap.get("sale_amount"));
			itemHistory.put("return_quantity", returnOrderLineMap.get("sale_quantity"));
			itemHistory.put("return_adjusted_amount", MapUtil.getBigDecimal(returnOrderLineMap, "adjusted_amount") );
//			itemHistory.put("return_other_adjusted_amount", MapUtil.getBigDecimal(returnOrderLineMap, "other_adjusted_amount") );
			
			if(onlineReturnOrderIdBuffer.length()==0) {
				itemHistory.put("loss_amount_online", BigDecimal.ZERO);
				itemHistory.put("return_amount_online", BigDecimal.ZERO);
				itemHistory.put("return_quantity_online", BigDecimal.ZERO);
				itemHistory.put("loss_amount_offline", returnOrderLineMap.get("profit_amount"));
				itemHistory.put("return_amount_offline", returnOrderLineMap.get("sale_amount"));
				itemHistory.put("return_quantity_offline", returnOrderLineMap.get("sale_quantity"));
			} else if(offlineReturnOrderIdBuffer.length()==0) {
				itemHistory.put("loss_amount_online", returnOrderLineMap.get("profit_amount"));
				itemHistory.put("return_amount_online", returnOrderLineMap.get("sale_amount"));
				itemHistory.put("return_quantity_online", returnOrderLineMap.get("sale_quantity"));
				itemHistory.put("loss_amount_offline", BigDecimal.ZERO);
				itemHistory.put("return_amount_offline", BigDecimal.ZERO);
				itemHistory.put("return_quantity_offline", BigDecimal.ZERO);
			} else {
				itemHistory.put("loss_amount_online", returnOrderLineMap.get("profit_amount_online"));
				itemHistory.put("return_amount_online", returnOrderLineMap.get("sale_amount_online"));
				itemHistory.put("return_quantity_online", returnOrderLineMap.get("sale_quantity_online"));
				itemHistory.put("loss_amount_offline", returnOrderLineMap.get("profit_amount_offline"));
				itemHistory.put("return_amount_offline", returnOrderLineMap.get("sale_amount_offline"));
				itemHistory.put("return_quantity_offline", returnOrderLineMap.get("sale_quantity_offline"));
			}
			if(itemHistoryMap.containsKey(itemHistoryIdentity)) {
				((Map<String, Object>)itemHistoryMap.get(itemHistoryIdentity)).putAll(itemHistory);
			} else {
				itemHistory.put("settlement_id", IDUtil.getId());
				itemHistory.put("settlement_date", date);
				itemHistory.put("merch_id", merchId);
				itemHistory.put("item_id", returnOrderLineMap.get("item_id"));
				itemHistoryMap.put(itemHistoryIdentity, itemHistory);
			}
		}
		//添加默认值
		Collection<Map<String, Object>> merchItemList=itemHistoryMap.values();
		for(Map<String, Object> merchItemMap : merchItemList) {
			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_amount_online")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_amount_online", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_amount_offline")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_amount_offline", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_quantity")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_quantity", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_amount")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_amount", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("loss_amount_offline")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("loss_amount_offline", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_quantity_offline")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_quantity_offline", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("loss_amount")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("loss_amount", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_quantity_online")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_quantity_online", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("loss_amount_online")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("loss_amount_online", BigDecimal.ZERO);
			}
		}
	}
	/**
	 * saleDailySettlement方法重载
	 * 应用与在销售流水中，打印今日日结
	 * @author lizhxi1995
	 * String merchId, String date,
	 * @param merchHistoryMap
	 * @param itemHistoryMap
	 * @throws Exception
	 */
	private void returnDailySettlement(Map<String, Object> userMap, Map<String, Object> merchHistoryMap, Map<String, Map<String, Object>> itemHistoryMap) throws Exception {
		BigDecimal lossAmount = BigDecimal.ZERO; // 退货损失利润总额
		BigDecimal lossAmountOnline = BigDecimal.ZERO; // 网络退货损失利润
		BigDecimal lossAmountOffline = BigDecimal.ZERO; // 线下退货损失利润
		BigDecimal returnAmount = BigDecimal.ZERO; // 退货总额
		BigDecimal returnAmountOnline = BigDecimal.ZERO; // 网络退货总额
		BigDecimal returnAmountOffline = BigDecimal.ZERO; // 线下退货总额
		StringBuffer returnOrderIdBuffer = new StringBuffer();
		StringBuffer onlineReturnOrderIdBuffer = new StringBuffer();
		StringBuffer offlineReturnOrderIdBuffer = new StringBuffer();
		String merchId = MapUtil.getString(userMap, "merch_id");
		String orderDate = MapUtil.getString(userMap, "order_date");
		// 参数
		Map<String, Object> returnOrderParam = new HashMap<String, Object>();
		returnOrderParam.put("merch_id", merchId);
		returnOrderParam.put("return_order_date", orderDate);
		returnOrderParam.put("role_id", MapUtil.getString(userMap, "role_id") );
		returnOrderParam.put("role_name", MapUtil.getString(userMap, "role_name"));
		returnOrderParam.put("user_code", MapUtil.getString(userMap, "user_code"));
		returnOrderParam.put("page_index", -1);
		returnOrderParam.put("page_size", -1);
		// 因为要区分各种销售种类的order_id, 所以取出明细用逻辑加起来, 不用group by
		List<Map<String, Object>> returnOrderList = orderDao.selectReturnOrder(returnOrderParam);
		for(Map<String, Object> returnOrderMap : returnOrderList) {
			String returnOrderId = (String) returnOrderMap.get("return_order_id");
			if("04".equals(returnOrderMap.get("order_type"))) { // 楼下店
				if(onlineReturnOrderIdBuffer.length()==0) {
					onlineReturnOrderIdBuffer.append(returnOrderId);
				} else {
					onlineReturnOrderIdBuffer.append(","+returnOrderId);
				}
				lossAmountOnline = lossAmountOnline.add(new BigDecimal(returnOrderMap.get("amt_return_loss").toString()));
				returnAmountOnline = returnAmountOnline.add(new BigDecimal(returnOrderMap.get("amt_return_total").toString()));
			} else {
				if(offlineReturnOrderIdBuffer.length()==0) {
					offlineReturnOrderIdBuffer.append(returnOrderId);
				} else {
					offlineReturnOrderIdBuffer.append(","+returnOrderId);
				}
				lossAmountOffline = lossAmountOffline.add(new BigDecimal(returnOrderMap.get("amt_return_loss").toString()));
				returnAmountOffline = returnAmountOffline.add(new BigDecimal(returnOrderMap.get("amt_return_total").toString()));
			}
			if(returnOrderIdBuffer.length()==0) {
				returnOrderIdBuffer.append(returnOrderId);
			} else {
				returnOrderIdBuffer.append(","+returnOrderId);
			}
			lossAmount = lossAmount.add(new BigDecimal(returnOrderMap.get("amt_return_loss").toString()));
			returnAmount = returnAmount.add(new BigDecimal(returnOrderMap.get("amt_return_total").toString()));
		}
		
		// 赋值
		merchHistoryMap.put("loss_amount", lossAmount);
		merchHistoryMap.put("loss_amount_online", lossAmountOnline);
		merchHistoryMap.put("loss_amount_offline", lossAmountOffline);
		merchHistoryMap.put("return_amount", returnAmount);
		merchHistoryMap.put("return_amount_online", returnAmountOnline);
		merchHistoryMap.put("return_amount_offline", returnAmountOffline);

		// = * = * = * = * = 退货单行 = * = * = * = * = 
		// 参数
		Map<String, Object> returnOrderLineParam = new HashMap<String, Object>();
		returnOrderLineParam.put("order_id", returnOrderIdBuffer.toString());
		returnOrderLineParam.put("online_order_id", onlineReturnOrderIdBuffer.toString());
		returnOrderLineParam.put("offline_order_id", offlineReturnOrderIdBuffer.toString());
		List<Map<String, Object>> returnOrderLineList = orderDao.searchReturnOrderLineByItemId(returnOrderLineParam);
		for(Map<String, Object> returnOrderLineMap : returnOrderLineList) {
			String itemHistoryIdentity = merchId+","+returnOrderLineMap.get("item_id"); // merch_id+item_id作为map的key
			Map<String, Object> itemHistory = new HashMap<String, Object>();
			itemHistory.put("loss_amount", returnOrderLineMap.get("profit_amount"));
			itemHistory.put("return_amount", returnOrderLineMap.get("sale_amount"));
			itemHistory.put("return_quantity", returnOrderLineMap.get("sale_quantity"));
			
			itemHistory.put("return_adjusted_amount", returnOrderLineMap.get("adjusted_amount"));
//			itemHistory.put("return_other_adjusted_amount", returnOrderLineMap.get("other_adjusted_amount"));
			
			if(onlineReturnOrderIdBuffer.length()==0) {
				itemHistory.put("loss_amount_online", BigDecimal.ZERO);
				itemHistory.put("return_amount_online", BigDecimal.ZERO);
				itemHistory.put("return_quantity_online", BigDecimal.ZERO);
				itemHistory.put("loss_amount_offline", returnOrderLineMap.get("profit_amount"));
				itemHistory.put("return_amount_offline", returnOrderLineMap.get("sale_amount"));
				itemHistory.put("return_quantity_offline", returnOrderLineMap.get("sale_quantity"));
			} else if(offlineReturnOrderIdBuffer.length()==0) {
				itemHistory.put("loss_amount_online", returnOrderLineMap.get("profit_amount"));
				itemHistory.put("return_amount_online", returnOrderLineMap.get("sale_amount"));
				itemHistory.put("return_quantity_online", returnOrderLineMap.get("sale_quantity"));
				itemHistory.put("loss_amount_offline", BigDecimal.ZERO);
				itemHistory.put("return_amount_offline", BigDecimal.ZERO);
				itemHistory.put("return_quantity_offline", BigDecimal.ZERO);
			} else {
				itemHistory.put("loss_amount_online", returnOrderLineMap.get("profit_amount_online"));
				itemHistory.put("return_amount_online", returnOrderLineMap.get("sale_amount_online"));
				itemHistory.put("return_quantity_online", returnOrderLineMap.get("sale_quantity_online"));
				itemHistory.put("loss_amount_offline", returnOrderLineMap.get("profit_amount_offline"));
				itemHistory.put("return_amount_offline", returnOrderLineMap.get("sale_amount_offline"));
				itemHistory.put("return_quantity_offline", returnOrderLineMap.get("sale_quantity_offline"));
			}
			if(itemHistoryMap.containsKey(itemHistoryIdentity)) {
				((Map<String, Object>)itemHistoryMap.get(itemHistoryIdentity)).putAll(itemHistory);
			} else {
				itemHistory.put("settlement_id", IDUtil.getId());
				itemHistory.put("settlement_date", orderDate);
				itemHistory.put("merch_id", merchId);
				itemHistory.put("item_id", returnOrderLineMap.get("item_id"));
				itemHistoryMap.put(itemHistoryIdentity, itemHistory);
			}
		}
		//添加默认值
		Collection<Map<String, Object>> merchItemList=itemHistoryMap.values();
		for(Map<String, Object> merchItemMap : merchItemList) {
			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_amount_online")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_amount_online", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_amount_offline")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_amount_offline", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_quantity")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_quantity", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_amount")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_amount", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("loss_amount_offline")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("loss_amount_offline", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_quantity_offline")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_quantity_offline", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("loss_amount")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("loss_amount", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("return_quantity_online")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("return_quantity_online", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("loss_amount_online")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("loss_amount_online", BigDecimal.ZERO);
			}
		}
	}
	
	/**
	 * 日结：计算进货
	 * @param merchId
	 * @param date
	 * @param merchHistoryMap
	 * @param itemHistoryMap
	 * @throws Exception
	 */
	private void purchDailySettlement(String merchId, String date, Map<String, Object> merchHistoryMap, Map<String, Map<String, Object>> itemHistoryMap) throws Exception {
		BigDecimal purchAmount = BigDecimal.ZERO; // 进货总额
		BigDecimal purchQuantity = BigDecimal.ZERO; // 进货总量
		Set<String> purchItemSet = new HashSet<String>(); // 进货商品种类
		int purchCount = 0; // 进货次数
		StringBuffer purchOrderIdBuffer = new StringBuffer();
		// 参数
		Map<String, Object> purchOrderParam = new HashMap<String, Object>();
		purchOrderParam.put("merch_id", merchId);
		purchOrderParam.put("voucher_date", date);
		purchOrderParam.put("page_index", -1);
		purchOrderParam.put("page_size", -1);
		// 因为要区分各种销售种类的order_id, 所以取出明细用逻辑加起来, 不用group by
		List<Map<String, Object>> purchOrderList = orderDao.selectPurchOrder(purchOrderParam);
		for(Map<String, Object> purchOrderMap : purchOrderList) {
			if(purchOrderIdBuffer.length()==0) {
				purchOrderIdBuffer.append(purchOrderMap.get("order_id"));
			} else {
				purchOrderIdBuffer.append(","+purchOrderMap.get("order_id"));
			}
			purchAmount = purchAmount.add(new BigDecimal(purchOrderMap.get("amt_purch_total").toString()));
			purchQuantity = purchQuantity.add(new BigDecimal(purchOrderMap.get("qty_purch_total").toString()));
			purchCount++;
		}

		// 赋值
		merchHistoryMap.put("purch_amount", purchAmount);
		merchHistoryMap.put("purch_quantity", purchQuantity);
		merchHistoryMap.put("purch_count", purchCount);

		// = * = * = * = * = 进货单行 = * = * = * = * = 
		Map<String, Object> purchOrderLineParam = new HashMap<String, Object>();
		purchOrderLineParam.put("order_id", purchOrderIdBuffer.toString());
		List<Map<String, Object>> purchOrderLineList = orderDao.searchPurchOrderLineByItem(purchOrderLineParam);
		for(Map<String, Object> purchOrderLineMap : purchOrderLineList) {
			purchItemSet.add(purchOrderLineMap.get("item_id").toString());
			String itemHistoryIdentity = merchId+","+purchOrderLineMap.get("item_id"); // merch_id+item_id作为map的key
			Map<String, Object> itemHistory = new HashMap<String, Object>();
			itemHistory.put("purch_amount", purchOrderLineMap.get("purch_amount"));
			itemHistory.put("purch_quantity", purchOrderLineMap.get("purch_quantity"));
			if(itemHistoryMap.containsKey(itemHistoryIdentity)) {
				((Map<String, Object>)itemHistoryMap.get(itemHistoryIdentity)).putAll(itemHistory);
			} else {
				itemHistory.put("settlement_id", IDUtil.getId());
				itemHistory.put("settlement_date", date);
				itemHistory.put("merch_id", merchId);
				itemHistory.put("item_id", purchOrderLineMap.get("item_id"));
				itemHistoryMap.put(itemHistoryIdentity, itemHistory);
			}
		}
		merchHistoryMap.put("purch_item_count", purchItemSet.size());
		//添加默认值
		Collection<Map<String, Object>> merchItemList=itemHistoryMap.values();
		for(Map<String, Object> merchItemMap : merchItemList) {
			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			if(itemHistoryMap.get(itemHistoryIdentity).get("purch_quantity")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("purch_quantity", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("purch_amount")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("purch_amount", BigDecimal.ZERO);
			}
		}
	}
	
	/**
	 * 日结：计算库存
	 * @param merchId
	 * @param date
	 * @param merchHistoryMap
	 * @param itemHistoryMap
	 * @throws Exception
	 */
	private void whseDailySettlement(String merchId, String date, Map<String, Object> merchHistoryMap, Map<String, Map<String, Object>> itemHistoryMap) throws Exception {
		BigDecimal whseAmount = BigDecimal.ZERO; // 库存总额
		BigDecimal whseQuantity = BigDecimal.ZERO; // 库存总量
		
		int whseItemCount = 0; // 库存商品种类
		// 参数 库存表只需要merch_id作为参数
		Map<String, Object> whseMerchParam = new HashMap<String, Object>();
		whseMerchParam.put("merch_id", merchId);
		whseMerchParam.put("page_index", -1);
		whseMerchParam.put("page_size", -1);
		List<Map<String, Object>> whseMerchList = whseDao.selectWhseMerch(whseMerchParam);
		for(Map<String, Object> whseMerchMap : whseMerchList) {
			BigDecimal itemWhseAmount = BigDecimal.ZERO;
			BigDecimal itemWhseQuantity = new BigDecimal(whseMerchMap.get("qty_whse").toString());
			BigDecimal itemWhseWarnQuantity = new BigDecimal(whseMerchMap.get("qty_whse_warn").toString());
			whseQuantity = whseQuantity.add(itemWhseQuantity);
			String itemHistoryIdentity = merchId+","+whseMerchMap.get("item_id"); // merch_id+item_id作为map的key
			if(itemHistoryMap.containsKey(itemHistoryIdentity)) {
				Map<String, Object> itemMap = (Map<String, Object>)itemHistoryMap.get(itemHistoryIdentity);
				if(itemMap.get("cost")!=null) {
					itemWhseAmount = itemWhseQuantity.multiply(new BigDecimal(itemMap.get("cost").toString()));
				}
				whseAmount = whseAmount.add(itemWhseAmount);
				itemMap.put("whse_amount", itemWhseAmount);
				itemMap.put("whse_quantity", itemWhseQuantity);
				itemMap.put("whse_warn_quantity", itemWhseWarnQuantity);
				
			} else {
				whseAmount = whseAmount.add(itemWhseAmount);
				Map<String, Object> itemHistory = new HashMap<String, Object>();
				itemHistory.put("settlement_id", IDUtil.getId());
				itemHistory.put("settlement_date", date);
				itemHistory.put("merch_id", merchId);
				itemHistory.put("item_id", whseMerchMap.get("item_id"));
				itemHistory.put("whse_amount", itemWhseAmount);
				itemHistory.put("whse_quantity", itemWhseQuantity);
				itemHistory.put("whse_warn_quantity", itemWhseWarnQuantity);
				itemHistoryMap.put(itemHistoryIdentity, itemHistory);
			}
			whseItemCount++;
		}
		// 赋值
		merchHistoryMap.put("whse_amount", whseAmount);
		merchHistoryMap.put("whse_quantity", whseQuantity);
		merchHistoryMap.put("whse_item_count", whseItemCount);
	}
	
	/**
	 * 日结：计算盘点
	 * @param merchId
	 * @param date
	 * @param merchHistoryMap
	 * @param itemHistoryMap
	 * @throws Exception
	 */
	private void turnDailySettlement(String merchId, String date, Map<String, Object> merchHistoryMap, Map<String, Map<String, Object>> itemHistoryMap) throws Exception {
		BigDecimal whseTurnProfitQuantity = BigDecimal.ZERO; // 盘点收益金额
		BigDecimal whseTurnLossQuantity = BigDecimal.ZERO; // 盘点损失金额
		BigDecimal whseTurnProfitAmount = BigDecimal.ZERO; // 盘点收益数量
		BigDecimal whseTurnLossAmount = BigDecimal.ZERO; // 盘点损失数量
		int whseTurnCount = 0; // 盘点次数
		StringBuffer turnIdBuffer = new StringBuffer();
		// 参数 库存表只需要merch_id作为参数
		Map<String, Object> whseTurnParam = new HashMap<String, Object>();
		whseTurnParam.put("merch_id", merchId);
		whseTurnParam.put("turn_date", date);
		List<Map<String, Object>> whseTurnList = whseDao.selectWhseTurn(whseTurnParam);
		for(Map<String, Object> whseTurnMap : whseTurnList) {
			if(whseTurnMap.get("amt_profit")!=null) {
				whseTurnProfitAmount = whseTurnProfitAmount.add(new BigDecimal(whseTurnMap.get("amt_profit").toString()));
			}
			if(whseTurnMap.get("amt_loss")!=null) {
				whseTurnLossAmount = whseTurnLossAmount.add(new BigDecimal(whseTurnMap.get("amt_loss").toString()));
			}
			if(whseTurnMap.get("qty_profit")!=null) {
				whseTurnProfitQuantity = whseTurnProfitQuantity.add(new BigDecimal(whseTurnMap.get("qty_profit").toString()));
			}
			if(whseTurnMap.get("qty_loss")!=null) {
				whseTurnLossQuantity = whseTurnLossQuantity.add(new BigDecimal(whseTurnMap.get("qty_loss").toString()));
			}
			whseTurnCount++;
			if(turnIdBuffer.length()==0) {
				turnIdBuffer.append(whseTurnMap.get("turn_id"));
			} else {
				turnIdBuffer.append(","+whseTurnMap.get("turn_id"));
			}
		}
		// 赋值
		merchHistoryMap.put("whse_turn_profit", whseTurnProfitAmount);
		merchHistoryMap.put("whse_turn_loss", whseTurnLossAmount);
		merchHistoryMap.put("whse_turn_profit_quantity", whseTurnProfitQuantity);
		merchHistoryMap.put("whse_turn_loss_quantity", whseTurnLossQuantity);
		merchHistoryMap.put("whse_turn_count", whseTurnCount);

		// = * = * = * = * = 盘点单行 = * = * = * = * = 
		Map<String, Object> whseTurnLineParam = new HashMap<String, Object>();
		whseTurnLineParam.put("turn_id", turnIdBuffer.toString());
		List<Map<String, Object>> whseTurnLineList = whseDao.searchWhseTurnLineByItem(whseTurnLineParam);
		for(Map<String, Object> whseTurnLineMap : whseTurnLineList) {
			String itemHistoryIdentity = merchId+","+whseTurnLineMap.get("item_id"); // merch_id+item_id作为map的key
			Map<String, Object> itemHistory = new HashMap<String, Object>();
			itemHistory.put("whse_turn_pl", whseTurnLineMap.get("amt_pl"));
			itemHistory.put("whse_turn_pl_quantity", whseTurnLineMap.get("qty_pl"));
			if(itemHistoryMap.containsKey(itemHistoryIdentity)) {
				((Map<String, Object>)itemHistoryMap.get(itemHistoryIdentity)).putAll(itemHistory);
			} else {
				itemHistory.put("settlement_id", IDUtil.getId());
				itemHistory.put("settlement_date", date);
				itemHistory.put("merch_id", merchId);
				itemHistory.put("item_id", whseTurnLineMap.get("item_id"));
				itemHistoryMap.put(itemHistoryIdentity, itemHistory);
			}
		}
		//添加默认值
		Collection<Map<String, Object>> merchItemList=itemHistoryMap.values();
		for(Map<String, Object> merchItemMap : merchItemList) {
			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			if(itemHistoryMap.get(itemHistoryIdentity).get("whse_turn_pl")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("whse_turn_pl", BigDecimal.ZERO);
			}
			if(itemHistoryMap.get(itemHistoryIdentity).get("whse_turn_pl_quantity")==null){
				itemHistoryMap.get(itemHistoryIdentity).put("whse_turn_pl_quantity", BigDecimal.ZERO);
			}
		}
	}
	
	@Override
	public String searchSystemSettlement() throws Exception {
		LOG.debug("StatisticsServiceImpl searchSystemSettlement");
		return statisticsDao.selectSystemSettlement();
	}
	
	@Override
	public void updateSystemSettlement(String settlementDate) throws Exception {
		LOG.debug("StatisticsServiceImpl searchSystemSettlement settlementDate: " + settlementDate);
		statisticsDao.updateSystemSettlement(settlementDate);
	}
	
	/**
	 * listener进行日结专用, 会查日结到哪一天, 如果不是今天则开始今天的日结
	 * @throws Exception
	 */
	private void autoDailySettlement(Map<String, Object> paramMap) throws Exception {
//		String currentServerUrl = InetAddress.getLocalHost().toString();
//		currentServerUrl = currentServerUrl.substring(currentServerUrl.indexOf("/")+1);
//		String settlementServerUrl = RetailConfig.getSettlementServerUrl();
//		// 如果当前的ip地址与配置的ip地址相同, 则进行日结否则跳出日结
//		if(!currentServerUrl.equals(settlementServerUrl)) {
//			LOG.debug("当前服务器地址:"+currentServerUrl+", 结算服务器地址:"+settlementServerUrl+", 跳过结算步骤");
//			return;
//		}
		paramMap.put("is_settling", true);
		try {
			String settlementDate = statisticsDao.selectSystemSettlement();
			String today = DateUtil.getToday();
			if(settlementDate!=null && Integer.valueOf(settlementDate)>=Integer.valueOf(today)) {//????
				LOG.debug("当前日期:"+today+", 结算日期:"+settlementDate+", 跳过结算步骤");
			} else {
				LOG.debug("当前日期:"+today+", 结算日期:"+settlementDate+", 日结开始时间:"+DateUtil.getCurrentTime());
			}
			long lastMilliTime = DateUtil.getCurrentTimeMillis();
			while(settlementDate==null || Integer.valueOf(settlementDate)<Integer.valueOf(today)) {
				if(settlementDate==null) {
					settlementDate = today;
				} else {
					settlementDate = DateUtil.getNextDay(settlementDate);
				}
				Map<String, Object> settlementParam = new HashMap<String, Object>();
				settlementParam.put("settlement_date", settlementDate);
				if(paramMap.get("merch_id")!=null) {
					settlementParam.put("merch_id", paramMap.get("merch_id"));
					LOG.debug("当前结算日期:"+settlementDate+", 结算以下商户编码:"+paramMap.get("merch_id"));
				} else {
					LOG.debug("当前结算日期:"+settlementDate+", 结算所有商户");
				}
				createDailySettlement(settlementParam);
				LOG.debug("结算日期:"+settlementDate+", 日结结束时间:"+DateUtil.getCurrentTime()+", 用时:"+(DateUtil.getCurrentTimeMillis()-lastMilliTime));
				if(Integer.valueOf(settlementDate)>=Integer.valueOf(today)) {
					statisticsDao.updateSystemSettlement(settlementDate);
					LOG.debug("更新结算日期:"+settlementDate);
				}
			}
		} catch (Exception e) {
			LOG.error("日结错误", e);
		} finally {
			paramMap.put("is_settling", false);
		}
	}

	// 正向重日结
	@Override
	public void resetDailySettlementAscendingly(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl resetDailySettlementAscendingly paramMap: " + paramMap);
		String today = DateUtil.getToday();
		String startDate = MapUtil.getString(paramMap, "start_date", today);
		String endDate = MapUtil.getString(paramMap, "end_date", today);
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String itemIdParam = MapUtil.getString(paramMap, "item_id");
		while(ParamUtil.compareTo(startDate, endDate)<=0) {
			// 商户日结初值
			BigDecimal profitAmount = BigDecimal.ZERO;//当日利润总额
			BigDecimal profitAmountOnline = BigDecimal.ZERO;
			BigDecimal profitAmountOffline = BigDecimal.ZERO;
			
			BigDecimal saleAmount = BigDecimal.ZERO;//当日销售总额
			BigDecimal saleAmountOnline = BigDecimal.ZERO;
			BigDecimal saleAmountOffline = BigDecimal.ZERO;
			
			BigDecimal saleLoss = BigDecimal.ZERO;//当日销售抹零总额
			BigDecimal saleLossOnline = BigDecimal.ZERO;
			BigDecimal saleLossOffline = BigDecimal.ZERO;
			
			int saleCount = 0;//当日销售总笔数
			int saleCountOnline = 0;
			int saleCountOffline = 0;
			
			BigDecimal lossAmount = BigDecimal.ZERO;//当日损耗总额(退货利润损耗)
			BigDecimal lossAmountOnline = BigDecimal.ZERO;
			BigDecimal lossAmountOffline = BigDecimal.ZERO;
			
			BigDecimal returnAmount = BigDecimal.ZERO;//当日退货总额
			BigDecimal returnAmountOnline = BigDecimal.ZERO;
			BigDecimal returnAmountOffline = BigDecimal.ZERO;
			
			BigDecimal purchAmount = BigDecimal.ZERO;//当日进货总额
			BigDecimal purchQuantity = BigDecimal.ZERO;//当日进货总量
			BigDecimal purchItemCount = BigDecimal.ZERO;//当日进货商品种类
			int purchCount = 0;///当日进货次数
			
			BigDecimal whseAmount = BigDecimal.ZERO;//库存总额
			BigDecimal whseQuantity = BigDecimal.ZERO;//库存总量
			// int whseItemCount 库存商品数与商品属性数相同
			
			BigDecimal whseTurnProfit = BigDecimal.ZERO;//当日盘点收益金额
			BigDecimal whseTurnLoss = BigDecimal.ZERO;//当日盘点损失金额
			BigDecimal whseTurnProfitQuantity = BigDecimal.ZERO;//当日盘点收益数量
			BigDecimal whseTurnLossQuantity = BigDecimal.ZERO;//当日盘点损失数量
			int whseTurnCount = 0;//当日盘点次数
			
			
			// 商品日结属性列表 {item_id : 内容}
			Map<String, Map<String, Object>> itemResultMap = new HashMap<String, Map<String, Object>>();
			// 商品日结处理过的订单
			Map<String, Object> itemUsedOrderMap = new HashMap<String, Object>();
			// 商户日结处理过的订单
			Map<String, Object> usedOrderMap = new HashMap<String, Object>();
			// 重新日结的查询参数
			Map<String, Object> settlementParam = new HashMap<String, Object>();
			settlementParam.put("merch_id", merchId);
			settlementParam.put("settlement_date", startDate);
			if(!StringUtil.isBlank(itemIdParam)) settlementParam.put("item_id", itemIdParam);
			// 获取重新日结的原始数据
			List<Map<String, Object>> itemSettlement = statisticsDao.resetMerchDailySettlement(settlementParam);
			// 加工重新日结数据
			for(Map<String, Object> item : itemSettlement) {
				// 商品日结初值
				String itemId = MapUtil.getString(item, "item_id");
				String itemBar = MapUtil.getString(item, "item_bar");
				String itemName = MapUtil.getString(item, "item_name");
				String itemKindId = MapUtil.getString(item, "item_kind_id");
				String unitName = MapUtil.getString(item, "unit_name");
				BigDecimal cost = MapUtil.getBigDecimal(item, "cost");
				BigDecimal pri1 = MapUtil.getBigDecimal(item, "pri1");
				BigDecimal pri2 = MapUtil.getBigDecimal(item, "pri2");
				BigDecimal pri4 = MapUtil.getBigDecimal(item, "pri4");
				BigDecimal discount = MapUtil.getBigDecimal(item, "discount");
				boolean isSaleOnline = "04".equals(MapUtil.getString(item, "sale_type")); //用销售类型来判断是线上还是线下销售
				boolean isReturnOnline = "04".equals(MapUtil.getString(item, "return_type")); //用退货类型来判断是线上还是线下
				String saleId = MapUtil.getString(item, "sale_id");
				String returnId = MapUtil.getString(item, "return_id");
				String purchId = MapUtil.getString(item, "purch_id");
				String turnId = MapUtil.getString(item, "turn_id");
				
				BigDecimal itemProfitAmount = BigDecimal.ZERO;
				BigDecimal itemProfitAmountOnline = BigDecimal.ZERO;
				BigDecimal itemProfitAmountOffline = BigDecimal.ZERO;
				BigDecimal itemLossAmount = BigDecimal.ZERO;
				BigDecimal itemLossAmountOnline = BigDecimal.ZERO;
				BigDecimal itemLossAmountOffline = BigDecimal.ZERO;
				BigDecimal itemSaleAmount = BigDecimal.ZERO;
				BigDecimal itemSaleAmountOnline = BigDecimal.ZERO;
				BigDecimal itemSaleAmountOffline = BigDecimal.ZERO;
				BigDecimal itemSaleQuantity = BigDecimal.ZERO;
				BigDecimal itemSaleQuantityOnline = BigDecimal.ZERO;
				BigDecimal itemSaleQuantityOffline = BigDecimal.ZERO;
				BigDecimal itemReturnAmount = BigDecimal.ZERO;
				BigDecimal itemReturnAmountOnline = BigDecimal.ZERO;
				BigDecimal itemReturnAmountOffline = BigDecimal.ZERO;
				BigDecimal itemReturnQuantity = BigDecimal.ZERO;
				BigDecimal itemReturnQuantityOnline = BigDecimal.ZERO;
				BigDecimal itemReturnQuantityOffline = BigDecimal.ZERO;
				BigDecimal itemPurchAmount = BigDecimal.ZERO;
				BigDecimal itemPurchQuantity = BigDecimal.ZERO;
				BigDecimal itemWhseTurnPLAmount = BigDecimal.ZERO;
				BigDecimal itemWhseTurnPLQuantity = BigDecimal.ZERO;
				BigDecimal itemWhseWarnQuantity = MapUtil.getBigDecimal(item, "whse_warn_quantity");
				BigDecimal itemWhseAdjustedQuantity = BigDecimal.ZERO; // 库存调整量=退货量+入库量+盘差量-销售量
				// itemWhseAmount = itemWhseQuantity * cost
				// 处理商品日结
				Map<String, Object> itemResult = MapUtil.get(itemResultMap, itemId, new HashMap<String, Object>());
				if(!itemResult.isEmpty()) { // 如果已有此商品则取值
					itemProfitAmount = MapUtil.getBigDecimal(itemResult, "profit_amount");
					itemProfitAmountOnline = MapUtil.getBigDecimal(itemResult, "profit_amount_online");
					itemProfitAmountOffline = MapUtil.getBigDecimal(itemResult, "profit_amount_offline");
					itemLossAmount = MapUtil.getBigDecimal(itemResult, "loss_amount");
					itemLossAmountOnline = MapUtil.getBigDecimal(itemResult, "loss_amount_online");
					itemLossAmountOffline = MapUtil.getBigDecimal(itemResult, "loss_amount_offline");
					itemSaleAmount = MapUtil.getBigDecimal(itemResult, "sale_amount");
					itemSaleAmountOnline = MapUtil.getBigDecimal(itemResult, "sale_amount_online");
					itemSaleAmountOffline = MapUtil.getBigDecimal(itemResult, "sale_amount_offline");
					itemSaleQuantity = MapUtil.getBigDecimal(itemResult, "sale_quantity");
					itemSaleQuantityOnline = MapUtil.getBigDecimal(itemResult, "sale_quantity_online");
					itemSaleQuantityOffline = MapUtil.getBigDecimal(itemResult, "sale_quantity_offline");
					itemReturnAmount = MapUtil.getBigDecimal(itemResult, "return_amount");
					itemReturnAmountOnline = MapUtil.getBigDecimal(itemResult, "return_amount_online");
					itemReturnAmountOffline = MapUtil.getBigDecimal(itemResult, "return_amount_offline");
					itemReturnQuantity = MapUtil.getBigDecimal(itemResult, "return_quantity");
					itemReturnQuantityOnline = MapUtil.getBigDecimal(itemResult, "return_quantity_online");
					itemReturnQuantityOffline = MapUtil.getBigDecimal(itemResult, "return_quantity_offline");
					itemPurchAmount = MapUtil.getBigDecimal(itemResult, "purch_amount");
					itemPurchQuantity = MapUtil.getBigDecimal(itemResult, "purch_quantity");
					itemWhseTurnPLAmount = MapUtil.getBigDecimal(itemResult, "whse_turn_pl_amount");
					itemWhseTurnPLQuantity = MapUtil.getBigDecimal(itemResult, "whse_turn_pl_quantity");
				} else { // 如果没有此商品则放入属性
					itemResult.put("settlement_id", IDUtil.getId());
					itemResult.put("merch_id", merchId);
					itemResult.put("settlement_date", startDate);
					itemResult.put("item_id", itemId);
					itemResult.put("item_bar", itemBar);
					itemResult.put("item_name", itemName);
					itemResult.put("item_kind_id", itemKindId);
					itemResult.put("unit_name", unitName);
					itemResult.put("cost", cost);
					itemResult.put("pri1", pri1);
					itemResult.put("pri2", pri2);
					itemResult.put("pri4", pri4);
					itemResult.put("discount", discount);
					itemResult.put("whse_warn_quantity", itemWhseWarnQuantity);
				}
				// 放入商品日结数据, 存储数据 = 原数据 + 本行数据
				// 1. 销售
				if(!StringUtil.isBlank(saleId) && !itemUsedOrderMap.containsKey(saleId+","+itemId)) {
					itemProfitAmount = itemProfitAmount.add(MapUtil.getBigDecimal(item, "item_profit"));
					itemSaleAmount = itemSaleAmount.add(MapUtil.getBigDecimal(item, "item_sale_amount"));
					itemSaleQuantity = itemSaleQuantity.add(MapUtil.getBigDecimal(item, "item_sale_quantity"));
					itemWhseAdjustedQuantity = itemWhseAdjustedQuantity.subtract(MapUtil.getBigDecimal(item, "item_sale_quantity")); // 库存调整
					if(isSaleOnline) {
						itemProfitAmountOnline = itemProfitAmountOnline.add(MapUtil.getBigDecimal(item, "item_profit"));
						itemSaleAmountOnline = itemSaleAmountOnline.add(MapUtil.getBigDecimal(item, "item_sale_amount"));
						itemSaleQuantityOnline = itemSaleQuantityOnline.add(MapUtil.getBigDecimal(item, "item_sale_quantity"));
					} else {
						itemProfitAmountOffline = itemProfitAmountOffline.add(MapUtil.getBigDecimal(item, "item_profit"));
						itemSaleAmountOffline = itemSaleAmountOffline.add(MapUtil.getBigDecimal(item, "item_sale_amount"));
						itemSaleQuantityOffline = itemSaleQuantityOffline.add(MapUtil.getBigDecimal(item, "item_sale_quantity"));
					}
					// 标记为处理过
					itemUsedOrderMap.put(saleId+","+itemId, null);
				}
				// 2. 退货
				if(!StringUtil.isBlank(returnId) && !itemUsedOrderMap.containsKey(returnId+","+itemId)) {
					itemLossAmount = itemLossAmount.add(MapUtil.getBigDecimal(item, "item_loss"));
					itemReturnAmount = itemReturnAmount.add(MapUtil.getBigDecimal(item, "item_return_amount"));
					itemReturnQuantity = itemReturnQuantity.add(MapUtil.getBigDecimal(item, "item_return_quantity"));
					itemWhseAdjustedQuantity = itemWhseAdjustedQuantity.add(MapUtil.getBigDecimal(item, "item_return_quantity")); // 库存调整
					if(isReturnOnline) {
						itemReturnAmountOnline = itemReturnAmountOnline.add(MapUtil.getBigDecimal(item, "item_return_amount"));
						itemReturnQuantityOnline = itemReturnQuantityOnline.add(MapUtil.getBigDecimal(item, "item_return_quantity"));
						itemLossAmountOnline = itemLossAmountOnline.add(MapUtil.getBigDecimal(item, "item_loss"));
					} else {
						itemLossAmountOffline = itemLossAmountOffline.add(MapUtil.getBigDecimal(item, "item_loss"));
						itemReturnAmountOffline = itemReturnAmountOffline.add(MapUtil.getBigDecimal(item, "item_return_amount"));
						itemReturnQuantityOffline = itemReturnQuantityOffline.add(MapUtil.getBigDecimal(item, "item_return_quantity"));
					}
					// 标记为处理过
					itemUsedOrderMap.put(returnId+","+itemId, null);
				}
				// 3. 采购
				if(!StringUtil.isBlank(purchId) && !itemUsedOrderMap.containsKey(purchId+","+itemId)) {
					itemPurchAmount = itemPurchAmount.add(MapUtil.getBigDecimal(item, "item_purch_amount"));
					itemPurchQuantity = itemPurchQuantity.add(MapUtil.getBigDecimal(item, "item_purch_quantity"));
					itemWhseAdjustedQuantity = itemWhseAdjustedQuantity.add(MapUtil.getBigDecimal(item, "item_purch_quantity")); // 库存调整
					purchCount++;
					// 标记为处理过
					itemUsedOrderMap.put(purchId+","+itemId, null);
				}
				// 4. 盘点
				if(!StringUtil.isBlank(turnId) && !itemUsedOrderMap.containsKey(turnId+","+itemId)) {
					itemWhseTurnPLAmount = itemWhseTurnPLAmount.add(MapUtil.getBigDecimal(item, "item_turn_amount"));
					itemWhseTurnPLQuantity = itemWhseTurnPLQuantity.add(MapUtil.getBigDecimal(item, "item_turn_quantity"));
					itemWhseAdjustedQuantity = itemWhseAdjustedQuantity.add(MapUtil.getBigDecimal(item, "item_turn_quantity")); // 库存调整
					whseTurnCount++;
					// 标记为处理过
					itemUsedOrderMap.put(turnId+","+itemId, null);
				}
				// 5. 库存
				if(!itemResultMap.containsKey(itemId)) { // 如果无此商品则加库存
					itemResult.put("whse_quantity", MapUtil.getBigDecimal(item, "whse_quantity").add(itemWhseAdjustedQuantity));
					itemResult.put("whse_amount", MapUtil.getBigDecimal(item, "whse_quantity").add(itemWhseAdjustedQuantity).multiply(cost)); // 库存额=(库存量+调整量)*成本价
				} else { // 如果有此商品则加库存增量
					itemResult.put("whse_quantity", MapUtil.getBigDecimal(itemResult, "whse_quantity").add(itemWhseAdjustedQuantity));
					itemResult.put("whse_amount", MapUtil.getBigDecimal(itemResult, "whse_amount").add(itemWhseAdjustedQuantity.multiply(cost))); // 库存额=原库存额+调整量*成本价
				}
				// 放入商品日结数据
				itemResult.put("profit_amount", itemProfitAmount);
				itemResult.put("sale_amount", itemSaleAmount);
				itemResult.put("sale_quantity", itemSaleQuantity);
				itemResult.put("profit_amount_online", itemProfitAmountOnline);
				itemResult.put("sale_amount_online", itemSaleAmountOnline);
				itemResult.put("sale_quantity_online", itemSaleQuantityOnline);
				itemResult.put("profit_amount_offline", itemProfitAmountOffline);
				itemResult.put("sale_amount_offline", itemSaleAmountOffline);
				itemResult.put("sale_quantity_offline", itemSaleQuantityOffline);
				itemResult.put("loss_amount", itemLossAmount);
				itemResult.put("return_amount", itemReturnAmount);
				itemResult.put("return_quantity", itemReturnQuantity);
				itemResult.put("return_amount_online", itemReturnAmountOnline);
				itemResult.put("return_quantity_online", itemReturnQuantityOnline);
				itemResult.put("loss_amount_online", itemLossAmountOnline);
				itemResult.put("loss_amount_offline", itemLossAmountOffline);
				itemResult.put("return_amount_offline", itemReturnAmountOffline);
				itemResult.put("return_quantity_offline", itemReturnQuantityOffline);
				itemResult.put("purch_amount", itemPurchAmount);
				itemResult.put("purch_quantity", itemPurchQuantity);
				itemResult.put("whse_turn_pl_amount", itemWhseTurnPLAmount);
				itemResult.put("whse_turn_pl_quantity", itemWhseTurnPLQuantity);
				
				// 在循环的末尾保存处理后的商品
				// 处理商户日结, 只处理没有处理过的进销盘退单
				// 1. 销售
				if(!StringUtil.isBlank(saleId) && !usedOrderMap.containsKey(saleId)) {
					profitAmount = profitAmount.add(MapUtil.getBigDecimal(item, "sale_profit"));
					saleAmount = saleAmount.add(MapUtil.getBigDecimal(item, "sale_amount"));
					saleLoss = saleLoss.add(MapUtil.getBigDecimal(item, "sale_loss"));
					saleCount++;
					if(isSaleOnline) {
						profitAmountOnline = profitAmountOnline.add(MapUtil.getBigDecimal(item, "sale_profit"));
						saleAmountOnline = saleAmountOnline.add(MapUtil.getBigDecimal(item, "sale_amount"));
						saleLossOnline = saleLossOnline.add(MapUtil.getBigDecimal(item, "sale_loss"));
						saleCountOnline++;
					} else {
						profitAmountOffline = profitAmountOffline.add(MapUtil.getBigDecimal(item, "sale_profit"));
						saleAmountOffline = saleAmountOffline.add(MapUtil.getBigDecimal(item, "sale_amount"));
						saleLossOffline = saleLossOffline.add(MapUtil.getBigDecimal(item, "sale_loss"));
						saleCountOffline++;
					}
					// 标记为处理过
					usedOrderMap.put(saleId, null);
				}
				// 2. 退货
				if(!StringUtil.isBlank(returnId) && !usedOrderMap.containsKey(returnId)) {
					lossAmount = lossAmount.add(MapUtil.getBigDecimal(item, "loss_amount"));
					returnAmount = returnAmount.add(MapUtil.getBigDecimal(item, "return_amount"));
					if(isReturnOnline) {
						lossAmountOnline = lossAmountOnline.add(MapUtil.getBigDecimal(item, "loss_amount"));
						returnAmountOnline = returnAmountOnline.add(MapUtil.getBigDecimal(item, "return_amount"));
					} else {
						lossAmountOffline = lossAmountOffline.add(MapUtil.getBigDecimal(item, "loss_amount"));
						returnAmountOffline = returnAmountOffline.add(MapUtil.getBigDecimal(item, "return_amount"));
					}
					// 标记为处理过
					usedOrderMap.put(returnId, null);
				}
				// 3. 采购
				if(!StringUtil.isBlank(purchId) && !usedOrderMap.containsKey(purchId)) {
					purchAmount = purchAmount.add(MapUtil.getBigDecimal(item, "purch_amount"));
					purchQuantity = purchQuantity.add(MapUtil.getBigDecimal(item, "purch_quantity"));
					purchItemCount = purchItemCount.add(MapUtil.getBigDecimal(item, "purch_item_count"));
					purchCount++;
					// 标记为处理过
					usedOrderMap.put(purchId, null);
				}
				// 4. 盘点
				if(!StringUtil.isBlank(turnId) && !usedOrderMap.containsKey(turnId)) {
					whseTurnProfit = whseTurnProfit.add(MapUtil.getBigDecimal(item, "whse_turn_profit_amount"));
					whseTurnLoss = whseTurnLoss.add(MapUtil.getBigDecimal(item, "whse_turn_loss_amount"));
					whseTurnProfitQuantity = whseTurnProfitQuantity.add(MapUtil.getBigDecimal(item, "whse_turn_profit_quantity"));
					whseTurnLossQuantity = whseTurnLossQuantity.add(MapUtil.getBigDecimal(item, "whse_turn_loss_quantity"));
					whseTurnCount++;
					// 标记为处理过
					usedOrderMap.put(turnId, null);
				}
				// 5. 库存
				if(!itemResultMap.containsKey(itemId)) { // 如果无此商品则加库存
					whseQuantity = whseQuantity.add(MapUtil.getBigDecimal(itemResult, "whse_quantity"));
					whseAmount = whseAmount.add(MapUtil.getBigDecimal(itemResult, "whse_amount"));
				} else { // 如果有此商品则加库存增量
					whseQuantity = whseQuantity.add(itemWhseAdjustedQuantity);
					whseAmount = whseAmount.add(itemWhseAdjustedQuantity.multiply(cost));
				}
				// int whseItemCount 库存商品数与商品属性数相同
				// 处理商户库存时需要判断此商品是否曾被处理过, 所以先处理商户库存再保存处理后的商品
				itemResultMap.put(itemId, itemResult);
			} // 商品循环结束
			// 放入商户日结数据
			Map<String, Object> merchResult = new HashMap<String, Object>();
			merchResult.put("settlement_id", IDUtil.getId());
			merchResult.put("merch_id", merchId);
			merchResult.put("settlement_date", startDate);
			merchResult.put("profit_amount", profitAmount);
			merchResult.put("profit_amount_online", profitAmountOnline);
			merchResult.put("profit_amount_offline", profitAmountOffline);
			merchResult.put("sale_amount", saleAmount);
			merchResult.put("sale_amount_online", saleAmountOnline);
			merchResult.put("sale_amount_offline", saleAmountOffline);
			merchResult.put("sale_loss", saleLoss);
			merchResult.put("sale_loss_online", saleLossOnline);
			merchResult.put("sale_loss_offline", saleLossOffline);
			merchResult.put("sale_count", saleCount);
			merchResult.put("sale_count_online", saleCountOnline);
			merchResult.put("sale_count_offline", saleCountOffline);
			merchResult.put("loss_amount", lossAmount);
			merchResult.put("loss_amount_online", lossAmountOnline);
			merchResult.put("loss_amount_offline", lossAmountOffline);
			merchResult.put("return_amount", returnAmount);
			merchResult.put("return_amount_online", returnAmountOnline);
			merchResult.put("return_amount_offline", returnAmountOffline);
			merchResult.put("purch_amount", purchAmount);
			merchResult.put("purch_quantity", purchQuantity);
			merchResult.put("purch_item_count", purchItemCount);
			merchResult.put("purch_count", purchCount);
			merchResult.put("whse_amount", whseAmount);
			merchResult.put("whse_quantity", whseQuantity);//库存总量
			merchResult.put("whse_item_count", itemResultMap.size()); // 库存商品数与商品属性数相同
			merchResult.put("whse_turn_profit_amount", whseTurnProfit);
			merchResult.put("whse_turn_loss_amount", whseTurnLoss);
			merchResult.put("whse_turn_profit_quantity", whseTurnProfitQuantity);
			merchResult.put("whse_turn_loss_quantity", whseTurnLossQuantity);
			merchResult.put("whse_turn_count", whseTurnCount);
			// 清除临时商户日结表
			statisticsDao.cleanMerchTempSettlement(settlementParam);
			// 生成临时商户日结表
			statisticsDao.tempMerchDailySettlement(merchResult);
			// 备份商户日结表
			statisticsDao.backupMerchDailySettlement(settlementParam);
			// 更新商户日结表
			statisticsDao.refreshMerchDailySettlement(settlementParam);
			// 清除临时商品日结表
			statisticsDao.cleanMerchItemTempSettlement(settlementParam);
			// 生成临时商品日结表
			statisticsDao.tempMerchItemDailySettlement(new ArrayList(itemResultMap.values()));
			// 备份商品日结表
			statisticsDao.backupMerchItemDailySettlement(settlementParam);
			// 更新商品日结表
			statisticsDao.refreshMerchItemDailySettlement(settlementParam);
			// 下一天继续循环
			startDate = DateUtil.getNextDay(startDate);
		}
	}
	
	// 逆向重日结, 先查询当前库存和今日的进销盘退, 计算出昨天的库存, 合并上昨天的进销盘退保存到第一个日结
	// 第一个日结库存减去第一个日结的进销盘退, 计算出前天的库存, 合并上前天的进销盘退保存到第二个日结
	@Override
	public void resetDailySettlementDescendingly(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl resetDailySettlementDescendingly paramMap: " + paramMap);
		String today = DateUtil.getToday();
		String startDate = today; // 开始日期固定为今天
		String endDate = MapUtil.getString(paramMap, "end_date", startDate);
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String itemIdParam = MapUtil.getString(paramMap, "item_id");
		
		
		while(ParamUtil.compareTo(endDate, startDate)<=0) { // 从后往前推
			boolean isToday = today.equals(startDate); // 逆向重日结第一天和其他各天取的数据不一样
			// 商户日结初值
			BigDecimal profitAmount = BigDecimal.ZERO;//当日总利润
			BigDecimal profitAmountOnline = BigDecimal.ZERO;
			BigDecimal profitAmountOffline = BigDecimal.ZERO;
			BigDecimal saleAmount = BigDecimal.ZERO;//当日销售总额
			BigDecimal saleAmountOnline = BigDecimal.ZERO;
			BigDecimal saleAmountOffline = BigDecimal.ZERO;
			BigDecimal saleLoss = BigDecimal.ZERO;//当日抹零总额
			BigDecimal saleLossOnline = BigDecimal.ZERO;
			BigDecimal saleLossOffline = BigDecimal.ZERO;
			int saleCount = 0;//销售笔数
			int saleCountOnline = 0;
			int saleCountOffline = 0;
			BigDecimal lossAmount = BigDecimal.ZERO;//退货利润损失额
			BigDecimal lossAmountOnline = BigDecimal.ZERO;
			BigDecimal lossAmountOffline = BigDecimal.ZERO;
			BigDecimal returnAmount = BigDecimal.ZERO;//当日退货总金额
			BigDecimal returnAmountOnline = BigDecimal.ZERO;
			BigDecimal returnAmountOffline = BigDecimal.ZERO;
			BigDecimal purchAmount = BigDecimal.ZERO;//采购总额
			BigDecimal purchQuantity = BigDecimal.ZERO;
			BigDecimal purchItemCount = BigDecimal.ZERO;
			int purchCount = 0;//采购次数
			BigDecimal whseAmount = BigDecimal.ZERO;//库存总额
			BigDecimal whseQuantity = BigDecimal.ZERO;//库存总量
			// int whseItemCount 库存商品数与商品属性数相同
			BigDecimal whseTurnProfit = BigDecimal.ZERO;//当日盘点收益金额
			BigDecimal whseTurnLoss = BigDecimal.ZERO;//当日盘点损失金额
			BigDecimal whseTurnProfitQuantity = BigDecimal.ZERO;//当日盘点收益数量
			BigDecimal whseTurnLossQuantity = BigDecimal.ZERO;//当日盘点损失数量
			int whseTurnCount = 0;//当日盘点次数
			
			
			
			// 商品日结属性列表 {item_id : 内容}
			Map<String, Map<String, Object>> itemResultMap = new HashMap<String, Map<String, Object>>();
			// 商品日结处理过的订单
			Map<String, Object> itemUsedOrderMap = new HashMap<String, Object>();
			// 商户日结处理过的订单
			Map<String, Object> usedOrderMap = new HashMap<String, Object>();
			// 重新日结的查询参数
			Map<String, Object> settlementParam = new HashMap<String, Object>();
			settlementParam.put("merch_id", merchId);
			settlementParam.put("settlement_date", startDate);
			settlementParam.put("next_date", DateUtil.getNextDay(startDate));
			if(!StringUtil.isBlank(itemIdParam)) settlementParam.put("item_id", itemIdParam);
			
			
			// 获取重新日结的原始数据
			List<Map<String, Object>> itemSettlement = 
					isToday ? statisticsDao.resetMerchTodaySettlement(settlementParam) : statisticsDao.resetMerchDailySettlement(settlementParam);

			
			// 加工重新日结数据
			for(Map<String, Object> item : itemSettlement) {
				// 商品日结初值
				String itemId = MapUtil.getString(item, "item_id"); // item_id可以重用
				String itemBar = MapUtil.getString(item, "item_bar");
				String itemName = MapUtil.getString(item, "item_name");
				String itemKindId = MapUtil.getString(item, "item_kind_id");
				String unitName = MapUtil.getString(item, "unit_name");
				BigDecimal cost = MapUtil.getBigDecimal(item, "cost");
				BigDecimal pri1 = MapUtil.getBigDecimal(item, "pri1");
				BigDecimal pri2 = MapUtil.getBigDecimal(item, "pri2");
				BigDecimal pri4 = MapUtil.getBigDecimal(item, "pri4");
				BigDecimal discount = MapUtil.getBigDecimal(item, "discount");
				boolean isSaleOnline = "04".equals(MapUtil.getString(item, "sale_type")); // 用销售类型来判断是线上还是线下销售,04:线上;03：线下
				boolean isReturnOnline = "04".equals(MapUtil.getString(item, "return_type")); // 用退货类型来判断是线上还是线下
				String saleId = MapUtil.getString(item, "sale_id");
				String returnId = MapUtil.getString(item, "return_id");
				String purchId = MapUtil.getString(item, "purch_id");
				String turnId = MapUtil.getString(item, "turn_id");
				BigDecimal itemProfitAmount = BigDecimal.ZERO;
				BigDecimal itemProfitAmountOnline = BigDecimal.ZERO;
				BigDecimal itemProfitAmountOffline = BigDecimal.ZERO;
				BigDecimal itemLossAmount = BigDecimal.ZERO;
				BigDecimal itemLossAmountOnline = BigDecimal.ZERO;
				BigDecimal itemLossAmountOffline = BigDecimal.ZERO;
				BigDecimal itemSaleAmount = BigDecimal.ZERO;
				BigDecimal itemSaleAmountOnline = BigDecimal.ZERO;
				BigDecimal itemSaleAmountOffline = BigDecimal.ZERO;
				BigDecimal itemSaleQuantity = BigDecimal.ZERO;
				BigDecimal itemSaleQuantityOnline = BigDecimal.ZERO;
				BigDecimal itemSaleQuantityOffline = BigDecimal.ZERO;
				BigDecimal itemReturnAmount = BigDecimal.ZERO;
				BigDecimal itemReturnAmountOnline = BigDecimal.ZERO;
				BigDecimal itemReturnAmountOffline = BigDecimal.ZERO;
				BigDecimal itemReturnQuantity = BigDecimal.ZERO;
				BigDecimal itemReturnQuantityOnline = BigDecimal.ZERO;
				BigDecimal itemReturnQuantityOffline = BigDecimal.ZERO;
				BigDecimal itemPurchAmount = BigDecimal.ZERO;
				BigDecimal itemPurchQuantity = BigDecimal.ZERO;
				BigDecimal itemWhseTurnPLAmount = BigDecimal.ZERO;
				BigDecimal itemWhseTurnPLQuantity = BigDecimal.ZERO;
				BigDecimal itemWhseWarnQuantity = MapUtil.getBigDecimal(item, "whse_warn_quantity");//合理库存
				// itemWhseAmount = itemWhseQuantity * cost
				// 处理商品日结
				Map<String, Object> itemResult = MapUtil.get(itemResultMap, itemId, new HashMap<String, Object>());
				if(!itemResult.isEmpty()) { // 如果已有此商品则取值
					itemProfitAmount = MapUtil.getBigDecimal(itemResult, "profit_amount");
					itemProfitAmountOnline = MapUtil.getBigDecimal(itemResult, "profit_amount_online");
					itemProfitAmountOffline = MapUtil.getBigDecimal(itemResult, "profit_amount_offline");
					itemLossAmount = MapUtil.getBigDecimal(itemResult, "loss_amount");
					itemLossAmountOnline = MapUtil.getBigDecimal(itemResult, "loss_amount_online");
					itemLossAmountOffline = MapUtil.getBigDecimal(itemResult, "loss_amount_offline");
					itemSaleAmount = MapUtil.getBigDecimal(itemResult, "sale_amount");
					itemSaleAmountOnline = MapUtil.getBigDecimal(itemResult, "sale_amount_online");
					itemSaleAmountOffline = MapUtil.getBigDecimal(itemResult, "sale_amount_offline");
					itemSaleQuantity = MapUtil.getBigDecimal(itemResult, "sale_quantity");
					itemSaleQuantityOnline = MapUtil.getBigDecimal(itemResult, "sale_quantity_online");
					itemSaleQuantityOffline = MapUtil.getBigDecimal(itemResult, "sale_quantity_offline");
					itemReturnAmount = MapUtil.getBigDecimal(itemResult, "return_amount");
					itemReturnAmountOnline = MapUtil.getBigDecimal(itemResult, "return_amount_online");
					itemReturnAmountOffline = MapUtil.getBigDecimal(itemResult, "return_amount_offline");
					itemReturnQuantity = MapUtil.getBigDecimal(itemResult, "return_quantity");
					itemReturnQuantityOnline = MapUtil.getBigDecimal(itemResult, "return_quantity_online");
					itemReturnQuantityOffline = MapUtil.getBigDecimal(itemResult, "return_quantity_offline");
					itemPurchAmount = MapUtil.getBigDecimal(itemResult, "purch_amount");
					itemPurchQuantity = MapUtil.getBigDecimal(itemResult, "purch_quantity");
					itemWhseTurnPLAmount = MapUtil.getBigDecimal(itemResult, "whse_turn_pl_amount");
					itemWhseTurnPLQuantity = MapUtil.getBigDecimal(itemResult, "whse_turn_pl_quantity");
				} else { // 如果没有此商品则放入属性
					itemResult.put("settlement_id", IDUtil.getId());
					itemResult.put("merch_id", merchId);
					itemResult.put("settlement_date", startDate);
					itemResult.put("item_id", itemId);
					itemResult.put("item_bar", itemBar);
					itemResult.put("item_name", itemName);
					itemResult.put("item_kind_id", itemKindId);
					itemResult.put("unit_name", unitName);
					itemResult.put("cost", cost);
					itemResult.put("pri1", pri1);
					itemResult.put("pri2", pri2);
					itemResult.put("pri4", pri4);
					itemResult.put("discount", discount);
					if(isToday) { // 第一天的库存不用计算进销盘退, 就是whse_merch.qty_whse的值
						itemResult.put("whse_quantity", MapUtil.getBigDecimal(item, "whse_quantity"));
						itemResult.put("whse_amount", MapUtil.getBigDecimal(item, "whse_quantity").multiply(cost));//库存额=库存量*成本价
					}
					itemResult.put("whse_warn_quantity", itemWhseWarnQuantity);//合理库存
				}
				// 放入商品日结数据, 存储数据 = 原数据 + 本行数据
				// 1. 销售
				if(!StringUtil.isBlank(saleId) && !itemUsedOrderMap.containsKey(saleId+","+itemId)) {
					itemProfitAmount = itemProfitAmount.add(MapUtil.getBigDecimal(item, "item_profit"));
					itemSaleAmount = itemSaleAmount.add(MapUtil.getBigDecimal(item, "item_sale_amount"));
					itemSaleQuantity = itemSaleQuantity.add(MapUtil.getBigDecimal(item, "item_sale_quantity"));
					if(isSaleOnline) {
						itemProfitAmountOnline = itemProfitAmountOnline.add(MapUtil.getBigDecimal(item, "item_profit"));
						itemSaleAmountOnline = itemSaleAmountOnline.add(MapUtil.getBigDecimal(item, "item_sale_amount"));
						itemSaleQuantityOnline = itemSaleQuantityOnline.add(MapUtil.getBigDecimal(item, "item_sale_quantity"));
					} else {
						itemProfitAmountOffline = itemProfitAmountOffline.add(MapUtil.getBigDecimal(item, "item_profit"));
						itemSaleAmountOffline = itemSaleAmountOffline.add(MapUtil.getBigDecimal(item, "item_sale_amount"));
						itemSaleQuantityOffline = itemSaleQuantityOffline.add(MapUtil.getBigDecimal(item, "item_sale_quantity"));
					}
					// 标记为处理过
					itemUsedOrderMap.put(saleId+","+itemId, null);
				}
				// 2. 退货
				if(!StringUtil.isBlank(returnId) && !itemUsedOrderMap.containsKey(returnId+","+itemId)) {
					itemLossAmount = itemLossAmount.add(MapUtil.getBigDecimal(item, "item_loss"));
					itemReturnAmount = itemReturnAmount.add(MapUtil.getBigDecimal(item, "item_return_amount"));
					itemReturnQuantity = itemReturnQuantity.add(MapUtil.getBigDecimal(item, "item_return_quantity"));
					if(isReturnOnline) {
						itemReturnAmountOnline = itemReturnAmountOnline.add(MapUtil.getBigDecimal(item, "item_return_amount"));
						itemReturnQuantityOnline = itemReturnQuantityOnline.add(MapUtil.getBigDecimal(item, "item_return_quantity"));
						itemLossAmountOnline = itemLossAmountOnline.add(MapUtil.getBigDecimal(item, "item_loss"));
					} else {
						itemLossAmountOffline = itemLossAmountOffline.add(MapUtil.getBigDecimal(item, "item_loss"));
						itemReturnAmountOffline = itemReturnAmountOffline.add(MapUtil.getBigDecimal(item, "item_return_amount"));
						itemReturnQuantityOffline = itemReturnQuantityOffline.add(MapUtil.getBigDecimal(item, "item_return_quantity"));
					}
					// 标记为处理过
					itemUsedOrderMap.put(returnId+","+itemId, null);
				}
				// 3. 采购
				if(!StringUtil.isBlank(purchId) && !itemUsedOrderMap.containsKey(purchId+","+itemId)) {
					itemPurchAmount = itemPurchAmount.add(MapUtil.getBigDecimal(item, "item_purch_amount"));
					itemPurchQuantity = itemPurchQuantity.add(MapUtil.getBigDecimal(item, "item_purch_quantity"));
					purchCount++;
					// 标记为处理过
					itemUsedOrderMap.put(purchId+","+itemId, null);
				}
				// 4. 盘点
				if(!StringUtil.isBlank(turnId) && !itemUsedOrderMap.containsKey(turnId+","+itemId)) {
					itemWhseTurnPLAmount = itemWhseTurnPLAmount.add(MapUtil.getBigDecimal(item, "item_turn_amount"));
					itemWhseTurnPLQuantity = itemWhseTurnPLQuantity.add(MapUtil.getBigDecimal(item, "item_turn_quantity"));
					whseTurnCount++;
					// 标记为处理过
					itemUsedOrderMap.put(turnId+","+itemId, null);
				}
				// 5. 库存??
				if(!isToday) { // 其余各天的库存需要计算进销盘退
					if(!itemResultMap.containsKey(itemId)) { // 如果无此商品则加库存
						itemResult.put("whse_quantity", MapUtil.getBigDecimal(item, "whse_quantity").subtract(MapUtil.getBigDecimal(item, "whse_adjusted_quantity")));
						itemResult.put("whse_amount", MapUtil.getBigDecimal(item, "whse_quantity").subtract(MapUtil.getBigDecimal(item, "whse_adjusted_quantity")).multiply(cost)); // 库存额=(库存量+调整量)*成本价
					}
				}
				// 放入商品日结数据
				itemResult.put("profit_amount", itemProfitAmount);
				itemResult.put("sale_amount", itemSaleAmount);
				itemResult.put("sale_quantity", itemSaleQuantity);
				itemResult.put("profit_amount_online", itemProfitAmountOnline);
				itemResult.put("sale_amount_online", itemSaleAmountOnline);
				itemResult.put("sale_quantity_online", itemSaleQuantityOnline);
				itemResult.put("profit_amount_offline", itemProfitAmountOffline);
				itemResult.put("sale_amount_offline", itemSaleAmountOffline);
				itemResult.put("sale_quantity_offline", itemSaleQuantityOffline);
				itemResult.put("loss_amount", itemLossAmount);
				itemResult.put("return_amount", itemReturnAmount);
				itemResult.put("return_quantity", itemReturnQuantity);
				itemResult.put("return_amount_online", itemReturnAmountOnline);
				itemResult.put("return_quantity_online", itemReturnQuantityOnline);
				itemResult.put("loss_amount_online", itemLossAmountOnline);
				itemResult.put("loss_amount_offline", itemLossAmountOffline);
				itemResult.put("return_amount_offline", itemReturnAmountOffline);
				itemResult.put("return_quantity_offline", itemReturnQuantityOffline);
				itemResult.put("purch_amount", itemPurchAmount);
				itemResult.put("purch_quantity", itemPurchQuantity);
				itemResult.put("whse_turn_pl_amount", itemWhseTurnPLAmount);
				itemResult.put("whse_turn_pl_quantity", itemWhseTurnPLQuantity);
				
				
				
				// 在循环的末尾保存处理后的商品
				// 处理商户日结, 只处理没有处理过的进销盘退单
				// 1. 销售
				if(!StringUtil.isBlank(saleId) && !usedOrderMap.containsKey(saleId)) {
					profitAmount = profitAmount.add(MapUtil.getBigDecimal(item, "sale_profit"));
					saleAmount = saleAmount.add(MapUtil.getBigDecimal(item, "sale_amount"));
					saleLoss = saleLoss.add(MapUtil.getBigDecimal(item, "sale_loss"));
					saleCount++;
					if(isSaleOnline) {
						profitAmountOnline = profitAmountOnline.add(MapUtil.getBigDecimal(item, "sale_profit"));
						saleAmountOnline = saleAmountOnline.add(MapUtil.getBigDecimal(item, "sale_amount"));
						saleLossOnline = saleLossOnline.add(MapUtil.getBigDecimal(item, "sale_loss"));
						saleCountOnline++;
					} else {
						profitAmountOffline = profitAmountOffline.add(MapUtil.getBigDecimal(item, "sale_profit"));
						saleAmountOffline = saleAmountOffline.add(MapUtil.getBigDecimal(item, "sale_amount"));
						saleLossOffline = saleLossOffline.add(MapUtil.getBigDecimal(item, "sale_loss"));
						saleCountOffline++;
					}
					// 标记为处理过
					usedOrderMap.put(saleId, null);
				}
				// 2. 退货
				if(!StringUtil.isBlank(returnId) && !usedOrderMap.containsKey(returnId)) {
					lossAmount = lossAmount.add(MapUtil.getBigDecimal(item, "return_loss"));
					returnAmount = returnAmount.add(MapUtil.getBigDecimal(item, "return_amount"));
					if(isReturnOnline) {
						lossAmountOnline = lossAmountOnline.add(MapUtil.getBigDecimal(item, "return_loss"));
						returnAmountOnline = returnAmountOnline.add(MapUtil.getBigDecimal(item, "return_amount"));
					} else {
						lossAmountOffline = lossAmountOffline.add(MapUtil.getBigDecimal(item, "return_loss"));
						returnAmountOffline = returnAmountOffline.add(MapUtil.getBigDecimal(item, "return_amount"));
					}
					// 标记为处理过
					usedOrderMap.put(returnId, null);
				}
				// 3. 采购
				if(!StringUtil.isBlank(purchId) && !usedOrderMap.containsKey(purchId)) {
					purchAmount = purchAmount.add(MapUtil.getBigDecimal(item, "purch_amount"));
					purchQuantity = purchQuantity.add(MapUtil.getBigDecimal(item, "purch_quantity"));
					purchItemCount = purchItemCount.add(MapUtil.getBigDecimal(item, "purch_item_count"));
					purchCount++;
					// 标记为处理过
					usedOrderMap.put(purchId, null);
				}
				// 4. 盘点
				if(!StringUtil.isBlank(turnId) && !usedOrderMap.containsKey(turnId)) {
					whseTurnProfit = whseTurnProfit.add(MapUtil.getBigDecimal(item, "whse_turn_profit_amount"));
					whseTurnLoss = whseTurnLoss.add(MapUtil.getBigDecimal(item, "whse_turn_loss_amount"));
					whseTurnProfitQuantity = whseTurnProfitQuantity.add(MapUtil.getBigDecimal(item, "whse_turn_profit_quantity"));
					whseTurnLossQuantity = whseTurnLossQuantity.add(MapUtil.getBigDecimal(item, "whse_turn_loss_quantity"));
					whseTurnCount++;
					// 标记为处理过
					usedOrderMap.put(turnId, null);
				}
				// 5. 库存
				if(!itemResultMap.containsKey(itemId)) { // 如果无此商品则减库存
					whseQuantity = whseQuantity.add(MapUtil.getBigDecimal(itemResult, "whse_quantity"));
					whseAmount = whseAmount.add(MapUtil.getBigDecimal(itemResult, "whse_amount"));
				}
				// int whseItemCount 库存商品数与商品属性数相同
				// 处理商户库存时需要判断此商品是否曾被处理过, 所以先处理商户库存再保存处理后的商品
				itemResultMap.put(itemId, itemResult);
			} // 商品循环结束
			
			
			
			
			
			
			
			
			
			
			
			// 放入商户日结数据
			Map<String, Object> merchResult = new HashMap<String, Object>();
			merchResult.put("settlement_id", IDUtil.getId());
			merchResult.put("merch_id", merchId);
			merchResult.put("settlement_date", startDate);
			merchResult.put("profit_amount", profitAmount);
			merchResult.put("profit_amount_online", profitAmountOnline);
			merchResult.put("profit_amount_offline", profitAmountOffline);
			merchResult.put("sale_amount", saleAmount);
			merchResult.put("sale_amount_online", saleAmountOnline);
			merchResult.put("sale_amount_offline", saleAmountOffline);
			merchResult.put("sale_loss", saleLoss);
			merchResult.put("sale_loss_online", saleLossOnline);
			merchResult.put("sale_loss_offline", saleLossOffline);
			merchResult.put("sale_count", saleCount);
			merchResult.put("sale_count_online", saleCountOnline);
			merchResult.put("sale_count_offline", saleCountOffline);
			merchResult.put("loss_amount", lossAmount);
			merchResult.put("loss_amount_online", lossAmountOnline);
			merchResult.put("loss_amount_offline", lossAmountOffline);
			merchResult.put("return_amount", returnAmount);
			merchResult.put("return_amount_online", returnAmountOnline);
			merchResult.put("return_amount_offline", returnAmountOffline);
			merchResult.put("purch_amount", purchAmount);
			merchResult.put("purch_quantity", purchQuantity);
			merchResult.put("purch_item_count", purchItemCount);
			merchResult.put("purch_count", purchCount);
			merchResult.put("whse_amount", whseAmount);
			merchResult.put("whse_quantity", whseQuantity);
			merchResult.put("whse_item_count", itemResultMap.size()); // 库存商品数与商品属性数相同
			merchResult.put("whse_turn_profit_amount", whseTurnProfit);
			merchResult.put("whse_turn_loss_amount", whseTurnLoss);
			merchResult.put("whse_turn_profit_quantity", whseTurnProfitQuantity);
			merchResult.put("whse_turn_loss_quantity", whseTurnLossQuantity);
			merchResult.put("whse_turn_count", whseTurnCount);
			// 清除临时商户日结表
			statisticsDao.cleanMerchTempSettlement(settlementParam);
			// 生成临时商户日结表
			statisticsDao.tempMerchDailySettlement(merchResult);
			// 备份商户日结表
			statisticsDao.backupMerchDailySettlement(settlementParam);
			// 更新商户日结表
			statisticsDao.refreshMerchDailySettlement(settlementParam);
			
			// 清除临时商品日结表
			statisticsDao.cleanMerchItemTempSettlement(settlementParam);
			// 生成临时商品日结表
			statisticsDao.tempMerchItemDailySettlement(new ArrayList(itemResultMap.values()));
			// 备份商品日结表
			statisticsDao.backupMerchItemDailySettlement(settlementParam);
			// 更新商品日结表
			statisticsDao.refreshMerchItemDailySettlement(settlementParam);
			// 手工删除今天(第一天)的日结表
			// 下一天继续循环
			startDate = DateUtil.getPreviousDay(startDate, 1);
		}
		
		
		
	}
	
	/**
	 * 按照零售户进行进销存利等数据的结算, 如果传入结算日期, 则结算那天之前的数据 
	 */
	@Override
	public void createDailySettlement(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl createDailySettlement paramMap: " + paramMap);
//		List<Map<String, Object>> historyMapList = new ArrayList<Map<String, Object>>();
		// 改为查询base_merch表, 如果查询pub_user会因为多用户导致多个merch_id
		Map<String, Object> merchParam = new HashMap<String, Object>();
		merchParam.put("merch_id", paramMap.get("merch_id"));
		List<Map<String, Object>> merchList = baseDataDao.selectBaseMerch(merchParam);//查询商户信息
		String settlementDate = DateUtil.getToday();
		if(paramMap.get("settlement_date")!=null) {
			settlementDate = paramMap.get("settlement_date").toString();
		}
		settlementDate = DateUtil.getPreviousDay(settlementDate, 1);
		for(Map<String, Object> merchMap : merchList) {
			String merchId = (String) merchMap.get("merch_id");
			Map<String, Object> merchHistoryMap = new HashMap<String, Object>(); // 各商户历史信息
			merchHistoryMap.put("settlement_id", IDUtil.getId());
			merchHistoryMap.put("settlement_date", settlementDate);
			merchHistoryMap.put("merch_id", merchId);
			Map<String, Map<String, Object>> itemHistoryMap = new HashMap<String, Map<String, Object>>(); // 商户商品历史信息
			Map<String, Object> merchItemParam = new HashMap<String, Object>();
			merchItemParam.put("merch_id", merchId);
			merchItemParam.put("page_index", -1);
			merchItemParam.put("page_size", -1);
			List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(merchItemParam);
			for(Map<String, Object> merchItemMap : merchItemList) {
				String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
				Map<String, Object> itemHistory = new HashMap<String, Object>();
				itemHistory.put("settlement_id", IDUtil.getId());
				itemHistory.put("settlement_date", settlementDate);
				itemHistory.put("merch_id", merchId);
				itemHistory.put("item_id", merchItemMap.get("item_id"));
				itemHistory.put("item_bar", merchItemMap.get("item_bar"));
				itemHistory.put("item_name", merchItemMap.get("item_name"));
				itemHistory.put("item_kind_id", merchItemMap.get("item_kind_id"));
				itemHistory.put("unit_name", merchItemMap.get("unit_name"));
				itemHistory.put("cost", merchItemMap.get("cost"));
				itemHistory.put("pri1", merchItemMap.get("pri1"));
				itemHistory.put("pri2", merchItemMap.get("pri2"));
				itemHistory.put("pri4", merchItemMap.get("pri4"));
				itemHistory.put("discount", merchItemMap.get("discount"));
				itemHistory.put("status", MapUtil.getString(merchItemMap, "status", "1"));
				itemHistoryMap.put(itemHistoryIdentity, itemHistory);
			}
			// = * = * = * = * = 销售单 = * = * = * = * = 
			saleDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
			// = * = * = * = * = 退货 = * = * = * = * = 
			returnDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
			// = * = * = * = * = 进货 = * = * = * = * = 
			purchDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
			// = * = * = * = * = 库存 = * = * = * = * = 
			whseDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
			// = * = * = * = * = 盘点 = * = * = * = * = 
			turnDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
//			historyMapList.add(merchHistoryMap);
			// insert
			statisticsDao.insertMerchDailySettlement(merchHistoryMap);
			statisticsDao.insertMerchItemDailySettlement(itemHistoryMap.values());
		}
	}
	
	@Override
	public void autoWarehousingMerchTobaccoOrder(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl autoWarehousingMerchTobaccoOrder paramMap: " + paramMap);
//		paramMap.put("is_warehousing", true);
		try {
			String warehousingDate = statisticsDao.selectWarehousingDate();
			String today = DateUtil.getToday();
			if(warehousingDate!=null && Integer.valueOf(warehousingDate)>=Integer.valueOf(today)) {
				LOG.debug("当前日期:"+today+", 入库日期:"+warehousingDate+", 跳过入库步骤");
			} else {
				LOG.debug("当前日期:"+today+", 入库日期:"+warehousingDate+", 入库开始时间:"+DateUtil.getCurrentTime());
			}
			long lastMilliTime = DateUtil.getCurrentTimeMillis();
			while(warehousingDate==null || Integer.valueOf(warehousingDate)<Integer.valueOf(today)) {
				warehousingDate = warehousingDate==null ? today : DateUtil.getNextDay(warehousingDate);
				Map<String, Object> warehousingParam = new HashMap<String, Object>();
				warehousingParam.put("warehousing_date", warehousingDate);
				if(paramMap.get("lice_id")!=null) {
					warehousingParam.put("lice_id", paramMap.get("lice_id"));
					LOG.debug("当前入库日期:"+warehousingDate+", 入库以下商户:"+paramMap.get("lice_id"));
				} else {
					// 在日结中增加自动卷烟入库的逻辑, 只自动入库试点12户	2014年9月3日	2014年10月21日
					// 现在日结中的自动入库不在限制商户
//					warehousingParam.put("lice_id", "370102100764,370112205547,370112208275,370112107111,370102209522,370102106350,370102109510,370102210912,370102206266,370102110837,370102107633,370112109801");
//					LOG.debug("当前入库日期:"+warehousingDate+", 入库试点12户");
					LOG.debug("当前入库日期:"+warehousingDate+", 入库所有商户");
				}
				warehousingMerchTobaccoOrder(warehousingParam);
				LOG.debug("入库日期:"+warehousingDate+", 入库结束时间:"+DateUtil.getCurrentTime()+", 用时:"+(DateUtil.getCurrentTimeMillis()-lastMilliTime));
			}
			if(Integer.valueOf(warehousingDate)>=Integer.valueOf(today)) {
				statisticsDao.updateWarehousingDate(warehousingDate);
				LOG.debug("更新入库日期:"+warehousingDate);
			}
		} catch (Exception e) {
			LOG.error("自动入库卷烟订单错误", e);
		}/* finally {
			paramMap.put("is_warehousing", false);
		}*/
	}
	
	public void warehousingMerchTobaccoOrder(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl warehousingMerchTobaccoOrder paramMap: " + paramMap);
		Map<String, Object> merchParam = new HashMap<String, Object>();
		merchParam.put("lice_id", paramMap.get("lice_id"));
		List<Map<String, Object>> merchList = baseDataDao.selectBaseMerch(merchParam);
		// 日结昨天数据, 入库前天订单
		String warehousingDate = MapUtil.getString(paramMap, "warehousing_date", DateUtil.getToday());
		for(Map<String, Object> merch : merchList) {
			List<Map<String, Object>> temporaryOrderList = orderDao.selectTemporaryMerchOrder(MapUtil.rename(merch, "merch_id"));
			if(!temporaryOrderList.isEmpty()) {
				String orderDate = MapUtil.getString(temporaryOrderList.get(0), "orderdate");
				// 如果日结日期等于卷烟订单提交日期+3, 就进行自动入库
				LOG.debug("当前入库日期: " + warehousingDate + " 订单日期: " + orderDate);
				if(warehousingDate.equals(DateUtil.getNextDay(orderDate, 3))) {
					Map<String, String> map = new HashMap<String, String>();
					for(Entry<String, Object> merchEntry : merch.entrySet()) {
						String key = merchEntry.getKey();
						Object value = merchEntry.getValue();
						if(value!=null) {
							if("merch_id".equalsIgnoreCase(key)) {
								map.put("custId", value.toString());
								map.put("refId", value.toString());
							}
							if("cgt_com_id".equalsIgnoreCase(key)) map.put("comId", value.toString());
							map.put(key, value.toString());
						}
					}
					map.put("beginDate", orderDate);
					map.put("endDate", orderDate);
					try {
						cgtOrderService.getOrderList(map);
					} catch (Exception e) {
						LOG.error("商户" + MapUtil.getString(map, "merch_id") + " 自动入库失败, 订单日期: "+ orderDate, e);
					}
				}
			}
		}
	}
	
	@Override
	public Map<String, Object> searchMerchCurrentSettlement(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchMerchCurrentSettlement paramMap: " + paramMap);
		Map<String, Object> merchHistoryMap = new HashMap<String, Object>();
		Map<String, Map<String, Object>> itemHistoryMap = new HashMap<String, Map<String, Object>>();
		String merchId = (String) paramMap.get("merch_id");
		String settlementDate = DateUtil.getToday();
		
		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", merchId);
		merchItemParam.put("page_index", -1);
		merchItemParam.put("page_size", -1);
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(merchItemParam);
		for(Map<String, Object> merchItemMap : merchItemList) {
			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			Map<String, Object> itemHistory = new HashMap<String, Object>();
			itemHistory.put("settlement_id", IDUtil.getId());
			itemHistory.put("settlement_date", settlementDate);
			itemHistory.put("merch_id", merchId);
			itemHistory.put("item_id", merchItemMap.get("item_id"));
			itemHistory.put("item_bar", merchItemMap.get("item_bar"));
			itemHistory.put("item_name", merchItemMap.get("item_name"));
			itemHistory.put("item_kind_id", merchItemMap.get("item_kind_id"));
			itemHistory.put("unit_name", merchItemMap.get("unit_name"));
			itemHistory.put("cost", merchItemMap.get("cost"));
			itemHistory.put("pri1", merchItemMap.get("pri1"));
			itemHistory.put("pri2", merchItemMap.get("pri2"));
			itemHistory.put("pri4", merchItemMap.get("pri4"));
			itemHistory.put("discount", merchItemMap.get("discount"));
			itemHistoryMap.put(itemHistoryIdentity, itemHistory);
		}
		saleDailySettlement(merchId, settlementDate, merchHistoryMap, new HashMap<String, Map<String, Object>>());//销售
		returnDailySettlement(merchId, settlementDate, merchHistoryMap, new HashMap<String, Map<String, Object>>());//退货
		purchDailySettlement(merchId, settlementDate, merchHistoryMap, new HashMap<String, Map<String, Object>>());//入库
		whseDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
		turnDailySettlement(merchId, settlementDate, merchHistoryMap, new HashMap<String, Map<String, Object>>());//盘点
		merchHistoryMap.put("merch_id", merchId);
		merchHistoryMap.put("settlement_date", settlementDate);
		return merchHistoryMap;
	}
	
	@Override
	public Map<String, Object> searchMerchItemCurrentSettlement(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchMerchItemCurrentSettlement paramMap: " + paramMap);
		Map<String, Object> merchHistoryMap = new HashMap<String, Object>();
		//卷烟和非卷烟利润
		Map<String, Map<String, Object>> itemHistoryMap = new HashMap<String, Map<String, Object>>();
		String merchId = (String) paramMap.get("merch_id");
		String settlementDate = DateUtil.getToday();

		Map<String, Object> merchItemParam = new HashMap<String, Object>();
		merchItemParam.put("merch_id", merchId);
		merchItemParam.put("status", "1,2");
		merchItemParam.put("page_index", -1);
		merchItemParam.put("page_size", -1);
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(merchItemParam); // 商户商品
		for(Map<String, Object> merchItemMap : merchItemList) {
			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			Map<String, Object> itemHistory = new HashMap<String, Object>();
			itemHistory.put("settlement_id", IDUtil.getId());
			itemHistory.put("settlement_date", settlementDate);
			itemHistory.put("merch_id", merchId);
			itemHistory.put("item_id", merchItemMap.get("item_id"));
			itemHistory.put("item_bar", merchItemMap.get("item_bar"));
			itemHistory.put("item_name", merchItemMap.get("item_name"));
			itemHistory.put("item_kind_id", merchItemMap.get("item_kind_id"));
			itemHistory.put("unit_name", merchItemMap.get("unit_name"));
			itemHistory.put("cost", merchItemMap.get("cost"));
			itemHistory.put("pri1", merchItemMap.get("pri1"));
			itemHistory.put("pri2", merchItemMap.get("pri2"));
			itemHistory.put("pri4", merchItemMap.get("pri4"));
			itemHistory.put("discount", merchItemMap.get("discount"));
			itemHistoryMap.put(itemHistoryIdentity, itemHistory);
		}
		
		
		
		saleDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
		returnDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
		purchDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
		whseDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
		turnDailySettlement(merchId, settlementDate, merchHistoryMap, itemHistoryMap);
		Map<String, Object> currentSettlement = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> itemHistory : itemHistoryMap.values()) {
			list.add(itemHistory);
		}
		currentSettlement.put("merch_id", merchId);
		currentSettlement.put("settlement_date", settlementDate);
		currentSettlement.put("list", list);
		/*BigDecimal profitCigaretteProfit=(BigDecimal)merchHistoryMap.get("profit_cigarette_amount_offline");
		BigDecimal profitNoncigaretteProfit=(BigDecimal)merchHistoryMap.get("profit_noncigarette_amount_offline");
		BigDecimal whseCigaretteProfit=(BigDecimal)merchHistoryMap.get("whse_cigarette_amount_offline");
		BigDecimal whseNoncigaretteProfit=(BigDecimal)merchHistoryMap.get("whse_noncigarette_amount_offline");
		currentSettlement.put("profit_cigarette_amount_offline",profitCigaretteProfit ); //卷烟利润 
		currentSettlement.put("profit_noncigarette_amount_offline",profitNoncigaretteProfit ); //卷烟利润 
		currentSettlement.put("whse_cigarette_amount_offline",whseCigaretteProfit ); //卷烟利润 
		currentSettlement.put("whse_noncigarette_amount_offline",whseNoncigaretteProfit ); //卷烟利润 
*/		return currentSettlement;
	}

	@Override
	public Map<String, Object> searchMerchSettlement(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchDailySettlement paramMap: " + paramMap);
		Map<String,Object> returnMap=new HashMap<String, Object>();
		if(paramMap.get("day")!=null) {
			Map<String, Object> settlementParam = new HashMap<String, Object>();
			settlementParam.put("merch_id", paramMap.get("merch_id"));
			settlementParam.put("settlement_date", paramMap.get("day"));
			List<Map<String, Object>> settlementList = statisticsDao.selectMerchDailySettlement(settlementParam);
			if(settlementList.isEmpty()) {
				return new HashMap<String, Object>();
			} else {
				 returnMap.putAll(settlementList.get(0));
			}
		} else if(paramMap.get("month")!=null) {
			Map<String, Object> settlementParam = new HashMap<String, Object>();
			settlementParam.put("merch_id", paramMap.get("merch_id"));
			settlementParam.put("settlement_date_floor", paramMap.get("month")+"01");
			settlementParam.put("settlement_date_ceiling", paramMap.get("month")+"31");
			List<Map<String, Object>> settlementList = statisticsDao.selectMerchDailySettlement(settlementParam);
			if(settlementList.isEmpty()) {
				return new HashMap<String, Object>();
			} else {
				Map<String, Object> settlementResult = new HashMap<String, Object>();
				for(Map<String, Object> settlementMap : settlementList) {
					for(String key : settlementMap.keySet()) {
						if("settlement_id".equals(key) || "merch_id".equals(key) || "settlement_date".equals(key)) {
							continue;
						} else {
							if(settlementResult.get(key)!=null) {
								settlementResult.put(key, new BigDecimal(settlementResult.get(key).toString()).add(new BigDecimal(settlementMap.get(key).toString())));
							} else {
								settlementResult.put(key, new BigDecimal(settlementMap.get(key).toString()));
							}
						}
					}
				}
				returnMap.putAll( settlementResult);
			}
		}
		BigDecimal profitAmount=BigDecimal.ZERO;//总利润
		BigDecimal lossAmount=BigDecimal.ZERO;//退货利润
		BigDecimal saleAmount=BigDecimal.ZERO;
		BigDecimal returnAmount=BigDecimal.ZERO;
		BigDecimal profitAmountOnline=BigDecimal.ZERO;
		BigDecimal lossAmountOnline=BigDecimal.ZERO;//
		BigDecimal saleAmountOnline=BigDecimal.ZERO;//
		BigDecimal returnAmountOnline=BigDecimal.ZERO;
		BigDecimal saleAmountOffline=BigDecimal.ZERO;//
		BigDecimal returnAmountOffline=BigDecimal.ZERO;
		BigDecimal profitAmountOffline=BigDecimal.ZERO;//
		BigDecimal lossAmountOffline=BigDecimal.ZERO;///
		
		if(returnMap.get("PROFIT_AMOUNT")!=null) profitAmount=new BigDecimal(returnMap.get("PROFIT_AMOUNT")+"");
		if(returnMap.get("LOSS_AMOUNT")!=null) lossAmount=new BigDecimal(returnMap.get("LOSS_AMOUNT")+"");
		if(returnMap.get("SALE_AMOUNT")!=null) saleAmount=new BigDecimal(returnMap.get("SALE_AMOUNT")+"");
		if(returnMap.get("RETURN_AMOUNT")!=null) returnAmount=new BigDecimal(returnMap.get("RETURN_AMOUNT")+"");
		if(returnMap.get("PROFIT_AMOUNT_ONLINE")!=null) profitAmountOnline=new BigDecimal(returnMap.get("PROFIT_AMOUNT_ONLINE")+"");
		if(returnMap.get("LOSS_AMOUNT_ONLINE")!=null) lossAmountOnline=new BigDecimal(returnMap.get("LOSS_AMOUNT_ONLINE")+"");
		if(returnMap.get("SALE_AMOUNT_ONLINE")!=null) saleAmountOnline=new BigDecimal(returnMap.get("SALE_AMOUNT_ONLINE")+"");
		if(returnMap.get("RETURN_AMOUNT_ONLINE")!=null) returnAmountOnline=new BigDecimal(returnMap.get("RETURN_AMOUNT_ONLINE")+"");
		if(returnMap.get("SALE_AMOUNT_OFFLINE")!=null) saleAmountOffline=new BigDecimal(returnMap.get("SALE_AMOUNT_OFFLINE")+"");
		if(returnMap.get("RETURN_AMOUNT_OFFLINE")!=null) returnAmountOffline=new BigDecimal(returnMap.get("RETURN_AMOUNT_OFFLINE")+"");
		if(returnMap.get("PROFIT_AMOUNT_OFFLINE")!=null) profitAmountOffline=new BigDecimal(returnMap.get("PROFIT_AMOUNT_OFFLINE")+"");
		if(returnMap.get("LOSS_AMOUNT_OFFLINE")!=null) lossAmountOffline=new BigDecimal(returnMap.get("LOSS_AMOUNT_OFFLINE")+"");
		profitAmount=profitAmount.subtract(new BigDecimal("0").subtract( lossAmount));//利润
		profitAmountOnline=profitAmountOnline.subtract(new BigDecimal("0").subtract(lossAmountOnline));//网上利润总额
		profitAmountOffline=profitAmountOffline.subtract(new BigDecimal("0").subtract(lossAmountOffline));//线下利润总额
		saleAmount=saleAmount.subtract(returnAmount);//销售额
		saleAmountOnline=saleAmountOnline.subtract(returnAmountOnline);//网上销售额
		saleAmountOffline=saleAmountOffline.subtract(returnAmountOffline);//线下销售额		
		returnMap.put("PROFIT_AMOUNT", profitAmount);
		returnMap.put("PROFIT_AMOUNT_ONLINE", profitAmountOnline);
		returnMap.put("PROFIT_AMOUNT_OFFLINE", profitAmountOffline);
		returnMap.put("SALE_AMOUNT", saleAmount);
		returnMap.put("SALE_AMOUNT_ONLINE", saleAmountOnline);
		returnMap.put("SALE_AMOUNT_OFFLINE",saleAmountOffline );
		return returnMap;
	}
	
	@Override
	public List<Map<String, Object>> searchMerchMonthlyGrossMargin(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchMerchMonthlyGrossMargin paramMap: " + paramMap);
		
		String month = DateUtil.getCurrentTimeMillisAsString("yyyyMM");
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String oldMonth = MapUtil.getString(paramMap, "start_month", month);
		String newMonth = MapUtil.getString(paramMap, "end_month", month);
		
		Map<String, Object> midsParam = new HashMap<String, Object>();
		midsParam.put("settlement_date_floor", oldMonth+"01");//开始日期
		midsParam.put("settlement_date_ceiling", newMonth+"31");//结束日期
		midsParam.put("merch_id", merchId);
		
		List<String> monthList = DateUtil.getListBetweenStartingAndEnding(oldMonth,newMonth);//得到开始、结束日期
		List<Map<String, Object>> midsList = statisticsDao.searchSumMerchItemDailySettlement(midsParam);
		Map<String, Map<String, Object>> midsMap = new HashMap<String, Map<String,Object>>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> m = null;
		
		for (Map<String, Object> map : midsList) {
			month = MapUtil.getString(map, "settlement_date");
			if(midsMap.containsKey(month)){
				midsMap.get(month).put("gross_margin", (MapUtil.getBigDecimal(midsMap.get(month), "gross_margin").add(MapUtil.getBigDecimal(map, "sum_profit_amount"))).setScale(2, BigDecimal.ROUND_HALF_UP));
			}else{
				m = new HashMap<String, Object>();
				m.put("order_date", month);
				m.put("gross_margin", (MapUtil.getBigDecimal(map, "sum_profit_amount")).setScale(2, BigDecimal.ROUND_HALF_UP));
				midsMap.put(month, m);
			}
		}
		
		for (String monthStr : monthList) {
			if(midsMap.containsKey(monthStr)){
				data.add(midsMap.get(monthStr));
			}else{
				m = new HashMap<String, Object>();
				m.put("order_date", monthStr);
				m.put("gross_margin", (BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_UP));
				data.add(m);
			}
		}
		return data;
//		
//		
//		String today = DateUtil.getToday();
//		String merchId = paramMap.get("merch_id").toString();
//		String settlementDateCeiling = today;
//		String endMonth = settlementDateCeiling.substring(0, 6);
//		String settlementDateFloor = today;
//		String startMonth = settlementDateFloor.substring(0, 6);
//		if(paramMap.get("start_month")!=null) {
//			startMonth = paramMap.get("start_month").toString();
//			settlementDateFloor = startMonth+"01";
//		}
//		if(paramMap.get("end_month")!=null) {
//			endMonth = paramMap.get("end_month").toString();
//			settlementDateCeiling = endMonth+"31";
//		}
//		Map<String, Object> grossMarginParam = new HashMap<String, Object>();
//		grossMarginParam.put("merch_id", merchId);
//		grossMarginParam.put("settlement_date_ceiling", settlementDateCeiling);
//		grossMarginParam.put("settlement_date_floor", settlementDateFloor);
//		List<Map<String, Object>> settlementList = this.searchMerchMonthlySettlement(grossMarginParam); 
//		List<String> monthList = DateUtil.getListBetweenStartingAndEnding(startMonth,endMonth);
//		List<Map<String, Object>> newSettlementList = new ArrayList<Map<String, Object>>();
//		for(int i=0; i<monthList.size(); i++) {
//			String month = monthList.get(i);
//			Map<String, Object> settlementOriginalMap = new HashMap<String, Object>();
//			settlementOriginalMap.put("order_date", month);
//			// 如果有本月需要查今天
//			if(DateUtil.getToday().startsWith(month)) {
//				Map<String, Object> merchHistoryMap = new HashMap<String, Object>();
//				saleDailySettlement(merchId, today, merchHistoryMap, new HashMap<String, Map<String, Object>>());
//				returnDailySettlement(merchId, today, merchHistoryMap, new HashMap<String, Map<String,Object>>());
//				BigDecimal profitAmount=BigDecimal.ZERO;
////				BigDecimal lossAmount=BigDecimal.ZERO;
//				if(merchHistoryMap.get("profit_amount")!=null) profitAmount=new BigDecimal(merchHistoryMap.get("profit_amount")+"");
////				if(merchHistoryMap.get("loss_amount")!=null) lossAmount=new BigDecimal(merchHistoryMap.get("loss_amount")+"");
////				profitAmount = profitAmount.subtract(new BigDecimal("0").subtract(lossAmount));
//				settlementOriginalMap.put("gross_margin", profitAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
//			} else {
//				settlementOriginalMap.put("gross_margin", (BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_UP));
//			}
//			// 如果查询出本月结算数据
//			if(settlementList.size()!=0&&settlementList.size()>i) {
//				if(month.equals(settlementList.get(i).get("settlement_month"))) {
//					BigDecimal profitAmount = (BigDecimal) settlementList.get(i).get("profit_amount");
////					BigDecimal lossAmount=BigDecimal.ZERO;
////					if(settlementList.get(i).get("loss_amount")!=null) lossAmount=new BigDecimal(settlementList.get(i).get("loss_amount")+"");
////					profitAmount=profitAmount.subtract(new BigDecimal("0").subtract(lossAmount));
//					BigDecimal grossMargin = (BigDecimal) settlementOriginalMap.get("gross_margin");
//					settlementOriginalMap.put("gross_margin", (profitAmount.add(grossMargin)).setScale(2, BigDecimal.ROUND_HALF_UP));
//				} else {
//					settlementList.add(i, settlementOriginalMap);
//				}
//			} else {
//				settlementList.add(i, settlementOriginalMap);
//			}
//			newSettlementList.add(settlementOriginalMap);
//		}
//		return newSettlementList;
	}
	
	
	@Override
	public List<Map<String, Object>> searchMerchDailyGrossMargin(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchMerchDailyGrossMargin paramMap: " + paramMap);
		
		String day = DateUtil.getToday();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String oldDate = MapUtil.getString(paramMap, "start_date", DateUtil.getNextDay(day, -7));
		String newDate = MapUtil.getString(paramMap, "end_date", DateUtil.getNextDay(day, -1));
		
		Map<String, Object> midsParam = new HashMap<String, Object>();
		midsParam.put("settlement_date_floor", oldDate);//开始日期
		midsParam.put("settlement_date_ceiling", newDate);//结束日期
		midsParam.put("merch_id", merchId);
		midsParam.put("time_interval", "day");
		
		List<String> monthList = DateUtil.getListBetweenStartingAndEnding(oldDate, newDate);//得到开始、结束日期
		List<Map<String, Object>> midsList = statisticsDao.searchSumMerchItemDailySettlement(midsParam);
		Map<String, Map<String, Object>> midsMap = new HashMap<String, Map<String,Object>>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> m = null;
		
		for (Map<String, Object> map : midsList) {
			day = MapUtil.getString(map, "settlement_date");
			if(midsMap.containsKey(day)){
				midsMap.get(day).put("gross_margin", (MapUtil.getBigDecimal(midsMap.get(day), "gross_margin").add(MapUtil.getBigDecimal(map, "sum_profit_amount"))).setScale(2, BigDecimal.ROUND_HALF_UP));
			}else{
				m = new HashMap<String, Object>();
				m.put("order_date", day);
				m.put("gross_margin", (MapUtil.getBigDecimal(map, "sum_profit_amount")).setScale(2, BigDecimal.ROUND_HALF_UP));
				midsMap.put(day, m);
			}
		}
		
		for (String dayStr : monthList) {
			if(midsMap.containsKey(dayStr)){
				data.add(midsMap.get(dayStr));
			}else{
				m = new HashMap<String, Object>();
				m.put("order_date", dayStr);
				m.put("gross_margin", (BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_UP));
				data.add(m);
			}
		}
		return data;
		
		
//		String today = DateUtil.getToday();
//		String merchId = paramMap.get("merch_id").toString();
//		String settlementDateCeiling = today;
//		String settlementDateFloor = today;
//		if(paramMap.get("start_date")!=null) {
//			settlementDateFloor = paramMap.remove("start_date").toString();
//		}
//		paramMap.put("settlement_date_floor", settlementDateFloor);
//		if(paramMap.get("end_date")!=null) {
//			settlementDateCeiling = paramMap.remove("end_date").toString();
//		}
//		paramMap.put("settlement_date_ceiling", settlementDateCeiling);
////		List<Map<String, Object>> settlementList = statisticsDao.selectMerchDailySettlement(paramMap);
//		List<Map<String, Object>> settlementList = this.searchMerchDailySettlement(paramMap);
//		List<String> dayList = DateUtil.getListBetweenStartingAndEnding(settlementDateFloor, settlementDateCeiling);
//		List<Map<String, Object>> newSettlementList = new ArrayList<Map<String, Object>>();
//		for(int i=0; i<dayList.size(); i++) {
//			String day = dayList.get(i);
//			Map<String, Object> settlementOriginalMap = new HashMap<String, Object>();
//			settlementOriginalMap.put("order_date", day);
//			// 如果有本日需要单独查
//			if(today.equals(day)) {
//				Map<String, Object> merchHistoryMap = new HashMap<String, Object>();
//				saleDailySettlement(merchId, today, merchHistoryMap, new HashMap<String, Map<String, Object>>());
//				returnDailySettlement(merchId, today, merchHistoryMap, new HashMap<String, Map<String,Object>>());
//				BigDecimal profitAmount=new BigDecimal(merchHistoryMap.get("profit_amount")+"");
////				BigDecimal lossAmount=new BigDecimal(merchHistoryMap.get("loss_amount")+"");
////				profitAmount=profitAmount.subtract(new BigDecimal("0").subtract(lossAmount));
//				settlementOriginalMap.put("gross_margin", profitAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
//			} else {
//				settlementOriginalMap.put("gross_margin", (BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_UP));
//			}
//			if(settlementList.size()!=0&&settlementList.size()>i) {
//				if(day.equals(settlementList.get(i).get("settlement_date"))) {
//					BigDecimal profitAmount = (BigDecimal) settlementList.get(i).get("profit_amount");
////					BigDecimal lossAmount=(BigDecimal) settlementList.get(i).get("loss_amount");
//					BigDecimal grossMargin = (BigDecimal) settlementOriginalMap.get("gross_margin");
////					profitAmount=profitAmount.subtract(new BigDecimal("0").subtract(lossAmount));
//					settlementOriginalMap.put("gross_margin", (profitAmount.add(grossMargin)).setScale(2, BigDecimal.ROUND_HALF_UP));
//				} else {
//					settlementList.add(i, settlementOriginalMap);
//				}
//			} else {
//				settlementList.add(i, settlementOriginalMap);
//			}
//			newSettlementList.add(settlementOriginalMap);
//		}
//		return newSettlementList;
	}
	
	@Override
	public List<Map<String, Object>> searchMerchDailySettlement(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchMerchDailySettlement paramMap: " + paramMap);
		return statisticsDao.selectMerchDailySettlement(paramMap);
	}
	
	@Override
	public List<Map<String, Object>> searchMerchItemDailySettlement(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchMerchItemDailySettlement paramMap: " + paramMap);
		return statisticsDao.selectMerchItemDailySettlement(paramMap);
	}
	
	/**
	 * 获取日利润数据, 包含总利润, 卷烟利润, 非烟利润
	 */
	@Override
	public List<Map<String, Object>> searchMerchDailyProfit(Map<String, Object> profitMap) throws Exception{
		LOG.debug("StatisticsServiceImpl searchMerchDailyProfit profitMap: "+profitMap);
		int gapDays = Integer.valueOf(profitMap.get("number").toString());
		String endDay = DateUtil.getPreviousDay(DateUtil.getToday(), 1);
		String startDay = DateUtil.getPreviousDay(endDay, gapDays-1);
		List<String> dayList = DateUtil.getListBetweenStartingAndEnding(startDay, endDay); // 日期列表
		profitMap.put("settlement_date_floor", startDay); // 开始日期
		profitMap.put("settlement_date_ceiling", endDay); // 结束日期
		//数据
		List<Map<String, Object>> itemSettlementList = statisticsDao.searchSumMerchItemDailySettlement(profitMap);
		
		List<Map<String, Object>> merchDailyProfitList = new ArrayList<Map<String, Object>>();
		
		BigDecimal totalProfit = BigDecimal.ZERO;//总利润
		BigDecimal tobaccoProfit = BigDecimal.ZERO;//卷烟利润
		BigDecimal cigarProfit = BigDecimal.ZERO;//雪茄利润
		BigDecimal otherProfit = BigDecimal.ZERO;//非烟利润
		BigDecimal myProfit = BigDecimal.ZERO;//利润
		BigDecimal myLossAmount= BigDecimal.ZERO;//退货利润
		String settlementDate = null;
		String itemKindId = null;
		Map<String, Object> tempItemSettlementMap = null;
		for (String dateStr: dayList) {
			tempItemSettlementMap = new HashMap<String, Object>();
			totalProfit = BigDecimal.ZERO;//总利润
			tobaccoProfit = BigDecimal.ZERO;//卷烟利润
			cigarProfit = BigDecimal.ZERO;//雪茄利润
			otherProfit = BigDecimal.ZERO;//非烟利润
			myProfit = BigDecimal.ZERO;//利润
			for (Map<String, Object> map : itemSettlementList) {
				settlementDate = MapUtil.get(map, "settlement_date", null);
				itemKindId = MapUtil.get(map, "item_kind_id", "99");
				if(!dateStr.equals(settlementDate)){
					continue;
				}
				myProfit = MapUtil.getBigDecimal(map, "sum_profit_amount");
				myLossAmount = MapUtil.getBigDecimal(map, "sum_loss_amount");
				myProfit = myProfit.subtract(BigDecimal.ZERO.subtract(myLossAmount));
				totalProfit = totalProfit.add(myProfit);//总利润
				if(itemKindId.equals("01")){
					tobaccoProfit = tobaccoProfit.add(myProfit);//卷烟利润 
				}else if(itemKindId.equals("0102")) {
					cigarProfit = cigarProfit.add(myProfit);//卷烟利润 
				} else {
					otherProfit = otherProfit.add(myProfit);//非烟利润
				}
			}
			tempItemSettlementMap.put("settlement_date",dateStr);
			tempItemSettlementMap.put("total_profit", totalProfit);
			tempItemSettlementMap.put("tobacco_profit", tobaccoProfit);
			tempItemSettlementMap.put("cigar_profit", cigarProfit);
			tempItemSettlementMap.put("other_profit", otherProfit);
			merchDailyProfitList.add(tempItemSettlementMap);
		}
		
		return merchDailyProfitList;
//		Map<String, Map<String, Object>> itemSettlementListMap = new HashMap<String, Map<String, Object>>();
//		String settlementDate = null;
//		String itemKindId = null;
//		for(Map<String, Object> itemSettlementMap : itemSettlementList) {
//			settlementDate = itemSettlementMap.get("settlement_date").toString();//时间
//			
//			itemKindId = MapUtil.getString(itemSettlementMap, "item_kind_id", "99");
//			//总利润
//			BigDecimal profitAmount = new BigDecimal(itemSettlementMap.get("profit_amount").toString());
//			//退货利润
//			BigDecimal LossAmount = new BigDecimal(itemSettlementMap.get("loss_amount").toString());
//			
//			BigDecimal totalProfit = BigDecimal.ZERO;//总利润
//			BigDecimal tobaccoProfit = BigDecimal.ZERO;//卷烟利润
//			BigDecimal otherProfit = BigDecimal.ZERO;//非烟利润
//			Map<String, Object> tempItemSettlementMap = itemSettlementListMap.get(settlementDate);
//			if(tempItemSettlementMap!=null) {
//				totalProfit = (BigDecimal) tempItemSettlementMap.get("total_profit");
//				tobaccoProfit = (BigDecimal) tempItemSettlementMap.get("tobacco_profit");
//				otherProfit = (BigDecimal) tempItemSettlementMap.get("other_profit");
//			}
//			totalProfit = totalProfit.add(profitAmount).add(LossAmount);
//			if("01".equals(itemKindId)) {
//				tobaccoProfit = tobaccoProfit.add(profitAmount).add(LossAmount);
//			} else {
//				otherProfit = otherProfit.add(profitAmount).add(LossAmount);
//			}
////			if(tempItemSettlementMap==null) {
//				tempItemSettlementMap = new HashMap<String, Object>();
//				tempItemSettlementMap.put("settlement_date",settlementDate);
//				tempItemSettlementMap.put("total_profit", totalProfit);
//				tempItemSettlementMap.put("tobacco_profit", tobaccoProfit);
//				tempItemSettlementMap.put("other_profit", otherProfit);
////			} 
//			itemSettlementListMap.put(settlementDate, tempItemSettlementMap);
//		}
//		List<Map<String, Object>> merchDailyProfitList = new ArrayList<Map<String, Object>>();
//		for(String day : dayList) {
//			Map<String, Object> tempItemSettlementMap = itemSettlementListMap.get(day);
//			if(tempItemSettlementMap==null) {
//				tempItemSettlementMap = new HashMap<String, Object>();
//				tempItemSettlementMap.put("settlement_date",day);
//				tempItemSettlementMap.put("total_profit", BigDecimal.ZERO);
//				tempItemSettlementMap.put("tobacco_profit", BigDecimal.ZERO);
//				tempItemSettlementMap.put("other_profit", BigDecimal.ZERO);
//			}
//			merchDailyProfitList.add(tempItemSettlementMap);
//		}
//		return merchDailyProfitList;
	}
	
	/**
	 * 获取周利润数据, 包含总利润, 卷烟利润, 非烟利润
	 */
	@Override
	public List<Map<String, Object>> searchMerchWeeklyProfit(Map<String, Object> profitMap) throws Exception{
		LOG.debug("StatisticsServiceImpl searchMerchWeeklyProfit profitMap: " + profitMap);
		int gap = Integer.valueOf(profitMap.get("number").toString());
		int gapDays = gap * 7;
		String today = DateUtil.getToday();
		String endDay = DateUtil.getPreviousDay(today, 1);
		String startDay = DateUtil.getPreviousDay(endDay, gapDays-1);
		List<String> dayList = DateUtil.getListBetweenStartingAndEnding(startDay, endDay); // 日期集合
		profitMap.put("settlement_date_floor", startDay);//开始日期
		profitMap.put("settlement_date_ceiling", endDay);//结束日期
		
		List<Map<String, Object>> itemSettlementList = statisticsDao.searchSumMerchItemDailySettlement(profitMap);
		
		List<Map<String, Object>> merchDailyProfitList = new ArrayList<Map<String, Object>>();
		
		BigDecimal totalProfit = BigDecimal.ZERO;//总利润
		BigDecimal tobaccoProfit = BigDecimal.ZERO;//卷烟利润
		BigDecimal cigarProfit = BigDecimal.ZERO;//雪茄利润
		BigDecimal otherProfit = BigDecimal.ZERO;//非烟利润
		BigDecimal myProfit = BigDecimal.ZERO;//利润
		BigDecimal myLossAmount= BigDecimal.ZERO;//退货利润
		String settlementDate = null;
		String itemKindId = null;
		int dataIndex = 0;
		Map<String, Object> tempItemSettlementMap = new HashMap<String, Object>();
		for (int i = 0; i < dayList.size(); i++) {
			myProfit = BigDecimal.ZERO;//利润
			myLossAmount= BigDecimal.ZERO;//退货利润
			for (int j = dataIndex; j < itemSettlementList.size(); j++) {
				dataIndex = j;
//			}
//			for (Map<String, Object> map : itemSettlementList) {
				settlementDate = MapUtil.get(itemSettlementList.get(j), "settlement_date", null);
				itemKindId = MapUtil.get(itemSettlementList.get(j), "item_kind_id", "99");
				if(!dayList.get(i).equals(settlementDate)){
					if(Integer.valueOf(dayList.get(i))<Integer.valueOf(settlementDate)){
						break;
					}
					continue;
				}
				myProfit = MapUtil.getBigDecimal(itemSettlementList.get(j), "sum_profit_amount");
				myLossAmount = MapUtil.getBigDecimal(itemSettlementList.get(j), "sum_loss_amount");
				myProfit = myProfit.subtract(BigDecimal.ZERO.subtract(myLossAmount));
				
				totalProfit = totalProfit.add(myProfit);//总利润
				if(itemKindId.equals("01")){
					tobaccoProfit = tobaccoProfit.add(myProfit);//卷烟利润 
				} else if (itemKindId.equals("0102")) {
					cigarProfit = cigarProfit.add(myProfit);//卷烟利润 
				} else{
					otherProfit = otherProfit.add(myProfit);//非烟利润
				}
			}
			
			if( (i+1) % 7 != 0){
				continue;
			}
			tempItemSettlementMap.put("settlement_date","第"+(i+1)/7+"周");
			tempItemSettlementMap.put("total_profit", totalProfit);
			tempItemSettlementMap.put("tobacco_profit", tobaccoProfit);
			tempItemSettlementMap.put("cigar_profit", cigarProfit);
			tempItemSettlementMap.put("other_profit", otherProfit);
			merchDailyProfitList.add(tempItemSettlementMap);
			
			totalProfit = BigDecimal.ZERO;//总利润
			tobaccoProfit = BigDecimal.ZERO;//卷烟利润
			cigarProfit = BigDecimal.ZERO;//雪茄利润
			otherProfit = BigDecimal.ZERO;//非烟利润
			tempItemSettlementMap = new HashMap<String, Object>();
		}
		return merchDailyProfitList;
	}
	
	/**
	 * 获取月利润数据, 包含总利润, 卷烟利润, 非烟利润
	 */
	@Override
	public List<Map<String, Object>> searchMerchMonthlyProfit(Map<String, Object> profitMap) throws Exception{
		LOG.debug("PurchOrderServiceImpl searchMonthPurchOrderReport purchMap :"+profitMap);
		Integer number=Integer.valueOf( (String) profitMap.get("number"));//周期
		Date date  = new Date();
		String newDate  = (new SimpleDateFormat("yyyyMMdd")).format(date ) ;
		newDate =newDate .substring(0,newDate.length()-2);		
		Calendar cal  = Calendar.getInstance();
		cal .setTime(date );
		cal .add(Calendar.MARCH, -number+1);
		String oldDate  = (new SimpleDateFormat("yyyyMMdd")).format(cal .getTime());
		oldDate =oldDate .substring(0,oldDate.length()-2);
		profitMap.put("settlement_date_floor", oldDate+"01");//开始日期
		profitMap.put("settlement_date_ceiling", newDate+"31");//结束日期
		List<String> monthList=DateUtil.getListBetweenStartingAndEnding(oldDate,newDate);//得到开始、结束日期
		List<Map<String, Object>> data = statisticsDao.searchSumMerchItemDailySettlement(profitMap);
		
		List<Map<String, Object>> merchDailyProfitList = new ArrayList<Map<String, Object>>();
		String settlementDate=null;
		BigDecimal totalProfit = BigDecimal.ZERO;//总利润
		BigDecimal tobaccoProfit = BigDecimal.ZERO;//卷烟利润
		BigDecimal cigarProfit = BigDecimal.ZERO;//雪茄利润
		BigDecimal otherProfit = BigDecimal.ZERO;//非烟利润
		BigDecimal myProfit = BigDecimal.ZERO;//利润
		BigDecimal myLossAmount= BigDecimal.ZERO;//退货利润
		String itemKindId = null;
		Map<String, Object> tempItemSettlementMap = null;
		for (String monthStr : monthList) {
			tempItemSettlementMap = new HashMap<String, Object>();
			totalProfit = BigDecimal.ZERO;//总利润
			tobaccoProfit = BigDecimal.ZERO;//卷烟利润
			cigarProfit = BigDecimal.ZERO;//雪茄利润
			otherProfit = BigDecimal.ZERO;//非烟利润
			myProfit = BigDecimal.ZERO;//利润
			for (Map<String, Object> map : data) {
				settlementDate = MapUtil.get(map, "settlement_date", null);
				itemKindId = MapUtil.get(map, "item_kind_id", "99");
				if(!settlementDate.equals(monthStr)){
					continue;
				}
				myProfit = MapUtil.getBigDecimal(map, "sum_profit_amount");
				myLossAmount = MapUtil.getBigDecimal(map, "sum_loss_amount");
				myProfit = myProfit.subtract(BigDecimal.ZERO.subtract(myLossAmount));
				totalProfit = totalProfit.add(myProfit);//总利润
				if(itemKindId.equals("01")){
					tobaccoProfit = tobaccoProfit.add(myProfit);//卷烟利润 
				} else if(itemKindId.equals("0102")) {
					cigarProfit = cigarProfit.add(myProfit);//雪茄利润 
				} else{
					otherProfit = otherProfit.add(myProfit);//非烟利润
				}
			}
			tempItemSettlementMap.put("settlement_date",monthStr);
			tempItemSettlementMap.put("total_profit", totalProfit);
			tempItemSettlementMap.put("tobacco_profit", tobaccoProfit);
			tempItemSettlementMap.put("cigar_profit", cigarProfit);
			tempItemSettlementMap.put("other_profit", otherProfit);
			merchDailyProfitList.add(tempItemSettlementMap);
		}
		return merchDailyProfitList;
//		List<Map<String, Object>> data=searchMerchMonthlySettlement(profitMap);
//		List<Map<String, Object>> data=searchMerchMonthlySettlement(profitMap);
//		Map<String, Map<String, Object>> dataMap=new HashMap<String, Map<String, Object>>();
//		for (Map<String, Object> map : data) {
//			String dataMonth=(String) map.get("SETTLEMENT_MONTH");
//			dataMap.put(dataMonth, map);
//		}
//		/**
//		 * 查询月卷烟非烟利润
//		 */
//		List<Map<String, Object>> itemSettlementList = statisticsDao.searchMerchItemMonthlySettlement(profitMap);
//		Map<String, Map<String, Object>> itemSettlementListMap = new HashMap<String, Map<String, Object>>();
//		for(Map<String, Object> itemSettlementMap : itemSettlementList) {
//			String settlementMonth = itemSettlementMap.get("SETTLEMENT_MONTH").toString();
////			String itemKindId = itemSettlementMap.get("ITEM_KIND_ID").toString();
//			//分类
//			String itemKindId ="99";
//			if(itemSettlementMap.get("item_kind_id")!=null){
//				itemKindId = itemSettlementMap.get("item_kind_id").toString();
//			}
//			Map<String, Object> tempItemSettlementMap = itemSettlementListMap.get(settlementMonth);
//			BigDecimal totalProfit=BigDecimal.ZERO;
//			if(tempItemSettlementMap==null) {
//				tempItemSettlementMap = new HashMap<String, Object>();
//				tempItemSettlementMap.put("tobacco_profit", BigDecimal.ZERO);
//				tempItemSettlementMap.put("other_profit", BigDecimal.ZERO);
//			}else{
//				totalProfit=new BigDecimal(tempItemSettlementMap.get("total_profit").toString());
//			}
//			if("01".equals(itemKindId)) {
//				BigDecimal tobaccoProfit = new BigDecimal(tempItemSettlementMap.get("tobacco_profit").toString());
//				tobaccoProfit = tobaccoProfit.add(new BigDecimal(itemSettlementMap.get("PROFIT_AMOUNT").toString())).add(new BigDecimal(itemSettlementMap.get("LOSS_AMOUNT").toString()));
//				tempItemSettlementMap.put("tobacco_profit", tobaccoProfit);
//			} else {
//				BigDecimal otherProfit = new BigDecimal(tempItemSettlementMap.get("other_profit").toString());
//				otherProfit = otherProfit.add(new BigDecimal(itemSettlementMap.get("PROFIT_AMOUNT").toString())).add(new BigDecimal(itemSettlementMap.get("LOSS_AMOUNT").toString()));
//				tempItemSettlementMap.put("other_profit", otherProfit);
//			}
//			totalProfit = totalProfit.add(new BigDecimal(itemSettlementMap.get("PROFIT_AMOUNT").toString())).add(new BigDecimal(itemSettlementMap.get("LOSS_AMOUNT").toString()));
//			tempItemSettlementMap.put("total_profit", totalProfit);
//			itemSettlementListMap.put(settlementMonth, tempItemSettlementMap);
//		}
////		List<Map<String, Object>> returnData=searchMerchDailySettlement(profitMap);
//		List<Map<String, Object>> returnData=new ArrayList<Map<String,Object>>();
//		for (int i = 0; i < monthList.size(); i++) {
//			String today=monthList.get(i);
////			BigDecimal returnProfit=BigDecimal.ZERO;
//			Map<String, Object> returnMap=new HashMap<String, Object>();
////			if(itemSettlementListMap.containsKey(today)){
////				BigDecimal newProfitAmount=BigDecimal.ZERO;//利润
////				BigDecimal newLossAmount=BigDecimal.ZERO;//退货利润
////				if(dataMap.get(today).get("PROFIT_AMOUNT")!=null)newProfitAmount=new BigDecimal(dataMap.get(today).get("PROFIT_AMOUNT")+"");
////				if(dataMap.get(today).get("LOSS_AMOUNT")!=null)newLossAmount=new BigDecimal(dataMap.get(today).get("LOSS_AMOUNT")+"");
////				returnProfit=newProfitAmount.subtract(new BigDecimal("0").subtract(newLossAmount));
////			}
//			BigDecimal tobaccoProfit = BigDecimal.ZERO; // 卷烟利润
//			BigDecimal otherProfit = BigDecimal.ZERO; // 非烟利润
//			BigDecimal totalProfit=BigDecimal.ZERO;//全部利润
//			if(itemSettlementListMap.get(today)!=null) {
//				if(itemSettlementListMap.get(today).get("tobacco_profit")!=null) tobaccoProfit = new BigDecimal(itemSettlementListMap.get(today).get("tobacco_profit").toString());
//				if(itemSettlementListMap.get(today).get("other_profit")!=null) otherProfit = new BigDecimal(itemSettlementListMap.get(today).get("other_profit").toString());
//				if(itemSettlementListMap.get(today).get("total_profit")!=null) totalProfit = new BigDecimal(itemSettlementListMap.get(today).get("total_profit").toString());
//			}
//			returnMap.put("settlement_date", today);
//			returnMap.put("total_profit", totalProfit);
//			returnMap.put("tobacco_profit", tobaccoProfit);
//			returnMap.put("other_profit", otherProfit);
//			returnData.add(returnMap);
////			if(i>=data.size()||!data.get(i).get("settlement_date").equals(today)){
////				Map<String,Object> nullMap=new HashMap<String, Object>();
////				nullMap.put("settlement_date", today);
////				nullMap.put("profit_amount", 0);
////				data.add(i,nullMap);
////			}else{
////				
////			}
//		}	
//		return returnData;
	}
	
	//进销存报表--日\
	@Override	
	public List<Map<String, Object>> searchDayJxcReportForms(Map<String, Object> profitMap)
			throws Exception{
		LOG.debug("PurchOrderServiceImpl searchDayPurchOrderReport purchMap :"+profitMap);
		Integer number=MapUtil.getInt(profitMap, "number", 15);//周期
		
		String newDate=DateUtil.getToday();
		newDate=DateUtil.getPreviousDay(newDate, 1);
		String oldDate =DateUtil.getPreviousDay(newDate, number-1);
		
		List<String> dayList=DateUtil.getListBetweenStartingAndEnding(oldDate,newDate );//日期集合
		profitMap.put("settlement_date_floor", oldDate);//开始日期
		profitMap.put("settlement_date_ceiling", newDate);//结束日期
		
		List<Map<String, Object>> data=searchMerchDailySettlement(profitMap);
		
		Map<String, Object> nullMap = null;
		BigDecimal saleAmount=BigDecimal.ZERO;
		BigDecimal returnAmount=BigDecimal.ZERO;
		for (int i = 0; i < dayList.size(); i++) {
			if ( i>=data.size()
					|| !dayList.get(i).equals(data.get(i).get("settlement_date")))
			{
				nullMap=new HashMap<String, Object>();
				nullMap.put("settlement_date", dayList.get(i));
				nullMap.put("purch_amount", 0);//进货   PURCH_AMOUNT
				nullMap.put("sale_amount", 0);//销售  SALE_AMOUNT
				nullMap.put("whse_amount", 0);//库存  WHSE_AMOUNT
				data.add(i,nullMap);
			}else{
				saleAmount = MapUtil.getBigDecimal(data.get(i), "sale_amount");
				returnAmount = MapUtil.getBigDecimal(data.get(i), "return_amount");
				saleAmount=saleAmount.subtract(returnAmount);
				data.get(i).put("sale_amount", saleAmount);
			}
		}
		return data;
	}
	
	//进销存报表--周 
	@Override
	public List<Map<String, Object>> searchWeekJxcReportForms(Map<String, Object> profitMap)
			throws Exception{
		LOG.debug("PurchOrderServiceImpl searchWeekPurchOrderReport purchMap :"+profitMap);
		Integer number = MapUtil.getInt(profitMap, "number", 5);//周期
		Integer day=number*7;
		
		String newDate = DateUtil.getToday();
		newDate = DateUtil.getPreviousDay(newDate, 1);
		String oldDate = DateUtil.getPreviousDay(newDate, day-1);
		
		List<String> dayList=DateUtil.getListBetweenStartingAndEnding(oldDate,newDate);//日期集合
		
		profitMap.put("settlement_date_floor", oldDate);//开始日期
		profitMap.put("settlement_date_ceiling", newDate);//结束日期		
		List<Map<String, Object>> data=searchMerchDailySettlement(profitMap);
		
		List<Map<String, Object>>weekData=new ArrayList<Map<String,Object>>();
		int weekNum=0;//判读日期集合（daylist）,防止重复读取
		int dataInde=0;//data的下标
		BigDecimal sumPurch = BigDecimal.ZERO;//进货总额
		BigDecimal sumWhse = BigDecimal.ZERO;//库存总额
		BigDecimal sumSale = BigDecimal.ZERO;//销售总额
		BigDecimal purch = BigDecimal.ZERO;
		BigDecimal whse = BigDecimal.ZERO;
		BigDecimal sale = BigDecimal.ZERO;
		BigDecimal returnAmount = BigDecimal.ZERO;
		for (int i = 0; i < number; i++) {
			sumPurch = new BigDecimal("0");//进货总额
			sumWhse=new BigDecimal("0");//库存总额
			sumSale=new BigDecimal("0");//销售总额
			Map<String, Object> nullMap=new HashMap<String, Object>();
			for (int j = weekNum; j < dayList.size(); j++) {
//				if(data.size()>dataInde){
//					System.out.print(j+"==="+weekNum+"==="+dayList.get(j)+"-----"+data.get(dataInde).get("settlement_date")+":" +
//							":(进："+data.get(dataInde).get("purch_amount")+"-销："+data.get(dataInde).get("sale_amount")+"-存："+data.get(dataInde).get("whse_amount")+")");
//				}
				//daylist[j]这一天data[i]中也有
				if (data.size()>dataInde 
						&& data.get(dataInde).get("settlement_date").equals(dayList.get(j))){
					purch = MapUtil.getBigDecimal(data.get(dataInde), "purch_amount");
					whse = MapUtil.getBigDecimal(data.get(dataInde), "whse_amount");
					sale = MapUtil.getBigDecimal(data.get(dataInde), "sale_amount");
					returnAmount = MapUtil.getBigDecimal(data.get(i), "return_amount");
					sale = sale.subtract(returnAmount);
					sumPurch = sumPurch.add(purch);
					sumWhse = sumWhse.add(whse);
					sumSale = sumSale.add(sale);
					dataInde++;//data的下一个下标
				}
				weekNum++;
//				System.out.print("```"+weekNum+"==="+j+"==="+i);
				if((weekNum)%7==0){//一个周末，
//					System.out.println("------"+(i+1)+"周了");
					nullMap.put("settlement_date", "第"+(i+1)+"周");
					nullMap.put("purch_amount", sumPurch);//进货   PURCH_AMOUNT
					nullMap.put("sale_amount", sumSale);//销售  SALE_AMOUNT
					nullMap.put("whse_amount", sumWhse);//库存  WHSE_AMOUNT
					weekData.add(i,nullMap);
					break;
				}
//				System.out.println();
			}
		}		
		return weekData;
	}
	//进销存报表--月
	@Override
	public List<Map<String, Object>> searchMonthJxcReportForms(Map<String, Object> profitMap)
			throws Exception{
		LOG.debug("PurchOrderServiceImpl searchMonthPurchOrderReport purchMap :"+profitMap);
		Integer number = MapUtil.getInt(profitMap, "number", 6);//周期
		String newDate = DateUtil.getPreviousDay(DateUtil.getToday(),1);
		newDate = newDate.substring(0, 6);
		String oldDate = DateUtil.getPreviousMonth(newDate, number-1);
		profitMap.put("settlement_date_floor", oldDate+"01");//开始日期
		profitMap.put("settlement_date_ceiling", newDate+"31");//结束日期
		List<String> monthList=DateUtil.getListBetweenStartingAndEnding(oldDate,newDate);//得到开始、结束日期
		List<Map<String, Object>> data=searchMerchMonthlySettlement(profitMap);
		Map<String, Map<String, Object>> dataMap=new HashMap<String, Map<String, Object>>();
		String dataMonth = null;
		for (Map<String, Object> map : data) {
			dataMonth=(String) map.get("settlement_month");
			dataMap.put(dataMonth, map);
		}
		List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
		String today = null;
		Map<String,Object> nullMap = null;
		BigDecimal purchAmount = null;
		BigDecimal whseAmount = null;
		BigDecimal saleAmount = null;
		BigDecimal returnAmount = null;
		for (int i = 0; i < monthList.size(); i++) {
			today=monthList.get(i);
			nullMap=new HashMap<String, Object>();
			if(dataMap.containsKey(today)){
				purchAmount = MapUtil.getBigDecimal(dataMap.get(today), "purch_amount");
				whseAmount = MapUtil.getBigDecimal(dataMap.get(today), "whse_amount");
				saleAmount = MapUtil.getBigDecimal(dataMap.get(today), "sale_amount");
				returnAmount = MapUtil.getBigDecimal(dataMap.get(today), "return_amount");
				
				saleAmount=saleAmount.subtract(returnAmount);

				nullMap.put("purch_amount", purchAmount);//进货   PURCH_AMOUNT
				nullMap.put("sale_amount", saleAmount);//销售  SALE_AMOUNT
				nullMap.put("whse_amount", whseAmount);//库存  WHSE_AMOUNT
			}else{
				nullMap.put("purch_amount", BigDecimal.ZERO);//进货   PURCH_AMOUNT
				nullMap.put("sale_amount", BigDecimal.ZERO);//销售  SALE_AMOUNT
				nullMap.put("whse_amount", BigDecimal.ZERO);//库存  WHSE_AMOUNT
			}
			nullMap.put("settlement_date", today);
			returnList.add(i,nullMap);
		}
		return returnList;

	}
	
	//进销存表格
	@Override
	public List<Map<String, Object>> searchJxcReportTable (Map<String , Object> reportMap)throws Exception{
		LOG.debug("StatisticsServiceImpl jxcReportTable  reportMap:"+reportMap);
		//List<Map<String, Object>> data = statisticsDao.searchSaleReportTable(saleMap);
		String startDate = MapUtil.getString(reportMap, "start_date");
		String endDate = MapUtil.getString(reportMap, "end_date");
		List<Map<String, Object>> data = statisticsDao.searchJxcReportTable(reportMap);
		if(data.isEmpty()){
			return data;
		}
		
		BigDecimal sumSaleQuantity = BigDecimal.ZERO;//销售数量
		BigDecimal sumSaleAmount = BigDecimal.ZERO;//销售金额
		BigDecimal sumProfitAmount = BigDecimal.ZERO;//销售利润
		StringBuffer itemIdSb = new StringBuffer();
		String newItemId =  null;
		List<Map<String, Object>> newData = new ArrayList<Map<String,Object>>();
		Map<String, Object> dataMap = null;
		String [] itemInfoArr = null;
		
		for (Map<String, Object> map : data) {
			//销售数量
			sumSaleQuantity = MapUtil.getBigDecimal(map, "sum_sale_quantity").subtract(MapUtil.getBigDecimal(map, "sum_return_quantity"));
			
			if(sumSaleQuantity.compareTo(BigDecimal.ZERO) == 0 
					&& MapUtil.getBigDecimal(map, "sum_purch_quantity").compareTo(BigDecimal.ZERO) == 0 
					&& MapUtil.getBigDecimal(map, "WHSE_TURN_PL_QUANTITY").compareTo(BigDecimal.ZERO) == 0 ){
				continue;
			}
			
			newItemId = MapUtil.get(map, "item_id", null);
			itemInfoArr = MapUtil.getString(map, "item_info").split(",");
			
			if(StringUtil.isBlank(newItemId) || itemInfoArr.length<2 || StringUtil.isBlank(itemInfoArr[2]) || StringUtil.isBlank(itemInfoArr[1])){
				continue;
			}
			
			//销售利润
			sumProfitAmount = MapUtil.getBigDecimal(map, "sum_profit_amount").subtract(MapUtil.getBigDecimal(map, "sum_return_profit"));
			//销售金额
			sumSaleAmount = MapUtil.getBigDecimal(map, "sum_sale_amount").subtract(MapUtil.getBigDecimal(map, "sum_return_amount"));
			itemIdSb.append(newItemId+",");
			dataMap = new HashMap<String, Object>();
			
			dataMap.put("item_id", newItemId);
			dataMap.put("item_name", itemInfoArr[2]);
			dataMap.put("item_bar", itemInfoArr[1]);
			dataMap.put("unit_name", itemInfoArr[3]);
			dataMap.put("sum_purch_quantity", MapUtil.getBigDecimal(map, "sum_purch_quantity"));//进货量
			dataMap.put("sum_purch_amount", MapUtil.getBigDecimal(map, "sum_purch_amount"));//进货金额
			dataMap.put("sum_profit_amount", sumProfitAmount);//销售利润
			dataMap.put("sum_sale_quantity", sumSaleQuantity);//销售数量
			dataMap.put("sum_sale_amount", sumSaleAmount);//销售金额
			dataMap.put("sum_whse_turn_pl_quantity", MapUtil.getBigDecimal(map, "sum_whse_turn_pl_quantity"));//盘点量
			dataMap.put("sum_whse_turn_pl_amount", MapUtil.getBigDecimal(map, "sum_whse_turn_pl_amount"));//盘点金额			
			newData.add(dataMap);
		}
		
		//前一天
		String dayBefore=DateUtil.getPreviousDay(startDate, 1);
		Map<String, Object> dailyMap = new HashMap<String, Object>();
		dailyMap.put("settlement_date", dayBefore+","+endDate);
		dailyMap.put("item_id", itemIdSb.toString());
		dailyMap.put("merch_id", MapUtil.get(reportMap, "merch_id", null));
		List<Map<String, Object>> whseDailySettlement = statisticsDao.searchItemDailySettlement(dailyMap);//查询期初期末库存
		Map<String, Map<String, Object>> newDailySettlementMap = new HashMap<String, Map<String, Object>>();
		
		String newDate = null;
		for (Map<String, Object> map : whseDailySettlement) {
			newItemId = MapUtil.getString(map, "item_id");
			newDate = MapUtil.getString(map, "settlement_date");
			if(newDate.equals(dayBefore)){
				if(newDailySettlementMap.containsKey(newItemId)){
					newDailySettlementMap.get(newItemId).put("begin_whse_amount", MapUtil.getBigDecimal(map, "whse_amount"));
					newDailySettlementMap.get(newItemId).put("begin_whse_quantity", MapUtil.getBigDecimal(map, "whse_quantity"));
				}else{
					dataMap = new HashMap<String, Object>();
					dataMap.put("begin_whse_amount", MapUtil.getBigDecimal(map, "whse_amount"));
					dataMap.put("begin_whse_quantity", MapUtil.getBigDecimal(map, "whse_quantity"));
					newDailySettlementMap.put(newItemId, dataMap);
				}
			}else if(newDate.equals(endDate)){
				if(newDailySettlementMap.containsKey(newItemId)){
					newDailySettlementMap.get(newItemId).put("end_whse_amount", MapUtil.getBigDecimal(map, "whse_amount"));
					newDailySettlementMap.get(newItemId).put("end_whse_quantity", MapUtil.getBigDecimal(map, "whse_quantity"));
				}else{
					dataMap = new HashMap<String, Object>();
					dataMap.put("end_whse_amount", MapUtil.getBigDecimal(map, "whse_amount"));
					dataMap.put("end_whse_quantity", MapUtil.getBigDecimal(map, "whse_quantity"));
					newDailySettlementMap.put(newItemId, dataMap);
				}
			}
		}
		
		for (Map<String, Object> map : newData) {
			newItemId = MapUtil.getString(map, "item_id");
			if(newDailySettlementMap.containsKey(newItemId)){
				map.putAll(newDailySettlementMap.get(newItemId));
			}
		}
		return newData;
	}
	
	@Override
	public List<Map<String, Object>> searchMerchMonthProfit(
			Map<String, Object> paramMap) throws Exception {
		List<Map<String, Object>> maxProfitList=null;
		List<Map<String, Object>> minProfitList=null;
		Map<String,Object> profitMap=null;
		List<Map<String,Object>> profitList=new ArrayList<Map<String,Object>>();
		if(paramMap.get("month")!=null) {
			profitMap=new HashMap<String,Object>();
			Map<String, Object> settlementParam = new HashMap<String, Object>();
			settlementParam.put("merch_id", paramMap.get("merch_id"));
			settlementParam.put("settlement_date_floor", paramMap.get("month")+"01");
			settlementParam.put("settlement_date_ceiling", paramMap.get("month")+"31");
			//显示数据条数
			settlementParam.put("num", paramMap.get("num"));
			
			maxProfitList = statisticsDao.selectMerchMonthMaxProfit(settlementParam);
			for (Map<String, Object> map : maxProfitList) {
				map.put("pri4", MapUtil.getBigDecimal(map, "pri4").setScale(2, BigDecimal.ROUND_HALF_UP));
				map.put("sale_quantity", MapUtil.getBigDecimal(map, "sale_quantity").setScale(1, BigDecimal.ROUND_HALF_UP));
				map.put("profit_amount", MapUtil.getBigDecimal(map, "profit_amount").setScale(2, BigDecimal.ROUND_HALF_UP));
			}
			minProfitList= statisticsDao.selectMerchMonthMinProfit(settlementParam);
			for (Map<String, Object> map : minProfitList) {
				map.put("pri4", MapUtil.getBigDecimal(map, "pri4").setScale(2, BigDecimal.ROUND_HALF_UP));
				map.put("sale_quantity", MapUtil.getBigDecimal(map, "sale_quantity").setScale(1, BigDecimal.ROUND_HALF_UP));
				map.put("profit_amount", MapUtil.getBigDecimal(map, "profit_amount").setScale(2, BigDecimal.ROUND_HALF_UP));
			}
			profitMap.put("maxProfitList", maxProfitList);
			profitMap.put("minProfitList", minProfitList);
			profitList.add(profitMap);
			return profitList;
		}
		
		return new ArrayList<Map<String,Object>>();
	}
	
	
	@Override
	public List<Map<String, Object>> searchMerchMonthProfitRate(
			Map<String, Object> paramMap) throws Exception {
		List<Map<String, Object>> maxProfitRateList=null;
		List<Map<String, Object>> minProfitRateList=null;
		Map<String,Object> profitRateMap=null;
		List<Map<String,Object>> profitRateList=new ArrayList<Map<String,Object>>();
		if(paramMap.get("month")!=null) {
			profitRateMap=new HashMap<String,Object>();
			Map<String, Object> settlementParam = new HashMap<String, Object>();
			settlementParam.put("merch_id", paramMap.get("merch_id"));
			settlementParam.put("settlement_date_floor", paramMap.get("month")+"01");
			settlementParam.put("settlement_date_ceiling", paramMap.get("month")+"31");
			//显示数据条数
			settlementParam.put("num", paramMap.get("num"));
			
			maxProfitRateList = statisticsDao.selectMerchMonthMaxProfitRate(settlementParam);
			for(Map<String, Object> profitRate : maxProfitRateList) {
				profitRate.put("PROFIT_RATE", MapUtil.getBigDecimal(profitRate, "profit_rate").setScale(0, BigDecimal.ROUND_HALF_UP)+"%");
				
				profitRate.put("pri4", MapUtil.getBigDecimal(profitRate, "pri4").setScale(2, BigDecimal.ROUND_HALF_UP));
				profitRate.put("sale_quantity", MapUtil.getBigDecimal(profitRate, "sale_quantity").setScale(1, BigDecimal.ROUND_HALF_UP));
				profitRate.put("sale_amount", MapUtil.getBigDecimal(profitRate, "sale_amount").setScale(2, BigDecimal.ROUND_HALF_UP));
				profitRate.put("profit_amount", MapUtil.getBigDecimal(profitRate, "profit_amount").setScale(2, BigDecimal.ROUND_HALF_UP));
			}
			minProfitRateList= statisticsDao.selectMerchMonthMinProfitRate(settlementParam);
			for(Map<String, Object> profitRate : minProfitRateList) {
				profitRate.put("PROFIT_RATE", MapUtil.getBigDecimal(profitRate, "profit_rate").setScale(0, BigDecimal.ROUND_HALF_UP)+"%");
				
				profitRate.put("pri4", MapUtil.getBigDecimal(profitRate, "pri4").setScale(2, BigDecimal.ROUND_HALF_UP));
				profitRate.put("sale_quantity", MapUtil.getBigDecimal(profitRate, "sale_quantity").setScale(1, BigDecimal.ROUND_HALF_UP));
				profitRate.put("sale_amount", MapUtil.getBigDecimal(profitRate, "sale_amount").setScale(2, BigDecimal.ROUND_HALF_UP));
				profitRate.put("profit_amount", MapUtil.getBigDecimal(profitRate, "profit_amount").setScale(2, BigDecimal.ROUND_HALF_UP));
			}
			profitRateMap.put("maxProfitRateList", maxProfitRateList);
			profitRateMap.put("minProfitRateList", minProfitRateList);
			profitRateList.add(profitRateMap);
			return profitRateList;
		}
		
		return new ArrayList<Map<String,Object>>();
	}
	
	//销售表格详情
	@Override
	public List<Map<String, Object>> searchSaleReportTable(Map<String, Object> saleMap)throws Exception{
		LOG.debug("StatisticsServiceImp searchSaleReportTable saleMpa:"+saleMap);
		List<Map<String, Object>> data = statisticsDao.searchSaleReportTable(saleMap);
		BigDecimal sumSaleQuantion = BigDecimal.ZERO;//销售量
		BigDecimal sumProfitAmount = BigDecimal.ZERO;//利润
		BigDecimal sumSaleAmount = BigDecimal.ZERO;//销售额
		BigDecimal sumUnknownSaleQuantity = BigDecimal.ZERO;//未知销售量
		BigDecimal sumUnknownSaleAmount = BigDecimal.ZERO;//未知销售额
		BigDecimal sumUnknownProfitAmount = BigDecimal.ZERO;//未知利润额
		
		Map<String, Object> newMap  = null;
		List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
		String [] itemInfo = null;
		for (Map<String, Object> map : data) {
			newMap = new HashMap<String, Object>();
			sumSaleQuantion = MapUtil.getBigDecimal(map, "sum_sale_quantity").subtract(MapUtil.getBigDecimal(map, "sum_return_quantity"));//销售总量 
			itemInfo = MapUtil.getString(map, "item_info").split(",");
			if(sumSaleQuantion.compareTo(BigDecimal.ZERO) == 0){//没有销售
				continue;
			}
			sumProfitAmount = MapUtil.getBigDecimal(map, "sum_profit_amount").subtract(MapUtil.getBigDecimal(map, "sum_return_profit"));//销售总利润
			sumSaleAmount = MapUtil.getBigDecimal(map, "sum_sale_amount").subtract(MapUtil.getBigDecimal(map, "sum_return_amount"));//销售总额
			
			if(StringUtil.isBlank(itemInfo[1]) || StringUtil.isBlank(itemInfo[2])){//未知商品(没有条码和名称)
				sumUnknownProfitAmount = sumUnknownProfitAmount.add(sumProfitAmount);
				sumUnknownSaleAmount = sumUnknownSaleAmount.add(sumSaleAmount);
				sumUnknownSaleQuantity = sumUnknownSaleQuantity.add(sumSaleQuantion);
				continue;
			}
			newMap.put("sum_profit_amount", sumProfitAmount);//利润
			newMap.put("sum_sale_amount", sumSaleAmount);//销售金额
			newMap.put("sum_sale_quantion", sumSaleQuantion);//销售数量
			newMap.put("avg_sale_profit", sumProfitAmount.multiply(sumSaleQuantion));//平均利润
			newMap.put("avg_sale_amount", sumSaleAmount.multiply(sumSaleQuantion));//平均售价
			newMap.put("last_jnjw", itemInfo[4]);//最后一次进价
			newMap.put("cost", itemInfo[6]);//成本
			newMap.put("unit_name", itemInfo[3]);//单位
			newMap.put("item_name", itemInfo[2]);//名称
			newMap.put("item_bar", itemInfo[1]);//条码
			newMap.put("item_id", MapUtil.getString(map, "item_id"));//item_id
			returnList.add(newMap);
		}
		
		if(sumUnknownSaleQuantity.compareTo(BigDecimal.ZERO) != 0){
			newMap = new HashMap<String, Object>();
			newMap.put("sum_profit_amount", sumUnknownProfitAmount);//利润
			newMap.put("sum_sale_amount", sumUnknownSaleAmount);//销售金额
			newMap.put("sum_sale_quantion", sumUnknownSaleQuantity);//销售数量
			newMap.put("avg_sale_profit", sumUnknownProfitAmount.multiply(sumUnknownSaleQuantity));//平均利润
			newMap.put("avg_sale_amount", sumUnknownSaleAmount.multiply(sumUnknownSaleQuantity));//平均售价
			newMap.put("last_jnjw", "未知");//最后一次进价
			newMap.put("cost", "未知");//成本
			newMap.put("unit_name", "未知");//单位
			newMap.put("item_name", "未知");//名称
			newMap.put("item_bar", "未知");//条码
			newMap.put("item_id", null);//item_id
			returnList.add(newMap);
		}
		return returnList;
	}
	
	/**
	 * description  今日 利/存 -卷烟/非卷烟利润，本月利润
	 * @return
	 * */
	public Map<String, Object> searchBusinessAnalysis(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsServiceImpl searchBusinessAnalysis paramMap: " + paramMap);
		Map<String, Object> result = new HashMap<String, Object>();
		BigDecimal profitTobacco = BigDecimal.ZERO;
		BigDecimal profitCigar = BigDecimal.ZERO; // 雪茄烟的利润, 数据不予卷烟叠加
		BigDecimal profitOther = BigDecimal.ZERO;
		BigDecimal whseTobacco = BigDecimal.ZERO;
		BigDecimal whseTobaccoQuantity = BigDecimal.ZERO;
		BigDecimal whseCigar = BigDecimal.ZERO; // 雪茄烟的利润, 数据不予卷烟叠加
		BigDecimal whseCigarQuantity = BigDecimal.ZERO;
		BigDecimal whseOther = BigDecimal.ZERO;
		BigDecimal whseOtherQuantity = BigDecimal.ZERO;
		
		Map<String, Object> settlementParam = new HashMap<String, Object>();
		settlementParam.put("merch_id", paramMap.get("merch_id"));
		settlementParam.put("month", DateUtil.getToday().substring(0, 6));
		
//		// 获取本月进销利数据
//		Map<String, Object> merchSettlementMap = searchMerchSettlement(settlementParam);
//		result.put("profit_month", merchSettlementMap.get("profit_amount")!=null?merchSettlementMap.get("profit_amount").toString():"0"); // 当月利润
//		result.put("sale_month", merchSettlementMap.get("sale_amount")!=null?merchSettlementMap.get("sale_amount").toString():"0"); // 当月销售额
//		result.put("sale_count_month", merchSettlementMap.get("sale_count")!=null?merchSettlementMap.get("sale_count").toString():"0"); // 当月销售笔数
//		result.put("purch_month", merchSettlementMap.get("purch_amount")!=null?merchSettlementMap.get("purch_amount").toString():"0"); // 当月进货额
//		
		Map<String, Object> currentSettlementParam = new HashMap<String, Object>();
		currentSettlementParam.put("merch_id", paramMap.get("merch_id"));
		
		// 获取当日存/利的卷烟/非烟数据
		Map<String, Object> merchItemCurrentSettlementMap = searchMerchItemCurrentSettlement(currentSettlementParam);
		List<Map<String, Object>> merchItemList = (List<Map<String, Object>>) merchItemCurrentSettlementMap.get("list");
		BigDecimal profitAmount=BigDecimal.ZERO;
		BigDecimal whseAmount = null;
		BigDecimal whseQuantity = BigDecimal.ZERO;
		
		BigDecimal adjustedAmount = BigDecimal.ZERO;
		BigDecimal otherAdjustedAmount = BigDecimal.ZERO;
		BigDecimal returnAdjustedAmount = BigDecimal.ZERO;
		BigDecimal returnOtherAdjustedAmount = BigDecimal.ZERO;
		
		if(merchItemList!=null) {
			for(Map<String, Object> merchItemMap : merchItemList) {
				profitAmount=MapUtil.getBigDecimal(merchItemMap,"profit_amount");
				whseAmount=MapUtil.getBigDecimal(merchItemMap, "whse_amount");
				whseQuantity=MapUtil.getBigDecimal(merchItemMap, "whse_quantity");
				BigDecimal returnProfit=MapUtil.getBigDecimal(merchItemMap, "loss_amount");
				adjustedAmount = adjustedAmount.add(MapUtil.getBigDecimal(merchItemMap, "adjusted_amount"));
//				otherAdjustedAmount = otherAdjustedAmount.add(MapUtil.getBigDecimal(merchItemMap, "other_adjusted_amount"));
				returnAdjustedAmount = returnAdjustedAmount.add(MapUtil.getBigDecimal(merchItemMap, "return_adjusted_amount"));
//				returnOtherAdjustedAmount = returnOtherAdjustedAmount.add(MapUtil.getBigDecimal(merchItemMap, "return_other_adjusted_amount"));
				
				if (WhseServiceImpl.showPositiveWhse && whseQuantity.compareTo(BigDecimal.ZERO)<0) {
					whseQuantity = BigDecimal.ZERO;
					whseAmount = BigDecimal.ZERO;
				}
				
//				return_adjusted_amount = return_adjusted_amount.add
				// 退货利润为负
				profitAmount = profitAmount.subtract(returnProfit);	
				
				if("01".equals(merchItemMap.get("item_kind_id"))) {
					profitTobacco = profitTobacco.add(profitAmount);
					whseTobacco = whseTobacco.add(whseAmount);
					whseTobaccoQuantity = whseTobaccoQuantity.add(whseQuantity);
				} else if("0102".equals(merchItemMap.get("item_kind_id"))) {
					profitCigar = profitCigar.add(profitAmount);
					whseCigar = whseCigar.add(whseAmount);
					whseCigarQuantity = whseCigarQuantity.add(whseQuantity);
				} else {
					profitOther = profitOther.add(profitAmount);
					whseOther = whseOther.add(whseAmount);
					whseOtherQuantity = whseOtherQuantity.add(whseQuantity);
				}
			}
		}
		result.put("profit_tobacco", profitTobacco.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日卷烟利润
		result.put("whse_tobacco", whseTobacco.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日卷烟库存额
		result.put("whse_tobacco_quantity", whseTobaccoQuantity.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日卷烟库存量
		result.put("profit_cigar", profitCigar.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日雪茄利润
		result.put("whse_cigar", whseCigar.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日雪茄库存额
		result.put("whse_cigar_quantity", whseCigarQuantity.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日雪茄库存量
		result.put("profit_other", profitOther.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日非烟利润
		result.put("whse_other", whseOther.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日非烟库存额
		result.put("whse_other_quantity", whseOtherQuantity.setScale(2,BigDecimal.ROUND_HALF_UP)); // 今日非烟库存量
		
		// 获取当日进销存利其他数据
		Map<String, Object> merchCurrentSettlementMap = searchMerchCurrentSettlement(currentSettlementParam);
		BigDecimal newProfitAmount=BigDecimal.ZERO;	//销售利润
		BigDecimal newSaleAmount=BigDecimal.ZERO;//销售金额
		BigDecimal newSaleAmountOnline=BigDecimal.ZERO;//网上销售额
		BigDecimal newSaleAmountOffline=BigDecimal.ZERO;//线下销售额
		BigDecimal newReturnAmountOnline=BigDecimal.ZERO;//网上退货金额return_amount_online
		BigDecimal newLossAmount=BigDecimal.ZERO;//退货利润
		BigDecimal newReturnAmountOffLine=BigDecimal.ZERO;//线下退货金额  RETURN_AMOUNT_OFFLINE
		BigDecimal newReturnAmount=BigDecimal.ZERO;//退货总额
		BigDecimal newWhseAmount=BigDecimal.ZERO;// 当日库存额
		BigDecimal newPuchAmount=BigDecimal.ZERO;//当日进货额
		newPuchAmount = MapUtil.getBigDecimal(merchCurrentSettlementMap, "purch_amount");
//		当日进货额--merchCurrentSettlementMap.get("purch_amount")!=null?merchCurrentSettlementMap.get("purch_amount").toString():"0"
//		newWhseAmount=MapUtil.getBigDecimal(merchCurrentSettlementMap, "whse_amount");
		newWhseAmount = whseCigar.add(whseTobacco).add(whseOther);
		newProfitAmount=MapUtil.getBigDecimal(merchCurrentSettlementMap, "profit_amount");
		newSaleAmount=MapUtil.getBigDecimal(merchCurrentSettlementMap, "sale_amount");
		newSaleAmountOnline=MapUtil.getBigDecimal(merchCurrentSettlementMap,"sale_amount_online");
		newSaleAmountOffline=MapUtil.getBigDecimal(merchCurrentSettlementMap,"sale_amount_offline");
		newReturnAmountOnline=MapUtil.getBigDecimal(merchCurrentSettlementMap,"return_amount_online");
		newLossAmount = MapUtil.getBigDecimal(merchCurrentSettlementMap, "loss_amount");
		newReturnAmountOffLine=MapUtil.getBigDecimal(merchCurrentSettlementMap,"return_amount_offline");
		newReturnAmount=MapUtil.getBigDecimal(merchCurrentSettlementMap,"return_amount");
//		newProfitAmount=newProfitAmount.subtract(new BigDecimal("0").subtract(newLossAmount));
		newProfitAmount=newProfitAmount.subtract(newLossAmount);
		newSaleAmount=newSaleAmount.subtract((newReturnAmount));//销售额
		newSaleAmountOnline=newSaleAmountOnline.subtract((newReturnAmountOnline));
		newSaleAmountOffline=newSaleAmountOffline.subtract((newReturnAmountOffLine));
		// 当日利润
		result.put("profit_today",newProfitAmount.setScale(2, BigDecimal.ROUND_HALF_UP) );
		// 当日销售额
		result.put("sale_today", newSaleAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
		// 当月销售笔数
		result.put("sale_count_today", merchCurrentSettlementMap.get("sale_count")!=null?merchCurrentSettlementMap.get("sale_count").toString():"0"); 
		result.put("sale_online",newSaleAmountOnline.setScale(2, BigDecimal.ROUND_HALF_UP)); // 当日在线销售额
		// 当日在线销售笔数
		result.put("sale_count_online", merchCurrentSettlementMap.get("sale_count_online")!=null?merchCurrentSettlementMap.get("sale_count_online").toString():"0"); 
		result.put("sale_offline", newSaleAmountOffline.setScale(2, BigDecimal.ROUND_HALF_UP)); // 当日线下销售额
		// 当日线下销售笔数
		result.put("sale_count_offline", merchCurrentSettlementMap.get("sale_count_offline")!=null?merchCurrentSettlementMap.get("sale_count_offline").toString():"0");
		// 当日进货额
		result.put("purch_today", newPuchAmount.setScale(2,BigDecimal.ROUND_HALF_UP)); 
		// 当日进货笔数
		result.put("purch_count_today", merchCurrentSettlementMap.get("purch_count")!=null?merchCurrentSettlementMap.get("purch_count").toString():"0"); 
		// 当日进货品种数
		result.put("purch_item_count", merchCurrentSettlementMap.get("purch_item_count")!=null?merchCurrentSettlementMap.get("purch_item_count").toString():"0");
		// 当日库存额
		result.put("whse_today",newWhseAmount.setScale(2,BigDecimal.ROUND_HALF_UP) ); 
		// 当日库存品种数
		result.put("whse_item_count", merchCurrentSettlementMap.get("whse_item_count")!=null?merchCurrentSettlementMap.get("whse_item_count").toString():"0");
		
		result.put("return_amount", newReturnAmount);
		result.put("sum_adjusted_amount", (adjustedAmount.add(otherAdjustedAmount)).subtract(returnOtherAdjustedAmount.add(returnAdjustedAmount)));
		
		return result;
	}
		
	@Override
	public List<Map<String, Object>> searchMerchMonthlySettlement(Map<String, Object> paramsMap) throws Exception{
		LOG.debug("StatisticsServiceImpl searchMerchMonthlySettlement paramMap: " + paramsMap);
		return statisticsDao.searchMerchMonthlySettlement(paramsMap);
	}
	@Override
	public Map<String, Object> getSaleDailySettlement(Map<String, Object> dataMap) throws Exception {
		LOG.debug("getSaleDailySettlement:"+dataMap);
		String merchId=dataMap.get("merch_id").toString();
		String dateOrder = DateUtil.getToday();
		Map<String, Object> merchHistoryMap = new HashMap<String,Object>();
		Map<String, Map<String,Object>> itemHistoryMap=new HashMap<String,Map<String,Object>>();
		Map<String, Object> saleOrderParam = new HashMap<String, Object>();
		saleOrderParam.put("merch_id", merchId);
		saleOrderParam.put("page_index", -1);
		saleOrderParam.put("page_size", -1);
		
		List<Map<String, Object>> merchItemList = itemDao.selectMerchItem(saleOrderParam);
		for(Map<String, Object> merchItemMap : merchItemList) {
			String itemHistoryIdentity = merchId+","+merchItemMap.get("item_id"); // merch_id+item_id作为map的key
			Map<String, Object> itemHistory = new HashMap<String, Object>();
			itemHistory.put("settlement_id", IDUtil.getId());
			itemHistory.put("settlement_date", dateOrder);
			itemHistory.put("merch_id", merchId);
			itemHistory.put("item_id", merchItemMap.get("item_id"));
			itemHistory.put("item_bar", merchItemMap.get("item_bar"));
			itemHistory.put("item_name", merchItemMap.get("item_name"));
			itemHistory.put("item_kind_id", merchItemMap.get("item_kind_id"));
			itemHistory.put("unit_name", merchItemMap.get("unit_name"));
			itemHistory.put("cost", merchItemMap.get("cost"));
			itemHistory.put("pri1", merchItemMap.get("pri1"));
			itemHistory.put("pri2", merchItemMap.get("pri2"));
			itemHistory.put("pri4", merchItemMap.get("pri4"));
			itemHistory.put("discount", merchItemMap.get("discount"));
			itemHistory.put("unit_ratio", merchItemMap.get("unit_ratio"));
			itemHistoryMap.put(itemHistoryIdentity, itemHistory);
		}
		Map<String, Object> userMap = new HashMap<String, Object>();
		userMap.put("merch_id", merchId);
		userMap.put("order_date", dateOrder);
		userMap.put("role_id", MapUtil.getString(dataMap, "role_id") );
		userMap.put("role_name", MapUtil.getString(dataMap, "role_name"));
		userMap.put("user_code", MapUtil.getString(dataMap, "user_code"));
		saleDailySettlement(userMap, merchHistoryMap, itemHistoryMap);//销售详情
		returnDailySettlement(userMap, merchHistoryMap, itemHistoryMap);//退货详情
		
		List<Map<String,Object>> saleDataList=new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> returnDataList=new ArrayList<Map<String,Object>>();//退货list
		
	    Iterator it = itemHistoryMap.entrySet().iterator();  
	    
	    BigDecimal amtys_ord_total = MapUtil.getBigDecimal(merchHistoryMap, "sale_amount");//销售总额
	    BigDecimal return_ord_amount = MapUtil.getBigDecimal(merchHistoryMap, "return_amount");//销售总额
	    amtys_ord_total = amtys_ord_total.subtract(return_ord_amount);//销售额-退货额
	    
	    BigDecimal lossAmount = MapUtil.getBigDecimal(merchHistoryMap, "loss_amount");//退货利润
	    BigDecimal profitAmount = MapUtil.getBigDecimal(merchHistoryMap, "profit_amount");//销售利润
	    BigDecimal newSaleProfitAmount = profitAmount.subtract(lossAmount);//销售利润-退货利润
	    
	    BigDecimal qty_ord_total = BigDecimal.ZERO;
		BigDecimal return_ord_quantity=BigDecimal.ZERO;
		BigDecimal unit_ratio = BigDecimal.ZERO;
	    
	    while (it.hasNext()) {  
	    	Map<String,Object> saleMap=new HashMap<String,Object>();
	    	Map<String, Object> returnMap=new HashMap<String, Object>();//退货map
	        Map.Entry entry = (Map.Entry) it.next();  
	        Map<String,Object> value =(Map<String,Object>)entry.getValue();
	        BigDecimal returnItemQty = BigDecimal.ZERO;
	        BigDecimal returnItemAmt = BigDecimal.ZERO;
	        BigDecimal returnItemLoss = BigDecimal.ZERO;
	        BigDecimal returnAdjustedAmount = BigDecimal.ZERO;
//	        BigDecimal returnOtherAdjusteAmount = BigDecimal.ZERO;
	        unit_ratio = MapUtil.getBigDecimal(value, "unit_ratio");//转换比
	        if(MapUtil.getBigDecimal(value, "return_quantity").compareTo(BigDecimal.ZERO)!=0){
	        	//2015-02-06朱鹏,由于在今日汇总中按照管理包装显示数据,所以改动如下:
	        	returnItemQty = MapUtil.getBigDecimal(value, "return_quantity" );
	        	if(!unit_ratio.equals(BigDecimal.ZERO)){
	        		returnItemQty = returnItemQty.divide(unit_ratio, 1, BigDecimal.ROUND_HALF_UP);
	        	}
	        	return_ord_quantity = return_ord_quantity.add(returnItemQty);
//	        	return_ord_quantity = return_ord_quantity.add(new BigDecimal(value.get("return_quantity").toString()));
	        	//改动结束
	        	
	 	        returnItemAmt = MapUtil.getBigDecimal(value, "return_amount");
	 	        returnItemLoss = MapUtil.getBigDecimal(value, "loss_amount");
	        	returnAdjustedAmount = MapUtil.getBigDecimal(value, "return_adjusted_amount");
//	        	returnOtherAdjusteAmount = MapUtil.getBigDecimal(value, "return_other_adjusted_amount");
	 	        
	        	returnMap.put("return_quantity",returnItemQty);
			    returnMap.put("return_amount", returnItemAmt);
			    returnMap.put("item_name", MapUtil.getString(value, "item_name"));
			    returnMap.put("profit_amount", returnItemLoss);
			    
			    returnMap.put("return_adjusted_amount ",returnAdjustedAmount );
//			    returnMap.put("return_other_adjusted_amount", returnOtherAdjusteAmount);
			    returnDataList.add(returnMap);
	        }
	        if(MapUtil.getBigDecimal(value, "sale_quantity").compareTo(BigDecimal.ZERO)!=0){
	        	BigDecimal saleOtherAdjustedAmount = MapUtil.getBigDecimal(value, "sale_other_adjusted_amount");
	        	BigDecimal saleAdjustedAmount = MapUtil.getBigDecimal(value, "sale_adjusted_amount");
//	        	BigDecimal sumAdjusteOrReturnAmount = saleAdjustedAmount.add(saleOtherAdjustedAmount).add(returnItemAmt);
	        	
	        	BigDecimal sale_quantity = MapUtil.getBigDecimal(value, "sale_quantity");
	        	if(!unit_ratio.equals(BigDecimal.ZERO)){
	        		sale_quantity = sale_quantity.divide(unit_ratio, 1, BigDecimal.ROUND_HALF_UP);
	        	}
//	        	sale_quantity = sale_quantity.subtract(returnItemQty);
	        	
		        saleMap.put("sale_quantity", sale_quantity);
		        saleMap.put("sale_amount", MapUtil.getBigDecimal(value, "sale_amount").subtract(saleAdjustedAmount).subtract(saleOtherAdjustedAmount));
		        saleMap.put("sale_pri", MapUtil.getBigDecimal(value, "pri4"));
		        saleMap.put("item_name", MapUtil.getString(value, "item_name"));
		        saleMap.put("profit_amount", MapUtil.getBigDecimal(value, "profit_amount"));
		        
		        saleMap.put("sale_other_adjusted_amount", saleOtherAdjustedAmount);
		        saleMap.put("sale_adjusted_amount", saleAdjustedAmount);
		        qty_ord_total = qty_ord_total.add(sale_quantity);
		        saleDataList.add(saleMap);
	        }
	    }
	    
	    qty_ord_total = qty_ord_total.subtract(return_ord_quantity);
	    
	    Map<String, Object> orderData=new HashMap<String,Object>();
	    orderData.put("return_ord_amount", return_ord_amount);//退货总金额
	    orderData.put("return_ord_quantity", return_ord_quantity);//退货总量
		orderData.put("qty_ord_total", qty_ord_total);//销售总量
		orderData.put("amtys_ord_total", amtys_ord_total);//销售总金额
		orderData.put("current", DateUtil.getCurrentTimeMillisAsString("yyyy-MM-dd HH:mm:ss"));
		orderData.put("sale_list", saleDataList);
		orderData.put("return_list", returnDataList);
		orderData.put("sale_profit_amount", newSaleProfitAmount);
		return orderData;
	}
	
	
//	进货明细表====new
	@Override
	public Map<String, Object> searchPurchRecords(Map<String, Object> paramMap)throws Exception{
		LOG.debug("searchPurchRecords params:"+paramMap);
//		if(paramMap.containsKey("keyword")){
//			paramMap.put("key", MapUtil.getString(paramMap, "keyword"));
//			Map<String, Object> result = itemService.searchItemByLucene(paramMap);
//			StringBuffer itemIdBuffer = new StringBuffer();
//			List<Map<String, Object>> itemList = (List<Map<String, Object>>)MapUtil.get(result, "item_list", new ArrayList<Map<String, Object>>());
////					(List<Map<String, Object>>) result.get("item_list");
//			int index = 1;
//			for(Map<String, Object> item : itemList) {
//				if(index++==1) {
//					itemIdBuffer.append(item.get("item_id").toString());
//				} else {
//					itemIdBuffer.append(","+item.get("item_id").toString());
//				}
//			}
//			paramMap.put("item_id", itemIdBuffer.toString());
//		}
		
		List<Map<String, Object>> purchList = statisticsDao.selectPurchRecords(paramMap);
		
		BigDecimal sumPurchAmount = BigDecimal.ZERO;
		BigDecimal sumMaxPri = BigDecimal.ZERO;
		BigDecimal sumMinPri = BigDecimal.ZERO;
		BigDecimal sumpurchQuantity = BigDecimal.ZERO;
		BigDecimal sumAvgPri = BigDecimal.ZERO;
		BigDecimal sumPurchSaleRatio = BigDecimal.ZERO;
		int sumPurchTimes = 0;
		
		BigDecimal maxPri = BigDecimal.ZERO;
		BigDecimal minPri = BigDecimal.ZERO;
		BigDecimal avgPri = BigDecimal.ZERO;
		BigDecimal purchAmount = BigDecimal.ZERO;
		BigDecimal purchQuantity = BigDecimal.ZERO;
		BigDecimal purchSaleRatio = BigDecimal.ZERO;
		
		String [] itemInfo = null;
		String itemName = null;
		for (Map<String, Object> map : purchList) {
			itemName = "未知";
			itemInfo = MapUtil.getString(map, "item_info").split(",");
			if(itemInfo.length > 1){
				itemName = itemInfo[1];
			}
			maxPri = MapUtil.getBigDecimal(map, "max_pri").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			minPri = MapUtil.getBigDecimal(map, "min_pri").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			avgPri = MapUtil.getBigDecimal(map, "avg_pri").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			purchAmount = MapUtil.getBigDecimal(map, "purch_amount").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			purchQuantity = MapUtil.getBigDecimal(map, "purch_quantity").setScale(RFQTYDIGIT, BigDecimal.ROUND_HALF_UP);
			int purchTimes = MapUtil.getInt(map, "purch_times", 0);
			purchSaleRatio = MapUtil.getBigDecimal(map, "purch_sale_ratio").setScale(RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP);
			map.put("item_name", itemName);
			map.put("max_pri", maxPri);
			map.put("min_pri", minPri);
			map.put("avg_pri", avgPri);
			map.put("purch_amount",purchAmount);
			map.put("purch_quantity", purchQuantity);
			map.put("purch_sale_ratio",purchSaleRatio.multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP)+"%");
			
			sumPurchAmount = sumPurchAmount.add(purchAmount);
			sumpurchQuantity = sumpurchQuantity.add(purchQuantity);
			sumAvgPri = sumAvgPri.add(avgPri);
			sumPurchSaleRatio = sumPurchSaleRatio.add(purchSaleRatio);
			sumPurchTimes = sumPurchTimes + purchTimes;
			sumMaxPri = sumMaxPri.add(maxPri);
			sumMinPri = sumMinPri.add(minPri);
		}
		int listSize = purchList.size();
		if(listSize == 0){
			listSize = 1;
		}
		
		Map<String, Object> purchMap = new HashMap<String, Object>();
		purchMap.put("purch_list", purchList);
		purchMap.put("total_purch_amount", sumPurchAmount);
		purchMap.put("total_purch_quantity", sumpurchQuantity);
		purchMap.put("total_purch_sale_ratio", sumPurchSaleRatio.divide(new BigDecimal(listSize),RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP)+"%");
		purchMap.put("total_purch_times", sumPurchTimes);
		purchMap.put("total_avg_pri", sumAvgPri.divide(new BigDecimal(listSize), RFAMTDIGIT, BigDecimal.ROUND_HALF_UP));
		purchMap.put("total_max_pri", sumMaxPri.divide(new BigDecimal(listSize), RFAMTDIGIT, BigDecimal.ROUND_HALF_UP));
		purchMap.put("total_min_pri", sumMinPri.divide(new BigDecimal(listSize), RFAMTDIGIT, BigDecimal.ROUND_HALF_UP));
		
		return  purchMap;
	}
	
//	 * 销售明细表---new
	@Override
	public Map<String, Object> searchSalesRecords(Map<String, Object> paramMap)throws Exception{
		LOG.debug("searchSalesRecords params:"+paramMap);
		/*//luch查询
		if(paramMap.containsKey("keyword")){
			paramMap.put("key", MapUtil.getString(paramMap, "keyword"));
			Map<String, Object> result = itemService.searchItemByLucene(paramMap);
			StringBuffer itemIdBuffer = new StringBuffer();
			List<Map<String, Object>> itemList = (List<Map<String, Object>>) MapUtil.get(result, "item_list", new ArrayList<Map<String, Object>>());
//					result.get("item_list");
			int index = 1;
			for(Map<String, Object> item : itemList) {
				if(index++==1) {
					itemIdBuffer.append(item.get("item_id").toString());
				} else {
					itemIdBuffer.append(","+item.get("item_id").toString());
				}
			}
			paramMap.put("item_id", itemIdBuffer.toString());
		}*/
		
		List<Map<String, Object>> saleList = statisticsDao.selectSalesRecords(paramMap);
		BigDecimal sumReturnAmount = BigDecimal.ZERO;
		BigDecimal sumSaleAmount = BigDecimal.ZERO;
		BigDecimal sumSaleQuantity = BigDecimal.ZERO;
		BigDecimal sumReturnQuantity = BigDecimal.ZERO;
		BigDecimal sumAvgPri = BigDecimal.ZERO;
		int sumSaleTimes = 0;
		
		BigDecimal saleAmount = BigDecimal.ZERO;
		BigDecimal returnAmount = BigDecimal.ZERO;
		BigDecimal saleQuantity = BigDecimal.ZERO;
		BigDecimal returnQuantity = BigDecimal.ZERO;
		BigDecimal maxPri = BigDecimal.ZERO;
		BigDecimal minPri = BigDecimal.ZERO;
		BigDecimal avgPri = BigDecimal.ZERO;
		int saleTimes = 0;
		
		String [] itemInfo = null;
		String itemName = null;
		for (Map<String, Object> map : saleList) {
			itemName = "未知";
			itemInfo = MapUtil.getString(map, "item_info").split(",");
			if(itemInfo.length > 1){
				itemName = itemInfo[1];
			}
			map.put("item_name", itemName);
			
			maxPri = MapUtil.getBigDecimal(map, "max_pri").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			minPri = MapUtil.getBigDecimal(map, "min_pri").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			avgPri = MapUtil.getBigDecimal(map, "avg_pri").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			saleQuantity = MapUtil.getBigDecimal(map, "sale_quantity").setScale(RFQTYDIGIT, BigDecimal.ROUND_HALF_UP);
			returnQuantity = MapUtil.getBigDecimal(map, "return_quantity").setScale(RFQTYDIGIT, BigDecimal.ROUND_HALF_UP);
			saleAmount = MapUtil.getBigDecimal(map, "sale_amount").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			returnAmount = MapUtil.getBigDecimal(map, "return_amount").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			saleTimes = MapUtil.getInt(map, "sale_times");
			
			map.put("max_pri", maxPri);
			map.put("min_pri", minPri);
			map.put("avg_pri", avgPri);
			map.put("sale_quantity", saleQuantity);
			map.put("return_quantity", returnQuantity);
			map.put("sale_amount",saleAmount);
			map.put("return_amount",returnAmount);
			map.put("sale_times", saleTimes);
			
			sumReturnAmount = sumReturnAmount.add(returnAmount);
			sumSaleAmount = sumSaleAmount.add(saleAmount);
			sumReturnQuantity = sumReturnQuantity.add(returnQuantity);
			sumSaleQuantity = sumSaleQuantity.add(saleQuantity);
//			sumAvgPri = sumAvgPri.add(avgPri);
			sumSaleTimes = sumSaleTimes + saleTimes;
		}
		int listSize = saleList.size();
		if(listSize == 0){
			listSize = 1;
		}
		
		Map<String, Object> saleMap = new HashMap<String, Object>();
		if(sumSaleQuantity.compareTo(BigDecimal.ZERO) == 0){
			sumAvgPri = sumSaleAmount;
		}else{
			sumAvgPri = sumSaleAmount.divide(sumSaleQuantity,RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
		}
		
		saleMap.put("sale_list", saleList);
		saleMap.put("total_sale_amount", sumSaleAmount.setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP));
		saleMap.put("total_sale_quantity", sumSaleQuantity);
		saleMap.put("total_return_amount", sumReturnAmount.setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP));
		saleMap.put("total_return_quantity", sumReturnQuantity);
		saleMap.put("total_avg_pri", sumAvgPri.setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP));
		saleMap.put("total_sale_times", sumSaleTimes );
		return saleMap;
	}

//	 * 库存明细表----new
	@Override
	public Map<String, Object> searchWhseRecords(Map<String, Object> paramMap)throws Exception{
		LOG.debug("searchWhseRecords params:"+paramMap);
		
//		if(paramMap.containsKey("keyword")){
//			paramMap.put("key", MapUtil.getString(paramMap, "keyword"));
//			Map<String, Object> result = itemService.searchItemByLucene(paramMap);
//			StringBuffer itemIdBuffer = new StringBuffer();
//			List<Map<String, Object>> itemList = (List<Map<String, Object>>)MapUtil.get(result, "item_list", new ArrayList<Map<String, Object>>()); 
////					result.get("item_list");
//			int index = 1;
//			for(Map<String, Object> item : itemList) {
//				if(index++==1) {
//					itemIdBuffer.append(item.get("item_id").toString());
//				} else {
//					itemIdBuffer.append(","+item.get("item_id").toString());
//				}
//			}
//			paramMap.put("item_id", itemIdBuffer.toString());
//		}
		
		List<Map<String, Object>> whseList = statisticsDao.selectWhseRecords(paramMap);
		
		BigDecimal sumDispersion = BigDecimal.ZERO;
		BigDecimal sumBeginingWhseAmount = BigDecimal.ZERO;
		BigDecimal sumEndingWhseAmount = BigDecimal.ZERO;
		BigDecimal sumEndingWhseQuantity = BigDecimal.ZERO;
		BigDecimal sumBeginingWhseQuantity = BigDecimal.ZERO;
		BigDecimal sumWhseSaleRatio = BigDecimal.ZERO; 
		
		BigDecimal beginingWhseAmount = BigDecimal.ZERO;
		BigDecimal endingWhseAmount = BigDecimal.ZERO;
		BigDecimal endingWhseQuantity = BigDecimal.ZERO;
		BigDecimal beginingWhseQuantity = BigDecimal.ZERO;
		BigDecimal whseSaleRatio = BigDecimal.ZERO;
		BigDecimal dispersion = BigDecimal.ZERO;
		
		String [] itemInfo = null;
		String itemName = null;
		for (Map<String, Object> map : whseList) {
			itemName = "未知";
			itemInfo = MapUtil.getString(map, "item_info", "未知,未知").split(",");
			if(itemInfo.length > 1){
				itemName = itemInfo[1];
			}
			map.put("item_name", itemName);
			
			beginingWhseAmount = MapUtil.getBigDecimal(map, "begining_whse_amount").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			endingWhseAmount = MapUtil.getBigDecimal(map, "ending_whse_amount").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			endingWhseQuantity = MapUtil.getBigDecimal(map, "ending_whse_quantity").setScale(RFQTYDIGIT, BigDecimal.ROUND_HALF_UP);
			beginingWhseQuantity = MapUtil.getBigDecimal(map, "begining_whse_quantity").setScale(RFQTYDIGIT, BigDecimal.ROUND_HALF_UP);
			whseSaleRatio = MapUtil.getBigDecimal(map, "whse_sale_ratio").setScale(RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP);
			endingWhseQuantity = endingWhseQuantity.compareTo(BigDecimal.ZERO) < 0 ? new BigDecimal("0") : endingWhseQuantity ;
			beginingWhseQuantity = beginingWhseQuantity.compareTo(BigDecimal.ZERO) < 0 ? new BigDecimal("0") : beginingWhseQuantity;
			dispersion =endingWhseQuantity.subtract(beginingWhseQuantity); 
			beginingWhseAmount = beginingWhseAmount.compareTo(BigDecimal.ZERO) <= 0 ? new BigDecimal("0.00") : beginingWhseAmount;
			endingWhseAmount  = endingWhseAmount.compareTo(BigDecimal.ZERO) <= 0 ? new BigDecimal("0.00") : endingWhseAmount;
			
			map.put("begining_whse_amount",beginingWhseAmount);
			map.put("ending_whse_amount",endingWhseAmount);
			map.put("ending_whse_quantity",endingWhseQuantity);
			map.put("begining_whse_quantity",beginingWhseQuantity);
			map.put("whse_sale_ratio", whseSaleRatio.multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP)+"%");
			map.put("dispersion", dispersion);
			
			sumDispersion = sumDispersion.add(dispersion);
			sumWhseSaleRatio = sumWhseSaleRatio.add(whseSaleRatio);
			sumBeginingWhseQuantity = sumBeginingWhseQuantity.add(beginingWhseQuantity);
			sumEndingWhseQuantity = sumEndingWhseQuantity.add(endingWhseQuantity);
			sumEndingWhseAmount = sumEndingWhseAmount.add(endingWhseAmount);
			sumBeginingWhseAmount = sumBeginingWhseAmount.add(beginingWhseAmount);
		}
		int whseSize = whseList.size();
		if(whseSize == 0){
			whseSize = 1;
		}
		Map<String, Object> whseMap = new HashMap<String, Object>();
		whseMap.put("whse_list", whseList);
		whseMap.put("total_dispersion", sumDispersion);
		whseMap.put("total_whse_sale_ratio", sumWhseSaleRatio.divide(new BigDecimal(whseSize),RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP)+"%");
		whseMap.put("total_begining_whse_quantity", sumBeginingWhseQuantity);
		whseMap.put("total_ending_whse_quantity", sumEndingWhseQuantity);
		whseMap.put("total_ending_whse_amount", sumEndingWhseAmount);
		whseMap.put("total_begining_whse_amount", sumBeginingWhseAmount);
		return whseMap;
	}
	
	
//	 * 利润明细表---new
	@Override
	public Map<String, Object> searchProfitRecords(Map<String, Object> paramMap)throws Exception{
		LOG.debug("searchProfitRecords params:"+paramMap);
		/*//使用locu查询
		if(paramMap.containsKey("keyword")){
			paramMap.put("key", MapUtil.getString(paramMap, "keyword"));
			Map<String, Object> result = itemService.searchItemByLucene(paramMap);
			StringBuffer itemIdBuffer = new StringBuffer();
			List<Map<String, Object>> itemList = (List<Map<String, Object>>)MapUtil.get(result, "item_list", new ArrayList<Map<String, Object>>());
//					(List<Map<String, Object>>) result.get("item_list");
			int index = 1;
			for(Map<String, Object> item : itemList) {
				if(index++==1) {
					itemIdBuffer.append(item.get("item_id").toString());
				} else {
					itemIdBuffer.append(","+item.get("item_id").toString());
				}
			}
			paramMap.put("item_id", itemIdBuffer.toString());
		}*/
		
		List<Map<String, Object>> profitList =  statisticsDao.selectProfitRecords(paramMap);
		BigDecimal sumProfit = BigDecimal.ZERO;
		BigDecimal sumAvgCost = BigDecimal.ZERO; 
        BigDecimal sumAvgPri = BigDecimal.ZERO;
        BigDecimal sumAvgProfit = BigDecimal.ZERO;
        BigDecimal sumProfitCostRatio = BigDecimal.ZERO; 
        BigDecimal sumProfitSaleRatio = BigDecimal.ZERO;
		BigDecimal sumSaleQuantity = BigDecimal.ZERO;
		BigDecimal sumSaleAmount = BigDecimal.ZERO;
		BigDecimal sumCost = BigDecimal.ZERO;
		
		BigDecimal profit = BigDecimal.ZERO;
		BigDecimal avgCost = BigDecimal.ZERO; 
        BigDecimal avgPri = BigDecimal.ZERO;
        BigDecimal avgProfit = BigDecimal.ZERO;
        BigDecimal profitCostRatio = BigDecimal.ZERO; 
        BigDecimal profitSaleRatio = BigDecimal.ZERO;
		BigDecimal saleQuantity = BigDecimal.ZERO;
		BigDecimal saleAmount = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
		
        // 加工报表里的每行数据, 并汇总合计值
		for (Map<String, Object> map : profitList) {
			// 商品名称, 条码, 单位名称都是从同一个字段中解析出来的, 如果最后一个字段没有值, 则需要特殊处理成空串
			String[] itemInfo = MapUtil.getString(map, "item_info").split(",");
			String itemName = itemInfo[1];
			map.put("item_name", "".equals(itemName)?"未知":itemName);
			map.put("bar_code", itemInfo[2]);
			map.put("unit_name", itemInfo.length==4?itemInfo[3]:"");
			
			profit = MapUtil.getBigDecimal(map, "profit").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			avgCost = MapUtil.getBigDecimal(map, "avg_cost").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			avgPri = MapUtil.getBigDecimal(map, "avg_pri").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			avgProfit = MapUtil.getBigDecimal(map, "avg_profit").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			profitCostRatio = MapUtil.getBigDecimal(map, "profit_cost_ratio").setScale(RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP);
			profitSaleRatio = MapUtil.getBigDecimal(map, "profit_sale_ratio").setScale(RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP);
			saleQuantity = MapUtil.getBigDecimal(map, "sale_quantity").setScale(RFQTYDIGIT, BigDecimal.ROUND_HALF_UP);
			saleAmount = MapUtil.getBigDecimal(map, "sale_amount").setScale(RFQTYDIGIT, BigDecimal.ROUND_HALF_UP);
			cost = MapUtil.getBigDecimal(map, "cost").setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			
			map.put("cost", cost);
			map.put("avg_cost", avgCost);
			map.put("avg_pri", avgPri);
			map.put("avg_profit", avgProfit);
			map.put("profit_cost_ratio", profitCostRatio.multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP)+"%");
			map.put("profit_sale_ratio", profitSaleRatio.multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP)+"%");
			map.put("profit",profit);
			map.put("sale_quantity", saleQuantity);
			map.put("sale_amount", saleAmount);
			
			sumSaleAmount = sumSaleAmount.add(saleAmount); // 销售总额
			sumProfit = sumProfit.add(profit); // 利润总额
			sumCost = sumCost.add(cost); // 成本总额
			sumProfitCostRatio = sumProfitCostRatio.add(profitCostRatio);
			sumProfitSaleRatio = sumProfitSaleRatio.add(profitSaleRatio);
			sumSaleQuantity = sumSaleQuantity.add(saleQuantity); // 管理单位销售量
		}
		Map<String, Object> profitMap = new HashMap<String, Object>();
		int listSize = profitList.size();
		if(listSize == 0 ){
			listSize = 1;
		}
		BigDecimal totalProfitCostRatio = BigDecimal.ZERO;
		BigDecimal totalProfitSaleRatio = BigDecimal.ZERO;
		if(sumSaleQuantity.compareTo(BigDecimal.ZERO)==0){
			sumAvgPri = sumSaleAmount;
			sumAvgProfit = sumProfit; 
			sumAvgCost = sumCost;
		}else{
			sumAvgPri = sumSaleAmount.divide(sumSaleQuantity, RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			sumAvgProfit = sumProfit.divide(sumSaleQuantity, RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			sumAvgCost = sumCost.divide(sumSaleQuantity, RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			totalProfitCostRatio = sumProfit.divide(sumCost, RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
			totalProfitSaleRatio = sumProfit.divide(sumSaleAmount, RFPERCENTAGEDIGIT, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP);
		}
		profitMap.put("total_profit", sumProfit.setScale(RFAMTDIGIT, BigDecimal.ROUND_HALF_UP));
		profitMap.put("total_avg_cost", sumAvgCost);
		profitMap.put("total_avg_pri", sumAvgPri);
		profitMap.put("total_avg_profit", sumAvgProfit);
		profitMap.put("total_sale_amount", sumSaleAmount);
		profitMap.put("total_profit_cost_ratio", totalProfitCostRatio+"%");
		profitMap.put("total_profit_sale_ratio", totalProfitSaleRatio+"%");
		profitMap.put("profit_list", profitList);
		profitMap.put("total_sale_quantity", sumSaleQuantity);
		
		return profitMap;
	}
	
	//青岛卷烟投放策略
	@Override
	public List<Map<String, Object>> getImportitemQty(Map<String, Object> paramMap) throws Exception {

		//连接服务器获取数据
		String tobasrUrl = RetailConfig.getTobaccoServer() + "Importitem_qd/getImportitemQty";
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("item_type", MapUtil.getString(paramMap, "item_type"));		
		param.put("position_name", MapUtil.getString(paramMap, "position_name"));
		param.put("com_type", MapUtil.getString(paramMap, "com_type"));
		
		String json = HttpUtil.post(tobasrUrl, param);
		Map result = JsonUtil.json2Map(json);
		
		//解析数据
		String code = (String) result.get("code");
		// {item_name : {item_name:大鸡, 51档：0, 52档：0, 53档：10 ...}}
		Map<String, Map<String, Object>> itemDwMapList = new HashMap<String, Map<String, Object>>();
		if(code!=null && Constants.SUCCESS.equals(code)) {
			int index = 0;
			List<Map<String, Object>> custDwMap = (List<Map<String, Object>>) result.get("result");
			for (Map<String, Object> map : custDwMap) { // {item_name：大鸡, dw：51档, qty：0}
				String itemName = MapUtil.getString(map, "ITEM_NAME");
				String dw = MapUtil.getString(map, "DW");
				BigDecimal qty = MapUtil.getBigDecimal(map, "QTY");
				if(itemDwMapList.containsKey(itemName)) {
					Map<String, Object> DwInMap = MapUtil.get(itemDwMapList, itemName, Collections.EMPTY_MAP);
					DwInMap.put(dw, qty);
				} else {
					Map<String, Object> DwInMap = new HashMap<String, Object>();
					DwInMap.put("item_name", itemName);
					DwInMap.put(dw, qty);
					//itemInMap.put("index", index++);
					itemDwMapList.put(itemName, DwInMap);
				}
			}
			/*
			for(Map<String, Object> temp : itemMap.values()) {
				items.add(temp);
			}
			*/
		}
		return new ArrayList<Map<String, Object>>(itemDwMapList.values());
	}
	
	/**
	 * 搜索卷烟日结数据-----潍坊
	 * 通过时间
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> searchTobaccoDailyData(Map<String, Object> paramMap) throws Exception{
		LOG.debug("StatisticsServiceImpl searchTobaccoDailyData paramMap:"+paramMap);
		return statisticsDao.searchTobaccoDailyData(paramMap);
	}
	
}
