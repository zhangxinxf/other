package succ;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * 马耳他航空单程抓取
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjdairkm001 implements QunarCrawler {
	private static Logger logger = LoggerFactory
			.getLogger(Wrapper_gjdairkm001.class);

	private static final String EXCEPTION_INFO = "excetpion";

	// 获取最低票价，及税费
	private static final String url = "https://reservations.airmalta.com/KMOnline/AirSelectOWCFlight.do";
	// 表单提交界面
	private static final String postUrl = "https://reservations.airmalta.com/KMOnline/AirLowFareSearchExternal.do";
	private static final String ajaxUrl = "https://reservations.airmalta.com/KMOnline/AirLowFareSearchExt.do";

	private static QFHttpClient httpClient = null;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("BCN");
		searchParam.setArr("MLA");
		searchParam.setDepDate("2014-07-15");
		searchParam.setTimeOut("600000");
		searchParam.setToken("");
		new Wrapper_gjdairkm001().run(searchParam);
	}

	public void run(FlightSearchParam searchParam) {
		String html = "";
		try {

			/*
			 * String filePath = "G:\\air.html"; File f = new File(filePath); if
			 * (!f.exists()) { html = new
			 * Wrapper_gjdairkm001().getHtml(searchParam); Files.write(html, f,
			 * Charsets.UTF_8); } else { html = Files.toString(f,
			 * Charsets.UTF_8); }
			 */

			html = new Wrapper_gjdairkm001().getHtml(searchParam);
			ProcessResultInfo result = new ProcessResultInfo();
			result = new Wrapper_gjdairkm001().process(html, searchParam);
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
		String[] dates = arg0.getDepDate().split("-");
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(postUrl);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("outboundOption.originLocationCode", arg0.getDep());
		map.put("outboundOption.destinationLocationCode", arg0.getArr());
		map.put("outboundOption.departureDay", dates[2]);
		map.put("outboundOption.departureMonth", dates[1]);
		map.put("outboundOption.departureYear", dates[0]);
		map.put("tripType", "OW");
		map.put("guestTypes[0].type", "ADT");
		map.put("guestTypes[1].type", "CHD");
		map.put("guestTypes[2].type", "INF");
		map.put("guestTypes[0].amount", "1");
		map.put("guestTypes[1].amount", "0");
		map.put("guestTypes[2].amount", "0");
		map.put("flexibleSearch", "true");
		map.put("lang", "en");
		map.put("pos", "AIRMALTA");
		map.put("directFlightsOnly", "false");
		bookingInfo.setInputs(map);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;

	}

	public String getHtml(FlightSearchParam arg0) {
		String getUrl = "";
		QFPostMethod post = null;
		QFGetMethod get = null;
		try {
			// 生成http对象
			httpClient = new QFHttpClient(arg0, false);
			// 按照浏览器的模式来处理cookie
			httpClient.getParams().setCookiePolicy(
					CookiePolicy.BROWSER_COMPATIBILITY);
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
			if (StringUtils.isBlank(ajaxResponseBody)) {
				return EXCEPTION_INFO;
			}
			ajaxResponseBody = ajaxResponseBody.replace("\n", "").replace("\t",
					"");
			ajaxResponseBody = ajaxResponseBody.substring(0,
					ajaxResponseBody.indexOf("/*"));
			JSONObject ajaxJson = JSONObject.parseObject(ajaxResponseBody);
			String errorInfo = ajaxJson.getString("errors");
			if (!StringUtils.isBlank(errorInfo)) {
				return Constants.INVALID_AIRLINE;
			}
			String redirect = ajaxJson.getString("redirect");
			String status = ajaxJson.getString("status");
			if (StringUtils.isBlank(status) || !status.equals("success")) {
				return EXCEPTION_INFO;
			}
			// 获取机票列表url，进行获取信息
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
		String html = arg0;
		String deptDate = arg1.getDepDate();// 首次出发时间
		ProcessResultInfo result = new ProcessResultInfo();
		if (EXCEPTION_INFO.equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		if (html.equals(Constants.INVALID_AIRLINE)) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_AIRLINE);
			return result;
		}
		if (html.contains("No flights found according to your search criteria. Please try again.")) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_AIRLINE);
			return result;
		}
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		if (html.contains("There are no flights available on your selected date(s), please choose other dates to continue")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		try {
			// 获取tbody内容
			String tbody = StringUtils
					.substringBetween(html, "<tbody>", "</tbody>");
			tbody = tbody.replace("\n", "").replace("\t", "")
					.replace("</tr></table>", "</java>");
			// 获取所有tr
			String reg = "(<tr(.+?)>(.+?))</td></tr>";
			Pattern pt = Pattern.compile(reg);
			Matcher titles = pt.matcher(tbody);
			// 匹配div
			String divReg = "<div(.+?)>(.+?)</div>";
			// 匹配a标签
			pt = Pattern.compile(divReg);
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			// 航班信息
			List<FlightSegement> info = new ArrayList<FlightSegement>();
			// 票价信息
			FlightDetail detail = new FlightDetail();
			
			// 获取航班列表信息
			while (titles.find()) {
				OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
				String trLine = titles.group();
				if (trLine.contains("</java>")) {
					// 还原真实的html内容
					trLine = trLine.replace("</java>", "</tr></table>");
				}
				FlightSegement seg = new FlightSegement();
				Matcher divs = pt.matcher(trLine);
				int i = 1;
				// 获取某航班信息
				while (divs.find()) {
					String divContent = divs.group();
					if (divContent.contains("N/A") && i == 1) {
						break;
					}
					if (divContent.contains("Hurry only")) {
						continue;
					}
					switch (i) {
					case 1:// 获取航班号
						divContent = StringUtils.substringBeforeLast(
								divContent, "</a>");
						divContent = StringUtils.substringAfterLast(divContent,
								">");
						seg.setFlightno(divContent);
						break;
					case 2:// 获取出发时间
						divContent = StringUtils.substringBetween(divContent,
								">", "</div>");
						String deptTime = divContent;
						if (divContent.contains("span")) {
							divContent = divContent.replace("<span>", "")
									.replace("</span>", "");
							String[] dates = divContent.split("\\+");
							int day = Integer.parseInt(dates[1]);
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(dateFormat.parse(deptDate));
							calendar.set(Calendar.DAY_OF_MONTH,
									calendar.get(Calendar.DAY_OF_MONTH) + day);
							seg.setDepDate(dateFormat.format(calendar.getTime()));
							seg.setDeptime(dates[0]);
						} else {
							seg.setDeptime(deptTime);
							seg.setDepDate(deptDate);
						}
						break;
					case 3:
						// 获取到达时间
						divContent = StringUtils.substringBetween(divContent,
								">", "</div>");
						String arrTime = divContent;
						if (divContent.contains("span")) {
							divContent = divContent.replace("<span>", "")
									.replace("</span>", "");
							String[] dates = divContent.split("\\+");
							int day = Integer.parseInt(dates[1]);
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(dateFormat.parse(deptDate));
							calendar.set(Calendar.DAY_OF_MONTH,
									calendar.get(Calendar.DAY_OF_MONTH) + day);
							seg.setArrDate(dateFormat.format(calendar.getTime()));
							seg.setArrtime(dates[0]);
						} else {
							seg.setArrtime(arrTime);
							seg.setArrDate(deptDate);
						}
						break;
					case 4:
						divContent = StringUtils.substringBetween(divContent,
								"style=\"display:none\">", "</span>");
						// 起飞站-降落站
						String[] airLine = divContent.split("-");
						seg.setDepairport(airLine[0]);
						seg.setArrairport(airLine[1]);
						break;
					case 5:
						if (divContent.contains("radio")
								&& !divContent.contains("N/A")) {
							divContent = StringUtils.substringBetween(
									divContent, "flightSelectGr", "onclick")
									.replace("\"", "");
							String[] ids = divContent.split("_");
							String idsValues = StringUtils.join(ids, ",");
							idsValues = idsValues.substring(1,
									idsValues.length());
							QFPostMethod post = new QFPostMethod(url);
							// 设置post提交表单数据
							NameValuePair[] parametersBody = new NameValuePair[] {
									new NameValuePair(
											"isFareFamilySearchResult", "true"),
									new NameValuePair("selectedItineraries",
											idsValues),
									new NameValuePair("selectedFlightIds",
											idsValues),
									new NameValuePair(
											"combinabilityReloadRequired",
											"false"),
									new NameValuePair("flightIndex", ""),
									new NameValuePair("flowStep",
											"AIR_COMBINABLE_FARE_FAMILIES_FLEXIBLE_SEARCH_RESULTS"),
									new NameValuePair("alignment", "horizontal"),
									new NameValuePair("context", "airSelection") };
							post.setRequestBody(parametersBody);

							int status = httpClient.executeMethod(post);
							if (status == HttpStatus.SC_OK) {
								//
							}
							String body = post.getResponseBodyAsString();
							body = StringUtils.substringBeforeLast(body, "/*");
							JSONObject json = JSONObject.parseObject(body);
							String data = json.getString("bottomBot");
							String priceData = StringUtils
									.substringBeforeLast(data,
											"<div class=\"bodySection collapsedSection\">");
							priceData = StringUtils.substringAfterLast(
									priceData, "<table");
							String totalPrice = StringUtils
									.substringBetween(
											data,
											"<div id=\"airSelection_SummaryBot_Bottom_totalPrice\" class=\"total\">",
											"</div>");
							totalPrice = totalPrice.replace("\n", "").replace(
									"\t", "");
							String priceReg = "(\\d)+(\\.){1}\\d+";
							Pattern pricePattern = Pattern.compile(priceReg);
							Matcher priceMatcher = pricePattern
									.matcher(priceData);
							if (priceMatcher.find()) {
								// EUS 212.25
								String[] currency = totalPrice.split(" ");
								// 设置货币单位
								detail.setMonetaryunit(currency[0].trim());
								String price = priceMatcher.group();
								if (detail.getPrice() == 0.0) {
									detail.setPrice(new Double(price));
								}
								if (detail.getTax() == 0.0) {
									String tax = String.format(
											"%.2f",
											new Double(currency[1])
													- detail.getPrice());
									detail.setTax(new Double(tax));
								}
							}
							post.releaseConnection();
						}
						break;
					case 6:
						if (divContent.contains("radio")
								&& !divContent.contains("N/A")) {
							divContent = StringUtils.substringBetween(
									divContent, "flightSelectGr", "onclick")
									.replace("\"", "");
							String[] ids = divContent.split("_");
							String idsValues = StringUtils.join(ids, ",");
							idsValues = idsValues.substring(1,
									idsValues.length());
							QFPostMethod post = new QFPostMethod(url);
							// 设置post提交表单数据
							NameValuePair[] parametersBody = new NameValuePair[] {
									new NameValuePair(
											"isFareFamilySearchResult", "true"),
									new NameValuePair("selectedItineraries",
											idsValues),
									new NameValuePair("selectedFlightIds",
											idsValues),
									new NameValuePair(
											"combinabilityReloadRequired",
											"false"),
									new NameValuePair("flightIndex", ""),
									new NameValuePair("flowStep",
											"AIR_COMBINABLE_FARE_FAMILIES_FLEXIBLE_SEARCH_RESULTS"),
									new NameValuePair("alignment", "horizontal"),
									new NameValuePair("context", "airSelection") };
							post.setRequestBody(parametersBody);

							int status = httpClient.executeMethod(post);
							if (status == HttpStatus.SC_OK) {
								//
							}
							String body = post.getResponseBodyAsString();
							body = StringUtils.substringBeforeLast(body, "/*");
							JSONObject json = JSONObject.parseObject(body);
							String data = json.getString("bottomBot");
							String priceData = StringUtils
									.substringBeforeLast(data,
											"<div class=\"bodySection collapsedSection\">");
							priceData = StringUtils.substringAfterLast(
									priceData, "<table");
							String totalPrice = StringUtils
									.substringBetween(
											data,
											"<div id=\"airSelection_SummaryBot_Bottom_totalPrice\" class=\"total\">",
											"</div>");
							totalPrice = totalPrice.replace("\n", "").replace(
									"\t", "");
							String priceReg = "(\\d)+(\\.){1}\\d+";
							Pattern pricePattern = Pattern.compile(priceReg);
							Matcher priceMatcher = pricePattern
									.matcher(priceData);
							if (priceMatcher.find()) {
								// EUS 212.25
								String[] currency = totalPrice.split(" ");
								// 设置货币单位
								detail.setMonetaryunit(currency[0].trim());
								String price = priceMatcher.group();
								if (detail.getPrice() == 0.0) {
									detail.setPrice(new Double(price));
								}
								if (detail.getTax() == 0.0) {
									String tax = new Double(currency[1])
											- detail.getPrice() + "";
									String fr = String.format("%.2f", tax);
									detail.setTax(new Double(fr));
								}
							}
							post.releaseConnection();
						}
						break;
					case 7:
						if (divContent.contains("radio")
								&& !divContent.contains("N/A")) {
							divContent = StringUtils.substringBetween(
									divContent, "flightSelectGr", "onclick")
									.replace("\"", "");
							String[] ids = divContent.split("_");
							String idsValues = StringUtils.join(ids, ",");
							idsValues = idsValues.substring(1,
									idsValues.length());
							QFPostMethod post = new QFPostMethod(url);
							// 设置post提交表单数据
							NameValuePair[] parametersBody = new NameValuePair[] {
									new NameValuePair(
											"isFareFamilySearchResult", "true"),
									new NameValuePair("selectedItineraries",
											idsValues),
									new NameValuePair("selectedFlightIds",
											idsValues),
									new NameValuePair(
											"combinabilityReloadRequired",
											"false"),
									new NameValuePair("flightIndex", ""),
									new NameValuePair("flowStep",
											"AIR_COMBINABLE_FARE_FAMILIES_FLEXIBLE_SEARCH_RESULTS"),
									new NameValuePair("alignment", "horizontal"),
									new NameValuePair("context", "airSelection") };
							post.setRequestBody(parametersBody);

							int status = httpClient.executeMethod(post);
							if (status == HttpStatus.SC_OK) {
								//
							}
							String body = post.getResponseBodyAsString();
							body = StringUtils.substringBeforeLast(body, "/*");
							JSONObject json = JSONObject.parseObject(body);
							String data = json.getString("bottomBot");
							String priceData = StringUtils
									.substringBeforeLast(data,
											"<div class=\"bodySection collapsedSection\">");
							priceData = StringUtils.substringAfterLast(
									priceData, "<table");
							String totalPrice = StringUtils
									.substringBetween(
											data,
											"<div id=\"airSelection_SummaryBot_Bottom_totalPrice\" class=\"total\">",
											"</div>");
							totalPrice = totalPrice.replace("\n", "").replace(
									"\t", "");
							String priceReg = "(\\d)+(\\.){1}\\d+";
							Pattern pricePattern = Pattern.compile(priceReg);
							Matcher priceMatcher = pricePattern
									.matcher(priceData);
							if (priceMatcher.find()) {
								// EUS 212.25
								String[] currency = totalPrice.split(" ");
								// 设置货币单位
								detail.setMonetaryunit(currency[0].trim());
								String price = priceMatcher.group();
								if (detail.getPrice() == 0.0) {
									detail.setPrice(new Double(price));
								}
								if (detail.getTax() == 0.0) {
									String tax = new Double(currency[1])
											- detail.getPrice() + "";
									String fr = String.format("%.2f", tax);
									detail.setTax(new Double(fr));
								}
							}
							post.releaseConnection();
						}
						break;
					case 8:
						if (divContent.contains("radio")
								&& !divContent.contains("N/A")) {
							divContent = StringUtils.substringBetween(
									divContent, "flightSelectGr", "onclick")
									.replace("\"", "");
							String[] ids = divContent.split("_");
							String idsValues = StringUtils.join(ids, ",");
							idsValues = idsValues.substring(1,
									idsValues.length());
							QFPostMethod post = new QFPostMethod(url);
							// 设置post提交表单数据
							NameValuePair[] parametersBody = new NameValuePair[] {
									new NameValuePair(
											"isFareFamilySearchResult", "true"),
									new NameValuePair("selectedItineraries",
											idsValues),
									new NameValuePair("selectedFlightIds",
											idsValues),
									new NameValuePair(
											"combinabilityReloadRequired",
											"false"),
									new NameValuePair("flightIndex", ""),
									new NameValuePair("flowStep",
											"AIR_COMBINABLE_FARE_FAMILIES_FLEXIBLE_SEARCH_RESULTS"),
									new NameValuePair("alignment", "horizontal"),
									new NameValuePair("context", "airSelection") };
							post.setRequestBody(parametersBody);

							int status = httpClient.executeMethod(post);
							if (status == HttpStatus.SC_OK) {
								//
							}
							String body = post.getResponseBodyAsString();
							body = StringUtils.substringBeforeLast(body, "/*");
							JSONObject json = JSONObject.parseObject(body);
							String data = json.getString("bottomBot");
							String priceData = StringUtils
									.substringBeforeLast(data,
											"<div class=\"bodySection collapsedSection\">");
							priceData = StringUtils.substringAfterLast(
									priceData, "<table");
							String totalPrice = StringUtils
									.substringBetween(
											data,
											"<div id=\"airSelection_SummaryBot_Bottom_totalPrice\" class=\"total\">",
											"</div>");
							totalPrice = totalPrice.replace("\n", "").replace(
									"\t", "");
							String priceReg = "(\\d)+(\\.){1}\\d+";
							Pattern pricePattern = Pattern.compile(priceReg);
							Matcher priceMatcher = pricePattern
									.matcher(priceData);
							if (priceMatcher.find()) {
								// EUS 212.25
								String[] currency = totalPrice.split(" ");
								// 设置货币单位
								detail.setMonetaryunit(currency[0].trim());
								String price = priceMatcher.group();
								if (detail.getPrice() == 0.0) {
									detail.setPrice(new Double(price));
								}
								if (detail.getTax() == 0.0) {
									String tax = new Double(currency[1])
											- detail.getPrice() + "";
									String fr = String.format("%.2f", tax);
									detail.setTax(new Double(fr));
								}
							}
							post.releaseConnection();
						}
						break;
					default:
						break;
					}
					i++;
				}
				// 根据此字段判断有转机情况
				boolean flag = false;
				// 添加完整信息
				if ((seg.getDepairport().equals(arg1.getDep()) && seg
						.getArrairport().equals(arg1.getArr()))) {
					flag = true;
				} else {
					if (info.size() > 0) {
						FlightSegement onSeg = (info.get(info.size() - 1));
						if (onSeg.getDepairport().equals(arg1.getDep())
								&& onSeg.getArrairport().equals(
										seg.getDepairport())
								&& seg.getArrairport().equals(arg1.getArr())) {
							flag = true;
						}
					}
				}
				// 添加航班信息
				info.add(seg);
				if (flag) {
					// 添加明细信息
					detail.setDepcity(arg1.getDep());
					detail.setArrcity(arg1.getArr());
					detail.setDepdate(dateFormat.parse(arg1.getDepDate()));
					// 获取航班号
					List<String> flightno = new ArrayList<String>();
					for (FlightSegement flightSegement : info) {
						flightno.add(flightSegement.getFlightno());
					}
					detail.setFlightno(flightno);
					oneWayFlightInfo.setDetail(detail);
					oneWayFlightInfo.setInfo(info);
					flightList.add(oneWayFlightInfo);
					detail.setWrapperid("gjdairkm001");
					detail = new FlightDetail();
					info = new ArrayList<FlightSegement>();
				}
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
