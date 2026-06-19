package com.deckflow.engine;

import java.io.*;
import android.graphics.*;

public class Vector2 implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public float x, y;
    
    public Vector2() { x = 0; y = 0; }
    public Vector2(float x, float y) { this.x = x; this.y = y; }
    
    public Vector2 clone() { return new Vector2(x, y); }
    
    public Vector2 add(Vector2 v) { return new Vector2(x + v.x, y + v.y); }
    public Vector2 sub(Vector2 v) { return new Vector2(x - v.x, y - v.y); }
    public Vector2 mul(float s) { return new Vector2(x * s, y * s); }
    
    public float length() { return (float)Math.sqrt(x * x + y * y); }
    public float lengthSquared() { return x * x + y * y; }
    
    public Vector2 normalized() {
        float len = length();
        if (len > 0) return new Vector2(x / len, y / len);
        return new Vector2();
    }
    
    public float dot(Vector2 v) { return x * v.x + y * v.y; }
    public float cross(Vector2 v) { return x * v.y - y * v.x; }
    
    public float distanceTo(Vector2 v) {
        return sub(v).length();
    }
    
    public float angle() {
        return (float)Math.atan2(y, x);
    }
    
    public Vector2 rotated(float angle) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        return new Vector2(x * cos - y * sin, x * sin + y * cos);
    }
    
    public Point toPoint() { return new Point((int)x, (int)y); }
    
    public String toJSON() {
        return "{\"x\":" + x + ",\"y\":" + y + "}";
    }
    
    public static Vector2 lerp(Vector2 a, Vector2 b, float t) {
        return new Vector2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
    }
    
    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }
}
