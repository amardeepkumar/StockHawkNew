package com.sam_chordas.android.stockhawk.utils;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Amardeep on 02-05-2016.
 */
public class AppUtils {

    private static final String KEY_CREATED = "created";
    private static final String QUERY = "query";
    private static final String COUNT = "count";
    private static final String RESULTS = "results";
    private static final String QUOTE = "quote";
    private static final String CHANGE = "Change";
    private static final String CHANGE_IN_PERCENT = "ChangeinPercent";
    private static final String BID = "Bid";
    private static final String LOG_TAG = AppUtils.class.getSimpleName();

    public static boolean showPercent = true;
    public static String created;


    public static ArrayList<ContentProviderOperation> quoteJsonToContentValues(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject(QUERY);
                int count = Integer.parseInt(jsonObject.getString(COUNT));
                created = jsonObject.getString(KEY_CREATED);
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject(RESULTS)
                            .getJSONObject(QUOTE);
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject(RESULTS).getJSONArray(QUOTE);

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        Log.d(LOG_TAG, "truncateBidPrice : " + bidPrice);
        bidPrice = String.format(Locale.getDefault(), "%.2f", Float.parseFloat(bidPrice));
        Log.d(LOG_TAG, "truncateBidPrice2 : " + bidPrice);
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.getDefault(), "%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString(CHANGE);
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(Constants.BundleConstants.SYMBOL));
            builder.withValue(QuoteColumns.BIDPRICE, jsonObject.getString(BID));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString(CHANGE_IN_PERCENT), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.CREATED, created);
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }
}
