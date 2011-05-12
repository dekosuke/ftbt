package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

//円状のリスト。大きい画像のURLリストを持つのに使われている。グローバルオブジェクト
class CircleList {
	private static ArrayList<String> list = new ArrayList<String>();
	private static int pointer = -1; // 基本的に-1になるときは0件のときのみ。

	public static void add(String str) {
		list.add(str);
		if (pointer == -1) {
			pointer = 0;
		}
	}

	public static String get() {
		return list.get(pointer);
	}

	public static void set(int i) {
		pointer = i;
	}

	public static void move(int i) {
		pointer += i;
		pointer = (pointer + list.size()) % list.size();
	}

	public static void moveToZero() {
		pointer = 0;
	}

	public static void moveToLast() {
		pointer = list.size() - 1;
	}

	public static int pos() {
		return pointer;
	}

	public static int size() {
		return list.size();
	}

	public static void clear() {
		list = new ArrayList<String>();
		pointer = -1;
	}
}