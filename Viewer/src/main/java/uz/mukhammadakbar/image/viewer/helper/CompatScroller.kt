package uz.mukhammadakbar.image.viewer.helper

import android.content.Context
import android.widget.OverScroller
import android.widget.Scroller

class CompatScroller(context: Context) {
    private lateinit var scroller: Scroller
    private var overScroller: OverScroller
    private var isPreGingerbread: Boolean = false

    init {
        isPreGingerbread = false
        overScroller = OverScroller(context)
    }

    val isFinished: Boolean
        get() = if (isPreGingerbread) {
            scroller.isFinished
        } else {
            overScroller.isFinished
        }

    val currX: Int
        get() = if (isPreGingerbread) {
            scroller.currX
        } else {
            overScroller.currX
        }

    val currY: Int
        get() = if (isPreGingerbread) {
            scroller.currY
        } else {
            overScroller.currY
        }

    fun fling(startX: Int, startY: Int, velocityX: Int, velocityY: Int, minX: Int, maxX: Int, minY: Int, maxY: Int) {
        if (isPreGingerbread) {
            scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        } else {
            overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        }
    }

    fun forceFinished(finished: Boolean) {
        if (isPreGingerbread) {
            scroller.forceFinished(finished)
        } else {
            overScroller.forceFinished(finished)
        }
    }

    fun computeScrollOffset(): Boolean {
        return if (isPreGingerbread) {
            scroller.computeScrollOffset()
        } else {
            overScroller.computeScrollOffset()
            overScroller.computeScrollOffset()
        }
    }
}