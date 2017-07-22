package com.ryx.social.retail.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IMerchConsumerService;
import com.ryx.social.retail.service.IOrderService;
import com.ryx.social.retail.service.IReturnOrderService;
import com.ryx.social.retail.service.ISaleService;
import com.ryx.social.retail.service.IStatisticsService;
import com.ryx.social.retail.service.IWhseService;
import com.ryx.social.retail.util.HttpServletRequestUtil;
import com.ryx.social.retail.util.ParamUtil;

@Controller
public class OrderController {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
	
	@Resource
	private IOrderService orderService;
	@Resource
	private IReturnOrderService returnOrderService;
	@Resource
	private IMerchConsumerService merchConsumerService;
	@Resource
	private ISaleService saleService; // 为了提交销售单, 暂时引用sale的Service
	@Resource
	private IWhseService whseService;
	@Resource
	private IStatisticsService statisticsService; 
	
	/**
	 * 管店宝提交销售单
	 */
	@RequestMapping(value="/retail/sale/submitSaleOrder")
	public void submitSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("operator", pm.get("user_code"));
		paramMap.put("order_type", "03");
		String orderDate = MapUtil.getString(pm, "order_date", DateUtil.getToday());
		paramMap.put("order_date", orderDate); //生成日期
		paramMap.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
		paramMap.put("status", "03"); //订单状态完成
		paramMap.put("pmt_status", "03"); //支付状态已付款
		
		LOG.debug("管店宝提交销售单-数据处理前- OrderController submitSaleOrder pm："+pm);
		LOG.debug("管店宝提交销售单-数据处理后- OrderController submitSaleOrder paramMap："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Map<String, Object> rtmsResult = saleService.submitSaleOrder(paramMap);
			data.put("pay_type", rtmsResult.get("pay_type"));
			data.put("order_id", rtmsResult.get("order_id"));
			data.put("order_date", rtmsResult.get("order_date"));
			data.put("order_time", rtmsResult.get("order_time"));
			data.put("consumer_id", rtmsResult.get("consumer_id"));
			data.put("card_id", rtmsResult.get("card_id"));
			data.put("qty_ord_total", rtmsResult.get("qty_ord_total"));
			data.put("amtys_ord_total", rtmsResult.get("amtys_ord_total"));
			data.put("amt_ord_total", rtmsResult.get("amt_ord_total"));
			data.put("note", rtmsResult.get("note"));
			data.put("operator", rtmsResult.get("operator"));
			data.put("rtms_result", rtmsResult);
			data.put("new_order_id", MapUtil.getString(rtmsResult, "new_order_id"));
			data.put("actual_amtys_ord_total", MapUtil.getBigDecimal(rtmsResult, "actual_amtys_ord_total"));
			data.put("amt_ord_loss", MapUtil.getBigDecimal(rtmsResult, "amt_ord_loss"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 提交销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * PC提交销售单, 刷卡结算
	 */
	@RequestMapping(value="/retail/order/submitPCCardSaleOrder")
	public void submitPCCardSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		String orderDate = MapUtil.getString(pm, "order_date", DateUtil.getToday());
		paramMap.put("order_date", orderDate); //生成日期
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("operator",  pm.get("user_code"));
		paramMap.put("order_type", "01");
		paramMap.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
		paramMap.put("status", "03"); //订单状态完成
		paramMap.put("pmt_status", "03"); //支付状态已付款
		paramMap.put("pay_type", "2"); // 刷卡支付
		LOG.debug("pc提交销售单刷卡结算-数据处理前- OrderController submitPCCardSaleOrder pm："+pm);
		LOG.debug("pc提交销售单刷卡结算-数据处理后- OrderController submitPCCardSaleOrder paramMap："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Map<String, Object> dataMap = saleService.submitSaleOrder(paramMap);
			data.put("pay_type", dataMap.get("pay_type"));
			data.put("order_id", dataMap.get("order_id"));
			data.put("order_date", dataMap.get("order_date"));
			data.put("order_time", dataMap.get("order_time"));
			data.put("consumer_id", dataMap.get("consumer_id"));
			data.put("card_id", dataMap.get("card_id"));
			data.put("qty_ord_total", dataMap.get("qty_ord_total"));
			data.put("amtys_ord_total", dataMap.get("amtys_ord_total"));
			data.put("amt_ord_total", dataMap.get("amt_ord_total"));
			data.put("note", dataMap.get("note"));
			data.put("operator", dataMap.get("operator"));
			data.put("rtms_result", dataMap.get("itms"));
			data.put("act_keys", dataMap.get("list"));
			data.put("new_order_id", MapUtil.getString(dataMap, "new_order_id"));
			data.put("actual_amtys_ord_total", MapUtil.getBigDecimal(dataMap, "actual_amtys_ord_total"));
			data.put("amt_ord_loss", MapUtil.getBigDecimal(dataMap, "amt_ord_loss"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = PC提交销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * PC提交销售单, 挂账
	 */
	@RequestMapping(value="/retail/order/submitPCHangingSaleOrder")
	public void submitPCHangingSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("operator",  pm.get("user_code"));
		paramMap.put("order_type", "01");
		String orderDate = MapUtil.getString(pm, "order_date", DateUtil.getToday());
		paramMap.put("order_date", orderDate); //生成日期
		paramMap.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
		paramMap.put("status", "03"); //订单状态完成
		paramMap.put("pmt_status", "02"); //支付状态挂账
		paramMap.put("pay_type", "1"); // 挂账算作现金支付
		paramMap.put("amt_ord_total", BigDecimal.ZERO); // 挂账的实收金额为0
		paramMap.put("amt_ord_change", BigDecimal.ZERO); // 挂账的找零为0
		
		LOG.debug("pc提交销售单挂账结算-数据处理前- OrderController submitPCHangingSaleOrder pm："+pm);
		LOG.debug("pc提交销售单挂账结算-数据处理后- OrderController submitPCHangingSaleOrder paramMap："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Map<String, Object> dataMap = saleService.submitSaleOrder(paramMap);
			data.put("pay_type", dataMap.get("pay_type"));
			data.put("order_id", dataMap.get("order_id"));
			data.put("order_date", dataMap.get("order_date"));
			data.put("order_time", dataMap.get("order_time"));
			data.put("consumer_id", dataMap.get("consumer_id"));
			data.put("card_id", dataMap.get("card_id"));
			data.put("qty_ord_total", dataMap.get("qty_ord_total"));
			data.put("amtys_ord_total", dataMap.get("amtys_ord_total"));
			data.put("amt_ord_total", dataMap.get("amt_ord_total"));
			data.put("pmt_status", "02"); // 支付状态挂账
			data.put("note", dataMap.get("note"));
			data.put("operator", dataMap.get("operator"));
			data.put("rtms_result", dataMap.get("itms"));
			data.put("act_keys", dataMap.get("list"));
			data.put("new_order_id", MapUtil.getString(dataMap, "new_order_id"));
			data.put("actual_amtys_ord_total", MapUtil.getBigDecimal(dataMap, "actual_amtys_ord_total"));
			data.put("amt_ord_loss", MapUtil.getBigDecimal(dataMap, "amt_ord_loss"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = PC提交销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * 智能终端提交销售单
	 */
	@RequestMapping(value="/retail/order/submitPOSSaleOrder")
	public void submitPOSSaleOrder(HttpServletRequest request, HttpServletResponse response){
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		paramMap.put("operator",  paramMap.get("user_code"));
		paramMap.put("order_type", "02");
		String orderDate = MapUtil.getString(pm, "order_date", DateUtil.getToday());
		paramMap.put("order_date", orderDate); //生成日期
		String orderTime=MapUtil.getString(pm, "order_time", DateUtil.getCurrentTime().substring(8));
		paramMap.put("order_time", orderTime); //生成时间
		paramMap.put("status", "03"); //订单状态完成
		paramMap.put("pmt_status", "03"); //支付状态已付款
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		String payType = MapUtil.getString(pm, "pay_type", "1");
		payType = "1".equals(payType)||"2".equals(payType) ? payType : "1";
		paramMap.put("pay_type", payType);//支付类型(1:现金支付;2:刷卡支付)
		
		LOG.debug("智能终端提交销售单-数据处理前- OrderController submitPOSSaleOrder："+pm);
		LOG.debug("智能终端提交销售单-数据处理后- OrderController submitPOSSaleOrder："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Map<String, Object> dataMap = saleService.submitSaleOrder(paramMap);
			data.put("pay_type", dataMap.get("pay_type"));
			data.put("order_id", dataMap.get("order_id"));
			data.put("order_date", dataMap.get("order_date"));
			data.put("order_time", dataMap.get("order_time"));
			data.put("consumer_id", dataMap.get("consumer_id"));
			data.put("card_id", dataMap.get("card_id"));
			data.put("qty_ord_total", dataMap.get("qty_ord_total"));
			data.put("amtys_ord_total", dataMap.get("amtys_ord_total"));
			data.put("amt_ord_total", dataMap.get("amt_ord_total"));
			data.put("note", dataMap.get("note"));
			data.put("operator", dataMap.get("operator"));
			data.put("rtms_result", dataMap.get("itms"));
			data.put("act_keys", dataMap.get("list"));
			data.put("new_order_id", MapUtil.getString(dataMap, "new_order_id"));
			data.put("actual_amtys_ord_total", MapUtil.getBigDecimal(dataMap, "actual_amtys_ord_total"));
			data.put("amt_ord_loss", MapUtil.getBigDecimal(dataMap, "amt_ord_loss"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = pos提交销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	/**
	 * PC提交销售单, 现金支付
	 */
	@RequestMapping(value="/retail/order/submitPCSaleOrder")
	public void submitPCSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("list", newItemList);
		paramMap.put("operator",  pm.get("user_code"));
		paramMap.put("order_type", "01");
		String orderDate = MapUtil.getString(pm, "order_date", DateUtil.getToday());
		paramMap.put("order_date", orderDate); //生成日期
		String orderTime = MapUtil.getString(pm, "order_time", DateUtil.getCurrentTime().substring(8));
		paramMap.put("order_time", orderTime); //生成时间
		paramMap.put("status", "03"); //订单状态完成
		paramMap.put("pmt_status", "03"); //支付状态已付款
		paramMap.put("pay_type", "1"); // 现金支付
		
		LOG.debug("pc提交销售单现金支付-数据处理前- OrderController submitPCSaleOrder："+pm);
		LOG.debug("pc提交销售单现金支付-数据处理后- OrderController submitPCSaleOrder："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Map<String, Object> dataMap = saleService.submitSaleOrder(paramMap);
			data.put("pay_type", dataMap.get("pay_type"));
			data.put("order_id", dataMap.get("order_id"));
			data.put("order_date", dataMap.get("order_date"));
			data.put("order_time", dataMap.get("order_time"));
			data.put("consumer_id", dataMap.get("consumer_id"));
			data.put("card_id", dataMap.get("card_id"));
			data.put("qty_ord_total", dataMap.get("qty_ord_total"));
			data.put("amtys_ord_total", dataMap.get("amtys_ord_total"));
			data.put("amt_ord_total", dataMap.get("amt_ord_total"));
			data.put("note", dataMap.get("note"));
			data.put("operator", dataMap.get("operator"));
			data.put("rtms_result", dataMap.get("itms"));
			data.put("act_keys", dataMap.get("list"));
			data.put("new_order_id", MapUtil.getString(dataMap, "new_order_id"));
			data.put("actual_amtys_ord_total", MapUtil.getBigDecimal(dataMap, "actual_amtys_ord_total"));
			data.put("amt_ord_loss", MapUtil.getBigDecimal(dataMap, "amt_ord_loss"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = PC提交销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 可能有问题
	 * PC批量提交销售单, 现金支付
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	@RequestMapping(value="/retail/order/batchSubmitPCSaleOrder")
	public void batchSubmitPCSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<String> errorSaleOrderList = new ArrayList<String>();//记录提交失败的销售单号
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String,Object>>  paramList =  (List<Map<String, Object>>) paramMap.get("saleList");
		int temp = 2;
		int size = paramList.size();//总记录数
		int count = size/temp;//次数
		int tail = size%temp;//剩余数据的遍历次数
		/*整数循环*/
		for(int i=0;i<count;i+=temp){
			List<Map<String,Object>> paramSonList = paramList.subList(i, i+temp);//获取某一区间的数据
			try {
				saleService.submitBatchSaleOrder(paramMap, paramSonList);
			} catch (Exception e) {
				/*如果批次提交失败,则改成单次提交*/
				for (Map<String, Object> map : paramSonList) {
					map.put("operator",  paramMap.get("user_code"));
					map.put("order_type", "01");
					String orderDate = MapUtil.getString(paramMap, "order_date", DateUtil.getToday());
					map.put("order_date", orderDate); //生成日期
					map.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
					map.put("status", "03"); //订单状态完成
					map.put("pmt_status", "03"); //支付状态已付款
					map.put("pay_type", "1"); // 现金支付
					map.put("ip", HttpServletRequestUtil.getIpAddr(request));
					try {
						saleService.submitSaleOrder(map);//单次提交
					} catch (Exception e1) {
						errorSaleOrderList.add(MapUtil.getString(map, "order_id"));//将提交失败的销售单号保存
						code = Constants.FAIL;
						msg = Constants.FAIL_MSG;
						LOG.error(" = * = * = * = * = PC批量提交销售单错误 = * = * = * = * = ", e);
					}
				}
			}
		}
		/*余数循环*/
		for(int j=0;j<tail;j++){
			List<Map<String,Object>> paramSonList = paramList.subList(count*temp,size);//获取最后一组数据
			try {
				saleService.submitBatchSaleOrder(paramMap, paramSonList);
			} catch (Exception e) {
				/*如果批次提交失败,则改成单次提交*/
				for (Map<String, Object> map : paramSonList) {
					map.put("operator",  paramMap.get("user_code"));
					map.put("order_type", "01");
					
					String orderDate = MapUtil.getString(paramMap, "order_date", DateUtil.getToday());
					map.put("order_date", orderDate); //生成日期
					map.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
					map.put("status", "03"); //订单状态完成
					map.put("pmt_status", "03"); //支付状态已付款
					map.put("pay_type", "1"); // 现金支付
					map.put("ip", HttpServletRequestUtil.getIpAddr(request));
					try {
						saleService.submitSaleOrder(map);//单次提交
					} catch (Exception e1) {
						errorSaleOrderList.add(MapUtil.getString(map, "order_id"));//将提交失败的销售单号保存
						code = Constants.FAIL;
						msg = Constants.FAIL_MSG;
						LOG.error(" = * = * = * = * = PC批量提交销售单错误 = * = * = * = * = ", e);
					}
				}
			}
		}
		ResponseUtil.write(request, response, code, msg, errorSaleOrderList);
	}
	
	/**
	 * PC提交销售单, 信用卡支付, 但是不是用咱们的pos
	 */
	@RequestMapping(value="/retail/order/submitPCSaleOrderWithoutPOS")
	public void submitPCSaleOrderWithoutPOS(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("operator",  pm.get("user_code"));
		paramMap.put("order_type", "01");
		String orderDate = MapUtil.getString(pm, "order_date", DateUtil.getToday());
		paramMap.put("order_date", orderDate); //生成日期
		paramMap.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
		paramMap.put("status", "03"); //订单状态完成
		paramMap.put("pmt_status", "03"); //支付状态已收款
		paramMap.put("pay_type", "2"); //刷卡消费
		
		LOG.debug("pc提交销售单信用卡支付-数据处理前- OrderController submitPCSaleOrderWithoutPOS："+pm);
		LOG.debug("pc提交销售单信用卡支付-数据处理后- OrderController submitPCSaleOrderWithoutPOS："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Map<String, Object> dataMap = saleService.submitSaleOrder(paramMap);
			data.put("pay_type", dataMap.get("pay_type"));
			data.put("order_id", dataMap.get("order_id"));
			data.put("order_date", dataMap.get("order_date"));
			data.put("order_time", dataMap.get("order_time"));
			data.put("consumer_id", dataMap.get("consumer_id"));
			data.put("card_id", dataMap.get("card_id"));
			data.put("qty_ord_total", dataMap.get("qty_ord_total"));
			data.put("amtys_ord_total", dataMap.get("amtys_ord_total"));
			data.put("amt_ord_total", dataMap.get("amt_ord_total"));
			data.put("note", dataMap.get("note"));
			data.put("operator", dataMap.get("operator"));
			data.put("rtms_result", dataMap.get("itms"));
			data.put("act_keys", dataMap.get("list"));
			data.put("new_order_id", MapUtil.getString(dataMap, "new_order_id"));
			data.put("actual_amtys_ord_total", MapUtil.getBigDecimal(dataMap, "actual_amtys_ord_total"));
			data.put("amt_ord_loss", MapUtil.getBigDecimal(dataMap, "amt_ord_loss"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = PC提交信用卡支付的销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * PC提交销售单, 信用卡支付
	 */
	@RequestMapping(value="/retail/order/submitPCSaleOrderWithCard")
	public void submitSaleOrderWithCard(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("operator", pm.get("user_code"));
		paramMap.put("order_type", "01");
		String orderDate = MapUtil.getString(pm, "order_date", DateUtil.getToday());
		paramMap.put("order_date", orderDate); //生成日期
		paramMap.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
		paramMap.put("status", "01"); //订单状态下单
		paramMap.put("pmt_status", "01"); //支付状态未收款
		paramMap.put("pay_type", "2"); //刷卡消费
		
		LOG.debug("pc提交销售单信用卡支付-数据处理前- OrderController submitSaleOrderWithCard："+pm);
		LOG.debug("pc提交销售单信用卡支付-数据处理后- OrderController submitSaleOrderWithCard："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Map<String, Object> dataMap = saleService.submitSaleOrder(paramMap);
//			whseService.modifyWhseMerch(paramMap);-------------------------------------
			data.put("pay_type", dataMap.get("pay_type"));
			data.put("order_id", dataMap.get("order_id"));
			data.put("order_date", dataMap.get("order_date"));
			data.put("order_time", dataMap.get("order_time"));
			data.put("consumer_id", dataMap.get("consumer_id"));
			data.put("card_id", dataMap.get("card_id"));
			data.put("qty_ord_total", dataMap.get("qty_ord_total"));
			data.put("amtys_ord_total", dataMap.get("amtys_ord_total"));
			data.put("amt_ord_total", dataMap.get("amt_ord_total"));
			data.put("note", dataMap.get("note"));
			data.put("operator", dataMap.get("operator"));
			data.put("rtms_result", dataMap.get("itms"));
			data.put("act_keys", dataMap.get("list"));
			data.put("new_order_id", MapUtil.getString(dataMap, "new_order_id"));
			data.put("actual_amtys_ord_total", MapUtil.getBigDecimal(dataMap, "actual_amtys_ord_total"));
			data.put("amt_ord_loss", MapUtil.getBigDecimal(dataMap, "amt_ord_loss"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = PC提交信用卡支付的销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * PC从缓存提交销售单, 现金/刷卡统一
	 */
	@RequestMapping(value="/retail/order/submitPCSaleOrderWithCache")
	public void submitPCSaleOrderWithCache(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "order_id", "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		paramMap.put("input_received_amount", MapUtil.getBigDecimal(pm, "input_received_amount", MapUtil.getBigDecimal(paramMap, "total_amount")));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("operator", pm.get("user_code"));
		paramMap.put("order_type", "01");
		paramMap.put("order_date", pm.get("order_date")); // 接收日期
		paramMap.put("order_time", pm.get("order_time")); // 接收时间
		paramMap.put("status", pm.get("status")); // 订单状态下单
		paramMap.put("pmt_status", pm.get("pmt_status")); //支付状态未收款
		paramMap.put("pay_type", pm.get("pay_type")); // 现金支付
		
		LOG.debug("pc提交销售单缓存提交-数据处理前- OrderController submitPCSaleOrderWithCache："+pm);
		LOG.debug("pc提交销售单缓存提交-数据处理后- OrderController submitPCSaleOrderWithCache："+paramMap);
		String data = "1";
		try {
			Map<String, Object> saleOrder = saleService.submitSaleOrder(paramMap);
			
			Map<String, Object> orderParam = new HashMap<String, Object>();
			orderParam.put("merch_id", saleOrder.get("merch_id"));
			orderParam.put("order_id", saleOrder.get("order_id"));
			orderParam.put("new_order_id", MapUtil.getString(saleOrder, "new_order_id"));
			List<Map<String, Object>> orderList = saleService.selectSaleOrder(orderParam);
			if(orderList.isEmpty()){
				data = "0"; // 如果为空则返回0
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = PC从缓存提交销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 销售单废弃, 只修改订单状态status
	 */
	@RequestMapping(value="/retail/order/removeSaleOrder")
	public void removeSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		
		paramMap.put("status", "00"); //订单状态废弃
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		try {
			saleService.updateSaleOrder(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 网购订单销售单废弃错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 销售单发货, 只修改订单状态status
	 * 暂时修改成完成, 并且支付状态为已支付
	 */
	@RequestMapping(value="/retail/order/deliverSaleOrder")
	public void deliverSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		
		paramMap.put("status", "03"); // 订单状态已发货
		paramMap.put("pmt_status", "03"); // 订单状态已发货
		paramMap.put("pay_type", "1"); // 支付类型。。现金
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		
		try {
			saleService.updateSaleOrder(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 网购订单销售单发货错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 销售单刷卡支付转现金支付
	 */
	@RequestMapping(value="/retail/order/cashPaySaleOrder")
	public void modifySaleOrderTransformPayType(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		
		paramMap.put("pay_type", "1"); // 改成现金支付
		paramMap.put("status", "03"); // 销售单状态: 完成
		paramMap.put("pmt_status", "03"); // 支付状态: 完成
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		
		try {
			saleService.updateSaleOrder(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 销售单刷卡支付转现金支付错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}

	/**
	 * 销售单刷卡支付完成
	 */
	@RequestMapping(value="/retail/order/completeCardPay")
	public void modifySaleOrderCompleteCardPay(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		
		paramMap.put("pay_type", "2"); // 刷卡支付
		paramMap.put("status", "03"); // 销售单状态: 完成
		paramMap.put("pmt_status", "03"); // 支付状态: 完成
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		
		try {
			saleService.updateSaleOrder(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * =刷卡更改支付状态失败= * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//销售流水-查询列表，查询销售单，及销售单的退货信息
	@RequestMapping(value="/retail/order/searchSaleOrder")
	public void searchSaleOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = null;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(requestMap.containsKey("params")) {
			paramMap = MapUtil.get(requestMap, "params", new HashMap<String, Object>());
		} else {
			paramMap = requestMap;
		}
		if(!paramMap.containsKey("page_index")) {
			paramMap.put("page_index", 1);
		}
		if(!paramMap.containsKey("page_size")) {
			paramMap.put("page_size", 20);
		}
		paramMap.put("merch_id", userMap.get("ref_id"));
		paramMap.put("role_id", MapUtil.getString(userMap, "role_id"));
		paramMap.put("role_name", MapUtil.getString(userMap, "role_name"));
		paramMap.put("user_code", MapUtil.getString(userMap, "user_code"));
		List<Map<String, Object>> data = null;
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = orderService.searchConsumerJoinSaleReturnOrder(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("page_count", paramMap.get("page_count")==null?0:paramMap.get("page_count"));
			pageParam.put("count", paramMap.get("count")==null?0:paramMap.get("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	
	@RequestMapping(value="/retail/order/searchSaleOrderDetail")
	public void searchSaleOrderJoinSaleOrderLine(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = null;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(requestMap.containsKey("params")) {
			paramMap = MapUtil.get(requestMap, "params", new HashMap<String, Object>());
		} else {
			paramMap = requestMap;
		}
		if(!paramMap.containsKey("page_index")) {
			paramMap.put("page_index", 1);
		}
		if(!paramMap.containsKey("page_size")) {
			paramMap.put("page_size", 20);
		}
		paramMap.put("merch_id", userMap.get("ref_id"));
		Map<String, Object> data = null;
		try {
			data = orderService.searchSaleOrderJoinSaleOrderLineJoinReturnQty(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询销售单明细错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	@RequestMapping(value="/retail/order/searchSaleOrderDetailJoinConsumer")
	public void searchSaleOrderConsumerDetail(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			data = orderService.searchSaleOrderJoinSaleOrderLine(paramMap);
			if(data.get("consumer_id")!=null && !"".equals(data.get("consumer_id"))) {
				paramMap.put("consumer_id", data.get("consumer_id"));
				paramMap.put("status", "1");
				List<Map<String, Object>> merchConsumerList = merchConsumerService.searchMerchConsumer(paramMap);
				if(merchConsumerList.size()>0) {
					Map<String, Object> merchConsumerMap = merchConsumerList.get(0);
					merchConsumerMap.putAll(data);
					data = merchConsumerMap;
				}
			}
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询销售单明细错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	@RequestMapping(value="/retail/order/searchPurchOrder")
	public void searchPurchOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = orderService.searchPurchOrder(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询采购单明细错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	/**
	 * 提交退货单
	 */
	@RequestMapping(value="/retail/order/submitReturnOrder")
	public void submitReturnOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Object returnStatic=paramMap.get("status");
		if(returnStatic==null||!returnStatic.equals("03")){
			code="1000";
			msg="销售单未完成，不可退货";
			ResponseUtil.write(request, response, code, msg, null);
			return;
		}
		
//		String returnId =  MapUtil.getString(paramMap, "return_order_id", IDUtil.getId());
		String returnDate = MapUtil.getString(paramMap, "return_order_date", DateUtil.getToday());
		String returnTime = MapUtil.getString(paramMap, "return_order_time", DateUtil.getCurrentTime().substring(8));
		
		paramMap.put("return_pmt_status", "03");
		paramMap.put("status", "03");
		
//		paramMap.put("return_order_id",returnId);//id
		paramMap.put("return_order_date", returnDate); //退货日期
		paramMap.put("return_order_time", returnTime);//退货时间
		paramMap.put("operator", paramMap.get("user_code"));//操作员
		paramMap.put("merch_id", MapUtil.getString(paramMap, "ref_id"));
		Map<String, Object> data = null;
		try {
			data = returnOrderService.submitReturnOrder(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("生成退货单失败: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/retail/order/searchReturnOrder")
	public void searchReturnOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> pageParam = new HashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = orderService.searchReturnOrder(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("count", paramMap.get("count"));
			pageParam.put("page_count", paramMap.get("page_count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询退货单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	
	@RequestMapping(value="/retail/order/searchReturnOrderDetail")
	public void searchReturnOrderDetail(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> pageParam = new HashMap<String, Object>();
		Map<String, Object> data = null;
		try {
			data = orderService.searchReturnOrderJoinSaleOrderLine(paramMap);
			pageParam.put("page_index", paramMap.get("page_index"));
			pageParam.put("page_size", paramMap.get("page_size"));
			pageParam.put("count", paramMap.get("count"));
			pageParam.put("page_count", paramMap.get("page_count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询退货单明细错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	
	@RequestMapping(value="/retail/order/searchSaleReturnOrder")
	public void searchSaleOrderJoinSaleOrderLineWithReturnOrder(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = orderService.searchSaleReturnOrderJoinSaleReturnOrderLine(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询销售退货明细错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}

	/**
	 * 管店宝 - 查询网购订单
	 */
	@RequestMapping(value="/retail/order/searchOnlineSaleOrder")
	public void getSaleOrderByOnline(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("order_type", "04"); // 销售单类型, 楼下店
		paramMap.put("status", "01"); // 计划,待发货
		if(paramMap.get("page_index")==null) {
			paramMap.put("page_index", 1);
		}
		if(paramMap.get("page_size")==null) {
			paramMap.put("page_size", 20);
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = orderService.searchWgddOrder(paramMap);
			pageParam.put("page_index", paramMap.remove("page_index"));
			pageParam.put("page_size", paramMap.remove("page_size"));
			pageParam.put("page_count", paramMap.remove("page_count"));
			pageParam.put("count", paramMap.remove("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 获取网购订单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	/**
	 * 打印销售报表
	 * @author 徐虎彬
	 * @date 2014年4月22日
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/retail/order/printSaleOrderLine")
	public void printSaleOrderLine(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		paramMap.put("role_id", MapUtil.getString(userMap, "role_id"));
		paramMap.put("role_name", MapUtil.getString(userMap, "role_name"));
		paramMap.put("user_code", MapUtil.getString(userMap, "user_code"));
		Map<String, Object> data = new HashMap<String, Object>();
//		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = statisticsService.getSaleDailySettlement(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 获取销售报表错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value="/retail/order/searchUnreadWGDDCount")
	public void searchUnreadWGDDCount(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		int data=0;
		Map<String, Object> paramsMap=new HashMap<String, Object>();
//		paramsMap.putAll(getParamMap(request));
		Map<String, Object> userMap=IdentityUtil.getUserMap(request);
		paramsMap.put("merch_id", userMap.get("ref_id"));
		try {
			data=orderService.searchUnreadWGDDCount(paramsMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("获得未读网购订单条数失败",e);
		}
		ResponseUtil.write(request, response, code,msg, data);
	}
	
	//pos获得单号的13位时间
	@RequestMapping(value="/retail/order/getIdSpan13")
	public void getIdSpan13(HttpServletRequest request, HttpServletResponse response) {
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Object orderId = null;
		try {
			orderId = new Date().getTime();
			LOG.debug("OrderController getIdSpan13 orderId："+orderId);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("获得 单号失败",e);
		}
		ResponseUtil.write(request, response, code,msg, orderId);
	}
}
