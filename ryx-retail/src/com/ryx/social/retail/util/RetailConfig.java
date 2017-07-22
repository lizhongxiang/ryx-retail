package com.ryx.social.retail.util;

import java.util.Map;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class RetailConfig {

	private static String _tobaccoServer;
	private static String _luceneServer;
	private static String _ucenterServer;
	private static String _masterServer;
	private static String _resourceServer;
	private static String collectServerUrl;
	// 
	private static String nfsdataPath;
	//获取零售上绑定的卡号信息
	private static String openDataUpload;
	//增值服务--对账服务
	private static String checkBillServer;
	//登陆服务
	private static String loginServer;
	//增值服务---齐鲁银行查水、电、燃气、暖气、
	private static String publicServiceServer;
	//增值服务--电话缴费
	private static String phonePayServer;
	//内管
	private static String rrximsServer;
	// 信用卡还款
	private static String repayCreditCardUrl;
	// 信用卡信息查询
	private static String queryCreditCardUrl;
	//消息推送url
	private static String msgPushUrl;
	
	/**
	 * 根据server_config数据表初始化属性
	 */
	public static void init(Map<String, String> serverConfig) {
		_tobaccoServer = serverConfig.get("tobaccoServer");
		_luceneServer = serverConfig.get("luceneServer");
		_ucenterServer = serverConfig.get("ucenterServer");
		_masterServer = serverConfig.get("masterServer");
		_resourceServer = serverConfig.get("resourceServer");
		collectServerUrl = serverConfig.get("collectServerUrl");
		checkBillServer = serverConfig.get("checkBillServer");
		publicServiceServer = serverConfig.get("publicServiceServer");
		loginServer = serverConfig.get("loginServer");
		phonePayServer = serverConfig.get("phonePayServer");
		rrximsServer = serverConfig.get("rrximsserver");
		repayCreditCardUrl = serverConfig.get("repayCreditCardUrl");
		queryCreditCardUrl = serverConfig.get("queryCreditCardUrl");
		msgPushUrl = serverConfig.get("msgPushUrl");
	}
	
	/**
	 * 根据本地配置文件初始化属性
	 */
	public static void init(Properties serverProperties) {
		nfsdataPath = serverProperties.getProperty("nfsdataPath");
		openDataUpload = serverProperties.getProperty("openDataUpload");
	}
	
	public static String getRepayCreditCardUrl() {
		return repayCreditCardUrl;
	}
	
	public static String getQueryCreditCardUrl() {
		return queryCreditCardUrl;
	}
	
	public static String getMsgPushUrl () {
		return msgPushUrl;
	}
	
	public static String getRrximsServer() {
		return rrximsServer;
	}
	public static String getTobaccoServer() {
		return _tobaccoServer;
	}
	
	public static String getLuceneServer() {
		return _luceneServer;
	}
	
	public static String getUcenterServer() {
		return _ucenterServer;
	}
	
	public static String getMasterServer() {
		return _masterServer;
	}
	
	public static String getResourceServer() {
		return _resourceServer;
	}
	
	/**
	 * 资金归集
	 */
	public static String getCollectServerUrl() {
		return collectServerUrl;
	}

	public static String getNfsdataPath(){
		return nfsdataPath;
	}
	
	
	public static String getOpenDataUpload() {
		return openDataUpload;
	}
	
	/**
	 * 登陆地址配置
	 */
	public static String getLoginServer(){
		return loginServer;
	}
	
	/**
	 * 对账服务配置
	 */
	public static String getCheckBillServer(){
		return checkBillServer;
	}
	
	/**
	 * 增值服务地址
	 */
	public static String getPublicServiceServer(){
		return publicServiceServer;
	
	}
	
	public static String getPhonePayServer(){
		return phonePayServer;
	}
	
}
