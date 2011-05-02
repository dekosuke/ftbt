package cx.ath.dekosuke.ftbt;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import cx.ath.dekosuke.ftbt.R.id;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostView extends LinearLayout {
	public PostView(Context context) {
		this(context, null);
	}

	public PostView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	public PostView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	*/

}
