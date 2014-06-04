package com.qunar.qfwrapper.bean.search;
/**
 * wrapper抓取参数类
 */
public class FlightSearchParam {
	/**
	 * 出发城市or出发机场三字码
	 */
	private String dep;

	/**
	 * 到达城市or到达机场三字码
	 */
	private String arr;
	/**
	 * 出发日期，格式为YYYY-MM-DD 例如2014-04-03
	 */
	private String depDate;
	/**
	 * 抓取器id
	 */
	private String wrapperid;
	/**
	 * http请求超时时间
	 */
	private String timeOut;
	/**
	 * 如果存在且有值，则说明使用qproxy；否则不使用
	 */
	private String token;
	/**
	 * 往返票返程日期，格式为YYYY-MM-DD，例如2014-09-14；如果是单程搜索，则该参数值为空
	 */
	private String retDate;

    private String queryId;

    public String getWrapperid() {
        return wrapperid;
    }

    public void setWrapperid(String wrapperid) {
        this.wrapperid = wrapperid;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getRetDate() {
		return retDate;
	}

	public void setRetDate(String retDate) {
		this.retDate = retDate;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return this.token;
	}
	public void setTimeOut(String timeOut) {
		this.timeOut = timeOut;
	}

	public String getTimeOut() {
		return this.timeOut;
	}

	public void setDep(String dep) {
		this.dep = dep;
	}

	public void setArr(String arr) {
		this.arr = arr;
	}

	public void setDepDate(String dept) {
		this.depDate = dept;
	}

	public String getDep() {
		return this.dep;
	}

	public String getArr() {
		return this.arr;
	}

	public String getDepDate() {
		return this.depDate;
	}

	public String toString() {
		return "FlightSearchParam{" + "wrapperid=" + wrapperid + ", dep=" + dep + ", arr='" + arr + '\'' + ", depdate='"
				+ depDate + '\'' +  '}';
	}

}
