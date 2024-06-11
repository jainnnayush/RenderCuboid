package com.example.rendercubekotlin

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class SensorData : SensorEventListener {
    private var accelerometerData: FloatArray? = null
    private var filteredAccelValues = FloatArray(3) { 0f } // Initialize filtered values to 0
    private var gyroscopeData: FloatArray? = null
    private var lastTime: Long = 0

    var orientationListener: ((Float, Float, Float) -> Unit)? = null

    // Filter coefficients
    private val alpha = 0.02f
    private val lowPassAlpha=0.7f
    private val highPassAlpha = 0.7f
    private var prevGyroAngles = FloatArray(3) { 0f }
    private var highPassGyroAngles = FloatArray(3) { 0f }
    private var roll = 0f
    private var pitch = 0f
    private var yaw = 0f

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerData = event.values.clone()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeData = event.values.clone()
                calculateOrientation(event.timestamp)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun calculateOrientation(timestamp: Long) {
        val acc = accelerometerData
        val gyr = gyroscopeData
        if (acc != null && gyr != null) {
            if (lastTime == 0L) {
                lastTime = timestamp
                return
            }
            filteredAccelValues[0]=lowPassFilter(acc[0]-0.142950005f,filteredAccelValues[0])
            filteredAccelValues[1]=lowPassFilter(acc[1]-0.25395f,filteredAccelValues[1])
            filteredAccelValues[2]=lowPassFilter(acc[2]+0.061999f,filteredAccelValues[2])
            val dt = (timestamp - lastTime) * 1.0f / 1_000_000_000.0f
            lastTime = timestamp

            // Accelerometer angles
            val accelRoll = atan(filteredAccelValues[1].toDouble() / sqrt(filteredAccelValues[0].toDouble().pow(2.0) + filteredAccelValues[2].toDouble().pow(2.0))) * (180 / Math.PI)
            val accelPitch = atan(-filteredAccelValues[0].toDouble() / sqrt(filteredAccelValues[1].toDouble().pow(2.0) + filteredAccelValues[2].toDouble().pow(2.0))) * (180 / Math.PI)

            // Integrate the gyroscope data -> get angles
            val gyrRoll = (roll + gyr[0] * dt * (180 / Math.PI))
            val gyrPitch = (pitch + gyr[1] * dt * (180 / Math.PI))
            val gyrYaw = (yaw + gyr[2] * dt * (180 / Math.PI))

            highPassGyroAngles[0] = highPassFilter(gyrRoll.toFloat(), prevGyroAngles[0], highPassGyroAngles[0])
            highPassGyroAngles[1] = highPassFilter(gyrPitch.toFloat(), prevGyroAngles[1], highPassGyroAngles[1])
            //highPassGyroAngles[2] = highPassFilter(gyrYaw.toFloat(), prevGyroAngles[2], highPassGyroAngles[2])

            // updating previous gyroscope angles
            prevGyroAngles[0] = gyrRoll.toFloat()
            prevGyroAngles[1] = gyrPitch.toFloat()
            //prevGyroAngles[2] = gyrYaw.toFloat()

            // Apply complementary filter

            roll = (alpha * highPassGyroAngles[0] + (1 - alpha) * accelRoll).toFloat()
            pitch = (alpha * highPassGyroAngles[1] + (1 - alpha) * accelPitch).toFloat()
            yaw = gyrYaw.toFloat() // yaw is calculted using gyroscope data only

            orientationListener?.invoke(roll, pitch, yaw)
        }
    }

    private fun lowPassFilter(value: Float, prevValue: Float): Float {
        return prevValue * lowPassAlpha + value *(1-lowPassAlpha)
    }
    private fun highPassFilter(currentValue: Float, prevValue: Float, prevHighPassValue: Float): Float {
        return highPassAlpha * (prevHighPassValue + currentValue - prevValue)
    }
}


//package com.example.rendercubekotlin
//
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.util.Log
//import java.lang.Math.pow
//import kotlin.math.atan
//import kotlin.math.sqrt
//import kotlin.math.pow
//
//
//class SensorData : SensorEventListener {
//    var accelerometerData: FloatArray? = null
//    var gyroscopeData: FloatArray? = null
//
//    var orientationListener: ((Float, Float, Float) -> Unit)? = null
//
//    private val alpha = 0.95f // Smoothing factor for the complementary filter
//
//    private var smoothedAccelerometerData = FloatArray(3)
//    private var roll = 0f
//    private var pitch = 0f
//    private var yaw = 0f
//
//    private var timestamp: Long = 0
//
//    override fun onSensorChanged(event: SensorEvent) {
//        when (event.sensor.type) {
//            Sensor.TYPE_ACCELEROMETER -> {
//                accelerometerData = lowPassFilter(event.values.clone(), smoothedAccelerometerData)
//            }
//            Sensor.TYPE_GYROSCOPE -> {
//                gyroscopeData = event.values.clone()
//            }
//        }
//        calculateOrientation(event.timestamp)
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
//
//    private fun lowPassFilter(input: FloatArray, output: FloatArray?): FloatArray {
//        if (output == null) return input
//
//        for (i in input.indices) {
//            output[i] = alpha * output[i] + (1 - alpha) * input[i]
//        }
//        return output
//    }
//
//    private fun calculateOrientation(currentTimestamp: Long) {
//        accelerometerData?.let { acc ->
//            gyroscopeData?.let { gyr ->
//
//                val dt = if (timestamp != 0L) (currentTimestamp - timestamp) * (1.0f / 1000000000.0f) else 0f
//                timestamp = currentTimestamp
//
//                val accelRoll = atan(acc[1].toDouble() / sqrt(acc[0].toDouble().pow(2.0) + acc[2].toDouble().pow(2.0))) * (180 / Math.PI)
//                val accelPitch = atan(-acc[0].toDouble() / sqrt(acc[1].toDouble().pow(2.0) + acc[2].toDouble().pow(2.0))) * (180 / Math.PI)
//
//                roll = alpha * (roll + gyr[0] * dt) + (1 - alpha) * accelRoll.toFloat()
//                pitch = alpha * (pitch + gyr[1] * dt) + (1 - alpha) * accelPitch.toFloat()
//                yaw += gyr[2] * dt // Simplified placeholder for yaw
//
//                orientationListener?.invoke(roll, pitch, yaw)
//            }
//        }
//    }
//}

//package com.example.rendercubekotlin
//
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.util.Log
//import java.lang.Math.pow
//import kotlin.math.atan
//import kotlin.math.sqrt
//import kotlin.math.pow
//
//
//class SensorData : SensorEventListener {
//    var accelerometerData: FloatArray? = null
//    var gyroscopeData: FloatArray? = null
//
//    var orientationListener: ((Float,Float,Float) -> Unit)? = null
//
//    override fun onSensorChanged(event: SensorEvent) {
//        when (event.sensor.type) {
//            Sensor.TYPE_ACCELEROMETER -> {
//                accelerometerData = event.values.clone()
//            }
//            Sensor.TYPE_GYROSCOPE -> {
//                gyroscopeData = event.values.clone()
//            }
//        }
//        calculateOrientation()
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
//
//    }
//
//    private fun calculateOrientation() {
//        accelerometerData?.let { acc ->
//            gyroscopeData?.let { gyr ->
//
////                val orientation = "Orientation: \n" +
////                        "Accel: x=${acc[0]}, y=${acc[1]}, z=${acc[2]}\n" +
////                        "Gyro: x=${gyr[0]}, y=${gyr[1]}, z=${gyr[2]}"
////                Log or display the orientation as needed
////                println(orientation)
//
//                val roll = atan(acc[1].toDouble()/sqrt(pow(acc[0].toDouble(), 2.0)+pow(acc[2].toDouble(),
//                    2.0
//                ))) * (180 / Math.PI)
//                val pitch = atan(-acc[0].toDouble()/sqrt(pow(acc[1].toDouble(), 2.0)+pow(acc[2].toDouble(),
//                    2.0
//                ))) * (180 / Math.PI)
//                val yaw = gyr[2]  // Simplified placeholder
//                orientationListener?.invoke(roll.toFloat(),pitch.toFloat(),yaw)
//
//            }
//        }
//    }
//}