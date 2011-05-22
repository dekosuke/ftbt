package cx.ath.dekosuke.ftbt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FutabaThreadOnLongClickListener implements
		AdapterView.OnItemLongClickListener, OnClickListener {
	public int chosen = 0;
	public int currentPosition = 0;
	FutabaThread fthread = null;

	public boolean onItemLongClick(AdapterView<?> arg0, View view, int arg2,
			long arg3) {
		if(view == null){
			FLog.d("null view at onItemLongClick");
			return false;
		}
		TextView bottomtext = (TextView) view.findViewById(R.id.bottomtext);
		if (bottomtext.length() < 5) { // 区切り線とか
			return false;
		}
		fthread = (FutabaThread) view.getContext();
		FLog.d("longclick arg2="+arg2+" arg3="+arg3);
		AlertDialog.Builder dlg;
		dlg = new AlertDialog.Builder(fthread);
		dlg.setTitle("レスに対する操作");
		String[] strs_temp = null;
		FutabaStatus item = (FutabaStatus)fthread.adapter.items.get(arg2);

		//これもっと良い書き方ないのか・・・
		if(arg2==0){
			String[] temp = { "削除", "引用して返信", "他アプリと共有"};
			strs_temp = temp;
		}else if(item!=null && item.id != fthread.adapter.shioriPosition){
			String[] temp = { "削除", "引用して返信", "他アプリと共有", "栞をはさむ" };
			strs_temp = temp;	
		}else{
			String[] temp = { "削除", "引用して返信", "他アプリと共有", "栞を削除" };
			strs_temp = temp;	
		}
		currentPosition = arg2;
		final String[] strs= strs_temp;
		final View view_f = view;
		dlg.setSingleChoiceItems(strs, 0, this);
		dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				FLog.d("onclick id=" + id + " chosen=" + chosen);
				if (chosen == 0) {
					// 削除のためのダイアログを開く
					processDeleteDialog(view_f);
				} else if (chosen == 1) {
					// 引用して返信のためのダイアログを開く
					processQuoteDialog(view_f);
				} else if (chosen == 2) {
					// 他アプリと共有のためのダイアログを開く
					processShareDialog(view_f);
				} else if (chosen == 3) {
					// 栞操作
					modifyShiori(view_f);
				}
			}
		});
		dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Catalog.cancel();
				// Catalog.this.deleteThreads(false);
			}
		});
		dlg.show();
		return false;

	}

	// これ数ヶ所で使いまわしてるので注意
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		chosen = which;
	}

	public void processQuoteDialog(View view) {
		chosen = 0;
		TextView text = (TextView) view.findViewById(R.id.maintext);
		if (text != null) {
			String[] addition = { "(レス全体)" };
			final String strs_all = text.getText().toString();
			final String[] strs = StringUtil.nonBlankSplit(strs_all, addition);
			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(fthread);
			dlg.setTitle("引用する場所を選択");
			// dlg.setMessage("クリップボードにコピーするテキストを選択してください");
			dlg.setCancelable(true);
			dlg.setSingleChoiceItems(strs, 0, this);
			dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FLog.d("chosen=" + chosen);
					if (chosen >= 0 && chosen < strs.length) {
						String text = strs[chosen];
						if (chosen == strs.length - 1 && strs.length > 2) { // すべて選択
							text = strs_all;
						}
						// アクティビティ飛ばす
						Intent intent = new Intent();
						intent.putExtra("baseURL", fthread.baseURL);
						intent.putExtra("threadNum", fthread.threadNum);
						intent.setClassName(fthread.getPackageName(),
								getClass().getPackage().getName() + ".Post");
						intent.putExtra("postText", StringUtil.quote(text));

						try {
							fthread.startActivity(intent);
						} catch (android.content.ActivityNotFoundException ex) {
							FLog.d("failed to find target activity to share text");
						}
					}
				}
			});
			dlg.setNegativeButton("キャンセル",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Catalog.cancel();
							// Catalog.this.deleteThreads(false);
						}
					});
			dlg.show();

		}
	}

	public void processShareDialog(View view) {
		chosen = 0;
		TextView text = (TextView) view.findViewById(R.id.maintext);
		if (text != null) {
			String[] addition = { "(レス全体)" };
			final String strs_all = text.getText().toString();
			final String[] strs = StringUtil.nonBlankSplit(strs_all, addition);
			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(fthread);
			dlg.setTitle("テキストを共有\n(外部アプリに送る)");
			// dlg.setMessage("クリップボードにコピーするテキストを選択してください");
			dlg.setCancelable(true);
			dlg.setSingleChoiceItems(strs, 0, this);
			dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FLog.d("chosen=" + chosen);
					if (chosen >= 0 && chosen < strs.length) {
						String text = strs[chosen];
						if (chosen == strs.length - 1 && strs.length > 2) { // すべて選択
							text = strs_all;
						}
						// アクティビティ飛ばす
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_TEXT,
								StringUtil.quote(text));
						try {
							fthread.startActivity(intent);
						} catch (android.content.ActivityNotFoundException ex) {
							FLog.d("failed to find target activity to share text");
						}
					}
				}
			});
			dlg.setNegativeButton("キャンセル",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Catalog.cancel();
							// Catalog.this.deleteThreads(false);
						}
					});
			dlg.show();

		}
	}

	public void processDeleteDialog(View view) {
		AlertDialog.Builder dlg;
		dlg = new AlertDialog.Builder(fthread);
		dlg.setTitle("削除キーを入力してください");
		// dlg.create()
		EditText editText = new EditText(fthread);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		TextView bottomtext = (TextView) view.findViewById(R.id.bottomtext);
		String[] splits = bottomtext.getText().toString().split("No.");
		final String resNum = splits[splits.length - 1];
		final EditText editText_f = editText;
		dlg.setView(editText);
		dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				FLog.d("ok pressed with key=" + editText_f.getText());
				// 割と力技
				deletePost(resNum, editText_f.getText().toString());
			}
		});
		dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		dlg.show();

	}

	public void deletePost(String resNum, String deletekey) {
		// というわけでリクエストの作成
		// 通常投稿のコードとだいぶ似てます
		// スレッドに一度アクセスしてcookieセット－＞書き込みの２度アクセス
		try {
			FLog.d("start post!");

			DefaultHttpClient httpClient;
			httpClient = new DefaultHttpClient();
			// httpClient.setHeader( "Connection", "Keep-Alive" );
			FutabaCookieManager.loadCookie(httpClient);
			httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
					CookiePolicy.BROWSER_COMPATIBILITY);
			httpClient.getParams()
					.setParameter("http.connection.timeout", 5000);
			httpClient.getParams().setParameter("http.socket.timeout", 3000);

			try {
				// クッキー内容の取得
				{
					List<Cookie> cookies = httpClient.getCookieStore()
							.getCookies();
					if (cookies.isEmpty()) {
						FLog.d("Cookie None");
					} else {
						for (int i = 0; i < cookies.size(); i++) {
							FLog.d("" + cookies.get(i).toString());
						}
					}
				}
			} catch (Exception e) {
				FLog.d("message", e);
			}

			FLog.d("deletekey=" + deletekey);
			FLog.d("resNum=" + resNum);

			String urlStr = fthread.baseURL + "futaba.php";

			HttpPost httppost = new HttpPost(urlStr);
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
			nameValuePair.add(new BasicNameValuePair("mode", "usrdel"));
			nameValuePair.add(new BasicNameValuePair(resNum, "delete"));
			nameValuePair.add(new BasicNameValuePair("pwd", deletekey));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
			httppost.addHeader("referer", fthread.threadURL);
			HttpResponse response = httpClient.execute(httppost);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			response.getEntity().writeTo(byteArrayOutputStream);
			String retData = byteArrayOutputStream.toString("Shift-JIS");

			FLog.v("data2=" + retData);
			FLog.d("2nd access end");

			DelpostParser parser = new DelpostParser();
			String contents = parser.parse(fthread, retData);
			if (!contents.equals("")) {
				Toast.makeText(fthread, contents, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(fthread, "レスを削除しました", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}
	
	public void modifyShiori(View view) {
		
		FutabaStatus item = (FutabaStatus)fthread.adapter.items.get(currentPosition);
		if(item.id == fthread.adapter.shioriPosition){ //栞があるー＞削除
			fthread.removeShiori(currentPosition);			
		}else{ //栞がない場所－＞栞追加
			fthread.registerShiori(currentPosition);
		}
	}	
}

/*
 * 
 * if (view != null) { TextView text = (TextView)
 * view.findViewById(R.id.maintext); if (text != null) { // これいまいちだな・・・ String[]
 * addition = { "(レス全体)" }; final String strs_all = text.getText().toString();
 * final String[] strs = StringUtil.nonBlankSplit(strs_all, addition); //
 * FLog.d(str);
 * 
 * // CharSequence[] items = new CharSequence[strs.length]; AlertDialog.Builder
 * dlg; dlg = new AlertDialog.Builder(FutabaThread.this);
 * dlg.setTitle("テキストを共有\n(外部アプリに送る)"); //
 * dlg.setMessage("クリップボードにコピーするテキストを選択してください"); dlg.setCancelable(true);
 * dlg.setSingleChoiceItems(strs, 0, new DialogInterface.OnClickListener() {
 * public void onClick(DialogInterface dialog, int item) { //
 * button.setText(String.format("%sが選択されました。",items[item])); //
 * Catalog.this.delete_option = item; FutabaThread.this.itemLongClick_chosen =
 * item; } }); dlg.setPositiveButton("OK", new DialogInterface.OnClickListener()
 * { public void onClick(DialogInterface dialog, int id) { //
 * Catalog.this.finish(); // Catalog.this.deleteThreads(); int chosen =
 * FutabaThread.this.itemLongClick_chosen; FLog.d("chosen=" + chosen); if
 * (chosen >= 0 && chosen < strs.length) { String text = strs[chosen];
 * if(chosen==strs.length-1 && strs.length>2){ //すべて選択 text = strs_all; } //
 * アクティビティ飛ばす Intent intent = new Intent( Intent.ACTION_SEND);
 * intent.setType("text/plain"); intent.putExtra(Intent.EXTRA_TEXT, text); try {
 * startActivity(intent); } catch (android.content.ActivityNotFoundException ex)
 * { FLog.d("failed to find target activity to share text"); } } } });
 * dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() { public
 * void onClick(DialogInterface dialog, int id) { // Catalog.cancel(); //
 * Catalog.this.deleteThreads(false); } }); dlg.show();
 * 
 * } }
 */