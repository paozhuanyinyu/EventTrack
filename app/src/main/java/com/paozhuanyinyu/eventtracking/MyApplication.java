package com.paozhuanyinyu.eventtracking;

import android.app.Application;
import com.paozhuanyinyu.eventtrack.android.sdk.EventTrackAPI;

/**
 * Created by 王灼洲 on 2018/7/22
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initEventTrackAPI(this);
    }

    /**
     * 初始化埋点 SDK
     *
     * @param application Application
     */
    private void initEventTrackAPI(Application application) {
        EventTrackAPI.init(application);
    }
}
