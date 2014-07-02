import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test {
	public static void main(String[] args) {
		String price="<input type=\"radio\" name=\"flight_select\" value=\"1|N-B|LOW\"/>HK$<span class=\"price\">2,117</span><!-- 票价和税等等隐藏信息 --><span class=\"tkt_amt\" style=\"display:none\">1,590</span><span class=\"tax_amt\" style=\"display:none\">527</span>";
		String priceReg = "(\\d)+,?\\d+(\\.?){1}\\d+";
		Pattern pattern=Pattern.compile(priceReg);
		Matcher p=	pattern.matcher(price);
		while (p.find()) {
			System.out.println(p.group());
		}
	}
}
