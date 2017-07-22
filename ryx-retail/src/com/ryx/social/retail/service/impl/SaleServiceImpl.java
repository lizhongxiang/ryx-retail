package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.ILotteryDao;
import com.ryx.social.retail.dao.ISaleOrderDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.ILotteryService;
import com.ryx.social.retail.service.IMerchConsumerService;
import com.ryx.social.retail.service.IOrderService;
import com.ryx.social.retail.service.IPromotionService;
import com.ryx.social.retail.service.ISaleService;
import com.ryx.social.retail.service.IUploadDataToRtmsService;
import com.ryx.social.retail.service.IWhseService;
import com.ryx.social.retail.util.MD5Util;
import com.ryx.social.retail.util.ParamUtil;

@Service
public class SaleServiceImpl implements ISaleService {
	private static final Logger LOG = LoggerFactory.getLogger(SaleServiceImpl.class);
	
	/**
	 * 如果判断是重复销售单则不插入
	 * 重复销售单的规则为: 单号/商户号/销售日期/销售时间/应收金额完全相同
	 */
	private static final int REPEATED_ORDER = 0;
	/**
	 * 无此单号则插入
	 */
	private static final int NO_ORDER = 1;
	/**
	 * 有此单号 但是 商户号不相同 或者 商户号相同销售日期/销售时间/应收金额不完全相同
	 */
	private static final int NOT_REPEATED_ORDER = 2;
	
	@Resource
	private ISaleOrderDao saleOrderDao;	
	@Resource
	private IWhseDao whseDao;
	@Resource
	private ILotteryDao lotteryDao;
	@Resource
	private ILotteryService lotteryService;
	@Resource
	private IUploadDataToRtmsService uploadDataToRtmsService;
	@Resource
	private IWhseService whseService;
	@Resource
	private IMerchConsumerService merchConsumerService;
	@Resource
	private IPromotionService promotionService;
	@Resource
	private IOrderService orderService;
	
	/**
	 * 查询销售单明细
	 */
	@Override
	public Map<String, Object> getSaleOrderDetail(Map<String, Object> saleOrderParam) throws Exception {
		LOG.debug("SaleServiceImpl getSaleOrderDetail saleOrderParam: " + saleOrderParam);
		return saleOrderDao.getSaleOrderDetail(saleOrderParam);
	}
	/**
	 * 查询销售单
	 */
	@Override
	public List<Map<String, Object>> selectSaleOrder(Map<String, Object> saleOrderParam) throws Exception {
		LOG.debug("SaleServiceImpl selectSaleOrder saleOrderParam: " + saleOrderParam);
		return saleOrderDao.selectSaleOrder(saleOrderParam);
	}
	
	/**
	 * 修改销售单 状态, 支付状态, 支付方式
	 */
	@Override
	public void updateSaleOrder(Map<String, Object>saleOrderParam)throws Exception{
		LOG.debug("(wgdd)SaleServiceImpl UpdateSaleOrder saleOrderParam: " + saleOrderParam);
		
		saleOrderDao.updateSaleOrder(saleOrderParam);//修改订单
		
		String status = MapUtil.getString(saleOrderParam, "status");
		LOG.debug("(wgdd)SaleServiceImpl UpdateSaleOrder status: " + status);
		
		if("03".equals(status)){ // 只要销售状态为03 则认为需要扣减库存, 原来还需要判断支付状态
			Map<String, Object> searchOrderParam = MapUtil.rename(saleOrderParam, "order_id", "page_index:-1", "page_size:-1");
			List<Map<String, Object>> saleOrderLineList= orderService.searchSaleOrderLineJoinItem(searchOrderParam);//销售单行
			
			List<Map<String, Object>> saleOrderDate = saleOrderDao.selectSaleOrder(searchOrderParam);//销售单
			
			Map<String, Object> saleOrderMap = new HashMap<String, Object>();
			if (saleOrderDate.size() >= 1 &&  saleOrderLineList.size() >= 1 ) {
				
				saleOrderMap = saleOrderDate.get(0);
				
				for(Map<String, Object> saleOrderLine: saleOrderLineList) {//组织数据
					saleOrderLine.put("qty_sub", MapUtil.getBigDecimal(saleOrderLine, "qty_ord"));
					saleOrderLine.put("sale_quantity", MapUtil.getBigDecimal(saleOrderLine, "qty_ord"));
					saleOrderLine.put("sale_amount", MapUtil.getBigDecimal(saleOrderLine, "amt_ord"));
					saleOrderLine.put("big_pri4", MapUtil.getBigDecimal(saleOrderLine, "big_pri3"));
				}
				saleOrderMap.put("list", saleOrderLineList);
				
				whseDao.updateWhseMerch(saleOrderMap);//修改库存
				
				saleOrderMap.putAll(MapUtil.rename(saleOrderMap, "amtys_ord_total.total_amount", "amt_ord_total.received_amount"));
				saleOrderMap.put("ip", MapUtil.getString(saleOrderParam, "ip"));
				if (MapUtil.getString(saleOrderMap, "order_type").equals("04") ) { //向rtms上传销售单
					uploadDataToRtmsService.submitSaleOrder2RTMS(saleOrderMap);//卷烟销售单上传
				}
			}
		}
	}
	
	/**
	 * 批量提交销售单, 并减库存
	 */
	@SuppressWarnings({"unchecked","unused"})
	@Override
	public Map<String, Object> submitBatchSaleOrder(Map<String, Object> paramMap,List<Map<String, Object>> paramList) throws Exception {
		LOG.debug("SaleServiceImpl submitSaleOrder paramMap: " + paramList);
		String merchId = MapUtil.getString(paramMap, "merch_id");//商户id
		List<Map<String, Object>> orderParamList = new ArrayList<Map<String, Object>>();//销售单,销售单行,卷烟销售单上传itms
		List<Map<String, Object>> recordList = new ArrayList<Map<String,Object>>();//促销流水
		List<Map<String, Object>> whseList = new ArrayList<Map<String, Object>>();//库存
		List<Map<String, Object>> consumerList = new ArrayList<Map<String, Object>>();//会员信息
		/*1.处理数据*/
		for (Map<String, Object> map : paramList) {
			map.put("merch_id", merchId);//
			batchSubmitPCSaleOrderTool(paramMap, map, orderParamList, recordList, whseList, consumerList);//加工数据
		}
		/*2.提交数据*/
		insertBatchSaleOrder(orderParamList);//批量提交销售单
		insertBatchSaleOrderLine(orderParamList);//批量提交销售单行
		whseService.modifyBatchWhseMerch(whseList);//批量提交库存
		uploadDataToRtmsService.submitBatchSaleOrder2RTMS(orderParamList);//卷烟销售单批量上传
		if(recordList.size()>0){
			promotionService.insertMerchPromotionRecord(recordList);//批量提交销售流水
		}
		if(consumerList.size()>0){
			merchConsumerService.updateBatchMerchConsumer(consumerList);//批量提交会员信息
		}
		return null;
	}
	
	/**
	 * 提交销售单, 并减库存
	 */
//	@Override
	public Map<String, Object> submitSaleOrder2(Map<String, Object> paramMap) throws Exception {
		LOG.debug("SaleServiceImpl submitSaleOrder paramMap: " + paramMap);
		List<Map<String, Object>> itemList = MapUtil.get(paramMap, "list", null);
		if(itemList==null || itemList.isEmpty()){
			// 如果没有销售单行在日志上记录信息不保存销售记录 by 梁凯 2014-7-11
			LOG.debug(MapUtil.getString(paramMap, "order_id") + "销售单无销售的商品信息！");
			return Collections.EMPTY_MAP;
		}
		
		String merchId = MapUtil.getString(paramMap, "merch_id"); 
		String orderId = MapUtil.getString(paramMap, "order_id", IDUtil.getId());
		String consumerId = "";
		if(paramMap.containsKey("consumer_id")){
			consumerId = MapUtil.getString(paramMap, "consumer_id");
			if(!paramMap.containsKey("consumer_rebate_amount")){
				paramMap.put("consumer_rebate_amount", jsHuiYuanRangLi(paramMap));//计算会员让利金额
			}
		}
		
		//查询促销信息
		Map<String, Object> searchPromotionParam = new HashMap<String, Object>();
		searchPromotionParam.put("merch_id", merchId);
		searchPromotionParam.put("status", "1");
		//查询未过期促销
		searchPromotionParam.put("is_clash", "1");
		searchPromotionParam.put("start_date", DateUtil.getToday());
		List<Map<String, Object>> promotionList = promotionService.searchMerchPromotion(searchPromotionParam);//所有可用促销
		Map<String, Map<String, Object>> promotionMap  = new HashMap<String, Map<String,Object>>();;//促销map
		for (Map<String, Object> map : promotionList) {
			promotionMap.put(MapUtil.getString(map, "promotion_id"), map);
		}
		
		List<Map<String, Object>> recordList = new ArrayList<Map<String,Object>>();//插入促销流水
		BigDecimal sumAdjustedAmount = BigDecimal.ZERO;//单行总折扣金额 
		BigDecimal adjustedAmount = BigDecimal.ZERO;//单行折扣金额
		//"amtys_ord_total":"54.00","amt_ord_total":"54.00","order_time":"182133","pay_type":"1","amt_ord_change":"0.00"
		BigDecimal amtOrdChange = MapUtil.getBigDecimal(paramMap, "change_amount"); // 找零
		LOG.debug("找零：----------:"+amtOrdChange+","+MapUtil.getBigDecimal(paramMap, "amt_ord_change"));
		// 兼容智能终端找零
//		if(amtOrdChange.compareTo(BigDecimal.ZERO)==0) {
//			BigDecimal oldChange = MapUtil.getBigDecimal(paramMap, "amt_ord_change");
//			if(oldChange.compareTo(BigDecimal.ZERO)!=0) {
//				amtOrdChange = oldChange;
//				// 赋值给change_amount
//				paramMap.put("change_amount", amtOrdChange);
//			}
//		}
		BigDecimal amtOrdTotal = MapUtil.getBigDecimal(paramMap, "received_amount"); // 实收
		BigDecimal amtysOrdTotal = MapUtil.getBigDecimal(paramMap, "total_amount", amtOrdTotal); // 应收
		LOG.debug("应收：--------------"+amtysOrdTotal+","+MapUtil.getBigDecimal(paramMap, "amtys_ord_total"));
		LOG.debug("实收：--------------"+amtOrdTotal+","+MapUtil.getBigDecimal(paramMap, "amt_ord_total"));
		// 兼容智能终端实收
//		if(amtOrdTotal.compareTo(BigDecimal.ZERO)==0) {
//			BigDecimal oldReceived = MapUtil.getBigDecimal(paramMap, "amt_ord_total");
//			if(oldReceived.compareTo(BigDecimal.ZERO)!=0) {
//				amtOrdTotal = oldReceived;
//				// 赋值给received_amount
//				paramMap.put("received_amount", amtOrdTotal);
//			}
//		}
		// 兼容智能终端应收
//		if(amtysOrdTotal.compareTo(BigDecimal.ZERO)==0) {
//			BigDecimal oldTotal = MapUtil.getBigDecimal(paramMap, "amtys_ord_total");
//			if(oldTotal.compareTo(BigDecimal.ZERO)!=0) {
//				amtysOrdTotal = oldTotal;
//				// 赋值给total_amount
//				paramMap.put("total_amount", amtysOrdTotal);
//			}
//		}
		BigDecimal totalRebateAmount = MapUtil.getBigDecimal(paramMap, "total_rebate_amount");//单 总折扣金额
		BigDecimal amtOrdLoss = BigDecimal.ZERO;//抹零
		BigDecimal amtOrd = BigDecimal.ZERO;//单行应收金额
		BigDecimal amtOrdProfit = BigDecimal.ZERO; // 销售单利润, 通过行表计算，
		BigDecimal qtyOrdTotal = BigDecimal.ZERO; //  销售单商品个数, 通过行表计算
		StringBuilder orderNoteBuilder = new StringBuilder(); //单备注
		
		if(!"02".equals(MapUtil.getString(paramMap, "pmt_status"))) {
			if(amtOrdChange.compareTo(BigDecimal.ZERO) ==-1){
				amtOrdLoss = amtysOrdTotal.subtract(amtOrdTotal); // 抹零 = 应收+找零-实付(正数：亏损，负数：净赚)
			}else{
				amtOrdLoss = amtysOrdTotal.add(amtOrdChange).subtract(amtOrdTotal); // 抹零 = 应收+找零-实付(正数：亏损，负数：净赚)
			}
		}
		BigDecimal sumOrderAmount = amtysOrdTotal.add(totalRebateAmount).add(amtOrdLoss);//未折扣金额
		
		int sumAdjustedPoint = MapUtil.getInt(paramMap, "adjusted_point", 0);//得到的积分（增或减）
		
		int index = 0;
		try {
			recordList.addAll(setPromotionRecordParams(paramMap, paramMap));//插入促销流水，数据组件
		} catch (Exception e) {
			paramMap.put("promotion_ids", setPromotionIds(paramMap, promotionMap));//计算promotion_is数据
			recordList.addAll(setPromotionRecordParams(paramMap, paramMap));//插入促销流水，数据组件
		}
		
		for (Map<String, Object> map : recordList) {
			if(MapUtil.getString(map, "promotion_type").equals("40")){
				sumAdjustedAmount = sumAdjustedAmount.add(MapUtil.getBigDecimal(map, "promotion_key"));
			}
		}
		
		//除商品折扣的折扣金额 = 会员折扣+整单折扣+抹零
		sumAdjustedAmount = sumAdjustedAmount.add(MapUtil.getBigDecimal(paramMap, "consumer_rebate_amount").add(amtOrdLoss));
		BigDecimal sumAdjustedAmount2 = BigDecimal.ZERO;
		//当前行 分配促销值（用于退货（单促销+会员的金额分配））
		BigDecimal otherLineAdjustedAmount = BigDecimal.ZERO;
		
		for(Map<String, Object> itemMap : itemList) {
			
			// order.putAll(do2(order, itemMap));
			
			// cuxiao.putAll(do3(cuxiao, itemMap));
			
			if(index++!=0) {
				orderNoteBuilder.append(",");
			}
			// 兼容line_num
			itemMap.put("line_num", MapUtil.getInt(itemMap, "line_num",  index));
			LOG.debug("line_num：--------------"+MapUtil.getInt(itemMap, "line_num")+","+ MapUtil.getInt(itemMap, "line_label"));
			//当前行 的 商品促销金额
			adjustedAmount = MapUtil.getBigDecimal(itemMap, "line_rebate_amount");
			amtOrd = MapUtil.getBigDecimal(itemMap, "sale_amount");//单行应收（未折扣
			LOG.debug("line_num：--------------"+MapUtil.getInt(itemMap, "line_num")+","+ MapUtil.getInt(itemMap, "line_label"));
			LOG.debug("当行应收：--------------"+amtOrd+","+ MapUtil.getInt(itemMap, "amt_ord"));
			// 兼容智能终端销售金额
//			if(amtOrd.compareTo(BigDecimal.ZERO)==0) {
//				BigDecimal oldAmount = MapUtil.getBigDecimal(itemMap, "amt_ord");
//				if(oldAmount.compareTo(BigDecimal.ZERO)!=0) {
//					amtOrd = oldAmount;
//					// 将金额赋值给sale_amount
//					itemMap.put("sale_amount", amtOrd);
//				}
//			}
			itemMap.put("merch_id", merchId);
			itemMap.put("order_id", orderId);
			itemMap.put("consumer_id", consumerId);
			itemMap.put("amt_ord", amtOrd);
			
			itemMap.put("sale_amount", amtOrd);
			
			if(itemList.size() == index){//最后一行分配促销金额：用差计算
				otherLineAdjustedAmount = sumAdjustedAmount.subtract(sumAdjustedAmount2);
			}else{//向单行分配单促销（促销，会员）金额，用于退货
				otherLineAdjustedAmount = ((amtOrd).divide(sumOrderAmount,4, BigDecimal.ROUND_HALF_UP)).multiply(sumAdjustedAmount).setScale(3, BigDecimal.ROUND_HALF_UP);
			}
			BigDecimal itemPrice = amtOrd.subtract(adjustedAmount).subtract(otherLineAdjustedAmount);//销售单行折扣后金额
			sumAdjustedAmount2 = sumAdjustedAmount2.add(otherLineAdjustedAmount);
			itemMap.put("other_adjusted_amount", otherLineAdjustedAmount);
			
			try {
				recordList.addAll(setPromotionRecordParams(itemMap, itemMap));//插入促销流水，数据组件
			} catch (Exception e) {
				itemMap.put("promotion_ids", setPromotionIds(itemMap, promotionMap));
				recordList.addAll(setPromotionRecordParams(paramMap, paramMap));//插入促销流水，数据组件
			}
			
			//促销积分
			sumAdjustedPoint = sumAdjustedPoint + MapUtil.getInt(itemMap, "adjusted_point", 0);
			
			// 销售单行，销售商品数量（如果没有传qty_ord字段则报错）
			BigDecimal qtyOrd = MapUtil.getBigDecimal(itemMap, "sale_quantity");
			LOG.debug("当行数量：--------------"+qtyOrd+","+ MapUtil.getInt(itemMap, "qty_ord"));
			// 兼容智能终端销售数量
//			if(qtyOrd.compareTo(BigDecimal.ZERO)==0) {
//				BigDecimal oldQuantity = MapUtil.getBigDecimal(itemMap, "qty_ord");
//				if(oldQuantity.compareTo(BigDecimal.ZERO)!=0) {
//					qtyOrd = oldQuantity;
//					// 将数量赋值给sale_quantity
//					itemMap.put("sale_quantity", qtyOrd);
//				}
//			}
			if(qtyOrd.compareTo(BigDecimal.ZERO)==0){
				throw new RuntimeException("销售单" + MapUtil.getString(paramMap, "order_id") + "没有销售此商品的数量或者销售了0个商品" + MapUtil.getString(itemMap, "item_id"));
			}
			
			
			// 销售单行，销售商品转换比（如果没有传unit_ratio字段则报错）
			BigDecimal unitRatio = MapUtil.getBigDecimal(itemMap, "unit_ratio");
			if(unitRatio.compareTo(BigDecimal.ZERO)==0){
				throw new RuntimeException("销售单" + MapUtil.getString(paramMap, "order_id") + "中商品" + MapUtil.getString(itemMap, "item_id") + "的转换比为0或者没有转换比");
			}
			// 单品售价 = amt_ord / qty_ord / unit_ratio（用于计算利润）
			BigDecimal price = itemPrice.divide(qtyOrd, 5, BigDecimal.ROUND_HALF_UP).divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP);
			// 单品利润  = 单品售价(以扣减调整额) - 单品成本
			BigDecimal profit = price.subtract(MapUtil.getBigDecimal(itemMap, "cost"));
			itemMap.put("profit", profit);
			// 销售单总利润 = 单品利润 * 转换比 * 销售数量
			amtOrdProfit = amtOrdProfit.add(profit.multiply(unitRatio).multiply(qtyOrd));
			// 销售单总商品数
			qtyOrdTotal = qtyOrdTotal.add(qtyOrd);
			
			orderNoteBuilder.append(MapUtil.getString(itemMap, "item_name"));
		}
		//插入促销流水
		if(recordList.size() >0){
			promotionService.insertMerchPromotionRecord(recordList);//插入促销流水
		}
		
		String note = "lzx";
		if(MapUtil.getString(paramMap, "order_date").equals(DateUtil.getToday())){
			if(orderNoteBuilder.length()>500){
				orderNoteBuilder = new StringBuilder(orderNoteBuilder.substring(0, 500)).append("...");
			}
			note = orderNoteBuilder.toString();
		}
		
		paramMap.put("qty_ord_count", itemList.size());
		paramMap.put("amt_ord_loss", amtOrdLoss);
		paramMap.put("amt_ord_profit", amtOrdProfit);
		paramMap.put("qty_ord_total", qtyOrdTotal);
		paramMap.put("amtys_ord_total", amtysOrdTotal);
		paramMap.put("amt_ord_total", amtOrdTotal);
		paramMap.put("note", note);
		
		this.insertSaleOrder(paramMap);//增加销售单
		this.insertSaleOrderLine(paramMap);//增加销售单行
		
		// 组织库存的map，修改库存
		if("03".equals(MapUtil.getString(paramMap, "status"))) {
			Map<String, Object> whseParam = new HashMap<String, Object>();
			whseParam = setWhseMerchParams(paramMap);
			whseService.modifyWhseMerch(whseParam);//修改库存
		}
		
		List<String> activityList=getLottery(paramMap);
		Map<String,Object> dataMap=new HashMap<String,Object>();
		dataMap.put("list", activityList);
		dataMap.put("itms",new HashMap<String, Object>());
		
		//调整会员积分
		BigDecimal consumerPoint = MapUtil.getBigDecimal(paramMap, "consumer_point");
		if(!StringUtil.isBlank(consumerId) && consumerPoint.compareTo(BigDecimal.ZERO)>0){
			Map<String, Object> consumerMap = setConsumerPointParams(paramMap);
			merchConsumerService.updateMerchConsumer(consumerMap);//修改会员积分
		}
		
		// 状态是完成的才给rtms
		if("03".equals(paramMap.get("status"))) {
			uploadDataToRtmsService.submitSaleOrder2RTMS(paramMap);//卷烟销售单上传
		}
		
		return dataMap;
	}
	
	/**
	 * 处理销售单
	 * @param promotonMap:促销map
	 * @param paramMap:销售单map
	 * @return
	 */
	private Map<String, Object> buildOrderParams(Map<String, Map<String, Object>> promotionMap, Map<String, Object> paramMap, List<Map<String, Object>> promoList) throws Exception {
		LOG.debug("SaleServiceImpl buildOrderParams paramMap: " + paramMap);
		//TODO 销售单
		List<Map<String, Object>> itemList = MapUtil.get(paramMap, "list", null);
		String consumerId = MapUtil.getString(paramMap, "consumer_id", "");
		
		BigDecimal amtOrdTotal = MapUtil.getBigDecimal(paramMap, "received_amount"); // 实收
		BigDecimal amtysOrdTotal = MapUtil.getBigDecimal(paramMap, "total_amount", amtOrdTotal); // 应收（实际应收
		BigDecimal inputAmtysOrdTotal = MapUtil.getBigDecimal(paramMap, "input_received_amount", amtysOrdTotal);//输入的应收
		BigDecimal amtOrdChange = amtOrdTotal.subtract(inputAmtysOrdTotal);//找零 (实收 - 输入应收
		BigDecimal amtOrdLoss = amtysOrdTotal.subtract(inputAmtysOrdTotal);//抹零 (实际应收-输入应收,(正数：亏损，负数：净赚)
		BigDecimal totalRebateAmount = MapUtil.getBigDecimal(paramMap, "total_rebate_amount").setScale(3, BigDecimal.ROUND_HALF_UP);//单，单行 总折扣金额
		totalRebateAmount = totalRebateAmount.add(amtOrdLoss);//单、单行总折扣金额
		BigDecimal amtOrdProfit = BigDecimal.ZERO; // 销售单利润, 通过行表计算，
		BigDecimal qtyOrdTotal = BigDecimal.ZERO; //  销售单商品个数, 通过行表计算
		StringBuilder orderNoteBuilder = new StringBuilder(); //单备注
		
		BigDecimal consumerRebateAmount = BigDecimal.ZERO;
		if(!StringUtil.isBlank(consumerId) ) {
			consumerRebateAmount = jsHuiYuanRangLi(paramMap);
		}
		
//		List<Map<String, Object>> recordList = new ArrayList<Map<String,Object>>();
//		
//		try {
//			recordList.addAll(setPromotionRecordParams(paramMap, paramMap));//插入促销流水，数据组件
//		} catch (Exception e) {
//			paramMap.put("promotion_ids", setPromotionIds(paramMap, promotionMap));//计算promotion_is数据
//			recordList.addAll(setPromotionRecordParams(paramMap, paramMap));//插入促销流水，数据组件
//		}
		
		BigDecimal orderPromotionAmt = BigDecimal.ZERO;//销售单促销金额
		for (Map<String, Object> map : promoList) {
			if(MapUtil.getString(map, "promotion_type").equals("40")){
				orderPromotionAmt = orderPromotionAmt.add(MapUtil.getBigDecimal(map, "promotion_key"));
			}
		}
		
//		if(!"02".equals(MapUtil.getString(paramMap, "pmt_status"))) {
//			if(amtOrdChange.compareTo(BigDecimal.ZERO) ==-1){
//				// 抹零 = 应收+找零-实付(正数：亏损，负数：净赚)+(实际应收-输入应收)
//				amtOrdLoss = inputAmtysOrdTotal.subtract(amtOrdTotal).add(moLing); 
//			}else{
//				// 抹零 = 应收+找零-实付(正数：亏损，负数：净赚)+(实际应收-输入应收)
//				amtOrdLoss = inputAmtysOrdTotal.add(amtOrdChange).subtract(amtOrdTotal).add(moLing); 
//			}
//			totalRebateAmount = totalRebateAmount.add(amtOrdLoss);//单、单行总折扣金额
//		}
		
		//未折扣金额 = 输入应收 + 所有优惠（ 抹零包含在优惠中）
		BigDecimal sumOrderAmount = (inputAmtysOrdTotal.add(totalRebateAmount)).setScale(3, BigDecimal.ROUND_HALF_UP);
		
		//销售单总折扣金额 = 会员折扣+整单折扣+抹零(不包含单行优惠
		BigDecimal orderAdjustedAmt = consumerRebateAmount.add(orderPromotionAmt).add(amtOrdLoss);
		
		Map<String, Object> newOrderMap = new HashMap<String, Object>();
		newOrderMap.put("order_id", MapUtil.getString(paramMap, "order_id"));
		newOrderMap.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		newOrderMap.put("consumer_id", consumerId);
		newOrderMap.put("qty_ord_count", itemList.size());
		newOrderMap.put("amt_ord_loss", amtOrdLoss);//抹零
		newOrderMap.put("amt_ord_profit", amtOrdProfit);//总利润（单行计算
		newOrderMap.put("qty_ord_total", qtyOrdTotal);//总数量（单行计算
		newOrderMap.put("amtys_ord_total", inputAmtysOrdTotal);//应收（输入
		newOrderMap.put("amt_ord_total", amtOrdTotal);//实收
		newOrderMap.put("note", orderNoteBuilder.toString());//备注(单行计算
		newOrderMap.put("order_type", MapUtil.getString(paramMap, "order_type", "01"));
		newOrderMap.put("pay_type", MapUtil.getString(paramMap, "pay_type", "1"));
		newOrderMap.put("order_date", MapUtil.getString(paramMap, "order_date", DateUtil.getToday()));
		newOrderMap.put("order_time", MapUtil.getString(paramMap, "order_time", DateUtil.getCurrentTimeMillisAsString("HHmmss")));
		newOrderMap.put("status", MapUtil.getString(paramMap, "status", "03"));
		newOrderMap.put("pmt_status", MapUtil.getString(paramMap, "pmt_status", "03"));
		newOrderMap.put("change_amount", amtOrdChange);//找零
		newOrderMap.put("total_rebate_amount", totalRebateAmount);//总折扣
		newOrderMap.put("operator", MapUtil.getString(paramMap, "operator"));
		newOrderMap.put("consumer_rebate_amount", consumerRebateAmount);//计算会员让利金额
		newOrderMap.put("actual_amtys_ord_total", amtysOrdTotal);//应收（实际应收
		newOrderMap.put("order_adjusted_amount", orderAdjustedAmt);//销售单总折扣金额 = 会员折扣+整单折扣+抹零（不包括单行
		newOrderMap.put("order_amt_total", sumOrderAmount);//销售总金额，未折扣
		
		return newOrderMap;
	}
	
	/**
	 * 处理销售单行
	 * @param paramMap
	 * @return
	 */
	private Map<String, Object> buildOrderLineParams(Map<String, Object> orderMap, Map<String, Object> itemMap ) throws Exception {
//		LOG.debug("SaleServiceImpl buildOrderLIneParams itemMap: " + itemMap);
		
		List<Map<String, Object>> lineList = MapUtil.get(orderMap, "list", new ArrayList<Map<String, Object>>());
		int lineNum = MapUtil.getInt(itemMap, "line_num", lineList.indexOf(itemMap)+1);
		StringBuilder orderNoteBuilder = new StringBuilder(MapUtil.getString(orderMap, "note"));
		if( 1 != lineNum ) {
			orderNoteBuilder.append(",");
		}
		orderNoteBuilder.append(MapUtil.getString(itemMap, "item_name"));
		
		String merchId = MapUtil.getString(orderMap, "merch_id");
		String orderId = MapUtil.getString(orderMap, "order_id");
		
		BigDecimal adjustedAmount = MapUtil.getBigDecimal(itemMap, "line_rebate_amount");//单行促销金额
		adjustedAmount = adjustedAmount.setScale(3, BigDecimal.ROUND_HALF_UP); 
		BigDecimal amtOrd = MapUtil.getBigDecimal(itemMap, "sale_amount");//单行应收（未折扣
		BigDecimal saleQuantity = MapUtil.getBigDecimal(itemMap, "sale_quantity");//单行数量（如果没有传qty_ord字段则报错
		
		//销售单折扣 = 会员折扣 + 单折扣(满减) + 抹零（不包括单行
		BigDecimal orderAdjustedAmount = MapUtil.getBigDecimal(orderMap, "order_adjusted_amount");
		BigDecimal sumAdjustedAmount = MapUtil.getBigDecimal(orderMap, "other_adjusted_amount_total");//单行分配折扣金额，每行进行相加，用于计算最后一次
		BigDecimal otherLineAdjustedAmount = BigDecimal.ZERO;
		
		if(lineNum == MapUtil.getInt(itemMap, "item_size")){//最后一行分配促销金额：用差计算
			otherLineAdjustedAmount = orderAdjustedAmount.subtract(sumAdjustedAmount);
		}else{//向单行分配单总折扣金额（促销，会员， 抹零）金额，用于退货（单行金额·未折扣 / 总单金额·未折扣 * 单总折扣金额）
			BigDecimal orderAmtTotal = MapUtil.getBigDecimal(orderMap, "order_amt_total");//销售总金额，未折扣
			if (orderAmtTotal.compareTo(BigDecimal.ZERO) != 0) {
				otherLineAdjustedAmount = amtOrd.divide(orderAmtTotal, 4 , BigDecimal.ROUND_HALF_UP).multiply(orderAdjustedAmount).setScale(3, BigDecimal.ROUND_HALF_UP);
			}
		}
		
		//TODO 销售单行
		if(saleQuantity.compareTo(BigDecimal.ZERO)==0){
			throw new RuntimeException("销售单" + orderId + "没有销售此商品的数量或者销售了0个商品" + MapUtil.getString(itemMap, "item_id"));
		}
		
		// 销售单行，销售商品转换比（如果没有传unit_ratio字段则报错）
		BigDecimal unitRatio = MapUtil.getBigDecimal(itemMap, "unit_ratio", 1);
		if(unitRatio.compareTo(BigDecimal.ZERO)==0){
			throw new RuntimeException("销售单" + orderId + "中商品" + MapUtil.getString(itemMap, "item_id") + "的转换比为0或者没有转换比");
		}
		
		//销售单行折扣后金额 = 未折扣金额 - 折扣金额 - 单行金额
		BigDecimal itemPrice = amtOrd.subtract(adjustedAmount).subtract(otherLineAdjustedAmount);
		
		// 单品售价 = amt_ord / qty_ord / unit_ratio（用于计算利润）
		BigDecimal price = itemPrice.divide(saleQuantity, 5, BigDecimal.ROUND_HALF_UP).divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP);
		
		// 单品利润  = 单品售价(以扣减调整额) - 单品成本
		BigDecimal profit = price.subtract(MapUtil.getBigDecimal(itemMap, "cost"));
		itemMap.put("profit", profit);
		
		// 销售单总利润 = 单品利润 * 转换比 * 销售数量
		BigDecimal lineProfitTotal = profit.multiply(unitRatio).multiply(saleQuantity);
		
		sumAdjustedAmount = sumAdjustedAmount.add(otherLineAdjustedAmount);
		
		orderMap.put("note", orderNoteBuilder.toString());//备注
		orderMap.put("amt_ord_profit", MapUtil.getBigDecimal(orderMap, "amt_ord_profit").add(lineProfitTotal));//总利润
		orderMap.put("qty_ord_total", MapUtil.getBigDecimal(orderMap, "qty_ord_total").add(saleQuantity));//总数量
		orderMap.put("other_adjusted_amount_total", sumAdjustedAmount);
		
		Map<String, Object> lineParam = new HashMap<String, Object>();
		lineParam.put("line_num", lineNum);
		lineParam.put("merch_id", merchId);
		lineParam.put("order_id", orderId);
		lineParam.put("item_id", MapUtil.getString(itemMap, "item_id"));
		lineParam.put("item_name", MapUtil.getString(itemMap, "item_name"));
		lineParam.put("pri4", MapUtil.getBigDecimal(itemMap, "pri4"));
		lineParam.put("cost", MapUtil.getBigDecimal(itemMap, "cost"));
		lineParam.put("big_bar", MapUtil.getString(itemMap, "big_bar"));
		lineParam.put("item_bar", MapUtil.getString(itemMap, "item_bar"));
		lineParam.put("unit_ratio", MapUtil.getString(itemMap, "unit_ratio"));
		lineParam.put("big_pri4", MapUtil.getBigDecimal(itemMap, "big_pri4"));
		lineParam.put("item_unit_name", MapUtil.getString(itemMap, "item_unit_name"));
		lineParam.put("item_kind_id", MapUtil.getString(itemMap, "item_kind_id"));
		lineParam.put("big_unit_name", MapUtil.getString(itemMap, "big_unit_name"));
		lineParam.put("profit", profit);//单行利润（小包装，单数量
		lineParam.put("sale_amount", amtOrd);//单行总金额（未折扣
//		lineParam.put("amt_ord", amtOrd);//单行总金额（未折扣
		lineParam.put("sale_quantity", saleQuantity);//单行销售数量
		lineParam.put("line_rebate_amount", adjustedAmount);//单行折扣金额（*数量
		lineParam.put("other_adjusted_amount", otherLineAdjustedAmount);//单行分配金额（*数量
		lineParam.put("note", "");//备注
		
		lineList.add(lineParam);
		orderMap.put("list", lineList);
		return orderMap;
	}
	
	/**
	 * 处理销售流水
	 * @param promotionMap:可用促销map
	 * @param paramMap：组建map
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> buildPromotionParams(Map<String, Map<String, Object>> promotionMap, Map<String, Object> paramMap, Map<String, Object> lineItemMap) throws Exception  {
		
		try {
			return setPromotionRecordParams(paramMap, lineItemMap);//插入促销流水，数据组件
		} catch (Exception e) {
			paramMap.put("promotion_ids", setPromotionIds(paramMap, promotionMap));
			return setPromotionRecordParams(paramMap, lineItemMap);//插入促销流水，数据组件
		}
	}
	
	/**
	 * 调整会员积分
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> buildConsumerParams(Map<String, Object> paramMap) throws Exception {
		LOG.debug("SaleServiceImpl buildConsumerParams paramMap:"+paramMap);
		//调整会员积分
		String consumerId = MapUtil.getString(paramMap, "consumer_id", null);
		if(StringUtil.isBlank(consumerId)){
			return null;
		}
		BigDecimal consumerPoint = MapUtil.getBigDecimal(paramMap, "consumer_point");
		Map<String, Object> consumerMap = new HashMap<String, Object>();
		if(!StringUtil.isBlank(consumerId) && consumerPoint.compareTo(BigDecimal.ZERO)>0){
			consumerMap.put("consumer_id", consumerId);
			consumerMap.put("adjusted_point", consumerPoint);
			consumerMap.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		}
		return consumerMap;
		
	}
	
	/**
	 * 处理库存
	 * @param paramMap
	 * @return
	 */
	private Map<String, Object> buildWhseParams(Map<String, Object> orderMap, Map<String, Object> itemMap) {
		LOG.debug("SaleServiceImpl buildWhseParams orderMap:"+orderMap);
		LOG.debug("SaleServiceImpl buildWhseParams itemMap:"+itemMap);
		Map<String, Object> whseMap = new HashMap<String, Object>();
		whseMap.put("item_id", MapUtil.getString(itemMap, "item_id"));
		whseMap.put("merch_id", MapUtil.getString(orderMap, "merch_id"));
		BigDecimal unitRatio = MapUtil.getBigDecimal(itemMap, "unit_ratio");
		BigDecimal saleQty = MapUtil.getBigDecimal(itemMap, "sale_quantity");
		BigDecimal whseQty = saleQty.multiply(unitRatio);
		
		whseMap.put("qty_sub", whseQty);
		return whseMap;
	}
	
	/**
	 * 查询可用促销信息,（未过期，有效）
	 * 获得可用的促销map
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	private Map<String, Map<String, Object>> getUsablePromotion(Map<String, Object>paramMap) throws Exception {
		//查询可用促销信息,（未过期，有效）
		Map<String, Object> searchPromotionParam = new HashMap<String, Object>();
		searchPromotionParam.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
		searchPromotionParam.put("status", "1");
		searchPromotionParam.put("is_clash", "1");
		searchPromotionParam.put("start_date", DateUtil.getToday());
		List<Map<String, Object>> promotionList = promotionService.searchMerchPromotion(searchPromotionParam);//所有可用促销
		Map<String, Map<String, Object>> promotionMap  = new HashMap<String, Map<String,Object>>();;//促销map
		for (Map<String, Object> map : promotionList) {
			promotionMap.put(MapUtil.getString(map, "promotion_id"), map);
		}
		return promotionMap;
	}
	
	/**
	 * 判断销售单是否存在
	 * 0:不插入
	 * 1：插入（不改 order_id）
	 * 2：插入（修改order_id）
	 * 
	 * @param paramMap
	 * @return 是否插入
	 * @throws Exception
	 */
	public int isRepeatedSaleOrder(Map<String, Object> paramMap)throws Exception{
		LOG.debug("saleServiceImpl isInsertSaleOrder paramMap:"+paramMap);
		//查询销售单
		Map<String, Object> searchSaleOrder = MapUtil.remain(paramMap, "order_id");
		List<Map<String, Object>> saleOrderList = this.selectSaleOrder(searchSaleOrder);
		
		//通过 order_id 没有查询到，插入，不修改 order_id
		if (saleOrderList.isEmpty()) {
			return NO_ORDER;
		}
		
		Map<String, Object> saleOrderMap = saleOrderList.get(0);
		
		//merch_id 不相同, 插入，修改 order_id
		if (!MapUtil.getString(saleOrderMap, "merch_id").equals(MapUtil.getString(paramMap, "merch_id"))) {
			return NOT_REPEATED_ORDER;
		}
		
		//判断，//实收， 应收, 日期, 时间
		//相同， 不插入
		//不相同，插入，修改 order_id 
		if (MapUtil.getBigDecimal(saleOrderMap, "amt_ord_total").compareTo(MapUtil.getBigDecimal(paramMap, "received_amount")) == 0
				&& MapUtil.getBigDecimal(saleOrderMap, "amtys_ord_total").compareTo(MapUtil.getBigDecimal(paramMap, "total_amount")) == 0
				&& MapUtil.getBigDecimal(saleOrderMap, "order_date").compareTo(MapUtil.getBigDecimal(paramMap, "order_date")) == 0 
				&& MapUtil.getBigDecimal(saleOrderMap, "order_time").compareTo(MapUtil.getBigDecimal(paramMap, "order_time")) == 0 
				) {
			return REPEATED_ORDER;
		} else {
			return NOT_REPEATED_ORDER;
		}
	}
	
	/**
	 * 商品销售主干
	 * 1、组织提交dao层的数值
	 * 2、调用各个dao
	 */
	@Override
	public Map<String, Object> submitSaleOrder(Map<String, Object> paramMap) throws Exception {
		LOG.debug("SaleServiceImpl submitSaleOrder paramMap:"+paramMap);
		Map<String, Object> insertOrderParam = new HashMap<String, Object>();//销售单
		List<Map<String, Object>> insertPromotionParam = new ArrayList<Map<String,Object>>();//促销流水
		Map<String, Object> updateConsumerParam = new HashMap<String, Object>();//会员
		List<Map<String, Object>> updateWhseParam = new ArrayList<Map<String,Object>>();//库存
		
		List<Map<String, Object>> itemList = MapUtil.get(paramMap, "list", null);
		if(itemList==null || itemList.isEmpty()){
			LOG.debug(MapUtil.getString(paramMap, "order_id") + "销售单无销售的商品信息！");
			return Collections.EMPTY_MAP;
		}
		
		String orderId = MapUtil.getString(paramMap, "order_id", null);//传入的销售单号
		String newOrderId = orderId;//新的销售单号
		
		/*
		 *  判断销售单是否存在
		 * 	0:不插入
		 * 	1：插入（不改 order_id）、销售单号不存在
		 * 	2：插入（修改order_id）
		 */
		int badge;
		if (!StringUtil.isBlank(orderId)) {
			badge = this.isRepeatedSaleOrder(paramMap);
		} else {
			badge = NO_ORDER;
			orderId = IDUtil.getId();
			newOrderId = orderId;
			paramMap.put("order_id", orderId);
		}
		
		if (badge == NOT_REPEATED_ORDER) {
			newOrderId = IDUtil.getId();
			paramMap.put("order_id", newOrderId);
		}
		
		Map<String, Map<String, Object>> promotionMap  = getUsablePromotion(paramMap);//可用的促销信息
		insertPromotionParam.addAll(buildPromotionParams(promotionMap, paramMap, paramMap));//组建促销流水数据
		insertOrderParam = buildOrderParams(promotionMap, paramMap, insertPromotionParam);//组建销售单数据
//		insertPromotionParam.addAll(buildPromotionParams(promotionMap, paramMap, paramMap));//组建促销流水数据
		updateConsumerParam = buildConsumerParams(paramMap);//组建会员积分数据
		
		LOG.debug("组织销售单数据 SaleServiceImpl submitSaleOrder insertOrderParam:"+insertOrderParam);
		
//		TODO 现在的销售主干
		for (Map<String, Object> itemMap : itemList) {
			itemMap.put("item_size", itemList.size());
			
			insertOrderParam = buildOrderLineParams(insertOrderParam, itemMap);//组件销售单行数据,及销售单部分数据
			insertPromotionParam.addAll(buildPromotionParams(promotionMap, paramMap, itemMap));//组建促销流水数据
			updateWhseParam.add(buildWhseParams(insertOrderParam, itemMap));//组建库存数据
		}
		
		insertOrderParam.put("ip", MapUtil.getString(paramMap, "ip"));
		LOG.debug("组织销售单行数据 SaleServiceImpl submitSaleOrder insertOrderParam:"+insertOrderParam);
		
		/*
		 *  判断销售单是否存在
		 * 	0:不插入
		 * 	1：插入（不改 order_id）、销售单号不存在
		 * 	2：插入（修改order_id）
		 * 放在组织数据后，用于返回数据准确性
		 */
		if (badge == REPEATED_ORDER) {
			insertOrderParam.put("new_order_id", orderId);
			return insertOrderParam;
		}
		
		//插入销售单
		this.insertSaleOrder(insertOrderParam);
		
		//插入销售单行
		this.insertSaleOrderLine(insertOrderParam);
		
		// 组织库存的map，修改库存
		if ("03".equals(MapUtil.getString(paramMap, "status"))) {
			whseService.modifyBatchWhseMerch(updateWhseParam);//修改库存
		}
		
		//调整会员积分
		if (updateConsumerParam != null && !updateConsumerParam.isEmpty()) {
			merchConsumerService.updateMerchConsumer(updateConsumerParam);//修改会员积分
		}
		
		//插入促销流水
		if (insertPromotionParam !=null && !insertPromotionParam.isEmpty()) {
			promotionService.insertMerchPromotionRecord(insertPromotionParam);//插入促销流水
		}
		
		// 状态是完成的才给rtms
		if("03".equals(paramMap.get("status"))) {
			uploadDataToRtmsService.submitSaleOrder2RTMS(insertOrderParam);//卷烟销售单上传
		}
		
		insertOrderParam.put("order_id", orderId);
		insertOrderParam.put("new_order_id", newOrderId);
		return insertOrderParam;
	}
	
	
	/**
	 * 组件paramMap中的promotion_ids 数据
	 * @param paramMap
	 * @param promotionMap
	 * @throws Exception
	 */
	public Map<String, Map<String, Object>> setPromotionIds(Map<String, Object> paramMap, Map<String, Map<String, Object>> promotionMap)throws Exception {
		
		List<String> pmtList = MapUtil.get(paramMap, "promotion_ids", new ArrayList<String>());
		Map<String, Map<String, Object>> promotion = new HashMap<String, Map<String,Object>>();
		for (String str : pmtList) {
			if(promotionMap.containsKey(str)){
				Map<String, Object> myPromotion = promotionMap.get(str);
				BigDecimal promotionKey = BigDecimal.ZERO; 
				String promotionType = MapUtil.getString(myPromotion, "promotion_type");//促销类型
				if(promotionType.equals("40")){
					promotionKey = MapUtil.getBigDecimal( (Map<String, Object>) ((Map<String, Object>) myPromotion.get("promotion_action")).get("ceiling_reduction"), "reduction");
				}else if(promotionType.equals("10")){
					promotionKey = MapUtil.getBigDecimal(paramMap, "line_rebate_amount");
				}
				
				Map<String, Object> newPromotion = new HashMap<String, Object>();
				newPromotion.put("promotion_type", promotionType);
				newPromotion.put("promotion_key", promotionKey);
				promotion.put(str, newPromotion);
			}
		}
		return promotion;
	}
	
	/**
	 * 计算会员让利金额
	 * @return
	 * @throws Exception
	 */
	public BigDecimal jsHuiYuanRangLi(Map<String, Object> paramMap)throws Exception {
		String consumerId = MapUtil.getString(paramMap, "consumer_id", null);
		
		if(StringUtil.isBlank(consumerId)){
			return BigDecimal.ZERO;
		}
		Map<String, Object> consumerParam = new HashMap<String, Object>();
		consumerParam.put("consumer_id", consumerId);
		List<Map<String, Object>> consumerList = merchConsumerService.searchMerchConsumer(consumerParam);
		BigDecimal consumerDiscont = BigDecimal.ZERO;
		if(!consumerList.isEmpty()){
			consumerDiscont = MapUtil.getBigDecimal(consumerList.get(0), "discount");
		}
		
		if(consumerDiscont.compareTo(BigDecimal.ZERO) == 0){
			return BigDecimal.ZERO;
		}
		BigDecimal cd = consumerDiscont.divide(new BigDecimal("100"), 5, BigDecimal.ROUND_HALF_UP);
		BigDecimal totalAmount = MapUtil.getBigDecimal(paramMap, "total_amount");//应收
		//（应收/折扣 ） - 应收  = 会员折扣金额
		BigDecimal consumerRebateAmount =  totalAmount.divide(cd, 5, BigDecimal.ROUND_HALF_UP).subtract(totalAmount);
		
		
		return consumerRebateAmount;
	}
	
	/**
	 * 设置 修改会员积分参数
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> setConsumerPointParams (Map<String, Object> paramMap) throws Exception {
		//调整会员积分
		String consumerId = MapUtil.getString(paramMap, "consumer_id");
		BigDecimal consumerPoint = MapUtil.getBigDecimal(paramMap, "consumer_point");
		Map<String, Object> consumerMap = new HashMap<String, Object>();
		if(!StringUtil.isBlank(consumerId) && consumerPoint.compareTo(BigDecimal.ZERO)>0){
			consumerMap.put("consumer_id", consumerId);
			consumerMap.put("adjusted_point", consumerPoint);
			consumerMap.put("merch_id", MapUtil.getString(paramMap, "merch_id"));
//			merchConsumerService.updateMerchConsumer(consumerMap);//修改会员积分
		}
		return consumerMap;
	}
	
	
	/**
	 * 设置修改用户库存参数
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> setWhseMerchParams (Map<String, Object> paramMap) throws Exception {
		
		Map<String, Object> whseParam = new HashMap<String, Object>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		List<Map<String, Object>> itemList = MapUtil.get(paramMap, "list", new ArrayList<Map<String, Object>>());
		whseParam.put("merch_id", merchId);
		whseParam.put("whse_date", MapUtil.getString(paramMap, "order_date"));
		List<Map<String, Object>> whseList = new ArrayList<Map<String, Object>>();
		Map<String, Object> whseMap = null;
		for(Map<String, Object> itemMap : itemList) {
			whseMap = new HashMap<String, Object>();
			whseMap.put("item_id", itemMap.get("item_id"));
			BigDecimal qtyOrd = MapUtil.getBigDecimal(itemMap, "sale_quantity");
			// 兼容智能终端销售数量
			if(qtyOrd.compareTo(BigDecimal.ZERO)==0) {
				BigDecimal oldQuantity = MapUtil.getBigDecimal(itemMap, "qty_ord");
				if(oldQuantity.compareTo(BigDecimal.ZERO)!=0) {
					qtyOrd = oldQuantity;
				}
			}
			BigDecimal unitRatio = MapUtil.getBigDecimal(itemMap, "unit_ratio");
			whseMap.put("qty_sub", qtyOrd.multiply(unitRatio));
			whseList.add(whseMap);
		}
		whseParam.put("list", whseList);
		return whseParam;
	}
	
	/**
	 * 插入促销流水,数据组织
	 * 1、组织 插入促销流水 数据
	 * 单促销和单行促销
	 *结构：paramMap:merch_id,consumer_id,order_id,line_num,pack_id,order_date,promotion(促销map key:id val(金额，类型))
	 *promotion(map()),map()
	 *
	 */
	public List<Map<String, Object>> setPromotionRecordParams (Map<String, Object> paramMap, Map<String, Object> lineItemMap) throws Exception {
		
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String consumerId = MapUtil.getString(paramMap, "consumer_id", "");
		String orderId = MapUtil.getString(paramMap, "order_id");
		String orderDate = MapUtil.getString(paramMap, "order_date", DateUtil.getToday());
		
		String lineNum = MapUtil.getString(lineItemMap, "line_num");
		String seqId = MapUtil.getString(lineItemMap, "pack_id", "");
		
		
		Map<String, Map<String, Object>> promotionsMap = MapUtil.get(lineItemMap, "promotion_ids", new HashMap<String, Map<String, Object>>());
		
		List<Map<String, Object>> recordList = new ArrayList<Map<String,Object>>();//流水记录list
		
		Map<String, Object> recordMap = new HashMap<String, Object>();
		BigDecimal adjustedAmount = BigDecimal.ZERO;
		
		String promotionType = "";
		for (String key : promotionsMap.keySet()) {
			
			recordMap = new HashMap<String, Object>();
			adjustedAmount = MapUtil.getBigDecimal(promotionsMap.get(key), "promotion_key");//促销金额
			promotionType = MapUtil.getString(promotionsMap.get(key), "promotion_type");//促销类型
			recordMap.put("merch_id", merchId);
			recordMap.put("consumer_id", consumerId);
			recordMap.put("promotion_id", key);
			recordMap.put("order_id", orderId);
			recordMap.put("line_num", lineNum);
			recordMap.put("pack_id", seqId);
			recordMap.put("record_date", orderDate);
			recordMap.put("promotion_type", promotionType);
			recordMap.put("promotion_key", adjustedAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
			recordList.add(recordMap);
			
		}
		
		return recordList;
	}
	
	
	
	
	/**
	 * 二维码活动判断
	 * @author 徐虎彬
	 * @date 2014年4月21日
	 * @return
	 * @throws Exception
	 */
	public List<String> getLottery(Map<String,Object> paramMap)throws Exception{
		
		Map<String,String> lotteryData=new HashMap<String,String>();
		
		lotteryData.put("cust_id", paramMap.get("merch_id").toString());
		lotteryData.put("custId", paramMap.get("merch_id").toString());
		
		lotteryService.getActivityParameter(lotteryData);
		
		Map<String,Object> activetyDataMap=(Map<String,Object>)LotteryServiceImpl.activityParameter.get(paramMap.get("merch_id").toString());
		List<Map<String,Object>> activetyData=(List<Map<String,Object>>)activetyDataMap.get("list");//获取活动列表
		List<String> thisMerchActivity=new ArrayList<String>();
		if(activetyData!=null&&activetyData.size()>0){
			
			Map<String,Object> cgtData=new HashMap<String,Object>();
			List<Map<String,Object>> orderDataList=(List<Map<String,Object>>)paramMap.get("list");//获取商品列表
			//提取卷烟商品
			if(orderDataList!=null&&orderDataList.size()>0){
				for(Map<String,Object> map:orderDataList){
					if(ParamUtil.isTobacco(map)){
						cgtData.put( map.get("item_id").toString(),map);
					}
				}
				thisMerchActivity=lottery( paramMap,activetyData,cgtData);
			}
		}
		
		return insertMerchActivity(thisMerchActivity,paramMap);
	}
	/**
	 * 将活动数据（二维码数据）持久化操作
	 * @author 徐虎彬
	 * @date 2014年4月21日
	 * @param thisMerchActivity
	 * @param paramMap
	 * @throws Exception
	 */
	public List<String> insertMerchActivity(List<String> thisMerchActivity,Map<String,Object> paramMap)throws Exception{
		List<Map<String,Object>> lotteryActivityList=new ArrayList<Map<String,Object>>();
		List<String> activityKeyList=new ArrayList<String>();
		for(String actId:thisMerchActivity){
			Map<String,Object> thisMap=new HashMap<String,Object>();
			String orderId= paramMap.get("order_id").toString();
			String custId=paramMap.get("merch_id").toString();
			thisMap.put("orderId",orderId);
			thisMap.put("actId", actId);
			thisMap.put("custId",custId);
			String str="cust_id="+custId+"&act_id="+actId+"&order_id="+orderId;
			thisMap.put("key",MD5Util.getMD5Code(str));
			activityKeyList.add(thisMap.get("key").toString());
			lotteryActivityList.add(thisMap);
		}
		lotteryDao.insertLottery(lotteryActivityList);
		
		return activityKeyList;
	}
	
	/**
	 * 判断二维码打印数量
	 * @author 徐虎彬
	 * @date 2014年4月21日
	 * @param paramMap
	 * @param activetyData
	 * @param cgtData
	 * @return
	 */
	public List<String> lottery(Map<String,Object> paramMap,List<Map<String,Object>> activetyData,Map<String,Object> cgtData){
		
		List<String> thisMerchActivity=new ArrayList<String>();
		
		String havCard = "0";
		if(paramMap.containsKey("card_id")){
			havCard = paramMap.get("card_id").toString();
		}
		String payType = "1";
		
		for(int i=0;i<activetyData.size();i++){
			Map<String,Object> param = activetyData.get(i);
			String orderPayType=param.get("pay_type").toString();
			String memberLmt=param.get("member_lmt").toString();
			if((havCard.equals(memberLmt)||"0".equals(memberLmt)) && ("0".equals(orderPayType)|| payType.equals(orderPayType))){
				
				if("0".equals(param.get("is_item_lmt").toString())){
					thisMerchActivity.add(param.get("act_id").toString());
				}else{
					List<Map<String,Object>> itemMap=(List<Map<String,Object>>)param.get("item_list");
					for(Map<String,Object> map : itemMap) {
						if(cgtData.containsKey(map.get("item_id").toString())){
							thisMerchActivity.add(param.get("act_id").toString());
							break;
						}
					}
				}
			}
		}
		return thisMerchActivity;
	}
	
	
	@Override
	public List<Map<String, Object>> searchSaleQuantityDaily(Map<String, Object> saleQuantityDailyParam) throws Exception {
		LOG.debug("SaleServiceImpl searchSaleQuantityDaily saleQuantityDailyParam: " + saleQuantityDailyParam);
		String startDate = (String) saleQuantityDailyParam.get("start_date");
		String endDate = (String) saleQuantityDailyParam.get("end_date");
		List<String> dayList = DateUtil.getListBetweenStartingAndEnding(startDate, endDate);
		List<Map<String, Object>> saleQuantityList = saleOrderDao.selectSaleQuantityDaily(saleQuantityDailyParam);
		for(int index=0; index<dayList.size(); index++) {
			String day = dayList.get(index);
			if(index>=saleQuantityList.size() || !day.equals(saleQuantityList.get(index).get("order_date"))) {
				Map<String, Object> newSaleQuantityMap = new HashMap<String, Object>();
				newSaleQuantityMap.put("order_date", day);
				newSaleQuantityMap.put("sale_quantity", BigDecimal.ZERO);
				saleQuantityList.add(index, newSaleQuantityMap);
			}
		}
		return saleQuantityList;
	}
	@Override
	public List<Map<String, Object>> searchSaleQuantityMonthly(Map<String, Object> saleQuantityMonthlyParam) throws Exception {
		LOG.debug("SaleServiceImpl searchSaleQuantityMonthly saleQuantityMonthlyParam: " + saleQuantityMonthlyParam);
		String startMonth = (String) saleQuantityMonthlyParam.get("start_month");
		String endMonth = (String) saleQuantityMonthlyParam.get("end_month");
		List<String> monthList = DateUtil.getListBetweenStartingAndEnding(startMonth, endMonth);
		List<Map<String, Object>> saleQuantityList = saleOrderDao.selectSaleQuantityMonthly(saleQuantityMonthlyParam);
		for(int index=0; index<monthList.size(); index++) {
			String month = monthList.get(index);
			if(index>=saleQuantityList.size() || !month.equals(saleQuantityList.get(index).get("order_month"))) {
				Map<String, Object> newSaleQuantityMap = new HashMap<String, Object>();
				newSaleQuantityMap.put("order_month", month);
				newSaleQuantityMap.put("sale_quantity", BigDecimal.ZERO);
				saleQuantityList.add(index, newSaleQuantityMap);
			}
		}
		return saleQuantityList;
	}
	@Override
	public List<Map<String, Object>> searchGrossMarginDaily(Map<String, Object> grossMarginDailyParam) throws Exception {
		LOG.debug("SaleServiceImpl searchGrossMarginDaily grossMarginDailyParam: " + grossMarginDailyParam);
		String startDate = (String) grossMarginDailyParam.get("start_date");
		String endDate = (String) grossMarginDailyParam.get("end_date");
		List<String> dayList = DateUtil.getListBetweenStartingAndEnding(startDate, endDate);
		List<Map<String, Object>> grossMarginList = saleOrderDao.selectGrossMarginDaily(grossMarginDailyParam);
		for(int index=0; index<dayList.size(); index++) {
			String day = dayList.get(index);
			if(index>=grossMarginList.size() || !day.equals(grossMarginList.get(index).get("order_date"))) {
				Map<String, Object> newGrossMarginMap = new HashMap<String, Object>();
				newGrossMarginMap.put("order_date", day);
				newGrossMarginMap.put("gross_margin", BigDecimal.ZERO);
				grossMarginList.add(index, newGrossMarginMap);
			}
		}
		return grossMarginList;
	}
	@Override
	public List<Map<String, Object>> searchGrossMarginMonthly(Map<String, Object> grossMarginMonthlyParam) throws Exception {
		LOG.debug("SaleServiceImpl searchGrossMarginMonthly grossMarginMonthlyParam: " + grossMarginMonthlyParam);
		String startMonth = (String) grossMarginMonthlyParam.get("start_month");
		String endMonth = (String) grossMarginMonthlyParam.get("end_month");
		List<String> monthList = DateUtil.getListBetweenStartingAndEnding(startMonth, endMonth);
		List<Map<String, Object>> grossMarginList = saleOrderDao.selectGrossMarginMonthly(grossMarginMonthlyParam);
		for(int index=0; index<monthList.size(); index++) {
			String month = monthList.get(index);
			if(index>=grossMarginList.size() || !month.equals(grossMarginList.get(index).get("order_month"))) {
				Map<String, Object> newGrossMarginMap = new HashMap<String, Object>();
				newGrossMarginMap.put("order_month", month);
				newGrossMarginMap.put("gross_margin", BigDecimal.ZERO);
				grossMarginList.add(index, newGrossMarginMap);
			}
		}
		return grossMarginList;
	}
	
	
	private void insertSaleOrder(Map<String, Object> orderParam) throws Exception {
		LOG.debug("SaleServiceImpl insertSaleOrder orderParam: " + orderParam);
		saleOrderDao.insertSaleOrder(orderParam);
	}
	
	private void insertSaleOrderLine(Map<String, Object> lineParam) throws Exception {
		LOG.debug("SaleServiceImpl insertSaleOrderLine lineParam: " + lineParam);
		saleOrderDao.insertSaleOrderLine(lineParam);
	}
	
	/**
	 * 批量插入销售单
	 * @param orderParamList 销售单列表
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void insertBatchSaleOrder(List<Map<String, Object>> orderParamList) throws Exception {
		LOG.debug("SaleServiceImpl insertBatchSaleOrder orderParam: " + orderParamList);
		saleOrderDao.insertBatchSaleOrder(orderParamList);
	}
	
	/**
	 * 批量插入销售单行
	 * @param lineParamList 销售单行
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void insertBatchSaleOrderLine(List<Map<String, Object>> lineParamList) throws Exception {
		LOG.debug("SaleServiceImpl insertBatchSaleOrderLine lineParam: " + lineParamList);
		saleOrderDao.insertBatchSaleOrderLine(lineParamList);
	}
	
	/**
	 * 组装数据
	 */
	private void batchSubmitPCSaleOrderTool(
			Map<String, Object> paramMap,
			Map<String,Object> valueMap,
			List<Map<String, Object>> orderParamList,
			List<Map<String, Object>> recordList,
			List<Map<String, Object>> whseList,
			List<Map<String, Object>> consList) throws Exception{
		valueMap.put("operator",  paramMap.get("user_code"));
		valueMap.put("order_type", "01");
		valueMap.put("order_id", IDUtil.getId()); //生成id
		String orderDate = MapUtil.getString(paramMap, "order_date", DateUtil.getToday());
		valueMap.put("order_date", orderDate); //生成日期
		valueMap.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
		valueMap.put("status", "03"); //订单状态完成
		valueMap.put("pmt_status", "03"); //支付状态已付款
		valueMap.put("pay_type", "1"); // 现金支付
		
		List<Map<String, Object>> itemList = MapUtil.get(valueMap, "list", null);
		if(itemList==null || itemList.isEmpty()){
			// 如果没有销售单行在日志上记录信息不保存销售记录 by 梁凯 2014-7-11
			LOG.debug(MapUtil.getString(valueMap, "order_id") + "销售单无销售的商品信息！");
			return;
		}
		
		String consumerId = "";
		if(valueMap.containsKey("consumer_id")){
			consumerId = MapUtil.getString(valueMap, "consumer_id");
		}
		int qtyOrdCount = itemList.size(); // 销售商品种类
		List<String> usePromotoinList = null;//使用的促销list
		Map<String, Object> recordMap = null;//正在使用的促销map
		BigDecimal sumAdjustedAmount = BigDecimal.ZERO;//单行总折扣金额 
		BigDecimal adjustedAmount = BigDecimal.ZERO;//一个折扣金额
		
		String merchId = MapUtil.getString(paramMap, "merch_id"); // 销售单行中使用, 减库存中使用
		BigDecimal amtOrdChange = MapUtil.getBigDecimal(valueMap, "amt_ord_change"); // 找零
		BigDecimal amtOrdTotal = MapUtil.getBigDecimal(valueMap, "amt_ord_total"); // 实收
		BigDecimal amtysOrdTotal = MapUtil.getBigDecimal(valueMap, "amtys_ord_total", amtOrdTotal); // 应收
		BigDecimal amtOrdLoss = BigDecimal.ZERO;
		if(!"02".equals(MapUtil.getString(valueMap, "pmt_status"))) {
			amtOrdLoss = amtysOrdTotal.add(amtOrdChange).subtract(amtOrdTotal); // 抹零 = 应收+找零-实付
		}
		
		BigDecimal amtOrdProfit = BigDecimal.ZERO; // 利润, 通过行表计算
		BigDecimal qtyOrdTotal = BigDecimal.ZERO; // 销售商品个数, 通过行表计算
		int sumAdjustedPoint = 0;//得到的积分（增或减）
		sumAdjustedPoint = sumAdjustedPoint + MapUtil.getInt(valueMap, "adjusted_point", 0);
		StringBuilder orderNoteBuilder = new StringBuilder(); 
		int index = 0;
		
		Map<String, Object> searchPromotionParam = new HashMap<String, Object>();
		searchPromotionParam.put("merch_id", merchId);
		searchPromotionParam.put("status", "1");
		List<Map<String, Object>> promotionList = promotionService.searchMerchPromotion(searchPromotionParam);//促销
		Map<String, Map<String, Object>> promotionMap = new HashMap<String, Map<String,Object>>();
		for (Map<String, Object> map : promotionList) {
			promotionMap.put(MapUtil.getString(map, "promotion_id"), map);
		}
		
		
		for(Map<String, Object> itemMap : itemList) {
			
			if(index++!=0) orderNoteBuilder.append(",");
			orderNoteBuilder.append(MapUtil.getString(itemMap, "item_name"));
			sumAdjustedPoint = sumAdjustedPoint + MapUtil.getInt(itemMap, "adjusted_point", 0);
			itemMap.put("merch_id", merchId);
			itemMap.put("order_id", MapUtil.getString(valueMap, "order_id"));
			// 如果没有传qty_ord字段则报错
			BigDecimal qtyOrd = MapUtil.getBigDecimal(itemMap, "qty_ord");
			if(qtyOrd.compareTo(BigDecimal.ZERO)==0){
				throw new RuntimeException("销售单" + MapUtil.getString(valueMap, "order_id") + "没有销售此商品的数量或者销售了0个商品" + MapUtil.getString(itemMap, "item_id"));
			}
			// 如果没有传amt_ord字段则认为此商品一共卖了0元
			BigDecimal amtOrd = MapUtil.getBigDecimal(itemMap, "amt_ord");
			itemMap.put("amt_ord", amtOrd);
			// 如果没有传unit_ratio字段则报错
			BigDecimal unitRatio = MapUtil.getBigDecimal(itemMap, "unit_ratio");
			if(unitRatio.compareTo(BigDecimal.ZERO)==0){
				throw new RuntimeException("销售单" + MapUtil.getString(valueMap, "order_id") + "中商品" + MapUtil.getString(itemMap, "item_id") + "的转换比为0或者没有转换比");
			}
			// 单品售价 = amt_ord / qty_ord / unit_ratio
			BigDecimal price = amtOrd.divide(qtyOrd, 5, BigDecimal.ROUND_HALF_UP).divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP);
			// 单品利润  = 单品售价(以扣减调整额) - 单品成本
			BigDecimal profit = price.subtract(MapUtil.getBigDecimal(itemMap, "cost"));
			itemMap.put("profit", profit);
			// 销售单总利润 = 单品利润 * 转换比 * 销售数量
			amtOrdProfit = amtOrdProfit.add(profit.multiply(unitRatio).multiply(qtyOrd));
			// 销售单总商品数
			qtyOrdTotal = qtyOrdTotal.add(qtyOrd);
			
			usePromotoinList = MapUtil.get(itemMap, "promotion_ids", new ArrayList<String>());
			for (String str : usePromotoinList) {
				adjustedAmount = MapUtil.getBigDecimal(itemMap, "adjusted_amount");
				sumAdjustedAmount = sumAdjustedAmount.add(adjustedAmount);
				recordMap = new HashMap<String, Object>();
				recordMap.put("merch_id", merchId);
				recordMap.put("consumer_id", consumerId);
				recordMap.put("promotion_id", str);
				recordMap.put("order_id", MapUtil.getString(valueMap, "order_id"));
				recordMap.put("line_num", MapUtil.getString(itemMap, "line_num"));
				recordMap.put("pack_id", MapUtil.getString(itemMap, "seq_id"));
				recordMap.put("record_date", MapUtil.getString(valueMap, "order_date", DateUtil.getToday()));
				recordMap.put("promotion_type", promotionMap.get(str).get("promotion_type"));
				recordMap.put("promotion_key", adjustedAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
				recordList.add(recordMap);
			}
		}
		
		usePromotoinList = MapUtil.get(valueMap, "promotion_ids", new ArrayList<String>());
		for (String str : usePromotoinList) {
			recordMap = new HashMap<String, Object>();
			recordMap.put("merch_id", merchId);
			recordMap.put("consumer_id", consumerId);
			recordMap.put("promotion_id", str);
			recordMap.put("order_id", MapUtil.getString(valueMap, "order_id"));
			recordMap.put("line_num", "");
			recordMap.put("pack_id", "");
			recordMap.put("promotion_type", promotionMap.get(str).get("promotion_type"));
			recordMap.put("record_date", MapUtil.getString(valueMap, "order_date", DateUtil.getToday()));
			BigDecimal manJianArount = BigDecimal.ZERO;
			adjustedAmount = MapUtil.getBigDecimal(valueMap, "adjusted_amount");
			//（应收+总优惠）-（应收/会员折扣+总单行优惠）
			if(!StringUtil.isBlank(consumerId)){
				Map<String, Object> consumerParam = new HashMap<String, Object>();
				consumerParam.put("consumer_id", consumerId);
				List<Map<String, Object>> consumerList = merchConsumerService.searchMerchConsumer(consumerParam);
				if(consumerList.size()>=1){
					Map<String, Object> consumerMap = consumerList.get(0);
					BigDecimal discount = MapUtil.getBigDecimal(consumerMap, "discount");
					if(discount.compareTo(BigDecimal.ZERO)>0){
						discount = (discount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
						BigDecimal a = amtOrdTotal.add(adjustedAmount);
						BigDecimal b = (amtOrdTotal.divide(discount,2, BigDecimal.ROUND_HALF_UP)).add(sumAdjustedAmount);
						manJianArount = (a.subtract(b).setScale(2, BigDecimal.ROUND_HALF_UP));
//						manJianArount = (amtOrdTotal.add(adjustedAmount)).subtract((amtOrdTotal.divide(discount)).add(sumAdjustedAmount));
					}
				}
			}
			if(manJianArount.compareTo(BigDecimal.ZERO)<=0){
				manJianArount = adjustedAmount.subtract(sumAdjustedAmount);
			}
			recordMap.put("promotion_key", manJianArount);
			recordList.add(recordMap);
		}
		valueMap.put("qty_ord_count", qtyOrdCount);
		valueMap.put("amt_ord_loss", amtOrdLoss);
		valueMap.put("amt_ord_profit", amtOrdProfit);
		valueMap.put("qty_ord_total", qtyOrdTotal);
		valueMap.put("amtys_ord_total", amtysOrdTotal);
		valueMap.put("amt_ord_total", amtOrdTotal);
		String note = "lzx";
		if(MapUtil.getString(valueMap, "order_date").equals(DateUtil.getToday())){
			note = orderNoteBuilder.length()>500?orderNoteBuilder.substring(0, 500):orderNoteBuilder.toString();
		}
		valueMap.put("note", note);
		orderParamList.add(valueMap);//销售单,销售单行
		
		// 组织库存的map
		if("03".equals(MapUtil.getString(valueMap, "status"))) {
			boolean flag = false;
			Map<String, Object> whseParam = new HashMap<String, Object>();
			for(Map<String, Object> itemMap : itemList) {
				Map<String, Object> whseMap = new HashMap<String, Object>();
				BigDecimal qtyOrd = MapUtil.getBigDecimal(itemMap, "qty_ord");
				BigDecimal unitRatio = MapUtil.getBigDecimal(itemMap, "unit_ratio");
				if(!whseList.isEmpty()){
					for (Map<String, Object> map : whseList) {
						if(map.containsValue(itemMap.get("item_id"))){//判断是否存在
							map.put("qty_sub", MapUtil.getBigDecimal(map, "qty_sub").add(qtyOrd.multiply(unitRatio)));//累加覆盖
							flag = true;
						}
					}
				}
				if(!flag){
					whseMap.put("merch_id", merchId);
					whseMap.put("item_id", itemMap.get("item_id"));
					whseMap.put("qty_sub", qtyOrd.multiply(unitRatio));
					whseList.add(whseMap);//添加
					flag = false;
				}
			}
		}
		List<String> activityList=getLottery(valueMap);
		Map<String,Object> dataMap=new HashMap<String,Object>();
		dataMap.put("list", activityList);
		dataMap.put("itms",new HashMap<String, Object>());
		
		//调整会员积分
//		/*
		BigDecimal consumerPoint = MapUtil.getBigDecimal(valueMap, "consumer_point");
		if(!StringUtil.isBlank(consumerId) && consumerPoint.compareTo(BigDecimal.ZERO)>0){
			for (Map<String, Object> map : consList) {
				if(map.containsKey(consumerId)){
					BigDecimal adjusted_point = MapUtil.getBigDecimal(map, "adjusted_point");
					map.put("adjusted_point", adjusted_point.add(consumerPoint));
				}else{
					Map<String, Object> consumerMap = new HashMap<String, Object>();
					consumerMap.put("consumer_id", consumerId);
					consumerMap.put("adjusted_point", consumerPoint);
					consumerMap.put("merch_id", MapUtil.getString(valueMap, "merch_id"));
					consList.add(consumerMap);
				}
			}
		}
	}
	
	
}
