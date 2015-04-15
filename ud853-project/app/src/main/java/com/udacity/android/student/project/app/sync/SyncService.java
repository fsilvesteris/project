package com.udacity.android.student.project.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {
	private static final Object syncAdapterLock = new Object();
	private static SyncAdapter syncAdapter = null;

	@Override
	public void onCreate() {
		Log.d(getClass().getSimpleName(), "onCreate");
		synchronized (syncAdapterLock) {
			if (syncAdapter == null) {
				syncAdapter = new SyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return syncAdapter.getSyncAdapterBinder();
	}
}