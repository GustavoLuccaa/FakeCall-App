package com.example.fakecall;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.media.AudioAttributes;              // definir atributos de áudio no Ringtone
import android.media.Ringtone;                     // para tocar o ringtone do sistema
import android.media.RingtoneManager;              // para obter o ringtone padrão
import android.net.Uri;                            // Uri do ringtone
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/*
 CallActivity: tela que simula uma chamada recebida.
 - É iniciada pelo BroadcastReceiver (CallAlarmReceiver) quando o alarme dispara.
 - Mostra o nome do "chamador", e tem botões para "atender" e "recusar".
 - Importante: essa Activity pode ser aberta quando o app está em background; por isso
   precisa ter cuidado com background activity starts em versões modernas do Android.
 */

public class CallActivity extends AppCompatActivity {

    public static final String EXTRA_CALLER_NAME = "extra_caller_name";

    // Guardamos como campo para parar o som facilmente em onPause/onDestroy e nos listeners.
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_activity);

        TextView tvCallerName = findViewById(R.id.tvCallerName);
        ImageButton btnAnswer = findViewById(R.id.btnAnswer);
        ImageButton btnDecline = findViewById(R.id.btnDecline);

        // LER EXTRAS DO INTENT
        // Por que: o BroadcastReceiver colocou o nome do "chamador" em extras para que a Activity exiba.
        String callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);
        if (callerName != null && !callerName.isEmpty()) {
            tvCallerName.setText(callerName);
        }

        // ADICIONADO: iniciar o ringtone assim que a Activity for criada (logo após mostrar o nome).
        // Observação: o som pode ser silenciado por Do Not Disturb (DND) dependendo das configurações do dispositivo.
        startRingtone();

        btnAnswer.setOnClickListener(v -> {
            // parar o ringtone antes de abrir a tela de "chamada atendida"
            stopRingtone();

            Intent it = new Intent(this, TelaPreta.class);
            startActivity(it); //chama a tela toda preta
        });

        btnDecline.setOnClickListener(v -> {
            // parar o ringtone antes de simular recusa/ir para home.
            stopRingtone();

            // Go to home screen (simula botão home)
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    // Usa RingtoneManager para pegar o ringtone do usuário ( TYPE_RINGTONE ).
    // try/catch para evitar crashes se algo der errado com o Uri ou player.
    private void startRingtone() {
        try {
            // Pega URI do toque do sistema; se faltar, faz fallback para som de notificação
            //Um URI (Uniform Resource Identifier) é um identificador que aponta onde está um recurso
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) {
                // A partir da API 21 podemos definir atributos de áudio; bom pra categorizar como toque.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AudioAttributes attrs = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE) // indica propósito de toque
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build();
                    ringtone.setAudioAttributes(attrs);
                } else {
                    // Em APIs antigas, RingtoneManager usa streams automaticamente; manter comportamento padrão.
                    // Não mudamos o volume do usuário aqui.
                }
                ringtone.play();
            }
        } catch (Exception e) {
            // Falha ao tocar o ringtone: log/Toast podem ajudar no debug (não obrigatório).
            // Mantive comentário para você saber que tratamos a exceção.
            e.printStackTrace();
        }
    }

    // Chamamos em onPause/onDestroy e antes de iniciar outras Activities.
    private void stopRingtone() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
        } catch (Exception e) {
            // só logar para evitar crash se algo inesperado acontecer
            e.printStackTrace();
        } finally {
            // libera referência (boa prática)
            ringtone = null;
        }
    }

    // garantir que o ringtone pare quando a Activity não estiver visível.
    @Override
    protected void onPause() {
        super.onPause();
        stopRingtone();
    }

    // garantir parada também em onDestroy como redundância.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
    }
}