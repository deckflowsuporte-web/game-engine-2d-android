package com.deckflow.editor;

import android.content.*;
import android.util.*;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import com.deckflow.engine.*;

/**
 * Toolbar - Top toolbar with tools and actions
 */
public class Toolbar extends LinearLayout {
    
    private EditorView editorView;
    private OnToolSelectedListener listener;
    
    public interface OnToolSelectedListener {
        void onToolSelected(EditorView.ToolMode tool);
        void onAction(String action);
    }
    
    public Toolbar(Context ctx) {
        super(ctx);
        init();
    }
    
    public Toolbar(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }
    
    private void init() {
        setOrientation(HORIZONTAL);
        setBackgroundColor(0xFF2A2A4A);
        setPadding(16, 8, 16, 8);
        
        // Logo/Title
        TextView logo = new TextView(getContext());
        logo.setText("🎮 DeckFlow");
        logo.setTextSize(18);
        logo.setTextColor(0xFF3498DB);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        addView(logo);
        
        addSeparator();
        
        // Tools
        addToolButton("🔲", "Select", EditorView.ToolMode.SELECT);
        addToolButton("✋", "Move", EditorView.ToolMode.MOVE);
        addToolButton("🔄", "Rotate", EditorView.ToolMode.ROTATE);
        addToolButton("📐", "Scale", EditorView.ToolMode.SCALE);
        addToolButton("➕", "Add", EditorView.ToolMode.ADD);
        
        addSeparator();
        
        // Actions
        addActionButton("▶", "Play");
        addActionButton("⏸", "Pause");
        addActionButton("📷", "Screenshot");
        
        addSeparator();
        
        // Grid toggle
        CheckBox gridCheck = new CheckBox(getContext());
        gridCheck.setText("Grid");
        gridCheck.setTextColor(Color.WHITE);
        gridCheck.setChecked(true);
        gridCheck.setOnCheckedChangeListener((b, checked) -> {
            if (editorView != null) {
                editorView.setShowGrid(checked);
            }
        });
        addView(gridCheck);
        
        // Snap toggle
        CheckBox snapCheck = new CheckBox(getContext());
        snapCheck.setText("Snap");
        snapCheck.setTextColor(Color.WHITE);
        snapCheck.setChecked(true);
        addView(snapCheck);
    }
    
    private void addToolButton(String icon, String tooltip, EditorView.ToolMode mode) {
        Button btn = new Button(getContext());
        btn.setText(icon);
        btn.setTextSize(20);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setPadding(16, 8, 16, 8);
        btn.setOnClickListener(v -> {
            if (listener != null) listener.onToolSelected(mode);
        });
        
        // Tooltip via long press
        final String tip = tooltip;
        btn.setOnLongClickListener(v -> {
            Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
            return true;
        });
        
        addView(btn);
    }
    
    private void addActionButton(String icon, String action) {
        Button btn = new Button(getContext());
        btn.setText(icon);
        btn.setTextSize(20);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setPadding(16, 8, 16, 8);
        btn.setOnClickListener(v -> {
            if (listener != null) listener.onAction(action);
        });
        addView(btn);
    }
    
    private void addSeparator() {
        View sep = new View(getContext());
        sep.setBackgroundColor(0xFF444466);
        sep.setLayoutParams(new LayoutParams(2, 40));
        sep.setPadding(8, 0, 8, 0);
        addView(sep);
    }
    
    public void setEditorView(EditorView view) {
        this.editorView = view;
    }
    
    public void setOnToolSelectedListener(OnToolSelectedListener l) {
        this.listener = l;
    }
}
