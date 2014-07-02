import java.io.File;
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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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

/**
 * 中国东方航空航空双程
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjsairmu004 implements QunarCrawler {

	private static final String EXCEPTION_INFO = "excetpion";

	private static Map<String, String> data = new HashMap<String, String>();

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static QFHttpClient httpClient = null;

	public static void main(String[] args) {
		//
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("PVG");
		searchParam.setArr("PEK");
		searchParam.setDepDate("2014-07-12");
		searchParam.setRetDate("2014-07-25");
		searchParam.setTimeOut("600000");
		searchParam.setWrapperid("gjsairmu004");
		searchParam.setToken("");
		new Wrapper_gjsairmu004().run(searchParam);

	}

	public void run(FlightSearchParam searchParam) {
		String html = "";
		try {
			String filePath = "G:\\k.html";
			File f = new File(filePath);
			// if (!f.exists()) {
			// html = getHtml(searchParam);
			// Files.write(html, f, Charsets.UTF_8);
			// } else {
			// html = Files.toString(f, Charsets.UTF_8);
			// }

			html = getHtml(searchParam);
			Files.write(html, f, Charsets.UTF_8);
			ProcessResultInfo result = new ProcessResultInfo();
			result = process(html, searchParam);
			if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
				List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result
						.getData();
				System.out.println("总量" + flightList.size());
				for (int i = 0; i < flightList.size(); i++) {
					RoundTripFlightInfo in = flightList.get(i);
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
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo
				.setAction("http://ck.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml");
		bookingInfo.setMethod("get");
		Map<String, String> body = new LinkedHashMap<String, String>();
		body.put("cond.tripType", "RT");
		body.put("cond.depCode_reveal", "");
		body.put("cond.depCode", arg0.getDep());
		body.put("cond.arrCode_reveal", "");
		body.put("cond.arrCode", arg0.getArr());
		body.put("cond.routeType", "1");
		body.put("depDate", arg0.getDepDate());
		body.put("depRtDate", arg0.getRetDate());
		body.put("cond.cabinRank", "ECONOMY");
		//body.put("submit", "");
		bookingInfo.setInputs(body);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	public String getHtml(FlightSearchParam arg0) {
		QFGetMethod get = null;
		try {
			String getUrl = String
					.format("http://ck.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml?cond.tripType=RT&cond.depCode_reveal=%s&cond.depCode=%s&cond.arrCode_reveal=%s&cond.arrCode=%s&cond.routeType=1&depDate=%s&depRtDate=%s&cond.cabinRank=ECONOMY&submit=%s",
							"", arg0.getDep(), "", arg0.getArr(),
							arg0.getDepDate(), arg0.getRetDate(), "");
			// 生成http对象
			httpClient = new QFHttpClient(arg0, false);
			// 按照浏览器的模式来处理cookie
			httpClient.getParams().setCookiePolicy(
					CookiePolicy.BROWSER_COMPATIBILITY);
			get = new QFGetMethod(getUrl);
			int getStatus = httpClient.executeMethod(get);
			if (getStatus != HttpStatus.SC_OK) {
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
		}
	}

	/*
	 * ProcessResultInfo中，
	 * ret为true时，status可以为：SUCCESS(抓取到机票价格)|NO_RESULT(无结果，没有可卖的机票) ret为false时
	 * ，status可以为:CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL
	 * |PARAM_ERROR
	 */
	public ProcessResultInfo process(String html, FlightSearchParam arg0) {
		ProcessResultInfo result = new ProcessResultInfo();
		// 判断票是否已卖完
		if (EXCEPTION_INFO.equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		if (html.contains("閣下搜索的航班已滿座或不適用，請選擇其它日期，謝謝您的合作")) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_AIRLINE);
			return result;
		}
		getCity();
		String priceReg = "(\\d)+,?\\d+(\\.?){1}\\d+";
		Pattern pricePattern = Pattern.compile(priceReg);
		try {
			html=html.replaceAll("\t", "")
					.replaceAll("\n", "").replaceAll("\r", "");
			// 出发
			String flight_table_rt_go = StringUtils.substringBetween(html,
					"<div class=\"flight_table rt_go\">", "</div>");
			String[] table = StringUtils.substringsBetween(flight_table_rt_go,
					"<tbody>", "</tbody>");
			// 返回
			String flight_table_rt_back = StringUtils
					.substringBetween(html,
							"<div class=\"flight_table rt_back\" style=''>",
							"</div>");
			String[] rentable = StringUtils.substringsBetween(
					flight_table_rt_back, "<tbody>", "</tbody>");
			// 单程
			List<BaseFlightInfo> inway = new ArrayList<BaseFlightInfo>();
			// 返程
			List<BaseFlightInfo> outway = new ArrayList<BaseFlightInfo>();
			// 往返集合
			List<RoundTripFlightInfo> roundTripFlightInfos = new ArrayList<RoundTripFlightInfo>();
			// 获取单程信息
			findListInfo(inway, arg0, pricePattern, table);
			// 获取返程信息
			findListInfo(outway, arg0, pricePattern, rentable);
			for (BaseFlightInfo out : outway) {
				FlightDetail outDetail = out.getDetail();
				double outprice = outDetail.getPrice();
				double outTax = outDetail.getTax();
				for (BaseFlightInfo in : inway) {
					RoundTripFlightInfo round = new RoundTripFlightInfo();
					FlightDetail detail = in.getDetail();
					FlightDetail newDetail = new FlightDetail();
					// 获取机票价格
					double inprice = detail.getPrice();
					double inTax = detail.getTax();
					double totalprice = inprice + outprice;
					double totalTax = outTax + inTax;

					// 设置明细信息
					newDetail.setPrice(new Double(String.format("%.2f",
							totalprice)));// 票价
					newDetail
							.setTax(new Double(String.format("%.2f", totalTax)));// 税费
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

			result.setStatus(Constants.SUCCESS);
			result.setData(roundTripFlightInfos);
			result.setRet(true);
		} catch (Exception e) {
			result.setStatus(Constants.PARSING_FAIL);
			result.setRet(false);
			e.printStackTrace();
		}
		return result;
	}

	// 获取飞行信息
	public List<BaseFlightInfo> findListInfo(List<BaseFlightInfo> flightList,
			FlightSearchParam arg1, Pattern pricePattern, String tbody[])
			throws Exception {
		try {
			String deptDate = arg1.getDepDate();
			for (int i = 0; i < tbody.length; i++) {
				// 航线信息
				List<FlightSegement> info = new ArrayList<FlightSegement>();
				// 航班号
				List<String> fliNo = new ArrayList<String>();
				// 单条航班完整信息
				OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
				FlightDetail detail = new FlightDetail();
				// 一条完整的航班信息
				String trConent = tbody[i];
				String price = "";
				String tax = "";
				String monetaryunit = "";
				// 获取航班信息
				String[] fliInfo = StringUtils.substringsBetween(trConent,
						" <tr class=\"booking\">", "</tr>");
				for (int n = 0; n < fliInfo.length; n++) {
					FlightSegement flightSegement = new FlightSegement();
					String datahtml = fliInfo[n].replaceAll("\t", "")
							.replaceAll("\n", "").replaceAll("\r", "");
					// 获取当前信息
					String[] tds = StringUtils.substringsBetween(datahtml,
							"<td>", "</td>");
					if (StringUtils.isBlank(price) || StringUtils.isBlank(tax)
							|| StringUtils.isBlank(monetaryunit)) {
						String[] priceHtmls = StringUtils.substringsBetween(
								datahtml, "class=\"price center\">", "</td>");
						String priceHtml = priceHtmls[1];
						Matcher priceMatcher = pricePattern.matcher(priceHtml);
						int num = 0;
						while (priceMatcher.find()) {
							if (num == 1)
								price = priceMatcher.group();
							if (num == 2) {
								tax = priceMatcher.group();
								break;
							}
							num++;
						}
						// 获取货币单位
						monetaryunit = StringUtils.substringBetween(priceHtml,
								">", "<span");
					}
					String depDateHtml = "";
					String arrDateHtml = "";
					String flightNo = "";
					String depPort = "";
					String arrport = "";
					// 获取起飞数据
					if (tds.length == 5) {
						depDateHtml = StringUtils.substringAfterLast(tds[0],
								">").trim();
						arrDateHtml = StringUtils.substringAfterLast(tds[1],
								">").trim();
						flightNo = StringUtils.substringAfterLast(tds[2], ">")
								.replaceAll("\\s", "");
						depPort = tds[3].replaceAll("\\s", "");
						arrport = tds[4].replaceAll("\\s", "");
					} else {
						depDateHtml = StringUtils.substringAfterLast(tds[1],
								">").trim();
						arrDateHtml = StringUtils.substringAfterLast(tds[2],
								">").trim();
						flightNo = StringUtils.substringAfterLast(tds[3], ">")
								.replaceAll("\\s", "");
						depPort = tds[4].replaceAll("\\s", "");
						arrport = tds[5].replaceAll("\\s", "");
					}
					depPort = data.get(depPort);
					arrport = data.get(arrport);
					String[] depData = depDateHtml.split(" ");
					String[] arrData = arrDateHtml.split(" ");

					String arrtime = arrData[0].replaceAll("\\s", "");
					String deptime = depData[0].replaceAll("\\s", "");

					String arrdate = arrData[1].replaceAll("日", "");
					String depDate = depData[1].replaceAll("日", "");
					String arrDates[] = arrdate.split("月");
					String depdates[] = depDate.split("月");
					if (monetaryunit.contains("HK")) {
						monetaryunit = "HKD";
					}
					// 分割查询时间，获取年
					String[] date = arg1.getDepDate().split("-");
					flightSegement.setFlightno(flightNo);
					flightSegement.setDepairport(depPort);
					flightSegement.setArrairport(arrport);

					flightSegement.setArrDate(date[0] + "-" + arrDates[0] + "-"
							+ arrDates[1]);
					flightSegement.setDepDate(date[0] + "-" + depdates[0] + "-"
							+ depdates[1]);
					flightSegement.setDeptime(deptime);
					flightSegement.setArrtime(arrtime);
					fliNo.add(flightNo);
					info.add(flightSegement);
				}
				detail.setMonetaryunit(monetaryunit.replace("$", ""));
				detail.setPrice(new Double(price.replaceAll(",", "")));
				detail.setDepcity(arg1.getDep());
				detail.setArrcity(arg1.getArr());
				detail.setTax(new Double(tax.replaceAll(",", "")));
				detail.setFlightno(fliNo);
				detail.setDepdate(dateFormat.parse(deptDate));
				detail.setWrapperid("gjsairmu004");
				oneWayFlightInfo.setDetail(detail);
				oneWayFlightInfo.setInfo(info);
				flightList.add(oneWayFlightInfo);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		return flightList;
	}

	/**
	 * 获取城市信息
	 * 
	 * @return
	 */
	public void getCity() {
		QFGetMethod getJs = null;
		String url = "http://ck.ceair.com/muovc/resource/zh_HK/js/city.js";
		getJs = new QFGetMethod(url);
		int getStatus;
		try {
			getStatus = httpClient.executeMethod(getJs);
			if (getStatus != HttpStatus.SC_OK) {
				return;
			}
			String html = getJs.getResponseBodyAsString();
			if (data.size() == 0) {
				html = new String(html.getBytes("ISO-8859-1"), "UTF-8");
				String[] results_html = StringUtils
						.substringAfter(html, "var _cityData =")
						.replace("+\n", "").split(";\"+");
				for (int i = 0; i < results_html.length - 1; i++) {
					String[] array_str = results_html[i].trim().split("\\|");
					String[] array = array_str[0].split(":");
					data.put(array[1], array[0].replace("\"", ""));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != getJs)
				getJs.releaseConnection();
		}
	}
}
