package com.example.mynotepade;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**具体实现绘图撤销恢复重做功能，自定义的view
 *
 */
public class paintView extends View {


    private static final float TOUCH_TOLERANCE = 4;
    private int currentSize = 5;
    private int currentStyle = 1;
    private static ArrayList<DrawPath> savePath;
    private static Bitmap mBitmap;
    private static Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private static ArrayList<DrawPath> deletePath;
    private int bitmapWidth;
    private int bitmapHeight;
    private DrawPath mDrawpath;
    private boolean isMoving = false;
    private int currentColor= Color.RED;

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


    public paintView(Context c) {
        super(c);
        //得到屏幕的分辨率
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm);

        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - 2 * 45;

        initCanvas();

        //保存已画路径
        savePath = new ArrayList<DrawPath>();
        //保存删除路径
        deletePath = new ArrayList<DrawPath>();
    }

    //AttributeSet是xml文件中元素属性的一个集合。其中提供了各种Api，供我们从已编译好的xml文件获取属性值
    public paintView(Context c, AttributeSet attributeSet) {
        super(c, attributeSet);

        //得到屏幕的分辨率
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm);

        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - 2 * 45;

        initCanvas();

        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    //初始化画布
    private void initCanvas() {

        setPaintStyle();
        //画笔抗抖动
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        //画布大小
        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
        //所有mbitmap画的东西都存放在mcanvas中
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    private void setPaintStyle() {
        mPaint = new Paint();
        //防锯齿
        mPaint.setAntiAlias(true);
        //抖动
        mPaint.setDither(true);
        mPaint.setColor(0xFF00FF00);
        //填充样式
        mPaint.setStyle(Paint.Style.STROKE);
        //设置结合处的样子，圆角
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        //设置线帽样式
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);

        //原始画笔
        if(currentStyle == 1)
            mPaint.setColor(currentColor);
        else{
            mPaint.setColor(Color.WHITE);
        }
    }

    //绘画实现
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);//显示旧的画布
        if (mPath != null) {
            //实时显示
            canvas.drawPath(mPath, mBitmapPaint);//画最后的path

            //移动时显示铅笔图标
            if(this.isMoving && currentColor != Color.WHITE){
                //设置画笔的图标
                Bitmap pen = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.pen);
                canvas.drawBitmap(pen, this.mX, this.mY - pen.getHeight(),
                        new Paint(Paint.DITHER_FLAG));
            }
        }
    }

    public void selectPaintColor(int which) {
        currentColor = paintColor[which];
        setPaintStyle();
    }

    public void selectPaintSize(int which) {
        int size = Integer.parseInt(this.getResources().getStringArray(R.array.paintsize)[which]);
        currentSize = size;
        setPaintStyle();
    }

    //设置画笔样式
    public void selectPaintStyle(int which) {
        //初始配置
        if(which == 0){
            currentStyle = 1;
            setPaintStyle();
        }
        //当选择的是橡皮擦时，设置颜色为白色
        if(which == 1){
            currentStyle = 2;
            setPaintStyle();
            mPaint.setStrokeWidth(20);
        }
    }

    //路径对象，这个是恢复以及删除的关键
    class DrawPath {
        Path path;
        Paint paint;
    }


    /**
     * 撤销的实现：
     * 将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将空路径画在画布上面
     */
    public void undo() {
        System.out.print(savePath.size() + "----------------------------");
        if (savePath != null && savePath.size() > 0) {
            //清空画布
            initCanvas();

            //将路径保存列表中的最后哟个元素删除，并将它保存在删除列表中
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);
            //将路径保存列表中的路径重绘在画布上
            Iterator<DrawPath> iterator = savePath.iterator();//迭代器模式，单项移动
            while (iterator.hasNext()) {
                DrawPath next = iterator.next();
                mCanvas.drawPath(next.path, next.paint);
            }
            invalidate();//刷新
        }
    }

    /**
     * 恢复的实现：
     * 将删除的路径保存到另外一个保存路径里面，
     * 然后从保存的列表里面取出最顶端对象，
     * 画在画布上面即可
     */
    public void redo() {
        if (deletePath.size() > 0) {
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath drawPath = deletePath.get(deletePath.size() - 1);
            savePath.add(drawPath);
            //重新绘制
            mCanvas.drawPath(drawPath.path, drawPath.paint);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }

    /**
     * 清空功能的实现
     * 将两个列表清空并初始化画布
     */
    public void removeAllPaint() {
        deletePath.clear();
        savePath.clear();
        initCanvas();
        invalidate();
    }

    private float mX, mY;

    //开始画图
    private void touchStart(float x, float y) {
        mPath.reset();//刷新路径
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    //画图中
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);//起始点与现在触摸点的距离之差
        float dy = Math.abs(y - mY);//起始点与现在触摸点的距离之差
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mY = y;
            mX = x;
        }
    }

    //画图结束
    private void touchUp(float x, float y) {
        //方法实现的仅仅是两点连成一线的绘制线路
        mPath.lineTo(x, y);
        mCanvas.drawPath(mPath, mBitmapPaint);
        mPath.reset();
    }

    //触摸的事件
    //MotionEvent中就有一系列与标触摸事件发生位置相关的函数
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        //判断事件类型
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                mDrawpath = new DrawPath();

                mDrawpath.paint = mPaint;
                mDrawpath.path = mPath;

                touchStart(x, y);
                invalidate();//清屏
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();//清屏
                break;

            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                invalidate();//清屏
                break;

            default:
                break;
        }
        return true;
    }

    //保存绘图的图片
    //Bitmap位图包括像素以及长、宽、颜色等描述信息,位图的意思
    public static String saveBitmap() {
        //获取系统的时间用来当作文件名
        //时间日期格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒 绘图");
        //获取当前的时间
        Date curdate = new Date(System.currentTimeMillis());
        String str = simpleDateFormat.format(curdate);
        String painPath = "";
        str = str + "paint.png";
        //存储路径
        File dir = new File("/sdcard/mynotepade/");
        //文件名
        File file = new File("/sdcard/mynotepade/", str);
        //判断路径是否已经存在
        if (!dir.exists()) {
            //Creates the directory named by this abstract pathname
            dir.mkdir();
        } else {
            //判断路径下的相同文件名是否存在
            if (file.exists()) {
                file.delete();
            }
        }

        //文件输出流
        try {
            FileOutputStream out = new FileOutputStream(file);
            //压缩文件
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            //清空
            out.flush();
            //关闭
            out.close();

            //保存文件路径
            painPath = "/sdcard/mynotepade/" + str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return painPath;
    }
}
