package com.deckflow.engine;

import java.util.*;
import java.io.*;

/**
 * Scene - Container for all nodes, like Godot scenes
 */
public class Scene extends Node {
    private static final long serialVersionUID = 1L;
    
    public String scenePath;
    public String sceneName = "Scene";
    public Camera2D currentCamera;
    
    protected ArrayList<Node> rootNodes = new ArrayList<>();
    protected ArrayList<Node> toAdd = new ArrayList<>();
    protected ArrayList<Node> toRemove = new ArrayList<>();
    
    @Override
    public void addChild(Node child) {
        rootNodes.add(child);
        child.parent = this;
        child._ready();
    }
    
    @Override
    public void removeChild(Node child) {
        rootNodes.remove(child);
        toRemove.add(child);
    }
    
    @Override
    public ArrayList<Node> getChildren() {
        return rootNodes;
    }
    
    public void _processAll(float delta) {
        // Add queued nodes
        for (Node n : toAdd) {
            rootNodes.add(n);
        }
        toAdd.clear();
        
        // Process all nodes
        processNode(this, delta);
        
        // Remove queued nodes
        for (Node n : toRemove) {
            rootNodes.remove(n);
        }
        toRemove.clear();
    }
    
    private void processNode(Node node, float delta) {
        if (node.enabled) {
            node._process(delta);
        }
        
        for (Node child : node.getChildren()) {
            processNode(child, delta);
        }
    }
    
    public void _physicsProcessAll(float delta) {
        for (Node node : rootNodes) {
            processPhysicsNode(node, delta);
        }
    }
    
    private void processPhysicsNode(Node node, float delta) {
        if (node.enabled && node instanceof RigidBody2D) {
            ((RigidBody2D)node)._physicsProcess(delta);
        }
        
        for (Node child : node.getChildren()) {
            processPhysicsNode(child, delta);
        }
    }
    
    public ArrayList<Node> getNodesByClass(Class<?> cls) {
        ArrayList<Node> result = new ArrayList<>();
        collectNodesByClass(this, cls, result);
        return result;
    }
    
    private void collectNodesByClass(Node node, Class<?> cls, ArrayList<Node> result) {
        if (cls.isInstance(node)) {
            result.add(node);
        }
        for (Node child : node.getChildren()) {
            collectNodesByClass(child, cls, result);
        }
    }
    
    public Node instantiate(String resourcePath) {
        // Load node from resource
        return new Node();
    }
    
    // Save scene to JSON
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"sceneName\":\"").append(sceneName).append("\",");
        sb.append("\"nodes\":[");
        
        for (int i = 0; i < rootNodes.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(rootNodes.get(i).toJSON());
        }
        
        sb.append("]}");
        return sb.toString();
    }
    
    // Load scene from JSON
    public void fromJSON(String json) {
        // Parse and create nodes
    }
}
