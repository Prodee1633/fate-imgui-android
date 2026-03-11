package com.fate.imgui;

import android.view.Surface;

public class NativeHelper {
    
    static {
        System.loadLibrary("fateimgui");
    }
    
    // 设置渲染表面
    public static native void nativeSetSurface(Surface surface);
    
    // 生命周期回调
    public static native void nativeOnPause();
    public static native void nativeOnResume();
    
    // 设置背景透明度 (0.0 - 1.0)
    public static native void nativeSetBackgroundAlpha(float alpha);
    
    // 设置阴影透明度 (0.0 - 1.0)
    public static native void nativeSetShadowAlpha(float alpha);
    
    // 设置图标颜色 (RGB: 0.0 - 1.0)
    public static native void nativeSetIconColor(float r, float g, float b);
    
    // 获取当前值
    public static native float nativeGetBackgroundAlpha();
    public static native float nativeGetShadowAlpha();
    public static native float[] nativeGetIconColor();
}
