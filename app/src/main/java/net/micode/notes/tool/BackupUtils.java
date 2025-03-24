package net.micode.notes.tool;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/*
 * 数据分类导出：优先处理通话记录文件夹，然后是普通文件夹，最后导出根目录笔记
 * 格式规范化：使用R.array.format_for_exported_note定义文本格式模板
 * 异常处理：捕获SecurityException/IOException等系统级异常
 * 文件管理：自动创建带时间戳的文件（格式：/sdcard/micronotes/note_yyyyMMdd.txt）
 */
public class BackupUtils {
  private static final String TAG = "BackupUtils";
  // 单例相关代码
  private static BackupUtils sInstance;

  public static synchronized BackupUtils getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new BackupUtils(context);
    }
    return sInstance;
  }

  /**
   * 备份操作状态码说明：
   * 0: SD卡未挂载
   * 1: 备份文件不存在
   * 2: 数据格式异常（可能被其他程序修改）
   * 3: 系统级错误（备份/恢复过程中出现运行时异常）
   * 4: 操作成功完成
   */
  public static final int STATE_SD_CARD_UNMOUONTED = 0;
  // 备份文件不存在
  public static final int STATE_BACKUP_FILE_NOT_EXIST = 1;
  // 数据格式不正确，可能会被其他程序更改
  public static final int STATE_DATA_DESTROIED = 2;
  // 导致恢复或备份失败的某些运行时异常
  public static final int STATE_SYSTEM_ERROR = 3;
  // 备份或恢复成功
  public static final int STATE_SUCCESS = 4;

  private TextExport mTextExport;

  private BackupUtils(Context context) {
    mTextExport = new TextExport(context);
  }

  private static boolean externalStorageAvailable() {
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }

  /**
   * 执行文本导出主入口
   *
   * @return 操作状态码（参见类常量STATE_*）
   */
  public int exportToText() {
    return mTextExport.exportToText();
  }

  public String getExportedTextFileName() {
    return mTextExport.mFileName;
  }

  public String getExportedTextFileDir() {
    return mTextExport.mFileDirectory;
  }

  private static class TextExport {
    private static final String[] NOTE_PROJECTION = {
      NoteColumns.ID,
      NoteColumns.MODIFIED_DATE,
      NoteColumns.SNIPPET,
      NoteColumns.TYPE
    };

    /**
     * 文本格式索引说明：
     * 0: 文件夹名称格式
     * 1: 笔记日期格式
     * 2: 笔记内容格式
     */
    private static final int NOTE_COLUMN_ID = 0;

    private static final int NOTE_COLUMN_MODIFIED_DATE = 1;

    private static final int NOTE_COLUMN_SNIPPET = 2;

    private static final String[] DATA_PROJECTION = {
      DataColumns.CONTENT,
      DataColumns.MIME_TYPE,
      DataColumns.DATA1,
      DataColumns.DATA2,
      DataColumns.DATA3,
      DataColumns.DATA4,
    };

    private static final int DATA_COLUMN_CONTENT = 0;

    private static final int DATA_COLUMN_MIME_TYPE = 1;

    private static final int DATA_COLUMN_CALL_DATE = 2;

    private static final int DATA_COLUMN_PHONE_NUMBER = 4;

    private final String[] TEXT_FORMAT;
    private static final int FORMAT_FOLDER_NAME = 0;
    private static final int FORMAT_NOTE_DATE = 1;
    private static final int FORMAT_NOTE_CONTENT = 2;

    private Context mContext;
    private String mFileName;
    private String mFileDirectory;

    /**
     * 导出核心流程：
     * 1. 检查外部存储状态
     * 2. 创建带时间戳的文本文件
     * 3. 分步导出数据：
     * - 优先导出特殊文件夹（通话记录）
     * - 导出普通文件夹
     * - 最后导出根目录笔记
     * 4. 关闭资源返回状态
     */
    public TextExport(Context context) {
      TEXT_FORMAT = context.getResources().getStringArray(R.array.format_for_exported_note);
      mContext = context;
      mFileName = "";
      mFileDirectory = "";
    }

    private String getFormat(int id) {
      return TEXT_FORMAT[id];
    }

    /**
     * 将文件夹 id 标识的文件夹导出为文本
     */
    private void exportFolderToText(String folderId, PrintStream ps) {
      // 查询笔记属于此文件夹
      Cursor notesCursor = mContext.getContentResolver().query(Notes.CONTENT_NOTE_URI,
        NOTE_PROJECTION, NoteColumns.PARENT_ID + "=?", new String[]{
          folderId
        }, null);

      if (notesCursor != null) {
        if (notesCursor.moveToFirst()) {
          do {
            // 打印笔记的最后修改日期
            ps.println(String.format(getFormat(FORMAT_NOTE_DATE), DateFormat.format(
              mContext.getString(R.string.format_datetime_mdhm),
              notesCursor.getLong(NOTE_COLUMN_MODIFIED_DATE))));
            // 查询属于此笔记的数据
            String noteId = notesCursor.getString(NOTE_COLUMN_ID);
            exportNoteToText(noteId, ps);
          } while (notesCursor.moveToNext());
        }
        notesCursor.close();
      }
    }

    /**
     * 导出单条笔记详细内容
     *
     * @param noteId 笔记ID
     * @param ps     输出流（需保证非空）
     *               <p>
     *               处理逻辑：
     *               1. 根据MIME类型区分处理：
     *               - 通话记录：输出电话号码、通话时间、位置信息
     *               - 普通笔记：直接输出文本内容
     *               2. 笔记间插入分隔符
     */
    private void exportNoteToText(String noteId, PrintStream ps) {
      Cursor dataCursor = mContext.getContentResolver().query(Notes.CONTENT_DATA_URI,
        DATA_PROJECTION, DataColumns.NOTE_ID + "=?", new String[]{
          noteId
        }, null);

      if (dataCursor != null) {
        if (dataCursor.moveToFirst()) {
          do {
            String mimeType = dataCursor.getString(DATA_COLUMN_MIME_TYPE);
            if (DataConstants.CALL_NOTE.equals(mimeType)) {
              // 打印电话号码
              String phoneNumber = dataCursor.getString(DATA_COLUMN_PHONE_NUMBER);
              long callDate = dataCursor.getLong(DATA_COLUMN_CALL_DATE);
              String location = dataCursor.getString(DATA_COLUMN_CONTENT);

              if (!TextUtils.isEmpty(phoneNumber)) {
                ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT),
                  phoneNumber));
              }
              // 打印通话时间
              ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT), DateFormat
                .format(mContext.getString(R.string.format_datetime_mdhm),
                  callDate)));
              // 打印位置信息
              if (!TextUtils.isEmpty(location)) {
                ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT),
                  location));
              }
            } else if (DataConstants.NOTE.equals(mimeType)) {
              String content = dataCursor.getString(DATA_COLUMN_CONTENT);
              if (!TextUtils.isEmpty(content)) {
                ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT),
                  content));
              }
            }
          } while (dataCursor.moveToNext());
        }
        dataCursor.close();
      }
      // 打印分隔符
      try {
        ps.write(new byte[]{
          Character.LINE_SEPARATOR, Character.LETTER_NUMBER
        });
      } catch (IOException e) {
        Log.e(TAG, e.toString());
      }
    }

    /**
     * 注释将导出为用户可读的文本
     */
    public int exportToText() {
      if (!externalStorageAvailable()) {
        Log.d(TAG, "Media was not mounted");
        return STATE_SD_CARD_UNMOUONTED;
      }

      PrintStream ps = getExportToTextPrintStream();
      if (ps == null) {
        Log.e(TAG, "get print stream error");
        return STATE_SYSTEM_ERROR;
      }
      // 第一个导出文件夹及其注释
      Cursor folderCursor = mContext.getContentResolver().query(
        Notes.CONTENT_NOTE_URI,
        NOTE_PROJECTION,
        "(" + NoteColumns.TYPE + "=" + Notes.TYPE_FOLDER + " AND "
          + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER + ") OR "
          + NoteColumns.ID + "=" + Notes.ID_CALL_RECORD_FOLDER, null, null);

      if (folderCursor != null) {
        if (folderCursor.moveToFirst()) {
          do {
            // 打印文件夹名称
            String folderName = "";
            if (folderCursor.getLong(NOTE_COLUMN_ID) == Notes.ID_CALL_RECORD_FOLDER) {
              folderName = mContext.getString(R.string.call_record_folder_name);
            } else {
              folderName = folderCursor.getString(NOTE_COLUMN_SNIPPET);
            }
            if (!TextUtils.isEmpty(folderName)) {
              ps.println(String.format(getFormat(FORMAT_FOLDER_NAME), folderName));
            }
            String folderId = folderCursor.getString(NOTE_COLUMN_ID);
            exportFolderToText(folderId, ps);
          } while (folderCursor.moveToNext());
        }
        folderCursor.close();
      }

      // 导出根文件夹中的笔记
      Cursor noteCursor = mContext.getContentResolver().query(
        Notes.CONTENT_NOTE_URI,
        NOTE_PROJECTION,
        NoteColumns.TYPE + "=" + +Notes.TYPE_NOTE + " AND " + NoteColumns.PARENT_ID
          + "=0", null, null);

      if (noteCursor != null) {
        if (noteCursor.moveToFirst()) {
          do {
            ps.println(String.format(getFormat(FORMAT_NOTE_DATE), DateFormat.format(
              mContext.getString(R.string.format_datetime_mdhm),
              noteCursor.getLong(NOTE_COLUMN_MODIFIED_DATE))));
            // Query data belong to this note
            String noteId = noteCursor.getString(NOTE_COLUMN_ID);
            exportNoteToText(noteId, ps);
          } while (noteCursor.moveToNext());
        }
        noteCursor.close();
      }
      ps.close();

      return STATE_SUCCESS;
    }

    /**
     * 获取指向文件 {@generateExportedTextFile} 的打印流
     */
    private PrintStream getExportToTextPrintStream() {
      File file = generateFileMountedOnSDcard(mContext, R.string.file_path,
        R.string.file_name_txt_format);
      if (file == null) {
        Log.e(TAG, "create file to exported failed");
        return null;
      }
      mFileName = file.getName();
      mFileDirectory = mContext.getString(R.string.file_path);
      PrintStream ps = null;
      try {
        FileOutputStream fos = new FileOutputStream(file);
        ps = new PrintStream(fos);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return null;
      } catch (NullPointerException e) {
        e.printStackTrace();
        return null;
      }
      return ps;
    }
  }

  /**
   * 生成用于存储导入数据的文本文件
   *
   * @param context             上下文对象，用于获取资源字符串
   * @param filePathResId       文件路径的资源ID
   * @param fileNameFormatResId 文件名格式的资源ID
   * @return 如果文件创建成功，返回文件对象；否则返回null
   */
  private static File generateFileMountedOnSDcard(Context context, int filePathResId, int fileNameFormatResId) {
    // 创建一个StringBuilder对象，用于构建文件路径
    StringBuilder sb = new StringBuilder();
    // 追加外部存储目录的路径
    sb.append(Environment.getExternalStorageDirectory());
    // 追加文件路径的资源字符串
    sb.append(context.getString(filePathResId));
    // 根据构建的路径创建文件目录对象
    File filedir = new File(sb.toString());
    // 追加文件名，文件名包含当前日期
    sb.append(context.getString(
      fileNameFormatResId,
      DateFormat.format(context.getString(R.string.format_date_ymd),
        System.currentTimeMillis())));
    // 根据完整路径创建文件对象
    File file = new File(sb.toString());

    try {
      // 检查文件目录是否存在，如果不存在则创建
      if (!filedir.exists()) {
        filedir.mkdir();
      }
      // 检查文件是否存在，如果不存在则创建新文件
      if (!file.exists()) {
        file.createNewFile();
      }
      // 文件创建成功，返回文件对象
      return file;
    } catch (SecurityException e) {
      // 处理安全异常，打印异常堆栈信息
      e.printStackTrace();
    } catch (IOException e) {
      // 处理IO异常，打印异常堆栈信息
      e.printStackTrace();
    }

    // 文件创建失败，返回null
    return null;
  }
}