package com.ninethree.playchannel.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.ninethree.playchannel.R;
import com.ninethree.playchannel.util.CodeUtil;
import com.ninethree.playchannel.util.DensityUtil;
import com.ninethree.playchannel.util.Log;
import com.youth.banner.Banner;

public class MainActivity extends BaseActivity {

    private Banner mBanner;
    private Integer[] mImages = {R.drawable.banner_1, R.drawable.banner_2, R.drawable.banner_3, R.drawable.banner_4};

    private Button mScanBtn;
    private Button mMyCardBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);
        mLeftBtn.setVisibility(View.INVISIBLE);

        initView();

        //设置样式
        mBanner.setBannerStyle(Banner.CIRCLE_INDICATOR);
        //指示器居中
        mBanner.setIndicatorGravity(Banner.CENTER);
        //设置轮播间隔时间
        mBanner.setDelayTime(4000);
        //设置轮播图，可以是图片网址、资源文件，默认用Glide加载
        mBanner.setImages(mImages);
        //设置点击事件
        mBanner.setOnBannerClickListener(new Banner.OnBannerClickListener() {
            @Override
            public void OnBannerClick(View view, int position) {
                toast("点击了：" + position);
            }
        });
    }

    @Override
    public void setLayout() {
        setContentView(R.layout.ac_main);
    }

    private void initView() {
        mBanner = (Banner) findViewById(R.id.banner);

        mScanBtn = (Button) findViewById(R.id.btn_scan);
        mScanBtn.setOnClickListener(this);

        mMyCardBtn = (Button) findViewById(R.id.btn_my_card);
        mMyCardBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBanner.isAutoPlay(true);//开始轮播
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBanner.isAutoPlay(false);//停止轮播
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_scan:
                intent = new Intent(this,ScanCodeActivity.class);
                startActivity(intent);

                break;
            case R.id.btn_my_card:
                intent = new Intent(this, MyCardActivity.class);
                startActivity(intent);

                break;
        }
    }
}
