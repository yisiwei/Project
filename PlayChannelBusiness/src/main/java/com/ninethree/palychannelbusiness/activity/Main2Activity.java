package com.ninethree.palychannelbusiness.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.ninethree.palychannelbusiness.MyApp;
import com.ninethree.palychannelbusiness.R;
import com.ninethree.palychannelbusiness.adapter.ImagePagerAdapter;
import com.ninethree.palychannelbusiness.bean.Upgrade;
import com.ninethree.palychannelbusiness.service.DownLoadService;
import com.ninethree.palychannelbusiness.util.ListUtils;
import com.ninethree.palychannelbusiness.util.Log;
import com.ninethree.palychannelbusiness.util.PackageUtil;
import com.ninethree.palychannelbusiness.view.AutoScrollViewPager;
import com.ninethree.palychannelbusiness.view.MyDialog;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;
import com.youth.banner.Banner;

import org.json.JSONObject;

import java.util.ArrayList;

public class Main2Activity extends BaseActivity {

//    private Banner mBanner;
//    private Integer[] mImages = {R.drawable.banner_1, R.drawable.banner_2, R.drawable.banner_3, R.drawable.banner_4};

    //轮播
    private ArrayList<Integer> imgList; // 轮播图片List
    private ArrayList<ImageView> portImg;// 轮播圆点List
    private LinearLayout dotsLayout = null;// 圆点布局
    private int preSelImgIndex = 0;
    private AutoScrollViewPager viewPager;// 轮播ViewPager

    private Button mPlayChannelBtn;
    private Button mMyPlayBtn;
    private Button mJoinChannelBtn;

    private Button mPlayEquipmentBtn;//游乐设备
    private Button mEquipmentBtn;
    private Button mUsedEquipmentBtn;//二手设备

    private Button mInvitationBtn;//轻量招标
    private Button mPlaceBtn;   //场地寻租

    private Button mMyClientBtn;//我的客户
    private Button mOrderBtn;//销售订单

    private Button mRecordBtn;//服务记录
    private Button mSaleBtn;//收入提现
    private Button mManagerBtn;//商家管理

    private RequestQueue requestQueue;

    @Override
    public void setLayout() {
        setContentView(R.layout.ac_main2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);
        mLeftBtn.setVisibility(View.INVISIBLE);

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
        viewPager = (AutoScrollViewPager) findViewById(R.id.view_pager);

        mPlayChannelBtn = (Button) findViewById(R.id.play_channel);
        mMyPlayBtn = (Button) findViewById(R.id.my_play);
        mJoinChannelBtn = (Button) findViewById(R.id.join_channel);

        mPlayEquipmentBtn = (Button) findViewById(R.id.play_equipment);
        mEquipmentBtn = (Button) findViewById(R.id.equipment);
        mUsedEquipmentBtn = (Button) findViewById(R.id.used_equipment);

        mInvitationBtn = (Button) findViewById(R.id.invitation);
        mPlaceBtn = (Button) findViewById(R.id.place);

        mMyClientBtn = (Button) findViewById(R.id.my_client);
        mOrderBtn = (Button) findViewById(R.id.order);

        mRecordBtn = (Button) findViewById(R.id.record);
        mSaleBtn = (Button) findViewById(R.id.sale);
        mManagerBtn = (Button) findViewById(R.id.manager);
    }

    private void initEvent() {
        mPlayChannelBtn.setOnClickListener(this);
        mMyPlayBtn.setOnClickListener(this);
        mJoinChannelBtn.setOnClickListener(this);

        mPlayEquipmentBtn.setOnClickListener(this);
        mEquipmentBtn.setOnClickListener(this);
        mUsedEquipmentBtn.setOnClickListener(this);

        mInvitationBtn.setOnClickListener(this);
        mPlaceBtn.setOnClickListener(this);

        mMyClientBtn.setOnClickListener(this);
        mOrderBtn.setOnClickListener(this);

        mRecordBtn.setOnClickListener(this);
        mSaleBtn.setOnClickListener(this);
        mManagerBtn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewPager.stopAutoScroll();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, WebViewActivity.class);
        String url = null;
        switch (v.getId()) {
            case R.id.play_channel:
                url = "http://ylc.93966.net";
                break;
            case R.id.my_play:  //我的游乐场
                url = "http://sj.m.93966.net/Home/MyStore";
                break;
            case R.id.join_channel: //加入频道
                url = "http://ylc.93966.net/Org/Check/2";
                break;
            case R.id.play_equipment:   //游乐设备频道
                url = "http://ylb.93966.net";
                break;
            case R.id.equipment:    //设备大全
                url = "http://ylb.93966.net/PlayEquipment/Product/Category";
                break;
            case R.id.used_equipment: //二手设备
                url = "http://ylb.93966.net/PlayEquipment/Ask/List/1";
                break;
            case R.id.invitation:   //轻量招标
                url = "http://ylb.93966.net/PlayEquipment/Ask/List/3";
                break;
            case R.id.place:    //场地寻租
                url = "http://ylb.93966.net/PlayEquipment/Ask/List/5";
                break;
            case R.id.my_client:    //我的客户
                url = "http://sj.m.93966.net/ClientManager";
                break;
            case R.id.order:    //销售订单
                url = "http://sj.m.93966.net/OrderManager/SaleOrder";
                break;
            case R.id.record:   //服务记录
                url = "http://sj.m.93966.net/OrderManager/UseRecord";
                break;
            case R.id.sale: //收入提现
                url = "http://sj.m.93966.net/CardSale";
                break;
            case R.id.manager:  //商家管理
                url = "http://sj.m.93966.net/";
                break;
        }
        intent.putExtra("url", url);
        startActivity(intent);
    }


    private void upgrade() {
        Request<JSONObject> request = NoHttp.createJsonObjectRequest("http://m.93966.net:1210/AndroidDown/ylpd_upgrade.json");

        requestQueue.add(1000, request, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {
                mProgressDialog.show();
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
                mProgressDialog.dismiss();
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

}
