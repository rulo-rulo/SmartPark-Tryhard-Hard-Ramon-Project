package com.example.smartpark;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReservaNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String parkingName = intent.getStringExtra("nombreParking");

        // Crear canal de notificaciones (solo 1 vez en Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reserva_channel",
                    "Notificaciones de reservas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Avisos sobre reservas finalizadas");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Intent al abrir la notificación → abrir HomePage
        Intent openIntent = new Intent(context, HomePage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reserva_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tu reserva ha finalizado")
                .setContentText("Tu reserva en " + parkingName + " ha terminado. ¡Esperamos verte pronto!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Mostrar notificación
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
