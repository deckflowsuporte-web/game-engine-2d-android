#ifndef SCRIPT_MANAGER_H
#define SCRIPT_MANAGER_H

#include <stdarg.h>
#include <string>

class ScriptManager {
public:
    ScriptManager();
    ~ScriptManager();

    bool initialize();
    void shutdown();

    bool loadScript(const char* scriptPath);
    bool executeString(const char* code);
    void callFunction(const char* funcName, int argc, ...);

    bool isInitialized() const { return m_Initialized; }

private:
    bool m_Initialized;
    std::string m_LastError;
};

#endif // SCRIPT_MANAGER_H
