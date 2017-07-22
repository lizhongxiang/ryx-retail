package com.ryx.social.retail.service.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.HttpUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IInfointerDao;
import com.ryx.social.retail.service.IInfointerService;
import com.ryx.social.retail.util.RetailConfig;
import com.ryx.social.retail.util.TobaccoUtil;

@Service
public class InfointerServiceImpl implements IInfointerService {
	private static final Logger LOG = LoggerFactory.getLogger(InfointerServiceImpl.class);
	@Resource
	private IInfointerDao infointerDao;

	private String rootPath = null;
	
	//获取系统公告信息列表
	public List<Map<String,Object>> getNoticeList(Map<String,String> map) throws Exception{
		LOG.debug("InfointerServiceImpl getNoticeList map" + map);
		return infointerDao.getNoticeList(map);
	}
	
	//获取公告信息明细
	public Map<String,Object> getNoticeDetail(Map<String,String> map) throws Exception{
		LOG.debug("InfointerServiceImpl getNoticeDetail map:"+map);
		//从服务器
//		String cgtComId = (String) map.get("comId");
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "infointer/getNoticeDetail";
		
//		Map<String, Object> jsonMap = HttpPostUtil.postValueStringMap(cgtItemUrl, map);
//		
//		Map<String, Object> noticeMap = null;
//		
//		if(HttpPostUtil.judgeNull(jsonMap, "result")){
//			noticeMap = (Map<String, Object>)jsonMap.get("result");
//		}else { //返回码异常
//			throw new RuntimeException((String) jsonMap.get("msg"));
//		}
	
		String json = HttpUtil.post(cgtItemUrl, map);
		Map<String, Object> jsonMap = null;
		if(!StringUtils.isEmpty(json)){
			jsonMap = JsonUtil.json2Map(json);
		}
		 
//		Map<String,Object> jsonMap = TobaccoUtil.getJsonFromServer(map,"infointer/getNoticeDetail");
		//解析获取的数据
		String code = MapUtil.getString(jsonMap, "code", null);
		Map<String, Object> noticeMap = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			noticeMap = (Map<String, Object>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		return noticeMap;
	}
	
	//更新阅读消息状态
	public void updateNoticeDetail(List<String> list) throws Exception {
		
		LOG.debug("InfointerServiceImpl updateNoticeDetail list" + list);
		infointerDao.updateNoticeDetail(list);
	}
	
	//获取01、系统公告。02：货源信息。03:1532非烟商品
	public List<Map<String, Object>> getActivityList(Map<String, String> map) throws Exception {
		LOG.debug("InfointerServiceImpl getActivityList map:"+map);
//		勿删 、参数（userId、comId、date1、noticeType）
//		noticeType：01、系统公告。02：货源信息。03:1532非烟商品
//		http://192.168.0.14:9081/tobaccoserver/infointer/getNoticeList
//		?userId=1111037010507633&comId=10370101&date1=20150403
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "infointer/getNoticeList";
		
		String json = HttpUtil.post(cgtItemUrl, map);
		LOG.debug("InfointerServiceImpl getActivityList json:"+json);
		Map<String, Object> jsonMap = null;
		if(!StringUtils.isEmpty(json)){
			jsonMap = JsonUtil.json2Map(json);
		}
		//解析获取的数据
		String code = MapUtil.getString(jsonMap, "code", null);
		List<Map<String, Object>> noticeList = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			noticeList = (List<Map<String, Object>>)jsonMap.get("result");
			Integer page_count = (int) Math.floor(noticeList.size()/20) + 1;
			map.put("page_count", page_count+"");
			map.put("count", noticeList.size()+"");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		return noticeList;
	}
	
	//获取咨询投诉列表
	public List<Map<String,Object>> getInfointerList(Map<String,String> map) throws Exception{
		LOG.debug("InfointerServiceImpl getInfointerList map:"+map);
		//从服务器
//		String cgtComId = (String) map.get("comId");
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "infointer/getInfointerList";
		LOG.debug("InfointerServiceImpl getInfointerList http:"+cgtItemUrl+"; param:"+map);
		String json = HttpUtil.post(cgtItemUrl, map);
		LOG.debug("InfointerServiceImpl getInfointerList json:"+json);
		Map<String, Object> jsonMap = null;
		if(!StringUtils.isEmpty(json)){
			jsonMap = JsonUtil.json2Map(json);
		}
		//解析获取的数据
		String code = MapUtil.getString(jsonMap, "code", null);
		List<Map<String,Object>>  dataList = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			dataList = (List<Map<String,Object>>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		return dataList;
	}
	
	//获取咨询投诉信息明细
	public Map<String,Object> getInfointerDetail(Map<String,String> map) throws Exception{
		LOG.debug("InfointerServiceImpl getInfointerDetail map:"+map);
		//从服务器
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "infointer/getInfointerDetail";
		LOG.debug("InfointerServiceImpl getInfointerDetail http:"+cgtItemUrl+"; param:"+map);
		String json = HttpUtil.post(cgtItemUrl, map);
		LOG.debug("InfointerServiceImpl getInfointerDetail json:"+json);
		Map<String, Object> jsonMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			jsonMap = JsonUtil.json2Map(json);
		}
		
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		Map<String, Object> InfointerMap = null;

		if(code!=null && Constants.SUCCESS.equals(code)) {
			InfointerMap = (Map<String, Object>)jsonMap.get("result");
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
		return InfointerMap;
	}
	
	//提交咨询投诉
	public void submitInfointer(Map<String,String> map) throws Exception{
		LOG.debug("InfointerServiceImpl submitInfointer map:"+map);
		//从服务器
		String cgtItemUrl = RetailConfig.getTobaccoServer() + "infointer/submitInfointer";
		LOG.debug("InfointerServiceImpl submitInfointer http:"+cgtItemUrl+"; param:"+map);
		String json = HttpUtil.post(cgtItemUrl, map);
		LOG.debug("InfointerServiceImpl submitInfointer json:"+json);
		Map<String, Object> jsonMap = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(json)){
			jsonMap = JsonUtil.json2Map(json);
		}
		//解析获取的数据
		String code = (String)jsonMap.get("code");
		if(code!=null && Constants.SUCCESS.equals(code)) {
			
		} else { //返回码异常
			throw new RuntimeException((String) jsonMap.get("msg"));
		}
	}

}
