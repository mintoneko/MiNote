package net.micode.notes.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.ResourceParser;

/**
 * 4x2尺寸笔记小部件实现类，提供：
 * 1. 大尺寸小部件的布局配置
 * 2. 4x2尺寸专用的背景资源映射
 * 3. 大尺寸小部件类型标识定义
 */
public class NoteWidgetProvider_4x extends NoteWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.update(context, appWidgetManager, appWidgetIds);
    }

    protected int getLayoutId() {
        return R.layout.widget_4x;
    }

    @Override
    protected int getBgResourceId(int bgId) {
        return ResourceParser.WidgetBgResources.getWidget4xBgResource(bgId);
    }

    @Override
    protected int getWidgetType() {
        return Notes.TYPE_WIDGET_4X;
    }
}
