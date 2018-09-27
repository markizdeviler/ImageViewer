package uz.mukhammadakbar.image.viewer.views

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import uz.mukhammadakbar.image.viewer.listeners.OnCoordinationChangeListener
import uz.mukhammadakbar.image.viewer.listeners.OnDragChangeListener
import uz.mukhammadakbar.image.viewer.utils.Coordination
import uz.mukhammadakbar.image.viewer.utils.DragHelper


class DraggableImageView : AppCompatImageView, OnCoordinationChangeListener {

    private var dragHelper = DragHelper(context)
    private lateinit var gestureDetector: GestureDetector


    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        dragHelper.setInitialCord(this)
        dragHelper.setOnCoordinationChangeListener(this)
        gestureDetector = GestureDetector(context, dragHelper)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        dragHelper.onTouchEvent(event, Coordination(this.x, this.y))
        gestureDetector.onTouchEvent(event)
        return true
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
        dragHelper.setOnDragChangeListener(onDragChangeListener)
    }
}