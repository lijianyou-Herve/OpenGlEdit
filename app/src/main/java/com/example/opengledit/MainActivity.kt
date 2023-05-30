package com.example.opengledit

import android.os.Bundle
import com.example.opengledit.base.BaseActivity
import com.example.opengledit.databinding.ActivityMainBinding
import com.example.opengledit.utils.SPUtils
import com.example.opengledit.utils.XXPermissionsHelper

class MainActivity : BaseActivity() {

    private val TAG: String = "MainActivity"

    //自定义的OpenGL渲染器，详情请继续往下看
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()

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


    private fun requestPermissions() {
        XXPermissionsHelper.requestPermissions(this)
    }

}