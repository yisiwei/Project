package com.ninethree.playchannel.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninethree.playchannel.R;

import java.util.ArrayList;

/**
 * Created by user on 2016/7/11.
 */
public class MyCardAdapter extends RecyclerView.Adapter<MyCardAdapter.ViewHolder> {

    public ArrayList<String> mDatas;

    public MyCardAdapter(ArrayList<String> datas) {
        this.mDatas = datas;
    }

    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_my_card, viewGroup, false);
        return new ViewHolder(view);
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.mTextView.setText(mDatas.get(position));
    }

    //获取数据的数量
    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.text);
        }
    }
}
