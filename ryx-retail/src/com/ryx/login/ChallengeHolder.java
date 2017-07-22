package com.ryx.login;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;

public class ChallengeHolder {
	
	// 清除过期挑战码使用的锁
	private static final Object holderLock = new Object();
	// 最大空闲挑战码数量 10W个预计能撑1500个并发 1分钟 占用内存不超过10M
	private static final int IDLE_CHANLLENGES_MAX_NUMBER = 99999;
	// 空闲挑战码过期比例
	private static final double IDLE_CHANLLENGES_EXPIRED_RATIO = .7;
	// 挑战码id -> 挑战码的映射
	private static HashMap<String, String> challenges = new HashMap<String, String>();
	// 挑战码id的列表
	private static LinkedList<String> challengeIds = new LinkedList<String>();
	
	public static void setChallenge(String challengeId, String challengeValue) {
		Assert.notNull(challengeId, "challengeId in ChallengeHolder should NOT be NULL");
		Assert.notNull(challengeValue, "challengeValue in ChallengeHolder should NOT be NULL");
		// 超过挑战码保存数量的最大上限时开始清空
		if(challenges.size() > IDLE_CHANLLENGES_MAX_NUMBER) {
			synchronized(holderLock) { // 锁之
				if(challenges.size() > IDLE_CHANLLENGES_MAX_NUMBER) { // 被唤醒的线程不用再清理过期挑战码
					// 找到过期的挑战码id
					List<String> expiredIds = challengeIds.subList(0, ((int) (IDLE_CHANLLENGES_MAX_NUMBER*IDLE_CHANLLENGES_EXPIRED_RATIO)));
					// 将过期的挑战码id从列表和映射中删除
					// 直接从challengeIds removeAll expiredIds会报错ConcurrentModificationException
					Iterator<String> idIter = expiredIds.iterator();
					while(idIter.hasNext()) {
						String expiredId = idIter.next();
						idIter.remove();
						challenges.remove(expiredId);
					}
				}
			}
		}
		challenges.put(challengeId, challengeValue);
		challengeIds.add(challengeId);
	}
	
	public static boolean validateChanllenge(String challengeId, String challengeValue) {
		return challenges.containsKey(challengeId) && challenges.get(challengeId).equals(challengeValue);
	}
	
	public static String removeChanllenge(String challengeId) {
		challengeIds.remove(challengeId);
		return challenges.remove(challengeId);
	}
	
	public static String getChanllenge(String challengeId) {
		return challenges.get(challengeId);
	}
	
	public static HashMap<String, String> getChanllenges() {
		return challenges;
	}
	
}
