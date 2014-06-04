package com.qunar.qfwrapper.util;

import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.exception.QFHttpClientException;
import org.apache.commons.httpclient.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


public class QFHttpClient extends HttpClient {

	private int TIME_OUT = 60000;
    private FlightSearchParam flightSearchParam = null;
	private Logger wrapperHttpInfo = LoggerFactory.getLogger("wrapperHttpInfo");

	public QFHttpClient(FlightSearchParam flightSearchParam, boolean ifAddConnectionCloseHeader) {
        this.flightSearchParam = flightSearchParam;
		init(flightSearchParam.getWrapperid(), ifAddConnectionCloseHeader, flightSearchParam.getTimeOut(),flightSearchParam.getToken());
	}

	private void init(String codebase, boolean ifAddConnectionCloseHeader, String timeout, String token) {
		if(null != timeout && timeout.length()>0){
            this.TIME_OUT = Integer.parseInt(timeout);
        }

		Collection<Header> headers = new ArrayList<Header>();
		headers.add(new Header("Accept", "*/*"));
		headers.add(new Header("Accept-Language", "zh-cn"));
		headers.add(new Header("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)"));
		headers.add(new Header("UA-CPU", "x86"));
		headers.add(new Header("Accept-Encoding", "gzip, deflate"));
		if (ifAddConnectionCloseHeader) {
			headers.add(new Header("Connection", "close"));
		}

		this.getHttpConnectionManager().getParams().setConnectionTimeout(TIME_OUT);
		this.getHttpConnectionManager().getParams().setSoTimeout(TIME_OUT);

		this.getParams().setParameter("http.default-headers", headers);
	}

	public int executeMethod(HttpMethod method) throws IOException, HttpException, QFHttpClientException{
		URI uri = method.getURI();
		int status = super.executeMethod(method);
        if (!StringUtils.isEmpty(flightSearchParam.getWrapperid())) {
            wrapperHttpInfo.info("queryId: " + flightSearchParam.getQueryId()
                    + "WrapperID: " + flightSearchParam.getWrapperid()
                    + ", URL: " + uri
                    + ", Method: " + method.getName()
                    + ", Status: " + status);
        }
        if (status >= 400) {
            wrapperHttpInfo.warn("抓取失败，HTTP返回状态："+status);
            throw new QFHttpClientException(status);
        }

		return status;
	}
}
