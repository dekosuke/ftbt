package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;
import android.view.View;
import android.widget.TextView;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.util.Log;
import android.os.AsyncTask;
import android.content.Intent;

import java.io.InputStream;
import java.net.URL;

//BufferedStreamのエラー問題対応
import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.OutputStream;
//その２
import java.net.HttpURLConnection;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

import java.lang.Thread; //To call Thread.sleep

public class FutabaAdapter extends ArrayAdapter {  
  
    private ArrayList items;  
    private LayoutInflater inflater;

    //画面サイズ
    private int width;
    private int height;
  
    public FutabaAdapter(Context context, int textViewResourceId,  
                         ArrayList items) {  
        super(context, textViewResourceId, items);  
        this.items = items;  
        this.inflater = (LayoutInflater) context  
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
 
        //画面サイズの取得
        WindowManager wm = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }  
  
    @Override  
    public View getView(int position, View convertView,
                        ViewGroup parent) {  
         // ビューを受け取る  
        View view = convertView;  

        try{
 
        if (view == null) {  
            // 受け取ったビューがnullなら新しくビューを生成  
            view = inflater.inflate(R.layout.futaba_thread_row, null);  
            // 背景画像をセットする  
            //view.setBackgroundResource(R.drawable.back);  
        }

        // 表示すべきデータの取得  
        FutabaStatus item = (FutabaStatus)items.get(position);  
        if (item != null) {  
            TextView screenName = (TextView)view.findViewById(R.id.toptext);  
            screenName.setTypeface(Typeface.DEFAULT_BOLD);  
  
            // スクリーンネームをビューにセット  
            TextView text = (TextView)view.findViewById(R.id.bottomtext);  
            if (screenName != null) {  
                screenName.setText("無題 Name としあき");//item.getImgURL());
            }

            Bitmap bm = null;
            ImageView iv = (ImageView)view.findViewById(R.id.image);
            iv.setImageBitmap(bm); 

            //画像をセット
            try{
                if(item.getImgURL() != null){
                    iv.setTag(item.bigImgURL);
                    bm = Bitmap.createBitmap(item.width, item.height, Bitmap.Config.ALPHA_8);
                    iv.setImageBitmap(bm);  
                    ImageGetTask task = new ImageGetTask(iv);
                    task.execute(item.getImgURL());
                    screenName.setText("(画像あり)");
                }else{ //画像なし
                    /*
                    Log.d("ftbt", "w="+item.width+" h="+item.height );
                    bm = Bitmap.createBitmap(item.width, item.height, Bitmap.Config.ALPHA_8);
                    iv.setImageBitmap(bm); 
                    */
                }
            } catch (Exception e) {
                Log.i("ftbt", "message", e);
            }

            // テキストをビューにセット  
            if (text != null) {  
                text.setText(item.getText());  
            }
        } 

        }catch(Exception e){
            Log.i("ftbt", "message", e);
        }
        
        return view;  
    }

   static int getSmlImageWidth(Bitmap bm, int width, int height){
        float s_x = Math.max(1.0f, 
                   (float) bm.getWidth()*1.5f  / (float)width );
        float s_y = Math.max(1.0f,
                   (float) bm.getHeight()*2.0f / (float)height );
        float scale = Math.max(s_x, s_y);
        return (int)( bm.getWidth()  / scale );
    }
   static int getSmlImageHeight(Bitmap bm, int width, int height){
        float s_x = Math.max(1.0f, 
                   (float) bm.getWidth()*1.5f  / (float)width );
        float s_y = Math.max(1.0f,
                   (float) bm.getHeight()*2.0f / (float)height );
        float scale = Math.max(s_x, s_y);
        return (int)( bm.getHeight() / scale );
    }

    static Object lock = new Object();
    static Object lock_id = new Object();
    static int LastTaskID=-1;
    //画像取得用スレッド
    class ImageGetTask extends AsyncTask<String,Void,Bitmap> {
        private ImageView image;
        private String tag;
        private int id;
        
        public ImageGetTask(ImageView _image) {
            image = _image;
            if( _image == null ){
                Log.d("ftbt", "imageview is null!!!");
            }
            tag = image.getTag().toString();
            synchronized (FutabaAdapter.lock_id){
                FutabaAdapter.LastTaskID+=1;
                id=FutabaAdapter.LastTaskID; 
            }
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bm = ImageCache.getImage(urls[0]);
            HttpURLConnection con=null;
            InputStream is = null;
            Log.d( "ftbt", "futabaAdapter thread start" );
            if (bm == null){ //does not exist on cache
                //synchronized (FutabaAdapter.lock){
                try{
                    ImageCache.setImage(urls[0]);
                    bm = ImageCache.getImage(urls[0]);
                } catch (Exception e) {
                    Log.i( "ftbt", "message", e );
                    Log.d( "ftbt", "fail with "+urls[0] ); 
                    try{
                        Thread.sleep(1 * 1000);
                    } catch (Exception e2){
                        Log.i( "ftbt", "message", e2 );
                    }
                } finally {  
                    try {  
                        if (con != null) {  
                            con.disconnect();  
                        }  
                        if (is != null) {  
                            is.close();  
                        }  
                    } catch (Exception e) {  
                        Log.e( "ftbt", "doInBackground", e);  
                    }           
                //}
                }
            }
            return bm;
        }

        //メインスレッドで実行する処理
        @Override
        protected void onPostExecute(Bitmap result) {
            //Log.d( "ftbt", "tag="+tag+" image.getTag="+image.getTag().toString() );
            try{
            // Tagが同じものが確認して、同じであれば画像を設定する
            if (tag.equals(image.getTag())) {
                if(result == null){ //画像読み込み失敗時
                    TextView screenName = (TextView)image.findViewById(R.id.toptext);  
                    if (screenName != null) {  
                        screenName.setText("(画像読み込み失敗)");//item.getImgURL());
                    }
                    return; 
                }
                image.setImageBitmap(result);
        if(true){ //クリックのリスナー登録 このリスナー登録は、画像をロードしたときにするようにしたい
            image.setOnClickListener( new View.OnClickListener() {   
                public void onClick(View v) {
                    Log.d ( "ftbt", "intent calling thread activity" );
                    Intent intent = new Intent();
                    fthread activity = (fthread)getContext();
                    //Log.d ( "ftbt", threadNum ); 
                    //これスレッドごとに作られているのが結構ひどい気がする
                    intent.putExtra("imgURLs", activity.getImageURLs() );
                    intent.putExtra("myImgURL", tag);
                    intent.setClassName(activity.getPackageName(), 
                        activity.getClass().getPackage().getName()+".imageCatalog");
                    activity.startActivity(intent); //Never called!
                }}
            );
        }

            }

            } catch (Exception e) {
                Log.i( "ftbt", "message", e);
            }
        }
        
        private Bitmap MyDecodeStream(InputStream in){
            final int IO_BUFFER_SIZE = 4*1024;
            Bitmap bitmap = null;
            BufferedOutputStream out = null;
            try {

                in = new BufferedInputStream(in, IO_BUFFER_SIZE);

                final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
                byte[] b = new byte[IO_BUFFER_SIZE];
                int read;
                while ((read = in.read(b)) != -1) {
                    out.write(b, 0, read);
                }
                //               streamCopy(in, out);
                out.flush();

                final byte[] data = dataStream.toByteArray();
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            } catch (Exception e){
                Log.i( "ftbt", "message", e);
            }        
            return bitmap;
        }

        private void streamCopy(InputStream in, OutputStream out) throws IOException {

        }
    }
}  
