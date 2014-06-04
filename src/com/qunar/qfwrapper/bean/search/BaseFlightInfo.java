package com.qunar.qfwrapper.bean.search;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 航班信息基类
 * 字类
 * OneWayFlightInfo 单航
 * RoundTripFlightInfo 往返
 */
public abstract class BaseFlightInfo {
	
    /**
     *  list储存航段信息
     */
    protected List<FlightSegement> info = Lists.newArrayList();
    /**
     * detail储存航线的基本信息（包括票价，税费等）
     */
    protected FlightDetail detail = new FlightDetail();

    public List<FlightSegement> getInfo() {
        return info;
    }

    public void setInfo(List<FlightSegement> info) {
        this.info = info;
    }

    public FlightDetail getDetail() {
        return detail;
    }

    public void setDetail(FlightDetail detail) {
        this.detail = detail;
    }
    
}