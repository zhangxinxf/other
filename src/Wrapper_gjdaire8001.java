import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

public class Wrapper_gjdaire8001 implements QunarCrawler {

	private static final String EXCEPTION_INFO = "Excetpion";
	private static Logger logger = LoggerFactory
			.getLogger(Wrapper_gjdaire8001.class);

	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("BCN");
		searchParam.setArr("MLA");
		searchParam.setDepDate("2014-07-19");
		searchParam.setTimeOut("600000");
		searchParam.setToken("");

		String html = new Wrapper_gjdaire8001().getHtml(searchParam);
		 ProcessResultInfo result = new ProcessResultInfo();
		 result = new Wrapper_gjdaire8001().process(html, searchParam);
		 if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
		 List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result
		 .getData();
		 for (OneWayFlightInfo in : flightList) {
		 System.out.println("************" + in.getInfo().toString());
		 System.out.println("++++++++++++" + in.getDetail().toString());
		 }
		 } else {
		 System.out.println(result.getStatus());
		 }
	}

	public BookingResult getBookingInfo(FlightSearchParam arg0) {

		String bookingUrlPre = "http://ashley4.com/webaccess/cityairways/fareresult.php";
		BookingResult bookingResult = new BookingResult();

		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("ro", "0");
		map.put("from", arg0.getDep());
		map.put("to", arg0.getArr());
		map.put("cur", "HKD");
		map.put("sdate", arg0.getDepDate().replaceAll("-", "/"));
		map.put("edate", arg0.getDepDate().replaceAll("-", "/"));
		map.put("adult", "1");
		map.put("child", "0");
		map.put("infant", "0");
		map.put("view", "0");
		map.put("btnsubmit", "Flight Search");
		bookingInfo.setInputs(map);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;

	}

	public String getHtml(FlightSearchParam arg0) {
		// 表单提交界面
		String postUrl = "https://reservations.airmalta.com/KMOnline/AirLowFareSearchExternal.do";
		String ajaxUrl = "https://reservations.airmalta.com/KMOnline/AirLowFareSearchExt.do";
		String getUrl = "";
		QFPostMethod post = null;
		QFGetMethod get = null;
		try {
			// 生成http对象
			QFHttpClient httpClient = new QFHttpClient(arg0, false);
			// 获取年月日
			String[] dates = arg0.getDepDate().split("-");
			post = new QFPostMethod(postUrl);
			// 设置post提交表单数据
			NameValuePair[] parametersBody = new NameValuePair[] {
					new NameValuePair("outboundOption.originLocationCode",
							arg0.getDep()),
					new NameValuePair("outboundOption.destinationLocationCode",
							arg0.getArr()),
					new NameValuePair("outboundOption.departureDay", dates[2]),
					new NameValuePair("outboundOption.departureMonth", dates[1]),
					new NameValuePair("outboundOption.departureYear", dates[0]),
					new NameValuePair("tripType", "OW"),
					new NameValuePair("guestTypes[0].type", "ADT"),
					new NameValuePair("guestTypes[1].type", "CHD"),
					new NameValuePair("guestTypes[2].type", "INF"),
					new NameValuePair("guestTypes[0].amount", "1"),
					new NameValuePair("guestTypes[1].amount", "0"),
					new NameValuePair("guestTypes[2].amount", "0"),
					new NameValuePair("flexibleSearch", "true"),
					new NameValuePair("lang", "en"),
					new NameValuePair("pos", "AIRMALTA"),
					new NameValuePair("directFlightsOnly", "false") };
			post.setRequestBody(parametersBody);
			int postStatus = httpClient.executeMethod(post);
			if (postStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			post = null;
			post = new QFPostMethod(ajaxUrl);
			NameValuePair[] ajaxBody = new NameValuePair[] { new NameValuePair(
					"ajaxAction", "true") };
			post.setRequestBody(ajaxBody);
			int ajaxPostStatus = httpClient.executeMethod(post);
			if (ajaxPostStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			String ajaxResponseBody = post.getResponseBodyAsString();
			ajaxResponseBody = ajaxResponseBody.replace("\n", "").replace("\t",
					"");
			ajaxResponseBody = ajaxResponseBody.substring(0,
					ajaxResponseBody.indexOf("/*"));
			JSONObject ajaxJson = JSONObject.parseObject(ajaxResponseBody);
			String redirect = ajaxJson.getString("redirect");
			String status = ajaxJson.getString("status");
			if (StringUtils.isBlank(status) || !status.equals("success")) {
				return EXCEPTION_INFO;
			}
			// 获取机票里列表界面url，进行获取信息
			getUrl = "https://reservations.airmalta.com" + "/" + redirect;
			get = new QFGetMethod(getUrl);
			int getStatus = httpClient.executeMethod(get);
			if (getStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			return get.getResponseBodyAsString();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取网站信息异常", e);
		} finally {
			if (null != get) {
				get.releaseConnection();
			}

			if (null != post) {
				post.releaseConnection();
			}
		}
		return EXCEPTION_INFO;
	}

	public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {
		String html = arg0;

		/*
		 * ProcessResultInfo中，
		 * ret为true时，status可以为：SUCCESS(抓取到机票价格)|NO_RESULT(无结果，没有可卖的机票)
		 * ret为false时
		 * ，status可以为:CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL
		 * |PARAM_ERROR
		 */
		ProcessResultInfo result = new ProcessResultInfo();
		if ("Exception".equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		if (html.contains("Today Flight is full, select an other day or check later for any seat released. ")) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;
		}

		String jsonStr = org.apache.commons.lang.StringUtils.substringBetween(
				html, "var json = '", "';");
		try {
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			JSONArray ajson = JSON.parseArray(jsonStr);
			for (int i = 0; i < ajson.size(); i++) {
				OneWayFlightInfo baseFlight = new OneWayFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();
				FlightSegement seg = new FlightSegement();
				List<String> flightNoList = new ArrayList<String>();
				JSONObject ojson = ajson.getJSONObject(i);
				String flightNo = ojson.getString("flight").replaceAll(
						"[^a-zA-Z\\d]", "");
				flightNoList.add(flightNo);
				seg.setFlightno(flightNo);
				seg.setDepDate(ojson.getString("date"));
				seg.setDepairport(ojson.getString("org"));
				seg.setArrairport(ojson.getString("dst"));
				seg.setDeptime(ojson.getString("dep"));
				seg.setArrtime(ojson.getString("arr"));
				flightDetail.setDepdate(ojson.getDate("date"));
				JSONArray classArray = ojson.getJSONArray("class");
				double price = 0;
				String cur = "";
				for (int j = 0; j < classArray.size(); j++) {
					JSONObject jsonObject = classArray.getJSONObject(j);
					if (StringUtils.isEmpty(cur)) {
						cur = jsonObject.getString("cur");
					}
					double tmpPrice = jsonObject.getDouble("adult");
					if (0 == price || price > tmpPrice) {
						price = tmpPrice;
					}
				}
				flightDetail.setFlightno(flightNoList);
				flightDetail.setMonetaryunit(cur);
				flightDetail.setPrice(price);
				flightDetail.setDepcity(arg1.getDep());
				flightDetail.setArrcity(arg1.getArr());
				flightDetail.setWrapperid(arg1.getWrapperid());
				segs.add(seg);
				baseFlight.setDetail(flightDetail);
				baseFlight.setInfo(segs);
				flightList.add(baseFlight);
			}
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			result.setData(flightList);
			return result;
		} catch (Exception e) {
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			return result;
		}
	}

}
