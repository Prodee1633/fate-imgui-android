# Fate ImGui Android - 构建指南

## 快速开始

### 方法一：使用 Android Studio（推荐）

1. **下载并安装 Android Studio**
   - 访问 https://developer.android.com/studio
   - 下载并安装最新版本

2. **打开项目**
   ```
   File → Open → 选择 fate-imgui-android 文件夹
   ```

3. **同步 Gradle**
   - 等待 Android Studio 自动同步 Gradle
   - 如果出现提示，点击 "Install NDK" 安装 NDK

4. **构建 APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

5. **获取 APK**
   - 构建完成后，点击右下角通知
   - 选择 "locate" 打开 APK 所在文件夹
   - APK 路径: `app/build/outputs/apk/debug/app-debug.apk`

### 方法二：使用命令行

#### 前提条件

1. **安装 JDK 11 或更高版本**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install openjdk-11-jdk

   # macOS
   brew install openjdk@11

   # Windows
   # 下载并安装 from https://adoptium.net/
   ```

2. **安装 Android SDK**
   ```bash
   # 下载命令行工具
   mkdir -p ~/Android/Sdk/cmdline-tools
   cd ~/Android/Sdk/cmdline-tools
   wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
   unzip commandlinetools-linux-9477386_latest.zip
   mv cmdline-tools latest
   ```

3. **设置环境变量**
   ```bash
   # 添加到 ~/.bashrc 或 ~/.zshrc
   export ANDROID_SDK_ROOT=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin
   export PATH=$PATH:$ANDROID_SDK_ROOT/platform-tools
   ```

4. **安装必要的 SDK 组件**
   ```bash
   sdkmanager --install "platform-tools" "platforms;android-33" "build-tools;33.0.0" "ndk;25.1.8937393" "cmake;3.22.1"
   ```

#### 构建步骤

1. **进入项目目录**
   ```bash
   cd fate-imgui-android
   ```

2. **运行构建脚本**
   ```bash
   # 构建 Debug APK
   python3 build.py --debug

   # 构建并安装
   python3 build.py --debug --install

   # 或使用 Gradle 直接构建
   ./gradlew assembleDebug
   ```

3. **获取 APK**
   - Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

### 方法三：使用 Docker

如果你不想在本地安装 Android SDK，可以使用 Docker：

1. **创建 Dockerfile**
   ```dockerfile
   FROM openjdk:11-jdk

   # 安装 Android SDK
   RUN mkdir -p /opt/android-sdk && \
       cd /opt/android-sdk && \
       wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
       unzip -q commandlinetools-linux-9477386_latest.zip && \
       mkdir -p cmdline-tools/latest && \
       mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true && \
       rm commandlinetools-linux-9477386_latest.zip

   ENV ANDROID_SDK_ROOT=/opt/android-sdk
   ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin

   # 安装 SDK 组件
   RUN yes | sdkmanager --licenses && \
       sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.0" "ndk;25.1.8937393" "cmake;3.22.1"

   WORKDIR /project
   ```

2. **构建并运行**
   ```bash
   docker build -t android-builder .
   docker run -v $(pwd):/project android-builder ./gradlew assembleDebug
   ```

## 常见问题

### 1. NDK 未找到

**错误信息：**
```
NDK not configured
```

**解决方案：**
```bash
# 在 local.properties 中添加
sdk.dir=/path/to/android-sdk
ndk.dir=/path/to/android-sdk/ndk/25.1.8937393
```

### 2. CMake 版本不匹配

**错误信息：**
```
CMake '3.10.2' was not found in PATH
```

**解决方案：**
```bash
sdkmanager --install "cmake;3.22.1"
```

### 3. 编译错误：找不到 GLES3

**错误信息：**
```
GLES3/gl3.h: No such file or directory
```

**解决方案：**
确保 NDK 正确安装，并在 CMakeLists.txt 中设置了正确的路径。

### 4. Gradle 同步失败

**解决方案：**
```bash
# 清理并重新同步
./gradlew clean
./gradlew build --refresh-dependencies
```

## 项目配置

### build.gradle (app)

```gradle
android {
    compileSdk 33
    
    defaultConfig {
        minSdk 21
        targetSdk 33
    }
    
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version "3.22.1"
        }
    }
    
    ndkVersion '25.1.8937393'
}
```

### CMakeLists.txt

```cmake
cmake_minimum_required(VERSION 3.10.2)
project("fateimgui")

find_library(log-lib log)
find_library(android-lib android)
find_library(EGL-lib EGL)
find_library(GLESv3-lib GLESv3)

add_library(fateimgui SHARED main.cpp)

target_link_libraries(fateimgui
    ${log-lib}
    ${android-lib}
    ${EGL-lib}
    ${GLESv3-lib}
)
```

## 调试技巧

### 1. 查看日志

```bash
adb logcat -s FateImGui:D
```

### 2. 调试原生代码

在 Android Studio 中：
```
Run → Debug 'app'
```

### 3. 性能分析

```bash
# GPU 分析
adb shell dumpsys gfxinfo com.fate.imgui
```

## 发布构建

### 1. 生成签名密钥

```bash
keytool -genkey -v -keystore fate-imgui.keystore -alias fate -keyalg RSA -keysize 2048 -validity 10000
```

### 2. 配置签名

在 `app/build.gradle` 中添加：

```gradle
android {
    signingConfigs {
        release {
            storeFile file("fate-imgui.keystore")
            storePassword "your-password"
            keyAlias "fate"
            keyPassword "your-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 3. 构建发布版

```bash
./gradlew assembleRelease
```

APK 将生成在：`app/build/outputs/apk/release/app-release.apk`

## 支持

如有问题，请提交 Issue 或联系开发者。
