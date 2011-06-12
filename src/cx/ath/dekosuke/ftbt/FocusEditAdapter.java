package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FocusEditAdapter extends ArrayAdapter {
	public ArrayList<String> items;
	private LayoutInflater inflater;
	private Context context;

	public FocusEditAdapter(Context context, int textViewResourceId,
			ArrayList items) {
		super(context, textViewResourceId, items);

		this.items = (ArrayList<String>) items;
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ビューを受け取る
		View view = convertView;
		if (view == null) {
			// 受け取ったビューがnullなら新しくビューを生成
			view = inflater.inflate(R.layout.focuswords_row, null);
		}
		final String item = (String) items.get(position);
		view = inflater.inflate(R.layout.focuswords_row, null);
		TextView word_v = (TextView) view.findViewById(R.id.word);
		word_v.setText(item);

		final FocusEditAdapter adapter = this;
		final int position_f = position;

		// 保存ボタン
		Button delWordButton = (Button) view.findViewById(R.id.delword);
		delWordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					/*
					 * FutabaThread fthread = (FutabaThread) v .getContext();
					 * fthread.saveImage(tag);
					 */
					// Toast.makeText(getContext(),
					// "削除ボタンがクリックされました",Toast.LENGTH_SHORT).show();
					adapter.deleteAt(position_f);
				} catch (Exception e) {
					FLog.d("message", e);
				}
			}
		});

		return view;
	}

	public void deleteAt(int position) {
		this.items.remove(position);
		notifyDataSetChanged();
		try {
			FocusedSettings.set(getContext(), this.items);
		} catch (IOException e) {
			FLog.d("message", e);
		}
	}

	public void addLast(String elem) {
		this.items.add(elem);
		this.notifyDataSetChanged();
		try {
			FocusedSettings.set(getContext(), this.items);
		} catch (IOException e) {
			FLog.d("message", e);
		}
	}
}
