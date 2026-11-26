package mx.edu.utez.memorias.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

@Composable
fun ShakeDetector(
    onShake: () -> Unit
) {
    val context = LocalContext.current
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(context) {
        // TODO: Obten el manejador de sensor necesario
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // TODO: Obten el sensor acelerometro para usarse
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShakeTimestamp = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val values = event?.values ?: return
                // TODO: Completa el algoritmo accediendo a los datos del acelerometrico segun
                //  el sentido de la variable: x, y o z. (nota solo reemplaza los simbolos de "?")
                val gX = ? / SensorManager.GRAVITY_EARTH
                val gY = ? / SensorManager.GRAVITY_EARTH
                val gZ = ? / SensorManager.GRAVITY_EARTH
                val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

                if (gForce > 2.4f) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTimestamp > 1500L) {
                        lastShakeTimestamp = now
                        currentOnShake()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}
