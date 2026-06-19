cmake_minimum_required(VERSION 3.22.1)

project(gameengine VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Optimization flags for weak devices
set(CMAKE_C_FLAGS_RELEASE "-O2 -ffast-math")
set(CMAKE_CXX_FLAGS_RELEASE "-O2 -ffast-math")

# Android specific settings
if(ANDROID)
    set(ANDROID_STL "c++_shared")
endif()

# Source files for the engine core
set(ENGINE_SOURCES
    native_app_glue/android_native_app_glue.c
    ScriptManager.cpp
    GameEngine.cpp
)

# Create the shared library
add_library(gameengine SHARED ${ENGINE_SOURCES})

# Include directories
target_include_directories(gameengine PRIVATE 
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/native_app_glue
)

# Link libraries - minimal set for weak devices
target_link_libraries(gameengine
    android
    log
)

# Export compile commands
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
