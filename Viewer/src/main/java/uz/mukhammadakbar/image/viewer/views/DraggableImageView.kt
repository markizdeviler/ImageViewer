package uz.mukhammadakbar.image.viewer.views

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator


class DraggableImageView : AppCompatImageView, GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    override fun onShowPress(event: MotionEvent?) {

    }

    override fun onSingleTapUp(event: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(event: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(eventOld: MotionEvent?, eventNew: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onScroll(event: MotionEvent?, event2: MotionEvent?, p2: Float, p3: Float): Boolean {
        val difference= event?.x?.minus(event2?.x?:0f)?:0f
        Log.d("difference", "${if (difference > 0) difference else difference*-1}")
        onDragChangeListener?.onDragChanged(if (difference > 0) difference else difference*-1)
        return true
    }

    override fun onLongPress(event: MotionEvent?) {

    }

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

    private fun init() {
        initialView = this
        gestureDetector = GestureDetector(context, this)
        this.setOnTouchListener { view, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent )
        }

    }




    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = this.x - event.rawX
                dY = this.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
            }
            MotionEvent.ACTION_UP ->{
                dragCoefficient = event.rawX + dX
                if (dragCoefficient in -100..100) {
                    animateDragToStart(initialView.left.toFloat(), initialView.top.toFloat())
                }else{
                    animateDragToStart(1000f, -1000f)
                    onDragChangeListener?.onDragFinished()
                }
            }
            else -> {
            }
        }
        performClick()
        return gestureDetector.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    fun setOnDragChangeListener(onDragChangeListener: OnDragChangeListener){
        this.onDragChangeListener = onDragChangeListener
    }

    private fun animateDragToStart(left: Float, top: Float) {
        animate()
                .x(left)
                .y(top)
                .setInterpolator(LinearInterpolator())
                .setDuration(200)
                .start()
    }


    interface OnDragChangeListener{

        fun onDragFinished()

        fun onDragChanged(difference: Float)
    }
}