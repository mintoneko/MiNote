package net.micode.notes.data;

import android.net.Uri;

/**
 * Notes 类定义了整个笔记应用中使用的常量和数据结构，包括笔记和数据的 URI、系统文件夹的标识、Intent 传递的数据键以及数据库列名等。
 */
public class Notes {
    // 定义内容提供者的授权名，作为 URI 构造的一部分
    public static final String AUTHORITY = "micode_notes";
    // 日志标签，用于调试日志输出
    public static final String TAG = "Notes";

    // 定义不同的笔记类型：普通笔记、文件夹、系统类型（如系统预设的文件夹）
    public static final int TYPE_NOTE     = 0;
    public static final int TYPE_FOLDER   = 1;
    public static final int TYPE_SYSTEM   = 2;

    /**
     * 以下 ID 用于系统文件夹的标识：
     * ID_ROOT_FOLDER：默认的根文件夹，用于存放笔记
     * ID_TEMPARAY_FOLDER：用于存放未归类到文件夹的笔记（临时文件夹）
     * ID_CALL_RECORD_FOLDER：用于存放通话记录笔记
     * ID_TRASH_FOLER：回收站，用于存放已删除的笔记
     */
    public static final int ID_ROOT_FOLDER = 0;
    public static final int ID_TEMPARAY_FOLDER = -1;
    public static final int ID_CALL_RECORD_FOLDER = -2;
    public static final int ID_TRASH_FOLER = -3;

    // Intent 传递时使用的额外数据键，用于在不同组件之间传递相关参数
    public static final String INTENT_EXTRA_ALERT_DATE = "net.micode.notes.alert_date";
    public static final String INTENT_EXTRA_BACKGROUND_ID = "net.micode.notes.background_color_id";
    public static final String INTENT_EXTRA_WIDGET_ID = "net.micode.notes.widget_id";
    public static final String INTENT_EXTRA_WIDGET_TYPE = "net.micode.notes.widget_type";
    public static final String INTENT_EXTRA_FOLDER_ID = "net.micode.notes.folder_id";
    public static final String INTENT_EXTRA_CALL_DATE = "net.micode.notes.call_date";

    // 定义 Widget 类型常量，用于区分不同尺寸或者无效的 widget
    public static final int TYPE_WIDGET_INVALIDE      = -1;
    public static final int TYPE_WIDGET_2X            = 0;
    public static final int TYPE_WIDGET_4X            = 1;

    /**
     * DataConstants 类定义了数据的 MIME 类型常量，主要区分文本笔记和通话记录笔记。
     * 这些常量用于数据库中区分不同类型的内容。
     */
    public static class DataConstants {
        // 文本笔记的 MIME 类型，引用 TextNote 定义的常量
        public static final String NOTE = TextNote.CONTENT_ITEM_TYPE;
        // 通话记录笔记的 MIME 类型，引用 CallNote 定义的常量
        public static final String CALL_NOTE = CallNote.CONTENT_ITEM_TYPE;
    }

    /**
     * 定义查询所有笔记和文件夹的 URI，供内容提供者使用。
     */
    public static final Uri CONTENT_NOTE_URI = Uri.parse("content://" + AUTHORITY + "/note");

    /**
     * 定义查询笔记数据的 URI。
     */
    public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/data");

    /**
     * NoteColumns 接口定义了 note 表中各列的名称和说明。
     * 这些列用于存储笔记或文件夹的各种属性，如创建时间、修改时间、所属文件夹、笔记内容摘要等。
     */
    public interface NoteColumns {
        /**
         * 唯一标识符，类型为 INTEGER (long)
         */
        public static final String ID = "_id";

        /**
         * 父级 ID，指向所属的文件夹，类型为 INTEGER (long)
         */
        public static final String PARENT_ID = "parent_id";

        /**
         * 笔记或文件夹的创建日期，类型为 INTEGER (long)
         */
        public static final String CREATED_DATE = "created_date";

        /**
         * 最近修改日期，类型为 INTEGER (long)
         */
        public static final String MODIFIED_DATE = "modified_date";

        /**
         * 提醒日期，用于闹钟提醒功能，类型为 INTEGER (long)
         */
        public static final String ALERTED_DATE = "alert_date";

        /**
         * 笔记的摘要或文件夹的名称，类型为 TEXT
         */
        public static final String SNIPPET = "snippet";

        /**
         * 小部件（Widget）的标识 ID，类型为 INTEGER (long)
         */
        public static final String WIDGET_ID = "widget_id";

        /**
         * 小部件的类型，类型为 INTEGER (long)
         */
        public static final String WIDGET_TYPE = "widget_type";

        /**
         * 背景颜色的标识 ID，类型为 INTEGER (long)
         */
        public static final String BG_COLOR_ID = "bg_color_id";

        /**
         * 表示笔记是否包含附件，类型为 INTEGER
         */
        public static final String HAS_ATTACHMENT = "has_attachment";

        /**
         * 文件夹中笔记的数量，类型为 INTEGER (long)
         */
        public static final String NOTES_COUNT = "notes_count";

        /**
         * 文件类型，标识是笔记还是文件夹，类型为 INTEGER
         */
        public static final String TYPE = "type";

        /**
         * 同步时使用的最后同步标识，类型为 INTEGER (long)
         */
        public static final String SYNC_ID = "sync_id";

        /**
         * 本地修改标志，类型为 INTEGER，标识笔记是否被本地修改
         */
        public static final String LOCAL_MODIFIED = "local_modified";

        /**
         * 笔记在移动到临时文件夹前的原始父级 ID，类型为 INTEGER
         */
        public static final String ORIGIN_PARENT_ID = "origin_parent_id";

        /**
         * 同步任务的 ID，类型为 TEXT
         */
        public static final String GTASK_ID = "gtask_id";

        /**
         * 笔记的版本号，类型为 INTEGER (long)
         */
        public static final String VERSION = "version";
    }

    /**
     * DataColumns 接口定义了 data 表中各列的名称和说明。
     * 这些列用于存储笔记具体内容或附件信息，同时支持多种数据类型。
     */
    public interface DataColumns {
        /**
         * 唯一标识符，类型为 INTEGER (long)
         */
        public static final String ID = "_id";

        /**
         * 数据的 MIME 类型，类型为 TEXT
         */
        public static final String MIME_TYPE = "mime_type";

        /**
         * 所属的笔记 ID，类型为 INTEGER (long)
         */
        public static final String NOTE_ID = "note_id";

        /**
         * 数据的创建日期，类型为 INTEGER (long)
         */
        public static final String CREATED_DATE = "created_date";

        /**
         * 数据的最近修改日期，类型为 INTEGER (long)
         */
        public static final String MODIFIED_DATE = "modified_date";

        /**
         * 数据的主要内容，类型为 TEXT
         */
        public static final String CONTENT = "content";

        /**
         * 通用数据字段1，通常用于存储整数类型数据，含义依赖于 MIME 类型
         */
        public static final String DATA1 = "data1";

        /**
         * 通用数据字段2，通常用于存储整数类型数据，含义依赖于 MIME 类型
         */
        public static final String DATA2 = "data2";

        /**
         * 通用数据字段3，通常用于存储文本类型数据，含义依赖于 MIME 类型
         */
        public static final String DATA3 = "data3";

        /**
         * 通用数据字段4，通常用于存储文本类型数据，含义依赖于 MIME 类型
         */
        public static final String DATA4 = "data4";

        /**
         * 通用数据字段5，通常用于存储文本类型数据，含义依赖于 MIME 类型
         */
        public static final String DATA5 = "data5";
    }

    /**
     * TextNote 类定义了文本笔记的数据结构及相关常量。
     * 它实现了 DataColumns 接口，因此继承了 data 表中定义的列名称。
     */
    public static final class TextNote implements DataColumns {
        /**
         * 模式标识，用于指示文本是否处于清单模式
         * 使用 DATA1 列存储，数值 1 表示清单模式，0 表示普通模式
         */
        public static final String MODE = DATA1;
        public static final int MODE_CHECK_LIST = 1;

        // 定义文本笔记的多条目 MIME 类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/text_note";
        // 定义单条目文本笔记的 MIME 类型
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/text_note";

        // 定义文本笔记内容提供者的 URI
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/text_note");
    }

    /**
     * CallNote 类定义了通话记录笔记的数据结构及相关常量。
     * 它同样实现了 DataColumns 接口，扩展了特定于通话记录的字段。
     */
    public static final class CallNote implements DataColumns {
        /**
         * 通话记录的日期，存储在 DATA1 列中，类型为 INTEGER (long)
         */
        public static final String CALL_DATE = DATA1;

        /**
         * 通话记录对应的电话号码，存储在 DATA3 列中，类型为 TEXT
         */
        public static final String PHONE_NUMBER = DATA3;

        // 定义通话记录笔记的多条目 MIME 类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/call_note";
        // 定义单条目通话记录笔记的 MIME 类型
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/call_note";

        // 定义通话记录笔记内容提供者的 URI
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/call_note");
    }
}
