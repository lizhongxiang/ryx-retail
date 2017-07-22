package com.ryx.social.retail.controller;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ryx.framework.util.DateUtil;
import com.ryx.social.retail.service.IStatisticsService;
import com.ryx.social.retail.util.SettlementLock;
import com.ryx.social.retail.util.WarehousingLock;

@Component
public class StatisticsTask {
	
	private Logger logger = LoggerFactory.getLogger(StatisticsTask.class);
	
	@Resource
	private IStatisticsService statisticsService;
	
//	@Scheduled(cron = "0 0/10 * * * ?") // 每10分钟
	public void autoDailySettlement() {
		try {
			Thread.sleep(Math.round(10000*Math.random()));
		} catch (Exception e) {
			logger.debug(" = * = * = * = * = 定时任务执行开始前休眠异常 = * = * = * = * = ", e);
		}
		logger.debug(" = * = * = * = * = 定时任务执行开始 = * = * = * = * = " + DateUtil.currentDatetime() + "\r\n");
		logger.debug(" = * = * = * = * = 定时任务执行实例 = * = * = * = * = " + this + "\r\n");
		logger.debug(" = * = * = * = * = 定时任务执行进程 = * = * = * = * = " + ManagementFactory.getThreadMXBean() + "\r\n");
		// tomcat开机10分钟后开始第一次重建lucene索引和检测是否需要日结, 之后每10分钟进行一次
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		// 再入库
		if(WarehousingLock.getLock().isUnlocked()) {
			try {
				WarehousingLock.getLock().lock();
				statisticsService.autoWarehousingMerchTobaccoOrder(paramMap);
			} catch (Exception e) {
				logger.error(" = * = * = * = * = StatisticsTask ERROR = * = * = * = * = ", e);
			} finally {
				WarehousingLock.getLock().unlock();
			}
		} else {
			logger.debug("正在自动入库...");
		}
		
		// 如果想结算个别商户, 设置merch_id
//		paramMap.put("merch_id", "1037010500760,1037010500764,1037010507633,1037010507750,1037010506526,1037010510910,1037010510912");
		// 先重建索引, 再日结
//		long lastMillisTime = DateUtil.getCurrentTimeMillis();
////		logger.debug("正在重建商品索引... 开始时间:"+DateUtil.getCurrentTime());
////		String rebuildGoodsLuceneResult = HttpUtil.post(RetailConfig.getLuceneServer() + "tobacco/retail/retailGoods/db2luce", null);
////		String refreshGoodsLuceneResult = HttpUtil.post(RetailConfig.getLuceneServer() + "tobacco/retail/retailGoods/refresh", null);
////		logger.debug("重建商品索引完成, 用时: "+(DateUtil.getCurrentTimeMillis()-lastMillisTime)+"毫秒, 结果:"+rebuildGoodsLuceneResult);
//		lastMillisTime = DateUtil.getCurrentTimeMillis();
//		logger.debug("正在重建商户索引... 开始时间:"+DateUtil.getCurrentTime());
//		String rebuildStoreLuceneResult = HttpUtil.post(RetailConfig.getLuceneServer() + "tobacco/retail/retailStore/db2luce");
////		String refreshStoreLuceneResult = HttpUtil.post(RetailConfig.getLuceneServer() + "tobacco/retail/retailStore/refresh");
//		logger.debug("重建商户索引完成, 用时:"+(DateUtil.getCurrentTimeMillis()-lastMillisTime)+"毫秒, 结果:"+rebuildStoreLuceneResult);
		// 先日结
		if(SettlementLock.getLock().isUnlocked()) {
			try {
				SettlementLock.getLock().lock();
				String settlementDate = statisticsService.searchSystemSettlement();
				String today = DateUtil.getToday();
				if(settlementDate!=null && Integer.valueOf(settlementDate)>=Integer.valueOf(today)) {
					logger.debug(" = * = * = * = * = 当前日期 = * = * = * = * = " + today 
							+ "\r\n = * = * = * = * = 结算日期 = * = * = * = * = " + settlementDate 
							+ "\r\n = * = * = * = * = 跳过结算步骤 = * = * = * = * = \r\n");
				} else {
					logger.debug(" = * = * = * = * = 当前日期 = * = * = * = * = " + today 
							+ "\r\n = * = * = * = * = 结算日期 = * = * = * = * = " + settlementDate 
							+ "\r\n = * = * = * = * = 日结开始时间 = * = * = * = * = " + DateUtil.currentDatetime() + "\r\n");
				}
				long lastMilliTime = DateUtil.getCurrentTimeMillis();
				while(settlementDate==null || Integer.valueOf(settlementDate)<Integer.valueOf(today)) {
					if(settlementDate==null) {
						settlementDate = today;
					} else {
						settlementDate = DateUtil.getNextDay(settlementDate);
					}
					Map<String, Object> settlementParam = new HashMap<String, Object>();
					settlementParam.put("settlement_date", settlementDate);
					if(paramMap.get("merch_id")!=null) {
						settlementParam.put("merch_id", paramMap.get("merch_id"));
						logger.debug(" = * = * = * = * = 结算日期 = * = * = * = * = " + settlementDate 
								+ "\r\n = * = * = * = * = 结算以下商户编码 = * = * = * = * = " + paramMap.get("merch_id") + "\r\n");
					} else {
						logger.debug(" = * = * = * = * = 结算日期 = * = * = * = * = " + settlementDate 
								+ "\r\n = * = * = * = * = 结算所有商户 = * = * = * = * = \r\n");
					}
					statisticsService.createDailySettlement(settlementParam);
					logger.debug(" = * = * = * = * = 结算日期 = * = * = * = * = " + settlementDate 
							+ "\r\n = * = * = * = * = 日结结束时间 = * = * = * = * = " + DateUtil.currentDatetime() 
							+ "\r\n = * = * = * = * = 用时 = * = * = * = * = " + (DateUtil.getCurrentTimeMillis()-lastMilliTime) + " ms\r\n");
					statisticsService.updateSystemSettlement(settlementDate);
					logger.debug(" = * = * = * = * = 更新结算日期 = * = * = * = * = " + settlementDate + "\r\n");
				}
			} catch (Exception e) {
				logger.error(" = * = * = * = * = StatisticsTask ERROR = * = * = * = * = ", e);
			}finally {
				SettlementLock.getLock().unlock();
			}
		} else {
			logger.debug("正在进行结算...");
		}
		logger.debug(" = * = * = * = * = 定时任务执行结束 = * = * = * = * = " + DateUtil.currentDatetime() + "\r\n");
	}

}
