package com.example.mynotepade;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HandwriteActivity extends Activity {

    private MyEditText et_handwrite;
    private GridView paintBottomMenu;
    private ArrayList<CharSequence> deleteChar;
    private int select_handwrite_size_index=0;
    private TouchView touchView;
    private int select_handwrite_color_index=0;

    private Button btn_save;
    private Button btn_back;

    //菜单资源
    private int[]  paintItems = {
            R.drawable.paint_pencil,
            R.drawable.paint_icon_color,
            R.drawable.paint_icon_back,
            R.drawable.paint_icon_forward,
            R.drawable.paint_icon_delete
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //替换为自己的标题栏
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_handwrite);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_add);

        //将自定义标题栏的文字改为手写
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText("手写");

        //将图片插入到文本中
        et_handwrite = (MyEditText) findViewById(R.id.et_handwrite);
        paintBottomMenu = (GridView) findViewById(R.id.paintBottomMenu);
        paintBottomMenu.setOnItemClickListener(new MenuClickListener());

        //初始化菜单
        InitPaintMenu();
        //用于存储删除的文字
        deleteChar = new ArrayList<>();

        touchView = (TouchView)findViewById(R.id.touch_view);
        touchView.setHandler(handler);

        btn_save = (Button)findViewById(R.id.bt_save);
        btn_back = (Button)findViewById(R.id.bt_back);
        btn_save.setOnClickListener(new ClickEvent());
        btn_back.setOnClickListener(new ClickEvent());
    }

    //配置绘图菜单
    public void InitPaintMenu(){
        ArrayList<Map<String,Object>> menus = new ArrayList<Map<String,Object>>();
        for(int i = 0;i < paintItems.length;i++){
            Map<String,Object> item = new HashMap<String,Object>();
            item.put("image",paintItems[i]);
            menus.add(item);
        }
        paintBottomMenu.setNumColumns(paintItems.length);
        paintBottomMenu.setSelector(R.drawable.bottom_item);
        SimpleAdapter mAdapter = new SimpleAdapter(HandwriteActivity.this, menus,R.layout.item_button, new String[]{"image"}, new int[]{R.id.item_image});
        paintBottomMenu.setAdapter(mAdapter);
    }

    //监听
    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(v == btn_save){

                //得到调用该Activity的Intent对象
                Intent intent = getIntent();
                Bundle b = new Bundle();
                String path = saveBitmap();
                b.putString("handwritePath", path);
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                HandwriteActivity.this.finish();

            }
            else if(v == btn_back){
                HandwriteActivity.this.finish();
            }
        }

    }

    //处理界面得到绘制字体的bitmap
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle b = new Bundle();
            b = msg.getData();
            Bitmap myBitmap = b.getParcelable("bitmap");
            //插入图片到文本
            InsertToEditText(myBitmap);
        }
    };


    //插入手写字到文本
    private void InsertToEditText(Bitmap mBitmap) {
        int S = 120;

        int imgWidth = mBitmap.getWidth();
        int imgHeight = mBitmap.getHeight();
        double partion = imgWidth * 1.0 / imgHeight;
        double sqrtLength = Math.sqrt(partion * partion + 1);

        //新的缩略图的大小
        double newImgW = S * (partion / sqrtLength);
        double newImgH = S * (1 / sqrtLength);
        float scaleW = (float) (80f / imgWidth);
        float scaleH = (float) (100f / imgHeight);

        Matrix mx = new Matrix();
        //对原图片进行缩放
        mx.postScale(scaleW, scaleH);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, imgWidth, imgHeight, mx, true);
        //将手写的字插入到edittext中
        SpannableString ss = new SpannableString("1");
        ImageSpan span = new ImageSpan(mBitmap, ImageSpan.ALIGN_BOTTOM);
        ss.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        et_handwrite.append(ss);
    }

    private class MenuClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                //画笔大小对话框
                case 0:
                    showPaintSizeDialog(view);
                    break;
                //颜色
                case 1:
                    showPaintColorDialog(view);
                    break;
                //删除
                //1）取得最后一个字的位置
                //2）将第0个到倒数第二个位置的所有内容设置为自定义editText的内容，间接实现删除最后一个字的功能
                //3）更新最后一个字的位置为删除后的最后一个字的位置
                //4）将删除的字存储在列表中，用于恢复
                case 2:
                    Editable editable = et_handwrite.getText();
                    //找到最后一个文字位置并删除
                    int selectionEnd = et_handwrite.getSelectionEnd();
                    System.out.println("end" + "----------------" + selectionEnd);
                    //无字
                    if (selectionEnd < 1) {
                        et_handwrite.setText("");
                    }
                    //有字，将最后字删除并添加到队列
                    else if (selectionEnd == 1) {
                        et_handwrite.setText("");
                        CharSequence sequence = editable.subSequence(0, 1);
                        deleteChar.add(sequence);
                    }
                    //删除中间的一个字
                    else {
                        System.out.println("delete");

                        CharSequence charSeq = editable.subSequence(0, selectionEnd - 1);
                        CharSequence deleteCharSeq = editable.subSequence(selectionEnd - 1, selectionEnd);
                        //保留未删除的
                        et_handwrite.setText(charSeq);
                        //光标减一
                        et_handwrite.setSelection(selectionEnd - 1);
                        //将删除的文字存储以备恢复
                        deleteChar.add(deleteCharSeq);
                    }
                    break;
                //恢复
                //1）取出删除列表中的最后一个元素
                //2）将取出的元素添加在自定义editText的末尾
                //3）从删除列表中删掉最后一个元素
                case 3:
                    int size = deleteChar.size();
                    //说明有字
                    if (size > 0) {
                        //获得
                        et_handwrite.append(deleteChar.get(size - 1));
                        //删除
                        deleteChar.remove(size - 1);
                    }
                    break;
                //清空屏幕
                //1）创建Dialog，弹出是否删除提示
                //2）为Dialog添加PositiveButton和setNegativeButton
                //3）在PositiveButton单击事件中，设置自定义editText的内容为空
                case 4:
                    if (et_handwrite.getSelectionEnd()>0){
                        //创建对话框
                        final AlertDialog.Builder builder = new AlertDialog.Builder(HandwriteActivity.this);
                        builder.setTitle("您确定要清空全部吗");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                et_handwrite.setText("");
                                //对话框消失
                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        //显示出来
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void showPaintColorDialog(View view) {
        //弹出画笔颜色选项对话框
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
            alertDialogBuilder.setTitle("选择画笔颜色：");

            alertDialogBuilder.setSingleChoiceItems(R.array.paintcolor, select_handwrite_color_index, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    select_handwrite_color_index = which;
                    touchView.selectHandWriteColor(which);
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.setNegativeButton("取消",  new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.create().show();
        }

    //弹出画笔大小的选项
    private void showPaintSizeDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.custom_dialog);
        builder.setTitle("请选择画笔大小：");

        builder.setSingleChoiceItems(R.array.paintsize, select_handwrite_size_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_handwrite_size_index = which;
                touchView.selectHandWriteSize(which);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //保存手写文件
    public String saveBitmap(){
        //获得系统当前时间，并以该时间作为文件名
        SimpleDateFormat   formatter   =   new SimpleDateFormat("yyyyMMddHHmmss");
        Date   curDate   =   new Date(System.currentTimeMillis());//获取当前时间
        String   str   =   formatter.format(curDate);
        String paintPath = "";
        str = str + "write.png";
        File dir = new File("/sdcard/mynotes/");
        File file = new File("/sdcard/mynotes/",str);
        if (!dir.exists()) {
            dir.mkdir();
        }
        else{
            if(file.exists()){
                file.delete();
            }
        }

        //将view转换成图片
        et_handwrite.setDrawingCacheEnabled(true);
        Bitmap cutHandwriteBitmap = Bitmap.createBitmap(et_handwrite.getDrawingCache());
        et_handwrite.setDrawingCacheEnabled(false);
        try {
            //保存绘图文件路径
            paintPath = "/sdcard/notes/" + str;
            FileOutputStream out = new FileOutputStream(file);
            cutHandwriteBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return paintPath;
    }
}
