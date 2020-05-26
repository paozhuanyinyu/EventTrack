package com.paozhuanyinyu.eventtrack.android.sdk;

import android.app.Application;
import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.util.Map;
@Keep
public class EventTrackAPI {
    private final String TAG = this.getClass().getSimpleName();
    public static final String SDK_VERSION = "1.0.0";
    private static EventTrackAPI INSTANCE;
    private static final Object mLock = new Object();
    private static Map<String,Object> mDeviceInfo;
    private String mDeviceId;
    @Keep
    public static EventTrackAPI init(Application application){
        synchronized (mLock){
            if(null == INSTANCE){
                INSTANCE = new EventTrackAPI(application);
            }
            return INSTANCE;
        }
    }
    @Keep
    public static EventTrackAPI getInstance(){
        return INSTANCE;
    }
    private EventTrackAPI(Application application){
        mDeviceId = EventTrackPrivate.getAndroidID(application.getApplicationContext());
        mDeviceInfo = EventTrackPrivate.getDeviceInfo(application.getApplicationContext());
    }
    @Keep
    public void track(@NonNull final String eventName, @NonNull JSONObject properties){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", eventName);
            jsonObject.put("device_id", mDeviceId);

            JSONObject sendProperties = new JSONObject(mDeviceInfo);

            if (properties != null) {
                EventTrackPrivate.mergeJSONObject(properties, sendProperties);
            }

            jsonObject.put("properties", sendProperties);
            jsonObject.put("time", System.currentTimeMillis());

            Log.i(TAG, EventTrackPrivate.formatJson(jsonObject.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
