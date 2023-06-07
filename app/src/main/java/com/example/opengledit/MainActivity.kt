package com.example.opengledit

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.Toast
import com.example.opengledit.base.BaseActivity
import com.example.opengledit.databinding.ActivityMainBinding
import com.example.opengledit.media.BaseDecoder
import com.example.opengledit.media.DefDecoderStateListener
import com.example.opengledit.media.Frame
import com.example.opengledit.media.IDecoder
import com.example.opengledit.media.decoder.AudioDecoder
import com.example.opengledit.media.decoder.DefDecodeStateListener
import com.example.opengledit.media.decoder.VideoDecoder
import com.example.opengledit.media.encoder.AudioEncoder
import com.example.opengledit.media.encoder.BaseEncoder
import com.example.opengledit.media.encoder.DefEncodeStateListener
import com.example.opengledit.media.encoder.VideoEncoder
import com.example.opengledit.media.muxer.MMuxer
import com.example.opengledit.opengl.drawer.FourPartFilter
import com.example.opengledit.opengl.drawer.Lattice4VideoDrawer
import com.example.opengledit.opengl.drawer.SoulVideoDrawer
import com.example.opengledit.opengl.drawer.VideoDrawer
import com.example.opengledit.opengl.egl.CustomerGLRenderer
import com.example.opengledit.utils.SPUtils
import java.io.File
import java.util.concurrent.Executors

class MainActivity : BaseActivity(), MMuxer.IMuxerStateListener {

    private val TAG: String = "MainActivity"

    //自定义的OpenGL渲染器，详情请继续往下看
    private lateinit var binding: ActivityMainBinding

    private var audioDecoder: IDecoder? = null
    private var videoDecoder: IDecoder? = null

    private var path1: String = ""
    private var path2: String = ""

    private val threadPool = Executors.newFixedThreadPool(10)

    private var videoEncoder: VideoEncoder? = null
    private var audioEncoder: AudioEncoder? = null

    private var muxer = MMuxer()

    private var renderer = CustomerGLRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        muxer.setStateListener(this)

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
            val old = renderer
            renderer = CustomerGLRenderer()
            old.stop()
            addVideos()
            initAudio()
            onStartClick()
        }

        path1 = binding.video1.text.toString()
        path2 = binding.video2.text.toString()
        val file = File(path1)
        if (file.exists()) {
            addVideos()
            initAudio()
            setRenderSurface()
        }
    }


    private fun addVideos() {
        initVideo(path1, 0)
        initVideo(path2, 1)
        initVideo(path2, 2)
        initVideo(path2, 3)
    }


    private var startTime = 0L

    fun onStartClick() {
        startTime = System.currentTimeMillis() / 1000
        Toast.makeText(this, "正在编码", Toast.LENGTH_LONG).show()

        audioDecoder?.withoutSync()
        videoDecoder?.withoutSync()

        initAudioEncoder()
        initVideoEncoder()
    }

    private fun initVideoEncoder() {
        // 视频编码器
        videoEncoder = VideoEncoder(muxer, 1920, 1080)

        renderer.setRenderMode(CustomerGLRenderer.RenderMode.RENDER_WHEN_DIRTY)
        renderer.setSurface(videoEncoder!!.getEncodeSurface()!!, 1920, 1080)

        videoEncoder!!.setStateListener(object : DefEncodeStateListener {
            override fun encoderFinish(encoder: BaseEncoder) {
                renderer.stop()
            }
        })
        threadPool.execute(videoEncoder)
    }

    private fun initAudioEncoder() {
        // 音频编码器
        audioEncoder = AudioEncoder(muxer)
        // 启动编码线程
        threadPool.execute(audioEncoder)
    }

    private fun initVideo(path: String, index: Int) {
        val drawer = VideoDrawer() // SoulVideoDrawer()
//        val drawer = SoulVideoDrawer() // SoulVideoDrawer()
//        val drawer = Lattice4VideoDrawer() // SoulVideoDrawer()
//        val drawer = FourPartFilter(yxApp) // SoulVideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initVideoDecoder(path, Surface(it))
        }

        val sx = 0.5f
        val sy = 0.5f
        val dx = -0.25f + 0.5f * (index % 2)
        val dy = -0.25f + 0.5f * (index / 2)

//        drawer.scale(sx, sy)
//        drawer.translate(dx, dy)
        if(index==3){
//            drawer.translate(-0.1f, 0f)
        }
//        drawer.setAlpha(0.5f)

//        drawer.setIndex(index)
        renderer.addDrawer(drawer)
//        binding.glSurface.addDrawer(drawer)


//        val dx = 0f
//        val dy = 0f

//        Handler().postDelayed({
//            drawer.scale(1f, 1f)
//        }, 1000)
    }

    private fun initVideoDecoder(path: String, sf: Surface) {
        val videoDecoder = VideoDecoder(path, null, sf)
//            .withoutSync()
        videoDecoder!!.setStateListener(object : DefDecodeStateListener {
            override fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame) {
                renderer.notifySwap(frame.bufferInfo.presentationTimeUs)
                videoEncoder?.encodeOneFrame(frame)
            }

            override fun decoderFinish(decodeJob: BaseDecoder?) {
                if (path == path1) {
                    videoEncoder?.endOfStream()
                }
            }
        })
        videoDecoder!!.goOn()

        //启动解码线程
        threadPool.execute(videoDecoder!!)
    }

    private fun initAudio() {
        audioDecoder?.stop()
        audioDecoder = AudioDecoder(path1)
//            .withoutSync()
        audioDecoder!!.setStateListener(object : DefDecodeStateListener {

            override fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame) {
                audioEncoder?.encodeOneFrame(frame)
            }

            override fun decoderFinish(decodeJob: BaseDecoder?) {
                audioEncoder?.endOfStream()
            }
        })
        audioDecoder!!.goOn()

        //启动解码线程
        threadPool.execute(audioDecoder!!)
    }

    override fun onMuxerFinish() {
        runOnUiThread {
            Toast.makeText(this, "编码完成", Toast.LENGTH_LONG).show()

            Log.i(TAG, "onMuxerFinish:总时间 =  ${System.currentTimeMillis() / 1000 - startTime}")
        }

        audioDecoder?.stop()
        audioDecoder = null

        videoDecoder?.stop()
        videoDecoder = null
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

    private fun setRenderSurface() {
        renderer.setSurface(binding.glSurface)
    }

}