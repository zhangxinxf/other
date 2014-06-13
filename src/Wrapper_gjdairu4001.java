import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 佛陀航空
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjdairu4001 implements QunarCrawler {
	private static Logger logger = LoggerFactory
			.getLogger(Wrapper_gjdairu4001.class);

	private static final String EXCEPTION_INFO = "excetpion";

	// 表单提交界面
	private static final String postUrl = "http://buddhaair.com/booking";
	// 航班号前缀
	private static final String filghtPrefix = "U4";
	private static QFHttpClient httpClient = null;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("KTM");
		searchParam.setArr("JKR");
		searchParam.setDepDate("2014-07-19");
		searchParam.setTimeOut("600000");
		searchParam.setToken("");
		new Wrapper_gjdairu4001().run(searchParam);
	}

	public void run(FlightSearchParam searchParam) {
		String html = "";
		try {
//
//			String filePath = "G:\\air.html";
//			File f = new File(filePath);
//			if (!f.exists()) {
//				html = new Wrapper_gjdairu4001().getHtml(searchParam);
//				Files.write(html, f, Charsets.UTF_8);
//			} else {
//				html = Files.toString(f, Charsets.UTF_8);
//			}

			html = new Wrapper_gjdairu4001().getHtml(searchParam);
			ProcessResultInfo result = new ProcessResultInfo();
			result = new Wrapper_gjdairu4001().process(html, searchParam);
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
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(postUrl);
		bookingInfo.setMethod("post");
		// 时间处理
		String[] serachDepDate = arg0.getDepDate().split("-");
		String depDate = serachDepDate[2]
				+ MONTHS[Integer.parseInt(serachDepDate[1]) - 1]
				+ serachDepDate[0];
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("depart", arg0.getDep());
		map.put("arrival", arg0.getArr());
		map.put("trip_type", "0");
		map.put("adult", "1");
		map.put("child", "0");
		map.put("bookingtype", "H");
		map.put("flight_departure_date", depDate);
		bookingInfo.setInputs(map);
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
			post = new QFPostMethod(postUrl);
			String[] serachDepDate = arg0.getDepDate().split("-");
			String depDate = serachDepDate[2]
					+ MONTHS[Integer.parseInt(serachDepDate[1]) - 1]
					+ serachDepDate[0];
			// 设置post提交表单数据
			NameValuePair[] parametersBody = new NameValuePair[] {
					new NameValuePair("depart", arg0.getDep()),
					new NameValuePair("arrival", arg0.getArr()),
					new NameValuePair("trip_type", "0"),
					new NameValuePair("adult", "1"),
					new NameValuePair("child", "0"),
					new NameValuePair("bookingtype", "H"),
					new NameValuePair("flight_departure_date", depDate) };
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
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		if (html.contains("Sorry. There are no flights available that meet your request ")) {
			result.setRet(false);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		try {
			// 获取tbody内容
			String tbody = StringUtils.substringBetween(html, "<tbody>",
					"</tbody>");
			// 获取所有tr
			String[] trs = StringUtils
					.substringsBetween(tbody, "<tr>", "</tr>");
			for (String content : trs) {
				List<FlightSegement> info = new ArrayList<FlightSegement>();
				List<String> fliNo = new ArrayList<String>();
				OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
				FlightSegement flightSegement = new FlightSegement();
				FlightDetail detail = new FlightDetail();
				// 解析html界面
				String[] ligInfos = StringUtils.substringsBetween(content,
						"<td>", "</td>");
				String flightNo =filghtPrefix+ligInfos[0];// 航班号
				String depTime = ligInfos[1];// 起飞时间
				String arrTime = ligInfos[2];// 到达时间
				String[] price = ligInfos[3].split("\\$");// 票价
				String[] fuelCharge = ligInfos[4].split("\\$");
				String[] tax = ligInfos[5].split("\\$");
				//
				fliNo.add(flightNo);
				//
				detail.setMonetaryunit(price[0].trim());
				detail.setPrice(new Double(price[1]));
				detail.setDepcity(arg1.getDep());
				detail.setArrcity(arg1.getArr());
				detail.setTax(new Double(tax[1]) + new Double(fuelCharge[1]));
				detail.setFlightno(fliNo);
				detail.setDepdate(dateFormat.parse(deptDate));
				detail.setWrapperid("gjdairu4001");
				// 航班信息
				flightSegement.setArrairport(arg1.getArr());
				flightSegement.setDepairport(arg1.getDep());
				flightSegement.setArrtime(arrTime);
				flightSegement.setDeptime(depTime);
				flightSegement.setDepDate(deptDate);
				flightSegement.setArrDate(deptDate);
				flightSegement.setFlightno(flightNo);
				//
				info.add(flightSegement);
				oneWayFlightInfo.setDetail(detail);
				oneWayFlightInfo.setInfo(info);
				flightList.add(oneWayFlightInfo);
			}
		} catch (Exception e) {
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			e.printStackTrace();
		}
		result.setRet(true);
		result.setStatus(Constants.SUCCESS);
		result.setData(flightList);
		return result;
	}
}
