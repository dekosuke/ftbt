package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import android.util.Log;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FutabaCatalogParser {  
  
    private String urlStr;
    private String title;
    private String titleImgURL;

    private ArrayList<FutabaThread> fthreads;    
  
    public FutabaCatalogParser(String urlStr) {  
        this.urlStr = urlStr;
        title="(title)";
        statuses = new ArrayList<FutabaThread>();
    }  
 
    // メモ:ふたばのスレッドはhtml-body-2つめのformのなかにある
    // スレッドの形式:
    public void parse() {  
        try {  
            //正規表現でパーズ範囲を絞り込む
            Pattern honbunPattern = 
                Pattern.compile("<table.+?>.+?</table>", Pattern.DOTALL);
            Pattern resPattern = 
                Pattern.compile("<td.*?>(.+?)</td>", Pattern.DOTALL);
            Pattern textPattern = 
                Pattern.compile("<small.*?>(.+?)</small>", Pattern.DOTALL);
            Pattern imgPattern = 
                Pattern.compile("<img.*?src=\"(.+?)\"", Pattern.DOTALL);
            Pattern tagPattern = Pattern.compile("<.+?>", Pattern.DOTALL);
         
            cookieSyncManager.createInstance(this);
            CookieSyncManager.getInstance().startSync();
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().removeExpiredCookie();

            // HttpClientの準備
            DefaultHttpClient httpClient;
            httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.getParams().setParameter("http.connection.timeout", 5000);
            httpClient.getParams().setParameter("http.socket.timeout", 3000);
            HttpPost httppost = new HttpPost(urlStr);
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
            nameValuePair.add(new BasicNameValuePair("cx", "10"));
            nameValuePair.add(new BasicNameValuePair("cy", "5"));
            nameValuePair.add(new BasicNameValuePair("cl", "10"));

            // ログイン処理
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                HttpResponse response = httpClient.execute(httppost);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                response.getEntity().writeTo(byteArrayOutputStream);
            } catch (Exception e) {
                Log.d( "ftbt", "httppost error" );
            }


            byte[] data = httpClient.getByteArrayFromURL(urlStr);  
            //parser.setInput(new StringReader(new String(data, "UTF-8")));  
            Matcher mc = honbunPattern.matcher(new String(data, "Shift-JIS"));
            mc.find();
            mc.find(); //2つ目
            String honbun = mc.group(0);
            //Log.d( "ftbt", honbun );
            Matcher mcRes = resPattern.matcher(honbun);
            while( mcRes.find() ){
                Matcher mcText = textPattern.matcher(mcRes.group(1));
                //Log.d( "ftbt", mcRes.group(1) );
                mcText.find();
                FutabaStatus status = new FutabaStatus();
                String text = mcText.group(1);
                text = tagPattern.matcher(text).replaceAll(""); //タグ除去
                status.setText(text);
                Matcher mcImg = thumbPattern.matcher(mcRes.group(1));
                if( mcImg.find() ){
                    status.setImgURL(mcImg.group(1));
                    Log.d( "ftbt", mcImg.group(1) );
                }
                //Log.d( "ftbt", text );
                statuses.add(status);
            }
            Log.d( "ftbt", String.valueOf(statuses.size()) );
        } catch (Exception e) { 
            Log.d( "ftbt", e.toString() ); 
            throw new RuntimeException(e);  
        }  
        //return list;  
    }

    public ArrayList<FutabaThread> getThreads(){
        return fthreads;
    }
} 
