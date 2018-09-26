package uz.mukhammadakbar.image.viewer.listeners

import android.view.ScaleGestureDetector
import android.view.View

class OnScaleListener(private var mScaleFactor: Float, private var view: View)
    : ScaleGestureDetector.SimpleOnScaleGestureListener() {

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        mScaleFactor *= scaleGestureDetector.scaleFactor
        mScaleFactor = Math.max(0.1f,
                Math.min(mScaleFactor, 10.0f))
        view.scaleX = mScaleFactor
        view.scaleY = mScaleFactor
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return super.onScaleBegin(detector)
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        super.onScaleEnd(detector)
    }

}