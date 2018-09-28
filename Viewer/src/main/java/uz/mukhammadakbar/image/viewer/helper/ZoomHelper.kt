package uz.mukhammadakbar.image.viewer.helper

import android.graphics.Matrix
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import uz.mukhammadakbar.image.viewer.listeners.OnChangeStateListener
import uz.mukhammadakbar.image.viewer.listeners.OnCompatPostAnimationListener
import uz.mukhammadakbar.image.viewer.listeners.OnMatrixChangeListener
import uz.mukhammadakbar.image.viewer.utils.State
import uz.mukhammadakbar.image.viewer.utils.ZoomVariables
import uz.mukhammadakbar.image.viewer.views.DraggableImageView

class ZoomHelper(var view: DraggableImageView) {

    private var fling: Fling? = null
    var state: State? = null
    private var prevMatrix: Matrix? = null
    var matrix: Matrix? = null

    var normalizedScale: Float = 0.toFloat()

    var minScale: Float = 0.toFloat()
    var maxScale: Float = 0.toFloat()
    private var superMinScale: Float = 0.toFloat()
    private var superMaxScale: Float = 0.toFloat()
    var mArray: FloatArray? = null

    private var matchViewWidth: Float = 0.toFloat()
    private var matchViewHeight:Float = 0.toFloat()
    private var prevMatchViewWidth:Float = 0.toFloat()
    private var prevMatchViewHeight:Float = 0.toFloat()

    var viewWidth: Int = 0
    var viewHeight:Int = 0
    private var prevViewWidth:Int = 0
    private var prevViewHeight:Int = 0

    var mScaleType: ImageView.ScaleType? = null
    private var imageRenderedAtLeastOnce: Boolean = false
    var onDrawReady: Boolean = false

    private var delayedZoomVariables: ZoomVariables? = null

    init {
        prevMatrix = Matrix()
        matrix = Matrix()
    }

    fun onSaveInstance(parcelable: Parcelable?): Bundle {
        return Bundle().apply {
            putParcelable("instanceState", parcelable)
            putFloat("saveScale", normalizedScale)
            putFloat("matchViewHeight", matchViewHeight)
            putFloat("matchViewWidth", matchViewWidth)
            putInt("viewWidth", viewWidth)
            putInt("viewHeight", viewHeight)
            matrix?.getValues(mArray)
            putFloatArray("matrix", mArray)
            putBoolean("imageRendered", imageRenderedAtLeastOnce)
        }
    }

    fun onRestoreInstanceState(parcelable: Parcelable?) {
        if (parcelable is Bundle) {
            normalizedScale = parcelable.getFloat("saveScale")
            mArray = parcelable.getFloatArray("matrix")
            prevMatrix!!.setValues(mArray)
            prevMatchViewHeight = parcelable.getFloat("matchViewHeight")
            prevMatchViewWidth = parcelable.getFloat("matchViewWidth")
            prevViewHeight = parcelable.getInt("viewHeight")
            prevViewWidth = parcelable.getInt("viewWidth")
            imageRenderedAtLeastOnce = parcelable.getBoolean("imageRendered")
        }
    }

    fun savePreviousImageValues() {
        if (matrix != null && viewHeight != 0 && viewWidth != 0) {
            matrix?.getValues(mArray)
            prevMatrix?.setValues(mArray)
            prevMatchViewHeight = matchViewHeight
            prevMatchViewWidth = matchViewWidth
            prevViewHeight = viewHeight
            prevViewWidth = viewWidth
        }
    }

    fun onFling(velocityX: Float, velocityY: Float){
        if (fling != null) {
            fling?.cancelFling()
        }
        fling = Fling(view.context, velocityX.toInt(), velocityY.toInt(), mArray, getImageWidth().toInt(), viewWidth, getImageHeight().toInt(), viewHeight)
        fling?.setOnCompatPostAnimationListener(onCompatPostAnimationListener)
        fling?.setOnMatrixChangeListener(onMatrixChangeListener)
        fling?.setOnStateChangeListener(onChangeStateListener)
        onCompatPostAnimationListener.compatPostAnimation(fling)
    }

    fun onDraw(){
        onDrawReady = true
        imageRenderedAtLeastOnce = true
        if (delayedZoomVariables != null) {
            if (delayedZoomVariables?.scale != null && delayedZoomVariables?.focusX != null && delayedZoomVariables?.focusY != null && delayedZoomVariables?.scaleType != null)
            setZoom(delayedZoomVariables?.scale as Float, delayedZoomVariables?.focusX as Float, delayedZoomVariables?.focusY as Float, delayedZoomVariables?.scaleType as ImageView.ScaleType)
            delayedZoomVariables = null
        }
    }

    private val last = PointF()

    fun onTouchEvent(event: MotionEvent, isDragging: Boolean?){
        if (isDragging == true) return

        val curr = PointF(event.x, event.y)

        if (state === State.NONE || state === State.DRAG || state === State.FLING) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    if (fling != null)
                        fling!!.cancelFling()
                    state = State.DRAG
                }

                MotionEvent.ACTION_MOVE -> if (state === State.DRAG) {
                    val deltaX = curr.x - last.x
                    val deltaY = curr.y - last.y
                    val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(), getImageWidth())
                    val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(), getImageHeight())
                    matrix?.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    last.set(curr.x, curr.y)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> state = State.NONE
            }
        }

        view.imageMatrix = matrix
    }

    fun init(){
        mArray = FloatArray(9)
        normalizedScale = 1f
        if (mScaleType == null) {
            mScaleType = ImageView.ScaleType.FIT_CENTER
        }
        minScale = 1f
        maxScale = 3f
        superMinScale = SUPER_MIN_MULTIPLIER * minScale
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
        view.imageMatrix = matrix
        view.setScaleType(ImageView.ScaleType.MATRIX)
        state = State.NONE
        onDrawReady = false
    }

    /**
     * Returns false if image is in initial, unzoomed state. False, otherwise.
     * @return true if image is zoomed
     */
    fun isZoomed(): Boolean {
        return normalizedScale != 1f
    }

    /**
     * Get the current zoom. This is the zoom relative to the initial
     * scale, not the original resource.
     * @return current zoom multiplier.
     */
    fun getCurrentZoom(): Float {
        return normalizedScale
    }

    /**
     * Reset zoom and translation to initial state.
     */
    fun resetZoom() {
        normalizedScale = 1f
        fitImageToView()
    }

    fun getImageWidth(): Float {
        return matchViewWidth * normalizedScale
    }

    fun getImageHeight(): Float {
        return matchViewHeight * normalizedScale
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize

        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans)
            return -trans + minTrans
        return if (trans > maxTrans) -trans + maxTrans else 0f
    }

    fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
        return when (mode) {
            View.MeasureSpec.EXACTLY -> size

            View.MeasureSpec.AT_MOST -> Math.min(drawableWidth, size)

            View.MeasureSpec.UNSPECIFIED -> drawableWidth

            else -> size
        }
    }

    private fun translateMatrixAfterRotate(axis: Int, trans: Float, prevImageSize: Float, imageSize: Float, prevViewSize: Int, viewSize: Int, drawableSize: Int) {
        when {
            imageSize < viewSize -> mArray?.set(axis, (viewSize - drawableSize * mArray!![Matrix.MSCALE_X]) * 0.5f)

            trans > 0 -> mArray?.set(axis, -((imageSize - viewSize) * 0.5f))

            else -> {
                val percentage = (Math.abs(trans) + 0.5f * prevViewSize) / prevImageSize
                mArray?.set(axis, -(percentage * imageSize - viewSize * 0.5f))
            }
        }
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) 0f  else delta
    }

    fun canScrollHorizontally(direction: Int): Boolean {
        matrix?.getValues(mArray)
        val x = mArray?.get(Matrix.MTRANS_X)
        if (getImageWidth() <viewWidth) {
            return false

        } else if (x != null) {
            if (x >= -1 && direction < 0) {
                return false
            } else if (Math.abs(x) + viewWidth.toFloat() + 1f >= getImageWidth() && direction > 0) {
                return false
            }
        }
        return true
    }

    fun fitImageToView() {
        val drawable = view.drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return
        }
        if (matrix == null || prevMatrix == null) {
            return
        }

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        var scaleX = viewWidth.toFloat() / drawableWidth
        var scaleY = viewHeight.toFloat() / drawableHeight

        when (mScaleType) {
            ImageView.ScaleType.CENTER -> {
                scaleY = 1f
                scaleX = scaleY
            }

            ImageView . ScaleType . CENTER_CROP -> {
                scaleY = Math.max(scaleX, scaleY)

                scaleX = scaleY
            }
            ImageView . ScaleType . CENTER_INSIDE -> {
                scaleY = Math.min(1f, Math.min(scaleX, scaleY))
                scaleX = scaleY
                scaleY = Math.min(scaleX, scaleY)
                scaleX = scaleY
            }

            ImageView.ScaleType.FIT_CENTER -> {
                scaleY = Math.min(scaleX, scaleY)
                scaleX = scaleY
            }
            ImageView.ScaleType. FIT_XY -> {

            }

            else ->
                throw UnsupportedOperationException("DraggableImageView does not support FIT_START or FIT_END")
        }

        val redundantXSpace = viewWidth - scaleX * drawableWidth
        val redundantYSpace = viewHeight - scaleY * drawableHeight
        matchViewWidth = viewWidth - redundantXSpace
        matchViewHeight = viewHeight - redundantYSpace
        if (!isZoomed() && !imageRenderedAtLeastOnce) {

            matrix?.setScale(scaleX, scaleY)
            matrix?.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            normalizedScale = 1f

        } else {

            if (prevMatchViewWidth == 0f || prevMatchViewHeight == 0f) {
                savePreviousImageValues()
            }

            prevMatrix?.getValues(mArray)

            mArray?.set(Matrix.MSCALE_X, matchViewWidth / drawableWidth * normalizedScale)
            mArray?.set(Matrix.MSCALE_Y, matchViewHeight / drawableHeight * normalizedScale)

            val transX = mArray!![Matrix.MTRANS_X]
            val transY = mArray!![Matrix.MTRANS_Y]

            val prevActualWidth = prevMatchViewWidth * normalizedScale
            val actualWidth = getImageWidth()
            translateMatrixAfterRotate(Matrix.MTRANS_X, transX, prevActualWidth, actualWidth, prevViewWidth, viewWidth, drawableWidth)

            val prevActualHeight = prevMatchViewHeight * normalizedScale
            val actualHeight = getImageHeight()
            translateMatrixAfterRotate(Matrix.MTRANS_Y, transY, prevActualHeight, actualHeight, prevViewHeight, viewHeight, drawableHeight)

            matrix?.setValues(mArray)
        }
        fixTrans()
        view.imageMatrix = matrix
    }

    private fun fixTrans() {
        matrix?.getValues(mArray)
        val transX = mArray?.get(Matrix.MTRANS_X) ?: 0f
        val transY = mArray?.get(Matrix.MTRANS_Y) ?: 0f

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), getImageWidth())
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), getImageHeight())

        if (fixTransX != 0f || fixTransY != 0f) {
            matrix?.postTranslate(fixTransX, fixTransY)
        }
    }

    fun fixScaleTrans() {
        fixTrans()
        matrix?.getValues(mArray)
        if (getImageWidth() < viewWidth) {
            mArray?.set(Matrix.MTRANS_X, (viewWidth - getImageWidth()) / 2)
        }

        if (getImageHeight() < viewHeight) {
            mArray?.set(Matrix.MTRANS_Y, (viewHeight - getImageHeight()) / 2)
        }
        matrix?.setValues(mArray)
    }

    fun scaleImage(deltaScale: Double, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) {
        var mDeltaScale = deltaScale

        val lowerScale: Float
        val upperScale: Float
        if (stretchImageToSuper) {
            lowerScale = superMinScale
            upperScale = superMaxScale

        } else {
            lowerScale = minScale
            upperScale = maxScale
        }

        val origScale = normalizedScale
        normalizedScale *= mDeltaScale.toFloat()
        if (normalizedScale > upperScale) {
            normalizedScale = upperScale
            mDeltaScale = (upperScale / origScale).toDouble()
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale
            mDeltaScale = (lowerScale / origScale).toDouble()
        }
        matrix?.postScale(mDeltaScale.toFloat(), mDeltaScale.toFloat(), focusX, focusY)
        fixScaleTrans()
    }

    fun setZoom(scale: Float, focusX: Float, focusY: Float, scaleType: ImageView.ScaleType) {
        if (!onDrawReady) {
            delayedZoomVariables = ZoomVariables(scale, focusX, focusY, scaleType)
            return
        }

        if (scaleType != mScaleType) {
            view.setScaleType(scaleType)
        }
        resetZoom()
        scaleImage(scale.toDouble(), (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), true)
        matrix?.getValues(mArray)
        mArray?.set(Matrix.MTRANS_X, -(focusX * getImageWidth() - viewWidth * 0.5f))
        mArray?.set(Matrix.MTRANS_Y, -(focusY * getImageHeight() - viewHeight * 0.5f))
        matrix?.setValues(mArray)
        fixTrans()
        view.imageMatrix = matrix
    }

    private val onCompatPostAnimationListener = object : OnCompatPostAnimationListener {

        override fun compatPostAnimation(runnable: Runnable?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.postOnAnimation(runnable)
            } else {
                view.postDelayed(runnable, (1000 / 60).toLong())
            }
        }
    }

    private val onMatrixChangeListener = object : OnMatrixChangeListener {
        override fun postTranslate(transX: Int, transY: Int) {
            matrix?.postTranslate(transX.toFloat(), transY.toFloat())
        }

        override fun getValues(array: FloatArray?) {
            matrix?.getValues(array)
        }

        override fun onSetImageMatrix() {
            view.imageMatrix = matrix
        }
    }

    private val onChangeStateListener = object : OnChangeStateListener {
        override fun onSetState(newState: State) {
            state = newState
        }
    }

    companion object {
        private const val SUPER_MIN_MULTIPLIER = 0.75f
        private const val SUPER_MAX_MULTIPLIER = 1.25f
    }

}