package com.ninethree.palychannelbusiness.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ninethree.palychannelbusiness.R;
import com.ninethree.palychannelbusiness.util.Log;


public class WebViewActivity extends BaseActivity {

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private String mUrl;

    @Override
    public void setLayout() {
        setContentView(R.layout.ac_web_view);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle.setText("正在加载...");
        //mRightBtn.setVisibility(View.VISIBLE);
        //mRightBtn.setImageResource(android.R.drawable.ic_menu_share);
        //mRightBtn.setOnClickListener(this);
        mLeftBtn.setOnClickListener(this);

        mWebView = (WebView) findViewById(R.id.web_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        Log.i(mUrl);

        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);// 设置支持Javascript
//		setting.setCacheMode(WebSettings.LOAD_DEFAULT);  //设置 缓存模式  
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);  //不适用缓存
//		setting.setDomStorageEnabled(true); // 开启 DOM storage API 功能
//		setting.setDatabaseEnabled(true);//开启 database storage API 功能  

        // mWebView.getSettings().setSupportZoom(true);//页面支持缩放
        // mWebView.getSettings().setBuiltInZoomControls(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面下载完毕,却不代表页面渲染完毕显示出来
                Log.i("onPageFinished");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 自身加载新链接,不做外部跳转
                //view.loadUrl(url);

                if (url.startsWith("tel:") || url.startsWith("sms:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                return false;
            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.i("" + newProgress);
                if (newProgress == 100) {
                    // 网页加载完成
                    mProgressBar.setVisibility(View.GONE);
                    Log.i("title:" + view.getTitle());
                    mTitle.setText(view.getTitle());
                } else {
                    // 加载中
                    mProgressBar.setProgress(newProgress);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        //mWebView.addJavascriptInterface(this, "VECloud");

        mWebView.loadUrl(mUrl);

        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && mWebView.canGoBack()) { // 表示按返回键 时的操作
                        mWebView.goBack(); // 后退
                        // mWebView.goForward();//前进
                        return true; // 已处理
                    }
                }
                return false;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_left_btn://返回
                if (mWebView.canGoBack()) {
                    mWebView.goBack(); // 后退
                } else {
                    finish();//关闭
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.clearCache(true);
    }

}
