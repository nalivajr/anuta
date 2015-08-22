package by.nalivajr.anuta.components;

import android.app.Application;

import by.nalivajr.anuta.callbacks.AnutaActivityLifecycleCallbacks;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class AnutaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AnutaActivityLifecycleCallbacks());
    }
}
