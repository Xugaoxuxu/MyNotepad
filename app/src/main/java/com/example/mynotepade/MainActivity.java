package com.example.mynotepade;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    private Button bt_add;
    private Button bt_setting;
    private SQLiteDatabase db;
    private DatabaseOperation dop;
    private ListView lv_notes;
    private TextView tv_note_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //替换标题栏
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.new_title);

        bt_add = (Button)findViewById(R.id.bt_add);
        bt_add.setOnClickListener(new ClickEvent());
        bt_setting = (Button)findViewById(R.id.bt_setting);
        //数据库操作
        dop = new DatabaseOperation(this, db);
        lv_notes = (ListView) findViewById(R.id.lv_notes);

        //申请权限
        performCodeWithPermission("app运行有关的权限申请", new PermissionCallback() {
            @Override
            public void hasPermission() {

            }

            @Override
            public void noPermission() {
                Toast.makeText(MainActivity.this, "请先允许相关权限！", Toast.LENGTH_SHORT).show();
            }
        }, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
    }

    //activity开始时调用的方法
    @Override
    protected void onStart() {
        super.onStart();
        //显示记事列表
        showNotesList();
        //为记事本列表添加监听器
        lv_notes.setOnItemClickListener(new ItemClickEvent());
        //为记事本列表添加长按事件
        lv_notes.setOnItemLongClickListener(new ItemLongClickEvent());
    }

    //显示记事列表
    private void showNotesList() {
        //创建或打开数据库
        dop.create_db();
        Cursor cursor = dop.query_db();
        //适配器显示listview
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.note_item,
                cursor,
                new String[]{"_id", "title", "time"}, new int[]{R.id.tv_note_id, R.id.tv_note_title, R.id.tv_note_time});
        lv_notes.setAdapter(adapter);
        dop.close_db();
    }

    //记事列表单击监听器
    //点击列表项目，自然就是查看或修改记事的详细内容了
    class ItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            //用textview的ID来表示操作
            tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
            //找到这个id
            int item_id = Integer.parseInt(tv_note_id.getText().toString());
            //进入添加修改界面
            //当单击一个列表项时，就会新打开一个activity，用于显示记事的详细内容，这里依然用的是新增记事Activity，
            //这样做的好处就是在查看记事的同时，也可以修改
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            intent.putExtra("editModel", "update");
            intent.putExtra("noteId", item_id);
            startActivity(intent);
        }
    }


    //为记事列表添加长按事件
    //当长按列表项目时，弹出操作选择，共有两个，一个是编辑，一个是删除
    //删除，当选中了删除时，就将相应的记事条目从数据库中删除，并刷新列表
    class ItemLongClickEvent implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            tv_note_id = (TextView)view.findViewById(R.id.tv_note_id);
            int item_id = Integer.parseInt(tv_note_id.getText().toString());
            simpleList(item_id);
            return true;
        }
    }
    //简单列表对话框，用于选择操作
    public void simpleList(final int item_id) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.custom_dialog);
        alertDialogBuilder.setTitle("选择操作");
        //标题的图标
        alertDialogBuilder.setIcon(R.drawable.ic_launcher);
        alertDialogBuilder.setItems(R.array.itemOperation, new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    //编辑
                    case 0:
                        Intent intent = new Intent(MainActivity.this, AddActivity.class);
                        intent.putExtra("editModel", "update");
                        intent.putExtra("noteId", item_id);
                        startActivity(intent);
                        break;
                    //删除
                    case 1:
                        dop.create_db();
                        dop.delete_db(item_id);
                        dop.close_db();
                        //刷新列表显示
                        lv_notes.invalidate();
                        showNotesList();
                        break;
                }
            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.bt_add :
                    Intent intent = new Intent(MainActivity.this,AddActivity.class);
                    intent.putExtra("editModel", "newAdd");
                    startActivity(intent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
