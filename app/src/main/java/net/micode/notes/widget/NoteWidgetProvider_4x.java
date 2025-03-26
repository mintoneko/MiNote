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
