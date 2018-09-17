package uz.mukhammadakbar.image.viewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

class ImageViewer: AppCompatImageView {

    private var defaultImg: Int = 0
    private var errorImg: Int = 0
    private var imageUrl: String = ""

    constructor(context: Context): super(context){
        init()
    }

    constructor(context: Context, attr: AttributeSet): super(context, attr){
        init()
    }

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
            BitmapUtils(drawable).loadPhotos(resources)
            getLocationOnScreen(screenLocation)
            val activity =  context as Activity
            val subActivity = Intent(context,
                    PictureDetailsActivity::class.java)
            Log.d("screenLocation1", screenLocation[1].toString())
            Log.d("beetwenw", "size: ${(this.height - drawable.intrinsicHeight)/2}")
            subActivity.putExtra(PACKAGE + ".orientation",  resources.configuration.orientation)
                    .putExtra(PACKAGE + ".resourceId", defaultImg)
                    .putExtra(PACKAGE + ".errorImg", errorImg)
                    .putExtra(PACKAGE + ".url", imageUrl)
                    .putExtra(PACKAGE + ".left", screenLocation[0] + (this.width - drawable.intrinsicWidth)/2)
                    .putExtra(PACKAGE + ".top", screenLocation[1] + (this.height - drawable.intrinsicHeight)/2)
                    .putExtra(PACKAGE + ".width", drawable.intrinsicWidth)
                    .putExtra(PACKAGE + ".height", 2*drawable.intrinsicHeight)
            activity.startActivity(subActivity)

            activity.overridePendingTransition(0, 0)
        }
    }

    companion object {
        private const val PACKAGE = "uz.mukhammadakbar.ImageViewer"
    }
}