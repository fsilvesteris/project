package com.udacity.android.student.project.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.udacity.android.student.project.app.MainActivity;
import com.udacity.android.student.project.app.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentResolver.CURSOR_DIR_BASE_TYPE;
import static android.content.ContentResolver.CURSOR_ITEM_BASE_TYPE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.CODE;

/**
 * Created by fsilvesteris -
 * based on code from com.example.android.sunshine.app.data.WeatherProvider
 * code refactored to use enumerations and maps
 */
public class ApplicationContentProvider extends ContentProvider {

	public static final String LOG_TAG = ApplicationContentProvider.class.getSimpleName();

	public static final String CONTENT_AUTHORITY = MainActivity.class.getPackage().getName();

	private static final String PATH_COURSE_CATALOG = "course_catalog";

	private static final Uri CONTENT_URI = Uri.parse("content://" + getContentAuthority()).buildUpon().appendPath(PATH_COURSE_CATALOG).build();

	private static final Map<URI_TYPE, String> ContentTypes = new HashMap<>();

	// The URI Matcher used by this content provider.
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


	private DBHelper dbHelper;


	private enum URI_TYPE {
		CATALOG(100, CURSOR_DIR_BASE_TYPE), COURSE(101, CURSOR_ITEM_BASE_TYPE), UNKNOWN(-1, "UNKNOWN");

		private final int type;
		private final String baseContentResolverType;

		private static final Map<Integer, URI_TYPE> map = new HashMap<>();

		private URI_TYPE(int type, String baseContentResolverType) {
			this.type = type;
			this.baseContentResolverType = baseContentResolverType;
		}

		private static URI_TYPE get(int type) {
			URI_TYPE value = map.get(type);
			return value == null ? UNKNOWN : value;
		}

		static {
			for (URI_TYPE value : values()) {
				map.put(value.type, value);
			}

		}

	}

	/*
		Students: Here is where you need to create the UriMatcher. This UriMatcher will
		match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
		and LOCATION integer constants defined above.  You can test this by uncommenting the
		testUriMatcher test within TestUriMatcher.
	 */
	static {
		// I know what you're thinking.  Why create a UriMatcher when you can use regular
		// expressions instead?  Because you're not crazy, that's why.

		// All paths added to the UriMatcher have a corresponding code to return when a match is
		// found.  The code passed into the constructor represents the code to return for the root
		// URI.  It's common to use NO_MATCH as the code for this case.


		// map ContentResolver types
		for (URI_TYPE uriType : URI_TYPE.values()) {
			if (uriType != URI_TYPE.UNKNOWN) {
				String contentResolverType = uriType.baseContentResolverType + "/" + getContentAuthority() + "/" + PATH_COURSE_CATALOG;
				ContentTypes.put(URI_TYPE.CATALOG, contentResolverType);
			}
		}

		// For each type of URI you want to add, create a corresponding code.
		uriMatcher.addURI(getContentAuthority(), PATH_COURSE_CATALOG, URI_TYPE.CATALOG.type);
		uriMatcher.addURI(getContentAuthority(), PATH_COURSE_CATALOG + "/*", URI_TYPE.COURSE.type);

		int type = uriMatcher.match(CONTENT_URI);
		Log.i(LOG_TAG, "URI matcher " + CONTENT_URI + " type " + type);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext(), getContext().getString(R.string.course_catalog_db_name), Integer.valueOf(getContext().getString(R.string.course_catalog_db_version)));
		return true;
	}


	@Override
	public String getType(Uri uri) {
		URI_TYPE uriType = match(uri);

		String contentType = ContentTypes.get(uriType);

		if (contentType == null) {
			throw new UnsupportedOperationException("Unknown content type for uri: " + uri);
		}

		return contentType;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Here's the switch statement that, given a URI, will determine what kind of request it is,
		// and query the database accordingly.
		Cursor retCursor;

		URI_TYPE uriType = match(uri);

		switch (uriType) {

			// "course_catalog"
			case CATALOG: {
				Log.i(LOG_TAG, "Query TYPE_CATALOG " + Arrays.toString(projection) + " selection " + selection + " args " + Arrays.toString(selectionArgs) + " sortOrder " + sortOrder);

				retCursor = dbHelper.getReadableDatabase().query(
						CourseTable.name(),
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);

			}
			break;

			// "course_catalog/*"
			case COURSE: {

				final String selectionStr = CourseTable.name() + "." + CODE.getFieldName() + " = ? ";

				String[] args = new String[]{getCourseCodeFromUri(uri)};

				Log.i(LOG_TAG, "Query TYPE_COURSE " + Arrays.toString(projection) + " selection " + selectionStr + " args " + Arrays.toString(args) + " sortOrder " + sortOrder);

				retCursor = dbHelper.getReadableDatabase().query(
						CourseTable.name(),
						projection,
						selectionStr,
						args,
						null,
						null,
						sortOrder
				);

			}
			break;

			default: {

				String msg;

				if (uriType == URI_TYPE.UNKNOWN) {
					msg = "uri " + uri + " cannot be resolved";
				} else {
					msg = "uri " + uri + " resolves to unhandled type #" + uriType;
				}

				UnsupportedOperationException e = new UnsupportedOperationException(msg);
				Log.e(LOG_TAG, msg, e);

				throw e;
			}

		}
		retCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return retCursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		URI_TYPE uriType = match(uri);
		Uri returnUri;

		switch (uriType) {


			case CATALOG: {

				long _id = dbHelper.insert(null, values);
				if (_id > 0)
					returnUri = buildCourseUri(_id);
				else
					throw new android.database.SQLException("Failed to insert row into " + uri);
				break;
			}

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);

		}


		getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		URI_TYPE uriType = match(uri);
		int rowsDeleted;

		switch (uriType) {
			case CATALOG:
				rowsDeleted = dbHelper.delete(selection, selectionArgs);
				break;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}


		// Because a null deletes all rows
		if (rowsDeleted != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsDeleted;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		URI_TYPE uriType = match(uri);
		int rowsUpdated;

		switch (uriType) {
			case CATALOG:
				rowsUpdated = dbHelper.update(values, selection, selectionArgs);
				break;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		if (rowsUpdated != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsUpdated;
	}


	/*
	* @param uri The content:// URI of the insertion request.
	* @param values An array of sets of column_name/value pairs to add to the database.
	*    This must not be {@code null}.
	* @return The number of values that were inserted.
	*/
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {

		URI_TYPE uriType = match(uri);

		Log.i(LOG_TAG, "Bulk insert " + uri + " - type:" + uriType);

		switch (uriType) {

			case CATALOG: {
				int returnCount = dbHelper.insert(values);
				getContext().getContentResolver().notifyChange(uri, null);
				return returnCount;
			}

			default:
				return super.bulkInsert(uri, values);
		}
	}


	private URI_TYPE match(Uri uri) {
		// Use the Uri Matcher to determine what kind of URI this is.
		return URI_TYPE.get(uriMatcher.match(uri));
	}


	// You do not need to call this method. This is a method specifically to assist the testing
	// framework in running smoothly. You can read more at:
	// http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
	@Override
	@TargetApi(11)
	public void shutdown() {
		dbHelper.close();
		super.shutdown();
	}

	public static Uri getContentUri() {
		return CONTENT_URI;
	}


	/**
	 * return a uri given course code
	 *
	 * @param courseCode courseCode
	 * @return uri with courseCode
	 */
	public static Uri buildCourseUri(String courseCode) {
		return getContentUri().buildUpon().appendPath(courseCode).build();
	}


	/**
	 * return a uri for content item given record id
	 *
	 * @param id database record id
	 * @return uri with record id
	 */
	private static Uri buildCourseUri(long id) {
		return ContentUris.withAppendedId(getContentUri(), id);
	}


	public static String getCourseCodeFromUri(Uri uri) {
		return uri.getPathSegments().get(1);
	}

	public static String getContentAuthority() {
		return CONTENT_AUTHORITY;
	}
}