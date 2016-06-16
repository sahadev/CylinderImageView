package com.sahadev.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.sahadev.cylinderapplication.R;

/**
 * Created by shangbin on 2016/6/16.
 * Email: sahadev@foxmail.com
 */
public class CylinderImageView extends View {
    //用于裁剪的原始图片资源
    private Bitmap mSourceBitmap = null;

    // 图片的高宽
    private int mBitmapHeight, mBitmapWidth;

    // 移动单位，每次移动多少个单位
    private final int mMoveUnit = 1;

    // 图片整体移动的偏移量
    private int xOffset = 0;

    private Bitmap mPointerA, mPointerB;// 用于持有两张拼接图片的引用，并释放原先的图片资源

    /**
     * 循环滚动标志位
     */
    private boolean mRunningFlag;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                invalidate();
            }
        }
    };

    public CylinderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView();
    }

    public CylinderImageView(Context context) {
        super(context);
        initVideoView();
    }

    public CylinderImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView();
    }

    private void initVideoView() {
        // 获取需要循环展示的图片的高宽
        mSourceBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.android_m_hero_1200);
        mBitmapHeight = mSourceBitmap.getHeight();
        mBitmapWidth = mSourceBitmap.getWidth();

        mRunningFlag = true;

        setFocusableInTouchMode(true);
        requestFocus();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 简单设置一下控件的宽高,这里的高度以图片的高度为准
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mBitmapHeight, MeasureSpec.getMode(heightMeasureSpec)));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        recycleTmpBitmap();

        final int left = getLeft();
        final int top = getTop();
        final int right = getRight();
        final int bottom = getBottom();

        // 计算图片的高度
        int height = bottom - top;
        // 第一张图的宽带
        int tempWidth = right - left;

        // 如果一张图片轮播完，则从头开始
        if (xOffset >= mBitmapWidth) {
            xOffset = 0;
        }

        // 重新计算截取的图的宽度
        tempWidth = xOffset + tempWidth >= mBitmapWidth ? mBitmapWidth - xOffset : tempWidth;

        mPointerA = Bitmap
                .createBitmap(mSourceBitmap, xOffset, 0, tempWidth, height > mBitmapHeight ? mBitmapHeight : height);

        Paint bitmapPaint = new Paint();

        // 绘制这张图
        canvas.drawBitmap(mPointerA, getMatrix(), bitmapPaint);

        // 如果最后的图片已经不足以填充整个屏幕，则截取图片的头部以连接上尾部，形成一个闭环
        if (tempWidth < right - left) {
            Rect dst = new Rect(tempWidth, 0, right, mBitmapHeight);
            mPointerB = Bitmap.createBitmap(mSourceBitmap, 0, 0, right - left - tempWidth,
                    height > mBitmapHeight ? mBitmapHeight : height);
            // 将另一张图片绘制在这张图片的后半部分
            canvas.drawBitmap(mPointerB, null, dst, bitmapPaint);
        }

        // 累计图片的偏移量
        xOffset += mMoveUnit;

        //由handler的延迟发送产生绘制间隔
        if (mRunningFlag) {
            mHandler.sendEmptyMessageDelayed(0, 1);
        }
    }

    /**
     * 回收临时图像
     */
    private void recycleTmpBitmap() {
        if (mPointerA != null) {
            mPointerA.recycle();
            mPointerA = null;
        }

        if (mPointerB != null) {
            mPointerB.recycle();
            mPointerB = null;
        }
    }

    /**
     * 暂停
     */
    public void resume() {
        mRunningFlag = true;
        invalidate();
    }

    /**
     * 恢复
     */
    public void pause() {
        mRunningFlag = false;
    }

    /**
     * 回收清理工作
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pause();
        recycleTmpBitmap();
        mSourceBitmap.recycle();
    }
}
