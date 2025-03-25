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

package net.micode.notes.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.ResourceParser;

/**
 * 4x尺寸笔记桌面小部件的提供器
 * 继承自NoteWidgetProvider基类，实现4x尺寸的小部件界面和功能
 */
public class NoteWidgetProvider_4x extends NoteWidgetProvider {
    /**
     * 当小部件更新时调用
     * 调用父类的update方法更新小部件的内容和外观
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.update(context, appWidgetManager, appWidgetIds);
    }

    /**
     * 获取4x尺寸小部件的布局资源ID
     * @return 返回4x小部件的布局资源ID
     */
    protected int getLayoutId() {
        return R.layout.widget_4x;
    }

    /**
     * 根据背景ID获取4x尺寸小部件对应的背景资源ID
     * @param bgId 背景ID
     * @return 返回对应的背景资源ID
     */
    @Override
    protected int getBgResourceId(int bgId) {
        return ResourceParser.WidgetBgResources.getWidget4xBgResource(bgId);
    }

    /**
     * 获取小部件类型
     * @return 返回4x小部件类型常量
     */
    @Override
    protected int getWidgetType() {
        return Notes.TYPE_WIDGET_4X;
    }
}
