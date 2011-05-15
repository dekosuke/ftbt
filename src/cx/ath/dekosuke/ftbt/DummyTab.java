package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class DummyTab extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
    	setContentView(linearLayout);

    }

}
