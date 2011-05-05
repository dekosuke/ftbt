package cx.ath.dekosuke.ftbt;

import java.io.Serializable;

import android.util.Log;

//板の情報を持つクラス
public class FutabaBBSContents implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String url;
	public String name;

	public FutabaBBSContents() {
	}

	public FutabaBBSContents(String s) {
		String[] elems = s.split(":::");
		url = elems[0];
		name = elems[1];
	}

	public String toString(){ //適当文字列化
		return url+":::"+name;
	}
	
	public boolean equals(Object obj){
		FutabaBBSContents rhs = (FutabaBBSContents)obj;
		return (url.equals(rhs.url)) && (name.equals(rhs.name));
	}
	
}
