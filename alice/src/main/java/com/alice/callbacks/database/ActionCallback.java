package com.alice.callbacks.database;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface ActionCallback<R> {

    public void onSuccess(R result);

    public void onFailed(Throwable e);
}
