package com.example.rendercubekotlin


import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cube {

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
        -0.25f,  0.25f,  0.25f,  // 0. top left
        -0.25f, -0.25f,  0.25f,  // 1. bottom left
        0.25f, -0.25f,  0.25f,  // 2. bottom right
        0.25f,  0.25f,  0.25f,  // 3. top right
        // Back face
        -0.25f,  0.25f, -0.25f,  // 4. top left
        -0.25f, -0.25f, -0.25f,  // 5. bottom left
        0.25f, -0.25f, -0.25f,  // 6. bottom right
        0.25f,  0.25f, -0.25f   // 7. top right
    )

    private val colors = floatArrayOf(
        // Colors for the vertices in the same order
        1.0f, 0.0f, 0.0f, 1.0f,  // Red
        0.0f, 1.0f, 0.0f, 1.0f,  // Green
        0.0f, 0.0f, 1.0f, 1.0f,  // Blue
        1.0f, 1.0f, 0.0f, 1.0f,  // Yellow
        1.0f, 0.0f, 1.0f, 1.0f,  // Magenta
        0.0f, 1.0f, 1.0f, 1.0f,  // Cyan
        0.5f, 0.5f, 0.5f, 1.0f,  // Gray
        1.0f, 1.0f, 1.0f, 1.0f   // White
    )

    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3,  // Front face
        4, 5, 6, 4, 6, 7,  // Back face
        0, 1, 5, 0, 5, 4,  // Left face
        2, 3, 7, 2, 7, 6,  // Right face
        0, 3, 7, 0, 7, 4,  // Top face
        1, 2, 6, 1, 6, 5   // Bottom face
    )

    init {
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        val cb = ByteBuffer.allocateDirect(colors.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer()
        colorBuffer.put(colors)
        colorBuffer.position(0)

        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )

        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(
            colorHandle, COLORS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            colorStride, colorBuffer
        )

        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT, drawListBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val COLORS_PER_VERTEX = 4

        fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}


