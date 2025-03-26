package net.micode.notes.gtask.data;

import android.database.Cursor;
import android.util.Log;

import net.micode.notes.tool.GTaskStringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * GTask元数据管理类，负责处理任务关联关系
 * <p>
 * 用于存储和解析与Google Task关联的元数据信息，包含：
 * - 关联任务组的唯一标识（GID）
 * - 元信息的JSON序列化与反序列化
 * <p>
 * 注意：本类仅通过远程JSON数据更新内容，本地操作会抛出异常
 */
public class MetaData extends Task {
    //目标日志
    private final static String TAG = MetaData.class.getSimpleName();
    // 关联的Google Task ID（重要：存储关联任务的核心标识）
    private String mRelatedGid = null;
    /**
     * 设置元数据信息（核心方法）
     * @param gid 关联的Google Task ID
     * @param metaInfo 需要注入gid的元数据JSON对象
     * 说明：将gid存入metaInfo并设置notes字段，固定名称为META_NOTE_NAME
     */
    public void setMeta(String gid, JSONObject metaInfo) {
        // 将关联ID插入元数据头部
        try {
            metaInfo.put(GTaskStringUtils.META_HEAD_GTASK_ID, gid);
        } catch (JSONException e) {
            Log.e(TAG, "failed to put related gid");
        }
        // 将元数据JSON字符串存入notes字段
        setNotes(metaInfo.toString());
        // 设置固定名称标识这是一个元数据任务
        setName(GTaskStringUtils.META_NOTE_NAME);
    }
    /**
     * 获取关联的Google Task ID
     */
    public String getRelatedGid() {
        return mRelatedGid;
    }
    /**
     * 判断是否需要持久化（覆盖父类方法）
     * @return 当notes字段存在数据时需要保存
     */
    @Override
    public boolean isWorthSaving() {
        return getNotes() != null;
    }
    /**
     * 从远程JSON数据解析内容（核心反序列化方法）
     * 流程：先调用父类解析，然后从notes字段提取关联GID
     */
    @Override
    public void setContentByRemoteJSON(JSONObject js) {
        super.setContentByRemoteJSON(js);
        if (getNotes() != null) {
            try {
                // 解析notes字段中的元数据JSON
                JSONObject metaInfo = new JSONObject(getNotes().trim());
                mRelatedGid = metaInfo.getString(GTaskStringUtils.META_HEAD_GTASK_ID);
            } catch (JSONException e) {
                Log.w(TAG, "failed to get related gid");
                mRelatedGid = null;// 确保解析失败时清空ID
            }
        }
    }
    // 以下方法禁止使用，因为这个类仅用于远程数据同步

    /**
     * 禁止本地JSON设置（设计约束）
     * 原因：元数据仅通过远程同步获取，不提供本地设置方式
     */
    @Override
    public void setContentByLocalJSON(JSONObject js) {
        // this function should not be called
        throw new IllegalAccessError("MetaData:setContentByLocalJSON should not be called");
    }
    /**
     * 禁止获取本地JSON（设计约束）
     * 原因：元数据不存储在本地数据库
     */
    @Override
    public JSONObject getLocalJSONFromContent() {
        throw new IllegalAccessError("MetaData:getLocalJSONFromContent should not be called");
    }
    /**
     * 禁止同步操作（设计约束）
     * 原因：元数据作为附属数据，应由主任务触发同步
     */
    @Override
    public int getSyncAction(Cursor c) {
        throw new IllegalAccessError("MetaData:getSyncAction should not be called");
    }

}
