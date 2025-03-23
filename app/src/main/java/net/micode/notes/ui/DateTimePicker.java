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

 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 
 import net.micode.notes.R;
 
 import android.content.Context;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.NumberPicker;
 
 /**
  * DateTimePicker 是一个自定义控件，用于选择日期和时间。
  * 它支持 24 小时制和 12 小时制（AM/PM 模式）。
  */
 public class DateTimePicker extends FrameLayout {
 
     private static final boolean DEFAULT_ENABLE_STATE = true; // 默认启用状态
     private static final int HOURS_IN_HALF_DAY = 12; // 半天的小时数
     private static final int HOURS_IN_ALL_DAY = 24; // 一天的小时数
     private static final int DAYS_IN_ALL_WEEK = 7; // 一周的天数
     private static final int DATE_SPINNER_MIN_VAL = 0; // 日期选择器的最小值
     private static final int DATE_SPINNER_MAX_VAL = DAYS_IN_ALL_WEEK - 1; // 日期选择器的最大值
     private static final int HOUR_SPINNER_MIN_VAL_24_HOUR_VIEW = 0; // 24 小时制的最小小时值
     private static final int HOUR_SPINNER_MAX_VAL_24_HOUR_VIEW = 23; // 24 小时制的最大小时值
     private static final int HOUR_SPINNER_MIN_VAL_12_HOUR_VIEW = 1; // 12 小时制的最小小时值
     private static final int HOUR_SPINNER_MAX_VAL_12_HOUR_VIEW = 12; // 12 小时制的最大小时值
     private static final int MINUT_SPINNER_MIN_VAL = 0; // 分钟选择器的最小值
     private static final int MINUT_SPINNER_MAX_VAL = 59; // 分钟选择器的最大值
     private static final int AMPM_SPINNER_MIN_VAL = 0; // AM/PM 选择器的最小值
     private static final int AMPM_SPINNER_MAX_VAL = 1; // AM/PM 选择器的最大值
 
     private final NumberPicker mDateSpinner; // 日期选择器
     private final NumberPicker mHourSpinner; // 小时选择器
     private final NumberPicker mMinuteSpinner; // 分钟选择器
     private final NumberPicker mAmPmSpinner; // AM/PM 选择器
     private Calendar mDate; // 当前日期和时间
 
     private String[] mDateDisplayValues = new String[DAYS_IN_ALL_WEEK]; // 日期显示值数组
     private boolean mIsAm; // 是否为上午
     private boolean mIs24HourView; // 是否为 24 小时制
     private boolean mIsEnabled = DEFAULT_ENABLE_STATE; // 是否启用
     private boolean mInitialising; // 是否正在初始化
     private OnDateTimeChangedListener mOnDateTimeChangedListener; // 日期时间变化监听器
 
     // 日期选择器的值变化监听器
     private NumberPicker.OnValueChangeListener mOnDateChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             mDate.add(Calendar.DAY_OF_YEAR, newVal - oldVal); // 更新日期
             updateDateControl(); // 更新日期控件
             onDateTimeChanged(); // 通知日期时间变化
         }
     };
 
     // 小时选择器的值变化监听器
     private NumberPicker.OnValueChangeListener mOnHourChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             boolean isDateChanged = false;
             Calendar cal = Calendar.getInstance();
             if (!mIs24HourView) { // 如果是 12 小时制
                 if (!mIsAm && oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, 1); // 日期加 1 天
                     isDateChanged = true;
                 } else if (mIsAm && oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, -1); // 日期减 1 天
                     isDateChanged = true;
                 }
                 if (oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY ||
                         oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                     mIsAm = !mIsAm; // 切换 AM/PM
                     updateAmPmControl(); // 更新 AM/PM 控件
                 }
             } else { // 如果是 24 小时制
                 if (oldVal == HOURS_IN_ALL_DAY - 1 && newVal == 0) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, 1); // 日期加 1 天
                     isDateChanged = true;
                 } else if (oldVal == 0 && newVal == HOURS_IN_ALL_DAY - 1) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, -1); // 日期减 1 天
                     isDateChanged = true;
                 }
             }
             int newHour = mHourSpinner.getValue() % HOURS_IN_HALF_DAY + (mIsAm ? 0 : HOURS_IN_HALF_DAY);
             mDate.set(Calendar.HOUR_OF_DAY, newHour); // 更新小时
             onDateTimeChanged(); // 通知日期时间变化
             if (isDateChanged) {
                 setCurrentYear(cal.get(Calendar.YEAR)); // 更新年份
                 setCurrentMonth(cal.get(Calendar.MONTH)); // 更新月份
                 setCurrentDay(cal.get(Calendar.DAY_OF_MONTH)); // 更新日期
             }
         }
     };
 
     // 分钟选择器的值变化监听器
     private NumberPicker.OnValueChangeListener mOnMinuteChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             int minValue = mMinuteSpinner.getMinValue();
             int maxValue = mMinuteSpinner.getMaxValue();
             int offset = 0;
             if (oldVal == maxValue && newVal == minValue) {
                 offset += 1; // 分钟从最大值滚动到最小值，小时加 1
             } else if (oldVal == minValue && newVal == maxValue) {
                 offset -= 1; // 分钟从最小值滚动到最大值，小时减 1
             }
             if (offset != 0) {
                 mDate.add(Calendar.HOUR_OF_DAY, offset); // 更新小时
                 mHourSpinner.setValue(getCurrentHour()); // 更新小时选择器
                 updateDateControl(); // 更新日期控件
                 int newHour = getCurrentHourOfDay();
                 if (newHour >= HOURS_IN_HALF_DAY) {
                     mIsAm = false; // 设置为下午
                     updateAmPmControl(); // 更新 AM/PM 控件
                 } else {
                     mIsAm = true; // 设置为上午
                     updateAmPmControl(); // 更新 AM/PM 控件
                 }
             }
             mDate.set(Calendar.MINUTE, newVal); // 更新分钟
             onDateTimeChanged(); // 通知日期时间变化
         }
     };
 
     // AM/PM 选择器的值变化监听器
     private NumberPicker.OnValueChangeListener mOnAmPmChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             mIsAm = !mIsAm; // 切换 AM/PM
             if (mIsAm) {
                 mDate.add(Calendar.HOUR_OF_DAY, -HOURS_IN_HALF_DAY); // 从下午切换到上午，小时减 12
             } else {
                 mDate.add(Calendar.HOUR_OF_DAY, HOURS_IN_HALF_DAY); // 从上午切换到下午，小时加 12
             }
             updateAmPmControl(); // 更新 AM/PM 控件
             onDateTimeChanged(); // 通知日期时间变化
         }
     };
 
     /**
      * 日期时间变化监听器接口
      */
     public interface OnDateTimeChangedListener {
         void onDateTimeChanged(DateTimePicker view, int year, int month,
                 int dayOfMonth, int hourOfDay, int minute);
     }
 
     // 构造函数和其他方法省略...
 }