package com.ryx.social.retail.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.ResponseUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.login.user.service.IUserService;

/**
 * PermitController 零售户权限类
 * 
 */
@Controller
public class PermitController {
	@Resource
	IUserService userService;
	@RequestMapping(value = "/retail/permit/getPermit")
	public void getPermit(HttpServletRequest request, HttpServletResponse response) {
		
		String userId = IdentityUtil.getUser(request).getUserCode();
		List<Map> result=userService.getUserResource(userId);
		
		ResponseUtil.write(request, response, result);
		
		/*
		List list = new ArrayList();
		list.add(createItem("spxs", "商品销售", "1"));
		list.add(createItem("jydh", "卷烟订货", "1"));

		list.add(createItem("qdrk", "签单入库", "0"));
		list.add(createItem("jyrk", "卷烟入库", "1", "qdrk"));
		list.add(createItem("fyrk", "非烟入库", "1", "qdrk"));

		list.add(createItem("kcgl", "库存管理", "0"));
		list.add(createItem("kcpd", "库存盘点", "1", "kcgl"));
		list.add(createItem("hlkc", "合理库存", "1", "kcgl"));
		list.add(createItem("yskc", "原始库存", "1", "kcgl"));
		list.add(createItem("pdjl", "盘点记录", "1", "kcgl"));
		
		list.add(createItem("wgdd", "网购订单", "1"));

		list.add(createItem("xsgl", "销售管理", "0"));
		list.add(createItem("xsls", "销售流水", "1", "xsgl"));
		list.add(createItem("xsth", "销售退货", "1", "xsgl"));
		
		list.add(createItem("tjfx", "统计分析", "1"));
		
		list.add(createItem("jcsj", "基础数据", "0"));
		list.add(createItem("dpsz", "店铺设置", "1", "jcsj"));
		list.add(createItem("spgl", "商品管理", "1", "jcsj"));
		list.add(createItem("hygl", "会员管理", "1", "jcsj"));
		list.add(createItem("dysz", "打印设置", "1", "jcsj"));

		list.add(createItem("yxhd", "营销互动", "0"));
		list.add(createItem("dcwj", "调查问卷", "1", "yxhd"));
		list.add(createItem("lshcj", "零售户抽奖", "1", "yxhd"));
		list.add(createItem("xfzcj", "消费者抽奖", "1", "yxhd"));
		
		list.add(createItem("ycjt", "烟草讲堂", "1"));

		list.add(createItem("bmfw", "便民服务", "0"));
		list.add(createItem("sjcz", "手机充值", "1", "bmfw"));
		
		list.add(createItem("yyzx", "应用中心", "1"));
		
		ResponseUtil.write(request, response, list);
		*/
	}
	
//	private Map createItem(String id, String name, String haspage)
//	{
//		return createItem(id, name, haspage, null);
//	}
	
	private Map createItem(String id, String name, String haspage, String parent_id) {
		Map map = new HashMap();
		map.put("module_id", id);
		map.put("title", name);
		map.put("haspage", haspage);
		map.put("parent_id", parent_id);
		return map;
	}
}
