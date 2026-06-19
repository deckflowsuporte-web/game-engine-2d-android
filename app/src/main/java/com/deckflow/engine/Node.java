package com.deckflow.engine;

import java.util.*;
import java.io.*;

/**
 * DeckFlow Engine - 2D Game Engine for Android
 * Base class for all scene objects
 */
public class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String name = "Node";
    public String id = UUID.randomUUID().toString();
    public Vector2 position = new Vector2();
    public Vector2 scale = new Vector2(1, 1);
    public float rotation = 0;
    public boolean visible = true;
    public boolean enabled = true;
    public int zIndex = 0;
    
    protected Node parent;
    protected ArrayList<Node> children = new ArrayList<>();
    
    // Lifecycle
    public void _ready() {}
    public void _process(float delta) {}
    public void _physicsProcess(float delta) {}
    public void _input(Input event) {}
    
    // Tree operations
    public void addChild(Node child) {
        child.parent = this;
        children.add(child);
        child._ready();
    }
    
    public void removeChild(Node child) {
        children.remove(child);
        child.parent = null;
    }
    
    public ArrayList<Node> getChildren() {
        return children;
    }
    
    public Node getParent() {
        return parent;
    }
    
    public Node findChild(String name) {
        for (Node child : children) {
            if (child.name.equals(name)) return child;
            Node found = child.findChild(name);
            if (found != null) return found;
        }
        return null;
    }
    
    // Transform
    public Vector2 getGlobalPosition() {
        if (parent == null) return position.clone();
        Vector2 parentPos = parent.getGlobalPosition();
        return new Vector2(parentPos.x + position.x, parentPos.y + position.y);
    }
    
    public float getGlobalRotation() {
        if (parent == null) return rotation;
        return parent.getGlobalRotation() + rotation;
    }
    
    public void queueFree() {
        if (parent != null) {
            parent.removeChild(this);
        }
    }
    
    // Serialize
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"").append(getClass().getSimpleName()).append("\",");
        sb.append("\"name\":\"").append(name).append("\",");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"position\":").append(position.toJSON()).append(",");
        sb.append("\"scale\":").append(scale.toJSON()).append(",");
        sb.append("\"rotation\":").append(rotation).append(",");
        sb.append("\"visible\":").append(visible).append(",");
        sb.append("\"enabled\":").append(enabled).append(",");
        sb.append("\"zIndex\":").append(zIndex);
        sb.append("}");
        return sb.toString();
    }
    
    public void fromJSON(String json) {
        // Parse JSON and set properties
    }
}
