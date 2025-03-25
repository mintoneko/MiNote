/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.gtask.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.NotesDatabaseHelper.TABLE;
import net.micode.notes.gtask.exception.ActionFailureException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 数据库操作类，负责与数据内容表的交互
 * 核心职责：
 * 1. 提供与内容数据库的增、删、改、查接口
 * 2. 支持从远程JSON解析数据，并将数据写入数据库
 * 3. 提供同步操作所需的字段差异检测与更新
 */
public class SqlData {
    // 调试时的TAG标签
    private static final String TAG = SqlData.class.getSimpleName();
    // 无效的ID标记
    private static final int INVALID_ID = -99999;
    // 数据查询时需要的列名（映射数据库字段）
    public static final String[] PROJECTION_DATA = new String[] {
            DataColumns.ID, DataColumns.MIME_TYPE, DataColumns.CONTENT, DataColumns.DATA1,
            DataColumns.DATA3
    };
    // 各字段在游标中的位置索引
    public static final int DATA_ID_COLUMN = 0;

    public static final int DATA_MIME_TYPE_COLUMN = 1;

    public static final int DATA_CONTENT_COLUMN = 2;

    public static final int DATA_CONTENT_DATA_1_COLUMN = 3;

    public static final int DATA_CONTENT_DATA_3_COLUMN = 4;
    // 内容解析器，用于与内容提供者进行交互
    private ContentResolver mContentResolver;
    // 标志当前数据是否为新建状态
    private boolean mIsCreate;
    // 数据字段
    private long mDataId; // 数据ID

    private String mDataMimeType;// 数据类型

    private String mDataContent;// 数据内容

    private long mDataContentData1;// 数据附加字段1

    private String mDataContentData3; // 数据附加字段3

    private ContentValues mDiffDataValues; // 存储数据变更时的临时数据
    /**
     * 构造函数，用于新建数据并初始化默认值。
     * 新建操作时，数据ID为无效值，其他字段初始化为空或默认值。
     *
     * @param context 上下文环境，用于获取内容解析器
     */
    public SqlData(Context context) {
        mContentResolver = context.getContentResolver();
        mIsCreate = true;// 初始化为新建状态
        mDataId = INVALID_ID;
        mDataMimeType = DataConstants.NOTE;
        mDataContent = "";
        mDataContentData1 = 0;
        mDataContentData3 = "";
        mDiffDataValues = new ContentValues();
    }
    /**
     * 构造函数，用于从游标中加载现有的数据。
     * 游标中的数据会被映射到类的字段中，准备进行更新或读取操作。
     *
     * @param context 上下文环境，用于获取内容解析器
     * @param c 数据库查询返回的游标，包含现有数据
     */
    public SqlData(Context context, Cursor c) {
        mContentResolver = context.getContentResolver();
        mIsCreate = false;
        loadFromCursor(c);
        mDiffDataValues = new ContentValues();
    }
    /**
     * 从游标加载数据到类的字段中，用于后续的更新操作。
     *
     * @param c 数据库查询返回的游标
     */
    private void loadFromCursor(Cursor c) {
        mDataId = c.getLong(DATA_ID_COLUMN);// 从游标中获取数据ID
        mDataMimeType = c.getString(DATA_MIME_TYPE_COLUMN); // 获取数据类型
        mDataContent = c.getString(DATA_CONTENT_COLUMN); // 获取数据内容
        mDataContentData1 = c.getLong(DATA_CONTENT_DATA_1_COLUMN);// 获取附加字段1
        mDataContentData3 = c.getString(DATA_CONTENT_DATA_3_COLUMN);// 获取附加字段3
    }
    /**
     * 将远程服务器传递的JSON数据解析并映射到SqlData的字段中。
     * 若当前为新建操作，则会将字段的初始值放入差异数据中；否则，将变更的字段存储到差异数据中。
     *
     * @param js 来自服务器的JSON对象，包含了数据的详细信息
     * @throws JSONException 如果JSON格式不正确，抛出异常
     */
    public void setContent(JSONObject js) throws JSONException {
        long dataId = js.has(DataColumns.ID) ? js.getLong(DataColumns.ID) : INVALID_ID;
        if (mIsCreate || mDataId != dataId) {
            mDiffDataValues.put(DataColumns.ID, dataId);// 如果是新建或ID不同，更新差异数据
        }
        mDataId = dataId;// 更新数据ID
        // 解析数据类型（MIME类型）
        String dataMimeType = js.has(DataColumns.MIME_TYPE) ? js.getString(DataColumns.MIME_TYPE)
                : DataConstants.NOTE;
        if (mIsCreate || !mDataMimeType.equals(dataMimeType)) {
            mDiffDataValues.put(DataColumns.MIME_TYPE, dataMimeType);// 如果是新建或类型不同，更新差异数据
        }
        mDataMimeType = dataMimeType;// 更新数据类型
        // 解析数据内容
        String dataContent = js.has(DataColumns.CONTENT) ? js.getString(DataColumns.CONTENT) : "";
        if (mIsCreate || !mDataContent.equals(dataContent)) {
            mDiffDataValues.put(DataColumns.CONTENT, dataContent);
        }
        mDataContent = dataContent;
        // 解析附加字段1
        long dataContentData1 = js.has(DataColumns.DATA1) ? js.getLong(DataColumns.DATA1) : 0;
        if (mIsCreate || mDataContentData1 != dataContentData1) {
            mDiffDataValues.put(DataColumns.DATA1, dataContentData1);
        }
        mDataContentData1 = dataContentData1;
        // 解析附加字段3
        String dataContentData3 = js.has(DataColumns.DATA3) ? js.getString(DataColumns.DATA3) : "";
        if (mIsCreate || !mDataContentData3.equals(dataContentData3)) {
            mDiffDataValues.put(DataColumns.DATA3, dataContentData3);
        }
        mDataContentData3 = dataContentData3;
    }
    /**
     * 将当前数据内容转化为JSON格式，以便与外部系统进行数据交互。
     * 如果当前数据为新建状态，将会抛出异常，提示数据尚未创建。
     *
     * @return 转换后的JSON对象，包含当前数据的所有字段
     * @throws JSONException 如果数据转换过程中出现问题，抛出异常
     */
    public JSONObject getContent() throws JSONException {
        if (mIsCreate) {
            Log.e(TAG, "it seems that we haven't created this in database yet");
            return null;// 如果是新建状态，抛出错误并返回null
        }
        // 构建并返回JSON对象
        JSONObject js = new JSONObject();
        js.put(DataColumns.ID, mDataId);
        js.put(DataColumns.MIME_TYPE, mDataMimeType);
        js.put(DataColumns.CONTENT, mDataContent);
        js.put(DataColumns.DATA1, mDataContentData1);
        js.put(DataColumns.DATA3, mDataContentData3);
        return js;
    }
    /**
     * 提交当前数据的变更到数据库中。根据是新建还是更新，执行相应的操作。
     * 新建时将数据插入数据库，更新时根据版本号判断是否进行更新操作。
     *
     * 操作步骤：
     * 1. 如果是新建操作，插入数据并返回ID；
     * 2. 如果是更新操作，先构建差异数据，再根据版本号检查是否允许更新；
     * 3. 提交差异数据到数据库。
     *
     * @param noteId 关联的笔记ID
     * @param validateVersion 是否验证版本号（防止数据冲突）
     * @param version 当前版本号，用于检查数据一致性
     */
    public void commit(long noteId, boolean validateVersion, long version) {
        // 新建操作：插入数据
        if (mIsCreate) {
            if (mDataId == INVALID_ID && mDiffDataValues.containsKey(DataColumns.ID)) {
                mDiffDataValues.remove(DataColumns.ID); // 移除无效的ID
            }

            mDiffDataValues.put(DataColumns.NOTE_ID, noteId);// 设置笔记ID
            Uri uri = mContentResolver.insert(Notes.CONTENT_DATA_URI, mDiffDataValues);// 插入数据并获取URI
            try {
                mDataId = Long.valueOf(uri.getPathSegments().get(1));// 提取返回的ID
            } catch (NumberFormatException e) {
                Log.e(TAG, "Get note id error :" + e.toString());
                throw new ActionFailureException("create note failed"); // 插入失败时抛出异常
            }
        } else {
            // 更新操作：根据版本号验证是否允许更新
            if (mDiffDataValues.size() > 0) {
                int result = 0;
                if (!validateVersion) {
                    result = mContentResolver.update(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mDataId), mDiffDataValues, null, null);
                } else {
                    // 验证版本号后执行更新
                    result = mContentResolver.update(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mDataId), mDiffDataValues,
                            " ? in (SELECT " + NoteColumns.ID + " FROM " + TABLE.NOTE
                                    + " WHERE " + NoteColumns.VERSION + "=?)", new String[] {
                                    String.valueOf(noteId), String.valueOf(version)
                            });
                }
                if (result == 0) {
                    Log.w(TAG, "there is no update. maybe user updates note when syncing");// 更新失败时抛出异常
                }
            }
        }
        // 清空变更值，准备下一次操作
        mDiffDataValues.clear();
        mIsCreate = false;
    }
    /**
     * 获取当前数据的ID
     * @return 数据的ID
     */
    public long getId() {
        return mDataId;
    }
}
