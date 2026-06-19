package com.engine2d.game;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity {
    
    private GLSurfaceView glSurfaceView;
    private GameRenderer renderer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Force landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // Create OpenGL Surface
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        
        // Create renderer
        renderer = new GameRenderer();
        glSurfaceView.setRenderer(renderer);
        
        // Set content view
        setContentView(glSurfaceView);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        renderer.shutdown();
    }
}

// OpenGL ES 2.0 Renderer
class GameRenderer implements GLSurfaceView.Renderer {
    
    private static native void nativeInit();
    private static native void nativeRender();
    private static native void nativeResize(int width, int height);
    private static native void nativeShutdown();
    
    static {
        System.loadLibrary("gameengine");
    }
    
    private boolean initialized = false;
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        nativeInit();
        initialized = true;
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        nativeResize(width, height);
    }
    
    @Override
    public void onDrawFrame(GL10 gl) {
        nativeRender();
    }
    
    public void shutdown() {
        if (initialized) {
            nativeShutdown();
            initialized = false;
        }
    }
}
