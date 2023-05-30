package com.example.opengledit.media.decode

import com.example.opengledit.media.Frame

interface DecoderListener {

    fun decodeOneFrame(frame: Frame)

    fun decoderFinish()

}