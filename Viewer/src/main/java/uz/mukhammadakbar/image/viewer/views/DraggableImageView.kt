package uz.mukhammadakbar.image.viewer.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import uz.mukhammadakbar.image.viewer.utils.Convertor
import java.util.*

class DraggableImageView : AppCompatImageView, GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector

    private var dX: Float = 0.toFloat()
    private var dY:Float = 0.toFloat()

    private lateinit var initialView: View
    private var dragCoefficient= 0f
    private var onDragChangeListener: OnDragChangeListener ? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        initialView = this
        gestureDetector = GestureDetector(context, this)
    }

    override fun onShowPress(event: MotionEvent?) {}

    override fun onSingleTapUp(event: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(event: MotionEvent?): Boolean = true

    override fun onFling(eventOld: MotionEvent?, eventNew: MotionEvent?, p2: Float, p3: Float)
            : Boolean = true

    override fun onScroll(event: MotionEvent?, event2: MotionEvent?, p2: Float, p3: Float): Boolean {
        if (event == null) return true

        if (event2 == null) return true

        dragCoefficient = Convertor.px2Dp(context, Convertor.findNearest(
                (event.rawX-event2.rawX).toDouble(),(event.rawY - event2.rawY).toDouble()))

        animate()
                .x(event2.rawX + dX)
                .y(event2.rawY + dY)
                .setDuration(0)
                .start()
        Log.d("difference", "$dragCoefficient")
        onDragChangeListener?.onDragChanged( if (dragCoefficient > 0) dragCoefficient else dragCoefficient*-1f)
        return true
    }

    override fun onLongPress(event: MotionEvent?) {

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = this.x - event.rawX
                dY = this.y - event.rawY
            }
            MotionEvent.ACTION_UP ->{
                Log.d("dragCoef", "coef:${Convertor.px2Dp(context, dragCoefficient)}")
                if (dragCoefficient in -100..100) {
                    animateDragToStart(initialView.left.toFloat(), initialView.top.toFloat())
                    dragCoefficient = if (dragCoefficient > 0) dragCoefficient else dragCoefficient*-1f
                    val coef = dragCoefficient / 10
                    Timer().schedule(object : TimerTask(){
                        override fun run() {
                            (context as Activity).runOnUiThread {
                                Log.d("timerTask", "$dragCoefficient")
                                onDragChangeListener?.onDragChanged(if (dragCoefficient > 0) dragCoefficient else dragCoefficient * -1f)
                                dragCoefficient -= coef
                                if (dragCoefficient < 5) this.cancel()
                            }
                        }
                    }, 0, INITITAL_STATE_TIME/10)
                }else{
                    animateDragToStart(1000f, -1000f)
                    dragCoefficient = if (dragCoefficient > 0) dragCoefficient else dragCoefficient*-1f
                    val coef = (255-dragCoefficient) / 10
                    Timer().schedule(object : TimerTask(){
                        override fun run() {
                            (context as Activity).runOnUiThread {
                                Log.d("timerTask", "$dragCoefficient")
                                onDragChangeListener?.onDragChanged(if (dragCoefficient > 0) dragCoefficient else dragCoefficient * -1f)
                                dragCoefficient += coef
                                if (dragCoefficient > 250) this.cancel()
                            }
                        }
                    }, 0, EXIT_ANIMATION_TIME/10)
                    handler.postDelayed({
                        onDragChangeListener?.onDragFinished()
                    }, EXIT_ANIMATION_TIME)
                }
            }
            else -> {
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    fun setOnDragChangeListener(onDragChangeListener: OnDragChangeListener){
        this.onDragChangeListener = onDragChangeListener
    }

    private fun animateDragToStart(left: Float, top: Float) {
        animate()
                .x(left)
                .y(top)
                .setInterpolator(LinearInterpolator())
                .setDuration(EXIT_ANIMATION_TIME)
                .start()
    }


    interface OnDragChangeListener{
        fun onDragFinished()
        fun onDragChanged(difference: Float)
    }

    companion object {
        private const val EXIT_ANIMATION_TIME = 200L
        private const val INITITAL_STATE_TIME = 400L
    }
}