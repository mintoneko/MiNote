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
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.NoteColumns;
 
 /**
  * FoldersListAdapter 是一个用于显示文件夹列表的适配器。
  * 它继承自 CursorAdapter，通过绑定数据库中的数据来动态生成视图。
  */
 public class FoldersListAdapter extends CursorAdapter {
     // 数据库查询的列名数组
     public static final String[] PROJECTION = {
         NoteColumns.ID,       // 文件夹 ID
         NoteColumns.SNIPPET   // 文件夹名称（片段）
     };
 
     // 数据库列索引
     public static final int ID_COLUMN = 0;   // ID 列索引
     public static final int NAME_COLUMN = 1; // 名称列索引
 
     /**
      * 构造函数，初始化适配器
      * @param context 上下文
      * @param c 数据库游标
      */
     public FoldersListAdapter(Context context, Cursor c) {
         super(context, c);
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
         return new FolderListItem(context); // 返回自定义的 FolderListItem 视图
     }
 
     /**
      * 绑定视图，将数据绑定到视图上
      * @param view 要绑定的视图
      * @param context 上下文
      * @param cursor 数据库游标
      */
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         if (view instanceof FolderListItem) {
             // 获取文件夹名称，如果是根文件夹则显示 "父文件夹"
             String folderName = (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) 
                 ? context.getString(R.string.menu_move_parent_folder) 
                 : cursor.getString(NAME_COLUMN);
             // 将文件夹名称绑定到视图
             ((FolderListItem) view).bind(folderName);
         }
     }
 
     /**
      * 获取指定位置的文件夹名称
      * @param context 上下文
      * @param position 列表中的位置
      * @return 文件夹名称
      */
     public String getFolderName(Context context, int position) {
         Cursor cursor = (Cursor) getItem(position); // 获取指定位置的游标
         return (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) 
             ? context.getString(R.string.menu_move_parent_folder) 
             : cursor.getString(NAME_COLUMN); // 返回文件夹名称
     }
 
     /**
      * FolderListItem 是一个自定义的视图类，用于显示单个文件夹项。
      */
     private class FolderListItem extends LinearLayout {
         private TextView mName; // 显示文件夹名称的 TextView
 
         /**
          * 构造函数，初始化 FolderListItem
          * @param context 上下文
          */
         public FolderListItem(Context context) {
             super(context);
             // 加载布局文件
             inflate(context, R.layout.folder_list_item, this);
             // 获取布局中的 TextView
             mName = (TextView) findViewById(R.id.tv_folder_name);
         }
 
         /**
          * 绑定文件夹名称到视图
          * @param name 文件夹名称
          */
         public void bind(String name) {
             mName.setText(name); // 设置文件夹名称
         }
     }
 }