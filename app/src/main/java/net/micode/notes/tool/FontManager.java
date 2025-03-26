package net.micode.notes.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import net.micode.notes.R;

public class FontManager {
  private static final int[] FONT_RESOURCES = {
    R.font.a,       // 第一个字体文件
    R.font.b,       // 第二个字体文件
    R.font.c,       // 第三个字体文件
    R.font.d        // 第四个字体文件
  };

  public static Typeface getCurrentFont(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("app_font", Context.MODE_PRIVATE);
    int fontIndex = prefs.getInt("current_font", 0);
    return ResourcesCompat.getFont(context, FONT_RESOURCES[fontIndex]);
  }
}
