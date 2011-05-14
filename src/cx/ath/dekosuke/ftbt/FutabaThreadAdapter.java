package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;
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
import android.content.Intent;

import java.io.InputStream;
import java.net.URL;

//BufferedStreamのエラー問題対応
import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.OutputStream;
//その２
import java.net.HttpURLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.ImageView;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

import java.lang.Thread; //To call Thread.sleep

public class FutabaThreadAdapter extends ArrayAdapter {

	public ArrayList items;
	private LayoutInflater inflater;

	// 画面サイズ
	private int width;
	private int height;

	public FutabaThreadAdapter(Context context, int textViewResourceId,
			ArrayList items) {
		super(context, textViewResourceId, items);
		this.items = items;
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

		try {

			if (view == null) {
				// 受け取ったビューがnullなら新しくビューを生成
				view = inflater.inflate(R.layout.futaba_thread_row, null);
				// 背景画像をセットする
				// view.setBackgroundResource(R.drawable.back);
			}
			
			FutabaThread activity = (FutabaThread) getContext();

			if (position == 0) { // 最初だけ色違う
				view.setBackgroundColor(Color.rgb(255, 255, 238));
			}else {
				// ここのルーチンがないとおかしくなるので,view再利用の様子が良く分かる
				view.setBackgroundColor(Color.rgb(240, 224, 214));
			}

			// 表示すべきデータの取得
			FutabaStatus item = (FutabaStatus) items.get(position);
			FLog.d("potision=" + position + " datestr=" + item.datestr);
			if (item != null) {
				TextView title = (TextView) view.findViewById(R.id.title);
				String title_base = item.title;// StringUtil.safeCut(, 30);
				if (item.name != null) { // こういう風に足さないと改行時に消えてしまうのでやむなく
					title_base += " <font color=\"#117743\">" + item.name
							+ "</font>";
					if(activity.currentSize!=0 && position>=activity.prevSize){ //新着
						title_base += " New!";
					}
					// name.setText(item.name);// item.getImgURL());
				}
				CharSequence cs_title = Html.fromHtml(title_base); // HTML表示
				title.setText(cs_title);// item.getImgURL());
				// TextView name = (TextView) view.findViewById(R.id.name);

				// スクリーンネームをビューにセット
				TextView text = (TextView) view.findViewById(R.id.maintext);
				TextView bottomtext = (TextView) view
						.findViewById(R.id.bottomtext);
				if (item.datestr != null) {
					bottomtext.setText(item.datestr + " " + item.idstr);
				}

				Bitmap bm = null;
				ImageView iv = (ImageView) view.findViewById(R.id.image);
				iv.setImageBitmap(bm);

				// 画像をセット
				try {
					if (item.imgURL != null) {
						iv.setTag(item.bigImgURL);
						bm = Bitmap.createBitmap(item.width, item.height,
								Bitmap.Config.ALPHA_8);
						iv.setImageBitmap(bm);
						ImageGetTask task = new ImageGetTask(iv);
						task.execute(item.imgURL);
						// title.setText("(画像あり)");
					} else { // 画像なし
						/*
						 * FLog.d("w="+item.width+" h="+item.height ); bm =
						 * Bitmap.createBitmap(item.width, item.height,
						 * Bitmap.Config.ALPHA_8); iv.setImageBitmap(bm);
						 */
					}
				} catch (Exception e) {
					FLog.d("message", e);
				}

				// テキストをビューにセット
				if (text != null) {
					CharSequence cs = Html.fromHtml(item.text); // HTML表示
					text.setText(cs);
				}
			}

		} catch (Exception e) {
			FLog.d("message", e);
		}

		return view;
	}

	static int getSmlImageWidth(Bitmap bm, int width, int height) {
		float s_x = Math
				.max(1.0f, (float) bm.getWidth() * 1.5f / (float) width);
		float s_y = Math.max(1.0f, (float) bm.getHeight() * 2.0f
				/ (float) height);
		float scale = Math.max(s_x, s_y);
		return (int) (bm.getWidth() / scale);
	}

	static int getSmlImageHeight(Bitmap bm, int width, int height) {
		float s_x = Math
				.max(1.0f, (float) bm.getWidth() * 1.5f / (float) width);
		float s_y = Math.max(1.0f, (float) bm.getHeight() * 2.0f
				/ (float) height);
		float scale = Math.max(s_x, s_y);
		return (int) (bm.getHeight() / scale);
	}

	// 画像取得用スレッド
	class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView image;
		private String tag;

		public ImageGetTask(ImageView _image) {
			image = _image;
			if (_image == null) {
				FLog.d("imageview is null!!!");
			}
			tag = image.getTag().toString();
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap bm = ImageCache.getImage(urls[0]);
			FLog.d("futabaAdapter thread start");
			if (bm == null) { // does not exist on cache
				// synchronized (FutabaAdapter.lock){
				try {
					ImageCache.setImage(urls[0]);
					bm = ImageCache.getImage(urls[0]);
				} catch (Exception e) {
					FLog.d("message", e);
					FLog.d("fail with " + urls[0]);
					try {
						Thread.sleep(1 * 1000);
					} catch (Exception e2) {
						FLog.d("message", e2);
					}
				} finally {

				}
			}
			return bm;
		}

		// メインスレッドで実行する処理
		@Override
		protected void onPostExecute(Bitmap result) {
			// FLog.d(,
			// "tag="+tag+" image.getTag="+image.getTag().toString() );
			try {
				// Tagが同じものが確認して、同じであれば画像を設定する
				if (tag.equals(image.getTag())) {
					if (result == null) { // 画像読み込み失敗時
						TextView screenName = (TextView) image
								.findViewById(R.id.title);
						if (screenName != null) {
							screenName.setText("(画像読み込み失敗)");// item.getImgURL());
						}
						return;
					}
					image.setImageBitmap(result);
					if (true) { // クリックのリスナー登録 このリスナー登録は、画像をロードしたときにするようにしたい
						image.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								try {
									FLog.d("intent calling thread activity");
									Intent intent = new Intent();
									FutabaThread activity = (FutabaThread) getContext();
									// Log.d ( "ftbt", threadNum );
									// これスレッドごとに作られているのが結構ひどい気がする
									intent.putExtra("imgURLs",
											activity.getImageURLs());
									intent.putExtra("thumbURLs", activity.getThumbURLs());
									intent.putExtra("myImgURL", tag);
									intent.setClassName(
											activity.getPackageName(), activity
													.getClass().getPackage()
													.getName()
													+ ".ImageCatalog");
									// http://android.roof-balcony.com/intent/intent/
									activity.startActivityForResult(intent,
											activity.TO_IMAGECATALOG);
								} catch (Exception e) {
									FLog.d("message", e);
								}
							}
						});
					}

				}

			} catch (Exception e) {
				FLog.d("message", e);
			}
		}

	}
}
