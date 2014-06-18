package com.qunar.qfwrapper.bean.search;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

public class FlightSegement {

	/**
	 * 出发机场三字码
	 */
	private String depairport;
	/**
	 * 到达机场三字码
	 */
	private String arrairport;
	/**
	 * 航班号
	 */
	private String flightno;
	/**
	 * 出发时间，24小时制，例如09:45
	 */
	private String deptime;
	/**
	 * 到达时间，24小时制,例如15:35
	 */
	private String arrtime;
	/**
	 * 每一段的起飞日期 默认为第一段的起飞日期
	 */
	private String depDate;
	/**
	 * 每一段的到达日期 默认为第一段的到达日期
	 */
	private String arrDate;
	
	private String avcanbin;
	/**
	 * 航空公司二字码
	 */
	private String company;

	public FlightSegement(String dep, String arr, String fn, String deptime, String arrtime, String depDate,String arrDate, String company) {
		this.depairport = dep;
		this.arrairport = arr;
		this.flightno = fn;
		this.deptime = deptime;
		this.arrtime = arrtime;
		this.depDate = depDate;
		this.arrDate = arrDate;
        this.company = company;
	}
	
	public FlightSegement(){
		super();
	}
	/*
	 * "虚假航段信息"构造函数
	 */
	public FlightSegement(String fn) {
		setFlightno(fn);
	}
	
	public String forHiveLog() {
		return StringUtils.join(Lists.newArrayList(depairport,arrairport,company,flightno,deptime,arrtime,avcanbin), "^");
	}

	public String getDepairport() {
		return depairport;
	}

	public void setDepairport(String depairport) {
		this.depairport = depairport;
	}

	public String getArrairport() {
		return arrairport;
	}

	public void setArrairport(String arrairport) {
		this.arrairport = arrairport;
	}

	public String getFlightno() {
		return flightno;
	}

	public void setFlightno(String flightno) {
		this.flightno = flightno;
		this.company = StringUtils.substring(flightno, 0, 2);
	}

	public String getDeptime() {
		return deptime;
	}

	public void setDeptime(String deptime) {
		this.deptime = deptime;
	}

	public String getArrtime() {
		return arrtime;
	}

	public void setArrtime(String arrtime) {
		this.arrtime = arrtime;
	}

	public String getDepDate() {
		return depDate;
	}

	public void setDepDate(String depDate) {
		this.depDate = depDate;
	}
	

	public String getArrDate() {
		return arrDate;
	}

	public void setArrDate(String arrDate) {
		this.arrDate = arrDate;
	}
	
	

	public String getAvcanbin() {
		return avcanbin;
	}

	public void setAvcanbin(String avcanbin) {
		this.avcanbin = avcanbin;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	@Override
	public String toString() {
		return "FlightSegement [depairport=" + depairport + ", arrairport=" + arrairport + ", flightno=" + flightno
				+ ", company=" + company + ", deptime=" + deptime + ", arrtime=" + arrtime + ", depdate=" + depDate + ", arrDate=" + arrDate + ", arrDate=" + arrDate+ "]";
	}
}