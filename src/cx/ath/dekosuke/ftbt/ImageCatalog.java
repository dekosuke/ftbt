package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.LinearLayout;

public class ImageCatalog extends Activity {

	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	private String[] data = { "Orange", "Apple", "Melon", "Lemon" };

	FutabaImageCatalogAdapter adapter;
	
	ImageCatalogGallery gallery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			setContentView(R.layout.imagegallery);

			gallery = (ImageCatalogGallery) findViewById(R.id.gallery);
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

			gallery.setAdapter(new FutabaImageCatalogAdapter(this, imgURLs));
			adapter.notifyDataSetChanged();

		} catch (Exception e) {
			Log.d("ftbt", "message", e);
		}
	}

	private LinearLayout.LayoutParams createParam(int w, int h) {
		return new LinearLayout.LayoutParams(w, h);
	}

}
