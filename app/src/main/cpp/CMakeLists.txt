cmake_minimum_required(VERSION 3.22.1)

project(gameengine VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Optimization flags
set(CMAKE_C_FLAGS_RELEASE "-O2 -ffast-math")
set(CMAKE_CXX_FLAGS_RELEASE "-O2 -ffast-math")

# Android specific
if(ANDROID)
    set(ANDROID_STL "c++_shared")
endif()

# Source files
set(ENGINE_SOURCES
    GameEngine.cpp
)

# Create shared library
add_library(gameengine SHARED ${ENGINE_SOURCES})

# Include directories
target_include_directories(gameengine PRIVATE 
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Link OpenGL ES 2.0 and EGL
target_link_libraries(gameengine
    android
    log
    GLESv2
    EGL
)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
