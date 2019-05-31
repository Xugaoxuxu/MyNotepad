package com.example.mynotepade;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

/**
 * @author Admin
 * @version $Rev$
 * @des ${保存查看删除，就是增删改查}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class DatabaseOperation {
    private SQLiteDatabase db;
    private Context context;

    //构造方法
    public DatabaseOperation(Context context, SQLiteDatabase db) {
        this.db = db;
        this.context = context;
    }

    //打开与创建数据库
    public void create_db() {
        //创建或打开
        db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/mynotepade.db3", null);
        db.execSQL("DROP TABLE IF EXISTS studentScore");

        if (db == null) {
            Toast.makeText(context, "创建数据库失败请重试", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(context, "创建数据库成功", Toast.LENGTH_SHORT).show();

        //创建一个表
        db.execSQL("create table if not exists notes(_id integer primary key autoincrement," +
                "title text," +
                "context text," +
                "time varchar(20))");
    }

    //插入,包含标题，文本以及时间信息
    public void insert_db(String title, String text, String time) {
        if (text.isEmpty()) {
            Toast.makeText(context, "各字段不能为空", Toast.LENGTH_SHORT).show();
        } else {
            db.execSQL("insert into notes(title,context,time) values('" + title + "','" + text + "','" + time + "');");
            Toast.makeText(context, "插入成功", Toast.LENGTH_SHORT).show();
        }
    }

    //更新修改
    public void update_db(String title, String text, String time, int item_ID) {
        if (text.isEmpty()) {
            Toast.makeText(context, "各字段不能为空", Toast.LENGTH_SHORT).show();
        } else {
            db.execSQL("update notes set context='" + text + "',title='" + title + "',time='" + time + "'where _id='" + item_ID + "'");
            Toast.makeText(context, "修改保存成功", Toast.LENGTH_SHORT).show();
        }
    }

    //查找,返回查找结果
    //查找全部
    public Cursor query_db() {
        Cursor cursor = db.rawQuery("select * from notes", null);
        return cursor;
    }

    //按ID查找
    public Cursor query_db(int item_ID) {
        Cursor cursor = db.rawQuery("select * from notes where _id='" + item_ID + "';", null);
        return cursor;
    }

    //删除数据库
    public void delete_db(int item_ID) {
        db.execSQL("delete from notes where _id ='" + item_ID + "'");
        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
    }

    //关闭数据库
    public void close_db() {
        db.close();
    }
}
