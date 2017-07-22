package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IOrderDao;
import com.ryx.social.retail.dao.ISupplierDao;
import com.ryx.social.retail.dao.IWhseDao;
import com.ryx.social.retail.service.ICgtOrderService;
import com.ryx.social.retail.service.IItemService;
import com.ryx.social.retail.service.IMerchService;
import com.ryx.social.retail.service.IPurchOrderService;
import com.ryx.social.retail.service.ServiceHttpUtil;
import com.ryx.social.retail.util.GetUrlData;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class CgtOrderServiceImpl implements ICgtOrderService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CgtOrderServiceImpl.class);
	
	@Resource
	private IWhseDao whseDao;
	@Resource
	private ISupplierDao tobaccoSupplierDao;
	@Resource
	private IItemService itemService;
	
	@Resource
	private IPurchOrderService purchOrderService;
	@Resource
	private IOrderDao orderDao;
	@Resource
	private IMerchService merchService;

	
	@Override
	public void updateTobaccoList(Map<String, String> paramMap) throws Exception {
		LOG.debug("CgtOrderServiceImpl updateTobaccoList paramMap:"+paramMap);
		//获取卷烟系统服务地址
		String cgtComId = (String) paramMap.get("comId");
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "cgtorder/getItems";

		//从卷烟系统服务上获取数据
		String json = HttpUtil.post(cgtItemUrl, paramMap);
		Map<String, Object> itemMap = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
		
		//解析获取的数据
		String code = (String)itemMap.get("code");
		List<Map<String, Object>> itemList = null;
//		String merchId = (String) paramMap.get("refId"); //商户号
		if(code!=null && Constants.SUCCESS.equals(code)) {
			itemList = (List<Map<String, Object>>)itemMap.get("result");
			List<Map<String,Object>> updateItemList=new ArrayList<Map<String,Object>>();//需要修改的数据集合
			List<Map<String,Object>> insertItemList=new ArrayList<Map<String,Object>>();//需要添加的数据集合
			
			List<Map<String, Object>> thisAllItemList=tobaccoSupplierDao.getAllItemList();
			for(Map<String, Object> map:itemList){
				boolean ishas=true;
				for(Map<String, Object> thisMap:thisAllItemList){
					if(map.get("item_id").toString().trim().equals(thisMap.get("item_id").toString().trim())){
						map.put("supplier_id", cgtComId);
						updateItemList.add(map);
						ishas=false;
					}
				}
				if(ishas){
					insertItemList.add(map);
				}
			}
			
			tobaccoSupplierDao.updateItems2(updateItemList);
			tobaccoSupplierDao.insertItems(cgtComId, insertItemList);
		} else { //返回码异常
			throw new RuntimeException((String) itemMap.get("msg"));
		}
	}
	
	
	
	
	
	public List<Map<String, Object>> getTobaccoItemList(Map<String, String> paramMap) throws Exception {
		LOG.debug("CgtOrderServiceImpl getTobaccoItemList paramMap:"+paramMap);
		List<Map<String, Object>> itemList = null, tempList = null;
		
		itemList = tobaccoSupplierDao.getItemList(paramMap);
		Map<String, Map<String, Object>> itemMap = new HashMap<String, Map<String,Object>>();
		for (Map<String, Object> map : itemList) {
			map.put("QTY_WHSE", null);
			map.put("QTY_WHSE_WARN", null);
			map.put("QTY_SUGGEST", null);
			map.put("QTY_LMT", 999);
			map.put("IS_SHORT", null);
			map.put("IS_ADVISE", null);
			map.put("IS_NEW", null);
			map.put("ORD_TIMES", null);
			map.put("ORD_QTY", null);
			map.put("SEQ", null);
			map.put("QTY", null);
			map.put("IS_PROMOTE", null);
			map.put("PROMOTE_DESC", null);
			map.put("HISTORY_ORDER_FLAG", "0");
			itemMap.put(map.get("ITEM_ID").toString(), map);
		}
		
		String merchId = (String) paramMap.get("refId"); //商户号
		//查询库存表的数据
		Map<String, Object> whseParms = new HashMap<String, Object>();
		whseParms.put("merch_id", merchId);	
		List<Map<String, Object>> whseList = whseDao.getWhseList(whseParms);
		
		Map<String, Object> item = null;
		if(whseList!=null && whseList.size()>0) {
			for(Map<String, Object> whseRow : whseList) {
				String itemId = (String)whseRow.get("ITEM_ID");
				if(itemMap.containsKey(itemId)) {
					item = itemMap.get(itemId);
					BigDecimal qtyWhseWarn = MapUtil.getBigDecimal(whseRow, "QTY_WHSE_WARN", BigDecimal.ZERO); 	// 合理库存
					BigDecimal qtyWhse = MapUtil.getBigDecimal(whseRow, "QTY_WHSE", BigDecimal.ZERO);			// 当前库存
					// 如果库存量和建议订购量小于0 则赋值为0
					if (WhseServiceImpl.showPositiveWhse && qtyWhse.compareTo(BigDecimal.ZERO)<0) {
						qtyWhse = BigDecimal.ZERO;
					}
					BigDecimal qtySuggest = qtyWhseWarn.subtract(qtyWhse); // 建议订购量 = 合理库存-当前库存
					if(qtySuggest.compareTo(BigDecimal.ZERO)<0) qtySuggest = BigDecimal.ZERO;
					
					BigDecimal unitRatio = MapUtil.getBigDecimal(whseRow, "UNIT_RATIO", BigDecimal.ONE);
					
					
					item.put("QTY_WHSE", qtyWhse.divide(unitRatio, 2, BigDecimal.ROUND_HALF_UP));
					item.put("QTY_WHSE_WARN", qtyWhseWarn.divide(unitRatio, 2, BigDecimal.ROUND_HALF_UP));
					
					DecimalFormat format = new DecimalFormat("0");
					format.setRoundingMode(RoundingMode.UP);
					//合理库存-当前库存<=0, 则建议订购量为0
					
//					item.put("QTY_SUGGEST", format.format(qtySuggest.doubleValue()));
					item.put("QTY_SUGGEST", qtySuggest.divide(unitRatio, 2, BigDecimal.ROUND_HALF_UP));
				}
			}
		}
		
		//连接服务器获取数据
		String cgtlmtUrl = RetailConfig.getTobaccoServer() + "cgtorder/getCustItems";
		Map<String, String> param = new HashMap<String, String>();
		param.put("custId", paramMap.get("refId"));
		String json = HttpUtil.post(cgtlmtUrl, param);
		Map result = JsonUtil.json2Map(json);
		
		int index = 0;
		//解析数据
		String code = (String) result.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			List<Map<String, Object>> custItems = (List<Map<String, Object>>) result.get("result");
			for (Map<String, Object> map : custItems) {
				if(itemMap.containsKey(map.get("item_id"))) {
					item = itemMap.get(map.get("item_id"));
					item.put("IS_SHORT", map.get("is_short"));
					item.put("IS_ADVISE", map.get("is_advise"));
					item.put("IS_NEW", map.get("is_new"));
					item.put("ORD_TIMES", map.get("ord_times"));
					item.put("ORD_QTY", map.get("ord_qty"));
					item.put("SEQ", map.get("seq"));
					item.put("QTY", map.get("qty"));
					item.put("IS_PROMOTE", map.get("is_promote"));
					item.put("PROMOTE_DESC", map.get("promote_desc"));
					item.put("HISTORY_ORDER_FLAG", "1");
					
					/* 20140827 去掉新品的建议定量2条
					if("1".equals(map.get("is_new"))) {
						item.put("QTY_SUGGEST", 2);
					}*/
					
					itemList.remove(item);
					itemList.add(index, item);
					index++;
				}
			}
		}
		/*
		String lmturl = RetailConfig.getTobaccoServer() + "cgtorder/getItemLimits";
		json = HttpUtil.post(lmturl, param);
		result = JsonUtil.json2Map(json);
		
		//解析数据
		code = (String) result.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			List<Map<String, Object>> custItems = (List<Map<String, Object>>) result.get("result");
			BigDecimal lmt = null;
			BigDecimal suggest = null;
			for (Map<String, Object> map : custItems) {
				item = itemMap.get(map.get("item_id"));
				if(item != null) {
					item.put("QTY_LMT", map.get("qty_lmt"));
					lmt = MapUtil.getBigDecimal(map, "qty_lmt");
					suggest = MapUtil.getBigDecimal(item, "QTY_SUGGEST");
					if(lmt.compareTo(suggest) == -1) {
						item.put("QTY_SUGGEST", lmt);
					}
				}
			}
		}*/
		
		return itemList;
	}	
	
	//获取零售户当前周期的订单
	public Map<String, Object> getTobaccoOrder(Map<String, String> paramMap) throws Exception {
		LOG.debug("CgtOrderServiceImpl getTobaccoOrder paramMap:"+paramMap);
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/getOrderDetail";
		String json = HttpUtil.post(cgtOrderUrl, paramMap);
//		String json="{'result':{'supplier_id':'10370101','born_date':'20140221','flag':'0','crt_date':'20140221','order_date':'20140221','order_id':'','voucher_date':'','purch_order_line_temp':[{'qty_ord':'2','item_name':'泰山(大鸡)','item_id':'6901028153898'},{'qty_ord':'0','item_name':'南京(红)','item_id':'6901028300063'},{'qty_ord':'0','item_name':'红杉树(硬新)','item_id':'6901028305624'}]},'code':'0000','msg':'请求成功'}";
//		String json="{'result':{'supplier_id':'10370101','born_date':'20140208','crt_date':'20140208','status':'01','com_id':'10370101','slsman_id':'103701010129','order_id':'JN0000343845','cust_id':'1037010508807','voucher_date':'','qty_purch_total':'55','pmt_status':'02','flag':'1','amt_purch_total':'4000.5','dpt_sale_id':'01010300','co_num':'JN0000343845','order_date':'20140208','purch_order_line':[{'pri1':'54.00','qty_ord':'1','item_name':'双喜(软国际)','item_id':'6901028001472','amt_ord':'54.00','pri_wsale':'57','qty_rsn':'1','qty_req':'9'},{'pri1':'80.00','qty_ord':'4','item_name':'娇子(X)','item_id':'6901028024976','amt_ord':'320.00','pri_wsale':'56','qty_rsn':'50','qty_req':'4'},{'pri1':'72.00','qty_ord':'4','item_name':'白沙(8mg绿和)','item_id':'6901028069823','amt_ord':'288.00','pri_wsale':'80','qty_rsn':'50','qty_req':'9'},{'pri1':'24.00','qty_ord':'2','item_name':'黄山松(5支)','item_id':'6901028124133','amt_ord':'48.00','pri_wsale':'50','qty_rsn':'50','qty_req':'9'},{'pri1':'45.00','qty_ord':'1','item_name':'七匹狼(豪情)','item_id':'6901028138369','amt_ord':'45.00','pri_wsale':'60','qty_rsn':'1','qty_req':'9'},{'pri1':'22.50','qty_ord':'1','item_name':'哈德门(软)','item_id':'6901028149242','amt_ord':'22.50','pri_wsale':'30','qty_rsn':'1','qty_req':'9'},{'pri1':'81.00','qty_ord':'8','item_name':'泰山(白将军)','item_id':'6901028151634','amt_ord':'648.00','pri_wsale':'90','qty_rsn':'8','qty_req':'9'},{'pri1':'63.00','qty_ord':'6','item_name':'泰山(红将)','item_id':'6901028153201','amt_ord':'378.00','pri_wsale':'70','qty_rsn':'6','qty_req':'9'},{'pri1':'132.00','qty_ord':'5','item_name':'泰山(大鸡)','item_id':'6901028153898','amt_ord':'660.00','pri_wsale':'150','qty_rsn':'5','qty_req':'29'},{'pri1':'97.00','qty_ord':'3','item_name':'泰山(沂蒙)','item_id':'6901028157834','amt_ord':'291.00','pri_wsale':'100','qty_rsn':'10','qty_req':'3'},{'pri1':'54.00','qty_ord':'10','item_name':'泰山(硬红八喜)','item_id':'6901028159548','amt_ord':'540.00','pri_wsale':'60','qty_rsn':'10','qty_req':'90'},{'pri1':'22.50','qty_ord':'2','item_name':'红金龙(硬喜)','item_id':'6901028184793','amt_ord':'45.00','pri_wsale':'30','qty_rsn':'2','qty_req':'9'},{'pri1':'45.00','qty_ord':'3','item_name':'钻石(软红)','item_id':'6901028250641','amt_ord':'135.00','pri_wsale':'56','qty_rsn':'3','qty_req':'9'},{'pri1':'97.00','qty_ord':'1','item_name':'南京(红)','item_id':'6901028300063','amt_ord':'97.00','pri_wsale':'110','qty_rsn':'1','qty_req':'9'},{'pri1':'122.00','qty_ord':'3','item_name':'南京(佳品)','item_id':'6901028300087','amt_ord':'366.00','pri_wsale':'130','qty_rsn':'3','qty_req':'9'},{'pri1':'63.00','qty_ord':'1','item_name':'红塔山(软经典)','item_id':'6901028315012','amt_ord':'63.00','pri_wsale':'70','qty_rsn':'1','qty_req':'9'}]},'code':'0000','msg':'请求成功'}";
		Map<String, Object> orderMap = (Map<String, Object>)JsonUtil.json2Map(json);
		
		//解析数据
		Map<String, Object> orderData = null;
		String code = (String) orderMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			orderData = (Map<String, Object>) orderMap.get("result");
			if("0".equals(orderData.get("flag"))) {
				List<Map<String,Object>> orderDataList = null;
				String orderDate = (String)orderData.get("order_date");
				Map<String,Object> map=new HashMap<String,Object>();
				if(StringUtils.hasText(orderDate)) {
					map.put("merch_id", paramMap.get("ref_id"));
					map.put("orderDate", reduceDate(orderDate));
					orderDataList = orderDao.selectTemporaryMerchOrder(map);
				}
		        if(orderDataList!=null&&orderDataList.size()>0){
		        	List<Map<String,Object>> items=orderDao.selectTemporaryMerchOrderLine(map);
		        	if(items.size() > 0) {
		        		orderData.put("PURCH_ORDER_LINE_TEMP", items);
		        	} else {
		        		orderData.put("PURCH_ORDER_LINE_TEMP", getAdviceOrder(MapUtils.getString(paramMap, "ref_id")));
//		        		getAdviseOrder(paramMap,orderData);
		        	}
		        }else{
	        		orderData.put("PURCH_ORDER_LINE_TEMP", getAdviceOrder(MapUtils.getString(paramMap, "ref_id")));
//		        	getAdviseOrder(paramMap,orderData);
		        }
		        // 将微商盟系统生成的建议订单中的不在销卷烟删除
		        // 1. 先查询此零售户的公司号 根据公司号查询在销卷烟商品
		        Map<String, String> onSaleParam = new HashMap<String, String>();
		        onSaleParam.put("comId", paramMap.get("com_id"));
		        List<Map<String, Object>> onSaleTobaccoItems = tobaccoSupplierDao.getItemList(onSaleParam);
		        Map<String, Object> onSaleTobaccoItemIds = new HashMap<String, Object>();
		        for(Map<String, Object> onSaleTobaccoItem : onSaleTobaccoItems) {
		        	onSaleTobaccoItemIds.put((String) onSaleTobaccoItem.get("item_id"), null);
		        }
		        // 2. 再根据在销卷烟商品排除建议订单中的卷烟商品
        		List<Map<String, Object>> itemsInOrder = MapUtil.get(orderData, "PURCH_ORDER_LINE_TEMP", Collections.EMPTY_LIST);
		        int itemSize = itemsInOrder.size();
		        int itemIndex = itemSize-1;
        		for(; itemIndex>=0; itemIndex--) {
        			Map<String, Object> item = itemsInOrder.get(itemIndex);
        			String itemId = MapUtil.getString(item, "ITEM_ID", MapUtil.getString(item, "item_id"));
        			if(!onSaleTobaccoItemIds.containsKey(itemId)) {
        				itemsInOrder.remove(itemIndex);
        			}
		        }
			}
		} else { //返回码异常
			LOG.debug("获取卷烟订单失败：" + orderMap.get("msg"));
			throw new RuntimeException((String) orderMap.get("msg"));
			//orderData = new HashMap<String, Object>();
			//orderData.put("flag", "0");
		}
		return orderData;
	}
	/**
	 * 将日期减两天
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @return
	 */
	public String reduceDate(String str)throws Exception{
		LOG.debug("CgtOrderServiceImpl reduceDate str:"+str);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date dt = sdf.parse(str);
		Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.DAY_OF_YEAR,-2);
        Date dt1=rightNow.getTime();
        String orderDate = sdf.format(dt1);
		return orderDate;
	}

	
	/**
	 * 如果当前订货周期没有订单 则由RTMS和微商盟分别推荐一些卷烟 生成建议订单<br>
	 * 生成规则:
	 * <ol>
	 * <li>RTMS返回新荐促和最近60天订购过的卷烟商品(最多20条)</li>
	 * <li>微商盟根据合理库存-当前库存计算建议订购量 如果建议订购量>0的放到建议订单中</li>
	 * <li>如果启用自动配货 则订购量=min(建议量,限量) 如果没有启用自动配货 则全部为0</li>
	 * </ol>
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getAdviceOrder(String merchId) throws Exception {
		LOG.debug("CgtOrderServiceImpl getAdviceOrder merchId: " + merchId);
		// 新荐促和最近订货卷烟 最后返回此列表
		List<Map<String, Object>> tobaccoItems;
		// 新荐促和最近订货卷烟ID - 卷烟
		HashMap<String, Map<String, Object>> tobaccoItemMap = new HashMap<String, Map<String, Object>>();
		// 可能需要获取限量的商品ID
		List<String> limitIds;
		// 全部复用此请求URL
		String url;
		// 全部复用此请求参数
		Map<String, Object> param;
		// 全部复用此请求返回字符串
		String resultString;
		// 全部复用此请求返回对象
		Map<String, Object> result;
		
		// 从RTMS获取新荐促和最近订货卷烟列表
		param = new HashMap<String, Object>();
		param.put("custId", merchId);
		
		url = RetailConfig.getTobaccoServer() + "cgtorder/getCustItems";
		LOG.debug("从RTMS获取新荐促和最近订货卷烟列表URL: " + url + ", PARAM: " + param);
		resultString = HttpUtil.post(url, param);
		LOG.debug("从RTMS获取新荐促和最近订货卷烟列表返回信息: " + resultString);
		
		result = JsonUtil.json2Map(resultString);
		if(Constants.SUCCESS.equals(MapUtil.getString(result, "code"))) {
			tobaccoItems = MapUtil.get(result, "result", Collections.EMPTY_LIST);
			for(Map<String, Object> tobaccoItem : tobaccoItems) {
				tobaccoItem.put("qty_ord", BigDecimal.ZERO); // 默认订购量是0
				tobaccoItemMap.put(MapUtil.getString(tobaccoItem, "item_id"), tobaccoItem); // 注册卷烟
			}
		} else {
			throw new RuntimeException(MapUtil.getString(result, "msg"));
		}
		
		// 商户是否开启自动配货 默认是不开启
		param = new HashMap<String, Object>();
		param.put("merch_id", merchId);
		boolean adviseQuantity = false;
		List<Map<String, Object>> merchList = merchService.selectMerch(param);
		if(merchList!=null && !merchList.isEmpty()) {
			adviseQuantity = "1".equals(MapUtil.getString(merchList.get(0), "order_type", "0"));
		}
		
		// 查询库存量小于合理库存的卷烟商品 如果开启自动配货 则赋值为建议订购量 否则为0
		List<Map<String, Object>> itemWhses = whseDao.selectWhseMerchJoinMerchItem(param);
		// 可能需要获取限量的商品ID
		limitIds = new ArrayList<String>(itemWhses.size());
		for(Map<String, Object> whse : itemWhses) {
			String itemId = MapUtil.getString(whse, "item_id");
			// 库存量
			BigDecimal quantity = MapUtil.getBigDecimal(whse, "qty_whse");
			if(quantity.compareTo(BigDecimal.ZERO)<0) quantity = BigDecimal.ZERO;
			// 合理库存量
			BigDecimal warnQuantity = MapUtil.getBigDecimal(whse, "qty_whse_warn");
			if(warnQuantity.compareTo(BigDecimal.ZERO)<0) warnQuantity = BigDecimal.ZERO;
			// 条盒比
			BigDecimal unitRatio = MapUtil.getBigDecimal(whse, "big_unit_ratio", BigDecimal.ONE);
			// 建议订购量 = (合理库存量-库存量)/条盒比, 四舍五入取整
			BigDecimal adviceQuantity = warnQuantity.subtract(quantity).divide(unitRatio, 0, BigDecimal.ROUND_HALF_UP);
			if(adviceQuantity.compareTo(BigDecimal.ZERO)<0) adviceQuantity = BigDecimal.ZERO;
			
			// 如果建议订购量>0 再考虑是否获取限量
			if(adviceQuantity.compareTo(BigDecimal.ZERO)>0) limitIds.add(itemId);
			
			Map<String, Object> tobaccoItem;
			boolean containsId = tobaccoItemMap.containsKey(itemId);
			if(adviseQuantity && containsId) {
				tobaccoItem = tobaccoItemMap.get(itemId);
				tobaccoItem.put("qty_ord", adviceQuantity);
			} else if(!containsId) {
				tobaccoItem = new HashMap<String, Object>();
				tobaccoItem.put("item_id", itemId);
				tobaccoItem.put("item_name", MapUtil.getString(whse, "item_name"));
				if(adviseQuantity) tobaccoItem.put("qty_ord", adviceQuantity);
				else tobaccoItem.put("qty_ord", BigDecimal.ZERO);
				tobaccoItemMap.put(itemId, tobaccoItem);
				tobaccoItems.add(tobaccoItem);
			}
		}
		
		// 如果开启自动配货 需要获取限量 如果建议订购量大于限量 则赋值为限量
		if(adviseQuantity) {
			param = new HashMap<String, Object>();
			param.put("cust_id", merchId);
			param.put("item_list", limitIds);
			String params = JsonUtil.map2json(param);
			param = new HashMap<String, Object>();
			param.put("params", params);
			
			url = RetailConfig.getTobaccoServer() + "cgtorder/getCustItemLmt";
			LOG.debug("从RTMS获取限量列表URL: " + url + ", PARAM: " + param);
			resultString = HttpUtil.post(url, param);
			LOG.debug("从RTMS获取限量列表返回值: " + resultString);
			result = JsonUtil.json2Map(resultString);
			
			if(Constants.SUCCESS.equals(MapUtils.getString(result, "code"))) {
				List<Map<String, Object>> tobaccoLimits = MapUtil.get(result, "result", Collections.EMPTY_LIST);
				for(Map<String, Object> tobaccoLimit : tobaccoLimits) {
					String itemId = MapUtil.getString(tobaccoLimit, "item_id");
					BigDecimal limitQuantity = MapUtil.getBigDecimal(tobaccoLimit, "qty_lmt");
					if(tobaccoItemMap.containsKey(itemId)) {
						Map<String, Object> tobaccoItem = tobaccoItemMap.get(itemId);
						BigDecimal orderQuantity = MapUtil.getBigDecimal(tobaccoItem, "qty_ord");
						// 如果建议订购量大于限量 则赋值为限量
						if(orderQuantity.compareTo(limitQuantity)>0) tobaccoItem.put("qty_ord", limitQuantity);
					}
				}
			} else {
				throw new RuntimeException(MapUtils.getString(result, "msg"));
			}
		}
		
		return tobaccoItems;
	}
	
	/**
	 * 生成建议订单
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	private void getAdviseOrder(Map<String,String> paramMap,Map<String, Object> orderData)throws Exception{
		LOG.debug("CgtOrderServiceImpl getAdviseOrder orderData:"+orderData);
		// 新订单
		//连接服务器获取数据
		String cgtlmtUrl = RetailConfig.getTobaccoServer() + "cgtorder/getCustItems";
		Map<String, String> param = new HashMap<String, String>();
		param.put("custId", paramMap.get("refId"));
		String json2 = HttpUtil.post(cgtlmtUrl, param);
		Map result = JsonUtil.json2Map(json2);
		
		List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
		Map<String, Object> item = null;
		//解析数据
		String code2 = (String) result.get("code");
		Map<String, Map> keys = new HashMap<String, Map>();
		int index = 0;
		if(code2!=null && Constants.SUCCESS.equals(code2)) {
			// 获取建议订购的卷烟
			List<Map<String, Object>> custItems = (List<Map<String, Object>>) result.get("result");
			for (Map<String, Object> map : custItems) {
				item = new HashMap<String, Object>();
				item.put("ITEM_ID", map.get("item_id"));
				item.put("ITEM_NAME", map.get("item_name"));
				/* 20140729 去掉新品默认两条
				if("1".equals(map.get("is_new"))) {
					item.put("QTY_ORD", 2);
				} else {
					item.put("QTY_ORD", 0);
				}*/
				item.put("QTY_ORD", 0);
				/* 20150127 去掉必须为新促荐时才作为建议卷烟
				if("1".equals(map.get("is_new")) || "1".equals(map.get("is_advise"))
					|| "1".equals(map.get("is_promote"))) {
					index++;
					keys.put((String)map.get("item_id"), item);
					items.add(item);
				}
				*/
				/* 20150127 暂时不加is_hot字段，加新字段智能终端可能会有问题
				if(!("1".equals(map.get("is_new")) || "1".equals(map.get("is_advise"))
						|| "1".equals(map.get("is_promote")))) {
					map.put("is_hot", "1");
				}
				*/
				index++;
				keys.put((String)map.get("item_id"), item);
				items.add(item);
			}
		}
		
		// 获取商户order_type(是否自动卷烟配货) #by 梁凯 2014-7-30
		Map<String, Object> merchParam = new HashMap<String, Object>();
		merchParam.put("merch_id", paramMap.get("custId"));
		List<Map<String, Object>> merchList = merchService.selectMerch(merchParam);
		boolean autoOrder = true;
		if(!merchList.isEmpty()) {
			Map<String, Object> merchMap = merchList.get(0);
			String orderType = MapUtil.getString(merchMap, "order_type", "1");
			autoOrder = "1".equals(orderType);
		}
		
		// 获取库存小于合理库存的卷烟
		Map<String, Object> whseParms = new HashMap<String, Object>();
		whseParms.put("merch_id", paramMap.get("refId"));	
		List<Map<String, Object>> whseList = whseDao.selectWhseMerchJoinMerchItem(whseParms);
		for (Map<String, Object> map : whseList) {
			double tqtyWhseWarn = Double.valueOf(map.get("QTY_WHSE_WARN").toString().trim());// 合理库存
			if(tqtyWhseWarn!=0) {
				BigDecimal unitRatio = MapUtil.getBigDecimal(map, "BIG_UNIT_RATIO", BigDecimal.ONE);
				BigDecimal qtyWhseWarn = (BigDecimal)map.get("QTY_WHSE_WARN"); // 合理库存
				BigDecimal qtyWhse = (BigDecimal)map.get("QTY_WHSE"); // 当前库存
				if(qtyWhse.intValue() < 0) qtyWhse = BigDecimal.ZERO;
				BigDecimal qtySuggest = qtyWhseWarn.subtract(qtyWhse); // 建议订购量 = 合理库存-当前库存
				//如果小于0，则赋值为0
				qtySuggest = qtySuggest.compareTo(BigDecimal.ZERO)<=0 ? BigDecimal.ZERO : qtySuggest;
				/**
				 * 20140806 去掉默认订购量不超过10条的逻辑
				 */
				//qtySuggest = qtySuggest.compareTo(new BigDecimal(100)) > 0 ? new BigDecimal(100) : qtySuggest;
				
				DecimalFormat format = new DecimalFormat("0");
				format.setRoundingMode(RoundingMode.UP);
				//合理库存-当前库存<=0, 则建议订购量为0
				//如果商户的order_type字段为0, 建议订购量为0 #by 梁凯 2014-7-30
//				int qtyOrd = autoOrder ? (int)Math.ceil(Double.valueOf(qtySuggest.toString().trim())/10) : 0;
				// 根据条盒比计算建议订购量
				BigDecimal qtyOrd = autoOrder ? qtySuggest.divide(unitRatio, 0, BigDecimal.ROUND_CEILING) : BigDecimal.ZERO;
				
				if(!keys.containsKey(map.get("ITEM_ID"))) {
					item = new HashMap<String, Object>();
					item.put("ITEM_ID", map.get("ITEM_ID"));
					item.put("ITEM_NAME", map.get("ITEM_NAME"));
					item.put("QTY_ORD", qtyOrd);
					//item.put("QTY_ORD", 0);
					
					items.add(index, item);
					keys.put((String)map.get("ITEM_ID"), item);
					index++;
				} else {
					item = keys.get(map.get("ITEM_ID"));
					item.put("QTY_ORD", qtyOrd);
					//item.put("QTY_ORD", 0);
				}
			}
		}
		
		// 放入建议订单中
		orderData.put("PURCH_ORDER_LINE_TEMP", items);
	}
	
	
	
	//提交订单
	public Map<String, Object> submitTobaccoOrder(Map<String, String> paramMap) throws Exception {
		LOG.debug("CgtOrderServiceImpl submitTobaccoOrder paramMap:"+paramMap);
		//连接服务器获取数据
		String submitOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/submitOrder";
		Map<String, Object> jsonData = JsonUtil.json2Map(paramMap.get("jsonParam"));
		LOG.debug(" = * = * = * = * = 客户端提交卷烟订单数据 = * = * = * = * = " + jsonData);
		Map<String, String> submitOrderParams = new HashMap<String, String>();
		submitOrderParams.put("params", JsonUtil.map2json(jsonData));
		submitOrderParams.put("custId", paramMap.get("custId"));
		
		
		String flag="0";
		if(jsonData.containsKey("flag")){
			flag=jsonData.get("flag").toString();
		}
		String json =null;
		Map<String, Object> orderMap=null;
		Map<String, Object> orderData = new HashMap<String, Object>();
		
		if("2".equals(flag)){
			LOG.debug("删除订单：" + JsonUtil.map2json(submitOrderParams));
			json = HttpUtil.post(submitOrderUrl, submitOrderParams);
			LOG.debug("删除订单完成：" + json);
//			json="{'result':{'flag':'0','order_id':'','voucher_date':'','purch_order_line_temp':[{'qty_ord':'2','item_name':'泰山(大鸡)','item_id':'6901028153898'},{'qty_ord':'0','item_name':'南京(红)','item_id':'6901028300063'},{'qty_ord':'0','item_name':'红杉树(硬新)','item_id':'6901028305624'}]},'code':'0000','msg':'请求成功'}";
			orderMap = JsonUtil.json2Map(json);
			
			if(orderMap != null) {
				//解析数据
				String code = (String) orderMap.get("code");
				if(code!=null && Constants.SUCCESS.equals(code)) {
					orderData = (Map<String, Object>)orderMap.get("result");
					if(orderData!=null&&orderData.containsKey("flag")&&"0".equals(orderData.get("flag"))){
						saveTemporaryOrder(submitOrderParams);//删除临时订单
					}
					if(orderData != null) {
						orderData.put("submitmsg", orderMap.get("msg"));
					}
				} else { //返回码异常
					throw new RuntimeException((String) orderMap.get("msg"));
				}
			} else {
				throw new RuntimeException("服务器返回的数据为空");
			}
		}else{
			
			saveTemporaryOrder(submitOrderParams);//保存临时订单
			
			LOG.debug("提交订单：" + JsonUtil.map2json(submitOrderParams));
			json = HttpUtil.post(submitOrderUrl, submitOrderParams);
			LOG.debug("提交订单完成：" + json);
			
//			json="{'result':{'supplier_id':'10370101','born_date':'20140221','flag':'0','crt_date':'20140221','order_date':'20140221','order_id':'','voucher_date':'','purch_order_line_temp':[{'qty_ord':'2','item_name':'泰山(大鸡)','item_id':'6901028153898'},{'qty_ord':'0','item_name':'南京(红)','item_id':'6901028300063'},{'qty_ord':'0','item_name':'红杉树(硬新)','item_id':'6901028305624'}]},'code':'0000','msg':'请求成功'}";
			orderMap= JsonUtil.json2Map(json);
			
			if(orderMap != null) {
				//解析数据
				String code = (String) orderMap.get("code");
				if(code!=null && Constants.SUCCESS.equals(code)) {
					orderData = (Map<String, Object>)orderMap.get("result");
					if(orderData==null||!orderData.containsKey("flag")||"0".equals(orderData.get("flag"))){
						orderData=getTobaccoOrder(paramMap);
					}
					if(orderData != null) {
						orderData.put("submitmsg", orderMap.get("msg"));
					}
				} else { //返回码异常
					throw new RuntimeException((String) orderMap.get("msg"));
				}
			} else {
				throw new RuntimeException("服务器返回的数据为空");
			}
		}
		
//		saveTemporaryOrder(submitOrderParams);//保存临时订单
//		
//		LOG.debug("提交订单：" + JsonUtil.map2json(submitOrderParams));
////		String json = HttpUtil.post(submitOrderUrl, submitOrderParams);
//		String json="{'result':{'supplier_id':'10370101','born_date':'20140221','flag':'0','crt_date':'20140221','order_date':'20140221','order_id':'','voucher_date':'','purch_order_line_temp':[{'qty_ord':'2','item_name':'泰山(大鸡)','item_id':'6901028153898'},{'qty_ord':'0','item_name':'南京(红)','item_id':'6901028300063'},{'qty_ord':'0','item_name':'红杉树(硬新)','item_id':'6901028305624'}]},'code':'0000','msg':'请求成功'}";
//		LOG.debug("提交订单完成：" + json);
		return orderData;
	}
	/**
	 * 将订单放入用户临时订单
	 * @author 徐虎彬
	 * @date 2014年4月23日
	 * @param submitOrderParams
	 * @throws Exception
	 */
	public void saveTemporaryOrder(Map<String, String> submitOrderParams)throws Exception{
		LOG.debug("CgtOrderServiceImpl saveTemporaryOrder submitOrderParams:"+submitOrderParams);
		Map<String,Object> dataMap=new HashMap<String,Object>();
		String merch_id=submitOrderParams.get("custId").toLowerCase();
		dataMap.put("merch_id",merch_id );
		String orderData=submitOrderParams.get("params");
		Map<String,Object> orderDataMap=JsonUtil.json2Map(orderData);
		dataMap.put("order_id", orderDataMap.get("order_id"));
		dataMap.put("order_date", orderDataMap.get("order_date"));
		List<Map<String,Object>> orderDateMapList=orderDao.selectTemporaryMerchOrder(dataMap);
		if(orderDateMapList!=null&&orderDateMapList.size()>0){
			orderDao.updateTemporaryMerchOrder(dataMap);//覆盖之前的临时订单
		}else{
			orderDao.insertTemporaryMerchOrder(dataMap);
		}
		orderDao.deleteTemporaryMerchOrderLine(dataMap);//删除临时订单行
		
		List<Map<String,Object>> orderDataListLine = new ArrayList<Map<String,Object>>();
		if(orderDataMap.containsKey("list")){
			orderDataListLine =(List<Map<String,Object>>) orderDataMap.get("list");
		}
		for(int i=0;i<orderDataListLine.size();i++){
			orderDataListLine.get(i).put("merch_id", merch_id);
		}
		
		orderDao.insertTemporaryMerchOrderLine(orderDataListLine);
		
	}

	//下载订货参数
	public Map<String, Object> getOrderParams(Map<String, String> paramMap)throws Exception{
		LOG.debug("CgtOrderServiceImpl getOrderParams paramMap:"+paramMap);
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/orderparams";
		String json = HttpUtil.post(cgtOrderUrl, paramMap);
//		String json="{'result':{'is_order':'0','call_period':'','show_limit':'0','order_begintime':'20140321 01:00:00','adviseorder_update_enabled':'1','adviseorder_enabled':'1','issuer_code':'14144500','pay_begintime':'20140321 01:00:00','order_limit':'800','order_endtime':'20140321 14:00:00','payee_account':'90104045020100056217','is_payment':'0','creditpay_enabled':'0','pay_endtime':'20140321 14:00:00','beneficiary_bank':'山东济南润丰农村合作银行佛山支行','pay_date':'','order_date':'20140321'},'code':'0000','msg':'请求成功'}";
		Map<String, Object> orderMap = JsonUtil.json2Map(json);
		
		//解析数据
		Map<String, Object> orderData = null;
		String code = (String) orderMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			orderData = (Map<String, Object>) orderMap.get("result");
		} else { //返回码异常
			LOG.debug("url=" + cgtOrderUrl + ", params=" + paramMap + ", result=" + json);
			throw new RuntimeException((String) orderMap.get("msg"));
		}
		return orderData;
	}

	//订单列表
	public List<Map<String, Object>> getOrderList(Map<String, String> paramMap) throws Exception{
		LOG.debug("CgtOrderServiceImpl getOrderList paramMap: " + paramMap);
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/getOrderList";
		String json = HttpUtil.post(cgtOrderUrl, paramMap);
		LOG.debug(cgtOrderUrl + " 返回数据: " + json);
//		String json="{'result':[{'supplier_id':'10370101','born_date':'20140208','crt_date':'20140208','status':'09','com_id':'10370101','slsman_id':'103701010129','order_id':'JN0000343845','cust_id':'1037010508807','voucher_date':'','qty_purch_total':'55','pmt_status':'03','flag':'1','amt_purch_total':'4000.5','dpt_sale_id':'01010300','co_num':'JN0000343845','order_date':'20140208'},{'supplier_id':'10370101','born_date':'20140124','crt_date':'20140124','status':'09','com_id':'10370101','slsman_id':'103701010129','order_id':'X0003794214','cust_id':'1037010508807','voucher_date':'','qty_purch_total':'198','pmt_status':'03','flag':'1','amt_purch_total':'20987.5','dpt_sale_id':'01010300','co_num':'X0003794214','order_date':'20140124'},{'supplier_id':'10370101','born_date':'20140213','crt_date':'20140213','status':'01','com_id':'10370101','slsman_id':'103701010129','order_id':'JN0000350890','cust_id':'1037010508807','voucher_date':'','qty_purch_total':'51','pmt_status':'02','flag':'1','amt_purch_total':'4873','dpt_sale_id':'01010300','co_num':'JN0000350890','order_date':'20140213'},{'supplier_id':'10370101','born_date':'20140129','crt_date':'20140129','status':'09','com_id':'10370101','slsman_id':'103701010129','order_id':'JN0000340149','cust_id':'1037010508807','voucher_date':'','qty_purch_total':'36','pmt_status':'03','flag':'1','amt_purch_total':'3066','dpt_sale_id':'01010300','co_num':'JN0000340149','order_date':'20140129'}],'code':'0000','msg':'请求成功'}";
		Map<String, Object> orderMap = JsonUtil.json2Map(json);
		
		//解析数据
		List<Map<String, Object>> orderList = null;
		List<Map<String, Object>> orderList3 = new ArrayList<Map<String, Object>>();//订单入库标记后的列表
		String code = (String) orderMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			orderList = (List<Map<String, Object>>) orderMap.get("result");
			orderList3=putOrderTag(paramMap,orderList);
		} else { // rtms异常只记录日志
			LOG.debug((String)orderMap.get("msg"));
		}
		
		return orderList3;
	}
	
	//订单明细
	public Map<String, Object> getOrderDetail(Map<String, String> paramMap) throws Exception{
		LOG.debug("CgtOrderServiceImpl getOrderDetail paramMap:"+paramMap);
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/getOrderDetail";
		String json = HttpUtil.post(cgtOrderUrl, paramMap);
//		String json="{'result':{'supplier_id':'10370101','born_date':'20140208','crt_date':'20140208','status':'09','com_id':'10370101','slsman_id':'103701010129','order_id':'JN0000343845','cust_id':'1037010508807','voucher_date':'','qty_purch_total':'55','pmt_status':'03','flag':'1','amt_purch_total':'4000.5','dpt_sale_id':'01010300','co_num':'JN0000343845','order_date':'20140208','purch_order_line':[{'pri1':'54.00','qty_ord':'1','item_name':'双喜(软国际)','item_id':'6901028001472','amt_ord':'54.00','qty_rsn':'1','qty_req':'9'},{'pri1':'80.00','qty_ord':'4','item_name':'娇子(X)','item_id':'6901028024976','amt_ord':'320.00','qty_rsn':'50','qty_req':'4'},{'pri1':'72.00','qty_ord':'4','item_name':'白沙(8mg绿和)','item_id':'6901028069823','amt_ord':'288.00','qty_rsn':'50','qty_req':'9'},{'pri1':'24.00','qty_ord':'2','item_name':'黄山松(5支)','item_id':'6901028124133','amt_ord':'48.00','qty_rsn':'50','qty_req':'9'},{'pri1':'45.00','qty_ord':'1','item_name':'七匹狼(豪情)','item_id':'6901028138369','amt_ord':'45.00','qty_rsn':'1','qty_req':'9'},{'pri1':'22.50','qty_ord':'1','item_name':'哈德门(软)','item_id':'6901028149242','amt_ord':'22.50','qty_rsn':'1','qty_req':'9'},{'pri1':'81.00','qty_ord':'8','item_name':'泰山(白将军)','item_id':'6901028151634','amt_ord':'648.00','qty_rsn':'8','qty_req':'9'},{'pri1':'63.00','qty_ord':'6','item_name':'泰山(红将)','item_id':'6901028153201','amt_ord':'378.00','qty_rsn':'6','qty_req':'9'},{'pri1':'132.00','qty_ord':'5','item_name':'泰山(大鸡)','item_id':'6901028153898','amt_ord':'660.00','qty_rsn':'5','qty_req':'29'},{'pri1':'97.00','qty_ord':'3','item_name':'泰山(沂蒙)','item_id':'6901028157834','amt_ord':'291.00','qty_rsn':'10','qty_req':'3'},{'pri1':'54.00','qty_ord':'10','item_name':'泰山(硬红八喜)','item_id':'6901028159548','amt_ord':'540.00','qty_rsn':'10','qty_req':'90'},{'pri1':'22.50','qty_ord':'2','item_name':'红金龙(硬喜)','item_id':'6901028184793','amt_ord':'45.00','qty_rsn':'2','qty_req':'9'},{'pri1':'45.00','qty_ord':'3','item_name':'钻石(软红)','item_id':'6901028250641','amt_ord':'135.00','qty_rsn':'3','qty_req':'9'},{'pri1':'97.00','qty_ord':'1','item_name':'南京(红)','item_id':'6901028300063','amt_ord':'97.00','qty_rsn':'1','qty_req':'9'},{'pri1':'122.00','qty_ord':'3','item_name':'南京(佳品)','item_id':'6901028300087','amt_ord':'366.00','qty_rsn':'3','qty_req':'9'},{'pri1':'63.00','qty_ord':'1','item_name':'红塔山(软经典)','item_id':'6901028315012','amt_ord':'63.00','qty_rsn':'1','qty_req':'9'}]},'code':'0000','msg':'请求成功'}";
		Map<String, Object> orderMap = JsonUtil.json2Map(json);
		//解析数据
		Map<String, Object> orderData = null;
		String code = (String) orderMap.get("code");
		
		List<Map<String, Object>> orderList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> orderList3 = null;
		
		if(code!=null && Constants.SUCCESS.equals(code)) {
			orderData = (Map<String, Object>) orderMap.get("result");
			orderList.add(orderData);
			orderList3=putOrderTag(paramMap,orderList);
		} else { //返回码异常
			throw new RuntimeException((String) orderMap.get("msg"));
		}
		if(orderList3!=null&&orderList3.size()>0){
			orderData=orderList3.get(0);
		}
		return orderData;
	}
	/**
	 * 对已入库的订单进行标记
	 * @author 徐虎彬
	 * @date 2014年4月1日
	 * @param paramMap
	 * @param orderList2
	 * @param orderList
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> putOrderTag(Map<String, String> paramMap,
			List<Map<String, Object>> orderList)throws Exception{
		LOG.debug("CgtOrderServiceImpl putOrderTag paramMap: " + paramMap);
		LOG.debug("CgtOrderServiceImpl putOrderTag orderList: " + orderList);
		List<Map<String, Object>> orderList2 = null;
		List<Map<String, Object>> orderList3 = new ArrayList<Map<String, Object>>();//订单入库标记后的列表
		
		/*
		 * 查询已入库订单
		 */
		Map mp=new HashMap();
		mp.put("MERCH_ID",paramMap.get("custId"));
		orderList2=whseDao.selectOrderList(mp);
		//List<String> listOrderId=new ArrayList<String>();
		Map<String, String> orderIds = new HashMap<String, String>();
		
		/*
		 * 标记已入库订单 
		 */
		if(orderList2!=null&&orderList2.size()>0){
			for(Map<String, Object> m:orderList2){
				//listOrderId.add((String)m.get("ORDER_ID"));
				orderIds.put(MapUtil.getString(m, "ORDER_ID"), MapUtil.getString(m, "NOTE"));
			}
		}
		for(Map<String,Object> m:orderList){
			String oid = MapUtil.getString(m, "order_id", "");
			if(StringUtil.isBlank(oid)) {
				// 订单号为空
				continue;
			}
			// 已入库判断标准: 
			// 1.有初始化时间且订单时间在初始化时间之前的; 
			// 2.在order_merch表中存在的入库单
			// 3.订单日期超出15天的
			// 三者满足其一则认为是已入库
//			if(paramMap.get("init_date")!=null && Integer.parseInt(paramMap.get("init_date"))>Integer.parseInt((String)m.get("order_date"))) {
			int orderDate = MapUtil.getInt(m, "order_date",0);
			if(orderIds.containsKey(oid) 
					|| MapUtil.getInt(paramMap, "init_date",0) > orderDate
					|| Integer.parseInt(DateUtil.getPreviousDay(DateUtil.getToday(), 15)) > orderDate) {
				m.put("SU_IN", "1");//标记已入库
				m.put("note", MapUtil.getString(orderIds, oid));
			} else {
				String orderData = MapUtil.getString(m, "order_date");
				// 订单日期+3天，如果订单没入库则自动入库 2014年10月21日
				int intOrderData = Integer.parseInt(DateUtil.getNextDay(orderData, 3));
				int intNow = Integer.parseInt(DateUtil.getToday());
				if(intNow >= intOrderData) {
					// 订单未入库，自动入库
					Map<String, String> putParam = new HashMap<String, String>();
					putParam.put("orderId", oid);
					putParam.put("custId", paramMap.get("custId"));
					putParam.put("coNum", oid);
					putParam.put("comId", paramMap.get("com_id"));
					putParam.put("operator", paramMap.get("user_code"));
					boolean b = autoUpdateAndGetOrderList(putParam);

					m.put("SU_IN", "1");//标记已入库
					if(b) {
						m.put("IS_AUTO", "1");
					}
				}
			}
			
			if(!m.containsKey("SU_IN")){
				if("09".equals(m.get("status"))){
					m.put("ORDER_STATUS2", "1");
				}else{
					m.put("ORDER_STATUS", "1");
				}
			}
			orderList3.add(m);
		}
		
		return orderList3;
	}


	/**
	 * 订单入库
	 * @param paramMap
	 * @return 订单列表
	 * @throws Exception
	 */
	public List<Map<String, Object>> updateAndGetOrderList(Map<String, String> paramMap)
			throws Exception{
		LOG.debug("CgtOrderServiceImpl updateAndGetOrderList paramMap: " + paramMap);
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/getOrderDetail";
		String json = HttpUtil.post(cgtOrderUrl, paramMap);
		Map<String, Object> orderMap = JsonUtil.json2Map(json);
		Map<String, Object> orderData=(Map<String,Object>)orderMap.get("result");
		
		putOrderToWhse(paramMap, orderData);
		/*
		List<Map<String,Object>> myThisOrdeList=(List<Map<String,Object>>)orderData.get("purch_order_line");
		
		if(myThisOrdeList!=null&&myThisOrdeList.size()>0){
			//调用新增商品服务初始化数据
			List<Map<String,Object>> listAddOrder=createMerchTobacco(orderData,paramMap);
			//调用修改库存dao，修改库存信息
			Map<String,Object> od=updateWeshMerch(orderData,listAddOrder,paramMap);
			
			//将订单加入已入库订单表
			Map<String,Object> myOrderMap=new HashMap<String,Object>();
			myOrderMap.put("ORDER_ID", paramMap.get("orderId"));
			myOrderMap.put("MERCH_ID", paramMap.get("custId"));
			whseDao.insertOrder(myOrderMap);
			
			//调用添加采购单服务，插入采购单
			Map<String,Object> odOrder=new HashMap<String,Object>();
			orderData.remove("purch_order_line");
			od.putAll(orderData);
			
			String orderDate = MapUtil.getString(orderData, "order_date");
			odOrder.put("order_date", orderDate);
			odOrder.put("voucher_date", DateUtil.getNextDay(orderDate, 2));
			
			String odStr=JsonUtil.map2json(od);
			odOrder.put("json_param", odStr);
			odOrder.put("merch_id", paramMap.get("custId"));
			odOrder.put("order_id", paramMap.get("orderId"));
			odOrder.put("operator", paramMap.get("operator"));
			purchOrderService.insertPurchOrder(odOrder);
			
		}
		*/
		
		return getOrderList(paramMap);
	}
	
	public boolean autoUpdateAndGetOrderList(Map<String, String> paramMap)
			throws Exception{
		LOG.debug("CgtOrderServiceImpl autoUpdateAndGetOrderList paramMap:"+paramMap);
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/getOrderDetail";
		String json = HttpUtil.post(cgtOrderUrl, paramMap);
		Map<String, Object> orderMap = JsonUtil.json2Map(json);
		Map<String, Object> orderData=(Map<String,Object>)orderMap.get("result");
		
		return putOrderToWhse(paramMap, orderData);
	}
	
	public boolean putOrderToWhse(Map<String, String> paramMap, Map<String, Object> orderData) 
			throws Exception {
		LOG.debug("CgtOrderServiceImpl putOrderToWhse orderData:"+orderData);
		if(orderData == null) return false;
		
		List<Map<String,Object>> myThisOrdeList=(List<Map<String,Object>>)orderData.get("purch_order_line");
		
		if(myThisOrdeList!=null&&myThisOrdeList.size()>0){
			
			StringBuffer itemIdSb = new StringBuffer();
			Map<String, Map<String, Object>> orderMap = new HashMap<String, Map<String,Object>>();
			String itemId = "";
			for (Map<String, Object> map : myThisOrdeList) {
				itemId = MapUtil.getString(map, "item_id");
				itemIdSb.append(itemId);
				itemIdSb.append(",");
				orderMap.put(itemId, map);
			}
			Map<String, Object> whseParam = new HashMap<String, Object>();
			whseParam.put("merch_id", MapUtil.getString(orderData, "cust_id"));
			whseParam.put("item_id", itemIdSb.toString());
			whseParam.put("page_size", -1);
			whseParam.put("page_index", -1);
			List<Map<String, Object>> whseList = whseDao.searchMerchItemAndWhse(whseParam);
			BigDecimal unitRatio = new BigDecimal("1");
			for (Map<String, Object> map : whseList) {
				itemId = MapUtil.getString(map, "item_id");
				map.put("qty_whse", MapUtil.getBigDecimal(map, "qty_whse").multiply(MapUtil.getBigDecimal(map, "unit_ratio")));
				unitRatio = MapUtil.getBigDecimal(map, "unit_ratio", new BigDecimal("1"));
				unitRatio = unitRatio.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("1") : unitRatio;
				map.put("pri1", MapUtil.getBigDecimal(map, "pri1").divide(unitRatio));
				map.put("cost", MapUtil.getBigDecimal(map, "cost").divide(unitRatio));
				if(orderMap.containsKey(itemId)){
					map.putAll(MapUtil.rename(orderMap.get(itemId), "amt_ord.amount", "qty_ord.quantity"));
				}
			}
			purchOrderService.modifyMerchItemCostAndPri1(whseList);//修改cost
			//调用新增商品服务初始化数据
			List<Map<String,Object>> listAddOrder=createMerchTobacco(orderData,paramMap);
			//调用修改库存dao，修改库存信息
			Map<String,Object> od=updateWeshMerch(orderData,listAddOrder,paramMap);
			
			//将订单加入已入库订单表
			Map<String,Object> myOrderMap=new HashMap<String,Object>();
			myOrderMap.put("ORDER_ID", paramMap.get("orderId"));
			myOrderMap.put("MERCH_ID", paramMap.get("custId"));
			myOrderMap.put("NOTE", paramMap.get("note"));
			whseDao.insertOrder(myOrderMap);
			
			//调用添加采购单服务，插入采购单
			Map<String,Object> odOrder=new HashMap<String,Object>();
			orderData.remove("purch_order_line");
			od.putAll(orderData);
			
			String orderDate = MapUtil.getString(orderData, "order_date");
			/* 20140828 将入库日期仍然改为当天,避免无法日结的问题
			String nextDate = DateUtil.getNextDay(orderDate, 2);
			if(Integer.parseInt(nextDate) > Integer.parseInt(DateUtil.getToday())) {
				nextDate = DateUtil.getToday();
			}*/
			String nextDate = DateUtil.getToday();
			odOrder.put("order_date", orderDate);
			odOrder.put("voucher_date", nextDate);
			
			String odStr=JsonUtil.map2json(od);
			odOrder.put("json_param", odStr);
			odOrder.put("merch_id", paramMap.get("custId"));
			odOrder.put("order_id", paramMap.get("orderId"));
			odOrder.put("operator", paramMap.get("operator"));
			purchOrderService.insertPurchOrder(odOrder);
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 修改库存
	 * @author 徐虎彬
	 * @date 2014年3月24日
	 * @param orderData
	 * @param listAddOrder
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> updateWeshMerch(Map<String, Object> orderData,
			List<Map<String,Object>> listAddOrder,Map<String, String> paramMap)throws Exception{
		LOG.debug("CgtOrderServiceImpl updateWeshMerch orderData: " + orderData);
		LOG.debug("CgtOrderServiceImpl updateWeshMerch listAddOrder: " + listAddOrder);
		LOG.debug("CgtOrderServiceImpl updateWeshMerch paramMap: " + paramMap);
		Map<String,Object> od=new HashMap<String,Object>();
		od.put("merch_id", paramMap.get("custId"));
		od.put("whse_date", DateUtil.getToday());
		List<Map<String,Object>> odList=(List<Map<String,Object>>)orderData.get("purch_order_line");
		for(int i=0;i<odList.size();i++){
			odList.get(i).put("qty_add","0");
			for(int j=0;j<listAddOrder.size();j++){
				String odListItemId=odList.get(i).get("item_id").toString().trim();
				String listAddOrderItemId=listAddOrder.get(j).get("item_id").toString().trim();
				if(odListItemId.equals(listAddOrderItemId)){
					double qty_ord=Double.valueOf(odList.get(i).get("qty_ord").toString().trim());
					double unit_ratio=Double.valueOf(listAddOrder.get(j).get("unit_ratio").toString().trim());
					double qty_add_sum=qty_ord*unit_ratio;
					odList.get(i).put("qty_add", qty_add_sum);
					odList.get(i).put("quantity",qty_ord);
					odList.get(i).put("amount",odList.get(i).get("amt_ord"));
					odList.get(i).putAll(listAddOrder.get(j));
					odList.get(i).putAll(((List<Map<String,Object>>)odList.get(i).get("list")).get(0));
					odList.get(i).put("big_bar", listAddOrder.get(j).get("BIG_BAR"));
					odList.get(i).put("big_unit_name", listAddOrder.get(j).get("BIG_UNIT_NAME"));
					odList.get(i).put("unit_ratio", listAddOrder.get(j).get("UNIT_RATIO"));
					odList.get(i).remove("list");
					break;
				}
			}
			
		}
		List<Map<String,Object>> dataOdList= new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map:odList){
			if(map.containsKey("amount")){
				dataOdList.add(map);
			}
		}
		od.put("list", dataOdList);
		//调用修改库存dao，修改库存信息
		whseDao.updateWhseMerch(od);
		return od;
	}
	
	
	public Map<String,Object> orderPayment(Map<String, String> paramMap)throws Exception{
		LOG.debug("CgtOrderServiceImpl orderPayment paramMap: " + paramMap);
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/getOrderDetail";
		String json = HttpUtil.post(cgtOrderUrl, paramMap);
//		String json="{'result':{'supplier_id':'10370101','born_date':'20140208','crt_date':'20140208','status':'01','com_id':'10370101','slsman_id':'103701010129','order_id':'JN0000343845','cust_id':'1037010508807','voucher_date':'','qty_purch_total':'55','pmt_status':'02','flag':'1','amt_purch_total':'0.05','dpt_sale_id':'01010300','co_num':'JN0000343845','order_date':'20140208','purch_order_line':[{'pri1':'54.00','qty_ord':'1','item_name':'双喜(软国际)','item_id':'6901028001472','amt_ord':'54.00','pri_wsale':'57','qty_rsn':'1','qty_req':'9'},{'pri1':'80.00','qty_ord':'4','item_name':'娇子(X)','item_id':'6901028024976','amt_ord':'320.00','pri_wsale':'56','qty_rsn':'50','qty_req':'4'},{'pri1':'72.00','qty_ord':'4','item_name':'白沙(8mg绿和)','item_id':'6901028069823','amt_ord':'288.00','pri_wsale':'80','qty_rsn':'50','qty_req':'9'},{'pri1':'24.00','qty_ord':'2','item_name':'黄山松(5支)','item_id':'6901028124133','amt_ord':'48.00','pri_wsale':'50','qty_rsn':'50','qty_req':'9'},{'pri1':'45.00','qty_ord':'1','item_name':'七匹狼(豪情)','item_id':'6901028138369','amt_ord':'45.00','pri_wsale':'60','qty_rsn':'1','qty_req':'9'},{'pri1':'22.50','qty_ord':'1','item_name':'哈德门(软)','item_id':'6901028149242','amt_ord':'22.50','pri_wsale':'30','qty_rsn':'1','qty_req':'9'},{'pri1':'81.00','qty_ord':'8','item_name':'泰山(白将军)','item_id':'6901028151634','amt_ord':'648.00','pri_wsale':'90','qty_rsn':'8','qty_req':'9'},{'pri1':'63.00','qty_ord':'6','item_name':'泰山(红将)','item_id':'6901028153201','amt_ord':'378.00','pri_wsale':'70','qty_rsn':'6','qty_req':'9'},{'pri1':'132.00','qty_ord':'5','item_name':'泰山(大鸡)','item_id':'6901028153898','amt_ord':'660.00','pri_wsale':'150','qty_rsn':'5','qty_req':'29'},{'pri1':'97.00','qty_ord':'3','item_name':'泰山(沂蒙)','item_id':'6901028157834','amt_ord':'291.00','pri_wsale':'100','qty_rsn':'10','qty_req':'3'},{'pri1':'54.00','qty_ord':'10','item_name':'泰山(硬红八喜)','item_id':'6901028159548','amt_ord':'540.00','pri_wsale':'60','qty_rsn':'10','qty_req':'90'},{'pri1':'22.50','qty_ord':'2','item_name':'红金龙(硬喜)','item_id':'6901028184793','amt_ord':'45.00','pri_wsale':'30','qty_rsn':'2','qty_req':'9'},{'pri1':'45.00','qty_ord':'3','item_name':'钻石(软红)','item_id':'6901028250641','amt_ord':'135.00','pri_wsale':'56','qty_rsn':'3','qty_req':'9'},{'pri1':'97.00','qty_ord':'1','item_name':'南京(红)','item_id':'6901028300063','amt_ord':'97.00','pri_wsale':'110','qty_rsn':'1','qty_req':'9'},{'pri1':'122.00','qty_ord':'3','item_name':'南京(佳品)','item_id':'6901028300087','amt_ord':'366.00','pri_wsale':'130','qty_rsn':'3','qty_req':'9'},{'pri1':'63.00','qty_ord':'1','item_name':'红塔山(软经典)','item_id':'6901028315012','amt_ord':'63.00','pri_wsale':'70','qty_rsn':'1','qty_req':'9'}]},'code':'0000','msg':'请求成功'}";
		Map<String, Object> orderMap = (Map<String, Object>)JsonUtil.json2Map(json).get("result");
		
		
		Map<String,String> orderData=new HashMap<String,String>();
		Map<String,Object> infoData = null;
		if(orderMap!=null){
			double money=MapUtil.getDouble(orderMap, "amt_purch_total", 0);
			if(money == 0) {
				throw new RuntimeException("资金归集失败，支付金额为0");
			}
			int thisMoney=(int)(money*100);
			StringBuffer fieldMoney=new StringBuffer();
			fieldMoney.append(thisMoney);
			int sum=12-fieldMoney.toString().trim().length();
			StringBuffer s=new StringBuffer();
			for(int i=0;i<sum;i++){
				s.append("0");
			}
			fieldMoney.insert(0,s);
			orderData.put("field4",fieldMoney.toString().trim());//交易金额12位单位(分)不足前补零
			orderData.put("fieldZMID",paramMap.get("lice_id").trim());//烟草专卖证号
//			orderData.put("fieldZMID","370102107633");
			orderData.put("fieldOrderID",orderMap.get("co_num").toString().trim());//订单编号
			orderData.put("orderDate",orderMap.get("order_date").toString().trim());//订单日期
			orderData.put("fieldChannel","02");//渠道编号，协议代扣
			String dataOrder=JsonUtil.map2json(orderData);
			LOG.debug("调用资金归集：url=" + RetailConfig.getCollectServerUrl() + "/rest/resource/tobacco/2002222" + ", params=" + dataOrder);
			String dataJson = ServiceHttpUtil.post(RetailConfig.getCollectServerUrl() + "/rest/resource/tobacco/2002222", dataOrder);
			LOG.debug("调用资金归集获取到数据：" + dataJson);

			infoData=JsonUtil.json2Map(dataJson);
			if(infoData != null) {
				if("00".equals(infoData.get("field39"))) {
					return infoData;
				} else {
					throw new RuntimeException(MapUtil.getString(infoData, "msg"));
				}
			} else {
				throw new RuntimeException("返回的数据为空");
			}
		} else {
			throw new RuntimeException("未查询到订单信息");
		}
	}
	
	/**
	 * 调用新增商品服务初始化数据
	 * @author 徐虎彬
	 * @date 2014年3月15日
	 * @param orderData
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> createMerchTobacco(Map<String, Object> orderData,
			Map<String, String> paramMap)throws Exception{
		LOG.debug("CgtOrderServiceImpl createMerchTobacco paramMap: " + paramMap);
		//解析数据
		Map<String,Object> thisOrderMap=new HashMap<String,Object>();
		thisOrderMap.put("supplier_id", orderData.get("supplier_id").toString());
		StringBuffer sb=new StringBuffer();
		for(Map<String, Object> map:(List<Map<String,Object>>)(orderData.get("purch_order_line"))){
			sb.append(",'"+map.get("item_id").toString().trim()+"'");
		}
		thisOrderMap.put("item_id",sb.toString().substring(1));
		List<Map<String,Object>> tobaccoItemList = tobaccoSupplierDao.selectItem(thisOrderMap);
		List<Map<String,Object>> listAddOrder=new ArrayList<Map<String,Object>>();
		
		//组装数据
		for(Map<String,Object> tobaccoItemMap : tobaccoItemList){
			List<Map<String,Object>> itemUnitList = new ArrayList<Map<String,Object>>();
			tobaccoItemMap.remove("describe");
			tobaccoItemMap.remove("subjectcolor");
			tobaccoItemMap.remove("tiptype");
			tobaccoItemMap.remove("tarcontent");
			tobaccoItemMap.remove("co");
			tobaccoItemMap.remove("pack");
			tobaccoItemMap.remove("tipcolor");
			BigDecimal priWsale = new BigDecimal(tobaccoItemMap.get("pri_wsale").toString());//成本价-条
			BigDecimal priDrtl = new BigDecimal(tobaccoItemMap.get("pri_drtl").toString());//销售价-条
			BigDecimal unitRatio = new BigDecimal(tobaccoItemMap.get("unit_ratio").toString());
			tobaccoItemMap.put("merch_id", paramMap.get("custId"));
			tobaccoItemMap.put("cost", priWsale.divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
			tobaccoItemMap.put("pri1", priWsale.divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
			tobaccoItemMap.put("pri2", priWsale.divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
			tobaccoItemMap.put("pri4", priDrtl.divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
			tobaccoItemMap.put("unit_name", tobaccoItemMap.get("item_unit_name"));
			if(tobaccoItemMap.get("item_kind_id")==null) tobaccoItemMap.put("item_kind_id", "01");
			if(tobaccoItemMap.get("item_bar")==null || tobaccoItemMap.get("item_id").equals(tobaccoItemMap.get("item_bar"))) {
				tobaccoItemMap.put("item_bar", tobaccoItemMap.get("item_id"));
				Map<String,Object> unitMap = new HashMap<String,Object>();
				unitMap.put("seq_id", IDUtil.getId());
				unitMap.put("merch_id", paramMap.get("custId"));
				unitMap.put("item_id", tobaccoItemMap.get("item_id"));
				unitMap.put("item_bar", tobaccoItemMap.get("item_id"));
				unitMap.put("item_unit_name", tobaccoItemMap.get("item_unit_name"));
				unitMap.put("big_bar", tobaccoItemMap.get("item_id"));
				unitMap.put("big_unit_name", tobaccoItemMap.get("item_unit_name"));
				unitMap.put("unit_ratio", 1);
				unitMap.put("pri4", priDrtl.divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
				itemUnitList.add(unitMap);
			} else {
				Map<String,Object> unitMap = new HashMap<String,Object>();
				Map<String,Object> bigUnitMap = new HashMap<String,Object>();
				unitMap.put("seq_id", IDUtil.getId());
				unitMap.put("merch_id", paramMap.get("custId"));
				unitMap.put("item_id", tobaccoItemMap.get("item_id"));
				unitMap.put("item_bar", tobaccoItemMap.get("item_bar"));
				unitMap.put("item_unit_name", tobaccoItemMap.get("item_unit_name"));
				unitMap.put("big_bar", tobaccoItemMap.get("item_bar"));
				unitMap.put("big_unit_name", tobaccoItemMap.get("item_unit_name"));
				unitMap.put("unit_ratio", 1);
				unitMap.put("pri4", priDrtl.divide(unitRatio, 5, BigDecimal.ROUND_HALF_UP));
				bigUnitMap.put("seq_id", IDUtil.getId());
				bigUnitMap.put("merch_id", paramMap.get("custId"));
				bigUnitMap.put("item_id", tobaccoItemMap.get("item_id"));
				bigUnitMap.put("item_bar", tobaccoItemMap.get("item_bar"));
				bigUnitMap.put("item_unit_name", tobaccoItemMap.get("item_unit_name"));
				bigUnitMap.put("big_bar", tobaccoItemMap.get("big_bar"));
				bigUnitMap.put("big_unit_name", tobaccoItemMap.get("big_unit_name"));
				bigUnitMap.put("unit_ratio", unitRatio);
				bigUnitMap.put("pri4", priDrtl);
				itemUnitList.add(unitMap);
				itemUnitList.add(bigUnitMap);
			}
			tobaccoItemMap.put("list", itemUnitList);
			listAddOrder.add(tobaccoItemMap);
		}
		
		//调用新增商品服务初始化数据
		itemService.createMerchTobaccoItem(listAddOrder);
		return listAddOrder;
	}
	
	
	@Override
	public List<Map<String, Object>> getTobaccoLmt(Map<String, String> paramMap) throws Exception{
		LOG.debug("CgtOrderServiceImpl getTobaccoLmt paramMap: " + paramMap);
		//连接服务器获取数据
		String cgtlmtUrl =null;
		String json=null;
		if("hand".equals(MapUtil.getString(paramMap, "snychType", ""))){
			cgtlmtUrl = RetailConfig.getTobaccoServer() + "cgtorder/getSynItemLimits";//手动获取限量
//			json="{'result':[{'qty_lmt':'50','item_id':'6901028135269'}],'code':'0000','msg':'获取商品限量成功！'}";
		}else{
			cgtlmtUrl = RetailConfig.getTobaccoServer() + "cgtorder/getItemLimits";//自动获取限量
//			Map<String, String> param = new HashMap<String, String>();
//			param.put("custId", paramMap.get("refId"));
//			json = HttpUtil.post(cgtlmtUrl, param);
		}
		Map<String, String> param = new HashMap<String, String>();
		param.put("custId", paramMap.get("refId"));
		json = HttpUtil.post(cgtlmtUrl, param);
		Map result = JsonUtil.json2Map(json);
		
		//解析数据
		String code = (String) result.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			return (List<Map<String, Object>>) result.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) result.get("msg"));
		}
	}
	
	@Override
	public List<Map<String, Object>> getTobaccoItemLmt(Map<String, Object> paramMap) throws Exception{
		LOG.debug("CgtOrderServiceImpl getTobaccoItemLmt paramMap: " + paramMap);
		//连接服务器获取数据
		String cgtlmtUrl = RetailConfig.getTobaccoServer() + "cgtorder/getCustItemLmt";// 获取单品限量
		//String cgtlmtUrl = "http://202.110.222.207:7082/tobaccoserver/cgtorder/getCustItemLmt";// 获取单品限量
		String json=null;

		Map<String, Object> reqparam = new HashMap<String, Object>();
		Map<String, Object> param = new HashMap<String, Object>();
		
		param.put("cust_id", paramMap.get("cust_id"));
		param.put("item_list", paramMap.get("item_list"));
		
		reqparam.put("cust_id", paramMap.get("cust_id"));
		reqparam.put("item_id", paramMap.get("item_id"));
		String paramString = JsonUtil.map2json(param);
		paramString = paramString.replaceAll("\"\\[", "\\[");
		paramString = paramString.replaceAll("\\]\"", "\\]");
		paramString = paramString.replaceAll("\\\\\"", "\"");
		reqparam.put("params", paramString);
		
		// 传limitParams作为参数
		json = HttpUtil.post(cgtlmtUrl, reqparam);
		Map result = JsonUtil.json2Map(json);
		
		//解析数据
		String code = (String) result.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			Object r = result.get("result");
			List<Map<String, Object>> list = null;
			if(r instanceof Map) {
				list = new ArrayList<Map<String,Object>>();
				list.add((Map<String,Object>) r);
				
				List ids = JsonUtil.json2List(MapUtil.getString(paramMap, "item_list"));
				if(ids != null && ids.size() > 1) {
					param.remove("item_list");
					for (int i = 1; i < ids.size(); i++) {
						param.put("item_id", (String)ids.get(i));
						
						json = HttpUtil.post(cgtlmtUrl, param);
						result = JsonUtil.json2Map(json);
						
						code = (String) result.get("code");
						if(code!=null && Constants.SUCCESS.equals(code)) {
							list.add((Map<String,Object>)result.get("result"));
						}
					}
				}
				
			} else {
				list = (List<Map<String, Object>>) r;
			}
			return list;
		} else { //返回码异常
			throw new RuntimeException((String) result.get("msg"));
		}
	}
	
	@Override
	public void updateTobaccoData() throws Exception {
		LOG.debug("CgtOrderServiceImpl updateTobaccoData " );
		GetUrlData getData=new GetUrlData();
		List<Map<String,Object>> allItemList=new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> allItem=tobaccoSupplierDao.getAllItemList();
		for(Map<String,Object> item:allItem){
			Map<String,Object> map=getData.cutOutData(item.get("item_id").toString());
			map.put("supplier_id", item.get("supplier_id"));
			map.put("item_id", item.get("item_id"));
			map.put("item_name", item.get("item_name"));
			allItemList.add(map);
		}
		tobaccoSupplierDao.updateItems(allItemList,"str");
		
	}
	
	@Override
	public void getTobaccoItemListToBaseMerchItem(Map<String, String> paramMap) throws Exception {
		LOG.debug("CgtOrderServiceImpl getTobaccoItemListToBaseMerchItem paramMap:"+paramMap );
		//连接服务器获取数据
		String cgtlmtUrl = RetailConfig.getTobaccoServer() + "cgtorder/getCustItems";
		Map<String, String> param = new HashMap<String, String>();
		param.put("custId", paramMap.get("refId"));
		String json = HttpUtil.post(cgtlmtUrl, param);
		Map<String, Object> orderMap = JsonUtil.json2Map(json);
		
		if("0000".equals(orderMap.get("code"))) {
			List<Map<String,Object>> listData=(List<Map<String,Object>>)orderMap.get("result");
			if(listData!=null&&listData.size()>0){
				Map<String, Object> orderData=new HashMap<String,Object>();
				orderData.put("supplier_id", paramMap.get("comId"));
				orderData.put("purch_order_line", listData);
				
				createMerchTobacco(orderData,paramMap);
			} else {
				throw new Exception("未获取到用户订购数据，列表为空");
			}
		} else {
			throw new Exception("未获取到用户订购数据：" + orderMap.get("msg"));
		}
	}





	@Override
	public Map<String, String> orderPayByPOS(Map<String, String> payMeg, Map<String,String> serachOrderParm) {
		LOG.debug("CgtOrderServiceImpl orderPayByPOS payMeg:"+payMeg );
		LOG.debug("CgtOrderServiceImpl orderPayByPOS serachOrderParm:"+serachOrderParm );
		//连接服务器获取数据
		String cgtOrderUrl = RetailConfig.getTobaccoServer() + "cgtorder/getOrderDetail";
		String payURL = RetailConfig.getCollectServerUrl()+"/rest/resource/tobacco/2002222";
		
		
		String json = HttpUtil.post(cgtOrderUrl, serachOrderParm);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> orderInfoMap= (Map<String, Object>)JsonUtil.json2Map(json).get("result");
		if(orderInfoMap!=null){
			double money=MapUtil.getDouble(orderInfoMap, "amt_purch_total", 0);
			if(money == 0) {
				throw new RuntimeException("支付金额为0");
			}
			int thisMoney=(int)(money*100);
			StringBuffer fieldMoney=new StringBuffer();
			fieldMoney.append(thisMoney);
			int sum=12-fieldMoney.toString().trim().length();
			StringBuffer s=new StringBuffer();
			for(int i=0;i<sum;i++){
				s.append("0");
			}
			fieldMoney.insert(0,s);
			payMeg.put("field4", fieldMoney.toString().trim());
			payMeg.put("fieldOrderID", orderInfoMap.get("co_num").toString().trim());
			payMeg.put("orderDate", orderInfoMap.get("order_date").toString().trim());
			payMeg.put("fieldZMID",serachOrderParm.get("lice_id").trim());//烟草专卖证号
			try {
				String reqPayMsg = this.fittingPayMes(payMeg);
				LOG.debug("刷卡资金归集：url=" + payURL + ", params=" + reqPayMsg);
				String payResponseData = ServiceHttpUtil.post(payURL, reqPayMsg);
				//String payResponseData = "{\"field60\":\"22000074000201000\",\"field62\":\"\",\"remark\":\"\",\"field39\":\"00\",\"fieldOutBankName\":\"山东省农村信用社联合社\",\"field4\":\"000000822000\",\"field44\":\"\",\"field25\":\"00\",\"field3\":\"460000\",\"field2\":\"6215210101724188\",\"field42\":\"409489853319003\",\"field49\":\"156\",\"field7\":\"20140828140002\",\"fieldMAC\":\"4138353837434335\",\"field41\":\"90000003\",\"fieldIssuerCode\":\"14144500\",\"fieldMAB\":\"fieldTrancode;messtype;field2;field3;field4;field11;field12;field13;field39;field41;field42;field102;field103;fieldZMID\",\"fieldTrancode\":\"2002222\",\"fieldOrderID\":\"JN0000704500\",\"fieldMessage\":\"交易成功\",\"messtype\":\"0210\",\"fieldInBankName\":\"山东济南润丰农村合作银行佛山支行\",\"field28\":\"000000000000\",\"field54\":\"\",\"field13\":\"0828\",\"field15\":\"0828\",\"field12\":\"140002\",\"fieldZMID\":\"370102106350\",\"field11\":\"000000\",\"field37\":\"547881\",\"field103\":\"90104045020100056217\",\"fieldPayee\":\"\",\"field102\":\"6215210101724188\"}";
				LOG.debug("刷卡资金归集获取到数据：" + payResponseData);
				@SuppressWarnings("unchecked")
				Map<String,String> repInfo = JsonUtil.json2Map(payResponseData);
				if(repInfo != null) {
					if("00".equals(repInfo.get("field39"))) {
						return repInfo;
					} else {
						throw new RuntimeException(repInfo.get("fieldMessage"));
					}
				} else {
					throw new RuntimeException("返回的数据为空");
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		} else {
			throw new RuntimeException("未查询到订单信息");
		}
	}
	
	private String  fittingPayMes(Map<String, String> payMeg){
		LOG.debug("CgtOrderServiceImpl fittingPayMes payMeg:"+payMeg );
		Map<String,String> map = new HashMap<String, String>();
		map.put("fieldTrancode", "2002222");
		map.put("field2", payMeg.get("field2"));
		map.put("field3", "460000");
		map.put("field4", payMeg.get("field4"));
		//map.put("field22", "021");
		//map.put("field23", "000");
		map.put("field25", "00");
		map.put("field26", "06");
		map.put("field41", payMeg.get("field41"));
		map.put("field42", payMeg.get("field42"));
		map.put("field49", "156");
		map.put("field53", "2600000000000000");
		map.put("field60", "22000074000201000");
		map.put("fieldChannel", "03");
		map.put("field52",payMeg.get("field52"));
		map.put("field35", payMeg.get("track_2_data"));
		map.put("field36",payMeg.get("track_3_data"));
		map.put("fieldMAB", payMeg.get("fieldMAB"));
		map.put("fieldMAC", payMeg.get("fieldMAC"));
		map.put("fieldAddress", "");
		map.put("field62", "");
		map.put("field7",DateUtil.getCurrentTime());
		map.put("ReaderID", "");
		map.put("field55", payMeg.get("field55"));
		map.put("termMobile", "");
		map.put("field13", DateUtil.getToday().substring(4, 8));
		map.put("fieldOrderID",payMeg.get("fieldOrderID"));//订单编号
		map.put("fieldZMID",payMeg.get("fieldZMID"));//烟草专卖证号
		map.put("orderDate",payMeg.get("orderDate"));//订单日期
		map.put("filedRemark","山东烟草资金归集");
		map.put("fieldIssuerCode",payMeg.get("fieldIssuerCode"));//收款行行号
		map.put("field12",DateUtil.getCurrentTime().substring(8,14));//受卡方所在地时间
		map.put("field103",payMeg.get("field103"));//账户标志1
		map.put("field102",payMeg.get("field2"));//账户标志2
		//区分IC卡磁条卡
		String track_2_data = payMeg.get("track_2_data");
		if(this.isIcCard(track_2_data)){
			map.put("field23", "001");
			map.put("field22", "051");
		}
		else{
			map.put("field23", "000");
			map.put("field22", "021");
		}
		
		return JsonUtil.map2json(map);
	}
	/*
	 * 根据二磁判断IC卡、磁条卡
	 */
	private  boolean isIcCard(String track2data){
		LOG.debug("CgtOrderServiceImpl isIcCard track2data:"+track2data );
 		
 		if("".equals(track2data)||track2data==null){
 			return false;
 		}
 		if((!track2data.contains("="))&&(!track2data.contains("D"))){
 			return false;
 		}
 		String temp[]=null;
 		String key ="";
 		if(track2data.contains("=")){
 			temp=track2data.split("=");
 			key = temp[1].substring(4,5);
 		}else if(track2data.contains("D")){
 			temp=track2data.split("D");
 			key = temp[1].substring(4,5);
 		}else{
 			return false;
 		}
 		return  "2".equals(key)||"6".equals(key);
 	}
	
	@Override
	public Map<String, Object> searchTobaccoItem(Map<String, Object> paramMap) throws Exception {
		LOG.debug("CgtOrderServiceImpl searchTobaccoItem paramMap: " + paramMap);
		String itemId = MapUtil.getString(paramMap, "item_id");
		List<Map<String, Object>> items = tobaccoSupplierDao.getTobaccoItem(itemId);
		return items.isEmpty() ? new HashMap<String, Object>() : items.get(0);
	}

}
