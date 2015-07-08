package com.alice.tools;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alice.annonatations.ui.AutoActivity;
import com.alice.annonatations.ui.AutoFragment;
import com.alice.annonatations.ui.AutoView;
import com.alice.annonatations.ui.InnerView;
import com.alice.exceptions.NotAnnotatedActivityUsedException;
import com.alice.exceptions.NotAnnotatedFragmentUsedException;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class ViewTools {

    public static final String TAG = String.format("%s.%s", Alice.class.getSimpleName(), ViewTools.class.getSimpleName());

    ViewTools() {
    }

    /**
     * Sets content view to activity. Finds and injects all views, annotated with {@link InnerView}
     *
     * @param activity the activity to setContent view
     * @return the view, set as a content
     * @throws NotAnnotatedActivityUsedException if activity is not annotated with {@link AutoActivity}
     */
    public View setContentView(Activity activity) throws NotAnnotatedActivityUsedException {
        AutoActivity annotation = activity.getClass().getAnnotation(AutoActivity.class);
        if (annotation == null) {
            throw new NotAnnotatedActivityUsedException();
        }
        int id = annotation.layoutId();
        boolean recursive = annotation.recursive();
        return setContentView(activity, id, recursive);
    }

    /**
     * Creates layout for fragment. Finds and injects all views, annotated with {@link InnerView}
     *
     * @param fragment the fragment to create view
     * @param context  the context
     * @return the view, set as a root view
     */
    public View createView(Fragment fragment, Context context) {
        AutoFragment annotation = fragment.getClass().getAnnotation(AutoFragment.class);
        if (annotation == null) {
            throw new NotAnnotatedFragmentUsedException();
        }
        int layoutId = annotation.layoutId();
        boolean recursive = annotation.recursive();
        return createView(context, fragment, layoutId, recursive);
    }

    /**
     * Creates layout for fragment. Finds and injects all views, annotated with {@link InnerView}
     *
     * @param fragment the fragment to create view
     * @param context  the context
     * @return the view, set as a root view
     */
    public View createView(Context context, Fragment fragment, int layoutId, boolean recursive) {
        View view = LayoutInflater.from(context).inflate(layoutId, null);

        Field[] fields = fragment.getClass().getDeclaredFields();
        for (Field field : fields) {
            InnerView innerViewAnnotation = field.getAnnotation(InnerView.class);
            if (innerViewAnnotation != null) {
                int id = innerViewAnnotation.value();
                View v = view.findViewById(id);
                field.setAccessible(true);
                try {
                    field.set(fragment, v);
                } catch (IllegalAccessException e) {
                    Log.w(TAG, "Could not initialize annotated field", e);
                }
                field.setAccessible(false);
            }
        }
        initView(context, view, recursive);
        return view;
    }

    /**
     * Sets content view to activity. Finds and injects all views, annotated with {@link InnerView}
     *
     * @param activity the activity to setContent view
     * @param layoutId the id of layout resource
     * @return the view, set as a content
     */
    public View setContentView(Activity activity, int layoutId, boolean recursive) {
        View root = activity.getLayoutInflater().inflate(layoutId, null);
        activity.setContentView(root);

        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field : fields) {
            InnerView innerViewAnnotation = field.getAnnotation(InnerView.class);
            if (innerViewAnnotation != null) {
                int id = innerViewAnnotation.value();
                View v = root.findViewById(id);
                field.setAccessible(true);
                try {
                    field.set(activity, v);
                } catch (IllegalAccessException e) {
                    Log.w(TAG, "Could not initialize annotated field", e);
                }
                field.setAccessible(false);
            }
        }
        initView(activity, root, recursive);
        return root;
    }

    /**
     * Finds and injects all views, annotated with {@link InnerView}
     *
     * @param view      target view
     * @param recursive if true then all sub views in hierarchy will be initialized too
     */
    public void initView(Context context, View view, boolean recursive) {
        if (view == null) {
            return;
        }

        if (ViewGroup.class.isAssignableFrom(view.getClass())) {
            AutoView autoView = view.getClass().getAnnotation(AutoView.class);
            ViewGroup v = (ViewGroup) view;
            if (autoView != null) {
                int layoutId = autoView.layoutId();
                LayoutInflater.from(context).inflate(layoutId, v);
                recursive = autoView.recursive();
            }

            for (int i = 0; i < v.getChildCount() && recursive; i++) {
                View child = v.getChildAt(i);
                initView(context, child, recursive);
            }
        }

        Field[] fields = view.getClass().getDeclaredFields();
        for (Field field : fields) {
            InnerView innerViewAnnotation = field.getAnnotation(InnerView.class);
            if (innerViewAnnotation != null) {
                int id = innerViewAnnotation.value();
                View v = view.findViewById(id);
                field.setAccessible(true);
                try {
                    field.set(view, v);
                } catch (IllegalAccessException e) {
                    Log.w(TAG, "Could not initialize annotated field", e);
                }
                field.setAccessible(false);
            }
            if (View.class.isAssignableFrom(field.getType()) && (field.getType().getAnnotation(AutoView.class) != null)) {
                field.setAccessible(true);
                try {
                    View v = (View) field.get(view);
                    initView(context, v, recursive);
                } catch (IllegalAccessException e) {
                    Log.w(TAG, "Could not get access", e);
                }
                field.setAccessible(false);
            }
        }
    }
}
