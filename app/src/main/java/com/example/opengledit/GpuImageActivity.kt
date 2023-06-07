package com.example.opengledit

import android.R
import android.graphics.BlurMaskFilter
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import com.example.opengledit.base.BaseActivity
import com.example.opengledit.databinding.ActivityGpuBinding
import com.example.opengledit.utils.SPUtils
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageZoomBlurFilter


class GpuImageActivity : BaseActivity() {

    private lateinit var binding: ActivityGpuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.video1.setText(SPUtils.get().getString("image1"))
        binding.video2.setText(SPUtils.get().getString("image2"))


        val imageUri = Uri.parse("https://upload-images.jianshu.io/upload_images/16311248-4ee6c079e02773d1.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240")
        val gpuImage = GPUImage(this)
        gpuImage.setGLSurfaceView(binding.glSurface)
        gpuImage.setImage(imageUri) // this loads image on the current thread, should be run in a thread

//        gpuImage.setFilter(GPUImageZoomBlurFilter())
//        gpuImage.setFilter(GPUImageGaussianBlurFilter(2f))
        gpuImage.setFilter(GPUImageSketchFilter())

//        gpuImage.setFilter(GPUImageSaturationFilter(0.2f));
    }

    override fun onFileCallback(fileList: ArrayList<String>) {
        super.onFileCallback(fileList)

        if (binding.start.tag == binding.btnPickVideo1) {
            fileList.getOrNull(0)?.let {
                SPUtils.get().putCommit("image1", it)
                binding.video1.text = it;
            }
        }
        if (binding.start.tag == binding.btnPickVideo2) {
            fileList.getOrNull(0)?.let {
                SPUtils.get().putCommit("image2", it)
                binding.video2.text = it;
            }
        }
    }
}