package com.engine2d.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.content.pm.ActivityInfo;

public class MainActivity extends Activity {
    
    private TextView statusText;
    private volatile boolean running = true;
    private Thread gameThread;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Force landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // Create simple UI
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#1a1a2e"));
        
        statusText = new TextView(this);
        statusText.setText("GameEngine 2D\n\nIniciando...");
        statusText.setTextSize(24);
        statusText.setTextColor(Color.WHITE);
        statusText.setGravity(android.view.Gravity.CENTER);
        
        layout.addView(statusText);
        setContentView(layout);
        
        // Start game loop
        startGame();
    }
    
    private void startGame() {
        gameThread = new Thread(new Runnable() {
            int frame = 0;
            long lastTime = System.currentTimeMillis();
            
            @Override
            public void run() {
                while (running) {
                    long currentTime = System.currentTimeMillis();
                    long delta = currentTime - lastTime;
                    lastTime = currentTime;
                    frame++;
                    
                    final int fps = (int)(1000 / Math.max(delta, 1));
                    final int f = frame;
                    
                    // Update UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("GameEngine 2D\n\n" +
                                "Frame: " + f + "\n" +
                                "FPS: " + fps + "\n\n" +
                                "Funcionando!");
                        }
                    });
                    
                    // 30 FPS
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        gameThread.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join(500);
            } catch (InterruptedException e) {}
        }
    }
}
