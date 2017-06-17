package greenviews.lib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

import kifio.greenviews.R;

public class GreenSwitch extends View implements ValueAnimator.AnimatorUpdateListener {

    private float mWidth, mHeight;
    private float mTrackDistance;
    private float mThumbStartPosition;
    private float mThumbFinishPosition;
    private float mCornerRadius;
    private float mStep;
    private float mStartPadding;
    private float mEndPadding;

    private Paint mStrokePaint;
    private Paint mFillPaint;
    private Paint mBackgroundPaint;

    private RectF mBorder = new RectF();
    private RectF mCircle = new RectF();
    private RectF mBackground = new RectF();
    private RectF mBackgroundWhite = new RectF();

    private boolean mEnabled;
    private boolean mAnimInProgress;

    public GreenSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        mWidth = res.getDimension(R.dimen.switch_track_width);
        mHeight = res.getDimension(R.dimen.switch_track_height);
        mCornerRadius = res.getDimension(R.dimen.switch_track_corner);
        mTrackDistance = mWidth - mHeight;
        mStep = mTrackDistance / 400;
        float density = res.getDisplayMetrics().density;
        mStartPadding = 2.2f * density;
        mEndPadding = mHeight - 4.4f * density;
        buildFillPaint();
        buildStrokePaint(density);
        buildBackgroundPaint();
        setLayerType(LAYER_TYPE_SOFTWARE, mStrokePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            buildRect(mBorder);
            buildRect(mBackground);
            buildRect(mBackgroundWhite);
            int width = (int) mWidth + getPaddingLeft() + getPaddingRight();
            int height = (int) mHeight + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(width, height);
            mThumbStartPosition = mStartPadding + getPaddingLeft();
            mThumbFinishPosition = mThumbStartPosition + mTrackDistance;
            mCircle.left = mThumbStartPosition;
            mCircle.right = mCircle.left + mEndPadding;
            mCircle.top = mStartPadding + getPaddingTop();
            mCircle.bottom = mCircle.top + mEndPadding;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(mBackground, mCornerRadius, mCornerRadius, mBackgroundPaint);
        canvas.drawRoundRect(mBackgroundWhite, mCornerRadius, mCornerRadius, mFillPaint);
        canvas.drawOval(mCircle, mStrokePaint);
        canvas.drawOval(mCircle, mFillPaint);
        canvas.drawRoundRect(mBorder, mCornerRadius, mCornerRadius, mStrokePaint);
    }

    private void buildRect(RectF rectF) {
        rectF.left = getPaddingLeft();
        rectF.top = getPaddingTop();
        rectF.right = mWidth+ getPaddingLeft();
        rectF.bottom = mHeight+ getPaddingTop();
    }

    private void buildFillPaint() {
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(Color.WHITE);
    }

    private void buildStrokePaint(float density) {
        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setShadowLayer(2 * density, 0 * density, 0, Color.BLACK);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setColor(Color.WHITE);
        mStrokePaint.setStrokeWidth(density * 2f);
    }

    private void buildBackgroundPaint() {
        AssetManager assetManager = getContext().getAssets();
        try {
            String filename = "grass.webp";
            InputStream is = assetManager.open(filename, AssetManager.ACCESS_BUFFER);
            Bitmap bmp = BitmapFactory.decodeStream(is);

            BitmapShader shader = new BitmapShader(bmp,
                    Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setAntiAlias(true);
            mBackgroundPaint.setShader(shader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (!mAnimInProgress) {
                mAnimInProgress = true;
                ValueAnimator animator = ValueAnimator
                        .ofInt(0, 1)
                        .setDuration(400);
                animator.addUpdateListener(this);
                animator.start();
            }
        }
        return false;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        long currentPlayTime = animation.getCurrentPlayTime();
        if (!mEnabled) {
            mCircle.left = mThumbStartPosition + mStep * currentPlayTime;
        } else {
            mCircle.left = mThumbFinishPosition - mStep * currentPlayTime;
        }
        if ((int) animation.getAnimatedValue() == 1) {
            mEnabled = !mEnabled;
            mAnimInProgress = false;
        }
        mCircle.right = mCircle.left + mEndPadding;
        mBackgroundWhite.left = mCircle.left;
        invalidate();
    }
}
