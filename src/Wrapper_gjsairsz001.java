import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * 索蒙航空双程
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjsairsz001 implements QunarCrawler {

	private static final String EXCEPTION_INFO = "excetpion";

	// 表单提交界面
	private static final String postUrl = "http://booking.somonair.com/en/indexformprocessing";
	// 获取城市编码
	private static final String addressUrl = "http://booking.somonair.com/en/json/dependence-cities?isBooking=true&param=origin&type=json";
	private static final Map<String, String> city = new HashMap<String, String>();

	private static QFHttpClient httpClient = null;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("DXB");
		searchParam.setArr("URC");
		searchParam.setDepDate("2014-08-16");
		searchParam.setRetDate("2014-08-23");
		searchParam.setTimeOut("600000");
		searchParam.setToken("");
		new Wrapper_gjsairsz001().run(searchParam);
	}

	public void run(FlightSearchParam searchParam) {
		String html = "";
		try {

			String filePath = "G:\\air.html";
			File f = new File(filePath);
			// if (!f.exists()) {
			// html = new Wrapper_gjdairgr001().getHtml(searchParam);
			// html = html.replace("\\u003c", "<").replace("\\u003e", ">")
			// .replace("\\", "").replace("//", "");
			// Files.write(html, f, Charsets.UTF_8);
			// } else {
			// html = Files.toString(f, Charsets.UTF_8);
			// }

			html = new Wrapper_gjsairsz001().getHtml(searchParam);
			Files.write(html, f, Charsets.UTF_8);
			ProcessResultInfo result = new ProcessResultInfo();
			result = new Wrapper_gjsairsz001().process(html, searchParam);
			if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
				List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result
						.getData();
				for (RoundTripFlightInfo in : flightList) {
					System.out.println("returnInfo"
							+ in.getRetinfo().toString());
					System.out.println("OWDetail:" + in.getDetail());
					System.out.println("OWINFo:" + in.getInfo());
					System.err.println("*****************************");
				}
			} else {
				System.out.println(result.getStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		BookingResult bookingResult = new BookingResult();
		QFGetMethod get = null;
		// 生成http对象
		httpClient = new QFHttpClient(arg0, false);
		// 按照浏览器的模式来处理cookie
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		try {
			get = new QFGetMethod(addressUrl);
			int status = httpClient.executeMethod(get);
			if (status != HttpStatus.SC_OK) {
				bookingResult.setRet(false);
				return bookingResult;
			}
			String cityJson = get.getResponseBodyAsString();
			String cityStr = StringUtils.substringBetween(cityJson, ":{", "}")
					.replace("\"", "");
			String[] values = cityStr.split(",");
			for (String string : values) {
				String[] key_value = string.split(":");
				city.put(key_value[1], key_value[0]);
			}
			String[] serachArrDate = arg0.getDepDate().split("-");
			String[] arrDate = arg0.getRetDate().split("-");
			String thereDate = serachArrDate[2] + "." + serachArrDate[1] + "."
					+ serachArrDate[0];
			String backDate = arrDate[2] + "." + arrDate[1] + "." + arrDate[0];
			BookingInfo bookingInfo = new BookingInfo();
			bookingInfo.setAction(postUrl);
			bookingInfo.setMethod("post");
			Map<String, String> body = new LinkedHashMap<String, String>();
			body.put("back-date", backDate);
			body.put("count-aaa", "1");
			body.put("count-rbg", "0");
			body.put("count-rmg", "0");
			body.put("origin-city-name", city.get(arg0.getDep()));
			body.put("destination-city-name", city.get(arg0.getArr()));
			body.put("pricetable", "123");
			body.put("there-class", "2232");
			body.put("there-date", thereDate);
			body.put("use-back", "1");
			body.put("x", "40");
			body.put("y", "7");
			bookingInfo.setInputs(body);
			bookingResult.setData(bookingInfo);
			bookingResult.setRet(true);
		} catch (Exception e) {
			bookingResult.setRet(false);
		} finally {
			get.releaseConnection();
		}
		return bookingResult;
	}

	public String getHtml(FlightSearchParam arg0) {
		QFPostMethod post = null;
		QFGetMethod get = null;
		try {
			// 生成http对象
			httpClient = new QFHttpClient(arg0, false);
			// 按照浏览器的模式来处理cookie
			httpClient.getParams().setCookiePolicy(
					CookiePolicy.BROWSER_COMPATIBILITY);
			if (city.size() == 0) {
				get = new QFGetMethod(addressUrl);
				int status = httpClient.executeMethod(get);
				if (status != HttpStatus.SC_OK) {
					return EXCEPTION_INFO;
				}
				String cityJson = get.getResponseBodyAsString();
				String cityStr = StringUtils.substringBetween(cityJson, ":{",
						"}").replace("\"", "");
				String[] values = cityStr.split(",");
				for (String string : values) {
					String[] key_value = string.split(":");
					city.put(key_value[1], key_value[0]);
				}
				get.releaseConnection();
			}
			// 提交表单
			post = new QFPostMethod(postUrl);
			// 时间处理
			String[] serachArrDate = arg0.getDepDate().split("-");
			String[] arrDate = arg0.getRetDate().split("-");
			String thereDate = serachArrDate[2] + "." + serachArrDate[1] + "."
					+ serachArrDate[0];
			String backDate = arrDate[2] + "." + arrDate[1] + "." + arrDate[0];
			// 设置post提交表单数据
			NameValuePair[] parametersBody = new NameValuePair[] {
					new NameValuePair("back-date", backDate),
					new NameValuePair("count-aaa", "1"),
					new NameValuePair("count-rbg", "0"),
					new NameValuePair("count-rmg", "0"),
					new NameValuePair("origin-city-name", city.get(arg0
							.getDep())),
					new NameValuePair("destination-city-name", city.get(arg0
							.getArr())),
					new NameValuePair("pricetable", "123"),
					new NameValuePair("there-class", "2232"),
					new NameValuePair("there-date", thereDate),
					new NameValuePair("use-back", "1"),
					new NameValuePair("x", "40"), new NameValuePair("y", "7")

			};
			post.setRequestBody(parametersBody);
			post.setFollowRedirects(false);
			String cookie = StringUtils.join(
					httpClient.getState().getCookies(), ";");
			post.setRequestHeader("Cookie", cookie);
			post.setRequestHeader("Referer", "http://somonair.com/");
			int postStatus = httpClient.executeMethod(post);
			String url = "";
			if (postStatus == HttpStatus.SC_MOVED_TEMPORARILY
					|| postStatus == HttpStatus.SC_MOVED_PERMANENTLY) {
				Header location = post.getResponseHeader("Location");
				if (location != null) {
					url = location.getValue();
					if (!url.startsWith("http")) {
						url = post.getURI().getScheme()
								+ "://"
								+ post.getURI().getHost()
								+ (post.getURI().getPort() == -1 ? ""
										: (":" + post.getURI().getPort()))
								+ url;
					}
				}
			} else {
				return EXCEPTION_INFO;
			}
			get = new QFGetMethod(url);
			cookie = StringUtils.join(httpClient.getState().getCookies(), ";");
			get.setRequestHeader("Cookie", cookie);
			get.setRequestHeader("Referer", "http://somonair.com/");
			int getStatus = httpClient.executeMethod(get);
			if (getStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			get.releaseConnection();
			String getJsonurl = "http://booking.somonair.com/en/json/pricetableagency-prices-flights";
			cookie = StringUtils.join(httpClient.getState().getCookies(), ";");
			get.setRequestHeader("Cookie", cookie);
			get.setRequestHeader("Referer", url);
			get = new QFGetMethod(getJsonurl);
			int getJsonStatus = httpClient.executeMethod(get);
			if (getJsonStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			return get.getResponseBodyAsString();
		} catch (Exception e) {
			e.printStackTrace();
			return EXCEPTION_INFO;
		} finally {
			if (null != get) {
				get.releaseConnection();
			}
			if (null != post) {
				post.releaseConnection();
			}
		}
	}

	/*
	 * ProcessResultInfo中，
	 * ret为true时，status可以为：SUCCESS(抓取到机票价格)|NO_RESULT(无结果，没有可卖的机票) ret为false时
	 * ，status可以为:CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL
	 * |PARAM_ERROR
	 */
	public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {
		// 判断票是否已卖完
		String html = arg0;
		String deptDate = arg1.getDepDate();
		String retDate = arg1.getRetDate();
		ProcessResultInfo result = new ProcessResultInfo();
		if (EXCEPTION_INFO.equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		if (html.contains("thereNearestDates")
				|| html.contains("backNearestDates")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		if (StringUtils.isBlank(html)) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_AIRLINE);
			return result;
		}
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		if (html.contains("No flight found.")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
		try {
			// 获取tbody内容
			JSONArray jsonStr = JSONObject.parseArray(html);
			// 获取最低价
			JSONObject json = jsonStr.getJSONObject(0);
			String price = json.getString("price");
			String currency = json.getString("currency");
			JSONArray flightArray = json.getJSONArray("flight_variants");
			for (Object object : flightArray) {
				// 航线信息
				List<FlightSegement> info = new ArrayList<FlightSegement>();
				List<FlightSegement> retinfo = new ArrayList<FlightSegement>();
				// 航班号
				List<String> fliNo = new ArrayList<String>();
				List<String> retfliNo = new ArrayList<String>();
				// 航班明细
				FlightDetail detail = new FlightDetail();
				// 单条航班完整信息
				RoundTripFlightInfo round = new RoundTripFlightInfo();
				JSONObject flightInfo = (JSONObject) object;
				JSONArray detailJson = flightInfo.getJSONArray("flights");
				JSONArray backflightArray = flightInfo
						.getJSONArray("backward_flights");
				// 启程
				for (Object detailObject : detailJson) {
					FlightSegement flightSegement = new FlightSegement();
					JSONObject data = (JSONObject) detailObject;
					String company = data.getJSONObject("company").getString(
							"code");
					String racenumber = data.getString("racenumber");
					String flightNo = company  + racenumber;
					String depairport = data.getString("origincity");
					String arrairport = data.getString("destinationcity");
					String depDate = data.getString("departuredate");
					String airDate = data.getString("arrivaldate");
					String deptime = data.getString("departuretime");
					String arrtime = data.getString("arrivaltime");
					flightSegement.setFlightno(flightNo);
					flightSegement.setDepairport(depairport);
					flightSegement.setArrairport(arrairport);
					String[] depdates = depDate.split("\\.");
					flightSegement.setDepDate(depdates[2] + "-" + depdates[1]
							+ "-" + depdates[0]);
					String[] airdates = airDate.split("\\.");
					flightSegement.setArrDate(airdates[2] + "-" + airdates[1]
							+ "-" + airdates[0]);
					flightSegement.setDeptime(deptime.replaceAll("\\s", ""));
					flightSegement.setArrtime(arrtime.replaceAll("\\s", ""));
					//
					fliNo.add(flightNo);
					info.add(flightSegement);
				}
				// 返程
				for (Object detailObject : backflightArray) {
					JSONObject obj = (JSONObject) detailObject;
					JSONArray backInfoArray = obj.getJSONArray("flights");
					for (Object backArray : backInfoArray) {
						FlightSegement flightSegement = new FlightSegement();
						JSONObject data = (JSONObject) backArray;
						String company = data.getJSONObject("company")
								.getString("code");
						String racenumber = data.getString("racenumber");
						String flightNo = company + racenumber;
						String depairport = data.getString("origincity");
						String arrairport = data.getString("destinationcity");
						String depDate = data.getString("departuredate");
						String airDate = data.getString("arrivaldate");
						String deptime = data.getString("departuretime");
						String arrtime = data.getString("arrivaltime");
						flightSegement.setFlightno(flightNo);
						flightSegement.setDepairport(depairport);
						flightSegement.setArrairport(arrairport);
						String[] depdates = depDate.split("\\.");
						flightSegement.setDepDate(depdates[2] + "-"
								+ depdates[1] + "-" + depdates[0]);
						String[] airdates = airDate.split("\\.");
						flightSegement.setArrDate(airdates[2] + "-"
								+ airdates[1] + "-" + airdates[0]);
						flightSegement
								.setDeptime(deptime.replaceAll("\\s", ""));
						flightSegement
								.setArrtime(arrtime.replaceAll("\\s", ""));
						//
						retfliNo.add(flightNo);
						retinfo.add(flightSegement);
					}
				}
				detail.setMonetaryunit(currency);
				detail.setPrice(new Double(price));
				detail.setDepcity(arg1.getDep());
				detail.setArrcity(arg1.getArr());
				detail.setFlightno(fliNo);
				detail.setDepdate(dateFormat.parse(deptDate));
				detail.setWrapperid("gjsairsz001");
				round.setDetail(detail);
				round.setInfo(info);
				round.setRetdepdate(dateFormat.parse(retDate));
				round.setRetflightno(retfliNo);
				round.setRetinfo(retinfo);
				flightList.add(round);
			}
			result.setStatus(Constants.SUCCESS);
			result.setData(flightList);
			result.setRet(true);
		} catch (Exception e) {
			result.setStatus(Constants.PARSING_FAIL);
			result.setRet(false);
			e.printStackTrace();
		}
		return result;
	}
}
