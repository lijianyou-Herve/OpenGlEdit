package com.example.opengledit.opengl.drawer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import com.example.opengledit.opengl.bean.BaseRenderBean
import com.example.opengledit.utils.OpenGLESUtils
import java.nio.FloatBuffer

open class BaseRender @JvmOverloads constructor(
    /**
     * Context
     */
    var context: Context,
    /**
     * 顶点着色器代码路径
     */
    var vertexFilename: String = "render/base/base/vertex.frag",
    /**
     * 片元着色器代码路径
     */
    var fragFilename: String = "render/base/base/frag.frag"
) : IDrawer {

    /**
     * 渲染数据
     */
    var renderBean: BaseRenderBean? = null

    /**
     * 顶点坐标
     */
    var vertexBuffer: FloatBuffer? = null

    /**
     * 纹理坐标
     */
    var coordinateBuffer: FloatBuffer? = null

    /**
     * 顶点坐标维数（即x, y, z）
     */
    var vertexSize = 2

    /**
     * 纹理坐标维数（即x, y, z）
     */
    var coordinateSize = 2

    /**
     * 顶点坐标步长（即维数 * 字节数）
     */
    var vertexStride = vertexSize * 4

    /**
     * 纹理坐标步长（即维数 * 字节数）
     */
    var coordinateStride = coordinateSize * 4

    /**
     * 顶点个数
     */
    var vertexCount = 4

    /**
     * 纹理点个数
     */
    var coordinateCount = 4

    /**
     * vertex shader
     */
    var vertexShader = 0

    /**
     * frag shader
     */
    var fragShader = 0

    /**
     * program
     */
    var program = 0

    /**
     * 纹理 id
     */
    var textureId = 0

    /**
     * fbo纹理id
     */
    var fboTextureId = 0

    /**
     * fbo id
     */
    var fboId = 0

    /**
     * vbo id
     */
    var vboId = 0

    /**
     * 尺寸
     */
    var width = 0
    var height = 0

    /**
     * 是否绑定Fbo
     */
    var isBindFbo = false

    /**
     * 着色器顶点坐标位置
     */
    var posLocation = 0

    /**
     * 着色器纹理坐标位置
     */
    var coordinateLocation = 0

    /**
     * 着色器纹理位置
     */
    var samplerLocation = 0

    /**
     * 是否执行了onCreate
     */
    var isCreate = false

    /**
     * 是否执行了onChange
     */
    var isChange = false

    override fun setVideoSize(videoW: Int, videoH: Int) {
        if (isCreate) {
            return
        }
        onCreatePre()
        onClearColor()
        onInitBlend()
        onInitVertexBuffer()
        onInitCoordinateBuffer()
        onInitVbo()
        onInitProgram()
        onCreateAfter()
        isCreate = true
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        if (isChange) {
            return
        }
        onChangePre()
        width = width
        height = height
        onViewport()
        onInitFbo()
        onChangeAfter()
        isChange = true
    }

    override fun draw() {
        if (!onReadyToDraw()) {
            return
        }
        onDrawPre()
        onClear()
        onUseProgram()
        onInitLocation()
        onBindFbo()
        onBindVbo()
        onActiveTexture(textureId)
        onEnableVertexAttributeArray()
        onSetVertexData()
        onSetCoordinateData()
        onSetOtherData()
        onDraw()
        onDisableVertexAttributeArray()
        onUnBind()
        onDrawAfter()
    }

    override fun release() {
        onDeleteProgram(program)
        onDeleteShader(vertexShader)
        onDeleteShader(fragShader)
        onDeleteTexture(textureId)
        onDeleteTexture(fboTextureId)
        onDeleteFbo(fboId)
        onDeleteVbo(vboId)
    }

    /**
     * 创建之前
     */
    fun onCreatePre() {}

    /**
     * 设置背景颜色
     */
    fun onClearColor() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
    }

    /**
     * 是否启用混色
     */
    fun onEnableBlend(): Boolean {
        return false
    }

    /**
     * 初始化混色
     */
    private fun onInitBlend() {
        if (!onEnableBlend()) {
            return
        }
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    /**
     * 初始化顶点坐标
     */
    fun onInitVertexBuffer() {
        vertexBuffer = OpenGLESUtils.getSquareVertexBuffer()
    }

    /**
     * 初始化纹理坐标
     */
    fun onInitCoordinateBuffer() {
        coordinateBuffer = if (isBindFbo) {
            OpenGLESUtils.getSquareCoordinateReverseBuffer()
        } else {
            OpenGLESUtils.getSquareCoordinateBuffer()
        }
    }

    /**
     * 初始化Vbo
     */
    fun onInitVbo() {
        vboId = OpenGLESUtils.getVbo(vertexBuffer, coordinateBuffer)
    }

    /**
     * 初始化Program
     */
    fun onInitProgram() {
        val vertexShaderCode = OpenGLESUtils.getShaderCode(context, vertexFilename)
        val fragShaderCode = OpenGLESUtils.getShaderCode(context, fragFilename)
        vertexShader = OpenGLESUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        fragShader = OpenGLESUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderCode)
        program = OpenGLESUtils.linkProgram(vertexShader, fragShader)
    }

    /**
     * 创建之后
     */
    fun onCreateAfter() {}

    /**
     * 设置尺寸之前
     */
    fun onChangePre() {}

    /**
     * 设置窗口尺寸
     */
    fun onViewport() {
        GLES20.glViewport(0, 0, width, height)
    }

    /**
     * 初始化Fbo
     */
    fun onInitFbo() {
        if (!isBindFbo) {
            return
        }
        val fboData = OpenGLESUtils.getFbo(width, height)
        fboId = fboData[0]
        fboTextureId = fboData[1]
    }

    /**
     * 设置尺寸之后
     */
    fun onChangeAfter() {}

    /**
     * 绘制之前的准备
     */
    fun onReadyToDraw(): Boolean {
        return true
    }

    /**
     * 绘制之前
     */
    fun onDrawPre() {}

    /**
     * 清屏
     */
    fun onClear() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    /**
     * 使用Program
     */
    fun onUseProgram() {
        GLES20.glUseProgram(program)
    }

    /**
     * 初始化着色器各个位置
     */
    fun onInitLocation() {
        posLocation = GLES20.glGetAttribLocation(program, "aPos")
        coordinateLocation = GLES20.glGetAttribLocation(program, "aCoordinate")
        samplerLocation = GLES20.glGetUniformLocation(program, "uSampler")
    }

    /**
     * 绑定Fbo
     */
    fun onBindFbo() {
        if (!isBindFbo) {
            return
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, fboTextureId, 0
        )
        GLES20.glViewport(0, 0, width, height)
    }

    /**
     * 绑定Vbo
     */
    fun onBindVbo() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
    }

    /**
     * 激活并绑定纹理
     */
    fun onActiveTexture(textureId: Int) {
        this.textureId = textureId
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(samplerLocation, 0)
    }

    /**
     * 启用顶点坐标
     */
    fun onEnableVertexAttributeArray() {
        GLES20.glEnableVertexAttribArray(posLocation)
        GLES20.glEnableVertexAttribArray(coordinateLocation)
    }

    /**
     * 设置顶点坐标
     */
    fun onSetVertexData() {
        GLES20.glVertexAttribPointer(posLocation, vertexSize, GLES20.GL_FLOAT, false, vertexStride, 0)
    }

    /**
     * 设置纹理坐标
     */
    fun onSetCoordinateData() {
        GLES20.glVertexAttribPointer(coordinateLocation, coordinateSize, GLES20.GL_FLOAT, false, coordinateStride, vertexBuffer!!.limit() * 4)
    }

    /**
     * 设置其他数据
     */
    fun onSetOtherData() {}

    /**
     * 绘制
     */
    fun onDraw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
    }

    /**
     * 禁用顶点坐标
     */
    fun onDisableVertexAttributeArray() {
        GLES20.glDisableVertexAttribArray(posLocation)
        GLES20.glDisableVertexAttribArray(coordinateLocation)
    }

    /**
     * 解除绑定
     */
    fun onUnBind() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    /**
     * 绘制之后
     */
    fun onDrawAfter() {}

    /**
     * 删除Program
     */
    fun onDeleteProgram(program: Int) {
        GLES20.glDeleteProgram(program)
    }

    /**
     * 删除Shader
     */
    fun onDeleteShader(shader: Int) {
        GLES20.glDeleteShader(shader)
    }

    /**
     * 删除纹理
     */
    fun onDeleteTexture(textureId: Int) {
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
    }

    /**
     * 删除Fbo
     */
    fun onDeleteFbo(fboId: Int) {
        GLES20.glDeleteFramebuffers(1, intArrayOf(fboId), 0)
    }

    /**
     * 删除Vbo
     */
    fun onDeleteVbo(vboId: Int) {
        GLES20.glDeleteBuffers(1, intArrayOf(vboId), 0)
    }

    fun updateRenderBean(renderBean: BaseRenderBean?) {
        this.renderBean = renderBean
    }

    private var mSurfaceTexture: SurfaceTexture? = null

    override fun setAlpha(alpha: Float) {}
    override fun setTextureID(id: Int) {
        textureId = id
        mSurfaceTexture = SurfaceTexture(id)
        mSftCb?.invoke(mSurfaceTexture!!)
    }

    private var mSftCb: ((SurfaceTexture) -> Unit)? = null

    override fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {
        mSftCb = cb
    }
}