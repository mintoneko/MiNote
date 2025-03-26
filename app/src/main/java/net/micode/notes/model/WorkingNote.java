package net.micode.notes.model;

import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;
import net.micode.notes.tool.ResourceParser.NoteBgResources;

/**
 * WorkingNote 类用于表示一个正在编辑或操作的笔记对象。
 * 它封装了笔记的基本信息、数据内容以及相关的操作。
 * 通过该类，可以创建新笔记、加载已有笔记、保存笔记以及管理笔记的属性。
 */
public class WorkingNote {
  // Note for the working note
  // 笔记对象实例
  private Note mNote;
  // Note Id
  // 笔记ID，如果为0表示是新创建的笔记
  private long mNoteId;
  // Note content
  // 笔记的内容
  private String mContent;
  // Note mode
  // 笔记的模式
  private int mMode;
  // 笔记的提醒时间，0表示没有提醒
  private long mAlertDate;
  // 笔记的最后修改时间
  private long mModifiedDate;
  // 笔记的背景颜色ID
  private int mBgColorId;
  // 关联的桌面小部件ID
  private int mWidgetId;
  // 小部件类型
  private int mWidgetType;
  // 父文件夹ID
  private long mFolderId;
  // 上下文对象，用于数据库操作
  private Context mContext;
  // 日志标签
  private static final String TAG = "WorkingNote";
  // 标志笔记是否已删除
  private boolean mIsDeleted;
  // 笔记设置更改的监听器
  private NoteSettingChangedListener mNoteSettingStatusListener;
  /**
   * 数据表的列映射，用于从数据库中查询笔记数据。
   */
  public static final String[] DATA_PROJECTION = new String[]{
    DataColumns.ID,
    DataColumns.CONTENT,
    DataColumns.MIME_TYPE,
    DataColumns.DATA1,
    DataColumns.DATA2,
    DataColumns.DATA3,
    DataColumns.DATA4,
  };
  /**
   * 笔记表的列映射，用于查询笔记的基本信息。
   */
  public static final String[] NOTE_PROJECTION = new String[]{
    NoteColumns.PARENT_ID,
    NoteColumns.ALERTED_DATE,
    NoteColumns.BG_COLOR_ID,
    NoteColumns.WIDGET_ID,
    NoteColumns.WIDGET_TYPE,
    NoteColumns.MODIFIED_DATE
  };
  // 数据表的列索引
  private static final int DATA_ID_COLUMN = 0;

  private static final int DATA_CONTENT_COLUMN = 1;

  private static final int DATA_MIME_TYPE_COLUMN = 2;

  private static final int DATA_MODE_COLUMN = 3;

  private static final int NOTE_PARENT_ID_COLUMN = 0;

  private static final int NOTE_ALERTED_DATE_COLUMN = 1;

  private static final int NOTE_BG_COLOR_ID_COLUMN = 2;

  private static final int NOTE_WIDGET_ID_COLUMN = 3;

  private static final int NOTE_WIDGET_TYPE_COLUMN = 4;

  private static final int NOTE_MODIFIED_DATE_COLUMN = 5;

  // New note construct

  /**
   * 创建一个新的笔记对象。
   *
   * @param context  上下文对象
   * @param folderId 笔记所在的文件夹ID
   */
  private WorkingNote(Context context, long folderId) {
    mContext = context;
    mAlertDate = 0;
    mModifiedDate = System.currentTimeMillis();
    mFolderId = folderId;
    mNote = new Note();
    mNoteId = 0;
    mIsDeleted = false;
    mMode = 0;
    mWidgetType = Notes.TYPE_WIDGET_INVALIDE;
  }

  // Existing note construct

  /**
   * 加载已有的笔记对象。
   *
   * @param context  上下文对象
   * @param noteId   笔记ID
   * @param folderId 笔记所在的文件夹ID
   */
  private WorkingNote(Context context, long noteId, long folderId) {
    mContext = context;
    mNoteId = noteId;
    mFolderId = folderId;
    mIsDeleted = false;
    mNote = new Note();
    loadNote();
  }

  /**
   * 从数据库中加载笔记的基本信息。
   */
  private void loadNote() {
    Cursor cursor = mContext.getContentResolver().query(
      ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mNoteId), NOTE_PROJECTION, null,
      null, null);

    if (cursor != null) {
      if (cursor.moveToFirst()) {
        mFolderId = cursor.getLong(NOTE_PARENT_ID_COLUMN);
        mBgColorId = cursor.getInt(NOTE_BG_COLOR_ID_COLUMN);
        mWidgetId = cursor.getInt(NOTE_WIDGET_ID_COLUMN);
        mWidgetType = cursor.getInt(NOTE_WIDGET_TYPE_COLUMN);
        mAlertDate = cursor.getLong(NOTE_ALERTED_DATE_COLUMN);
        mModifiedDate = cursor.getLong(NOTE_MODIFIED_DATE_COLUMN);
      }
      cursor.close();
    } else {
      Log.e(TAG, "No note with id:" + mNoteId);
      throw new IllegalArgumentException("Unable to find note with id " + mNoteId);
    }
    loadNoteData();
  }

  /**
   * 从数据库中加载笔记的内容和数据。
   */
  private void loadNoteData() {
    Cursor cursor = mContext.getContentResolver().query(Notes.CONTENT_DATA_URI, DATA_PROJECTION,
      DataColumns.NOTE_ID + "=?", new String[]{
        String.valueOf(mNoteId)
      }, null);

    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String type = cursor.getString(DATA_MIME_TYPE_COLUMN);
          if (DataConstants.NOTE.equals(type)) {
            mContent = cursor.getString(DATA_CONTENT_COLUMN);
            mMode = cursor.getInt(DATA_MODE_COLUMN);
            mNote.setTextDataId(cursor.getLong(DATA_ID_COLUMN));
          } else if (DataConstants.CALL_NOTE.equals(type)) {
            mNote.setCallDataId(cursor.getLong(DATA_ID_COLUMN));
          } else {
            Log.d(TAG, "Wrong note type with type:" + type);
          }
        } while (cursor.moveToNext());
      }
      cursor.close();
    } else {
      Log.e(TAG, "No data with id:" + mNoteId);
      throw new IllegalArgumentException("Unable to find note's data with id " + mNoteId);
    }
  }

  /**
   * 创建一个空笔记
   *
   * @param context          应用上下文
   * @param folderId         文件夹ID
   * @param widgetId         小部件ID
   * @param widgetType       小部件类型
   * @param defaultBgColorId 默认背景颜色ID
   * @return 创建的 WorkingNote 对象
   */
  public static WorkingNote createEmptyNote(Context context, long folderId, int widgetId,
                                            int widgetType, int defaultBgColorId) {
    WorkingNote note = new WorkingNote(context, folderId);
    note.setBgColorId(defaultBgColorId);
    note.setWidgetId(widgetId);
    note.setWidgetType(widgetType);
    return note;
  }

  /**
   * 加载已存在的笔记
   *
   * @param context 应用上下文
   * @param id      笔记ID
   * @return 加载的 WorkingNote 对象
   */
  public static WorkingNote load(Context context, long id) {
    return new WorkingNote(context, id, 0);
  }

  /**
   * 保存笔记
   *
   * @return 保存是否成功
   */
  public synchronized boolean saveNote() {
    if (isWorthSaving()) { // 判断笔记是否值得保存
      if (!existInDatabase()) { // 判断笔记是否存在于数据库
        if ((mNoteId = Note.getNewNoteId(mContext, mFolderId)) == 0) {
          Log.e(TAG, "Create new note fail with id:" + mNoteId);
          return false; // 创建笔记失败
        }
      }

      mNote.syncNote(mContext, mNoteId); // 同步笔记数据到数据库

      /**
       * Update widget content if there exist any widget of this note
       * 如果笔记关联了桌面小部件，则通知监听器更新小部件
       */
      if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
        && mWidgetType != Notes.TYPE_WIDGET_INVALIDE
        && mNoteSettingStatusListener != null) {
        mNoteSettingStatusListener.onWidgetChanged();
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * 判断笔记是否已存在于数据库中
   *
   * @return 是否存在
   */
  public boolean existInDatabase() {
    return mNoteId > 0;
  }

  /**
   * 判断笔记是否值得保存
   *
   * @return 是否值得保存
   */
  private boolean isWorthSaving() {
    if (mIsDeleted || (!existInDatabase() && TextUtils.isEmpty(mContent))
      || (existInDatabase() && !mNote.isLocalModified())) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * 设置监听器以监测笔记设置的变化
   *
   * @param l 监听器实例
   */
  public void setOnSettingStatusChangedListener(NoteSettingChangedListener l) {
    mNoteSettingStatusListener = l;
  }

  /**
   * 设置笔记的提醒时间
   *
   * @param date 提醒时间
   * @param set  是否设置提醒
   */
  public void setAlertDate(long date, boolean set) {
    if (date != mAlertDate) {
      mAlertDate = date;
      mNote.setNoteValue(NoteColumns.ALERTED_DATE, String.valueOf(mAlertDate));
    }
    if (mNoteSettingStatusListener != null) {
      mNoteSettingStatusListener.onClockAlertChanged(date, set);
    }
  }

  /**
   * 标记笔记是否被删除
   *
   * @param mark 删除标志
   */
  public void markDeleted(boolean mark) {
    mIsDeleted = mark;
    if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
      && mWidgetType != Notes.TYPE_WIDGET_INVALIDE && mNoteSettingStatusListener != null) {
      mNoteSettingStatusListener.onWidgetChanged();
    }
  }

  /**
   * 设置笔记的背景颜色
   *
   * @param id 背景颜色ID
   */
  public void setBgColorId(int id) {
    if (id != mBgColorId) {
      mBgColorId = id;
      if (mNoteSettingStatusListener != null) {
        mNoteSettingStatusListener.onBackgroundColorChanged();
      }
      mNote.setNoteValue(NoteColumns.BG_COLOR_ID, String.valueOf(id));
    }
  }

  /**
   * 设置笔记的检查清单模式
   *
   * @param mode 新的模式
   */
  public void setCheckListMode(int mode) {
    if (mMode != mode) {
      if (mNoteSettingStatusListener != null) {
        mNoteSettingStatusListener.onCheckListModeChanged(mMode, mode);
      }
      mMode = mode;
      mNote.setTextData(TextNote.MODE, String.valueOf(mMode));
    }
  }

  /**
   * 设置笔记的小部件类型
   *
   * @param type 小部件类型
   */
  public void setWidgetType(int type) {
    if (type != mWidgetType) {
      mWidgetType = type;
      // 更新数据库中的小部件类型
      mNote.setNoteValue(NoteColumns.WIDGET_TYPE, String.valueOf(mWidgetType));
    }
  }

  /**
   * 设置笔记的小部件ID
   *
   * @param id 小部件ID
   */
  public void setWidgetId(int id) {
    if (id != mWidgetId) {
      mWidgetId = id;
      // 更新数据库中的小部件ID
      mNote.setNoteValue(NoteColumns.WIDGET_ID, String.valueOf(mWidgetId));
    }
  }

  /**
   * 设置笔记的内容文本
   *
   * @param text 要设置的笔记内容
   */
  public void setWorkingText(String text) {
    if (!TextUtils.equals(mContent, text)) {
      mContent = text;
      mNote.setTextData(DataColumns.CONTENT, mContent);
    }
  }

  /**
   * 将当前笔记转换为通话记录笔记
   *
   * @param phoneNumber 通话号码
   * @param callDate    通话时间
   */
  public void convertToCallNote(String phoneNumber, long callDate) {
    // 设置通话时间
    mNote.setCallData(CallNote.CALL_DATE, String.valueOf(callDate));
    // 设置通话号码
    mNote.setCallData(CallNote.PHONE_NUMBER, phoneNumber);
    // 将笔记父级设置为通话记录文件夹
    mNote.setNoteValue(NoteColumns.PARENT_ID, String.valueOf(Notes.ID_CALL_RECORD_FOLDER));
  }

  /**
   * 判断笔记是否设置了闹钟提醒
   *
   * @return 如果有提醒时间则返回true，否则返回false
   */
  public boolean hasClockAlert() {
    return (mAlertDate > 0 ? true : false);
  }

  /**
   * 获取笔记的内容
   *
   * @return 笔记内容字符串
   */
  public String getContent() {
    return mContent;
  }

  /**
   * 获取笔记的提醒时间
   *
   * @return 提醒时间的时间戳
   */
  public long getAlertDate() {
    return mAlertDate;
  }

  /**
   * 获取笔记的最后修改时间
   *
   * @return 最后修改时间的时间戳
   */
  public long getModifiedDate() {
    return mModifiedDate;
  }

  /**
   * 获取笔记背景颜色的资源ID
   *
   * @return 背景颜色的资源ID
   */
  public int getBgColorResId() {
    return NoteBgResources.getNoteBgResource(mBgColorId);
  }

  /**
   * 获取笔记的背景颜色ID
   *
   * @return 背景颜色ID
   */
  public int getBgColorId() {
    return mBgColorId;
  }

  /**
   * 获取笔记标题背景的资源ID
   *
   * @return 标题背景颜色的资源ID
   */
  public int getTitleBgResId() {
    return NoteBgResources.getNoteTitleBgResource(mBgColorId);
  }

  /**
   * 获取当前笔记的清单模式状态
   *
   * @return 清单模式状态（0：普通模式，1：清单模式）
   */
  public int getCheckListMode() {
    return mMode;
  }

  /**
   * 获取笔记的ID
   *
   * @return 笔记的唯一标识ID
   */
  public long getNoteId() {
    return mNoteId;
  }

  /**
   * 获取笔记所在的文件夹ID
   *
   * @return 文件夹的唯一标识ID
   */
  public long getFolderId() {
    return mFolderId;
  }

  /**
   * 获取小部件ID
   *
   * @return 小部件ID
   */
  public int getWidgetId() {
    return mWidgetId;
  }

  /**
   * 获取小部件类型
   *
   * @return 小部件类型
   */
  public int getWidgetType() {
    return mWidgetType;
  }

  /**
   * 定义笔记设置变化监听器接口
   */
  public interface NoteSettingChangedListener {
    /**
     * Called when the background color of current note has just changed
     * 当笔记背景颜色发生变化时调用
     */
    void onBackgroundColorChanged();

    /**
     * Called when user set clock
     * 当用户设置或取消闹钟提醒时调用
     *
     * @param date 提醒时间
     * @param set  是否设置提醒
     */
    void onClockAlertChanged(long date, boolean set);

    /**
     * Call when user create note from widget
     * 当从小部件创建笔记时调用
     */
    void onWidgetChanged();

    /**
     * Call when switch between check list mode and normal mode
     * 当清单模式和普通模式之间切换时调用
     *
     * @param oldMode is previous mode before change 旧模式
     * @param newMode is new mode 新模式
     */
    void onCheckListModeChanged(int oldMode, int newMode);
  }
}