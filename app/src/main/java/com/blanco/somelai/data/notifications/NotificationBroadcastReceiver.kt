package com.blanco.somelai.data.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.blanco.somelai.R
import com.blanco.somelai.ui.home.HomeActivity


// Todo para notificaciones. Lo usaremos? o seran push desde firebase?
class NotificationBroadcastReceiver : BroadcastReceiver() {
    // Nos traemos el channel ID
    private val channelId = "SOMMELAI"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            //Llamamos a la función de mostrar la notificación y ahora le pasámos como parámetro el contexto
            showWelcomeNotificationWithAction(context)
        }
    }

    //Cambiamos todos los this por el contexto que recibimos por parámetro
    private fun showWelcomeNotificationWithAction(context: Context) {
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        var notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("¡Notificación con acción!")
            .setContentText("¡Qué bueno verte de nuevo! \nAbre la Home.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            //Añadimos esto al nuestra notificación
            .build()

        val notificationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        if (notificationPermission == PackageManager.PERMISSION_GRANTED) {
            //Tenemos que cambiar el manager
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            with(manager) {
                //borramos el .build()
                notify(3, notificationBuilder)
            }
        }
    }
}