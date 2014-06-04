package com.qunar.qfwrapper.bean.search;

import com.qunar.qfwrapper.bean.search.BaseFlightInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author runjing.qiu
 * 页面抓取后，处理后的结果类。
 */
public class ProcessResultInfo {

    /**
     * 返回用户的key，是reqeust.getQueryString()
     */
    private String key;
    /**
     * 是否正确解析到机票信息，如果是，则在data中存储航班信息
     */
    private boolean ret;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public List<? extends BaseFlightInfo> getData() {
        return data;
    }

    public void setData(List<? extends BaseFlightInfo> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
	 *  航班的list，网页抓取解析后可能存在符合条件的多个航班
	 */

	private List<? extends BaseFlightInfo> data = new ArrayList<BaseFlightInfo>();

	/**
	 * status取值统一从constants中调用
	 */
	private String status = "";



}
