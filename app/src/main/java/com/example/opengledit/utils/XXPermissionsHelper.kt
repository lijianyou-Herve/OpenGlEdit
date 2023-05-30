package com.example.opengledit.utils

import android.content.Context
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions


object XXPermissionsHelper {

    fun requestPermissions(context: Context) {
        XXPermissions.with(context)
//            .permission(Permission.READ_MEDIA_IMAGES) // 申请多个权限
//            .permission(Permission.READ_MEDIA_VIDEO)
//            .permission(Permission.READ_MEDIA_AUDIO) //                .permission(Permission.WRITE_EXTERNAL_STORAGE)
//            .permission(Permission.WRITE_EXTERNAL_STORAGE) //                .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE) //                .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .request(OnPermissionCallback { permissions, allGranted -> }

            )
    }
}