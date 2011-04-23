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

import android.view.MotionEvent;

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
        ArrayList<String> imgURLs = (ArrayList<String>) intent.getSerializableExtra("imgURLs");        

        Log.d( "ftbt", "hoge1" );
        String myImageURL = (String) intent.getSerializableExtra("myImgURL");
        Log.d( "ftbt", "hoge2" );
 
        //ここでIntentによる追加情報からCircleListを構築する

        //これスタティックにするのはどうかという感じがする
        for(int i=0;i<imgURLs.size();i++){
            String imgURL = imgURLs.get(i);
            CircleList.add(imgURL);
            if(imgURL.equals(myImageURL)){ CircleList.moveToLast(); }
        }
        //CircleList.add(myImageURL);
        //CircleList.move(1);

        imageCatalogView view = new imageCatalogView(this);
        view.setOnTouchListener(new FlickTouchListener());
        setContentView(view);
    } 

    private float lastTouchX;
    private float currentX;
    private class FlickTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                break;

            case MotionEvent.ACTION_UP:
                currentX = event.getX();
                if (lastTouchX < currentX) {
                    //前に戻る動作
                    Log.d("ftbt", "motion prev");
                    CircleList.move(-1);
                    ((imageCatalogView)v).doDraw();
                    //v.doDraw();
                }
                if (lastTouchX > currentX) {
                    //次に移動する動作
                    CircleList.move(1);
                    Log.d("ftbt", "motion next");
                    ((imageCatalogView)v).doDraw();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                currentX = event.getX();
                if (lastTouchX < currentX) {
                    //前に戻る動作
                }
                if (lastTouchX > currentX) {
                     //次に移動する動作
                }
                break;
            }
            return true;
        }
    }

}

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
        doDraw();
    }

    public void doDraw() {
        SurfaceHolder holder = getHolder();
        // SurfaceViewが作成された時の処理（初期画面の描画等）を記述
        Canvas canvas = holder.lockCanvas();

        // この間にグラフィック描画のコードを記述する。

        canvas.drawColor(0,PorterDuff.Mode.CLEAR ); 
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
        if(pointer==-1){ pointer=0; }
    }

    public static String get(){
        return list.get(pointer);
    }

    public static void set(int i){ pointer=i; }

    public static void move(int i){
        pointer+=i;
        pointer = (pointer+list.size()) % list.size();
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
