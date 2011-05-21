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
	public int id;
	public String imgURL;
	public String bigImgURL;
	public int width;
	public int height;
	public String endTime; //これは先頭にしか入れないよ

	// 投稿時刻private date
	// 画像private

	// コンストラクタ
	FutabaStatus() {
		userName = "";
		title = "";
		text = "";
		mailTo = "";
		id = 0;
		width = 0;
		height = 0;
		endTime="";
	}
	
	//区切り用仮想レス作成
	public static FutabaStatus createBlank(){
		FutabaStatus st = new FutabaStatus();
		st.id = -1;
		return st;
	}

	//スレ落ち時刻表示用仮想レス作成
	public static FutabaStatus createEndTime(String str){
		FutabaStatus st = new FutabaStatus();
		st.id = -2;
		st.text = "<font color=\"red\">("+str+")</font>";
		return st;
	}
	
	
	public static boolean isBlank(FutabaStatus st){
		return st.id==-1;
	}
	
	public static boolean isEndTime(FutabaStatus st){
		return st.id==-2;
	}

	public String toString(){
		return "FTC userName="+userName+
		" title="+title+
		" text="+text+
		" name="+name+
		" datestr="+datestr+
		" mailTo="+mailTo+
		" id="+id+
		" imgURL="+imgURL+
		" bigImgURL="+bigImgURL+
		" width="+width+
		" height="+height;
	}
}
