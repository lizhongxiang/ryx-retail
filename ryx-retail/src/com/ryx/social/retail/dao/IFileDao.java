package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IFileDao {
	public void insertMerchFile(Map<String, Object> fileMap) throws Exception;
	public List<Map<String, Object>> selectMerchFile(Map<String, Object> merchFileParam) throws Exception;
	public void deleteMerchFile(Map<String, Object> merchFileParam) throws Exception;
	public void updateMerchFile(Map<String, Object> merchFileParam) throws Exception;
	/**
	 * 梁凯 2014年5月23日11:59:59
	 * 获取某商户某种文件的最大序列号
	 */
	public int searchMaxFileSequence(Map<String, Object> fileParam) throws Exception;
	
	public List<Map<String, Object>> selectAllMerchFile(Map<String, Object> fileParam)throws Exception;
}
