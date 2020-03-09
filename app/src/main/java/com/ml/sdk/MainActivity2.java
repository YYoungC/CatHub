package com.ml.sdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity2 extends AppCompatActivity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        webView = (WebView) findViewById(R.id.web_view);

        //调用setJavaScriptEnabled()方法让WebView支持JS脚本
        webView.getSettings().setJavaScriptEnabled(true);
        //跳转网页仍然在当前WebView中显示
        webView.setWebViewClient(new WebViewClient());

        //调用loadUrl()方法，并传入网址，展示相应内容
        webView.loadUrl(getIntent().getStringExtra("url"));

    }
}