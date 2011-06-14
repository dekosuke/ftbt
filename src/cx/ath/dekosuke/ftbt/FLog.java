package cx.ath.dekosuke.ftbt;

import android.util.Log;

//出力可否切り替え可能ログクラス
public class FLog {
	static final boolean use_log=false;
	static public int d(String msg){
		if(use_log){
			return Log.d("ftbt", msg);
		}else{
			return -1;
		}
	}
	static public int d(String msg, Throwable tr){
		if(use_log){
			return Log.d("ftbt", msg, tr);
		}else{
			return -1;
		}
	}
	static public int e(String msg){
		if(use_log){
			return Log.e("ftbt", msg);
		}else{
			return -1;
		}
	}
	static public int e(String msg, Throwable tr){
		if(use_log){
			return Log.e("ftbt", msg, tr);
		}else{
			return -1;
		}
	}
	static public int i(String msg){
		if(use_log){
			return Log.i("ftbt", msg);
		}else{
			return -1;
		}
	}
	static public int i(String msg, Throwable tr){
		if(use_log){
			return Log.i("ftbt", msg, tr);
		}else{
			return -1;
		}
	}
	static public int w(String msg){
		if(use_log){
			return Log.w("ftbt", msg);
		}else{
			return -1;
		}
	}
	static public int w(String msg, Throwable tr){
		if(use_log){
			return Log.w("ftbt", msg, tr);
		}else{
			return -1;
		}
	}
	static public int v(String msg){
		if(use_log){
			return Log.v("ftbt", msg);
		}else{
			return -1;
		}
	}
	static public int v(String msg, Throwable tr){
		if(use_log){
			return Log.v("ftbt", msg, tr);
		}else{
			return -1;
		}
	}
}
