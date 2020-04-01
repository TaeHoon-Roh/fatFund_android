package com.uxfac.fatfund;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String TAG = "Main Activity";
    WebView contentWebView;
    private ValueCallback<Uri[]> mFilePathCallback;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckPermission();
        hideNavigationBar();
        CreateWebView("http://www.uxfacdev.com:9001");


    }

    private void hideNavigationBar() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.d(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.d(TAG, "Turning immersive mode mode on.");
        }
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void CreateWebView(String targetUrl) {

        contentWebView = (WebView) findViewById(R.id.mywebview);

        WebSettings webSettings = contentWebView.getSettings();

        webSettings.setSaveFormData(true);      // 폼 입력 값 저장 여부
        webSettings.setSupportZoom(true);       // 줌 사용 여부 : HTML Meta태그에 적어놓은 설정이 우선 됨
        webSettings.setBuiltInZoomControls(true); // 줌 사용 여부와 같이 사용해야 하는 설정(안드로이드 내장 기능)
        webSettings.setDisplayZoomControls(false); // 줌 사용 시 하단에 뜨는 +, - 아이콘 보여주기 여부
        webSettings.setJavaScriptEnabled(true); // 자바스크립트 사용 여부
        webSettings.setDomStorageEnabled(true); // 웹뷰내의 localStorage 사용 여부
        webSettings.setGeolocationEnabled(true); // 웹뷰내의 위치 정보 사용 여부

        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(false);


        //cookieManager
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(contentWebView, true);

        //userAgent
        webSettings.setGeolocationEnabled(true);
        String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgent + ";;Uxfactory_APP_ANDROID");


        contentWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
                mFilePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                startActivityForResult(intent, 0);
                return true;


            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {


                Log.i("MyCheck", "Oncreate");
                Log.i("MyCheck", "" + view);
                WebView newWebView = new WebView(MainActivity.this);
                newWebView.getSettings().setJavaScriptEnabled(true);
                newWebView.getSettings().setSupportZoom(true);
                newWebView.getSettings().setBuiltInZoomControls(true);
                newWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                newWebView.getSettings().setSupportMultipleWindows(true);
                view.addView(newWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });

                return true;
            }


            @Override
            public void onCloseWindow(WebView mywindows) {
                super.onCloseWindow(mywindows);
//                super.onCloseWindow(window);
                Log.d(TAG, "Window close");
//                CookieManager cookieManager = CookieManager.getInstance();
//                cookieManager.removeAllCookie();
            }

        });

        contentWebView.setWebViewClient(new WebViewClient() {
            public static final String INTENT_PROTOCOL_START = "intent:";
            public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
            public static final String INTENT_PROTOCOL_END = ";end;";
            public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";


        });


        contentWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        contentWebView.addJavascriptInterface(new myWebviewController(contentWebView), "android");

        contentWebView.loadUrl(targetUrl);

        Log.i("MyCheck", webSettings.getUserAgentString());

    }

    private class myWebviewController {
        private WebView mWebView;

        @JavascriptInterface
        public void requestImage(final String arg) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    goToAlbum();
                }
            });
        }

        private myWebviewController(WebView mWebView) {
            this.mWebView = mWebView;
        }
    }

    private static final int PICK_FROM_ALBUM = 1;

    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e("resultCode:: ", String.valueOf(resultCode));
//        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            } else {
                mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
            }
            mFilePathCallback = null;
        } else {
            mFilePathCallback.onReceiveValue(null);
        }
    }


    private boolean mAcquirePermission;

    private void CheckPermission() {
        boolean bNeedCheckPermission = false;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            bNeedCheckPermission = packageInfo.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.M;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (bNeedCheckPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            mAcquirePermission = true;

        }
    }
    private static final int PERMISSION_REQUEST_STORAGE = 1;
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        }
    }
}
