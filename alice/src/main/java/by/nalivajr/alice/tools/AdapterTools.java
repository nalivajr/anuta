package by.nalivajr.alice.tools;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import by.nalivajr.alice.components.adapters.AliceAbstractAdapter;
import by.nalivajr.alice.components.adapters.AliceDataProvidedSingleViewAdapter;
import by.nalivajr.alice.components.adapters.data.binder.DataBinder;
import by.nalivajr.alice.components.adapters.data.provider.CursorDataProvider;
import by.nalivajr.alice.components.adapters.data.provider.DataProvider;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;
import by.nalivajr.alice.components.database.entitymanager.AliceEntityManager;
import by.nalivajr.alice.components.database.entitymanager.AliceRelationalEntityManager;
import by.nalivajr.alice.components.database.query.AliceQuery;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class AdapterTools {

    AdapterTools() {
    }

    private static final String LOG_TAG = AdapterTools.class.getSimpleName();

    /**
     * Parses XML layouts from resources and extracts ids for all widgets for this.
     * BE AWARE to parse many layout and big layouts in main tread as it can be heavy operation. You can build the map
     * by yourself, if needed
     * @param context the context
     * @param resLayoutsIds a set of ids for layouts to be parsed
     * @return the map, which contains pairs: layout-resource-id -> list of ids of widgets from this layout
     */
    public Map<Integer, List<Integer>> buildIdsMap(Context context , int ... resLayoutsIds) {
        Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
        if (resLayoutsIds == null || resLayoutsIds.length == 0) {
            return result;
        }
        for (int layoutId : resLayoutsIds) {
            List<Integer> ids = parseLayout(context, layoutId);
            result.put(layoutId, ids);
        }
        return result;
    }

    private List<Integer> parseLayout(Context context, int layoutId) {
        List<Integer> ids = new ArrayList<Integer>();
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

    private void extractId(List<Integer> ids, XmlResourceParser parser) {
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

    /**
     * Creates instance of {@link DataProvider} based on item's collection
     * @param items source items collection
     * @return created instance
     */
    public <T> DataProvider<T> createProvider(final Collection<T> items) {
        return new DataProvider<T>() {

            private List<T> itemsList = new ArrayList<T>(items);

            @Override
            public int count() {
                return itemsList.size();
            }

            @Override
            public T getItem(int position) {
                if (position < itemsList.size()) {
                    return itemsList.get(position);
                }
                throw new IndexOutOfBoundsException("Index is greater then items count in provider");
            }
        };
    }

    /**
     * Creates instance of {@link DataProvider} based on item's collection
     * @param items source items collection
     * @return created instance
     */
    public <T> DataProvider<T> createProvider(final T ... items) {
        return new DataProvider<T>() {

            private List<T> itemsList = Arrays.asList(items);

            @Override
            public int count() {
                return itemsList.size();
            }

            @Override
            public T getItem(int position) {
                if (position < itemsList.size()) {
                    return itemsList.get(position);
                }
                throw new IndexOutOfBoundsException("Index is greater then items count in provider");
            }
        };
    }

    /**
     * Creates instance of {@link DataProvider} based on item's collection
     * @param cursor source items cursor
     * @param dataUpdatedAction the callback which will be invoked when data updated
     * @return created instance
     */
    public <T> DataProvider<T> createProvider(final AliceEntityCursor<T> cursor, final Runnable dataUpdatedAction) {
        return new CursorDataProvider<T>(cursor) {
            @Override
            protected void onDataUpdated() {
                if (dataUpdatedAction != null) {
                    dataUpdatedAction.run();
                }
            }
        };
    }

    /**
     * Creates instance of {@link DataProvider} based on item's collection
     * Be aware that this is potentially heavy operation to be run in main thread
     * @param context the context of provider usage
     * @param query the query for items
     * @param dataUpdatedAction the callback which will be invoked when data updated
     * @return created instance
     */
    public <T> DataProvider<T> createProvider(Context context, AliceQuery<T> query, Runnable dataUpdatedAction) {

        final List<Class<?>> entityClasses = new ArrayList<>(1);
        entityClasses.add(query.getTargetClass());

        AliceEntityManager entityManager = new AliceRelationalEntityManager(context) {
            @Override
            protected List<Class<?>> getEntityClasses() {
                return entityClasses;
            }
        };
        return createProvider(entityManager.getEntityCursor(query), dataUpdatedAction);
    }

    /**
     * Creates an instance of {@link AliceDataProvidedSingleViewAdapter} using {@link DataBinder}, {@link DataProvider} and layoutId
     * @param context the context where adapter will be used
     * @param provider the instance of data provider
     * @param binder the instance of data binder
     * @param layoutId the id of layout which will be used to present data
     * @return created instance
     */
    public <T> AliceAbstractAdapter<T> buildAdapter(Context context, DataProvider<T> provider, final DataBinder<T> binder, final int layoutId) {
        return new AliceDataProvidedSingleViewAdapter<T>(context, layoutId, provider) {

            @Override
            protected void bindView(View view, Integer viewId, T item) {
                binder.bindView(view, layoutId, viewId, item);
            }
        };
    }

    /**
     * Creates an instance of {@link AliceDataProvidedSingleViewAdapter} using {@link DataBinder}, {@link DataProvider} and layoutId
     * @param context the context where adapter will be used
     * @param items the items to be presented
     * @param binder the instance of data binder
     * @param layoutId the id of layout which will be used to present data
     * @return created instance
     */
    public <T> AliceAbstractAdapter<T> buildAdapter(Context context, final DataBinder<T> binder, final int layoutId, Collection<T> items) {
        return new AliceDataProvidedSingleViewAdapter<T>(context, layoutId, createProvider(items)) {
            @Override
            protected void bindView(View view, Integer viewId, T item) {
                binder.bindView(view, layoutId, viewId, item);
            }
        };
    }

    /**
     * Creates an instance of {@link AliceDataProvidedSingleViewAdapter} using {@link DataBinder}, {@link DataProvider} and layoutId
     * @param context the context where adapter will be used
     * @param items the items to be presented
     * @param binder the instance of data binder
     * @param layoutId the id of layout which will be used to present data
     * @return created instance
     */
    public <T> AliceAbstractAdapter<T> buildAdapter(Context context, final DataBinder<T> binder, final int layoutId, T ... items) {
        return new AliceDataProvidedSingleViewAdapter<T>(context, layoutId, createProvider(items)) {
            @Override
            protected void bindView(View view, Integer viewId, T item) {
                binder.bindView(view, layoutId, viewId, item);
            }
        };
    }

    /**
     * Creates an instance of {@link AliceDataProvidedSingleViewAdapter} using {@link DataBinder}, {@link DataProvider} and layoutId
     * Be aware that this is potentially heavy operation to be run in main thread
     * @param context the context where adapter will be used
     * @param binder the instance of data binder
     * @param layoutId the id of layout which will be used to present data
     * @return created instance
     */
    public <T> AliceAbstractAdapter<T> buildAdapter(Context context, final DataBinder<T> binder, final int layoutId, AliceQuery<T> query) {
        class AdapterWrapper {
            private AliceAbstractAdapter<T> adapter;
        }

        final AdapterWrapper wrapper = new AdapterWrapper();
        Runnable callback = new Runnable() {

            private Handler uiHandler = new Handler(Looper.getMainLooper());

            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wrapper.adapter.notifyDataSetChanged();
                    }
                });
            }
        };

        DataProvider<T> dataProvider = createProvider(context, query, callback);
        wrapper.adapter = new AliceDataProvidedSingleViewAdapter<T>(context, layoutId, dataProvider) {
            @Override
            protected void bindView(View view, Integer viewId, T item) {
                binder.bindView(view, layoutId, viewId, item);
            }
        };

        return wrapper.adapter;
    }
}
