package com.alice.tools;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
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
}
