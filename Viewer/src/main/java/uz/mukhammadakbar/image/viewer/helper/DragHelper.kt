package uz.mukhammadakbar.image.viewer.helper

import android.app.Activity
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import uz.mukhammadakbar.image.viewer.extensions.negative
import uz.mukhammadakbar.image.viewer.extensions.positive
import uz.mukhammadakbar.image.viewer.extensions.px2Dp
import uz.mukhammadakbar.image.viewer.listeners.OnCoordinationChangeListener
import uz.mukhammadakbar.image.viewer.listeners.OnDragChangeListener
import java.util.*
import android.os.Handler
import android.view.View
import uz.mukhammadakbar.image.viewer.utils.Converter
import uz.mukhammadakbar.image.viewer.utils.Coordination
import uz.mukhammadakbar.image.viewer.utils.DirectionsEnum

class DragHelper(private var context: Context): GestureDetector.OnGestureListener {

    private var direction: DirectionsEnum? = null
    private var onDragChangeListener: OnDragChangeListener? = null
    private var onCoordinationChangeListener: OnCoordinationChangeListener? = null

    private var isZoomed = false
    var isDragging = false

    private lateinit var initialView: View
    private var dX: Float = 0.toFloat()
    private var dY:Float = 0.toFloat()
    private var dragCoefficient= 0f

    override fun onShowPress(event: MotionEvent?) {}

    override fun onSingleTapUp(event: MotionEvent?): Boolean = true

    override fun onDown(event: MotionEvent?): Boolean = true

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = true

    override fun onScroll(event: MotionEvent?, event2: MotionEvent?, p2: Float, p3: Float): Boolean {
        if (isZoomed) return true

        isDragging = true

        if (event == null) return true

        if (event2 == null) return true

        dragCoefficient = Converter.findNearest(
                (event.rawX - event2.rawX).toDouble(), (event.rawY - event2.rawY).toDouble()).px2Dp(context)

        onCoordinationChangeListener?.coordinationChanged(
                Coordination(event2.rawX + dX, event2.rawY + dY), 0L)
        getDirection(Coordination(event2.rawX + dX, event2.rawY + dY))


        onDragChangeListener?.onDragChanged( if (dragCoefficient > 0) dragCoefficient else dragCoefficient*-1f)
        return true
    }

    override fun onLongPress(event: MotionEvent?) {}

    fun setInitialCord(initialView: View){
        this.initialView = initialView
    }

    private fun getDirection(cord: Coordination){
        if (cord.x.positive() && cord.y.positive()){
            direction = DirectionsEnum.NORTH_EAST
            return
        }
        if (cord.x.positive() && cord.y.negative()){
            direction = DirectionsEnum.SOUTH_EAST
            return
        }
        if (cord.x.negative() && cord.y.positive()){
            direction = DirectionsEnum.NORTH_WEST
            return
        }
        if (cord.x.negative() && cord.y.negative()){
            direction = DirectionsEnum.SOUTH_WEST
            return
        }
    }

    private fun getCoordination(): Coordination?{
        return when(direction){
            DirectionsEnum.NORTH_EAST -> Coordination(1000f, 1000F)

            DirectionsEnum.SOUTH_EAST -> Coordination(1000f, -1000F)

            DirectionsEnum.NORTH_WEST -> Coordination(-1000f, 1000f)

            DirectionsEnum.SOUTH_WEST -> Coordination(-1000f, -1000f)

            else -> null
        }
    }

    fun onTouchEvent(event: MotionEvent?, viewCord: Coordination, isZoomed: Boolean?) {
        this.isZoomed = isZoomed == true
        if (this.isZoomed){
            return
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = viewCord.x - event.rawX
                dY = viewCord.y - event.rawY
            }
            MotionEvent.ACTION_UP ->{
                if (dragCoefficient in -100..100) {
                    backToInitialState()
                }else{
                    exitAnimation()
                }
            }
            else -> {

            }
        }
    }

    private fun exitAnimation() {
        getCoordination()?.let {coordination ->
            onCoordinationChangeListener?.coordinationChanged(coordination, EXIT_ANIMATION_TIME)
        }
        dragCoefficient = if (dragCoefficient > 0) dragCoefficient else dragCoefficient*-1f
        val coefficient = (255-dragCoefficient) / 10
        Timer().schedule(object : TimerTask(){
            override fun run() {
                (context as Activity).runOnUiThread {
                    onDragChangeListener?.onDragChanged(if (dragCoefficient > 0) dragCoefficient else dragCoefficient * -1f)
                    dragCoefficient += coefficient
                    if (dragCoefficient > 250) this.cancel()
                }
            }
        }, 0, EXIT_ANIMATION_TIME /10)
        val handler = Handler()
        handler.postDelayed({
            onDragChangeListener?.onDragFinished()
            isDragging = false
        }, EXIT_ANIMATION_TIME)
    }

    private fun backToInitialState() {
        onCoordinationChangeListener?.coordinationChanged(
                Coordination(initialView.left.toFloat(), initialView.top.toFloat()), EXIT_ANIMATION_TIME)
        dragCoefficient = if (dragCoefficient > 0) dragCoefficient else dragCoefficient*-1f
        val coefficient = dragCoefficient / 10
        Timer().schedule(object : TimerTask(){
            override fun run() {
                (context as Activity).runOnUiThread {
                    onDragChangeListener?.onDragChanged(if (dragCoefficient > 0) dragCoefficient else dragCoefficient * -1f)
                    dragCoefficient -= coefficient
                    if (dragCoefficient < 5) this.cancel()
                }
            }
        }, 0, INITIAL_STATE_TIME /10)

        isDragging = false
    }

    fun setOnCoordinationChangeListener(onCoordinationChangeListener: OnCoordinationChangeListener){
        this.onCoordinationChangeListener = onCoordinationChangeListener
    }

    fun setOnDragChangeListener(onDragChangeListener: OnDragChangeListener){
        this.onDragChangeListener = onDragChangeListener
    }

    companion object {
        private const val EXIT_ANIMATION_TIME = 200L
        private const val INITIAL_STATE_TIME = 400L
    }
}