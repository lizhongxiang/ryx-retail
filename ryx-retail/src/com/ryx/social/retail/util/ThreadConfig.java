package com.ryx.social.retail.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadConfig {
	 public static final boolean FIND_ENABLE = true;//FINDENABLE 启用
	 public static final boolean FIND_UNABLE = false;//FINDUNABLE 关闭
	 
	 public static boolean receiveFind = FIND_UNABLE ;//ReceiveFind 默认为线程关闭
	 
	 public static ConcurrentLinkedQueue<Object> concurrentLinkedQueue = new ConcurrentLinkedQueue<Object>();//创建队列
}
