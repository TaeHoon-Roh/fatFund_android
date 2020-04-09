package com.uxfac.fatfund;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.util.exception.KakaoException;

public class KakaoLoginActivity extends AppCompatActivity {
    final String session = "KAKAO_SESSION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_login);
    }

    //Session call back
    private ISessionCallback sessionCallback = new ISessionCallback() {
        @Override
        public void onSessionOpened() {
            Log.i(session, "Login Success");
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e(session, "Login Failure", exception);
        }
    };

//    @Override
//    protected void onCreated(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_kakao_login);
//
//        Session.getCurrentSession().addCallback(sessionCallback);
//    }
//
//    @Override
//    protected void onDestory() {
//        super.onDestroy();
//
//        Session.getCurrentSession().removeCallback(sessionCallback);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
