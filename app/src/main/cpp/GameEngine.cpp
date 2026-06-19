#include "GameEngine.h"
#include "ScriptManager.h"
#include <android/log.h>
#include <android/looper.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <thread>
#include <chrono>
#include "native_app_glue/android_native_app_glue.h"

#define LOG_TAG "GameEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static EGLDisplay g_Display = EGL_NO_DISPLAY;
static EGLSurface g_Surface = EGL_NO_SURFACE;
static EGLContext g_Context = EGL_NO_CONTEXT;

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

    // Initialize script manager
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

bool GameEngine::initEGL() {
    // Initialize EGL - for weak device support
    EGLint format, numConfigs, majorVersion, minorVersion;
    EGLConfig config;

    // Choose OpenGL ES 2.0 configuration (compatible with weak devices)
    EGLint attribList[] = {
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        EGL_BLUE_SIZE, 5,
        EGL_GREEN_SIZE, 6,
        EGL_RED_SIZE, 5,
        EGL_ALPHA_SIZE, 0,
        EGL_DEPTH_SIZE, 16,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_NONE
    };

    g_Display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (g_Display == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay failed");
        return false;
    }

    if (!eglInitialize(g_Display, &majorVersion, &minorVersion)) {
        LOGE("eglInitialize failed");
        return false;
    }

    if (!eglChooseConfig(g_Display, attribList, &config, 1, &numConfigs)) {
        LOGE("eglChooseConfig failed");
        return false;
    }

    if (!eglGetConfigAttrib(g_Display, config, EGL_NATIVE_VISUAL_ID, &format)) {
        LOGE("eglGetConfigAttrib failed");
        return false;
    }

    ANativeWindow_setBuffersGeometry(m_App->window, 0, 0, format);

    g_Surface = eglCreateWindowSurface(g_Display, config, m_App->window, nullptr);
    if (g_Surface == EGL_NO_SURFACE) {
        LOGE("eglCreateWindowSurface failed");
        return false;
    }

    // Create OpenGL ES 2.0 context
    EGLint contextAttribs[] = {
        EGL_CONTEXT_CLIENT_VERSION, 2,
        EGL_NONE
    };
    g_Context = eglCreateContext(g_Display, config, nullptr, contextAttribs);
    if (g_Context == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed");
        return false;
    }

    if (!eglMakeCurrent(g_Display, g_Surface, g_Surface, g_Context)) {
        LOGE("eglMakeCurrent failed");
        return false;
    }

    LOGI("EGL initialized: OpenGL ES %d.%d", majorVersion, minorVersion);
    return true;
}

void GameEngine::destroyEGL() {
    if (g_Display != EGL_NO_DISPLAY) {
        eglMakeCurrent(g_Display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (g_Surface != EGL_NO_SURFACE) {
            eglDestroySurface(g_Display, g_Surface);
            g_Surface = EGL_NO_SURFACE;
        }
        if (g_Context != EGL_NO_CONTEXT) {
            eglDestroyContext(g_Display, g_Context);
            g_Context = EGL_NO_CONTEXT;
        }
        eglTerminate(g_Display);
        g_Display = EGL_NO_DISPLAY;
    }
}

void GameEngine::shutdown() {
    if (!m_Initialized) {
        return;
    }

    LOGI("Shutting down GameEngine...");

    m_Running = false;
    destroyEGL();

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

    LOGI("Starting optimized game loop for weak devices...");
    m_Running = true;
    m_LastTime = getTimeMs();

    // Target 30 FPS for weak devices (adjustable)
    const uint64_t targetFrameTime = 33; // ~30 FPS
    uint64_t sleepTime;

    while (m_Running) {
        // Process all pending events
        int events;
        struct android_poll_source* source;

        // Use blocking poll for weak devices (saves battery)
        int pollResult = ALooper_pollOnce(0, nullptr, &events, (void**)&source);

        if (pollResult >= 0) {
            if (source != nullptr) {
                source->process(m_App, source);
            }

            if (m_App->destroyRequested) {
                LOGI("App destroy requested");
                shutdown();
                return;
            }
        }

        if (m_Initialized && m_Running && m_App->window != nullptr) {
            // Calculate delta time
            uint64_t currentTime = getTimeMs();
            uint64_t deltaTime = currentTime - m_LastTime;
            
            // Cap delta time to prevent spiral of death on slow devices
            if (deltaTime > 100) {
                deltaTime = 100;
            }
            m_LastTime = currentTime;

            // Initialize EGL when window is ready
            static bool eglInitialized = false;
            if (!eglInitialized && m_App->window != nullptr) {
                if (initEGL()) {
                    eglInitialized = true;
                }
            }

            // Update game state
            update(deltaTime);

            // Render frame
            render();

            // Swap buffers
            if (g_Display != EGL_NO_DISPLAY) {
                eglSwapBuffers(g_Display, g_Surface);
            }

            // Frame rate limiting for weak devices
            currentTime = getTimeMs();
            sleepTime = targetFrameTime - (currentTime - m_LastTime);
            if (sleepTime > 0 && sleepTime <= targetFrameTime) {
                // Use C++ thread sleep for cross-platform compatibility
                std::this_thread::sleep_for(std::chrono::milliseconds(sleepTime));
            }
        }
    }

    LOGI("Game loop ended");
}

void GameEngine::update(uint64_t deltaTime) {
    // Update game logic here
    // This is called every frame with deltaTime in milliseconds
}

void GameEngine::render() {
    // Clear screen (basic OpenGL ES 2.0)
    glClearColor(0.1f, 0.1f, 0.2f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
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
            LOGI("Window initialized - ready for rendering");
            break;
        case APP_CMD_TERM_WINDOW:
            LOGI("Window terminated");
            destroyEGL();
            break;
        case APP_CMD_GAINED_FOCUS:
            LOGI("Gained focus");
            m_Running = true;
            break;
        case APP_CMD_LOST_FOCUS:
            LOGI("Lost focus");
            m_Running = false;
            break;
        case APP_CMD_PAUSE:
            LOGI("App paused");
            m_Running = false;
            break;
        case APP_CMD_RESUME:
            LOGI("App resumed");
            m_Running = true;
            break;
        case APP_CMD_STOP:
            LOGI("App stopped");
            break;
        case APP_CMD_DESTROY:
            LOGI("App destroyed");
            shutdown();
            break;
        default:
            LOGI("Unknown command: %d", cmd);
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
    // Handle input events
    return 0;
}

ScriptManager* GameEngine::getScriptManager() {
    return m_ScriptManager;
}

// Application entry point
void android_main(android_app* app) {
    LOGI("android_main called - GameEngine 2D for weak devices");

    // Initialize the game engine
    GameEngine engine(app);
    if (!engine.initialize()) {
        LOGE("Failed to initialize game engine");
        return;
    }

    // Load main script if available
    ScriptManager* scriptManager = engine.getScriptManager();
    if (scriptManager) {
        scriptManager->loadScript("assets/main.lua");
    }

    // Run the game loop
    engine.run();

    LOGI("android_main exiting");
}
