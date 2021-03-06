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

public class FutabaBBSMenuAdapter extends ArrayAdapter {

	public ArrayList items;
	private LayoutInflater inflater;

	public FutabaBBSMenuAdapter(Context context, int textViewResourceId,
			ArrayList items) {
		super(context, textViewResourceId, items);
		try {
			this.items = items;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		} catch (Exception e) {
			FLog.d("message", e);
		}
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
			final FutabaBBSContent item = (FutabaBBSContent) items
					.get(position);
			if (item != null) {
				TextView url = (TextView) view.findViewById(R.id.url);
				url.setTextSize(StateMan.getBBSDescFontSize(getContext()));
				url.setText(item.url);
				TextView name = (TextView) view.findViewById(R.id.name);
				name.setTextSize(StateMan.getBBSFontSize(getContext()));
				name.setText(item.name);
				// ここでスレURL表示を消してる!!!
				// url.setVisibility(View.GONE);
				final Button buttonFavorite = (Button) view
						.findViewById(R.id.favorite_btn);
				final FutabaBBSMenu activity = (FutabaBBSMenu) getContext();
				// Log.d("ftbt", "pos=" + position + " faved=" + item.faved);
				if (true) {
					if (!item.faved) {
						// buttonFavorite.setText("追加");
						buttonFavorite.setBackgroundDrawable(getContext()
								.getResources().getDrawable(
										R.drawable.star_big_off));
					} else {
						buttonFavorite.setBackgroundDrawable(getContext()
								.getResources().getDrawable(
										R.drawable.star_big_on));
						// buttonFavorite.setText("削除");
					}
				} else {
					buttonFavorite.setVisibility(View.GONE);
				}

				final ViewGroup view_parent = parent;
				buttonFavorite.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (activity.mode.equals("all")) {
							if (!item.faved) {
								ftbt ftbt_top = (ftbt) activity.getParent();
								// ftbt_top.favoriteBBSs.add(item);
								item.faved = true;
								ftbt_top.addFavoriteBBSs(item);
								FutabaBBSMenu activity = (FutabaBBSMenu) getContext();
								activity.adapter.notifyDataSetChanged();
								Toast.makeText(activity,
										item.name + "をお気に入りに追加しました",
										Toast.LENGTH_SHORT).show();
							} else {
								ftbt ftbt_top = (ftbt) activity.getParent();
								item.faved = false;
								ftbt_top.removeFavoriteBBSs(item);
								Toast.makeText(activity,
										item.name + "をお気に入りから削除しました",
										Toast.LENGTH_SHORT).show();
							}
						} else {

							ftbt ftbt_top = (ftbt) activity.getParent();
							item.faved = false;
							ftbt_top.removeFavoriteBBSs(item);
							Toast.makeText(activity,
									item.name + "をお気に入りから削除しました",
									Toast.LENGTH_SHORT).show();

						}
						view_parent.invalidate();
						notifyDataSetChanged();
					}
				});

				// 非ボタン部分
				LinearLayout ll_main = (LinearLayout) view
						.findViewById(R.id.ll_main);
				ll_main.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						FLog.d("onclick_bbs");
						FutabaBBSMenu activity = (FutabaBBSMenu) getContext();
						activity.transSetting(item);
					}
				});
			}

		} catch (Exception e) {
			FLog.d("message", e);
		}

		return view;
	}
}
