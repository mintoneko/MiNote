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

 package net.micode.notes.ui;

 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 
 import net.micode.notes.data.Notes;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  * NotesListAdapter 是一个自定义的适配器，用于显示笔记列表。
  * 它继承自 CursorAdapter，通过绑定数据库中的数据动态生成视图。
  */
 public class NotesListAdapter extends CursorAdapter {
     private static final String TAG = "NotesListAdapter"; // 日志标签
     private Context mContext; // 上下文对象
     private HashMap<Integer, Boolean> mSelectedIndex; // 存储选中项的索引及其选中状态
     private int mNotesCount; // 笔记总数
     private boolean mChoiceMode; // 是否处于选择模式
 
     /**
      * AppWidgetAttribute 是一个内部类，用于存储小部件的属性。
      */
     public static class AppWidgetAttribute {
         public int widgetId; // 小部件 ID
         public int widgetType; // 小部件类型
     }
 
     /**
      * 构造函数，初始化适配器
      * @param context 上下文
      */
     public NotesListAdapter(Context context) {
         super(context, null);
         mSelectedIndex = new HashMap<>();
         mContext = context;
         mNotesCount = 0;
     }
 
     /**
      * 创建新视图
      * @param context 上下文
      * @param cursor 数据库游标
      * @param parent 父视图
      * @return 新创建的视图
      */
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return new NotesListItem(context); // 返回自定义的 NotesListItem 视图
     }
 
     /**
      * 绑定视图，将数据绑定到视图上
      * @param view 要绑定的视图
      * @param context 上下文
      * @param cursor 数据库游标
      */
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         if (view instanceof NotesListItem) {
             NoteItemData itemData = new NoteItemData(context, cursor); // 从游标中获取笔记数据
             ((NotesListItem) view).bind(context, itemData, mChoiceMode,
                     isSelectedItem(cursor.getPosition())); // 绑定数据到视图
         }
     }
 
     /**
      * 设置某一项的选中状态
      * @param position 项目位置
      * @param checked 是否选中
      */
     public void setCheckedItem(final int position, final boolean checked) {
         mSelectedIndex.put(position, checked);
         notifyDataSetChanged(); // 通知数据集已更改
     }
 
     /**
      * 判断是否处于选择模式
      * @return 如果处于选择模式返回 true，否则返回 false
      */
     public boolean isInChoiceMode() {
         return mChoiceMode;
     }
 
     /**
      * 设置选择模式
      * @param mode 是否启用选择模式
      */
     public void setChoiceMode(boolean mode) {
         mSelectedIndex.clear(); // 清空选中项
         mChoiceMode = mode;
     }
 
     /**
      * 全选或取消全选
      * @param checked 是否选中所有项
      */
     public void selectAll(boolean checked) {
         Cursor cursor = getCursor();
         for (int i = 0; i < getCount(); i++) {
             if (cursor.moveToPosition(i)) {
                 if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) {
                     setCheckedItem(i, checked);
                 }
             }
         }
     }
 
     /**
      * 获取所有选中项的 ID 集合
      * @return 选中项的 ID 集合
      */
     public HashSet<Long> getSelectedItemIds() {
         HashSet<Long> itemSet = new HashSet<>();
         for (Integer position : mSelectedIndex.keySet()) {
             if (mSelectedIndex.get(position)) {
                 Long id = getItemId(position);
                 if (id == Notes.ID_ROOT_FOLDER) {
                     Log.d(TAG, "Wrong item id, should not happen");
                 } else {
                     itemSet.add(id);
                 }
             }
         }
         return itemSet;
     }
 
     /**
      * 获取所有选中的小部件属性
      * @return 选中的小部件属性集合
      */
     public HashSet<AppWidgetAttribute> getSelectedWidget() {
         HashSet<AppWidgetAttribute> itemSet = new HashSet<>();
         for (Integer position : mSelectedIndex.keySet()) {
             if (mSelectedIndex.get(position)) {
                 Cursor c = (Cursor) getItem(position);
                 if (c != null) {
                     AppWidgetAttribute widget = new AppWidgetAttribute();
                     NoteItemData item = new NoteItemData(mContext, c);
                     widget.widgetId = item.getWidgetId();
                     widget.widgetType = item.getWidgetType();
                     itemSet.add(widget);
                     /**
                      * 不要在这里关闭游标，只有适配器可以关闭游标
                      */
                 } else {
                     Log.e(TAG, "Invalid cursor");
                     return null;
                 }
             }
         }
         return itemSet;
     }
 
     /**
      * 获取选中项的数量
      * @return 选中项的数量
      */
     public int getSelectedCount() {
         Collection<Boolean> values = mSelectedIndex.values();
         if (values == null) {
             return 0;
         }
         int count = 0;
         for (Boolean value : values) {
             if (value) {
                 count++;
             }
         }
         return count;
     }
 
     /**
      * 判断是否所有项都被选中
      * @return 如果所有项都被选中返回 true，否则返回 false
      */
     public boolean isAllSelected() {
         int checkedCount = getSelectedCount();
         return (checkedCount != 0 && checkedCount == mNotesCount);
     }
 
     /**
      * 判断某一项是否被选中
      * @param position 项目位置
      * @return 如果选中返回 true，否则返回 false
      */
     public boolean isSelectedItem(final int position) {
         return mSelectedIndex.getOrDefault(position, false);
     }
 
     @Override
     protected void onContentChanged() {
         super.onContentChanged();
         calcNotesCount(); // 重新计算笔记数量
     }
 
     @Override
     public void changeCursor(Cursor cursor) {
         super.changeCursor(cursor);
         calcNotesCount(); // 重新计算笔记数量
     }
 
     /**
      * 计算笔记数量
      */
     private void calcNotesCount() {
         mNotesCount = 0;
         for (int i = 0; i < getCount(); i++) {
             Cursor c = (Cursor) getItem(i);
             if (c != null) {
                 if (NoteItemData.getNoteType(c) == Notes.TYPE_NOTE) {
                     mNotesCount++;
                 }
             } else {
                 Log.e(TAG, "Invalid cursor");
                 return;
             }
         }
     }
 }