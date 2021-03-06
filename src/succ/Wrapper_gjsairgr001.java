package succ;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.BaseFlightInfo;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * Aurigny航空往返
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjsairgr001 implements QunarCrawler {
	private static Logger logger = LoggerFactory
			.getLogger(Wrapper_gjsairgr001.class);

	private static final String EXCEPTION_INFO = "excetpion";
	// 获取最低票价，及税费
	private static final String url = "http://www.aurigny.com/WebService/B2cService.asmx/AddFlight";
	// 表单提交界面
	private static final String postUrl = "http://www.aurigny.com/WebService/B2cService.asmx/GetAvailability";
	// 首页
	private static final String root = "http://www.aurigny.com";
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
		searchParam.setDep("JER");
		searchParam.setArr("ACI");
		searchParam.setDepDate("2014-07-07");
		searchParam.setTimeOut("600000");
		searchParam.setRetDate("2014-07-07");
		searchParam.setToken("");
		new Wrapper_gjsairgr001().run(searchParam);
	}

	public void run(FlightSearchParam searchParam) {
		String html = "";
		try {
			 String filePath = "G:\\air.html";
			// File f = new File(filePath);
			// if (!f.exists()) {
			// html = new Wrapper_gjsairgr001().getHtml(searchParam);
			// html = html.replace("\\u003c", "<").replace("\\u003e", ">")
			// .replace("\\", "").replace("//", "");
			// Files.write(html, f, Charsets.UTF_8);
			// } else {
			// html = Files.toString(f, Charsets.UTF_8);
			// }

			long startTime = System.currentTimeMillis();
			html = new Wrapper_gjsairgr001().getHtml(searchParam);
			ProcessResultInfo result = new ProcessResultInfo();
			result = new Wrapper_gjsairgr001().process(html, searchParam);
			if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
				List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result
						.getData();
				System.out.println("数量:"+flightList.size());
				for (RoundTripFlightInfo in : flightList) {
					System.out.println("returnInfo"
							+ in.getRetinfo().toString());
					System.out.println("OWDetail:" + in.getDetail());
					System.out.println("OWINFo:" + in.getInfo());
					System.out.println("\n\n");
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
		String retdates = arg0.getRetDate().replace("-", "");
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(root);
		bookingInfo.setMethod("get");
		bookingInfo.setContentType("application/json; charset=utf-8");
		Map<String, String> body = new LinkedHashMap<String, String>();
		body.put("origin", arg0.getDep());
		body.put("destination", arg0.getArr());
		body.put("dateFrom", dates);
		body.put("dateTo", retdates);
		body.put("iOneWay", "false");
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
			// 转换日期格式
			String dates = arg0.getDepDate().replace("-", "");
			post = new QFPostMethod(postUrl);
			post.setRequestHeader("Content-Type",
					"application/json; charset=utf-8");
			// 设置post提交表单数据
			JSONObject body = new JSONObject();
			body.put("origin", arg0.getDep());
			body.put("destination", arg0.getArr());
			body.put("dateFrom", dates);
			body.put("dateTo", dates);
			body.put("iOneWay", true);
			body.put("iFlightOnly", "0");
			body.put("iAdult", 1);
			body.put("iChild", 0);
			body.put("iInfant", 0);
			body.put("BoardingClass", "Y");
			body.put("CurrencyCode", "");
			body.put("strPromoCode", "");
			body.put("SearchType", "FARE");
			body.put("iOther", "0");
			body.put("otherType", "");
			body.put("strIpAddress", "");
			post.setRequestBody(body.toJSONString());
			int postStatus = httpClient.executeMethod(post);
			if (postStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			return post.getResponseBodyAsString();
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
	public ProcessResultInfo process(String html, FlightSearchParam arg1) {
		ProcessResultInfo result = new ProcessResultInfo();
		if (EXCEPTION_INFO.equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		if (html.contains("Invalid Date") ) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;
		}
		if (html.contains("No flights found according to your search criteria. Please try again.")|| html.contains("{003}")) {
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
		QFPostMethod post = null;
		try {
			// 获取tbody内容
			String tbody = html.replace("\\u003c", "<").replace("\\u003e", ">").replace("\\", "").replace("//", "")
					.replace("\n", "").replace("\t", "").replace("\r", "");
			String table = StringUtils.substringBetween(tbody, "<table",
					"</table>");
			// 单程
			ProcessResultInfo inway = getFlightInfoByStatus(table, arg1, 0);

			String retdates = arg1.getRetDate().replace("-", "");
			post = new QFPostMethod(postUrl);
			post.setRequestHeader("Content-Type",
					"application/json; charset=utf-8");
			// 设置post提交表单数据
			JSONObject body = new JSONObject();
			body.put("origin", arg1.getArr());
			body.put("destination", arg1.getDep());
			body.put("dateFrom", retdates);
			body.put("dateTo", retdates);
			body.put("iOneWay", true);
			body.put("iFlightOnly", "0");
			body.put("iAdult", 1);
			body.put("iChild", 0);
			body.put("iInfant", 0);
			body.put("BoardingClass", "Y");
			body.put("CurrencyCode", "");
			body.put("strPromoCode", "");
			body.put("SearchType", "FARE");
			body.put("iOther", "0");
			body.put("otherType", "");
			body.put("strIpAddress", "");
			post.setRequestBody(body.toJSONString());
			int postStatus = httpClient.executeMethod(post);
			if (postStatus != 200) {
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			String retHtml= post.getResponseBodyAsString();
			String rettbody = retHtml.replace("\\u003c", "<").replace("\\u003e", ">")
					.replace("\n", "").replace("\t", "").replace("\r", "")
					.replace("\\", "").replace("//", "");
			String rettable = StringUtils.substringBetween(rettbody, "<table",
					"</table>");
			// 往返
			ProcessResultInfo outway = getFlightInfoByStatus(rettable, arg1, 1);
			if (!inway.isRet()) {
				return inway;
			}
			if (!outway.isRet()) {
				return outway;
			}
			if ((inway.isRet() && inway.getStatus().equals(Constants.NO_RESULT))
					|| (outway.isRet() && outway.getStatus().equals(
							Constants.NO_RESULT))) {
				inway.setRet(true);
				inway.setStatus(Constants.NO_RESULT);
				return inway;
			}
			// 进行组合
			List<RoundTripFlightInfo> roundTripFlightInfos = new ArrayList<RoundTripFlightInfo>();
			for (BaseFlightInfo out : outway.getData()) {
				FlightDetail outDetail = out.getDetail();
				double outPrice = outDetail.getPrice();
				double outTax = outDetail.getTax();
				for (BaseFlightInfo in : inway.getData()) {
					RoundTripFlightInfo round = new RoundTripFlightInfo();
					FlightDetail detail = in.getDetail();
					FlightDetail newDetail = new FlightDetail();
					// 获取机票价格
					double inPrice = detail.getPrice();
					double inTax = detail.getTax();
					double totalPrice=inPrice + outPrice;
					double totalTax=inTax + outTax;
					String price = String.format("%.2f",totalPrice);
					String tax = String.format("%.2f", totalTax);
					
					// 设置明细信息
					newDetail.setPrice(new Double(price));// 票价
					newDetail.setTax(new Double(tax));// 税费
					newDetail.setMonetaryunit(detail.getMonetaryunit());
					newDetail.setArrcity(detail.getArrcity());
					newDetail.setDepdate(detail.getDepdate());
					newDetail.setDepcity(detail.getDepcity());
					newDetail.setFlightno(detail.getFlightno());
					newDetail.setWrapperid(detail.getWrapperid());
					round.setDetail(newDetail);
					round.setInfo(in.getInfo());
					// 返程信息
					round.setRetinfo(out.getInfo());
					round.setRetdepdate(outDetail.getDepdate());
					round.setRetflightno(outDetail.getFlightno());
					roundTripFlightInfos.add(round);
				}
			}
			result.setData(roundTripFlightInfos);
			result.setStatus(Constants.SUCCESS);
			result.setRet(true);
			return result;
		} catch (Exception e) {
			result.setStatus(Constants.PARSING_FAIL);
			result.setRet(false);
			e.printStackTrace();
			return result;
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
	}

	/**
	 * 根据status判断单程or双程 0单程 1双程
	 * 
	 * @param tbody
	 * @param arg1
	 * @param status
	 * @return
	 */
	public ProcessResultInfo getFlightInfoByStatus(String table,
			FlightSearchParam arg1, int status) {
		ProcessResultInfo result = new ProcessResultInfo();
		// 首次出发时间
		String deptDate = arg1.getDepDate();
		if (status == 1) {
			deptDate = arg1.getRetDate();
		}
		QFPostMethod post = null;
		// 判断票是否已卖完
		boolean flag = false;
		// 获取所有tr
		String[] trs = StringUtils.substringsBetween(table, "<tr>", "</tr>");
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		try {
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
						flightSegement.setDepDate(deptDate);
						String[] airdates = airDate.split("/");
						flightSegement.setArrDate(airdates[2] + "-"
								+ airdates[1] + "-" + airdates[0]);
						flightSegement.setDeptime(deptime.replace(" ", ""));
						flightSegement.setArrtime(arrtime.replace(" ", ""));
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
					detail.setWrapperid("Wrapper_gjsairgr001");
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
				if (!flag) {
					result.setRet(true);
					result.setStatus(Constants.NO_RESULT);
					return result;
				}
			}
			result.setStatus(Constants.SUCCESS);
			result.setData(flightList);
			result.setRet(true);
		} catch (Exception e) {
			e.printStackTrace();
				result.setRet(false);
				result.setStatus(Constants.PARSING_FAIL);
				return result;
		}
		return result;
	}
}
