package com.deckflow.engine;

import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

/**
 * DeckFlow Engine - The Core
 * Handles game loop, rendering, and scene management
 */
public class Engine {
    private static Engine instance;
    
    public static Engine getInstance() {
        if (instance == null) instance = new Engine();
        return instance;
    }
    
    // Configuration
    public boolean debugMode = true;
    public boolean pauseWhenUnfocused = true;
    public int targetFPS = 60;
    public Vector2 viewportSize = new Vector2(1920, 1080);
    
    // State
    private boolean running = false;
    private boolean paused = false;
    private Scene currentScene;
    private Scene nextScene;
    private float deltaTime = 0;
    private float timeScale = 1;
    private long lastTime;
    
    // Physics
    private float physicsDelta = 1f/60f;
    private float physicsAccumulator = 0;
    
    // Debug
    private boolean showFPS = true;
    private boolean showCollisionShapes = false;
    private float fps = 0;
    private int drawCalls = 0;
    
    // Callbacks
    public interface OnInitCallback { void onInit(); }
    public interface OnReadyCallback { void onReady(Scene scene); }
    public interface OnProcessCallback { void onProcess(float delta); }
    
    public OnInitCallback onInit;
    public OnReadyCallback onReady;
    public OnProcessCallback onProcess;
    
    private Engine() {}
    
    public void init() {
        if (debugMode) android.util.Log.i("DeckFlow", "=== DECKFLOW ENGINE 2D ===");
        if (debugMode) android.util.Log.i("DeckFlow", "Professional 2D Engine for Android");
        if (onInit != null) onInit.onInit();
    }
    
    public void start() {
        running = true;
        lastTime = System.nanoTime();
        if (debugMode) android.util.Log.i("DeckFlow", "Engine started");
    }
    
    public void stop() {
        running = false;
        if (debugMode) android.util.Log.i("DeckFlow", "Engine stopped");
    }
    
    public void _process() {
        if (!running || paused) return;
        
        // Calculate delta time
        long now = System.nanoTime();
        deltaTime = (now - lastTime) / 1000000000f;
        lastTime = now;
        
        // Cap delta time to prevent spiral of death
        if (deltaTime > 0.25f) deltaTime = 0.25f;
        
        // Apply time scale
        deltaTime *= timeScale;
        
        // Calculate FPS
        fps = 1f / deltaTime;
        
        // Physics
        physicsAccumulator += deltaTime;
        while (physicsAccumulator >= physicsDelta) {
            if (currentScene != null) {
                currentScene._physicsProcessAll(physicsDelta);
            }
            physicsAccumulator -= physicsDelta;
        }
        
        // Update scene
        if (currentScene != null) {
            currentScene._processAll(deltaTime);
        }
        
        // Process callbacks
        if (onProcess != null) {
            onProcess.onProcess(deltaTime);
        }
        
        // Process input
        Input._process();
        
        // Change scene if requested
        if (nextScene != null) {
            currentScene = nextScene;
            nextScene = null;
            if (onReady != null) onReady.onReady(currentScene);
        }
    }
    
    public void render(Canvas canvas) {
        if (currentScene == null) return;
        
        drawCalls = 0;
        
        // Get all sprites and render
        ArrayList<Sprite> sprites = new ArrayList<>();
        for (Node n : currentScene.getNodesByClass(Sprite.class)) {
            sprites.add((Sprite)n);
        }
        
        // Sort by zIndex
        Collections.sort(sprites, (a, b) -> Integer.compare(a.zIndex, b.zIndex));
        
        for (Sprite sprite : sprites) {
            if (sprite.visible) {
                renderSprite(canvas, sprite);
                drawCalls++;
            }
        }
        
        // Debug collision shapes
        if (showCollisionShapes) {
            ArrayList<CollisionShape2D> shapes = new ArrayList<>();
            for (Node n : currentScene.getNodesByClass(CollisionShape2D.class)) {
                shapes.add((CollisionShape2D)n);
            }
            renderCollisionShapes(canvas, shapes);
        }
        
        // Debug info
        if (debugMode && showFPS) {
            renderDebugInfo(canvas);
        }
    }
    
    private void renderSprite(Canvas canvas, Sprite sprite) {
        Vector2 pos = sprite.getGlobalPosition();
        int w = (int)(64 * sprite.scale.x);
        int h = (int)(64 * sprite.scale.y);
        
        Paint paint = new Paint();
        paint.setColor(Color.argb(
            (int)(255 * sprite.modulateA),
            (int)(255 * sprite.modulateR),
            (int)(255 * sprite.modulateG),
            (int)(255 * sprite.modulateB)
        ));
        paint.setStyle(Paint.Style.FILL);
        
        if (sprite.texture != null) {
            Rect src = new Rect(0, 0, sprite.texture.getWidth(), sprite.texture.getHeight());
            Rect dst = new Rect((int)pos.x - w/2, (int)pos.y - h/2, (int)pos.x + w/2, (int)pos.y + h/2);
            canvas.drawBitmap(sprite.texture, src, dst, paint);
        } else {
            // Draw colored rectangle if no texture
            canvas.drawRect(pos.x - w/2, pos.y - h/2, pos.x + w/2, pos.y + h/2, paint);
        }
    }
    
    private void renderCollisionShapes(Canvas canvas, ArrayList<CollisionShape2D> shapes) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        
        for (CollisionShape2D shape : shapes) {
            Rect2 bb = shape.boundingBox;
            canvas.drawRect(bb.x, bb.y, bb.x + bb.width, bb.y + bb.height, paint);
        }
    }
    
    private void renderDebugInfo(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setAntiAlias(true);
        
        int y = 40;
        canvas.drawText("FPS: " + (int)fps, 20, y, textPaint); y += 35;
        canvas.drawText("Draw Calls: " + drawCalls, 20, y, textPaint); y += 35;
        canvas.drawText("Nodes: " + countNodes(), 20, y, textPaint); y += 35;
        canvas.drawText("DECKFLOW ENGINE v1.0", 20, y, textPaint);
    }
    
    private int countNodes() {
        if (currentScene == null) return 0;
        int count = 0;
        for (Node n : currentScene.getChildren()) {
            count += countNodeRecursive(n);
        }
        return count;
    }
    
    private int countNodeRecursive(Node n) {
        int count = 1;
        for (Node c : n.getChildren()) {
            count += countNodeRecursive(c);
        }
        return count;
    }
    
    // Scene management
    public void loadScene(Scene scene) {
        nextScene = scene;
    }
    
    public Scene getCurrentScene() {
        return currentScene;
    }
    
    // Getters
    public float getDelta() { return deltaTime; }
    public float getFPS() { return fps; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    
    // Setters
    public void setPaused(boolean p) { paused = p; }
    public void setTimeScale(float s) { timeScale = s; }
    public void setDebugMode(boolean d) { debugMode = d; }
    public void setShowCollisionShapes(boolean s) { showCollisionShapes = s; }
}
