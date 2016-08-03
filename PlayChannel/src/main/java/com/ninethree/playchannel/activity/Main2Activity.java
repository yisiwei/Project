package com.ninethree.playchannel.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.ninethree.playchannel.MyApp;
import com.ninethree.playchannel.R;
import com.ninethree.playchannel.adapter.ImagePagerAdapter;
import com.ninethree.playchannel.bean.SessionInfo;
import com.ninethree.playchannel.bean.Upgrade;
import com.ninethree.playchannel.service.DownLoadService;
import com.ninethree.playchannel.util.AppUtil;
import com.ninethree.playchannel.util.FileUtils;
import com.ninethree.playchannel.util.ListUtils;
import com.ninethree.playchannel.util.Log;
import com.ninethree.playchannel.util.PackageUtil;
import com.ninethree.playchannel.view.AutoScrollViewPager;
import com.ninethree.playchannel.view.MyDialog;
import com.ninethree.playchannel.webservice.DBPubService;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

public class Main2Activity extends BaseActivity {

    //轮播
    private ArrayList<Integer> imgList; // 轮播图片List
    private ArrayList<ImageView> portImg;// 轮播圆点List
    private LinearLayout dotsLayout = null;// 圆点布局
    private int preSelImgIndex = 0;
    private AutoScrollViewPager viewPager;// 轮播ViewPager

    private Button mPlayChannelBtn;
    private Button mNearbyPlayBtn;
    private Button mMyFootprintBtn;

    private Button mScanBtn;

    private Button mMyPduBtn;
    private Button mMyCardBtn;
    //private Button mMyCollectBtn;
    private Button mUserCenterBtn;

    private Button mPromotionBtn;//优惠促销
    private Button mActivityBtn;

    private Button mTouristBtn;
    private Button mKursaalBtn;

    private Button mMyRecordBtn;//游玩记录

    private RequestQueue requestQueue;

    private SessionInfo mSessionInfo;

    @Override
    public void setLayout() {
        setContentView(R.layout.ac_main2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);
        mLeftBtn.setImageResource(R.drawable.icon_clear_cache);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCache();
            }
        });
        mRightBtn.setVisibility(View.VISIBLE);
        mRightBtn.setImageResource(R.drawable.icon_scan);
        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),CaptureActivity.class));
            }
        });

        initView();
        initEvent();

        // 轮播广告图
        dotsLayout = (LinearLayout) findViewById(R.id.home_navig_dots);
        imgList = new ArrayList<Integer>();
        imgList.add(R.drawable.banner_1);
        imgList.add(R.drawable.banner_2);
        imgList.add(R.drawable.banner_3);
        imgList.add(R.drawable.banner_4);

        // 初始化轮播圆点
        InitFocusIndicatorContainer();

        viewPager.setAdapter(new ImagePagerAdapter(this, imgList)
                .setInfiniteLoop(true));
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        // 轮播间隔时间
        viewPager.setInterval(3000);
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2
                % ListUtils.getSize(imgList));

        requestQueue = NoHttp.newRequestQueue();

        upgrade();

    }

    /**
     * ViewPager监听
     *
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {

            position = position % imgList.size();
            // 修改上一次选中项的背景
            portImg.get(preSelImgIndex).setImageResource(R.drawable.doc_unshow);
            // 修改当前选中项的背景
            portImg.get(position).setImageResource(R.drawable.doc_show);
            preSelImgIndex = position;

        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    }

    /**
     * 轮播圆点
     */
    private void InitFocusIndicatorContainer() {
        portImg = new ArrayList<ImageView>();
        for (int i = 0; i < imgList.size(); i++) {
            ImageView localImageView = new ImageView(this);
            localImageView.setId(i);
            ImageView.ScaleType localScaleType = ImageView.ScaleType.FIT_XY;
            localImageView.setScaleType(localScaleType);
            LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(
                    36, 36);
            localImageView.setLayoutParams(localLayoutParams);
            localImageView.setPadding(10, 10, 10, 10);
            localImageView.setImageResource(R.drawable.doc_unshow);
            portImg.add(localImageView);
            dotsLayout.addView(localImageView);
        }
    }

    private void initView() {
        //mBanner = (Banner) findViewById(R.id.banner);
        // 轮播广告图
        viewPager = (AutoScrollViewPager) findViewById(R.id.view_pager);

        mPlayChannelBtn = (Button) findViewById(R.id.play_channer);
        mNearbyPlayBtn = (Button) findViewById(R.id.nearby_play);
        mMyFootprintBtn = (Button) findViewById(R.id.my_footprint);

        mScanBtn = (Button) findViewById(R.id.scan);

        mMyPduBtn = (Button) findViewById(R.id.my_pdu);
        mMyCardBtn = (Button) findViewById(R.id.my_card);
        //mMyCollectBtn = (Button) findViewById(R.id.my_collect);
        mUserCenterBtn = (Button) findViewById(R.id.user_center);

        mPromotionBtn = (Button) findViewById(R.id.promotion);
        mActivityBtn = (Button) findViewById(R.id.activity);

        mTouristBtn = (Button) findViewById(R.id.tourist);
        mKursaalBtn = (Button) findViewById(R.id.kursaal);

        mMyRecordBtn = (Button) findViewById(R.id.my_record);
    }

    private void initEvent() {
        mPlayChannelBtn.setOnClickListener(this);
        mNearbyPlayBtn.setOnClickListener(this);
        mMyFootprintBtn.setOnClickListener(this);

        mScanBtn.setOnClickListener(this);

        mMyPduBtn.setOnClickListener(this);
        mMyCardBtn.setOnClickListener(this);
        //mMyCollectBtn.setOnClickListener(this);
        mUserCenterBtn.setOnClickListener(this);

        mPromotionBtn.setOnClickListener(this);
        mActivityBtn.setOnClickListener(this);

        mTouristBtn.setOnClickListener(this);
        mKursaalBtn.setOnClickListener(this);

        //游玩记录
        mMyRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSessionInfo != null && mSessionInfo.getReturnCode() == 0){
                    Intent intent = new Intent(getApplicationContext(),MyRecordActivity.class);
                    intent.putExtra("user",mSessionInfo.getReturnObject().getUserBasic());
                    startActivity(intent);
                }else{
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                }
            }
        });
    }

    private void clearCache(){
        // 清理Webview缓存数据库
        deleteDatabase("webview.db");
        deleteDatabase("webviewCookiesChromium.db");
        deleteDatabase("webviewCookiesChromiumPrivate.db");

        FileUtils.deleteFile(getCacheDir());

        toast("缓存清理成功");
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.startAutoScroll();
//        Log.i("Main>>>onResume");
//        if(MyApp.resp != null){
//            if(MyApp.resp.getType() == ConstantsAPI.COMMAND_SENDAUTH){
//                Log.i("token:"+MyApp.resp.token);
//                //getToken(MyApp.resp.token);
//            }
//        }

        String cookies = AppUtil.getCookies("http://shop.93966.net/h5user/card/mycard");

        if (cookies != null){
            if(mSessionInfo == null){
                String[] arr = cookies.split("=");
                Log.i("value:"+arr[1]);
                new SessionTask().execute(arr[1]);
            }
        }else{
            mSessionInfo = null;
            mTitle.setText(R.string.app_name);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        viewPager.stopAutoScroll();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, WebViewActivity.class);
        String url = null;
        switch (v.getId()) {
            case R.id.play_channer:
                url = "http://ylc.93966.net";
                break;
            case R.id.nearby_play:
                url = "http://ylc.93966.net/playground/store/nearby";
                break;
            case R.id.my_footprint:
                url = "http://ylc.93966.net/playground/my/collect";
                break;
            case R.id.scan://任性刷
                url = "http://pay.93966.net/ScanCode";
                break;
            case R.id.my_pdu:
                url = "http://shop.93966.net/h5user/pdu/mypdu";
                break;
            case R.id.my_card:
                url = "http://shop.93966.net/h5user/card/mycard";
                break;
//            case R.id.my_collect:
//                url = "http://shop.93966.net/h5user/collect/mycollect";
//                break;
            case R.id.user_center://用户中心
                url = "http://shop.93966.net/h5user/UserCenter/Info";
                break;
            case R.id.promotion:
                url = "http://ylc.93966.net/playground/home/index#Yl_md6";
                break;
            case R.id.activity:
                url = "http://ylc.93966.net/Playground/Store/News";
                break;
            case R.id.tourist:
                url = "http://ylc.93966.net/Playground/Topic/List?id=9";
                break;
            case R.id.kursaal:
                url = "http://ylc.93966.net/Playground/Topic/List?id=8";
                break;
//            case R.id.my_record://游玩记录
//                url = "http://shop.93966.net/h5user/pdu/MyConRec";
//                break;
        }
        intent.putExtra("url", url);
        startActivity(intent);
    }


    private void upgrade() {
        Request<JSONObject> request = NoHttp.createJsonObjectRequest("http://m.93966.net:1210/AndroidDown/ylpd_upgrade.json");

        requestQueue.add(1000, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {

            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                int code = response.getHeaders().getResponseCode();
                Log.i("success-ResponseCode:" + code);
                Log.i("success--get:" + response.get());
                if (code == 200 || code == 304) {
                    Upgrade upgrade = MyApp.getGson().fromJson(response.get().toString(), Upgrade.class);
                    if (PackageUtil.getVersionCode(getApplicationContext()) < upgrade
                            .getCode()) {
                        showUpgradeDialog(upgrade.getUrl());
                    }
                }
            }

            @Override
            public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
                Log.i("onFailed:" + exception.getMessage());
            }

            @Override
            public void onFinish(int what) {

            }
        });

    }

    private void showUpgradeDialog(final String url) {
        MyDialog.show(this, "检测到新版本", "立即更新", new MyDialog.OnConfirmListener() {

            @Override
            public void onConfirmClick() {
                downLoad(url);
            }
        });
    }

    private void downLoad(String url) {
        Log.i("下载Url:" + url);
        Intent intent = new Intent(this, DownLoadService.class);
        intent.putExtra("downloadUrl", url);
        startService(intent);
    }

    private class SessionTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            SoapObject result = DBPubService.getSessionInfo(params[0]);
            String code = null;
            try {
                code = result.getProperty(0).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();

            Log.i("result:"+result);

            if (null != result) {
                success(result);
            } else {
                toast("连接超时，请检查您的网络");
            }
        }
    }

    private void success(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);

            mSessionInfo = MyApp.getGson().fromJson(jsonObject.toString(),
                    SessionInfo.class);

            if (mSessionInfo.getReturnCode() == 0){
                mTitle.setText(mSessionInfo.getReturnObject().getOrg().getOrgName());
            }

        } catch (JSONException e) {
            toast("服务器错误");
            e.printStackTrace();
        }
    }

}
