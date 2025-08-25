package com.example.fakecall;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private EditText etName, etDateTime;
    private Button btnSave, btnCancel;

    private Calendar chosenCalendar = Calendar.getInstance(); // Usar Calendar facilita setar ano/mês/dia/hora/minuto separadamente.
    private int slotIndex = 1; // // slotIndex: qual slot (1..3) estamos editando. Vem da MainActivity.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        etName = findViewById(R.id.etName);
        etDateTime = findViewById(R.id.etDateTime);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Ler dados enviados pela MainActivity (se estivermos editando um slot existente).
        Intent incoming = getIntent();
        if (incoming != null) {
            slotIndex = incoming.getIntExtra(MainActivity.EXTRA_SLOT, 1);
            String name = incoming.getStringExtra(MainActivity.EXTRA_NAME);
            String datetime = incoming.getStringExtra(MainActivity.EXTRA_DATETIME);
            if (name != null) etName.setText(name);
            if (datetime != null && !datetime.isEmpty()) {
                etDateTime.setText(datetime);
                // opcional: tente parse para chosenCalendar (omiti para simplicidade)
            }
        }

         // Quando o usuário clicar no campo de data/hora, abrimos o DatePicker.
        etDateTime.setOnClickListener(v -> openDatePicker());

        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            String datetime = etDateTime.getText() == null ? "" : etDateTime.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Informe um nome");
                etName.requestFocus();
                return;
            }
            if (datetime.isEmpty()) {
                etDateTime.setError("Selecione data e hora");
                etDateTime.requestFocus();
                return;
            }
            // Se passou nas validações, montamos um Intent de resultado com os dados
            Intent result = new Intent();
            result.putExtra(MainActivity.EXTRA_SLOT, slotIndex);
            result.putExtra(MainActivity.EXTRA_NAME, name);
            result.putExtra(MainActivity.EXTRA_DATETIME, datetime);
            setResult(Activity.RESULT_OK, result);
            finish();
        });
    }


    // Abre o DatePicker com o ano/mês/dia do chosenCalendar.
    private void openDatePicker() {
        int y = chosenCalendar.get(Calendar.YEAR);
        int m = chosenCalendar.get(Calendar.MONTH);
        int d = chosenCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            chosenCalendar.set(Calendar.YEAR, year);
            chosenCalendar.set(Calendar.MONTH, month);
            chosenCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            openTimePicker();
        }, y, m, d);

        dp.show();
    }

    //Abre o timePicker
    private void openTimePicker() {
        int hour = chosenCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = chosenCalendar.get(Calendar.MINUTE);

        TimePickerDialog tp = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            chosenCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            chosenCalendar.set(Calendar.MINUTE, minute1);
            chosenCalendar.set(Calendar.SECOND, 0);
            updateDateTimeText();
        }, hour, minute, true);

        tp.show();
    }

    // Formata chosenCalendar para "dd/MM/yyyy HH:mm" e coloca no etDateTime para exibição.
    // Por que: esse formato é o que o MainActivity espera parsear depois para epoch millis.
    private void updateDateTimeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formatted = sdf.format(chosenCalendar.getTime());
        etDateTime.setText(formatted);
    }
}
