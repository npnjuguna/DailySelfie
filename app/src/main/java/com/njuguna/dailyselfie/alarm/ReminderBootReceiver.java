package com.njuguna.dailyselfie.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderBootReceiver extends BroadcastReceiver {
    ReminderAlarmReceiver alarm = new ReminderAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}
