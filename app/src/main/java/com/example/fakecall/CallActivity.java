package com.example.fakecall;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CallActivity extends AppCompatActivity {

    public static final String EXTRA_CALLER_NAME = "extra_caller_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_activity);

        TextView tvCallerName = findViewById(R.id.tvCallerName);
        ImageButton btnAnswer = findViewById(R.id.btnAnswer);
        ImageButton btnDecline = findViewById(R.id.btnDecline);

        String callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);
        if (callerName != null && !callerName.isEmpty()) {
            tvCallerName.setText(callerName);
        }

        btnAnswer.setOnClickListener(v -> {
            Intent it = new Intent(this, TelaPreta.class);
            startActivity(it);
        });

        btnDecline.setOnClickListener(v -> {
            // Go to home screen (simula botão home)
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}

