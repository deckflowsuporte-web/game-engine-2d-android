#include "GameEngine.h"
#include "ScriptManager.h"
#include <android/log.h>
#include <android/looper.h>
#include <thread>
#include <chrono>
#include "native_app_glue/android_native_app_glue.h"

#define LOG_TAG "GameEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

GameEngine::GameEngine(android_app* app) 
    : m_App(app)
    , m_Running(false)
    , m_Initialized(false)
    , m_ScriptManager(nullptr)
    , m_LastTime(0) {
}

GameEngine::~GameEngine() {
    shutdown();
}

bool GameEngine::initialize() {
    if (m_Initialized) {
        LOGI("GameEngine already initialized");
        return true;
    }

    LOGI("Initializing GameEngine for weak devices...");

    // Initialize script manager (placeholder)
    m_ScriptManager = new ScriptManager();
    if (!m_ScriptManager->initialize()) {
        LOGE("Failed to initialize ScriptManager");
        delete m_ScriptManager;
        m_ScriptManager = nullptr;
        return false;
    }

    // Set the app state
    m_App->userData = this;
    m_App->onAppCmd = handleCommand;
    m_App->onInputEvent = handleInput;

    m_Initialized = true;
    LOGI("GameEngine initialized successfully");
    return true;
}

void GameEngine::shutdown() {
    if (!m_Initialized) {
        return;
    }

    LOGI("Shutting down GameEngine...");

    m_Running = false;

    if (m_ScriptManager) {
        m_ScriptManager->shutdown();
        delete m_ScriptManager;
        m_ScriptManager = nullptr;
    }

    m_Initialized = false;
    LOGI("GameEngine shutdown complete");
}

void GameEngine::run() {
    if (!m_Initialized) {
        LOGE("GameEngine not initialized");
        return;
    }

    LOGI("Starting game loop...");
    m_Running = true;
    m_LastTime = getTimeMs();

    int frameCount = 0;

    while (m_Running) {
        // Process all pending events
        int events;
        struct android_poll_source* source;

        // Blocking poll - waits for events
        int pollResult = ALooper_pollOnce(-1, nullptr, &events, (void**)&source);

        if (pollResult >= 0) {
            if (source != nullptr) {
                source->process(m_App, source);
            }

            if (m_App->destroyRequested) {
                LOGI("App destroy requested");
                break;
            }
        }

        if (m_Running) {
            // Calculate delta time
            uint64_t currentTime = getTimeMs();
            uint64_t deltaTime = currentTime - m_LastTime;
            
            // Cap delta time
            if (deltaTime > 100) {
                deltaTime = 100;
            }
            m_LastTime = currentTime;

            // Simple update
            update(deltaTime);

            // Simple render (draw to window if available)
            render();

            frameCount++;
            if (frameCount % 60 == 0) {
                LOGI("Frame %d - running", frameCount);
            }
        }
    }

    LOGI("Game loop ended");
}

void GameEngine::update(uint64_t deltaTime) {
    // Update game logic here
}

void GameEngine::render() {
    // Render game graphics here
    // Simple drawing handled by ANativeActivity
}

uint64_t GameEngine::getTimeMs() {
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (uint64_t)(now.tv_sec * 1000 + now.tv_nsec / 1000000);
}

void GameEngine::handleCommand(android_app* app, int32_t cmd) {
    GameEngine* engine = (GameEngine*)app->userData;
    if (engine) {
        engine->onCommand(cmd);
    }
}

void GameEngine::onCommand(int32_t cmd) {
    switch (cmd) {
        case APP_CMD_INIT_WINDOW:
            LOGI("Window initialized");
            break;
        case APP_CMD_TERM_WINDOW:
            LOGI("Window terminated");
            break;
        case APP_CMD_GAINED_FOCUS:
            LOGI("Gained focus");
            m_Running = true;
            break;
        case APP_CMD_LOST_FOCUS:
            LOGI("Lost focus");
            break;
        case APP_CMD_PAUSE:
            LOGI("App paused");
            break;
        case APP_CMD_RESUME:
            LOGI("App resumed");
            break;
        case APP_CMD_STOP:
            LOGI("App stopped");
            break;
        case APP_CMD_DESTROY:
            LOGI("App destroyed");
            shutdown();
            break;
    }
}

int32_t GameEngine::handleInput(android_app* app, AInputEvent* event) {
    GameEngine* engine = (GameEngine*)app->userData;
    if (engine) {
        return engine->onInput(event);
    }
    return 0;
}

int32_t GameEngine::onInput(AInputEvent* event) {
    return 0;
}

ScriptManager* GameEngine::getScriptManager() {
    return m_ScriptManager;
}

// Application entry point
void android_main(android_app* app) {
    LOGI("android_main called");

    // Create and initialize engine
    GameEngine engine(app);
    if (!engine.initialize()) {
        LOGE("Failed to initialize game engine");
        return;
    }

    // Run the game loop
    engine.run();

    LOGI("android_main exiting");
}
