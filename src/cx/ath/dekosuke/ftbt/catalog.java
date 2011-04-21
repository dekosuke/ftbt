package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.WindowManager;

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

//板カタログ表示アクティビティ
public class catalog extends ListActivity {

    private ArrayList<FutabaThread> fthreads = null;
    private FutabaCatalogAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String catalogURL = "http://may.2chan.net/40/futaba.php";
        fthreads = new ArrayList<FutabaThread>();
        FutabaCatalogParser parser = new FutabaCatalogParser(catalogURL);
        parser.parse(getApplicationContext());
        fthreads = parser.getThreads();
        Log.d( "ftbt", "catalog parse end" );

        adapter = new FutabaCatalogAdapter(this, R.layout.futaba_catalog_row, fthreads);
        Log.d( "ftbt", "adapter created" );
        setListAdapter(adapter);
        Log.d( "ftbt", "setlitadapter end" );

        DebugUtility.showToast(this, "catalog");
    }

}
