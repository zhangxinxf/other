import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

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
 * Golden Myanmar Airlines航空单程
 * 
 * 
 */
public class Wrapper_gjdairy5001 implements QunarCrawler {

	private static final String EXCEPTION_INFO = "excetpion";

	// 表单提交界面
	private static final String postUrl = "https://golden.crane.aero/Common/MemberRezvResults.jsp";

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static QFHttpClient httpClient = null;

	public static void main(String[] args) {
		//
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("MYT");
		searchParam.setArr("SIN");
		searchParam.setDepDate("2014-09-01");
		searchParam.setTimeOut("600000");
		searchParam.setWrapperid("gjdairsx001");
		searchParam.setToken("");
//		 BookingResult book= new
//		 Wrapper_gjdairy5001().getBookingInfo(searchParam);
//		 System.out.println(JSON.toJSONString(book));
		new Wrapper_gjdairy5001().run(searchParam);
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
			html = Files.toString(f, Charsets.UTF_8);
			html = html.replace("\\u003c", "<").replace("\\u003e", ">")
					.replace("\\n", "").replace("\\r", "").replace("\\t", "")
					.replace("\\", "").replace("//", "");
			Files.write(html, f, Charsets.UTF_8);
			ProcessResultInfo result = new ProcessResultInfo();
			result = process(html, searchParam);
			if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
				List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result
						.getData();
				System.out.println("总量" + flightList.size());
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
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(postUrl);
		bookingInfo.setMethod("post");
		String[] serachArrDate = arg0.getDepDate().split("-");
		String dep = serachArrDate[2] + "." + serachArrDate[1] + "."
				+ serachArrDate[0];
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("clickedButton", "btnSearch");
		map.put("TRIPTYPE", "O");
		map.put("DEPPORT", arg0.getDep());
		map.put("ARRPORT", arg0.getArr());
		map.put("DEPDATE", dep);
		map.put("RETDATE", "");
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
//			Protocol myhttps = new Protocol("https",
//					new MySecureProtocolSocketFactory(), 443);
//			// 注册刚才创建的https 协议对象
//			Protocol.registerProtocol("https", myhttps);
			String[] serachArrDate = arg0.getDepDate().split("-");
			String dep = serachArrDate[2] + "/" + serachArrDate[1] + "/"
					+ serachArrDate[0];
			// 提交表单
			post = new QFPostMethod(postUrl);
			NameValuePair[] parametersBody = new NameValuePair[] {
					new NameValuePair("clickedButton", "btnSearch"),
					new NameValuePair("TRIPTYPE", "O"),
					new NameValuePair("DEPPORT", arg0.getDep()),
					new NameValuePair("ARRPORT", arg0.getArr()),
					new NameValuePair("DEPDATE", dep),
					new NameValuePair("RETDATE", ""),
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
		try {
			// 获取input信息
			String tables[] = StringUtils.substringsBetween(html,
					"<input style='cursor:pointer;' type='radio'", ">");
			if (tables == null || tables.length < 1) {
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			// 获取航班列表信息
			findListbyPageNum(flightList, arg0, tables, html);
			if (flightList.size() == 0) {
				result.setStatus(Constants.NO_RESULT);
				result.setRet(true);
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

	/**
	 * 根据分页数据获取航班信息
	 * 
	 * @return
	 */
	public List<OneWayFlightInfo> findListbyPageNum(
			List<OneWayFlightInfo> flightList, FlightSearchParam arg1,
			String tables[], String html) throws Exception {
		try {
			String deptDate = arg1.getDepDate();
			for (int i = 0; i < tables.length; i++) {
				// 航班完整信息
				OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
				FlightDetail detail = new FlightDetail();
				// 航线信息
				List<FlightSegement> info = new ArrayList<FlightSegement>();
				// 航班号
				List<String> fliNo = new ArrayList<String>();
				String trConent = tables[i];
				String radiovalue = StringUtils.substringBetween(trConent,
						"value='", "'");
				String[] radiovalues = radiovalue.split("#");
				String state = radiovalues[6];
				// 获取价格
				String priceHtml = StringUtils.substringBetween(html,
						"sfArray[0]", "sfList[" + radiovalues[2] + "]");
				String[] prices = priceHtml.split(";");
				String basePrice = StringUtils.substringBetween(html,
						"<span name='fareOfFlight'>", "</span>");
				String feedecimal = StringUtils.substringBetween(html,
						"<span name='fareDecimalOfFlight' class='feedecimal'>",
						"</span>");
				basePrice += feedecimal;
				double price = 0;
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
				String totalPrice = String.format("%.2f",
						new Double(basePrice).doubleValue() + price);
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
				detail.setWrapperid("gjdairy5001");
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
