package uz.mukhammadakbar.image.viewer.utils

import android.content.Context

object Convertor {

    fun dp2Px(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun px2Dp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun findNearest(differX: Double, differY: Double): Float{
        return Math.sqrt(Math.pow(differX, 2.0) + Math.pow(differY, 2.0)).toFloat()
    }
}