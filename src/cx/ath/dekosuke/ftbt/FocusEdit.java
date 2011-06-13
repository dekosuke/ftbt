package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.R.id;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.focuswords);
		ListView listView = (ListView) findViewById(id.listview);
		
		// EditTextでenter押したときのイベント取る
		// http://android-vl0c0lv.blogspot.com/2009/08/edittext.html
		// このくらい簡単な処理書くのに行数多すぎるだろjk...
		EditText text_e = (EditText) findViewById(id.newword);
		text_e.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// ここではEditTextに改行が入らないようにしている。
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					return true;
				}
				// Enterを離したときに検索処理を実行
				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					FocusEdit activity = (FocusEdit) v.getContext();
					activity.onClickRegNewWordButton(v);
					return false;
				}
				return false;
			}
		});

		
		try {
			words = FocusedSettings.get(this);
		} catch (Exception e) {
			FLog.d("message", e);
		}
		 adapter = new FocusEditAdapter(this,
				R.layout.focuswords_row, words);
		listView.setAdapter(adapter);
		setTitle("キーワード編集"+" - "+getString(R.string.app_name));

		
	}
	
	public void onClickRegNewWordButton(View v){
		EditText text_e = (EditText) findViewById(id.newword);
		String text = text_e.getText().toString();
		try{
			StringUtil.validateFocusWord(text);
		}catch(Exception e){
			FLog.d(e.getMessage());
			if(e.getMessage().equals("noword")){
				//Toast.makeText(this, "空のキーワードは登録できません", Toast.LENGTH_LONG).show();	
				//空白だと何も言わない
			}else if(e.getMessage().equals("toolongword")){
				Toast.makeText(this, "キーワードは10文字以内に収めてください", Toast.LENGTH_LONG).show();				
			}else if(e.getMessage().equals("spaces_exist")){
				Toast.makeText(this, "キーワードにスペース(空白)を含めることはできません", Toast.LENGTH_LONG).show();
			}else if(e.getMessage().equals("newline_exist")){
				Toast.makeText(this, "キーワードに改行を含めることはできません", Toast.LENGTH_LONG).show();				
			}else{
				Toast.makeText(this, "不正なキーワードです", Toast.LENGTH_LONG).show();				
			}
			return;
		}
		for(int i=0;i<adapter.items.size();++i){
			if(adapter.items.get(i).equals(text)){
				Toast.makeText(this, "そのキーワードはすでに登録済です", Toast.LENGTH_LONG).show();					
				return;
			}
		}
		adapter.addLast(text);
		text_e.setText(""); //登録されたキーワードを消す
		//adapter.notifyDataSetChanged();
		//adapter.notifyAll();
	}
}
