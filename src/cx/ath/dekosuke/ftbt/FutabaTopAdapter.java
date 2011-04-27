package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;
import android.view.View;
import android.widget.TextView;
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
			FutabaBBS item = (FutabaBBS) items.get(position);
			if (item != null) {
				TextView url = (TextView) view.findViewById(R.id.url);
				url.setText(item.url);
				TextView name = (TextView) view.findViewById(R.id.name);
				name.setText(item.name);
			}

		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}

		return view;
	}

}
