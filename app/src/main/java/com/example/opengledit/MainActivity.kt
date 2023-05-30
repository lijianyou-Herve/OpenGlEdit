package com.example.opengledit

import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.lifecycle.lifecycleScope
import com.example.opengledit.base.BaseActivity
import com.example.opengledit.databinding.ActivityMainBinding
import com.example.opengledit.ext.Ext.io
import com.example.opengledit.media.Frame
import com.example.opengledit.media.decode.BaseDecoder
import com.example.opengledit.media.decode.DecoderListener
import com.example.opengledit.media.encode.BaseEncoder
import com.example.opengledit.opengl.drawer.SoulVideoDrawer
import com.example.opengledit.opengl.drawer.VideoDrawer
import com.example.opengledit.opengl.render.SimpleRender
import com.example.opengledit.utils.SPUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BaseActivity() {

    private lateinit var videoEncoder: BaseEncoder
    private val TAG: String = "MainActivity"

    //自定义的OpenGL渲染器，详情请继续往下看
    private lateinit var binding: ActivityMainBinding

    private val mRender = SimpleRender()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.video1.setText(SPUtils.get().getString("video1"))
        binding.video2.setText(SPUtils.get().getString("video2"))

        binding.btnPickVideo1.setOnClickListener {
            binding.start.setTag(it)
            openFile()
        }

        binding.btnPickVideo2.setOnClickListener {
            binding.start.setTag(it)
            openFile()
        }

        binding.start.setOnClickListener {

//            val video1 = binding.video1.text.toString()
//            val file = File(video1)
//            if (file.exists()) {
//                addVideo(video1)
//            }
        }


        val video1 = binding.video1.text.toString()
        val file = File(video1)
        if (file.exists()) {
            addVideo(video1)
//            initVideoEncoder()
        }

    }

    private fun addVideo(path: String) {
        val drawer = VideoDrawer() // SoulVideoDrawer()
//        val drawer = SoulVideoDrawer() // SoulVideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initVideoDecoder(path, Surface(it))
        }
        mRender.addDrawer(drawer)

        binding.glSurface.setEGLContextClientVersion(2)
        binding.glSurface.setRenderer(mRender)
    }

    private fun initVideoDecoder(path: String, surface: Surface) {
        val videoDecoder = BaseDecoder(path, null, surface)

        videoDecoder.mDecoderListener = object : DecoderListener {
            override fun decodeOneFrame(frame: Frame) {

//                videoEncoder.encodeOneFrame(frame)
                Log.i(TAG, "decodeOneFrame: frame = $frame")

            }

            override fun decoderFinish() {

                Log.i(TAG, "decoderFinish")


            }

        }


        lifecycleScope.launch {
            io {
                delay(200)
                videoDecoder.start()
            }
        }

    }


    private fun initVideoEncoder() {
        // 视频编码器
        videoEncoder = BaseEncoder( 1920, 1080)

//        mRender.setRenderMode(CustomerGLRenderer.RenderMode.RENDER_WHEN_DIRTY)
//        mRender.setSurface(videoEncoder.getEncodeSurface()!!, 1920, 1080)

        lifecycleScope.launch {
            io {
                videoEncoder.loopEncode()
            }
        }

    }

    override fun onFileCallback(fileList: ArrayList<String>) {
        super.onFileCallback(fileList)

        if (binding.start.tag == binding.btnPickVideo1) {
            fileList.getOrNull(0)?.let {
                SPUtils.get().putCommit("video1", it)
                binding.video1.text = it;
            }
        }
        if (binding.start.tag == binding.btnPickVideo2) {
            fileList.getOrNull(0)?.let {
                SPUtils.get().putCommit("video2", it)
                binding.video2.text = it;
            }
        }
    }

}