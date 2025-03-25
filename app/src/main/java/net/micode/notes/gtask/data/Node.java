package net.micode.notes.gtask.data;

import android.database.Cursor;

import org.json.JSONObject;
/**
 * 同步任务数据结构的抽象基类
 * 核心职责：
 * 1. 定义数据节点通用属性
 * 2. 声明数据同步操作接口
 * 3. 提供双向同步动作标识体系
 */
public abstract class Node {
    // 同步动作常量体系（关键同步逻辑标识）
    // 注意：这些值用于决策同步流程方向

    /** 无需同步操作 */
    public static final int SYNC_ACTION_NONE = 0;
    /** 需要在远程服务端创建新条目 */
    public static final int SYNC_ACTION_ADD_REMOTE = 1;
    /** 需要在本地数据库创建新条目 */
    public static final int SYNC_ACTION_ADD_LOCAL = 2;
    /** 需要删除远程服务端条目 */
    public static final int SYNC_ACTION_DEL_REMOTE = 3;
    /** 需要删除本地数据库条目 */
    public static final int SYNC_ACTION_DEL_LOCAL = 4;
    /** 需要将本地修改同步到远程 */
    public static final int SYNC_ACTION_UPDATE_REMOTE = 5;
    /** 需要将远程修改同步到本地 */
    public static final int SYNC_ACTION_UPDATE_LOCAL = 6;
    /** 检测到数据冲突需要解决 */
    public static final int SYNC_ACTION_UPDATE_CONFLICT = 7;
    /** 同步过程中出现错误 */
    public static final int SYNC_ACTION_ERROR = 8;
    // 核心数据字段
    private String mGid;// 远程服务端全局唯一标识（关键索引字段）

    private String mName;// 节点显示名称

    private long mLastModified;// 最后修改时间戳（用于冲突检测）

    private boolean mDeleted;// 软删除标记
    /**
     * 节点初始化构造
     * 初始状态说明：
     * - 未分配远程GID
     * - 名称为空字符串
     * - 时间戳为0（表示未持久化）
     * - 未标记为删除状态
     */
    public Node() {
        mGid = null;
        mName = "";
        mLastModified = 0;
        mDeleted = false;
    }
    //---------------- 抽象方法定义（子类必须实现同步逻辑）----------------

    /**
     * 生成创建动作的JSON指令（网络请求用）
     * @param actionId 动作序列ID（用于保证操作顺序）
     * @return 符合Google Task API规范的JSON对象
     */
    public abstract JSONObject getCreateAction(int actionId);
    /**
     * 生成更新动作的JSON指令（网络请求用）
     * @param actionId 动作序列ID
     * @return 包含差异字段的更新指令
     */
    public abstract JSONObject getUpdateAction(int actionId);
    /**
     * 从远程JSON数据解析节点内容（反序列化）
     * @param js 来自服务端的JSON数据包
     */
    public abstract void setContentByRemoteJSON(JSONObject js);
    /**
     * 从本地JSON数据解析节点内容（反序列化）
     * @param js 本地数据库存储的JSON结构
     */
    public abstract void setContentByLocalJSON(JSONObject js);
    /**
     * 生成本地存储的JSON结构（序列化）
     * @return 适配本地数据库的JSON格式
     */
    public abstract JSONObject getLocalJSONFromContent();
    /**
     * 根据数据库游标判断同步动作（核心决策逻辑）
     * @param c 指向当前记录的数据库游标
     * @return 返回SYNC_ACTION_*常量之一
     */
    public abstract int getSyncAction(Cursor c);
//---------------- 基础字段访问器 --------------------

    /** 设置远程服务端ID（通常在首次同步成功后调用） */
    public void setGid(String gid) {
        this.mGid = gid;
    }
    /** 设置节点显示名称（需考虑字符编码问题） */
    public void setName(String name) {
        this.mName = name;
    }
    /** 更新时间戳（通常自动维护，单位：毫秒） */
    public void setLastModified(long lastModified) {
        this.mLastModified = lastModified;
    }
    /** 标记删除状态（支持软删除模式） */
    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }
    /** 获取远程服务端ID（可能为null表示未同步） */
    public String getGid() {
        return this.mGid;
    }
    /** 获取显示名称（可能为空字符串） */
    public String getName() {
        return this.mName;
    }
    /** 获取最后修改时间（用于冲突检测的关键字段） */
    public long getLastModified() {
        return this.mLastModified;
    }
    /** 判断是否已标记删除（注意：需结合同步状态处理） */
    public boolean getDeleted() {
        return this.mDeleted;
    }

}
