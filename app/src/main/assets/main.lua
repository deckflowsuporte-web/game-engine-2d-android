-- GameEngine2D Test Script
-- This script runs when the game starts

print("========================================")
print("  GameEngine2D - Lua Script Loaded!")
print("========================================")
print("Script: main.lua")
print("Status: Running")
print("========================================")

-- Game state
local gameState = {
    score = 0,
    playerX = 100,
    playerY = 100,
    isRunning = true
}

-- Print game state
print("\nInitial Game State:")
print("  Score: " .. gameState.score)
print("  Player Position: (" .. gameState.playerX .. ", " .. gameState.playerY .. ")")
print("  Running: " .. tostring(gameState.isRunning))

-- Function to update game logic
function updateGame(deltaTime)
    -- Update player position based on deltaTime
    gameState.playerX = gameState.playerX + (deltaTime * 0.1)
    gameState.playerY = gameState.playerY + (deltaTime * 0.05)
    
    -- Increment score
    gameState.score = gameState.score + 1
    
    -- Print update every 100 frames
    if gameState.score % 100 == 0 then
        print("\n[UPDATE] Frame " .. gameState.score)
        print("  Player Position: (" .. string.format("%.2f", gameState.playerX) .. ", " .. string.format("%.2f", gameState.playerY) .. ")")
    end
end

-- Function to render graphics
function renderGame()
    -- Placeholder for rendering logic
    -- In a real engine, this would draw sprites, textures, etc.
end

-- Function called on input events
function onInput(eventType, x, y)
    print("\n[INPUT] Type: " .. eventType .. " at (" .. x .. ", " .. y .. ")")
    gameState.playerX = x
    gameState.playerY = y
end

-- Main game loop callback
function gameLoop(deltaTime)
    updateGame(deltaTime)
    renderGame()
end

print("\nCallbacks registered:")
print("  - updateGame(deltaTime)")
print("  - renderGame()")
print("  - onInput(eventType, x, y)")
print("  - gameLoop(deltaTime)")
print("\nGame loop is running...")
print("========================================")

-- Return success
return true
