package com.njuguna.dailyselfie.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.digits.sdk.android.Digits;
import com.facebook.FacebookSdk;
import com.njuguna.dailyselfie.data.CBLDatabaseHelper;
import com.njuguna.dailyselfie.data.DatabaseHelper;
import com.njuguna.dailyselfie.data.RemindersDataHelper;
import com.njuguna.dailyselfie.data.SelfieContract;
import com.njuguna.dailyselfie.data.SelfiesDataHelper;
import com.njuguna.dailyselfie.data.SelfiesFtsDataHelper;
import com.njuguna.dailyselfie.profile.entity.User;
import com.njuguna.dailyselfie.service.ReplicationManagerService;
import com.njuguna.dailyselfie.service.SyncGatewayConnectionService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Observable;

import io.fabric.sdk.android.Fabric;

public class SelfieApplication extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "hidden";
    private static final String TWITTER_SECRET = "hidden";

    public static final String TAG = SelfieApplication.class.getSimpleName();

    private Context mContext;
    private int versionNumber = 0;

    // application state variables
    private User mUser;

    // network connection state variables
    private Boolean mConnectedToSyncGateway = false;
    private Boolean mConnectedToInternet = false;
    private Boolean mConnectedToWiFi = false;

    // replication state variables
    private String mSyncGatewaySessionId;
    private Boolean mSessionExpired = false;

    // data helpers
    private Manager mManager;
    private Database mDatabase;
    private DatabaseHelper mOpenHelper;
    private CBLDatabaseHelper cblDatabaseHelper;
    private Preferences mPreferences;
    private Boolean mSignedIn = false;

    private SelfiesDataHelper selfiesDataHelper;
    private SelfiesFtsDataHelper selfiesFtsDataHelper;
    private RemindersDataHelper remindersDataHelper;

    // replication stuff
    private int syncCompletedChangedCount;
    private int syncTotalChangedCount;
    private OnSyncProgressChangeObservable onSyncProgressChangeObservable;
    private OnSyncUnauthorizedObservable onSyncUnauthorizedObservable;

    private Replication pullReplication;
    private Replication pushReplication;
    private Replication pullLogReplication;
    private Replication pushLogReplication;

    private ImageLoaderConfiguration mImageLoaderConfiguration;

    public enum AuthenticationType { FACEBOOK, CUSTOM_COOKIE, BASIC }
    private AuthenticationType authenticationType = AuthenticationType.CUSTOM_COOKIE;

    // used for local broadcasts
    private LocalBroadcastManager mLocalBroadcastManager;

    private void initObservable() {
        onSyncProgressChangeObservable = new OnSyncProgressChangeObservable();
        onSyncUnauthorizedObservable = new OnSyncUnauthorizedObservable();
    }

    private synchronized void updateSyncProgress(int completedCount, int totalCount, Replication.ReplicationStatus status) {
        onSyncProgressChangeObservable.notifyChanges(completedCount, totalCount, status);
    }

    public void startReplicationSyncWithCustomCookie(String name, String value, String path, Date expirationDate, boolean secure, boolean httpOnly) {

        if (pullReplication == null && pushReplication == null) {
            Replication[] replications = createReplications();
            pullReplication = replications[0];
            pushReplication = replications[1];

            pullReplication.setCookie(name, value, path, expirationDate, secure, httpOnly);
            pushReplication.setCookie(name, value, path, expirationDate, secure, httpOnly);

            pullReplication.start();
            pushReplication.start();

            Log.v(TAG, "startReplicationSyncWithCustomCookie(): Start Replication Sync ...");
        } else {
            Log.v(TAG, "startReplicationSyncWithCustomCookie(): doing nothing, already have existing replications");

        }

    }

    public void startReplicationSyncWithFacebookLogin(String accessToken) {

        if (pullReplication == null && pushReplication == null) {

            Authenticator facebookAuthenticator = AuthenticatorFactory.createFacebookAuthenticator(accessToken);

            Replication[] replications = createReplications();
            pullReplication = replications[0];
            pushReplication = replications[1];

            pullReplication.setAuthenticator(facebookAuthenticator);
            pushReplication.setAuthenticator(facebookAuthenticator);

            pullReplication.start();
            pushReplication.start();

            Log.v(TAG, "startReplicationSyncWithFacebookLogin(): Start Replication Sync ...");

        } else {
            Log.v(TAG, "startReplicationSyncWithFacebookLogin(): doing nothing, already have existing replications");

        }
    }

    public Replication[] createReplications() {

        URL syncUrl;
        try {
            syncUrl = new URL(Config.SYNC_URL);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid Sync Url", e);
            throw new RuntimeException(e);
        }

        Replication pullRep = mDatabase.createPullReplication(syncUrl);
        pullRep.setContinuous(true);
        pullRep.addChangeListener(getReplicationChangeListener());

        Replication pushRep = mDatabase.createPushReplication(syncUrl);
        pushRep.setContinuous(true);
        pushRep.addChangeListener(getReplicationChangeListener());

        return new Replication[]{pullRep, pushRep};

    }

    public void stopReplicationSync() {
        if (pullReplication != null && pushReplication != null) {
            pullReplication.stop();
            pushReplication.stop();
        }
    }

    public void restartReplicationSync() {
        if (pullReplication != null && pushReplication != null) {
            pullReplication.start();
            pushReplication.start();
        }
    }

    public Boolean isReplicationsExist() {
        return pullReplication != null && pushReplication != null;
    }

    public Replication.ReplicationStatus getReplicationPullStatus() {
        if (pullReplication != null) {
            return pullReplication.getStatus();
        }
        return null;
    }

    public Replication.ReplicationStatus getReplicationPushStatus() {
        if (pushReplication != null) {
            return pushReplication.getStatus();
        }
        return null;
    }

    private Replication.ChangeListener getReplicationChangeListener() {
        return new Replication.ChangeListener() {

            @Override
            public void changed(Replication.ChangeEvent event) {
                Replication replication = event.getSource();
                Log.d(TAG, event.toString());
                updateSyncProgress(
                        replication.getCompletedChangesCount(),
                        replication.getChangesCount(),
                        replication.getStatus()
                );
            }
        };
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public void setPreferences(Preferences mPreferences) {
        this.mPreferences = mPreferences;
    }

    public Boolean isConnectedToSyncGateway() {
        return mConnectedToSyncGateway;
    }

    public void setConnectedToSyncGateway(Boolean mConnectedToSyncGateway) {
        this.mConnectedToSyncGateway = mConnectedToSyncGateway;
    }

    public Boolean isConnectedToInternet() {
        return mConnectedToInternet;
    }

    public void setConnectedToInternet(Boolean mConnectedToInternet) {
        this.mConnectedToInternet = mConnectedToInternet;
    }

    public Boolean isConnectedToWiFi() {
        return mConnectedToWiFi;
    }

    public void setConnectedToWiFi(Boolean mConnectedToWiFi) {
        this.mConnectedToWiFi = mConnectedToWiFi;
    }

    public String getSyncGatewaySessionId() {
        return mSyncGatewaySessionId;
    }

    public void setSyncGatewaySessionId(String mSyncGatewaySessionId) {
        this.mSyncGatewaySessionId = mSyncGatewaySessionId;
        getPreferences().setLastSyncGatewaySessionId(mSyncGatewaySessionId);
    }

    public Boolean isSessionExpired() {
        return mSessionExpired;
    }

    public void setSessionExpired(Boolean mSessionExpired) {
        this.mSessionExpired = mSessionExpired;
    }

    public SelfieApplication getInstance() {
        return this;
    }

    public LocalBroadcastManager getLocalBroadcastManager() {
        return this.mLocalBroadcastManager;
    }

    public void setLocalBroadcastManager(
            LocalBroadcastManager localBroadcastManager) {
        this.mLocalBroadcastManager = localBroadcastManager;
    }

    /**
     * Checks whether this app has available mobile or wireless connection
     *
     * @return
     */
    public boolean hasAvailableNetworkConnection() {
        boolean hasConnectedWifi = false;
        boolean hasConnectedMobile = false;

        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    hasConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    hasConnectedMobile = true;
        }
        boolean hasNetworkConnection = hasConnectedWifi || hasConnectedMobile;
        return hasNetworkConnection;
    }

    /**
     * Checks whether this app has active mobile or wireless connection
     *
     * @return
     */
    public boolean hasActiveNetworkConnection() {
        boolean hasConnectedWifi = false;
        boolean hasConnectedMobile = false;

        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                hasConnectedWifi = true;
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                hasConnectedMobile = true;
            }
        }
        boolean hasNetworkConnection = hasConnectedWifi || hasConnectedMobile;
        return hasNetworkConnection;
    }

    public void onCreate(){
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());

        FacebookSdk.sdkInitialize(getApplicationContext());


        mOpenHelper = new DatabaseHelper(getApplicationContext());
        Log.d(TAG, "Application State: onCreate()");

        this.mContext = getApplicationContext();
        setPreferences(new Preferences(getApplicationContext()));
        setUser(getPreferences().getLastUser());
        setSyncGatewaySessionId(getPreferences().getLastSyncGatewaySessionId());
        setSignedIn(getPreferences().getSignedIn());

        // get the app version number
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNumber = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        // initDatabase();
        cblDatabaseHelper = CBLDatabaseHelper.init(getApplicationContext());
        if (null != cblDatabaseHelper) {
            mDatabase = cblDatabaseHelper.getDatabase();
        }
        initObservable();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        SyncGatewayConnectionService.startActionCheckSyncGatewayConnection(getApplicationContext(), null);
        ReplicationManagerService.startActionStartReplication(getApplicationContext());

        selfiesDataHelper = new SelfiesDataHelper(getApplicationContext());
        selfiesFtsDataHelper = new SelfiesFtsDataHelper(getApplicationContext());
        remindersDataHelper = new RemindersDataHelper(getApplicationContext());

        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(mImageLoaderConfiguration);

    }

    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Returns the application mContext
     * @return Application {@link Context} object
     */
    public Context getAppContext() {
        return this.mContext;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
        getPreferences().setLastUser(mUser);
        getContentResolver().notifyChange(SelfieContract.Selfies.CONTENT_URI, null);
    }

    public Database getDatabase() {
        return mDatabase;
    }

    public Boolean isSignedIn() {
        return mSignedIn;
    }

    public void setSignedIn(Boolean mSignedIn) {
        this.mSignedIn = mSignedIn;
        getPreferences().setSignedIn(mSignedIn);
    }

    public OnSyncProgressChangeObservable getOnSyncProgressChangeObservable() {
        return onSyncProgressChangeObservable;
    }

    public OnSyncUnauthorizedObservable getOnSyncUnauthorizedObservable() {
        return onSyncUnauthorizedObservable;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public class OnSyncProgressChangeObservable extends Observable {
        private void notifyChanges(int completedCount, int totalCount, Replication.ReplicationStatus status) {
            SyncProgress progress = new SyncProgress();
            progress.completedCount = completedCount;
            progress.totalCount = totalCount;
            progress.status = status;
            setChanged();
            notifyObservers(progress);
        }
    }

    public class OnSyncUnauthorizedObservable extends Observable {
        private void notifyChanges() {
            setChanged();
            notifyObservers();
        }
    }

    public class SyncProgress {
        public int completedCount;
        public int totalCount;
        public Replication.ReplicationStatus status;
    }

    public SelfiesDataHelper getSelfiesDataHelper() {
        return selfiesDataHelper;
    }

    public SelfiesFtsDataHelper getSelfiesFtsDataHelper() {
        return selfiesFtsDataHelper;
    }

    public RemindersDataHelper getRemindersDataHelper() {
        return remindersDataHelper;
    }

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }

}
