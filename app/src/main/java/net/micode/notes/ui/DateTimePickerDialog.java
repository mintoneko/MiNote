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

 import java.util.Calendar;
 
 import net.micode.notes.R;
 import net.micode.notes.ui.DateTimePicker;
 import net.micode.notes.ui.DateTimePicker.OnDateTimeChangedListener;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.text.format.DateFormat;
 import android.text.format.DateUtils;
 
 /**
  * DateTimePickerDialog 是一个自定义的对话框，用于选择日期和时间。
  * 它封装了 DateTimePicker 控件，并提供了回调接口以处理用户选择的日期和时间。
  */
 public class DateTimePickerDialog extends AlertDialog implements OnClickListener {
 
     private Calendar mDate = Calendar.getInstance(); // 当前日期和时间
     private boolean mIs24HourView; // 是否为 24 小时制
     private OnDateTimeSetListener mOnDateTimeSetListener; // 日期时间设置回调接口
     private DateTimePicker mDateTimePicker; // 自定义的日期时间选择控件
 
     /**
      * 日期时间设置回调接口
      */
     public interface OnDateTimeSetListener {
         /**
          * 当用户设置日期时间时调用
          * @param dialog 当前对话框
          * @param date 用户选择的日期时间（以毫秒为单位）
          */
         void OnDateTimeSet(AlertDialog dialog, long date);
     }
 
     /**
      * 构造函数，初始化 DateTimePickerDialog
      * @param context 上下文
      * @param date 初始日期时间（以毫秒为单位）
      */
     public DateTimePickerDialog(Context context, long date) {
         super(context);
         mDateTimePicker = new DateTimePicker(context); // 初始化 DateTimePicker 控件
         setView(mDateTimePicker); // 将 DateTimePicker 设置为对话框的内容视图
 
         // 设置日期时间变化监听器
         mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener() {
             public void onDateTimeChanged(DateTimePicker view, int year, int month,
                     int dayOfMonth, int hourOfDay, int minute) {
                 // 更新内部的 Calendar 对象
                 mDate.set(Calendar.YEAR, year);
                 mDate.set(Calendar.MONTH, month);
                 mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                 mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                 mDate.set(Calendar.MINUTE, minute);
                 updateTitle(mDate.getTimeInMillis()); // 更新对话框标题
             }
         });
 
         // 初始化日期时间
         mDate.setTimeInMillis(date);
         mDate.set(Calendar.SECOND, 0); // 将秒数设置为 0
         mDateTimePicker.setCurrentDate(mDate.getTimeInMillis()); // 设置 DateTimePicker 的当前日期时间
 
         // 设置对话框按钮
         setButton(context.getString(R.string.datetime_dialog_ok), this); // 确定按钮
         setButton2(context.getString(R.string.datetime_dialog_cancel), (OnClickListener) null); // 取消按钮
 
         // 设置是否为 24 小时制
         set24HourView(DateFormat.is24HourFormat(this.getContext()));
 
         // 更新对话框标题
         updateTitle(mDate.getTimeInMillis());
     }
 
     /**
      * 设置是否为 24 小时制
      * @param is24HourView 如果为 true，则为 24 小时制；否则为 12 小时制
      */
     public void set24HourView(boolean is24HourView) {
         mIs24HourView = is24HourView;
     }
 
     /**
      * 设置日期时间设置回调接口
      * @param callBack 回调接口
      */
     public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
         mOnDateTimeSetListener = callBack;
     }
 
     /**
      * 更新对话框标题，显示当前选择的日期和时间
      * @param date 当前日期时间（以毫秒为单位）
      */
     private void updateTitle(long date) {
         int flag =
             DateUtils.FORMAT_SHOW_YEAR | // 显示年份
             DateUtils.FORMAT_SHOW_DATE | // 显示日期
             DateUtils.FORMAT_SHOW_TIME; // 显示时间
         flag |= mIs24HourView ? DateUtils.FORMAT_24HOUR : DateUtils.FORMAT_12HOUR; // 根据设置选择时间格式
         setTitle(DateUtils.formatDateTime(this.getContext(), date, flag)); // 设置对话框标题
     }
 
     /**
      * 当用户点击对话框按钮时调用
      * @param dialog 对话框接口
      * @param which 被点击的按钮
      */
     public void onClick(DialogInterface dialog, int which) {
         if (mOnDateTimeSetListener != null) {
             // 调用回调接口，传递用户选择的日期时间
             mOnDateTimeSetListener.OnDateTimeSet(this, mDate.getTimeInMillis());
         }
     }
 }