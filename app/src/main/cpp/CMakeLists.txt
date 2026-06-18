cmake_minimum_required(VERSION 3.22.1)

project(gameengine VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Android specific settings
if(ANDROID)
    set(ANDROID_STL "c++_shared")
endif()

# Import the native_app_glue module
include(${ANDROID_NDK}/cmake/android.toolchain.cmake)
find_library(android_native_app_glue_lib android_native_app_glue)

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
)

# Link libraries
target_link_libraries(gameengine
    ${android_native_app_glue_lib}
    android
    log
)

# Export compile commands for IDE integration
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
