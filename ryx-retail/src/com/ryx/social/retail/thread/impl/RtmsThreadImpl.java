package com.ryx.social.retail.thread.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.HttpUtil;
import com.ryx.social.retail.service.impl.StatisticsServiceImpl;
import com.ryx.social.retail.thread.IRtmsThread;
import com.ryx.social.retail.util.RetailConfig;
import com.ryx.social.retail.util.ThreadConfig;
@Service
public class RtmsThreadImpl implements IRtmsThread {
	
	private Logger LOG = LoggerFactory.getLogger(StatisticsServiceImpl.class);
	
	public class SYNDataToRtmsThread implements Runnable {
		
		private Thread thread;  // 分配新的 线程 对象。
		
		public void start(){
			//异步同步数据是否开启
			if("true".equals(RetailConfig.getOpenDataUpload())){
				if(!ThreadConfig.receiveFind){ //若线程未开启则开启线程
					LOG.debug("开启一个线程");
					ThreadConfig.receiveFind=ThreadConfig.FIND_ENABLE;
					thread = new Thread(this);// this 是其 run 方法被调用的对象
					thread.start(); // 开始一个线程
//					System.out.println("开启一个线程");
				} else {
					LOG.debug("异步上传数据线程已开启");
				}
			}else{
				LOG.debug("异步同步数据已关闭！");
			}
		}
		
		public void stop() {//结束线程
			ThreadConfig.receiveFind=ThreadConfig.FIND_UNABLE;
			thread = null;
//			System.out.println("线程处于空闲状态，关闭线程");
			LOG.debug("线程处于空闲状态，关闭线程");
		}
		
		public void run(){
			while(!ThreadConfig.concurrentLinkedQueue.isEmpty()){
				Map<String,Object> map=(Map<String,Object>)ThreadConfig.concurrentLinkedQueue.remove();
				String url=map.get("url").toString();
				Map<String,String> dataMap=(Map<String,String>)map.get("data");
//				Map<String, Object> jsonMap=HttpPostUtil.postValueStringMap(RetailConfig.getTobaccoServer()+url, dataMap);
				String json = HttpUtil.post(RetailConfig.getTobaccoServer()+url, dataMap);
				/*
				 * 2014-06-27 去掉上传完成后，转换返回的数据，此处没有用到转换后的数据
				 * 张礼现
				 *
				Map<String, Object> itemMap = null;
				if(!StringUtils.isEmpty(json)){
					itemMap =JsonUtil.json2Map(json);
				}*/
				LOG.debug("异步上传数据url: " + url);
				LOG.debug("异步上传数据data: " + dataMap);
				LOG.debug("异步上传数据返回结果: " + json);
				
				if(ThreadConfig.concurrentLinkedQueue.isEmpty()) {
					try{
						Thread.sleep(5000);
					} catch(Exception e) {
						e.fillInStackTrace();
					}
				}
			}
			
			stop();
		}
	}
	
	public void startThread(){
		SYNDataToRtmsThread sdtr=new SYNDataToRtmsThread();
		sdtr.start();
	}
}
