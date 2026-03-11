# Fate ImGui Android

一个基于OpenGL ES 3.0的Android HUD界面，参考Minecraft游戏内的Opai界面设计。

## 功能特性

- **精美的HUD界面**：顶部状态栏显示 Fate Logo、用户名、延迟和FPS
- **阴影效果**：多层阴影渲染，可调节透明度
- **透明度控制**：通过滑块调节背景和阴影的透明度
- **图标颜色调色盘**：支持RGB调节和预设颜色选择
- **流畅的OpenGL ES 3.0渲染**

## 界面预览

```
┌─────────────────────────────────────────────────────────┐
│  ⬡  Fate  •  👤 YaoMao  •  📶 67ms to mc.hypixel.net  •  🔄 144 fps  │
└─────────────────────────────────────────────────────────┘
```

## 项目结构

```
fate-imgui-android/
├── app/
│   ├── src/main/
│   │   ├── cpp/              # C++ 原生代码
│   │   │   ├── main.cpp      # 主渲染逻辑
│   │   │   └── CMakeLists.txt
│   │   ├── java/com/fate/imgui/
│   │   │   ├── MainActivity.java    # 主Activity
│   │   │   └── NativeHelper.java    # JNI接口
│   │   ├── res/              # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## 构建要求

- Android Studio Arctic Fox (2020.3.1) 或更高版本
- Android SDK 33
- Android NDK 25.1.8937393
- CMake 3.22.1
- OpenGL ES 3.0 支持

## 构建步骤

### 方法一：使用 Android Studio

1. 打开 Android Studio
2. 选择 "Open an existing Android Studio project"
3. 选择 `fate-imgui-android` 文件夹
4. 等待 Gradle 同步完成
5. 点击 "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"
6. APK 文件将生成在 `app/build/outputs/apk/debug/app-debug.apk`

### 方法二：使用命令行

```bash
# 设置 ANDROID_SDK_ROOT 环境变量
export ANDROID_SDK_ROOT=/path/to/android-sdk

# 进入项目目录
cd fate-imgui-android

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

## 安装和运行

```bash
# 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 运行应用
adb shell am start -n com.fate.imgui/.MainActivity
```

## 技术实现

### 原生层 (C++)

- **OpenGL ES 3.0**: 使用自定义着色器实现圆角矩形、阴影和图标渲染
- **EGL**: 创建和管理 OpenGL 上下文
- **多线程渲染**: 独立的渲染线程确保流畅的60 FPS

### Java 层

- **SurfaceView**: 提供原生渲染表面
- **JNI 接口**: 与原生代码通信
- **UI 控制面板**: 滑块和颜色选择器

### 着色器

1. **圆角矩形着色器**: 使用 SDF (Signed Distance Field) 实现平滑圆角
2. **阴影着色器**: 多层模糊阴影效果
3. **图标着色器**: 程序生成的 Fate 风格六边形图标

## 自定义设置

### 调节透明度

- **背景透明度**: 0.0 - 1.0 (默认: 0.85)
- **阴影透明度**: 0.0 - 1.0 (默认: 0.5)

### 图标颜色

- **RGB 滑块**: 精确调节红、绿、蓝通道
- **预设颜色**: 8种常用颜色快速选择
  - 白色、红色、绿色、蓝色
  - 金色、紫色、青色、橙色

## 性能优化

- **VSync 同步**: 60 FPS 稳定渲染
- **混合模式优化**: 高效的 Alpha 混合
- **顶点缓存**: 减少 GPU 数据传输

## 许可证

MIT License

## 致谢

- 界面设计灵感来源于 Minecraft Opai Client
- Fate Logo 设计参考 Fate/Stay Night 系列
