package uz.mukhammadakbar.image.viewer.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import uz.mukhammadakbar.image.viewer.R
import uz.mukhammadakbar.image.viewer.listeners.OnDragChangeListener
import uz.mukhammadakbar.image.viewer.utils.Constants
import uz.mukhammadakbar.image.viewer.views.DraggableImageView
import uz.mukhammadakbar.image.viewer.views.ShadowLayout

class PictureDetailsActivity : Activity() {

    private var mBitmapDrawable: BitmapDrawable? = null
    private val colorizeMatrix = ColorMatrix()
    private lateinit var mBackground: ColorDrawable
    private var mLeftDelta: Int = 0
    private var mTopDelta: Int = 0
    private var mWidthScale: Float = 0.toFloat()
    private var mHeightScale: Float = 0.toFloat()
    private lateinit var mImageView: DraggableImageView
    private lateinit var mTopLevelLayout: FrameLayout
    private lateinit var mShadowLayout: ShadowLayout
    private var mOriginalOrientation: Int = 0

    private var isResourcesReady: Boolean = false
    private var isAnimated: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_info)
        mImageView = findViewById(R.id.imageView)
        mTopLevelLayout = findViewById(R.id.topLevelLayout)
        mShadowLayout = findViewById(R.id.shadowLayout)

        val bundle = intent.extras
        val drawable = bundle.getInt(Constants.DEFAULT_IMG, 0)
        val errorDrawable = bundle.getInt(Constants.ERROR_IMG, 0)
        val url = bundle.getString(Constants.IMAGE_URL, "")
        val thumbnailTop = bundle.getInt(Constants.Y_COORD)
        val thumbnailLeft = bundle.getInt(Constants.X_COORD)
        val thumbnailWidth = bundle.getInt(Constants.WIDTH)
        val thumbnailHeight = bundle.getInt(Constants.HEIGHT)
        mOriginalOrientation = bundle.getInt(Constants.ORIENTATION)

        mBackground = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.black))
        mTopLevelLayout.background = mBackground
        var bitmap: Bitmap?
        when {
            url != "" -> Glide.with(applicationContext).load(url)
                    .apply(RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(errorDrawable)
                            .fitCenter())
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            bitmap = (resource as BitmapDrawable).bitmap
                            mBitmapDrawable = BitmapDrawable(resources, bitmap)
                            mImageView.setImageDrawable(mBitmapDrawable)

                            if (!isAnimated && !isResourcesReady){
                                readyForStartAnimation(savedInstanceState, thumbnailWidth, thumbnailHeight,
                                        thumbnailLeft, thumbnailTop)
                                isResourcesReady = true
                                isAnimated = true
                            }
                            return false
                        }
                    })
                    .into(mImageView)
            drawable != 0 -> {
                bitmap = ContextCompat.getDrawable(applicationContext, drawable)?.let { (it as BitmapDrawable).bitmap }
                mBitmapDrawable = BitmapDrawable(resources, bitmap)
                mImageView.setImageDrawable(mBitmapDrawable)
            }
            errorDrawable != 0 -> {
                bitmap = ContextCompat.getDrawable(applicationContext, errorDrawable)?.let { (it as BitmapDrawable).bitmap }
                mBitmapDrawable = BitmapDrawable(resources, bitmap)
                mImageView.setImageDrawable(mBitmapDrawable)
            }
        }

        if (isResourcesReady && !isAnimated) {
            readyForStartAnimation(savedInstanceState, thumbnailWidth, thumbnailHeight,
                    thumbnailLeft, thumbnailTop)
            isAnimated = true
            isResourcesReady = true
        }

        mImageView.setOnDragChangeListener(object : OnDragChangeListener {
            override fun onDragFinished() {
                finish()
            }

            override fun onDragChanged(difference: Float) {
                val differ = if (difference > 255) 0f else 255-difference
                mTopLevelLayout.background.alpha  = differ.toInt()
            }
        })

        // make toolbar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    fun readyForStartAnimation(savedInstanceState: Bundle?, thumbnailWidth: Int,
                               thumbnailHeight: Int, thumbnailLeft: Int, thumbnailTop: Int){
        if (savedInstanceState == null) {
            val observer = mImageView.viewTreeObserver
            observer.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    mImageView.viewTreeObserver.removeOnPreDrawListener(this)
                    val screenLocation = IntArray(2)
                    mImageView.getLocationOnScreen(screenLocation)
                    mLeftDelta = thumbnailLeft - screenLocation[0]
                    mTopDelta = thumbnailTop - screenLocation[1]-100
                    mWidthScale = thumbnailWidth.toFloat() / mImageView.width
                    mHeightScale = (thumbnailHeight.toFloat() / mImageView.height)
                    runEnterAnimation()
                    return true
                }
            })
        }
    }

    fun runEnterAnimation() {
        val duration = ANIM_DURATION.toLong()
        mImageView.pivotX = 0f
        mImageView.pivotY = 0f
        mImageView.scaleX = mWidthScale
        mImageView.scaleY = mHeightScale
        mImageView.translationX = mLeftDelta.toFloat()
        mImageView.translationY = mTopDelta.toFloat()

        mImageView.animate()
                .setDuration(duration)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .interpolator = sDecelerator

        ObjectAnimator.ofInt(mBackground, "alpha", 0, 255).apply {
            this.duration = duration
            start()
        }

        ObjectAnimator.ofFloat(this, "saturation", 0f, 1f).apply {
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
            mImageView.pivotX = (mImageView.width / 2).toFloat()
            mImageView.pivotY = (mImageView.height / 2).toFloat()
            mLeftDelta = 0
            mTopDelta = 0
            fadeOut = true
        } else {
            fadeOut = false
        }
        mImageView.animate()
                .setDuration(duration)
                .scaleX(mWidthScale)
                .scaleY(mHeightScale)
                .translationX(mLeftDelta.toFloat())
                .translationY(mTopDelta.toFloat())
                .withEndAction(endAction)
        if (fadeOut) {
            mImageView.animate()?.alpha(0f)
        }

        ObjectAnimator.ofInt(mBackground, "alpha", 0).apply {
            this.duration = duration
            start()
        }

        ObjectAnimator.ofFloat(mShadowLayout, "shadowDept", 1f, 0f).apply {
            this.duration = duration
            start()
        }

        ObjectAnimator.ofFloat(this@PictureDetailsActivity, "saturation", 1f, 0f).apply {
            this.duration = duration
            start()
        }
    }

    override fun onBackPressed() {
        if (mImageView.canBack()){
            mImageView.onBackPressed()
        }else {
            runExitAnimation(Runnable { finish() })
        }
    }

    fun setSaturation(value: Float) {
        colorizeMatrix.setSaturation(value)
        val colorizeFilter = ColorMatrixColorFilter(colorizeMatrix)
        mBitmapDrawable?.colorFilter = colorizeFilter
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        private val sDecelerator = DecelerateInterpolator()
        private const val ANIM_DURATION = 400
    }
}