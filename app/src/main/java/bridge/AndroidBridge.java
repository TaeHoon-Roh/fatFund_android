package bridge;

import android.util.Log;
import android.webkit.JavascriptInterface;

import android.os.Handler;
import android.webkit.WebView;

import com.uxfac.fatfund.MainActivity;

public class AndroidBridge {
    private String TAG = "Android Bridge";
    final public Handler handler = new Handler();

    private WebView mAppView;
    private MainActivity mContext;

    public AndroidBridge(WebView wv, MainActivity context) {
        mAppView = wv;
        mContext = context;
    }

    @JavascriptInterface
    public void call_log(final String _message) {
        Log.d(TAG, _message);

        handler.post(new Runnable() {
            @Override
            public void run() {
                mAppView.loadUrl("javascript:document.getElementbyId('android-test').innerHTML = test success");
            }
        });
    }
}
