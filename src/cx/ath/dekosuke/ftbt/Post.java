package cx.ath.dekosuke.ftbt;

import java.net.URL;

import cx.ath.dekosuke.ftbt.R.id;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Post extends Activity {
	public String urlStr;
	public String threadNum;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			Log.d("ftbt", "start Post activity");
			Intent intent = getIntent();
			// urlStr = (String) intent.getSerializableExtra("urlStr");
			String baseURL = (String) intent.getSerializableExtra("baseURL");
			String threadNum = (String) intent
					.getSerializableExtra("threadNum");
			urlStr = baseURL + "futaba.php";
			Log.d("ftbt", "threadNum="+threadNum);
			threadNum = threadNum.split("[.]")[0];

			setContentView(R.layout.post);
			Button postbutton = (Button) findViewById(id.postbutton);
			postbutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onClickPostButton(v);
				}
			});
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	public void onClickPostButton(View v){
		//HTTPリクエストを作成する
		//futaba.php?guid=on POST 
		//<b>題　　名</b></td><td><input type=text name=sub size="35"
		//<input type=submit value="返信する" onClick="ptfk(48912)">
		//<textarea name=com cols="48" rows="4" id="ftxa"></textarea>
		//<b>削除キー</b></td><td><input type=password name=pwd size=8 maxlength=8 value="">
		/*
		 * http://localhost/futaba.php?mode=regist&MAX_FILE_SIZE=512000&
		 * pthb=&pthc=&pthd=&flvr=&scsz=&js=off&
		 * resto=48912&name=name&email=email&sub=title&com=text&textonly=on&pwd=1111
		 */
		//viewからデータ取得
		TextView name_v = (TextView) findViewById(id.name);
		String name = name_v.getText().toString();
		TextView email_v = (TextView) findViewById(id.email);
		String email = email_v.getText().toString();
		TextView comment_v = (TextView) findViewById(id.comment);
		String comment = comment_v.getText().toString();
		TextView deletekey_v = (TextView) findViewById(id.deletekey);
		String deletekey = deletekey_v.getText().toString();
		
		/*
		Post activity = (Post) getContext();
		String threadNum = activity.threadNum;
		String urlStr = activity.urlStr;
		*/
		
		Log.d("ftbt", "threadNum="+threadNum);
		Log.d("ftbt", "urlStr="+urlStr);
		Log.d("ftbt", "comment="+comment);
		Log.d("ftbt", "deletekey="+deletekey);
		Log.d("ftbt", "email="+email);
		Log.d("ftbt", "name="+name);
		
		return;
		
		//というわけでリクエストの作成
		// HttpClientの準備
		/*
		DefaultHttpClient httpClient;
		httpClient = new DefaultHttpClient();
		httpClient.getParams()
				.setParameter("http.connection.timeout", 5000);
		httpClient.getParams().setParameter("http.socket.timeout", 3000);
		HttpPost httppost = new HttpPost(urlStr);
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("email", email));
		nameValuePair.add(new BasicNameValuePair("name", "name"));
		nameValuePair.add(new BasicNameValuePair("textonly", "on"));
		nameValuePair.add(new BasicNameValuePair("mode", "regist"));
		nameValuePair.add(new BasicNameValuePair("resto", threadNum));
		nameValuePair.add(new BasicNameValuePair("com", comment));
		nameValuePair.add(new BasicNameValuePair("pwd", deletekey));
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		HttpResponse response = httpClient.execute(httppost);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		response.getEntity().writeTo(byteArrayOutputStream);
		String retData = byteArrayOutputStream.toString();
		*/
	}
}
