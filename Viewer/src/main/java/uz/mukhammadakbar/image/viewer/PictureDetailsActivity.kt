package uz.mukhammadakbar.image.viewer

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView

class PictureDetailsActivity : Activity() {

    private var mBitmapDrawable: BitmapDrawable? = null
    private val colorizerMatrix = ColorMatrix()
    internal lateinit var mBackground: ColorDrawable
    internal var mLeftDelta: Int = 0
    internal var mTopDelta: Int = 0
    internal var mWidthScale: Float = 0.toFloat()
    internal var mHeightScale: Float = 0.toFloat()
    private var drawableHeight: Int? = 0
    private var drawableWidth: Int? = 0
    private var mImageView: PhotoView? = null
    private var mTopLevelLayout: FrameLayout? = null
    private var mShadowLayout: ShadowLayout? = null
    private var mOriginalOrientation: Int = 0
    private var isResourcesReady: Boolean = false
    private var isAnimated: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_info)
        mImageView = findViewById<PhotoView>(R.id.imageView)
        mTopLevelLayout = findViewById(R.id.topLevelLayout)
        mShadowLayout = findViewById(R.id.shadowLayout)

        val bundle = intent.extras
        val drawable = bundle!!.getInt("$PACKAGE_NAME.resourceId", 0)
        val errorDrawable = bundle.getInt("$PACKAGE_NAME.errorImg", 0)
        val url = bundle.getString("$PACKAGE_NAME.url", "")
        Log.d("urlImage", "url: $url")

        val thumbnailTop = bundle.getInt("$PACKAGE_NAME.top")
        val thumbnailLeft = bundle.getInt("$PACKAGE_NAME.left")
        val thumbnailWidth = bundle.getInt("$PACKAGE_NAME.width")
        val thumbnailHeight = bundle.getInt("$PACKAGE_NAME.height")
        mOriginalOrientation = bundle.getInt("$PACKAGE_NAME.orientation")

        mBackground = ColorDrawable(Color.BLACK)
        mTopLevelLayout?.background = mBackground
        var bitmap: Bitmap? = null
        when {
            url != "" -> Glide.with(applicationContext).load(url)
                    .apply(RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(errorDrawable)
                            .fitCenter())
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            Log.d("failed", "failed")
                            return false
                        }

                        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            bitmap = BitmapUtils.getBitmap(resources, resource)
                            mBitmapDrawable = BitmapDrawable(resources, bitmap)
                            mImageView?.setImageDrawable(mBitmapDrawable)

                            if (!isAnimated && !isResourcesReady){
                                readyForStartAnimation(savedInstanceState, thumbnailWidth, thumbnailHeight,
                                        thumbnailLeft, thumbnailTop)
                                isResourcesReady = true
                                isAnimated = true
                            }
                            Log.d("onResourceReady", "onResourceReady")

                            return false
                        }
                    })
                    .into(mImageView!!)
            drawable != 0 -> {
                bitmap = BitmapUtils.getBitmap(resources, ContextCompat.getDrawable(applicationContext, drawable))
                mBitmapDrawable = BitmapDrawable(resources, bitmap)
                mImageView?.setImageDrawable(mBitmapDrawable)
            }
            errorDrawable != 0 -> {
                bitmap = BitmapUtils.getBitmap(resources, ContextCompat.getDrawable(applicationContext, errorDrawable))
                mBitmapDrawable = BitmapDrawable(resources, bitmap)
                mImageView?.setImageDrawable(mBitmapDrawable)
            }
        }

        if (isResourcesReady && !isAnimated) {
            readyForStartAnimation(savedInstanceState, thumbnailWidth, thumbnailHeight,
                    thumbnailLeft, thumbnailTop)
            Log.d("isResourcesReady", "isResourcesReady")
            isAnimated = true
            isResourcesReady = true
        }
    }

    fun readyForStartAnimation(savedInstanceState: Bundle?, thumbnailWidth: Int,
                               thumbnailHeight: Int, thumbnailLeft: Int, thumbnailTop: Int){
        Log.d("savedInstance","${savedInstanceState == null}")
        if (savedInstanceState == null) {
            val observer = mImageView!!.viewTreeObserver
            observer.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {

                override fun onPreDraw(): Boolean {
                    mImageView!!.viewTreeObserver.removeOnPreDrawListener(this)

                    val screenLocation = IntArray(2)
                    mImageView?.getLocationOnScreen(screenLocation)
                    mLeftDelta = thumbnailLeft - screenLocation[0]
                    mTopDelta = thumbnailTop - screenLocation[1]-100
                    Log.d("screebLocation", screenLocation[1].toString())
                    Log.d("thumbnailTop", thumbnailTop.toString())
                    Log.d("thumbnailHeight", thumbnailHeight.toString())

                    mWidthScale = thumbnailWidth.toFloat() / mImageView!!.width
                    mHeightScale = (thumbnailHeight.toFloat() / mImageView!!.height)
                    runEnterAnimation()
                    return true
                }
            })
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun runEnterAnimation() {
        val duration = ANIM_DURATION.toLong()
        Log.d("runEnterANimation", "run Enter")

        mImageView!!.pivotX = 0f
        mImageView!!.pivotY = 0f
        mImageView!!.scaleX = mWidthScale
        mImageView!!.scaleY = mHeightScale
        mImageView!!.translationX = mLeftDelta.toFloat()
        mImageView!!.translationY = mTopDelta.toFloat()


        mImageView?.animate()
                ?.setDuration(duration)
                ?.scaleX(1f)
                ?.scaleY(1f)
                ?.translationX(0f)
                ?.translationY(0f)
                ?.interpolator = sDecelerator

        ObjectAnimator.ofInt(mBackground,
                "alpha", 0, 255).apply {
            this.duration = duration
            start()
        }

        ObjectAnimator.ofFloat(this@PictureDetailsActivity,
                "saturation", 0f, 1f).apply {
            this.duration = duration
            start()
        }

        ObjectAnimator.ofFloat(mShadowLayout, "shadowDepth", 0f, 1f).apply {
            this.duration = duration
            start()
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun runExitAnimation(endAction: Runnable) {
        val duration = ANIM_DURATION.toLong()
        val fadeOut: Boolean
        if (resources.configuration.orientation != mOriginalOrientation) {
            mImageView?.pivotX = (mImageView!!.width / 2).toFloat()
            mImageView?.pivotY = (mImageView!!.height / 2).toFloat()
            mLeftDelta = 0
            mTopDelta = 0
            fadeOut = true
        } else {
            fadeOut = false
        }

        Log.d("mLeftDelta", mLeftDelta.toString())
        Log.d("mTopDelta", mTopDelta.toString())
        mImageView?.animate()
                ?.setDuration(duration)
                ?.scaleX(mWidthScale)
                ?.scaleY(mHeightScale)
                ?.translationX(mLeftDelta.toFloat())
                ?.translationY(mTopDelta.toFloat())
                ?.withEndAction(endAction)
        if (fadeOut) {
            mImageView?.animate()?.alpha(0f)
        }

        ObjectAnimator.ofInt(mBackground, "alpha", 0).apply {
            this.duration = duration
            start()
        }

        ObjectAnimator.ofFloat(mShadowLayout,
                "shadowDept", 1f, 0f).apply {
            this.duration = duration
            start()
        }

        ObjectAnimator.ofFloat(this@PictureDetailsActivity,
                "saturation", 1f, 0f).apply {
            this.duration = duration
            start()
        }
    }

    override fun onBackPressed() {
        mImageView?.scale = 1f
        runExitAnimation(Runnable { finish() })
    }

    fun setSaturation(value: Float) {
        colorizerMatrix.setSaturation(value)
        val colorizerFilter = ColorMatrixColorFilter(colorizerMatrix)
        mBitmapDrawable?.colorFilter = colorizerFilter
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    companion object {

        private val sDecelerator = DecelerateInterpolator()
        private val sAccelerator = AccelerateInterpolator()
        private val PACKAGE_NAME = "uz.mukhammadakbar.ImageViewer"
        private val ANIM_DURATION = 400
    }
}