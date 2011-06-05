package cx.ath.dekosuke.ftbt;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.R.id;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
	ArrayList<String> imgURLs = new ArrayList<String>();
	ArrayList<String> thumbURLs = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 無操作で暗くなるのを防ぐ
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		try {

			// linearLayout.addView(gallery, createParam(WC, FP));

			setTitle("画像ビューワ - " + getString(R.string.app_name));

			FLog.d("ImageCatalog.onCreate start");
			Intent intent = getIntent();
			FLog.d("hoge");
			imgURLs = (ArrayList<String>) intent
					.getSerializableExtra("imgURLs");
			thumbURLs = (ArrayList<String>) intent
					.getSerializableExtra("thumbURLs");
			FLog.d("hoge-" + imgURLs.size());
			myImageURL = (String) intent.getSerializableExtra("myImgURL");
			FLog.d(myImageURL);

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
			setContentView(R.layout.imagegallery);
			Button download = (Button) findViewById(id.down_btn);
			download.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						// 画像を保存する
						String imgFile = CircleList.get();
						File file = new File(imgFile);
						File saved_file= ImageCache.saveImage(imgFile);
						if (saved_file != null) {
							if (toast != null) {
								toast.cancel();
							}
							toast = Toast.makeText(v.getContext(),
									saved_file.getAbsolutePath() + "に保存しました", Toast.LENGTH_SHORT);
							toast.show();
							
							// ギャラリーに反映されるように登録
							// http://www.adakoda.com/adakoda/2010/08/android-34.html
							String mimeType = StringUtil.getMIMEType(saved_file.getName());
							
							FLog.d("name="+saved_file.getName());
							FLog.d("mime="+mimeType);

							// ContentResolver を使用する場合
							ContentResolver contentResolver = getContentResolver();
							ContentValues values = new ContentValues(7);
							values.put(Images.Media.TITLE, saved_file.getName());
							values.put(Images.Media.DISPLAY_NAME, saved_file.getName());
							values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
							values.put(Images.Media.MIME_TYPE, mimeType);
							values.put(Images.Media.ORIENTATION, 0);
							values.put(Images.Media.DATA, saved_file.getPath());
							values.put(Images.Media.SIZE, saved_file.length());
							contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
						}
					} catch (Exception e) {
						FLog.d("message", e);
					}
				}
			});

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
						FLog.d("message", e);
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
						FLog.d("message", e);
					}
				}
			});
			Button rotate = (Button) findViewById(id.rotate_btn);
			rotate.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						ImageCatalogSingleView imageview = (ImageCatalogSingleView) findViewById(id.image);
						imageview.rotateImage();
					} catch (Exception e) {
						FLog.d("message", e);
					}
				}
			});

			Button gridview = (Button) findViewById(id.gridview_btn);
			gridview.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						FLog.d("intent calling gridview activity");
						Intent intent = new Intent();
						// Log.d ( "ftbt", threadNum );
						intent.putExtra("position", CircleList.pos());
						intent.putExtra("imgURLs", imgURLs);
						intent.putExtra("thumbURLs", thumbURLs);
						intent.setClassName(getPackageName(), getClass()
								.getPackage().getName() + ".ThumbGrid");
						startActivity(intent);
					} catch (Exception e) {
						FLog.d("message", e);
					}
				}
			});
			// 一覧─＞画像ー＞一覧ー＞画像とたどるのをふせぐために、特定の場所からしか見せない
			gridview.setVisibility(View.GONE);
			try {
				String className = getCallingActivity().getClassName();
				FLog.d("className=" + className);
				if (className.equals("cx.ath.dekosuke.ftbt.FutabaThread")) {
					gridview.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				FLog.d("message", e);
			}

			moveImage(0);
			setReturnImage();

		} catch (Exception e) {
			FLog.d("message", e);
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
		// for GC Imageview.clearImageはSystem.gc呼んでる
		ImageCatalogSingleView imageview = (ImageCatalogSingleView) findViewById(id.image);
		imageview.clearImage();
		FLog.d("ImageCatalog::onDestroy()");

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
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, myImageURL
					+ getString(R.string.hashtagstr));
			try {
				startActivityForResult(intent, 0);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "client not found", Toast.LENGTH_LONG)
						.show();
			}
			return true;

		case R.id.share:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, myImageURL);
			try {
				startActivityForResult(intent, 0);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "client not found", Toast.LENGTH_SHORT)
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
			Uri uri = Uri.parse(getString(R.string.helpurl));
			intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setClassName("com.android.browser",
					"com.android.browser.BrowserActivity");
			try {
				startActivity(intent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "ブラウザが見つかりません", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return false;
	}
}
