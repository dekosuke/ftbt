package cx.ath.dekosuke.ftbt;

import java.security.MessageDigest;
import android.util.Log;

//ファイル名をハッシュにする
class FutabaCrypt {
	public static String createDigest(String source) {
		Log.d("ftbt", "crypt source=" + source + " html=" + isHTMLName(source));
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
			return "";
		}

		byte[] data = source.getBytes();
		md.update(data);

		byte[] digest = md.digest();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			sb.append(Integer.toHexString(0xff & digest[i]));
		}
		if (FutabaCrypt.isHTMLName(source)) {
			return sb.toString() + ".htm";
		}
		return sb.toString();
	}

	public static boolean isHTMLName(String str) {
		String last_str = str.substring(Math.max(0, str.length() - 4));
		boolean is_html = last_str.equals(".htm") || last_str.equals("html")
				|| str.contains(".php");
		return is_html;
	}
}
