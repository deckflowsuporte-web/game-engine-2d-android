package com.deckflow.editor;

import android.content.*;
import android.util.*;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import com.deckflow.engine.*;
import java.util.*;

/**
 * SceneTree - Shows the node hierarchy
 */
public class SceneTree extends LinearLayout {
    
    private Scene currentScene;
    private Node selectedNode;
    private ArrayList<NodeView> nodeViews = new ArrayList<>();
    
    private ScrollView scrollView;
    private LinearLayout nodeContainer;
    
    private OnNodeSelectedListener nodeSelectedListener;
    private OnNodeChangedListener nodeChangedListener;
    
    public interface OnNodeSelectedListener {
        void onNodeSelected(Node node);
    }
    
    public interface OnNodeChangedListener {
        void onNodeChanged();
    }
    
    public SceneTree(Context ctx) {
        super(ctx);
        init();
    }
    
    public SceneTree(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }
    
    private void init() {
        setOrientation(VERTICAL);
        setBackgroundColor(0xFF1E1E2E);
        
        // Header
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setPadding(16, 16, 16, 16);
        header.setBackgroundColor(0xFF2A2A4A);
        
        TextView title = new TextView(getContext());
        title.setText("SCENE");
        title.setTextSize(14);
        title.setTextColor(0xFF888888);
        title.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        
        Button addBtn = new Button(getContext());
        addBtn.setText("+");
        addBtn.setTextSize(18);
        addBtn.setOnClickListener(v -> addRootNode());
        
        header.addView(title);
        header.addView(addBtn);
        addView(header);
        
        // Node list
        scrollView = new ScrollView(getContext());
        nodeContainer = new LinearLayout(getContext());
        nodeContainer.setOrientation(VERTICAL);
        scrollView.addView(nodeContainer);
        addView(scrollView);
    }
    
    public void setScene(Scene scene) {
        this.currentScene = scene;
        refresh();
    }
    
    public void refresh() {
        nodeContainer.removeAllViews();
        nodeViews.clear();
        
        if (currentScene == null) return;
        
        // Add root nodes
        for (Node node : currentScene.getChildren()) {
            addNodeView(null, node, 0);
        }
        
        // Add "Add Node" button
        Button addBtn = new Button(getContext());
        addBtn.setText("+ Add Root Node");
        addBtn.setOnClickListener(v -> addRootNode());
        nodeContainer.addView(addBtn);
    }
    
    private void addNodeView(Node parent, Node node, int depth) {
        NodeView view = new NodeView(getContext(), node, depth);
        view.setOnClickListener(v -> {
            selectedNode = node;
            refresh();
            if (nodeSelectedListener != null) nodeSelectedListener.onNodeSelected(node);
        });
        
        // Indent
        view.setPadding(16 + depth * 24, 8, 16, 8);
        
        // Selection state
        if (node == selectedNode) {
            view.setBackgroundColor(0xFF3498DB);
        }
        
        nodeContainer.addView(view);
        nodeViews.add(view);
        
        // Add children
        for (Node child : node.getChildren()) {
            addNodeView(node, child, depth + 1);
        }
    }
    
    private void addRootNode() {
        if (currentScene == null) return;
        
        Node newNode = new Node();
        newNode.name = "Node_" + (currentScene.getChildren().size() + 1);
        newNode.position = new Vector2(200, 200);
        currentScene.addChild(newNode);
        
        refresh();
        
        if (nodeChangedListener != null) nodeChangedListener.onNodeChanged();
    }
    
    public void setOnNodeSelectedListener(OnNodeSelectedListener l) {
        this.nodeSelectedListener = l;
    }
    
    public void setOnNodeChangedListener(OnNodeChangedListener l) {
        this.nodeChangedListener = l;
    }
    
    // Inner class for node views
    private class NodeView extends LinearLayout {
        private Node node;
        
        NodeView(Context ctx, Node node, int depth) {
            super(ctx);
            this.node = node;
            init();
        }
        
        private void init() {
            setOrientation(HORIZONTAL);
            
            // Icon based on type
            TextView icon = new TextView(getContext());
            icon.setText(getNodeIcon(node));
            icon.setTextSize(16);
            icon.setTextColor(0xFF27AE60);
            icon.setMinWidth(30);
            
            // Name
            TextView name = new TextView(getContext());
            name.setText(node.name);
            name.setTextSize(14);
            name.setTextColor(Color.WHITE);
            name.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            
            // Child count
            TextView count = new TextView(getContext());
            int childCount = node.getChildren().size();
            if (childCount > 0) {
                count.setText(" (" + childCount + ")");
                count.setTextSize(12);
                count.setTextColor(0xFF888888);
            }
            
            addView(icon);
            addView(name);
            addView(count);
        }
        
        private String getNodeIcon(Node n) {
            if (n instanceof Sprite) return "🖼";
            if (n instanceof RigidBody2D) return "⚡";
            if (n instanceof CollisionShape2D) return "◻";
            if (n instanceof Scene) return "📁";
            return "📦";
        }
    }
}
