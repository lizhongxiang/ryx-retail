package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IItemDao;
import com.ryx.social.retail.dao.IMerchConsumerDao;
import com.ryx.social.retail.dao.IOrderDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.IOrderService;
import com.ryx.social.retail.service.IReturnOrderService;

@Service
public class OrderServiceImpl implements IOrderService {
	
	private Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);
	
	@Resource
	private IOrderDao orderDao;
	@Resource
	private IMerchConsumerDao merchConsumerDao;
	@Resource
	private IItemDao itemDao;
	@Resource
	private IWhseDao whseDao;
//	@Resource
//	private IReturnOrderDao returnOrderDao;
	
	@Resource
	private IReturnOrderService returnOrderService;
	
	/**
	 * 查询销售单, 销售/网购流水的列表, 可以按照order_id, order_date, status, pmt_status, ORDER_TYPE, CONSUMER_ID查询
	 * 增加对会员信息的关联
	 */
	@Override
	public List<Map<String, Object>> searchSaleOrder(Map<String, Object> saleOrderParam) throws Exception {
		LOG.debug("OrderServiceImpl searchSaleOrder saleOrderParam: " + saleOrderParam);
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(saleOrderParam);
		for (Map<String, Object> saleOrderMap : saleOrderList) {
			Map<String, Object> consumerParam = new HashMap<String, Object>();
			consumerParam.put("consumer_id", saleOrderMap.get("consumer_id"));
			if(saleOrderMap.get("consumer_id")==null){
				continue;
			}
			consumerParam.put("merch_id", saleOrderMap.get("merch_id"));			
			List<Map<String, Object>> consumerList = merchConsumerDao.selectMerchConsumer(consumerParam);
			if(consumerList!=null) {
				for(Map<String, Object> consumerMap : consumerList) {
					if(saleOrderMap.get("consumer_id").equals(consumerMap.get("consumer_id"))){
						consumerMap.remove("status");
						saleOrderMap.putAll(consumerMap);
					}
				}
			}
		}
		return saleOrderList;
	}
	
	/**
	 * 联表查询
	 * 销售管理(SALE_ORDER)  销售单行(SALE_ORDER_LINE) 关联 
	 */
	@Override
	public List<Map<String, Object>> searchSaleOrderAndSaleOrderLine(Map<String, Object> paramMap) throws Exception {
		LOG.debug("OrderServiceImpl searchSaleOrderAndSaleOrderLine paramMap: " + paramMap);
		
		List<Map<String, Object>> consumerListMap = orderDao.selectSaleOrderAndSaleOrderLine(paramMap);
		return consumerListMap;
	}
	/**
	 * 联表查询
	 * 采购管理(PURCH_ORDER)   采购单行(PURCH_ORDER_LINE)
	 */
	@Override
	public List<Map<String, Object>> searchPurchOrderAndPurchOrderLine(Map<String, Object> paramMap) throws Exception {
		LOG.debug("OrderServiceImpl searchSaleOrderAndSaleOrderLine paramMap: " + paramMap);
		
		List<Map<String, Object>> consumerListMap = orderDao.selectPurchOrderAndPurchOrderLine(paramMap);
		return consumerListMap;
	}
	
	//查询销售单及商品信息
	@Override
	public List<Map<String, Object>> searchSaleOrderLineJoinItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("OrderServiceImpl searchSaleOrderLineJoinItem paramMap: " + paramMap);
		return orderDao.searchSaleOrderLineJoinItem(paramMap);
	}
	
	
//	/**
//	 * 联表查询
//	 * 销售单行(SALE_ORDER_LINE) 	退货单(RETURN_ORDER)
//	 */
//	@Override
//	public List<Map<String, Object>> searchSaleOrderLineAndReturnOrder(Map<String, Object> paramMap) throws Exception {
//		LOG.debug("OrderServiceImpl searchSaleOrderLineAndReturnOrder paramMap: " + paramMap);
//		
//		List<Map<String, Object>> consumerListMap = orderDao.selectSaleOrderLineAndReturnOrder(paramMap);
//		return consumerListMap;
//	}
	
	/**
	 * 销售流水-查询列表
	 * (sale_order, sale_order_line, return_order)
	 * 把会员卡号和手机号也作为查询销售单的条件
	 */
	@Override
	public List<Map<String, Object>> searchConsumerJoinSaleReturnOrder(Map<String, Object> paramMap) throws Exception {
		LOG.debug("OrderServiceImpl searchConsumerJoinSaleReturnOrder paramMap: " + paramMap);
		Map<String, Map<String, Object>> consumerListMap = new HashMap<String, Map<String, Object>>();
		StringBuffer consumerIdBuffer = new StringBuffer();
		if(paramMap.get("consumer_keyword")!=null) {
			Map<String, Object> consumerParam = new HashMap<String, Object>();
			consumerParam.put("merch_id", paramMap.get("merch_id"));
			consumerParam.put("keyword", paramMap.get("consumer_keyword"));
			consumerParam.put("page_index", -1);
			consumerParam.put("page_size", -1);
			List<Map<String, Object>> consumerList = merchConsumerDao.selectMerchConsumer(consumerParam);
			for(Map<String, Object> consumerMap : consumerList) {
				consumerListMap.put(consumerMap.get("consumer_id").toString(), consumerMap);
				if(consumerIdBuffer.length()==0) consumerIdBuffer.append(consumerMap.get("consumer_id"));
				else consumerIdBuffer.append(","+consumerMap.get("consumer_id"));
			}
			if(consumerIdBuffer.length()==0) consumerIdBuffer.append(paramMap.get("consumer_keyword"));
			else consumerIdBuffer.append(","+paramMap.get("consumer_keyword"));
			paramMap.put("consumer_id", consumerIdBuffer.toString());
		}
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(paramMap);//查询销售单
		Map<String, Map<String, Object>> saleOrderListMap = new HashMap<String, Map<String, Object>>();
		StringBuffer orderIdBuffer = new StringBuffer();
		// 如果没有输入会员信息, 则查一遍会员信息
		if(paramMap.get("consumer_keyword")==null) {
			consumerIdBuffer = new StringBuffer();
			for(Map<String, Object> saleOrderMap : saleOrderList) {
				saleOrderMap.put("qty_return_total", BigDecimal.ZERO);
				saleOrderListMap.put(saleOrderMap.get("order_id").toString(), saleOrderMap);
				if(saleOrderMap.get("consumer_id")!=null) {
					if(consumerIdBuffer.length()==0) consumerIdBuffer.append(saleOrderMap.get("consumer_id").toString());
					else consumerIdBuffer.append(","+saleOrderMap.get("consumer_id"));
				}
				if(orderIdBuffer.length()==0) orderIdBuffer.append(saleOrderMap.get("order_id").toString());
				else orderIdBuffer.append(","+saleOrderMap.get("order_id"));
			}
			if(consumerIdBuffer.length()>0) {
				Map<String, Object> consumerParam = new HashMap<String, Object>();
				consumerParam.put("merch_id", paramMap.get("merch_id"));
				consumerParam.put("consumer_id", consumerIdBuffer.toString());
				List<Map<String, Object>> consumerList = merchConsumerDao.selectMerchConsumer(consumerParam);
				Map<String, Map<String, Object>> consumerMap = new HashMap<String, Map<String,Object>>();
				for (Map<String, Object> map : consumerList) {
					map.remove("status");
					consumerMap.put(MapUtil.getString(map, "consumer_id"), map);
				}
				String orderConsumerId = "";
				for(Map<String, Object> saleOrderMap : saleOrderList) {
					orderConsumerId = MapUtil.getString(saleOrderMap, "consumer_id");
					if(!StringUtil.isBlank(orderConsumerId)) {
						if(consumerMap.containsKey(orderConsumerId)){
							saleOrderMap.putAll(consumerMap.get(orderConsumerId));
						}
						else{
							saleOrderMap.put("telephone",  orderConsumerId);
						}
					}
				}
			}
		// 如果输入会员信息, 将上面的会员信息复制到销售单中
		} else {
			for(Map<String, Object> saleOrderMap : saleOrderList) {
				saleOrderMap.put("qty_return_total", BigDecimal.ZERO);
				saleOrderListMap.put(saleOrderMap.get("order_id").toString(), saleOrderMap);
				if(consumerListMap.containsKey(saleOrderMap.get("consumer_id"))) {
					Map<String, Object> consumerMap = consumerListMap.get(saleOrderMap.get("consumer_id"));
					consumerMap.remove("status");
					saleOrderMap.putAll(consumerMap);
				}
				if(orderIdBuffer.length()==0) orderIdBuffer.append(saleOrderMap.get("order_id").toString());
				else orderIdBuffer.append(","+saleOrderMap.get("order_id"));
			}
		}
		if(orderIdBuffer.length()>0) {
			Map<String, Object> returnOrderParam = new HashMap<String, Object>();
			returnOrderParam.put("merch_id", paramMap.get("merch_id"));
			returnOrderParam.put("order_id", orderIdBuffer.toString());
			returnOrderParam.put("page_index", -1);
			returnOrderParam.put("page_size", -1);
			List<Map<String, Object>> returnOrderList = orderDao.selectReturnOrder(returnOrderParam);
			for(Map<String, Object> returnOrderMap : returnOrderList) {
				if(saleOrderListMap.containsKey(returnOrderMap.get("order_id"))) {
					Map<String, Object> saleOrderMap = (Map<String, Object>) saleOrderListMap.get(returnOrderMap.get("order_id"));
					saleOrderMap.put("qty_return_total", new BigDecimal(saleOrderMap.get("qty_return_total").toString()).add(new BigDecimal(returnOrderMap.get("qty_return_total").toString())));
				}
			}
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(saleOrderListMap.values());
		Collections.sort(list, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> map1, Map<String, Object> map2) {
				int date1 = Integer.valueOf(map1.get("order_date").toString());
				int date2 = Integer.valueOf(map2.get("order_date").toString());
				int time1 = Integer.valueOf(map1.get("order_time").toString());
				int time2 = Integer.valueOf(map2.get("order_time").toString());
				if(date1==date2) {
					return time2-time1;
				} else {
					return date2-date1;
				}
			}
		});
		return list;
	}
	
	//查询网购订单
	@Override
	public List<Map<String, Object>> searchWgddOrder(Map<String, Object> saleOrderParam) throws Exception {
		LOG.debug("OrderServiceImpl selectWgddOrder saleOrderParam: " + saleOrderParam);
		return orderDao.selectWgddOrder(saleOrderParam);
	}
	
	/**
	 * 查询销售单包含销售单中所有商品, 只能按照order_id查询, 返回sale_order的map, 内含sale_order_line的list
	 * （sale_order, sale_order_line）
	 */
	@Override
	public Map<String, Object> searchSaleOrderJoinSaleOrderLine(Map<String, Object> saleOrderJoinSaleOrderLineParam) throws Exception {
		LOG.debug("OrderServiceImpl searchSaleOrderJoinSaleOrderLine saleOrderJoinSaleOrderLineParam: " + saleOrderJoinSaleOrderLineParam);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("merch_id", saleOrderJoinSaleOrderLineParam.get("merch_id"));
		paramMap.put("order_id", saleOrderJoinSaleOrderLineParam.get("order_id"));
		Map<String, Object> saleOrderJoinSaleOrderLineMap = new HashMap<String, Object>();
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(paramMap);
		if(saleOrderList.size()==1) {
			List<Map<String, Object>> saleOrderLineList = orderDao.selectSaleOrderLine(paramMap);
			if(saleOrderLineList.size()>0) {
				saleOrderJoinSaleOrderLineMap = saleOrderList.get(0);
				saleOrderJoinSaleOrderLineMap.put("list", saleOrderLineList);
				return saleOrderJoinSaleOrderLineMap;
			} // else return 空map;
		}
		return saleOrderJoinSaleOrderLineMap;
	}
	
	/**
	 * 销售流水-销售详情
	 * 查询销售单包含销售单中所有商品+退货量, 只能按照order_id查询, 返回sale_order的map, 内含sale_order_line的list
	 */
	@Override
	public Map<String, Object> searchSaleOrderJoinSaleOrderLineJoinReturnQty(Map<String, Object> saleOrderJoinSaleOrderLineParam) throws Exception {		
		LOG.debug("OrderServiceImpl searchSaleOrderJoinSaleOrderLine saleOrderJoinSaleOrderLineParam: " + saleOrderJoinSaleOrderLineParam);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("merch_id", saleOrderJoinSaleOrderLineParam.get("merch_id"));
		paramMap.put("order_id", saleOrderJoinSaleOrderLineParam.get("order_id"));
		Map<String, Object> saleOrderJoinSaleOrderLineMap = new HashMap<String, Object>();
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(paramMap);//获得销售单 
		if(saleOrderList.size()==1) {
			List<Map<String, Object>> returnOrderList = orderDao.selectReturnOrder(paramMap);//获得退货单		
			List<Map<String, Object>> saleOrderLineList = orderDao.selectSaleOrderLine(paramMap);//获得销售单行			
			if(saleOrderLineList.size()>0) {
				saleOrderJoinSaleOrderLineMap = saleOrderList.get(0);
				BigDecimal newSumReturnQty=BigDecimal.ZERO;//退货总量
				BigDecimal newSumReturnAmt=BigDecimal.ZERO;//退货总额
				BigDecimal newSumReturnLoss=BigDecimal.ZERO;//退货总利
				if(!returnOrderList.isEmpty()){
					for (Map<String, Object> returnMap : returnOrderList) {
						newSumReturnQty=newSumReturnQty.add(MapUtil.getBigDecimal(returnMap,"qty_return_total"));
						newSumReturnAmt=newSumReturnAmt.add(MapUtil.getBigDecimal(returnMap,"amt_return_total"));
						newSumReturnLoss=newSumReturnLoss.add(MapUtil.getBigDecimal(returnMap,"amt_return_loss"));
					}
				}
				saleOrderJoinSaleOrderLineMap.put("sum_return_qty",newSumReturnQty );
				saleOrderJoinSaleOrderLineMap.put("sum_return_amt", newSumReturnAmt);
				saleOrderJoinSaleOrderLineMap.put("sum_return_loss", newSumReturnLoss);
				List<Map<String, Object>> returnOrderLineList=new ArrayList<Map<String,Object>>();//退货单行
				for (Map<String, Object> map : returnOrderList) {
					Map<String, Object> searchROLineParam=new HashMap<String, Object>();
					searchROLineParam.put("order_id", map.get("return_order_id"));// 用退货单号在销售单行中查退货信息
					returnOrderLineList.addAll(this.searchReturnOrderLine(searchROLineParam));//获得退货单行
				}
				for (Map<String, Object> saleMap : saleOrderLineList) {
					saleMap.put("qty_ret", BigDecimal.ZERO);
					saleMap.put("amt_ret", BigDecimal.ZERO);
					BigDecimal adjustedAmount = MapUtil.getBigDecimal(saleMap, "adjusted_amount");
					BigDecimal otherAdjustedAmount = MapUtil.getBigDecimal(saleMap, "other_adjusted_amount");
					BigDecimal amtOrd = MapUtil.getBigDecimal(saleMap, "amt_ord");
					BigDecimal newAmtOrd = amtOrd.subtract(adjustedAmount).subtract(otherAdjustedAmount);
					saleMap.put("old_amt_ord", amtOrd);
					saleMap.put("amt_ord", newAmtOrd);
					for (Map<String, Object> returnMap : returnOrderLineList) {
						if(saleMap.get("line_num").equals(returnMap.get("sale_line_num"))){ 
							saleMap.put("qty_ret", MapUtil.getBigDecimal(saleMap, "qty_ret").add(MapUtil.getBigDecimal(returnMap, "qty_ord")));
							saleMap.put("amt_ret", MapUtil.getBigDecimal(saleMap, "amt_ret").add(MapUtil.getBigDecimal(returnMap, "amt_ord")));
						}
					}
				}
				saleOrderJoinSaleOrderLineMap.put("list", saleOrderLineList);
				return saleOrderJoinSaleOrderLineMap;
			} // else return 空map;
		}
		return saleOrderJoinSaleOrderLineMap;
	}
	
	/**
	 * 删除商品前, 根据item_id查询是否可以删除
	 */
	@Override
	public List<Map<String, Object>> searchPurchSaleOrderLineByItemId(Map<String, Object> paramMap) throws Exception {		
		LOG.debug("OrderServiceImpl searchSaleOrderLineByItemId paramMap: " + paramMap);
		// 如果库存为0 不能删除
		if(!paramMap.containsKey("page_index")) {
			paramMap.put("page_index", -1);
		}
		if(!paramMap.containsKey("page_size")) {
			paramMap.put("page_size", -1);
		}
		List<Map<String, Object>> whseList = whseDao.selectWhseMerch(paramMap);
		for(Map<String, Object> whseMap : whseList) {
			if(!(new BigDecimal(whseMap.get("qty_whse").toString()).compareTo(BigDecimal.ZERO)==0)) {
				return whseList;
			}
		}
		List<Map<String, Object>> itemList = itemDao.selectMerchItem(paramMap); // item_id, merch_id
		Set<String> orderIdSet = new HashSet<String>(); // 根据item_id获取有多少order_id
		// 先查询purch_order_line 如果有进货则不能删除
		for(Map<String, Object> itemMap : itemList) {
			Map<String, Object> purchOrderParam = new HashMap<String, Object>();
			purchOrderParam.put("order_date_floor", itemMap.get("create_date"));
			purchOrderParam.put("order_time_floor", itemMap.get("create_time"));
			purchOrderParam.put("merch_id", itemMap.get("merch_id"));
			List<Map<String, Object>> purchOrderList = orderDao.selectPurchOrder(purchOrderParam); // merch_id, date, time
			for(Map<String, Object> purchOrderMap : purchOrderList) {
				orderIdSet.add((String) purchOrderMap.get("order_id"));
			}
		}
		Map<String, Object> purchOrderLineParam = new HashMap<String, Object>();
		StringBuffer orderIdBuffer = new StringBuffer();
		int index = 1;
		for(String orderId : orderIdSet) {
			if(index==1) {
				orderIdBuffer.append(orderId);
			} else {
				orderIdBuffer.append(","+orderId);
			}
			index++;
		}
		purchOrderLineParam.put("order_id", orderIdBuffer.toString());
		purchOrderLineParam.put("item_id", paramMap.get("item_id"));
		List<Map<String, Object>> purchOrderLine = orderDao.selectPurchOrderLine(purchOrderLineParam);
		if(!purchOrderLine.isEmpty()) {
			return orderDao.selectSaleOrderLine(purchOrderLineParam);
		}
		// 再查询sale_order_line 如果有销售不能删除, 逻辑同上, 只是查询的表不同
		orderIdSet = new HashSet<String>();
		for(Map<String, Object> itemMap : itemList) {
			Map<String, Object> saleOrderParam = new HashMap<String, Object>();
			saleOrderParam.put("order_date_floor", itemMap.get("create_date"));
			saleOrderParam.put("order_time_floor", itemMap.get("create_time"));
			saleOrderParam.put("merch_id", itemMap.get("merch_id"));
			List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(saleOrderParam); // merch_id, date, time
			for(Map<String, Object> saleOrderMap : saleOrderList) {
				orderIdSet.add((String) saleOrderMap.get("order_id"));
			}
		}
		Map<String, Object> saleOrderLineParam = new HashMap<String, Object>();
		orderIdBuffer = new StringBuffer();
		index = 1;
		for(String orderId : orderIdSet) {
			if(index==1) {
				orderIdBuffer.append(orderId);
			} else {
				orderIdBuffer.append(","+orderId);
			}
			index++;
		}
		saleOrderLineParam.put("order_id", orderIdBuffer.toString());
		saleOrderLineParam.put("item_id", paramMap.get("item_id"));
		return orderDao.selectSaleOrderLine(saleOrderLineParam);
	}
	
	/**
	 * 查询订货单, 入库时使用, 可以按照order_id, supplier_id, order_date, stauts, pmt_status查询
	 */
	@Override
	public List<Map<String, Object>> searchPurchOrder(Map<String, Object> purchOrderParam) throws Exception {
		LOG.debug("OrderServiceImpl searchPurchOrder purchOrderParam: " + purchOrderParam);
		return orderDao.selectPurchOrder(purchOrderParam);
	}
	
	/**
	 * 查询订货单包含单中所有订购商品, 只能按照order_id查询, 返回purch_order的map, 内含purch_order_line的list
	 */
	@Override
	public Map<String, Object> searchPurchOrderJoinPurchOrderLine(Map<String, Object> purchOrderJoinPurchOrderLineParam) throws Exception {
		LOG.debug("OrderServiceImpl searchPurchOrderJoinPurchOrderLine purchOrderJoinPurchOrderLineParam: " + purchOrderJoinPurchOrderLineParam);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("merch_id", purchOrderJoinPurchOrderLineParam.get("merch_id"));
		paramMap.put("order_id", purchOrderJoinPurchOrderLineParam.get("order_id"));
		Map<String, Object> purchOrderJoinPurchOrderLineMap = new HashMap<String, Object>();
		List<Map<String, Object>> purchOrderList = orderDao.selectPurchOrder(paramMap);
		if(purchOrderList.size()==1) {
			List<Map<String, Object>> purchOrderLineList = orderDao.selectPurchOrderLine(paramMap);
			if(purchOrderLineList.size()>0) {
				purchOrderJoinPurchOrderLineMap = purchOrderList.get(0);
				purchOrderJoinPurchOrderLineMap.put("list", purchOrderLineList);
				return purchOrderJoinPurchOrderLineMap;
			} // else return 空map
		}
		return purchOrderJoinPurchOrderLineMap;
	}
	
	/**
	 * 查询退货单, 可以按照order_id, return_order_id, supplier_id, order_date, stauts, pmt_status查询
	 */
	@Override
	public List<Map<String, Object>> searchReturnOrder(Map<String, Object> returnOrderParam) throws Exception {
		LOG.debug("OrderServiceImpl searchReturnOrder purchOrderParam: " + returnOrderParam);
		return orderDao.selectReturnOrder(returnOrderParam);
	}
	
	//获得退货单行	
	@Override
	public List<Map<String, Object>> searchReturnOrderLine(Map<String, Object> paramMap)throws Exception{
		LOG.debug("ReturnOrderServiceImpl searchReturnOrderLine params:"+paramMap);
		return orderDao.searchMerchReturnOrderLine(paramMap);
	}
	
	/**
	 * 查询退货单包含单中所有订购商品, 只能按照return_order_id查询, 返回return_order的map, 内含sale_order_line的list
	 */
	@Override
	public Map<String, Object> searchReturnOrderJoinSaleOrderLine(Map<String, Object> returnOrderJoinSaleOrderLineParam) throws Exception {
		LOG.debug("OrderServiceImpl searchReturnOrderJoinSaleOrderLine returnOrderJoinSaleOrderLineParam: " + returnOrderJoinSaleOrderLineParam);
		Map<String, Object> orderParam = new HashMap<String, Object>();
		orderParam.put("merch_id", returnOrderJoinSaleOrderLineParam.get("merch_id"));
		orderParam.put("return_order_id", returnOrderJoinSaleOrderLineParam.get("return_order_id"));
		Map<String, Object> returnOrderJoinSaleOrderLineMap = new HashMap<String, Object>();
		List<Map<String, Object>> returnOrderList = orderDao.selectReturnOrder(orderParam);
		if(returnOrderList.size()==1) {
			Map<String, Object> orderLineParam = new HashMap<String, Object>();
			orderLineParam.put("merch_id", orderParam.get("merch_id"));
			orderLineParam.put("order_id", orderParam.get("return_order_id"));
			List<Map<String, Object>> saleOrderLineList = this.searchReturnOrderLine(orderLineParam);
			if(saleOrderLineList.size()>0) {
				returnOrderJoinSaleOrderLineMap = returnOrderList.get(0);
				returnOrderJoinSaleOrderLineMap.put("list", saleOrderLineList);
				return returnOrderJoinSaleOrderLineMap;
			} // else return 空map
		}
		return returnOrderJoinSaleOrderLineMap;
	}
	/**
	 * 销售单,退货单 关联
	 *
	public List<Map<String,Object>> searchSaleReturnOrderReturnOrderLine(Map<String, Object> saleReturnOrderParam) throws Exception {
		LOG.debug("OrderServiceImpl searchSaleReturnOrderJoinSaleReturnOrderLine saleReturnOrderParam: " + saleReturnOrderParam);
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrderLineAndReturnOrder(saleReturnOrderParam);//获得销售单
		// 用退货单号(return_order_id) 行号(line_num)在销售单行中查退货信息
		return new ArrayList<Map<String, Object>>();
	}
	 */
	
	/**
	 * 销售退货时，查询销售单，实现退货
	 * （return_order, return_order_line, sale_order_line, sale_order ）
	 * 根据销售单号查询销售单信息 和销售单行减退货单行信息
	 */
	public List<Map<String, Object>> searchSaleReturnOrderJoinSaleReturnOrderLine(Map<String, Object> saleReturnOrderParam) throws Exception {
		LOG.debug("OrderServiceImpl searchSaleReturnOrderJoinSaleReturnOrderLine saleReturnOrderParam: " + saleReturnOrderParam);
		List<Map<String, Object>> saleOrderList = orderDao.selectSaleOrder(saleReturnOrderParam);//获得销售单
		if(saleOrderList.size()>0) { // 应该只有一个sale_order, 并且不修改sale_order信息
			List<Map<String, Object>> returnOrderLineList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> returnOrderList = orderDao.selectReturnOrder(saleReturnOrderParam);//获得退货单
			for(Map<String, Object> returnOrderMap : returnOrderList) {
				returnOrderMap.put("order_id", returnOrderMap.get("return_order_id")); // 用退货单号在销售单行中查退货信息
				returnOrderLineList.addAll(this.searchReturnOrderLine(returnOrderMap));//获得退货单行
			}
			//获得销售单行
			List<Map<String, Object>> saleOrderLineList = orderDao.selectSaleOrderLine(saleReturnOrderParam);
			List<Map<String, Object>> saleReturnOrderLineList = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> saleOrderLineMap : saleOrderLineList) {//遍历销售单行
				saleOrderLineMap.put("qty_ret", BigDecimal.ZERO);
				saleOrderLineMap.put("amt_ret", BigDecimal.ZERO);
				BigDecimal amtOrd = MapUtil.getBigDecimal(saleOrderLineMap, "amt_ord");
				BigDecimal adjustedAmount = MapUtil.getBigDecimal(saleOrderLineMap, "adjusted_amount");
				BigDecimal otherAdjustedAmount = MapUtil.getBigDecimal(saleOrderLineMap, "other_adjusted_amount");
				BigDecimal newAmtOrd = amtOrd.subtract(adjustedAmount).subtract(otherAdjustedAmount);
				saleOrderLineMap.put("amt_ord", newAmtOrd);
				for(Map<String, Object> returnOrderLineMap : returnOrderLineList) {//遍历退货单行
					// 行号相等才认为是相同的销售行数据
					if(saleOrderLineMap.get("line_num").equals(returnOrderLineMap.get("sale_line_num"))) {
						saleOrderLineMap.put("qty_ret", MapUtil.getBigDecimal(saleOrderLineMap, "qty_ret").add(MapUtil.getBigDecimal(returnOrderLineMap, "qty_ord")));
						saleOrderLineMap.put("amt_ret", MapUtil.getBigDecimal(saleOrderLineMap, "amt_ret").add(MapUtil.getBigDecimal(returnOrderLineMap, "amt_ord")));
					}
				}
				saleReturnOrderLineList.add(saleOrderLineMap);
			}
			saleOrderList.get(0).put("list", saleReturnOrderLineList);
			return saleOrderList;
		}
		return new ArrayList<Map<String, Object>>();
	}
	
	//查询未读网购订单的条数
	@Override
	public int searchUnreadWGDDCount(Map<String, Object> paramsMap)throws Exception{
		LOG.debug("OrderServiceImpl searchUnreadWGDECount paramsMap："+paramsMap);
//		Object orderDate= paramsMap.get("order_date");
		return orderDao.searchUnreadWGDDCount(paramsMap);
	}
}
