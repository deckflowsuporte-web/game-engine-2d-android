#ifndef SCRIPT_MANAGER_H
#define SCRIPT_MANAGER_H

extern "C" {
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
}

#include <stdarg.h>

class ScriptManager {
public:
    ScriptManager();
    ~ScriptManager();

    bool initialize();
    void shutdown();

    bool loadScript(const char* scriptPath);
    bool executeString(const char* code);
    void callFunction(const char* funcName, int argc, ...);

    lua_State* getLuaState();

private:
    lua_State* m_LuaState;
    bool m_Initialized;
};

#endif // SCRIPT_MANAGER_H
