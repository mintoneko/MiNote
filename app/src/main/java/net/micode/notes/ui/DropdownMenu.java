package net.micode.notes.ui;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import net.micode.notes.R;

/**
 * DropdownMenu 是一个封装的下拉菜单组件。
 * 它基于 PopupMenu 实现，并通过一个按钮触发显示菜单。
 */
public class DropdownMenu {
  private Button mButton; // 触发下拉菜单的按钮
  private PopupMenu mPopupMenu; // PopupMenu 实例，用于显示菜单
  private Menu mMenu; // 菜单项

  /**
   * 构造函数，初始化 DropdownMenu
   *
   * @param context 上下文
   * @param button  触发菜单的按钮
   * @param menuId  菜单资源 ID
   */
  public DropdownMenu(Context context, Button button, int menuId) {
    mButton = button;
    // 设置按钮的背景图标为下拉菜单图标
    mButton.setBackgroundResource(R.drawable.dropdown_icon);
    // 初始化 PopupMenu，并将其绑定到按钮
    mPopupMenu = new PopupMenu(context, mButton);
    // 获取 PopupMenu 的菜单对象
    mMenu = mPopupMenu.getMenu();
    // 将指定的菜单资源加载到 PopupMenu 中
    mPopupMenu.getMenuInflater().inflate(menuId, mMenu);
    // 设置按钮的点击事件，点击时显示 PopupMenu
    mButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mPopupMenu.show(); // 显示下拉菜单
      }
    });
  }

  /**
   * 设置下拉菜单的菜单项点击监听器
   *
   * @param listener 菜单项点击监听器
   */
  public void setOnDropdownMenuItemClickListener(OnMenuItemClickListener listener) {
    if (mPopupMenu != null) {
      mPopupMenu.setOnMenuItemClickListener(listener); // 设置菜单项点击监听器
    }
  }

  /**
   * 根据菜单项 ID 查找菜单项
   *
   * @param id 菜单项 ID
   * @return 对应的菜单项
   */
  public MenuItem findItem(int id) {
    return mMenu.findItem(id); // 返回指定 ID 的菜单项
  }

  /**
   * 设置按钮的标题
   *
   * @param title 按钮标题
   */
  public void setTitle(CharSequence title) {
    mButton.setText(title); // 设置按钮的文本
  }
}