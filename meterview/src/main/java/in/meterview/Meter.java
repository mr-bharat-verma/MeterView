package in.meterview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import meterview.in.meterview.R;

/**
 * Created by Bharat on 19/11/15.
 */
public class Meter extends View {
    private static final String TAG = "MyClock";
    private static final int DEFAULT_NICKS_COUNT = 8;
    private static final int DEFAULT_MAX_VALUE_ON_SCALE = 100;  // provided, 0 is minimum.
    private static final int DEFAULT_NICKS_UNDER_ALERT = 2;    // Red color nicks
    private static final int DEFAULT_THRESHOLD_VALUE = 33;    // Red Area
    private static final float DEFAULT_ARC_ANGLE = 130f;        // In Degrees
    private static final int DEFAULT_ALERT_COLOR_CODE = 0xfff9000A;
    private static final int DEFAULT_SCALE_COLOR_CODE = 0xff000000;
    private static final int PREFERRED_WIDTH_IN_DP = 100;
    private  final float DEFAULT_NICK_LENGTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());//120px;
    private  final float DEFAULT_NICK_WIDTH =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());//35px;
    private  final float DEFAULT_RIM_WIDTH =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6.4f, getResources().getDisplayMetrics());//16px;
    private  final float DEFAULT_SCREW_RADIUS =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 23f, getResources().getDisplayMetrics());//70px;
    private  final float DEFAULT_FONT_SIZE =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, getResources().getDisplayMetrics());//45px;

    private int mTotalNicksCount = DEFAULT_NICKS_COUNT;
    private int mMaxValue = DEFAULT_MAX_VALUE_ON_SCALE;
    private int mRedNicks = 2;
    private float mCurveAngle = DEFAULT_ARC_ANGLE;
    private float mDegreesPerNick = -(mCurveAngle / mTotalNicksCount);
    private int mWarningColor = DEFAULT_ALERT_COLOR_CODE;
    private float mNickLength = DEFAULT_NICK_LENGTH;
    private float mNickWidth = DEFAULT_NICK_WIDTH;
    private float mRimWidth = DEFAULT_RIM_WIDTH;
    private int mScaleColor = DEFAULT_SCALE_COLOR_CODE;
    private int mAlertColor = DEFAULT_ALERT_COLOR_CODE;
    private float mFontSize = DEFAULT_FONT_SIZE;
    private float mScrewRadius = DEFAULT_SCREW_RADIUS;
    private float mCurrentValue = 0f;

    private int PADDING = 10;
    private float X1,X2,Y1,Y2;
    private float centerRectX = 0;
    private float centerRectY = 0;
    private float startAngle = 90f + ((180f - mCurveAngle) / 2f);
    private float preRotateAngle = startAngle - 90;
    private Paint mScalePaint;
    private Paint mReadingTextPaint;
    private Paint mScrewGradientPaint;
    private Paint mScrewPaint;
    private Paint mScrewReflectionPaint;
    private RectF mScaleRect;
    private Paint mNeedlePaint;

    private static float heightToWidthRatio = 1.86f;

    public Meter(Context context) {
        super(context);
    }

    public Meter(Context context, AttributeSet attrs) {
        super(context, attrs);

        setValuesFromAttributes(context, attrs);

    }

    private void setValuesFromAttributes(Context context, AttributeSet attrs) {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.meter,0, 0);
        setTotalNicksCount(attributes.getInteger(R.styleable.meter_totalNicks,DEFAULT_NICKS_COUNT));
        setNickLength(attributes.getFloat(R.styleable.meter_nickLength, DEFAULT_NICK_LENGTH));
        setNickWidth(attributes.getFloat(R.styleable.meter_nickWidth, DEFAULT_NICK_WIDTH));
        setRimWidth(attributes.getFloat(R.styleable.meter_rimWidth, DEFAULT_RIM_WIDTH));
        setAlertColor(attributes.getColor(R.styleable.meter_alertColor, DEFAULT_ALERT_COLOR_CODE));
        setScaleColor(attributes.getColor(R.styleable.meter_scaleColor, DEFAULT_SCALE_COLOR_CODE));
        setMaxValue(attributes.getInteger(R.styleable.meter_maxValue, DEFAULT_MAX_VALUE_ON_SCALE));
        setFontSize(attributes.getFloat(R.styleable.meter_fontSize, DEFAULT_FONT_SIZE));
        setCurveAngle(attributes.getFloat(R.styleable.meter_curveAngle, DEFAULT_ARC_ANGLE));
        setScrewRadius(attributes.getFloat(R.styleable.meter_screwRadius, DEFAULT_SCREW_RADIUS));
        setCurrentValue(attributes.getFloat(R.styleable.meter_currentValue, 0f));
    }

    public Meter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, String.format("Layout: changed %s, left = %d, top = %d, right = %d, bottom = %d ", changed, left, top, right, bottom));
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        Log.d(TAG, String.format("onDraw Dimentions[%s x %s]: ", getWidth(), getHeight()));
        mScrewRadius = getWidth() / 12;
        mRimWidth = getWidth() / 60;
        X1 = PADDING + mFontSize;
        X2 = 2 * (getWidth() - X1 - mScrewRadius);
        Y1 = X1;
        Y2 = X2;
        centerRectX = X1 + (X2 - X1) / 2;
        centerRectY = Y1 + (Y2 - Y1) / 2;

        mFontSize = mRimWidth * 5;
        mScaleRect = new RectF(X1 + mFontSize, Y1 + mFontSize, X2 - mFontSize, Y2 - mFontSize);
        initializePaintElements();
        drawScale(canvas, mScaleRect, mScalePaint, mReadingTextPaint);
        drawNeedle(canvas);
        drawScrew(canvas);
        setCurrentValue(mCurrentValue);

    }

    private void drawScrew(Canvas canvas) {
        canvas.drawCircle(centerRectX, centerRectY, mScrewRadius, mScrewPaint);
        canvas.drawCircle(centerRectX, centerRectY, mScrewRadius - (mScrewRadius / 10), mScrewGradientPaint);
        canvas.drawOval(new RectF(centerRectX - (mScrewRadius * 0.70f), centerRectY - (mScrewRadius * 0.90f), centerRectX + (mScrewRadius * 0.70f), centerRectY + (mScrewRadius * 0.20f)), mScrewReflectionPaint);
    }

    private void initializePaintElements() {
        mScalePaint = new Paint();
        mScalePaint.setStyle(Paint.Style.STROKE);
        mScalePaint.setColor(Color.BLACK);
        mScalePaint.setStrokeWidth(mRimWidth);
        mScalePaint.setAntiAlias(true);
        mReadingTextPaint = new Paint();
        mReadingTextPaint.setTextSize(mFontSize);
        mReadingTextPaint.setTypeface(Typeface.SANS_SERIF);
        mReadingTextPaint.setColor(Color.BLACK);
        mReadingTextPaint.setTextScaleX(1.1f);
        mReadingTextPaint.setStrokeWidth(mFontSize);
        mReadingTextPaint.setTextAlign(Paint.Align.CENTER);
        mReadingTextPaint.setAntiAlias(true);
        mReadingTextPaint.setShadowLayer(0.3f, -2f, -2f, 0x30000000);
        mScrewPaint = new Paint();
        mScrewPaint.setColor(Color.BLACK);
        mScrewGradientPaint = new Paint();
        mScrewGradientPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mScrewGradientPaint.setShader(new RadialGradient(centerRectX, centerRectY,
                90, 0x90ffffff, Color.TRANSPARENT, Shader.TileMode.MIRROR));
        mScrewPaint.setShadowLayer(0.7f, 7f, -7f, 0x70000000);
        mScrewPaint.setAntiAlias(true);
        Shader shader = new LinearGradient(centerRectX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()),
                centerRectY + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()),
                centerRectX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()),
                centerRectY - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()),
                Color.TRANSPARENT,
                0xffffffff,
                Shader.TileMode.CLAMP);
        mScrewReflectionPaint = new Paint();
        mScrewReflectionPaint.setShader(shader);
        mScrewReflectionPaint.setAntiAlias(true);

        // Needle PaintBrush
        mNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader needleShader = new LinearGradient(centerRectX, centerRectY, centerRectX, Y1, 0xffef6d15, 0xffc92609, Shader.TileMode.CLAMP);
        mNeedlePaint.setShader(needleShader);
//        mNeedlePaint.setStrokeWidth(2);
//        mNeedlePaint.setColor(Color.RED);
        mNeedlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mNeedlePaint.setAntiAlias(true);
        mNeedlePaint.setShadowLayer(0.2f, -5f, -5f, 0x33000000);

    }

    private void drawNeedle(Canvas canvas) {
        Point point1_draw = new Point((int) centerRectX, (int) (Y1 + mFontSize + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())));
        Point point2_draw = new Point((int) centerRectX - (int) (0.45 * mScrewRadius), (int) centerRectY);
        Point point3_draw = new Point((int) centerRectX + (int) (0.45 * mScrewRadius), (int) centerRectY);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1_draw.x, point1_draw.y);
        path.lineTo(point2_draw.x, point2_draw.y);
        path.lineTo(point3_draw.x, point3_draw.y);
        path.lineTo(point1_draw.x, point1_draw.y);
        path.close();

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(-preRotateAngle, centerRectX, centerRectY);

        float degreeToRotate = (mMaxValue - mCurrentValue)* mCurveAngle / 100 ;

        canvas.rotate(-degreeToRotate, centerRectX, centerRectY);
        canvas.drawPath(path, mNeedlePaint);
//        canvas.rotate(-mDegreesPerNick * 3);
        canvas.restore();
    }

   /* private void drawARectangle(Canvas canvas, RectF scaleRect) {
        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setColor(Color.BLUE);
        rectPaint.setStrokeWidth(4);

        canvas.drawRect(scaleRect, rectPaint);
        canvas.drawLine(scaleRect.centerX(), scaleRect.top, scaleRect.centerX(), scaleRect.bottom, rectPaint);
        canvas.drawLine(scaleRect.left, scaleRect.centerY(), scaleRect.right, scaleRect.centerY(), rectPaint);
    }*/

    private void drawScale(Canvas canvas, RectF scaleRect, Paint scalePaint, Paint readingPaint) {
        Log.d("DegreePerNick", "degrees :" + mDegreesPerNick);

        canvas.drawArc(scaleRect, startAngle, mCurveAngle, false, scalePaint);
        scalePaint.setColor(mWarningColor);
        canvas.drawArc(scaleRect, startAngle, mRedNicks * Math.abs(mDegreesPerNick), false, scalePaint);
        scalePaint.setColor(mScaleColor);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        float y1 = scaleRect.top - mRimWidth / 2;
        float y2 = 0;
        canvas.rotate(-preRotateAngle, centerRectX, centerRectY);
        for (int i = 0; i <= mTotalNicksCount; i++) {
            if (i >= mTotalNicksCount - mRedNicks) {
                scalePaint.setColor(mWarningColor);
            } else {
                scalePaint.setColor(mScaleColor);
            }

            if (i % 2 == 0) {
                y2 = y1 + mNickLength;
                scalePaint.setStrokeWidth(mNickWidth);
                canvas.drawLine(centerRectX, y1, centerRectX, y2, scalePaint);
                String value = nickToDegree(i);
                canvas.drawText(value, centerRectX, y1 - 10, readingPaint);
            } else {
                y2 = y1 + mNickLength * 0.6f;
                scalePaint.setStrokeWidth(mRimWidth);
                canvas.drawLine(centerRectX, y1, centerRectX, y2, scalePaint);
            }
            canvas.rotate(mDegreesPerNick, centerRectX, centerRectY);
        }
        canvas.rotate(preRotateAngle, centerRectX, centerRectY);

        canvas.restore();
    }

    private String nickToDegree(int nick) {
        int shiftedDegree = (mTotalNicksCount - nick) * mMaxValue / mTotalNicksCount;
        return shiftedDegree + "%";
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
        Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenHeight = chooseDimension(heightMode, heightSize, false);
        int chosenWidth = chooseDimension(widthMode, widthSize, true);

        int requiredHeight = (int) (chosenWidth * heightToWidthRatio);
        Log.d(TAG, "height compromised:" + chosenWidth + "x" + chosenHeight + "  final:" + chosenWidth + "x" + requiredHeight);
        setMeasuredDimension(chosenWidth, Math.min(requiredHeight, chosenHeight));


    }

    private int chooseDimension(int mode, int size, boolean isWidthSpec) {
        int preferredSize = getPreferredSize(isWidthSpec);
        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else if (mode == MeasureSpec.AT_MOST) {
            if (isWidthSpec) {
                return Math.min(size, preferredSize);
            } else {
                return size;
            }
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return preferredSize;
        }

    }

    private int getPreferredSize(boolean isWidthSpec) {
        if (isWidthSpec) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PREFERRED_WIDTH_IN_DP, getResources().getDisplayMetrics());
        } else {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (PREFERRED_WIDTH_IN_DP * heightToWidthRatio), getResources().getDisplayMetrics());
        }
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        if(maxValue>0 && mMaxValue!=maxValue) {
            mMaxValue = maxValue;
            invalidate();
            requestLayout();
        }
    }

    public int getTotalNicksCount() {
       return mTotalNicksCount;
    }

    public void setTotalNicksCount(int totalNicksCount) {
        if(totalNicksCount>=0 && totalNicksCount!=mTotalNicksCount) {
            mTotalNicksCount = totalNicksCount;
            invalidate();
            requestLayout();
        }
    }

    public float getCurveAngle() {
        return mCurveAngle;
    }

    public void setCurveAngle(float curveAngle) {
        if(curveAngle>=0 && mCurveAngle!=curveAngle) {
            mCurveAngle = curveAngle;
            mDegreesPerNick = -(mCurveAngle / mTotalNicksCount);
            startAngle = 90f + (180f - mCurveAngle) / 2f;
            preRotateAngle = startAngle - 90;
            invalidate();
            requestLayout();
        }

    }

    public int getWarningColor() {
        return mWarningColor;
    }

    public void setWarningColor(int warningColor) {
        mWarningColor = warningColor;
        invalidate();
        requestLayout();

    }

    public float getNickLength() {
        return mNickLength;
    }

    public void setNickLength(float nickLength) {
        if(nickLength>=0 && nickLength!=mNickLength) {
            mNickLength = nickLength;
            invalidate();
            requestLayout();

        }
    }

    public float getNickWidth() {
        return mNickWidth;
    }

    public void setNickWidth(float nickWidth) {
        if(nickWidth>=0 && nickWidth!=mNickWidth) {
            mNickWidth = nickWidth;
            invalidate();
            requestLayout();
        }
    }

    public float getRimWidth() {
        return mRimWidth;
    }

    public void setRimWidth(float rimWidth) {
        if(rimWidth>=0 && rimWidth!=mRimWidth) {
            mRimWidth = rimWidth;
            invalidate();
            requestLayout();
        }
    }

    public int getScaleColor() {
        return mScaleColor;
    }

    public void setScaleColor(int scaleColor) {
        if(scaleColor!=0&& scaleColor!=mScaleColor) {
            mScaleColor = scaleColor;
            invalidate();
            requestLayout();
        }
    }

    public int getAlertColor() {
        return mAlertColor;
    }

    public void setAlertColor(int alertColor) {
        if(alertColor!=0 && alertColor!=mAlertColor) {
            mAlertColor = alertColor;
            invalidate();
            requestLayout();
        }

    }

    public float getFontSize() {
        return mFontSize;
    }

    public void setFontSize(float fontSize) {
        if(fontSize!=0 && mFontSize!=fontSize)
        mFontSize = fontSize;
        invalidate();
        requestLayout();
    }

    public float getScrewRadius() {
        return mScrewRadius;
    }

    public void setScrewRadius(float screwRadius) {
        if(screwRadius>0 && screwRadius!=mScrewRadius) {
            mScrewRadius = screwRadius;
            invalidate();
            requestLayout();
        }
    }

    public float getCurrentValue() {
        return mCurrentValue;
    }

    public void setCurrentValue(float currentValue) {
        if(currentValue>=0 && currentValue!=mCurrentValue) {
            mCurrentValue = currentValue;
            invalidate();
            requestLayout();
        }
    }
}
