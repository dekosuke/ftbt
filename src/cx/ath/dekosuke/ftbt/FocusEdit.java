package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.R.id;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

public class FocusEdit extends Activity {

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
		ArrayList<String> words = new ArrayList<String>();
		words.add("こーまり");
		FocusEditAdapter adapter = new FocusEditAdapter(this, R.layout.focuswords_row,
				words);
		listView.setAdapter(adapter);

	}
}
