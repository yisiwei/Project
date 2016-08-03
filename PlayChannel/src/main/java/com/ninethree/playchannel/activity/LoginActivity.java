package com.ninethree.playchannel.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.reflect.TypeToken;
import com.jude.swipbackhelper.SwipeBackHelper;
import com.ninethree.playchannel.MyApp;
import com.ninethree.playchannel.R;
import com.ninethree.playchannel.bean.AscPackIntResult;
import com.ninethree.playchannel.bean.AscPackResult;
import com.ninethree.playchannel.bean.Org;
import com.ninethree.playchannel.bean.Result;
import com.ninethree.playchannel.bean.SessionInfo;
import com.ninethree.playchannel.bean.User;
import com.ninethree.playchannel.util.AppUtil;
import com.ninethree.playchannel.util.ByteUtil;
import com.ninethree.playchannel.util.GZipUtil;
import com.ninethree.playchannel.util.Log;
import com.ninethree.playchannel.util.Md5Util;
import com.ninethree.playchannel.util.PreferencesUtil;
import com.ninethree.playchannel.util.StringUtil;
import com.ninethree.playchannel.webservice.DBPubService;
import com.ninethree.playchannel.webservice.LoginService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kobjects.base64.Base64;
import org.ksoap2.serialization.SoapObject;

import java.util.List;

public class LoginActivity extends BaseActivity {

    private EditText mUsernameEt;
    private EditText mPasswordEt;
    private Button mLoginBtn;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);

        mTitle.setText(R.string.login);
        //mLeftBtn.setVisibility(View.INVISIBLE);

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

                if (StringUtil.isNullOrEmpty(username)) {
                    toast("请输入账号");
                    return;
                }

                if (StringUtil.isNullOrEmpty(password)) {
                    toast("请输入密码");
                    return;
                }

                new LoginTask().execute(username, Md5Util.md5(password));

                break;
        }
    }

    /**
     * 登录异步请求
     */
    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String param = LoginService.loginParamPack(getApplicationContext(),
                    params[0], params[1]);
            SoapObject result = LoginService.login(param);
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
            if (null != result) {
                // Base64转byte[]
                byte[] resultByte = Base64.decode(result);
                // 解Guid
                AscPackResult results = ByteUtil.ascPackDownGuid(resultByte);
                byte[] secBytes = (byte[]) results.getResultValue();
                String secBase64 = Base64.encode(secBytes);
                // 将密钥存入SharedPreferences
                PreferencesUtil.putString(getApplicationContext(), "userSec",
                        secBase64);

                // 解true/false
                results = ByteUtil.ascPackDownBoolean(results.getResultBytes());
                boolean bool = (boolean) results.getResultValue();
                Log.i("bool:" + bool);
                if (bool) {// 请求成功
                    // 拆包byte[]
                    AscPackIntResult byteLengthResult = ByteUtil
                            .ascPackDownBytes(results.getResultBytes());
                    Log.i("byte[]长度:" + byteLengthResult.resultValue);
                    // 解压byte[]
                    byte[] dataByte = GZipUtil
                            .unGZip(byteLengthResult.resultBytes);
                    // byte转String
                    String dataStr = ByteUtil.byteToString(dataByte);
                    Log.i("dataStr:" + dataStr);
                    success(dataStr);

                } else {// 请求失败
                    results = ByteUtil.ascPackDownString(results
                            .getResultBytes());
                    String resultData = results.getResultValue().toString();
                    Log.i("false_resultJson:" + resultData);
                    try {
                        JSONObject jsonObject = new JSONObject(resultData);
                        String msg = jsonObject.optString("msg");
                        toast(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                toast("连接超时，请检查您的网络");
            }
        }
    }

    private void success(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            JSONArray tableArray = jsonObject.optJSONArray("Table");
            TypeToken<List<Result>> typeToken = new TypeToken<List<Result>>() {
            };
            List<Result> results = MyApp.getGson().fromJson(
                    tableArray.toString(), typeToken.getType());
            Log.i("results:" + results.get(0));
            Result result = results.get(0);

            if (result.getResultNum() == 0) {// 登录成功

                // user
                JSONArray table6Array = jsonObject.optJSONArray("Table6");
                TypeToken<List<User>> userTypeToken = new TypeToken<List<User>>() {
                };
                List<User> users = MyApp.getGson().fromJson(
                        table6Array.toString(), userTypeToken.getType());

                mUser = users.get(0);

                new LoginGuidTask().execute(mUser.getUserGuid().replace("-","")+mUser.getUserID());

            }else{
                toast(result.getRetMsg());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class LoginGuidTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i("params:"+params[0]);
            SoapObject result = DBPubService.getRetLoginGuid(params[0]);
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
                AppUtil.setCookies(getApplicationContext(),"http://shop.93966.net/h5user/card/mycard","ULSID="+result+mUser.getUserID()+";Max-Age=3600;Domain=.93966.net;Path=/");

                finish();
            } else {
                toast("连接超时，请检查您的网络");
            }
        }
    }


}
