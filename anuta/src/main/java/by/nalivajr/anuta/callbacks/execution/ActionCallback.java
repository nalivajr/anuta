package by.nalivajr.anuta.callbacks.execution;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface ActionCallback<R> {

    public void onFinishedSuccessfully(R result);

    public void onErrorOccurred(Throwable e);
}
