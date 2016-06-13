package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.utils.AppUtils;
import com.sam_chordas.android.stockhawk.utils.Constants;

/**
 * Created by Amardeep on 02-05-2016.
 */
public class StockWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;

    private Cursor data;

    public StockWidgetDataProvider(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }

        final long identityToken = Binder.clearCallingIdentity();

        data = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP
                },
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);


        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));

        views.setTextViewText(R.id.stock_symbol, symbol);
        views.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));


        if (AppUtils.showPercent) {
            views.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        } else {
            views.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
        }


        if (data.getInt(data.getColumnIndex("is_up")) == 1) {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (data.moveToPosition(position)) {
            return data.getLong(data.getColumnIndexOrThrow(QuoteColumns._ID));
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
