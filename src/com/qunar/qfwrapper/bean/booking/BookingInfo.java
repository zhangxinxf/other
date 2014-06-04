package com.qunar.qfwrapper.bean.booking;

import java.util.Map;

public class BookingInfo {

	/**
	 * http的content-type，可以为空，默认给的是：text/html;charset=utf-8 
	 */
	private String contentType = "text/html;charset=utf-8";
	
	/**
	 * booking的URL
	 */
	private String action;
	
	/**
	 * get|post  
	 */
	private String method;
	
	/**
	 * 参数对
	 */
	private Map<String, String> inputs;
	
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Map<String, String> getInputs() {
		return inputs;
	}
	public void setInputs(Map<String, String> inputs) {
		this.inputs = inputs;
	}
	
	
}
