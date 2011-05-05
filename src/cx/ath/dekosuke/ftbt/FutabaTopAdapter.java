package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
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
import android.widget.ImageView;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

import java.lang.Thread; //To call Thread.sleep

public class FutabaTopAdapter extends ArrayAdapter {

	private ArrayList items;
	private LayoutInflater inflater;

	public FutabaTopAdapter(Context context, int textViewResourceId,
			ArrayList items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ビューを受け取る
		View view = convertView;

		try {

			if (view == null) {
				// 受け取ったビューがnullなら新しくビューを生成
				view = inflater.inflate(R.layout.futaba_bbs_row, null);
				// 背景画像をセットする
				// view.setBackgroundResource(R.drawable.back);
			}

			// 表示すべきデータの取得
			final FutabaBBS item = (FutabaBBS) items.get(position);
			if (item != null) {
				TextView url = (TextView) view.findViewById(R.id.url);
				url.setText(item.url);
				TextView name = (TextView) view.findViewById(R.id.name);
				name.setText(item.name);
				final Button buttonFavorite = (Button) view.findViewById(R.id.favorite_btn);
				final ftbt_tab activity = (ftbt_tab)getContext();
				if(activity.mode.equals("all")){
					buttonFavorite.setText("Fav!");
				}else{
					buttonFavorite.setText("Unfav");					
				}

				final ViewGroup view_parent = parent;
				buttonFavorite.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if(activity.mode.equals("all")){
							ftbt ftbt_top = (ftbt)activity.getParent();
							//ftbt_top.favoriteBBSs.add(item);
                            ftbt_top.addFavoriteBBSs(item);
    						Toast.makeText(activity, "お気に入り追加しました", Toast.LENGTH_SHORT).show();
						}else{
							ftbt ftbt_top = (ftbt)activity.getParent();
                            ftbt_top.removeFavoriteBBSs(item);
    						Toast.makeText(activity, "お気に入りから外しました", Toast.LENGTH_SHORT).show();
						}
						view_parent.invalidate();
						notifyDataSetChanged();
					}
				});			
				
				//非ボタン部分
				LinearLayout ll_main = (LinearLayout)view.findViewById(R.id.ll_main);
				ll_main.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Log.d("ftbt", "onclick_bbs");
						ftbt_tab activity = (ftbt_tab)getContext();
						activity.transSetting(item);
					}
				});			
			}		
			
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}

		return view;
	}

}
