package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.content.Intent;

import android.util.Log;

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

//XML Parser
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/*
public class fthread extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog);

        DebugUtility.showToast(this, "fthread");
    }
}
*/

//スレッド表示アクティビティ
public class fthread extends ListActivity {

    private ArrayList<FutabaStatus> statuses = null;
    private FutabaAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //setContentView(R.layout.catalog);
        //DebugUtility.showToast(this, "Catalog.onCreate()");
        
        //webページを取得
//        String result = 
//            doGetRequest("http://may.2chan.net/40/futaba.php?mode=cat"); //ふたば東方
//        String threadUrl = "http://may.2chan.net/40/res/1457519.htm"; //東方テストスレ

        Intent intent = getIntent();
        String threadURL = "http://may.2chan.net/40/res/" + (String) intent.getSerializableExtra("threadNum");
        Log.d( "ftbt", "threadURL:"+threadURL );
        statuses = new ArrayList<FutabaStatus>();
        FutabaThreadParser parser = new FutabaThreadParser(threadURL);
        parser.parse();
        statuses = parser.getStatuses(); 
        Log.d( "ftbt", "parse end" );
        /*
        statuses.add(new FutabaStatus());
        statuses.add(new FutabaStatus());
        statuses.add(new FutabaStatus());
        */

        //サムネイル画像を一括取得してキャッシュに放り込む
        //遅いので将来的には別スレッドに入れる必要があるかもしれない

        //Log.d( "ftbt", "hoge2" );
        adapter = new FutabaAdapter(this, R.layout.futaba_row, statuses);
        //ListView listView = (ListView)this.findViewById(R.id.hoge_list_view);
        //listView.setAdapter(adapter);
        //Log.d( "ftbt", "hoge4" );
        setListAdapter(adapter);
        //Log.d( "ftbt", "hoge5" );

        DebugUtility.showToast(this, "fthread");
    }

}
