package cx.ath.dekosuke.ftbt;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefSetting extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefsetting);
	}
}
