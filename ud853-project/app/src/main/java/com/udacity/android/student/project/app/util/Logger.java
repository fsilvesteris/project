package com.udacity.android.student.project.app.util;

import android.util.Log;

/**
 * Created by fsilvesteris
 */
public class Logger {

	private Logger() {

	}

	public static void log(Object context, String message) {
		Log.i(context instanceof String ? context.toString() : context.getClass().getSimpleName(), message);
	}

}
