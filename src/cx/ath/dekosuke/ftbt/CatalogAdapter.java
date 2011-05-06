package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.text.Html;
import android.util.Log;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.io.InputStream;
import java.net.URL;

import cx.ath.dekosuke.ftbt.R.id;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.ImageView;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

public class CatalogAdapter extends ArrayAdapter {

	public ArrayList<FutabaThreadContent> items;
	private LayoutInflater inflater;
	private Context context;

	// 画面サイズ
	private int width;
	private int height;

	public CatalogAdapter(Context context, int textViewResourceId,
			ArrayList items) {
		super(context, textViewResourceId, items);
		this.items = (ArrayList<FutabaThreadContent>)items;
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 画面サイズの取得
		WindowManager wm = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ビューを受け取る
		View view = convertView;

		// 表示すべきデータの取得
		final FutabaThreadContent item = (FutabaThreadContent) items.get(position);
		final String threadNum = item.threadNum;

		if (view == null) {
			// 受け取ったビューがnullなら新しくビューを生成
			view = inflater.inflate(R.layout.futaba_catalog_row, null);
			// 背景画像をセットする
			// view.setBackgroundResource(R.drawable.back);

		}

		//カタログからスレッドをクリックしたときのリスナー
		if (true) {
			view.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Log.d("ftbt", "intent calling thread activity");
					Intent intent = new Intent();
					Catalog activity = (Catalog) getContext();
					try{
						FutabaThreadContent thread = item;
						if(!activity.mode.equals("history")){ //通常
							thread.baseUrl = activity.baseUrl;
						}
						Calendar calendar = Calendar.getInstance();
						thread.lastAccessed = calendar.getTimeInMillis();
						HistoryManager man = new HistoryManager();
						man.Load();
						int maxHistoryNum = 5;
						try{
						SharedPreferences preferences = PreferenceManager
							.getDefaultSharedPreferences(activity);
						maxHistoryNum = Integer.parseInt(preferences.getString(
								activity.getString(R.string.historynum), "5"));
						}catch(Exception e){
							Log.d("ftbt", "message", e);
						}

						man.addThread(thread, maxHistoryNum);
						man.Save();
					}catch(Exception e){
						Log.i("ftbt", "message", e);
					}

					if(!activity.mode.equals("history")){ //通常
						String baseUrl = activity.baseUrl;
						intent.putExtra("baseUrl", baseUrl);						
						Log.d("ftbt", "normal intent");
					}else{
						String baseUrl = item.baseUrl; //履歴モード
						intent.putExtra("baseUrl", baseUrl);		
						Log.d("ftbt", "history intent");
					}
					intent.putExtra("threadNum", threadNum);
					intent.setClassName(activity.getPackageName(), activity
							.getClass().getPackage().getName()
							+ ".FutabaThread");
					activity.startActivity(intent); // Never called!
				}
			});
		}

		Bitmap bm = null;
		ImageView iv = (ImageView) view.findViewById(R.id.image);
		iv.setImageBitmap(bm);
		Catalog activity = (Catalog)getContext();

		if (item != null) {
			// テキストをビューにセット
			TextView text = (TextView) view.findViewById(R.id.bottomtext);
			if (item.text != null) {
				CharSequence cs = Html.fromHtml(item.text);
				text.setText(cs);
			}
			TextView resNum = (TextView) view.findViewById(R.id.resnum);
			resNum.setText(item.resNum+"レス");
			if(activity.mode.equals("history")){ //履歴モード
				TextView BBSName = (TextView) view.findViewById(R.id.bbsname);
				BBSName.setText("("+item.BBSName+")");
				view.setBackgroundColor(Color.parseColor("#F0E0D6"));
				CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
				checkbox.setChecked(false);
			}else{ //通常モード
				CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
				checkbox.setVisibility(View.GONE);
				TextView nonclickableblank = (TextView) view.findViewById(id.nonclickableblank);
				nonclickableblank.setVisibility(View.GONE);
			}

			//とりあえず空画像を作成
			bm = Bitmap.createBitmap(50, 50, Bitmap.Config.ALPHA_8);
			iv.setImageBitmap(bm);

			// 画像をセット
			try {
				if (item.imgURL != null) {
					iv.setTag(item.imgURL);
					ImageGetTask task = new ImageGetTask(iv);
					task.execute(item.imgURL);
					// Log.d( "ftbt", "image "+item.getImgURL()+"set" );
				} else {
					// Bitmap bm = null;
					// ImageView iv = (ImageView)view.findViewById(R.id.image);
					// iv.setImageBitmap(bm);
				}
			} catch (Exception e) {
				Log.d("ftbt", e.toString());
			}

		}
		return view;
	}

	static Object lock_id = new Object();
	static int LastTaskID = -1;

	// 画像取得用スレッド
	class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView image;
		private String tag;
		private int id;

		public ImageGetTask(ImageView _image) {
			image = _image;
			tag = _image.getTag().toString();
			synchronized (CatalogAdapter.lock_id) {
				CatalogAdapter.LastTaskID += 1;
				id = CatalogAdapter.LastTaskID;
			}
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap bm = null;
			try {
				bm = ImageCache.getImage(urls[0]);
				if (bm == null) { // does not exist on cache
					ImageCache.setImage(urls[0]);
					bm = ImageCache.getImage(urls[0]);
				}
				bm = ImageResizer.ResizeWideToSquare(bm);
			} catch (Exception e) {
				Log.d("ftbt", e.toString());
			}
			return bm;
		}

		// メインスレッドで実行する処理
		@Override
		protected void onPostExecute(Bitmap result) {
			// Log.d( "ftbt",
			// "tag="+tag+" image.getTag="+image.getTag().toString() );
			// Tagが同じものが確認して、同じであれば画像を設定する
			if (result != null && tag.equals(image.getTag().toString())) {
				image.setImageBitmap(result);
			}
		}
	}
}
