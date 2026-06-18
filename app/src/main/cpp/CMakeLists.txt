cmake_minimum_required(VERSION 3.22.1)

project(gameengine VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Android specific settings
if(ANDROID)
    set(ANDROID_STL "c++_shared")
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -llog")
endif()

# LuaJIT version
set(LUAJIT_VERSION "2.1.0-beta3")

# Fetch LuaJIT source from GitHub
include(FetchContent)
FetchContent_Declare(
    luajit
    GIT_REPOSITORY https://github.com/openresty/luajit2.git
    GIT_TAG v${LUAJIT_VERSION}
)

# Customize LuaJIT build for Android
set(LUAJIT_TARGET "android" CACHE STRING "" FORCE)
set(LUAJIT_ARCH "" CACHE STRING "" FORCE)
set(LUAJIT_ABIS "" CACHE STRING "" FORCE)

FetchContent_MakeAvailable(luajit)

# Source files for the engine core
set(ENGINE_SOURCES
    ScriptManager.cpp
    GameEngine.cpp
)

# Create the shared library
add_library(gameengine SHARED ${ENGINE_SOURCES})

# Include directories
target_include_directories(gameengine PRIVATE 
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${LUAJIT_SOURCE_DIR}/src
)

# Link libraries
target_link_libraries(gameengine
    luajit
    android
    log
)

# Export compile commands for IDE integration
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
