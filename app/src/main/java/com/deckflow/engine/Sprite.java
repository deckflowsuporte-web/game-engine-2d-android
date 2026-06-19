package com.deckflow.engine;

import android.graphics.*;
import java.io.*;

/**
 * Sprite component - renders a texture
 */
public class Sprite extends Node {
    private static final long serialVersionUID = 1L;
    
    public Bitmap texture;
    public String texturePath;
    public Rect2 region = new Rect2(0, 0, 0, 0);
    public Vector2 offset = new Vector2();
    public boolean flipH = false;
    public boolean flipV = false;
    public float modulateR = 1, modulateG = 1, modulateB = 1, modulateA = 1;
    public int frame = 0;
    
    // Animation
    public boolean animated = false;
    public int[] frames;
    public float animSpeed = 10;
    public boolean animPlaying = false;
    
    @Override
    public void _ready() {
        super._ready();
    }
    
    @Override
    public void _process(float delta) {
        super._process(delta);
        
        if (animated && animPlaying && frames != null) {
            frame = (int)((frame + animSpeed * delta) % frames.length);
        }
    }
    
    public void play() { animPlaying = true; }
    public void stop() { animPlaying = false; }
    public void setFrame(int f) { frame = f % (frames != null ? frames.length : 1); }
    
    public void setTexture(Bitmap bmp) {
        texture = bmp;
        if (bmp != null && region.width == 0) {
            region.width = bmp.getWidth();
            region.height = bmp.getHeight();
        }
    }
    
    public Rect2 getRect() {
        return new Rect2(position.x - offset.x, position.y - offset.y, 
                       (texture != null ? texture.getWidth() : 64) * scale.x,
                       (texture != null ? texture.getHeight() : 64) * scale.y);
    }
}
