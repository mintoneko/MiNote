# Mi Note

## 项目来源

`MiCode`便签是小米便签的社区开源版，由MIUI团队(www.miui.com) 发起并贡献第一批代码，遵循NOTICE文件所描述的开源协议，今后为`MiCode`社区(www.micode.net) 拥有，并由社区发布和维护。

项目源地址：https://github.com/MiCode/Notes

## 环境配置

**Android Studio 2024.2.2**

**Gradle 8.10.2**

**SDK 35**

**JDK 22**

> 为保障项目能够正常运行，请保证环境配置和上面一致

## 使用说明

可安装的程序包（apk文件） 在 release 文件下，直接安装运行。

## 项目结构

```bash
java
└── net
    └── micode
        └── notes
            ├── data                                    # 数据模型与数据库相关核心类
            │   ├── Contact.java                          # 联系人数据模型类
            │   ├── Notes.java                            # 便签基础数据模型类
            │   ├── NotesDatabaseHelper.java              # SQLite数据库帮助类，负责创建和维护数据库
            │   └── NotesProvider.java                    # ContentProvider实现，提供数据访问接口
            │
            ├── gtask                                   # Google任务同步功能模块
            │   ├── data                                  # 任务数据模型与本地存储
            │   │   ├── MetaData.java                     # 同步元数据管理类
            │   │   ├── Node.java                         # 任务树节点抽象基类
            │   │   ├── SqlData.java                      # SQLite数据操作抽象类
            │   │   ├── SqlNote.java                      # 便签数据与Google任务映射类
            │   │   ├── Task.java                         # 单个任务数据模型
            │   │   └── TaskList.java                     # 任务列表数据模型
            │   ├── exception                           # 自定义异常类
            │   │   ├── ActionFailureException.java       # 任务操作失败异常
            │   │   └── NetworkFailureException.java      # 网络连接异常
            │   └── remote                                # 远程同步服务相关
            │       ├── GTaskASyncTask.java             # 异步任务执行基类
            │       ├── GTaskClient.java                  # Google Tasks API客户端封装
            │       ├── GTaskManager.java                 # 任务同步流程管理类
            │       └── GTaskSyncService.java             # 后台同步服务实现
            │
            ├── model                                   # 业务逻辑模型
            │   ├── Note.java                             # 便签业务逻辑模型
            │   └── WorkingNote.java                      # 编辑态便签业务逻辑扩展
            │
            ├── tool                                    # 工具类与辅助方法
            │   ├── BackupUtils.java                      # 数据备份恢复工具
            │   ├── DataUtils.java                        # 通用数据操作工具
            │   ├── GTaskStringUtils.java                 # Google任务字符串处理工具
            │   └── ResourceParser.java                   # 资源解析工具类
            │
            ├── ui                                     # 用户界面相关组件
            │   ├── AlarmAlertActivity.java              # 闹钟提醒对话框
            │   ├── AlarmInitReceiver.java               # 开机启动广播接收器
            │   ├── AlarmReceiver.java                   # 闹钟事件广播接收器
            │   ├── DateTimePicker.java                  # 日期时间选择器控件
            │   ├── DateTimePickerDialog.java            # 日期时间选择对话框
            │   ├── DropdownMenu.java                    # 自定义下拉菜单控件
            │   ├── FoldersListAdapter.java              # 文件夹列表适配器
            │   ├── NoteEditActivity.java                # 便签编辑界面Activity
            │   ├── NoteEditText.java                    # 富文本编辑控件
            │   ├── NoteItemData.java                    # 列表项数据封装类
            │   ├── NotesListActivity.java               # 主列表界面Activity
            │   ├── NotesListAdapter.java                # 主列表数据适配器
            │   ├── NotesListItem.java                   # 列表项自定义View
            │   └── NotesPreferenceActivity.java         # 设置界面Activity
            │
            └── widget                                 # 桌面小部件实现
                ├── NoteWidgetProvider.java              # 基础尺寸便签小部件
                ├── NoteWidgetProvider_2x.java           # 2x尺寸便签小部件
                └── NoteWidgetProvider_4x.java           # 4x尺寸便签小部件
```

