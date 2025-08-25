package com.example.fakecall;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS = "fakecall_prefs";
    public static final String EXTRA_SLOT = "extra_slot";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_DATETIME = "extra_datetime";

    private TextView tvSlot1, tvSlot2, tvSlot3;
    private ImageButton btnEdit1, btnEdit2, btnEdit3;
    private ImageButton btnDelete1, btnDelete2, btnDelete3;
    private View fabAdd;

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSlot1 = findViewById(R.id.tvSlot1);
        tvSlot2 = findViewById(R.id.tvSlot2);
        tvSlot3 = findViewById(R.id.tvSlot3);

        btnEdit1 = findViewById(R.id.btnEdit1);
        btnEdit2 = findViewById(R.id.btnEdit2);
        btnEdit3 = findViewById(R.id.btnEdit3);

        btnDelete1 = findViewById(R.id.btnDelete1);
        btnDelete2 = findViewById(R.id.btnDelete2);
        btnDelete3 = findViewById(R.id.btnDelete3);

        fabAdd = findViewById(R.id.fabAdd);

        // Verificação de permissões/estado do AlarmManager em Android 12+ (API 31)
        //  Desde Android 12 existe política para "exact alarms" — o app
        // pode precisar que o usuário autorize explicitamente para que alarms exatos funcionem.
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent i = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(i);
            }
        }
        // launcher moderno (substitui startActivityForResult) - serve para receber os dados da EditActivity
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                res -> {
                    if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                        Intent data = res.getData();
                        int slot = data.getIntExtra(EXTRA_SLOT, -1);
                        String name = data.getStringExtra(EXTRA_NAME);
                        String datetime = data.getStringExtra(EXTRA_DATETIME);
                        // validação básica: slot válido e strings não nulas
                        // evita que o user crie mais de 3 chamadas ou coloque nome vazio ou sem a data
                        if (slot >= 1 && slot <= 3 && name != null && datetime != null) {
                            saveSlot(slot, name, datetime);
                            loadSlotsToUI();
                            Toast.makeText(this, "Chamada salva", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        loadSlotsToUI();
        // Listeners: abrir tela de edição para cada slot
        btnEdit1.setOnClickListener(v -> openEditActivity(1));
        btnEdit2.setOnClickListener(v -> openEditActivity(2));
        btnEdit3.setOnClickListener(v -> openEditActivity(3));
        // Deletar slot quando pressionado
        btnDelete1.setOnClickListener(v -> deleteSlot(1));
        btnDelete2.setOnClickListener(v -> deleteSlot(2));
        btnDelete3.setOnClickListener(v -> deleteSlot(3));
        // FAB: encontra o primeiro slot livre para criar uma nova chamada
        fabAdd.setOnClickListener(v -> {
            int free = firstFreeSlot();
            if (free == -1) {
                Toast.makeText(this, "Máximo de 3 chamadas agendadas", Toast.LENGTH_SHORT).show();
                return;
            }
            openEditActivity(free);
        });
    }

    // Abre EditActivity passando slot e valores já salvos (para edição)
    private void openEditActivity(int slotIndex) {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String name = sp.getString("call_name_" + slotIndex, "");
        String datetime = sp.getString("call_time_" + slotIndex, "");

        Intent it = new Intent(this, EditActivity.class);
        it.putExtra(EXTRA_SLOT, slotIndex);
        it.putExtra(EXTRA_NAME, name);
        it.putExtra(EXTRA_DATETIME, datetime);
        editLauncher.launch(it);
    }

    // Lê os 3 slots das SharedPreferences e mostra na UI
    private void loadSlotsToUI() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String n1 = sp.getString("call_name_1", "");
        String t1 = sp.getString("call_time_1", "");
        String n2 = sp.getString("call_name_2", "");
        String t2 = sp.getString("call_time_2", "");
        String n3 = sp.getString("call_name_3", "");
        String t3 = sp.getString("call_time_3", "");
        // Se vazio, mostra string padrão; caso contrário mostra "Nome - data/hora"
        tvSlot1.setText(n1.isEmpty() ? getString(R.string.call_1) : (n1 + " - " + t1));
        tvSlot2.setText(n2.isEmpty() ? getString(R.string.call_2) : (n2 + " - " + t2));
        tvSlot3.setText(n3.isEmpty() ? getString(R.string.call_3) : (n3 + " - " + t3));
    }

    // Retorna o primeiro slot livre (1..3) ou -1 se cheio
    private int firstFreeSlot() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (sp.getString("call_name_1", "").isEmpty()) return 1;
        if (sp.getString("call_name_2", "").isEmpty()) return 2;
        if (sp.getString("call_name_3", "").isEmpty()) return 3;
        return -1;
    }

    // Salva os dados do slot e agenda o alarme se a data for futura
    private void saveSlot(int slot, String name, String datetime) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("call_name_" + slot, name)
                .putString("call_time_" + slot, datetime)
                .apply();

        cancelAlarm(slot);
        long when = parseDate(datetime);
        if (when > System.currentTimeMillis()) {
            scheduleAlarm(slot, name, when);
        }
    }

    // Converte "dd/MM/yyyy HH:mm" -> epoch millis. Retorna -1 em erro.
    //epoch millis é o numero de milissegundos desde a "Época Unix" (01/01/1970 00:00:00 UTC).
    //Isso é útil porque permite comparar datas como números inteiros
    private long parseDate(String dt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            Date d = sdf.parse(dt);
            return d != null ? d.getTime() : -1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    //Função que agenda o alarme respeitando diferentes APIs (fazer alarmes exatos / allow while idle)
    private void scheduleAlarm(int slot, String name, long whenMillis) {
        if (whenMillis <= System.currentTimeMillis()) return; // não agenda no passado
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = buildPendingIntent(slot, name);

        try {
            if (android.os.Build.VERSION.SDK_INT >= 31) { // Lida com diferenças entre versões: Android 12+ tem política de "exact alarm"
                // Android 12+ tem API canScheduleExactAlarms()
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pi);
                } else {
                    // fallback não-exato para evitar crash/exception
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pi);
                }
//Quando o celular fica parado, sem carregar, com a tela desligada, o Android entra em Doze Mode.
//Nesse estado, o sistema suspende ou atrasa várias tarefas em segundo plano (como alarmes, sincronizações e jobs) para economizar bateria.
//A ideia desse IF é fazer a chamada tocar mesmo em doze mode.
            } else if (android.os.Build.VERSION.SDK_INT >= 23) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, whenMillis, pi);
            }
        } catch (SecurityException e) {
            // Proteção extra: se o sistema reclamar de permissão, usa fallback inexact
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pi);
        }
    }

    // Cria um PendingIntent que dispara o BroadcastReceiver quando chega a hora da chamada
    private PendingIntent buildPendingIntent(int slot, String name) {
        Intent i = new Intent(this, CallAlarmReceiver.class)
                .setAction("FAKECALL_ALARM_" + slot)
                .putExtra("caller", name);
        return PendingIntent.getBroadcast(
                this,
                1000 + slot,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    //cancela o alarme
    private void cancelAlarm(int slot) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, CallAlarmReceiver.class).setAction("FAKECALL_ALARM_" + slot);
        PendingIntent pi = PendingIntent.getBroadcast(
                this, 1000 + slot, i,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove("call_when_" + slot).apply();
    }

    // Remove os dados do slot e cancela alarme se necessário
    private void deleteSlot(int slotIndex) {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (sp.getString("call_name_" + slotIndex, "").isEmpty()) {
            Toast.makeText(this, "Slot vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        cancelAlarm(slotIndex);
        sp.edit()
                .remove("call_name_" + slotIndex)
                .remove("call_time_" + slotIndex)
                .apply();
        loadSlotsToUI();
        Toast.makeText(this, "Chamada removida", Toast.LENGTH_SHORT).show();
    }
}
