package anb.developers.com.easyqrmaker;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import anb.developers.com.easyqrmaker.helpers.util.SharedPrefUtil;
import anb.developers.com.easyqrmaker.helpers.util.database.DatabaseUtil;

public class QRMakerScanner extends MultiDexApplication {

    private static QRMakerScanner sInstance;

    public static Context getContext() {
        return sInstance.getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        SharedPrefUtil.init(getApplicationContext());
        DatabaseUtil.init(getApplicationContext());
    }
}
