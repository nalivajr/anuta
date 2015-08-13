package by.nalivajr.alice.components;

import android.app.Application;

import by.nalivajr.alice.callbacks.AliceActivityLifecycleCallbacks;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class AliceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AliceActivityLifecycleCallbacks());
    }
}
