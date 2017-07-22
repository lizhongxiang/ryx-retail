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
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IReturnOrderDao;
import com.ryx.social.retail.dao.ISaleOrderDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.IMerchConsumerService;
import com.ryx.social.retail.service.IReturnOrderService;


@Service
public class ReturnOrderServiceImpl implements IReturnOrderService {
	private Logger LOG = LoggerFactory.getLogger(ReturnOrderServiceImpl.class);
	
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
	private IReturnOrderDao returnOrderDao;
	@Resource
	private ISaleOrderDao saleOrderDao;
	@Resource
	private IWhseDao whseDao;
	@Resource
	private IMerchConsumerService merchConsumerService;
		
	/**
	 * 提交退货单
	 * (return_order, return_order_line, sale_order_line)
	 * 单行现在只需要上传：销售单行，退货金额，退货数量 
	 * @return 
	 */
	@Override
	public Map<String, Object> submitReturnOrder(Map<String, Object> returnOrderParam) throws Exception {
		LOG.debug("ReturnOrderServiceImpl submitReturnOrder returnOrderParam: " + returnOrderParam);
		// 先提交销售单
		Map<String, Object> whseParam = new HashMap<String, Object>();
		Map<String, Map<String, Object>> itemWhseMap = new HashMap<String, Map<String,Object>>();
		
		List<Map<String, Object>> returnOrderLineList = MapUtil.get(returnOrderParam, "list", new ArrayList<Map<String, Object>>());
		
		String returnOrderId = MapUtil.getString(returnOrderParam, "return_order_id");
		String newReturnOrderId = returnOrderId;
		
		int index = 0;
		String itemId = "";
		Map<String, Object> itemWhse = null;
		BigDecimal profit = BigDecimal.ZERO;//当行利润
		BigDecimal cost = BigDecimal.ZERO;//单行cost
		BigDecimal qtyOrd = BigDecimal.ZERO;//当行退货数量
		BigDecimal returnMoney = BigDecimal.ZERO;//当行退货金额
		BigDecimal unitRatio = BigDecimal.ZERO;
		BigDecimal whseAdd  = BigDecimal.ZERO;
		BigDecimal sumQtyTotal = BigDecimal.ZERO;//退货数量
		BigDecimal sumReturnMoney = BigDecimal.ZERO;//退货总金额
		BigDecimal amtReturnLoss = BigDecimal.ZERO;//退货总利润
		StringBuilder orderNoteBuilder = new StringBuilder();//备注
		boolean isRuKu = true;
		int isResalable = 0;//是否再次销售（1、销售，0，不销售
		int returnLineNum = 1;//退货商户（
		int saleLineNum = 1;
		Map<String, Object> saleLineMap = null;
		BigDecimal saleLineAmt = BigDecimal.ZERO;//销售单行金额
		BigDecimal saleLineQty = BigDecimal.ZERO;//销售单行数量
		BigDecimal returnAdjustedAmt= BigDecimal.ZERO;//销售退货让利金额
		BigDecimal saleLineAdjustedAmt = BigDecimal.ZERO;//销售单行总折扣
		List<Map<String, Object>> subRetLineList = new ArrayList<Map<String,Object>>();
		
//		查找销售当行
		Map<String, Object> searchOrderLineParam = new HashMap<String, Object>();
		
		StringBuffer saleLineNumBuffer = new StringBuffer();
		for (Map<String, Object> map : returnOrderLineList) {
			saleLineNumBuffer.append(MapUtil.getInt(map, "line_num"));
			saleLineNumBuffer.append(",");
		}
		
		searchOrderLineParam.putAll(MapUtil.rename(returnOrderParam, "order_id"));
		searchOrderLineParam.put("line_nums", saleLineNumBuffer.toString());
		List<Map<String, Object>> searchOrderLineList = saleOrderDao.selectSaleOrderLine(searchOrderLineParam);//销售单行
		Map<String, Map<String, Object>> saleOrderLine = new HashMap<String, Map<String,Object>>(); 
		for (Map<String, Object> map : searchOrderLineList) {
			saleOrderLine.put(MapUtil.getString(map, "line_num"), map);
		}
		
		for(Map<String, Object> lineMap : returnOrderLineList) {
			saleLineNum = MapUtil.getInt(lineMap, "line_num");//销售单行号（前台上传
			saleLineMap = saleOrderLine.get(saleLineNum+"");//销售单行
			saleLineAmt = MapUtil.getBigDecimal(saleLineMap, "amt_ord");//销售金额
			saleLineQty = MapUtil.getBigDecimal(saleLineMap, "qty_ord");//销售数量
			
			itemId = MapUtil.getString(saleLineMap, "item_id");
			orderNoteBuilder.append(MapUtil.getString(saleLineMap, "item_name"));
			unitRatio = MapUtil.getBigDecimal(saleLineMap, "unit_ratio");//转换比
			unitRatio = unitRatio.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("1") : unitRatio;
			cost = MapUtil.getBigDecimal(saleLineMap, "cost");
			
			qtyOrd = MapUtil.getBigDecimal(lineMap, "qty_ord");//数量
			qtyOrd = qtyOrd.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("1") : qtyOrd;
			returnMoney = MapUtil.getBigDecimal(lineMap, "return_money");
//			isRuKu = MapUtil.getBoolean(lineMap, "isruku", true);//是否入库 = false时，不进行入库
			isResalable = MapUtil.getInt(lineMap, "is_resalable", 1);
			
			if(index++!=0){
				orderNoteBuilder.append(",");
			}
			
			//计算退货让利
			if (saleLineNum == MapUtil.getInt(saleLineMap, "line_num")) {
				saleLineAdjustedAmt = MapUtil.getBigDecimal(saleLineMap, "adjusted_amount").add(MapUtil.getBigDecimal(saleLineMap, "other_adjusted_amount"));
				returnAdjustedAmt= saleLineAdjustedAmt.divide(MapUtil.getBigDecimal(saleLineMap, "qty_ord"), 4, BigDecimal.ROUND_HALF_UP).multiply(qtyOrd);
			}
			
			//计算利润
			if(isResalable == 1){
				profit = ( (returnMoney.divide(unitRatio, 4, BigDecimal.ROUND_HALF_UP) ).divide(qtyOrd, 4, BigDecimal.ROUND_HALF_UP)).subtract(cost); 
			} else {
				profit = ( (returnMoney.divide(unitRatio, 4, BigDecimal.ROUND_HALF_UP) ).divide(qtyOrd, 4, BigDecimal.ROUND_HALF_UP) );
			}
			
			Map<String, Object> subReturnLine = MapUtil.rename(saleLineMap, "item_id", "pri3", "item_name", "unit_name", "item_bar", 
					"big_bar", "big_unit_name", "unit_ratio", "big_pri3", "cost");
			subReturnLine.put("line_num", returnLineNum);
			subReturnLine.put("qty_ord", qtyOrd);
			subReturnLine.put("return_money", returnMoney);
			subReturnLine.put("profit", profit);
			subReturnLine.put("adjusted_amount", returnAdjustedAmt);
			subReturnLine.put("sale_line_num", saleLineNum);
			subReturnLine.put("line_num", returnLineNum);
			subReturnLine.put("is_resalable", isResalable);
			//退货造成的销售额损失=SALE_AMOUNT/SALE_QUANTITY*REFUND_QUANTITY
			subReturnLine.put("unsale_amount", (saleLineAmt.subtract(saleLineAdjustedAmt)).divide(saleLineQty, 4, BigDecimal.ROUND_HALF_UP).multiply(qtyOrd));
			subRetLineList.add(subReturnLine);
			
			// 退货单行 利润 = 利润*转换比*退货数量
			BigDecimal loss = profit.multiply(qtyOrd.multiply(unitRatio));
			sumReturnMoney = sumReturnMoney.add(returnMoney);
			amtReturnLoss = amtReturnLoss.add(loss);
			sumQtyTotal = sumQtyTotal.add(qtyOrd);
			returnLineNum++ ;
			
			if(isResalable != 1){
				continue;
			}
			
			itemWhse = new HashMap<String, Object>();
			whseAdd = qtyOrd.multiply(unitRatio);
			if(itemWhseMap.containsKey(itemId)){
				itemWhse = MapUtil.get(itemWhseMap,itemId, new HashMap<String, Object>());
				itemWhse.put("qty_add", MapUtil.getBigDecimal(itemWhse, "qty_add").add(whseAdd));
			}else{
				itemWhse.put("item_id", itemId);
				itemWhse.put("qty_add", whseAdd);
				itemWhseMap.put(itemId, itemWhse);
			}
		}
		
		if(orderNoteBuilder.length() > 500){
			orderNoteBuilder = new StringBuilder(orderNoteBuilder.substring(0, 500)).append("...");
		}
		returnOrderParam.put("amt_return_total", sumReturnMoney);//退货金额
		returnOrderParam.put("qty_return_total",sumQtyTotal);//退货商品数量
		returnOrderParam.put("amt_return_loss", amtReturnLoss);//利润
		returnOrderParam.put("qty_return_count", returnOrderLineList.size());//商品种类
		returnOrderParam.put("note", orderNoteBuilder.toString());
//		returnOrderDao.submitReturnOrder(returnOrderParam);
		
		returnOrderParam.put("list", subRetLineList);
		
		int badge;
		if (!StringUtil.isBlank(returnOrderId)) {
			badge = this.isRepeatedReturnOrder(returnOrderParam);
		} else {
			badge = NO_ORDER;
			returnOrderId = IDUtil.getId();
			newReturnOrderId = returnOrderId;
			returnOrderParam.put("return_order_id", returnOrderId);
		}
		if (badge == NOT_REPEATED_ORDER) {
			newReturnOrderId = IDUtil.getId();
			returnOrderParam.put("return_order_id", newReturnOrderId);
		}
		if (badge == REPEATED_ORDER) {
			returnOrderParam.put("new_return_order_id", returnOrderId);
			return returnOrderParam;
		}
		
		
		//插入退货表
		returnOrderDao.insertReturnOrder(returnOrderParam);
		//插入退货表行, returnOrderParam中必须有key="list"
		returnOrderDao.insertReturnOrderLine(returnOrderParam);
		
		//对商品进行修改库存
		List<Map<String, Object>> itemWhseList = new ArrayList<Map<String,Object>>();
		for (String	key : itemWhseMap.keySet()) {
			itemWhseList.add(itemWhseMap.get(key));
		}
		if(!itemWhseList.isEmpty()){
			whseParam.put("merch_id", returnOrderParam.get("merch_id"));
			whseParam.put("whse_date", DateUtil.getToday());
			whseParam.put("list", itemWhseList);
			whseDao.updateWhseMerch(whseParam);
		}
		
		//修改会员积分
		BigDecimal amountPerPoint = MapUtil.getBigDecimal(returnOrderParam, "amount_per_point");
		String consumerId = MapUtil.getString(returnOrderParam, "consumer_id");
		if(!StringUtil.isBlank(consumerId) && amountPerPoint.compareTo(BigDecimal.ZERO) > 0 ){
			BigDecimal consumerPoint =(BigDecimal.ZERO).subtract(sumReturnMoney.divide(amountPerPoint, 0, BigDecimal.ROUND_DOWN));
			Map<String, Object> consumerMap = new HashMap<String, Object>();
			consumerMap.put("consumer_id", consumerId);
			consumerMap.put("adjusted_point", consumerPoint);
			consumerMap.put("merch_id", MapUtil.getString(returnOrderParam, "merch_id"));
			merchConsumerService.updateMerchConsumer(consumerMap);//修改会员积分
		}
		returnOrderParam.put("return_order_id", returnOrderId);
		returnOrderParam.put("new_return_order_id", newReturnOrderId);
		return returnOrderParam; 
	}
	
	//查询退货单
	@Override
	public List<Map<String, Object>> searchMerchReturnOrder(Map<String, Object> orderMap)throws Exception{
		LOG.debug("ReturnOrderService searchMerchReturnOrder orderMap:"+orderMap);
		List<Map<String, Object>> data= returnOrderDao.selectMerchReturnOrder(orderMap);
		return data;
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
	public int isRepeatedReturnOrder(Map<String, Object> paramMap)throws Exception{
		LOG.debug("saleServiceImpl isInsertSaleOrder paramMap:"+paramMap);
		//查询销售单
		Map<String, Object> searchROParam= MapUtil.remain(paramMap, "return_order_id");
		List<Map<String, Object>> returnOrderList = this.searchMerchReturnOrder(searchROParam);
		
		//通过 order_id 没有查询到，插入，不修改 order_id
		if (returnOrderList.isEmpty()) {
			return NO_ORDER;
		}
		
		Map<String, Object> returnOrderMap = returnOrderList.get(0);
		
		//merch_id 不相同, 插入，修改 order_id
		if (!MapUtil.getString(returnOrderMap, "merch_id").equals(MapUtil.getString(paramMap, "merch_id"))) {
			return NOT_REPEATED_ORDER;
		}
		
		//判断，//实收， 应收, 日期, 时间
		//相同， 不插入
		//不相同，插入，修改 order_id 
		if (MapUtil.getBigDecimal(returnOrderMap, "amt_return_total").compareTo(MapUtil.getBigDecimal(paramMap, "amt_return_total")) == 0
				&& MapUtil.getBigDecimal(returnOrderMap, "qty_return_total").compareTo(MapUtil.getBigDecimal(paramMap, "qty_return_total")) == 0
				&& MapUtil.getBigDecimal(returnOrderMap, "return_order_date").compareTo(MapUtil.getBigDecimal(paramMap, "return_order_date")) == 0 
				&& MapUtil.getBigDecimal(returnOrderMap, "return_order_time").compareTo(MapUtil.getBigDecimal(paramMap, "return_order_time")) == 0 
				) {
			return REPEATED_ORDER;
		} else {
			return NOT_REPEATED_ORDER;
		}
	}
	
	
}
