package com.zyl.custompwdinputview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;


/**
 * Description: 密码 输入框
 * Created by zouyulong on 2017/7/31.
 */

public class CustomPasswordInputView extends EditText {
    private Context mContext;

    /**
     * 第一个密码实心圆的圆心坐标
     */
    private float mFirstCircleX;
    private float mFirstCircleY;

    /**
     * 当前输入的个数
     */
    private int mCurInputCount = 0;

    /**
     * 实心圆的半径
     */
    private int mCircleRadius = dip2px(getContext(), 5);

    /**
     * view的宽高
     */
    private int mHeight;
    private int mWidth;

    /**
     * 最大输入位数
     */
    private int mMaxCount = 6;

    /**
     * 圆的颜色   默认BLACK
     */
    private int mCircleColor = Color.BLACK;

    /**
     * 边线的颜色
     */
    private int mStrokeColor = Color.BLACK;

    /**
     * 分割线的颜色
     */
    private int mDevideLineColor = Color.BLACK;

    /**
     * 描边的画笔
     */
    private Paint mStrokePaint;

    /**
     * 分割线开始的坐标x
     */
    private int mDivideLineWStartX;

    /**
     * 分割线的宽度
     */
    private int mDivideLineWidth = dip2px(getContext(), 0.5f);

    /**
     * 描边的矩形
     */
    private RectF mFrameRectF = new RectF();

    /**
     * 矩形边框的圆角
     */
    private int mRectAngle = 0;

    /**
     * 竖直分割线的画笔
     */
    private Paint mDivideLinePaint;

    /**
     * 圆的画笔
     */
    private Paint mCirclePaint;

    /**
     * 密码输入完成事件
     */
    private OnPasswordCompleteListener mCompleteListener;

    public CustomPasswordInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        getAtt(attrs);
        initPaint();

        this.setCursorVisible(false);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxCount)});

    }

    private void getAtt(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CustomPasswordInputView);
        mMaxCount = typedArray.getInt(R.styleable.CustomPasswordInputView_maxCount, mMaxCount);
        mCircleColor = typedArray.getColor(R.styleable.CustomPasswordInputView_pwdcircleColor, mCircleColor);
        mCircleRadius = typedArray.getDimensionPixelOffset(R.styleable.CustomPasswordInputView_pwdCircleRadius, mCircleRadius);
        mStrokeColor = typedArray.getColor(R.styleable.CustomPasswordInputView_strokeColor, mStrokeColor);
        mDevideLineColor = typedArray.getColor(R.styleable.CustomPasswordInputView_devideLineColor, mDevideLineColor);
        mDivideLineWidth = typedArray.getDimensionPixelSize(R.styleable.CustomPasswordInputView_divideLineWidth, mDivideLineWidth);
        mRectAngle = typedArray.getDimensionPixelOffset(R.styleable.CustomPasswordInputView_rectAngle, mRectAngle);

        typedArray.recycle();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mCirclePaint = getPaint(dip2px(getContext(), 5), Paint.Style.FILL, mCircleColor);

        mStrokePaint = getPaint(dip2px(getContext(), 0.5f), Paint.Style.STROKE, mStrokeColor);

        mDivideLinePaint = getPaint(mDivideLineWidth, Paint.Style.FILL, mStrokeColor);
    }

    /**
     * 设置画笔
     *
     * @param strokeWidth 画笔宽度
     * @param style       画笔风格
     * @param color       画笔颜色
     * @return
     */
    private Paint getPaint(int strokeWidth, Paint.Style style, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        paint.setColor(color);
        paint.setAntiAlias(true);

        return paint;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;

        mDivideLineWStartX = w / mMaxCount;

        mFirstCircleX = w / mMaxCount / 2;
        mFirstCircleY = h / 2;

        mFrameRectF.set(0, 0, mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //不删除的画会默认绘制输入的文字
//        super.onDraw(canvas);
        drawWeChatBorder(canvas);
        drawPsdCircle(canvas);
    }


    /**
     * 画微信支付密码的样式
     *
     * @param canvas
     */
    private void drawWeChatBorder(Canvas canvas) {

        canvas.drawRoundRect(mFrameRectF, mRectAngle, mRectAngle, mStrokePaint);

        for (int i = 0; i < mMaxCount - 1; i++) {
            canvas.drawLine((i + 1) * mDivideLineWStartX,
                    0,
                    (i + 1) * mDivideLineWStartX,
                    mHeight,
                    mDivideLinePaint);
        }

    }

    /**
     * 画密码实心圆
     *
     * @param canvas
     */
    private void drawPsdCircle(Canvas canvas) {
        for (int i = 0; i < mCurInputCount; i++) {
            canvas.drawCircle(mFirstCircleX + i * 2 * mFirstCircleX,
                    mFirstCircleY,
                    mCircleRadius,
                    mCirclePaint);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        mCurInputCount = text.toString().length();
        if (mCurInputCount == mMaxCount && mCompleteListener !=null) {
            mCompleteListener.onComplete(getPasswordString());
        }
        invalidate();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //保证光标始终在最后
        if (selStart == selEnd) {
            setSelection(getText().length());
        }
    }

    /**
     * 获取输入的密码
     *
     * @return
     */
    public String getPasswordString() {
        return getText().toString().trim();
    }

    /**
     * 密码输入完成回调
     */
    public interface OnPasswordCompleteListener {
        void onComplete(String password);
    }

    public void setOnCompleteListener(OnPasswordCompleteListener mListener) {
        this.mCompleteListener = mListener;
    }

    /**
     * dp转px  自定义事件注意使用dp为单位
     * @param var0
     * @param var1
     * @return
     */
    public static int dip2px(Context var0, float var1) {
        float var2 = var0.getResources().getDisplayMetrics().density;
        return (int)(var1 * var2 + 0.5F);
    }
}
