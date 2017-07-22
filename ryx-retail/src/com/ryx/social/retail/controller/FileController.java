package com.ryx.social.retail.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.service.IFileService;
import com.ryx.social.retail.util.ParamUtil;

/**
 * 上传文件/图片
 */
@Controller
public class FileController {
	
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	
	@Resource
	private IFileService fileService;
	
	/**
	 * 梁凯 2014年5月23日12:14:49
	 * 智能终端备份数据库
	 */
	@RequestMapping(value = "/file/backupDB")
	public void backupDB(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap.put("lice_id", paramMap.remove("merch_id")); // 智能终端上传的merch_id是专卖证号lice_id, 在service中根据lice_id获取真正的merch_id
		paramMap.put("file_purpose", "88");
		paramMap.put("file_size", 10);
		try {
			data = fileService.backupDB(request, paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 梁凯 2014年5月26日10:37:00
	 * 智能终端下载数据库文件
	 */
	@RequestMapping(value = "/file/restoreDB")
	public void restoreDBCompatibly(HttpServletRequest request, HttpServletResponse response) throws Exception {
		restoreDB(request, response);
	}

	@RequestMapping(value = "/retail/file/restoreDB")
	public void restoreDB(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		paramMap = MapUtil.remain(paramMap, "merch_id", "file_id");
		paramMap.put("file_purpose", "88");
		paramMap.put("status", "1");
		try {
			fileService.downloadFile(request, response, paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error(" = * = * = * = * = 智能终端下载数据库文件错误 = * = * = * = * = ", e);
			ResponseUtil.write(request, response, code, msg, null);
		}
	}

	/**
	 * 管店宝商户店铺批量上传图片
	 */
	@RequestMapping(value = "/retail/file/uploadShopPictures")
	public void uploadShopPictures(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		if(paramMap.get("ref_id")!=null) paramMap.put("merch_id", paramMap.get("ref_id")); // 用session中的merch_id替换request中的
		paramMap.put("file_purpose", "01");
		try {
			data = fileService.uploadFile(request, paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error(" = * = * = * = * = 管店宝商户店铺批量上传图片错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value = "/retail/file/uploadShopPicture")
	public void uploadShopPicture(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		if(paramMap.get("ref_id")!=null) paramMap.put("merch_id", paramMap.get("ref_id")); // 用session中的merch_id替换request中的
		paramMap.put("file_purpose", "01");
		try {
			List<Map<String, Object>> uploadedFileList = fileService.uploadFile(request, paramMap);
			if(!uploadedFileList.isEmpty()) data = uploadedFileList.get(0);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error(" = * = * = * = * = 管店宝商户店铺上传图片错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	/**
	 * 设置店铺头像
	 */
	@RequestMapping(value = "/retail/file/modifyShopPortrait")
	public void modifyShopPortrait(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			fileService.modifyShopPortrait(paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = 商户头像修改错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, null);
	}
	
	/**
	 * 梁凯 2014年5月26日11:00:53
	 * 登陆后删除商户文件的通用接口
	 */
	@RequestMapping(value = "/retail/file/removeFile")
	public void removeFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		try {
			data = fileService.removeFile(request, paramMap);
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = e.getMessage();
			logger.error(" = * = * = * = * = 删除商户文件错误 = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value = "/retail/file/removeMerchFile")
	public void removeMerchFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		removeFile(request, response);
	}
	
	// 兼容老版本
	@RequestMapping(value = "/file/searchBackupFile")
	public void searchBackupFileCompatibly(HttpServletRequest request, HttpServletResponse response) throws Exception {
		searchBackupFile(request, response);
	}
	
	/**
	 * 梁凯 2014年5月29日10:48:05
	 * 获取智能终端数据库备份文件列表
	 */
	@RequestMapping(value = "/retail/file/searchBackupFile")
	public void searchBackupFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		/*Map<String, Object> paramMap = ParamUtil.getParamMap(request);
		String refId = MapUtil.getString(paramMap, "ref_id");
		// 如果智能终端没有登录则返回空列表
		if(!StringUtil.isBlank(refId)) {
			paramMap.put("merch_id", refId); // 用session中的merch_id替换request中的
			paramMap.put("file_purpose", "88");
			paramMap.put("status", "1");
			try {
				data = fileService.searchMerchFile(paramMap);
			} catch (Exception e) {
				code = Constants.FAIL;
				msg = e.getMessage();
				logger.error(" = * = * = * = * = 获取智能终端数据库备份文件列表错误 = * = * = * = * = ", e);
			}
		}*/
		ResponseUtil.write(request, response, code, msg, data);
	}
	
	@RequestMapping(value = "retail/file/deleteMerchExcessFiles")
	public void deleteMerchExcessFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = Constants.SUCCESS;
		String msg = Constants.SUCCESS_MSG;
		List<String> data = new ArrayList<String>();
		try {
			data = fileService.deleteMerchExcessFiles(new HashMap<String, Object>());
		} catch (Exception e) {
			code = Constants.FAIL;
			msg = Constants.FAIL_MSG;
			logger.error(" = * = * = * = * = deleteMerchExcessFile = * = * = * = * = ", e);
		}
		ResponseUtil.write(request, response, code, msg, data);
	}
}
