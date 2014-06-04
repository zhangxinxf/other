package com.qunar.qfwrapper.bean.booking;

public class BookingResult  {

	/**
	 * 是否成功获取booking信息，如果是，则在data中存储
	 */
	private boolean ret;
	/**
	 * booking信息
	 */
	private BookingInfo data;
	
	public boolean isRet() {
		return ret;
	}
	public void setRet(boolean ret) {
		this.ret = ret;
	}
	public BookingInfo getData() {
		return data;
	}
	public void setData(BookingInfo data) {
		this.data = data;
	}
		
}
