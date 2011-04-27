package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.widget.Toast;

public class DebugUtility {
	public static void showToast(Context context, String str) {
		Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
		toast.show();
	}
}
