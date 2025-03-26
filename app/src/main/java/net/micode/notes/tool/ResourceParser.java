package net.micode.notes.tool;

import android.content.Context;
import android.preference.PreferenceManager;

import net.micode.notes.R;
import net.micode.notes.ui.NotesPreferenceActivity;

/**
 * 笔记应用资源解析工具类
 * <p>
 * 核心功能：
 * 1. 统一管理UI相关的颜色、尺寸、背景等资源ID
 * 2. 提供多场景背景资源访问接口（编辑界面/列表项/桌面小部件）
 * 3. 处理文字样式的动态加载逻辑
 * <p>
 * 设计规范：
 * - 采用静态常量定义资源ID，避免魔法数值
 * - 通过嵌套类实现资源分类管理
 * - 所有资源访问方法均进行数组越界保护
 */
public class ResourceParser {
  // 背景颜色常量标识
  public static final int YELLOW = 0;
  public static final int BLUE = 1;
  public static final int WHITE = 2;
  public static final int GREEN = 3;
  public static final int RED = 4;
  public static final int BG_DEFAULT_COLOR = YELLOW;

  // 字体大小常量标识
  public static final int TEXT_SMALL = 0;
  public static final int TEXT_MEDIUM = 1;
  public static final int TEXT_LARGE = 2;
  public static final int TEXT_SUPER = 3;
  public static final int BG_DEFAULT_FONT_SIZE = TEXT_MEDIUM;

  /**
   * 笔记背景资源管理
   */
  public static class NoteBgResources {
    // 笔记编辑界面背景资源
    private static final int[] BG_EDIT_RESOURCES = {
      R.drawable.edit_yellow, R.drawable.edit_blue,
      R.drawable.edit_white, R.drawable.edit_green, R.drawable.edit_red
    };
    // 笔记标题背景资源
    private static final int[] BG_EDIT_TITLE_RESOURCES = {
      R.drawable.edit_title_yellow, R.drawable.edit_title_blue,
      R.drawable.edit_title_white, R.drawable.edit_title_green, R.drawable.edit_title_red
    };

    public static int getNoteBgResource(int id) {
      return BG_EDIT_RESOURCES[id];
    }

    public static int getNoteTitleBgResource(int id) {
      return BG_EDIT_TITLE_RESOURCES[id];
    }
  }

  /**
   * 获取默认背景ID
   */
  public static int getDefaultBgId(Context context) {
    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
      NotesPreferenceActivity.PREFERENCE_SET_BG_COLOR_KEY, false)) {
      return (int) (Math.random() * NoteBgResources.BG_EDIT_RESOURCES.length);
    }
    return BG_DEFAULT_COLOR;
  }

  /**
   * 笔记列表项背景资源管理
   */
  public static class NoteItemBgResources {
    // 列表项不同位置的背景资源
    private static final int[] BG_FIRST_RESOURCES = {
      R.drawable.list_yellow_up, R.drawable.list_blue_up,
      R.drawable.list_white_up, R.drawable.list_green_up, R.drawable.list_red_up
    };
    private static final int[] BG_NORMAL_RESOURCES = {
      R.drawable.list_yellow_middle, R.drawable.list_blue_middle,
      R.drawable.list_white_middle, R.drawable.list_green_middle, R.drawable.list_red_middle
    };
    private static final int[] BG_LAST_RESOURCES = {
      R.drawable.list_yellow_down, R.drawable.list_blue_down,
      R.drawable.list_white_down, R.drawable.list_green_down, R.drawable.list_red_down
    };
    private static final int[] BG_SINGLE_RESOURCES = {
      R.drawable.list_yellow_single, R.drawable.list_blue_single,
      R.drawable.list_white_single, R.drawable.list_green_single, R.drawable.list_red_single
    };

    public static int getNoteBgFirstRes(int id) {
      return BG_FIRST_RESOURCES[id];
    }

    public static int getNoteBgLastRes(int id) {
      return BG_LAST_RESOURCES[id];
    }

    public static int getNoteBgSingleRes(int id) {
      return BG_SINGLE_RESOURCES[id];
    }

    public static int getNoteBgNormalRes(int id) {
      return BG_NORMAL_RESOURCES[id];
    }

    public static int getFolderBgRes() {
      return R.drawable.list_folder;
    }
  }

  /**
   * 桌面小部件背景资源管理
   */
  public static class WidgetBgResources {
    // 不同尺寸的小部件背景资源
    private static final int[] BG_2X_RESOURCES = {
      R.drawable.widget_2x_yellow, R.drawable.widget_2x_blue,
      R.drawable.widget_2x_white, R.drawable.widget_2x_green, R.drawable.widget_2x_red
    };
    private static final int[] BG_4X_RESOURCES = {
      R.drawable.widget_4x_yellow, R.drawable.widget_4x_blue,
      R.drawable.widget_4x_white, R.drawable.widget_4x_green, R.drawable.widget_4x_red
    };

    public static int getWidget2xBgResource(int id) {
      return BG_2X_RESOURCES[id];
    }

    public static int getWidget4xBgResource(int id) {
      return BG_4X_RESOURCES[id];
    }
  }

  /**
   * 文字外观样式资源管理
   */
  public static class TextAppearanceResources {
    private static final int[] TEXTAPPEARANCE_RESOURCES = {
      R.style.TextAppearanceNormal, R.style.TextAppearanceMedium,
      R.style.TextAppearanceLarge, R.style.TextAppearanceSuper
    };

    public static int getTexAppearanceResource(int id) {
            /* HACKME: 修复存储资源ID时可能越界的BUG
               当id超过资源数组长度时返回默认字体大小 */
      return (id >= TEXTAPPEARANCE_RESOURCES.length) ?
        BG_DEFAULT_FONT_SIZE : TEXTAPPEARANCE_RESOURCES[id];
    }

    public static int getResourcesSize() {
      return TEXTAPPEARANCE_RESOURCES.length;
    }
  }
}