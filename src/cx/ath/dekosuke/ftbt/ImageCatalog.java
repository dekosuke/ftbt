package cx.ath.dekosuke.ftbt;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.R.id;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageCatalog extends Activity {

	Toast toast = null;
	String myImageURL = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {

			// linearLayout.addView(gallery, createParam(WC, FP));

			Log.d("ftbt", "ImageCatalog.onCreate start");
			Intent intent = getIntent();
			Log.d("ftbt", "hoge");
			ArrayList<String> imgURLs = (ArrayList<String>) intent
					.getSerializableExtra("imgURLs");

			Log.d("ftbt", "hoge-" + imgURLs.size());
			myImageURL = (String) intent.getSerializableExtra("myImgURL");
			Log.d("ftbt", myImageURL);

			// ここでIntentによる追加情報からCircleListを構築する
			CircleList.clear();

			// これスタティックにするのはどうかという感じがする
			for (int i = 0; i < imgURLs.size(); i++) {
				String imgURL = imgURLs.get(i);
				CircleList.add(imgURL);
				if (imgURL.equals(myImageURL)) {
					CircleList.moveToLast();
				}
			}

			// ImageCatalogSingleView sview = new ImageCatalogSingleView(this);
			setContentView(R.layout.imagegallery_row);
			Button download = (Button) findViewById(id.down_btn);
			download.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						// 画像を保存する
						String imgFile = CircleList.get();
						File file = new File(imgFile);
						ImageCache.saveImage(imgFile);
						/*
						 * SDCard.saveFromURL(file.getName(), new URL(imgFile),
						 * false);
						 */
						if (toast != null) {
							toast.cancel();
						}
						toast = Toast.makeText(v.getContext(),
								"画像" + file.getName() + "を保存しました",
								Toast.LENGTH_SHORT);
						toast.show();
					} catch (Exception e) {
						Log.i("ftbt", "message", e);
					}
				}
			});
			
			//これメモリリークがあるっぽいな・・・
			//Dog Days壁紙スレで落ちる・・
			Button prev = (Button) findViewById(id.prev_btn);
			prev.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						ImageCatalogSingleView imageview = (ImageCatalogSingleView) findViewById(id.image);
						imageview.clearImage();
						moveImage(-1);
						imageview.setImage();
						setReturnImage();
					} catch (Exception e) {
						Log.i("ftbt", "message", e);
					}
				}
			});
			Button next = (Button) findViewById(id.next_btn);
			next.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						ImageCatalogSingleView imageview = (ImageCatalogSingleView) findViewById(id.image);
						imageview.clearImage();
						moveImage(+1);
						imageview.setImage();
						setReturnImage();
					} catch (Exception e) {
						Log.i("ftbt", "message", e);
					}
				}
			});
			Button rotate = (Button) findViewById(id.rotate_btn);
			rotate.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						ImageCatalogSingleView imageview = 
							(ImageCatalogSingleView) findViewById(id.image);
						imageview.rotateImage();
						//imageview.clearImage(); //debug
					} catch (Exception e) {
						Log.i("ftbt", "message", e);
					}
				}
			});

			moveImage(0);
			setReturnImage();

		} catch (Exception e) {
			Log.d("ftbt", "message", e);
		}
	}

	public void moveImage(int num) {
		CircleList.move(num);
		TextView imagenum = (TextView) findViewById(id.imagenum);
		imagenum.setText("画像:" + (1 + CircleList.pos()) + "/"
				+ CircleList.size());
	}

	// 戻ったときのインテントにパラメータ渡す（場所復帰のため）
	public void setReturnImage() {
		Intent ret_i = new Intent();
		ret_i.putExtra("imgURL", CircleList.get());
		setResult(RESULT_OK, ret_i);
	}

	private LinearLayout.LayoutParams createParam(int w, int h) {
		return new LinearLayout.LayoutParams(w, h);
	}

	@Override
	public void onDestroy() {
		//for GC どうせ参照外れるしあまり意味ないかも
		ImageCatalogSingleView imageview = (ImageCatalogSingleView) findViewById(id.image);
		imageview.clearImage();
		Log.d("ftbt", "ImageCatalog::onDestroy()");
		
		super.onDestroy();
		// if(isFinishing()){
		// スレ一覧に戻ったときに渡す情報
		// }
	}

	// メニュー
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_imagecatalog, menu);
		return true;
	}

	// メニューをクリック
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.tweet:
			String status_encoded = myImageURL; // URIエンコードされた、ツイートしたい文章
			// Uri uri =
			// Uri.parse("http://twitter.com/?status="+status_encoded);
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, status_encoded);
			try {
				startActivityForResult(intent, 0);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "client not found", Toast.LENGTH_LONG)
						.show();
			}
			return true;

		case R.id.settings:
			intent = new Intent();
			intent.setClassName(getPackageName(), getClass().getPackage()
					.getName() + ".PrefSetting");
			startActivity(intent);
			return true;
		case R.id.about:
			Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
}
