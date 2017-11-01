package testvolley.dn.java.testvolley;

import android.app.Application;

/**
 * Created by dayanand on 01/11/17.
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        setupLeakCanary();
    }

    protected void setupLeakCanary() {
        if (com.squareup.leakcanary.LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        com.squareup.leakcanary.LeakCanary.install(this);
    }
}
