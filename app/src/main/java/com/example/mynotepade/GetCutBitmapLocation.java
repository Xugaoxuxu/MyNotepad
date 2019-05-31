package com.example.mynotepade;

/**
 * @author Admin
 * @version $Rev$
 * @des ${得到切割字的位置}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class GetCutBitmapLocation {

    private float cutLeft = 0;
    private float cutRight = 0;
    private float cutTop = 0;
    private float cutBottom = 0;

    //初始化，刚开始是上下左右重合
    private void init(float x, float y) {
        cutLeft = x;
        cutRight = x;
        cutTop = y;
        cutBottom = y;
    }

    //更新上下左右的位置
    public void setCutLeftAndRight(float x, float y) {
        cutLeft = (x < cutLeft ? x : cutLeft);
        cutRight = (x > cutRight ? x : cutRight);
        cutTop = (y < cutTop ? y : cutTop);
        cutBottom = (y > cutBottom ? y : cutBottom);
    }

    //返回手写字的切割位置
    public float getCutLeft() {
        return cutLeft;
    }

    public float getCutRight() {
        return cutRight;
    }

    public float getCutTop() {
        return cutTop;
    }

    public float getCutBottom() {
        return cutBottom;
    }
}
