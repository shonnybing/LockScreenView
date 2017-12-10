package com.example.lenovo.lockscreenview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ViewAnimator;

/**
 * Created by Lenovo on 2017/12/10.
 */

public class LockScreenView extends View {
    private int normalColor;
    private int smallRadius;
    private int bigRadius;
    private int rightColor;
    private int wrongColor;
    private State mCurrentState = State.STATE_NORMAL;
    private Paint mPaint;
    private boolean needZoomIn;

    public enum State { // 三种状态，分别是正常状态、选中状态、结果正确状态、结果错误状态
        STATE_NORMAL, STATE_CHOOSED, STATE_RESULT_RIGHT, STATE_RESULT_WRONG
    }

    public LockScreenView(Context context, int normalColor, int smallRadius, int bigRadius, int rightColor, int wrongColor) {
        super(context);
        this.normalColor = normalColor;
        this.smallRadius = smallRadius;
        this.bigRadius = bigRadius;
        this.rightColor = rightColor;
        this.wrongColor = wrongColor;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = (int) Math.round(bigRadius*2);
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = (int) Math.round(bigRadius*2);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch(mCurrentState) {
            case STATE_NORMAL:
                mPaint.setColor(normalColor);
                // 透明度为0到255，255为不透明，0为全透明
                mPaint.setAlpha(255);
                canvas.drawCircle(getWidth()/2, getHeight()/2, smallRadius, mPaint);
                // 放大以后，在下次恢复正常时要缩小回去
                if (needZoomIn) {
                    zoomIn();
                }
                break;
            case STATE_CHOOSED:
                mPaint.setColor(normalColor);
                mPaint.setAlpha(255);
                canvas.drawCircle(getWidth()/2, getHeight()/2, smallRadius, mPaint);
                mPaint.setAlpha(50);
                canvas.drawCircle(getWidth()/2, getHeight()/2, bigRadius, mPaint);
                zoomOut();
                break;
            case STATE_RESULT_RIGHT:
                mPaint.setColor(rightColor);
                mPaint.setAlpha(50);
                canvas.drawCircle(getWidth()/2, getHeight()/2, bigRadius, mPaint);
                mPaint.setAlpha(255);
                canvas.drawCircle(getWidth()/2, getHeight()/2, smallRadius, mPaint);
                break;
            case STATE_RESULT_WRONG:
                mPaint.setColor(wrongColor);
                mPaint.setAlpha(50);
                canvas.drawCircle(getWidth()/2, getHeight()/2, bigRadius, mPaint);
                mPaint.setAlpha(255);
                canvas.drawCircle(getWidth()/2, getHeight()/2, smallRadius, mPaint);
                break;
        }
    }

    public void setmCurrentState(State state) {
        this.mCurrentState = state;
        invalidate();
    }

    private void zoomOut() {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(this, "scaleX", 1, 1.2f);
        animatorX.setDuration(50);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(this, "scaleY", 1, 1.2f);
        animatorY.setDuration(50);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.start();
        needZoomIn = true;
    }

    private void zoomIn() {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(this, "scaleX", 1, 1f);
        animatorX.setDuration(0);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(this, "scaleY", 1, 1f);
        animatorY.setDuration(0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.start();
        needZoomIn = false;
    }
}
