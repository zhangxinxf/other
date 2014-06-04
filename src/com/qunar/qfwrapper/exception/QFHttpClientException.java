package com.qunar.qfwrapper.exception;

import org.apache.commons.httpclient.HttpException;

/**
 * Created with IntelliJ IDEA.
 * User: haiyin.wang
 * Date: 14-5-26
 * Time: 下午9:12
 * To change this template use File | Settings | File Templates.
 */
public class QFHttpClientException extends HttpException {

    public QFHttpClientException(int status){
        super("抓取失败，HTTP返回状态："+status);
    }

}
