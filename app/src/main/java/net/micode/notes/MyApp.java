package net.micode.notes;

import android.app.Application;

// 自定义Application类
public class MyApp extends Application {
  private String globalData="0";

  @Override
  public void onCreate() {
    super.onCreate();
    // 这里可以初始化全局数据
  }

  // 线程安全的getter/setter
  public synchronized String getGlobalData() {
    return globalData;
  }

  public synchronized void setGlobalData(String data) {
    this.globalData = data;
  }
}

