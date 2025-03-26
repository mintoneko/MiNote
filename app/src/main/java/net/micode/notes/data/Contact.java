package net.micode.notes.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Contact 类用于根据电话号码获取对应的联系人显示名称，并对查询结果进行缓存。
 * 主要功能和特点：
 * 1. 使用系统内容解析器查询联系人数据库，通过 PHONE_NUMBERS_EQUAL 函数
 * 来实现灵活的电话号码匹配。
 * 2. 采用最小匹配码 (min_match) 技术，解决不同格式电话号码的匹配问题。
 * 3. 结果通过静态缓存 (HashMap) 存储，避免重复查询，提高性能。
 */
public class Contact {
  // 静态缓存，用于存储电话号码与联系人名称的对应关系
  private static HashMap<String, String> sContactCache; // ggbond
  // 日志标签，用于输出调试信息
  private static final String TAG = "Contact";

  // 查询条件模板，用于从联系人数据库中查找与给定电话号码匹配的记录
  // 使用 PHONE_NUMBERS_EQUAL 函数和最小匹配码(min_match)过滤匹配结果
  private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
    + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
    + " AND " + Data.RAW_CONTACT_ID + " IN "
    + "(SELECT raw_contact_id "
    + " FROM phone_lookup"
    + " WHERE min_match = '+')";

  /**
   * 根据电话号码获取联系人显示名称
   *
   * @param context     应用上下文，用于获取内容解析器
   * @param phoneNumber 电话号码字符串
   * @return 如果找到匹配的联系人，则返回其显示名称；否则返回 null
   */
  public static String getContact(Context context, String phoneNumber) {
    // 初始化缓存，如果还未创建
    if (sContactCache == null) {
      sContactCache = new HashMap<String, String>();
    }

    // 检查缓存中是否已存在该电话号码对应的联系人名称
    if (sContactCache.containsKey(phoneNumber)) {
      return sContactCache.get(phoneNumber);
    }

    // 构造查询条件：将模板中占位符 "+" 替换为电话号码的最小匹配码
    // 这样可以确保查询时使用正确的匹配条件
    String selection = CALLER_ID_SELECTION.replace("+",
      PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));

    // 通过内容解析器查询联系人数据库，获取显示名称
    Cursor cursor = context.getContentResolver().query(
      Data.CONTENT_URI,                     // 数据 URI：联系人数据
      new String[]{Phone.DISPLAY_NAME},     // 返回的列：联系人显示名称
      selection,                            // 查询条件
      new String[]{phoneNumber},            // 查询参数：电话号码
      null);                                // 排序条件，此处不需要

    // 如果查询结果非空且至少有一条记录
    if (cursor != null && cursor.moveToFirst()) {
      try {
        // 从结果集中获取第 0 列，即联系人显示名称
        String name = cursor.getString(0);
        // 将查询到的结果放入缓存，便于后续直接获取
        sContactCache.put(phoneNumber, name);
        return name;
      } catch (IndexOutOfBoundsException e) {
        // 捕获索引越界异常，并通过日志记录错误信息
        Log.e(TAG, "Cursor get string error " + e.toString());
        return null;
      } finally {
        // 无论查询成功与否，都需要关闭 Cursor 释放资源
        cursor.close();
      }
    } else {
      // 如果没有查询到匹配的联系人，则在日志中记录相关信息
      Log.d(TAG, "No contact matched with number:" + phoneNumber);
      return null;
    }
  }
}
