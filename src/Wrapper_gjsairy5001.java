import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
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
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * Golden Myanmar Airlines航空单程
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjsairy5001 implements QunarCrawler {

	private static final String EXCEPTION_INFO = "excetpion";

	private static final String postUrl = "https://golden.crane.aero/Common/MemberRezvResults.jsp";

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static QFHttpClient httpClient = null;

	public static void main(String[] args) {
		//
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("SIN");
		searchParam.setArr("RGN");
		searchParam.setDepDate("2014-07-25");
		searchParam.setRetDate("2014-08-18");
		searchParam.setTimeOut("600000");
		searchParam.setWrapperid("gjdairsx001");
		searchParam.setToken("");
		// BookingResult book = new Wrapper_gjsairy5001()
		// .getBookingInfo(searchParam);
		// System.out.println(JSON.toJSONString(book));
		new Wrapper_gjsairy5001().run(searchParam);
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
			html = html.replace("\\u003c", "<").replace("\\u003e", ">")
					.replace("\\n", "").replace("\\r", "").replace("\\t", "")
					.replace("\\", "").replace("//", "");
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
		bookingInfo.setAction(postUrl);
		bookingInfo.setMethod("post");
		String[] depDate = arg0.getDepDate().split("-");
		String[] retDate = arg0.getRetDate().split("-");
		String dep = depDate[2] + "/" + depDate[1] + "/" + depDate[0];
		String ret = retDate[2] + "/" + retDate[1] + "/" + retDate[0];
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("clickedButton", "btnSearch");
		map.put("TRIPTYPE", "R");
		map.put("DEPPORT", arg0.getDep());
		map.put("ARRPORT", arg0.getArr());
		map.put("DEPDATE", dep);
		map.put("RETDATE", ret);
		map.put("ADULT", "1");
		map.put("CHILD", "0");
		map.put("INFANT", "0");
		map.put("DOMESTIC_CURR", "USD");
		bookingInfo.setInputs(map);
		bookingResult.setRet(true);
		bookingResult.setData(bookingInfo);
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
			// 指定协议名称和默认的端口号
			// Protocol myhttps = new Protocol("https",
			// new MySecureProtocolSocketFactory(), 443);
			// // 注册刚才创建的https 协议对象
			// Protocol.registerProtocol("https", myhttps);
			String[] depDate = arg0.getDepDate().split("-");
			String[] retDate = arg0.getRetDate().split("-");
			String dep = depDate[2] + "/" + depDate[1] + "/" + depDate[0];
			String ret = retDate[2] + "/" + retDate[1] + "/" + retDate[0];
			// 提交表单
			post = new QFPostMethod(postUrl);
			NameValuePair[] parametersBody = new NameValuePair[] {
					new NameValuePair("clickedButton", "btnSearch"),
					new NameValuePair("TRIPTYPE", "R"),
					new NameValuePair("DEPPORT", arg0.getDep()),
					new NameValuePair("ARRPORT", arg0.getArr()),
					new NameValuePair("DEPDATE", dep),
					new NameValuePair("RETDATE", ret),
					new NameValuePair("ADULT", "1"),
					new NameValuePair("CHILD", "0"),
					new NameValuePair("INFANT", "0"),
					new NameValuePair("DOMESTIC_CURR", "USD") };
			post.setRequestBody(parametersBody);
			int status = httpClient.executeMethod(post);
			if (status != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			return post.getResponseBodyAsString();
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
	public ProcessResultInfo process(String html, FlightSearchParam arg0) {
		ProcessResultInfo result = new ProcessResultInfo();
		// 判断票是否已卖完
		if (StringUtils.isBlank(EXCEPTION_INFO) || EXCEPTION_INFO.equals(html)) {
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
		if (html.contains("There are no available flights")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		html = html.replace("\\u003c", "<").replace("\\u003e", ">")
				.replace("\\n", "").replace("\\r", "").replace("\\t", "")
				.replace("\\", "").replace("//", "");
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		List<OneWayFlightInfo> retList = new ArrayList<OneWayFlightInfo>();
		try {
			// 获取div
			String[] tables = StringUtils.substringsBetween(html,
					"<div id='tablo scroll'", "<div class='colElem baggage'>");
			// 获取input信息
			if (tables == null || tables.length < 1) {
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			// 获取航班列表信息
			findListbyPageNum(flightList, arg0, tables[0], html, 0);
			findListbyPageNum(retList, arg0, tables[1], html, 1);
			if (flightList.size() == 0 || retList.size() == 0) {
				result.setStatus(Constants.NO_RESULT);
				result.setRet(true);
				return result;
			}
			List<RoundTripFlightInfo> roundTripFlightInfos = new ArrayList<RoundTripFlightInfo>();
			for (BaseFlightInfo out : retList) {
				FlightDetail outDetail = out.getDetail();
				double outprice = outDetail.getPrice();
				for (BaseFlightInfo in : flightList) {
					RoundTripFlightInfo round = new RoundTripFlightInfo();
					FlightDetail detail = in.getDetail();
					FlightDetail newDetail = new FlightDetail();
					// 获取机票价格
					double inprice = detail.getPrice();
					double totalprice = inprice + outprice;
					// 设置明细信息
					newDetail.setPrice(new Double(String.format("%.2f",
							totalprice)));// 票价
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

	/**
	 * 根据分页数据获取航班信息
	 * 
	 * @return
	 */
	public List<OneWayFlightInfo> findListbyPageNum(
			List<OneWayFlightInfo> flightList, FlightSearchParam arg1,
			String tables, String html, int status) throws Exception {
		try {

			String deptDate = status == 0 ? arg1.getDepDate() : arg1
					.getRetDate();
			String wayInfo[] = StringUtils.substringsBetween(tables,
					"<input style='cursor:pointer;' type='radio'", ">");
			for (int i = 0; i < wayInfo.length; i++) {
				// 航班完整信息
				OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
				FlightDetail detail = new FlightDetail();
				// 航线信息
				List<FlightSegement> info = new ArrayList<FlightSegement>();
				// 航班号
				List<String> fliNo = new ArrayList<String>();
				String trConent = wayInfo[i];
				String radiovalue = StringUtils.substringBetween(trConent,
						"value='", "'");
				String[] radiovalues = radiovalue.split("#");
				String state = radiovalues[6];
				// 获取价格
				String priceHtml = StringUtils.substringBetween(html,
						"sfArray[0]", "sfList[" + radiovalues[2] + "]");
				String[] prices = priceHtml.split(";");
				String basePrice = StringUtils.substringBetween(tables,
						"<span name='fareOfFlight'>", "</span>");
				String feedecimal = StringUtils.substringBetween(tables,
						"<span name='fareDecimalOfFlight' class='feedecimal'>",
						"</span>");
				basePrice += feedecimal;
				double price = 0;
				if (status == 0) {
					for (String string : prices) {
						if (string.contains(state)) {
							String value = StringUtils.substringBetween(string,
									"(", ")").replace("'", "");
							String[] values = value.split(",");
							double nprice = Double.parseDouble(values[3]);
							if (price == 0 || price > nprice) {
								price = nprice;
							}
						}
					}
				}
				String totalPrice = status == 0 ? String.format("%.2f",
						new Double(basePrice).doubleValue() + price) : String
						.format("%.2f", new Double(basePrice));
				System.out.println(totalPrice);
				String inputText = StringUtils.substringBetween(trConent,
						"flightInfo(", ")");
				inputText = inputText.replace("SIN, Changi T-1", "SIN")
						.replace("'", "");
				String[] values = inputText.split(",");
				// 航班号
				String flightNo = values[0];
				// 起飞日期
				String depDateStr = values[3];
				String arrDateStr = values[6];
				String depTime = values[4];
				String arrTime = values[7];
				String dep = values[11];
				String arr = values[13];
				String[] depDate = depDateStr.split("\\.");
				String[] arrDate = arrDateStr.split("\\.");
				String monetaryunit = "USD";
				FlightSegement flightSegement = new FlightSegement();
				flightSegement.setFlightno(flightNo);
				flightSegement.setDepairport(dep);
				flightSegement.setArrairport(arr);
				flightSegement.setDepDate(depDate[2] + "-" + depDate[1] + "-"
						+ depDate[0]);
				flightSegement.setArrDate(arrDate[2] + "-" + arrDate[1] + "-"
						+ arrDate[0]);
				flightSegement.setDeptime(depTime);
				flightSegement.setArrtime(arrTime);
				fliNo.add(flightNo);
				info.add(flightSegement);
				detail.setMonetaryunit(monetaryunit);
				detail.setPrice(new Double(totalPrice));
				detail.setDepcity(arg1.getDep());
				detail.setArrcity(arg1.getArr());
				detail.setFlightno(fliNo);
				detail.setDepdate(dateFormat.parse(deptDate));
				detail.setWrapperid("gjsairy5001");
				oneWayFlightInfo.setDetail(detail);
				oneWayFlightInfo.setInfo(info);
				flightList.add(oneWayFlightInfo);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		return flightList;
	}
}
