package com.example.gps_aplication;

import android.Manifest;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity2 extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";

    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private static final int PERMISSION_REQUEST_LOCATION = 2;
    private static final int PERMISSION_REQUEST_CALL_PHONE = 3;

    private CountDownTimer callTimer;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private TextView telefono;
    private boolean mandomesaje = false;

    // DevicePolicyManager y ComponentName para el bloqueo de pantalla
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Obtener el número de teléfono guardado en las preferencias
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "");

        // Configurar el número de teléfono en el TextView
        telefono = findViewById(R.id.tvtel);
        telefono.setText(phoneNumber);

        // Inicializar el DevicePolicyManager y el ComponentName
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        callTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // El temporizador está en progreso, no es necesario realizar ninguna acción aquí
            }

            @Override
            public void onFinish() {
                // Temporizador finalizado, obtener y enviar la ubicación
                getLocationAndSendSMS();
                // Realizar la llamada
                makePhoneCall();


                // Bloquear la pantalla y silenciar el micrófono
                lockScreenAndSilenceMicrophone();
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el temporizador cuando la actividad se destruye
        callTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Iniciar el temporizador cuando la actividad se reanuda
        callTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar el temporizador cuando la actividad se pausa
        callTimer.cancel();
    }

    private void getLocationAndSendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Obtener las coordenadas de ubicación en tiempo real
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Crear el mensaje con las coordenadas de ubicación
                    String message = "Mis coordenadas son: " + latitude + ", " + longitude;

                    // Enviar el mensaje por SMS
                    sendSMS(message);


                }

                @Override
                public void onStatusChanged(String provide, int status, Bundle extras) {
                    // Implementar si es necesario
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // Implementar si es necesario
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // Implementar si es necesario
                }
            };

            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
    }

    private void sendSMS(String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "");

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
        }
    }

    private void makePhoneCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Obtener el número de teléfono para realizar la llamada
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "");

            // Crear un intent para realizar la llamada telefónica
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
        }
    }

    private void lockScreenAndSilenceMicrophone() {
        // Verificar si la aplicación tiene los privilegios de administrador de dispositivos
        boolean isAdminActive = devicePolicyManager.isAdminActive(adminComponent);

        if (isAdminActive) {
            // Bloquear la pantalla
            devicePolicyManager.lockNow();

            // Silenciar el micrófono (requiere permisos adicionales)
            // ...
        } else {
            // Solicitar activar los privilegios de administrador de dispositivos
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Por favor, activa los privilegios de administrador de dispositivos");
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSendSMS();
            } else {
                Toast.makeText(this, "Permiso SEND_SMS denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSendSMS();
            } else {
                Toast.makeText(this, "Permiso ACCESS_FINE_LOCATION denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permiso CALL_PHONE denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class MyDeviceAdminReceiver extends DeviceAdminReceiver {
        // Esta clase está vacía, no se requieren implementaciones adicionales
    }
}
