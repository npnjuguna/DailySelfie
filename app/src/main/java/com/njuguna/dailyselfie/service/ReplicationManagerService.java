package com.njuguna.dailyselfie.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.couchbase.lite.replicator.Replication;
import com.njuguna.dailyselfie.app.Preferences;
import com.njuguna.dailyselfie.app.SelfieApplication;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class ReplicationManagerService extends Service {

    private static final String TAG = ReplicationManagerService.class.getSimpleName();
    private static Replication.ReplicationStatus mReplicationStatus;
    private static String mLastReplicationError;
    private static SelfieApplication mApplication;
    private static Preferences mPreferences;

    public static final String ACTION_START_REPLICATION = "com.njuguna.dailyselfie.service.action.ACTION_START_REPLICATION";
    public static final String ACTION_STOP_REPLICATION = "com.njuguna.dailyselfie.service.action.ACTION_STOP_REPLICATION";
    public static final String ACTION_REPLICATION_STATUS_CHANGE = "com.njuguna.dailyselfie.service.action.ACTION_REPLICATION_STATUS_CHANGE";

    public static final String EXTRA_REPLICATION_MESSAGE = "extra_replication_message";

    public ReplicationManagerService() {
    }

    public static void startActionStartReplication(Context context) {
        Intent intent = new Intent(context, ReplicationManagerService.class);
        intent.setAction(ACTION_START_REPLICATION);
        context.startService(intent);
    }

    public static void startActionStopReplication(Context context) {
        Intent intent = new Intent(context, ReplicationManagerService.class);
        intent.setAction(ACTION_STOP_REPLICATION);
        context.startService(intent);
    }

    private void initReplication() {
        SelfieApplication application = (SelfieApplication) getApplication();
        application.getOnSyncProgressChangeObservable().addObserver(new Observer() {
            @Override
            public void update(final Observable observable, final Object data) {
                SelfieApplication.SyncProgress progress = (SelfieApplication.SyncProgress) data;
                com.couchbase.lite.util.Log.d(TAG, "Sync progress changed.  Completed: %d Total: %d Status: %s", progress.completedCount, progress.totalCount, progress.status);
                setReplicationStatus(progress.status);
                Log.d(TAG, "Broadcasting replication status update message.");

                Intent intent = new Intent(ACTION_REPLICATION_STATUS_CHANGE);
                intent.putExtra(EXTRA_REPLICATION_MESSAGE, String.format("Completed: %d Total: %d", progress.completedCount, progress.totalCount, progress.status));
                LocalBroadcastManager.getInstance(ReplicationManagerService.this).sendBroadcast(intent);
            }
        });
        application.getOnSyncUnauthorizedObservable().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                com.couchbase.lite.util.Log.d(SelfieApplication.TAG, "OnSyncUnauthorizedObservable called, show toast");
                String msg = "Sync unable to continue due to invalid session/login. sessionId: " + mApplication.getSyncGatewaySessionId();
            }
        });
    }

    private void startSyncWithCustomCookie(String cookieVal) {

        String cookieName = "SyncGatewaySession";
        boolean isSecure = false;
        boolean httpOnly = false;

        // expiration date - 1 day from now
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int numDaysToAdd = 1;
        cal.add(Calendar.DATE, numDaysToAdd);
        Date expirationDate = cal.getTime();

        mApplication.startReplicationSyncWithCustomCookie(cookieName, cookieVal, "/", expirationDate, isSecure, httpOnly);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = (SelfieApplication) getApplication();
        mPreferences = new Preferences(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        if (action != null) {
            if (action.equals(ACTION_START_REPLICATION)) handleActionStartReplication();
            else if (action.equals(ACTION_STOP_REPLICATION)) handleActionStopReplication();
        }
        return START_STICKY;
    }

    /**
     * stop replication if it is running
     */
    private void handleActionStopReplication() {
        mApplication.stopReplicationSync();
    }

    /**
     * start the replication if it is not already started
     */
    private void handleActionStartReplication() {
        if (!mApplication.isReplicationsExist()) {
            initReplication();
        }
        if (getReplicationStatus() == Replication.ReplicationStatus.REPLICATION_STOPPED) {
            mApplication.restartReplicationSync();
        } else if (!((mPreferences.getLastSyncGatewaySessionId() == null)
                || mPreferences.getLastSyncGatewaySessionId().isEmpty())) {
            startSyncWithCustomCookie(mApplication.getSyncGatewaySessionId());
        }
    }

    public static Replication.ReplicationStatus getReplicationStatus() {
        return mReplicationStatus;
    }

    public static void setReplicationStatus(Replication.ReplicationStatus mReplicationStatus) {
        ReplicationManagerService.mReplicationStatus = mReplicationStatus;
    }

    public static String getLastReplicationError() {
        return mLastReplicationError;
    }

    public static void setLastReplicationError(String mLastReplicationError) {
        ReplicationManagerService.mLastReplicationError = mLastReplicationError;
    }
}
