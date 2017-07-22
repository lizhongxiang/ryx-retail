package com.ryx.social.retail.util;

public class SettlementLock {
	
	private boolean isLock;
	
	private static SettlementLock lock = null;
	
	private SettlementLock() {
		isLock = false;
	}
	
	public static SettlementLock getLock() {
		if(lock==null) lock = new SettlementLock();
		return lock;
	}
	
	public void unlock() {
		isLock = false;
	}
	
	public void lock() {
		isLock = true;
	}
	
	public boolean isLocked() {
		return isLock;
	}
	
	public boolean isUnlocked() {
		return !isLock;
	}
	
}

