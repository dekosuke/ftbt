package cx.ath.dekosuke.ftbt;

import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import android.os.Environment;

import java.io.File;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import android.graphics.BitmapFactory;

//File Saver
public class SDCard {  

    public static void saveBin(String name, byte[] bytes, boolean isCache) { 
        String sdcard_dir = Environment.
            getExternalStorageDirectory().getPath(); 
        String filename;
        if(!isCache){ 
            filename = sdcard_dir + "/ふたばと/" + name;
        }else{
            filename = sdcard_dir + "/cx.ath.dekosuke.ftbt/" + name;
        }
        Log.d("ftbt", "length="+bytes.length);
        File file = new File(filename);
        file.getParentFile().mkdir();
        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
            fos.write(bytes);
        } catch (Exception e) {
            Log.d( "ftbt", "failed to write file"+name );
        }
        //Environment.getDataDirectory().getPath(); // /dataなど
        //Environment.getDownloadCacheDirectory().getPath(); // cacheなど
    }

    public static void saveFromURL(String name, URL url, boolean isCache){
       try {
            InputStream is  = url.openStream();
            String sdcard_dir = Environment.
                         getExternalStorageDirectory().getPath();
            String filename;
            if(!isCache){ 
                filename = sdcard_dir + "/ふたばと/" + name;
            }else{
                filename = sdcard_dir + "/cx.ath.dekosuke.ftbt/" + name;
            }
             //OutputStream os = new FileOutputStream(filename);
            File file = new File(filename);
            file.getParentFile().mkdir();
            OutputStream fos = new FileOutputStream(filename);
            
            byte[] buf = new byte[1024];
            int len=0;
            while((len = is.read(buf)) > 0){
               fos.write(buf, 0, len);
            }
            fos.flush();
            is.close();
            fos.close();
        } catch (Exception e) {
            Log.d( "ftbt", "failed to write file"+name );
        }
    }
    
    public static String loadTextCache(String name) throws IOException {
        String sdcard_dir = Environment.
                         getExternalStorageDirectory().getPath();
        String filename = sdcard_dir + "/cx.ath.dekosuke.ftbt/" + name;
        File file = new File(filename);
        return FileToString.fileToString(file, "Shift-JIS");
    } 

    public static Bitmap loadBitmapCache(String name){
        String sdcard_dir = Environment.
                         getExternalStorageDirectory().getPath();
        String filename = sdcard_dir + "/cx.ath.dekosuke.ftbt/" + name;
        File file = new File(filename);
        //読み込み用のオプションオブジェクトを生成
        //BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(filename);
    } 

    public static boolean cacheExist(String name){
        String sdcard_dir = Environment.
                         getExternalStorageDirectory().getPath();
        String filename = sdcard_dir + "/cx.ath.dekosuke.ftbt/" + name;
        File file = new File(filename);
        return file.exists();
    }
} 
