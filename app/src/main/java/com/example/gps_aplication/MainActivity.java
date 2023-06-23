package com.example.gps_aplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";

    EditText tel;
    Button btnentra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar si se ha guardado un número de teléfono en las preferencias
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "");


        if (phoneNumber.isEmpty()) {
            // No se ha guardado un número de teléfono, abrir la MainActivity 1
            setContentView(R.layout.activity_main);

            tel = (EditText) findViewById(R.id.tvtel);
            btnentra = (Button) findViewById(R.id.btnentra);

            btnentra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = tel.getText().toString();

                    // Guardar el número de teléfono en las preferencias
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_PHONE_NUMBER, phoneNumber);
                    editor.apply();

                    // Abrir la MainActivity 2
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            // Ya se ha guardado un número de teléfono, abrir la MainActivity 2
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            finish();
        }


    }


}