package com.example.opengledit.opengl

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.opengledit.opengl.drawer.Lattice4VideoDrawer
import com.example.opengledit.opengl.drawer.VideoDrawer


/**
 * 自定义GLSurfaceView
 *
 * @author Chen Xiaoping (562818444@qq.com)
 *
 */
class TouchSurfaceView : SurfaceView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var mPrePoint = PointF()

    private var mDrawers: MutableList<Lattice4VideoDrawer> = mutableListOf()
    private var mDrawer: Lattice4VideoDrawer? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawer = mDrawers?.random()
                mPrePoint.x = event.x
                mPrePoint.y = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.x - mPrePoint.x) / width
                val dy = (event.y - mPrePoint.y) / height
                mDrawer?.translate(dx, dy)
                mPrePoint.x = event.x
                mPrePoint.y = event.y
            }
        }
        return true
    }

    fun addDrawer(drawer: Lattice4VideoDrawer) {
        mDrawers?.add(drawer)
    }
}