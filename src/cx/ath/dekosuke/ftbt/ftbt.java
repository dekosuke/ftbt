package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.os.Bundle;

//adding listview
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.util.Log;

//using Intent
import android.content.Intent;

import android.app.ProgressDialog;
import java.lang.Thread;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.R.id;

public class ftbt extends Activity implements Runnable
{
    private ProgressDialog waitDialog;
    private Thread thread;

    private FutabaTopAdapter adapter = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
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
        }catch(Exception e){
            Log.d("ftbt", "message", e);
        }
     }
    };

    private void loading(){ 
        setContentView(R.layout.main);
     
        FutabaBBSMenuParser parser = 
            new FutabaBBSMenuParser("http://www.2chan.net/bbsmenu.html");
        parser.parse();
 
        ArrayList<FutabaBBS> BBSs = parser.getBBSs();
        adapter = new FutabaTopAdapter(this, 
                R.layout.futaba_bbs_row, BBSs);
        // アイテムを追加します
        ListView listView = (ListView) findViewById(id.listview);
        // アダプターを設定します
        listView.setAdapter(adapter);

        Log.d("ftbt", "start");

        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                FutabaBBS item = (FutabaBBS) listView.getItemAtPosition(position);
              //  Toast.makeText(ftbt.this, item, Toast.LENGTH_LONG).show();
                transSetting(item);
            }
        });

        waitDialog.dismiss();
    }
 
    // 設定画面に遷移
    public void transSetting(FutabaBBS item) {
        Intent intent = new Intent();
        /*
        intent.setClassName(getPackageName(), 
            getClass().getPackage().getName()+".catalog");
        */
        Log.d("ftbt", item.url );
        intent.putExtra("baseUrl", item.url);
        intent.setClassName(getPackageName(), 
            //getClass().getPackage().getName()+".fthread");
            getClass().getPackage().getName()+".catalog");
        startActivity(intent);
    }
}
