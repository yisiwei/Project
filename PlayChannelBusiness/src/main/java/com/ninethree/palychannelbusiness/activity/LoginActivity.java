package com.ninethree.palychannelbusiness.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.ninethree.palychannelbusiness.R;

public class LoginActivity extends BaseActivity {

    private EditText mUsernameEt;
    private EditText mPasswordEt;
    private Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);

        mTitle.setText(R.string.login);
        mLeftBtn.setVisibility(View.INVISIBLE);

        initView();
    }

    @Override
    public void setLayout() {
        setContentView(R.layout.ac_login);
    }

    private void initView() {
        mUsernameEt = (EditText) findViewById(R.id.username);
        mPasswordEt = (EditText) findViewById(R.id.password);
        mLoginBtn = (Button) findViewById(R.id.btn_login);
        mLoginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login://登录
                String username = mUsernameEt.getText().toString();
                String password = mPasswordEt.getText().toString();

                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();

                break;
        }
    }
}
