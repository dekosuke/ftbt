package cx.ath.dekosuke.ftbt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

public class PostParser {
	private static Pattern tagPattern = Pattern
	.compile("<.+?>", Pattern.DOTALL);
	Pattern honbunPattern = Pattern.compile("<body[^>]*>(.+?)</body>",
			Pattern.DOTALL);

	public String parse(Context context, String str) {
		FLog.d("str="+str);
		try {
			Matcher mc = honbunPattern.matcher(str);
			mc.find();
			return removeTag(mc.group(1));
		} catch (Exception e) {
			FLog.d("message", e);
		}
		return "";
	}

	public static String removeTag(String str) {
		return tagPattern.matcher(str).replaceAll("");
	}
}
