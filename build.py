#!/usr/bin/env python3
"""
Fate ImGui Android 构建脚本
帮助自动化构建APK过程
"""

import os
import sys
import subprocess
import argparse

def run_command(cmd, cwd=None):
    """运行命令并输出结果"""
    print(f">>> {cmd}")
    result = subprocess.run(cmd, shell=True, cwd=cwd, capture_output=True, text=True)
    if result.stdout:
        print(result.stdout)
    if result.stderr:
        print(result.stderr, file=sys.stderr)
    return result.returncode == 0

def check_requirements():
    """检查构建环境要求"""
    print("Checking build requirements...")
    
    # 检查 Java
    if not run_command("java -version"):
        print("ERROR: Java not found. Please install JDK 11 or higher.")
        return False
    
    # 检查 Android SDK
    android_sdk = os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME")
    if not android_sdk:
        print("WARNING: ANDROID_SDK_ROOT not set. Please set it to your Android SDK path.")
        print("Example: export ANDROID_SDK_ROOT=/home/user/Android/Sdk")
    else:
        print(f"Android SDK: {android_sdk}")
    
    return True

def setup_sdk():
    """设置Android SDK组件"""
    android_sdk = os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME")
    if not android_sdk:
        print("ERROR: ANDROID_SDK_ROOT not set")
        return False
    
    sdkmanager = os.path.join(android_sdk, "cmdline-tools", "latest", "bin", "sdkmanager")
    if not os.path.exists(sdkmanager):
        print(f"ERROR: sdkmanager not found at {sdkmanager}")
        return False
    
    # 安装必要的组件
    components = [
        "platform-tools",
        "platforms;android-33",
        "build-tools;33.0.0",
        "ndk;25.1.8937393",
        "cmake;3.22.1"
    ]
    
    for component in components:
        print(f"Installing {component}...")
        if not run_command(f'yes | "{sdkmanager}" "{component}"', cwd=android_sdk):
            print(f"WARNING: Failed to install {component}")
    
    return True

def build_apk(debug=True):
    """构建APK"""
    project_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 清理
    print("Cleaning...")
    run_command("./gradlew clean", cwd=project_dir)
    
    # 构建
    if debug:
        print("Building Debug APK...")
        if run_command("./gradlew assembleDebug", cwd=project_dir):
            apk_path = os.path.join(project_dir, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
            if os.path.exists(apk_path):
                print(f"\n✓ Debug APK built successfully!")
                print(f"  Location: {apk_path}")
                return apk_path
    else:
        print("Building Release APK...")
        if run_command("./gradlew assembleRelease", cwd=project_dir):
            apk_path = os.path.join(project_dir, "app", "build", "outputs", "apk", "release", "app-release-unsigned.apk")
            if os.path.exists(apk_path):
                print(f"\n✓ Release APK built successfully!")
                print(f"  Location: {apk_path}")
                return apk_path
    
    print("\n✗ Build failed!")
    return None

def install_apk(apk_path):
    """安装APK到设备"""
    if not os.path.exists(apk_path):
        print(f"ERROR: APK not found: {apk_path}")
        return False
    
    print(f"Installing {apk_path}...")
    return run_command(f'adb install -r "{apk_path}"')

def main():
    parser = argparse.ArgumentParser(description="Fate ImGui Android Build Script")
    parser.add_argument("--setup", action="store_true", help="Setup Android SDK components")
    parser.add_argument("--debug", action="store_true", help="Build debug APK (default)")
    parser.add_argument("--release", action="store_true", help="Build release APK")
    parser.add_argument("--install", action="store_true", help="Install APK after building")
    parser.add_argument("--check", action="store_true", help="Check build requirements only")
    
    args = parser.parse_args()
    
    if args.check:
        check_requirements()
        return
    
    if args.setup:
        setup_sdk()
        return
    
    if not check_requirements():
        sys.exit(1)
    
    debug = not args.release
    apk_path = build_apk(debug=debug)
    
    if apk_path and args.install:
        install_apk(apk_path)

if __name__ == "__main__":
    main()
