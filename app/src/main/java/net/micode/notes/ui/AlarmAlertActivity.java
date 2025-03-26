package net.micode.notes.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;

import java.io.IOException;

/**
 * AlarmAlertActivity 负责处理闹钟通知。
 * 当触发闹钟时，它会显示一个警告对话框并播放闹钟声音。
 */
public class AlarmAlertActivity extends Activity implements OnClickListener, OnDismissListener {
    private long mNoteId; // 与闹钟关联的笔记 ID
    private String mSnippet; // 笔记内容的片段
    private static final int SNIPPET_PREW_MAX_LEN = 60; // 片段的最大长度
    MediaPlayer mPlayer; // 用于播放闹钟声音的 MediaPlayer 实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 移除标题栏

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED); // 即使屏幕锁定也显示活动

        // 如果屏幕关闭，添加额外的标志以唤醒屏幕
        if (!isScreenOn()) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        }

        Intent intent = getIntent();

        try {
            // 从 Intent 中提取笔记 ID 和片段
            mNoteId = Long.valueOf(intent.getData().getPathSegments().get(1));
            mSnippet = DataUtils.getSnippetById(this.getContentResolver(), mNoteId);
            // 如果片段长度超过最大值，则截断
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ? mSnippet.substring(0,
                    SNIPPET_PREW_MAX_LEN) + getResources().getString(R.string.notelist_string_info)
                    : mSnippet;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return; // 如果 Intent 数据无效，则退出
        }

        mPlayer = new MediaPlayer(); // 初始化 MediaPlayer
        // 检查笔记是否在数据库中可见
        if (DataUtils.visibleInNoteDatabase(getContentResolver(), mNoteId, Notes.TYPE_NOTE)) {
            showActionDialog(); // 显示警告对话框
            playAlarmSound(); // 播放闹钟声音
        } else {
            finish(); // 如果未找到笔记，则关闭活动
        }
    }

    /**
     * 检查屏幕是否当前处于打开状态。
     * @return 如果屏幕打开返回 true，否则返回 false。
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    /**
     * 使用 MediaPlayer 播放闹钟声音。
     */
    private void playAlarmSound() {
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM); // 获取默认闹钟声音的 URI

        // 检查静音模式是否影响闹钟流
        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams); // 使用静音模式流
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM); // 使用闹钟流
        }
        try {
            mPlayer.setDataSource(this, url); // 设置 MediaPlayer 的数据源
            mPlayer.prepare(); // 准备 MediaPlayer
            mPlayer.setLooping(true); // 设置闹钟声音循环播放
            mPlayer.start(); // 开始播放闹钟声音
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace(); // 处理 MediaPlayer 设置期间的异常
        }
    }

    /**
     * 显示一个带有用户交互选项的警告对话框。
     */
    private void showActionDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.app_name); // 设置对话框标题
        dialog.setMessage(mSnippet); // 设置对话框消息为笔记片段
        dialog.setPositiveButton(R.string.notealert_ok, this); // 添加“确定”按钮
        if (isScreenOn()) {
            dialog.setNegativeButton(R.string.notealert_enter, this); // 如果屏幕打开，添加“进入”按钮
        }
        dialog.show().setOnDismissListener(this); // 显示对话框并设置关闭监听器
    }

    /**
     * 处理警告对话框中的按钮点击事件。
     * @param dialog 对话框接口。
     * @param which 被点击的按钮。
     */
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE:
                // 当点击“进入”时，打开 NoteEditActivity
                Intent intent = new Intent(this, NoteEditActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Intent.EXTRA_UID, mNoteId);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 处理警告对话框的关闭事件。
     * 停止闹钟声音并结束活动。
     * @param dialog 对话框接口。
     */
    public void onDismiss(DialogInterface dialog) {
        stopAlarmSound(); // 停止闹钟声音
        finish(); // 关闭活动
    }

    /**
     * 停止闹钟声音并释放 MediaPlayer 资源。
     */
    private void stopAlarmSound() {
        if (mPlayer != null) {
            mPlayer.stop(); // 停止 MediaPlayer
            mPlayer.release(); // 释放 MediaPlayer 资源
            mPlayer = null; // 将 MediaPlayer 引用置为 null
        }
    }
}