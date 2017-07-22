package com.ryx.social.retail.controller;

import java.util.Collections;
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
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.login.tool.IdentityUtil;
import com.ryx.social.retail.service.IRetailUserManagerService;
import com.ryx.social.retail.util.ParamUtil;


@Controller
public class RetailUserManagerController {
	
	@Resource
	private IRetailUserManagerService retailUserManagerServiceImpl;
	
	private static final Logger LOG = LoggerFactory.getLogger(RetailUserManagerController.class);
	
	@RequestMapping("/retail/user/role/add")
	public void addRole(HttpServletRequest request, HttpServletResponse response){
		String roleID=null;
		try{
			String resourceIDS = request.getParameter("resourcesID");
			String roleName = request.getParameter("roleName");
			roleID=request.getParameter("roleID");
			String userCode = (String)IdentityUtil.getUserMap(request).get("user_code");
			retailUserManagerServiceImpl.addRole(resourceIDS, roleName, userCode,roleID);
			
		}catch (Exception e) {
			LOG.error("addRole:",e);
		}
		
		if(!roleID.equals("")){
			ResponseUtil.write(request,response,"角色修改成功");
		}
		else{
			ResponseUtil.write(request,response,"角色保存成功");
		}
	}
	
	@RequestMapping("/retail/user/role/show")
	public void showRoleList(HttpServletRequest request, HttpServletResponse response){
		String userCode = (String)IdentityUtil.getUserMap(request).get("user_code");
		try {
			List<Map<String,Object>> list = retailUserManagerServiceImpl.showRoleList(userCode);
			ResponseUtil.write(request,response,list);
		} catch (Exception e) {
			LOG.error("showRoleList:",e);
			ResponseUtil.write(request,response,"1000","系统错误","");
		}
	}
	
	//添加销售员
	@RequestMapping("/retail/user/add")
	public void addUser(HttpServletRequest request, HttpServletResponse response){
		String userName = request.getParameter("userName");
		String roleID = request.getParameter("roleID");
		String password = request.getParameter("password");
//		String userID = request.getParameter("userID");
		String islocked = request.getParameter("islocked");
		Map<String,Object> userMap = IdentityUtil.getUserMap(request);
		userMap.put("user_code", userName);
		if(MapUtil.get(userMap, "password", null)!=null){
			userMap.remove("password");
		}
		if(!StringUtil.isBlank(password)){
			userMap.put("password", password);
		}
		userMap.put("is_locked", islocked);
		userMap.put("token",(String)request.getSession(true).getAttribute("token"));
		userMap.put("status", request.getParameter("status"));
		try {
			retailUserManagerServiceImpl.addUser(roleID ,userMap);
			ResponseUtil.write(request,response,"添加用户成功");
		} catch (Exception e) {
			LOG.error("addUser:",e);
			ResponseUtil.write(request,response,"1000",e.getMessage(),"");
		}
	}
	
	//修改销售员
	@RequestMapping("/retail/user/updateUser")
	public void updateUser(HttpServletRequest request, HttpServletResponse response){
		String userName = request.getParameter("userName");
		String roleID = request.getParameter("roleID");
		String password = request.getParameter("password");
		String userID = request.getParameter("userID");
		String islocked = request.getParameter("islocked");
		String status = request.getParameter("status");
		Map<String,Object> userMap = IdentityUtil.getUserMap(request);
		userMap.put("user_code", userName);
		if(MapUtil.get(userMap, "password", null)!=null){
			userMap.remove("password");
		}
		if(!StringUtil.isBlank(password)){
			userMap.put("password", password);
		}
		userMap.put("is_locked", islocked);
		userMap.put("token",(String)request.getSession(true).getAttribute("token"));
		userMap.put("status", status);
		try {
			retailUserManagerServiceImpl.updateUser(roleID, userID, userMap);
			ResponseUtil.write(request,response,"修改用户成功");
		} catch (Exception e) {
			LOG.error("addUser:",e);
			ResponseUtil.write(request,response,"1000",e.getMessage(),"");
		}
	}
	
	//查询用户管理列表
	@RequestMapping("/retail/user/show")
	public void showUserList(HttpServletRequest request, HttpServletResponse response) {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);// 认证信息和前台参数
		String merchId = MapUtil.getString(paramMap, "merch_id");// 商户编号
		String token = (String) request.getSession(true).getAttribute("token");// 从session中获取token
		List<Map<String, Object>> list = Collections.EMPTY_LIST;
		try {
			list = retailUserManagerServiceImpl.showUserList(merchId, token);
		} catch (Exception e) {
			LOG.error(" = * = * = * = * = 查询店员信息错误  = * = * = * = * = ", e);
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
		}
		ResponseUtil.write(request, response, code, msg, list);
	}
	
	@RequestMapping("/retail/user/isexist")
	public void isExistUserCode(HttpServletRequest request, HttpServletResponse response){
		String userID=null;
		try{
		String userCode = request.getParameter("userCode");
		userID = retailUserManagerServiceImpl.isExistUserCode(userCode);
		}catch (Exception e) {
			LOG.error("isExistUserCode:",e);
		}
		ResponseUtil.write(request,response,userID);
		
	}
	
	
	
}
