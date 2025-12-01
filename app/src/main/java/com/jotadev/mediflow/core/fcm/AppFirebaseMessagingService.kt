package com.jotadev.mediflow.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.Intent
import com.jotadev.mediflow.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jotadev.mediflow.R
import com.jotadev.mediflow.core.events.AppEvents

class AppFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        // Aquí podrías enviar el token al backend si es necesario
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Mensaje FCM recibido desde: ${remoteMessage.from}")
        Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
        Log.d(TAG, "Notificación: ${remoteMessage.notification}")

        // Tipo de mensaje y detalles incluidos en data payload
        val messageType = remoteMessage.data["type"] ?: ""
        val horaEntrada = remoteMessage.data["hora_entrada"] ?: ""
        val horaSalida = remoteMessage.data["hora_salida"] ?: ""
        val horaRefrigerio = remoteMessage.data["hora_refrigerio"] ?: ""
        val diaSemana = remoteMessage.data["dia_semana"] ?: ""
        val turno = remoteMessage.data["turno"] ?: ""
        Log.d(TAG, "Tipo de mensaje: $messageType, entrada=$horaEntrada, salida=$horaSalida, refr=$horaRefrigerio, dia=$diaSemana, turno=$turno")

        val isAsignado = messageType == "HORARIO_ASIGNADO"
        val isActualizado = messageType == "HORARIO_ACTUALIZADO"

        if (isAsignado || isActualizado) {
            val title = when {
                remoteMessage.notification?.title != null -> remoteMessage.notification!!.title!!
                isAsignado -> "Horario asignado"
                else -> "Horario actualizado"
            }

            // Construir cuerpo detallado
            val partes = mutableListOf<String>()
            if (horaEntrada.isNotBlank()) partes.add("Entrada: $horaEntrada")
            if (horaSalida.isNotBlank()) partes.add("Salida: $horaSalida")
            if (horaRefrigerio.isNotBlank()) partes.add("Refrigerio: $horaRefrigerio")
            val baseBody = if (partes.isNotEmpty()) partes.joinToString(separator = " • ")
                else if (isAsignado) "Se te ha asignado un nuevo horario"
                else "Tu horario fue actualizado"
            val prefix = if (diaSemana.isNotBlank()) "($diaSemana${if (turno.isNotBlank()) ", $turno" else ""}) " else ""
            val body = prefix + baseBody

            // Emitir evento para refrescar horarios en HomeViewModel
            AppEvents.emit("horario_asignado")
            Log.d(TAG, "Evento de actualización de horario emitido para refrescar UI")

            showNotification(
                title = title,
                body = body
            )
        } else {
            Log.w(TAG, "Tipo de mensaje no reconocido o sin soporte: $messageType")
        }
    }

    private fun showNotification(title: String, body: String) {
        Log.d(TAG, "Mostrando notificación: $title - $body")
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mediflow_notifications"

        // Crear canal de notificación (Android 8.0+)
        val channel = NotificationChannel(
            channelId,
            "MediFlow Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        // Crear notificación
        // Intent para abrir Home al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("nav", "home")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notificación mostrada con ID: $notificationId")
    }
}