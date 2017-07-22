package com.ryx.social.retail.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.login.tool.SSOConfig;
import com.ryx.social.retail.dao.IFileDao;
import com.ryx.social.retail.dao.IMerchDao;
import com.ryx.social.retail.service.IMerchService;
import com.ryx.social.retail.service.IReturnOrderService;
import com.ryx.social.retail.service.ISaleService;
import com.ryx.social.retail.util.MD5Util;
import com.ryx.social.retail.util.RetailConfig;

@Service
public class MerchServiceImpl implements IMerchService{
	
	private Logger LOG = LoggerFactory.getLogger(MerchServiceImpl.class);
	
	@Resource
	private IMerchDao merchDao;
	@Resource
	private IFileDao merchFileDao;
	@Resource
	private ISaleService saleService;
	@Resource
	private IReturnOrderService returnOrderService;
	
	@Override
	public Map getMerchInfo(String merchId) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> merchMap = new HashMap<String, Object>();
		merchMap.putAll(merchDao.getMerchInfo(merchId));
		String orderType = MapUtil.getString(merchMap, "order_type", "0");
		if (!orderType.equals("1")) {
			merchMap.put("order_type", "0");
		}
		return merchMap;
	}
	//查询商户信息
	@Override
	public List<Map<String, Object>> selectMerch(Map<String, Object> merchMap)throws Exception{
		 LOG .debug("MerchServiceImpl selectMerch merchMap:"+merchMap);
		return merchDao.selectMerch(merchMap);
	}
	@Override
	public Map getUserInfo(String userId) throws Exception {
		// TODO Auto-generated method stub
		return merchDao.getUserInfo(userId);
	}
	
	@Override
	public List<Map<String, Object>> searchMerchJoinMerchFile(Map<String, Object> merchJoinMerchFileParam) throws Exception {
		 LOG .debug("MerchServiceImpl searchMerchJoinMerchFile merchJoinMerchFileParam: " + merchJoinMerchFileParam);
		List<Map<String, Object>> merchList =  merchDao.selectMerch(merchJoinMerchFileParam);
		List<Map<String, Object>> fileList = new ArrayList<Map<String, Object>>();
		Map<String, Object> fileParam = new HashMap<String, Object>();
		// 只查询店铺图片
		fileParam.put("merch_id", merchJoinMerchFileParam.get("merch_id"));
		fileParam.put("file_purpose", "01,02");
		for(Map<String, Object> merchMap : merchList) {
			List<Map<String, Object>> merchFileList = merchFileDao.selectMerchFile(merchJoinMerchFileParam);
			for(Map<String, Object> merchFileMap : merchFileList) {
				Map<String, Object> fileMap = new HashMap<String, Object>();
				fileMap.put("file_id", merchFileMap.get("file_id"));
				fileMap.put("file_location", merchFileMap.get("file_location"));
				fileList.add(fileMap);
			}
			merchMap.put("list", fileList);
		}
		return merchList;
	}

	@Override
	public void updateMerchInfo(Map merchInfo) throws Exception {
		// TODO Auto-generated method stub
		merchDao.updateMerchInfo(merchInfo);
	}

	//修改基本信息（经纬度，营业时间，经营范围）
	@Override
	public void updateMerchBasicInfo(Map<String,Object> merchInfo) throws Exception {
		LOG .debug("MerchServiceImpl updateMerchBasicInfo merchInfo: " + merchInfo);
		merchDao.updateMerchBasicInfo(merchInfo);
		
		if (merchInfo.containsKey("lat") && merchInfo.containsKey("lng") && merchInfo.containsKey("merch_id") && merchInfo.containsKey("lice_id")) {
			
			Map<String, Object> userMap = MapUtil.rename(merchInfo, "merch_id.id", "lice_id", "lng.longitude", "lat.latitude");
			
			Map<String, Object> upLatAndLngParams = new HashMap<String, Object>();
			upLatAndLngParams.put("params", JsonUtil.map2json(userMap));
			String  upLatAndLngUrl = "http://192.168.0.3:8818/ucenter/ucenter/tobacco/merch/update";
			
			String updateMerchLatLngJson = null;
			try {
				updateMerchLatLngJson = HttpUtil.post(upLatAndLngUrl, upLatAndLngParams);
			} catch (Exception e) {
				LOG .debug("MerchServiceImpl updateMerchBasicInfo 向用户中心提交经纬度数据失败 ");
			}
			if (updateMerchLatLngJson == null) {
				LOG .debug("MerchServiceImpl updateMerchBasicInfo 向用户中心提交经纬度数据失败 ");
				return ;
			}
			try {
				Map<String, Object> josn = JsonUtil.json2Map(updateMerchLatLngJson);
				String code = MapUtil.getString(josn, "code");
				if (Constants.CODE_SUCCESS.equals(code)) {
					LOG .debug("MerchServiceImpl updateMerchBasicInfo 向用户中心提交经纬度数据成功");
				} else {
					LOG .debug("MerchServiceImpl updateMerchBasicInfo 向用户中心提交经纬度数据失败, code:"+code);
				}
			} catch (Exception e) {
				LOG .debug("MerchServiceImpl updateMerchBasicInfo 向用户中心提交经纬度数据失败, 解析json失败: "+updateMerchLatLngJson);
			}
		}
	}
	
	@Override
	public void updateAndInsertMerchTicket(Map<String, Object> merchTicket)
			throws Exception {
		List<Map<String,Object>> ticketList=merchDao.selectMerchTicket(merchTicket);
		if(ticketList!=null&&ticketList.size()>0){
			merchDao.updateMertchTicket(merchTicket);
		}else{
			merchDao.insertMertchTicket(merchTicket);
		}
	}

	@Override
	public Map<String, Object> selectMerchTicket(Map<String, Object> merchTicket)
			throws Exception {
		List<Map<String,Object>> ticketList=merchDao.selectMerchTicket(merchTicket);
		Map<String, Object> ticketMap=new HashMap<String,Object>();
		if(ticketList!=null&&ticketList.size()>0){
			ticketMap = ticketList.get(0);
		}else{
			ticketMap.put("TICKET_TYPE", merchTicket.get("ticket_type"));
			ticketMap.put("WELCOME_WORD", merchTicket.get("merch_name")+"欢迎您！");
			ticketMap.put("NOTE", "");
			ticketMap.put("PHONE", merchTicket.get("phone"));
			ticketMap.put("NUM", "1");
			ticketMap.put("TICKET_DATE", "");
		}
		return ticketMap;
	}
	
	//交接班
	@Override
	public Map<String, Object> dutyShift(Map<String, Object> paramsMap)throws Exception{
		String userCode=MapUtil.getString(paramsMap, "user_code", null);
		String roleId = MapUtil.getString(paramsMap, "role_id");
		if(userCode==null){
			return null;
		}
		
		String dateAndTime=DateUtil.getCurrentTime();
		String today=DateUtil.getToday();//当前日期
		String newTime=dateAndTime.substring(8);//当前时间
		String oldTime=null;//上一次交接班时间
		String oldDate=null;//上一次交接班日期
		
		String merchId=MapUtil.get(paramsMap, "merch_id", null);
		//查询交接班map
		Map<String, Object> searchShiftParamMap=new HashMap<String, Object>();
		searchShiftParamMap.put("merch_id", merchId);
		searchShiftParamMap.put("page_size", "-1");
		searchShiftParamMap.put("page_index", "-1");
		searchShiftParamMap.put("user_code", userCode);
		searchShiftParamMap.put("role_id", roleId);
		//查询交接班信息
		List<Map<String, Object>> mySearchShiftList=this.searchDutyShift(searchShiftParamMap);
		if(mySearchShiftList.isEmpty()){
			oldTime="000000";
			oldDate=today;
		}else{
			Map<String, Object> lastSearchShift= mySearchShiftList.get(0);
			oldTime=MapUtil.getString(lastSearchShift, "shift_time",newTime);
			oldDate=MapUtil.getString(lastSearchShift, "shift_date",today);
		}
		if(DateUtil.differ(oldDate, today)>=1){//两个时间的差
//			oldDate=DateUtil.getPreviousDay(today, 1);//昨天
			oldDate=today;//昨天
			oldTime="000000";
		}
		
		
		Map<String, Object> searchSaleParamMap=new HashMap<String, Object>();
//		searchSaleParamMap.put("order_date", oldDate);
		searchSaleParamMap.put("start_date", oldDate);
		searchSaleParamMap.put("end_date", today);
		searchSaleParamMap.put("merch_id", merchId);
		searchSaleParamMap.put("start_time", oldTime);
		searchSaleParamMap.put("end_time", newTime);
		searchSaleParamMap.put("status", "03");
		searchSaleParamMap.put("page_size", "-1");
		searchSaleParamMap.put("page_index", "-1");
		searchSaleParamMap.put("user_code", userCode);
		searchSaleParamMap.put("role_id", "3"); // 交接班的时候只能看到自己的销售情况所以role_id强制为3 by 梁凯 2014-7-11
		//查询销售单
		List<Map<String, Object>> mySaleOrderList=saleService.selectSaleOrder(searchSaleParamMap);
		LOG.debug("mySaleOrderList:"+mySaleOrderList);
		
		Map<String, Object> searchReturnParamMap=new HashMap<String, Object>();
		searchReturnParamMap.put("merch_id", merchId);
//		searchReturnParamMap.put("return_order_date",today );
		searchReturnParamMap.put("start_date",oldDate );
		searchReturnParamMap.put("end_date",today );
		searchReturnParamMap.put("end_time", newTime);
		searchReturnParamMap.put("return_status", "03");
		searchReturnParamMap.put("start_time", oldTime);
		searchReturnParamMap.put("page_size", "-1");
		searchReturnParamMap.put("page_index", "-1");
		searchReturnParamMap.put("user_code", userCode);
		searchReturnParamMap.put("role_id", "3"); // 交接班的时候只能看到自己的退货情况所以role_id强制为3 by 梁凯 2014-7-11
		//查询退货单
		List<Map<String, Object>> myReturnOrderList=returnOrderService.searchMerchReturnOrder(searchReturnParamMap);
		LOG.debug("myReturnOrderList:"+myReturnOrderList);
		
		BigDecimal sumAmtSaleCard=BigDecimal.ZERO;//总应收
		BigDecimal sumAmtSaleCash=BigDecimal.ZERO;//总应收
		BigDecimal sumAmtLoss=BigDecimal.ZERO;//总抹零
		BigDecimal sumAmtProfit=BigDecimal.ZERO;//总销售利润
		
		BigDecimal sumAmtReturn=BigDecimal.ZERO;//总退货
		BigDecimal sumAmtLossRt=BigDecimal.ZERO;//总退货利润
		String payType=null;
		for (Map<String, Object> map : mySaleOrderList) {
			payType=MapUtil.getString(map, "pay_type", "1").trim();
			LOG.debug("payType:"+payType);
			if (payType.equals("1")) {
				sumAmtSaleCash = sumAmtSaleCash.add(MapUtil.getBigDecimal(map, "amtys_ord_total"));
			}else {
				sumAmtSaleCard = sumAmtSaleCard.add(MapUtil.getBigDecimal(map, "amtys_ord_total"));
			}
			
			sumAmtLoss=sumAmtLoss.add(MapUtil.getBigDecimal(map, "amt_ord_loss"));
			sumAmtProfit=sumAmtProfit.add(MapUtil.getBigDecimal(map, "amt_ord_profit"));
		}
//		sumAmtSaleCash=sumAmtSaleCash.subtract(sumAmtLoss);
		for (Map<String, Object> map : myReturnOrderList) {
			sumAmtReturn=sumAmtReturn.add(MapUtil.getBigDecimal(map, "amt_return_total"));
			sumAmtLossRt=sumAmtLossRt.add(MapUtil.getBigDecimal(map, "amt_return_loss"));
		}
		sumAmtProfit=sumAmtProfit.subtract(BigDecimal.ZERO.subtract(sumAmtLossRt));
		sumAmtSaleCash = sumAmtSaleCash.subtract(sumAmtReturn);
		LOG.debug("amt_sale_card:"+sumAmtSaleCard);
		LOG.debug("amt_sale_cash:"+sumAmtSaleCash);
		LOG.debug("amt_return:"+sumAmtReturn);
		//插入交接班map
		Map<String, Object> insShiftParamMap=new HashMap<String, Object>();
//		insShiftParamMap.put("merch_id", merchId);
		insShiftParamMap.put("user_code", MapUtil.get(paramsMap, "user_code", null));
		insShiftParamMap.put("amt_sale_card", sumAmtSaleCard);
		insShiftParamMap.put("amt_sale_cash", sumAmtSaleCash);
		insShiftParamMap.put("amt_return", sumAmtReturn);
		insShiftParamMap.put("shift_date", today);
		insShiftParamMap.put("shift_time", newTime);
//		insShiftParamMap.put("amt_loss", sumAmtProfit);
//		merchDao.insertDutyShift(insShiftParamMap);
		insShiftParamMap.put("last_shift_time", oldTime);
		insShiftParamMap.put("last_shift_date", oldDate);
//		start_shift_date
		return insShiftParamMap;
	}
	@Override
	public Map<String, Object> insertDutyShift(Map<String, Object> paramsMap)throws Exception{
		String code = "0000";
		String msg ="请求成功";
		Map<String, Object> returnMap = new HashMap<String, Object>();
		LOG.debug("insertDutyShift paramsMap:"+paramsMap);
		
		Map<String, Object> insShiftParamMap=new HashMap<String, Object>();
		
		String merchId = MapUtil.get(paramsMap, "merch_id", "");
		String userCode = MapUtil.get(paramsMap, "user_code", "");
		String lastShiftTime = MapUtil.get(paramsMap, "last_shift_time", "");
		String lastShiftDate = MapUtil.get(paramsMap, "last_shift_date", "");
		BigDecimal amtSaleCard = MapUtil.getBigDecimal(paramsMap, "amt_sale_card");
		BigDecimal amtSaleCash = MapUtil.getBigDecimal(paramsMap, "amt_sale_cash");
		BigDecimal amtReturn  =MapUtil.getBigDecimal(paramsMap, "amt_return");
		
		String dateAndTime=DateUtil.getCurrentTime();
		String today=DateUtil.getToday();//当前日期
		String newTime=dateAndTime.substring(8);//当前时间
		
		insShiftParamMap.put("shift_date", today);
		insShiftParamMap.put("shift_time", newTime);
		insShiftParamMap.put("merch_id", merchId);
		insShiftParamMap.put("user_code", userCode);
		insShiftParamMap.put("amt_sale_card", amtSaleCard);
		insShiftParamMap.put("amt_sale_cash", amtSaleCash);
		insShiftParamMap.put("amt_return", amtReturn);
		insShiftParamMap.put("last_shift_date", lastShiftDate);
		insShiftParamMap.put("last_shift_time", lastShiftTime);
		
		merchDao.insertDutyShift(insShiftParamMap);
		returnMap.put("code", code);
		returnMap.put("msg", msg);		
		return returnMap;
	}
	
	@Override
	public List<Map<String, Object>> searchDutyShift(Map<String, Object> paramsMap)throws Exception{
		LOG.debug("searchDutyShift:"+paramsMap);
		
		return merchDao.searchDutyShift(paramsMap);
	}
	
	@Override
	public List<Map<String, Object>> getOnlineDuration(Map<String, Object> param) throws Exception {
		LOG.debug("MerchServiceImpl getOnlineDuration param : " + param);
		param = MapUtil.rename(param, "merch_id.cust_id", "floor_date.begindate", "ceiling_date.enddate");
		Map<String, Object> durationParam = new HashMap<String, Object>();
		durationParam.put("params", JsonUtil.map2json(param));
		LOG.debug(" = * = * = * = * = 获取终端在线时长参数 = * = * = * = * = " + durationParam);
		String durationString = HttpUtil.post(RetailConfig.getTobaccoServer() + "retail/getCustTimeLength", durationParam);
		LOG.debug(" = * = * = * = * = 获取终端在线时长返回数据 = * = * = * = * = " + durationString);
		try {
			Map<String, Object> durationResult = JsonUtil.json2Map(durationString);
			String code = MapUtil.getString(durationResult, "code");
			if(Constants.CODE_SUCCESS.equals(code)) {
				return MapUtil.get(durationResult, "result", Collections.EMPTY_LIST);
			} else {
				throw new RuntimeException("获取终端在线时长错误: " + MapUtil.getString(durationResult, "msg"));
			}
		} catch (Exception e) {
			LOG.debug(" = * = * = * = * = 获取终端在线时长返回数据异常 = * = * = * = * = ", e);
			throw new RuntimeException("获取终端在线时长返回数据异常: " + durationString);
		}
	}
	
	@Override
	public void updateMerchLoginTime(String tokenSecret, String merchID) {
		Map<String,String> map = new HashMap<String, String>();
		map.put("tokenSecret", tokenSecret);
		map.put("serviceFlag", SSOConfig.serviceFlag);
		map.put("merchID", merchID);
		String updatTimeUrl = RetailConfig.getLoginServer()+"/sso/updateMerchLoginTime";
		HttpUtil.post(updatTimeUrl, map);
		
	}

	@Override
	public void changePassword() throws Exception {
		Map<String, String> merchPassword = new HashMap<String, String>();
		merchPassword.put("370112107981", "0");
		merchPassword.put("370102111334", "0000");
		merchPassword.put("370125100603", "000000");
		merchPassword.put("370102109184", "0002");
		merchPassword.put("370103110462", "001573");
		merchPassword.put("370782107168", "001982");
		merchPassword.put("370103103321", "006851");
		merchPassword.put("370105104950", "0109");
		merchPassword.put("370113104192", "0209");
		merchPassword.put("370112105606", "029");
		merchPassword.put("370102110537", "0326");
		merchPassword.put("370103211339", "051027");
		merchPassword.put("370104104378", "0531");
		merchPassword.put("370112109303", "070222");
		merchPassword.put("370112106736", "070809");
		merchPassword.put("370104103334", "071005");
		merchPassword.put("370105104939", "071712");
		merchPassword.put("370105105162", "091002");
		merchPassword.put("370105106646", "1");
		merchPassword.put("370181207515", "1");
		merchPassword.put("370105106872", "1");
		merchPassword.put("370105106912", "1");
		merchPassword.put("370104104265", "1");
		merchPassword.put("370181108105", "1");
		merchPassword.put("370103104598", "1");
		merchPassword.put("370103110814", "1");
		merchPassword.put("370113105618", "1");
		merchPassword.put("370112108793", "1");
		merchPassword.put("370112106247", "1");
		merchPassword.put("370102100764", "1");
		merchPassword.put("370112108566", "1");
		merchPassword.put("370181104544", "1");
		merchPassword.put("370104103069", "1");
		merchPassword.put("370103106194", "1");
		merchPassword.put("370104104278", "1");
		merchPassword.put("370103108315", "1");
		merchPassword.put("370124103827", "1");
		merchPassword.put("370102109250", "1");
		merchPassword.put("370102109151", "1");
		merchPassword.put("370112106313", "1");
		merchPassword.put("370126100037", "100037");
		merchPassword.put("370103203552", "100100");
		merchPassword.put("370102110237", "100313");
		merchPassword.put("370103110266", "110110");
		merchPassword.put("370102110328", "110328");
		merchPassword.put("370112100017", "110911");
		merchPassword.put("370105206452", "111112");
		merchPassword.put("370103111520", "111520");
		merchPassword.put("370102109511", "116119");
		merchPassword.put("370105104632", "120998");
		merchPassword.put("370112106084", "121212");
		merchPassword.put("370103107963", "121600");
		merchPassword.put("370105105713", "122232");
		merchPassword.put("370103105707", "122512");
		merchPassword.put("370105105069", "123");
		merchPassword.put("370112105572", "123108");
		merchPassword.put("370181109279", "123321");
		merchPassword.put("370102110311", "123321");
		merchPassword.put("370112107467", "12344321");
		merchPassword.put("370102110964", "123456");
		merchPassword.put("370102111356", "123456");
		merchPassword.put("370105104307", "123456");
		merchPassword.put("370105102287", "123456");
		merchPassword.put("370105104124", "1234567");
		merchPassword.put("370112104760", "127312");
		merchPassword.put("370104104333", "138640");
		merchPassword.put("370112104436", "139640");
		merchPassword.put("370126105204", "139690");
		merchPassword.put("370112108577", "150678");
		merchPassword.put("370104104036", "1573");
		merchPassword.put("370103107793", "159808");
		merchPassword.put("370112107477", "167777");
		merchPassword.put("370102110694", "196989");
		merchPassword.put("370102108913", "197118");
		merchPassword.put("370112103346", "197823");
		merchPassword.put("370104103585", "198058");
		merchPassword.put("370102108004", "198138");
		merchPassword.put("370113104097", "198308");
		merchPassword.put("370124102436", "198396");
		merchPassword.put("370105104536", "198709");
		merchPassword.put("370105105753", "19901220");
		merchPassword.put("370102109465", "199106");
		merchPassword.put("370104103404", "199775");
		merchPassword.put("370113102997", "2");
		merchPassword.put("370102106085", "20021201");
		merchPassword.put("370125104928", "200808");
		merchPassword.put("370105106059", "207312");
		merchPassword.put("370104102933", "215116");
		merchPassword.put("370113105293", "222");
		merchPassword.put("370103102983", "222222");
		merchPassword.put("370104103931", "222222");
		merchPassword.put("370103110857", "222222");
		merchPassword.put("370112107059", "2277");
		merchPassword.put("370102109356", "231");
		merchPassword.put("370112106112", "2525");
		merchPassword.put("370112104410", "252627");
		merchPassword.put("370105105756", "274700");
		merchPassword.put("370104103116", "3116");
		merchPassword.put("370105106380", "315066");
		merchPassword.put("370104103860", "319000");
		merchPassword.put("370103104124", "319521");
		merchPassword.put("370112107645", "321");
		merchPassword.put("370103104894", "3222");
		merchPassword.put("370112105117", "324418");
		merchPassword.put("370103110850", "369258");
		merchPassword.put("370102109223", "373536");
		merchPassword.put("370112109067", "410918");
		merchPassword.put("370105106408", "420828");
		merchPassword.put("370112104300", "4300");
		merchPassword.put("370102110976", "440618");
		merchPassword.put("370104100800", "456123");
		merchPassword.put("370105106108", "502138");
		merchPassword.put("370181109948", "521227");
		merchPassword.put("370103110862", "524806");
		merchPassword.put("370105104856", "526162");
		merchPassword.put("370105105928", "535353");
		merchPassword.put("370102110639", "536911");
		merchPassword.put("370103107497", "5588392");
		merchPassword.put("370104104299", "561061");
		merchPassword.put("370102110968", "567891234");
		merchPassword.put("370102108652", "581551");
		merchPassword.put("370105100524", "603730");
		merchPassword.put("370103110042", "616161");
		merchPassword.put("370102111111", "646411");
		merchPassword.put("370102107633", "65321");
		merchPassword.put("370105105699", "654321");
		merchPassword.put("370112106376", "666666");
		merchPassword.put("370112100116", "671208");
		merchPassword.put("370105106050", "679485");
		merchPassword.put("370102111325", "690908");
		merchPassword.put("370112201598", "7");
		merchPassword.put("370103110675", "707525");
		merchPassword.put("370104101586", "710208");
		merchPassword.put("370125102907", "721504");
		merchPassword.put("370102101569", "726903");
		merchPassword.put("370124100144", "731121");
		merchPassword.put("370103211387", "741107");
		merchPassword.put("370102211307", "750213");
		merchPassword.put("370102108232", "751204");
		merchPassword.put("370181107105", "771018");
		merchPassword.put("370104103271", "771120");
		merchPassword.put("370102110062", "777777");
		merchPassword.put("370103109282", "781230");
		merchPassword.put("370102111115", "7966696");
		merchPassword.put("370102111191", "800614");
		merchPassword.put("370102106918", "801125");
		merchPassword.put("370104100489", "817777");
		merchPassword.put("370105101485", "818181");
		merchPassword.put("370103111072", "824");
		merchPassword.put("370103111221", "830427");
		merchPassword.put("370104103027", "830612");
		merchPassword.put("370105106209", "830727");
		merchPassword.put("370125104030", "84217819");
		merchPassword.put("370104103885", "84547103");
		merchPassword.put("370113105844", "851029");
		merchPassword.put("370105106710", "851107");
		merchPassword.put("370104104307", "861666");
		merchPassword.put("370102109425", "86992336");
		merchPassword.put("370113105130", "87220912");
		merchPassword.put("370102111142", "873621");
		merchPassword.put("370112106862", "88526174");
		merchPassword.put("370112103073", "88799134");
		merchPassword.put("370102110718", "888777");
		merchPassword.put("370103103652", "888888");
		merchPassword.put("370102102498", "88984212");
		merchPassword.put("370103110741", "89821");
		merchPassword.put("370105100649", "898989");
		merchPassword.put("370105104763", "8999");
		merchPassword.put("370102102136", "9");
		merchPassword.put("370103111200", "901201");
		merchPassword.put("370105105638", "9121");
		merchPassword.put("370104104144", "923028");
		merchPassword.put("370112105625", "930414");
		merchPassword.put("370105103586", "941122");
		merchPassword.put("370103107960", "945618");
		merchPassword.put("370105105438", "946332");
		merchPassword.put("370102110174", "950321");
		merchPassword.put("370113100409", "977069");
		merchPassword.put("370103103255", "99");
		merchPassword.put("370103108839", "991122");
		merchPassword.put("370102111018", "999");
		merchPassword.put("370112106279", "abcd1234");
		merchPassword.put("370102110830", "asdzxc");
		merchPassword.put("370112106897", "junjun");
		merchPassword.put("370102100615", "la1");
		merchPassword.put("370102108783", "lbhwdw2234");
		merchPassword.put("370112102672", "qwaszx");
		merchPassword.put("370102111214", "qxy198744");

		for(Entry<String, String> userCodeAndPassword : merchPassword.entrySet()) {
			String userCode = userCodeAndPassword.getKey();
			String password = MD5Util.getMD5Code(userCodeAndPassword.getValue()+"{ryx}");
			System.out.println("update pub_user set password='"+password+"' where user_code='"+userCode+"'");
		}
	}
	
	//查询迁移数据，权限用户
	@Override
    public List<Map<String, Object>> selectTransferMerch(Map<String, Object> paramMap) throws Exception {
    	LOG.debug(" merchServiceImpl selectTransferMerch paramMap: " + paramMap);
		return merchDao.selectTransferMerch(paramMap);
	}
	
	//修改迁移数据，权限用户
	@Override
	public void updateTransferMerch(Map<String, Object> paramMap) throws Exception {
		LOG.debug(" merchServiceImpl updateTransferMerch paramMap: " + paramMap);
		merchDao.updateTransferMerch(paramMap);
	}
	
	//上传智能终端经纬度
	@Override
	public Map<String, Object> uploadLocation(Map<String, Object> paramMap) throws Exception {
		LOG.debug("merchServiceImpl uploadLocation paramMap: " + paramMap);
		
		String paramsStr = JsonUtil.map2json(MapUtil.rename(paramMap, "merch_id.cust_id", "longitude", "latitude"));
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("params", paramsStr);
		
		String url = RetailConfig.getTobaccoServer()+"base/uploadLocation";
		
		LOG.debug("merchServiceImpl uploadLocation params: " +params);
		LOG.debug("merchServiceImpl uploadLocation url: " +url);
		
		String json = HttpUtil.post(url, params);
		LOG.debug("merchServiceImpl uploadLocation json: " + json);
		Map<String, Object> resultMap = (Map<String, Object>)JsonUtil.json2Map(json);
		return resultMap;
	}	
}
