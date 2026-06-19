#ifndef GAME_ENGINE_H
#define GAME_ENGINE_H

#include <android/input.h>
#include <android/log.h>
#include <android/native_window.h>
#include <time.h>
#include <stdint.h>

// Forward declaration instead of including android_native_app_glue.h
struct android_app;

class ScriptManager;

class GameEngine {
public:
    GameEngine(android_app* app);
    ~GameEngine();

    bool initialize();
    void shutdown();
    void run();

    void update(uint64_t deltaTime);
    void render();

    // EGL initialization for OpenGL ES 2.0 (weak device support)
    bool initEGL();
    void destroyEGL();

    ScriptManager* getScriptManager();

    static void handleCommand(android_app* app, int32_t cmd);
    static int32_t handleInput(android_app* app, AInputEvent* event);

protected:
    void onCommand(int32_t cmd);
    int32_t onInput(AInputEvent* event);

private:
    static uint64_t getTimeMs();

    android_app* m_App;
    bool m_Running;
    bool m_Initialized;
    ScriptManager* m_ScriptManager;
    uint64_t m_LastTime;
};

#endif // GAME_ENGINE_H
