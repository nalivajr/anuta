package by.nalivajr.anuta.components.execution;

import java.util.concurrent.Callable;

import by.nalivajr.anuta.callbacks.execution.ActionCallback;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface ActionExecutor {

    public <T> void execute(Callable<T> action, ActionCallback<T> callback);

    public void execute(Runnable action, ActionCallback<Void> callback);

    public void cancelAll();
}
