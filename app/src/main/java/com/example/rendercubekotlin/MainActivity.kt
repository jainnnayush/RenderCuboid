package com.example.rendercubekotlin

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var sensorData: SensorData
    private lateinit var glSurfaceView: MyGLSurfaceView
    private lateinit var renderer: MyGLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize GLSurfaceView and Renderer
        glSurfaceView = MyGLSurfaceView(this, null)
        renderer = glSurfaceView.renderer as MyGLRenderer
        setContentView(glSurfaceView)

        // Initialize SensorManager and sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        sensorData = SensorData()

        // Set up sensor data listener
        sensorData.orientationListener = { roll, pitch, yaw ->
            renderer.updateRotation(roll, pitch, yaw)
            glSurfaceView.requestRender()
        }

        // Register the sensor listeners
        sensorManager.registerListener(sensorData, accelerometer, 5000)
        sensorManager.registerListener(sensorData, gyroscope, 5000)
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to save battery
        sensorManager.unregisterListener(sensorData)
    }

    override fun onResume() {
        super.onResume()
        // Re-register the sensor listeners
        sensorManager.registerListener(sensorData, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorData, gyroscope, SensorManager.SENSOR_DELAY_GAME)
    }
}
