package uz.mukhammadakbar.image.viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class ShadowLayout extends RelativeLayout {

    Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    float mShadowDepth;
    Bitmap mShadowBitmap;
    static final int BLUR_RADIUS = 200;
    static final RectF sShadowRectF = new RectF(0, 0, 200, 200);
    static final Rect sShadowRect = new Rect(0, 0, 200 + 2 * BLUR_RADIUS, 200 + 2 * BLUR_RADIUS);
    static RectF tempShadowRectF = new RectF(0, 0, 0, 0);

    public ShadowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ShadowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShadowLayout(Context context) {
        super(context);
        init();
    }


    private void init() {
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setStyle(Style.FILL);
        setWillNotDraw(false);
        mShadowBitmap = Bitmap.createBitmap(sShadowRect.width(),
                sShadowRect.height(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mShadowBitmap);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(BLUR_RADIUS, Blur.NORMAL));
        c.translate(BLUR_RADIUS, BLUR_RADIUS);
        c.drawRoundRect(sShadowRectF, sShadowRectF.width() / 40,
                sShadowRectF.height() / 40, mShadowPaint);
    }

    public void setShadowDepth(float depth) {
        if (depth != mShadowDepth) {
            mShadowDepth = depth;
            mShadowPaint.setAlpha((int) (100 + 150 * (1 - mShadowDepth)));
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getAlpha() == 0) {
                continue;
            }
            int depthFactor = (int) (80 * mShadowDepth);
            canvas.save();
            canvas.translate(child.getLeft() + depthFactor,
                    child.getTop() + depthFactor);
            canvas.concat(child.getMatrix());
            tempShadowRectF.right = child.getWidth();
            tempShadowRectF.bottom = child.getHeight();
            canvas.drawBitmap(mShadowBitmap, sShadowRect, tempShadowRectF, mShadowPaint);
            canvas.restore();
        }
    }
}