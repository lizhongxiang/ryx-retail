package com.ryx.social.retail.util;

public class WarehousingLock {
	
	private boolean isLock;

	private static WarehousingLock lock = null;
	
	private WarehousingLock() {
		isLock = false;
	}
	
	public static WarehousingLock getLock() {
		if(lock==null) lock = new WarehousingLock();
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
