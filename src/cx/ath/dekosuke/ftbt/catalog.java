package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View.OnClickListener;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.DefaultedHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.ProgressDialog;
import java.lang.Thread;
import android.os.Handler;
import android.os.Message;

//板カタログ表示アクティビティ
public class catalog extends ListActivity 
        implements OnClickListener, Runnable{

    private ArrayList<FutabaThread> fthreads = null;
    private FutabaCatalogParser parser;
    private FutabaCatalogAdapter adapter = null;
    public String baseUrl = "";
    private String catalogURL;
    private ProgressDialog waitDialog;
    private Thread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setWait();

    }

    public void setWait(){
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage("ネットワーク接続中...");
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //waitDialog.setCancelable(true);
        waitDialog.show();
 
        thread = new Thread(this);
        thread.start();
    }

    public void run(){
       handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg){
            // HandlerクラスではActivityを継承してないため
            // 別の親クラスのメソッドにて処理を行うようにした。
        try{
            loading();
 
            // プログレスダイアログ終了
        }catch(Exception e){
            Log.d("ftbt", "message", e);
        }
     }
    };

    private void loading(){
        Intent intent = getIntent();
        baseUrl = (String) intent.getSerializableExtra("baseUrl");
        catalogURL = baseUrl + "futaba.php";
        parser = new FutabaCatalogParser(catalogURL);
        parser.parse(getApplicationContext());
        fthreads = parser.getThreads();
        adapter = new FutabaCatalogAdapter(this, R.layout.futaba_catalog_row, fthreads);
        Log.d( "ftbt", "adapter created" );
        setListAdapter(adapter);
        Log.d( "ftbt", "setlitadapter end" );
        waitDialog.dismiss();
   }

 
    public void onClick(View v) {
        Log.d( "ftbt", "catalog onclick" );
        transSettingToThread();
    }

    // スレッド画面に遷移
    public void transSettingToThread() {
        Intent intent = new Intent();
        
        //TODO:IntentでActivityにデータを渡す
        intent.setClassName(getPackageName(), 
            getClass().getPackage().getName()+".fthread");
        startActivity(intent);
    }
}
