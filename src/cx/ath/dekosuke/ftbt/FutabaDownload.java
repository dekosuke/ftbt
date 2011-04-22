package cx.ath.dekosuke.ftbt;

import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import android.os.Environment;

//File Saver
public class SDCard {  
      
    public static void saveBin(String name, byte[] bytes) { 
        String sdcard_dir = Environment.
            getExternalStorageDirectory().getPath(); 
        String filename = sdcard_dif + "/" + name;
        File file = new File(filePath);
        file.getParentFile().mkdir();
        try {
             BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
            fos.write(bytes);
        } catch (Exception e) {
            Log.d( 'ftbt', "failed to write file"+name );
        }
        //Environment.getDataDirectory().getPath(); // /dataなど
        //Environment.getDownloadCacheDirectory().getPath(); // cacheなど
    }

    /**
     * 画像オブジェクトを指定した画像フォーマットのバイナリ表現に変換します。
     * @param image 画像オブジェクト
     * @param imageFormat 画像フォーマット
     * @return バイナリ表現
     */
    public static byte[] getImageBytes(BufferedImage image,
                                 String imageFormat)  throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream os = new BufferedOutputStream(bos);
        image.flush();
        ImageIO.write(image, imageFormat, os);
        os.flush();
        os.close();
        return bos.toByteArray();
    }

    public static void saveImage(String name, BufferdImage image, String imageFormat){
        byte[] bytes = getImageBytes(image, imageFormat);
        saveBin(name, bytes);
    }

    
} 
