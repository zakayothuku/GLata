package com.yalantis.glata.shader

import android.opengl.GLES20
import com.yalantis.glata.core.RendererParams
import com.yalantis.glata.core.model.ModelParams
import com.yalantis.glata.core.scene.SceneParams
import com.yalantis.glata.core.shader.BaseShader
import com.yalantis.glata.util.Utils

class FloatingAlphaTextureShader(
        val sourceAlpha: SourceAlpha = SourceAlpha.SRC_ALPHA,
        val destination: Destination = Destination.ONLY_ALPHA) : BaseShader() {

    private var uAlphaHandle: Int = 0

    override fun initShaders() {
        attributes = arrayOf("a_Position", "a_TexCoordinate")

        vertexShader =
                "uniform mat4 u_MVPMatrix; \n" + // A constant representing the combined model/view/projection matrix.
                "attribute vec4 a_Position; \n" + // Per-vertex position information we will pass in.
                "attribute vec2 a_TexCoordinate; \n" + // Per-vertex texture coordinate information we will pass in.
                "varying vec2 v_TexCoordinate; \n" + // This will be passed into the fragment shader.

                "void main() { \n" +
                "	  v_TexCoordinate = a_TexCoordinate; \n" + // Pass through the texture coordinate.
                "    gl_Position = u_MVPMatrix * a_Position; \n" +
                "} \n"


        fragmentShader =
                "precision mediump float; \n" +
                "uniform sampler2D u_Texture; \n" + // The input texture.
                "uniform float u_Alpha; \n" + // new alpha value
                "varying vec2 v_TexCoordinate; \n" + // Interpolated texture coordinate per fragment.

                "void main() { \n" +
                "		gl_FragColor = texture2D(u_Texture, v_TexCoordinate); \n" +
                "       ${destination.value} *= ${sourceAlpha.value}; \n" +
                "}"
    }

    override fun setShaderParams(
            rendererParams: RendererParams, modelParams: ModelParams, sceneParams: SceneParams) {
        setMvpMatrixHandle(modelParams, sceneParams)
        modelParams.shaderVars?.let { setAlpha(it.alpha) }
    }

    fun setAlpha(alpha: Float) {
        GLES20.glUniform1f(uAlphaHandle, Utils.clamp(0f, 1f, alpha))
    }

    override fun setVariableHandles() {
        uMvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix")
        uAlphaHandle = GLES20.glGetUniformLocation(programHandle, "u_Alpha")
        aPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        aTexCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate")
    }

    enum class SourceAlpha(val value: String) {
        SRC_ALPHA ("u_Alpha"),
        ONE_MINUS_SRC_ALPHA ("1.0 - u_Alpha")
    }

    enum class Destination(val value: String) {
        ONLY_ALPHA ("gl_FragColor.a"),
        FULL_COLOR ("gl_FragColor")
    }
}