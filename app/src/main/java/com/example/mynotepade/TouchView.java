package com.example.mynotepade;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Admin
 * @version $Rev$
 * @des ${实现绘制，及随着绘画的进行定时更新界面的功能，这是一个控件}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class TouchView extends View {

    private final DisplayMetrics dm;
    private final int w;
    private final int h;
    private GetCutBitmapLocation getCutBitmapLocation;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Handler bitmapHandler;
    private Paint mBitmapPaint;
    private Timer timer;
    private Bitmap myBitmap;
    private Path mPath;
    private Paint mPaint;
    private int currentColor= Color.RED;
    private float currentSize=5;

    //画笔颜色
    private int[] paintColor = {
            Color.RED,
            Color.BLUE,
            Color.BLACK,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN,
            Color.LTGRAY
    };

    public TouchView(Context context) {
        super(context);
        //获取分辨率
        dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        w = dm.widthPixels;
        h = dm.heightPixels;

        //初始化画布
        initPaint();
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context,attrs);
        dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        w = dm.widthPixels;
        h = dm.heightPixels;
        initPaint();
    }

    //设置handler
    public void setHandler(Handler mBitmapHandler) {
        bitmapHandler = mBitmapHandler;
    }

    private void initPaint() {
        //初始化画笔
        setPaintStyle();
        //切割画布中的手写字
        getCutBitmapLocation = new GetCutBitmapLocation();


        //初始化画布
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mCanvas.drawColor(Color.TRANSPARENT);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        timer = new Timer(true);
    }

    //屏幕UI更新
    Handler mHandler = new Handler() {
        private void handlerMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    myBitmap = getCutBitmap(mBitmap);
                    Message message = new Message();
                    message.what = 1;
                    Bundle b = new Bundle();
                    b.putParcelable("bitmap", myBitmap);
                    message.setData(b);
                    bitmapHandler.sendMessage(message);
                    RefreshBitmap();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //刷新画布
    private void RefreshBitmap() {
        initPaint();
        //Invalidate the whole view. If the view is visible
        invalidate();
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);     //显示旧的画布
        canvas.drawPath(mPath, mPaint);  //画最后的path
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    //总的来说这几个跟前面一样不过这几个增加了时间限制
    //在onTouchEvent的三个事件中都要通过handle发送消息来更新显示界面。
    //手按下时
    private void touchStart(float x, float y) {
        mPath.reset();//清空path
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        if (task != null)
            task.cancel();//取消之前的任务
        task = new TimerTask() {

            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                Log.i("线程---------------------", "来了");
                mHandler.sendMessage(message);
            }
        };
        getCutBitmapLocation.setCutLeftAndRight(mX, mY);
    }

    //手移动时，最长时间就是4s
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            if (task != null)
                task.cancel();//取消之前的任务
            task = new TimerTask() {

                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    Log.i("线程====================", "来了");
                    mHandler.sendMessage(message);
                }
            };
            getCutBitmapLocation.setCutLeftAndRight(mX, mY);
        }
    }

    //手抬起时
    private void touchUp() {
        //mPath.lineTo(mX mY);
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();

        if (timer != null) {
            if (task != null) {
                task.cancel();
                task = new TimerTask() {
                    public void run() {
                        Message message = new Message();
                        message.what = 1;
                        mHandler.sendMessage(message);
                    }
                };
                timer.schedule(task, 1000, 1000);//2200秒后发送消息给handler更新Activity
            }
        } else {
            timer = new Timer(true);
            timer.schedule(task, 1000, 1000);//2200秒后发送消息给handler更新Activity
        }

    }

    //处理界面事件，调用上述方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate(); //刷新
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    //发送消息给handler,更新activity
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 1;
            Log.i("线程开启-------------------", "---------");
            mHandler.sendMessage(msg);
        }
    };

    //切割画布中的字并返回
    private Bitmap getCutBitmap(Bitmap mBitmap) {
        //得到位置并向外延伸10px，防止切割的过小
        float cutLeft = getCutBitmapLocation.getCutLeft() - 10;
        float cutTop = getCutBitmapLocation.getCutTop() - 10;
        float cutRight = getCutBitmapLocation.getCutRight() + 10;
        float cutBottom = getCutBitmapLocation.getCutBottom() + 10;

        //这个地方代码可以复用的
        cutLeft = (0 > cutLeft ? 0 : cutLeft);
        cutTop = (0 > cutTop ? 0 : cutTop);
        cutRight = (mBitmap.getWidth() < cutRight ? mBitmap.getWidth() : cutRight);
        cutBottom = (mBitmap.getHeight() < cutBottom ? mBitmap.getHeight() : cutBottom);

        //取得手写的的高度和宽度
        float cutHeight = cutBottom - cutTop;
        float cutWidth = cutRight - cutLeft;

        //创建位图
        Bitmap cutBitmap = Bitmap.createBitmap(mBitmap, (int) cutLeft, (int) cutTop, (int) cutWidth, (int) cutHeight);
        if (myBitmap != null) {
            //clear the reference to the pixel data
            myBitmap.recycle();
            myBitmap = null;
        }
        return cutBitmap;
    }
    //其实更改画笔颜色大小等方法手写跟绘图都是一样的，所以我就在touchview中实现这些方法
    //设置画笔的样式
    public void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(currentSize);
        mPaint.setColor(currentColor);
    }

    //大小，给别的类用的
    public void selectHandWriteSize(int which){
        int size = Integer.parseInt(this.getResources().getStringArray(R.array.paintsize)[which]);
        currentSize = size;
        setPaintStyle();
    }

    //设置画笔颜色
    public void selectHandWriteColor(int which){
        currentColor = paintColor[which];
        setPaintStyle();
    }
}
