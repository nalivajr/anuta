package by.nalivajr.alice.callbacks.execution;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractUiCallback<T> implements UiCallback<T>, ActionCallback<T> {

    private static final String TAG = AbstractUiCallback.class.getName();
    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    public void onFinishedSuccessfully(T result) {
        boolean uiRequested = false;
        try {
            uiRequested = onSuccess(result);
        } catch (Throwable e) {
            Log.w(TAG, "An error occurred in onFinished method", e);
        }
        if (uiRequested) {
            postUiThreadCallback(result, null);
        }
    }

    public void onErrorOccurred(Throwable error) {
        boolean uiRequested = false;
        try {
            uiRequested = onFailed(error);
        } catch (Throwable e) {
            Log.w(TAG, "An error occurred in onFailed method", e);
        }
        if (uiRequested) {
            postUiThreadCallback(null, error);
        }
    }

    protected void postUiThreadCallback(final T result, final Throwable e) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    onUiThreadRequested(result, e);
                } catch (Throwable e) {
                    Log.w(TAG, "An error occurred in onUiThreadRequested method", e);
                }
            }
        });
    }
}
