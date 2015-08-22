package by.nalivajr.anuta.callbacks.execution;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface UiCallback<T> {

    /**
     * Is invoked when some task execution finished successfully
     * @param result the result of task execution
     * @return true, if the result should be also send to UI thread and false otherwise
     */
    public boolean onSuccess(T result);


    /**
     * Is invoked when some task execution finished with error
     * @param e the result of task execution
     * @return true, if the error should be also send to UI thread and false otherwise
     */
    public boolean onFailed(Throwable e);

    /**
     * Is invoked when any of {@link UiCallback#onSuccess(Object)} or {@link UiCallback#onFailed(Throwable)} returned true
     * @param result the result, which was passed to {@link UiCallback#onSuccess(Object)} method.
     * @param e the error, which was passed to {@link UiCallback#onFailed(Throwable)} method. Is {@code null} when {@link UiCallback#onFailed(Throwable)} was not invoked
     */
    public void onUiThreadRequested(T result, Throwable e);
}