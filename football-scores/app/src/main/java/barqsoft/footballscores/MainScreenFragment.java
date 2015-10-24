package barqsoft.footballscores;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import barqsoft.footballscores.service.MyFetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
    public ScoresAdapter mAdapter;
    private ListView mList;
    private TextView mInfo;

    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private int mLastSelectedItem = -1;
    private int mSelectedMatchId;


    private OnFragmentInteractionListener mListener;



    public interface OnFragmentInteractionListener {
        void onMatchSelected(int id);
    }

    public MainScreenFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getPackageName() + " must implement OnFragmentInteractionListener");
        }
    }


    private void updateScores() {
        Intent service_start = new Intent(getActivity(), MyFetchService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date) {
        fragmentdate[0] = date;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        updateScores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mList = (ListView) rootView.findViewById(R.id.scores_list);
        mList.setItemsCanFocus(true);
        mAdapter = new ScoresAdapter(getActivity(), null, 0);
        mList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.setDetailId(selected.match_id);
                mListener.onMatchSelected((int) selected.match_id);
                mAdapter.notifyDataSetChanged();
            }
        });

        mList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setDetailId(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mInfo = (TextView) rootView.findViewById(R.id.info);
        mList.setEmptyView(mInfo);

        return rootView;
    }

    @Override
    public void onResume(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);

        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(), null, null, fragmentdate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

        if (cursor != null && cursor.getCount() > 0) {

            int i = 0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                i++;
                cursor.moveToNext();
            }
            //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
            mAdapter.swapCursor(cursor);
            //mAdapter.notifyDataSetChanged();
        } else {
            updateInfoLabel();
        }
    }

    private void updateInfoLabel() {
        int message = R.string.info_empty_results;
        switch (Utilies.getServerStatus(getActivity())) {
            case MyFetchService.SERVER_INVALID:
                message = R.string.info_error_update;
                break;

            case MyFetchService.SERVER_DOWN:
                message = R.string.info_error_sever_down;
                break;

            default:
                if (!Utilies.isNetworkAvailable(getActivity())) {
                    message = R.string.info_no_internet;
                }
        }
        // notify user you are not online
        mInfo.setText(message);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_server_status))) {
            updateInfoLabel();
        }
    }

}
