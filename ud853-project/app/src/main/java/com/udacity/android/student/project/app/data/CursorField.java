package com.udacity.android.student.project.app.data;

import android.database.Cursor;

/**
 * Created by fsilvesteris
 */
public interface CursorField {
	public int getFieldIndex();
	public String name();
	public String getString(Cursor cursor);

}
