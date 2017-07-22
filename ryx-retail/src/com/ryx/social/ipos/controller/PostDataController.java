package com.ryx.social.ipos.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.SpellUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IItemService;
import com.ryx.social.retail.service.IMerchService;
import com.ryx.social.retail.service.ISaleService;
import com.ryx.social.retail.util.HttpServletRequestUtil;

@Controller 
public class PostDataController {

	private static final Logger logger = LoggerFactory.getLogger(PostDataController.class);
	
	@Resource
	private ISaleService saleService;
	@Resource
	private IMerchService merchService;
	@Resource
	private IItemService itemService;
	
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
	
	@RequestMapping(value = "/ipos/postdata/saleorder")
	public void saleOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Object returnData = null;
		
		String params = request.getParameter("params");
//		String params = "{'ticket_id':'21059620140523122232','tm':'20140523144628','moling':'0','yf':'18.0','pay_type':'现金','zhaoling':'0.0','sf':'18.0','list':[{'good_name':'黄鹤楼(软蓝)','good_in_price':'15.5','good_type':'001','good_unit':'盒','good_sale_amount':'1','good_sale_price':'18.0','good_code':'6901028180573','good_id':'6901028180580','good_sale_xiaoji':'18.0'}],'zk':'100','card_id':'','cust_id':'370102210596'}";
//		String params = "{'ticket_id':'20140414093607','tm':'20140414093730','moling':'0','yf':'10.0','pay_type':'现金','zhaoling':'0.0','sf':'10.0','list':[{'good_name':'可口可乐123','good_in_price':'8.3','good_type':'001','good_unit':'盒','good_sale_amount':'1','good_sale_price':'10.0','good_code':'186754567656','good_id':'234564325','good_sale_xiaoji':'10.0'}],'zk':'100','card_id':'','cust_id':'370102107633'}";
		if(StringUtils.hasText(params)) {
			logger.debug("提交销售单：" + params);
			
			Map<String,Object> paramMap = JsonUtil.json2Map(params);
			List<Map> paramList = (List<Map>)paramMap.get("list");
			
			String custId = (String)paramMap.get("cust_id");
			Map userInfo = merchService.getUserInfo(custId);
			// 需要从base_merch表里取com_id用来获取卷烟商品的supplier_id
			Map<String, Object> merchParam = MapUtil.rename(paramMap, "cust_id.lice_id");
			List<Map<String, Object>> merchList = merchService.selectMerch(merchParam);
			if(userInfo != null && "10".equals(userInfo.get("USER_TYPE")) && !merchList.isEmpty()) {
				Map<String, Object> merchMap = merchList.get(0);
				Map<String,Object> toParam = new HashMap<String, Object>();
				toParam.put("merch_id", userInfo.get("REF_ID"));
				/* 梁凯 2014年6月3日15:19:21
				 * 销售单操作者改为登陆信息中的user_code, 原来固定为POS
				 */
				toParam.put("operator", MapUtil.getString(userInfo, "USER_CODE", "POS"));
				/* 梁凯 2014年5月22日 
				 * 如果ticket_id以2014或2012开头则认为是旧销售单,加上merch_id的后6位防止重复
				 */
				String ticketId = (String) paramMap.get("ticket_id");
				String refId = (String) userInfo.get("REF_ID");
				if(ticketId!=null && refId!=null && (ticketId.startsWith("2014")||ticketId.startsWith("2012"))) ticketId += refId.substring(refId.length()-6);
				toParam.put("order_id", ticketId);
				List<Map<String, Object>> order = saleService.selectSaleOrder(toParam);
				if(order == null || order.size() == 0) {
					
					List<Map> toList = new ArrayList<Map>();
					
					Map tmpMap = null;
					int i = 0, j = 0;
					/* 梁凯 2014年5月22日 
					 * 如果good_code为null或者为空字符串,则使用good_id
					 */
					for (Map map : paramList) {
						i++;
						tmpMap = new HashMap();
						String goodCode = (String) map.get("good_code");
						Object goodId = map.get("good_id");
						if(goodCode==null || "".equals(goodCode.trim())) {
							tmpMap.put("item_bar", goodId);
							tmpMap.put("big_bar", goodId);
						} else {
							tmpMap.put("item_bar", goodCode);
							tmpMap.put("big_bar", goodCode);
						}
						tmpMap.put("item_id", goodId);
						tmpMap.put("item_unit_name", map.get("good_unit"));
						tmpMap.put("big_unit_name", map.get("good_unit"));
						tmpMap.put("unit_ratio", "1");
						tmpMap.put("pri4", map.get("good_sale_price"));
						tmpMap.put("big_pri4", map.get("good_sale_price"));
						tmpMap.put("item_name", map.get("good_name"));
						String type = (String)map.get("good_type");
						if(!StringUtils.hasText(type)) type = "09";
						else if("001".equals(type)) type = "01";
						tmpMap.put("item_kind_id", type);
						tmpMap.put("unit_name", map.get("good_unit"));
						tmpMap.put("cost", map.get("good_in_price"));
						tmpMap.put("pri1", map.get("good_in_price"));
						tmpMap.put("pri2", map.get("good_in_price"));
						tmpMap.put("discount", "100");
						tmpMap.put("line_num", "" + i);
						tmpMap.put("line_label", "" + i);
						String amount = (String) map.get("good_sale_amount");
						BigDecimal bd = new BigDecimal(amount);
						j += bd.intValue();
						tmpMap.put("sale_quantity", map.get("good_sale_amount"));
						tmpMap.put("qty_ord", map.get("good_sale_amount"));
						tmpMap.put("sale_amount", map.get("good_sale_xiaoji"));
						tmpMap.put("amt_ord", map.get("good_sale_xiaoji"));
						tmpMap.put("pri", map.get("good_sale_price"));
						
						// POS提交的销售，默认转换比为1，即销售的始终是最小单位
						tmpMap.put("qty_sub", map.get("good_sale_amount"));
						
						toList.add(tmpMap);
					}
					
					toParam.put("consumer_id", paramMap.get("card_id"));
					toParam.put("qty_ord_total", "" + j);
					toParam.put("qty_ord_count", "" + i);
					toParam.put("received_amount", paramMap.get("sf"));
					toParam.put("total_amount", paramMap.get("yf"));
					toParam.put("order_type", "02");
					toParam.put("change_amount", paramMap.get("zhaoling"));
					toParam.put("list", toList);
					
					String orderDate = (String) paramMap.get("tm");
					String orderTime = null;
					if(StringUtils.hasText(orderDate)) {
						if(orderDate.length() > 8) {
							orderTime = orderDate.substring(8, Math.min(orderDate.length(), 14));
						}
						orderDate = orderDate.substring(0, 8);
					} else {
						orderDate = DateUtil.getToday();
						orderTime = DateUtil.getCurrentTime().substring(8, 6);
					}
					toParam.put("order_date", orderDate); //生成日期
					toParam.put("order_time", orderTime); //生成时间
					toParam.put("status", "03"); //订单状态完成
					toParam.put("pmt_status", "03"); //支付状态已付款
					String pmt = (String) paramMap.get("pay_type");
					if("现金".equals(pmt)) pmt = "1";
					else if("刷卡".equals(pmt)) pmt = "2";
					else pmt = "0";
					toParam.put("pay_type", pmt);
					toParam.put("com_id", merchMap.get("cgt_com_id"));
					toParam.put("ip", HttpServletRequestUtil.getIpAddr(request));
					logger.debug("智能终端1.0提交销售单- PostDataController saleOrder："+toParam);
					
					try {
						itemService.initData(toParam);
						saleService.submitSaleOrder(toParam);
						returnData = "提交成功";
					} catch (Exception e) {
						code = Constants.FAIL;
						msg = "提交销售单错误：" + e.getMessage();
						logger.error(" = * = * = * = * = POS 提交销售单错误 = * = * = * = * = ", e);
					}
				} else {
					code = Constants.CODE_SUCCESS;
					msg = Constants.SUCCESS_MSG;
					returnData = "销售单已存在";
				}
			} else {
				code = Constants.FAIL;
				msg = "该用户尚未开通服务";
				logger.debug("该用户尚未开通服务，userCode=" + custId);
			}
		} else {
			code = Constants.FAIL;
			msg = "无params参数";
		}
		
		ResponseUtil.write(request, response, code, msg, returnData);
	}
	/**
	 * 向pos推荐商品
	 */
	@RequestMapping(value="/ipos/postdata/recommend2POS")
	public void recommend2POS(HttpServletRequest request,HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Map<String, Object> paramMap = getParamMap(request);
		paramMap.put("recommend_to", "02");
		paramMap.put("status", "1");
		List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
		paramMap.put("page_index", -1);
		paramMap.put("page_size", -1);
		try {
			data = itemService.searchRecommendedPOSItem(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 向pos推荐商品错误 = * = * = * = * = ",e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	/**
	 * pos批量修改商品基础信息
	 * @author 徐虎彬
	 * @date 2014年4月22日
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/ipos/postdata/posOperateMerchItem")
	public void posOperateMerchItem(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		
		Object returnData = null;
		
		String paramsList = request.getParameter("params");
//		if(paramsList.indexOf("{")==1){
//			paramsList=paramsList.substring(1);
//			paramsList=paramsList.substring(0,paramsList.length()-1);
//		}
		if(StringUtils.hasText(paramsList)) {
			logger.debug("批量修改商品信息：" + paramsList);
			
			Map<String,Object> paramMapList = JsonUtil.json2Map(paramsList);
			
			List<Map<String,Object>> paramMapList2 = (List<Map<String,Object>>)paramMapList.get("list");
			for(Map<String,Object> paramMap:paramMapList2){
				
			
			if(paramsList!=null && !"".equals(paramsList)) {
				paramMap.remove("status");
				if(paramMap.containsKey("item_name")) {
					paramMap.put("short_code", SpellUtil.getFullSpell((String)paramMap.get("item_name")));
				}
				if(paramMap.containsKey("item_name")) {
					paramMap.put("short_name", SpellUtil.getShortSpell((String)paramMap.get("item_name")));
				}
				String type = (String) paramMap.get("type");
				if("C".equals(type)) {
					paramMap.put("item_id", IDUtil.getId());
					paramMap.put("create_date", DateUtil.getToday());
					paramMap.put("create_time", DateUtil.getCurrentTime().substring(8));
				}
			} else {
				code = Constants.FAIL;
				msg = "params 参数为空";
				ResponseUtil.write(request, response, code, msg, null);
				return;
			}
			if(!paramMap.containsKey("discount")) {
				paramMap.put("discount", 100);
			}
			try {
				itemService.operateMerchItem(paramMap);
			} catch (Exception e) {
				code = Constants.FAIL;
				msg = Constants.FAIL_MSG;
				logger.error(" = * = * = * = * = 新增商户商品和商户商品属性错误 = * = * = * = * = ", e);
			}
			ResponseUtil.write(request, response, code, msg, null);
		}
	} else {
		code = Constants.FAIL;
		msg = "无params参数";
	}
	
	ResponseUtil.write(request, response, code, msg, returnData);
	}
}
