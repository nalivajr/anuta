package by.nalivajr.anuta.callbacks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import by.nalivajr.anuta.exceptions.NotAnnotatedActivityUsedException;
import by.nalivajr.anuta.tools.Anuta;
import by.nalivajr.anuta.annonatations.ui.AutoActivity;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class AnutaActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = AnutaActivityLifecycleCallbacks.class.getSimpleName();
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        try {
            Anuta.viewTools.setContentView(activity);
        } catch (NotAnnotatedActivityUsedException e) {
            Log.i(TAG, String.format("Activity %s can't be initialized automatically as it is not annotated wits %s",
                    activity.getClass().getName(), AutoActivity.class.getName()));
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
