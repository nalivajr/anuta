package by.nalivajr.anuta.sample;

import android.content.ComponentCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Looper;

import by.nalivajr.anuta.components.AnutaApplication;
import by.nalivajr.anuta.sample.tools.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class TestApplication extends AnutaApplication {

    private Logger logger = new Logger(TestApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // This method is never called, do not add any logic here
    @Override
    public void onTerminate() {
        super.onTerminate();
        logger.info("onTerminate");
    }

    // This method is called when configuration was changed. Provides new re-retrieved resources
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        logger.info("onConfigurationChanged");
    }

    // This method should flush all caches and release resources if possible to free memory
    // GC will be called after this method finished
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        logger.info("onLowMemory");
    }

    // Is called to free memory incrementally. Application could free resources step by step (level be level)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        logger.info("onTrim. Level: " + level);
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        super.registerComponentCallbacks(callback);
        logger.info("registerComponentCallbacks");
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        super.unregisterComponentCallbacks(callback);
        logger.info("unregisterComponentCallbacks");
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
        logger.info("registerActivityLifecycleCallbacks");
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.unregisterActivityLifecycleCallbacks(callback);
        logger.info("unregisterActivityLifecycleCallbacks");
    }

    @Override
    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.registerOnProvideAssistDataListener(callback);
    }

    @Override
    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.unregisterOnProvideAssistDataListener(callback);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public Context getBaseContext() {
        return super.getBaseContext();
    }

    @Override
    public AssetManager getAssets() {
        return super.getAssets();
    }

    @Override
    public Resources getResources() {
        return super.getResources();
    }

    @Override
    public PackageManager getPackageManager() {
        return super.getPackageManager();
    }

    @Override
    public ContentResolver getContentResolver() {
        return super.getContentResolver();
    }

    @Override
    public Looper getMainLooper() {
        return super.getMainLooper();
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    @Override
    public Resources.Theme getTheme() {
        return super.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        return super.getClassLoader();
    }

    @Override
    public String getPackageName() {
        return super.getPackageName();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return super.getApplicationInfo();
    }

    @Override
    public String getPackageResourcePath() {
        return super.getPackageResourcePath();
    }

    @Override
    public String getPackageCodePath() {
        return super.getPackageCodePath();
    }

    // Returns shared preferences, which is the same single instance for each call. Has several MODEs, and mutating bits.
    // In 2.3 and below - MUTATING MULTY_PROCESS, and single process in later versions
    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return super.openFileInput(name);
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return super.openFileOutput(name, mode);
    }

    @Override
    public boolean deleteFile(String name) {
        return super.deleteFile(name);
    }

    @Override
    public File getFileStreamPath(String name) {
        return super.getFileStreamPath(name);
    }

    @Override
    public String[] fileList() {
        return super.fileList();
    }

    @Override
    public File getFilesDir() {
        return super.getFilesDir();
    }
}
