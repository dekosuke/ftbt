package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import cx.ath.dekosuke.ftbt.R.id;

//タブ式トップページ

public class ftbt extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TabHostのインスタンスを取得
		TabHost tabs = getTabHost();
		// レイアウトを設定
		LayoutInflater.from(this).inflate(R.layout.tabmain,
				tabs.getTabContentView(), true);
		Intent intent;
		try{
			// タブシートの設定
			intent = new Intent().setClassName(getPackageName(), getClass().getPackage().getName() + ".ftbt_tab");
			intent.putExtra("mode", "all");
			TabSpec tab01 = tabs.newTabSpec("TabSheet1");
			tab01.setIndicator("すべて");
			tab01.setContent(intent);
			tabs.addTab(tab01);
		
			intent = new Intent().setClassName(getPackageName(), getClass().getPackage().getName() + ".ftbt_tab");
			intent.putExtra("mode", "fav");
			ArrayList<FutabaBBS> favThreads=new ArrayList<FutabaBBS>();
			FutabaBBS touhouBBS = new FutabaBBS();
			touhouBBS.name = "東方";
			touhouBBS.url="http://may.2chan.net/40/";
			favThreads.add(touhouBBS);
			intent.putExtra("favThreads", favThreads);
			TabSpec tab02 = tabs.newTabSpec("TabSheet2");
			tab02.setIndicator("お気に入り");
			tab02.setContent(R.id.sheet02_id);
			tab02.setContent(intent);
			tabs.addTab(tab02);
			// 初期表示のタブ設定
			tabs.setCurrentTab(0);
		}catch(Exception e){
			Log.i("ftbt", "message", e);
		}
	}
}