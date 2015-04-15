package com.udacity.android.student.project.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.android.student.project.app.R;

import java.io.Closeable;
import java.net.HttpURLConnection;

/**
 * Created by fsilvesteris
 */
public class Util {


	public static void close(Closeable c) {

		try {
			if (c != null) {
				c.close();
			}

		} catch (Exception e) {

		}

	}

	public static void close(HttpURLConnection urlConnection) {
		if (urlConnection != null) {
			try {
				urlConnection.disconnect();
			} catch (Exception e) {

			}

		}
	}


	public static String getActiveCourseCode(Context context) {

		//TODO FIXME - change key and default names
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.pref_location_key), context.getString(R.string.pref_location_default));
	}


	/*
	public static String[] getFieldNames(DatabaseField[] databaseFields) {

		String[] fieldNames = new String[databaseFields.length];

		for (int i = 0; i < databaseFields.length; i++) {
			fieldNames[i] = databaseFields[i].getFieldName();
		}
		return fieldNames;
	}
*/

}
