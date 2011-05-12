package cx.ath.dekosuke.ftbt;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.FutabaThreadAdapter.ImageGetTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class ThumbGridAdapter extends ArrayAdapter {
	ArrayList<String> urls;
	private LayoutInflater inflater;

	private int width;
	private int height;
	private int itemSize;

	public ThumbGridAdapter(Context context, int textViewResourceId,
			ArrayList items) {
		super(context, textViewResourceId, items);
		this.urls = items;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 画面サイズの取得
		WindowManager wm = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();
		itemSize = width / 3 - 10;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ビューを受け取る
		View view = convertView;

		try {
			if (view == null) {
				// 受け取ったビューがnullなら新しくビューを生成
				view = inflater.inflate(R.layout.thumbgridelement, null);
				// 背景画像をセットする
				// view.setBackgroundResource(R.drawable.back);
			}
			// 表示すべきデータの取得
			String url = (String) urls.get(position);
			if (url != null) {

				// 画像をセット
				try {
					ImageView iv = (ImageView) view.findViewById(R.id.image);

					iv.setTag("" + position);
					Bitmap bm = Bitmap.createBitmap(itemSize, itemSize,
							Bitmap.Config.ALPHA_8);
					iv.setImageBitmap(bm);
					ImageGetTask task = new ImageGetTask(iv);
					task.execute(url);
					// title.setText("(画像あり)");
				} catch (Exception e) {
					FLog.d("message", e);
				}
			}
		} catch (Exception e) {
			FLog.d("message", e);
		}

		return view;
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
			FLog.d("tag=" + tag);
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap bm = ImageCache.getImage(urls[0]);
			HttpURLConnection con = null;
			InputStream is = null;
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
					try {
						if (con != null) {
							con.disconnect();
						}
						if (is != null) {
							is.close();
						}
					} catch (Exception e) {
						FLog.d("doInBackground", e);
					}
					// }
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
						return;
					}
					/*
					 * image.setScaleType(ScaleType.MATRIX); Matrix matrix = new
					 * Matrix(); matrix.setScale(.5f, .5f);
					 * image.setImageMatrix(matrix);
					 */
					result = ImageResizer.ResizeCenter(result, itemSize);
					image.setImageBitmap(result);
					if (true) { // クリックのリスナー登録 このリスナー登録は、画像をロードしたときにするようにしたい
						image.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								try {
									FLog.d("intent calling imageCatalog activity");
									Intent intent = new Intent();
									ThumbGrid activity = (ThumbGrid) getContext();
									// Log.d ( "ftbt", threadNum );
									// これスレッドごとに作られているのが結構ひどい気がする
									intent.putExtra("imgURLs", activity.imgURLs);
									intent.putExtra("thumbURLs",
											activity.thumbURLs);
									intent.putExtra("myImgURL",
											activity.imgURLs.get(Integer
													.parseInt(tag)));
									intent.setClassName(
											activity.getPackageName(), activity
													.getClass().getPackage()
													.getName()
													+ ".ImageCatalog");
									// http://android.roof-balcony.com/intent/intent/
									activity.startActivity(intent);
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
