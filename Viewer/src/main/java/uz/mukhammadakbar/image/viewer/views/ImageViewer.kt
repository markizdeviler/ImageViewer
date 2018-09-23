package uz.mukhammadakbar.image.viewer.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import uz.mukhammadakbar.image.viewer.utils.Constants
import uz.mukhammadakbar.image.viewer.ui.PictureDetailsActivity
import android.content.ContextWrapper



class ImageViewer: AppCompatImageView {

    private var defaultImg: Int = 0
    private var errorImg: Int = 0
    private var imageUrl: String = ""

    constructor(context: Context): super(context){ init() }

    constructor(context: Context, attr: AttributeSet): super(context, attr){ init() }

    fun imageUrl(url: String){
        imageUrl = url
        if (URLUtil.isValidUrl(url))
            Glide.with(context).load(url)
                    .apply(RequestOptions()
                            .placeholder(defaultImg.let { ContextCompat.getDrawable(context, it) })
                            .fitCenter())
                    .into(this)
        else{
            setImageDrawable(errorImg.let { ContextCompat.getDrawable(context, it) })
        }
    }

    fun imageId(@DrawableRes drawable: Int){
        setImageDrawable(ContextCompat.getDrawable(context, drawable))
        defaultImg = drawable
    }

    fun errorImg(@DrawableRes drawable: Int){
        setImageDrawable(ContextCompat.getDrawable(context, drawable))
        errorImg = drawable
    }

    private fun init() {
        initListener()
    }

    private fun initListener() {
        setOnClickListener {
            val screenLocation = IntArray(2)
            getLocationOnScreen(screenLocation)
            val activity =  scanForActivity(context)
            val subActivity = Intent(context,
                    PictureDetailsActivity::class.java)
            subActivity.putExtra(Constants.ORIENTATION,  resources.configuration.orientation)
                    .putExtra(Constants.DEFAULT_IMG, defaultImg)
                    .putExtra(Constants.ERROR_IMG, errorImg)
                    .putExtra(Constants.IMAGE_URL, imageUrl)
                    .putExtra(Constants.X_COORD, screenLocation[0] + (this.width - drawable.intrinsicWidth)/2)
                    .putExtra(Constants.Y_COORD, screenLocation[1] + (this.height - drawable.intrinsicHeight)/2)
                    .putExtra(Constants.WIDTH, drawable.intrinsicWidth)
                    .putExtra(Constants.HEIGHT, 2*drawable.intrinsicHeight)
            activity?.startActivity(subActivity)

            activity?.overridePendingTransition(0, 0)
        }
    }

    private fun scanForActivity(cont: Context?): Activity? {
        return when (cont) {
            null -> null
            is Activity -> cont
            is ContextWrapper -> scanForActivity(cont.baseContext)
            else -> null
        }
    }
}