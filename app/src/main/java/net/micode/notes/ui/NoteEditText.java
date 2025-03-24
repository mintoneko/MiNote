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
 import android.graphics.Rect;
 import android.text.Layout;
 import android.text.Selection;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.text.style.URLSpan;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.MotionEvent;
 import android.widget.EditText;
 
 import net.micode.notes.R;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * NoteEditText 是一个自定义的 EditText，用于处理笔记编辑中的特殊行为。
  * 它支持链接点击、键盘事件处理以及动态文本变化监听。
  */
 public class NoteEditText extends EditText {
     private static final String TAG = "NoteEditText"; // 日志标签
     private int mIndex; // 当前编辑框的索引
     private int mSelectionStartBeforeDelete; // 删除键按下前的光标位置
 
     // 支持的链接类型
     private static final String SCHEME_TEL = "tel:"; // 电话链接
     private static final String SCHEME_HTTP = "http:"; // 网页链接
     private static final String SCHEME_EMAIL = "mailto:"; // 邮件链接
 
     // 链接类型与对应操作的资源 ID 映射
     private static final Map<String, Integer> sSchemaActionResMap = new HashMap<>();
     static {
         sSchemaActionResMap.put(SCHEME_TEL, R.string.note_link_tel);
         sSchemaActionResMap.put(SCHEME_HTTP, R.string.note_link_web);
         sSchemaActionResMap.put(SCHEME_EMAIL, R.string.note_link_email);
     }
 
     /**
      * 用于监听文本框变化的接口
      */
     public interface OnTextViewChangeListener {
         /**
          * 当文本框为空且按下删除键时，删除当前文本框
          */
         void onEditTextDelete(int index, String text);
 
         /**
          * 当按下回车键时，在当前文本框后添加一个新的文本框
          */
         void onEditTextEnter(int index, String text);
 
         /**
          * 当文本内容变化时，显示或隐藏选项
          */
         void onTextChange(int index, boolean hasText);
     }
 
     private OnTextViewChangeListener mOnTextViewChangeListener; // 文本框变化监听器
 
     // 构造函数
     public NoteEditText(Context context) {
         super(context, null);
         mIndex = 0; // 初始化索引为 0
     }
 
     public NoteEditText(Context context, AttributeSet attrs) {
         super(context, attrs, android.R.attr.editTextStyle);
     }
 
     public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     /**
      * 设置当前文本框的索引
      * @param index 文本框索引
      */
     public void setIndex(int index) {
         mIndex = index;
     }
 
     /**
      * 设置文本框变化监听器
      * @param listener 文本框变化监听器
      */
     public void setOnTextViewChangeListener(OnTextViewChangeListener listener) {
         mOnTextViewChangeListener = listener;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 // 获取点击位置的坐标
                 int x = (int) event.getX();
                 int y = (int) event.getY();
                 x -= getTotalPaddingLeft();
                 y -= getTotalPaddingTop();
                 x += getScrollX();
                 y += getScrollY();
 
                 // 根据点击位置设置光标
                 Layout layout = getLayout();
                 int line = layout.getLineForVertical(y);
                 int off = layout.getOffsetForHorizontal(line, x);
                 Selection.setSelection(getText(), off);
                 break;
         }
 
         return super.onTouchEvent(event);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_ENTER: // 处理回车键
                 if (mOnTextViewChangeListener != null) {
                     return false; // 交由监听器处理
                 }
                 break;
             case KeyEvent.KEYCODE_DEL: // 处理删除键
                 mSelectionStartBeforeDelete = getSelectionStart(); // 记录删除前的光标位置
                 break;
             default:
                 break;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_DEL: // 删除键松开时
                 if (mOnTextViewChangeListener != null) {
                     if (0 == mSelectionStartBeforeDelete && mIndex != 0) {
                         // 如果光标在开头且不是第一个文本框，则删除当前文本框
                         mOnTextViewChangeListener.onEditTextDelete(mIndex, getText().toString());
                         return true;
                     }
                 } else {
                     Log.d(TAG, "OnTextViewChangeListener 未设置");
                 }
                 break;
             case KeyEvent.KEYCODE_ENTER: // 回车键松开时
                 if (mOnTextViewChangeListener != null) {
                     int selectionStart = getSelectionStart();
                     String text = getText().subSequence(selectionStart, length()).toString();
                     setText(getText().subSequence(0, selectionStart));
                     // 通知监听器在当前文本框后添加新文本框
                     mOnTextViewChangeListener.onEditTextEnter(mIndex + 1, text);
                 } else {
                     Log.d(TAG, "OnTextViewChangeListener 未设置");
                 }
                 break;
             default:
                 break;
         }
         return super.onKeyUp(keyCode, event);
     }
 
     @Override
     protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
         if (mOnTextViewChangeListener != null) {
             // 当失去焦点且文本为空时，通知监听器隐藏选项
             if (!focused && TextUtils.isEmpty(getText())) {
                 mOnTextViewChangeListener.onTextChange(mIndex, false);
             } else {
                 // 当获得焦点或文本不为空时，通知监听器显示选项
                 mOnTextViewChangeListener.onTextChange(mIndex, true);
             }
         }
         super.onFocusChanged(focused, direction, previouslyFocusedRect);
     }
 
     @Override
     protected void onCreateContextMenu(ContextMenu menu) {
         if (getText() instanceof Spanned) {
             int selStart = getSelectionStart();
             int selEnd = getSelectionEnd();
 
             int min = Math.min(selStart, selEnd);
             int max = Math.max(selStart, selEnd);
 
             final URLSpan[] urls = ((Spanned) getText()).getSpans(min, max, URLSpan.class);
             if (urls.length == 1) {
                 int defaultResId = 0;
                 for (String schema : sSchemaActionResMap.keySet()) {
                     if (urls[0].getURL().indexOf(schema) >= 0) {
                         defaultResId = sSchemaActionResMap.get(schema);
                         break;
                     }
                 }
 
                 if (defaultResId == 0) {
                     defaultResId = R.string.note_link_other;
                 }
 
                 // 添加上下文菜单项并设置点击事件
                 menu.add(0, 0, 0, defaultResId).setOnMenuItemClickListener(
                         new OnMenuItemClickListener() {
                             public boolean onMenuItemClick(MenuItem item) {
                                 // 点击菜单项后触发链接点击事件
                                 urls[0].onClick(NoteEditText.this);
                                 return true;
                             }
                         });
             }
         }
         super.onCreateContextMenu(menu);
     }
 }