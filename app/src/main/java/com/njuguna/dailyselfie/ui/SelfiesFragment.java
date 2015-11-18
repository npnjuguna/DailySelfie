package com.njuguna.dailyselfie.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.njuguna.dailyselfie.R;
import com.njuguna.dailyselfie.common.Constants;
import com.njuguna.dailyselfie.data.SelfieContract;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class SelfiesFragment extends Fragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = SelfiesFragment.class.getSimpleName();

    private OnSelfiesFragmentInteractionListener mListener;

    private AbsListView mListView;
    private ProgressBar mProgressBar;

    private SimpleCursorAdapter mAdapter;

    static final String[] PROJECTION = new String[] {
            SelfieContract.Selfies._ID,
            SelfieContract.Selfies.COLUMN_GUID,
            SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH,
            SelfieContract.Selfies.COLUMN_CAPTION,
            SelfieContract.Selfies.COLUMN_CREATE_DATE,
    };

    static String mOrderBy = SelfieContract.Selfies.TABLE_NAME + "." + SelfieContract.Selfies.COLUMN_CREATE_DATE + " DESC";

    public SelfiesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {
                SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH,
                SelfieContract.Selfies.COLUMN_CAPTION,
                SelfieContract.Selfies.COLUMN_CREATE_DATE,
        };
        int[] toViews = {
                R.id.thumbnail,
                R.id.caption,
                R.id.cr_date,
        };

        final DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.lvi_selfie,null, fromColumns,toViews,0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndexOrThrow(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH)) {
                    final ImageView thumbnailView = (ImageView) view;
                    ImageLoader.getInstance().displayImage("file://" + cursor.getString(columnIndex), thumbnailView, options);
                    return true;
                }
                if (columnIndex == cursor.getColumnIndexOrThrow(SelfieContract.Selfies.COLUMN_CREATE_DATE)) {
                    final TextView dateTakenTextView = (TextView) view;
                    dateTakenTextView.setText(Constants.DATE_TIME_FORMATTER.format(cursor.getLong(columnIndex)));
                    return true;
                }
                return false;
            }
        });

        View view = inflater.inflate(R.layout.fragment_selfies_list, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.loading);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSelfiesFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            mListener.onSelfiesFragmentInteraction(id);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.getEmptyView().setVisibility(View.GONE);
        return new CursorLoader(getActivity(), SelfieContract.Selfies.CONTENT_URI,
                PROJECTION, null, null, mOrderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProgressBar.setVisibility(View.GONE);
        mListView.getEmptyView().setVisibility(View.VISIBLE);
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public interface OnSelfiesFragmentInteractionListener {
        void onSelfiesFragmentInteraction(long rowID);
    }

}
