package net.micode.notes.tool;

/**
 * Google Tasks API 字符串常量工具类
 * <p>
 * 功能概述：
 * 1. 定义与Google Tasks API交互时使用的JSON字段名称
 * 2. 维护任务数据结构中的固定值常量
 * 3. 提供MIUI笔记特有的文件夹命名规范
 * 4. 包含元数据存储的标识定义
 * <p>
 * 设计说明：
 * - 所有字段均为public final static常量，确保全局唯一性
 * - 常量命名遵循GTASK_JSON_前缀表示API字段，MIUI_前缀表示平台特定值
 * - 字段分组注释清晰区分不同业务场景的常量集合
 */
public class GTaskStringUtils {
  // JSON字段名称定义：任务操作相关
  public final static String GTASK_JSON_ACTION_ID = "action_id";               // 唯一标识操作序列
  public final static String GTASK_JSON_ACTION_LIST = "action_list";            // 操作请求列表
  public final static String GTASK_JSON_ACTION_TYPE = "action_type";            // 操作类型标识
  public final static String GTASK_JSON_ACTION_TYPE_CREATE = "create";          // 创建新条目操作
  public final static String GTASK_JSON_ACTION_TYPE_GETALL = "get_all";         // 获取全部数据操作
  public final static String GTASK_JSON_ACTION_TYPE_MOVE = "move";              // 移动条目操作
  public final static String GTASK_JSON_ACTION_TYPE_UPDATE = "update";          // 更新条目操作

  // JSON字段名称定义：实体结构相关
  public final static String GTASK_JSON_CREATOR_ID = "creator_id";              // 创建者标识
  public final static String GTASK_JSON_CHILD_ENTITY = "child_entity";          // 子实体关联字段
  public final static String GTASK_JSON_CLIENT_VERSION = "client_version";      // 客户端版本标识
  public final static String GTASK_JSON_COMPLETED = "completed";                // 任务完成状态
  public final static String GTASK_JSON_CURRENT_LIST_ID = "current_list_id";    // 当前所在列表ID
  public final static String GTASK_JSON_DEFAULT_LIST_ID = "default_list_id";   // 默认列表ID

  // JSON字段名称定义：数据操作参数
  public final static String GTASK_JSON_DELETED = "deleted";                    // 软删除标记
  public final static String GTASK_JSON_DEST_LIST = "dest_list";                // 目标列表标识
  public final static String GTASK_JSON_DEST_PARENT = "dest_parent";            // 目标父节点
  public final static String GTASK_JSON_DEST_PARENT_TYPE = "dest_parent_type";  // 目标父节点类型
  public final static String GTASK_JSON_ENTITY_DELTA = "entity_delta";          // 实体增量数据
  public final static String GTASK_JSON_ENTITY_TYPE = "entity_type";            // 实体类型定义

  // JSON字段名称定义：同步与索引
  public final static String GTASK_JSON_GET_DELETED = "get_deleted";            // 获取已删除条目标记
  public final static String GTASK_JSON_ID = "id";                             // 全局唯一标识符
  public final static String GTASK_JSON_INDEX = "index";                       // 排序索引值
  public final static String GTASK_JSON_LAST_MODIFIED = "last_modified";       // 最后修改时间戳
  public final static String GTASK_JSON_LATEST_SYNC_POINT = "latest_sync_point";// 最新同步点位

  // JSON字段名称定义：列表与任务结构
  public final static String GTASK_JSON_LIST_ID = "list_id";                   // 列表唯一标识
  public final static String GTASK_JSON_LISTS = "lists";                       // 列表集合字段
  public final static String GTASK_JSON_NAME = "name";                         // 条目名称字段
  public final static String GTASK_JSON_NEW_ID = "new_id";                     // 新分配ID
  public final static String GTASK_JSON_NOTES = "notes";                       // 备注信息字段

  // JSON字段名称定义：层级关系
  public final static String GTASK_JSON_PARENT_ID = "parent_id";               // 父节点ID
  public final static String GTASK_JSON_PRIOR_SIBLING_ID = "prior_sibling_id"; // 前序兄弟节点ID
  public final static String GTASK_JSON_RESULTS = "results";                   // 操作结果集合
  public final static String GTASK_JSON_SOURCE_LIST = "source_list";           // 源列表标识

  // JSON字段名称定义：类型分类
  public final static String GTASK_JSON_TASKS = "tasks";                       // 任务集合字段
  public final static String GTASK_JSON_TYPE = "type";                         // 类型标识字段
  public final static String GTASK_JSON_TYPE_GROUP = "GROUP";                  // 分组类型标识
  public final static String GTASK_JSON_TYPE_TASK = "TASK";                    // 任务类型标识
  public final static String GTASK_JSON_USER = "user";                         // 用户关联字段

  // 文件夹结构定义
  public final static String MIUI_FOLDER_PREFFIX = "[MIUI_Notes]";            // MIUI笔记专用前缀
  public final static String FOLDER_DEFAULT = "Default";                      // 默认文件夹名称
  public final static String FOLDER_CALL_NOTE = "Call_Note";                  // 通话记录文件夹
  public final static String FOLDER_META = "METADATA";                        // 元数据文件夹

  // 元数据标识定义
  public final static String META_HEAD_GTASK_ID = "meta_gid";                 // 元数据全局ID
  public final static String META_HEAD_NOTE = "meta_note";                    // 元数据备注字段
  public final static String META_HEAD_DATA = "meta_data";                    // 元数据存储字段
  public final static String META_NOTE_NAME = "[META INFO] DON'T UPDATE AND DELETE"; // 元数据保护声明
}