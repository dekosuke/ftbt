package cx.ath.dekosuke.ftbt;

import android.os.Bundle;

import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import android.preference.Preference.OnPreferenceChangeListener;  

public class PrefSetting extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefsetting);

		try{
			EditTextPreference etp = (EditTextPreference) this
					.findPreference("cachesize");
			// リスナーを設定する
			etp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					return CacheSizeChange(preference, newValue);
				}
			});
		}catch(Exception e){
			Log.i("ftbt", "message", e);
		}
	}

	private boolean CacheSizeChange(Preference preference, Object newValue) {
        String input = newValue.toString();  
        try{
	        if (input != null && Integer.parseInt(input) > 0
	        		&& Integer.parseInt(input) < 999){  
	            preference.setSummary(input);  
	            return true;  
	        } else {  
	            //nullまたは100以下はエラー  
	        }    
        }catch(Exception e){
        	
        }
        Toast.makeText(this, "1000より小さい数字を入力してください", Toast.LENGTH_LONG);
        return false;
	}
}
