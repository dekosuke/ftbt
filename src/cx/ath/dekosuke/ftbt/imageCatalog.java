package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import java.io.File;


//画像カタログ
//指定された画像の登録および、隣の画像への移動
//画像の円リストは別のデータ構造で。
public class imageCatalog extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        Log.d( "ftbt", "imageCatalog.onCreate start" ); 
        Intent intent = getIntent();
        Log.d( "ftbt", "hoge" );
        ArrayList<String> statuses = (ArrayList<String>) intent.getSerializableExtra("imgURLs");        

        Log.d( "ftbt", "hoge1" );
        String myImageURL = (String) intent.getSerializableExtra("myImgURL");
        Log.d( "ftbt", "hoge2" );
 
        //ここでIntentによる追加情報からCircleListを構築する

        //これスタティックにするのはどうかという感じがする
        CircleList.add(myImageURL);
        CircleList.move(1);

        setContentView(new imageCatalogView(this));
    } 
}

/*
public class imageCatalogView extends View {
    
    public imageCatalogView(Context context) {
        super(context);
    }
    
    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        Paint p = new Paint();
        //Bitmap img0, img1;
        
        Bitmap bmp = ImageCache.getImage(urls[0]);
        Resources res = this.getContext().getResources();
        //img0 = BitmapFactory.decodeResource(res, R.drawable.back);
        //img1 = BitmapFactory.decodeResource(res, R.drawable.image);
        
        c.drawBitmap(bmp, 0, 0, p);
        //c.drawBitmap(img1,0,0,p);
    }

}
*/

class imageCatalogView extends SurfaceView implements SurfaceHolder.Callback {

    public imageCatalogView(Context context) {
        super(context);
        
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged (SurfaceHolder holder, int format, int width, int height) {
        // SurfaceViewが変化（画面の大きさ，ピクセルフォーマット）した時のイベントの処理を記述
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // SurfaceViewが作成された時の処理（初期画面の描画等）を記述
        Canvas canvas = holder.lockCanvas();

        // この間にグラフィック描画のコードを記述する。

        Paint p = new Paint();
        String imgFile = CircleList.get();
        Log.d( "ftbt", "imgFile="+imgFile );
        Bitmap bmp = ImageCache.getImage(imgFile);
        canvas.drawBitmap(bmp, 0, 0, p);
 

        // この間にグラフィック描画のコードを記述する。

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // SurfaceViewが廃棄されたる時の処理を記述
    }
}

//円状のリスト。カタログに載っているファイルのリスト。
class CircleList {  
    private static ArrayList<String> list = new ArrayList<String>();  
    private static int pointer=-1; //基本的に-1になるときは0件のときのみ。
  
    public static void add(String str) {
        list.add(str); 
    }

    public static String get(){
        return list.get(pointer);
    }

    public static void set(int i){ pointer=i; }

    public static void move(int i){
        pointer+=i;
        pointer = pointer % list.size();
    }

    public static void moveToZero(){ pointer = 0; }  
    public static void moveToLast(){ pointer = list.size()-1; }  
    
    public static int pos(){
        return pointer;
    }

    public static int size(){
        return list.size();
    }
}
