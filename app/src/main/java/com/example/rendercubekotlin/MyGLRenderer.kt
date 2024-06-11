package com.example.rendercubekotlin

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    private lateinit var cuboid: Cuboid

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    var roll: Float = 0f
    var pitch: Float = 0f
    var yaw: Float = 0f

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Initialize a cuboid
        cuboid = Cuboid()
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_BLEND)

    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1f, 0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Create a rotation for the cuboid
        val scratch = FloatArray(16)

        // Rotate the cuboid
        Matrix.setRotateM(rotationMatrix, 0, -roll, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(rotationMatrix, 0, yaw, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(rotationMatrix, 0, pitch, 0.0f, 0.0f, 1.0f)

        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw shape
        cuboid.draw(scratch)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // This projection matrix is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -2f, 2f, 2f, 10f)
    }

    fun updateRotation(roll: Float, pitch: Float, yaw: Float) {
        this.roll = roll
        this.pitch = pitch
        this.yaw = yaw
    }
}
