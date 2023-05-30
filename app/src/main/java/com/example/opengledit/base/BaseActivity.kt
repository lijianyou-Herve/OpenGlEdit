package com.example.opengledit.base

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.zlylib.fileselectorlib.FileSelector
import com.zlylib.fileselectorlib.utils.Const
import java.util.ArrayList

open class BaseActivity : AppCompatActivity() {

    val FILE_REQUEST_CODE = 1

    fun openFile() {

        FileSelector.from(this) // .onlyShowFolder()  //只显示文件夹
            //.onlySelectFolder()  //只能选择文件夹
            // .isSingle() // 只能选择一个
            .setMaxCount(4) //设置最大选择数
            //            .setFileTypes("png", "doc","apk", "mp3", "gif", "txt", "mp4", "zip", "pdf") //设置文件类型
            .setFileTypes("mp4", "mp3", "aac", "wav") //设置文件类型
            //.setSortType(FileSelector.BY_TIME_ASC) //设置时间排序
            //.setSortType(FileSelector.BY_SIZE_DESC) //设置大小排序
            //.setSortType(FileSelector.BY_EXTENSION_DESC) //设置类型排序
            .setSortType(FileSelector.BY_NAME_ASC) //设置名字排序
            .requestCode(FILE_REQUEST_CODE) //设置返回码
            .setTargetPath("/storage/emulated/0/Download") //设置默认目录
            .start()
    }

    open fun onFileCallback(fileList: ArrayList<String>) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_REQUEST_CODE) {
            if (data != null) {
                val essFileList = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION)
                if (essFileList == null) {
                    Log.e("BaseFragment", "getFileCallback: 文件获取失败")
                } else {
                    onFileCallback(essFileList)
                }
            }
        }
    }

}