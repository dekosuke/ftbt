package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import android.os.Environment;

import java.io.File;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.net.URL;

import android.graphics.BitmapFactory;

//File Saver
public class SDCard {
	
	public static String getCacheDir(){
		String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
		String cacheDir = sdcard_dir + "/.ftbtcache/";
		File file = new File(cacheDir);
		file.mkdir(); //ディレクトリないときにつくる
		return cacheDir;
	}

	public static String getSaveDir(){
		String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
		String saveDir = sdcard_dir + "/ふたばと/";
		File file = new File(saveDir);
		file.mkdir(); //ディレクトリないときにつくる
		return saveDir;
	}

	
	public static String getSeriarizedDir(){
		String cacheDir = getCacheDir();
		String seriarizedDir = cacheDir + "bin/";
		File file = new File(seriarizedDir);
		file.mkdir(); //ディレクトリないときにつくる
		return seriarizedDir;
		
	}
	
	
	public static void saveBin(String name, byte[] bytes, boolean isCache) {
		String filename;
		if (!isCache) {
			filename = getSaveDir() + name;
		} else {
			filename = getCacheDir() + name;
		}
		Log.d("ftbt", "length=" + bytes.length);
		File file = new File(filename);
		Log.d("ftbt", filename);
		file.getParentFile().mkdir();
		try {
			BufferedOutputStream fos = new BufferedOutputStream(
					new FileOutputStream(file));
			fos.write(bytes);
		} catch (Exception e) {
			Log.d("ftbt", "failed to write file" + name);
		}
		// Environment.getDataDirectory().getPath(); // /dataなど
		// Environment.getDownloadCacheDirectory().getPath(); // cacheなど
	}

	public static void saveFromURL(String name, URL url, boolean isCache) {
		try {
			String filename;
			if (!isCache) {
				filename = getSaveDir() + name;
			} else {
				filename = getCacheDir() + name;
			}

			InputStream is = url.openStream();
			// OutputStream os = new FileOutputStream(filename);
			File file = new File(filename);
			file.getParentFile().mkdir();
			OutputStream fos = new FileOutputStream(filename);

			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = is.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			fos.flush();
			is.close();
			fos.close();
		} catch (Exception e) {
			Log.d("ftbt", "failed to write file" + name);
		}
	}

	public static String loadTextCache(String name) throws IOException {
		String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
		String filename = getCacheDir() + name;
		File file = new File(filename);
		return FileToString.fileToString(file, "Shift-JIS");
	}

	public static Bitmap loadBitmapCache(String name) {
		String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
		String filename = getCacheDir() + name;
		File file = new File(filename);
		// 読み込み用のオプションオブジェクトを生成
		// BitmapFactory.Options options = new BitmapFactory.Options();
		return BitmapFactory.decodeFile(filename);
	}

	public static boolean cacheExist(String name) {
		String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
		String filename = getCacheDir() + name;
		File file = new File(filename);
		return file.exists();
	}

	public static void copyCacheToFile(String urlhash, String url) throws IOException {
		String srcfilename = getCacheDir() + urlhash;
		String dstfilename = getSaveDir() + url;
		// ファイルコピーのフェーズ
		InputStream input = null;
		OutputStream output = null;
		input = new FileInputStream(new File(srcfilename));
		output = new FileOutputStream(new File(dstfilename));

		int DEFAULT_BUFFER_SIZE = 1024 * 4;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		input.close();
		output.close();

	}

	// ファイル新しい順ソート
	static Comparator comparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			File f1 = (File) o1;
			File f2 = (File) o2;

			return (int) (f2.lastModified() - f1.lastModified());
		}
	};

	// numMBになるまでキャッシュフォルダのファイルを（古い順に）削除
	// http://osima.jp/blog/howto_java_lastmodified/ 古い順にファイルソート
	// まだ未検証
	public static void limitCache(int num) {
		File cache_dir = new File(getCacheDir());
		File[] files = cache_dir.listFiles();
		ArrayList list = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			list.add(files[i]);
		}

		Collections.sort(list, comparator);

		// 順番に新しいファイルから加える─＞既定サイズになったときにそれ以降のファイルをすべて削除
		// ディレクトリはすべて削除　で。
		int sizeSum = 0;
		for (int i = 0; i < list.size(); i++) {
			File f = (File) list.get(i);
			// Log.d("ftbt", f.getName() + "," + toCalendarString(f));
			if (f.isDirectory()) { // 強制ディレクトリ削除
				// deleteDir(f);
				// Log.d("ftbt", "deleted directory "+f.getName());
			} else {
				// Log.d("ftbt", "size="+f.length());
				sizeSum += f.length();
				if (sizeSum > num * 1000000) { // 強制ファイル削除
					f.delete();
					Log.d("ftbt", "deleted file " + f.getName());
				}
			}
		}
	}

	static private void deleteDir(File f) {
		if (f.exists() == false) {
			return;
		}

		if (f.isFile()) {
			f.delete();
		}

		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteDir(files[i]);
			}
			f.delete();
		}
	}

	static private String toCalendarString(File f) {

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(f.lastModified());

		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		return String.valueOf(y) + "-" + String.valueOf(m + 1) + "-"
				+ String.valueOf(day);
	}

	public static boolean existSeriarized(String name) {
		String filename = getCacheDir() + "bin/" + name;
		File file = new File(filename);
		return file.exists();
	}

	static public ObjectInputStream getSerialized(String name)
			throws IOException {
		String filename = getSeriarizedDir() + name;
		File file = new File(filename);
		file.getParentFile().mkdir();
		FileInputStream inFile = new FileInputStream(filename);
		ObjectInputStream inObject = new ObjectInputStream(inFile);
		return inObject;
	}

	static public void setSerialized(String name, Object object)
			throws IOException {
		String filename = getSeriarizedDir() + name;
		FileOutputStream outFile = new FileOutputStream(filename);
		File file = new File(filename);
		file.getParentFile().mkdir();
		ObjectOutputStream outObject = new ObjectOutputStream(outFile);
		outObject.writeObject(object);
	}
}
