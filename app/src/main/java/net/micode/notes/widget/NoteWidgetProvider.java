package net.micode.notes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.tool.ResourceParser;
import net.micode.notes.ui.NoteEditActivity;
import net.micode.notes.ui.NotesListActivity;

/**
 * 笔记桌面小部件提供器的抽象基类
 * 负责管理笔记应用的桌面小部件，提供创建、更新和删除小部件的基本功能
 * 子类需要继承此类并实现特定尺寸小部件的布局和资源相关方法
 */
public abstract class NoteWidgetProvider extends AppWidgetProvider {
  /**
   * 数据库查询的投影列，用于获取小部件显示所需的笔记信息
   */
  public static final String[] PROJECTION = new String[]{
    NoteColumns.ID,
    NoteColumns.BG_COLOR_ID,
    NoteColumns.SNIPPET
  };

  /**
   * 查询结果列的索引常量
   */
  public static final int COLUMN_ID = 0;
  public static final int COLUMN_BG_COLOR_ID = 1;
  public static final int COLUMN_SNIPPET = 2;

  private static final String TAG = "NoteWidgetProvider";

  /**
   * 当小部件被用户从桌面删除时调用
   * 将对应笔记的widget_id设置为无效值
   */
  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
    ContentValues values = new ContentValues();
    values.put(NoteColumns.WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    for (int i = 0; i < appWidgetIds.length; i++) {
      context.getContentResolver().update(Notes.CONTENT_NOTE_URI,
        values,
        NoteColumns.WIDGET_ID + "=?",
        new String[]{String.valueOf(appWidgetIds[i])});
    }
  }

  /**
   * 获取与指定小部件ID关联的笔记信息
   * 查询条件确保不会获取到已删除（在垃圾箱中）的笔记
   */
  private Cursor getNoteWidgetInfo(Context context, int widgetId) {
    return context.getContentResolver().query(Notes.CONTENT_NOTE_URI,
      PROJECTION,
      NoteColumns.WIDGET_ID + "=? AND " + NoteColumns.PARENT_ID + "<>?",
      new String[]{String.valueOf(widgetId), String.valueOf(Notes.ID_TRASH_FOLER)},
      null);
  }

  /**
   * 更新小部件的便捷方法，默认非隐私模式
   */
  protected void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    update(context, appWidgetManager, appWidgetIds, false);
  }

  /**
   * 更新小部件的核心方法
   * 根据小部件ID获取关联的笔记信息，并更新小部件UI
   *
   * @param privacyMode 是否为隐私模式，隐私模式下不显示具体笔记内容
   */
  private void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds,
                      boolean privacyMode) {
    for (int i = 0; i < appWidgetIds.length; i++) {
      if (appWidgetIds[i] != AppWidgetManager.INVALID_APPWIDGET_ID) {
        int bgId = ResourceParser.getDefaultBgId(context);
        String snippet = "";
        Intent intent = new Intent(context, NoteEditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Notes.INTENT_EXTRA_WIDGET_ID, appWidgetIds[i]);
        intent.putExtra(Notes.INTENT_EXTRA_WIDGET_TYPE, getWidgetType());

        Cursor c = getNoteWidgetInfo(context, appWidgetIds[i]);
        if (c != null && c.moveToFirst()) {
          if (c.getCount() > 1) {
            Log.e(TAG, "Multiple message with same widget id:" + appWidgetIds[i]);
            c.close();
            return;
          }
          snippet = c.getString(COLUMN_SNIPPET);
          bgId = c.getInt(COLUMN_BG_COLOR_ID);
          intent.putExtra(Intent.EXTRA_UID, c.getLong(COLUMN_ID));
          intent.setAction(Intent.ACTION_VIEW);
        } else {
          snippet = context.getResources().getString(R.string.widget_havenot_content);
          intent.setAction(Intent.ACTION_INSERT_OR_EDIT);
        }

        if (c != null) {
          c.close();
        }

        RemoteViews rv = new RemoteViews(context.getPackageName(), getLayoutId());
        rv.setImageViewResource(R.id.widget_bg_image, getBgResourceId(bgId));
        intent.putExtra(Notes.INTENT_EXTRA_BACKGROUND_ID, bgId);
        /**
         * 为小部件生成点击时的待处理意图
         */
        PendingIntent pendingIntent = null;
        if (privacyMode) {
          rv.setTextViewText(R.id.widget_text,
            context.getString(R.string.widget_under_visit_mode));
          pendingIntent = PendingIntent.getActivity(context, appWidgetIds[i], new Intent(
            context, NotesListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
          rv.setTextViewText(R.id.widget_text, snippet);
          pendingIntent = PendingIntent.getActivity(context, appWidgetIds[i], intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        }

        rv.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
      }
    }
  }

  /**
   * 根据背景ID获取对应的背景资源ID
   * 由子类实现，用于提供不同尺寸小部件的背景资源
   */
  protected abstract int getBgResourceId(int bgId);

  /**
   * 获取小部件的布局资源ID
   * 由子类实现，用于提供不同尺寸小部件的布局
   */
  protected abstract int getLayoutId();

  /**
   * 获取小部件类型
   * 由子类实现，用于标识不同尺寸的小部件
   */
  protected abstract int getWidgetType();
}
