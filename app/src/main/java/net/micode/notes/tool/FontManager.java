package net.micode.notes.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import net.micode.notes.R;

/**
 * 字体管理工具类，负责应用字体的加载和切换
 */
public class FontManager {
  private static final int[] FONT_RESOURCES = {
    R.font.a,       // 第一个字体文件
    R.font.b,       // 第二个字体文件
    R.font.c,       // 第三个字体文件
    R.font.d        // 第四个字体文件
  };

  /**
   * 获取当前设置的字体
   *
   * @param context 上下文对象，用于访问资源
   * @return 当前生效的Typeface字体对象
   */
  public static Typeface getCurrentFont(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("app_font", Context.MODE_PRIVATE);
    int fontIndex = prefs.getInt("current_font", 0);
    return ResourcesCompat.getFont(context, FONT_RESOURCES[fontIndex]);
  }
}
