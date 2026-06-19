package com.deckflow.engine;

import java.util.*;

/**
 * Input - Handles touch and key input
 */
public class Input {
    private static ArrayList<Touch> touches = new ArrayList<>();
    private static HashSet<Integer> keysPressed = new HashSet<>();
    private static HashSet<Integer> keysJustPressed = new HashSet<>();
    private static HashSet<Integer> keysJustReleased = new HashSet<>();
    
    public static int MOUSE_BUTTON_LEFT = 0;
    public static int MOUSE_BUTTON_RIGHT = 1;
    
    public static int KEY_ESCAPE = 1;
    public static int KEY_ENTER = 2;
    public static int KEY_SPACE = 3;
    public static int KEY_LEFT = 4;
    public static int KEY_RIGHT = 5;
    public static int KEY_UP = 6;
    public static int KEY_DOWN = 7;
    
    public static class Touch {
        public int id;
        public Vector2 position = new Vector2();
        public Vector2 delta = new Vector2();
        public boolean pressed;
        public boolean justPressed;
        public boolean justReleased;
        public float pressure = 1.0f;
        
        public Touch(int id, float x, float y) {
            this.id = id;
            this.position.x = x;
            this.position.y = y;
        }
    }
    
    // Process new touch event
    public static void _inputTouch(int action, int id, float x, float y) {
        Touch touch = getTouch(id);
        
        if (action == 0) { // DOWN
            if (touch == null) {
                touch = new Touch(id, x, y);
                touches.add(touch);
            }
            touch.justPressed = true;
            touch.pressed = true;
            touch.justReleased = false;
        } else if (action == 1) { // UP
            if (touch != null) {
                touch.pressed = false;
                touch.justPressed = false;
                touch.justReleased = true;
            }
        } else if (action == 2) { // MOVE
            if (touch != null) {
                touch.delta.x = x - touch.position.x;
                touch.delta.y = y - touch.position.y;
                touch.position.x = x;
                touch.position.y = y;
            }
        }
    }
    
    public static void _process() {
        // Clear just pressed/released states
        keysJustPressed.clear();
        keysJustReleased.clear();
        
        for (Touch t : touches) {
            t.justPressed = false;
            t.justReleased = false;
        }
    }
    
    public static Touch getTouch(int id) {
        for (Touch t : touches) {
            if (t.id == id) return t;
        }
        return null;
    }
    
    public static int getTouchCount() {
        int count = 0;
        for (Touch t : touches) {
            if (t.pressed) count++;
        }
        return count;
    }
    
    public static Touch getTouchByIndex(int index) {
        int count = 0;
        for (Touch t : touches) {
            if (t.pressed) {
                if (count == index) return t;
                count++;
            }
        }
        return null;
    }
    
    public static boolean isKeyPressed(int key) {
        return keysPressed.contains(key);
    }
    
    public static boolean isKeyJustPressed(int key) {
        return keysJustPressed.contains(key);
    }
    
    public static boolean isKeyJustReleased(int key) {
        return keysJustReleased.contains(key);
    }
    
    public static void keyPress(int key) {
        if (!keysPressed.contains(key)) {
            keysJustPressed.add(key);
        }
        keysPressed.add(key);
    }
    
    public static void keyRelease(int key) {
        if (keysPressed.contains(key)) {
            keysJustReleased.add(key);
        }
        keysPressed.remove(key);
    }
}
