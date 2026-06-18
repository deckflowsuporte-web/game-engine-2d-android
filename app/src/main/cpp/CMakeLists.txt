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

# Source files for the engine core
set(ENGINE_SOURCES
    ScriptManager.cpp
    GameEngine.cpp
)

# Create the shared library
add_library(gameengine SHARED ${ENGINE_SOURCES})

# Include directories - need both app_glue and NDK paths
target_include_directories(gameengine PRIVATE 
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${ANDROID_NDK}/sources/android/native_app_glue
    ${ANDROID_NDK}/sysroot/usr/include
    ${ANDROID_NDK}/sysroot/usr/include/android
)

# Link libraries - native_app_glue is a static library
target_link_libraries(gameengine
    -Wl,--whole-archive
    ${ANDROID_NDK}/sources/android/native_app_glue/${ANDROID_ABI}/libandroid_native_app_glue.a
    -Wl,--no-whole-archive
    android
    log
)

# Export compile commands for IDE integration
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
