package cx.ath.dekosuke.ftbt;

import java.io.Serializable;

//板の情報を持つクラス
public class FutabaBBS implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String url;
	public String name;

	public FutabaBBS() {
	}

	public FutabaBBS(String s) {
		String[] elems = s.split(":::");
		url = elems[0];
		name = elems[1];
	}

	public String toString(){ //適当文字列化
		return url+":::"+name;
	}
}
