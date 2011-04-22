package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.graphics.*;

//画像カタログ
//指定された画像の登録および、隣の画像への移動
//画像の円リストは別のデータ構造で。
public class imageCatalog extend Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String threadURL = (String) intent.getSerializableExtra("threadNum");
        Log.d( "ftbt", "threadURL:"+threadURL );
 
    } 
}

public class imageCatalogView extends View {
    
    public imageCatalogView(Context context) {
        super(context);
    }
    
    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        Paint p = new Paint();
        Bitmap img0, img1;
        
        /*
        Resources res = this.getContext().getResources();
        img0 = BitmapFactory.decodeResource(res, R.drawable.back);
        img1 = BitmapFactory.decodeResource(res, R.drawable.image);
        
        c.drawBitmap(img0,0,0,p);
        c.drawBitmap(img1,0,0,p);
        */
    }

}

//円状のリスト。カタログに載っているファイルのリスト。
public class CircleList {  
    private static ArrayList<String> list = new ArrayList<String>();  
    private static int pointer=-1; //基本的に-1になるときは0件のときのみ。
  
    public static void add(String str) {
         
    }

    public static void get(){
    }

    public static void move(int i){
    }  
}
