#include <jni.h>
#include <android/log.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>

#define LOG_TAG "GameEngine2D"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Screen dimensions
static int sScreenWidth = 480;
static int sScreenHeight = 320;

// Frame counter
static int sFrameCount = 0;
static long sLastTime = 0;
static float sFPS = 0;

// Simple vertex shader for 2D rendering
static const char* VERTEX_SHADER = 
    "attribute vec4 aPosition;\n"
    "attribute vec4 aColor;\n"
    "varying vec4 vColor;\n"
    "void main() {\n"
    "   gl_Position = aPosition;\n"
    "   vColor = aColor;\n"
    "}\n";

// Simple fragment shader
static const char* FRAGMENT_SHADER = 
    "precision mediump float;\n"
    "varying vec4 vColor;\n"
    "void main() {\n"
    "   gl_FragColor = vColor;\n"
    "}\n";

static GLuint sProgram = 0;
static GLuint sPositionHandle = 0;
static GLuint sColorHandle = 0;

// Compile shader
static GLuint compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    return shader;
}

// Initialize OpenGL
static bool initGL() {
    // Create shaders
    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, VERTEX_SHADER);
    GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
    
    // Create program
    sProgram = glCreateProgram();
    glAttachShader(sProgram, vertexShader);
    glAttachShader(sProgram, fragmentShader);
    glLinkProgram(sProgram);
    
    // Get handles
    sPositionHandle = glGetAttribLocation(sProgram, "aPosition");
    sColorHandle = glGetAttribLocation(sProgram, "aColor");
    
    // Enable vertex attributes
    glEnableVertexAttribArray(sPositionHandle);
    glEnableVertexAttribArray(sColorHandle);
    
    LOGI("OpenGL ES 2.0 initialized");
    return true;
}

// Draw a colored rectangle
static void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
    float vertices[] = {
        // x, y, r, g, b, a
        x, y, r, g, b, a,
        x + w, y, r, g, b, a,
        x, y + h, r, g, b, a,
        x + w, y, r, g, b, a,
        x + w, y + h, r, g, b, a,
        x, y + h, r, g, b, a,
    };
    
    glVertexAttribPointer(sPositionHandle, 2, GL_FLOAT, GL_FALSE, 24, vertices);
    glVertexAttribPointer(sColorHandle, 4, GL_FLOAT, GL_FALSE, 24, vertices + 2);
    glDrawArrays(GL_TRIANGLES, 0, 6);
}

// JNI Functions
extern "C" {

JNIEXPORT void JNICALL
Java_com_engine2d_game_GameRenderer_nativeInit(JNIEnv* env, jobject obj) {
    LOGI("GameEngine 2D initializing...");
    initGL();
    sLastTime = 0;
    sFrameCount = 0;
    LOGI("GameEngine 2D ready!");
}

JNIEXPORT void JNICALL
Java_com_engine2d_game_GameRenderer_nativeResize(JNIEnv* env, jobject obj, jint width, jint height) {
    sScreenWidth = width;
    sScreenHeight = height;
    glViewport(0, 0, width, height);
    LOGI("Screen resized: %dx%d", width, height);
}

JNIEXPORT void JNICALL
Java_com_engine2d_game_GameRenderer_nativeRender(JNIEnv* env, jobject obj) {
    // Calculate FPS
    sFrameCount++;
    long currentTime = sLastTime + 1; // Simplified
    
    // Clear screen with dark blue
    glClearColor(0.1f, 0.1f, 0.2f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    
    // Use shader program
    glUseProgram(sProgram);
    
    // Draw background gradient (simple rectangles)
    drawRect(-1.0f, -1.0f, 2.0f, 2.0f, 0.1f, 0.1f, 0.2f, 1.0f);
    
    // Draw some demo shapes
    // Player (white square)
    drawRect(-0.1f, -0.1f, 0.2f, 0.2f, 1.0f, 1.0f, 1.0f, 1.0f);
    
    // Ground (green)
    drawRect(-1.0f, -0.8f, 2.0f, 0.1f, 0.2f, 0.8f, 0.2f, 1.0f);
    
    // Enemy 1 (red)
    drawRect(0.5f, -0.6f, 0.15f, 0.15f, 1.0f, 0.2f, 0.2f, 1.0f);
    
    // Enemy 2 (red)
    drawRect(0.7f, -0.7f, 0.15f, 0.15f, 1.0f, 0.3f, 0.2f, 1.0f);
    
    // Coin (yellow)
    drawRect(-0.5f, -0.5f, 0.08f, 0.08f, 1.0f, 0.9f, 0.2f, 1.0f);
    drawRect(-0.3f, -0.6f, 0.08f, 0.08f, 1.0f, 0.9f, 0.2f, 1.0f);
    
    // Debug: show frame count every 60 frames
    if (sFrameCount % 60 == 0) {
        LOGI("Frame %d", sFrameCount);
    }
}

JNIEXPORT void JNICALL
Java_com_engine2d_game_GameRenderer_nativeShutdown(JNIEnv* env, jobject obj) {
    LOGI("GameEngine 2D shutting down...");
    if (sProgram) {
        glDeleteProgram(sProgram);
        sProgram = 0;
    }
}

}
