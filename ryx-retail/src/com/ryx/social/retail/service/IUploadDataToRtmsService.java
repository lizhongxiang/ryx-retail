package com.ryx.social.retail.service;

import java.util.List;
import java.util.Map;

public interface IUploadDataToRtmsService {
	/**
	 * 卷烟销售单上传
	 * @author 徐虎彬
	 * @date 2014年5月5日
	 * @param paramMap
	 * @throws Exception
	 */
	public void submitSaleOrder2RTMS(Map<String, Object> dataMap) throws Exception;
	/**
	 * 卷烟销售单批量上传
	 * @author 朱鹏
	 * @date 2014年10月31日
	 * @param paramMap
	 * @throws Exception
	 */
	public void submitBatchSaleOrder2RTMS(List<Map<String, Object>> orderParamList) throws Exception;
	/**
	 * 卷烟库存盘点记录上传
	 * @author 徐虎彬
	 * @date 2014年5月5日
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public void insertWhseTobaccoToRTMS(Map<String, Object> dataMap) throws Exception;
	/**
	 * 卷烟商品入库信息上传
	 * @author 徐虎彬
	 * @date 2014年5月5日
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public void insertPurchTobaccoToRTMS(Map<String, Object> dataMap)throws Exception;
}
