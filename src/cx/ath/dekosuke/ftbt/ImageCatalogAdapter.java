package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.util.Log;
import android.os.AsyncTask;

import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.view.MotionEvent;

import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

public class ImageCatalogAdapter extends BaseAdapter {

	private ArrayList items;
	private LayoutInflater inflater;
	private Context context;

	// 画面サイズ
	private int width;
	private int height;
	
	public int getCount(){ return items.size(); }
    
    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
	public ImageCatalogAdapter(Context context, ArrayList items) {
		try {
			this.items = items;
			this.context = context;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
			// 画面サイズの取得
			WindowManager wm = ((WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE));
			Display display = wm.getDefaultDisplay();
			width = display.getWidth();
			height = display.getHeight();
		} catch (Exception e) {
			Log.d("ftbt", e.toString());
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// ビューを受け取る
		View view = convertView;

		// 表示すべきデータの取得
		String item = (String) items.get(position);

		try {
			if (view == null) {
				// 受け取ったビューがnullなら新しくビューを生成
				view = inflater.inflate(R.layout.imagegallery_row, null);
				// 背景画像をセットする
				// view.setBackgroundResource(R.drawable.back);

			}

			
			Bitmap bm = null;
			ImageCatalogSingleView iv = (ImageCatalogSingleView) view.findViewById(R.id.image);
			//LinearLayout imagebox = (LinearLayout) view.findViewById(R.id.imagebox);
			//imagebox.setLayoutParams(createParam(Lay));
			
			//画像すべて即読み込み（パフォーマンス悪い）
			bm = ImageCache.getImage(item);
			if (bm == null) { // does not exist on cache
				ImageCache.setImage(item);
				bm = ImageCache.getImage(item);
			}
			//画像サイズをスクリーン全体にする
			iv.setLayoutParams(createParam(width, height));
			iv.setImageBitmap(bm);
			//iv.setLayoutParams(createParam(bm.getWidth(), bm.getHeight()));
			//iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			//iv.setLayoutParams(new Gallery.LayoutParams(
			//			LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)); 

			
			if (false) { //これを普通に入れるとスライドの動きを奪ってしまう(OnTouchにしてfalse返すと親に渡る
				iv.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
						// TODO Auto-generated method stub
						return false;
					}
				});
			}

			if (false && item != null) {

				// 画像をセット
				iv.setTag(item);
				//ImageGetTask task = new ImageGetTask(iv);
				//task.execute(item);
			}
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
		return view;
	}
	
	private LinearLayout.LayoutParams createParam(int w, int h) {
		return new LinearLayout.LayoutParams(w, h);
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
			synchronized (FutabaCatalogAdapter.lock_id) {
				FutabaCatalogAdapter.LastTaskID += 1;
				id = FutabaCatalogAdapter.LastTaskID;
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
				//bm = ImageResizer.ResizeWideToSquare(bm);
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
