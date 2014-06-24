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
	private static final String postUrl = "http://flight.asiatravel.com/crs.flight/www/flight.aspx?lan=en-US";
	// 主页面
	private static final String root = "http://flight.asiatravel.com/crs.flight/www/flight.aspx?lan=en-US";
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
		searchParam.setDep("BJS");
		searchParam.setArr("HKG");
		searchParam.setDepDate("2014-07-12");
		searchParam.setTimeOut("600000");
		searchParam.setToken("");
		new Wrapper_gjdweb00031().run(searchParam);
	}

	public void run(FlightSearchParam searchParam) {
		String html = "";
		try {
			String filePath = "G:\\air.html";
			File f = new File(filePath);
			if (!f.exists()) {
				html = new Wrapper_gjdweb00031().getHtml(searchParam);
				Files.write(html, f, Charsets.UTF_8);
			} else {
				html = Files.toString(f, Charsets.UTF_8);
			}

			html = new Wrapper_gjdweb00031().getHtml(searchParam);
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
			// 释放连接
			get.releaseConnection();
			String viewState = StringUtils.substringBetween(response,
					"id=\"__VIEWSTATE\" value=\"", "\"");
			Files.write(viewState, new File("G:\\view.txt"), Charsets.UTF_8);
			post = new QFPostMethod(postUrl);
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
							"QuickSearch_View$CitySelect_DepartCity$DDL_CountryList",
							"107"),
					new NameValuePair(
							"QuickSearch_View$CitySelect_DepartCity$DDL_CityList",
							"BJS"),
					new NameValuePair(
							"QuickSearch_View$CitySelect_ReturnCity$TextBox_CityCode",
							arg0.getArr()),
					new NameValuePair(
							"QuickSearch_View$CitySelect_ReturnCity$DDL_CountryList",
							"108"),
					new NameValuePair(
							"QuickSearch_View$CitySelect_ReturnCity$DDL_CityList",
							"HKG"),
					new NameValuePair("QuickSearch_View$RouteType",
							"Radio_oneway"),
					new NameValuePair(
							"QuickSearch_View$DateSelection_DepartSml$hidden_SelectedDate",
							"24/June/2014"),
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
							"QuickSearch_View$DateSelection_Returnsml$hidden_SelectedDate",
							"26/June/2014"),
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
			post.setRequestHeader("Content-Type",
					"application/x-www-form-urlencoded");
			post.setRequestBody(parametersBody);
			post.setFollowRedirects(false);
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
			int getStatus = httpClient.executeMethod(get);
			if (getStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			// 释放连接
			get.releaseConnection();
			String processUrl = url.replace("Search.Wait.aspx",
					"Search.Wait.Process.aspx");
			get = new QFGetMethod(processUrl);
			int waitsucc = httpClient.executeMethod(get);
			Files.write(get.getResponseBodyAsString(),
					new File("G://wait.html"), Charsets.UTF_8);
			// 释放连接
			get.releaseConnection();
			String waitUrl = url.replace("Search.Wait.aspx", "Search.aspx");
			get = new QFGetMethod(waitUrl);
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
		boolean flag = false;
		String html = arg0;
		String deptDate = arg1.getDepDate();
		QFPostMethod post = null;
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
		if (html.contains("No flight found.")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		try {
			// 获取tbody内容
			String tbody = html.replace("\\u003c", "<").replace("\\u003e", ">")
					.replace("\\", "").replace("//", "");
			String table = StringUtils.substringBetween(tbody, "<table",
					"</table>");
			// 获取所有tr
			String[] trs = StringUtils
					.substringsBetween(table, "<tr>", "</tr>");

			// 获取航班列表信息
			for (int i = 1; i < trs.length; i++) {
				String content = trs[i];
				// 当前没有票
				String[] tds = StringUtils.substringsBetween(content,
						"<td class=\"BodyCOL6\">", "</td>");
				if (tds[0].contains("arrow.png") && tds[1].contains("Sold Out")) {
					continue;
				}
				String value = "";
				if (!tds[0].contains("arrow.png")) {
					value = StringUtils.substringBetween(tds[0], "value=\"",
							"\"");
				} else {
					value = StringUtils.substringBetween(tds[1], "value=\"",
							"\"");
				}
				try {
					post = new QFPostMethod(url);
					post.setRequestHeader("Content-Type",
							"application/json; charset=utf-8");
					// 设置post提交表单数据
					JSONObject body = new JSONObject();
					body.put("strOutward", value);
					body.put("strReturn", "");
					body.put("strIpAddress", "");
					post.setRequestBody(body.toJSONString());
					int postStatus = httpClient.executeMethod(post);
					if (postStatus != HttpStatus.SC_OK) {
						continue;
					}
					flag = true;
					String resultDetail = post.getResponseBodyAsString()
							.replace("\\u003c", "<").replace("\\u003e", ">")
							.replace("\\", "").replace("//", "");
					String[] detailTable = StringUtils.substringsBetween(
							resultDetail, "<table", "</table>");
					// 获取航线信息
					String[] fliTd = StringUtils.substringsBetween(
							detailTable[0], "<tr>", "</tr>");
					// 航线信息
					List<FlightSegement> info = new ArrayList<FlightSegement>();
					// 航班号
					List<String> fliNo = new ArrayList<String>();
					// 单条航班完整信息
					OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
					FlightDetail detail = new FlightDetail();
					for (int j = 1; j < fliTd.length; j++) {
						String trConent = fliTd[j];
						FlightSegement flightSegement = new FlightSegement();
						String flightNo = StringUtils.substringBetween(
								trConent, "<td class=\"BodyCOL1\">", "</td>")
								.replaceAll("\\s", "");
						// 截取字符串
						String reg = "\\d+";
						Pattern pricePattern = Pattern.compile(reg);
						Matcher priceMatcher = pricePattern.matcher(flightNo);
						if (priceMatcher.find()) {
							flightNo = flightNo.substring(0, 2)
									+ priceMatcher.group();
						}
						String depairport = StringUtils.substringBetween(
								trConent, "<td class=\"BodyCOL2\">", "</td>");
						String arrairport = StringUtils.substringBetween(
								trConent, "<td class=\"BodyCOL3\">", "</td>");
						String airDate = StringUtils.substringBetween(trConent,
								"<td class=\"BodyCOL4\">", "</td>");
						String deptime = StringUtils.substringBetween(trConent,
								"<td class=\"BodyCOL5\">", "</td>");
						String arrtime = StringUtils.substringBetween(trConent,
								"<td class=\"BodyCOL6\">", "</td>");
						flightSegement.setFlightno(flightNo);
						flightSegement.setDepairport(city.get(depairport));
						flightSegement.setArrairport(city.get(arrairport));
						flightSegement.setDepDate(arg1.getDepDate());
						String[] airdates = airDate.split("/");
						flightSegement.setArrDate(airdates[2] + "-"
								+ airdates[1] + "-" + airdates[0]);
						flightSegement
								.setDeptime(deptime.replaceAll("\\s", ""));
						flightSegement
								.setArrtime(arrtime.replaceAll("\\s", ""));
						//
						fliNo.add(flightNo);
						info.add(flightSegement);
					}
					// 获取价格
					String[] priceTr = StringUtils.substringsBetween(
							detailTable[1], "<tr>", "</tr>");
					String priceContent = priceTr[1];
					// 获取票价和总价
					String priceStr = StringUtils.substringBetween(
							priceContent, "<td class=\"BodyCOL3\">", "</td>")
							.replace("t", "");
					String totalPriceStr = StringUtils.substringBetween(
							priceContent, "<td class=\"BodyCOL4\">", "</td>")
							.replace("t", "");
					String priceReg = "(\\d)+(\\.){1}\\d+";
					Pattern pricePattern = Pattern.compile(priceReg);
					Matcher priceMatcher = pricePattern.matcher(totalPriceStr);

					String price = "0";
					String totalPrice = "0";
					if (priceMatcher.find())
						totalPrice = priceMatcher.group();
					priceMatcher = pricePattern.matcher(priceStr);
					if (priceMatcher.find())
						price = priceMatcher.group();
					String tax = String.format("%.2f", new Double(totalPrice)
							- new Double(price));
					detail.setMonetaryunit("GBP");
					detail.setPrice(new Double(price));
					detail.setDepcity(arg1.getDep());
					detail.setArrcity(arg1.getArr());
					detail.setTax(new Double(tax));
					detail.setFlightno(fliNo);
					detail.setDepdate(dateFormat.parse(deptDate));
					detail.setWrapperid("gjdairgr001");
					oneWayFlightInfo.setDetail(detail);
					oneWayFlightInfo.setInfo(info);
					flightList.add(oneWayFlightInfo);
				} catch (Exception e) {
					e.printStackTrace();
					result.setStatus(Constants.PARSING_FAIL);
					result.setRet(false);
					return result;
				} finally {
					post.releaseConnection();
				}
			}
			if (!flag) {
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
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
