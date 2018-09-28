package uz.mukhammadakbar.image.viewer.helper

import android.content.Context
import android.graphics.Matrix

import uz.mukhammadakbar.image.viewer.listeners.OnChangeStateListener
import uz.mukhammadakbar.image.viewer.listeners.OnCompatPostAnimationListener
import uz.mukhammadakbar.image.viewer.listeners.OnMatrixChangeListener
import uz.mukhammadakbar.image.viewer.utils.State

class Fling(context: Context,
                     velocityX: Int,
                     velocityY: Int,
                     array: FloatArray? = null,
                     imageWidth: Int,
                     viewWidth: Int,
                     imageHeight: Int,
                     viewHeight: Int) : Runnable {

    private var scroller: CompatScroller? = null
    private var currX: Int = 0
    private var currY: Int = 0
    private var onChangeStateListener: OnChangeStateListener? = null
    private var onMatrixChangeStateListener: OnMatrixChangeListener? = null
    private var onCompatPostAnimationListener: OnCompatPostAnimationListener? = null

    init {
        onChangeStateListener?.onSetState(State.FLING)
        scroller = CompatScroller(context)
        onMatrixChangeStateListener?.getValues(array)

        val startX = array?.get(Matrix.MTRANS_X)?.toInt()
        val startY = array?.get(Matrix.MTRANS_Y)?.toInt()
        val minX: Int
        val maxX: Int
        val minY: Int
        val maxY: Int

        if (imageWidth > viewWidth) {
            minX = viewWidth - imageWidth
            maxX = 0

        } else {
            maxX = startX ?: 0
            minX = maxX
        }

        if (imageHeight > viewHeight) {
            minY = viewHeight - imageHeight
            maxY = 0

        } else {
            maxY = startY ?: 0
            minY = maxY
        }

        if (startX != null && startY != null) {
            scroller?.fling(startX, startY, velocityX, velocityY, minX,
                    maxX, minY, maxY)
            currX = startX
            currY = startY
        }
    }

    fun cancelFling() {
        if (scroller != null) {
            onChangeStateListener?.onSetState(State.NONE)
            scroller!!.forceFinished(true)
        }
    }

    override fun run() {

        if (scroller?.isFinished == true) {
            scroller = null
            return
        }

        if (scroller?.computeScrollOffset() == true) {
            val newX = scroller?.currX
            val newY = scroller?.currY
            val transX = newX?.minus(currX) ?: 0
            val transY = newY?.minus(currY) ?: 0
            currX = newX ?: 0
            currY = newY ?: 0
            onMatrixChangeStateListener?.postTranslate(transX, transY)
//            fixTrans()
            onMatrixChangeStateListener?.onSetImageMatrix()
            onCompatPostAnimationListener?.compatPostAnimation(this)
        }
    }

    fun setOnStateChangeListener(onChangeStateListener: OnChangeStateListener){
        this.onChangeStateListener = onChangeStateListener
    }

    fun setOnCompatPostAnimationListener(onCompatPostAnimationListener: OnCompatPostAnimationListener){
        this.onCompatPostAnimationListener = onCompatPostAnimationListener
    }

    fun setOnMatrixChangeListener(onMatrixChangeListener: OnMatrixChangeListener){
        this.onMatrixChangeStateListener = onMatrixChangeListener
    }

}