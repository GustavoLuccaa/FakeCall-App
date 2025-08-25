package com.example.fakecall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//Basicamente o BroadcastReceiver é um componente do android que executa uma chamada ''broadcast''
// do sistema ou do proprio app. Um exemplo de broadcast é o proprio alarme do alarmManager.

public class CallAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String caller = intent.getStringExtra("caller");

        Intent i = new Intent(context, CallActivity.class); // chama a tela de ligação passando o nome de quem liga
        i.putExtra(CallActivity.EXTRA_CALLER_NAME, caller);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
}
