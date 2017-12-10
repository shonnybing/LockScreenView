package com.example.lenovo.lockscreenview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by Lenovo on 2017/12/10.
 */

public class LockScreenViewGroup extends RelativeLayout {
    // xml中获取的自定义属性
    private int itemCount;  // 一排有几个LockScreenView
    private int smallRadius;  // LockScreenView小圈的半径
    private int bigRadius;  // LockScreenView中大圆圈的半径
    private int normalColor;  // LockScreenView中默认的颜色
    private int rightColor;  // LockScreenView中图形码正确时的颜色
    private int wrongColor;  // LockScreenView中图形码错误时的颜色

    // 界面中所有的LockScreenView
    private LockScreenView[] lockScreenViews;

    // 选中的LockScreenView的列表
    private ArrayList<Integer> mCurrentViews;
    // 当前图案的路径
    private Path mCurrentPath;

    // 悬空线段起点的x、y，即上一个选中的LockScreenView的中心
    private int skyStartX = -1;
    private int skyStartY = -1;

    // 悬空线段终点的x、y
    private int mTempX;
    private int mTempY;

    private Paint mPaint;

    // 答案数组
    private int[] answers = {1, 2, 3, 6, 9};

    public LockScreenViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockScreenViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 从xml中获取自定义属性
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.LockScreenViewGroup);
        itemCount = array.getInt(R.styleable.LockScreenViewGroup_itemCount, 3);
        smallRadius = (int) array.getDimension(R.styleable.LockScreenViewGroup_smallRadius, 20);
        bigRadius = (int) array.getDimension(R.styleable.LockScreenViewGroup_bigRadius, 2);
        normalColor = array.getInt(R.styleable.LockScreenViewGroup_normalColor, 0xffffff);
        rightColor = array.getColor(R.styleable.LockScreenViewGroup_rightColor, 0x00ff00);
        wrongColor = array.getColor(R.styleable.LockScreenViewGroup_wrongColor, 0x0000ff);

        array.recycle();
        mCurrentViews = new ArrayList<>();
        mCurrentPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 画path时，paint要设置为stroke模式，path会化成一个填充区域
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(smallRadius * 2 * 0.7f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(normalColor);
        mPaint.setAlpha(5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // 将高度设置成和宽度一样
        setMeasuredDimension(width, width);

        // 动态添加LockScreenView
        if (lockScreenViews == null) {
            lockScreenViews = new LockScreenView[itemCount * itemCount];
            for (int i = 0; i < itemCount * itemCount; i++) {
                lockScreenViews[i] = new LockScreenView(getContext(), normalColor, smallRadius, bigRadius,
                        rightColor, wrongColor);
                lockScreenViews[i].setId(i + 1);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                // 这里不能通过lockScreenViews[i].getMeasuredWidth()来获取宽高，因为这时它的宽高还没有测量出来
                int marginWidth = (getMeasuredWidth() - bigRadius * 2 * itemCount) / (itemCount + 1);

                // 除了第一行以外，其它的View都在在某个LockScreenView的下面
                if (i >= itemCount) {
                    params.addRule(BELOW, lockScreenViews[i - itemCount].getId());
                }

                // 除了第一列以外，其它的View都在某个LockScreenView的右边
                if (i % itemCount != 0) {
                    params.addRule(RIGHT_OF, lockScreenViews[i - 1].getId());
                }

                // 为LockScreenView设置margin
                int left = marginWidth;
                int top = marginWidth;
                int bottom = 0;
                int right = 0;
                params.setMargins(left, top, right, bottom);
                lockScreenViews[i].setmCurrentState(LockScreenView.State.STATE_NORMAL);
                addView(lockScreenViews[i], params);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 重置状态
                resetView();
                break;
            case MotionEvent.ACTION_MOVE:
                mPaint.setColor(normalColor);
                mPaint.setStrokeCap(Paint.Cap.ROUND);

                LockScreenView view = findLockScreenView(x, y);
                if (view != null) {
                    int id = view.getId();
                    // 当前LockScreenView不在选中列表中时，将其添加到列表中，并设置其状态为选中
                    if (!mCurrentViews.contains(id)) {
                        mCurrentViews.add(id);
                        view.setmCurrentState(LockScreenView.State.STATE_CHOOSED);
                        skyStartX = (view.getLeft() + view.getRight()) / 2;
                        skyStartY = (view.getTop() + view.getBottom()) / 2;

                        // path中线段的添加
                        if (mCurrentViews.size() == 1) {
                            mCurrentPath.moveTo(skyStartX, skyStartY);
                        } else {
                            mCurrentPath.lineTo(skyStartX, skyStartY);
                        }
                    }
                }

                // 悬空线段末端的更新
                mTempX = x;
                mTempY = y;
                break;
            case MotionEvent.ACTION_UP:
                // 根据图案正确与否，对LockScreenView设置不同的状态
                if (checkAnswer()) {
                    setmCurrentViewsState(LockScreenView.State.STATE_RESULT_RIGHT);
                    mPaint.setColor(rightColor);
                } else {
                    setmCurrentViewsState(LockScreenView.State.STATE_RESULT_WRONG);
                    mPaint.setColor(wrongColor);
                }
                // 抬起手指后对悬空线段的起始点进行重置
                skyStartX = -1;
                skyStartY = -1;
        }
        invalidate();
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 进行子View的绘制
        super.dispatchDraw(canvas);

        // path线段的绘制
        if (!mCurrentPath.isEmpty()) {
            canvas.drawPath(mCurrentPath, mPaint);
        }

        // 悬空线段的绘制
        if (skyStartX != -1) {
            canvas.drawLine(skyStartX, skyStartY, mTempX, mTempY, mPaint);
        }
    }

    public void setAnswers(int[] answers) {
        this.answers = answers;
    }

    private boolean checkAnswer() {
        if (answers.length != mCurrentViews.size()) {
            return false;
        }
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] != mCurrentViews.get(i)) {
                return false;
            }
        }
        return true;
    }

    /*
    * 当前触碰区域是否属于某个LockScreenView，是则返回这个View，否则返回null
    * */
    private LockScreenView findLockScreenView(int x, int y) {
        for (int i = 0; i < itemCount * itemCount; i++) {
            if (isInLockViewArea(x, y, lockScreenViews[i])) {
                return lockScreenViews[i];
            }
        }
        return null;
    }

    private boolean isInLockViewArea(int x, int y, LockScreenView view) {
        // 为了便于LockScreenView被选中，范围扩大了5px
        if (x > view.getLeft() - 5 && x < view.getRight() + 5 && y > view.getTop() - 5 && y < view.getBottom() + 5) {
            return true;
        }
        return false;
    }

    private void resetView() {
        if (mCurrentViews.size() > 0) {
            mCurrentViews.clear();
        }
        if (!mCurrentPath.isEmpty()) {
            mCurrentPath.reset();
        }

        // 重置LockScreenView的状态
        for (int i = 0; i < itemCount * itemCount; i++) {
            lockScreenViews[i].setmCurrentState(LockScreenView.State.STATE_NORMAL);
        }

        skyStartX = -1;
        skyStartY = -1;
    }

    private void setmCurrentViewsState(LockScreenView.State state) {
        for (int i = 0; i < mCurrentViews.size(); i++) {
            LockScreenView view = (LockScreenView) findViewById(mCurrentViews.get(i));
            view.setmCurrentState(state);
        }
    }
}
