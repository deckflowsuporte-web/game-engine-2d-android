package com.deckflow.engine;

import java.io.*;

public class Rect2 implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public float x, y, width, height;
    
    public Rect2() { x = y = width = height = 0; }
    public Rect2(float x, float y, float w, float h) { this.x = x; this.y = y; this.width = w; this.height = h; }
    
    public float left() { return x; }
    public float right() { return x + width; }
    public float top() { return y; }
    public float bottom() { return y + height; }
    
    public Vector2 center() { return new Vector2(x + width/2, y + height/2); }
    
    public boolean intersects(Rect2 other) {
        return !(right() < other.left() || other.right() < left() ||
                 bottom() < other.top() || other.bottom() < top());
    }
    
    public boolean contains(float px, float py) {
        return px >= left() && px <= right() && py >= top() && py <= bottom();
    }
    
    public boolean contains(Vector2 p) { return contains(p.x, p.y); }
    
    public Rect2 intersection(Rect2 other) {
        if (!intersects(other)) return new Rect2();
        float nx = Math.max(left(), other.left());
        float ny = Math.max(top(), other.top());
        float nw = Math.min(right(), other.right()) - nx;
        float nh = Math.min(bottom(), other.bottom()) - ny;
        return new Rect2(nx, ny, nw, nh);
    }
    
    public String toJSON() {
        return "{\"x\":" + x + ",\"y\":" + y + ",\"w\":" + width + ",\"h\":" + height + "}";
    }
}
