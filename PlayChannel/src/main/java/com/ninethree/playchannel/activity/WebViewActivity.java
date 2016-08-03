package com.ninethree.playchannel.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ninethree.playchannel.MyApp;
import com.ninethree.playchannel.R;
import com.ninethree.playchannel.bean.WxToken;
import com.ninethree.playchannel.bean.WxUserInfo;
import com.ninethree.playchannel.util.Constants;
import com.ninethree.playchannel.util.Log;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONObject;

import java.net.URI;

public class WebViewActivity extends BaseWebActivity {

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private String mUrl;

    private String mReturnUrl;

    @Override
    public void setLayout() {
        setContentView(R.layout.ac_web_view);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
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
        Log.i("url"+mUrl);
        if (mUrl != null && !mUrl.startsWith("http")){
            toast("地址错误");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            },1000);

            return;
        }

        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);// 设置支持Javascript
//		setting.setCacheMode(WebSettings.LOAD_DEFAULT);  //设置 缓存模式  
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);  //不适用缓存
//		setting.setDomStorageEnabled(true); // 开启 DOM storage API 功能
//		setting.setDatabaseEnabled(true);//开启 database storage API 功能  

        // mWebView.getSettings().setSupportZoom(true);//页面支持缩放
        // mWebView.getSettings().setBuiltInZoomControls(true);
        //String ua = setting.getUserAgentString();
        setting.setUserAgentString("Android");

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面下载完毕,却不代表页面渲染完毕显示出来
                Log.i("onPageFinished---url:"+url);
                CookieManager cookieManager = CookieManager.getInstance();
                String CookieStr = cookieManager.getCookie(url);
                Log.i("Cookies = " + CookieStr);
                mReturnUrl = Uri.parse(url).getQueryParameter("ReturnUrl");
                if (mReturnUrl == null){
                    mReturnUrl = Uri.parse(url).getQueryParameter("returnUrl");
                }
                Log.i("ReturnUrl:" + mReturnUrl);
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

        mWebView.addJavascriptInterface(this, "YLPD");

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

    /**
     * Html调用 window.YLPD.wxLogin();
     */
    @JavascriptInterface
    public void wxLogin() {
        if (!MyApp.api.isWXAppInstalled()) {
            toast("您尚未安装微信");
            return;
        }

        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "ylpd_wx_login";
        MyApp.api.sendReq(req);
        Log.i("开始调用");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MyApp.resp != null){
            if(MyApp.resp.getType() == ConstantsAPI.COMMAND_SENDAUTH && MyApp.isLogin){
                Log.i("token:"+MyApp.resp.token);
                getToken(MyApp.resp.token);
                MyApp.isLogin = false;
            }
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

    //这个方法会取得accesstoken  和openID
    private void getToken(String code){

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+ Constants.APP_ID+"&secret="+Constants.APP_SECRET+"&code=" +code+"&grant_type=authorization_code";

        Request<JSONObject> request = NoHttp.createJsonObjectRequest(url);

        MyApp.getRequestQueue().add(1000, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {
                mProgressDialog.show();
            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                int code = response.getHeaders().getResponseCode();
                Log.i("success-ResponseCode:" + code);
                Log.i("success--get:" + response.get());
                if (code == 200) {
                    WxToken token = MyApp.getGson().fromJson(response.get().toString(), WxToken.class);
                    getUserInfo(token.getAccess_token(),token.getOpenid());
                }
            }

            @Override
            public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
                Log.i("onFailed:" + exception.getMessage());
                Toast.makeText(getApplicationContext(),"授权失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish(int what) {
                mProgressDialog.dismiss();
            }
        });

    }

    //获取到token和openID之后，调用此接口得到身份信息
    private void getUserInfo(String token,String openID){

        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" +token+"&openid=" +openID;

        Request<JSONObject> request = NoHttp.createJsonObjectRequest(url);

        MyApp.getRequestQueue().add(1000, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {
                mProgressDialog.show();
            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                int code = response.getHeaders().getResponseCode();
                Log.i("success-ResponseCode:" + code);
                Log.i("success--get:" + response.get());
                if (code == 200) {
                    WxUserInfo userInfo = MyApp.getGson().fromJson(response.get().toString(), WxUserInfo.class);
                    Log.i(""+userInfo.toString());
                    mWebView.loadUrl("javascript:getwxUserInfoByApp('"+userInfo.getUnionid()+"','"+userInfo.getNickname()+"','"+userInfo.getSex()+"','"+userInfo.getHeadimgurl()+"'"+")");
                }
            }

            @Override
            public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
                Log.i("onFailed:" + exception.getMessage());
                Toast.makeText(getApplicationContext(),"授权失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish(int what) {
                mProgressDialog.dismiss();
            }
        });
    }
}
