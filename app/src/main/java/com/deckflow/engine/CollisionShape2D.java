package com.deckflow.engine;

import java.io.*;

/**
 * CollisionShape2D - Base collision shape
 */
public class CollisionShape2D extends Node {
    private static final long serialVersionUID = 1L;
    
    public ShapeType shapeType = ShapeType.RECTANGLE;
    public Rect2 boundingBox = new Rect2();
    public float radius = 32;
    public boolean disabled = false;
    public int collisionLayer = 1;
    public int collisionMask = 1;
    
    public enum ShapeType {
        RECTANGLE,
        CIRCLE,
        LINE,
        POINT
    }
    
    @Override
    public void _ready() {
        super._ready();
        updateBoundingBox();
    }
    
    public void updateBoundingBox() {
        Vector2 globalPos = getGlobalPosition();
        
        switch(shapeType) {
            case RECTANGLE:
                boundingBox = new Rect2(
                    globalPos.x - 32, 
                    globalPos.y - 32, 
                    64, 64
                );
                break;
            case CIRCLE:
                boundingBox = new Rect2(
                    globalPos.x - radius,
                    globalPos.y - radius,
                    radius * 2,
                    radius * 2
                );
                break;
        }
    }
    
    public boolean collidesWith(CollisionShape2D other) {
        if (disabled || other.disabled) return false;
        
        updateBoundingBox();
        other.updateBoundingBox();
        
        return boundingBox.intersects(other.boundingBox);
    }
}
