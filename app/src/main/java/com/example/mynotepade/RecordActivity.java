package com.example.mynotepade;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 实现了录音的功能，同时，也实现了计时，试听，以及用逐帧动画的功能
 */
public class RecordActivity extends Activity {

    private Button bt_save;
    private Button bt_back;
    private Button bt_record;
    private ImageView iv_microphone;
    private ImageView iv_record_wave_left;
    private ImageView iv_record_wave_right;
    private AnimationDrawable ad_left;
    private AnimationDrawable ad_right;
    private TextView tv_recordTime;
    private String[] time;

    //开始录音的标志
    private int isReading = 0;
    private String FilePath = null;
    private Timer mTimer;
    private MediaRecorder mRecorder;
    private MediaPlayer mMediaPlayer;
    private int isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将标题栏消除，设置为自己的标题栏
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_record);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_add);
        //将自定义标题栏的标题改为录音
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("录音");

        //找到控件
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_back = (Button) findViewById(R.id.bt_back);
        //设置监听
        bt_back.setOnClickListener(new ClickEvent());
        bt_save.setOnClickListener(new ClickEvent());

        bt_record = (Button) findViewById(R.id.bt_record);
        bt_record.setOnClickListener(new ClickEvent());

        iv_microphone = (ImageView) findViewById(R.id.iv_microphone);
        iv_microphone.setOnClickListener(new ClickEvent());

        iv_record_wave_left = (ImageView) findViewById(R.id.iv_record_wave_left);
        iv_record_wave_right = (ImageView) findViewById(R.id.iv_record_wave_right);

        //逐帧动画
        ad_left = (AnimationDrawable) iv_record_wave_left.getBackground();
        ad_right = (AnimationDrawable) iv_record_wave_right.getBackground();


        tv_recordTime = (TextView) findViewById(R.id.tv_recordTime);
    }

    //为了更新UI所以要采用异步消息传递，在子线程进行
    Handler mHandler = new Handler() {
        private void handlerMessage(Message msg) {
            //是何种消息
            switch (msg.what) {
                //更新时间命令
                case 1:
                    //将时间转为转为字符串数组，并获取整形变量
                    String[] time = tv_recordTime.getText().toString().split(":");
                    int hour = Integer.parseInt(time[0]);
                    int minute = Integer.parseInt(time[1]);
                    int second = Integer.parseInt(time[2]);

                    //时间更改
                    if (second < 59) {
                        second++;
                        second = 0;
                    } else if (second == 59 && minute < 59) {
                        minute++;
                        second = 0;
                    } else if (second == 59 && minute == 59 && hour < 98) {
                        hour++;
                        minute = 0;
                        second = 0;
                    }

                    time[0] = hour + " ";
                    time[1] = minute + " ";
                    time[2] = second + " ";

                    //调整时间格式显示
                    if (second < 10) {
                        time[2] = "0" + second;
                    }
                    if (minute < 10) {
                        time[1] = "0" + minute;
                    }
                    if (hour < 10) {
                        time[0] = "0" + hour;
                    }
                    //显示在textview中
                    tv_recordTime.setText(time[0] + ":" + time[1] + ":" + time[2] + ":");
                    break;
                default:
                    break;
            }
        }
    };

    private class ClickEvent implements View.OnClickListener {

        private Timer mTimer;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //点击的是开始录音按钮
                case R.id.bt_record:
                    //开始录音
                    if (isReading == 0) {
                        //每一次调用录音，可以录音多次，至多满意为至，最后只将最后一次的录音文件保存，其他的删除
                        //当前有录音
                        if (FilePath != null) {
                            File oldFile = new File(FilePath);
                            oldFile.delete();
                        }
                        //获取系统的时间用来当作文件名
                        //时间日期格式
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒 绘图");
                        //获取当前的时间
                        Date curdate = new Date(System.currentTimeMillis());
                        String str = simpleDateFormat.format(curdate);

                        //录音文件名
                        str = str + "record.amr";
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

                        //赋值生成文件
                        FilePath = dir.getPath() + "/" + str;
                        //计时器
                        RecordActivity.this.mTimer = new Timer();

                        //设置麦克风图标为不可点击
                        iv_microphone.setClickable(false);
                        //将显示的时间设置为00：00：00
                        tv_recordTime.setText("00:00:00");
                        //将按钮替换为停止录音按钮
                        isReading = 1;
                        bt_record.setBackgroundResource(R.drawable.tabbar_record_stop);


                        //下面是录音的对象
                        mRecorder = new MediaRecorder();
                        //Microphone audio source
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        //设置输出品质
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        //设置输出路径
                        mRecorder.setOutputFile(FilePath);
                        //设置编码
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                        //准备
                        try {
                            mRecorder.prepare();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //开始
                        mRecorder.start();
                        //计时器开始工作
                        RecordActivity.this.mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                //传到子线程
                                Message message = new Message();
                                message.what = 1;
                                mHandler.sendMessage(message);
                            }
                        }, 1000, 1000);

                        //播放声波动画
                        ad_left.start();
                        ad_right.start();
                    }
                    //录音停止isRecord不为0,点击了停止后
                    else {
                        isReading = 0;
                        //换按钮的图片
                        bt_record.setBackgroundResource(R.drawable.tabbar_record_start);
                        //停止计时等
                        mRecorder.stop();
                        RecordActivity.this.mTimer.cancel();
                        RecordActivity.this.mTimer = null;
                        mRecorder.release();
                        mRecorder = null;

                        //将麦克风换为可点击
                        iv_microphone.setClickable(true);
                        //停止动画
                        ad_right.stop();
                        ad_left.stop();
                        Toast.makeText(RecordActivity.this, "单击麦克风图标可以试听，再次点击结束试听", Toast.LENGTH_SHORT).show();
                    }
                    break;
                //如果点击的是麦克风图标，就会播放录音
                case R.id.iv_microphone:
                    if (FilePath == null) {
                        Toast.makeText(RecordActivity.this, "没有可以试听的录音文件，请先录音", Toast.LENGTH_SHORT).show();
                    } else {
                        if (isPlaying == 0) {
                            //试听
                            isPlaying = 1;
                            mMediaPlayer = new MediaPlayer();
                            tv_recordTime.setText("00:00:00");
                            mTimer = new Timer();
                            //播放完成之后
                            mMediaPlayer.setOnCompletionListener(new MediaCompletion());
                            try {
                                mMediaPlayer.setDataSource(FilePath);
                                mMediaPlayer.prepare();
                                mMediaPlayer.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Message message = new Message();
                                    message.what = 1;
                                    mHandler.sendMessage(message);
                                }
                            }, 1000, 1000);
                            //播放动画
                            ad_left.start();
                            ad_right.start();
                        } else {
                            //结束试听
                            isPlaying = 0;
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                            mMediaPlayer = null;
                            mTimer.cancel();
                            mTimer = null;
                            //停止动画
                            ad_left.stop();
                            ad_right.stop();
                        }
                    }
                    break;
                //点击了确定保存按钮
                case R.id.bt_save:
                    //返回最终的路径
                    //得到该activity的intent对象
                    Intent intent = getIntent();
                    //存储
                    Bundle b = new Bundle();
                    b.putString("audio", FilePath);
                    intent.putExtras(b);
                    setResult(RESULT_OK, intent);
                    RecordActivity.this.finish();
                    break;

                //点击了返回按钮
                case R.id.bt_back:
                    //返回前将录音的文件删除
                    if (FilePath != null) {
                        File oldFile = new File(FilePath);
                        oldFile.delete();
                    }
                    RecordActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }

    //Called when the end of a media source is reached during playback.
    private class MediaCompletion implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mTimer.cancel();
            mTimer = null;
            isPlaying = 0;
            //停止播放动画
            ad_left.stop();
            ad_right.stop();
            Toast.makeText(RecordActivity.this, "播放完毕", Toast.LENGTH_SHORT).show();
            tv_recordTime.setText("00:00:00");
        }
    }
}
