package com.ryx.social.consumer.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.ryx.social.consumer.service.IConsumerSaleService;
import com.ryx.social.retail.service.IMerchService;
import com.ryx.social.retail.service.ISaleService;
import com.ryx.social.retail.util.HttpServletRequestUtil;
import com.ryx.social.retail.util.ParamUtil;

@Controller
public class ConsumerSaleController {
	
	private static final Logger LOG=LoggerFactory.getLogger(ConsumerSaleController.class);
	
	@Resource
	private IConsumerSaleService consumerSaleService;	
	@Resource
	private ISaleService saleService; // 为了提交销售单, 暂时引用sale的Service
	@Resource
	private IMerchService merchService; 

	private Map<String, Object> getParamMap(HttpServletRequest request) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		if(userMap!=null) {
			paramMap.putAll(userMap);
			paramMap.put("merch_id", paramMap.get("ref_id"));
		}
		if(requestMap.get("params")!=null) { // 如果前台传merch_id会覆盖session中的merch_id
			paramMap.putAll((Map<String, Object>) requestMap.get("params"));
		} else {
			paramMap.putAll(requestMap);
		}
		return paramMap;
	}
	
	
	/**
	 * 获取商户促销信息
	 */
	@RequestMapping(value="/consumer/promotion/searchMerchPromotion")
	public void searchMerchPromotion(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> requestMap = RequestUtil.getParameterMap(request);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		if(requestMap.get("params")!=null) {
			paramMap = (Map<String, Object>) requestMap.get("params");
		}
		if(paramMap.get("page_index")==null) {
			paramMap.put("page_index", 1);
		}
		if(paramMap.get("page_size")==null) {
			paramMap.put("page_size", 20);
		}
		String projectPath = request.getSession().getServletContext().getRealPath("/");
		paramMap.put("project_path", projectPath);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> pageParam = new HashMap<String, Object>();
		try {
			data = consumerSaleService.searchMerchPromotion(paramMap);
			pageParam.put("page_index", paramMap.remove("page_index"));
			pageParam.put("page_size", paramMap.remove("page_size"));
			pageParam.put("page_count", paramMap.remove("page_count"));
			pageParam.put("count", paramMap.remove("count"));
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询商户促销信息错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, pageParam);
	}
	
	/**
	 * 楼下店提交销售单 
	 */
	@RequestMapping(value="/consumer/sale/submitConsumerSaleOrder")
	public void submitConsumerSaleOrder(HttpServletRequest request,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		
		Map<String, Object> pm = ParamUtil.getParamMap(request);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		
		Map<String, Object> paramMap = MapUtil.rename(pm, "amt_ord_total.received_amount", "received_amount", "amtys_ord_total.total_amount", "total_amount", "adjusted_amount.total_rebate_amount", "total_rebate_amount", "consumer_rebate_amount", "consumer_id", "consumer_point", "promotion_ids", "amt_ord_change.change_amount", "change_amount", "merch_id");
		paramMap.putAll(MapUtil.rename(userMap, "ref_id.merch_id", "user_code"));
		
		List<Map<String, Object>> itemList = MapUtil.get(pm, "list", new ArrayList<Map<String, Object>>());
		
		List<Map<String, Object>> newItemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		for (Map<String, Object> map : itemList) {
			item = new HashMap<String, Object>();
			item = MapUtil.rename(map, "item_id", "item_bar", "cost", "item_kind_id", "line_label.line_num", "line_num", "item_name", "promotion_ids", "unit_name.item_unit_name", "item_unit_name", "pri4", "big_bar", "big_unit_name", "big_pri4", "unit_ratio", "seq_id.pack_id", "pack_id", "qty_ord.sale_quantity", "sale_quantity", "amt_ord.sale_amount", "sale_amount", "adjusted_amount.line_rebate_amount", "line_rebate_amount");
			newItemList.add(item);
		}
		paramMap.put("list", newItemList);
		
		
		BigDecimal amt_ord_total = MapUtil.getBigDecimal(paramMap, "amt_ord_total");
		BigDecimal amtys_ord_total = MapUtil.getBigDecimal(paramMap, "amtys_ord_total");
		if(amtys_ord_total.compareTo(BigDecimal.ZERO) == 0){
			paramMap.put("amtys_ord_total", amt_ord_total);
		}
		
		paramMap.put("ip", HttpServletRequestUtil.getIpAddr(request));
		paramMap.put("operator", "LXD");
		paramMap.put("order_type", "04");
		paramMap.put("order_id", IDUtil.getId()); //生成id
		paramMap.put("order_date", DateUtil.getToday()); //生成日期
		paramMap.put("order_time", DateUtil.getCurrentTime().substring(8)); //生成时间
		paramMap.put("status", "01"); //订单状态完成
		paramMap.put("pmt_status", "01"); //支付状态已付款
		
		LOG.debug("《楼下店提交销售单-数据处理前》 ConsumerSaleController submitConsumerSaleOrder："+pm);
		LOG.debug("《楼下店提交销售单-数据处理后》 ConsumerSaleController submitConsumerSaleOrder："+paramMap);
		Map<String, Object> data = new HashMap<String, Object>();
		
		try {
			Map<String, Object> saleData = saleService.submitSaleOrder(paramMap);
			data.put("order_id", saleData.get("order_id"));
			data.put("order_date", saleData.get("order_date"));
			data.put("order_time", saleData.get("order_time"));
			data.put("consumer_id", saleData.get("consumer_id")); // 这里是手机号
			data.put("qty_ord_total", saleData.get("qty_ord_total"));
			data.put("amt_ord_total", saleData.get("amt_ord_total")); // 应收和实收是一致的
			data.put("status", saleData.get("status"));
			data.put("pmt_status", saleData.get("pmt_status"));
			data.put("note", saleData.get("note"));
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 楼下店提交销售单错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	//查询销售单
	@RequestMapping(value="/consumer/sale/searchConsumerSaleOrder")
	public void getConsumerSaleOrder(HttpServletRequest request ,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		List<Map<String, Object>> data =null;
		Map<String, Object> paramsMap = JsonUtil.json2Map(request.getParameter("params"));
		List<Map<String, Object>> merchList=new ArrayList<Map<String,Object>>();
		try {
			paramsMap.put("order_type", "04");
			data=saleService.selectSaleOrder(paramsMap);
			Set<String> merchSet=new HashSet<String>();
			for (Map<String, Object> orderInfo : data) {
				String merchId=(String) orderInfo.get("merch_id");
				merchSet.add(merchId);
			}
			StringBuffer merchIdStr=new StringBuffer();
			for (Object obj : merchSet) {
				merchIdStr.append(obj+",");
			}
			Map<String, Object> merchParamsMap=new HashMap<String, Object>();
			merchParamsMap.put("merch_id", merchIdStr.toString());
			
			merchList=merchService.selectMerch(merchParamsMap);
			for (int i = 0; i < data.size(); i++) {
				for (Map<String, Object> map : merchList) {
					if(map.get("is_social")!=null){
						map.remove("is_social");
					}
					if(map.get("is_ord_cgt")!=null){
						map.remove("is_ord_cgt");
					}
					if(map.get("lice_id")!=null){
						map.remove("lice_id");
					}
					if(map.get("status")!=null){
						map.remove("status");
					}
					if(map.get("is_init")!=null){
						map.remove("is_init");
					}
					if(map.get("cgt_com_id")!=null){
						map.remove("cgt_com_id");
					}
					if(data.get(i).get("merch_id").equals(map.get("merch_id"))){
						data.get(i).put("merch_info",map );
					}
				}
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("查询网购订单失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
	//查询销售单行
	@RequestMapping(value="/consumer/sale/searchConsumerSaleOrderDetail")
	public void getConsumerSaleOrderLine(HttpServletRequest request ,HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> data =new HashMap<String, Object>();
		Map<String, Object> paramsMap=JsonUtil.json2Map(request.getParameter("params"));
		try {
			data=saleService.getSaleOrderDetail(paramsMap);
			List<Map<String, Object>> saleLineList=(List<Map<String, Object>>) data.get("list");
			if(data.get("amtys_ord_total")!=null){
				data.remove("amtys_ord_total");
			}
			if(data.get("order_type")!=null){
				data.remove("order_type");
			}
			for (Map<String, Object> saleLine : saleLineList) {
				if(saleLine.get("profit")!=null){
					saleLine.remove("profit");
				}
				if(saleLine.get("cost")!=null){
					saleLine.remove("cost");
				}
				if(saleLine.get("discount")!=null){
					saleLine.remove("discount");
				}
			}
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("查询网购订单单行失败",e);
			// TODO: handle exception
		}
		ResponseUtil.write(request, response, code,msg,data);
	}
}
