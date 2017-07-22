package com.ryx.social.retail.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;

public class LotteryUtil3 {
	
	private static List<Map<String, Object>> promotions = new ArrayList<Map<String, Object>>(); // 所有促销活动
	
	public static void lottery2() {
		PromotionExecutor p = PromotionExecutorFactory.getPromotionExecutor(promotions);
		
		//销售单
		Map<String, Object> orderMap = new HashMap<String, Object>();
		orderMap.put("order_id", "order123");
		orderMap.put("consumer_id", "consumer123");
		orderMap.put("consumer_grade", new BigDecimal(1));
		orderMap.put("amt_ord_total", new BigDecimal(10));
		orderMap.put("qty_ord_total", new BigDecimal(2));
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> itemMap1 = new HashMap<String, Object>();
		itemMap1.put("seq_id", "seq111");
		itemMap1.put("item_id", "item1111");
		itemMap1.put("item_name", "商品1");
		itemMap1.put("item_kind_id", "kind123");
		itemMap1.put("big_pri4", new BigDecimal(5));
		itemMap1.put("cost", new BigDecimal(3));
		itemMap1.put("amt_ord", new BigDecimal(10));
		itemMap1.put("qty_ord", new BigDecimal(2));
		list.add(itemMap1);
		orderMap.put("list", list);
		
		SaleOrder saleOrder = new SaleOrder(orderMap);
		p.execute(saleOrder);
		System.out.println(saleOrder);
		
		Map<String, Object> itemMap2 = new HashMap<String, Object>();
		orderMap.put("amt_ord_total", new BigDecimal(160));
		orderMap.put("qty_ord_total", new BigDecimal(5));
		itemMap2.put("seq_id", "seq222");
		itemMap2.put("item_id", "item222");
		itemMap1.put("item_name", "商品2");
		itemMap2.put("item_kind_id", "kind321");
		itemMap2.put("big_pri4", new BigDecimal(50));
		itemMap2.put("cost", new BigDecimal(35));
		itemMap2.put("amt_ord", new BigDecimal(150));
		itemMap2.put("qty_ord", new BigDecimal(3));
		list.add(itemMap2);
		
		saleOrder = new SaleOrder(orderMap);
		p.execute(saleOrder);
		System.out.println(saleOrder);
	}
	
	public static void main(String[] args) {
		
		/**
		 * -----3个促销
		 * 	"10":"商品折扣", "13":"会员折扣", "20":"搭配促销", "30":"商品积分", "33":"销售单积分", "40":"满减促销", "50":"抽奖"};
		 * 1、商品折扣，共存，退让，
		 * 2、商品折扣，共存，退让
		 * 3、促销满减，共存，退让
		 */
		
		Map<String, Object> promotion1 = new HashMap<String, Object>();
		promotion1.put("promotion_id", "promotion111");
		promotion1.put("promotion_title", "title111");
		promotion1.put("promotion_content", "content111");
		promotion1.put("promotion_type", "10");
		promotion1.put("start_date", "20140101");
		promotion1.put("end_date", "20140909");
		promotion1.put("start_time", "000000");
		promotion1.put("end_time", "130000");
		promotion1.put("is_coexistent", "1");
		promotion1.put("is_insistent", "1");
		Map<String, Object> must = new HashMap<String, Object>();
		Map<String, Object> target = new HashMap<String, Object>();
		target.put("all_memeber", "true");
		must.put("promotion_target", target);
		promotion1.put("promotion_must", must);
		Map<String, Object> should = new HashMap<String, Object>();
		Map<String, Object> seqId = new HashMap<String, Object>();
		Map<String, Object> seq111 = new HashMap<String, Object>();
		seq111.put("discount", new BigDecimal(40));
		seqId.put("seq111", seq111);
		should.put("seq_id", seqId);
		promotion1.put("promotion_should", should);
		promotions.add(promotion1);
		
		Map<String, Object> promotion2 = new HashMap<String, Object>();
		promotion2.put("promotion_id", "promotion222");
		promotion2.put("promotion_title", "title222");
		promotion2.put("promotion_content", "content222");
		promotion2.put("promotion_type", "10");
		promotion2.put("start_date", "20140101");
		promotion2.put("end_date", "20140909");
		promotion2.put("start_time", "000000");
		promotion2.put("end_time", "130000");
		promotion2.put("is_coexistent", "1");
		promotion2.put("is_insistent", "1");
		Map<String, Object> must2 = new HashMap<String, Object>();
		Map<String, Object> target2 = new HashMap<String, Object>();
		target2.put("all_memeber", "true");
		must2.put("promotion_target", target2);
		promotion2.put("promotion_must", must2);
		Map<String, Object> should2 = new HashMap<String, Object>();
		Map<String, Object> seqId2 = new HashMap<String, Object>();
		Map<String, Object> seq1232 = new HashMap<String, Object>();
		seq1232.put("discount", new BigDecimal(10));
		seqId2.put("seq123", seq1232);
		should2.put("seq_id", seqId2);
		promotion2.put("promotion_should", should2);
		promotions.add(promotion2);
		
		
		Map<String, Object> promotion3 = new HashMap<String, Object>();
		promotion3.put("promotion_id", "promotion333");//id
		promotion3.put("promotion_title", "title333");//标题
		promotion3.put("promotion_content", "content333");//内容
		promotion3.put("promotion_type", "40");//类型
		promotion3.put("start_date", "20140101");//开始日期
		promotion3.put("end_date", "20140909");//结束日期
		promotion3.put("start_time", "000000");//开始日期
		promotion3.put("end_time", "130000");//结束日期
		promotion3.put("is_coexistent", "1");//
		promotion3.put("is_insistent", "1");
		Map<String, Object> must3 = new HashMap<String, Object>();
		Map<String, Object> target3 = new HashMap<String, Object>();
		target3.put("all", "true");
		must3.put("promotion_target", target3);
		promotion3.put("promotion_must", must3);
		Map<String, Object> action3 = new HashMap<String, Object>();
		Map<String, Object> ceilingReduction = new HashMap<String, Object>();
		ceilingReduction.put("ceiling", new BigDecimal(100));
		ceilingReduction.put("reduction", new BigDecimal(10));
		action3.put("ceiling_reduction", ceilingReduction);
		promotion3.put("promotion_action", action3);
		promotions.add(promotion3);
		
		lottery2();
	}
	
}

class PromotionExecutorFactory {
	
	public static PromotionExecutor getPromotionExecutor(List<Map<String, Object>> promotions) {
		PromotionExecutor executor = null;
		for(Map<String, Object> promotion : promotions) {
			String promotionType = MapUtil.getString(promotion, "promotion_type");
			if("10".equals(promotionType)) {
				if(executor==null) {
					executor = new ItemDiscountPromotionExecutor(promotion);
				}
				else{
					executor.appendExecutor(new ItemDiscountPromotionExecutor(promotion));
				}
			} else if("13".equals(promotionType)) {
				if(executor==null) {
					executor = new DiscountPromotionExecutor(promotion);
				}
				else{
					executor.appendExecutor(new DiscountPromotionExecutor(promotion));
				}
			} else if("30".equals(promotionType)) {
				if(executor==null){
					executor = new ItemPointPromotionExecutor(promotion);
				}
				else{
					executor.appendExecutor(new ItemPointPromotionExecutor(promotion));
				}
			} else if("40".equals(promotionType)) {
				if(executor==null){
					executor = new CeilingReductionPromotionExecutor(promotion);
				}
				else{
					executor.appendExecutor(new CeilingReductionPromotionExecutor(promotion));
				}
			}
		}
		return executor;
	}
	
}

abstract class PromotionExecutor {
	
	static List<PromotionExecutor> executorHistory = new ArrayList<PromotionExecutor>(); // order by is_coexistent asc, is_insistent desc, start_date desc, start_time desc
	String promotionId, promotionTitle, promotionContent, promotionType, startDate, endDate, startTime, endTime, isCoexistent, isInsistent;
	Map<String, Object> must, target, action;
	Map<String, PromotionShouldElement> should;
	PromotionStrategy promotionStrategy;
	PromotionExecutor nextExecutor;
	boolean isUsed;
	
	PromotionExecutor(Map<String, Object> promotion) {
		promotionId = MapUtil.getString(promotion, "promotion_id");
		promotionTitle = MapUtil.getString(promotion, "promotion_title");
		promotionContent = MapUtil.getString(promotion, "promotion_content");
		promotionType = MapUtil.getString(promotion, "promotion_type");
		startDate = MapUtil.getString(promotion, "start_date");
		endDate = MapUtil.getString(promotion, "end_date");
		startTime = MapUtil.getString(promotion, "start_time");
		endTime = MapUtil.getString(promotion, "end_time");
		must = MapUtil.get(promotion, "promotion_must", Collections.EMPTY_MAP);
		target = MapUtil.get(must, "promotion_target", Collections.EMPTY_MAP);
		Map<String, Map> should = MapUtil.get(promotion, "promotion_should", Collections.EMPTY_MAP);
		this.should = new HashMap<String, PromotionShouldElement>();
		Object set = should.entrySet();
		
		for(Entry<String, Map> entry : should.entrySet()) {
			String key = entry.getKey();
			if("seq_id".equals(key)) {
				Set<Entry<String, Map<String, Object>>> valueSet = entry.getValue().entrySet();
				for(Entry<String, Map<String, Object>> e : valueSet) {
					this.should.put(e.getKey(), new PromotionShouldElement(e.getValue()));
				}
			} else if("item_kind_id".equals(key)) {
				Set<Entry<String, Map<String, Object>>> valueSet = entry.getValue().entrySet();
				for(Entry<String, Map<String, Object>> e : valueSet) {
					this.should.put(e.getKey(), new PromotionShouldElement(e.getValue()));
				}
			} else if("all_item".equals(key)) {
				Map<String, Object> value = entry.getValue();
				this.should.put(key, new PromotionShouldElement(value));
			}
		}
		action = MapUtil.get(promotion, "promotion_action", Collections.EMPTY_MAP);
		isCoexistent = MapUtil.getString(promotion, "is_coexistent");
		isInsistent = MapUtil.getString(promotion, "is_insistent");
	}
	
	void loadStrategy(PromotionStrategy promotionStrategy) {
		this.promotionStrategy = promotionStrategy;
	}
	
	void appendExecutor(PromotionExecutor nextExecutor) {
		this.nextExecutor = nextExecutor;
	}
	
	boolean matchLine(SaleOrderLine saleOrderLine) { return false; }
	
	PromotionShouldElement getShouldElementFromLine(SaleOrderLine saleOrderLine) {
		PromotionShouldElement element = 
				should.get(saleOrderLine.seqId)!=null ? should.get(saleOrderLine.seqId) 
						: should.get(saleOrderLine.itemKindId)!=null ? should.get(saleOrderLine.itemKindId) 
								: should.get("all_item")!=null ? element=should.get("all_item") : null;
		return element;
	}
	
	boolean matchOrder(SaleOrder saleOrder) { return true; }
	
	synchronized void execute(SaleOrder saleOrder) {
		if(promotionStrategy==null) {
			throw new RuntimeException("未设置促销策略(PromotionStrategy)");
		}
		if(executorHistory.isEmpty()) {
			saleOrder.reset();
		}
		else {
			saleOrder.first();
		}
		if(matchTime(saleOrder) && matchConsumer(saleOrder) && isActive()) {
			promotionStrategy.promote(saleOrder);
			if(isUsed){
				executorHistory.add(this);
			}
		}
		if(nextExecutor!=null) {
			nextExecutor.execute(saleOrder);
		}
		else {
			executorHistory.clear();
		}
	}
	
	boolean matchTime(SaleOrder saleOrder) {
		long saleTime = Long.parseLong(saleOrder.saleDate + saleOrder.saleTime);
		long startTime = Long.parseLong(this.startDate + this.startTime);
		long endTime = Long.parseLong(this.endDate + this.endTime);
		return saleTime>=startTime && saleTime<=endTime;
	}
	
	boolean matchConsumer(SaleOrder saleOrder) {
		return target.containsKey("all") 
				|| saleOrder.hasConsumer() && (target.containsKey("all_member") 
						|| target.containsKey("member_grade") && MapUtil.get(target, "member_grade", Collections.EMPTY_LIST).contains(saleOrder.consumerGrade)
						|| target.containsKey("member") && MapUtil.get(target, "member", Collections.EMPTY_LIST).contains(saleOrder.consumerId));
	}
	
	boolean isActive() {
		for(PromotionExecutor lastExecutor : executorHistory) {
			if(isConflict(lastExecutor)){
				return false;
			}
		}
		return true;
	}
	
	boolean isConflict(PromotionExecutor lastExecutor) {
		return isTypeConflict(lastExecutor) || isCoexistent()&&lastExecutor.isCoexistent() ? false : lastExecutor.isInsistent() ? true : isInsistent() ? false : true;
	}
	
	boolean isTypeConflict(PromotionExecutor lastExecutor) {
		return this.promotionType.equals(lastExecutor.promotionType);
	}
	
	boolean isCoexistent() {
		return "1".equals(isCoexistent);
	}
	
	boolean isInsistent() {
		return "1".equals(isInsistent);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(promotionId);
		sb.append(" ");
		if(nextExecutor!=null){
			sb.append(nextExecutor.toString());
		}
		return sb.toString();
	}
	
}

class ItemDiscountPromotionExecutor extends PromotionExecutor {
	
	ItemDiscountPromotionExecutor(Map<String, Object> promotion) {
		super(promotion);
		loadStrategy(new LinePromotionStrategy(this));
	}

	@Override
	boolean matchLine(SaleOrderLine line) {
		PromotionShouldElement element = getShouldElementFromLine(line);
		if(element!=null) {
			element.caculateAdjustedPrice(line);
			line.promotionIds.add(this.promotionId);
			return true;
		}
		return false;
	}
	
}

class ItemPointPromotionExecutor extends PromotionExecutor {
	
	ItemPointPromotionExecutor(Map<String, Object> promotion) {
		super(promotion);
		loadStrategy(new LinePromotionStrategy(this));
	}
	
	@Override
	boolean matchLine(SaleOrderLine line) {
		PromotionShouldElement element = getShouldElementFromLine(line);
		if(element!=null) {
			element.caculateAdjustedPoint(line);
			line.promotionIds.add(this.promotionId);
			return true;
		}
		return false;
	}
	
}

class DiscountPromotionExecutor extends PromotionExecutor {
	
	DiscountPromotionExecutor(Map<String, Object> promotion) {
		super(promotion);
		loadStrategy(new OrderPromotionStrategy(this));
	}

	@Override
	boolean matchOrder(SaleOrder saleOrder) {
		Map<String, Object> discountMap = MapUtil.get(action, "discount", Collections.EMPTY_MAP);
		BigDecimal discount = null;
		if((discount=MapUtil.getBigDecimal(discountMap, saleOrder.consumerId))!=null 
				|| (discount=MapUtil.getBigDecimal(discountMap, saleOrder.consumerGrade))!=null
				|| (discount=MapUtil.getBigDecimal(discountMap, "all"))!=null) {
			saleOrder.adjustedAmount = saleOrder.adjustedAmount
					.add(saleOrder.totalAmount.subtract(saleOrder.adjustedAmount).multiply(discount).divide(new BigDecimal(100)));
			saleOrder.promotionIds.add(this.promotionId);
			return true;
		}
		return false;
	}
	
}

class CeilingReductionPromotionExecutor extends PromotionExecutor {
	
	CeilingReductionPromotionExecutor(Map<String, Object> promotion) {
		super(promotion);
		loadStrategy(new OrderPromotionStrategy(this));
	}

	@Override
	boolean matchOrder(SaleOrder saleOrder) {
		Map<String, Object> ceilingReductionMap = MapUtil.get(action, "ceiling_reduction", Collections.EMPTY_MAP);
		BigDecimal ceiling = MapUtil.getBigDecimal(ceilingReductionMap, "ceiling");
		BigDecimal reduction = MapUtil.getBigDecimal(ceilingReductionMap, "reduction");
		if(saleOrder.totalAmount.subtract(saleOrder.adjustedAmount).compareTo(ceiling)>=0) {
			saleOrder.adjustedAmount = saleOrder.adjustedAmount.add(reduction);
			saleOrder.promotionIds.add(this.promotionId);
			return true;
		}
		return false;
	}
	
}

interface PromotionStrategy {
	void promote(SaleOrder saleOrder);
}

class LinePromotionStrategy implements PromotionStrategy {
	
	PromotionExecutor executor;
	
	LinePromotionStrategy(PromotionExecutor executor) {
		this.executor = executor;
	}
	
	public void promote(SaleOrder saleOrder) {
		while(saleOrder.hasMoreLines()) {
			SaleOrderLine saleOrderLine = saleOrder.next();
			boolean isMatch = executor.matchLine(saleOrderLine);
			executor.isUsed = isMatch || executor.isUsed;
			if(isMatch) {
				saleOrderLine.adjustedAmount = saleOrderLine.quantity.multiply(saleOrderLine.adjustedPrice);
				saleOrderLine.profit = saleOrderLine.price.subtract(saleOrderLine.adjustedPrice).subtract(saleOrderLine.cost);
				saleOrder.adjustedAmount = saleOrder.adjustedAmount.add(saleOrderLine.adjustedAmount);
			}
		}
	}
}

class OrderPromotionStrategy implements PromotionStrategy {
	
	PromotionExecutor executor;
	
	OrderPromotionStrategy(PromotionExecutor executor) {
		this.executor = executor;
	}
	
	public void promote(SaleOrder saleOrder) {
		executor.isUsed = executor.matchOrder(saleOrder) || executor.isUsed;
	}
}

class PromotionShouldElement {
	
	BigDecimal discount, discountPrice, point;
	
	PromotionShouldElement(Map<String, Object> shouldElement) {
		discount = MapUtil.getBigDecimal(shouldElement, "discount", null);
		discountPrice = MapUtil.getBigDecimal(shouldElement, "discount_price", null);
		point = MapUtil.getBigDecimal(shouldElement, "point", null);
	}
	
	void caculateAdjustedPrice(SaleOrderLine line) {
		if(discountPrice!=null) {
			line.adjustedPrice = line.price.subtract(discountPrice);
		} else if(discount!=null) {
			line.adjustedPrice = line.adjustedPrice
					.add(line.price.subtract(line.adjustedPrice).multiply(discount).divide(new BigDecimal(100)));
		}
	}
	
	void caculateAdjustedPoint(SaleOrderLine line) {
		line.promotionPoint = line.promotionPoint.add(this.point==null ? BigDecimal.ZERO : this.point);
	}
	
}

/*
class PromotionActionElement {
	
	BigDecimal point, reduction, ceiling;
	Map<String, BigDecimal> ceilingReduction;
	Map<String, BigDecimal> discount;
	
	PromotionActionElement(Map<String, Object> actionElement) {
		discount = MapUtil.get(actionElement, "discount", Collections.EMPTY_MAP);
		point = MapUtil.getBigDecimal(actionElement, "point", null);
		ceilingReduction = MapUtil.get(actionElement, "ceiling_reduction", Collections.EMPTY_MAP);
		ceiling = MapUtil.getBigDecimal(ceilingReduction, "ceiling", null);
		reduction = MapUtil.getBigDecimal(ceilingReduction, "reduction", null);
	}
	
	BigDecimal caculateAdjustedAmount(BigDecimal totalAmount) {
		return discount!=null ? totalAmount.multiply(discount).divide(new BigDecimal(100)) 
				: totalAmount.compareTo(ceiling)>0 ? totalAmount.subtract(reduction) : totalAmount;
	}
	
	BigDecimal caculateAdjustedPoint() {
		return this.point==null ? BigDecimal.ZERO : this.point;
	}
	
}
*/

class SaleOrder {
	
	String consumerId, saleDate, saleTime;
	int lineIndex, consumerGrade = -1;
	BigDecimal totalQuantity, totalAmount, adjustedAmount, adjustedPoint;
	List<SaleOrderLine> saleOrderLines = new ArrayList<SaleOrderLine>();
	List<String> promotionIds = new ArrayList<String>();
	
	SaleOrder(Map<String, Object> saleOrder) {
		consumerId = MapUtil.getString(saleOrder, "consumer_id");
		consumerGrade = MapUtil.getInt(saleOrder, "consumer_grade", -1);
		saleDate = MapUtil.getString(saleOrder, "sale_date", DateUtil.getToday());
		saleTime = MapUtil.getString(saleOrder, "sale_time", DateUtil.getCurrentTime().substring(8));
		totalQuantity = BigDecimal.ZERO;
		totalAmount = MapUtil.getBigDecimal(saleOrder, "amt_ord_total");
		adjustedAmount = BigDecimal.ZERO;
		adjustedPoint = BigDecimal.ZERO;
		List<Map<String, Object>> saleOrderLines = MapUtil.get(saleOrder, "list", Collections.EMPTY_LIST);
		for(Map<String, Object> saleOrderLine : saleOrderLines) {
			SaleOrderLine line = new SaleOrderLine(saleOrderLine);
			this.saleOrderLines.add(line);
			totalQuantity = totalQuantity.add(line.quantity);
		}
	}
	
	boolean hasConsumer() {
		return !StringUtil.isBlank(consumerId) && consumerGrade!=-1;
	}
	
	boolean hasMoreLines() {
		return lineIndex<saleOrderLines.size();
	}
	
	SaleOrderLine next() {
		return saleOrderLines.get(lineIndex++);
	}
	
	SaleOrderLine first() {
		lineIndex = 0;
		return saleOrderLines.get(lineIndex);
	}
	
	void reset() {
		lineIndex = 0;
		while(hasMoreLines()) {
			next().reset();
		}
		promotionIds.clear();
		lineIndex = 0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("consumerId: ");
		sb.append(consumerId);
		sb.append(", consumerGrade: ");
		sb.append(consumerGrade);
		sb.append(", saleDate: ");
		sb.append(saleDate);
		sb.append(", saleTime: ");
		sb.append(saleTime);
		sb.append(", totalAmount: ");
		sb.append(totalAmount);
		sb.append(", saleOrderLines: ");
		sb.append(saleOrderLines);
		sb.append(", adjustedAmount: ");
		sb.append(adjustedAmount);
		sb.append(", adjustedPoint: ");
		sb.append(adjustedPoint);
		sb.append(", promotionIds: ");
		sb.append(promotionIds);
		return sb.toString();
	}
	
}

class SaleOrderLine {
	
	String itemId, itemKindId, seqId;
	BigDecimal cost, profit, price, quantity, amount, adjustedPrice, promotionPoint, adjustedAmount;
	List<String> promotionIds = new ArrayList<String>();
	
	SaleOrderLine(Map<String, Object> saleOrderLine) {
		itemId = MapUtil.getString(saleOrderLine, "item_id");
		itemKindId = MapUtil.getString(saleOrderLine, "item_kind_id");
		seqId = MapUtil.getString(saleOrderLine, "seq_id");
		quantity = MapUtil.getBigDecimal(saleOrderLine, "qty_ord", BigDecimal.ONE);
		amount = MapUtil.getBigDecimal(saleOrderLine, "amt_ord");
		cost = MapUtil.getBigDecimal(saleOrderLine, "cost");
		price = MapUtil.getBigDecimal(saleOrderLine, "big_pri3");
		profit = MapUtil.getBigDecimal(saleOrderLine, "profit");
		adjustedPrice = MapUtil.getBigDecimal(saleOrderLine, "adjusted_price");
		promotionPoint = MapUtil.getBigDecimal(saleOrderLine, "promotion_point");
		adjustedAmount = MapUtil.getBigDecimal(saleOrderLine, "adjusted_amount");
	}
	
	void reset() {
		adjustedPrice = BigDecimal.ZERO;
		promotionPoint = BigDecimal.ZERO;
		promotionIds.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("itemId: ");
		sb.append(itemId);
		sb.append(", seqId: ");
		sb.append(seqId);
		sb.append(", itemKindId: ");
		sb.append(itemKindId);
		sb.append(", quantity: ");
		sb.append(quantity);
		sb.append(", amount: ");
		sb.append(amount);
		sb.append(", price: ");
		sb.append(price);
		sb.append(", adjustedPrice: ");
		sb.append(adjustedPrice);
		sb.append(", promotionPoint: ");
		sb.append(promotionPoint);
		sb.append(", adjustedAmount: ");
		sb.append(adjustedAmount);
		sb.append(", promotionIds: ");
		sb.append(promotionIds);
		return sb.toString();
	}
	
}
