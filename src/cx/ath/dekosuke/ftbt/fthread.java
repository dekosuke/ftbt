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

//スレッド表示アクティビティ
public class fthread extends ListActivity {

    private ArrayList<FutabaStatus> statuses = null;
    private FutabaAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String threadURL = "http://may.2chan.net/40/res/" + (String) intent.getSerializableExtra("threadNum");
        Log.d( "ftbt", "threadURL:"+threadURL );
        statuses = new ArrayList<FutabaStatus>();
        FutabaThreadParser parser = new FutabaThreadParser(threadURL);
        parser.parse();
        statuses = parser.getStatuses(); 
        Log.d( "ftbt", "parse end" );

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
