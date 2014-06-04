package com.qunar.qfwrapper.interfaces;


import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;

/**
 * wrapper接口
 *
 */
public interface QunarCrawler {

	/**
	 * @param param 抓取网页时传入的参数，一般有出发城市三字码、到达城市三字码、出发时间......
	 * @return      网页代码
	 */
	String getHtml(FlightSearchParam param);
	/**
	 * @param html   抓取的网页
	 * @param param  抓取网页的链接的参数
	 * @return       封装的结果bean，包含航班list和process状态
	 */
	ProcessResultInfo process(String html,FlightSearchParam param);
	/**
	 * @param param  booking时的参数
	 * @return       booking的信息
	 */
	 BookingResult getBookingInfo(FlightSearchParam param);


}