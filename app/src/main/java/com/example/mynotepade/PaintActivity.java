package com.example.mynotepade;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 实现步骤：
 * 1）GridView添加适配器，将选项菜单装载到GridView中。
 * 2）将绘制的图形以图片格式保存在指定的SD卡文件夹下。
 * 3）给顶部保存按钮添加监听器，保存绘图并返回图片路径，显示在EditText中
 */

public class PaintActivity extends Activity {

    //画板菜单图片资源
    //菜单资源
    private int[]  paintItems = {
            R.drawable.paint_more,
            R.drawable.paint_pencil,
            R.drawable.paint_icon_color,
            R.drawable.paint_icon_back,
            R.drawable.paint_icon_forward,
            R.drawable.paint_icon_delete
    };

    //画笔大小资源
    private int[] penceilSizes = {

    };

    private int select_paint_size_index = 0;
    private int select_paint_style_index = 0;
    private int select_paint_color_index = 0;

    private PopupWindow popupWindow;
    private Button bt_back;
    private Button bt_save;
    private GridView paintBottomMenu;
    private com.example.mynotepade.paintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置标题栏
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_paint);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_add);

        //将自定义标题栏的标题定义为绘图
        TextView title = (TextView)findViewById(R.id.tv_title);
        title.setText("绘图");

        //找到控件
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_back = (Button) findViewById(R.id.bt_back);
        //设置监听
        bt_back.setOnClickListener(new ClickEvent());
        bt_save.setOnClickListener(new ClickEvent());

        paintView = (paintView) findViewById(R.id.paint_layout);


        //为绘图的底部状态栏添加监听
        paintBottomMenu = (GridView) findViewById(R.id.paintBottomMenu);
        paintBottomMenu.setOnItemClickListener(new MenuClickEvent());
        
        //初始化绘图菜单
        InitPaintMenu();
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
        SimpleAdapter mAdapter = new SimpleAdapter(PaintActivity.this, menus,R.layout.item_button, new String[]{"image"}, new int[]{R.id.item_image});
        paintBottomMenu.setAdapter(mAdapter);
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(v == bt_save){

                //得到调用该Activity的Intent对象
                Intent intent = getIntent();
                Bundle b = new Bundle();
                String path = paintView.saveBitmap();
                b.putString("paintPath", path);
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                PaintActivity.this.finish();
            }
            else if(v == bt_back){
                PaintActivity.this.finish();
            }
        }

    }

    //与初始化时相同，设置底部菜单监听器
    private class MenuClickEvent implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent();
            switch (position) {
                //画笔样式
                case 0:
                    showMoreDialog(view);
                    break;
                //画笔大小
                case 1:
                    showPaintSizeDialog(view);
                    break;
                //颜色
                case 2:
                    showPaintColorDialog(view);
                    break;
                //撤销
                case 3:
                    paintView.undo();
                    break;
                //恢复
                case 4:
                    paintView.redo();
                    break;
                //清空
                case 5:
                    //创建对话框
                        final AlertDialog.Builder builder = new AlertDialog.Builder(PaintActivity.this);
                        builder.setTitle("您确定要清空全部吗");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                paintView.removeAllPaint();
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

                    break;
            }
        }
    }

    //画笔大小选项
    private void showPencileSize(View view) {
        if (popupWindow!=null){
            //使用Inflater对象来将布局文件解析成一个View
            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view1 = layoutInflater.inflate(R.layout.popup_window,null);
            TextView txt = (TextView) findViewById(R.id.tv_test);
            ArrayList<Map<String,Object>> more_item_list = new ArrayList<Map<String,Object>>();
            int[] more_items = new int[]{
                    R.string.track_line,
                    R.string.straight_line,
                    R.string.rectangle,
                    //椭圆
                    R.string.oval,
                    //圆
                    R.string.circle,
                    //点
                    R.string.dots,
                    //橡皮
                    R.string.Eraser
            };

            //填充数据
            for(int i = 0;i < more_items.length;i++){
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("more_item", more_items[i]);
                more_item_list.add(map);
            }
            //创建PopupWindow(弹出框）
            popupWindow = new PopupWindow(view);
            // 使其聚集
            popupWindow.setFocusable(true);
            // 设置允许在外点击消失
            popupWindow.setOutsideTouchable(true);
            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.setBackgroundDrawable(new BitmapDrawable());

            popupWindow.showAsDropDown(view);
        }
    }

    //弹出画笔颜色选项菜单
    private void showPaintColorDialog(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
        alertDialogBuilder.setTitle("选择画笔颜色：");

        //添加选项
        alertDialogBuilder.setSingleChoiceItems(R.array.paintcolor, select_paint_color_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_paint_color_index=which;
                paintView.selectPaintColor(which);
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    //弹出画笔大小选项对话框
    public void showPaintSizeDialog(View parent){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
        alertDialogBuilder.setTitle("选择画笔大小：");

        alertDialogBuilder.setSingleChoiceItems(R.array.paintsize, select_paint_size_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_paint_size_index = which;
                paintView.selectPaintSize(which);
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }

    //弹出更多选项对话框
    public void showMoreDialog(View parent){
     AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
        alertDialogBuilder.setTitle("选择画笔或橡皮擦：");

        alertDialogBuilder.setSingleChoiceItems(R.array.paintstyle, select_paint_style_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_paint_style_index = which;
                paintView.selectPaintStyle(which);
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }
 }


