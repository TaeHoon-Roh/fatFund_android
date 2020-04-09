package com.uxfac.fatfund;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import bridge.AndroidBridge;

public class MainActivity extends AppCompatActivity {

    String TAG = "Main Activity";
    WebView contentWebView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private GoogleSignInClient mGoogleSignInClient;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //KAKAO SDK Init
//        KakaoSDK.init(new KakaoAdapter(){
//            @Override
//            public IApplicationConfig getApplicationConfig() {
//                return new IApplicationConfig() {
//                    @Override
//                    public Context getApplicationContext() {
//                        return MainActivity.this;
//                    }
//                };
//            }
//        });

        setContentView(R.layout.activity_main);

        CheckPermission();

        hideNavigationBar();
        CreateWebView("http://192.168.1.101:3000/");
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

    @Override
    public void onBackPressed() {
        if(contentWebView.canGoBack()){
            contentWebView.goBack();
        }else{
            super.onBackPressed();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void CreateWebView(String targetUrl) {

        contentWebView = findViewById(R.id.mywebview);

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

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult  result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        contentWebView.setWebViewClient(new WebViewClient() {
            public static final String INTENT_PROTOCOL_START = "intent:";
            public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
            public static final String INTENT_PROTOCOL_END = ";end;";
            public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";


        });


        contentWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        contentWebView.addJavascriptInterface(new myWebviewController(contentWebView, MainActivity.this), "android");
        contentWebView.loadUrl(targetUrl);

        Log.i("MyCheck", webSettings.getUserAgentString());

    }

    private class myWebviewController {
        private WebView mWebView;
        private Activity mContext;
        @JavascriptInterface
        public void requestImage(final String arg) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    goToAlbum();
                }
            });
        }

        @JavascriptInterface
        public void call_log2(final String str) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Log.d("AndroidBridge", str);
                    Log.d("AndroidBridge", "go to the SDK");
                }
            });
        }

        private myWebviewController(WebView mWebView, Activity mContext) {
            this.mWebView = mWebView;
            this.mContext = mContext;
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
