package com.example.opengledit.media.encode

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Surface
import com.example.opengledit.media.Frame
import java.text.SimpleDateFormat
import java.util.Date

const val DEFAULT_ENCODE_FRAME_RATE = 30

class BaseEncoder(val width: Int, val height: Int) {

    private val TAG: String = "BaseEncoder"

    // 目标视频宽，只有视频编码的时候才有效
    protected val mWidth: Int = width

    // 目标视频高，只有视频编码的时候才有效
    protected val mHeight: Int = height

    // 编码器
    private lateinit var mCodec: MediaCodec


    private var mSurface: Surface? = null

    private var mMediaMuxer: MediaMuxer? = null

    private var mPath: String

    init {

        val fileName = "LVideo_Test" + SimpleDateFormat("yyyyMM_dd-HHmmss").format(Date()) + ".mp4"
        val filePath = Environment.getExternalStorageDirectory().absolutePath.toString() + "/"
        mPath = filePath + fileName
        mMediaMuxer = MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        initCodec()
    }

    /**
     * 初始化编码器
     */
    private fun initCodec() {
        mCodec = MediaCodec.createEncoderByType(encodeType())
        configEncoder(mCodec)
        mCodec.start()
        Log.i(TAG, "编码器初始化完成")
    }

    fun encodeType(): String {
        return "video/avc"
    }

    fun configEncoder(codec: MediaCodec) {
        if (mWidth <= 0 || mHeight <= 0) {
            throw IllegalArgumentException("Encode width or height is invalid, width: $mWidth, height: $mHeight")
        }
        val bitrate = 3 * mWidth * mHeight
        val outputFormat = MediaFormat.createVideoFormat(encodeType(), mWidth, mHeight)
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_ENCODE_FRAME_RATE)
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

        try {
            configEncoderWithCQ(codec, outputFormat)
        } catch (e: Exception) {
            e.printStackTrace()
            // 捕获异常，设置为系统默认配置 BITRATE_MODE_VBR
            try {
                configEncoderWithVBR(codec, outputFormat)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "配置视频编码器失败")
            }
        }

        mSurface = codec.createInputSurface()
    }


    // 编码帧序列
    private var mFrames = mutableListOf<Frame>()

    /**
     * 将一帧数据压入队列，等待编码
     */
    fun encodeOneFrame(frame: Frame) {
        synchronized(mFrames) {
            mFrames.add(frame)
        }
//        notifyGo()
        // 延时一点时间，避免掉帧
//        Thread.sleep(frameWaitTimeMs())
    }


    // 线程运行
    private var mRunning = true

    // 是否编码结束
    private var mIsEOS = false


    // 当前编码帧信息
    private val mBufferInfo = MediaCodec.BufferInfo()

    /**
     * 循环编码
     */
    fun loopEncode() {
        Log.i(TAG, "开始编码")
        while (mRunning && !mIsEOS) {
            val empty = synchronized(mFrames) {
                mFrames.isEmpty()
            }
            if (empty) {
                justWait()
            }
            if (mFrames.isNotEmpty()) {
                val frame = synchronized(mFrames) {
                    mFrames.removeAt(0)
                }

                if (encodeManually()) {
                    encode(frame)
                } else if (frame.buffer == null) { // 如果是自动编码（比如视频），遇到结束帧的时候，直接结束掉
                    Log.e(TAG, "发送编码结束标志")
                    // This may only be used with encoders receiving input from a Surface
                    mCodec.signalEndOfInputStream()
                    mIsEOS = true
                }
            }
            drain()
        }
    }

    private var mVideoTrackIndex = -1

    /**
     * 榨干编码输出数据
     */
    private fun drain() {
        loop@ while (!mIsEOS) {
            val index = mCodec.dequeueOutputBuffer(mBufferInfo, 1000)
            when (index) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> break@loop
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    mVideoTrackIndex = mMediaMuxer!!.addTrack(mCodec.outputFormat)
                }

                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
//                    mOutputBuffers = mCodec.outputBuffers
                }

                else -> {
                    if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        mIsEOS = true
                        mBufferInfo.set(0, 0, 0, mBufferInfo.flags)
                        Log.e(TAG, "编码结束")
                    }

                    if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                        // SPS or PPS, which should be passed by MediaFormat.
                        mCodec.releaseOutputBuffer(index, false)
                        continue@loop
                    }

                    if (!mIsEOS) {
                        mMediaMuxer?.writeSampleData(mVideoTrackIndex, mCodec.getOutputBuffer(index)!!, mBufferInfo)
                    }
                    mCodec.releaseOutputBuffer(index, false)
                }
            }
        }
    }

    /**
     * 编码
     */
    private fun encode(frame: Frame) {

        val index = mCodec.dequeueInputBuffer(-1)

        /*向编码器输入数据*/
        if (index >= 0) {
            val inputBuffer = mCodec.getInputBuffer(index)
            inputBuffer!!.clear()
            if (frame.buffer != null) {
                inputBuffer.put(frame.buffer)
            }
            if (frame.buffer == null || frame.bufferInfo.size <= 0) { // 小于等于0时，为音频结束符标记
                mCodec.queueInputBuffer(
                    index, 0, 0,
                    frame.bufferInfo.presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
            } else {
                frame.buffer?.flip()
                frame.buffer?.mark()
                mCodec.queueInputBuffer(
                    index, 0, frame.bufferInfo.size,
                    frame.bufferInfo.presentationTimeUs, 0
                )
            }
            frame.buffer?.clear()
        }
    }

    fun encodeManually(): Boolean {
        return true
    }


    private var mLock = Object()

    /**
     * 编码进入等待
     */
    private fun justWait() {
        try {
            synchronized(mLock) {
                mLock.wait(1000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun configEncoderWithCQ(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 本部分手机不支持 BITRATE_MODE_CQ 模式，有可能会异常
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ
            )
        }
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    private fun configEncoderWithVBR(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
            )
        }
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }
}