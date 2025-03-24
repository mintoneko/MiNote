package com.loliowo.minote;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class) //声明使用AndroidJUnit4测试运行器来进行测试
public class ExampleInstrumentedTest {
  @Test
  public void useAppContext() {
    // Context of the app under test.
    // 获取被测应用（Target Application）的上下文对象
    // InstrumentationRegistry 提供与测试环境相关的注册信息
    // getTargetContext() 获取被测试应用的上下文（而非测试应用本身的上下文）
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    // 使用断言验证包名是否符合预期
    // 这是测试的核心验证逻辑，检查应用包名是否与构建配置一致
    // 第一个参数是预期值，第二个参数是实际值
    assertEquals("com.loliowo.minote", appContext.getPackageName());
  }
}
