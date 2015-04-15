package com.udacity.android.student.project.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by fsilvesteris
 */
public class DBHelper extends SQLiteOpenHelper {

	private final String LOG_TAG = getClass().getSimpleName();

	private final int databaseVersion;

	public DBHelper(Context context, String databaseName, int databaseVersion) {
		super(context, databaseName, null, databaseVersion);
		this.databaseVersion = databaseVersion;


		Log.i(LOG_TAG, this.getClass().getSimpleName() + "::constructor( " + databaseName + ". " + databaseVersion + ")");
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {

		execSQL(sqLiteDatabase, CourseTable.getTableCreationSQL());
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		execSQL(sqLiteDatabase, "DROP TABLE IF EXISTS " + CourseTable.name());
		onCreate(sqLiteDatabase);
	}

	private void execSQL(SQLiteDatabase sqLiteDatabase, String sqlStatement) {

		try {
			Log.d(LOG_TAG, "executing SQL [" + sqlStatement + "]");
			sqLiteDatabase.execSQL(sqlStatement);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception when executing SQL [" + sqlStatement + "]");
			Log.e(LOG_TAG, "Exception", e);
			e.printStackTrace();
			throw e;
		}

	}

	public int getDatabaseVersion() {
		return databaseVersion;
	}


	public int insert(ContentValues[] values) {

		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();
		int returnCount = 0;
		try

		{
			for (ContentValues value : values) {

				long _id = db.insert(CourseTable.name(), null, value);
				if (_id != -1) {
					returnCount++;
				}
			}
			db.setTransactionSuccessful();
		} finally

		{
			db.endTransaction();
		}

		return returnCount;
	}


	public int update(ContentValues values, String whereClause, String[] whereArgs) {
		return getWritableDatabase().update(CourseTable.name(), values, whereClause, whereArgs);
	}


	public int delete(String whereClause, String[] whereArgs) {

		// this makes delete all rows return the number of rows deleted (whereClause == null) whereClause = "1";

		return getWritableDatabase().delete(CourseTable.name(), whereClause != null ? whereClause : "1", whereArgs);


	}


	public long insert(String nullColumnHack, ContentValues values) {
		return getWritableDatabase().insert(CourseTable.name(), nullColumnHack, values);
	}


}
