import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
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
 * SkyWork航空单程
 * 
 * @author zhangx
 * 
 */
public class Wrapper_gjdairsx001 implements QunarCrawler {

	private static final String EXCEPTION_INFO = "excetpion";

	// 表单提交界面
	private static final String postUrl = "https://booking.flyskywork.com/WebService/B2cService.asmx/GetAvailability";
	// 首页，获取城市三字码
	private static final String firstUrl = "http://flyskywork.com/en";
	private static Map<String, String> data = new HashMap<String, String>();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static QFHttpClient httpClient = null;

	public static void main(String[] args) {
		//
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("MUC");
		searchParam.setArr("BRN");
		searchParam.setDepDate("2014-07-19");
		searchParam.setTimeOut("600000");
		searchParam.setWrapperid("gjdairsx001");
		searchParam.setToken("");
//		BookingResult book=	new Wrapper_gjdairsx001().getBookingInfo(searchParam);
//		System.out.println(JSON.toJSONString(book));
//		new Wrapper_gjdairsx001().run(searchParam);
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
		bookingInfo.setAction("https://booking.flyskywork.com/default.aspx");
		bookingInfo.setMethod("get");
		String[] serachArrDate = arg0.getDepDate().split("-");
		String dep = serachArrDate[2] + "." + serachArrDate[1] + "."
				+ serachArrDate[0];
		Map<String, String> map=new LinkedHashMap<String, String>();
		map.put("oneway", "1");
		map.put("ori", arg0.getDep());
		map.put("des", arg0.getArr());
		map.put("departure", dep);
		map.put("dep", arg0.getDepDate());
		map.put("return", "");
		map.put("ret", "");
		map.put("adt", "1");
		map.put("chd", "0");
		map.put("inf", "0");
		map.put("currency", "EUR");
		map.put("langculture", "en-us");
		map.put("web", "swk");
		map.put("submit", "");
		map.put("pro", "");
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
			// 设置时间参数
			String[] serachArrDate = arg0.getDepDate().split("-");
			String dep = serachArrDate[2] + "." + serachArrDate[1] + "."
					+ serachArrDate[0];
			String getUrl = String
					.format("https://booking.flyskywork.com/default.aspx?oneway=1&ori=%s&des=%s&departure=%s&dep=%s&return=&ret=&adt=1&chd=0&inf=0&currency=EUR&langculture=en-us&web=swk&submit=&pro=",
							arg0.getDep(), arg0.getArr(), dep,
							arg0.getDepDate());
			get = new QFGetMethod(firstUrl);
			int cityStauts = httpClient.executeMethod(get);
			if (cityStauts != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			String cityStr=get.getResponseBodyAsString();
			String selects=StringUtils.substringBetween(cityStr,"<dd id=\"des-element\">","</dd>");
			String options[]=StringUtils.substringsBetween(selects, " <option", "</option>");
			if(null!=options&&options.length>0){
				for (String option : options) {
					if(option.contains(")")){
					String  str=StringUtils.substringAfter(option,">");
					String values[]=str.split(" ");
					data.put(values[0], values[1].replace("(", "").replace(")", ""));
					}
				}
			}
			if (null != options && options.length > 0) {
				for (String option : options) {
					if (option.contains(")")) {
						String str = StringUtils.substringAfter(option, ">");
						String values[] = str.split(" ");
						data.put(values[0],
								values[1].replace("(", "").replace(")", ""));
					}
				}
			}

			// 提交表单
			get = new QFGetMethod(getUrl);
			int getStatus = httpClient.executeMethod(get);
			if (getStatus != HttpStatus.SC_OK) {
				return EXCEPTION_INFO;
			}
			post = new QFPostMethod(postUrl);
			String cookie = StringUtils.join(
					httpClient.getState().getCookies(), ";");
			post.setRequestHeader("Cookie", cookie);
			post.setRequestHeader("Referer", getUrl);
			post.setRequestHeader("Origin", " https://booking.flyskywork.com");
			post.setRequestHeader("Content-Type",
					"application/json; charset=UTF-8");
			JSONObject json = new JSONObject();
			json.put("BoardingClass", "");
			json.put("CurrencyCode", "EUR");
			json.put("SearchType", "FARE");
			json.put("dateFrom", serachArrDate[0] + serachArrDate[1]
					+ serachArrDate[2]);
			json.put("dateTo", serachArrDate[0] + serachArrDate[1]
					+ serachArrDate[2]);
			json.put("destination", arg0.getArr());
			json.put("iAdult", "1");
			json.put("iChild", "0");
			json.put("iFlightOnly", "0");
			json.put("iInfant", "0");
			json.put("iOneWay", "true");
			json.put("iOther", "0");
			json.put("origin", arg0.getDep());
			json.put("otherType", "");
			json.put("strIpAddress", "");
			json.put("strPromoCode", "");
			json.put("langculture", "en-us");
			post.setRequestEntity(new ByteArrayRequestEntity(json.toString()
					.getBytes()));
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
		if (html.contains("there are no scheduled flights for your requested flight date")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		html = html.replace("\\u003c", "<").replace("\\u003e", ">")
				.replace("\\n", "").replace("\\r", "").replace("\\t", "")
				.replace("\\", "").replace("//", "");
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		String priceReg = "(\\d)+,?\\d+(\\.){1}\\d+";
		Pattern pricePattern = Pattern.compile(priceReg);
		try {
			// 截取字符串
			String table = StringUtils.substringBetween(html,
					"<table id=\"tb_Outward\" class=\"SelectFlight\">",
					"</table>");
			// 第一个table和最后一个table是分页信息
			String tables[] = StringUtils.substringsBetween(table, "<tr class",
					"</tr>");
			if (tables.length <= 1) {
				result.setRet(true);
				result.setStatus(Constants.NO_RESULT);
				return result;
			}
			String depKey = arg0.getDep();
			String arrKey = arg0.getArr();
			String dep = StringUtils.isBlank(data.get(depKey)) ? depKey : data
					.get(depKey);
			String arr = StringUtils.isBlank(data.get(arrKey)) ? arrKey : data
					.get(arrKey);
			// 获取航班列表信息
			findListbyPageNum(flightList, arg0, pricePattern, tables, depKey, arrKey);
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
			Pattern pricePattern, String tables[], String dep, String arr)
			throws Exception {
		try {
			String deptDate = arg1.getDepDate();
			String[] serachArrDate = deptDate.split("-");
			String depDateStr = serachArrDate[2] + "." + serachArrDate[1] + "."
					+ serachArrDate[0];
			for (int i = 1; i < tables.length; i++) {
				String trConent = tables[i];
				// 飞行日期
				String flyDate = StringUtils.substringBetween(trConent,
						"<div class=\"DayRightDate\">", "</div>");
				if (flyDate.contains(depDateStr)) {
					// 航班完整信息
					OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
					FlightDetail detail = new FlightDetail();
					// 航线信息
					List<FlightSegement> info = new ArrayList<FlightSegement>();
					// 航班号
					List<String> fliNo = new ArrayList<String>();
					// 飞行时间
					String rightTime = StringUtils.substringBetween(trConent,
							"<div class=\"DayRightTime\">", "</div>");
					String times[] = rightTime.replace(" ", "").split("-");
					String depTime = times[0];
					String arrTime = times[1];
					// 航班号
					String flightNo = StringUtils.substringBetween(trConent,
							"<div class=\"DayRightRute\">", "</div>");
					String price = "";
					String monetaryunit = "EUR";
					String priceTable[] = StringUtils.substringsBetween(
							trConent, "<div class=\"ShowFare\">", "</div>");
					for (String string : priceTable) {
						String priceStr = StringUtils.substringBetween(string,
								"<span>", "</span>");
						if (priceStr.contains("---")) {
							continue;
						}
						price = priceStr;
						break;
					}
					if (!StringUtils.isBlank(price)) {
						FlightSegement flightSegement = new FlightSegement();
						flightSegement.setFlightno(flightNo);
						flightSegement.setDepairport(arg1.getDep());
						flightSegement.setArrairport(arg1.getArr());
						flightSegement.setDepDate(arg1.getDepDate());
						flightSegement.setArrDate(arg1.getDepDate());
						flightSegement.setDeptime(depTime);
						flightSegement.setArrtime(arrTime);
						fliNo.add(flightNo);
						info.add(flightSegement);
						detail.setMonetaryunit(monetaryunit);
						detail.setPrice(new Double(price));
						detail.setDepcity(dep);
						detail.setArrcity(arr);
						detail.setFlightno(fliNo);
						detail.setDepdate(dateFormat.parse(deptDate));
						detail.setWrapperid("gjdairsx001");
						oneWayFlightInfo.setDetail(detail);
						oneWayFlightInfo.setInfo(info);
						flightList.add(oneWayFlightInfo);
					}
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		return flightList;
	}
}
