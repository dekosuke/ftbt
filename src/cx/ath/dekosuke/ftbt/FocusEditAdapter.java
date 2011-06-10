package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FocusEditAdapter extends ArrayAdapter{
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
			final String item = (String) items.get(position);
			view = inflater.inflate(R.layout.focuswords_row, null);
			TextView word_v = (TextView) view.findViewById(R.id.word);
			word_v.setText(item);
		}
		return view;
	}
}
