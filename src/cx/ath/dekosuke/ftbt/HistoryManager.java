package cx.ath.dekosuke.ftbt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

//過去スレッド一覧マネージャ
public class HistoryManager {
	private final String OPT_HISTORY = "history";

	// キーがスレッドidのlist
	private TreeMap<String, FutabaThreadContent> threads = new TreeMap<String, FutabaThreadContent>();

	// 初期化
	public void clear() {
		threads = new TreeMap<String, FutabaThreadContent>();
	}

	// スレッドの追加
	public void addThread(FutabaThreadContent thread, int maxHistoryNum) {
		if (!threads.containsKey(thread.threadNum)) {
			threads.put(thread.threadNum, thread);
		} else {
			// すでにある－＞更新
			threads.put(thread.threadNum, thread);
		}
		Log.d("ftbt", "maxHistoryNum=" + maxHistoryNum);
		if (threads.size() > maxHistoryNum) {
			threads.remove(threads.firstKey());
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

	public void set(ArrayList<FutabaThreadContent> threads_array) {
		clear();
		for (int i = 0; i < threads_array.size(); ++i) {
			addThread(threads_array.get(i), 10000);
		}
	}

	// arraylistに変換してかえす
	public ArrayList<FutabaThreadContent> getThreadsArray() {
		ArrayList<FutabaThreadContent> threads_array = new ArrayList<FutabaThreadContent>();
		Iterator it = threads.keySet().iterator();
		while (it.hasNext()) {
			threads_array.add(threads.get(it.next()));
		}
		// 更新日時順ソート
		Collections.sort(threads_array, comparator);
		return threads_array;
	}

	// スレッド最終閲覧時間順ソート
	static Comparator comparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			FutabaThreadContent f1 = (FutabaThreadContent) o1;
			FutabaThreadContent f2 = (FutabaThreadContent) o2;
			return (int) (f2.lastAccessed - f1.lastAccessed);
		}
	};
}
