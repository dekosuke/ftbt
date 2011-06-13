package cx.ath.dekosuke.ftbt;

import java.net.URLEncoder;
import java.util.ArrayList;

import android.webkit.MimeTypeMap;

public class StringUtil {
	static String safeCut(String str, int length) {
		if (str.length() > length) {
			return str.substring(0, length) + "...";
		}
		return str;
	}

	static String safeCutNoDot(String str, int length) {
		if (str.length() > length) {
			return str.substring(0, length);
		}
		return str;
	}

	// これ汎用じゃない・・
	static String[] nonBlankSplit(String str, String[] addition) {
		String[] elems = str.split("\n");
		ArrayList<String> filtered_elems = new ArrayList<String>();
		for (int i = 0; i < elems.length; ++i) {
			// if(elems[i])
			if (elems[i].length() > 0) {
				filtered_elems.add(elems[i]);
			}
		}
		if (elems.length > 1) {
			for (int i = 0; i < addition.length; ++i) {
				filtered_elems.add(addition[i]);
			}
		}
		// FLog.d("length="+filtered_elems.size());
		return (String[]) filtered_elems.toArray(new String[0]);
	}
	
	static String quote(String str){
		String ret="";
		String[] elems = str.split("\n");
		ArrayList<String> filtered_elems = new ArrayList<String>();
		for (int i = 0; i < elems.length; ++i) {
			// if(elems[i])
			if (elems[i].length() > 0) {
				ret+=">"+elems[i].trim()+"\n";
			}
		}
		return ret;
	}

	// 検索クエリを正規化
	// TODO:大文字小文字、全角半角、ひらがなかたかなの標準化
	static String[] queryNormalize(String str) {
		String temp = normalize(str);
		String[] splits = temp.split("[ 　]");
		ArrayList<String> splits_tmp = new ArrayList<String>();
		for (int i = 0; i < splits.length; ++i) {
			if (splits[i].length() > 0) {
				splits_tmp.add(splits[i]);
			}
		}
		return (String[]) splits_tmp.toArray(new String[0]);
	}

	static boolean isQueryMatch(String str, String[] query) {
		String temp = normalize(str);
		for (int i = 0; i < query.length; ++i) {
			if (!temp.contains(query[i])) {
				return false;
			}
		}
		return true;
	}

	// http://ameblo.jp/archive-redo-blog/entry-10376390355.html
	private static String zenkakuToHankaku(String value) {
		StringBuilder sb = new StringBuilder(value);
		for (int i = 0; i < sb.length(); i++) {
			int c = (int) sb.charAt(i);
			if ((c >= 0xFF10 && c <= 0xFF19) || (c >= 0xFF21 && c <= 0xFF3A)
					|| (c >= 0xFF41 && c <= 0xFF5A)) {
				sb.setCharAt(i, (char) (c - 0xFEE0));
			}
		}
		value = sb.toString();
		return value;
	}

	// http://www7a.biglobe.ne.jp/~java-master/samples/string/ZenkakuKatakanaToZenkakuHiragana.html
	public static String zenkakuHiraganaToZenkakuKatakana(String s) {
		StringBuffer sb = new StringBuffer(s);
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c >= 'ァ' && c <= 'ン') {
				sb.setCharAt(i, (char) (c - 'ァ' + 'ぁ'));
			} else if (c == 'ヵ') {
				sb.setCharAt(i, 'か');
			} else if (c == 'ヶ') {
				sb.setCharAt(i, 'け');
			} else if (c == 'ヴ') {
				sb.setCharAt(i, 'う');
				sb.insert(i + 1, '゛');
				i++;
			}
		}
		return sb.toString();
	}

	// 正規化
	static String normalize(String str) {
		// 大文字ー＞小文字
		String temp = str.toLowerCase();
		// 全角ー＞半角
		temp = zenkakuToHankaku(temp);
		// かたかなー＞ひらがな
		temp = zenkakuHiraganaToZenkakuKatakana(temp);
		return temp;
	}

	/**
	 * 渡されたファイル名から、MIMEタイプを返します。
	 * 
	 */
	public static String getMIMEType(String targetFile) {
		String url = URLEncoder.encode(targetFile);
		String extention = MimeTypeMap.getFileExtensionFromUrl(url);
		String mtype = "";

		// 拡張子を小文字に変換
		extention = extention.toLowerCase();

		mtype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
		if (mtype == null) {
			// ソースコードなどは判定してくれないので自分で判定する

			String PLANETEXT = "text/plain";
			if (
			// テキストエディタで開けそうなソースファイルはテキストとして登録
			(targetFile.endsWith(".c")) || (targetFile.endsWith(".cp"))
					|| (targetFile.endsWith(".cpp"))
					|| (targetFile.endsWith(".java"))
					|| (targetFile.endsWith(".txt"))
					|| (targetFile.endsWith(".c++"))
					|| (targetFile.endsWith(".sh"))
					|| (targetFile.endsWith(".cmake"))
					|| (targetFile.endsWith(".ini"))
					|| (targetFile.endsWith(".php"))
					|| (targetFile.endsWith(".py")))
				mtype = PLANETEXT;
		}

		if (mtype == null)
			mtype = "";
		return mtype;
	}
	
	//キーワード編集のバリデーション。だめな場合は例外を吐く
	static boolean validateFocusWord(String word) throws Exception{
		String[] splits = word.split("[ 　]");
		if(splits.length>1){ //半角・全角スペースを含む
			throw new Exception("spaces_exist");
		}
		splits = word.split("\n");
		if(splits.length>1){ //改行コードを含む
			throw new Exception("newline_exist");
		}
		if(word.equals("")){
			throw new Exception("noword");			
		}
		if(word.length()>10){
			throw new Exception("toolongword");			
		}
		return true;
	}

	//テキストにキーワードのどれかが含まれているかどうか
	static boolean focusWordMatched(String text, ArrayList<String> focuswords){
		for(int i=0;i<focuswords.size();++i){
			if(text.contains(focuswords.get(i))){
				return true;
			}
		}
		return false;
	}

	static String highlightFocusWordMatched(String text, ArrayList<String> focuswords){
		for(int i=0;i<focuswords.size();++i){
			String fword = focuswords.get(i);
			if(text.contains(fword)){
				return text.replace(fword, "<font color=\"red\">"+fword+"</font>");
			}
		}
		return text;
	}
}
