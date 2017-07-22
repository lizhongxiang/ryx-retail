package com.ryx.social.retail.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.ConvertUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.JsonUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.RequestUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IMerchConsumerService;
import com.ryx.social.retail.util.ParamUtil;

@Controller
public class MerchConsumerController {
	private Logger LOG = LoggerFactory.getLogger(MerchConsumerController.class);
	@Resource
	private IMerchConsumerService merchConsumerService;
	
	//插入默认会员等级
	@RequestMapping(value="/retail/consumer/insertDefMerchConsumerGrade")
	public void insertDefMerchConsumerGrade(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String merchId = MapUtil.getString(userMap, "ref_id");
		paramMap.put("merch_id", merchId);
		try {
			merchConsumerService.insertDefMerchConsumerGrade(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("插入会员等级: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//插入会员等级
	@RequestMapping(value="/retail/consumer/insertMerchConsumerGrade")
	public void insertMerchConsumerGrade(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		String paramsStr = request.getParameter("params");
		LOG.debug("-------JSON："+paramsStr);
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		List<Map<String, Object>> paramList=JsonUtil.json2List(request.getParameter("params"));
		String merchId = MapUtil.getString(userMap, "ref_id");
		for (Map<String, Object> map : paramList) {
			map.put("merch_id", merchId);
		}
		try {
			merchConsumerService.insertMerchConsumerGrades(paramList);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("插入会员等级: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	// 查询商户会员级别
	@RequestMapping(value="/retail/consumer/selectMerchConsumerGrade")
	public void selectMerchConsumerGrade(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = Collections.EMPTY_LIST;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			data = merchConsumerService.searchMerchConsumerGrade(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询查询商户会员级别和对应会员数量错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	// 查询商户会员级别和对应会员数量
	@RequestMapping(value="/retail/consumer/searchMerchConsumerGradeAndConsumerNumber")
	public void searchMerchConsumerGradeAndConsumerNumber(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = Collections.EMPTY_LIST;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			data = merchConsumerService.searchMerchConsumerGradeAndConsumerNumber(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询查询商户会员级别和对应会员数量错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	////////////
	
	// 查询会员
	@RequestMapping(value="/retail/consumer/searchMerchConsumer")
	public void searchMerchConsumer(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = Collections.EMPTY_LIST;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			data = merchConsumerService.selectMerchConsumer(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 查询查询商户会员级别和对应会员数量错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	//操作会员等级---增删改
	@RequestMapping(value="/retail/consumer/operationMerchConsumerGrade")
	public void operateMerchConsumerGrade(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		List<Map<String, Object>> gradeList = MapUtil.get(paramMap, "list", Collections.EMPTY_LIST);
		for(Map<String, Object> gradeMap : gradeList) {
			gradeMap.put("merch_id", paramMap.get("merch_id"));
		}
		try {
			merchConsumerService.operateMerchConsumerGrade(gradeList);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error(" = * = * = * = * = 增删改商户会员级别错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//插入会员
	@RequestMapping(value="/retail/consumer/insertMerchConsumer")
	public void insertMerchConsumer(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		Map<String, Object> data = null;
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			data = merchConsumerService.insertMerchConsumer(paramMap);
			code = MapUtil.getString(data, "code");
			msg = MapUtil.getString(data, "msg");
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("插入会员: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//插入默认级别，或修改无级别会员（会员管理）
	@RequestMapping(value="/retail/consumer/insertDefaultGrade")
	public void insertDefaultGrade(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		LOG.debug("---------------"+request.getParameter("params"));
		List<Map<String, Object>> paramList=JsonUtil.json2List(request.getParameter("params"));
		Map<String, Object> data = null;
		for (Map<String, Object> map : paramList) {
			map.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		}
		try {
			merchConsumerService.insertDefaultGrade(paramList);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("插入默认级别，或修改无级别会员（会员管理）：", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//修改会员
	@RequestMapping(value="/retail/consumer/updateMerchConsumer")
	public void updateMerchConsumer(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		Map<String, Object> data = null;
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			merchConsumerService.updateMerchConsumer(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("修改会员: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	
	//修改会员---用于会员管理（防止手机号，卡号重复）
	@RequestMapping(value="/retail/consumer/updateMerchConsumerForHygl")
	public void updateMerchConsumerForHygl(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		Map<String, Object> data = null;
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			data = merchConsumerService.updateMerchConsumerForHygl(paramMap);
			msg = MapUtil.getString(data, "msg");
			code = MapUtil.getString(data, "code");
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("修改会员: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	
	//查询会员通过id
	@RequestMapping(value="/retail/consumer/selectMerchConsumerById")
	public void selectMerchConsumerById(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		Map<String, Object> data = null;
		try {
			data = merchConsumerService.selectMerchConsumerById(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("查询会员通过id: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	//查询会员关联（）
	@RequestMapping(value="/retail/consumer/searchMerchConsumerInfo")
	public void searchMerchConsumerInfo(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			data = merchConsumerService.searchMerchConsumer(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("查询会员关联: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	////////////
	
	
	//查询积分兑换流水
	@RequestMapping(value="/retail/consumer/searchScoreExchange")
	public void searchScoreExchange (HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			data = merchConsumerService.searchScoreExchange(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("查询积分兑换流水: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	//查询积分兑换商品
	@RequestMapping(value="/retail/consumer/searchExchangePrize")
	public void searchExchangePrize (HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			data = merchConsumerService.searchExchangePrize(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("查询积分兑换商品: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	
	
	//插入积分兑换流水
	@RequestMapping(value="/retail/consumer/insertScoreExchange")
	public void insertScoreExchange (HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
//		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			merchConsumerService.insertScoreExchange(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("插入积分兑换流水: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//修改积分兑换商品
	@RequestMapping(value="/retail/consumer/updateExchangePrize")
	public void updateExchangePrize (HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		LOG.debug(request.getParameter("params"));
//		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			merchConsumerService.updateExchangePrize(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("修改积分兑换商品: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	//插入积分兑换商品
	@RequestMapping(value="/retail/consumer/insertExchangePrize")
	public void insertExchangePrize (HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		LOG.debug(request.getParameter("params"));
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			merchConsumerService.insertExchangePrize(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("插入积分兑换: ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	//结构分析:按年龄性别分组
	@RequestMapping("/retail/customer/searchCustomerByAgeGender")
	public void searchCustomerByAgeGender(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap=ParamUtil.getParamMap(request);
		List<Map<String, Object>> data=new ArrayList<Map<String, Object>>();
		try {
			data=merchConsumerService.searchMerchCustomerByAgeSex(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("结构分析-按年龄性别分组", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	//结构分析:按学历分组
	@RequestMapping("/retail/customer/searchCustomerByEdu")
	public void searchCustomerByEdu(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap=ParamUtil.getParamMap(request);
		List<Map<String, Object>> data=new ArrayList<Map<String, Object>>();
		try {
			data=merchConsumerService.searchMerchCustomerByEdu(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("结构分析-按学历分组", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	//结构分析:按收入分组
	@RequestMapping("/retail/customer/searchCustomerByMoney")
	public void searchCustomerByMoney(HttpServletRequest request, HttpServletResponse response){
		String code=Constants.SUCCESS;
		String msg=Constants.SUCCESS_MSG;
		Map<String, Object> paramMap=ParamUtil.getParamMap(request);
		List<Map<String, Object>> data=new ArrayList<Map<String, Object>>();
		try {
			data=merchConsumerService.searchMerchCustomerByMoney(paramMap);
		} catch (Exception e) {
			code=Constants.FAIL;
			msg=Constants.FAIL_MSG;
			LOG.error("结构分析-按收入分组", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
	//行为分析
	@RequestMapping(value="/retail/consumer/searchConsumeBehaviorAnalysis")
	public void searchConsumeBehaviorAnalysis (HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		LOG.debug(request.getParameter("params"));
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			data = merchConsumerService.searchConsumeBehaviorAnalysis(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("--------------行为分析错误: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data, paramMap);
	}
	
	//行为分析单行
	@RequestMapping(value="/retail/consumer/searchConsumeBehaviorAnalysisLine")
	public void searchConsumeBehaviorAnalysisLine (HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		LOG.debug(request.getParameter("params"));
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> userMap = IdentityUtil.getUserMap(request);
		Map<String, Object> paramMap=JsonUtil.json2Map(request.getParameter("params"));
		paramMap.put("merch_id", MapUtil.getString(userMap, "ref_id"));
		try {
			data = merchConsumerService.searchConsumeBehaviorAnalysisLine(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			LOG.error("--------------行为分析单行错误: ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	
}
