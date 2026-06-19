package com.deckflow.editor;

import android.content.*;
import android.util.*;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import com.deckflow.engine.*;

/**
 * InspectorPanel - Edit properties of selected node
 */
public class InspectorPanel extends LinearLayout {
    
    private Node selectedNode;
    private EditText nameField;
    private EditText posXField, posYField;
    private EditText scaleXField, scaleYField;
    private EditText rotField;
    private EditText zIndexField;
    private CheckBox visibleCheck;
    private CheckBox enabledCheck;
    private Spinner nodeTypeSpinner;
    
    private OnPropertyChangedListener listener;
    
    public interface OnPropertyChangedListener {
        void onPropertyChanged(Node node, String property);
    }
    
    public InspectorPanel(Context ctx) {
        super(ctx);
        init();
    }
    
    public InspectorPanel(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }
    
    private void init() {
        setOrientation(VERTICAL);
        setPadding(16, 16, 16, 16);
        setBackgroundColor(0xFF1E1E2E);
        
        // Title
        TextView title = new TextView(getContext());
        title.setText("INSPECTOR");
        title.setTextSize(14);
        title.setTextColor(0xFF888888);
        title.setPadding(0, 0, 0, 16);
        addView(title);
        
        // Name field
        addLabeledField("Name:", nameField = new EditText(getContext()));
        nameField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && selectedNode != null) {
                selectedNode.name = nameField.getText().toString();
                if (listener != null) listener.onPropertyChanged(selectedNode, "name");
            }
        });
        
        // Position
        LinearLayout posLayout = new LinearLayout(getContext());
        posLayout.setOrientation(HORIZONTAL);
        addLabeledField("X:", posXField = new EditText(getContext()), posLayout);
        addLabeledField("Y:", posYField = new EditText(getContext()), posLayout);
        addView(posLayout);
        
        // Scale
        LinearLayout scaleLayout = new LinearLayout(getContext());
        scaleLayout.setOrientation(HORIZONTAL);
        addLabeledField("Scale X:", scaleXField = new EditText(getContext()), scaleLayout);
        addLabeledField("Scale Y:", scaleYField = new EditText(getContext()), scaleLayout);
        addView(scaleLayout);
        
        // Rotation
        addLabeledField("Rotation:", rotField = new EditText(getContext()));
        
        // Z-Index
        addLabeledField("Z-Index:", zIndexField = new EditText(getContext()));
        
        // Visibility
        visibleCheck = new CheckBox(getContext());
        visibleCheck.setText("Visible");
        visibleCheck.setTextColor(Color.WHITE);
        visibleCheck.setOnCheckedChangeListener((b, checked) -> {
            if (selectedNode != null) {
                selectedNode.visible = checked;
                if (listener != null) listener.onPropertyChanged(selectedNode, "visible");
            }
        });
        addView(visibleCheck);
        
        // Enabled
        enabledCheck = new CheckBox(getContext());
        enabledCheck.setText("Enabled");
        enabledCheck.setTextColor(Color.WHITE);
        enabledCheck.setOnCheckedChangeListener((b, checked) -> {
            if (selectedNode != null) {
                selectedNode.enabled = checked;
                if (listener != null) listener.onPropertyChanged(selectedNode, "enabled");
            }
        });
        addView(enabledCheck);
        
        // Add Component button
        Button addCompBtn = new Button(getContext());
        addCompBtn.setText("+ Add Component");
        addCompBtn.setOnClickListener(v -> showAddComponentDialog());
        addView(addCompBtn);
        
        // Delete button
        Button deleteBtn = new Button(getContext());
        deleteBtn.setText("Delete Node");
        deleteBtn.setBackgroundColor(0xFFE74C3C);
        deleteBtn.setOnClickListener(v -> {
            if (selectedNode != null && selectedNode.getParent() != null) {
                selectedNode.queueFree();
                selectedNode = null;
                if (listener != null) listener.onPropertyChanged(null, "deleted");
            }
        });
        addView(deleteBtn);
    }
    
    private void addLabeledField(String label, EditText field) {
        addLabeledField(label, field, this);
    }
    
    private void addLabeledField(String label, EditText field, LinearLayout container) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        
        TextView lbl = new TextView(getContext());
        lbl.setText(label);
        lbl.setTextSize(12);
        lbl.setTextColor(Color.WHITE);
        lbl.setMinWidth(100);
        
        field.setTextColor(Color.WHITE);
        field.setBackgroundColor(0xFF2A2A4A);
        field.setPadding(16, 8, 16, 8);
        
        LayoutParams lblParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lblParams.setMargins(0, 8, 16, 8);
        lbl.setLayoutParams(lblParams);
        
        LayoutParams fieldParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        fieldParams.setMargins(0, 8, 0, 8);
        field.setLayoutParams(fieldParams);
        
        row.addView(lbl);
        row.addView(field);
        container.addView(row);
    }
    
    public void setSelectedNode(Node node) {
        this.selectedNode = node;
        updateFields();
    }
    
    public void updateFields() {
        if (selectedNode == null) {
            clearFields();
            return;
        }
        
        nameField.setText(selectedNode.name);
        posXField.setText(String.valueOf((int)selectedNode.position.x));
        posYField.setText(String.valueOf((int)selectedNode.position.y));
        scaleXField.setText(String.valueOf(selectedNode.scale.x));
        scaleYField.setText(String.valueOf(selectedNode.scale.y));
        rotField.setText(String.valueOf((int)selectedNode.rotation));
        zIndexField.setText(String.valueOf(selectedNode.zIndex));
        visibleCheck.setChecked(selectedNode.visible);
        enabledCheck.setChecked(selectedNode.enabled);
        
        // Setup listeners for position/scale changes
        setupNumericListener(posXField, v -> { selectedNode.position.x = v; return selectedNode.position; }, "position");
        setupNumericListener(posYField, v -> { selectedNode.position.y = v; return selectedNode.position; }, "position");
        setupNumericListener(scaleXField, v -> { selectedNode.scale.x = v; return selectedNode.scale; }, "scale");
        setupNumericListener(scaleYField, v -> { selectedNode.scale.y = v; return selectedNode.scale; }, "scale");
        setupNumericListener(rotField, v -> { selectedNode.rotation = v; return null; }, "rotation");
        setupNumericListener(zIndexField, v -> { selectedNode.zIndex = (int)v; return null; }, "zIndex");
    }
    
    private interface NumericSetter {
        Object set(float v);
    }
    
    private void setupNumericListener(EditText field, NumericSetter setter, String prop) {
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && selectedNode != null) {
                try {
                    float val = Float.parseFloat(field.getText().toString());
                    setter.set(val);
                    if (listener != null) listener.onPropertyChanged(selectedNode, prop);
                } catch (NumberFormatException e) {}
            }
        });
    }
    
    private void clearFields() {
        nameField.setText("");
        posXField.setText("");
        posYField.setText("");
        scaleXField.setText("");
        scaleYField.setText("");
        rotField.setText("");
        zIndexField.setText("");
        visibleCheck.setChecked(true);
        enabledCheck.setChecked(true);
    }
    
    private void showAddComponentDialog() {
        // Would show a dialog to add Sprite, RigidBody2D, CollisionShape2D, etc.
        if (selectedNode == null) return;
        
        // For now, add a sprite
        Sprite sprite = new Sprite();
        sprite.name = "Sprite";
        selectedNode.addChild(sprite);
        
        if (listener != null) listener.onPropertyChanged(selectedNode, "component_added");
    }
    
    public void setOnPropertyChangedListener(OnPropertyChangedListener l) {
        this.listener = l;
    }
}
