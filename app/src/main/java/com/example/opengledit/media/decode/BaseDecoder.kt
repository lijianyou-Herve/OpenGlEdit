package com.example.opengledit.media.decode

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.opengledit.media.Frame

class BaseDecoder(filePath: String, sfv: SurfaceView?, surface: Surface?) {

    private val TAG: String = "BaseDecoder"
    private var isEndOfStream: Boolean = false
    private var mVideoTrack: Int = -1
    private var mExtractor: MediaExtractor? = null

    private val mSurfaceView = sfv
    private var mSurface = surface

    /**
     * 解码数据信息
     */
    private var mBufferInfo = MediaCodec.BufferInfo()

    private var mMediaFormat: MediaFormat? = null

    private val timeOut = 100L

    /**
     * 音视频解码器
     */
    private lateinit var mCodec: MediaCodec

    init {
        mExtractor = MediaExtractor()
        mExtractor!!.setDataSource(filePath)
        getVideoFormat()
        initCodec()
    }


    private fun initCodec(): Boolean {
        try {
            val type = mMediaFormat?.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(type!!)
            if (!configCodec(mCodec, mMediaFormat!!)) {
//                waitDecode()
            }
            mCodec.start()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0)
//            notifyDecode()
        } else if (mSurfaceView?.holder?.surface != null) {
            mSurface = mSurfaceView?.holder?.surface
            configCodec(codec, format)
        } else {
            mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    mSurface = holder.surface
                    configCodec(codec, format)
                }
            })

            return false
        }
        return true
    }

    /**
     * 获取视频格式参数
     */
    fun getVideoFormat(): MediaFormat? {
        if (mMediaFormat == null) {
            for (i in 0 until mExtractor!!.trackCount) {
                val mediaFormat = mExtractor!!.getTrackFormat(i)
                val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
                if (mime!!.startsWith("video/")) {
                    mVideoTrack = i
                    break
                }
            }
            if (mVideoTrack >= 0) {
                mMediaFormat = mExtractor!!.getTrackFormat(mVideoTrack)
            }
        }
        mExtractor!!.selectTrack(mVideoTrack)
        return mMediaFormat
    }

    fun pushBufferToDecoder(): Boolean {
        val inputBufferIndex = mCodec.dequeueInputBuffer(timeOut)
        if (inputBufferIndex >= 0) {
            val inputBuffer = mCodec.getInputBuffer(inputBufferIndex)!!
            inputBuffer.clear()
            val readSampleSize = mExtractor!!.readSampleData(inputBuffer, 0)
            if (readSampleSize < 0) {
                //如果数据已经取完，压入数据结束标志：MediaCodec.BUFFER_FLAG_END_OF_STREAM
                mCodec.queueInputBuffer(
                    inputBufferIndex, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = true
            } else {
                //解码
                mCodec.queueInputBuffer(
                    inputBufferIndex, 0,
                    readSampleSize, mExtractor!!.sampleTime, mExtractor!!.sampleFlags
                )
            }
            //进入下一帧
            mExtractor!!.advance()
        } else {
            //end
        }

        return isEndOfStream
    }

    fun pullBufferFromDecoder(): Int {
        val index = mCodec.dequeueOutputBuffer(mBufferInfo, timeOut)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
            MediaCodec.INFO_TRY_AGAIN_LATER -> {}
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
//                mOutputBuffers = mCodec.getOutputBuffer(index)
            }

            else -> {
                return index
            }
        }
        return -1
    }

    var mDecoderListener: DecoderListener? = null


    fun start() {

        Log.i(TAG, "解码开始")

        while (true) {
            if (isEndOfStream.not()) {
                Log.i(TAG, "解码压入")
                pushBufferToDecoder()
            } else {

            }

            if (mStartTimeForSync == -1L) {
                mStartTimeForSync = System.currentTimeMillis()
            }

            //【解码步骤：3. 将解码好的数据从缓冲区拉取出来】
            val index = pullBufferFromDecoder()
            Log.i(TAG, "解码获取")
            if (index >= 0) {
                Log.i(TAG, "解码有效")

                sleepRender()

                val outputBuffer = mCodec.getOutputBuffer(index)
                //将解码数据传递出去
                val frame = Frame()
                frame.buffer = outputBuffer
                frame.setBufferInfo(mBufferInfo)
                mDecoderListener?.decodeOneFrame(frame)
                //【解码步骤：5. 释放输出缓冲】
                mCodec.releaseOutputBuffer(index, true)
            } else {
                Log.i(TAG, "解码无效")
            }

            //【解码步骤：6. 判断解码是否完成】
            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.i(TAG, "解码结束")
                mDecoderListener?.decoderFinish()
                break
            }
        }

        mExtractor!!.release()
        mCodec.stop()
        mCodec.release()

    }

    fun getCurTimeStamp(): Long {
        return mBufferInfo.presentationTimeUs / 1000
    }

    /**
     * 开始解码时间，用于音视频同步
     */
    private var mStartTimeForSync = -1L

    private fun sleepRender() {
        val passTime = System.currentTimeMillis() - mStartTimeForSync
        val curTime = getCurTimeStamp()
        if (curTime > passTime) {
            Thread.sleep(curTime - passTime)
        }
    }
}