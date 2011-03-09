package ext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import play.templates.JavaExtensions;

public class DataViewExtensions extends JavaExtensions {

	public static String route2url(String route, Map<?, ?> args) {

		return null;
	}

	public static String asTag(String tag, Map<?, ?> args) {

		return null;
	}

	public static String wrap(Object content, String elem) {
		String[] s = elem.split("\\.");
		StringBuffer buffer = new StringBuffer();
		buffer.append("<");
		buffer.append(s[0]);
		if (s.length > 1) {
			buffer.append(" class=\"");
			int i = 1;
			while (i < s.length) {
				buffer.append(s[i] + " ");
				i++;
			}
			buffer.append("\"");
		}
		buffer.append(">");
		buffer.append(content);
		buffer.append("</" + s[0] + ">");
		return buffer.toString();
	}
}
