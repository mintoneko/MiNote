package net.micode.notes.ui;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import net.micode.notes.data.Contact;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.tool.DataUtils;

/**
 * NoteItemData 是一个数据类，用于封装笔记项的数据。
 * 它从数据库游标中读取数据，并提供相应的访问方法。
 */
public class NoteItemData {
  // 数据库查询的列名数组
  static final String[] PROJECTION = new String[]{
    NoteColumns.ID,              // 笔记 ID
    NoteColumns.ALERTED_DATE,    // 提醒日期
    NoteColumns.BG_COLOR_ID,     // 背景颜色 ID
    NoteColumns.CREATED_DATE,    // 创建日期
    NoteColumns.HAS_ATTACHMENT,  // 是否有附件
    NoteColumns.MODIFIED_DATE,   // 修改日期
    NoteColumns.NOTES_COUNT,     // 笔记数量
    NoteColumns.PARENT_ID,       // 父文件夹 ID
    NoteColumns.SNIPPET,         // 笔记内容片段
    NoteColumns.TYPE,            // 笔记类型
    NoteColumns.WIDGET_ID,       // 小部件 ID
    NoteColumns.WIDGET_TYPE,     // 小部件类型
  };

  // 数据库列索引
  private static final int ID_COLUMN = 0;
  private static final int ALERTED_DATE_COLUMN = 1;
  private static final int BG_COLOR_ID_COLUMN = 2;
  private static final int CREATED_DATE_COLUMN = 3;
  private static final int HAS_ATTACHMENT_COLUMN = 4;
  private static final int MODIFIED_DATE_COLUMN = 5;
  private static final int NOTES_COUNT_COLUMN = 6;
  private static final int PARENT_ID_COLUMN = 7;
  private static final int SNIPPET_COLUMN = 8;
  private static final int TYPE_COLUMN = 9;
  private static final int WIDGET_ID_COLUMN = 10;
  private static final int WIDGET_TYPE_COLUMN = 11;

  // 笔记项的属性
  private long mId; // 笔记 ID
  private long mAlertDate; // 提醒日期
  private int mBgColorId; // 背景颜色 ID
  private long mCreatedDate; // 创建日期
  private boolean mHasAttachment; // 是否有附件
  private long mModifiedDate; // 修改日期
  private int mNotesCount; // 笔记数量
  private long mParentId; // 父文件夹 ID
  private String mSnippet; // 笔记内容片段
  private int mType; // 笔记类型
  private int mWidgetId; // 小部件 ID
  private int mWidgetType; // 小部件类型
  private String mName; // 联系人名称
  private String mPhoneNumber; // 联系人电话号码

  // 笔记项的状态
  private boolean mIsLastItem; // 是否是最后一项
  private boolean mIsFirstItem; // 是否是第一项
  private boolean mIsOnlyOneItem; // 是否是唯一一项
  private boolean mIsOneNoteFollowingFolder; // 是否是文件夹后跟随的唯一笔记
  private boolean mIsMultiNotesFollowingFolder; // 是否是文件夹后跟随的多个笔记

  /**
   * 构造函数，从游标中初始化笔记项数据
   *
   * @param context 上下文
   * @param cursor  数据库游标
   */
  public NoteItemData(Context context, Cursor cursor) {
    mId = cursor.getLong(ID_COLUMN);
    mAlertDate = cursor.getLong(ALERTED_DATE_COLUMN);
    mBgColorId = cursor.getInt(BG_COLOR_ID_COLUMN);
    mCreatedDate = cursor.getLong(CREATED_DATE_COLUMN);
    mHasAttachment = (cursor.getInt(HAS_ATTACHMENT_COLUMN) > 0);
    mModifiedDate = cursor.getLong(MODIFIED_DATE_COLUMN);
    mNotesCount = cursor.getInt(NOTES_COUNT_COLUMN);
    mParentId = cursor.getLong(PARENT_ID_COLUMN);
    mSnippet = cursor.getString(SNIPPET_COLUMN);
    // 移除片段中的标记
    mSnippet = mSnippet.replace(NoteEditActivity.TAG_CHECKED, "").replace(
      NoteEditActivity.TAG_UNCHECKED, "");
    mType = cursor.getInt(TYPE_COLUMN);
    mWidgetId = cursor.getInt(WIDGET_ID_COLUMN);
    mWidgetType = cursor.getInt(WIDGET_TYPE_COLUMN);

    // 如果是通话记录文件夹，获取联系人信息
    mPhoneNumber = "";
    if (mParentId == Notes.ID_CALL_RECORD_FOLDER) {
      mPhoneNumber = DataUtils.getCallNumberByNoteId(context.getContentResolver(), mId);
      if (!TextUtils.isEmpty(mPhoneNumber)) {
        mName = Contact.getContact(context, mPhoneNumber);
        if (mName == null) {
          mName = mPhoneNumber;
        }
      }
    }

    if (mName == null) {
      mName = "";
    }
    checkPostion(cursor); // 检查笔记项的位置状态
  }

  /**
   * 检查笔记项的位置状态
   *
   * @param cursor 数据库游标
   */
  private void checkPostion(Cursor cursor) {
    mIsLastItem = cursor.isLast();
    mIsFirstItem = cursor.isFirst();
    mIsOnlyOneItem = (cursor.getCount() == 1);
    mIsMultiNotesFollowingFolder = false;
    mIsOneNoteFollowingFolder = false;

    // 如果当前项是笔记且不是第一项，检查是否跟随在文件夹后
    if (mType == Notes.TYPE_NOTE && !mIsFirstItem) {
      int position = cursor.getPosition();
      if (cursor.moveToPrevious()) {
        if (cursor.getInt(TYPE_COLUMN) == Notes.TYPE_FOLDER
          || cursor.getInt(TYPE_COLUMN) == Notes.TYPE_SYSTEM) {
          if (cursor.getCount() > (position + 1)) {
            mIsMultiNotesFollowingFolder = true;
          } else {
            mIsOneNoteFollowingFolder = true;
          }
        }
        if (!cursor.moveToNext()) {
          throw new IllegalStateException("cursor move to previous but can't move back");
        }
      }
    }
  }

  // 以下为获取笔记项属性的方法

  public boolean isOneFollowingFolder() {
    return mIsOneNoteFollowingFolder;
  }

  public boolean isMultiFollowingFolder() {
    return mIsMultiNotesFollowingFolder;
  }

  public boolean isLast() {
    return mIsLastItem;
  }

  public String getCallName() {
    return mName;
  }

  public boolean isFirst() {
    return mIsFirstItem;
  }

  public boolean isSingle() {
    return mIsOnlyOneItem;
  }

  public long getId() {
    return mId;
  }

  public long getAlertDate() {
    return mAlertDate;
  }

  public long getCreatedDate() {
    return mCreatedDate;
  }

  public boolean hasAttachment() {
    return mHasAttachment;
  }

  public long getModifiedDate() {
    return mModifiedDate;
  }

  public int getBgColorId() {
    return mBgColorId;
  }

  public long getParentId() {
    return mParentId;
  }

  public int getNotesCount() {
    return mNotesCount;
  }

  public long getFolderId() {
    return mParentId;
  }

  public int getType() {
    return mType;
  }

  public int getWidgetType() {
    return mWidgetType;
  }

  public int getWidgetId() {
    return mWidgetId;
  }

  public String getSnippet() {
    return mSnippet;
  }

  public boolean hasAlert() {
    return (mAlertDate > 0);
  }

  public boolean isCallRecord() {
    return (mParentId == Notes.ID_CALL_RECORD_FOLDER && !TextUtils.isEmpty(mPhoneNumber));
  }

  /**
   * 获取笔记类型
   *
   * @param cursor 数据库游标
   * @return 笔记类型
   */
  public static int getNoteType(Cursor cursor) {
    return cursor.getInt(TYPE_COLUMN);
  }
}