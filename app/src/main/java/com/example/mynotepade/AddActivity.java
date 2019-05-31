package com.example.mynotepade;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个是增加新的记事内容的activity
 */
public class AddActivity extends Activity {

    //这个是底部的图片按钮
    private int[] bottomItems = {
            //手写
            R.drawable.tabbar_handwrite,
            //绘画
            R.drawable.tabbar_paint,
            //录音
            R.drawable.tabbar_microphone,
            //图片
            R.drawable.tabbar_photo,
            //相机
            R.drawable.tabbar_camera,
            //附件
            R.drawable.tabbar_appendix
    };
    private GridView bottomMenu;
    private SimpleAdapter mAdapter;
    private EditText et_note;
    private Bundle extras;

    InputMethodManager imm;
    Intent intent;
    //记录是哪种记事方式
    String editModel = null;
    int item_Id;

    //图片存储路径
    private static String IMGPATH = "/sdcard/notes/yyyyMMddHHmmsspaint.png";
    //记录editText中的图片，用于单击时判断单击的是那一个图片
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();
    private Button bt_back;
    private Button bt_save;
    private TextView tv_title;
    private SQLiteDatabase db;
    private DatabaseOperation dop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_add);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_add);

        bt_back = (Button) findViewById(R.id.bt_back);
        bt_back.setOnClickListener(new ClickEvent());
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new ClickEvent());
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_note = (EditText) findViewById(R.id.et_note);

        bottomMenu = (GridView) findViewById(R.id.bottomMenu);

        //初始化底部菜单项
        initBottomMenu();

        //给选项菜单的容器GridView添加点击事件监听器
        bottomMenu.setOnItemClickListener(new MenuClickEvent());

        //这个是关于输入法(Input Methods)的，具体情况详见百度
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_note.getWindowToken(), 0);

        dop = new DatabaseOperation(this, db);
        //获得相应的editModle以及存储的id
        intent = getIntent();
        editModel = intent.getStringExtra("editModel");
        item_Id = intent.getIntExtra("noteId", 0);

        //加载数据
        loadData();

        //给edittext添加点击事件，具体就是区别新鞋记事还是查看记事之类的
        et_note.setOnClickListener(new TextClickEvent());
    }


    //初始化底部标签栏
    private void initBottomMenu() {
        ArrayList<Map<String, Object>> menus = new ArrayList<>();
        for (int i = 0; i < bottomItems.length; i++) {
            HashMap<String, Object> item = new HashMap<>();
            //关联image与数组bottomitems
            item.put("image", bottomItems[i]);
            menus.add(item);
        }
        //底部菜单的长度
        bottomMenu.setNumColumns(bottomItems.length);
        //Android中的Selector主要是用来改变ListView和Button控件的默认背景。
        bottomMenu.setSelector(R.drawable.bottom_item);
        //用simpladapter为gridview添加数据
        //把数据变成符合界面风格的形式，并且通过ListView显示出来。也就是说适配器是数据和界面之间的桥梁
        mAdapter = new SimpleAdapter(AddActivity.this, menus, R.layout.item_button, new String[]{"image"}, new int[]{R.id.item_image});
        bottomMenu.setAdapter(mAdapter);


    }

    //在监听器里实现打开图库选择添加图片等，并加图片返回
    //在监听器实现界面跳转等
    private class MenuClickEvent implements android.widget.AdapterView.OnItemClickListener {

        private Intent mIntent;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mIntent = new Intent();
            switch (position) {
                //手写
                case 0:
                    intent = new Intent(AddActivity.this, HandwriteActivity.class);
                    startActivityForResult(intent, 5);
                    break;
                //绘图
                case 1:
                    mIntent = new Intent(AddActivity.this, PaintActivity.class);
                    startActivityForResult(mIntent, 3);
                    break;
                //录音
                case 2:
                    mIntent = new Intent(AddActivity.this, RecordActivity.class);
                    startActivityForResult(mIntent, 4);
                    break;
                //图片
                case 3:
                    //设置图片格式为任意格式
                    mIntent.setType("image/*");
                    //设置action,Intent.ACTION_GET_CONTENT获取的是所有本地图片
                    mIntent.setAction(Intent.ACTION_GET_CONTENT);
                    //allow results to be delivered to fragments
                    startActivityForResult(mIntent, 1);
                    break;
                //相机
                case 4:
                    //调用系统的相机界面
                    mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    //allow results to be delivered to fragments
                    startActivityForResult(mIntent, 2);
                    break;
                //附件，，，，，，我还没实现，，，，，再说吧
                case 5:

                    break;
                default:
                    break;
            }

        }
    }

    //在进行界面间的跳转和传递数据的时候，我们有的时候要获得跳转之后界面传递回来的状态，数据等信息,类似于intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //-1
        if (requestCode == RESULT_OK) {
            //取的数据
            Uri uri = data.getData();
            ContentResolver cr = getContentResolver();
            Bitmap bitmap = null;
            //其他信息
            Bundle extras = null;

            //如果是插入图片
            if (requestCode == 1) {

                //图片绝对路径
                //目录下的绝对位置，因为这个照片一般都是存在手机其他文件夹，所以是绝对路径
                String[] proj = {MediaStore.Images.Media.DATA};
                //可以使Activity接管返回数据对象的生命周期,ManagedQuery()已过时
                Cursor actualimagecursor = getContentResolver().query(uri, proj, null, null, null);
                int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                actualimagecursor.moveToFirst();
                String path = actualimagecursor.getString(actual_image_column_index);
                try {
                    //将对象存入bitmap中
                    bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //插入图片,s是最小的限度
                InsertBitmap(bitmap, 480, path);
            }

            //如果选择的是拍照
            else if (requestCode == 2) {
                System.out.println("----------------------------------拍照啦");


                try {
                    if (uri != null) {
                        //通过uri获取图片的方法
                        try {
                            MediaStore.Images.Media.getBitmap(cr, uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                    else {
                        extras = data.getExtras();
                        extras.getParcelable("data");
                    }
                    //将拍的照片存入指定的文件夹下
                    //获得系统当前时间，并以该时间作为文件名
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    String str = formatter.format(curDate);
                    String paintPath = "";
                    str = str + "paint.png";
                    File dir = new File("/sdcard/notes/");
                    File file = new File("/sdcard/notes/", str);
                    if (!dir.exists()) {
                        dir.mkdir();
                    } else {
                        if (file.exists()) {
                            file.delete();
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        // 将 bitmap 压缩成其他格式的图片数据
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();
                        String picturePath = "/sdcard/notes/" + str;
                        //插入图片
                        InsertBitmap(bitmap, 480, picturePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //如果选择的是绘图,返回的是绘图的结果
            else if (requestCode == 3) {
                extras = data.getExtras();
                String path = extras.getString("PaintPath");
                //通过路径取出图片，放入bitmap中
                bitmap = BitmapFactory.decodeFile(path);
                //插入绘图文件
                InsertBitmap(bitmap, 480, path);
            }
            //在添加记事Activity中取出返回的录音文件的路径，并将相应的录音图标添加在记事中
            else if (requestCode == 4) {
                extras = data.getExtras();
                String audioPath = extras.getString("audio");
                //插入录音图标
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.record_icon);
                //插入录音图标
                InsertBitmap(bitmap, 200, audioPath);
            }
        }
    }

    //将图片等比例缩放到合适的大小并添加在EditText中
    //S就是缩放的大小
    private void InsertBitmap(Bitmap bitmap, int S, String imgPath) {
        bitmap = resize(bitmap, S);
        //添加边框
        bitmap = getBitmapHuaSeBianKuang(bitmap);
        //SpannableString其实和String一样，都是一种字符串类型，SpannableString可以直接作为TextView的显示文本
        //不同的是SpannableString可以通过使用其方法setSpan方法实现字符串各种形式风格的显示
        //重要的是可以指定设置的区间，也就是为字符串指定下标区间内的子字符串设置格式。
        //编辑框中加图片,解决图文混排
        final ImageSpan imageSpan = new ImageSpan(this, bitmap);
        SpannableString spannableString = new SpannableString(imgPath);
        //Object what主要是对文字的一些属性的设置，大小，颜色，已经文字的背景颜色
        //flags：对一段文字设置一些标识
        //0-length spans with type SPAN_MARK_MARK behave like text marks:
        //they remain at their original offset when text is inserted
        //at that offset. Conceptually, the text is added after the mark.
        spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
        //光标移到下一行
        Editable editable = et_note.getEditableText();
        int selectionIndex = et_note.getSelectionStart();
        spannableString.getSpans(0, spannableString.length(), ImageSpan.class);

        //将图片添加进EditText中
        editable.insert(selectionIndex, spannableString);
        //添加图片后自动空出两行
        et_note.append("\n\n");

        //如果是浏览已经存在于数据库中的记事时，在加载数据的同时，同样用ListView来记住所有的图片及录音的位置和路径
        //用List记录该录音的位置及所在路径，用于单击事件
        Map<String, String> map = new HashMap<String, String>();
        map.put("location", selectionIndex + "-" + (selectionIndex + spannableString.length()));
        map.put("path", imgPath);
        imgList.add(map);
    }

    //缩放
    private Bitmap resize(Bitmap bitmap, int S) {
        //进行图片的等比例缩放
        int imgHeight = bitmap.getHeight();
        int imgWidth = bitmap.getWidth();
        //比例
        double proportion = imgWidth * 1.0 / imgHeight;
        double sqrtLength = Math.sqrt(proportion * proportion + 1);

        //新的缩略图的大小
        double newImgW = S * (proportion / sqrtLength);
        double newImgH = S * (1 / sqrtLength);

        //进行缩放前后的比值从而确定了对原图的缩放大小
        float scaleW = (float) (newImgW / imgWidth);
        float scaleH = (float) (newImgH / imgHeight);

        //定义矩阵
        Matrix mx = new Matrix();
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
        return bitmap;
    }

    //给图片加边框，并返回边框后的图片
    public Bitmap getBitmapHuaSeBianKuang(Bitmap bitmap) {
        float frameSize = 0.2f;
        Matrix matrix = new Matrix();

        // 用来做底图
        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 设置底图为画布
        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        float scale_x = (bitmap.getWidth() - 2 * frameSize - 2) * 1f
                / (bitmap.getWidth());
        float scale_y = (bitmap.getHeight() - 2 * frameSize - 2) * 1f
                / (bitmap.getHeight());
        matrix.reset();
        matrix.postScale(scale_x, scale_y);

        // 对相片大小处理(减去边框的大小)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);

        // 绘制底图边框
        canvas.drawRect(
                new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()),
                paint);
        // 绘制灰色边框
        paint.setColor(Color.BLUE);
        canvas.drawRect(
                new Rect((int) (frameSize), (int) (frameSize), bitmapbg
                        .getWidth() - (int) (frameSize), bitmapbg.getHeight()
                        - (int) (frameSize)), paint);

        canvas.drawBitmap(bitmap, frameSize + 1, frameSize + 1, paint);

        return bitmapbg;
    }

    /**
     * 当编辑好一个记事时，这时点击顶部的保存按钮，就将所写的记事插入到数据库中
     * 如果记事里面有图片，录音等，并没有图片，录音本身存储不到数据库，而将其所在路径存储在数据库中
     * 等到再次查看时，再从数据库中读取，并根据所保存的路径取出源文件。
     * 保存记事的思想就是取出EditText中的内容，并从中截取前一部分作为该记事的标题，以及同时也保存了添加记事的时间。
     */
    //设置按钮监听器,将多个触摸事件集成到一个event中
    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_back:
                    //当前Activity结束，则返回上一个Activity
                    AddActivity.this.finish();
                    break;

                //将记事添加到数据库中
                case R.id.bt_save:
                    //取得EditText中的内容
                    String context = et_note.getText().toString();
                    if (context.isEmpty()) {
                        Toast.makeText(AddActivity.this, "记事为空!", Toast.LENGTH_LONG).show();
                    } else {
                        //准备存储
                        //设置时间格式
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        //获取当前时间
                        Date curDate = new Date(System.currentTimeMillis());
                        String time = formatter.format(curDate);
                        //截取EditText中的前一部分作为标题，用于显示在主页列表中，用个gettitle方法实现
                        String title = getTitle(context);
                        //打开数据库
                        dop.create_db();
                        //editModel表示当前是新增记事还是修改记事
                        //判断是更新还是新增记事
                        if (editModel.equals("newAdd")) {
                            //将记事插入到数据库中
                            dop.insert_db(title, context, time);
                        }
                        //如果是编辑则更新记事即可
                        else if (editModel.equals("update")) {
                            dop.update_db(title, context, time, item_Id);
                        }
                        dop.close_db();
                        //结束当前activity
                        AddActivity.this.finish();
                    }
                    break;
            }
        }
    }

    //截取EditText中的前15字作为标题，用于显示在主页列表中
    private String getTitle(String context) {
        //定义正则表达式，用于匹配路径
        //www
        Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
        Matcher m = p.matcher(context);
        StringBuffer strBuff = new StringBuffer();
        //先是个空的之后往里面加东西
        String title = "";
        int startIndex = 0;
        while (m.find()) {
            //取出路径前的文字
            if (m.start() > 0) {
                strBuff.append(context.substring(startIndex, m.start()));
            }
            //取出路径
            String path = m.group().toString();
            //取出路径的后缀
            String type = path.substring(path.length() - 3, path.length());
            //判断附件的类型
            if (type.equals("amr")) {
                strBuff.append("[录音]");
            } else {
                strBuff.append("[图片]");
            }
            startIndex = m.end();
            //只取出前15个字作为标题
            if (strBuff.length() > 15) {
                //统一将回车,等特殊字符换成空格
                title = strBuff.toString().replaceAll("\r|\n|\t", " ");
                return title;
            }
        }
        strBuff.append(context.substring(startIndex, context.length()));
        //统一将回车,等特殊字符换成空格
        title = strBuff.toString().replaceAll("\r|\n|\t", " ");
        return title;
    }

    //加载显示数据
    private void loadData() {

        //如果是新增记事模式，则将editText清空
        if (editModel.equals("newAdd")) {
            et_note.setText("");
        }
        //如果编辑的是已存在的记事，则将数据库的保存的数据取出，并显示在EditText中
        else if (editModel.equals("update")) {
            tv_title.setText("编辑记事");

            //建数据库
            dop.create_db();
            Cursor cursor = dop.query_db(item_Id);
            //移动到首位
            cursor.moveToFirst();
            //取出数据库中相应的字段内容
            String context = cursor.getString(cursor.getColumnIndex("context"));

            //定义正则表达式，用于匹配路径
            Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
            Matcher m = p.matcher(context);
            int startIndex = 0;
            while (m.find()) {
                //取出路径前的文字
                if (m.start() > 0) {
                    et_note.append(context.substring(startIndex, m.start()));
                }

                SpannableString ss = new SpannableString(m.group().toString());

                //取出路径
                String path = m.group().toString();
                //取出路径的后缀
                String type = path.substring(path.length() - 3, path.length());
                Bitmap bm = null;
                Bitmap rbm = null;
                //判断附件的类型，如果是录音文件，则从资源文件中加载图片
                if (type.equals("amr")) {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.record_icon);
                    //缩放图片
                    rbm = resize(bm, 200);

                } else {
                    //取出图片
                    bm = BitmapFactory.decodeFile(m.group());
                    //缩放图片
                    rbm = resize(bm, 480);
                }

                //为图片添加边框效果
                rbm = getBitmapHuaSeBianKuang(rbm);
                System.out.println(rbm.getWidth() + "-------" + rbm.getHeight());

                ImageSpan span = new ImageSpan(this, rbm);
                ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                et_note.append(ss);
                startIndex = m.end();

                //如何在图文混排的EditText中的判断单击的是图片，录音，还是文字呢
                // 这就需要从EditText中的识别那些是图片，那些是文字
                // 再进一步对图片分析到底单击的是那一个图片，从而实现查看具体图片及录音的功能

                //1.记录EditText中每个图片的位置及所在源路径
                //每次单击记事列表项，进入查看记事，在加载数据的同时将所有图片及录音的位置及路径记录下来
                //用List记录该录音的位置及所在路径，用于单击事件判断查看的是录音还是图片
                Map<String, String> map = new HashMap<String, String>();
                map.put("location", m.start() + "-" + m.end());
                map.put("path", path);
                imgList.add(map);
            }
            //将最后一个图片之后的文字添加在TextView中
            et_note.append(context.substring(startIndex, context.length()));
            dop.close_db();
        }
    }

    //为edittext设置监听器
    // 为了判断单击的是图片还是普通文字
    //，用到了Spanned，ImageSpan是对图片还有文字显示的一些安排
    // 主要思想，就是判断当前单击的位置是否在图片的位置范围内
    private class TextClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Spanned s = et_note.getText();
            ImageSpan[] imageSpans;
            //getSpans函数获取目标文本的span:,这是全文都获取
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);

            //获取光标位置
            int selectionStart = et_note.getSelectionStart();
            for (ImageSpan span : imageSpans) {

                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                //找到图片
                if (selectionStart >= start && selectionStart < end) {
                    //查找当前单击的图片是哪一个图片
                    System.out.println(start + "-----------" + end);

                    String path = null;
                    //因为之前已经将这个图片都存在了list中，录音也是以录音图标的形式存储的
                    for (int i = 0; i < imgList.size(); i++) {
                        Map map = imgList.get(i);
                        //找到了
                        if (map.get("location").equals(start + "-" + end)) {
                            path = imgList.get(i).get("path");
                            break;
                        }
                    }

                    //接着判断当前图片是否是录音，如果为录音，则跳转到试听录音的Activity，如果不是，则跳转到查看图片的界面，判断hou'zhu后缀
                    //录音，则跳转到试听录音的Activity
                    if (path.substring(path.length() - 3, path.length()).equals("amr")) {
                        Intent intent = new Intent(AddActivity.this, ShowRecordActivity.class);
                        intent.putExtra("audioPath", path);
                        startActivity(intent);
                    }
                    //图片，则跳转到查看图片的界面
                    else {
                        //有两种方法，查看图片，第一种就是直接调用系统的图库查看图片，第二种是自定义Activity
                        //调用系统图库查看图片,这里我们不用因为还得打开图库，不友好
                        /*Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File(path);
						Uri uri = Uri.fromFile(file);
						intent.setDataAndType(uri, "image/*");*/
                        //使用自定义Activity
                        Intent intent = new Intent(AddActivity.this, ShowPictureActivity.class);
                        intent.putExtra("imgPath", path);
                        startActivity(intent);
                    }
                } else
                    //如果单击的是空白出或文字，则获得焦点，即打开软键盘
                    imm.showSoftInput(et_note, 0);
            }
        }
    }
}

