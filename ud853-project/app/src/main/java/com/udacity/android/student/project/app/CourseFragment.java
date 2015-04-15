/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.android.student.project.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.android.student.project.app.data.CourseTable.CourseTableCursor;

import static com.udacity.android.student.project.app.data.ApplicationContentProvider.buildCourseUri;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableCursor.CODE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableCursor.HOMEPAGE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableCursor.SHORT_SUMMARY;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableCursor.SUBTITLE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableCursor.TITLE;

/**
 * A placeholder fragment containing a simple view.
 */
public class CourseFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CourseFragment.class.getSimpleName();
    static final String COURSE_URI = "URI";

    private static final String COURSE_SHARE_HASHTAG = " #UdacityCourseApp";

    private ShareActionProvider mShareActionProvider;
    private String shareIntentText;
    private Uri mUri;

    private static final int COURSE_LOADER = 0;

	private TextView codeAndTitleView;
	private TextView subTitleView;
	private TextView shortSummaryView;

	public CourseFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(CourseFragment.COURSE_URI);
        }

	    Log.i(LOG_TAG,"onCreateView uri="+mUri);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

	    codeAndTitleView = (TextView) rootView.findViewById(R.id.item_code_and_title);
	    subTitleView = (TextView) rootView.findViewById(R.id.item_subtitle);
	    shortSummaryView = (TextView) rootView.findViewById(R.id.item_short_summary_text);

	    Log.i(LOG_TAG,"onCreateView shortSummaryView="+shortSummaryView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (shareIntentText != null) {
            mShareActionProvider.setShareIntent(createShareCourseIntent());
        }
    }

    private Intent createShareCourseIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareIntentText + COURSE_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(COURSE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onCourseChanged(String newCourseCode) {
        // replace the uri, since the course has changed

        if (mUri != null) {
            mUri = buildCourseUri(newCourseCode);
            getLoaderManager().restartLoader(COURSE_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        if ( mUri != null) {
            // Now create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.

            return new CursorLoader(
                    getActivity(),
                    mUri,
		            CourseTableCursor.getFieldNames(),
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
	        codeAndTitleView.setText(CODE.getString(cursor)+ " "+TITLE.getString(cursor));
	        subTitleView.setText(SUBTITLE.getString(cursor));
			shortSummaryView.setText(SHORT_SUMMARY.getString(cursor));

	        String courseCode=CODE.getString(cursor);
	        String homepage=HOMEPAGE.getString(cursor);

	        // formatted text for share intent
	        shareIntentText = String.format("%s %s\n - %s\n %s", courseCode, TITLE.getString(cursor), SUBTITLE.getString(cursor), homepage);

	        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareCourseIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}