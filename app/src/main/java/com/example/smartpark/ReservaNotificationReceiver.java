package com.example.smartpark;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class ReservaNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String mensaje = intent.getStringExtra("mensaje");
        String fecha = intent.getStringExtra("fecha");

        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificacion =
                new NotificationCompat.Builder(context, "reservas_parking")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Aviso de reserva 🚗")
                        .setContentText(mensaje + " (" + fecha + ")")
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(),
                notificacion.build());
    }
}