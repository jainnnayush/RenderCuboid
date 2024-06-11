package com.example.rendercubekotlin

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cuboid {

    private val vertexShaderCode =
        """
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 outColor;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            outColor = vColor;
        }
        """.trimIndent()

    private val fragmentShaderCode =
        """
        precision mediump float;
        varying vec4 outColor;
        void main() {
            gl_FragColor = outColor;
        }
        """.trimIndent()

    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private val program: Int

    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val colorStride = COLORS_PER_VERTEX * 4 // 4 bytes per color

    private val vertices = floatArrayOf(
        // Front face
        -0.25f,  0.1f,  0.70f,  // 0. top left
        -0.25f, -0.1f,  0.70f,  // 1. bottom left
        0.25f, -0.1f,  0.70f,  // 2. bottom right
        0.25f,  0.1f,  0.70f,  // 3. top right
        // Back face
        -0.25f,  0.1f, -0.70f,  // 4. top left
        -0.25f, -0.1f, -0.70f,  // 5. bottom left
        0.25f, -0.1f, -0.70f,  // 6. bottom right
        0.25f,  0.1f, -0.70f   // 7. top right
    )

    private val colors = floatArrayOf(
        // Colors for the vertices in the same order
        1.0f, 0.0f, 0.0f, 1.0f,  // Front face
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,  // Back face
        0.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,
    )

    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3,    // Front face
        4, 6, 5, 4, 7, 6,    // Back face
        0, 1, 5, 0, 5, 4,    // Left face
        2, 3, 7, 2, 7, 6,    // Right face
        0, 3, 7, 0, 7, 4,    // Top face
        1, 2, 6, 1, 6, 5     // Bottom face
    )

    init {
        // Initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer().apply {
            put(vertices)
            position(0)
        }

        // Initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer().apply {
            put(drawOrder)
            position(0)
        }

        // Initialize color byte buffer
        val cb = ByteBuffer.allocateDirect(colors.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer().apply {
            put(colors)
            position(0)
        }

        // Compile shaders and link program
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(program)

        // Get handle to vertex shader's vPosition member
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition").also { position ->
            // Enable a handle to the vertices
            GLES20.glEnableVertexAttribArray(position)
            // Prepare the vertex data
            GLES20.glVertexAttribPointer(position, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        }

        // Get handle to fragment shader's vColor member
        val colorHandle = GLES20.glGetAttribLocation(program, "vColor").also { color ->
            // Enable a handle to the colors
            GLES20.glEnableVertexAttribArray(color)
            // Prepare the color data
            GLES20.glVertexAttribPointer(color, COLORS_PER_VERTEX, GLES20.GL_FLOAT, false, colorStride, colorBuffer)
        }

        // Get handle to shape's transformation matrix
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix").also { matrix ->
            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(matrix, 1, false, mvpMatrix, 0)
        }

        // Draw the cuboid
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    companion object {
        const val COORDS_PER_VERTEX = 3
        const val COLORS_PER_VERTEX = 4

        fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}
