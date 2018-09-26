package uz.mukhammadakbar.image.viewer.extensions

import android.content.Context
import android.view.View

fun View.visible(isVisible: Boolean){
    if (isVisible) {
        this.visibility = View.VISIBLE
    } else{
        this.visibility = View.GONE
    }
}

fun Float.dp2Px(context: Context): Float = this * context.resources.displayMetrics.density

fun Float.px2Dp(context: Context): Float = this / context.resources.displayMetrics.density

fun Float.negative(): Boolean = this < 0

fun Float.positive(): Boolean = this > 0
