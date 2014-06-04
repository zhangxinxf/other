package com.qunar.qfwrapper.bean.search;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

public class FlightDetail {
	/**
	 * 出发城市或机场，三字码
	 */
	protected String depcity;
	/**
	 * 到达城市或机场，三字码
	 */
	protected String arrcity;
	/**
	 * 出发日期，格式为YYYY-MM-DD
	 */
	protected Date depdate;
	/**
	 * 航班号list，航班号一般在航班有中转时为多个
	 */
	protected List<String> flightno = Lists.newArrayList();
	/**
	 * 货币单位
	 */
	protected String monetaryunit;//货币单位
	/**
	 * 税费
	 */
	protected double tax;
	/**
	 * 最低票价
	 */
	protected double price;
	/**
	 * wrapper的id
	 */
	protected String wrapperid;

	protected Timestamp createtime;
	
	protected Timestamp updatetime;
	
	protected String source;
	
	protected int status;

	public String getDepcity() {
		return depcity;
	}

	public void setDepcity(String depcity) {
		this.depcity = depcity;
	}

	public String getArrcity() {
		return arrcity;
	}

	public void setArrcity(String arrcity) {
		this.arrcity = arrcity;
	}

	public Date getDepdate() {
		return depdate;
	}

	public void setDepdate(Date depdate) {
		this.depdate = depdate;
	}

	public List<String> getFlightno() {
		return flightno;
	}

	public void setFlightno(List<String> flightno) {
		this.flightno = flightno;
	}

	public String getMonetaryunit() {
		return monetaryunit;
	}

	public void setMonetaryunit(String monetaryunit) {
		this.monetaryunit = monetaryunit;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getWrapperid() {
		return wrapperid;
	}

	public void setWrapperid(String wrapperid) {
		this.wrapperid = wrapperid;
	}
	@Override
	public String toString() {
		return "FlightDetail [ depcity=" + depcity + ", arrcity=" + arrcity + ", depdate=" + depdate + ", flightno="
				+ flightno + ", monetaryunit=" + monetaryunit + ", tax=" + tax + ",  price=" + price + ", wrapperid="
				+ wrapperid + "]";
	}
	public Timestamp getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}


}