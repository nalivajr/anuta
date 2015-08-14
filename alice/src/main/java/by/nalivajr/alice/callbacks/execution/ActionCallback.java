package by.nalivajr.alice.callbacks.execution;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface ActionCallback<R> {

    public void onFinishedSuccessfully(R result);

    public void onErrorOccurred(Throwable e);
}
