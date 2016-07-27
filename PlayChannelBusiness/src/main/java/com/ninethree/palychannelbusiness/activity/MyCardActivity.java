package com.ninethree.palychannelbusiness.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.ninethree.palychannelbusiness.R;
import com.ninethree.palychannelbusiness.adapter.MyCardAdapter;

import java.util.ArrayList;

public class MyCardActivity extends BaseActivity {

    private XRecyclerView mRecyclerView;
    private MyCardAdapter mAdapter;

    private ArrayList<String> mDatas;

    private int mRefreshTime = 0;
    private int mTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.iconfont_downgrey);

        View header = LayoutInflater.from(this).inflate(R.layout.ac_login, (ViewGroup)findViewById(android.R.id.content),false);
        mRecyclerView.addHeaderView(header);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                mRefreshTime ++;
                mTimes = 0;
                new Handler().postDelayed(new Runnable(){
                    public void run() {

                        mDatas.clear();
                        for(int i = 0; i < 15 ;i++){
                            mDatas.add("item" + i + "after " + mRefreshTime + " times of refresh");
                        }
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.refreshComplete();
                    }

                }, 1000);            //refresh data here
            }

            @Override
            public void onLoadMore() {
                if(mTimes < 2){
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            for(int i = 0; i < 15 ;i++){
                                mDatas.add("item" + (1 + mDatas.size() ) );
                            }
                            mRecyclerView.loadMoreComplete();
                            mAdapter.notifyDataSetChanged();
                        }
                    }, 1000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            for(int i = 0; i < 9 ;i++){
                                mDatas.add("item" + (1 + mDatas.size() ) );
                            }
                            //mRecyclerView.setNoMore(true);

                            mRecyclerView.setIsnomore(true);

                            mAdapter.notifyDataSetChanged();
                        }
                    }, 1000);
                }
                mTimes ++;
            }
        });

        mDatas = new  ArrayList<String>();
//        for(int i = 0; i < 15 ;i++){
//            mDatas.add("item" + i);
//        }
        mAdapter = new MyCardAdapter(mDatas);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setRefreshing(true);

    }

    private void initView() {
        mRecyclerView = (XRecyclerView) findViewById(R.id.recyclerview);
    }

    @Override
    public void setLayout() {
        setContentView(R.layout.ac_my_card);
    }

    @Override
    public void onClick(View v) {

    }
}
