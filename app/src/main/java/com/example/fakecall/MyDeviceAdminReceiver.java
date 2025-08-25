package com.example.fakecall;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Device admin ativado", Toast.LENGTH_SHORT).show();
    } // mostra um toast falando que foi ativado

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "Device admin desativado", Toast.LENGTH_SHORT).show();
    } // mostra um toast falando que foi desativado
}

/*
 MyDeviceAdminReceiver extends DeviceAdminReceiver — é a forma padrão (legacy)
 de receber callbacks quando o usuário ativa/desativa o app como "device admin".

 Por que isso existe:
 - Permite reagir quando o usuário concede/retira privilégios de administração do dispositivo.
 - Esses callbacks são úteis para mostrar feedback, habilitar funcionalidades condicionais,
   ou limpar dados quando o admin for removido.

 Observação: ser "device admin" dá ao app poderes sensíveis (ex.: bloquear tela, resetar senha,
 apagar dados), então só peça isso se realmente precisar. O Android tem políticas e
 expectativas rígidas para apps que usam isso.
*/