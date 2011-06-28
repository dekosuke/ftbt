package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.text.Html;
import android.util.Log;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.content.Intent;

import java.io.InputStream;
import java.net.URL;

//BufferedStreamのエラー問題対応
import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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

	// 検索クエリ(ハイライトする)
	public String[] searchQueries;

	// しおり位置
	public int shioriPosition = 0;

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

			// 表示すべきデータの取得
			FutabaStatus item = (FutabaStatus) items.get(position);
			// FLog.d("potision=" + position + " datestr=" + item.datestr);
			if (item != null) {
				TextView title = (TextView) view.findViewById(R.id.title);
				title.setTextSize(StateMan.getDescFontSize(getContext()));
				TextView bottomtext = (TextView) view
						.findViewById(R.id.bottomtext);
				bottomtext.setTextSize(StateMan.getDescFontSize(getContext()));
				TextView text = (TextView) view.findViewById(R.id.maintext);
				bottomtext.setTextSize(StateMan.getMainFontSize(getContext()));
				if (FutabaStatus.isBlank(item)) {
					// 区切り線
					ImageView iv = (ImageView) view.findViewById(R.id.image);
					iv.setImageBitmap(null);
					text.setText("----ここから新着----");
					text.setGravity(Gravity.CENTER);
					title.setVisibility(View.GONE);
					bottomtext.setVisibility(View.GONE);
					bottomtext.setText("");
					view.setBackgroundColor(Color.parseColor("#CCCCFF"));
					Button saveButton = (Button) view
							.findViewById(R.id.savebutton);
					saveButton.setVisibility(View.GONE);
				} else if (FutabaStatus.isEndTime(item)) {
					// 区切り線
					ImageView iv = (ImageView) view.findViewById(R.id.image);
					iv.setImageBitmap(null);
					String text_html = item.text;
					CharSequence cs = Html.fromHtml(text_html); // HTML表示
					text.setText(cs);
					text.setGravity(Gravity.CENTER);
					title.setVisibility(View.GONE);
					bottomtext.setVisibility(View.GONE);
					bottomtext.setText("");
					view.setBackgroundColor(Color.parseColor("#FFFFEE"));
					Button saveButton = (Button) view
							.findViewById(R.id.savebutton);
					saveButton.setVisibility(View.GONE);

				} else {
					String title_base = item.title;// StringUtil.safeCut(, 30);
					if (item.name != null) { // こういう風に足さないと改行時に消えてしまうのでやむなく
						title_base += " <font color=\"#117743\">" + item.name
								+ "</font>";
						if (item.mailTo != null && !item.mailTo.equals("")) {
							title_base += " <font color=\"#0000CC\">"
									+ item.mailTo + "</font>";
						}

						if (item.imgURL != null) {
							File imgFile = new File(item.bigImgURL);
							title_base += "<font color=\"#0000CC\">"
									+ imgFile.getName() + "</font>";
						}

						if (activity.currentSize != 0
								&& position >= activity.prevSize) { // 新着
							title_base += " New!";
							if (position != 0) { // レス番号
								title_base = "<font color=\"#800000\">["
										+ (position - 1) + "]</font> "
										+ title_base;
							}
						} else {
							if (position != 0) { // レス番号
								title_base = "<font color=\"#800000\">["
										+ position + "]</font> " + title_base;
							}
						}
						// name.setText(item.name);// item.getImgURL());
					} else {
						if (position != 0) { // レス番号
							title_base = "<font color=\"#800000\">[" + position
									+ "</font>] " + title_base;
						}
						if (item.mailTo != null && !item.mailTo.equals("")) {
							title_base += " <font color=\"#0000CC\">"
									+ item.mailTo + "</font>";
						}

					}

					CharSequence cs_title = Html.fromHtml(title_base); // HTML表示
					title.setText(cs_title);// item.getImgURL());
					// TextView name = (TextView) view.findViewById(R.id.name);

					// スクリーンネームをビューにセット
					text.setTextSize(StateMan.getMainFontSize(getContext()));
					bottomtext.setTextSize(StateMan
							.getDescFontSize(getContext()));
					if (item.datestr != null) {
						bottomtext.setText(item.datestr + " No." + item.id);
					}

					if (position == 0) { // 最初だけ色違う
						view.setBackgroundColor(Color.rgb(255, 255, 238));
					} else {
						// ここのルーチンがないとおかしくなるので,view再利用の様子が良く分かる
						if (item.id != 0 && item.id == shioriPosition) { // しおり位置
							setShioriStatus(view);
						} else {
							view.setBackgroundColor(Color.rgb(240, 224, 214));
						}
					}

					// ここらへんは区切り線で変えた可能性のあるデータを元に戻す
					text.setGravity(Gravity.LEFT);
					title.setVisibility(View.VISIBLE);
					bottomtext.setVisibility(View.VISIBLE);

					Bitmap bm = null;
					LinearLayout imageframe = (LinearLayout) view
							.findViewById(R.id.imageframe);
					// imageframe.setVisibility(View.GONE);
					Button saveButton = (Button) view
							.findViewById(R.id.savebutton);
					ImageView iv = (ImageView) view.findViewById(R.id.image);
					iv.setImageBitmap(bm);

					// 画像をセット
					try {
						if (item.imgURL != null) {
							iv.setTag(item.bigImgURL);
							bm = Bitmap.createBitmap(item.width, item.height,
									Bitmap.Config.ALPHA_8);
							// imageframe.setLayoutParams(createParam(item.width,
							// LayoutParams.FILL_PARENT));
							iv.setImageBitmap(bm);
							ImageGetTask task = new ImageGetTask(view);
							SharedPreferences preferences = PreferenceManager
									.getDefaultSharedPreferences(getContext());
							boolean enableSaveButton = preferences.getBoolean(
									getContext().getString(
											R.string.enablesavebutton), true);
							if (enableSaveButton) {
								saveButton.setVisibility(View.VISIBLE);
							} else {
								saveButton.setVisibility(View.GONE);
							}
							view.setLongClickable(true);
							task.execute(item.imgURL);
							// title.setText("(画像あり)");
						} else { // 画像なし
							saveButton.setVisibility(View.GONE);
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
						String text_html = item.text;
						/*
						 * if(!item.endTime.equals("")){
						 * text_html+="<br><font color=\"red\">("
						 * +item.endTime+")</font>"; }
						 */
						text_html = addHighlight(text_html); // 検索ワードのハイライトを行う
						CharSequence cs = Html.fromHtml(text_html); // HTML表示
						text.setText(cs);
					}
				}
			}

		} catch (Exception e) {
			FLog.d("message", e);
		}

		return view;
	}

	private LinearLayout.LayoutParams createParam(int width, int height) {
		return new LinearLayout.LayoutParams(width, height);
	}

	void setShioriStatus(View view) {
		view.setBackgroundColor(Color.parseColor("#BBFFBB"));
		TextView bottomText = (TextView) view.findViewById(R.id.bottomtext);
		String bottomTextStr = "[栞]" + bottomText.getText().toString();
		bottomText.setText(bottomTextStr);
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
		// private LinearLayout imageFrame;
		private Button saveButton;
		private String tag;

		public ImageGetTask(View view) {
			image = (ImageView) view.findViewById(R.id.image);
			saveButton = (Button) view.findViewById(R.id.savebutton);
			// imageFrame = (LinearLayout) view.findViewById(R.id.imageframe);
			if (image == null) {
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
				if (image != null && tag.equals(image.getTag())) {
					if (result == null) { // 画像読み込み失敗時
						TextView screenName = (TextView) image
								.findViewById(R.id.title);
						if (screenName != null) {
							screenName.setText("(画像読み込み失敗)");// item.getImgURL());
						}
						return;
					}

					// imageFrame.setVisibility(View.VISIBLE);
					image.setImageBitmap(result);
					// imageFrame.setMinimumWidth(image.getWidth());
					// imageFrame.invalidate();
					// imageFrame.notify();

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
								intent.putExtra("thumbURLs",
										activity.getThumbURLs());
								intent.putExtra("myImgURL", tag);
								intent.setClassName(activity.getPackageName(),
										activity.getClass().getPackage()
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
					// 保存ボタン
					saveButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							try {
								FutabaThread fthread = (FutabaThread) v
										.getContext();
								fthread.saveImage(tag);
							} catch (Exception e) {
								FLog.d("message", e);
							}
						}
					});

				}

			} catch (Exception e) {
				FLog.d("message", e);
			}
		}

	}

	public String addHighlight(String title) {
		if (searchQueries != null) {
			for (int i = 0; i < searchQueries.length; ++i) {
				String searchQuery = searchQueries[i];
				title = title.replaceAll(searchQuery, "<font color=\"red\">"+searchQuery+"</font>");
				FLog.d("searchrep"+searchQuery);
				FLog.d("replaced"+title);
			}
		}
		return title;
	}
}
