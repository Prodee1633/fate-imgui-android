package com.fate.imgui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    
    private SurfaceView mSurfaceView;
    private LinearLayout mControlsLayout;
    private SeekBar mBgAlphaSeekBar;
    private SeekBar mShadowAlphaSeekBar;
    private SeekBar mRedSeekBar;
    private SeekBar mGreenSeekBar;
    private SeekBar mBlueSeekBar;
    
    // 标记是否需要重启渲染
    private boolean mNeedRestartRender = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        // 创建主布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.rgb(30, 30, 35));
        
        // 创建SurfaceView用于OpenGL渲染
        mSurfaceView = new SurfaceView(this);
        mSurfaceView.getHolder().addCallback(this);
        mainLayout.addView(mSurfaceView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1.0f
        ));
        
        // 创建控制面板
        createControlsPanel(mainLayout);
        
        setContentView(mainLayout);
        
        // 注意：原生库在 NativeHelper 中加载，这里不需要重复加载
        // System.loadLibrary("fateimgui");  // 已移除，避免重复加载
    }
    
    private void createControlsPanel(LinearLayout parent) {
        mControlsLayout = new LinearLayout(this);
        mControlsLayout.setOrientation(LinearLayout.VERTICAL);
        mControlsLayout.setPadding(20, 20, 20, 20);
        mControlsLayout.setBackgroundColor(Color.rgb(25, 25, 28));
        
        // 背景透明度控制
        TextView bgAlphaLabel = new TextView(this);
        bgAlphaLabel.setText("Background Opacity");
        bgAlphaLabel.setTextColor(Color.WHITE);
        bgAlphaLabel.setTextSize(14);
        mControlsLayout.addView(bgAlphaLabel);
        
        mBgAlphaSeekBar = new SeekBar(this);
        mBgAlphaSeekBar.setMax(100);
        mBgAlphaSeekBar.setProgress(85);
        mBgAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alpha = progress / 100.0f;
                NativeHelper.nativeSetBackgroundAlpha(alpha);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mControlsLayout.addView(mBgAlphaSeekBar);
        
        // 阴影透明度控制
        TextView shadowAlphaLabel = new TextView(this);
        shadowAlphaLabel.setText("Shadow Opacity");
        shadowAlphaLabel.setTextColor(Color.WHITE);
        shadowAlphaLabel.setTextSize(14);
        shadowAlphaLabel.setPadding(0, 20, 0, 0);
        mControlsLayout.addView(shadowAlphaLabel);
        
        mShadowAlphaSeekBar = new SeekBar(this);
        mShadowAlphaSeekBar.setMax(100);
        mShadowAlphaSeekBar.setProgress(50);
        mShadowAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alpha = progress / 100.0f;
                NativeHelper.nativeSetShadowAlpha(alpha);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mControlsLayout.addView(mShadowAlphaSeekBar);
        
        // 图标颜色控制
        TextView colorLabel = new TextView(this);
        colorLabel.setText("Icon Color (RGB)");
        colorLabel.setTextColor(Color.WHITE);
        colorLabel.setTextSize(14);
        colorLabel.setPadding(0, 20, 0, 0);
        mControlsLayout.addView(colorLabel);
        
        // 红色
        LinearLayout redLayout = new LinearLayout(this);
        redLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView redLabel = new TextView(this);
        redLabel.setText("R: ");
        redLabel.setTextColor(Color.RED);
        redLayout.addView(redLabel);
        mRedSeekBar = new SeekBar(this);
        mRedSeekBar.setMax(255);
        mRedSeekBar.setProgress(255);
        mRedSeekBar.setOnSeekBarChangeListener(createColorChangeListener());
        redLayout.addView(mRedSeekBar, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        ));
        mControlsLayout.addView(redLayout);
        
        // 绿色
        LinearLayout greenLayout = new LinearLayout(this);
        greenLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView greenLabel = new TextView(this);
        greenLabel.setText("G: ");
        greenLabel.setTextColor(Color.GREEN);
        greenLayout.addView(greenLabel);
        mGreenSeekBar = new SeekBar(this);
        mGreenSeekBar.setMax(255);
        mGreenSeekBar.setProgress(255);
        mGreenSeekBar.setOnSeekBarChangeListener(createColorChangeListener());
        greenLayout.addView(mGreenSeekBar, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        ));
        mControlsLayout.addView(greenLayout);
        
        // 蓝色
        LinearLayout blueLayout = new LinearLayout(this);
        blueLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView blueLabel = new TextView(this);
        blueLabel.setText("B: ");
        blueLabel.setTextColor(Color.BLUE);
        blueLayout.addView(blueLabel);
        mBlueSeekBar = new SeekBar(this);
        mBlueSeekBar.setMax(255);
        mBlueSeekBar.setProgress(255);
        mBlueSeekBar.setOnSeekBarChangeListener(createColorChangeListener());
        blueLayout.addView(mBlueSeekBar, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        ));
        mControlsLayout.addView(blueLayout);
        
        // 预设颜色按钮
        LinearLayout presetLayout = new LinearLayout(this);
        presetLayout.setOrientation(LinearLayout.HORIZONTAL);
        presetLayout.setPadding(0, 20, 0, 0);
        
        int[] presetColors = {
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN,
            Color.parseColor("#FF8000")
        };
        
        for (int color : presetColors) {
            View colorButton = new View(this);
            colorButton.setBackgroundColor(color);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
            params.setMargins(10, 0, 10, 0);
            colorButton.setLayoutParams(params);
            colorButton.setOnClickListener(v -> setIconColor(color));
            presetLayout.addView(colorButton);
        }
        
        mControlsLayout.addView(presetLayout);
        
        parent.addView(mControlsLayout, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }
    
    private SeekBar.OnSeekBarChangeListener createColorChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateIconColor();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }
    
    private void updateIconColor() {
        float r = mRedSeekBar.getProgress() / 255.0f;
        float g = mGreenSeekBar.getProgress() / 255.0f;
        float b = mBlueSeekBar.getProgress() / 255.0f;
        NativeHelper.nativeSetIconColor(r, g, b);
    }
    
    private void setIconColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        mRedSeekBar.setProgress(r);
        mGreenSeekBar.setProgress(g);
        mBlueSeekBar.setProgress(b);
        NativeHelper.nativeSetIconColor(r / 255.0f, g / 255.0f, b / 255.0f);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 如果之前被销毁，需要重新启动渲染
        NativeHelper.nativeSetSurface(holder.getSurface());
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 可以在这里处理屏幕旋转等尺寸变化
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 停止渲染线程
        NativeHelper.nativeOnPause();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 标记需要重启渲染
        mNeedRestartRender = true;
        NativeHelper.nativeOnPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 注意：渲染会在 surfaceCreated 中自动重启
        // 因为 Activity 恢复后 SurfaceView 会重新创建 Surface
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保清理资源
        NativeHelper.nativeOnPause();
    }
}
