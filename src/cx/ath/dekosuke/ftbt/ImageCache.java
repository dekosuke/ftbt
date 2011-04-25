package cx.ath.dekosuke.ftbt;

import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import java.util.Iterator;

import java.io.File;
import java.net.URL;

public class ImageCache {  
    private static HashMap<String,Bitmap> cache = new HashMap<String,Bitmap>();  
    private final static int SIZE_SUM_MAX = 1000*1000*10;
    private static int sizeSum=0;
      
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
            if(sizeSum > SIZE_SUM_MAX){
                Log.d( "ftbt", "delete some cache" );
                GC();
            }
            cache.put(key, image);
            File file = new File(key);
            //SDCard.saveFromURL(file.getName(), new URL(key));
            sizeSum+=image.getWidth()*image.getHeight();
        }catch(Exception e){
            Log.i("ftbt", "failure in image cache set", e);
        }
    }

    public static void GC(){
        int num = cache.size()/2;
        Log.d( "ftbt", "gc num="+num );
        while(num>0){
            Iterator it = cache.keySet().iterator();
            String key = (String)it.next();
            cache.remove(key);
            Log.d( "ftbt", "removed cache of "+key );
            num--;
        }
        sizeSum/=2; 
    }
} 
