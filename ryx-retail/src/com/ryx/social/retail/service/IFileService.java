package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IFileService {
	public void modifyShopPortrait(Map<String, Object> merchFileParam) throws Exception;
	public List<Map<String, Object>> uploadFile(HttpServletRequest request, Map<String, Object> merchFileParam) throws Exception;
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, Map<String, Object> downloadParam) throws Exception;
	public Map<String, Object> removeFile(HttpServletRequest request, Map<String, Object> removeParam) throws Exception;
	public List<Map<String, Object>> backupDB(HttpServletRequest request, Map<String, Object> backupParam) throws Exception;
	public List<Map<String, Object>> searchMerchFile(Map<String, Object> fileParam) throws Exception;
	
	public List<Map<String, Object>> selectAllMerchFile(Map<String, Object> fileParam) throws Exception ;
	public List<String> deleteMerchExcessFiles(Map<String, Object> fileParam) throws Exception ;
}
