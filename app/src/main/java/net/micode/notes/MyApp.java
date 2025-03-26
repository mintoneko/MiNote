package net.micode.notes;

import android.app.Application;

// 自定义Application类，用于维护全局应用状态
public class MyApp extends Application {
  // 全局共享数据，初始值为"0"
  private String globalData = "0";

  @Override
  /**
   * 应用创建时调用，进行全局初始化
   * 可以在此初始化数据库连接、第三方库等
   */
  public void onCreate() {
    super.onCreate();
    // 这里可以初始化全局数据
  }

  // 线程安全的getter/setter
  // ----------------------------

  /**
   * 获取全局共享数据
   *
   * @return 当前存储的全局字符串数据
   */
  public synchronized String getGlobalData() {
    return globalData;
  }

  /**
   * 设置全局共享数据
   *
   * @param data 要存储的新字符串数据
   */
  public synchronized void setGlobalData(String data) {
    this.globalData = data;
  }
}
