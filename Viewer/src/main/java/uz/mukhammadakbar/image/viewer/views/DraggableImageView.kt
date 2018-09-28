package uz.mukhammadakbar.image.viewer.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import uz.mukhammadakbar.image.viewer.listeners.OnCoordinationChangeListener
import uz.mukhammadakbar.image.viewer.listeners.OnDragChangeListener
import uz.mukhammadakbar.image.viewer.utils.Coordination
import uz.mukhammadakbar.image.viewer.helper.DragHelper
import uz.mukhammadakbar.image.viewer.helper.ZoomHelper
import uz.mukhammadakbar.image.viewer.utils.State
import android.os.SystemClock



class DraggableImageView : android.support.v7.widget.AppCompatImageView, OnCoordinationChangeListener {

    private var dragHelper: DragHelper? = null
    private var zoomHelper: ZoomHelper? = null
    private var gestureDetector: GestureDetector? = null

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private val doubleTapListener: GestureDetector.OnDoubleTapListener? = null

    private val scrollPosition: PointF?
        get() {
            val drawable = drawable ?: return null
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            val point = transformCoordTouchToBitmap((zoomHelper!!.viewWidth / 2).toFloat(), (zoomHelper!!.viewHeight / 2).toFloat(), true)
            point.x /= drawableWidth.toFloat()
            point.y /= drawableHeight.toFloat()
            return point
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        super.setClickable(true)
        dragHelper = DragHelper(context)
        if (zoomHelper == null) {
            zoomHelper = ZoomHelper(this)
        }
        gestureDetector = GestureDetector(context, dragHelper)
        dragHelper!!.setInitialCord(this)
        dragHelper!!.setOnCoordinationChangeListener(this)

        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mGestureDetector = GestureDetector(context, gestureListener)
        zoomHelper?.init()
    }

    override fun coordinationChanged(coordination: Coordination, duration: Long) {
        animate()
                .x(coordination.x)
                .y(coordination.y)
                .setInterpolator(LinearInterpolator())
                .setDuration(duration)
                .start()
    }

    fun setOnDragChangeListener(onDragChangeListener: OnDragChangeListener) {
        dragHelper?.setOnDragChangeListener(onDragChangeListener)
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        savePreviousImageValues()
        zoomHelper?.fitImageToView()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        savePreviousImageValues()
        zoomHelper?.fitImageToView()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        savePreviousImageValues()
        zoomHelper?.fitImageToView()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        savePreviousImageValues()
        zoomHelper?.fitImageToView()
    }

    override fun setScaleType(type: ImageView.ScaleType) {
        if (type == ImageView.ScaleType.FIT_START || type == ImageView.ScaleType.FIT_END) {
            throw UnsupportedOperationException("DraggableImageView does not support FIT_START or FIT_END")
        }
        if (type == ImageView.ScaleType.MATRIX) {
            super.setScaleType(ImageView.ScaleType.MATRIX)

        } else {
            if (zoomHelper == null) {
                zoomHelper = ZoomHelper(this)
            }
            zoomHelper?.mScaleType = type
            if (zoomHelper!!.onDrawReady) {
                setZoom(this)
            }
        }
    }

    override fun getScaleType(): ImageView.ScaleType? {
        return zoomHelper?.mScaleType
    }

    private fun savePreviousImageValues() {
        zoomHelper?.savePreviousImageValues()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        return zoomHelper?.onSaveInstance(super.onSaveInstanceState())
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            zoomHelper?.onRestoreInstanceState(state)
            super.onRestoreInstanceState(state.getParcelable("instanceState"))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onDraw(canvas: Canvas) {
        zoomHelper?.onDraw()
        super.onDraw(canvas)
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        savePreviousImageValues()
    }

    private fun setZoom(img: DraggableImageView) {
        val center = img.scrollPosition
        zoomHelper?.setZoom(img.zoomHelper!!.getCurrentZoom(), center!!.x, center.y, img.scaleType!!)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            setMeasuredDimension(0, 0)
            return
        }

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        zoomHelper?.viewWidth = zoomHelper!!.setViewSize(widthMode, widthSize, drawableWidth)
        zoomHelper?.viewHeight = zoomHelper!!.setViewSize(heightMode, heightSize, drawableHeight)

        setMeasuredDimension(zoomHelper!!.viewWidth, zoomHelper!!.viewHeight)

        zoomHelper?.fitImageToView()
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return zoomHelper!!.canScrollHorizontally(direction)
    }

    private var gestureListener =
            object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return doubleTapListener?.onSingleTapConfirmed(e) ?: performClick()
        }

        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            zoomHelper?.onFling(velocityX, velocityY)
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            var consumed = false
            if (doubleTapListener != null) {
                consumed = doubleTapListener.onDoubleTap(e)
            }
            if (zoomHelper?.state === State.NONE) {
                val targetZoom = if (zoomHelper!!.normalizedScale == zoomHelper!!.minScale) zoomHelper!!.maxScale else zoomHelper!!.minScale
                val doubleTap = DoubleTapZoom(targetZoom, e.x, e.y, false)
                compatPostOnAnimation(doubleTap)
                consumed = true
            }
            return consumed
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return doubleTapListener?.onDoubleTapEvent(e) ?: false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper?.onTouchEvent(event, Coordination(x, y), if (zoomHelper?.isZoomed() == true) true else event.pointerCount >1)
        gestureDetector?.onTouchEvent(event)

        mScaleDetector?.onTouchEvent(event)
        mGestureDetector?.onTouchEvent(event)
        zoomHelper?.onTouchEvent(event, dragHelper?.isDragging)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (dragHelper?.isDragging == true) return true
            zoomHelper?.state = State.ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (dragHelper?.isDragging == true) return true
            zoomHelper?.scaleImage(detector.scaleFactor.toDouble(), detector.focusX, detector.focusY, true)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (dragHelper?.isDragging == true) return

            super.onScaleEnd(detector)
            zoomHelper?.state = State.NONE
            var animateToZoomBoundary = false
            var targetZoom = zoomHelper!!.normalizedScale
            if (zoomHelper!!.normalizedScale > zoomHelper!!.maxScale) {
                targetZoom = zoomHelper!!.maxScale
                animateToZoomBoundary = true

            } else if (zoomHelper!!.normalizedScale < zoomHelper!!.minScale) {
                targetZoom = zoomHelper!!.minScale
                animateToZoomBoundary = true
            }

            if (animateToZoomBoundary) {
                val doubleTap = DoubleTapZoom(targetZoom, (zoomHelper!!.viewWidth / 2).toFloat(), (zoomHelper!!.viewHeight / 2).toFloat(), true)
                compatPostOnAnimation(doubleTap)
            }
        }
    }

    private inner class DoubleTapZoom internal constructor(private val targetZoom: Float, focusX: Float, focusY: Float, private val stretchImageToSuper: Boolean) : Runnable {

        private val startTime: Long
        private val startZoom: Float
        private val bitmapX: Float
        private val bitmapY: Float
        private val interpolator = AccelerateDecelerateInterpolator()
        private val startTouch: PointF
        private val endTouch: PointF

        init {
            zoomHelper!!.state = State.ANIMATE_ZOOM
            startTime = System.currentTimeMillis()
            this.startZoom = zoomHelper!!.normalizedScale
            val bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false)
            this.bitmapX = bitmapPoint.x
            this.bitmapY = bitmapPoint.y

            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY)
            endTouch = PointF((zoomHelper!!.viewWidth / 2).toFloat(), (zoomHelper!!.viewHeight / 2).toFloat())
        }

        override fun run() {
            val t = interpolate()
            val deltaScale = calculateDeltaScale(t)
            zoomHelper?.scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper)
            translateImageToCenterTouchPosition(t)
            zoomHelper?.fixScaleTrans()
            imageMatrix = zoomHelper!!.matrix

            if (t < 1f) {
                compatPostOnAnimation(this)
            } else {
                zoomHelper?.state = State.NONE
            }
        }

        private fun translateImageToCenterTouchPosition(t: Float) {
            val targetX = startTouch.x + t * (endTouch.x - startTouch.x)
            val targetY = startTouch.y + t * (endTouch.y - startTouch.y)
            val curr = transformCoordBitmapToTouch(bitmapX, bitmapY)
            zoomHelper?.matrix?.postTranslate(targetX - curr.x, targetY - curr.y)
        }

        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed = (currTime - startTime) / ZOOM_TIME
            elapsed = Math.min(1f, elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        private fun calculateDeltaScale(t: Float): Double {
            val zoom = (startZoom + t * (targetZoom - startZoom)).toDouble()
            return zoom / zoomHelper!!.normalizedScale
        }

        private val ZOOM_TIME = 500f
    }

    private fun transformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        zoomHelper?.matrix?.getValues(zoomHelper?.mArray)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val transX = zoomHelper!!.mArray!![Matrix.MTRANS_X]
        val transY = zoomHelper!!.mArray!![Matrix.MTRANS_Y]
        var finalX = (x - transX) * origW / zoomHelper!!.getImageWidth()
        var finalY = (y - transY) * origH / zoomHelper!!.getImageHeight()

        if (clipToBitmap) {
            finalX = Math.min(Math.max(finalX, 0f), origW)
            finalY = Math.min(Math.max(finalY, 0f), origH)
        }

        return PointF(finalX, finalY)
    }

    private fun transformCoordBitmapToTouch(bx: Float, by: Float): PointF {
        zoomHelper?.matrix?.getValues(zoomHelper!!.mArray)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val px = bx / origW
        val py = by / origH
        val finalX = zoomHelper!!.mArray!![Matrix.MTRANS_X] + zoomHelper!!.getImageWidth() * px
        val finalY = zoomHelper!!.mArray!![Matrix.MTRANS_Y] + zoomHelper!!.getImageHeight() * py
        return PointF(finalX, finalY)
    }

    fun canBack(): Boolean {
        return zoomHelper?.isZoomed() == true
    }

    private fun compatPostOnAnimation(runnable: Runnable) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(runnable)
        } else {
            postDelayed(runnable, (1000 / 60).toLong())
        }
    }

    fun onBackPressed() {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = 0.0f
        val y = 0.0f
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        )

        gestureListener.onDoubleTap(motionEvent)
    }
}