package cx.ath.dekosuke.ftbt;

import java.io.Serializable;

//One thread Activity for Futaba

//import android.graphics.*;

public class FutabaThreadContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String userName;
	public String title;
	public String text;
	public String mailTo;
	public int id;
	public String imgURL;
	public int threadNum;
	public String baseUrl;//historyのときはこれが入っている。内容的にはFutabaThreadに渡すintentと同じ
	public String resNum;
	public String BBSName; //どの板か->過去ログ表示のときに使う
	public long lastAccessed; //最終アクセス時間(UnixTime)
	public boolean isChecked=false;

	// 投稿時刻private date
	// 画像private

	// コンストラクタ
	FutabaThreadContent() {
		userName = "";
		title = "";
		text = "";
		mailTo = "";
		id = 0;
		resNum="";
		BBSName="";
		baseUrl = "";
		lastAccessed = 0;
	}
	
	public String toString(){
		return "FTC userName="+userName+
		" title="+title+
		" text="+text+
		" mailTo="+mailTo+
		" id="+id+
		" imgURL="+imgURL+
		" threadNum="+threadNum+
		" baseUrl="+baseUrl+
		" resNum="+resNum+
		" BBSName="+BBSName+
		" lastAccessed="+lastAccessed+
		" isChecked="+isChecked;
	}
}
