package com.ninethree.palychannelbusiness;

import android.app.Application;

import com.google.gson.Gson;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;

/**
 * Created by user on 2016/7/11.
 */
public class MyApp extends Application {

    private static Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();

        gson = new Gson();

        NoHttp.initialize(this);

        Logger.setTag("EWork");
        Logger.setDebug(true);// 开始NoHttp的调试模式, 这样就能看到请求过程和日志
    }

    public static Gson getGson() {
        return gson;
    }
}
