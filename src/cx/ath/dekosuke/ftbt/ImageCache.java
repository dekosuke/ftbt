package cx.ath.dekosuke.ftbt;

import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import java.util.Iterator;

public class ImageCache {  
    private static HashMap<String,Bitmap> cache = new HashMap<String,Bitmap>();  
      
    public static Bitmap getImage(String key) {
        try{
            if (cache.containsKey(key)) {  
                Log.d("ftbt", "cache hit!");  
                Bitmap bmp = cache.get(key);  
                if(bmp == null){
                    Log.d("ftbt", "cache contents null");  
                    return null;
                }
                return bmp;
            }
        }catch(Exception e){   
            Log.i("ftbt", "failure in image cache get", e);
        }  
        return null;  
    }  
      
    public static void setImage(String key, Bitmap image) { 
        try{
            cache.put(key, image);  
        }catch(Exception e){
            Log.i("ftbt", "failure in image cache set", e);
            deleteRandomImage(5);
            try{
                cache.put(key, image);  
            }catch(Exception e2){
                Log.i("ftbt", "failure in image cache set2", e2);
            }
        }
    }

    public static void deleteRandomImage(int num){
        cache = new HashMap<String, Bitmap>();
        if(num>cache.size()){
            cache.clear();
        }else{ //ランダムに消す
            while(num>0){
                Iterator it = cache.keySet().iterator();
                cache.remove(it.next());
                num--;
            } 
        }
    }
} 
