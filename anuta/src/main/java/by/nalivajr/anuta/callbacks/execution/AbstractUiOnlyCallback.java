package by.nalivajr.anuta.callbacks.execution;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractUiOnlyCallback<T> extends AbstractUiCallback<T> {

    public void onFinishedSuccessfully(T result) {
        postUiThreadCallback(result, null);
    }

    public void onErrorOccurred(Throwable error) {
        postUiThreadCallback(null, error);
    }

    @Override
    public boolean onSuccess(T result) {
        return true;
    }

    @Override
    public boolean onFailed(Throwable e) {
        return true;
    }
}
