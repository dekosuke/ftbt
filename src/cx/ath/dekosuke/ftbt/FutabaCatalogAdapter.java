package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.util.Log;
import android.os.AsyncTask;

import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

public class FutabaCatalogAdapter extends ArrayAdapter {  
  
    private ArrayList items;  
    private LayoutInflater inflater;
    private Context context;

    //画面サイズ
    private int width;
    private int height;
  
    public FutabaCatalogAdapter(Context context, int textViewResourceId,  
                         ArrayList items) {  
        super(context, textViewResourceId, items);  
        this.items = items;
        this.context = context;
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

        if (view == null) {  
            // 受け取ったビューがnullなら新しくビューを生成  
            view = inflater.inflate(R.layout.futaba_catalog_row, null);  
            // 背景画像をセットする  
            //view.setBackgroundResource(R.drawable.back);
            
            view.setOnClickListener( new View.OnClickListener() {    
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    Activity activity = (Activity)getContext();
                    //TODO:IntentでActivityにデータを渡す
                    intent.setClassName(activity.getPackageName(), 
                        activity.getClass().getPackage().getName()+".fthread");
                    activity.startActivity(intent); //Never called!
                }}
            );
       }

        // 表示すべきデータの取得  
        FutabaThread item = (FutabaThread)items.get(position);  
        if (item != null) {  
            // スクリーンネームをビューにセット  
            TextView text = (TextView)view.findViewById(R.id.bottomtext);  
            Log.d( "ftbt", "aaa" );
            
            //画像をセット
            try{
                if(item.getImgURL() != null){
                    ImageView iv = (ImageView)view.findViewById(R.id.image);
                    ImageGetTask task = new ImageGetTask(iv);
                    task.execute(item.getImgURL());
                    Log.d( "ftbt", "image "+item.getImgURL()+"set" );
                }else{
                    //Bitmap bm = null;
                    //ImageView iv = (ImageView)view.findViewById(R.id.image);
                    //iv.setImageBitmap(bm); 
                }
            } catch (Exception e) {
                Log.d( "ftbt", e.toString() );
            }

            // テキストをビューにセット  
            if (text != null) {  
                text.setText(item.getText());  
            }
        } 
        return view;  
    }

    //画像取得用スレッド
    class ImageGetTask extends AsyncTask<String,Void,Bitmap> {
        private ImageView image;
        private String tag;
        
        public ImageGetTask(ImageView _image) {
            image = _image;
            //tag = image.getTag().toString();
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bm = ImageCache.getImage(urls[0]);
            if (bm == null){ //does not exist on cache
                try{
                    URL imgURL = new URL(urls[0]);
                    InputStream is = imgURL.openStream();
                    bm = BitmapFactory.decodeStream(is);
                    float s_x = Math.max(1.0f, 
                        (float) bm.getWidth()  / (float)Math.min(width, height) );
                    float s_y = Math.max(1.0f,
                        (float) bm.getHeight() / (float)Math.min(width, height) );
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
            // Tagが同じものが確認して、同じであれば画像を設定する
            //if (tag.equals(image.getTag())) {
                image.setImageBitmap(result);
            //}
        }
    }
}  
