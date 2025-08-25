package com.example.fakecall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CallAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String caller = intent.getStringExtra("caller");

        Intent i = new Intent(context, CallActivity.class);
        i.putExtra(CallActivity.EXTRA_CALLER_NAME, caller);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
}
