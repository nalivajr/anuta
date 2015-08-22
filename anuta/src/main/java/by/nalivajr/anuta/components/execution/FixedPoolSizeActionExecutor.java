package by.nalivajr.anuta.components.execution;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import by.nalivajr.anuta.callbacks.execution.ActionCallback;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class FixedPoolSizeActionExecutor implements ActionExecutor {

    public static final int DEFAULT_POOL_SIZE = 5;
    private static final String TAG  = FixedPoolSizeActionExecutor.class.getName();

    private final int poolSize;
    private ExecutorService executorService;

    public FixedPoolSizeActionExecutor() {
        this(DEFAULT_POOL_SIZE);
    }

    public FixedPoolSizeActionExecutor(int size) {
        poolSize = size;
        initExecutor();
    }

    private void initExecutor() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        executorService = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public <T> void execute(final Callable<T> action, final ActionCallback<T> callback) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    T result = action.call();
                    if (callback != null) {
                        callback.onFinishedSuccessfully(result);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not execute action.", e);
                    if (callback != null) {
                        callback.onErrorOccurred(e);
                    }
                }
            }
        });
    }

    @Override
    public void execute(final Runnable action, final ActionCallback<Void> callback) {
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                action.run();
                return null;
            }
        };
        execute(callable, callback);
    }

    @Override
    public void cancelAll() {
        initExecutor();
    }
}
