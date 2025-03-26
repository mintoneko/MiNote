package net.micode.notes.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;

import java.util.ArrayList;


/**
 * 笔记实体类，负责管理笔记的本地修改和数据库同步
 */
public class Note {
  // 存储笔记的差异数据，用于记录更改的字段
  private ContentValues mNoteDiffValues;
  // 封装了文本数据和通话数据的内部类对象
  private NoteData mNoteData;
  private static final String TAG = "Note";

  /**
   * Create a new note id for adding a new note to databases
   * 创建一个新的笔记，并返回笔记的ID
   *
   * @param context  上下文对象
   * @param folderId 笔记的父文件夹ID
   * @return 新创建笔记的ID
   */
  public static synchronized long getNewNoteId(Context context, long folderId) {
    // Create a new note in the database
    ContentValues values = new ContentValues();
    long createdTime = System.currentTimeMillis();
    values.put(NoteColumns.CREATED_DATE, createdTime);
    values.put(NoteColumns.MODIFIED_DATE, createdTime);
    values.put(NoteColumns.TYPE, Notes.TYPE_NOTE);
    values.put(NoteColumns.LOCAL_MODIFIED, 1);
    values.put(NoteColumns.PARENT_ID, folderId);
    // 插入新笔记到数据库中
    Uri uri = context.getContentResolver().insert(Notes.CONTENT_NOTE_URI, values);

    long noteId = 0;
    try {
      // 从插入的URI中解析出笔记ID
      noteId = Long.valueOf(uri.getPathSegments().get(1));
    } catch (NumberFormatException e) {
      Log.e(TAG, "Get note id error :" + e.toString());
      noteId = 0;
    }
    if (noteId == -1) {
      throw new IllegalStateException("Wrong note id:" + noteId);
    }
    return noteId;
  }

  /**
   * 构造方法，初始化成员变量
   */
  public Note() {
    mNoteDiffValues = new ContentValues();
    mNoteData = new NoteData();
  }

  /**
   * 设置笔记的属性值，同时记录本地修改状态
   *
   * @param key   属性键
   * @param value 属性值
   */
  public void setNoteValue(String key, String value) {
    mNoteDiffValues.put(key, value);
    mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
    mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
  }

  /**
   * 设置文本数据
   *
   * @param key   文本数据键
   * @param value 文本数据值
   */
  public void setTextData(String key, String value) {
    mNoteData.setTextData(key, value);
  }

  /**
   * 设置文本数据的ID
   *
   * @param id 文本数据ID
   */
  public void setTextDataId(long id) {
    mNoteData.setTextDataId(id);
  }

  /**
   * 获取文本数据的ID
   *
   * @return 文本数据ID
   */
  public long getTextDataId() {
    return mNoteData.mTextDataId;
  }

  /**
   * 设置通话数据的ID
   *
   * @param id 通话数据ID
   */
  public void setCallDataId(long id) {
    mNoteData.setCallDataId(id);
  }

  /**
   * 设置通话数据
   *
   * @param key   通话数据键
   * @param value 通话数据值
   */
  public void setCallData(String key, String value) {
    mNoteData.setCallData(key, value);
  }

  /**
   * 判断笔记是否被本地修改
   *
   * @return 如果本地有修改，返回true，否则返回false
   */
  public boolean isLocalModified() {
    return mNoteDiffValues.size() > 0 || mNoteData.isLocalModified();
  }

  /**
   * 将笔记数据同步到数据库
   *
   * @param context 上下文对象
   * @param noteId  笔记ID
   * @return 同步成功返回true，失败返回false
   */
  public boolean syncNote(Context context, long noteId) {
    if (noteId <= 0) {
      throw new IllegalArgumentException("Wrong note id:" + noteId);
    }

    if (!isLocalModified()) {
      return true; // 如果没有本地修改，则不需要同步
    }

    /**
     * In theory, once data changed, the note should be updated on {@link NoteColumns#LOCAL_MODIFIED} and
     * {@link NoteColumns#MODIFIED_DATE}. For data safety, though update note fails, we also update the
     * note data info
     */
    // 更新笔记的基本信息
    if (context.getContentResolver().update(
      ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), mNoteDiffValues, null,
      null) == 0) {
      Log.e(TAG, "Update note error, should not happen");
      // Do not return, fall through
    }
    mNoteDiffValues.clear();
    // 更新文本和通话数据
    if (mNoteData.isLocalModified()
      && (mNoteData.pushIntoContentResolver(context, noteId) == null)) {
      return false; // 数据同步失败
    }

    return true;
  }

  /**
   * 内部类，管理笔记的具体数据类型（文本/通话记录）
   */
  private class NoteData {
    private long mTextDataId;

    private ContentValues mTextDataValues;

    private long mCallDataId;

    private ContentValues mCallDataValues;

    private static final String TAG = "NoteData";

    public NoteData() {
      // 初始化文本和通话数据的存储对象
      mTextDataValues = new ContentValues();
      mCallDataValues = new ContentValues();
      mTextDataId = 0;
      mCallDataId = 0;
    }

    boolean isLocalModified() {
      // 判断文本或通话数据是否有本地修改
      return mTextDataValues.size() > 0 || mCallDataValues.size() > 0;
    }

    void setTextDataId(long id) {
      if (id <= 0) {
        throw new IllegalArgumentException("Text data id should larger than 0");
      }
      mTextDataId = id;
    }

    void setCallDataId(long id) {
      if (id <= 0) {
        throw new IllegalArgumentException("Call data id should larger than 0");
      }
      mCallDataId = id;
    }

    void setCallData(String key, String value) {
      // 设置通话数据，并标记为已修改
      mCallDataValues.put(key, value);
      mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
      mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
    }

    void setTextData(String key, String value) {
      // 设置文本数据，并标记为已修改
      mTextDataValues.put(key, value);
      mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
      mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
    }

    Uri pushIntoContentResolver(Context context, long noteId) {
      /**
       * Check for safety
       */
      if (noteId <= 0) {
        throw new IllegalArgumentException("Wrong note id:" + noteId);
      }

      ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
      ContentProviderOperation.Builder builder = null;
      // 插入或更新文本数据
      if (mTextDataValues.size() > 0) {
        mTextDataValues.put(DataColumns.NOTE_ID, noteId);
        if (mTextDataId == 0) {
          // 如果没有数据id，则插入新的文本数据
          mTextDataValues.put(DataColumns.MIME_TYPE, TextNote.CONTENT_ITEM_TYPE);
          Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
            mTextDataValues);
          try {
            setTextDataId(Long.valueOf(uri.getPathSegments().get(1)));
          } catch (NumberFormatException e) {
            Log.e(TAG, "Insert new text data fail with noteId" + noteId);
            mTextDataValues.clear();
            return null;
          }
        } else {
          builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
            Notes.CONTENT_DATA_URI, mTextDataId));
          builder.withValues(mTextDataValues);
          operationList.add(builder.build());
        }
        mTextDataValues.clear();
      }
      // 插入或更新通话数据（逻辑同上）
      if (mCallDataValues.size() > 0) {
        mCallDataValues.put(DataColumns.NOTE_ID, noteId);
        if (mCallDataId == 0) {
          mCallDataValues.put(DataColumns.MIME_TYPE, CallNote.CONTENT_ITEM_TYPE);
          Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
            mCallDataValues);
          try {
            setCallDataId(Long.valueOf(uri.getPathSegments().get(1)));
          } catch (NumberFormatException e) {
            Log.e(TAG, "Insert new call data fail with noteId" + noteId);
            mCallDataValues.clear();
            return null;
          }
        } else {
          builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
            Notes.CONTENT_DATA_URI, mCallDataId));
          builder.withValues(mCallDataValues);
          operationList.add(builder.build());
        }
        mCallDataValues.clear();
      }

      if (operationList.size() > 0) {
        try {
          ContentProviderResult[] results = context.getContentResolver().applyBatch(
            Notes.AUTHORITY, operationList);
          return (results == null || results.length == 0 || results[0] == null) ? null
            : ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId);
        } catch (RemoteException e) {
          Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
          return null;
        } catch (OperationApplicationException e) {
          Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
          return null;
        }
      }
      return null;
    }
  }
}
