package com.deckflow.editor;

import android.graphics.*;
import android.view.*;
import com.deckflow.engine.*;
import java.util.*;

/**
 * EditorView - The main editing canvas where you manipulate scenes
 */
public class EditorView extends View {
    
    // Scene being edited
    private Scene editingScene;
    private Node selectedNode;
    
    // Editor state
    private ToolMode toolMode = ToolMode.SELECT;
    private Vector2 panOffset = new Vector2(0, 0);
    private float zoom = 1.0f;
    
    // Grid
    private boolean showGrid = true;
    private int gridSize = 32;
    
    // Selection
    private Rect2 selectionRect;
    private boolean isSelecting = false;
    private Vector2 selectionStart = new Vector2();
    
    // Dragging
    private boolean isDragging = false;
    private Vector2 dragStart = new Vector2();
    private Vector2 nodeStartPos = new Vector2();
    
    // Snap
    private boolean snapToGrid = true;
    
    public enum ToolMode {
        SELECT,
        MOVE,
        ROTATE,
        SCALE,
        ADD
    }
    
    // Colors
    private static final int COLOR_BG = 0xFF1A1A2E;
    private static final int COLOR_GRID = 0xFF2A2A4E;
    private static final int COLOR_SELECT = 0xFF3498DB;
    private static final int COLOR_NODE = 0xFF27AE60;
    private static final int COLOR_NODE_HOVER = 0xFF2ECC71;
    
    public EditorView(Context ctx) {
        super(ctx);
        setBackgroundColor(COLOR_BG);
    }
    
    public void setScene(Scene scene) {
        this.editingScene = scene;
        invalidate();
    }
    
    public Scene getScene() { return editingScene; }
    
    public void setSelectedNode(Node node) {
        this.selectedNode = node;
        invalidate();
    }
    
    public Node getSelectedNode() { return selectedNode; }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Apply transform
        canvas.save();
        canvas.translate(panOffset.x, panOffset.y);
        canvas.scale(zoom, zoom);
        
        // Draw grid
        if (showGrid) {
            drawGrid(canvas);
        }
        
        // Draw scene nodes
        if (editingScene != null) {
            drawNodes(canvas, editingScene);
        }
        
        // Draw selection
        if (selectedNode != null) {
            drawSelection(canvas, selectedNode);
        }
        
        canvas.restore();
        
        // Draw toolbar overlay
        drawToolbar(canvas);
    }
    
    private void drawGrid(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(COLOR_GRID);
        gridPaint.setStrokeWidth(1);
        
        int w = (int)(getWidth() / zoom) + gridSize;
        int h = (int)(getHeight() / zoom) + gridSize;
        int ox = (int)(-panOffset.x / zoom) % gridSize;
        int oy = (int)(-panOffset.y / zoom) % gridSize;
        
        for (int x = -ox; x < w; x += gridSize) {
            canvas.drawLine(x, 0, x, h, gridPaint);
        }
        for (int y = -oy; y < h; y += gridSize) {
            canvas.drawLine(0, y, w, y, gridPaint);
        }
    }
    
    private void drawNodes(Canvas canvas, Node node) {
        for (Node child : node.getChildren()) {
            drawNode(canvas, child);
            drawNodes(canvas, child);
        }
    }
    
    private void drawNode(Canvas canvas, Node node) {
        Vector2 pos = node.position;
        int size = 48;
        
        Paint fillPaint = new Paint();
        fillPaint.setColor(node == selectedNode ? COLOR_SELECT : COLOR_NODE);
        fillPaint.setStyle(Paint.Style.FILL);
        
        Paint strokePaint = new Paint();
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);
        
        // Draw node rectangle
        Rect rect = new Rect(
            (int)(pos.x - size/2),
            (int)(pos.y - size/2),
            (int)(pos.x + size/2),
            (int)(pos.y + size/2)
        );
        canvas.drawRect(rect, fillPaint);
        canvas.drawRect(rect, strokePaint);
        
        // Draw node name
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(node.name, pos.x, pos.y + 8, textPaint);
        
        // Draw children connections
        for (Node child : node.getChildren()) {
            Paint linePaint = new Paint();
            linePaint.setColor(0x88FFFFFF);
            linePaint.setStrokeWidth(2);
            canvas.drawLine(pos.x, pos.y + size/2, child.position.x, child.position.y - size/2, linePaint);
        }
    }
    
    private void drawSelection(Canvas canvas, Node node) {
        Vector2 pos = node.getGlobalPosition();
        int size = 56;
        
        Paint handlePaint = new Paint();
        handlePaint.setColor(COLOR_SELECT);
        handlePaint.setStyle(Paint.Style.STROKE);
        handlePaint.setStrokeWidth(3);
        
        Rect rect = new Rect(
            (int)(pos.x - size/2) - 4,
            (int)(pos.y - size/2) - 4,
            (int)(pos.x + size/2) + 4,
            (int)(pos.y + size/2) + 4
        );
        canvas.drawRect(rect, handlePaint);
        
        // Draw corner handles
        handlePaint.setStyle(Paint.Style.FILL);
        int handleSize = 10;
        canvas.drawRect(rect.left - handleSize/2, rect.top - handleSize/2, rect.left + handleSize/2, rect.top + handleSize/2, handlePaint);
        canvas.drawRect(rect.right - handleSize/2, rect.top - handleSize/2, rect.right + handleSize/2, rect.top + handleSize/2, handlePaint);
        canvas.drawRect(rect.left - handleSize/2, rect.bottom - handleSize/2, rect.left + handleSize/2, rect.bottom + handleSize/2, handlePaint);
        canvas.drawRect(rect.right - handleSize/2, rect.bottom - handleSize/2, rect.right + handleSize/2, rect.bottom + handleSize/2, handlePaint);
    }
    
    private void drawToolbar(Canvas canvas) {
        // Draw tool indicator at bottom
        Paint bgPaint = new Paint();
        bgPaint.setColor(0xCC000000);
        
        int toolbarH = 60;
        canvas.drawRect(0, getHeight() - toolbarH, getWidth(), getHeight(), bgPaint);
        
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28);
        textPaint.setAntiAlias(true);
        
        String toolName = toolMode.name();
        canvas.drawText("Tool: " + toolName + " | Zoom: " + (int)(zoom * 100) + "%", 20, getHeight() - 25, textPaint);
        
        // Draw add button hint
        textPaint.setColor(0xFF3498DB);
        canvas.drawText("+ Add Node", getWidth() - 180, getHeight() - 25, textPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        // Transform touch to world coordinates
        float worldX = (x - panOffset.x) / zoom;
        float worldY = (y - panOffset.y) / zoom;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleTouchDown(worldX, worldY, (int)event.getPointerId(0));
                
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    // Two finger pan
                    handleTwoFingerPan(event);
                } else {
                    handleDrag(worldX, worldY);
                }
                break;
                
            case MotionEvent.ACTION_UP:
                handleTouchUp(worldX, worldY);
                break;
        }
        
        invalidate();
        return true;
    }
    
    private boolean handleTouchDown(float x, float y, int pointerId) {
        // Check if clicking on a node
        Node clicked = getNodeAtPosition(x, y);
        
        if (toolMode == ToolMode.ADD) {
            // Add new node at position
            if (editingScene != null) {
                Node newNode = new Node();
                newNode.name = "Node_" + System.currentTimeMillis();
                newNode.position = new Vector2(snapToGrid ? snap(x) : x, snapToGrid ? snap(y) : y);
                editingScene.addChild(newNode);
                setSelectedNode(newNode);
                toolMode = ToolMode.SELECT;
            }
            return true;
        }
        
        if (clicked != null) {
            setSelectedNode(clicked);
            isDragging = true;
            dragStart = new Vector2(x, y);
            nodeStartPos = clicked.position.clone();
        } else {
            setSelectedNode(null);
        }
        
        return true;
    }
    
    private void handleDrag(float x, float y) {
        if (isDragging && selectedNode != null && toolMode == ToolMode.MOVE) {
            float dx = x - dragStart.x;
            float dy = y - dragStart.y;
            selectedNode.position.x = snapToGrid ? snap(nodeStartPos.x + dx) : nodeStartPos.x + dx;
            selectedNode.position.y = snapToGrid ? snap(nodeStartPos.y + dy) : nodeStartPos.y + dy;
        }
    }
    
    private void handleTouchUp(float x, float y) {
        isDragging = false;
        isSelecting = false;
    }
    
    private void handleTwoFingerPan(MotionEvent event) {
        // Implement pinch zoom and two finger pan
    }
    
    private Node getNodeAtPosition(float x, float y) {
        if (editingScene == null) return null;
        
        ArrayList<Node> allNodes = editingScene.getNodesByClass(Node.class);
        
        // Check in reverse order (top nodes first)
        for (int i = allNodes.size() - 1; i >= 0; i--) {
            Node node = allNodes.get(i);
            Vector2 pos = node.position;
            
            if (x >= pos.x - 30 && x <= pos.x + 30 &&
                y >= pos.y - 30 && y <= pos.y + 30) {
                return node;
            }
        }
        return null;
    }
    
    private float snap(float value) {
        return Math.round(value / gridSize) * gridSize;
    }
    
    // Tool controls
    public void setToolMode(ToolMode mode) {
        this.toolMode = mode;
        invalidate();
    }
    
    public ToolMode getToolMode() { return toolMode; }
    
    public void zoomIn() {
        zoom = Math.min(4, zoom * 1.2f);
        invalidate();
    }
    
    public void zoomOut() {
        zoom = Math.max(0.25f, zoom / 1.2f);
        invalidate();
    }
    
    public void resetView() {
        zoom = 1;
        panOffset = new Vector2(0, 0);
        invalidate();
    }
    
    public void centerOnSelected() {
        if (selectedNode != null) {
            panOffset.x = getWidth()/2 - selectedNode.position.x * zoom;
            panOffset.y = getHeight()/2 - selectedNode.position.y * zoom;
            invalidate();
        }
    }
}
