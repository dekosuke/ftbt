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
import java.util.Iterator;

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

    public ArrayList<FutabaStatus> statuses = null;  //レス一覧
    private FutabaAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        try{ 
            Intent intent = getIntent();
            String threadURL = 
                 (String) intent.getSerializableExtra("baseUrl")+
                 (String) intent.getSerializableExtra("threadNum");
            Log.d( "ftbt", "threadURL:"+threadURL );
            statuses = new ArrayList<FutabaStatus>();
            FutabaThreadParser parser = new FutabaThreadParser(threadURL);
            parser.parse();
            statuses = parser.getStatuses(); 
            Log.d( "ftbt", "parse end" );

            adapter = new FutabaAdapter(this, R.layout.futaba_row, statuses);
            setListAdapter(adapter);
        }catch(Exception e){
            Log.i("ftbt", "message", e);
        }
    }

    //スレッドに存在するすべての画像のURLを配列にして返す
    public ArrayList<String> getImageURLs(){
        Iterator iterator = statuses.iterator(); 
        int i=0;
        ArrayList list = new ArrayList<String>();
        //ループ
        while(iterator.hasNext()){
            FutabaStatus status = (FutabaStatus)iterator.next();
            if(status.bigImgURL != null){
                Log.d( "ftbt", "image"+status.bigImgURL );
                list.add(status.bigImgURL);
            }
            i++;
        }
        return list;
    }
}
