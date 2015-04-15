package com.udacity.android.student.project.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.udacity.android.student.project.app.MainActivity;
import com.udacity.android.student.project.app.R;
import com.udacity.android.student.project.app.data.ApplicationContentProvider;
import com.udacity.android.student.project.app.data.CourseCatalogReader;

import java.util.List;

import static com.udacity.android.student.project.app.data.ApplicationContentProvider.getContentAuthority;
import static com.udacity.android.student.project.app.data.ApplicationContentProvider.getContentUri;



public class SyncAdapter extends AbstractThreadedSyncAdapter {
	public static final String LOG_TAG = SyncAdapter.class.getSimpleName();
	// Interval at which to sync with the weather, in seconds.
	// 60 seconds (1 minute) * 180 = 3 hours
	public static final int SYNC_INTERVAL = 60 * 180;
	public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
	private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
	private static final int COURSE_NOTIFICATION_ID = 3006;

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

		Log.i(LOG_TAG, "ENTER onPerformSync");

		Log.i(LOG_TAG, "BEGIN COURSE SYNC");

		CourseCatalogReader courseCatalogReader = new CourseCatalogReader();

		Log.i(LOG_TAG, "Calling courseCatalogReader::read");
		List<ContentValues> entries = courseCatalogReader.read();
		Log.i(LOG_TAG, " returned from courseCatalogReader::read " + entries.size() + " entries");

		if (entries.size() != 0) {
			Log.i(LOG_TAG, "about to call bulkInsert");

			ContentValues[] contentValues = new ContentValues[entries.size()];
			entries.toArray(contentValues);

			Log.i(LOG_TAG, "contentValues " + contentValues.length);

			Log.i(LOG_TAG, "Calling bulkInsert");
			getContext().getContentResolver().bulkInsert(getContentUri(), contentValues);

			// delete old data so we don't build up an endless history
			//TODO FIXME		    getContext() .getContentResolver() CONTENT_URI,   COLUMN_DATE + " <= ?",  new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1))});

			notifySync();
		}

		Log.i(LOG_TAG, "END COURSE SYNC - read " + entries.size() + " entries");

		return;
	}


	private void notifySync() {
		Context context = getContext();
		//checking the last update and notify if it' the first of the day
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
		boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
				Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

		Log.i(LOG_TAG,"displayNotifications "+displayNotifications);
		if (displayNotifications) {
			Log.i(LOG_TAG,"building notification");

			String lastNotificationKey = context.getString(R.string.pref_last_notification);
			long lastSync = prefs.getLong(lastNotificationKey, 0);

			//if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
				// Last sync was more than 1 day ago, let's send a notification with the update.

					Resources resources = context.getResources();

					String title = context.getString(R.string.app_name);

					String contentText = "Course Catalog Downloaded";

					// NotificationCompatBuilder is a very convenient way to build backward-compatible
					// notifications.  Just throw in some data.
					NotificationCompat.Builder mBuilder =
							new NotificationCompat.Builder(getContext())
									.setColor(resources.getColor(R.color.app_color_primary_light))
									.setContentTitle(title)
									.setContentText(contentText);

					// Make something interesting happen when the user clicks on the notification.
					// In this case, opening the app is sufficient.
					Intent resultIntent = new Intent(context, MainActivity.class);

					// The stack builder object will contain an artificial back stack for the
					// started Activity.
					// This ensures that navigating backward from the Activity leads out of
					// your application to the Home screen.
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
					stackBuilder.addNextIntent(resultIntent);
					PendingIntent resultPendingIntent =	stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(resultPendingIntent);

					NotificationManager mNotificationManager =(NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

					// COURSE_NOTIFICATION_ID allows you to update the notification later on.
					mNotificationManager.notify(COURSE_NOTIFICATION_ID, mBuilder.build());

			Log.i(LOG_TAG,"sent notification");

					//refreshing last sync
					SharedPreferences.Editor editor = prefs.edit();
					editor.putLong(lastNotificationKey, System.currentTimeMillis());
					editor.commit();
			//}
		}
	}

	/**
	 * Helper method to schedule the sync adapter periodic execution
	 */
	public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
		Account account = getSyncAccount(context);
		String authority = context.getString(R.string.content_authority);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// we can enable inexact timers in our periodic sync
			SyncRequest request = new SyncRequest.Builder().
					syncPeriodic(syncInterval, flexTime).
					setSyncAdapter(account, authority).
					setExtras(new Bundle()).build();
			ContentResolver.requestSync(request);
		} else {
			ContentResolver.addPeriodicSync(account,
					authority, new Bundle(), syncInterval);
		}
	}

	/**
	 * Helper method to have the sync adapter sync immediately
	 *
	 * @param context The context used to access the account service
	 */
	public static void syncImmediately(Context context) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(getSyncAccount(context),
				context.getString(R.string.content_authority), bundle);
	}

	/**
	 * Helper method to get the fake account to be used with SyncAdapter, or make a new one
	 * if the fake account doesn't exist yet.  If we make a new account, we call the
	 * onAccountCreated method so we can initialize things.
	 *
	 * @param context The context used to access the account service
	 * @return a fake account.
	 */
	public static Account getSyncAccount(Context context) {
		// Get an instance of the Android account manager
		AccountManager accountManager =
				(AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		// Create the account type and default account
		Account newAccount = new Account(
				context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

		// If the password doesn't exist, the account doesn't exist
		if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
			if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
				return null;
			}
			/*
	         * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

			onAccountCreated(newAccount, context);
		}
		return newAccount;
	}

	private static void onAccountCreated(Account newAccount, Context context) {


		Log.i(LOG_TAG, "onAccountCreated " + newAccount + " starting sync");
        /*
         * Since we've created an account
         */
	//	SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
		SyncAdapter.configurePeriodicSync(context, 120, 60);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
		ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
		syncImmediately(context);
	}

	public static void initializeSyncAdapter(Context context) {

		String contentAuthority = getContentAuthority();
		String contentAuthorityFromContext = context.getString(R.string.content_authority);

		if (!contentAuthorityFromContext.equals(contentAuthority)) {

			RuntimeException e = new RuntimeException(
					"Check application configuration - content authority in context (" +
							contentAuthorityFromContext + ") does not match that returned by " +
							ApplicationContentProvider.class.getSimpleName() + " (" + contentAuthority + ")"
			);
			Log.e(SyncAdapter.class.getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}

		getSyncAccount(context);
	}

}