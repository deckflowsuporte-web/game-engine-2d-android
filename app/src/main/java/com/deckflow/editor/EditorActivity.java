package com.deckflow.editor;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.content.pm.ActivityInfo;
import com.deckflow.engine.*;

/**
 * EditorActivity - Main editor screen
 * Combines all editor components into one interface
 */
public class EditorActivity extends Activity {
    
    // Editor components
    private Toolbar toolbar;
    private EditorView editorView;
    private SceneTree sceneTree;
    private InspectorPanel inspector;
    
    // Scene
    private Scene currentScene;
    
    // Layouts
    private LinearLayout mainLayout;
    private LinearLayout leftPanel;
    private LinearLayout rightPanel;
    private LinearLayout centerPanel;
    
    // Preview mode
    private boolean isPlaying = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen landscape
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // Create demo scene
        createDemoScene();
        
        // Build UI
        buildEditorUI();
        
        setContentView(mainLayout);
        
        android.util.Log.i("DeckFlow", "=== DECKFLOW EDITOR ===");
        android.util.Log.i("DeckFlow", "Professional 2D Game Engine for Android");
        android.util.Log.i("DeckFlow", "Editor ready!");
    }
    
    private void createDemoScene() {
        currentScene = new Scene();
        currentScene.sceneName = "DemoScene";
        
        // Add some demo nodes
        Node background = new Node();
        background.name = "Background";
        background.position = new Vector2(400, 300);
        currentScene.addChild(background);
        
        Node player = new Node();
        player.name = "Player";
        player.position = new Vector2(200, 300);
        Sprite playerSprite = new Sprite();
        playerSprite.name = "PlayerSprite";
        player.addChild(playerSprite);
        currentScene.addChild(player);
        
        Node enemy1 = new Node();
        enemy1.name = "Enemy_1";
        enemy1.position = new Vector2(500, 300);
        Sprite enemySprite1 = new Sprite();
        enemySprite1.name = "EnemySprite";
        enemy1.addChild(enemySprite1);
        currentScene.addChild(enemy1);
        
        Node enemy2 = new Node();
        enemy2.name = "Enemy_2";
        enemy2.position = new Vector2(600, 200);
        currentScene.addChild(enemy2);
        
        Node ground = new Node();
        ground.name = "Ground";
        ground.position = new Vector2(400, 500);
        Sprite groundSprite = new Sprite();
        groundSprite.name = "GroundSprite";
        ground.addChild(groundSprite);
        currentScene.addChild(ground);
        
        Node coins = new Node();
        coins.name = "Coins";
        for (int i = 0; i < 5; i++) {
            Node coin = new Node();
            coin.name = "Coin_" + (i + 1);
            coin.position = new Vector2(100 + i * 100, 400);
            coins.addChild(coin);
        }
        currentScene.addChild(coins);
    }
    
    private void buildEditorUI() {
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Top toolbar
        toolbar = new Toolbar(this);
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 100));
        toolbar.setEditorView(editorView = new EditorView(this));
        toolbar.setOnToolSelectedListener(new Toolbar.OnToolSelectedListener() {
            @Override
            public void onToolSelected(EditorView.ToolMode tool) {
                editorView.setToolMode(tool);
            }
            
            @Override
            public void onAction(String action) {
                handleAction(action);
            }
        });
        mainLayout.addView(toolbar);
        
        // Main content area
        LinearLayout contentArea = new LinearLayout(this);
        contentArea.setOrientation(HORIZONTAL);
        contentArea.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        
        // Left panel - Scene Tree
        leftPanel = new LinearLayout(this);
        leftPanel.setOrientation(LinearLayout.VERTICAL);
        leftPanel.setBackgroundColor(0xFF1E1E2E);
        leftPanel.setLayoutParams(new LinearLayout.LayoutParams(300, 
            LinearLayout.LayoutParams.MATCH_PARENT));
        
        sceneTree = new SceneTree(this);
        sceneTree.setScene(currentScene);
        sceneTree.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        sceneTree.setOnNodeSelectedListener(node -> {
            editorView.setSelectedNode(node);
            inspector.setSelectedNode(node);
        });
        sceneTree.setOnNodeChangedListener(() -> {
            editorView.invalidate();
        });
        leftPanel.addView(sceneTree);
        
        // Right panel - Inspector
        rightPanel = new LinearLayout(this);
        rightPanel.setOrientation(LinearLayout.VERTICAL);
        rightPanel.setBackgroundColor(0xFF1E1E2E);
        rightPanel.setLayoutParams(new LinearLayout.LayoutParams(350, 
            LinearLayout.LayoutParams.MATCH_PARENT));
        
        inspector = new InspectorPanel(this);
        inspector.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        inspector.setOnPropertyChangedListener((node, prop) -> {
            editorView.invalidate();
            sceneTree.refresh();
        });
        rightPanel.addView(inspector);
        
        // Center - Editor View
        centerPanel = new LinearLayout(this);
        centerPanel.setOrientation(LinearLayout.VERTICAL);
        centerPanel.setLayoutParams(new LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        
        // Editor View fills center
        editorView.setScene(currentScene);
        editorView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.MATCH_PARENT));
        
        // Zoom controls overlay
        FrameLayout editorContainer = new FrameLayout(this);
        editorContainer.addView(editorView);
        
        // Bottom toolbar for editor controls
        LinearLayout zoomControls = new LinearLayout(this);
        zoomControls.setOrientation(HORIZONTAL);
        zoomControls.setBackgroundColor(0x88000000);
        zoomControls.setPadding(16, 8, 16, 8);
        zoomControls.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        
        Button zoomOut = new Button(this);
        zoomOut.setText("-");
        zoomOut.setOnClickListener(v -> editorView.zoomOut());
        
        Button zoomIn = new Button(this);
        zoomIn.setText("+");
        zoomIn.setOnClickListener(v -> editorView.zoomIn());
        
        Button resetView = new Button(this);
        resetView.setText("Reset");
        resetView.setOnClickListener(v -> editorView.resetView());
        
        Button centerBtn = new Button(this);
        centerBtn.setText("Center");
        centerBtn.setOnClickListener(v -> editorView.centerOnSelected());
        
        zoomControls.addView(zoomOut);
        zoomControls.addView(zoomIn);
        zoomControls.addView(resetView);
        zoomControls.addView(centerBtn);
        
        FrameLayout.LayoutParams zoomParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        zoomParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        zoomParams.setMargins(0, 0, 16, 16);
        editorContainer.addView(zoomControls, zoomParams);
        
        centerPanel.addView(editorContainer);
        
        // Add panels to content
        contentArea.addView(leftPanel);
        contentArea.addView(centerPanel);
        contentArea.addView(rightPanel);
        
        mainLayout.addView(contentArea);
    }
    
    private void handleAction(String action) {
        switch (action) {
            case "Play":
                playGame();
                break;
            case "Pause":
                pauseGame();
                break;
            case "Screenshot":
                takeScreenshot();
                break;
        }
    }
    
    private void playGame() {
        if (isPlaying) {
            // Stop playing
            isPlaying = false;
            android.util.Log.i("DeckFlow", "Editor mode");
            // Restore editor UI
        } else {
            // Start playing
            isPlaying = true;
            android.util.Log.i("DeckFlow", "Playing: " + currentScene.sceneName);
            
            // In a real implementation, this would:
            // 1. Save scene state
            // 2. Switch to game runtime view
            // 3. Run the game loop
            
            showToast("Play mode - Game would run here!");
        }
    }
    
    private void pauseGame() {
        android.util.Log.i("DeckFlow", "Pause toggled");
    }
    
    private void takeScreenshot() {
        android.util.Log.i("DeckFlow", "Screenshot saved");
        showToast("Screenshot saved!");
    }
    
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.i("DeckFlow", "Editor closed");
    }
}
