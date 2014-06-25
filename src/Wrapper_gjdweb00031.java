import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.font.EAttribute;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
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

/**
 * Asiatravel航空单程
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjdweb00031 implements QunarCrawler {
	private static Logger logger = LoggerFactory
			.getLogger(Wrapper_gjdweb00031.class);

	private static final String EXCEPTION_INFO = "excetpion";

	// 获取最低票价，及税费
	private static final String url = "http://www.aurigny.com/WebService/B2cService.asmx/AddFlight";
	// 表单提交界面
	private static final String postUrl = "http://flight.asiatravel.com/crs.flight/www/flight.aspx?scode=&lan=en-US";
	// 主页面
	private static final String root = "http://flight.asiatravel.com/crs.flight/www/flight.aspx?scode=&lan=en-US";
	private static final Map<String, String> city = new HashMap<String, String>();

	static {
		city.put("Alderney", "ACI");
		city.put("Bristol", "BRS");
		city.put("Dinard", "DNR");
		city.put("East Midlands", "EMA");
		city.put("Grenoble", "GNB");
		city.put("Guernsey", "GCI");
		city.put("Jersey", "JER");
		city.put("London City Airport", "LCY");
		city.put("London Gatwick", "LGW");
		city.put("London Stansted", "STN");
		city.put("Manchester", "MAN");
		city.put("Southampton", "SOU");
	}

	private static QFHttpClient httpClient = null;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("HGR");
		searchParam.setArr("BJS");
		searchParam.setDepDate("2014-07-12");
		searchParam.setTimeOut("600000");
		searchParam.setWrapperid("gjdweb00031");
		searchParam.setToken("");
		new Wrapper_gjdweb00031().run(searchParam);
	}

	public void run(FlightSearchParam searchParam) {
		String html = "";
		try {
			String filePath = "D:\\k.html";
			File f = new File(filePath);
			if (!f.exists()) {
				html = new Wrapper_gjdweb00031().getHtml(searchParam);
				Files.write(html, f, Charsets.UTF_8);
			} else {
				html = Files.toString(f, Charsets.UTF_8);
			}

			// html = new Wrapper_gjdweb00031().getHtml(searchParam);
			ProcessResultInfo result = new ProcessResultInfo();
			result = new Wrapper_gjdweb00031().process(html, searchParam);
			if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
				List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result
						.getData();
				for (OneWayFlightInfo in : flightList) {
					System.out
							.println("************" + in.getInfo().toString());
					System.out.println("++++++++++++"
							+ in.getDetail().toString());
				}
			} else {
				System.out.println(result.getStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		String dates = arg0.getDepDate().replace("-", "");
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(root);
		bookingInfo.setMethod("get");
		bookingInfo.setContentType("application/json; charset=utf-8");
		Map<String, String> body = new LinkedHashMap<String, String>();
		body.put("origin", arg0.getDep());
		body.put("destination", arg0.getArr());
		body.put("dateFrom", dates);
		body.put("dateTo", dates);
		body.put("iOneWay", "true");
		body.put("iFlightOnly", "0");
		body.put("iAdult", "1");
		body.put("iChild", "0");
		body.put("iInfant", "0");
		body.put("BoardingClass", "Y");
		body.put("CurrencyCode", "");
		body.put("strPromoCode", "");
		body.put("SearchType", "FARE");
		body.put("iOther", "0");
		body.put("otherType", "");
		body.put("strIpAddress", "");
		bookingInfo.setInputs(body);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
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
			get = new QFGetMethod(root);
			int status = httpClient.executeMethod(get);
			if (status != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			String response = get.getResponseBodyAsString();
			String viewState = StringUtils.substringBetween(response,
					"id=\"__VIEWSTATE\" value=\"", "\"");
			// 提交表单
			post = new QFPostMethod(postUrl);
			post.setRequestHeader("Referer", root);
			// 时间处理
			String[] serachArrDate = arg0.getDepDate().split("-");
			// 设置post提交表单数据
			NameValuePair[] parametersBody = new NameValuePair[] {
					new NameValuePair("__EVENTTARGET", ""),
					new NameValuePair("__EVENTARGUMENT", ""),
					new NameValuePair("__LASTFOCUS", ""),
					new NameValuePair("__VIEWSTATE", viewState),
					new NameValuePair(
							"__PREVIOUSPAGE",
							"2JqiabMc4EgWU0bvYRKKBVjZF6UK7JqbOdne0ZZalyI0jiX6BeH5VtxQ0-9XIOOE3FfS8QtmQ-zsi2vme_2oFSPb_IhhL1vR20b1OEuHk1os9WYI0"),
					new NameValuePair(
							"DisplayTopSubmenuMain1$DisplayLanguageSelector1$Dropdownlist_Languages",
							"en-US"),
					new NameValuePair(
							"QuickSearch_View$CitySelect_DepartCity$TextBox_CityCode",
							arg0.getDep()),
					new NameValuePair(
							"QuickSearch_View$CitySelect_ReturnCity$TextBox_CityCode",
							arg0.getArr()),
					new NameValuePair("QuickSearch_View$RouteType",
							"Radio_oneway"),
					new NameValuePair(
							"QuickSearch_View$DateSelection_DepartSml$Dropdownlist_Days",
							serachArrDate[2]),
					new NameValuePair(
							"QuickSearch_View$DateSelection_DepartSml$Dropdownlist_Month",
							Integer.parseInt(serachArrDate[1]) + ""),
					new NameValuePair(
							"QuickSearch_View$DateSelection_DepartSml$Dropdownlist_Year",
							serachArrDate[0]),
					new NameValuePair(
							"QuickSearch_View$DateSelection_DepartSml$Dropdownlist_Timing",
							"ANY"),
					new NameValuePair(
							"QuickSearch_View$Dropdownlist_SeatClass",
							"Economy"),
					new NameValuePair(
							"QuickSearch_View$Radiobuttonlist_SearchType", "0"),
					new NameValuePair(
							"QuickSearch_View$CarrierSelect_Carrier$TextBox_CarrierCode",
							""),
					new NameValuePair("QuickSearch_View$RadioButton_Sort", "0"),
					new NameValuePair("QuickSearch_View$Dropdownlist_Adult",
							"1"),
					new NameValuePair("QuickSearch_View$Dropdownlist_child",
							"0"),
					new NameValuePair("QuickSearch_View$Button_find", "Find"),
					new NameValuePair("QuickSearch_View$TextBox_AdvSearch",
							"False") };
			// post.setRequestEntity(new
			// ByteArrayRequestEntity(raw.getBytes(),"application/x-www-form-urlencoded"));
			post.setRequestBody(parametersBody);
			post.setFollowRedirects(false);
			String cookie = StringUtils.join(
					httpClient.getState().getCookies(), ";");
			post.setRequestHeader("Cookie", cookie);
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
			get.setRequestHeader("Referer", postUrl);
			int getStatus = httpClient.executeMethod(get);
			if (getStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			String processUrl = url.replace("Search.Wait.aspx",
					"Search.Wait.Process.aspx");
			get = new QFGetMethod(processUrl);
			cookie = StringUtils.join(httpClient.getState().getCookies(), ";");
			get.setRequestHeader("Cookie", cookie);
			get.setRequestHeader("Referer", url);
			int proces = httpClient.executeMethod(get);
			if (proces != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}

			String waitUrl = url.replace("Search.Wait.aspx", "Search.aspx");
			get = new QFGetMethod(waitUrl);
			cookie = StringUtils.join(httpClient.getState().getCookies(), ";");
			get.setRequestHeader("Referer", url);
			get.setRequestHeader("Cookie", cookie);
			int succ = httpClient.executeMethod(get);
			if (succ != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			return get.getResponseBodyAsString();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取网站信息异常", e);
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
		ProcessResultInfo result = new ProcessResultInfo();
		if (EXCEPTION_INFO.equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		if (html.contains("Please enter a new date")) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;
		}
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		// if (html.contains("No flight found.")) {
		// result.setRet(true);
		// result.setStatus(Constants.NO_RESULT);
		// return result;
		// }
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		String priceReg = "(\\d)+,?\\d+(\\.){1}\\d+";
		Pattern pricePattern = Pattern.compile(priceReg);
		try {
			// 截取字符串
			StringBuffer str = new StringBuffer("<table class=\"box1\"");
			String table = StringUtils.substringAfter(html, str.toString());
			str.append(table);
			table = StringUtils.substringBefore(str.toString(),
					"TicketingConditions");

			// 第一个table和最后一个table是分页信息
			String tables[] = table.split("</tr><tr>");
			if (tables.length <= 2) {
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			// 航线信息
			List<FlightSegement> info = new ArrayList<FlightSegement>();
			// 航班号
			List<String> fliNo = new ArrayList<String>();
			// 单条航班完整信息
			OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
			FlightDetail detail = new FlightDetail();
			// 获取航班列表信息
			for (int i = 1; i < tables.length - 1; i++) {
				String trConent = tables[i];
				// 获取当前水
				String priceTable = StringUtils
						.substringBetween(
								trConent,
								"<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">",
								"</table>");

				String price = "";
				String tax = "";
				String monetaryunit = StringUtils.substringBetween(priceTable,
						"<td class=\"price\">", "<span");
				Matcher priceMatcher = pricePattern.matcher(priceTable);
				int num = 0;
				while (priceMatcher.find()) {
					if (num == 0)
						price = priceMatcher.group();
					if (num == 1) {
						tax = priceMatcher.group();
						break;
					}
					num++;
				}
				// 获取航班信息
				String[] fliInfo = StringUtils.substringsBetween(priceTable,
						"<tr class=\"back_lighter_2\">", "</tr>");
				for (int n = 0; n < fliInfo.length - 1; n++) {
					FlightSegement flightSegement = new FlightSegement();
					String flightNo = StringUtils.substringBetween(trConent,
							"<td class=\"BodyCOL1\">", "</td>").replaceAll("\\s",
							"");
					flightSegement.setFlightno(flightNo);
					flightSegement.setDepairport("");
					flightSegement.setArrairport("");
					flightSegement.setDepDate(arg1.getDepDate());
					String[] airdates = airDate.split("/");
					flightSegement.setArrDate(airdates[2] + "-" + airdates[1] + "-"
							+ airdates[0]);
					flightSegement.setDeptime(deptime.replaceAll("\\s", ""));
					flightSegement.setArrtime(arrtime.replaceAll("\\s", ""));
					//
					fliNo.add(flightNo);
					info.add(flightSegement);
				}
			
			

				String depairport = StringUtils.substringBetween(trConent,
						"<td class=\"BodyCOL2\">", "</td>");
				String arrairport = StringUtils.substringBetween(trConent,
						"<td class=\"BodyCOL3\">", "</td>");
				String airDate = StringUtils.substringBetween(trConent,
						"<td class=\"BodyCOL4\">", "</td>");
				String deptime = StringUtils.substringBetween(trConent,
						"<td class=\"BodyCOL5\">", "</td>");
				String arrtime = StringUtils.substringBetween(trConent,
						"<td class=\"BodyCOL6\">", "</td>");
			

				detail.setMonetaryunit(monetaryunit);
				detail.setPrice(new Double(price.replaceAll(",", "")));
				detail.setDepcity(arg1.getDep());
				detail.setArrcity(arg1.getArr());
				detail.setTax(new Double(tax.replaceAll(",", "")));
				detail.setFlightno(fliNo);
				detail.setDepdate(dateFormat.parse(deptDate));
				detail.setWrapperid("gjdairgr001");
				oneWayFlightInfo.setDetail(detail);
				oneWayFlightInfo.setInfo(info);
				flightList.add(oneWayFlightInfo);
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
