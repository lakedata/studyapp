package ddwu.mobile.finalproject.ma02_20190999.todo;
import android.content.Context;
import android.widget.Toast;
public class SimpleToast {
    private Context context;

    SimpleToast(Context context) {
        this.context = context;
    }

    void showShortTimeToast(int messageResource) {
        Toast.makeText(context, messageResource, Toast.LENGTH_SHORT).show();
    }

    void showLongTimeToast(int messageResource) {
        Toast.makeText(context, messageResource, Toast.LENGTH_LONG).show();
    }
}
