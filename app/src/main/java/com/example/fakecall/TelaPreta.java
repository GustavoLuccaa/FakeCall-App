package com.example.fakecall;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class TelaPreta extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Deixa fullscreen com fundo preto
        View blackView = new View(this);
        blackView.setBackgroundColor(0xFF000000);
        setContentView(blackView); //setContentView(blackView) define essa view como o conteúdo da Activity.
        //Ou seja: em vez de carregar um layout.xml, você passa diretamente uma View criada no código.

        // Esconde barras
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
