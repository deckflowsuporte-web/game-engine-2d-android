package com.engine2d.game;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.content.pm.ActivityInfo;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    
    private SurfaceView surfaceView;
    private GameThread gameThread;
    
    // Paint objects for drawing
    private Paint playerPaint, enemyPaint, coinPaint, groundPaint, textPaint;
    
    // Game state
    private float playerX, playerY;
    private float playerVelX, playerVelY;
    private float playerWidth = 50, playerHeight = 50;
    
    private float[] enemyX = new float[5];
    private float[] enemyVelX = new float[5];
    private float[] coinX = new float[5];
    private float[] coinY = new float[5];
    private boolean[] coinActive = new boolean[5];
    
    private float groundY;
    private int screenWidth, screenHeight;
    private int coinCount = 0;
    private int lives = 3;
    
    // Touch
    private float touchX = -1;
    private boolean isJumping = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // Create paints
        playerPaint = new Paint();
        playerPaint.setColor(Color.parseColor("#3498db")); // Blue
        playerPaint.setStyle(Paint.Style.FILL);
        
        enemyPaint = new Paint();
        enemyPaint.setColor(Color.parseColor("#e74c3c")); // Red
        enemyPaint.setStyle(Paint.Style.FILL);
        
        coinPaint = new Paint();
        coinPaint.setColor(Color.parseColor("#f1c40f")); // Yellow
        coinPaint.setStyle(Paint.Style.FILL);
        
        groundPaint = new Paint();
        groundPaint.setColor(Color.parseColor("#27ae60")); // Green
        groundPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
        
        // Create surface view
        surfaceView = new SurfaceView(this);
        surfaceView.getHolder().addCallback(this);
        setContentView(surfaceView);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = surfaceView.getWidth();
        screenHeight = surfaceView.getHeight();
        groundY = screenHeight - 80;
        
        // Initialize game objects
        playerX = screenWidth / 2f;
        playerY = groundY - playerHeight;
        playerVelX = 0;
        playerVelY = 0;
        
        for (int i = 0; i < 5; i++) {
            enemyX[i] = 100 + i * 150;
            enemyVelX[i] = 3 + i;
            coinX[i] = 80 + i * 140;
            coinY[i] = groundY - 120 - (i % 3) * 50;
            coinActive[i] = true;
        }
        
        // Start game thread
        gameThread = new GameThread(holder);
        gameThread.setRunning(true);
        gameThread.start();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        groundY = screenHeight - 80;
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();
        
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Jump on tap
            if (playerY >= groundY - playerHeight - 5) {
                playerVelY = -18;
                isJumping = true;
            }
        }
        
        return true;
    }
    
    // Game Thread
    class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean running = false;
        
        GameThread(SurfaceHolder holder) {
            this.surfaceHolder = holder;
        }
        
        void setRunning(boolean running) {
            this.running = running;
        }
        
        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                
                try {
                    canvas = surfaceHolder.lockCanvas();
                    
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            update();
                            draw(canvas);
                        }
                    }
                    
                    Thread.sleep(16); // ~60 FPS
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {}
                    }
                }
            }
        }
        
        void update() {
            // Player movement
            if (touchX > 0) {
                if (touchX < screenWidth / 3) {
                    playerVelX = -8;
                } else if (touchX > screenWidth * 2 / 3) {
                    playerVelX = 8;
                } else {
                    playerVelX = 0;
                }
            }
            
            // Apply gravity
            playerVelY += 0.8f;
            
            // Update player position
            playerX += playerVelX;
            playerY += playerVelY;
            
            // Ground collision
            if (playerY > groundY - playerHeight) {
                playerY = groundY - playerHeight;
                playerVelY = 0;
                isJumping = false;
            }
            
            // Screen bounds
            if (playerX < playerWidth / 2) playerX = playerWidth / 2;
            if (playerX > screenWidth - playerWidth / 2) playerX = screenWidth - playerWidth / 2;
            
            // Update enemies
            for (int i = 0; i < 5; i++) {
                enemyX[i] += enemyVelX[i];
                if (enemyX[i] < 30 || enemyX[i] > screenWidth - 30) {
                    enemyVelX[i] *= -1;
                }
                
                // Collision with enemy
                if (Math.abs(playerX - enemyX[i]) < 40 && Math.abs(playerY - (groundY - 40)) < 40) {
                    lives--;
                    playerX = screenWidth / 2f;
                    playerY = groundY - playerHeight;
                    playerVelX = 0;
                    playerVelY = 0;
                }
            }
            
            // Check coin collection
            for (int i = 0; i < 5; i++) {
                if (coinActive[i]) {
                    float dx = Math.abs(playerX - coinX[i]);
                    float dy = Math.abs(playerY - coinY[i]);
                    if (dx < 35 && dy < 35) {
                        coinActive[i] = false;
                        coinCount++;
                    }
                }
            }
            
            // Reset coins when all collected
            if (coinCount >= 5) {
                coinCount = 0;
                for (int i = 0; i < 5; i++) {
                    coinActive[i] = true;
                    coinX[i] = 80 + i * 140;
                    coinY[i] = groundY - 120 - (i % 3) * 50;
                }
            }
            
            // Game over
            if (lives <= 0) {
                lives = 3;
                playerX = screenWidth / 2f;
                playerY = groundY - playerHeight;
            }
        }
        
        void draw(Canvas canvas) {
            // Background
            canvas.drawColor(Color.parseColor("#1a1a2e"));
            
            // Ground
            canvas.drawRect(0, groundY, screenWidth, screenHeight, groundPaint);
            
            // Coins
            for (int i = 0; i < 5; i++) {
                if (coinActive[i]) {
                    canvas.drawCircle(coinX[i], coinY[i], 15, coinPaint);
                }
            }
            
            // Enemies
            for (int i = 0; i < 5; i++) {
                canvas.drawRect(enemyX[i] - 25, groundY - 50, enemyX[i] + 25, groundY, enemyPaint);
            }
            
            // Player
            canvas.drawRect(playerX - playerWidth/2, playerY, playerX + playerWidth/2, playerY + playerHeight, playerPaint);
            
            // HUD
            canvas.drawText("Moedas: " + coinCount + "/5", 20, 50, textPaint);
            canvas.drawText("Vidas: " + lives, screenWidth - 150, 50, textPaint);
            
            // Controls hint
            textPaint.setTextSize(24);
            canvas.drawText("< ESQ | DIR >  |  TOQUE PRA PULAR", screenWidth/2 - 150, screenHeight - 20, textPaint);
            textPaint.setTextSize(40);
        }
    }
}
