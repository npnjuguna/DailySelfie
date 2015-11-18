package com.njuguna.dailyselfie.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.njuguna.dailyselfie.api.ProfileServerHealthServiceApi;
import com.njuguna.dailyselfie.app.Config;
import com.njuguna.dailyselfie.app.SelfieApplication;
import com.njuguna.dailyselfie.util.TimedOutUrlConnectionClient;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SyncGatewayConnectionService extends IntentService {

    private SelfieApplication mSelfieApplication;
    private final static String TAG = SyncGatewayConnectionService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CHECK_SYNC_GATEWAY_CONNECTION = "com.njuguna.dailyselfie.service.action.CHECK_SYNC_GATEWAY_CONNECTION";

    /**
     * Starts this service to perform action CheckSyncGatewayConnection with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCheckSyncGatewayConnection(Context context, Intent broadcastIntent) {
        Intent intent = new Intent(context, SyncGatewayConnectionService.class);
        intent.setAction(ACTION_CHECK_SYNC_GATEWAY_CONNECTION);
        context.startService(intent);
    }

    public SyncGatewayConnectionService() {
        super("SyncGatewayConnectionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSelfieApplication = (SelfieApplication) getApplication();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_SYNC_GATEWAY_CONNECTION.equals(action)) {
                handleActionCheckSyncGatewayConnection();
            }
        }
    }

    /**
     * Handle action ActionCheckSyncGatewayConnection in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckSyncGatewayConnection() {
        if (mSelfieApplication.hasActiveNetworkConnection()) {

            final ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    mSelfieApplication.setConnectedToWiFi(true);
                    mSelfieApplication.setConnectedToInternet(true);
                }
                if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    mSelfieApplication.setConnectedToWiFi(false);
                    mSelfieApplication.setConnectedToInternet(true);
                }
            }

            RestAdapter restProfileServerAdapter = new RestAdapter.Builder()
                    .setEndpoint(Config.PROFILE_SERVER)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setClient(new TimedOutUrlConnectionClient())
                    .build();

            ProfileServerHealthServiceApi profileServerHealthService = restProfileServerAdapter.create(ProfileServerHealthServiceApi.class);

            try {
                Response response = profileServerHealthService.getHealth();
                if (200 == response.getStatus()) {
                    // update the global app status
                    mSelfieApplication.setConnectedToSyncGateway(true);
                } else {
                    mSelfieApplication.setConnectedToSyncGateway(false);
                }
            } catch (RetrofitError e) {
                mSelfieApplication.setConnectedToSyncGateway(false);
                e.printStackTrace();
            }
        } else {
            mSelfieApplication.setConnectedToInternet(false);
            mSelfieApplication.setConnectedToWiFi(false);
            mSelfieApplication.setConnectedToSyncGateway(false);
        }
    }

}
