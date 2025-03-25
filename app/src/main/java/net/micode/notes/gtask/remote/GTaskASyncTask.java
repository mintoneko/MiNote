package net.micode.notes.gtask.remote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import net.micode.notes.R;
import net.micode.notes.ui.NotesListActivity;
import net.micode.notes.ui.NotesPreferenceActivity;


/**
 * 异步任务类，用于处理Google Tasks的同步操作
 */
public class GTaskASyncTask extends AsyncTask<Void, String, Integer> {

  // 同步通知的唯一ID
  private static int GTASK_SYNC_NOTIFICATION_ID = 5234235;

  /**
   * 同步完成回调接口
   */
  public interface OnCompleteListener {
    void onComplete();
  }

  // 上下文对象
  private Context mContext;
  // 通知管理器
  private NotificationManager mNotifiManager;
  // 任务管理器实例
  private GTaskManager mTaskManager;
  // 同步完成监听器
  private OnCompleteListener mOnCompleteListener;


  /**
   * 构造函数
   *
   * @param context  上下文对象
   * @param listener 同步完成监听器
   */
  public GTaskASyncTask(Context context, OnCompleteListener listener) {
    mContext = context;
    mOnCompleteListener = listener;
    mNotifiManager = (NotificationManager) mContext
      .getSystemService(Context.NOTIFICATION_SERVICE);
    mTaskManager = GTaskManager.getInstance();
  }


  /**
   * 取消同步操作
   */
  public void cancelSync() {
    mTaskManager.cancelSync();
  }

  /**
   * 发布同步进度
   *
   * @param message 要显示的进度信息
   */
  public void publishProgess(String message) {
    publishProgress(new String[]{
      message
    });
  }

  /**
   * 显示同步状态通知
   *
   * @param tickerId 通知标题资源ID
   * @param content  通知内容
   */
  private void showNotification(int tickerId, String content) {
    Notification notification = new Notification(R.drawable.notification, mContext
      .getString(tickerId), System.currentTimeMillis());
    notification.defaults = Notification.DEFAULT_LIGHTS;
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    PendingIntent pendingIntent;
    if (tickerId != R.string.ticker_success) {
      pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
        NotesPreferenceActivity.class), 0);

    } else {
      pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
        NotesListActivity.class), 0);
    }
    //notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), content,
    //       pendingIntent);
    mNotifiManager.notify(GTASK_SYNC_NOTIFICATION_ID, notification);
  }

  // 后台执行任务
  @Override
  protected Integer doInBackground(Void... unused) {
    publishProgess(mContext.getString(R.string.sync_progress_login, NotesPreferenceActivity
      .getSyncAccountName(mContext)));
    return mTaskManager.sync(mContext, this);
  }

  // 更新进度
  @Override
  protected void onProgressUpdate(String... progress) {
    showNotification(R.string.ticker_syncing, progress[0]);
    if (mContext instanceof GTaskSyncService) {
      ((GTaskSyncService) mContext).sendBroadcast(progress[0]);
    }
  }

  // 任务完成后回调
  @Override
  protected void onPostExecute(Integer result) {
    if (result == GTaskManager.STATE_SUCCESS) {
      showNotification(R.string.ticker_success, mContext.getString(
        R.string.success_sync_account, mTaskManager.getSyncAccount()));
      NotesPreferenceActivity.setLastSyncTime(mContext, System.currentTimeMillis());
    } else if (result == GTaskManager.STATE_NETWORK_ERROR) {
      showNotification(R.string.ticker_fail, mContext.getString(R.string.error_sync_network));
    } else if (result == GTaskManager.STATE_INTERNAL_ERROR) {
      showNotification(R.string.ticker_fail, mContext.getString(R.string.error_sync_internal));
    } else if (result == GTaskManager.STATE_SYNC_CANCELLED) {
      showNotification(R.string.ticker_cancel, mContext
        .getString(R.string.error_sync_cancelled));
    }
    if (mOnCompleteListener != null) {
      new Thread(new Runnable() {

        public void run() {
          mOnCompleteListener.onComplete();
        }
      }).start();
    }
  }
}