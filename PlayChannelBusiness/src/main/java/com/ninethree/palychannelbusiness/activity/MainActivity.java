package com.ninethree.palychannelbusiness.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ninethree.palychannelbusiness.MyApp;
import com.ninethree.palychannelbusiness.R;
import com.ninethree.palychannelbusiness.adapter.ImagePagerAdapter;
import com.ninethree.palychannelbusiness.bean.SessionInfo;
import com.ninethree.palychannelbusiness.bean.Upgrade;
import com.ninethree.palychannelbusiness.service.DownLoadService;
import com.ninethree.palychannelbusiness.util.AppUtil;
import com.ninethree.palychannelbusiness.util.DensityUtil;
import com.ninethree.palychannelbusiness.util.ListUtils;
import com.ninethree.palychannelbusiness.util.Log;
import com.ninethree.palychannelbusiness.util.PackageUtil;
import com.ninethree.palychannelbusiness.view.AutoScrollViewPager;
import com.ninethree.palychannelbusiness.view.MyDialog;
import com.ninethree.palychannelbusiness.view.MyProgressDialog;
import com.ninethree.palychannelbusiness.webservice.DBPubService;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //标题栏
    public LinearLayout mTitleLayout;
    public ImageButton mLeftBtn;
    public TextView mRightBtn;
    public TextView mTitle;

    //轮播
    private ArrayList<Integer> imgList; // 轮播图片List
    private ArrayList<ImageView> portImg;// 轮播圆点List
    private LinearLayout dotsLayout = null;// 圆点布局
    private int preSelImgIndex = 0;
    private AutoScrollViewPager viewPager;// 轮播ViewPager

    //按钮
    private Button mMyPlayBtn;//我的游乐场
    private Button mPlayChannelBtn;//游乐频道
    private Button mPlayEquipmentBtn;//游乐设备
    private Button mBusinessBtn;//游乐验票 -- 已安装直接打开，否则跳转到下载页

    private Button mEquipmentBtn;//设备大全
    private Button mInvitationBtn;//设备招标
    private Button mUsedEquipmentBtn;//二手设备
    private Button mPlaceBtn;   //场地寻租

    private Button mMyClientBtn;//我的客户
    private Button mOrderBtn;//销售订单
    private Button mRecordBtn;//服务记录
    private Button mManagerBtn;//商家管理

    private Button mTerminalBtn;//设备管理

    //Session
    private SessionInfo mSessionInfo;

    //Cookie
    private String mCookiesValue;

    //loading对话框
    public MyProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);//禁止截屏

        setContentView(R.layout.ac_main);

        initView();
        initEvent();

        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            // tintManager.setNavigationBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.transparent);

            mTitleLayout.setPadding(0, DensityUtil.getStatusBarHeight(this), 0,
                    0);
        }

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

        //初始化loading对话框
        mProgressDialog = new MyProgressDialog(this);

        //检查新版本
        upgrade();

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * ViewPager监听
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

    //初始化控件
    private void initView() {

        //标题栏
        mTitleLayout = (LinearLayout) findViewById(R.id.title_layout);
        mTitle = (TextView) findViewById(R.id.title_text);
        mLeftBtn = (ImageButton) findViewById(R.id.title_left_btn);
        mRightBtn = (TextView) findViewById(R.id.title_right_btn);

        viewPager = (AutoScrollViewPager) findViewById(R.id.view_pager);

        //
        mPlayChannelBtn = (Button) findViewById(R.id.play_channel);
        mMyPlayBtn = (Button) findViewById(R.id.my_play);
        mBusinessBtn = (Button) findViewById(R.id.business);

        mPlayEquipmentBtn = (Button) findViewById(R.id.play_equipment);
        mEquipmentBtn = (Button) findViewById(R.id.equipment);
        mUsedEquipmentBtn = (Button) findViewById(R.id.used_equipment);

        mInvitationBtn = (Button) findViewById(R.id.invitation);
        mPlaceBtn = (Button) findViewById(R.id.place);

        mMyClientBtn = (Button) findViewById(R.id.my_client);
        mOrderBtn = (Button) findViewById(R.id.order);
        mRecordBtn = (Button) findViewById(R.id.record);
        mManagerBtn = (Button) findViewById(R.id.manager);

        //设备管理
        mTerminalBtn = (Button) findViewById(R.id.terminal);

    }

    //初始化事件
    private void initEvent() {

        //设置
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                intent.putExtra("SessionInfo",mSessionInfo);
                startActivity(intent);
            }
        });

        //入驻
        mRightBtn.setOnClickListener(this);

        mPlayChannelBtn.setOnClickListener(this);
        mMyPlayBtn.setOnClickListener(this);

        //游乐验票
        mBusinessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startYouLeYanPiao();
            }
        });

        mPlayEquipmentBtn.setOnClickListener(this);
        mEquipmentBtn.setOnClickListener(this);
        mUsedEquipmentBtn.setOnClickListener(this);

        mInvitationBtn.setOnClickListener(this);
        mPlaceBtn.setOnClickListener(this);

        mMyClientBtn.setOnClickListener(this);
        mOrderBtn.setOnClickListener(this);

        //服务记录
        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSessionInfo != null && mSessionInfo.getReturnCode() == 0) {
                    Intent intent = new Intent(getApplicationContext(), MyRecordActivity.class);
                    intent.putExtra("user", mSessionInfo.getReturnObject().getUserBasic());
                    intent.putExtra("org", mSessionInfo.getReturnObject().getOrg());
                    startActivity(intent);
                } else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });

        mManagerBtn.setOnClickListener(this);

        //设备管理
        mTerminalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSessionInfo != null && mSessionInfo.getReturnCode() == 0) {
                    Intent intent = new Intent(getApplicationContext(), TerminalActivity.class);
                    intent.putExtra("user", mSessionInfo.getReturnObject().getUserBasic());
                    intent.putExtra("org", mSessionInfo.getReturnObject().getOrg());
                    startActivity(intent);
                } else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });
    }

    //游乐验票
    private void startYouLeYanPiao() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.ninethree.business");
        if (intent != null) {
            startActivity(intent);
        } else {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra("url", "http://a.app.qq.com/o/simple.jsp?pkgname=com.ninethree.business");
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.startAutoScroll();

        String cookies = AppUtil.getCookies("http://sj.m.93966.net/");

        if (cookies != null) {
            if (mCookiesValue == null || !cookies.equals(mCookiesValue)) {
                mCookiesValue = cookies;
                String[] arr = cookies.split("=");
                Log.i("value:" + arr[1]);

                new SessionTask().execute(arr[1]);
            }
        } else {
            mCookiesValue = null;
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
    public void onClick(View v) {
        if (AppUtil.isFastClick()){
            return;
        }
        Intent intent = new Intent(this, WebViewActivity.class);
        String url = null;
        switch (v.getId()) {
            case R.id.title_right_btn://入驻
                url = "http://ylc.93966.net/Org/Join/2";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.play_channel:
                url = "http://ylc.93966.net";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.my_play:  //我的游乐场 http://sj.m.93966.net/Home/MyStore
                url = "http://sj.m.93966.net/Home/ylclist";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.play_equipment:   //游乐设备频道
                url = "http://ylb.93966.net";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.equipment:    //设备大全
                url = "http://ylb.93966.net/PlayEquipment/Product/Category";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.used_equipment: //二手设备
                url = "http://ylb.93966.net/PlayEquipment/Ask/List/1";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.invitation:   //设备招标
                url = "http://ylb.93966.net/PlayEquipment/Ask/List/3";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.place:    //场地寻租
                url = "http://ylb.93966.net/PlayEquipment/Ask/List/5";
                intent.putExtra("url", url);
                startActivity(intent);
                break;
            case R.id.my_client:    //我的客户
//                url = "http://sj.m.93966.net/ClientManager";
//                intent.putExtra("url", url);
//                startActivityForResult(intent,100);

                if (mSessionInfo != null && mSessionInfo.getReturnCode() == 0) {
                    intent = new Intent(this,MyCustomerActivity.class);
                    intent.putExtra("orgId",mSessionInfo.getReturnObject().getOrg().getOrgId());
                    startActivity(intent);
                }else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }

                break;
            case R.id.order:    //销售订单
//                url = "http://sj.m.93966.net/OrderManager/SaleOrder";
//                intent.putExtra("url", url);
//                startActivityForResult(intent,100);

                if (mSessionInfo != null && mSessionInfo.getReturnCode() == 0) {
                    intent = new Intent(this,OrderActivity.class);
                    intent.putExtra("orgId",mSessionInfo.getReturnObject().getOrg().getOrgId());
                    startActivity(intent);
                }else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }

                break;
            case R.id.manager:  //商家管理
                url = "http://sj.m.93966.net/";
                intent.putExtra("url", url);
                startActivityForResult(intent,100);
                break;
        }

    }

    //检查新版本
    private void upgrade() {
        Request<JSONObject> request = NoHttp.createJsonObjectRequest("http://m.93966.net:1210/AndroidDown/ylqyj_upgrade.json");

        MyApp.requestQueue.add(1000, request, new OnResponseListener<JSONObject>() {
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
            public void onFailed(int what, Response<JSONObject> response) {
                Log.i("onFailed:" + response.getException().getMessage());
            }

            @Override
            public void onFinish(int what) {
                mProgressDialog.dismiss();
            }
        });

    }

    //显示有新版本提示对话框
    private void showUpgradeDialog(final String url) {
        MyDialog.show(this, "检测到新版本", "立即更新", new MyDialog.OnConfirmListener() {

            @Override
            public void onConfirmClick() {
                downLoad(url);
            }
        });
    }

    //下载新版本
    private void downLoad(String url) {
        Log.i("下载Url:" + url);
        Intent intent = new Intent(this, DownLoadService.class);
        intent.putExtra("downloadUrl", url);
        startService(intent);
    }

    //获取会话信息
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

            Log.i("result:" + result);

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

            if (mSessionInfo.getReturnCode() == 0) {
                mTitle.setText(mSessionInfo.getReturnObject().getOrg().getOrgName());
            }

        } catch (JSONException e) {
            toast("服务器错误");
            e.printStackTrace();
        }
    }

    //toast提示
    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            Log.i("onActivityResult>>>");
            String cookies = AppUtil.getCookies("http://sj.m.93966.net/");
            if (cookies != null) {
                String[] arr = cookies.split("=");
                Log.i("value:" + arr[1]);
                new SessionTask().execute(arr[1]);
            }
        }
    }
}
