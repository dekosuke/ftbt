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
import java.io.InputStream;
import java.net.URL;

import android.view.MotionEvent;
import android.os.AsyncTask;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

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
        CircleList.clear();

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
        String imgFile = CircleList.get();
        setTag(imgFile);
        
        try{
            canvas.drawColor(0,PorterDuff.Mode.CLEAR ); 
            Paint p = new Paint();
            Bitmap bmp = ImageCache.getImage(imgFile);
            Log.d( "ftbt", "imgFile="+imgFile );
            if(bmp == null){ //キャッシュない
                Log.d( "ftbt", "cache miss and image retrieving start" );
                //ImageCache.asyncSetImage(imgFile, imgFile);
                ImageGetTask task = new ImageGetTask(this);
                task.execute(imgFile); 
            }else{
                Log.d( "ftbt", "load cache" );
                canvas.drawBitmap(bmp, 0, 0, p);
            }
        }catch (Exception e){
            //Log.i("ftbt", "message", new Throwable());
            //e.printStackTrace();
            //e.printStackTrace();
            Log.i("ftbt", "message", e);
        }

        // この間にグラフィック描画のコードを記述する。

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // SurfaceViewが廃棄されたる時の処理を記述
    }

    //画像取得用スレッド
    class ImageGetTask extends AsyncTask<String,Void,Bitmap> {
        private imageCatalogView image;
        private String tag;
        
        public ImageGetTask(imageCatalogView _image) {
            image = _image;
            tag = image.getTag().toString();
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Log.d( "ftbt", "getting width/height"+urls[0]);
            Context context = getContext();
            WindowManager wm = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE));
            Display display = wm.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
 
            Log.d( "ftbt", "cache loading start"+urls[0]);
            Bitmap bm = ImageCache.getImage(urls[0]);
            if (bm == null){ //does not exist on cache
                try{
                    URL imgURL = new URL(urls[0]);
                    InputStream is = imgURL.openStream();
                    bm = BitmapFactory.decodeStream(is);
                    float s_x = Math.max(1.0f, 
                        (float) bm.getWidth()  / (float)width );
                    float s_y = Math.max(1.0f,
                        (float) bm.getHeight() / (float)height );
                    float scale = Math.max(s_x, s_y);
                    int new_x = (int)( bm.getWidth()  / scale );
                    int new_y = (int)( bm.getHeight() / scale );
                    bm = Bitmap.createScaledBitmap(bm, new_x, new_y, false);
                    ImageCache.setImage(urls[0], bm);
                } catch (Exception e) {
                    Log.d( "ftbt", e.toString() );
                } 
            }
            return bm;
        }

        //メインスレッドで実行する処理
        @Override
        protected void onPostExecute(Bitmap result) {
            Log.d( "ftbt", "tag="+tag+" image.getTag="+image.getTag().toString() );
            // Tagが同じものが確認して、同じであれば画像を設定する
            if (tag.equals(image.getTag())) {
                //image.setImageBitmap(result);
                image.doDraw(); //再描画
            }
        }
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
    
    public static void clear(){
        list = new ArrayList<String>();
        pointer = -1;
    }
}
