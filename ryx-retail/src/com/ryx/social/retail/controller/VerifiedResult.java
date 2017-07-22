package com.ryx.social.retail.controller;


public class VerifiedResult {
	private boolean flag;
	private String message;
	private Object data;
	public VerifiedResult() {
		this.flag = true;
	}
	public VerifiedResult(boolean flag) {
		this.flag = flag;
	}
	public VerifiedResult(String message) {
		this.flag = false;
		this.message = message;
	}
	public VerifiedResult(Object data) {
		this.flag = true;
		this.data = data;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public VerifiedResult setData(Object data) {
		this.data = data;
		return this;
	}
	@Override
	public String toString() {
		return flag + " " + message + " " + data;
	}
}
