package com.alice.tools;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.alice.annonatations.AutoActivity;
import com.alice.annonatations.AutoView;
import com.alice.exceptions.NotAnnotatedActivityUsedException;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class Alice {

    public static final String TAG = Alice.class.getSimpleName();

    /**
     * Sets content view to activity. Finds and injects all views, annotated with {@link com.alice.annonatations.AutoView}
     * @param activity the activity to setContent view
     * @return the view, set as a content
     * @throws NotAnnotatedActivityUsedException if activity is not annotated with {@link AutoActivity}
     */
    public static View setContentView(Activity activity) throws NotAnnotatedActivityUsedException {
        AutoActivity annotation = activity.getClass().getAnnotation(AutoActivity.class);
        if (annotation == null) {
            throw new NotAnnotatedActivityUsedException();
        }
        int id = annotation.layoutId();
        return setContentView(activity, id);
    }

    /**
     * Sets content view to activity. Finds and injects all views, annotated with {@link com.alice.annonatations.AutoView}
     * @param activity the activity to setContent view
     * @param layoutId the id of layout resource
     * @return the view, set as a content
     */
    public static View setContentView(Activity activity, int layoutId) {
        View root = activity.getLayoutInflater().inflate(layoutId, null);
        activity.setContentView(root);

        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field : fields) {
            AutoView autoViewAnnotation = field.getAnnotation(AutoView.class);
            if (autoViewAnnotation != null) {
                int id = autoViewAnnotation.id();
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
        return root;
    }
}
