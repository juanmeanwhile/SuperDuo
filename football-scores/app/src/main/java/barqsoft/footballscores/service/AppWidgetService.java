package barqsoft.footballscores.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;

/**
 * Created by Juan on 20/10/2015.
 */
public class AppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MatchesRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class MatchesRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "StackRemoteViewsFactory";
    private Context mContext;
    private int mAppWidgetId;
    private Cursor mCursor;

    public MatchesRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        Log.d(TAG, "onCreate()");
        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String dateSt = (mformat.format(fragmentdate));

        mCursor = mContext.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, new String[]{dateSt}, null);
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        mCursor.moveToPosition(position);
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        String home = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
        String away = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
        int homeGoals = mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));
        int awayGoals = mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));

        rv.setTextViewText(R.id.text, String.format("%s %d-%d %s", home, homeGoals, awayGoals, away));

        // Return the remote views object.
        return rv;
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
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}