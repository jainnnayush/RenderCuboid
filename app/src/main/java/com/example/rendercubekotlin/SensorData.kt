package com.example.rendercubekotlin

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import java.lang.Math.pow
import kotlin.math.atan
import kotlin.math.sqrt
import kotlin.math.pow

class SensorData : SensorEventListener {
    var accelerometerData: FloatArray? = null
    var gyroscopeData: FloatArray? = null

    var orientationListener: ((String) -> Unit)? = null

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerData = event.values.clone()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeData = event.values.clone()
            }
        }
        calculateOrientation()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    private fun calculateOrientation() {
        accelerometerData?.let { acc ->
            gyroscopeData?.let { gyr ->

                val orientation = "Orientation: \n" +
                        "Accel: x=${acc[0]}, y=${acc[1]}, z=${acc[2]}\n" +
                        "Gyro: x=${gyr[0]}, y=${gyr[1]}, z=${gyr[2]}"
                // Log or display the orientation as needed
                println(orientation)

                val roll = atan(acc[1].toDouble()/sqrt(pow(acc[0].toDouble(), 2.0)+pow(acc[2].toDouble(),
                    2.0
                ))) * (180 / Math.PI)
                val pitch = atan(-acc[0].toDouble()/sqrt(pow(acc[1].toDouble(), 2.0)+pow(acc[2].toDouble(),
                    2.0
                ))) * (180 / Math.PI)
                val yaw = gyr[2]  // Simplified placeholder

                val orientation2 = "Orientation2: \n" +
                        "Roll: $roll\n" +
                        "Pitch: $pitch\n" +
                        "Yaw: $yaw"

                // Call the orientation listener with the calculated orientation
                orientationListener?.invoke(orientation2)
            }
        }
    }
}
