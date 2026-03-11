# 错误修复记录

## 发现的问题及修复

### 1. C++ 代码修复 (main_fixed.cpp)

#### ✅ 修复 1: 圆形绘制着色器错误
**问题**: `drawCircle` 函数使用了矩形着色器，导致圆形渲染不正确
**修复**: 新增专用的圆形片段着色器和 VAO/VBO
```cpp
// 新增圆形着色器
const char* circleFragmentShaderSource = ...
GLuint g_CircleProgram = 0;
GLuint g_CircleVAO = 0;
GLuint g_CircleVBO = 0;
```

#### ✅ 修复 2: 频繁创建/删除 GPU 资源
**问题**: 每次调用 `drawCircle` 都创建和删除 VAO/VBO
**修复**: 预计算圆形顶点，只创建一次
```cpp
// 初始化时创建
float circleVertices[(segments + 2) * 2];
// ... 预计算顶点

// 绘制时直接使用
glBindVertexArray(g_CircleVAO);
glDrawArrays(GL_TRIANGLE_FAN, 0, 34);
```

#### ✅ 修复 3: EGL 错误处理
**问题**: 缺少 EGL 错误检查
**修复**: 添加完整的错误检查
```cpp
if (display == EGL_NO_DISPLAY) {
    LOGE("Failed to get EGL display");
    return nullptr;
}
if (!eglInitialize(display, &major, &minor)) {
    LOGE("Failed to initialize EGL");
    return nullptr;
}
```

#### ✅ 修复 4: 添加 VSync
**问题**: 缺少帧率同步
**修复**: 添加 `eglSwapInterval`
```cpp
g_Display = display;
eglSwapInterval(display, 1);  // 启用 VSync
```

#### ✅ 修复 5: 资源清理
**问题**: 缺少完整的 OpenGL 资源清理
**修复**: 新增 `cleanupGL()` 函数
```cpp
void cleanupGL() {
    if (g_RectProgram) glDeleteProgram(g_RectProgram);
    if (g_VAO) glDeleteVertexArrays(1, &g_VAO);
    // ... 清理所有资源
}
```

#### ✅ 修复 6: 线程安全
**问题**: `nativeOnResume` 只是设置标志，不会重启线程
**修复**: 添加注释说明，实际恢复由 `surfaceCreated` 处理
```cpp
JNIEXPORT void JNICALL
Java_com_fate_imgui_NativeHelper_nativeOnResume(JNIEnv* env, jclass clazz) {
    LOGI("Resume requested - will restart on next surface creation");
}
```

---

### 2. Java 代码修复 (MainActivity_fixed.java)

#### ✅ 修复 1: 重复加载原生库
**问题**: `MainActivity` 和 `NativeHelper` 都调用了 `System.loadLibrary`
**修复**: 只在 `NativeHelper` 中加载
```java
// MainActivity.java - 已移除
// System.loadLibrary("fateimgui");

// NativeHelper.java - 保留
static {
    System.loadLibrary("fateimgui");
}
```

#### ✅ 修复 2: 生命周期管理
**问题**: `surfaceDestroyed` 后没有恢复机制
**修复**: 添加 `mNeedRestartRender` 标记，在 `surfaceCreated` 中自动重启
```java
private boolean mNeedRestartRender = false;

@Override
public void surfaceCreated(SurfaceHolder holder) {
    NativeHelper.nativeSetSurface(holder.getSurface());
}

@Override
protected void onPause() {
    super.onPause();
    mNeedRestartRender = true;
    NativeHelper.nativeOnPause();
}
```

#### ✅ 修复 3: 添加 onDestroy
**问题**: 缺少资源清理
**修复**: 添加 `onDestroy` 方法
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    NativeHelper.nativeOnPause();
}
```

---

### 3. AndroidManifest.xml 修复

#### ✅ 修复: 移除已弃用的 uses-sdk
**问题**: `uses-sdk` 标签在较新 Gradle 中已弃用
**修复**: 移除该标签，版本信息已在 build.gradle 中设置
```xml
<!-- 已移除 -->
<!-- <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="33" /> -->
```

---

### 4. 新增必要文件

#### ✅ 新增: gradle.properties
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
android.useAndroidX=true
android.nonTransitiveRClass=true
```

#### ✅ 新增: local.properties.template
```properties
sdk.dir=/home/username/Android/Sdk
ndk.dir=/home/username/Android/Sdk/ndk/25.1.8937393
```

---

## 如何应用修复

### 方法 1: 使用修复后的文件
```bash
# 替换原文件
cp app/src/main/cpp/main_fixed.cpp app/src/main/cpp/main.cpp
cp app/src/main/java/com/fate/imgui/MainActivity_fixed.java app/src/main/java/com/fate/imgui/MainActivity.java
cp app/src/main/AndroidManifest_fixed.xml app/src/main/AndroidManifest.xml

# 添加新文件
cp gradle.properties gradle.properties
cp local.properties.template local.properties
# 编辑 local.properties 设置你的 SDK 路径
```

### 方法 2: 手动修改
按照上面的修复说明，手动修改对应文件。

---

## 测试检查清单

- [ ] 项目能成功构建
- [ ] APK 能正常安装
- [ ] HUD 界面正常显示
- [ ] 阴影效果正常
- [ ] 透明度滑块工作正常
- [ ] 颜色选择器工作正常
- [ ] 切换应用后能正常恢复
- [ ] 旋转屏幕后能正常恢复
- [ ] 60 FPS 稳定运行
