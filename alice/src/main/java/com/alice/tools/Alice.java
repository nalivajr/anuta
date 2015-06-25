package com.alice.tools;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alice.annonatations.AutoActivity;
import com.alice.annonatations.AutoFragment;
import com.alice.annonatations.AutoView;
import com.alice.annonatations.InnerView;
import com.alice.exceptions.NotAnnotatedActivityUsedException;
import com.alice.exceptions.NotAnnotatedFragmentUsedException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class Alice {

    public static final String TAG = Alice.class.getSimpleName();

    /**
     * Sets content view to activity. Finds and injects all views, annotated with {@link InnerView}
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
        boolean recursive = annotation.recursive();
        return setContentView(activity, id, recursive);
    }

    /**
     * Creates layout for fragment. Finds and injects all views, annotated with {@link InnerView}
     * @param fragment the fragment to create view
     * @param context the context
     * @return the view, set as a root view
     */
    public static View createView(Fragment fragment, Context context) {
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
     * @param fragment the fragment to create view
     * @param context the context
     * @return the view, set as a root view
     */
    public static View createView(Context context, Fragment fragment, int layoutId, boolean recursive) {
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
     * @param activity the activity to setContent view
     * @param layoutId the id of layout resource
     * @return the view, set as a content
     */
    public static View setContentView(Activity activity, int layoutId, boolean recursive) {
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
     * @param view target view
     * @param recursive if true then all sub views in hierarchy will be initialized too
     */
    public static void initView(Context context, View view, boolean recursive) {
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

    public static abstract class AdapterTools {

        private static final String LOG_TAG = String.format("%s.%s", Alice.class.getSimpleName(), AdapterTools.class.getSimpleName()).toUpperCase();

        /**
         * Parses XML layouts from resources and extracts ids for all widgets for this.
         * BE AWARE to parse many layout and big layouts in main tread as it can be heavy operation. You can build the map
         * by yourself, if needed
         * @param context the context
         * @param resLayoutsIds a set of ids for layouts to be parsed
         * @return the map, which contains pairs: layout-resource-id -> list of ids of widgets from this layout
         */
        public static Map<Integer, List<Integer>> buildIdsMap(Context context , int ... resLayoutsIds) {
            Map<Integer, List<Integer>> result = new HashMap<>();
            if (resLayoutsIds == null || resLayoutsIds.length == 0) {
                return result;
            }
            for (int layoutId : resLayoutsIds) {
                List<Integer> ids = parseLayout(context, layoutId);
                result.put(layoutId, ids);
            }
            return result;
        }

        private static List<Integer> parseLayout(Context context, int layoutId) {
            List<Integer> ids = new ArrayList<>();
            XmlResourceParser parser = context.getResources().getLayout(layoutId);
            try {
                int eventType = parser.next();

                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType != XmlPullParser.START_TAG) {
                        eventType = parser.next();
                        continue;
                    }
                    Log.i(LOG_TAG, "Parsing: start TAG: " + parser.getName());
                    extractId(ids, parser);
                    eventType = parser.next();
                }
            } catch (Resources.NotFoundException e) {
                Log.w(LOG_TAG, String.format("Resource layout with id: %d was not found", layoutId), e);
            } catch (XmlPullParserException e) {
                Log.w(LOG_TAG, String.format("And error occurred during parsing layout with id: %d", layoutId), e);
            } catch (IOException e) {
                Log.w(LOG_TAG, String.format("IOException occurred during parsing layout with id: %d", layoutId), e);
            }
            return ids;
        }

        private static void extractId(List<Integer> ids, XmlResourceParser parser) {
            int count = parser.getAttributeCount();
            for (int i = 0; i < count; i++) {
                String name = parser.getAttributeName(i);
                if (name == null || !name.equalsIgnoreCase("id")) {
                    continue;
                }
                String val = parser.getAttributeValue(i);
                try {
                    Integer id = Integer.parseInt(val.substring(1));
                    ids.add(id);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Could not extract id", e);
                }
                break;
            }
        }
    }
}
