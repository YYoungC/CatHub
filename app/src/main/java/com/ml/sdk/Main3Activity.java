package com.ml.sdk;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Main3Activity extends AppCompatActivity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri url = Uri.parse("http://stu.baidu.com");
//        intent.setData(url);
//        startActivity(intent);

        webView = (WebView) findViewById(R.id.baidu);

        //调用setJavaScriptEnabled()方法让WebView支持JS脚本
        webView.getSettings().setJavaScriptEnabled(true);
        //跳转网页仍然在当前WebView中显示
        webView.setWebViewClient(new WebViewClient());

        //调用loadUrl()方法，并传入网址，展示相应内容
        webView.loadUrl("http://stu.baidu.com");

    }
}