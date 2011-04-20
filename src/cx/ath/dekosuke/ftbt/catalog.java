package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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


public class catalog extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.catalog);
        //DebugUtility.showToast(this, "Catalog.onCreate()");
        
        //webページを取得
        String result = 
            doGetRequest("http://may.2chan.net/40/futaba.php?mode=cat"); //ふたば東方
        DebugUtility.showToast(this, result.substring(1000,1010));
    }


    //URLにGETリクエストURL文字列を指定
    public static String doGetRequest(String sUrl){
        
        String sReturn = "";
        HttpGet httpGetObj   = new HttpGet(sUrl);
        HttpClient httpClientObj = new DefaultHttpClient();  
        HttpParams httpParamsObj = httpClientObj.getParams();
        HttpEntity httpEntityObj = null;
        InputStream inpurStreamObj = null;
        InputStreamReader inputStreamReaderObj = null;
        BufferedReader bufferedReaderObj = null;
        
        //接続のタイムアウト（単位：ms）
        HttpConnectionParams.setConnectionTimeout(httpParamsObj, 5000);
        //データ取得のタイムアウト（単位：ms）サーバ側のプログラム(phpとか)でsleepなどを使えばテストできる
        HttpConnectionParams.setSoTimeout(httpParamsObj, 10000);   
        //user-agent
        httpParamsObj.setParameter("http.useragent", "hogehoge testHttp ua");
        
        try {  
            //httpリクエスト（時間切れなどサーバへのリクエスト時に問題があると例外が発生する）
            HttpResponse httpResponseObj = httpClientObj.execute(httpGetObj);
            //httpレスポンスの400番台以降はエラーだから
            if (httpResponseObj.getStatusLine().getStatusCode() < 400){
                //
                httpEntityObj = httpResponseObj.getEntity();
                //レスポンス本体を取得
                inpurStreamObj = httpEntityObj.getContent();
                
                inputStreamReaderObj = new InputStreamReader(inpurStreamObj);  
                bufferedReaderObj = new BufferedReader(inputStreamReaderObj);  
                StringBuilder stringBuilderObj = new StringBuilder();  
                String sLine;  
                while((sLine = bufferedReaderObj.readLine()) != null){  
                    stringBuilderObj.append(sLine+"\r\n");  
                }
                //
                sReturn = stringBuilderObj.toString();  
                
            }  
        } catch (Exception e) {  
            return null;  
        } finally{
            try {
                if(bufferedReaderObj != null)
                    bufferedReaderObj.close();
                if(inpurStreamObj != null)
                    inpurStreamObj.close();
                if(inputStreamReaderObj != null)
                    inputStreamReaderObj.close();
            } catch (IOException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
                return null;
            }
            
        }
        
        return sReturn; 
    }
}

