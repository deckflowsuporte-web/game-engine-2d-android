# ProGuard rules for GameEngine2D

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep application class
-keep class com.engine2d.game.** { *; }

# LuaJIT rules
-keep class luaj.** { *; }
-dontwarn luaj.**
