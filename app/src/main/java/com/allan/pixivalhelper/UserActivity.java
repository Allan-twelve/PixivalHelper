package com.allan.pixivalhelper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class UserActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent intent = this.getIntent();
        String id = intent.getStringExtra("user_id");
        String url = "https://pixiviz.pwp.app/artist/" + id;
//        String url = "https://pixivel.moe/illustrator/" + id;
        WebView webView = findViewById(R.id.user_info_page);
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);//设置允许js
        settings.setDomStorageEnabled(true);//设置允许缓存
        settings.setLoadsImagesAutomatically(true);//设置图片自动加载
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js自动打开窗口
        settings.setAllowFileAccess(true);//设置访问文件file
        settings.setAllowContentAccess(true);//设置访问文件content
        settings.setUseWideViewPort(true);//设置自动适应屏幕
        settings.setSupportMultipleWindows(true);//设置支持多窗口

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }
}