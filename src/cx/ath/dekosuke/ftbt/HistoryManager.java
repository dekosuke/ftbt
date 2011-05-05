package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

//過去スレッド一覧マネージャ
public class HistoryManager {
	private final String OPT_HISTORY = "history";

	// キーがスレッドidのlist
	private TreeMap<String, FutabaThreadContent> threads = new TreeMap<String, FutabaThreadContent>();

	// スレッドの追加
	public void addThread(FutabaThreadContent thread) {
		if (!threads.containsKey(thread.threadNum)) {
			threads.put(thread.threadNum, thread);
		} else {
			// すでにある－＞更新
			threads.put(thread.threadNum, thread);
		}
	}

	// スレッドの削除
	public void removeThread(FutabaThreadContent thread) {
		if (threads.containsKey(thread.threadNum)) {
			threads.remove(thread.threadNum);
		} else {
			// すでにない
			// do nothing
		}
	}

	// 永続化
	public boolean Save() {
		try {
			SDCard.setSerialized(OPT_HISTORY, threads);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void Load() {
		try {
			if (SDCard.existSeriarized(OPT_HISTORY)) {
				threads = (TreeMap<String, FutabaThreadContent>) SDCard
						.getSerialized(OPT_HISTORY).readObject();
			}
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	// arraylistに変換してかえす
	public ArrayList<FutabaThreadContent> getThreadsArray() {
		ArrayList<FutabaThreadContent> threads_array = new ArrayList<FutabaThreadContent>();
		Iterator it = threads.keySet().iterator();
		while (it.hasNext()) {
			threads_array.add( threads.get( it.next() ));
		}
		return threads_array;
	}
}
