package net.micode.notes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * NotesDatabaseHelper 继承自 SQLiteOpenHelper，用于管理数据库的创建和版本升级。
 * 该类主要负责笔记数据和相关触发器的初始化、升级和系统文件夹的创建。
 */
public class NotesDatabaseHelper extends SQLiteOpenHelper {
    // 数据库名称和版本号
    private static final String DB_NAME = "note.db";
    private static final int DB_VERSION = 4;

    // 定义数据库中的表名常量
    public interface TABLE {
        public static final String NOTE = "note";
        public static final String DATA = "data";
    }

    private static final String TAG = "NotesDatabaseHelper";

    // 单例模式，保证数据库帮助器全局只有一个实例
    private static NotesDatabaseHelper mInstance;

    // 创建 note 表的 SQL 语句，包含笔记的各个属性字段及默认值
    private static final String CREATE_NOTE_TABLE_SQL =
            "CREATE TABLE " + TABLE.NOTE + "(" +
                    NoteColumns.ID + " INTEGER PRIMARY KEY," +
                    NoteColumns.PARENT_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.ALERTED_DATE + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.BG_COLOR_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
                    NoteColumns.HAS_ATTACHMENT + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
                    NoteColumns.NOTES_COUNT + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.SNIPPET + " TEXT NOT NULL DEFAULT ''," +
                    NoteColumns.TYPE + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.WIDGET_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.WIDGET_TYPE + " INTEGER NOT NULL DEFAULT -1," +
                    NoteColumns.SYNC_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.LOCAL_MODIFIED + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.ORIGIN_PARENT_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.GTASK_ID + " TEXT NOT NULL DEFAULT ''," +
                    NoteColumns.VERSION + " INTEGER NOT NULL DEFAULT 0" +
                    ")";

    // 创建 data 表的 SQL 语句，保存笔记内容、附件等数据
    private static final String CREATE_DATA_TABLE_SQL =
            "CREATE TABLE " + TABLE.DATA + "(" +
                    DataColumns.ID + " INTEGER PRIMARY KEY," +
                    DataColumns.MIME_TYPE + " TEXT NOT NULL," +
                    DataColumns.NOTE_ID + " INTEGER NOT NULL DEFAULT 0," +
                    NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
                    NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
                    DataColumns.CONTENT + " TEXT NOT NULL DEFAULT ''," +
                    DataColumns.DATA1 + " INTEGER," +
                    DataColumns.DATA2 + " INTEGER," +
                    DataColumns.DATA3 + " TEXT NOT NULL DEFAULT ''," +
                    DataColumns.DATA4 + " TEXT NOT NULL DEFAULT ''," +
                    DataColumns.DATA5 + " TEXT NOT NULL DEFAULT ''" +
                    ")";

    // 为 data 表的 NOTE_ID 字段创建索引，提高查询性能
    private static final String CREATE_DATA_NOTE_ID_INDEX_SQL =
            "CREATE INDEX IF NOT EXISTS note_id_index ON " +
                    TABLE.DATA + "(" + DataColumns.NOTE_ID + ");";

    /**
     * 以下定义的触发器主要用于维护笔记文件夹的笔记数量、更新笔记摘要以及数据与笔记的级联删除。
     */

    // 当笔记移动到新的文件夹时，增加新文件夹的笔记数量
    private static final String NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
            "CREATE TRIGGER increase_folder_count_on_update "+
                    " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + TABLE.NOTE +
                    " BEGIN " +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + " + 1" +
                    "  WHERE " + NoteColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
                    " END";

    // 当笔记从文件夹中移出时，减少原文件夹的笔记数量
    private static final String NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
            "CREATE TRIGGER decrease_folder_count_on_update " +
                    " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + TABLE.NOTE +
                    " BEGIN " +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + "-1" +
                    "  WHERE " + NoteColumns.ID + "=old." + NoteColumns.PARENT_ID +
                    "  AND " + NoteColumns.NOTES_COUNT + ">0" + ";" +
                    " END";

    // 当向文件夹中插入新的笔记时，增加该文件夹的笔记数量
    private static final String NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER =
            "CREATE TRIGGER increase_folder_count_on_insert " +
                    " AFTER INSERT ON " + TABLE.NOTE +
                    " BEGIN " +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + " + 1" +
                    "  WHERE " + NoteColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
                    " END";

    // 当从文件夹中删除笔记时，减少该文件夹的笔记数量
    private static final String NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER =
            "CREATE TRIGGER decrease_folder_count_on_delete " +
                    " AFTER DELETE ON " + TABLE.NOTE +
                    " BEGIN " +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + "-1" +
                    "  WHERE " + NoteColumns.ID + "=old." + NoteColumns.PARENT_ID +
                    "  AND " + NoteColumns.NOTES_COUNT + ">0;" +
                    " END";

    // 当插入笔记数据（类型为 NOTE）时，自动更新笔记表中的摘要内容
    private static final String DATA_UPDATE_NOTE_CONTENT_ON_INSERT_TRIGGER =
            "CREATE TRIGGER update_note_content_on_insert " +
                    " AFTER INSERT ON " + TABLE.DATA +
                    " WHEN new." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
                    " BEGIN" +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.SNIPPET + "=new." + DataColumns.CONTENT +
                    "  WHERE " + NoteColumns.ID + "=new." + DataColumns.NOTE_ID + ";" +
                    " END";

    // 当更新笔记数据（类型为 NOTE）时，自动更新笔记摘要内容
    private static final String DATA_UPDATE_NOTE_CONTENT_ON_UPDATE_TRIGGER =
            "CREATE TRIGGER update_note_content_on_update " +
                    " AFTER UPDATE ON " + TABLE.DATA +
                    " WHEN old." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
                    " BEGIN" +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.SNIPPET + "=new." + DataColumns.CONTENT +
                    "  WHERE " + NoteColumns.ID + "=new." + DataColumns.NOTE_ID + ";" +
                    " END";

    // 当删除笔记数据（类型为 NOTE）时，清空对应笔记的摘要内容
    private static final String DATA_UPDATE_NOTE_CONTENT_ON_DELETE_TRIGGER =
            "CREATE TRIGGER update_note_content_on_delete " +
                    " AFTER delete ON " + TABLE.DATA +
                    " WHEN old." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
                    " BEGIN" +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.SNIPPET + "=''" +
                    "  WHERE " + NoteColumns.ID + "=old." + DataColumns.NOTE_ID + ";" +
                    " END";

    // 当笔记删除时，同时删除属于该笔记的所有数据记录
    private static final String NOTE_DELETE_DATA_ON_DELETE_TRIGGER =
            "CREATE TRIGGER delete_data_on_delete " +
                    " AFTER DELETE ON " + TABLE.NOTE +
                    " BEGIN" +
                    "  DELETE FROM " + TABLE.DATA +
                    "   WHERE " + DataColumns.NOTE_ID + "=old." + NoteColumns.ID + ";" +
                    " END";

    // 当删除文件夹时，删除该文件夹下的所有笔记
    private static final String FOLDER_DELETE_NOTES_ON_DELETE_TRIGGER =
            "CREATE TRIGGER folder_delete_notes_on_delete " +
                    " AFTER DELETE ON " + TABLE.NOTE +
                    " BEGIN" +
                    "  DELETE FROM " + TABLE.NOTE +
                    "   WHERE " + NoteColumns.PARENT_ID + "=old." + NoteColumns.ID + ";" +
                    " END";

    // 当文件夹被移动到回收站时，将该文件夹下的所有笔记移动到回收站
    private static final String FOLDER_MOVE_NOTES_ON_TRASH_TRIGGER =
            "CREATE TRIGGER folder_move_notes_on_trash " +
                    " AFTER UPDATE ON " + TABLE.NOTE +
                    " WHEN new." + NoteColumns.PARENT_ID + "=" + Notes.ID_TRASH_FOLER +
                    " BEGIN" +
                    "  UPDATE " + TABLE.NOTE +
                    "   SET " + NoteColumns.PARENT_ID + "=" + Notes.ID_TRASH_FOLER +
                    "  WHERE " + NoteColumns.PARENT_ID + "=old." + NoteColumns.ID + ";" +
                    " END";

    /**
     * 构造方法，通过 Context 初始化 SQLiteOpenHelper
     */
    public NotesDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 创建 note 表，并调用方法创建相关触发器和系统文件夹
     */
    public void createNoteTable(SQLiteDatabase db) {
        db.execSQL(CREATE_NOTE_TABLE_SQL);
        // 重新创建 note 表相关的触发器
        reCreateNoteTableTriggers(db);
        // 插入系统预设的文件夹（如根目录、回收站等）
        createSystemFolder(db);
        Log.d(TAG, "note table has been created");
    }

    /**
     * 重新创建 note 表的触发器（先删除旧触发器，再创建新的）
     */
    private void reCreateNoteTableTriggers(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS increase_folder_count_on_update");
        db.execSQL("DROP TRIGGER IF EXISTS decrease_folder_count_on_update");
        db.execSQL("DROP TRIGGER IF EXISTS decrease_folder_count_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS delete_data_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS increase_folder_count_on_insert");
        db.execSQL("DROP TRIGGER IF EXISTS folder_delete_notes_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS folder_move_notes_on_trash");

        // 重新创建所有与 note 表相关的触发器
        db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
        db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
        db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER);
        db.execSQL(NOTE_DELETE_DATA_ON_DELETE_TRIGGER);
        db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER);
        db.execSQL(FOLDER_DELETE_NOTES_ON_DELETE_TRIGGER);
        db.execSQL(FOLDER_MOVE_NOTES_ON_TRASH_TRIGGER);
    }

    /**
     * 插入系统级别的文件夹，作为笔记的特殊分类：
     * - 通话记录文件夹
     * - 根目录（默认文件夹）
     * - 临时文件夹（用于移动笔记时临时保存）
     * - 回收站文件夹
     */
    private void createSystemFolder(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        /**
         * 通话记录文件夹，专门用于保存通话记录笔记
         */
        values.put(NoteColumns.ID, Notes.ID_CALL_RECORD_FOLDER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);

        /**
         * 根目录，默认的笔记文件夹
         */
        values.clear();
        values.put(NoteColumns.ID, Notes.ID_ROOT_FOLDER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);

        /**
         * 临时文件夹，用于移动笔记时临时存放数据
         */
        values.clear();
        values.put(NoteColumns.ID, Notes.ID_TEMPARAY_FOLDER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);

        /**
         * 回收站文件夹，用于保存被删除的笔记
         */
        values.clear();
        values.put(NoteColumns.ID, Notes.ID_TRASH_FOLER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);
    }

    /**
     * 创建 data 表，并建立触发器及索引
     */
    public void createDataTable(SQLiteDatabase db) {
        db.execSQL(CREATE_DATA_TABLE_SQL);
        // 重新创建 data 表相关的触发器
        reCreateDataTableTriggers(db);
        // 为 data 表的 NOTE_ID 字段创建索引，提升查询性能
        db.execSQL(CREATE_DATA_NOTE_ID_INDEX_SQL);
        Log.d(TAG, "data table has been created");
    }

    /**
     * 重新创建 data 表的触发器（先删除旧触发器，再创建新的）
     */
    private void reCreateDataTableTriggers(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_insert");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_update");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_delete");

        db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_INSERT_TRIGGER);
        db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_UPDATE_TRIGGER);
        db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_DELETE_TRIGGER);
    }

    /**
     * 使用单例模式获取 NotesDatabaseHelper 实例
     */
    static synchronized NotesDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotesDatabaseHelper(context);
        }
        return mInstance;
    }

    /**
     * 数据库第一次创建时调用，建立 note 表和 data 表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createNoteTable(db);
        createDataTable(db);
    }

    /**
     * 数据库升级时调用，根据旧版本和新版本号进行相应的升级操作
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean reCreateTriggers = false;
        boolean skipV2 = false;

        // 如果旧版本为1，则直接升级到版本2，同时标记升级中已包含版本3的部分更新
        if (oldVersion == 1) {
            upgradeToV2(db);
            skipV2 = true; // 此次升级同时包含了从 v2 到 v3 的部分更新
            oldVersion++;
        }

        // 如果旧版本为2且没有跳过，则升级到版本3
        if (oldVersion == 2 && !skipV2) {
            upgradeToV3(db);
            reCreateTriggers = true;
            oldVersion++;
        }

        // 如果旧版本为3，则升级到版本4
        if (oldVersion == 3) {
            upgradeToV4(db);
            oldVersion++;
        }

        // 如果升级后版本号与期望的新版本号不一致，则抛出异常
        if (oldVersion != newVersion) {
            throw new IllegalStateException("Upgrade notes database to version " + newVersion
                    + "fails");
        }

        // 如果触发器需要重建，则重新创建 note 和 data 表的触发器
        if (reCreateTriggers) {
            reCreateNoteTableTriggers(db);
            reCreateDataTableTriggers(db);
        }
    }

    /**
     * 升级到版本2：
     * - 删除旧的 note 和 data 表
     * - 重新创建 note 表和 data 表及相关触发器
     */
    private void upgradeToV2(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE.NOTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE.DATA);
        createNoteTable(db);
        createDataTable(db);
    }

    /**
     * 升级到版本3：
     * - 删除不再使用的触发器
     * - 为 note 表添加 gtask id 列，用于同步或其他业务逻辑
     * - 插入回收站系统文件夹
     */
    private void upgradeToV3(SQLiteDatabase db) {
        // 删除已废弃的触发器
        db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_insert");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_update");
        // 为 note 表添加 gtask id 字段
        db.execSQL("ALTER TABLE " + TABLE.NOTE + " ADD COLUMN " + NoteColumns.GTASK_ID
                + " TEXT NOT NULL DEFAULT ''");
        // 插入回收站文件夹
        ContentValues values = new ContentValues();
        values.put(NoteColumns.ID, Notes.ID_TRASH_FOLER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);
    }

    /**
     * 升级到版本4：
     * - 为 note 表增加 version 列，记录笔记版本信息
     */
    private void upgradeToV4(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE.NOTE + " ADD COLUMN " + NoteColumns.VERSION
                + " INTEGER NOT NULL DEFAULT 0");
    }
}
