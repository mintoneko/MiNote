package net.micode.notes.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import net.micode.notes.R;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.NotesDatabaseHelper.TABLE;

/**
 * NotesProvider 是整个笔记应用的内容提供者，负责处理笔记和数据的增删改查操作，
 * 同时支持搜索建议功能。
 */
public class NotesProvider extends ContentProvider {
  // UriMatcher 对象用于匹配传入 URI 与预定义的 URI 模式
  private static final UriMatcher mMatcher;

  // 数据库助手，用于访问 SQLite 数据库
  private NotesDatabaseHelper mHelper;

  // 日志标签
  private static final String TAG = "NotesProvider";

  // 定义 URI 模式对应的常量标识
  private static final int URI_NOTE = 1;
  private static final int URI_NOTE_ITEM = 2;
  private static final int URI_DATA = 3;
  private static final int URI_DATA_ITEM = 4;
  private static final int URI_SEARCH = 5;
  private static final int URI_SEARCH_SUGGEST = 6;

  // 初始化 URI 匹配规则
  static {
    mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // 对应：content://micode_notes/note
    mMatcher.addURI(Notes.AUTHORITY, "note", URI_NOTE);
    // 对应：content://micode_notes/note/# （# 表示数字 ID）
    mMatcher.addURI(Notes.AUTHORITY, "note/#", URI_NOTE_ITEM);
    // 对应：content://micode_notes/data
    mMatcher.addURI(Notes.AUTHORITY, "data", URI_DATA);
    // 对应：content://micode_notes/data/# （数据项的 ID）
    mMatcher.addURI(Notes.AUTHORITY, "data/#", URI_DATA_ITEM);
    // 搜索 URI
    mMatcher.addURI(Notes.AUTHORITY, "search", URI_SEARCH);
    // 搜索建议 URI，系统搜索框会调用
    mMatcher.addURI(Notes.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, URI_SEARCH_SUGGEST);
    mMatcher.addURI(Notes.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", URI_SEARCH_SUGGEST);
  }

  /**
   * NOTES_SEARCH_PROJECTION 定义了搜索结果返回的列和格式：
   * - 将笔记 ID 同时作为建议的额外数据传递；
   * - 使用 TRIM 和 REPLACE 去除换行符，确保在搜索建议中显示的文本更整洁；
   * - 指定建议图标、意图动作和 MIME 类型。
   */
  private static final String NOTES_SEARCH_PROJECTION = NoteColumns.ID + ","
    + NoteColumns.ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA + ","
    + "TRIM(REPLACE(" + NoteColumns.SNIPPET + ", x'0A','')) AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ","
    + "TRIM(REPLACE(" + NoteColumns.SNIPPET + ", x'0A','')) AS " + SearchManager.SUGGEST_COLUMN_TEXT_2 + ","
    + R.drawable.search_result + " AS " + SearchManager.SUGGEST_COLUMN_ICON_1 + ","
    + "'" + Intent.ACTION_VIEW + "' AS " + SearchManager.SUGGEST_COLUMN_INTENT_ACTION + ","
    + "'" + Notes.TextNote.CONTENT_TYPE + "' AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA;

  /**
   * NOTES_SNIPPET_SEARCH_QUERY 为笔记摘要搜索构造 SQL 查询语句：
   * - 通过 LIKE 子句进行模糊匹配；
   * - 排除回收站中的笔记；
   * - 仅匹配笔记类型为普通笔记的记录。
   */
  private static String NOTES_SNIPPET_SEARCH_QUERY = "SELECT " + NOTES_SEARCH_PROJECTION
    + " FROM " + TABLE.NOTE
    + " WHERE " + NoteColumns.SNIPPET + " LIKE ?"
    + " AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER
    + " AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE;

  /**
   * onCreate 方法在内容提供者创建时调用，初始化数据库助手。
   */
  @Override
  public boolean onCreate() {
    mHelper = NotesDatabaseHelper.getInstance(getContext());
    return true;
  }

  /**
   * query 方法处理查询请求，根据传入的 URI 来选择不同的查询策略。
   *
   * @param uri           查询的 URI
   * @param projection    返回的列
   * @param selection     查询条件
   * @param selectionArgs 查询条件参数
   * @param sortOrder     排序方式
   * @return 查询结果的 Cursor
   */
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {
    Cursor c = null;
    SQLiteDatabase db = mHelper.getReadableDatabase();
    String id = null;
    // 根据 URI 类型执行不同的查询逻辑
    switch (mMatcher.match(uri)) {
      case URI_NOTE:
        // 查询所有笔记或文件夹
        c = db.query(TABLE.NOTE, projection, selection, selectionArgs, null, null,
          sortOrder);
        break;
      case URI_NOTE_ITEM:
        // 查询单个笔记：从 URI 中解析出 ID，然后添加条件
        id = uri.getPathSegments().get(1);
        c = db.query(TABLE.NOTE, projection, NoteColumns.ID + "=" + id
          + parseSelection(selection), selectionArgs, null, null, sortOrder);
        break;
      case URI_DATA:
        // 查询所有数据记录
        c = db.query(TABLE.DATA, projection, selection, selectionArgs, null, null,
          sortOrder);
        break;
      case URI_DATA_ITEM:
        // 查询单个数据项：从 URI 中解析出 ID
        id = uri.getPathSegments().get(1);
        c = db.query(TABLE.DATA, projection, DataColumns.ID + "=" + id
          + parseSelection(selection), selectionArgs, null, null, sortOrder);
        break;
      case URI_SEARCH:
      case URI_SEARCH_SUGGEST:
        // 搜索查询时不允许指定 sortOrder、projection 或 selection
        if (sortOrder != null || projection != null) {
          throw new IllegalArgumentException(
            "do not specify sortOrder, selection, selectionArgs, or projection" + "with this query");
        }

        String searchString = null;
        // 根据 URI 类型获取搜索字符串
        if (mMatcher.match(uri) == URI_SEARCH_SUGGEST) {
          if (uri.getPathSegments().size() > 1) {
            searchString = uri.getPathSegments().get(1);
          }
        } else {
          searchString = uri.getQueryParameter("pattern");
        }

        if (TextUtils.isEmpty(searchString)) {
          return null;
        }

        try {
          // 使用通配符构造 LIKE 查询字符串
          searchString = String.format("%%%s%%", searchString);
          c = db.rawQuery(NOTES_SNIPPET_SEARCH_QUERY,
            new String[]{searchString});
        } catch (IllegalStateException ex) {
          Log.e(TAG, "got exception: " + ex.toString());
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    // 设置通知 URI，当数据发生变化时通知对应的观察者
    if (c != null) {
      c.setNotificationUri(getContext().getContentResolver(), uri);
    }
    return c;
  }

  /**
   * insert 方法用于插入新的记录（笔记或数据）。
   *
   * @param uri    目标 URI
   * @param values 要插入的内容
   * @return 插入记录对应的 URI
   */
  @Override
  public Uri insert(Uri uri, ContentValues values) {
    SQLiteDatabase db = mHelper.getWritableDatabase();
    long dataId = 0, noteId = 0, insertedId = 0;
    switch (mMatcher.match(uri)) {
      case URI_NOTE:
        // 插入一条新的笔记记录
        insertedId = noteId = db.insert(TABLE.NOTE, null, values);
        break;
      case URI_DATA:
        // 插入一条新的数据记录，检查是否包含关联的笔记 ID
        if (values.containsKey(DataColumns.NOTE_ID)) {
          noteId = values.getAsLong(DataColumns.NOTE_ID);
        } else {
          Log.d(TAG, "Wrong data format without note id:" + values.toString());
        }
        insertedId = dataId = db.insert(TABLE.DATA, null, values);
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    // 插入成功后，通知对应的内容 URI 发生了变化
    if (noteId > 0) {
      getContext().getContentResolver().notifyChange(
        ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), null);
    }
    if (dataId > 0) {
      getContext().getContentResolver().notifyChange(
        ContentUris.withAppendedId(Notes.CONTENT_DATA_URI, dataId), null);
    }
    // 返回新插入记录对应的 URI
    return ContentUris.withAppendedId(uri, insertedId);
  }

  /**
   * delete 方法用于删除记录（笔记或数据）。
   *
   * @param uri           目标 URI
   * @param selection     删除条件
   * @param selectionArgs 删除条件参数
   * @return 删除的记录数
   */
  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int count = 0;
    String id = null;
    SQLiteDatabase db = mHelper.getWritableDatabase();
    boolean deleteData = false; // 标记是否删除的是数据表中的记录
    switch (mMatcher.match(uri)) {
      case URI_NOTE:
        // 删除多条笔记记录，添加额外条件保证 ID 大于 0（非系统文件夹）
        selection = "(" + selection + ") AND " + NoteColumns.ID + ">0 ";
        count = db.delete(TABLE.NOTE, selection, selectionArgs);
        break;
      case URI_NOTE_ITEM:
        // 删除单个笔记记录
        id = uri.getPathSegments().get(1);
        /**
         * ID 小于等于 0 的记录为系统文件夹，不允许删除或放入回收站
         */
        long noteId = Long.valueOf(id);
        if (noteId <= 0) {
          break;
        }
        count = db.delete(TABLE.NOTE,
          NoteColumns.ID + "=" + id + parseSelection(selection), selectionArgs);
        break;
      case URI_DATA:
        // 删除多条数据记录
        count = db.delete(TABLE.DATA, selection, selectionArgs);
        deleteData = true;
        break;
      case URI_DATA_ITEM:
        // 删除单个数据记录
        id = uri.getPathSegments().get(1);
        count = db.delete(TABLE.DATA,
          DataColumns.ID + "=" + id + parseSelection(selection), selectionArgs);
        deleteData = true;
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    // 如果删除成功，通知相关 URI 数据发生变化
    if (count > 0) {
      if (deleteData) {
        getContext().getContentResolver().notifyChange(Notes.CONTENT_NOTE_URI, null);
      }
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return count;
  }

  /**
   * update 方法用于更新记录（笔记或数据）。
   *
   * @param uri           目标 URI
   * @param values        要更新的内容
   * @param selection     更新条件
   * @param selectionArgs 更新条件参数
   * @return 更新的记录数
   */
  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int count = 0;
    String id = null;
    SQLiteDatabase db = mHelper.getWritableDatabase();
    boolean updateData = false; // 标记是否更新的是数据表中的记录
    switch (mMatcher.match(uri)) {
      case URI_NOTE:
        // 更新多条笔记记录之前，增加笔记版本号
        increaseNoteVersion(-1, selection, selectionArgs);
        count = db.update(TABLE.NOTE, values, selection, selectionArgs);
        break;
      case URI_NOTE_ITEM:
        // 更新单个笔记记录，先根据 URI 中的 ID 增加该笔记版本号
        id = uri.getPathSegments().get(1);
        increaseNoteVersion(Long.valueOf(id), selection, selectionArgs);
        count = db.update(TABLE.NOTE, values, NoteColumns.ID + "=" + id
          + parseSelection(selection), selectionArgs);
        break;
      case URI_DATA:
        // 更新多条数据记录
        count = db.update(TABLE.DATA, values, selection, selectionArgs);
        updateData = true;
        break;
      case URI_DATA_ITEM:
        // 更新单个数据记录
        id = uri.getPathSegments().get(1);
        count = db.update(TABLE.DATA, values, DataColumns.ID + "=" + id
          + parseSelection(selection), selectionArgs);
        updateData = true;
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    // 更新成功后通知相关 URI 数据发生变化
    if (count > 0) {
      if (updateData) {
        getContext().getContentResolver().notifyChange(Notes.CONTENT_NOTE_URI, null);
      }
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return count;
  }

  /**
   * 辅助方法，用于解析传入的 selection 参数，拼接成完整的查询条件
   *
   * @param selection 原始查询条件
   * @return 返回处理后的查询条件字符串
   */
  private String parseSelection(String selection) {
    return (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
  }

  /**
   * increaseNoteVersion 方法用于在更新笔记前自动增加笔记的版本号，
   * 以便于跟踪笔记的修改历史或同步状态。
   *
   * @param id            笔记 ID，如果为 -1 则针对所有满足条件的记录更新版本号
   * @param selection     附加的更新条件
   * @param selectionArgs 更新条件参数
   */
  private void increaseNoteVersion(long id, String selection, String[] selectionArgs) {
    StringBuilder sql = new StringBuilder(120);
    sql.append("UPDATE ");
    sql.append(TABLE.NOTE);
    sql.append(" SET ");
    // 将版本号增加 1
    sql.append(NoteColumns.VERSION);
    sql.append("=" + NoteColumns.VERSION + "+1 ");

    // 如果指定了具体 ID 或者有额外的查询条件，则添加 WHERE 子句
    if (id > 0 || !TextUtils.isEmpty(selection)) {
      sql.append(" WHERE ");
    }
    if (id > 0) {
      sql.append(NoteColumns.ID + "=" + String.valueOf(id));
    }
    // 如果有额外条件，则将条件中的占位符替换为具体的参数
    if (!TextUtils.isEmpty(selection)) {
      String selectString = id > 0 ? parseSelection(selection) : selection;
      for (String args : selectionArgs) {
        selectString = selectString.replaceFirst("\\?", args);
      }
      sql.append(selectString);
    }

    // 执行构造好的 SQL 更新语句
    mHelper.getWritableDatabase().execSQL(sql.toString());
  }

  /**
   * getType 方法用于返回给定 URI 的 MIME 类型。
   * 此处未做具体实现，根据需要可返回相应的 MIME 类型字符串。
   *
   * @param uri 目标 URI
   * @return 返回 null（待实现）
   */
  @Override
  public String getType(Uri uri) {
    // TODO Auto-generated method stub
    return null;
  }
}
