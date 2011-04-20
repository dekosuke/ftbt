package cx.ath.dekosuke.ftbt;

import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;

public class ImageCache {  
    private static HashMap<String,Bitmap> cache = new HashMap<String,Bitmap>();  
      
    public static Bitmap getImage(String key) {  
        if (cache.containsKey(key)) {  
            Log.d("cache", "cache hit!");  
            return cache.get(key);  
        }  
        return null;  
    }  
      
    public static void setImage(String key, Bitmap image) {  
        cache.put(key, image);  
    }  
} 
