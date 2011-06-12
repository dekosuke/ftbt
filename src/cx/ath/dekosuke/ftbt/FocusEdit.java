package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.R.id;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

public class FocusEdit extends Activity {
	ArrayList<String> words = new ArrayList<String>();
	FocusEditAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 無操作で暗くなるのを防ぐ
		if (getResources().getBoolean(R.bool.avoidsleep)) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		setContentView(R.layout.focuswords);
		ListView listView = (ListView) findViewById(id.listview);
		try {
			words = FocusedSettings.get(this);
		} catch (Exception e) {
			FLog.d("message", e);
		}
		 adapter = new FocusEditAdapter(this,
				R.layout.focuswords_row, words);
		listView.setAdapter(adapter);

	}
	
	public void onClickRegNewWordButton(View v){
		EditText text_e = (EditText) findViewById(id.newword);
		String text = text_e.getText().toString();
		adapter.addLast(text);
		//adapter.notifyDataSetChanged();
		//adapter.notifyAll();
	}
}
