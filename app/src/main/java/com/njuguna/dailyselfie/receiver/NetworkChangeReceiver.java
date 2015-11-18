package com.njuguna.dailyselfie.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.njuguna.dailyselfie.app.SelfieApplication;
import com.njuguna.dailyselfie.service.SyncGatewayConnectionService;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // check network state if connected, call service to check whether we can connect to sync gateway
        if ((intent != null) && (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))) {
            SyncGatewayConnectionService.startActionCheckSyncGatewayConnection(context, intent);
        } else {
            ((SelfieApplication) context.getApplicationContext()).setConnectedToInternet(false);
            ((SelfieApplication) context.getApplicationContext()).setConnectedToWiFi(false);
            ((SelfieApplication) context.getApplicationContext()).setConnectedToSyncGateway(false);
        }
    }
}
