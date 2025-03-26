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
 * Google Tasks异步同步任务控制器，功能包括：
 * 1. 实现后台同步任务的生命周期管理
 * 2. 处理同步过程中的通知栏交互（进度/成功/失败）
 * 3. 封装与GTaskManager的协同工作
 * 4. 提供同步取消功能
 * 5. 同步状态回调通知（通过OnCompleteListener）
 * 6. 同步结果分类处理（网络错误/内部错误/用户取消）
 */
public class GTaskASyncTask extends AsyncTask<Void, String, Integer> {

    private static int GTASK_SYNC_NOTIFICATION_ID = 5234235;

    public interface OnCompleteListener {
        void onComplete();
    }

    private Context mContext;

    private NotificationManager mNotifiManager;

    private GTaskManager mTaskManager;

    private OnCompleteListener mOnCompleteListener;

    public GTaskASyncTask(Context context, OnCompleteListener listener) {
        mContext = context;
        mOnCompleteListener = listener;
        mNotifiManager = (NotificationManager) mContext
          .getSystemService(Context.NOTIFICATION_SERVICE);
        mTaskManager = GTaskManager.getInstance();
    }

    public void cancelSync() {
        mTaskManager.cancelSync();
    }

    public void publishProgess(String message) {
        publishProgress(new String[] {
          message
        });
    }

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

    @Override
    protected Integer doInBackground(Void... unused) {
        publishProgess(mContext.getString(R.string.sync_progress_login, NotesPreferenceActivity
          .getSyncAccountName(mContext)));
        return mTaskManager.sync(mContext, this);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        showNotification(R.string.ticker_syncing, progress[0]);
        if (mContext instanceof GTaskSyncService) {
            ((GTaskSyncService) mContext).sendBroadcast(progress[0]);
        }
    }

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