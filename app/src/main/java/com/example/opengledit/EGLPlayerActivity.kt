package com.example.opengledit

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.example.opengledit.databinding.ActivityEglPlayerBinding
import com.example.opengledit.media.BaseDecoder
import com.example.opengledit.media.DefDecoderStateListener
import com.example.opengledit.media.Frame
import com.example.opengledit.media.decoder.AudioDecoder
import com.example.opengledit.media.decoder.VideoDecoder
import com.example.opengledit.opengl.drawer.VideoDrawer
import com.example.opengledit.opengl.egl.CustomerGLRenderer
import com.example.opengledit.utils.SPUtils
import java.util.concurrent.Executors


/**
 * 使用自定义的OpenGL（EGL+Thread）渲染器，渲染多个视频画面的播放器
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @since LearningVideo
 * @version LearningVideo
 * @Datetime 2019-10-26 21:07
 *
 */
class EGLPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEglPlayerBinding
    private val path = SPUtils.get().getString("video1")
    private val path2 = SPUtils.get().getString("video2")

    private val threadPool = Executors.newFixedThreadPool(10)

    private var mRenderer = CustomerGLRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEglPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFirstVideo()
        initSecondVideo()
        setRenderSurface()
    }

    private fun initFirstVideo() {
        val drawer = VideoDrawer()
        drawer.setVideoSize(1080,1920, )
        drawer.getSurfaceTexture {
            initPlayer(path, Surface(it), true)
        }
        mRenderer.addDrawer(drawer)
    }

    private fun initSecondVideo() {
        val drawer = VideoDrawer()
        drawer.setAlpha(0.5f)
        drawer.setVideoSize(1080,1920, )
        drawer.getSurfaceTexture {
            initPlayer(path2, Surface(it), false)
        }
        mRenderer.addDrawer(drawer)

        Handler().postDelayed({
            drawer.scale(0.5f, 0.5f)
        }, 1000)
    }

    private fun initPlayer(path: String, sf: Surface, withSound: Boolean) {
        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)
        videoDecoder.goOn()
        videoDecoder.setStateListener(object : DefDecoderStateListener {
            override fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame) {
                mRenderer.notifySwap(frame.bufferInfo.presentationTimeUs)
            }
        })

        if (withSound) {
            val audioDecoder = AudioDecoder(path)
            threadPool.execute(audioDecoder)
            audioDecoder.goOn()
        }
    }

    private fun setRenderSurface() {
        mRenderer.setSurface(binding.sfv)
    }
}