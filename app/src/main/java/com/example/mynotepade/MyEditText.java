package com.example.mynotepade;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * @author Admin
 * @version $Rev$
 * @des ${自定义的输入行}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class MyEditText extends android.support.v7.widget.AppCompatEditText {

    private final Rect mRect;
    private final Paint mPaint;

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        //原始为黑色
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取总行数
        int lineCount = getLineCount();
        Rect r = mRect;
        Paint p = mPaint;
        //设置每一行的格式
        for (int i = 0; i < lineCount; i++) {
            //取得每一行的基准Y坐标，并将每一行的界限值写到r中
            int baseLine = getLineBounds(i, r);
            //文字带下划线
            canvas.drawLine(r.left, baseLine + 5, r.right, baseLine + 5, p);
        }
    }
}
