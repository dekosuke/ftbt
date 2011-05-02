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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ImageCatalog extends Activity {

	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	Toast toast = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {

		//	linearLayout.addView(gallery, createParam(WC, FP));

			/*
			 * ArrayAdapter<String> arrayAdapter = new
			 * ArrayAdapter<String>(this, R.layout.imagegallery_row, data);
			 * 
			 * gallery.setAdapter(arrayAdapter);
			 */

			Log.d("ftbt", "ImageCatalog.onCreate start");
			Intent intent = getIntent();
			Log.d("ftbt", "hoge");
			ArrayList<String> imgURLs = (ArrayList<String>) intent
					.getSerializableExtra("imgURLs");

			Log.d("ftbt", "hoge-"+imgURLs.size());
			String myImageURL = (String) intent
					.getSerializableExtra("myImgURL");
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

			//ImageCatalogSingleView sview = new ImageCatalogSingleView(this);
			setContentView(R.layout.imagegallery_row);
			Button download = (Button) findViewById(id.down_btn);
			download.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try{
						// 画像を保存する
						String imgFile = CircleList.get();
						File file = new File(imgFile);
						//TODO:ここでキャッシュあるならそれから保存
						ImageCache.saveImage(imgFile);
						/*
						SDCard.saveFromURL(file.getName(), new URL(imgFile),
								false);
						*/
						if(toast!=null){
							toast.cancel();
						}
						toast = Toast.makeText(v.getContext(), "画像"+file.getName()+"を保存しました", 
								Toast.LENGTH_SHORT);
						toast.show();
					}catch(Exception e){
						Log.i("ftbt", "message", e);
					}
				}
			});
			Button prev = (Button) findViewById(id.prev_btn);
			prev.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try{
						CircleList.move(-1);
						ImageCatalogSingleView imageview = (ImageCatalogSingleView) findViewById(id.image);
						imageview.setImage();
					}catch(Exception e){
						Log.i("ftbt", "message", e);
					}
				}
			});
			Button next = (Button) findViewById(id.next_btn);
			next.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try{
						CircleList.move(+1);
						ImageCatalogSingleView imageview = (ImageCatalogSingleView) findViewById(id.image);
						imageview.setImage();
					}catch(Exception e){
						Log.i("ftbt", "message", e);
					}
				}
			});

		} catch (Exception e) {
			Log.d("ftbt", "message", e);
		}
	}

	private LinearLayout.LayoutParams createParam(int w, int h) {
		return new LinearLayout.LayoutParams(w, h);
	}

}
