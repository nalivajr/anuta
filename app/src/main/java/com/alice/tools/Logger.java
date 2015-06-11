package com.alice.tools;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class Logger {

    private final String tag;
    public Logger (@NonNull Class cls) {
        tag = cls.getSimpleName();
    }

    private void log(LogLevel logLevel, String message, Throwable e) {
        if (e == null) {
            logWithoutStackTrace(logLevel, message);
        } else {
            logWithStackTrace(logLevel, message, e);
        }
    }

    private void logWithoutStackTrace(LogLevel logLevel, String message) {
        switch (logLevel) {
            case INFO:
                Log.i(tag, message);
                break;
            case WARN:
                Log.w(tag, message);
                break;
            case ERROR:
                Log.e(tag, message);
                break;
        }
    }

    private void logWithStackTrace(LogLevel logLevel, String message, Throwable e) {
        switch (logLevel) {
            case INFO:
                Log.i(tag, message, e);
                break;
            case WARN:
                Log.w(tag, message, e);
                break;
            case ERROR:
                Log.e(tag, message, e);
                break;
        }
    }

    public void info(String message) {
        info(message, null);
    }

    public void info(String message, Throwable e) {
        log(LogLevel.INFO, message, e);
    }

    public void warn(String message) {
        warn(message, null);
    }

    public void warn(String message, Throwable e) {
        log(LogLevel.WARN, message, e);
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable e) {
        log(LogLevel.ERROR, message, e);
    }

    private enum LogLevel {
        INFO,
        WARN,
        ERROR
    }
}
