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

import cx.ath.dekosuke.ftbt.R.id;

public class ftbt extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ArrayAdapter<String> adapter = 
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        // アイテムを追加します
        adapter.add("東方");
        ListView listView = (ListView) findViewById(id.listview);
        // アダプターを設定します
        listView.setAdapter(adapter);

        Log.d("ftbt", "start");

        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                String item = (String) listView.getItemAtPosition(position);
              //  Toast.makeText(ftbt.this, item, Toast.LENGTH_LONG).show();
                transSetting();
            }
        });

        // リストビューのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                // 選択されたアイテムを取得します
                String item = (String) listView.getSelectedItem();
                //Toast.makeText(ftbt.this, item, Toast.LENGTH_LONG).show();
                //Log.d( "caller", "テスト" );
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });    	    
    }

    // 設定画面に遷移
    public void transSetting() {
        Intent intent = new Intent();
        /*
        intent.setClassName(getPackageName(), 
            getClass().getPackage().getName()+".catalog");
        */
        intent.setClassName(getPackageName(), 
            //getClass().getPackage().getName()+".fthread");
            getClass().getPackage().getName()+".catalog");
        startActivity(intent);
    }
}
