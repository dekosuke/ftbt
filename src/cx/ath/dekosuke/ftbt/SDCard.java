package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.StatFs;

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

import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.BitmapFactory;

//SDカードといいつつSDカードへの保存に加えてHTTPアクセスも扱っているクラス
public class SDCard {

	public static String getCacheDir() {
		String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
		// String sdcard_dir = Environment.getDataDirectory().getPath();
		// FLog.d("dir="+sdcard_dir);
		String cacheDir = sdcard_dir + "/.ftbtcache/";
		File file = new File(cacheDir);
		file.mkdir(); // ディレクトリないときにつくる
		return cacheDir;
	}

	public static String getSaveDir() {
		String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
		String saveDir = sdcard_dir + "/ふたばと/";
		File file = new File(saveDir);
		file.mkdir(); // ディレクトリないときにつくる
		return saveDir;
	}

	// 保存ディレクトリ内にサブディレクトリを生成する
	public static String getThreadDir(String threadName) {
		String saveDir = getSaveDir();
		String threadDir = saveDir + threadName + "/";
		File file = new File(threadDir);
		file.mkdir(); // ディレクトリないときにつくる
		return threadDir;

	}

	public static String getSeriarizedDir() {
		String cacheDir = getCacheDir();
		String seriarizedDir = cacheDir + "bin/";
		File file = new File(seriarizedDir);
		file.mkdir(); // ディレクトリないときにつくる
		return seriarizedDir;

	}

	public static void saveBin(String name, byte[] bytes, boolean isCache) {
		String filename;
		if (!isCache) {
			filename = getSaveDir() + name;
		} else {
			filename = getCacheDir() + name;
		}
		FLog.d("length=" + bytes.length);
		File file = new File(filename);
		FLog.d(filename);
		file.getParentFile().mkdir();
		try {
			BufferedOutputStream fos = new BufferedOutputStream(
					new FileOutputStream(file));
			fos.write(bytes);
		} catch (Exception e) {
			FLog.d("failed to write file" + name);
		}

		// Environment.getDataDirectory().getPath(); // /dataなど
		// Environment.getDownloadCacheDirectory().getPath(); // cacheなど
	}

	public static void saveFromURL(String name, URL url, boolean isCache)
			throws IOException {
		try {
			String filename;
			if (!isCache) {
				filename = getSaveDir() + name;
			} else {
				filename = getCacheDir() + name;
			}

			// InputStream is = url.openStream();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			if (!String.valueOf(conn.getResponseCode()).startsWith("2")) {
				throw new IOException("Incorrect response code "
						+ conn.getResponseCode());
			}

			FLog.d("HTTP Response code=" + conn.getResponseCode());
			InputStream is = conn.getInputStream();

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
		} catch (IOException e) { // 2XX代以外のレスポンスコードとか
			throw new IOException(e.toString());
		} catch (Exception e) {
			FLog.d("failed to write file" + name);
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

	public static File copyCacheToFile(String urlhash, String url)
			throws IOException {
		String srcfilename = getCacheDir() + urlhash;
		String dstfilename = getSaveDir() + url;
		// ファイルコピーのフェーズ
		InputStream input = null;
		OutputStream output = null;
		File dstFile = new File(dstfilename);
		input = new FileInputStream(new File(srcfilename));
		output = new FileOutputStream(dstFile);

		int DEFAULT_BUFFER_SIZE = 1024 * 4;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		input.close();
		output.close();
		return dstFile;
	}
	
	public static boolean savedImageToThreadExist(String fileName, String threadName){
		String dstfilename = getThreadDir(threadName) + fileName;
		File file = new File(dstfilename);
		return file.exists();
	}

	public static File copyCacheToThreadFile(String urlhash, String url, String threadName)
			throws IOException {
		String srcfilename = getCacheDir() + urlhash;
		String dstfilename = getThreadDir(threadName) + url;
		// ファイルコピーのフェーズ
		InputStream input = null;
		OutputStream output = null;
		File dstFile = new File(dstfilename);
		input = new FileInputStream(new File(srcfilename));
		output = new FileOutputStream(dstFile);

		int DEFAULT_BUFFER_SIZE = 1024 * 4;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		input.close();
		output.close();
		return dstFile;
	}

	// ファイル古いものが先にくるようにソート
	// HTMLファイルは消えにくいように時間を一日伸ばしている
	static Comparator comparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			final long additional_days = 3 * 24 * 3600 * 1000;
			File f1 = (File) o1;
			File f2 = (File) o2;
			long f1_lastmodified = f1.lastModified();
			// Log.d("ftbt",
			// f1.toString()+" is "+FutabaCrypt.isHTMLName(f1.toString()));
			if (FutabaCrypt.isHTMLName(f1.toString())) {
				// Log.d("ftbt", f1.toString()+" is HTML");
				f1_lastmodified += additional_days;
			}
			long f2_lastmodified = f2.lastModified();
			if (FutabaCrypt.isHTMLName(f2.toString())) {
				f2_lastmodified += additional_days;
			}

			return (int) (f2_lastmodified - f1_lastmodified);
		}
	};

	// numMBになるまでキャッシュフォルダのファイルを（古い順に）削除
	// http://osima.jp/blog/howto_java_lastmodified/ 古い順にファイルソート
	//
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
			// Log.d("ftbt", f.toString()+" lastmodified="+f.lastModified());
			// FLog.d(f.getName() + "," + toCalendarString(f));
			if (f.isDirectory()) { // 強制ディレクトリ削除
				// deleteDir(f);
				// FLog.d("deleted directory "+f.getName());
			} else {
				// FLog.d("size="+f.length());
				sizeSum += f.length();
				if (sizeSum > num * 1000000) { // 強制ファイル削除
					f.delete();
					FLog.d("deleted file " + f.getName());
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

	// Galaxy S以外だと使えるらしいSDカードマウントチェック
	// http://sakaneya.blogspot.com/2011/02/galaxy-ssd.html
	public static boolean isMountedExSD() {
		return Environment.MEDIA_MOUNTED.equals(Environment.MEDIA_MOUNTED);
	}

}
