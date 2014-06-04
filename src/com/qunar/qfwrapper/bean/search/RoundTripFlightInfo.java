package com.qunar.qfwrapper.bean.search;

import java.util.Date;
import java.util.List;

import com.qunar.qfwrapper.bean.search.BaseFlightInfo;

import com.google.common.collect.Lists;

/**
 *  往返航程bean
 *
 */
public class RoundTripFlightInfo extends BaseFlightInfo {
  
    /**
     * 返程日期，格式为YYYY-MM-DD，例如2014-06-12
     */
    private Date retdepdate;
    /**
     * 返程航班号list，航班号一般在航班有中转时为多个
     */
    private List<String> retflightno = Lists.newArrayList();  //返程航班号列表
    /**
     * 返程航段信息
     */
    private List<FlightSegement> retinfo = Lists.newArrayList();  //返程航班信息列表
    
    /**
     * 去程价格
     */
    private double outboundPrice;
    
    /**
     * 返程价格
     */
    private double returnedPrice;

    /**
     * 无参构造方法
     */
    public RoundTripFlightInfo() {
        super();
    }
    
    /**
     * @param goInfo        去程航段list
     * @param goDetail      去程航班详细信息
     * @param retdepdate	返程出发日期
     * @param retflightno	返程航班号list，一般在航班有中转时，需要储存多个航班号
     * @param retinfo		返程航段list
     */
    public RoundTripFlightInfo(List<FlightSegement> goInfo, FlightDetail goDetail, Date retdepdate, List<String> retflightno, List<FlightSegement> retinfo) {
        this.info = goInfo;
        this.detail = goDetail;
        this.retdepdate = retdepdate;
        this.retflightno = retflightno;
        this.retinfo = retinfo;
    }

    public Date getRetdepdate() {
        return retdepdate;
    }

    public void setRetdepdate(Date retdepdate) {
        this.retdepdate = retdepdate;
    }

    public List<String> getRetflightno() {
        return retflightno;
    }

    public void setRetflightno(List<String> retflightno) {
        this.retflightno = retflightno;
    }

    public List<FlightSegement> getRetinfo() {
        return retinfo;
    }

    public void setRetinfo(List<FlightSegement> retinfo) {
        this.retinfo = retinfo;
    }

	public double getOutboundPrice() {
		return outboundPrice;
	}

	public void setOutboundPrice(double outboundPrice) {
		this.outboundPrice = outboundPrice;
	}

	public double getReturnedPrice() {
		return returnedPrice;
	}

	public void setReturnedPrice(double returnedPrice) {
		this.returnedPrice = returnedPrice;
	}
    @Override
    public String toString() {
        return "RoundTripFlightInfo [retdepdate=" + retdepdate + ", retflightno=" + retflightno + ", retinfo=" + retinfo + ", info=" + info + ", detail=" + detail + "]";
    }


}
