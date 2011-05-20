package cx.ath.dekosuke.ftbt;

//One thread Activity for Futaba

//import android.graphics.*;

public class FutabaStatus {
	public String userName;
	public String title;
	public String text;
	public String name;
	public String datestr;
	public String mailTo;
	public String idstr;
	public String imgURL;
	public String bigImgURL;
	public int width;
	public int height;

	// 投稿時刻private date
	// 画像private

	// コンストラクタ
	FutabaStatus() {
		userName = "";
		title = "";
		text = "";
		mailTo = "";
		idstr = "";
		width = 0;
		height = 0;
	}
	
	//区切り用仮想レス作成
	public static FutabaStatus createBlank(){
		FutabaStatus st = new FutabaStatus();
		st.idstr = "-1";
		return st;
	}
	
	public static boolean isBlank(FutabaStatus st){
		return st.idstr.equals("-1");
	}

	public String toString(){
		return "FTC userName="+userName+
		" title="+title+
		" text="+text+
		" name="+name+
		" datestr="+datestr+
		" mailTo="+mailTo+
		" idstr="+idstr+
		" imgURL="+imgURL+
		" bigImgURL="+bigImgURL+
		" width="+width+
		" height="+height;
	}
}
