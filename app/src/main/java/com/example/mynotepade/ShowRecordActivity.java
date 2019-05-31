package com.example.mynotepade;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于单独查看音频的界面
 */
public class ShowRecordActivity extends Activity {

    private Timer mTimer;
    private AnimationDrawable ad_left,ad_right;
    private ImageView iv_record_wave_left,iv_record_wave_right,iv_microphone;
    private int isPlaying = 0;
    //语音操作对象
    private MediaPlayer mPlayer = null;
    private String audioPath;

    private TextView tv_recordTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_show_record);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_add);

        //设置标题
        TextView tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("查看录音");
        Button bt_back = (Button)findViewById(R.id.bt_back);

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果正在播放，就停止播放并且关闭这个Activity
                if(isPlaying == 1){
                    mPlayer.stop();
                    mPlayer.release();
                }
                ShowRecordActivity.this.finish();
            }
        });

        Button bt_del = (Button)findViewById(R.id.bt_save);
        bt_del.setBackgroundResource(R.drawable.paint_icon_delete);

        //获取语音文件的路径
        Intent intent = this.getIntent();
        audioPath = intent.getStringExtra("audioPath");

        //为麦克风的图标设置点击事件，令他可以实现暂停以及开始的功能
        iv_microphone = (ImageView)findViewById(R.id.iv_microphone);
        iv_microphone.setOnClickListener(new ClickEvent());

        iv_record_wave_left = (ImageView)findViewById(R.id.iv_record_wave_left);
        iv_record_wave_right = (ImageView)findViewById(R.id.iv_record_wave_right);

        //设置音波帧动画
        ad_left = (AnimationDrawable)iv_record_wave_left.getBackground();
        ad_right = (AnimationDrawable)iv_record_wave_right.getBackground();
        tv_recordTime = (TextView)findViewById(R.id.tv_recordTime);
    }

    //跟之前在录音界面中设置的一样，这个线程用于更新时间的UI
    final Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1 :
                    String time[] = tv_recordTime.getText().toString().split(":");
                    int hour = Integer.parseInt(time[0]);
                    int minute = Integer.parseInt(time[1]);
                    int second = Integer.parseInt(time[2]);

                    if(second < 59){
                        second++;

                    }
                    else if(second == 59 && minute < 59){
                        minute++;
                        second = 0;

                    }
                    if(second == 59 && minute == 59 && hour < 98){
                        hour++;
                        minute = 0;
                        second = 0;
                    }

                    time[0] = hour + "";
                    time[1] = minute + "";
                    time[2] = second + "";
                    //调整格式显示到屏幕上
                    if(second < 10)
                        time[2] = "0" + second;
                    if(minute < 10)
                        time[1] = "0" + minute;
                    if(hour < 10)
                        time[0] = "0" + hour;

                    //显示在TextView中
                    tv_recordTime.setText(time[0]+":"+time[1]+":"+time[2]);

                    break;

            }

        }
    };

    //点击事件的功能同之前的就直接复制了
    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {
            //试听
            if(isPlaying == 0){
                isPlaying = 1;
                mPlayer = new MediaPlayer();
                tv_recordTime.setText("00:00:00");
                mTimer = new Timer();
                mPlayer.setOnCompletionListener(new MediaCompletion());
                try {
                    mPlayer.setDataSource(audioPath);
                    mPlayer.prepare();
                    mPlayer.start();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                }, 1000,1000);
                //播放动画
                ad_left.start();
                ad_right.start();
            }
            //结束试听
            else{
                isPlaying = 0;
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
                mTimer.cancel();
                mTimer = null;
                //停止动画
                ad_left.stop();
                ad_right.stop();
            }
        }
    }

    //Called when the end of a media source is reached during playback.
    //就是播放结束的时候会调用的方法
    private class MediaCompletion implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //进行一些释放以及停止的操作
            mTimer.cancel();
            mTimer = null;
            isPlaying = 0;
            //停止动画
            ad_left.stop();
            ad_right.stop();
            Toast.makeText(ShowRecordActivity.this, "播放完毕", Toast.LENGTH_SHORT).show();
            tv_recordTime.setText("00:00:00");
        }
    }
}
