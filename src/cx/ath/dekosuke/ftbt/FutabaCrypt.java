package cx.ath.dekosuke.ftbt;

import java.security.MessageDigest;
import android.util.Log;

//ファイル名をハッシュにする
class FutabaCrypt {
	public static String createDigest(String source) {
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
		if( FutabaCrypt.isHTMLName(source) ){
			return sb.toString()+".htm";
		}
		return sb.toString();
	}
	
	public static boolean isHTMLName(String str){
		return str.substring(Math.max(0, str.length()-4)).equals(".htm");
	}
}
