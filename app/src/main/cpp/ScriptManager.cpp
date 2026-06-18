#include "ScriptManager.h"
#include <android/log.h>
#include <string.h>

#define LOG_TAG "ScriptManager"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

ScriptManager::ScriptManager() : m_LuaState(nullptr), m_Initialized(false) {
}

ScriptManager::~ScriptManager() {
    shutdown();
}

bool ScriptManager::initialize() {
    if (m_Initialized) {
        LOGI("ScriptManager already initialized");
        return true;
    }

    LOGI("Initializing LuaJIT...");

    // Initialize LuaJIT state
    m_LuaState = luaL_newstate();
    if (!m_LuaState) {
        LOGE("Failed to create LuaJIT state");
        return false;
    }

    // Open standard Lua libraries
    luaL_openlibs(m_LuaState);

    m_Initialized = true;
    LOGI("LuaJIT initialized successfully");
    return true;
}

void ScriptManager::shutdown() {
    if (m_LuaState) {
        LOGI("Shutting down LuaJIT...");
        lua_close(m_LuaState);
        m_LuaState = nullptr;
    }
    m_Initialized = false;
}

bool ScriptManager::loadScript(const char* scriptPath) {
    if (!m_Initialized || !m_LuaState) {
        LOGE("ScriptManager not initialized");
        return false;
    }

    LOGI("Loading script: %s", scriptPath);

    int result = luaL_loadfile(m_LuaState, scriptPath);
    if (result != LUA_OK) {
        LOGE("Failed to load script: %s", lua_tostring(m_LuaState, -1));
        lua_pop(m_LuaState, 1);
        return false;
    }

    result = lua_pcall(m_LuaState, 0, LUA_MULTRET, 0);
    if (result != LUA_OK) {
        LOGE("Failed to execute script: %s", lua_tostring(m_LuaState, -1));
        lua_pop(m_LuaState, 1);
        return false;
    }

    LOGI("Script loaded successfully");
    return true;
}

bool ScriptManager::executeString(const char* code) {
    if (!m_Initialized || !m_LuaState) {
        LOGE("ScriptManager not initialized");
        return false;
    }

    int result = luaL_dostring(m_LuaState, code);
    if (result != LUA_OK) {
        LOGE("Failed to execute code: %s", lua_tostring(m_LuaState, -1));
        return false;
    }

    return true;
}

void ScriptManager::callFunction(const char* funcName, int argc, ...) {
    if (!m_Initialized || !m_LuaState) {
        LOGE("ScriptManager not initialized");
        return;
    }

    lua_getglobal(m_LuaState, funcName);
    if (!lua_isfunction(m_LuaState, -1)) {
        LOGE("Function %s not found", funcName);
        lua_pop(m_LuaState, 1);
        return;
    }

    va_list args;
    va_start(args, argc);
    for (int i = 0; i < argc; i++) {
        lua_pushstring(m_LuaState, va_arg(args, const char*));
    }
    va_end(args);

    int result = lua_pcall(m_LuaState, argc, 0, 0);
    if (result != LUA_OK) {
        LOGE("Error calling function %s: %s", funcName, lua_tostring(m_LuaState, -1));
        lua_pop(m_LuaState, 1);
    }
}

lua_State* ScriptManager::getLuaState() {
    return m_LuaState;
}
