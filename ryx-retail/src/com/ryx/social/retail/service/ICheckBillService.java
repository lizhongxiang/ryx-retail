package com.ryx.social.retail.service;

import java.util.Map;

public interface ICheckBillService {
	/**
	 * 便民服务--对账服务--话费查询
	 * @return
	 */
	public String checkBillPhoneFee(Map<String,String> map);
	/**
	 * 便民服务--对账服务--消费查询
	 * @return
	 */
	public String checkBillConsume(Map<String,String> map);
	
	/**
	 * 便民服务--对账服务--齐鲁银行公缴费
	 * @return
	 */
	public String checkBillGJF(Map<String,String> map);

}
